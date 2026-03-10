2026-03-09T22:37:02.771+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 📥 接收Kafka消息 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 455 | RedisKey: kafka:offset:chat:blog-ai-chat-history
2026-03-09T22:37:02.771+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> lua路径: /lua/check_and_update_offset.lua
2026-03-09T22:37:02.771+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> 传递给 Lua 的 ARGV 参数：长度=2, 元素=[partition-0, 455]
2026-03-09T22:37:02.774+08:00  INFO 9060 --- [share-thing-ai-impl] [impl-producer-1] c.z.k.listener.ProduceListenerImpl       : 消息发送成功 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 455 | Key: null | Value: {"sessionId":"12203","messagesJson":"[{\"text\":\"你是一位专业的知识顾问，具备以下核心能力：\\r\\n1.  **深度知识问答**：基于你的内置知识库，提供准确、详尽、结构化的知识性回答。\\r\\n2.  **智能检索代理**：当你判断问题涉及外部知识库（如最新文献、企业内部数据）时，必须通过MCP协议调用向量数据库（Milvus）进行精准检索。\\r\\n3.  **分析与整合**：将检索到的信息与你的知识进行整合，提供有洞察力的分析，而非简单罗列。\\r\\n\\r\\n**工作原则：**\\r\\n- **精准理解**：首先分析用户问题的意图、关键实体（如主题、时间、领域）和所需信息类型（事实、分析、总结）。\\r\\n- **智能决策**：若问题涉及时效性强、专业性强或特定数据集的内容（如“最新研究”、“公司销售数据”），必须使用MCP调用向量数据库。若为通用知识或概念性问题，直接回答。\\r\\n- **结构化输出**：回答需逻辑清晰，必要时使用Markdown格式（如标题、列表、表格）。\\r\\n- **透明交互**：当调用外部工具时，向用户说明：“正在为您查询最新资料...”或“已从知识库中检索到相关信息”。\\r\\n\\r\\n**禁止行为：**\\r\\n- 猜测或编造未检索到的信息（避免“幻觉”）。\\r\\n- 在未调用工具的情况下，声称已查询外部数据库。\\r\\n\\r\\n你的目标是成为用户最可靠的知识伙伴，通过智能检索与深度分析，提供超越简单搜索的答案。\\r\\n\",\"type\":\"SYSTEM\"}]"}
2026-03-09T22:37:02.788+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 🔍 偏移量检查结果 | Status: 1 | Message: allow
2026-03-09T22:37:02.788+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : ✅ 允许消费 | Type: allow
2026-03-09T22:37:03.088+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.b.b.Listener.ChatHistoryConsumer     : Successfully saved chat history for session: 12203
2026-03-09T22:37:03.292+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 📥 接收Kafka消息 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 456 | RedisKey: kafka:offset:chat:blog-ai-chat-history
2026-03-09T22:37:03.292+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> lua路径: /lua/check_and_update_offset.lua
2026-03-09T22:37:03.292+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> 传递给 Lua 的 ARGV 参数：长度=2, 元素=[partition-0, 456]
2026-03-09T22:37:03.295+08:00  INFO 9060 --- [share-thing-ai-impl] [impl-producer-1] c.z.k.listener.ProduceListenerImpl       : 消息发送成功 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 456 | Key: null | Value: {"sessionId":"12203","messagesJson":"[{\"text\":\"你是一位专业的知识顾问，具备以下核心能力：\\r\\n1.  **深度知识问答**：基于你的内置知识库，提供准确、详尽、结构化的知识性回答。\\r\\n2.  **智能检索代理**：当你判断问题涉及外部知识库（如最新文献、企业内部数据）时，必须通过MCP协议调用向量数据库（Milvus）进行精准检索。\\r\\n3.  **分析与整合**：将检索到的信息与你的知识进行整合，提供有洞察力的分析，而非简单罗列。\\r\\n\\r\\n**工作原则：**\\r\\n- **精准理解**：首先分析用户问题的意图、关键实体（如主题、时间、领域）和所需信息类型（事实、分析、总结）。\\r\\n- **智能决策**：若问题涉及时效性强、专业性强或特定数据集的内容（如“最新研究”、“公司销售数据”），必须使用MCP调用向量数据库。若为通用知识或概念性问题，直接回答。\\r\\n- **结构化输出**：回答需逻辑清晰，必要时使用Markdown格式（如标题、列表、表格）。\\r\\n- **透明交互**：当调用外部工具时，向用户说明：“正在为您查询最新资料...”或“已从知识库中检索到相关信息”。\\r\\n\\r\\n**禁止行为：**\\r\\n- 猜测或编造未检索到的信息（避免“幻觉”）。\\r\\n- 在未调用工具的情况下，声称已查询外部数据库。\\r\\n\\r\\n你的目标是成为用户最可靠的知识伙伴，通过智能检索与深度分析，提供超越简单搜索的答案。\\r\\n\",\"type\":\"SYSTEM\"},{\"contents\":[{\"text\":\"帮我生产一张小猫在看书的图片吧\",\"type\":\"TEXT\"}],\"type\":\"USER\"}]"}
2026-03-09T22:37:03.308+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 🔍 偏移量检查结果 | Status: 1 | Message: allow
2026-03-09T22:37:03.308+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : ✅ 允许消费 | Type: allow
2026-03-09T22:37:03.637+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.b.b.Listener.ChatHistoryConsumer     : Successfully saved chat history for session: 12203
2026-03-09T22:37:04.925+08:00  INFO 9060 --- [share-thing-ai-impl] [liyuncs.com/...] c.z.blog.blogai.Tools.ImageGenTool       : Request to generate image with prompt: A cute cartoon-style kitten sitting at a wooden desk, wearing small round glasses, attentively reading an open book with illustrated pages. Warm lighting, cozy library background with soft shadows and a few floating dust particles.
2026-03-09T22:37:04.925+08:00  INFO 9060 --- [share-thing-ai-impl] [liyuncs.com/...] c.z.b.blogai.Service.Text2ImageService   : Generating image for prompt: A cute cartoon-style kitten sitting at a wooden desk, wearing small round glasses, attentively reading an open book with illustrated pages. Warm lighting, cozy library background with soft shadows and a few floating dust particles.
2026-03-09T22:37:05.002+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 📥 接收Kafka消息 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 457 | RedisKey: kafka:offset:chat:blog-ai-chat-history
2026-03-09T22:37:05.002+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> lua路径: /lua/check_and_update_offset.lua
2026-03-09T22:37:05.002+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> 传递给 Lua 的 ARGV 参数：长度=2, 元素=[partition-0, 457]
2026-03-09T22:37:05.006+08:00  INFO 9060 --- [share-thing-ai-impl] [impl-producer-1] c.z.k.listener.ProduceListenerImpl       : 消息发送成功 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 457 | Key: null | Value: {"sessionId":"12203","messagesJson":"[{\"text\":\"你是一位专业的知识顾问，具备以下核心能力：\\r\\n1.  **深度知识问答**：基于你的内置知识库，提供准确、详尽、结构化的知识性回答。\\r\\n2.  **智能检索代理**：当你判断问题涉及外部知识库（如最新文献、企业内部数据）时，必须通过MCP协议调用向量数据库（Milvus）进行精准检索。\\r\\n3.  **分析与整合**：将检索到的信息与你的知识进行整合，提供有洞察力的分析，而非简单罗列。\\r\\n\\r\\n**工作原则：**\\r\\n- **精准理解**：首先分析用户问题的意图、关键实体（如主题、时间、领域）和所需信息类型（事实、分析、总结）。\\r\\n- **智能决策**：若问题涉及时效性强、专业性强或特定数据集的内容（如“最新研究”、“公司销售数据”），必须使用MCP调用向量数据库。若为通用知识或概念性问题，直接回答。\\r\\n- **结构化输出**：回答需逻辑清晰，必要时使用Markdown格式（如标题、列表、表格）。\\r\\n- **透明交互**：当调用外部工具时，向用户说明：“正在为您查询最新资料...”或“已从知识库中检索到相关信息”。\\r\\n\\r\\n**禁止行为：**\\r\\n- 猜测或编造未检索到的信息（避免“幻觉”）。\\r\\n- 在未调用工具的情况下，声称已查询外部数据库。\\r\\n\\r\\n你的目标是成为用户最可靠的知识伙伴，通过智能检索与深度分析，提供超越简单搜索的答案。\\r\\n\",\"type\":\"SYSTEM\"},{\"contents\":[{\"text\":\"帮我生产一张小猫在看书的图片吧\",\"type\":\"TEXT\"}],\"type\":\"USER\"},{\"toolExecutionRequests\":[{\"id\":\"call_c6ff4ecf1ac149b9813bf8\",\"name\":\"generateImage\",\"arguments\":\"{\\\"arg0\\\": \\\"A cute cartoon-style kitten sitting at a wooden desk, wearing small round glasses, attentively reading an open book with illustrated pages. Warm lighting, cozy library background with soft shadows and a few floating dust particles.\\\"}\"}],\"type\":\"AI\"}]"}
2026-03-09T22:37:05.017+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 🔍 偏移量检查结果 | Status: 1 | Message: allow
2026-03-09T22:37:05.017+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : ✅ 允许消费 | Type: allow
2026-03-09T22:37:05.091+08:00 ERROR 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.b.b.Listener.ChatHistoryConsumer     : Failed to consume chat history message

java.lang.NullPointerException: Cannot invoke "String.equals(Object)" because the return value of "com.zhangzc.blog.blogai.Store.MongoChatMemoryStore.getText(dev.langchain4j.data.message.ChatMessage)" is null
	at com.zhangzc.blog.blogai.Store.MongoChatMemoryStore.updateMessages(MongoChatMemoryStore.java:101) ~[classes/:na]
	at com.zhangzc.blog.blogai.Listener.ChatHistoryConsumer.consumeChatHistory(ChatHistoryConsumer.java:69) ~[classes/:na]
	at jdk.internal.reflect.GeneratedMethodAccessor91.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:354) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed(MethodInvocationProceedingJoinPoint.java:89) ~[spring-aop-6.1.8.jar:6.1.8]
	at com.zhangzc.kafkaspringbootstart.aspect.KafkaOffsetAspect.around(KafkaOffsetAspect.java:94) ~[classes/:na]
	at jdk.internal.reflect.GeneratedMethodAccessor102.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:637) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:627) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.aspectj.AspectJAroundAdvice.invoke(AspectJAroundAdvice.java:71) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:173) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:720) ~[spring-aop-6.1.8.jar:6.1.8]
	at com.zhangzc.blog.blogai.Listener.ChatHistoryConsumer$$SpringCGLIB$$0.consumeChatHistory(<generated>) ~[classes/:na]
	at jdk.internal.reflect.GeneratedMethodAccessor91.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:169) ~[spring-messaging-6.1.8.jar:6.1.8]
	at org.springframework.kafka.listener.adapter.KotlinAwareInvocableHandlerMethod.doInvoke(KotlinAwareInvocableHandlerMethod.java:45) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:119) ~[spring-messaging-6.1.8.jar:6.1.8]
	at org.springframework.kafka.listener.adapter.HandlerAdapter.invoke(HandlerAdapter.java:70) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:420) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.MessagingMessageListenerAdapter.invoke(MessagingMessageListenerAdapter.java:384) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter.onMessage(RecordMessagingMessageListenerAdapter.java:85) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter.onMessage(RecordMessagingMessageListenerAdapter.java:50) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeOnMessage(KafkaMessageListenerContainer.java:2800) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeOnMessage(KafkaMessageListenerContainer.java:2778) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.lambda$doInvokeRecordListener$53(KafkaMessageListenerContainer.java:2701) ~[spring-kafka-3.2.0.jar:3.2.0]
	at io.micrometer.observation.Observation.observe(Observation.java:565) ~[micrometer-observation-1.13.0.jar:1.13.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeRecordListener(KafkaMessageListenerContainer.java:2699) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeWithRecords(KafkaMessageListenerContainer.java:2541) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeRecordListener(KafkaMessageListenerContainer.java:2430) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeListener(KafkaMessageListenerContainer.java:2085) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeIfHaveRecords(KafkaMessageListenerContainer.java:1461) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.pollAndInvoke(KafkaMessageListenerContainer.java:1426) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.run(KafkaMessageListenerContainer.java:1296) ~[spring-kafka-3.2.0.jar:3.2.0]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run$$$capture(CompletableFuture.java:1804) ~[na:na]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run(CompletableFuture.java) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:840) ~[na:na]

2026-03-09T22:37:08.242+08:00  INFO 9060 --- [share-thing-ai-impl] [-idCache-thread] c.z.leaf.core.segment.SegmentIDGenImpl   : update cache from db
2026-03-09T22:37:08.291+08:00  INFO 9060 --- [share-thing-ai-impl] [-idCache-thread] org.perf4j.TimingLogger                  : start[1773067028242] time[48] tag[updateCacheFromDb]
2026-03-09T22:37:17.905+08:00  INFO 9060 --- [share-thing-ai-impl] [liyuncs.com/...] c.z.b.blogai.Service.Text2ImageService   : Image generated successfully: https://dashscope-result-wlcb-acdr-1.oss-cn-wulanchabu-acdr-1.aliyuncs.com/1d/6a/20260309/77c9ad12/ddfdecb8-e488-407d-b43c-c2fbc84ea5bc2842430215.png?Expires=1773153436&OSSAccessKeyId=LTAI5tKPD3TMqf2Lna1fASuh&Signature=DyM%2Fml7GKzMi6D7njRuoq2DsSpY%3D
2026-03-09T22:37:18.016+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 📥 接收Kafka消息 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 458 | RedisKey: kafka:offset:chat:blog-ai-chat-history
2026-03-09T22:37:18.016+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> lua路径: /lua/check_and_update_offset.lua
2026-03-09T22:37:18.016+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> 传递给 Lua 的 ARGV 参数：长度=2, 元素=[partition-0, 458]
2026-03-09T22:37:18.019+08:00  INFO 9060 --- [share-thing-ai-impl] [impl-producer-1] c.z.k.listener.ProduceListenerImpl       : 消息发送成功 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 458 | Key: null | Value: {"sessionId":"12203","messagesJson":"[{\"text\":\"你是一位专业的知识顾问，具备以下核心能力：\\r\\n1.  **深度知识问答**：基于你的内置知识库，提供准确、详尽、结构化的知识性回答。\\r\\n2.  **智能检索代理**：当你判断问题涉及外部知识库（如最新文献、企业内部数据）时，必须通过MCP协议调用向量数据库（Milvus）进行精准检索。\\r\\n3.  **分析与整合**：将检索到的信息与你的知识进行整合，提供有洞察力的分析，而非简单罗列。\\r\\n\\r\\n**工作原则：**\\r\\n- **精准理解**：首先分析用户问题的意图、关键实体（如主题、时间、领域）和所需信息类型（事实、分析、总结）。\\r\\n- **智能决策**：若问题涉及时效性强、专业性强或特定数据集的内容（如“最新研究”、“公司销售数据”），必须使用MCP调用向量数据库。若为通用知识或概念性问题，直接回答。\\r\\n- **结构化输出**：回答需逻辑清晰，必要时使用Markdown格式（如标题、列表、表格）。\\r\\n- **透明交互**：当调用外部工具时，向用户说明：“正在为您查询最新资料...”或“已从知识库中检索到相关信息”。\\r\\n\\r\\n**禁止行为：**\\r\\n- 猜测或编造未检索到的信息（避免“幻觉”）。\\r\\n- 在未调用工具的情况下，声称已查询外部数据库。\\r\\n\\r\\n你的目标是成为用户最可靠的知识伙伴，通过智能检索与深度分析，提供超越简单搜索的答案。\\r\\n\",\"type\":\"SYSTEM\"},{\"contents\":[{\"text\":\"帮我生产一张小猫在看书的图片吧\",\"type\":\"TEXT\"}],\"type\":\"USER\"},{\"toolExecutionRequests\":[{\"id\":\"call_c6ff4ecf1ac149b9813bf8\",\"name\":\"generateImage\",\"arguments\":\"{\\\"arg0\\\": \\\"A cute cartoon-style kitten sitting at a wooden desk, wearing small round glasses, attentively reading an open book with illustrated pages. Warm lighting, cozy library background with soft shadows and a few floating dust particles.\\\"}\"}],\"type\":\"AI\"},{\"id\":\"call_c6ff4ecf1ac149b9813bf8\",\"toolName\":\"generateImage\",\"text\":\"https://dashscope-result-wlcb-acdr-1.oss-cn-wulanchabu-acdr-1.aliyuncs.com/1d/6a/20260309/77c9ad12/ddfdecb8-e488-407d-b43c-c2fbc84ea5bc2842430215.png?Expires=1773153436&OSSAccessKeyId=LTAI5tKPD3TMqf2Lna1fASuh&Signature=DyM%2Fml7GKzMi6D7njRuoq2DsSpY%3D\",\"type\":\"TOOL_EXECUTION_RESULT\"}]"}
2026-03-09T22:37:18.031+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 🔍 偏移量检查结果 | Status: 1 | Message: allow
2026-03-09T22:37:18.031+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : ✅ 允许消费 | Type: allow
2026-03-09T22:37:18.114+08:00 ERROR 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.b.b.Listener.ChatHistoryConsumer     : Failed to consume chat history message

java.lang.NullPointerException: Cannot invoke "String.equals(Object)" because the return value of "com.zhangzc.blog.blogai.Store.MongoChatMemoryStore.getText(dev.langchain4j.data.message.ChatMessage)" is null
	at com.zhangzc.blog.blogai.Store.MongoChatMemoryStore.updateMessages(MongoChatMemoryStore.java:101) ~[classes/:na]
	at com.zhangzc.blog.blogai.Listener.ChatHistoryConsumer.consumeChatHistory(ChatHistoryConsumer.java:69) ~[classes/:na]
	at jdk.internal.reflect.GeneratedMethodAccessor91.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:354) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed(MethodInvocationProceedingJoinPoint.java:89) ~[spring-aop-6.1.8.jar:6.1.8]
	at com.zhangzc.kafkaspringbootstart.aspect.KafkaOffsetAspect.around(KafkaOffsetAspect.java:94) ~[classes/:na]
	at jdk.internal.reflect.GeneratedMethodAccessor102.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:637) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:627) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.aspectj.AspectJAroundAdvice.invoke(AspectJAroundAdvice.java:71) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:173) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:720) ~[spring-aop-6.1.8.jar:6.1.8]
	at com.zhangzc.blog.blogai.Listener.ChatHistoryConsumer$$SpringCGLIB$$0.consumeChatHistory(<generated>) ~[classes/:na]
	at jdk.internal.reflect.GeneratedMethodAccessor91.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:169) ~[spring-messaging-6.1.8.jar:6.1.8]
	at org.springframework.kafka.listener.adapter.KotlinAwareInvocableHandlerMethod.doInvoke(KotlinAwareInvocableHandlerMethod.java:45) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:119) ~[spring-messaging-6.1.8.jar:6.1.8]
	at org.springframework.kafka.listener.adapter.HandlerAdapter.invoke(HandlerAdapter.java:70) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:420) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.MessagingMessageListenerAdapter.invoke(MessagingMessageListenerAdapter.java:384) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter.onMessage(RecordMessagingMessageListenerAdapter.java:85) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter.onMessage(RecordMessagingMessageListenerAdapter.java:50) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeOnMessage(KafkaMessageListenerContainer.java:2800) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeOnMessage(KafkaMessageListenerContainer.java:2778) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.lambda$doInvokeRecordListener$53(KafkaMessageListenerContainer.java:2701) ~[spring-kafka-3.2.0.jar:3.2.0]
	at io.micrometer.observation.Observation.observe(Observation.java:565) ~[micrometer-observation-1.13.0.jar:1.13.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeRecordListener(KafkaMessageListenerContainer.java:2699) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeWithRecords(KafkaMessageListenerContainer.java:2541) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeRecordListener(KafkaMessageListenerContainer.java:2430) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeListener(KafkaMessageListenerContainer.java:2085) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeIfHaveRecords(KafkaMessageListenerContainer.java:1461) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.pollAndInvoke(KafkaMessageListenerContainer.java:1426) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.run(KafkaMessageListenerContainer.java:1296) ~[spring-kafka-3.2.0.jar:3.2.0]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run$$$capture(CompletableFuture.java:1804) ~[na:na]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run(CompletableFuture.java) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:840) ~[na:na]

2026-03-09T22:37:24.417+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 📥 接收Kafka消息 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 459 | RedisKey: kafka:offset:chat:blog-ai-chat-history
2026-03-09T22:37:24.417+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> lua路径: /lua/check_and_update_offset.lua
2026-03-09T22:37:24.417+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.redisspringbootstart.utills.LuaUtil  : ==> 传递给 Lua 的 ARGV 参数：长度=2, 元素=[partition-0, 459]
2026-03-09T22:37:24.420+08:00  INFO 9060 --- [share-thing-ai-impl] [impl-producer-1] c.z.k.listener.ProduceListenerImpl       : 消息发送成功 | Topic: blog-ai-chat-history | Partition: 0 | Offset: 459 | Key: null | Value: {"sessionId":"12203","messagesJson":"[{\"text\":\"你是一位专业的知识顾问，具备以下核心能力：\\r\\n1.  **深度知识问答**：基于你的内置知识库，提供准确、详尽、结构化的知识性回答。\\r\\n2.  **智能检索代理**：当你判断问题涉及外部知识库（如最新文献、企业内部数据）时，必须通过MCP协议调用向量数据库（Milvus）进行精准检索。\\r\\n3.  **分析与整合**：将检索到的信息与你的知识进行整合，提供有洞察力的分析，而非简单罗列。\\r\\n\\r\\n**工作原则：**\\r\\n- **精准理解**：首先分析用户问题的意图、关键实体（如主题、时间、领域）和所需信息类型（事实、分析、总结）。\\r\\n- **智能决策**：若问题涉及时效性强、专业性强或特定数据集的内容（如“最新研究”、“公司销售数据”），必须使用MCP调用向量数据库。若为通用知识或概念性问题，直接回答。\\r\\n- **结构化输出**：回答需逻辑清晰，必要时使用Markdown格式（如标题、列表、表格）。\\r\\n- **透明交互**：当调用外部工具时，向用户说明：“正在为您查询最新资料...”或“已从知识库中检索到相关信息”。\\r\\n\\r\\n**禁止行为：**\\r\\n- 猜测或编造未检索到的信息（避免“幻觉”）。\\r\\n- 在未调用工具的情况下，声称已查询外部数据库。\\r\\n\\r\\n你的目标是成为用户最可靠的知识伙伴，通过智能检索与深度分析，提供超越简单搜索的答案。\\r\\n\",\"type\":\"SYSTEM\"},{\"contents\":[{\"text\":\"帮我生产一张小猫在看书的图片吧\",\"type\":\"TEXT\"}],\"type\":\"USER\"},{\"toolExecutionRequests\":[{\"id\":\"call_c6ff4ecf1ac149b9813bf8\",\"name\":\"generateImage\",\"arguments\":\"{\\\"arg0\\\": \\\"A cute cartoon-style kitten sitting at a wooden desk, wearing small round glasses, attentively reading an open book with illustrated pages. Warm lighting, cozy library background with soft shadows and a few floating dust particles.\\\"}\"}],\"type\":\"AI\"},{\"id\":\"call_c6ff4ecf1ac149b9813bf8\",\"toolName\":\"generateImage\",\"text\":\"https://dashscope-result-wlcb-acdr-1.oss-cn-wulanchabu-acdr-1.aliyuncs.com/1d/6a/20260309/77c9ad12/ddfdecb8-e488-407d-b43c-c2fbc84ea5bc2842430215.png?Expires=1773153436&OSSAccessKeyId=LTAI5tKPD3TMqf2Lna1fASuh&Signature=DyM%2Fml7GKzMi6D7njRuoq2DsSpY%3D\",\"type\":\"TOOL_EXECUTION_RESULT\"},{\"text\":\"已为您生成小猫看书的图片！ 🐱📖  \\n图片链接如下（可直接点击或复制到浏览器查看）：\\n\\n[👉 小猫在看书的温馨插画](https://dashscope-result-wlcb-acdr-1.oss-cn-wulanchabu-acdr-1.aliyuncs.com/1d/6a/20260309/77c9ad12/ddfdecb8-e488-407d-b43c-c2fbc84ea5bc2842430215.png?Expires=1773153436&OSSAccessKeyId=LTAI5tKPD3TMqf2Lna1fASuh&Signature=DyM%2Fml7GKzMi6D7njRuoq2DsSpY%3D)\\n\\n这是一幅温馨可爱的卡通风格插画：小猫戴着圆圆的小眼镜，专注地阅读一本打开的图画书，背景是暖光笼罩的 cozy 图书馆，充满童趣与宁静感 ✨\\n\\n如需调整风格（例如写实风、水彩风、添加文字、更换场景等），欢迎随时告诉我，我可以为您重新生成！\",\"toolExecutionRequests\":[],\"type\":\"AI\"}]"}
2026-03-09T22:37:24.433+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : 🔍 偏移量检查结果 | Status: 1 | Message: allow
2026-03-09T22:37:24.433+08:00  INFO 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.k.aspect.KafkaOffsetAspect           : ✅ 允许消费 | Type: allow
2026-03-09T22:37:24.510+08:00 ERROR 9060 --- [share-thing-ai-impl] [ntainer#0-0-C-1] c.z.b.b.Listener.ChatHistoryConsumer     : Failed to consume chat history message

java.lang.NullPointerException: Cannot invoke "String.equals(Object)" because the return value of "com.zhangzc.blog.blogai.Store.MongoChatMemoryStore.getText(dev.langchain4j.data.message.ChatMessage)" is null
	at com.zhangzc.blog.blogai.Store.MongoChatMemoryStore.updateMessages(MongoChatMemoryStore.java:101) ~[classes/:na]
	at com.zhangzc.blog.blogai.Listener.ChatHistoryConsumer.consumeChatHistory(ChatHistoryConsumer.java:69) ~[classes/:na]
	at jdk.internal.reflect.GeneratedMethodAccessor91.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:354) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed(MethodInvocationProceedingJoinPoint.java:89) ~[spring-aop-6.1.8.jar:6.1.8]
	at com.zhangzc.kafkaspringbootstart.aspect.KafkaOffsetAspect.around(KafkaOffsetAspect.java:94) ~[classes/:na]
	at jdk.internal.reflect.GeneratedMethodAccessor102.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:637) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:627) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.aspectj.AspectJAroundAdvice.invoke(AspectJAroundAdvice.java:71) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:173) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768) ~[spring-aop-6.1.8.jar:6.1.8]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:720) ~[spring-aop-6.1.8.jar:6.1.8]
	at com.zhangzc.blog.blogai.Listener.ChatHistoryConsumer$$SpringCGLIB$$0.consumeChatHistory(<generated>) ~[classes/:na]
	at jdk.internal.reflect.GeneratedMethodAccessor91.invoke(Unknown Source) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:169) ~[spring-messaging-6.1.8.jar:6.1.8]
	at org.springframework.kafka.listener.adapter.KotlinAwareInvocableHandlerMethod.doInvoke(KotlinAwareInvocableHandlerMethod.java:45) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:119) ~[spring-messaging-6.1.8.jar:6.1.8]
	at org.springframework.kafka.listener.adapter.HandlerAdapter.invoke(HandlerAdapter.java:70) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:420) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.MessagingMessageListenerAdapter.invoke(MessagingMessageListenerAdapter.java:384) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter.onMessage(RecordMessagingMessageListenerAdapter.java:85) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter.onMessage(RecordMessagingMessageListenerAdapter.java:50) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeOnMessage(KafkaMessageListenerContainer.java:2800) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeOnMessage(KafkaMessageListenerContainer.java:2778) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.lambda$doInvokeRecordListener$53(KafkaMessageListenerContainer.java:2701) ~[spring-kafka-3.2.0.jar:3.2.0]
	at io.micrometer.observation.Observation.observe(Observation.java:565) ~[micrometer-observation-1.13.0.jar:1.13.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeRecordListener(KafkaMessageListenerContainer.java:2699) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeWithRecords(KafkaMessageListenerContainer.java:2541) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeRecordListener(KafkaMessageListenerContainer.java:2430) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeListener(KafkaMessageListenerContainer.java:2085) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeIfHaveRecords(KafkaMessageListenerContainer.java:1461) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.pollAndInvoke(KafkaMessageListenerContainer.java:1426) ~[spring-kafka-3.2.0.jar:3.2.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.run(KafkaMessageListenerContainer.java:1296) ~[spring-kafka-3.2.0.jar:3.2.0]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run$$$capture(CompletableFuture.java:1804) ~[na:na]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run(CompletableFuture.java) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:840) ~[na:na]