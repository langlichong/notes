# Cursor-Based Pagination (游标分页模式)

通过记录上一页最后一条记录的唯一标识（Cursor），并以此作为下一页查询的起点，从而避免深分页场景下 `OFFSET` 带来的性能灾难，实现稳定、高效的大数据集分页查询。

## 1. 痛点：OFFSET 深分页的"性能悬崖"

**传统分页方式 (OFFSET/LIMIT)**：
```sql
-- 第 1 页
SELECT * FROM posts ORDER BY id LIMIT 20 OFFSET 0;

-- 第 2 页
SELECT * FROM posts ORDER BY id LIMIT 20 OFFSET 20;

-- 第 100 万页 ❌
SELECT * FROM posts ORDER BY id LIMIT 20 OFFSET 20000000;
```

**性能分析**：
*   **执行逻辑**：数据库必须先扫描并**跳过** 2000 万行，然后才返回 20 行。
*   **耗时**：从第 1 页的 10ms 暴涨到第 100 万页的 **30 秒**。
*   **CPU 爆表**：如果 100 个用户同时翻到深页，数据库直接卡死。

**问题本质**：OFFSET 不走索引优化。无论翻到多深，都要从头开始数。

---

## 2. 解决方案：基于游标的"接力查询"

### 核心思想
不再告诉数据库"跳过多少行"，而是告诉它"从哪个 ID 开始查"。

### 查询流程
```sql
-- 第 1 页（初始查询）
SELECT * FROM posts 
ORDER BY id 
LIMIT 20;
-- 返回：id = 1, 2, ..., 20
-- 最后一条的 ID: 20

-- 第 2 页（带游标查询）
SELECT * FROM posts 
WHERE id > 20      -- 游标：上一页最后的 ID
ORDER BY id 
LIMIT 20;
-- 返回：id = 21, 22, ..., 40

-- 第 100 万页（依然很快！）
SELECT * FROM posts 
WHERE id > 上一页最后的ID
ORDER BY id 
LIMIT 20;
```

**性能分析**：
*   **执行计划**：`WHERE id > 20` 直接走主键索引，定位到第 21 行。
*   **耗时**：无论翻到多少页，始终是 **10-20ms**（常数时间）。
*   **无性能悬崖**：第 1 页和第 100 万页速度一样快。

---

## 3. 实现策略

### API 设计
**请求参数**：
```json
GET /api/posts?limit=20&cursor=MjA=  // cursor 是加密后的 "20"
```

**响应格式**：
```json
{
  "data": [
    {"id": 21, "title": "..."},
    ...
    {"id": 40, "title": "..."}
  ],
  "pagination": {
    "next_cursor": "NDA=",       // Base64("40")
    "has_more": true
  }
}
```

### 后端实现
```java
@GetMapping("/posts")
public Page<Post> getPosts(
    @RequestParam(required = false) String cursor,
    @RequestParam(defaultValue = "20") int limit
) {
    Long lastId = cursor != null ? decodeCursor(cursor) : 0L;
    
    // 使用游标查询
    List<Post> posts = postRepository.findByIdGreaterThanOrderById(lastId, limit);
    
    // 生成下一页游标
    String nextCursor = posts.isEmpty() ? null : encodeCursor(posts.get(posts.size() - 1).getId());
    
    return new Page(posts, nextCursor, posts.size() == limit);
}

// JPA Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.id > :lastId ORDER BY p.id")
    List<Post> findByIdGreaterThanOrderById(@Param("lastId") Long lastId, Pageable pageable);
}
```

---

## 4. 复杂场景：多字段排序

**需求**：按创建时间倒序排列（最新的在前）。

**挑战**：如果用 `WHERE created_at < ?`，可能有多条记录的时间戳相同，导致翻页时记录重复或遗漏。

**解决方案**：复合游标（时间 + 唯一ID）。
```sql
-- 游标包含两个字段：(created_at, id)
SELECT * FROM posts 
WHERE (created_at, id) < (?, ?)  -- 复合条件
ORDER BY created_at DESC, id DESC 
LIMIT 20;
```

**API 示例**：
```json
GET /api/posts?limit=20&cursor=eyJjcmVhdGVkX2F0IjoiMjAyMy0xMi0yNSIsImlkIjo0MH0=
// Base64 解码后：{"created_at": "2023-12-25", "id": 40}
```

---

## 5. 游标加密与安全

**为什么要加密游标？**
1.  **防止篡改**：用户手动修改 Cursor 可能跳过付费内容或访问未授权数据。
2.  **隐藏内部结构**：不暴露数据库 ID 增长规律。

**加密方案**：
```java
// 简单方案：Base64 (不安全，仅防肉眼识别)
String encodeCursor(Long id) {
    return Base64.getEncoder().encodeToString(id.toString().getBytes());
}

// 安全方案：AES 加密
String encodeCursor(Long id) {
    return AES.encrypt(id.toString(), SECRET_KEY);
}
```

---

## 6. 关键限制

### 限制 A: 无法跳页
*   **问题**：用户不能直接跳到"第 100 页"，只能"下一页、下一页..."。
*   **解决方案**：如果业务必须支持跳页，使用混合方案：
    *   前 10 页：使用 OFFSET（性能可接受）。
    *   10 页后：强制使用游标分页。

### 限制 B: 数据动态变化
*   **场景**：用户在翻第 2 页时，第 1 页有新数据插入。可能导致重复或遗漏。
*   **应对**：
    *   **明确告知**：提示"内容实时更新，可能有重复"。
    *   **快照查询**：记录用户查询时的时间戳，只查询该时间点之前的数据（牺牲实时性）。

---

## 7. 与 OFFSET 分页的对比

| 对比维度     | OFFSET 分页          | Cursor 分页                 |
| :----------- | :------------------- | :-------------------------- |
| 性能         | 深分页极慢（O(N)）   | 恒定（O(1)）                |
| 跳页能力     | 支持                 | 不支持                      |
| 实现复杂度   | 简单                 | 中等                        |
| 数据变化敏感 | 每次查询独立，不敏感 | 依赖上次结果，敏感          |
| 适用场景     | 数据量小（< 10万行） | 无限滚动、Feed 流、大数据集 |

---

## 8. 适用场景

*   **社交媒体 Feed 流**：微博、朋友圈、抖音视频列表。
*   **电商商品列表**：搜索结果、分类浏览。
*   **日志查询系统**：ELK、Splunk 日志翻页。
*   **GitHub / GitLab**：Issue、Pull Request 列表。
*   **移动端无限滚动**：下拉加载更多。

## 9. 总结
Cursor Pagination 是大数据集分页的 **"索引友好型"** 解决方案。
*   **信条**：不要跳，要追。
*   **心法**：用"接力赛"取代"全程跑"，用索引查找取代顺序扫描。
