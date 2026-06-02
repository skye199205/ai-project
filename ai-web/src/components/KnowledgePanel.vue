<script setup lang="ts">
import { computed } from 'vue'
import DocumentUpload from './DocumentUpload.vue'
import TextIngest from './TextIngest.vue'
import ArticleIngest from './ArticleIngest.vue'
import {
  useVectorBackend,
  VECTOR_BACKEND_LABEL,
} from '../composables/useVectorBackend'

const backend = useVectorBackend()
const backendLabel = computed(() => VECTOR_BACKEND_LABEL[backend.value])
</script>

<template>
  <el-card shadow="hover" class="knowledge-panel">
    <template #header>
      <div class="card-title">
        <el-icon><Collection /></el-icon>
        <span>知识库入库</span>
        <el-tag size="small" type="info" class="backend-tag">{{ backendLabel }}</el-tag>
      </div>
    </template>

    <el-tabs type="card">
      <el-tab-pane label="文件上传">
        <DocumentUpload embedded />
      </el-tab-pane>
      <el-tab-pane label="文本段落">
        <TextIngest />
      </el-tab-pane>
      <el-tab-pane label="长文粘贴">
        <ArticleIngest />
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<style scoped>
.knowledge-panel :deep(.el-card__body) {
  padding-top: 8px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.backend-tag {
  margin-left: 4px;
  font-weight: normal;
}
</style>
