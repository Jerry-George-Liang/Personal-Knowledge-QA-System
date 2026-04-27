<template>
  <div class="document-list">
    <div class="list-header">
      <h4>已加载文档（{{ documents.length }}）</h4>
      <el-button
        v-if="documents.length > 0"
        text
        size="small"
        :icon="Refresh"
        @click="refreshList"
      />
    </div>

    <div v-if="documents.length === 0" class="empty-list">
      <el-icon :size="32" color="#ccc"><FolderOpened /></el-icon>
      <p>暂无文档</p>
      <span>请上传文件以开始问答</span>
    </div>

    <transition-group name="list" tag="div" class="doc-items">
      <div
        v-for="doc in documents"
        :key="doc.documentId"
        class="doc-item"
      >
        <div class="doc-icon">
          <el-icon :color="getFileColor(doc.fileName)">
            <component :is="getFileIcon(doc.fileName)" />
          </el-icon>
        </div>
        <div class="doc-info">
          <span class="doc-name" :title="doc.fileName">{{ doc.fileName }}</span>
          <span class="doc-meta">
            {{ doc.segmentCount }} 个段落 · {{ formatTime(doc.uploadTime) }}
          </span>
        </div>
        <el-tag size="small" type="success" effect="plain">已就绪</el-tag>
        <el-button
          size="small"
          type="danger"
          :icon="Delete"
          circle
          plain
          class="delete-btn"
          @click.stop="handleDelete(doc)"
        />
      </div>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { Refresh, FolderOpened, Document, Files, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDocumentStore } from '@/stores/document'

const documentStore = useDocumentStore()

const documents = computed(() => documentStore.documents)

onMounted(() => {
  documentStore.fetchDocuments()
})

function refreshList() {
  documentStore.fetchDocuments()
}

async function handleDelete(doc: any) {
  try {
    await ElMessageBox.confirm(
      `确定删除文档 "${doc.fileName}" 吗？删除后将无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await documentStore.deleteDocument(doc.documentId)
    ElMessage.success('文档已删除')
  } catch {
    // 用户取消
  }
}

function getFileIcon(fileName: string): any {
  const ext = fileName.split('.').pop()?.toLowerCase()
  return 'pdf' === ext ? Document : Files
}

function getFileColor(fileName: string): string {
  const ext = fileName.split('.').pop()?.toLowerCase()
  const colors: Record<string, string> = {
    pdf: '#f56c6c',
    docx: '#409eff',
    txt: '#909399',
    md: '#e6a23c'
  }
  return colors[ext || ''] || '#909399'
}

function formatTime(timeStr: string): string {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  } catch {
    return timeStr
  }
}
</script>

<style scoped lang="scss">
.document-list {
  .list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    h4 { margin: 0; font-size: 14px; color: #333; }
  }

  .empty-list {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 32px 0;
    color: #ccc;
    gap: 6px;
    p { font-size: 14px; margin: 0; }
    span { font-size: 12px; }
  }

  .doc-items {
    .doc-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 12px;
      background: white;
      border-radius: 8px;
      margin-bottom: 6px;
      border: 1px solid #eee;
      transition: box-shadow 0.2s;

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        .delete-btn { opacity: 1; }
      }

      .doc-icon {
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f7fa;
        border-radius: 6px;
      }

      .doc-info {
        flex: 1;
        min-width: 0;

        .doc-name {
          display: block;
          font-size: 13px;
          font-weight: 500;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .doc-meta {
          display: block;
          font-size: 11px;
          color: #999;
          margin-top: 2px;
        }

        .delete-btn {
          opacity: 0;
          transition: opacity 0.2s;
        }
      }
    }
  }
}

.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}
.list-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>
