package com.example.springaidemo.mcp;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 为 MCP 的 Streamable HTTP 传输增加 DashScope 鉴权头（与 OpenAI 兼容接口使用同一 API Key）。
 * <p>
 * Spring AI 1.1.x 的 streamable-http 配置项暂不直接支持 headers，故通过请求自定义器注入。
 */
@Configuration
public class DashScopeMcpHttpCustomizerConfiguration {

    @Bean
    McpSyncHttpClientRequestCustomizer dashScopeMcpBearerAuth(
            @Value("${spring.ai.openai.api-key:}") String dashScopeApiKey) {
        return (builder, method, endpoint, body, context) -> {
            if (dashScopeApiKey != null && !dashScopeApiKey.isBlank()) {
                builder.header("Authorization", "Bearer " + dashScopeApiKey.trim());
            }
        };
    }
}
