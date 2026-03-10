package com.zhangzc.blog.blogai.Controller;

import com.zhangzc.blog.blogai.Pojo.domain.TSystemMessage;
import com.zhangzc.blog.blogai.Service.TSystemMessageService;
import com.zhangzc.blog.blogai.Utils.R;
import com.zhangzc.leaf.server.service.SegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prompt")
@RequiredArgsConstructor
public class PromptController {

    private final TSystemMessageService tSystemMessageService;
    private final SegmentService segmentService;

    @PostMapping("/list")
    public R list(@RequestBody Map<String, String> params) {
        String userIdObj = params.get("userId");

        List<TSystemMessage> userPrompt = tSystemMessageService.lambdaQuery()
                .eq(TSystemMessage::getUserId, userIdObj)
                .list();

        return R.Success("success", userPrompt);
    }

    @PostMapping("/save")
    public R save(@RequestBody TSystemMessage message) {
        if (message.getUserId() == null) {
            // Maybe default to system prompt (null user_id) if not provided? Or error?
            // API doc implies userId is required for user prompts.
            // Assume provided.
        }
        message.setCreatedAt(new Date());
        message.setUpdatedAt(new Date());
        message.setName(message.getTitle());
        long promptId = segmentService.getId("prompt_id").getId();
        message.setId(promptId);
        message.setUserId(message.getUserId());
        boolean saved = tSystemMessageService.save(message);
        if (saved) {
            return R.Success("success", Map.of("id", message.getId()));
        }
        return R.Faile("Save failed");
    }

    @PostMapping("/update")
    public R update(@RequestBody TSystemMessage message) {
        if (message.getId() == null) {
            return R.Faile("ID is required");
        }
        message.setUpdatedAt(new Date());
        boolean updated = tSystemMessageService.updateById(message);
        return R.Boolen(updated);
    }

    @PostMapping("/delete")
    public R delete(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id");
        Long id = null;
        if (idObj instanceof Number) {
            id = ((Number) idObj).longValue();
        } else if (idObj instanceof String) {
            try {
                id = Long.valueOf((String) idObj);
            } catch (NumberFormatException e) {
                return R.Faile("Invalid ID format");
            }
        }

        if (id == null) {
            return R.Faile("ID is required");
        }

        boolean removed = tSystemMessageService.removeById(id);
        return R.Boolen(removed);
    }
}
