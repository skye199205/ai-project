<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadInstance, UploadRawFile } from 'element-plus'
import { uploadDocument, extractErrorMessage } from '../api/client'
import type { RagIngestDocumentResponse } from '../api/types'

/** 嵌入知识库面板时不渲染外层卡片 */
defineProps<{ embedded?: boolean }>()

const ACCEPT_TYPES = '.pdf,.doc,.docx,.txt,.md'
const uploadRef = ref<UploadInstance>()
const title = ref('')
const uploading = ref(false)
const lastResult = ref<RagIngestDocumentResponse | null>(null)

async function handleUpload(raw: UploadRawFile) {
  uploading.value = true
  lastResult.value = null
  try {
    const result = await uploadDocument(raw as File, title.value)
    lastResult.value = result
    ElMessage.success(`「${result.fileName}」已入库，共 ${result.chunkCount} 个分块`)
    uploadRef.value?.clearFiles()
  } catch (err) {
    ElMessage.error(extractErrorMessage(err))
  } finally {
    uploading.value = false
  }
  return false
}
</script>

<template>
  <div class="document-upload">
    <p class="hint">
      支持 PDF、Word、TXT、Markdown。上传后自动解析并写入向量库，即可在右侧进行 RAG 问答。
    </p>

    <el-form label-position="top">
      <el-form-item label="文档标题（可选）">
        <el-input
          v-model="title"
          placeholder="不填则使用文件名"
          clearable
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-upload
        ref="uploadRef"
        drag
        :accept="ACCEPT_TYPES"
        :auto-upload="true"
        :limit="1"
        :disabled="uploading"
        :before-upload="handleUpload"
        :show-file-list="true"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">单文件最大 50MB</div>
        </template>
      </el-upload>
    </el-form>

    <el-skeleton v-if="uploading" :rows="2" animated class="result-skeleton" />

    <el-result v-else-if="lastResult" icon="success" title="入库成功" class="upload-result">
      <template #sub-title>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="文件名">{{ lastResult.fileName }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ lastResult.fileType }}</el-descriptions-item>
          <el-descriptions-item label="分块数">{{ lastResult.chunkCount }}</el-descriptions-item>
          <el-descriptions-item label="正文长度">
            {{ lastResult.contentLength }} 字符
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.hint {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin: 0 0 16px;
}

.upload-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 8px;
}

.result-skeleton {
  margin-top: 20px;
}

.upload-result {
  padding: 12px 0 0;
}

.upload-result :deep(.el-result__title) {
  margin-top: 8px;
}
</style>
