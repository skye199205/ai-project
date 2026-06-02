/** 聊天请求 */
export interface ChatRequest {
  message: string
}

/** 聊天响应 */
export interface ChatResponse {
  reply: string
}

/** RAG 问答请求 */
export interface RagAskRequest {
  question: string
}

/** 检索引用片段 */
export interface RagSource {
  excerpt: string
  score: number | null
}

/** RAG 问答响应 */
export interface RagAskResponse {
  answer: string
  sources: RagSource[]
}

/** 混合 RAG 引用片段 */
export interface RagHybridSource {
  excerpt: string
  score: number | null
  recallPath: string
}

/** 混合召回统计 */
export interface RagRecallStats {
  vectorHitCount: number
  keywordHitCount: number
  fusedCount: number
  keywordIndexSize: number
}

/** 混合 RAG 问答响应 */
export interface RagHybridAskResponse {
  answer: string
  sources: RagHybridSource[]
  recallStats: RagRecallStats
}

/** 多段文本入库请求 */
export interface RagIngestRequest {
  texts: string[]
}

/** 多段文本入库响应 */
export interface RagIngestResponse {
  addedCount: number
}

/** 长文入库请求 */
export interface RagIngestArticleRequest {
  content: string
  title?: string
}

/** 长文入库响应 */
export interface RagIngestArticleResponse {
  chunkCount: number
  contentLength: number
}

/** 文档上传入库响应 */
export interface RagIngestDocumentResponse {
  fileName: string
  fileType: string
  chunkCount: number
  contentLength: number
}
