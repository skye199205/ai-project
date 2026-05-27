<script setup lang="ts">
import KnowledgePanel from './components/KnowledgePanel.vue'
import RagChat from './components/RagChat.vue'
import GeneralChat from './components/GeneralChat.vue'
</script>

<template>
  <el-container class="app-layout">
    <el-header class="app-header">
      <div class="header-inner">
        <el-icon :size="28" color="#409eff"><ChatDotRound /></el-icon>
        <div>
          <h1>Spring AI Demo</h1>
          <p>知识库入库 + RAG 问答 + 通用对话</p>
        </div>
      </div>
    </el-header>

    <el-main class="app-main">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="10">
          <KnowledgePanel />
        </el-col>
        <el-col :xs="24" :lg="14">
          <el-tabs type="border-card" class="chat-tabs">
            <el-tab-pane label="RAG 文档问答">
              <RagChat />
            </el-tab-pane>
            <el-tab-pane label="通用对话">
              <GeneralChat />
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    sans-serif;
  background: #f0f2f5;
}

#app {
  min-height: 100vh;
}

.app-layout {
  min-height: 100vh;
}

.app-header {
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 100%);
  color: #fff;
  height: auto !important;
  padding: 20px 24px;
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-inner h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.header-inner p {
  margin: 4px 0 0;
  font-size: 13px;
  opacity: 0.85;
}

.app-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 40px;
  width: 100%;
}

.chat-tabs {
  min-height: 520px;
}

.chat-tabs .el-tabs__content {
  padding: 16px;
}
</style>
