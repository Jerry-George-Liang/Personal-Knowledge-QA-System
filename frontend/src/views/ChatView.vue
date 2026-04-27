<template>
  <div class="chat-layout">
    <Sidebar />
    <ChatPanel />
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/chat/Sidebar.vue'
import ChatPanel from '@/components/chat/ChatPanel.vue'
</script>

<style scoped lang="scss">
.chat-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: #f5f5f5;
}
</style>
