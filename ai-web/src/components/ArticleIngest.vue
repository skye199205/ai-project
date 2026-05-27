<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ingestArticle, extractErrorMessage } from '../api/client'
import type { RagIngestArticleResponse } from '../api/types'

const title = ref('')
const content = ref('')
const loading = ref(false)
const lastResult = ref<RagIngestArticleResponse | null>(null)

async function submit() {
  const text = content.value.trim()
  if (!text) {
    ElMessage.warning('请输入长文内容')
    return
  }

  loading.value = true
  lastResult.value = null
  try {
    const res = await ingestArticle({
      content: text,
      title: title.value.trim() || undefined,
    })
    lastResult.value = res
    ElMessage.success(`长文已入库，共 ${res.chunkCount} 个分块`)
    content.value = ''
  } catch (err) {
    ElMessage.error(extractErrorMessage(err))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="article-ingest">
    <p class="hint">粘贴整篇长文，服务端按 token 自动分块后写入向量库（最多 50 万字符）。</p>

    <el-form label-position="top">
      <el-form-item label="标题（可选）">
        <el-input
          v-model="title"
          placeholder="便于检索时区分来源"
          clearable
          maxlength="200"
          show-word-limit
          :disabled="loading"
        />
      </el-form-item>
      <el-form-item label="正文">
        <el-input
          v-model="content"
          type="textarea"
          :rows="12"
          placeholder="粘贴文章、报告、笔记等长文本…"
          maxlength="500000"
          show-word-limit
          :disabled="loading"
        />
      </el-form-item>
    </el-form>

    <el-button type="primary" :loading="loading" @click="submit">分块并入库</el-button>

    <el-descriptions
      v-if="lastResult"
      :column="2"
      border
      size="small"
      class="result-desc"
    >
      <el-descriptions-item label="分块数">{{ lastResult.chunkCount }}</el-descriptions-item>
      <el-descriptions-item label="正文长度">{{ lastResult.contentLength }} 字符</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<style scoped>
.hint {
  color: #606266;
  font-size: 13px;
  margin: 0 0 12px;
}

.result-desc {
  margin-top: 16px;
}
</style>
