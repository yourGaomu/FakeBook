package com.zhangzc.blog.blogai.Store;



import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class RidsChatMemoryStore implements ChatMemoryStore {

    private final RedisUtil redisUtil;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = memoryId.toString();
        String json = (String) redisUtil.get(key);
        List<ChatMessage> chatMessages = ChatMessageDeserializer.messagesFromJson(json);
        return chatMessages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String json = ChatMessageSerializer.messagesToJson(list);
        //设置redis中的key
        String key = memoryId.toString();
        redisUtil.set(key, json, 60 * 60 * 24*3);//三天
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = memoryId.toString();
        redisUtil.del(key);
    }
}
