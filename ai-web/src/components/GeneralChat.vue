<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { chat, extractErrorMessage } from '../api/client'

interface MessageItem {
  role: 'user' | 'assistant'
  content: string
}

const message = ref('')
const loading = ref(false)
const messages = ref<MessageItem[]>([])

async function sendMessage() {
  const text = message.value.trim()
  if (!text) {
    ElMessage.warning('请输入消息')
    return
  }

  messages.value.push({ role: 'user', content: text })
  message.value = ''
  loading.value = true

  try {
    const res = await chat({ message: text })
    messages.value.push({ role: 'assistant', content: res.reply })
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
  <div class="general-chat">
    <el-alert
      title="通用对话不依赖上传文档，可询问天气（高德 MCP）等开放问题。"
      type="info"
      :closable="false"
      show-icon
      class="tip-alert"
    />

    <div class="message-list">
      <el-empty v-if="messages.length === 0" description="开始一段新对话" />
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message-item', msg.role]"
      >
        <div class="bubble">
          <div class="role-label">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
          <div class="content">{{ msg.content }}</div>
        </div>
      </div>
      <div v-if="loading" class="loading-row">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>思考中…</span>
      </div>
    </div>

    <div class="input-area">
      <el-input
        v-model="message"
        type="textarea"
        :rows="3"
        placeholder="输入消息，例如：北京今天天气怎么样？"
        maxlength="8000"
        show-word-limit
        :disabled="loading"
        @keydown.enter.exact.prevent="sendMessage"
      />
      <div class="actions">
        <el-button :disabled="loading || messages.length === 0" @click="clearMessages">
          清空对话
        </el-button>
        <el-button type="primary" :loading="loading" @click="sendMessage">
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.general-chat {
  display: flex;
  flex-direction: column;
  height: 460px;
}

.tip-alert {
  margin-bottom: 12px;
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
  background: #67c23a;
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
