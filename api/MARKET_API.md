# 二手交易市场 API 接口文档

## 1. 基础说明

- **Base URL**: `/api`
- **认证方式**: Bearer Token
  - 在请求头中添加 `Authorization: Bearer <your_token>`
- **数据格式**: JSON

---

## 2. 接口列表

### 2.1 获取商品列表

获取二手商品列表，支持分页、分类筛选和关键词搜索。

- **URL**: `/market/items`
- **Method**: `GET`
- **权限**: 公开 (Public) / 登录用户 (User)

**请求参数 (Query Parameters)**

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| `page` | Integer | 否 | 1 | 页码 |
| `pageSize` | Integer | 否 | 20 | 每页数量 |
| `category` | String | 否 | `all` | 分类筛选。可选值: `digital`(数码), `furniture`(家居), `clothing`(服饰), `game`(游戏), `camera`(摄影), `other`(其他) |
| `keyword` | String | 否 | - | 搜索关键词 |

**响应示例**

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "title": "iPhone 14 Pro Max 256GB 暗夜紫",
        "price": 6500,
        "originalPrice": 8999,
        "cover": "https://images.unsplash.com/photo-1678652197831-2d180705cd2c?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
        "seller": {
          "id": 101,
          "name": "数码发烧友",
          "avatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix"
        },
        "location": "北京",
        "likes": 12,
        "views": 345,
        "createTime": "2023-10-25T10:00:00Z"
      }
    ],
    "total": 100,
    "hasMore": true
  }
}
```

---

### 2.2 获取商品详情

获取单个商品的详细信息。

- **URL**: `/market/items/:id`
- **Method**: `GET`
- **权限**: 公开 (Public)

**路径参数 (Path Parameters)**

| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `id` | Integer | 商品 ID |

**响应示例**

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "iPhone 14 Pro Max 256GB 暗夜紫",
    "price": 6500,
    "originalPrice": 8999,
    "description": "换新手机了，出闲置。成色99新，电池健康95%，无拆无修，箱说全。",
    "images": [
      "https://images.unsplash.com/photo-1678652197831-2d180705cd2c?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
      "https://images.unsplash.com/photo-detail-2.jpg"
    ],
    "category": "digital",
    "location": "北京",
    "seller": {
      "id": 101,
      "name": "数码发烧友",
      "avatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix",
      "rating": 4.9
    },
    "createTime": "2023-10-25T10:00:00Z",
    "likes": 12,
    "views": 345,
    "status": "active" // active(在售), sold(已售), off_shelf(下架)
  }
}
```

---

### 2.3 发布商品

用户发布新的闲置商品。

- **URL**: `/market/items`
- **Method**: `POST`
- **权限**: 需要登录 (Authenticated)

**请求体 (Request Body)**

```json
{
  "title": "Sony WH-1000XM4 降噪耳机",
  "price": 1200,
  "originalPrice": 2299,
  "description": "音质超棒，降噪无敌。买来用的不多...",
  "images": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "category": "digital",
  "location": "广州"
}
```

**字段说明**

| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `title` | String | 是 | 商品标题 (5-50字符) |
| `price` | Number | 是 | 出售价格 |
| `originalPrice` | Number | 否 | 原价 |
| `description` | String | 是 | 商品详情描述 |
| `images` | Array<String> | 是 | 图片URL数组，至少1张 |
| `category` | String | 是 | 商品分类 |
| `location` | String | 是 | 发货地/自提地 |

**响应示例**

```json
{
  "success": true,
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 1005,
    "createTime": "2023-10-26T14:30:00Z"
  }
}
```

---

### 2.4 创建订单/购买

用户发起交易请求或直接购买。

- **URL**: `/market/orders`
- **Method**: `POST`
- **权限**: 需要登录 (Authenticated)

**请求体 (Request Body)**

```json
{
  "itemId": 1,
  "remark": "希望尽快发货，如果是同城可以面交吗？",
  "addressId": 5 // 收货地址ID (如果是邮寄)
}
```

**响应示例**

```json
{
  "success": true,
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "orderId": "ORD202310260001",
    "status": "pending_payment", // pending_payment(待支付), pending_shipment(待发货)
    "amount": 6500
  }
}
```

---

### 2.5 收藏/取消收藏商品

- **URL**: `/market/items/:id/like`
- **Method**: `POST`
- **权限**: 需要登录 (Authenticated)

**响应示例**

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "isLiked": true,
    "likes": 13
  }
}
```
