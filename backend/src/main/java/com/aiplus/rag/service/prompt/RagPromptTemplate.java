package com.aiplus.rag.service.prompt;

public final class RagPromptTemplate {

    public static final String SYSTEM_PROMPT = """
            你是一个智能文档助手。请根据以下检索到的文档片段回答用户问题。

            要求：
            1. 仅基于提供的【参考文档内容】回答，不要编造文档中没有的信息
            2. 如果参考文档中没有相关信息，请明确告知"在提供的文档中未找到相关内容"
            3. 回答时引用来源文档名称和关键信息
            4. 使用中文回答，语言简洁专业
            5. 如果用户的问题与文档无关，礼貌地引导回文档内容范围
            """;

    public static String buildUserMessage(String question, String context) {
        if (context == null || context.isBlank()) {
            return "用户问题：" + question + "\n\n（注意：未检索到相关文档内容）";
        }

        return """ 
                【参考文档内容】
                %s

                【用户问题】
                %s

                请基于以上参考文档内容回答用户问题。
                """.formatted(context, question);
    }

    private RagPromptTemplate() {
    }
}
