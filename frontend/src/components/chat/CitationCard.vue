<template>
  <div class="citation-card">
    <div class="citation-header" @click="expanded = !expanded">
      <el-icon><Document /></el-icon>
      <span class="citation-label">参考来源（{{ citations.length }} 条）</span>
      <el-icon class="expand-icon" :class="{ rotated: expanded }"><ArrowDown /></el-icon>
    </div>
    <transition name="slide">
      <div v-show="expanded" class="citation-body">
        <div
          v-for="(cite, index) in citations"
          :key="index"
          class="citation-item"
        >
          <div class="cite-meta">
            <el-tag size="small" type="info">{{ cite.sourceFileName }}</el-tag>
            <el-rate
              :model-value="Math.round(cite.relevanceScore * 5)"
              disabled
              size="small"
              show-score
              :score-template="'{value}'"
            />
          </div>
          <div class="cite-content">{{ truncate(cite.content, 150) }}</div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Document, ArrowDown } from '@element-plus/icons-vue'
import type { Citation } from '@/types'

defineProps<{ citations: Citation[] }>()
const expanded = ref(false)

function truncate(text: string, len: number): string {
  if (!text || text.length <= len) return text
  return text.substring(0, len) + '...'
}
</script>

<style scoped lang="scss">
.citation-card {
  margin-top: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  background: #fafcff;

  .citation-header {
    padding: 8px 12px;
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
    font-size: 13px;
    color: #606266;

    &:hover { background: #f0f5ff; }

    .citation-label { flex: 1; }

    .expand-icon {
      transition: transform 0.3s;
      &.rotated { transform: rotate(180deg); }
    }
  }

  .citation-body {
    border-top: 1px solid #ebeef5;
    padding: 8px 12px;

    .citation-item {
      padding: 8px 0;
      border-bottom: 1px dashed #eee;

      &:last-child { border-bottom: none; }

      .cite-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;
      }

      .cite-content {
        font-size: 12px;
        color: #666;
        line-height: 1.6;
        background: white;
        padding: 8px;
        border-radius: 4px;
      }
    }
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
}
.slide-enter-to,
.slide-leave-from {
  opacity: 1;
  max-height: 500px;
}
</style>
