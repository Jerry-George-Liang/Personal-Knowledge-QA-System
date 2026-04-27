import { defineStore } from 'pinia'
import { ref, triggerRef } from 'vue'
import type { SseChunkEvent, Citation, ChatMessageHistory } from '@/types'
import { fetchSSE } from '@/utils/sse'
import { useSessionStore } from './session'

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  citations: Citation[]
  timestamp: number
  loading?: boolean
}

export const useChatStore = defineStore('chat', () => {
  const messages = ref<Message[]>([])
  const currentSessionId = ref<string>('')
  const isStreaming = ref(false)

  function addUserMessage(content: string) {
    messages.value.push({
      id: crypto.randomUUID(),
      role: 'user',
      content,
      citations: [],
      timestamp: Date.now()
    })
    triggerRef(messages)
  }

  function addAssistantPlaceholder() {
    const msg: Message = {
      id: crypto.randomUUID(),
      role: 'assistant',
      content: '',
      citations: [],
      timestamp: Date.now(),
      loading: true
    }
    messages.value.push(msg)
    triggerRef(messages)
    return msg.id
  }

  function updateAssistantContent(msgId: string, content: string) {
    const index = messages.value.findIndex(m => m.id === msgId)
    if (index !== -1) {
      messages.value[index] = { ...messages.value[index], content }
      triggerRef(messages)
    }
  }

  function updateAssistantDone(msgId: string, citations: Citation[]) {
    const index = messages.value.findIndex(m => m.id === msgId)
    if (index !== -1) {
      messages.value[index] = { ...messages.value[index], citations, loading: false }
      triggerRef(messages)
    }
  }

  function updateAssistantError(msgId: string, error: string) {
    const index = messages.value.findIndex(m => m.id === msgId)
    if (index !== -1) {
      messages.value[index] = { ...messages.value[index], content: error, loading: false }
      triggerRef(messages)
    }
  }

  async function sendMessage(question: string) {
    console.log('[Chat] sendMessage 开始，问题:', question)
    console.log('[Chat] 当前会话ID:', currentSessionId.value)

    addUserMessage(question)
    const assistantMsgId = addAssistantPlaceholder()
    isStreaming.value = true

    try {
      const eventGenerator = fetchSSE('/api/chat/stream', {
        question,
        sessionId: currentSessionId.value || undefined
      })

      let fullAnswer = ''
      const collectedCitations: Citation[] = []
      let eventCount = 0

      for await (const event of eventGenerator) {
        eventCount++
        const chunk = event as SseChunkEvent
        console.log(`[Chat] 收到事件 #${eventCount}:`, chunk.type, chunk)

        switch (chunk.type) {
          case 'chunk':
            fullAnswer += chunk.content || ''
            updateAssistantContent(assistantMsgId, fullAnswer)
            break
          case 'citation':
            if (chunk.citation) {
              collectedCitations.push(chunk.citation)
            }
            break
          case 'done':
            console.log('[Chat] 收到 done 事件，完整答案:', fullAnswer)
            console.log('[Chat] sessionId:', chunk.sessionId)
            updateAssistantDone(assistantMsgId, collectedCitations)
            if (chunk.sessionId && !currentSessionId.value) {
              currentSessionId.value = chunk.sessionId
              const sessionStore = useSessionStore()
              sessionStore.fetchSessions()
              sessionStore.switchSession(chunk.sessionId)
            }
            break
          case 'error':
            console.error('[Chat] 收到错误事件:', chunk.error)
            updateAssistantError(assistantMsgId, chunk.error || '生成答案时出错')
            break
          default:
            console.warn('[Chat] 未知事件类型:', chunk.type)
        }
      }

      console.log(`[Chat] 事件循环结束，共处理 ${eventCount} 个事件`)

      const index = messages.value.findIndex(m => m.id === assistantMsgId)
      if (index !== -1 && messages.value[index].loading) {
        updateAssistantDone(assistantMsgId, collectedCitations)
      }
    } catch (error: any) {
      console.error('[Chat] 问答请求异常:', error)
      console.error('[Chat] 错误堆栈:', error.stack)
      updateAssistantError(assistantMsgId, error.message || '网络错误，请重试')
    } finally {
      isStreaming.value = false
      console.log('[Chat] sendMessage 结束')
    }
  }

  function clearMessages() {
    messages.value = []
  }

  function setSessionId(sessionId: string) {
    currentSessionId.value = sessionId
  }

  function loadHistoryMessages(history: ChatMessageHistory[]) {
    messages.value = history.map(msg => ({
      id: crypto.randomUUID(),
      role: msg.role,
      content: msg.content,
      citations: [],
      timestamp: Date.now()
    }))
  }

  return {
    messages,
    currentSessionId,
    isStreaming,
    sendMessage,
    clearMessages,
    setSessionId,
    loadHistoryMessages
  }
})
