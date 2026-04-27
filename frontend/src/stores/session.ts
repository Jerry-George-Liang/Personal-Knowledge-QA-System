import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { SessionInfo, ChatMessageHistory } from '@/types'
import request from '@/api/request'

export const useSessionStore = defineStore('session', () => {
  const sessions = ref<SessionInfo[]>([])
  const currentSessionId = ref<string>('')

  async function createSession() {
    const res: any = await request.post('/sessions/create')
    const session: SessionInfo = res.data
    sessions.value.unshift(session)
    currentSessionId.value = session.sessionId
    return session
  }

  async function fetchSessions() {
    const res: any = await request.get('/sessions/list')
    sessions.value = res.data || []
  }

  async function deleteSession(sessionId: string) {
    await request.delete(`/sessions/${sessionId}`)
    sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = ''
    }
  }

  function switchSession(sessionId: string) {
    currentSessionId.value = sessionId
  }

  async function fetchSessionHistory(sessionId: string): Promise<ChatMessageHistory[]> {
    const res: any = await request.get(`/sessions/${sessionId}/history`)
    return res.data || []
  }

  return {
    sessions,
    currentSessionId,
    createSession,
    fetchSessions,
    deleteSession,
    switchSession,
    fetchSessionHistory
  }
})
