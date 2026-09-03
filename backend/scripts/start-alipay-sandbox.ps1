[CmdletBinding()]
param(
    [switch]$TunnelOnly
)

$backendDirectory = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$projectDirectory = [IO.Path]::GetFullPath((Join-Path $backendDirectory '..'))
$localConfigPath = [IO.Path]::GetFullPath((Join-Path $backendDirectory 'alipay.local'))
$relayScriptPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'alipay-notify-relay.mjs'))
$logDirectory = [IO.Path]::GetFullPath((Join-Path $projectDirectory 'tmp\alipay-callback'))

if (-not $localConfigPath.StartsWith($projectDirectory, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Alipay local config path is outside the project'
}
if (-not (Test-Path -LiteralPath $localConfigPath)) {
    throw 'backend/alipay.local is missing; configure sandbox credentials first'
}
if (-not (Get-Command node.exe -ErrorAction SilentlyContinue)) {
    throw 'Node.js is required for the restricted callback relay'
}
if (-not (Get-Command ssh.exe -ErrorAction SilentlyContinue)) {
    throw 'Windows OpenSSH Client is required for the callback tunnel'
}

New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
$relayStdout = Join-Path $logDirectory 'relay.stdout.log'
$relayStderr = Join-Path $logDirectory 'relay.stderr.log'
$tunnelStdout = Join-Path $logDirectory 'tunnel.stdout.log'
$tunnelStderr = Join-Path $logDirectory 'tunnel.stderr.log'

$relayProcess = $null
$tunnelProcess = $null
$keepaliveJob = $null

try {
    $relayArgument = '"' + $relayScriptPath + '"'
    $relayProcess = Start-Process -FilePath 'node.exe' -ArgumentList $relayArgument `
        -WorkingDirectory $projectDirectory -WindowStyle Hidden `
        -RedirectStandardOutput $relayStdout -RedirectStandardError $relayStderr -PassThru

    $tunnelProcess = Start-Process -FilePath 'ssh.exe' -ArgumentList @(
        '-T', '-o', 'StrictHostKeyChecking=accept-new', '-o', 'ExitOnForwardFailure=yes',
        '-o', 'ServerAliveInterval=30', '-R', '80:127.0.0.1:18080', 'nokey@localhost.run'
    ) -WorkingDirectory $projectDirectory -WindowStyle Hidden `
        -RedirectStandardOutput $tunnelStdout -RedirectStandardError $tunnelStderr -PassThru

    $tunnelUrl = $null
    for ($attempt = 0; $attempt -lt 30 -and -not $tunnelUrl; $attempt++) {
        Start-Sleep -Seconds 1
        if ($tunnelProcess.HasExited) {
            throw "Tunnel stopped during startup: $(Get-Content -Raw $tunnelStderr -ErrorAction SilentlyContinue)"
        }
        $output = Get-Content -Raw $tunnelStdout -ErrorAction SilentlyContinue
        if ($output) {
            $match = [regex]::Match($output, 'https://[a-z0-9.-]+\.life')
            if ($match.Success) {
                $tunnelUrl = $match.Value
            }
        }
    }
    if (-not $tunnelUrl) {
        throw 'Timed out waiting for the public callback URL'
    }

    $notifyUrl = "$tunnelUrl/api/payments/alipay/notify"
    $config = [IO.File]::ReadAllText($localConfigPath)
    if ($config -notmatch '(?m)^alipay\.notify-url=') {
        throw 'alipay.notify-url is missing from backend/alipay.local'
    }
    $config = [regex]::Replace($config, '(?m)^alipay\.notify-url=.*$', "alipay.notify-url=$notifyUrl")
    [IO.File]::WriteAllText($localConfigPath, $config, [Text.UTF8Encoding]::new($false))

    $keepaliveJob = Start-Job -ScriptBlock {
        param($Url)
        while ($true) {
            try {
                Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 15 | Out-Null
            } catch {
                # A 404 is expected; the request itself keeps the anonymous tunnel active.
            }
            Start-Sleep -Seconds 60
        }
    } -ArgumentList "$tunnelUrl/"

    Write-Host "Alipay notify URL: $notifyUrl"
    if ($TunnelOnly) {
        Write-Host 'Restart the backend now so it reloads backend/alipay.local. Press Ctrl+C to stop the tunnel.'
        Wait-Process -Id $tunnelProcess.Id
    } else {
        Push-Location $backendDirectory
        try {
            & .\mvnw.cmd spring-boot:run
        } finally {
            Pop-Location
        }
    }
} finally {
    if ($keepaliveJob) {
        Stop-Job $keepaliveJob -ErrorAction SilentlyContinue
        Remove-Job $keepaliveJob -Force -ErrorAction SilentlyContinue
    }
    foreach ($process in @($tunnelProcess, $relayProcess)) {
        if ($process -and -not $process.HasExited) {
            Stop-Process -Id $process.Id -ErrorAction SilentlyContinue
        }
    }
}
