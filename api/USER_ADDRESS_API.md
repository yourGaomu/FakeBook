# 用户地址管理接口文档

本文档描述了用户收货地址的增删改查接口。

## 1. 集合定义 (MongoDB)

建议在 `init_mongo_market.js` 中新增 `user_addresses` 集合。

```javascript
const UserAddressSchema = new Schema({
  userId: { type: Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  name: { type: String, required: true },       // 收货人姓名
  phone: { type: String, required: true },      // 手机号
  province: { type: String, required: true },   // 省
  city: { type: String, required: true },       // 市
  district: { type: String, required: true },   // 区/县
  detail: { type: String, required: true },     // 详细地址
  isDefault: { type: Boolean, default: false }, // 是否默认地址
  createdAt: { type: Date, default: Date.now }
});
```

---

## 2. API 接口列表

- **Base URL**: `/user/address`
- **权限**: 需要登录 (Authenticated)

### 2.1 获取地址列表

获取当前登录用户的所有收货地址。

- **URL**: `/user/address/list`
- **Method**: `GET`

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "addr_001",
      "name": "张三",
      "phone": "13800138000",
      "province": "北京市",
      "city": "北京市",
      "district": "朝阳区",
      "detail": "建国路88号",
      "isDefault": true
    }
  ]
}
```

### 2.2 添加地址

- **URL**: `/user/address/add`
- **Method**: `POST`

**请求体**

```json
{
  "name": "李四",
  "phone": "13900139000",
  "province": "上海市",
  "city": "上海市",
  "district": "浦东新区",
  "detail": "世纪大道1号",
  "isDefault": false
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "添加成功",
  "data": "addr_002" // 返回新地址ID
}
```

### 2.3 修改地址

- **URL**: `/user/address/update`
- **Method**: `POST`

**请求体**

```json
{
  "id": "addr_002",
  "name": "李四",
  "phone": "13900139000",
  "province": "上海市",
  "city": "上海市",
  "district": "黄浦区", // 修改了区
  "detail": "南京东路88号",
  "isDefault": true
}
```

### 2.4 删除地址

- **URL**: `/user/address/delete/{id}`
- **Method**: `POST` (或 DELETE)

**响应示例**

```json
{
  "code": 200,
  "message": "删除成功"
}
```
