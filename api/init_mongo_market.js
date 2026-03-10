// init_mongo_market.js

// =========================================================
// MongoDB 初始化脚本 - 二手交易市场模块
// 使用方法:
// 1. 确保已安装 MongoDB 和 Mongosh
// 2. 在终端运行: mongosh < init_mongo_market.js
//    或者在 MongoDB Compass 的 Mongosh 窗口中粘贴以下内容运行
// =========================================================

// 切换到 fakeGoods_db 数据库 (如果不存在会自动创建)
db = db.getSiblingDB('fakeGoods_db');

print("🚀 开始初始化数据库: fakeGoods_db");

// =========================================================
// 1. 商品集合 (market_items)
// =========================================================
print("正在处理集合: market_items...");

// 创建集合 (如果不存在)
try {
    db.createCollection("market_items");
} catch (e) {
    print("  - 集合 market_items 已存在，跳过创建");
}

// 创建索引
print("  - 创建索引中...");

// 1. 全文索引: 用于搜索 title 和 description
//    使用方法: db.market_items.find({ $text: { $search: "iPhone" } })
db.market_items.createIndex({ "title": "text", "description": "text" }, { name: "TextIndex" });

// 2. 复合索引: 分类筛选 + 按时间倒序
//    使用方法: db.market_items.find({ category: "digital" }).sort({ createdAt: -1 })
db.market_items.createIndex({ "category": 1, "createdAt": -1 }, { name: "CategoryTimeIndex" });

// 3. 复合索引: 卖家商品查询 + 状态过滤
//    使用方法: db.market_items.find({ "seller._id": ObjectId("..."), status: "active" })
db.market_items.createIndex({ "seller._id": 1, "status": 1 }, { name: "SellerStatusIndex" });

// 4. 单字段索引: 价格排序
db.market_items.createIndex({ "price": 1 }, { name: "PriceIndex" });


// =========================================================
// 2. 订单集合 (market_orders)
// =========================================================
print("正在处理集合: market_orders...");

try {
    db.createCollection("market_orders");
} catch (e) {
    print("  - 集合 market_orders 已存在，跳过创建");
}

print("  - 创建索引中...");

// 1. 买家查询自己的订单
db.market_orders.createIndex({ "buyerId": 1, "createdAt": -1 }, { name: "BuyerOrderIndex" });

// 2. 卖家查询收到的订单
db.market_orders.createIndex({ "sellerId": 1, "createdAt": -1 }, { name: "SellerOrderIndex" });

// 3. 状态查询
db.market_orders.createIndex({ "status": 1 }, { name: "OrderStatusIndex" });


// =========================================================
// 3. 收藏集合 (market_favorites)
// =========================================================
print("正在处理集合: market_favorites...");

try {
    db.createCollection("market_favorites");
} catch (e) {
    print("  - 集合 market_favorites 已存在，跳过创建");
}

// 唯一索引: 防止用户重复收藏同一个商品
db.market_favorites.createIndex({ "userId": 1, "itemId": 1 }, { unique: true, name: "UniqueFavoriteIndex" });


// =========================================================
// 4. 插入初始化测试数据
// =========================================================
print("📝 正在插入测试数据...");

// 模拟的卖家 ID
var sellerId1 = new ObjectId();
var sellerId2 = new ObjectId();

var items = [
  {
    title: "iPhone 14 Pro Max 256GB 暗夜紫",
    description: "换新手机了，出闲置。成色99新，电池健康95%，无拆无修，箱说全。",
    price: 6500,
    originalPrice: 8999,
    images: ["https://images.unsplash.com/photo-1678652197831-2d180705cd2c?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"],
    category: "digital",
    tags: ["自用", "急出"],
    location: "北京",
    seller: {
      _id: sellerId1,
      nickname: "数码发烧友",
      avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix",
      creditScore: 100
    },
    status: "active",
    views: 345,
    likes: 12,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    title: "宜家书桌，白色，九成新",
    description: "搬家带不走，低价出。自提，坐标朝阳区。",
    price: 150,
    originalPrice: 399,
    images: ["https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"],
    category: "furniture",
    location: "上海",
    seller: {
      _id: sellerId2,
      nickname: "爱生活的喵",
      avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Bella",
      creditScore: 98
    },
    status: "active",
    views: 120,
    likes: 5,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    title: "Sony WH-1000XM4 降噪耳机",
    description: "音质超棒，降噪无敌。买来用的不多，耳罩有点磨损，其他功能正常。",
    price: 1200,
    originalPrice: 2299,
    images: ["https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"],
    category: "digital",
    location: "广州",
    seller: {
      _id: sellerId1, // 同一个卖家
      nickname: "数码发烧友",
      avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix",
      creditScore: 100
    },
    status: "active",
    views: 560,
    likes: 28,
    createdAt: new Date(new Date().getTime() - 86400000), // 昨天
    updatedAt: new Date()
  }
];

try {
    // 简单去重：先清空测试数据（可选，防止重复插入）
    // db.market_items.deleteMany({ "seller.nickname": { $in: ["数码发烧友", "爱生活的喵"] } });
    
    db.market_items.insertMany(items);
    print("  - 成功插入 " + items.length + " 条商品数据");
} catch (e) {
    print("  - 插入数据失败: " + e);
}

print("✅ 数据库初始化完成！");
