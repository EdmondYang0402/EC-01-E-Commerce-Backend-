import http from 'node:http'

const listenHost = '127.0.0.1'
const listenPort = 18080
const allowedPath = '/api/payments/alipay/notify'
const backendUrl = new URL('http://127.0.0.1:8080' + allowedPath)
const maxBodyBytes = 64 * 1024

const server = http.createServer((request, response) => {
  const requestUrl = new URL(request.url ?? '/', `http://${request.headers.host ?? listenHost}`)
  if (request.method !== 'POST' || requestUrl.pathname !== allowedPath) {
    response.writeHead(404, { 'content-type': 'text/plain; charset=utf-8' })
    response.end('not found')
    return
  }

  let receivedBytes = 0
  const chunks = []
  request.on('data', (chunk) => {
    receivedBytes += chunk.length
    if (receivedBytes > maxBodyBytes) {
      response.writeHead(413, { 'content-type': 'text/plain; charset=utf-8' })
      response.end('payload too large')
      request.destroy()
      return
    }
    chunks.push(chunk)
  })

  request.on('end', () => {
    if (receivedBytes > maxBodyBytes) return

    const body = Buffer.concat(chunks)
    console.log(`${new Date().toISOString()} forwarding Alipay notify (${body.length} bytes)`)
    const proxyRequest = http.request(backendUrl, {
      method: 'POST',
      headers: {
        'content-type': request.headers['content-type'] ?? 'application/x-www-form-urlencoded',
        'content-length': body.length,
      },
    }, (proxyResponse) => {
      console.log(`${new Date().toISOString()} backend responded ${proxyResponse.statusCode ?? 502}`)
      response.writeHead(proxyResponse.statusCode ?? 502, {
        'content-type': proxyResponse.headers['content-type'] ?? 'text/plain; charset=utf-8',
      })
      proxyResponse.pipe(response)
    })

    proxyRequest.on('error', () => {
      response.writeHead(502, { 'content-type': 'text/plain; charset=utf-8' })
      response.end('backend unavailable')
    })
    proxyRequest.end(body)
  })
})

server.listen(listenPort, listenHost, () => {
  console.log(`Alipay notify relay listening on http://${listenHost}:${listenPort}${allowedPath}`)
})
