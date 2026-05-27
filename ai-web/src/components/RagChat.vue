<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ragAsk, extractErrorMessage } from '../api/client'
import type { RagSource } from '../api/types'

interface MessageItem {
  role: 'user' | 'assistant'
  content: string
  sources?: RagSource[]
}

const question = ref('')
const loading = ref(false)
const messages = ref<MessageItem[]>([])

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
    const res = await ragAsk({ question: q })
    messages.value.push({
      role: 'assistant',
      content: res.answer,
      sources: res.sources ?? [],
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
    <div class="message-list">
      <el-empty v-if="messages.length === 0" description="上传文档后，在此基于知识库提问" />
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message-item', msg.role]"
      >
        <div class="bubble">
          <div class="role-label">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
          <div class="content">{{ msg.content }}</div>
          <div v-if="msg.sources && msg.sources.length > 0" class="sources">
            <el-divider content-position="left">引用片段</el-divider>
            <el-collapse>
              <el-collapse-item
                v-for="(src, i) in msg.sources"
                :key="i"
                :title="`片段 ${i + 1}${src.score != null ? ` · 相似度 ${src.score.toFixed(3)}` : ''}`"
              >
                <p class="excerpt">{{ src.excerpt }}</p>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>
      </div>
      <div v-if="loading" class="loading-row">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在检索并生成回答…</span>
      </div>
    </div>

    <div class="input-area">
      <el-input
        v-model="question"
        type="textarea"
        :rows="3"
        placeholder="针对已上传文档提问，例如：文档的核心结论是什么？"
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

.message-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
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

.sources {
  margin-top: 8px;
}

.sources :deep(.el-divider__text) {
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
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
