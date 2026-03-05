// =========================================================================================
// MongoDB 初始化脚本
// 使用方法：
// 1. 使用 Mongo Compass 连接数据库
// 2. 复制下方代码到 "Mongosh (Beta)" 命令行工具中执行
// =========================================================================================

// 切换到目标数据库 (如果数据库名不同请修改)
usefake_book;

// =========================================================================================
// 1. 创建 chat_message 集合 (存储聊天记录)
// =========================================================================================
try {
    db.createCollection("chat_message");
} catch (e) {
    print("集合 chat_message 可能已存在，跳过创建");
}

// 索引 1: 核心查询索引 - 根据会话ID查询历史记录，按时间倒序
// 作用：打开聊天窗口时，快速拉取最近 N 条消息
db.chat_message.createIndex({ "conversationId": 1, "createTime": -1 }, { name: "idx_conv_time" });

// 索引 2: 辅助索引 - 消息状态查询 (可选，用于后台管理或统计)
// db.chat_message.createIndex({ "status": 1 }); 

print("chat_message 集合及索引创建完成");


// =========================================================================================
// 2. 创建 chat_conversation 集合 (存储会话列表)
// =========================================================================================
try {
    db.createCollection("chat_conversation");
} catch (e) {
    print("集合 chat_conversation 可能已存在，跳过创建");
}

// 索引 1: 唯一索引 - 确保同一个会话ID唯一
db.chat_conversation.createIndex({ "conversationId": 1 }, { unique: true, name: "uk_conversation_id" });

// 索引 2: 列表查询索引 - 查询某人的会话列表，按最后一条消息时间排序
// 注意：members 是数组，MongoDB 会自动对数组中的每个元素建立索引，支持 { members: "userId" } 查询
db.chat_conversation.createIndex({ "members": 1, "lastMessageTime": -1 }, { name: "idx_members_time" });

print("chat_conversation 集合及索引创建完成");

// =========================================================================================
// (可选) 3. 验证器 (Schema Validation) - 类似于 MySQL 的字段类型约束
// 如果需要强校验数据格式，可以执行以下命令更新集合配置
// =========================================================================================
/*
db.runCommand({
   collMod: "chat_message",
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: [ "conversationId", "fromUserId", "msgType", "content", "createTime" ],
         properties: {
            conversationId: { bsonType: "string" },
            fromUserId: { bsonType: "string" },
            msgType: { bsonType: "int" },
            content: { bsonType: "string" },
            createTime: { bsonType: "date" }
         }
      }
   }
});
*/
