export async function* fetchSSE(url: string, body: object): AsyncGenerator<SSEEvent> {
  console.log('[SSE] 开始请求:', url)
  console.log('[SSE] 请求体:', body)

  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })

  console.log('[SSE] 响应状态:', response.status, response.statusText)
  console.log('[SSE] 响应头 Content-Type:', response.headers.get('content-type'))
  console.log('[SSE] 是否有响应体:', !!response.body)

  if (!response.ok || !response.body) {
    throw new Error(`SSE request failed: ${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let totalChunks = 0
  let totalData = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      console.log(`[SSE] 流结束，共接收 ${totalChunks} 个数据块`)
      break
    }

    totalChunks++
    const chunk = decoder.decode(value, { stream: true })
    console.log(`[SSE] 数据块 #${totalChunks}:`, chunk)

    buffer += chunk
    totalData += chunk

    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed) continue
      console.log('[SSE] 解析行:', trimmed)

      if (!trimmed.startsWith('data:')) {
        console.warn('[SSE] 非数据行，跳过:', trimmed)
        continue
      }

      const data = trimmed.slice(5).trim()
      console.log('[SSE] 数据内容:', data)

      if (data === '[DONE]') {
        console.log('[SSE] 收到 [DONE] 标记')
        return
      }

      try {
        const parsed = JSON.parse(data) as SSEEvent
        console.log('[SSE] 解析成功:', parsed.type, parsed)
        yield parsed
      } catch (e) {
        console.error('[SSE] JSON解析失败:', data, e)
      }
    }
  }
}

interface SSEEvent {
  type: string
  content?: string
  citation?: any
  answer?: string
  citations?: any[]
  error?: string
}
