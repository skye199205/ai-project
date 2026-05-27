import axios from 'axios'
import type {
  ChatRequest,
  ChatResponse,
  RagAskRequest,
  RagAskResponse,
  RagIngestArticleRequest,
  RagIngestArticleResponse,
  RagIngestDocumentResponse,
  RagIngestRequest,
  RagIngestResponse,
} from './types'

const http = axios.create({
  baseURL: '/api',
  timeout: 120000,
})

/** 通用聊天（含 MCP 能力） */
export async function chat(request: ChatRequest): Promise<ChatResponse> {
  const { data } = await http.post<ChatResponse>('/chat', request)
  return data
}

/** 多段文本写入向量库 */
export async function ingestTexts(request: RagIngestRequest): Promise<RagIngestResponse> {
  const { data } = await http.post<RagIngestResponse>('/rag/documents', request)
  return data
}

/** 长文自动分块后写入向量库 */
export async function ingestArticle(
  request: RagIngestArticleRequest
): Promise<RagIngestArticleResponse> {
  const { data } = await http.post<RagIngestArticleResponse>('/rag/documents/article', request)
  return data
}

/** 上传文档并写入向量库 */
export async function uploadDocument(
  file: File,
  title?: string
): Promise<RagIngestDocumentResponse> {
  const formData = new FormData()
  formData.append('file', file)
  if (title?.trim()) {
    formData.append('title', title.trim())
  }
  const { data } = await http.post<RagIngestDocumentResponse>(
    '/rag/documents/upload',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
  return data
}

/** RAG 问答 */
export async function ragAsk(request: RagAskRequest): Promise<RagAskResponse> {
  const { data } = await http.post<RagAskResponse>('/rag/ask', request)
  return data
}

/** 从 axios 错误中提取可读消息 */
export function extractErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const body = err.response?.data as { message?: string; error?: string } | undefined
    if (body?.message) return body.message
    if (body?.error) return body.error
    if (err.message) return err.message
  }
  if (err instanceof Error) return err.message
  return '请求失败，请稍后重试'
}
