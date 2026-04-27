export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface DocumentUploadResponse {
  documentId: string
  fileName: string
  segmentCount: number
  message: string
}

export interface DocumentMetadata {
  documentId: string
  fileName: string
  uploadTime: string
  segmentCount: number
}

export interface ChatRequest {
  question: string
  sessionId?: string
}

export interface Citation {
  sourceFileName: string
  content: string
  relevanceScore: number
}

export interface ChatResponse {
  answer: string
  citations: Citation[]
  sessionId: string
}

export interface SessionInfo {
  sessionId: string
  createdAt: number
  messageCount: number
  title?: string
}

export interface ChatMessageHistory {
  role: 'user' | 'assistant'
  content: string
}

export interface SseChunkEvent {
  type: 'chunk' | 'citation' | 'done' | 'error'
  content?: string
  citation?: Citation
  answer?: string
  citations?: Citation[]
  error?: string
  sessionId?: string
}
