# 二手交易市场 MongoDB 数据库设计文档

本文档详细描述了二手交易市场模块的 MongoDB 数据库 Schema 设计、索引策略及数据关联方案。

## 1. 设计原则

- **读多写少**: 商品浏览频率远高于发布频率，因此在商品文档中适当冗余卖家信息 (`seller`)，减少 `$lookup` 联表查询，提高列表页加载速度。
- **文档结构**: 利用 MongoDB 的文档嵌套特性，将图片列表 (`images`) 直接嵌入商品文档。
- **扩展性**: 预留 `tags` 和 `attributes` 字段，以支持不同品类商品的特殊属性。

---

## 2. 集合定义 (Collections)

### 2.1 商品集合 (`market_items`)

存储所有二手商品的核心信息。

```javascript
const MarketItemSchema = new Schema({
  // 基本信息
  title: { type: String, required: true, index: 'text' }, // 支持文本搜索
  description: { type: String, required: true, index: 'text' }, // 支持文本搜索
  price: { type: Number, required: true, min: 0 },
  originalPrice: { type: Number, min: 0 },
  
  // 图片数组 (首张为封面)
  images: [{ type: String }], 
  
  // 分类与标签
  category: { 
    type: String, 
    enum: ['digital', 'furniture', 'clothing', 'game', 'camera', 'other'],
    required: true,
    index: true 
  },
  tags: [{ type: String }], // 例如: ["99新", "急出", "包邮"]
  
  // 地理位置
  location: { type: String, default: '未知' },
  // 如果需要基于坐标的附近搜索，可以使用 GeoJSON
  // coordinates: { type: [Number], index: '2dsphere' },

  // 卖家信息 (冗余存储，避免列表页联表查询)
  seller: {
    _id: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    nickname: { type: String },
    avatar: { type: String },
    creditScore: { type: Number, default: 100 } // 卖家信用分
  },

  // 状态管理
  status: { 
    type: String, 
    enum: ['active', 'sold', 'off_shelf', 'deleted'], 
    default: 'active',
    index: true 
  },
  
  // 统计数据
  views: { type: Number, default: 0 },
  likes: { type: Number, default: 0 },
  
  // 时间戳
  createdAt: { type: Date, default: Date.now, index: -1 }, // 按时间倒序
  updatedAt: { type: Date, default: Date.now }
});

// 复合索引建议
// 场景: "获取数码分类下最新的商品"
MarketItemSchema.index({ category: 1, createdAt: -1 });
// 场景: "获取某个卖家的所有在售商品"
MarketItemSchema.index({ 'seller._id': 1, status: 1 });
```

### 2.2 订单集合 (`market_orders`)

存储交易记录。

```javascript
const MarketOrderSchema = new Schema({
  // 商品引用
  item: {
    _id: { type: Schema.Types.ObjectId, ref: 'MarketItem', required: true },
    title: { type: String }, // 冗余商品标题，防止商品删除后订单无法显示
    image: { type: String }, // 冗余商品封面
    price: { type: Number }  // 交易时的价格 (快照)
  },

  // 买卖双方
  buyerId: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  sellerId: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },

  // 交易金额
  totalAmount: { type: Number, required: true },
  
  // 订单状态
  status: {
    type: String,
    enum: [
      'pending_payment',  // 待支付
      'paid',             // 已支付/待发货
      'shipped',          // 已发货/待收货
      'completed',        // 交易完成
      'cancelled',        // 已取消
      'refunded'          // 已退款
    ],
    default: 'pending_payment',
    index: true
  },

  // 收货信息
  shippingAddress: {
    name: String,
    phone: String,
    province: String,
    city: String,
    district: String,
    detail: String
  },

  // 备注
  remark: { type: String },

  // 时间戳
  createdAt: { type: Date, default: Date.now },
  paidAt: { type: Date },
  shippedAt: { type: Date },
  completedAt: { type: Date }
});
```

### 2.3 用户收藏集合 (`market_favorites`)

用户收藏/想要商品的记录。也可以直接放在 `User` 表中，但为了避免文档无限增长，建议单独建表。

```javascript
const MarketFavoriteSchema = new Schema({
  userId: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  itemId: { type: Schema.Types.ObjectId, ref: 'MarketItem', required: true },
  createdAt: { type: Date, default: Date.now }
});

// 复合唯一索引，防止重复收藏
MarketFavoriteSchema.index({ userId: 1, itemId: 1 }, { unique: true });
```

### 2.4 留言/评论集合 (`market_comments`)

商品详情页下的提问或留言。

```javascript
const MarketCommentSchema = new Schema({
  itemId: { type: Schema.Types.ObjectId, ref: 'MarketItem', required: true, index: true },
  userId: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  
  // 发言人信息冗余
  userNickname: String,
  userAvatar: String,

  content: { type: String, required: true },
  
  // 支持回复嵌套
  parentId: { type: Schema.Types.ObjectId, ref: 'MarketComment', default: null },
  replyToUserId: { type: Schema.Types.ObjectId, ref: 'User' },

  createdAt: { type: Date, default: Date.now }
});
```

---

## 3. 索引策略总结

| 集合 | 索引字段 | 类型 | 用途 |
| :--- | :--- | :--- | :--- |
| `market_items` | `title`, `description` | Text | 全文搜索 (关键词查询) |
| `market_items` | `category`, `createdAt` | Compound | 分类筛选 + 按时间排序 |
| `market_items` | `status`, `createdAt` | Compound | 首页推荐流 (排除已下架商品) |
| `market_orders` | `buyerId`, `createdAt` | Compound | "我的购买" 列表 |
| `market_orders` | `sellerId`, `createdAt` | Compound | "我的售出" 列表 |
| `market_favorites` | `userId`, `itemId` | Unique | 快速判断是否收藏，防止重复 |

---

## 4. 数据一致性维护

由于采用了 **反范式设计 (Denormalization)**，即在 `market_items` 中冗余了 `seller` 信息，当用户信息（如头像、昵称）发生变更时，需要考虑一致性问题：

1.  **方案 A (推荐)**: 接受短暂的不一致。用户修改头像后，历史发布的商品仍显示旧头像，只有新发布的商品显示新头像。
2.  **方案 B**: 后台异步任务。用户修改资料后，触发消息队列，后台 worker 批量更新该用户所有商品的 `seller` 字段。
    ```javascript
    // 伪代码
    db.market_items.updateMany(
      { 'seller._id': userId },
      { $set: { 'seller.nickname': newNickname, 'seller.avatar': newAvatar } }
    );
    ```

## 5. 示例数据 (JSON)

### 商品文档示例
```json
{
  "_id": "651a...001",
  "title": "iPhone 14 Pro Max 256GB 暗夜紫",
  "description": "换新手机了，出闲置。成色99新，电池健康95%...",
  "price": 6500,
  "originalPrice": 8999,
  "images": [
    "https://oss.example.com/img1.jpg",
    "https://oss.example.com/img2.jpg"
  ],
  "category": "digital",
  "tags": ["自用", "箱说全"],
  "location": "北京",
  "seller": {
    "_id": "user_101",
    "nickname": "数码发烧友",
    "avatar": "https://oss.example.com/avatar1.jpg"
  },
  "status": "active",
  "views": 345,
  "likes": 12,
  "createdAt": "2023-10-25T10:00:00Z"
}
```
