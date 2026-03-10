
### 2.6 获取评论列表

获取商品下的评论列表。

- **URL**: `/market/items/comments/list/{itemId}`
- **Method**: `POST`
- **权限**: 公开 (Public)

**路径参数**

| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `itemId` | String | 商品 ID |

**响应示例**

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "comment_001",
      "itemId": "item_123",
      "userId": "user_456",
      "userName": "用户A",
      "userAvatar": "https://...",
      "content": "请问还在吗？可以小刀吗？",
      "createTime": "2023-10-27T10:00:00Z",
      "parentId": null
    }
  ]
}
```

### 2.7 发布评论

在商品下发布新的评论。

- **URL**: `/market/items/comments/add`
- **Method**: `POST`
- **权限**: 需要登录 (Authenticated)

**请求体 (Request Body)**

```json
{
  "itemId": "item_123",
  "content": "诚心想要，私聊",
  "parentId": "comment_001" // 可选，如果是回复某条评论
}
```

**响应示例**

```json
{
  "success": true,
  "code": 200,
  "message": "评论成功",
  "data": {
    "id": "comment_002",
    "createTime": "2023-10-27T10:05:00Z"
  }
}
```
