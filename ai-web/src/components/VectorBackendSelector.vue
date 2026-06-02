<script setup lang="ts">
import {
  useVectorBackend,
  VECTOR_BACKEND_LABEL,
  type VectorBackend,
} from '../composables/useVectorBackend'

const backend = useVectorBackend()

const options: { value: VectorBackend; label: string }[] = [
  { value: 'chroma', label: VECTOR_BACKEND_LABEL.chroma },
  { value: 'pg', label: VECTOR_BACKEND_LABEL.pg },
]
</script>

<template>
  <div class="backend-selector">
    <span class="label">向量库</span>
    <el-radio-group v-model="backend" size="small">
      <el-radio-button
        v-for="opt in options"
        :key="opt.value"
        :value="opt.value"
      >
        {{ opt.label }}
      </el-radio-button>
    </el-radio-group>
  </div>
</template>

<style scoped>
.backend-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.label {
  font-size: 13px;
  opacity: 0.9;
  white-space: nowrap;
}

.backend-selector :deep(.el-radio-button__inner) {
  padding: 6px 12px;
}
</style>
