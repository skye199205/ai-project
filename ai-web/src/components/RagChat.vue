<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ragAskHybrid, extractErrorMessage } from '../api/client'
import type { RagHybridSource, RagRecallStats } from '../api/types'
import {
  useVectorBackend,
  VECTOR_BACKEND_LABEL,
} from '../composables/useVectorBackend'

const backend = useVectorBackend()
const backendLabel = computed(() => VECTOR_BACKEND_LABEL[backend.value])

interface MessageItem {
  role: 'user' | 'assistant'
  content: string
  sources?: RagHybridSource[]
  recallStats?: RagRecallStats
}

const question = ref('')
const loading = ref(false)
const messages = ref<MessageItem[]>([])

const recallPathLabel: Record<string, string> = {
  VECTOR: '向量',
  KEYWORD: '关键词',
  FUSION: '融合',
}

function recallPathText(path: string): string {
  return recallPathLabel[path] ?? path
}

function recallPathTagType(path: string): 'primary' | 'success' | 'warning' | 'info' {
  if (path === 'FUSION') return 'success'
  if (path === 'VECTOR') return 'primary'
  if (path === 'KEYWORD') return 'warning'
  return 'info'
}

async function sendQuestion() {
  const q = question.value.trim()
  if (!q) {
    ElMessage.warning('请输入问题')
    return
  }

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  loading.value = true

  try {
    const res = await ragAskHybrid({ question: q }, backend.value)
    messages.value.push({
      role: 'assistant',
      content: res.answer,
      sources: res.sources ?? [],
      recallStats: res.recallStats,
    })
  } catch (err) {
    messages.value.push({
      role: 'assistant',
      content: `错误：${extractErrorMessage(err)}`,
    })
  } finally {
    loading.value = false
  }
}

function clearMessages() {
  messages.value = []
}
</script>

<template>
  <div class="rag-chat">
    <el-alert
      :title="`混合 RAG（${backendLabel}）：向量语义召回 + 关键词召回，经 RRF 融合后生成答案。`"
      type="info"
      :closable="false"
      show-icon
      class="tip-alert"
    />

    <div class="message-list">
      <el-empty
        v-if="messages.length === 0"
        description="在左侧选择向量库并入库后，在此进行混合 RAG 问答"
      />
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message-item', msg.role]"
      >
        <div class="bubble">
          <div class="role-label">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
          <div class="content">{{ msg.content }}</div>

          <div v-if="msg.recallStats" class="recall-stats">
            <el-tag size="small" type="info">向量 {{ msg.recallStats.vectorHitCount }}</el-tag>
            <el-tag size="small" type="warning">关键词 {{ msg.recallStats.keywordHitCount }}</el-tag>
            <el-tag size="small" type="success">融合 {{ msg.recallStats.fusedCount }}</el-tag>
            <el-tag size="small">索引 {{ msg.recallStats.keywordIndexSize }}</el-tag>
          </div>

          <div v-if="msg.sources && msg.sources.length > 0" class="sources">
            <el-divider content-position="left">引用片段</el-divider>
            <el-collapse>
              <el-collapse-item
                v-for="(src, i) in msg.sources"
                :key="i"
              >
                <template #title>
                  <span>片段 {{ i + 1 }}</span>
                  <el-tag
                    size="small"
                    :type="recallPathTagType(src.recallPath)"
                    class="path-tag"
                  >
                    {{ recallPathText(src.recallPath) }}
                  </el-tag>
                  <span v-if="src.score != null" class="score-text">
                    融合分 {{ src.score.toFixed(4) }}
                  </span>
                </template>
                <p class="excerpt">{{ src.excerpt }}</p>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>
      </div>
      <div v-if="loading" class="loading-row">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>向量 + 关键词多路召回中…</span>
      </div>
    </div>

    <div class="input-area">
      <el-input
        v-model="question"
        type="textarea"
        :rows="3"
        placeholder="针对已入库文档提问，例如：文档的核心结论是什么？"
        maxlength="4000"
        show-word-limit
        :disabled="loading"
        @keydown.enter.exact.prevent="sendQuestion"
      />
      <div class="actions">
        <el-button :disabled="loading || messages.length === 0" @click="clearMessages">
          清空对话
        </el-button>
        <el-button type="primary" :loading="loading" @click="sendQuestion">
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.rag-chat {
  display: flex;
  flex-direction: column;
  height: 460px;
}

.tip-alert {
  margin-bottom: 12px;
  flex-shrink: 0;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
  min-height: 0;
}

.message-item {
  display: flex;
  margin-bottom: 12px;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 92%;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
}

.user .bubble {
  background: #409eff;
  color: #fff;
}

.assistant .bubble {
  background: #f4f4f5;
  color: #303133;
}

.role-label {
  font-size: 12px;
  opacity: 0.75;
  margin-bottom: 4px;
}

.content {
  white-space: pre-wrap;
  word-break: break-word;
}

.recall-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.sources {
  margin-top: 8px;
}

.sources :deep(.el-divider__text) {
  font-size: 12px;
  color: #909399;
}

.sources :deep(.el-collapse-item__header) {
  font-size: 13px;
  gap: 6px;
}

.path-tag {
  margin-left: 8px;
}

.score-text {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.excerpt {
  margin: 0;
  font-size: 13px;
  color: #606266;
  white-space: pre-wrap;
}

.loading-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 13px;
  padding: 8px 0;
}

.input-area {
  margin-top: 12px;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
  flex-shrink: 0;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
