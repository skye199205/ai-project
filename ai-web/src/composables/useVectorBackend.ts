import { inject, provide, ref, watch, type InjectionKey, type Ref } from 'vue'

/** 向量库后端：Chroma 或 PostgreSQL pgvector */
export type VectorBackend = 'chroma' | 'pg'

export const VECTOR_BACKEND_LABEL: Record<VectorBackend, string> = {
  chroma: 'Chroma',
  pg: 'PostgreSQL (pgvector)',
}

const vectorBackendKey: InjectionKey<Ref<VectorBackend>> = Symbol('vectorBackend')
const STORAGE_KEY = 'ai-demo.vector-backend'

function readStoredBackend(): VectorBackend | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    if (value === 'chroma' || value === 'pg') {
      return value
    }
  } catch {
    // 隐私模式等场景下 localStorage 可能不可用
  }
  return null
}

/** 在根组件调用，向子树提供可切换的向量库后端 */
export function provideVectorBackend(defaultBackend: VectorBackend = 'pg') {
  const backend = ref<VectorBackend>(readStoredBackend() ?? defaultBackend)
  watch(backend, (value) => {
    try {
      localStorage.setItem(STORAGE_KEY, value)
    } catch {
      // ignore
    }
  })
  provide(vectorBackendKey, backend)
  return backend
}

/** 读取当前选中的向量库后端 */
export function useVectorBackend(): Ref<VectorBackend> {
  const backend = inject(vectorBackendKey)
  if (!backend) {
    throw new Error('useVectorBackend 必须在 provideVectorBackend 之后使用')
  }
  return backend
}
