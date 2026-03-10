package com.zhangzc.blog.blogai.Controller;

import com.zhangzc.blog.blogai.AiService.DynamicChatService;
import com.zhangzc.blog.blogai.Context.AiContext4User;
import com.zhangzc.blog.blogai.Exception.IsEmptyForChatCount;
import com.zhangzc.blog.blogai.Exception.IsEmptyForQN;
import com.zhangzc.blog.blogai.Exception.IsNoRole;
import com.zhangzc.blog.blogai.Pojo.Vo.AiMessage;
import com.zhangzc.blog.blogai.Pojo.Vo.ChatMessageVo;
import com.zhangzc.blog.blogai.Pojo.Vo.PostGrillMessage;
import com.zhangzc.blog.blogai.Pojo.Vo.SessionListVo;
import com.zhangzc.blog.blogai.Pojo.domain.MongoChatMessage;
import com.zhangzc.blog.blogai.Pojo.domain.TAi;
import com.zhangzc.blog.blogai.Service.TAiService;
import com.zhangzc.blog.blogai.Service.TSystemMessageService;
import com.zhangzc.blog.blogai.Service.Text2ImageService;
import com.zhangzc.blog.blogai.Service.VisionService;
import com.zhangzc.blog.blogai.Store.HybridChatMemoryStore;
import com.zhangzc.blog.blogai.Threds.ForDecTaiThred;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.leaf.core.common.Result;
import com.zhangzc.leaf.core.common.Status;
import com.zhangzc.leaf.server.service.SegmentService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final DynamicChatService dynamicChatService;
    private final TAiService tAiService;
    private final HybridChatMemoryStore hybridChatMemoryStore;
    private final VisionService visionService;
    private final Text2ImageService text2ImageService;
    private final TSystemMessageService tSystemMessageService;
    private final ObjectProvider<SegmentService> segmentServiceProvider;

    @PostMapping(value = "/chat/rem", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatWithRem(@RequestBody PostGrillMessage posterGrilMessage) throws Exception {
        //获取用户的id
        System.out.println(posterGrilMessage);
        String info = posterGrilMessage.getUserid();
        if (info.isEmpty()) {
            return Flux.empty();
        }
        return dynamicChatService.getService(1L, "postGrill")
                .chat("PostGrill_Rem_With_" + posterGrilMessage.getUserid(), posterGrilMessage.getInfo());
    }


    @PostMapping(value = "/chat/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody AiMessage aiMessage) throws Exception {
        String qq_num = aiMessage.getQq();
        String message = aiMessage.getMessage();
        String imageUrl = aiMessage.getImageUrl();
        String sessionId = aiMessage.getSessionId();

        if (qq_num.isEmpty()) {
            throw new IsEmptyForQN("请你登录一下", "500");
        }

        // 处理图片逻辑 (Vision Pipeline)
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            // 使用传入的 visionModelId，如果为空则 VisionService 会使用默认策略
            // 传入用户的 message，让视觉模型针对问题进行分析
            String imageDescription = visionService.describeImage(aiMessage.getVisionModelId(), imageUrl, message);

            // 将图片描述附加到用户消息中，形成新的 Prompt
            // 1. 保留原始问题
            // 2. 添加 Markdown 图片链接，以便前端历史记录能渲染图片
            // 3. 添加系统提示的图片描述(包含视觉模型对问题的回答)，以便不支持 Vision 的 LLM 能理解图片内容和视觉模型的分析结果
            // 使用 <vision_context> 标签包裹，方便后续在 getHistory 接口中过滤掉，不展示给用户
            message = String.format("%s\n\n![uploaded_image](%s)\n\n<vision_context>[系统提示：用户上传了一张图片。针对该图片和用户的问题，视觉模型的分析结果如下：%s]</vision_context>",
                    message, imageUrl, imageDescription);
        }

        TAi one = tAiService.lambdaQuery()
                .eq(TAi::getQq, qq_num)
                .eq(TAi::getIsBanned, 0)
                .eq(TAi::getRole, 1)
                .one();

        if (one == null) {
            throw new IsNoRole("你没有权限", "500");
        }

        if (one.getChatCount() == 0) {
            throw new IsEmptyForChatCount("对不起你的对话次数已经用尽了", "500");
        }

        // 设置联网搜索意图
        AiContext4User.setEnableWebSearch(aiMessage.getEnableWebSearch());

        //先启动另外一个线程减去对话次数
        ForDecTaiThred forDecTaiThred = new ForDecTaiThred(tAiService, one.getId().toString());
        String call = forDecTaiThred.call();
        //返回用户的询问
        boolean createdSession = false;
        String sessionTitle = null;
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = generateSessionId();
            sessionTitle = "New Chat";
            hybridChatMemoryStore.createSession(sessionId, Long.valueOf(qq_num), aiMessage.getModelId(), aiMessage.getPromptId(), sessionTitle);

            // 保存初始 System Message 到数据库，以便在历史记录中查看完整的 Prompt
            try {
                String fullSystemMessage = dynamicChatService.getFullSystemMessage(aiMessage.getModelId(), aiMessage.getPromptId());
                if (fullSystemMessage != null && !fullSystemMessage.isBlank()) {
                    List<ChatMessage> initMessages = new ArrayList<>();
                    initMessages.add(new SystemMessage(fullSystemMessage));
                    hybridChatMemoryStore.updateMessages(sessionId, initMessages);
                }
            } catch (Exception e) {
                // 记录错误但不中断流程
                System.err.println("Failed to save initial system message: " + e.getMessage());
            }

            createdSession = true;
        }

        // 使用新版 DynamicChatService.getService(modelId, promptId) 方法
        // 内部会自动处理 Prompt 拼接和缓存
        Flux<String> chatFlux = dynamicChatService.getService(aiMessage.getModelId(), aiMessage.getPromptId())
                .chat(sessionId, message);

        if (createdSession) {
            String sessionEvent = "{\"sessionId\":\"" + sessionId + "\",\"title\":\"" + sessionTitle + "\"}";
            return Flux.concat(Flux.just(sessionEvent), chatFlux.doFinally(signalType -> AiContext4User.clear()));
        }
        return chatFlux.doFinally(signalType -> AiContext4User.clear());
    }

    @DeleteMapping("/chat/delete")
    public void chatDelete(@RequestBody AiMessage aiMessage) {
        String qq = aiMessage.getQq();
        String sessionId = aiMessage.getSessionId();
        if (sessionId != null && !sessionId.isBlank()) {
            hybridChatMemoryStore.deleteMessages(sessionId);
            return;
        }
        if (qq == null || qq.isBlank()) {
            return;
        }
        TAi one = tAiService.lambdaQuery().eq(TAi::getQq, qq).one();
        if (one == null) {
            return;
        }
        Integer id = one.getId();
        if (id != null) {
            hybridChatMemoryStore.deleteMessages(qq + "_" + id);
        }
    }

    @PostMapping("/getSessionList")
    public List<SessionListVo> getSessionList(@RequestBody AiMessage aiMessage) {
        String qq = aiMessage.getQq();
        if (qq == null || qq.isBlank()) {
            return new ArrayList<>();
        }
        TAi one = tAiService.lambdaQuery()
                .eq(TAi::getQq, qq).one();
        if (one == null) {
            return new ArrayList<>();
        }
        return hybridChatMemoryStore.getSessions(Long.valueOf(one.getQq()));
    }

    @PostMapping("/getHistory")
    public List<ChatMessageVo> getHistory(@RequestBody com.zhangzc.blog.blogai.Pojo.Vo.AiMessage aiMessage) {
        String sessionId = aiMessage.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            return new java.util.ArrayList<>();
        }

        List<MongoChatMessage> messages = hybridChatMemoryStore.getHistoryDetails(sessionId);
        if (messages == null) {
            return new java.util.ArrayList<>();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return messages.stream()
                .map(msg -> {
            ChatMessageVo vo = new ChatMessageVo();

            //过滤掉工具调用
            String content = msg.getContent();

            // 过滤掉 <vision_context> 标签包裹的内容，这部分是 Vision 模型生成的上下文，不需要展示给用户
            if (content != null) {
                content = content.replaceAll("<vision_context>[\\s\\S]*?</vision_context>", "").trim();
            }

            vo.setContent(content);
            if (msg.getCreatedAt() != null) {
                vo.setCreateTime(msg.getCreatedAt().format(formatter));
            }

            // Map ChatMessageType to simple string
            if (msg.getType() != null) {
                switch (msg.getType()) {
                    case USER:
                        vo.setRole("user");
                        break;
                    case AI:
                        vo.setRole("ai");
                        break;
                    case SYSTEM:
                        vo.setRole("system");
                        break;
                    default:
                        vo.setRole("unknown");
                }
            } else {
                vo.setRole("unknown");
            }

            return vo;
        }).collect(Collectors.toList());
    }

    private String buildSessionEvent(String sessionId, String title) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId", sessionId);
        payload.put("title", title);
        return "event: session\ndata: " + JsonUtils.toJsonString(payload) + "\n\n";
    }

    private String summarizeTitle(String message) {
        if (message == null) {
            return "新对话";
        }
        String cleaned = message.replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) {
            return "新对话";
        }
        int max = 20;
        if (cleaned.length() <= max) {
            return cleaned;
        }
        return cleaned.substring(0, max);
    }

    private String generateSessionId() {
        SegmentService segmentService = segmentServiceProvider.getIfAvailable();
        if (segmentService != null) {
            Result result = segmentService.getId("chat_session");
            if (result != null && result.getStatus() == Status.SUCCESS) {
                return String.valueOf(result.getId());
            }
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
