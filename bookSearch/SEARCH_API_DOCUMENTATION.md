# 搜索模块 API 文档

本文档描述了搜索模块相关接口，包括热门搜索、搜索历史等功能。
所有接口均使用 `POST` 方法。

## 1. 获取热门搜索列表

获取系统当前的热门搜索关键词。

*   **URL**: `/search/hot/list`
*   **Method**: `POST`
*   **Content-Type**: `application/json`

### 请求体 (Request Body)

支持分页查询。

```json
{
  "pageNo": 1,
  "pageSize": 10
}
```

### 响应体 (Response Body)

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      "美食",
      "旅行",
      "穿搭",
      "数码",
      "健身",
      "家居",
      "Vue3",
      "人工智能"
    ],
    "total": 100,
    "pageNo": 1,
    "pageSize": 10,
    "totalPage": 10
  }
}
```

---

## 2. 获取用户搜索历史

获取当前登录用户的搜索历史记录，支持分页。

*   **URL**: `/search/history/list`
*   **Method**: `POST`
*   **Content-Type**: `application/json`

### 请求体 (Request Body)

```json
{
  "pageNo": 1,
  "pageSize": 20,
  "userId": "12345"  // 可选，如果通过 Token 获取用户则不需要
}
```

### 响应体 (Response Body)

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      "Vue3 教程",
      "React vs Vue",
      "前端面试题"
    ],
    "total": 50,
    "pageNo": 1,
    "pageSize": 20,
    "totalPage": 3
  }
}
```

---

## 3. 添加搜索历史

用户进行搜索时调用，将搜索关键词保存到历史记录中。后端应处理去重（将已存在的词移到最前）。

*   **URL**: `/search/history/add`
*   **Method**: `POST`
*   **Content-Type**: `application/json`

### 请求体 (Request Body)

```json
{
  "keyword": "Spring Boot"
}
```

### 响应体 (Response Body)

```json
{
  "success": true,
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

---

## 4. 清空搜索历史

清空当前用户的搜索历史记录。

*   **URL**: `/search/history/clear`
*   **Method**: `POST`
*   **Content-Type**: `application/json`

### 请求体 (Request Body)

不需要请求体参数。**请勿发送空 JSON 对象 `{}`，直接不发送 Body 或 Body 为空。**

### 响应体 (Response Body)

```json
{
  "success": true,
  "code": 200,
  "message": "清空成功",
  "data": null
}
```

---

## 5. 删除单条搜索历史

删除用户指定的某一条搜索历史记录。

*   **URL**: `/search/history/delete`
*   **Method**: `POST`
*   **Content-Type**: `application/json`

### 请求体 (Request Body)

```json
{
  "keyword": "Spring Boot"
}
```

### 响应体 (Response Body)

```json
{
  "success": true,
  "code": 200,
  "message": "删除成功",
  "data": null
}
```
