<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ingestTexts, extractErrorMessage } from '../api/client'

const paragraphs = ref([''])
const loading = ref(false)
const lastAdded = ref<number | null>(null)

function addParagraph() {
  if (paragraphs.value.length >= 50) {
    ElMessage.warning('单次最多提交 50 段文本')
    return
  }
  paragraphs.value.push('')
}

function removeParagraph(index: number) {
  if (paragraphs.value.length <= 1) {
    paragraphs.value = ['']
    return
  }
  paragraphs.value.splice(index, 1)
}

async function submit() {
  const texts = paragraphs.value.map((t) => t.trim()).filter(Boolean)
  if (texts.length === 0) {
    ElMessage.warning('请至少填写一段有效文本')
    return
  }

  loading.value = true
  lastAdded.value = null
  try {
    const res = await ingestTexts({ texts })
    lastAdded.value = res.addedCount
    ElMessage.success(`已入库 ${res.addedCount} 段文本`)
    paragraphs.value = ['']
  } catch (err) {
    ElMessage.error(extractErrorMessage(err))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="text-ingest">
    <p class="hint">每段文本单独建嵌入，适合 FAQ、知识点等短文本批量入库。</p>

    <div v-for="(_, index) in paragraphs" :key="index" class="paragraph-row">
      <el-input
        v-model="paragraphs[index]"
        type="textarea"
        :rows="3"
        :placeholder="`第 ${index + 1} 段（最多 8000 字）`"
        maxlength="8000"
        show-word-limit
        :disabled="loading"
      />
      <el-button
        v-if="paragraphs.length > 1"
        type="danger"
        link
        :disabled="loading"
        @click="removeParagraph(index)"
      >
        删除
      </el-button>
    </div>

    <div class="actions">
      <el-button :disabled="loading || paragraphs.length >= 50" @click="addParagraph">
        添加一段
      </el-button>
      <el-button type="primary" :loading="loading" @click="submit">提交入库</el-button>
    </div>

    <el-alert
      v-if="lastAdded != null"
      :title="`上次成功入库 ${lastAdded} 段`"
      type="success"
      show-icon
      :closable="false"
      class="result-alert"
    />
  </div>
</template>

<style scoped>
.hint {
  color: #606266;
  font-size: 13px;
  margin: 0 0 12px;
}

.paragraph-row {
  margin-bottom: 12px;
}

.actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.result-alert {
  margin-top: 16px;
}
</style>
