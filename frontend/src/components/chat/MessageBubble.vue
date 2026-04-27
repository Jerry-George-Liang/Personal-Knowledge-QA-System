<template>
  <div class="message-bubble" :class="[message.role]">
    <div class="avatar" :class="message.role">
      <el-icon v-if="message.role === 'assistant'" :size="18"><MagicStick /></el-icon>
      <el-icon v-else :size="18"><User /></el-icon>
    </div>
    <div class="bubble-content">
      <div class="message-text" v-html="renderedContent"></div>
      <CitationCard
        v-if="message.citations && message.citations.length > 0"
        :citations="message.citations"
      />
      <div v-if="message.loading" class="typing-indicator">
        <span></span><span></span><span></span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { User, MagicStick } from '@element-plus/icons-vue'
import { marked } from 'marked'
import CitationCard from './CitationCard.vue'
import type { Message } from '@/stores/chat'

interface Props {
  message: Message
}

const props = defineProps<Props>()

const renderedContent = computed(() => {
  if (!props.message.content) return ''
  return marked.parse(props.message.content) as string
})
</script>

<style scoped lang="scss">
.message-bubble {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeInUp 0.3s ease;

  &.user {
    flex-direction: row-reverse;

    .avatar {
      background: #409eff;
      color: white;
    }

    .bubble-content {
      background: #ecf5ff;
      border-radius: 16px 16px 4px 16px;
    }
  }

  &.assistant {
    .avatar {
      background: #67c23a;
      color: white;
    }

    .bubble-content {
      background: #f5f7fa;
      border-radius: 16px 16px 16px 4px;
    }
  }

  .avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    font-size: 14px;

    img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
  }

  .bubble-content {
    max-width: 70%;
    padding: 12px 16px;
    word-break: break-word;

    .message-text {
      line-height: 1.7;
      font-size: 14px;
      color: #333;

      :deep(p) { margin: 0 0 8px; &:last-child { margin: 0; } }
      :deep(code) {
        background: #f0f0f0;
        padding: 2px 6px;
        border-radius: 4px;
        font-family: Consolas, monospace;
        font-size: 13px;
      }
      :deep(pre) {
        background: #282c34;
        color: #abb2bf;
        padding: 12px;
        border-radius: 8px;
        overflow-x: auto;
        code { background: none; color: inherit; }
      }
    }
  }
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding-top: 8px;

  span {
    width: 8px;
    height: 8px;
    background: #999;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out both;

    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
