import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DocumentMetadata } from '@/types'
import request from '@/api/request'

export const useDocumentStore = defineStore('document', () => {
  const documents = ref<DocumentMetadata[]>([])
  const uploading = ref(false)

  async function fetchDocuments() {
    const res: any = await request.get('/documents/list')
    documents.value = res.data || []
  }

  async function uploadFile(file: File) {
    uploading.value = true
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res: any = await request.post('/documents/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (progressEvent) => {
          // progress handling can be added here
        }
      })
      await fetchDocuments()
      return res.data
    } finally {
      uploading.value = false
    }
  }

  async function clearAllDocuments() {
    await request.delete('/documents/clear')
    documents.value = []
  }

  async function deleteDocument(documentId: string) {
    await request.delete(`/documents/${documentId}`)
    documents.value = documents.value.filter(doc => doc.documentId !== documentId)
  }

  return {
    documents,
    uploading,
    fetchDocuments,
    uploadFile,
    clearAllDocuments,
    deleteDocument
  }
})
