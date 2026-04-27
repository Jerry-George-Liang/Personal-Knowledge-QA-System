<template>
  <main class="chat-panel">
    <div class="chat-header">
      <h2>AI+ 智能文档助手</h2>
      <el-tag v-if="chatStore.isStreaming" type="warning" effect="dark" round>
        思考中...
      </el-tag>
    </div>

    <div class="message-container" ref="messageContainerRef">
      <div v-if="chatStore.messages.length === 0" class="welcome-screen">
        <el-icon :size="64" color="#409eff"><MagicStick /></el-icon>
        <h3>你好，我是 AI+ 智能文档助手</h3>
        <p>上传文档后，我可以基于文档内容回答你的问题</p>
        <div class="quick-tips">
          <el-tag>支持 PDF / Word / TXT / Markdown</el-tag>
          <el-tag type="success">本地向量化存储</el-tag>
          <el-tag type="warning">引用来源可追溯</el-tag>
        </div>
      </div>

      <MessageBubble
        v-for="msg in chatStore.messages"
        :key="`${msg.id}-${msg.content.length}-${msg.loading ? '1' : '0'}`"
        :message="msg"
      />
    </div>

    <div class="input-area">
      <el-input
        v-model="inputQuestion"
        type="textarea"
        :rows="2"
        :maxlength="2000"
        placeholder="输入你的问题... (Enter 发送，Shift+Enter 换行)"
        resize="none"
        :disabled="chatStore.isStreaming"
        @keydown.enter.exact.prevent="handleSend"
      />
      <el-button
        type="primary"
        :icon="Promotion"
        :loading="chatStore.isStreaming"
        :disabled="!inputQuestion.trim()"
        circle
        size="large"
        class="send-btn"
        @click="handleSend"
      />
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { Promotion, MagicStick } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat'
import MessageBubble from './MessageBubble.vue'

const chatStore = useChatStore()
const inputQuestion = ref('')
const messageContainerRef = ref<HTMLDivElement>()

watch(() => chatStore.messages.length, () => {
  scrollToBottom()
})

function scrollToBottom() {
  nextTick(() => {
    if (messageContainerRef.value) {
      messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight
    }
  })
}

async function handleSend() {
  const question = inputQuestion.value.trim()
  if (!question || chatStore.isStreaming) return

  inputQuestion.value = ''
  await chatStore.sendMessage(question)
  scrollToBottom()
}
</script>

<style scoped lang="scss">
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  min-width: 0;

  .chat-header {
    padding: 16px 24px;
    border-bottom: 1px solid #eee;
    display: flex;
    align-items: center;
    gap: 12px;

    h2 {
      margin: 0;
      font-size: 18px;
      color: #333;
    }
  }

  .message-container {
    flex: 1;
    overflow-y: auto;
    padding: 20px 24px;

    .welcome-screen {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #888;
      gap: 12px;

      h3 { color: #333; font-size: 20px; }
      p { font-size: 14px; max-width: 400px; text-align: center; line-height: 1.6; }

      .quick-tips {
        display: flex;
        gap: 8px;
        margin-top: 16px;
        flex-wrap: wrap;
        justify-content: center;
      }
    }
  }

  .input-area {
    padding: 16px 24px;
    border-top: 1px solid #eee;
    display: flex;
    align-items: flex-end;
    gap: 12px;
    background: #fafafa;

    .el-textarea { flex: 1; }

    .send-btn {
      flex-shrink: 0;
      width: 48px;
      height: 48px;
    }
  }
}
</style>
