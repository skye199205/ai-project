package com.example.springaidemo.mcp;

import com.example.springaidemo.tools.CalculatorTools;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 将高德 MCP 工具与本地 @Tool（如计算器）挂到默认 {@link org.springframework.ai.chat.client.ChatClient}。
 */
@Configuration
public class AmapMcpChatClientConfiguration {

    @Bean
    ChatClientCustomizer amapMcpToolsAndWeatherInstructions(
            ObjectProvider<SyncMcpToolCallbackProvider> mcpToolCallbackProvider,
            CalculatorTools calculatorTools) {
        return builder -> {
            List<ToolCallback> toolCallbacks = new ArrayList<>();
            mcpToolCallbackProvider.ifAvailable(
                    provider -> toolCallbacks.addAll(List.of(provider.getToolCallbacks())));
            toolCallbacks.addAll(List.of(ToolCallbacks.from(calculatorTools)));
            builder.defaultToolCallbacks(toolCallbacks);

            builder.defaultSystem(
                    "你是助手。"
                            + "当用户询问天气、气温、降雨、空气质量、预报或与位置相关的出行问题时，必须先调用高德地图（amap）MCP 工具查询数据，再用中文简洁作答，不要编造实时天气；若缺少城市或坐标等参数可简短追问。"
                            + "当用户需要算术、精确数值运算时，必须调用 calculator 工具计算，不要自行心算。");
        };
    }
}
