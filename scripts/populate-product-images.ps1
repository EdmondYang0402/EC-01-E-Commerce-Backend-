param(
    [long]$ProductId = 0,
    [long[]]$ProductIds = @(),
    [int]$Limit = 0,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
$workspace = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $workspace 'backend'

$env:PRODUCT_IMAGE_PRODUCT_ID = [string]$ProductId
$env:PRODUCT_IMAGE_PRODUCT_IDS = $ProductIds -join ','
$env:PRODUCT_IMAGE_LIMIT = [string]$Limit
$env:PRODUCT_IMAGE_FORCE = $Force.IsPresent.ToString().ToLowerInvariant()

Push-Location $backend
try {
    .\mvnw.cmd -DskipTests compile exec:java "-Dexec.mainClass=com.ec01.productimagepopulate.ProductImagePopulateApplication"
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
} finally {
    Pop-Location
}
