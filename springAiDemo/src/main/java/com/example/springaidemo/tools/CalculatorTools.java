package com.example.springaidemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 供大模型通过 function calling 调用的计算器工具。
 */
@Component
public class CalculatorTools {

    /**
     * 对两个数做四则运算，返回可读的十进制字符串（模型可将结果整理后回复用户）。
     */
    @Tool(name = "calculator", description = "对两个数做四则运算。"
            + "operation 必须为英文小写：add（加）、subtract（减）、multiply（乘）、divide（除）。")
    public String calculate(
            @ToolParam(description = "左操作数（第一个数）") double left,
            @ToolParam(description = "右操作数（第二个数）") double right,
            @ToolParam(description = "运算类型：add | subtract | multiply | divide") String operation) {
        if (operation == null || operation.isBlank()) {
            return "错误：operation 不能为空，应使用 add、subtract、multiply 或 divide";
        }
        String op = operation.trim().toLowerCase(Locale.ROOT);
        return switch (op) {
            case "add", "+" -> Double.toString(left + right);
            case "subtract", "-", "minus" -> Double.toString(left - right);
            case "multiply", "*", "mul" -> Double.toString(left * right);
            case "divide", "/", "div" -> {
                if (right == 0.0) {
                    yield "错误：除数不能为 0";
                }
                yield Double.toString(left / right);
            }
            default -> "错误：不支持的 operation，请使用 add、subtract、multiply、divide";
        };
    }
}
