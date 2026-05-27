# ai-web

基于 **Vue 3 + Element Plus + Vite** 的前端，对接 `springAiDemo` 后端接口。

## 功能

| 模块 | 接口 | 说明 |
|------|------|------|
| 文件上传 | `POST /api/rag/documents/upload` | PDF / Word / TXT / Markdown |
| 文本段落 | `POST /api/rag/documents` | 多段短文本批量入库 |
| 长文粘贴 | `POST /api/rag/documents/article` | 自动分块入库 |
| RAG 问答 | `POST /api/rag/ask` | 基于向量库检索回答 |
| 通用对话 | `POST /api/chat` | 普通聊天（含 MCP） |

## 启动

1. 启动后端（默认 `8091`，需 Chroma + DashScope Key）：

```bash
cd ../springAiDemo
./mvnw spring-boot:run
```

2. 启动前端：

```bash
npm install
npm run dev
```

访问 http://localhost:5173 。开发环境通过 Vite 代理将 `/api` 转发到 `http://localhost:8091`。

## 构建

```bash
npm run build
```

产物在 `dist/` 目录。
