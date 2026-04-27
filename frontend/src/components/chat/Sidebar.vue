<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <el-button type="primary" @click="handleNewChat" :icon="Plus" round>
        新建对话
      </el-button>
    </div>

    <div class="session-list">
      <div v-if="sessionStore.sessions.length === 0" class="empty-sessions">
        <el-icon :size="32"><ChatDotRound /></el-icon>
        <p>暂无对话</p>
        <span>点击上方按钮开始新对话</span>
      </div>
      <div
        v-for="session in sessionStore.sessions"
        :key="session.sessionId"
        class="session-item"
        :class="{ active: sessionStore.currentSessionId === session.sessionId }"
        @click="handleSwitchSession(session.sessionId)"
      >
        <el-icon><ChatLineRound /></el-icon>
        <span class="session-title">{{ session.title || `会话 ${session.sessionId.slice(0, 8)}` }}</span>
        <span class="session-time">{{ formatTime(session.createdAt) }}</span>
        <el-button
          size="small"
          type="danger"
          :icon="Delete"
          circle
          plain
          @click.stop="handleDeleteSession(session.sessionId)"
          class="delete-btn"
        />
      </div>
    </div>

    <div class="sidebar-footer">
      <el-button @click="showDocDrawer = true" :icon="FolderOpened" text>
        知识库管理
      </el-button>
    </div>

    <el-drawer v-model="showDocDrawer" title="知识库管理" direction="ltr" size="360px">
      <DocumentUploader />
      <DocumentList style="margin-top: 16px;" />
      <template #footer>
        <el-button type="danger" plain @click="handleClearAllDocs" :loading="documentStore.uploading">
          清空所有文档
        </el-button>
      </template>
    </el-drawer>
  </aside>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Delete, ChatDotRound, ChatLineRound, FolderOpened } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useSessionStore } from '@/stores/session'
import { useDocumentStore } from '@/stores/document'
import { useChatStore } from '@/stores/chat'
import DocumentUploader from '@/components/upload/DocumentUploader.vue'
import DocumentList from '@/components/upload/DocumentList.vue'

const sessionStore = useSessionStore()
const documentStore = useDocumentStore()
const showDocDrawer = ref(false)

onMounted(() => {
  sessionStore.fetchSessions()
})

function handleNewChat() {
  chatStore.clearMessages()
  chatStore.setSessionId('')
  sessionStore.switchSession('')
}

async function handleSwitchSession(sessionId: string) {
  sessionStore.switchSession(sessionId)
  chatStore.setSessionId(sessionId)
  chatStore.clearMessages()
  if (sessionId) {
    try {
      const history = await sessionStore.fetchSessionHistory(sessionId)
      if (history.length > 0) {
        chatStore.loadHistoryMessages(history)
      }
    } catch (error) {
      console.error('加载会话历史失败:', error)
    }
  }
}

async function handleDeleteSession(sessionId: string) {
  try {
    await ElMessageBox.confirm('确定删除该会话吗？', '确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await sessionStore.deleteSession(sessionId)
    ElMessage.success('会话已删除')
  } catch {
    // cancelled
  }
}

async function handleClearAllDocs() {
  try {
    await ElMessageBox.confirm('确定清空所有已上传的文档吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确认清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await documentStore.clearAllDocuments()
    ElMessage.success('所有文档已清除')
  } catch {
    // cancelled
  }
}

function formatTime(timestamp: number | undefined | null): string {
  if (!timestamp || typeof timestamp !== 'number' || timestamp === 0) return '刚刚'
  const date = new Date(timestamp)
  if (isNaN(date.getTime())) return '刚刚'
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}

const chatStore = useChatStore()
</script>

<style scoped lang="scss">
.sidebar {
  width: 280px;
  min-width: 280px;
  background: #1a1a2e;
  color: #e0e0e0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #2a2a3e;

  .sidebar-header {
    padding: 16px;
    border-bottom: 1px solid #2a2a3e;

    .el-button {
      width: 100%;
    }
  }

  .session-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;

    .empty-sessions {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 200px;
      color: #666;
      gap: 8px;
      p { font-size: 14px; margin: 0; }
      span { font-size: 12px; }
    }

    .session-item {
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
      transition: background 0.2s;
      position: relative;

      &:hover {
        background: #2a2a4e;
        .delete-btn { opacity: 1; }
      }

      &.active {
        background: #3a3a6e;
      }

      .session-title {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 13px;
      }

      .session-time {
        font-size: 11px;
        color: #888;
      }

      .delete-btn {
        opacity: 0;
        transition: opacity 0.2s;
      }
    }
  }

  .sidebar-footer {
    padding: 12px 16px;
    border-top: 1px solid #2a2a3e;
    text-align: center;
  }
}
</style>
