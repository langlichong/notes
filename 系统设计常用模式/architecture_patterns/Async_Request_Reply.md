# Async Request-Reply (异步请求应答模式)

针对耗时极长的后台任务（如视频转码、大数据分析、复杂报表生成），通过将同步阻塞的 HTTP 请求拆分为"立即响应 + 异步处理 + 轮询/推送结果"的三段式流程，避免客户端长时间占用连接资源和超时问题。

## 1. 痛点：长任务阻塞连接的"死等"

在传统的同步 HTTP 请求中：

**灾难场景**：
```
Client: POST /api/video/transcode (上传 1GB 视频)
Server: 开始转码... (需要 20 分钟)
Client: [在这里傻等 20 分钟] ...
```

**后果**：
1.  **连接超时**：大部分 HTTP 客户端（浏览器、Nginx）的超时时间是 30 秒到 2 分钟。20 分钟早就超时报错了。
2.  **资源浪费**：1 个 HTTP 连接绑定 1 个服务器线程。这个线程在 20 分钟内什么都不干，只是在等待。如果 100 个用户同时上传视频，100 个线程全被占用。
3.  **重试风暴**：用户看到超时后，会点"重新上传"。结果服务器其实在处理第一次请求，又来了第二次，资源被双倍消耗。

**本质问题**：HTTP 是为 **"瞬时请求"** 设计的（几毫秒到几秒）。它不适合几十分钟的长任务。

---

## 2. 解决方案：拆分为三段式流程

### 标准流程
```
[阶段1: 提交任务]
Client -> POST /api/jobs (提交任务)
Server -> 立即返回: {job_id: "abc-123", status: "processing"}

[阶段2: 后台处理]
Server 异步处理任务（可能需要 20 分钟）

[阶段3: 获取结果]
Client -> GET /api/jobs/abc-123 (轮询状态)
Server -> {job_id: "abc-123", status: "completed", result_url: "https://..."}
```

---

## 3. 实现策略

### 方式 A: 轮询 (Polling) —— 最简单
```java
// 提交任务
@PostMapping("/jobs")
public ResponseEntity<?> submitJob(@RequestBody JobRequest req) {
    String jobId = UUID.randomUUID().toString();
    
    // 放入异步队列
    taskQueue.submit(jobId, req);
    
    // 立即返回
    return ResponseEntity.accepted()
        .body(Map.of("job_id", jobId, "status", "processing"));
}

// 查询状态
@GetMapping("/jobs/{id}")
public ResponseEntity<?> getJobStatus(@PathVariable String id) {
    Job job = jobRepository.findById(id);
    return ResponseEntity.ok(job);
}
```

**前端代码**：
```javascript
// 1. 提交任务
const resp = await fetch('/api/jobs', {method: 'POST', body: ...});
const {job_id} = await resp.json();

// 2. 轮询状态
const timer = setInterval(async () => {
    const status = await fetch(`/api/jobs/${job_id}`).then(r => r.json());
    if (status.status === 'completed') {
        clearInterval(timer);
        alert('任务完成！下载地址：' + status.result_url);
    }
}, 3000); // 每 3 秒查一次
```

### 方式 B: WebSocket / SSE推送 (Server Push) —— 更优雅
服务器处理完成后，主动推送消息给客户端，无需轮询。

### 方式 C: Webhook 回调 (Callback)
客户端在提交任务时，给一个回调 URL：
```json
{
  "callback_url": "https://my-app.com/webhook/job-completed"
}
```
服务器处理完后，调用这个 URL 通知客户端。

---

## 4. 任务状态机设计

一个标准的异步任务应该有以下状态：
*   `PENDING`: 已提交，排队中。
*   `PROCESSING`: 正在处理。
*   `COMPLETED`: 成功完成。
*   `FAILED`: 失败（附带错误信息）。
*   `CANCELLED`: 用户取消。

---

## 5. 关键挑战

*   **任务结果存储**：处理完的结果（比如转码后的视频）要存在哪？通常存 S3/OSS，状态表里只存 URL。
*   **任务过期清理**：如果用户提交后再也不来查询了，任务结果要保留多久？建议设置 TTL（如 7 天后自动删除）。
*   **轮询频率控制**：不能让前端每 100ms 轮询一次。建议最少间隔 3 秒，并采用"指数退避"（越等越慢）。

---

## 6. 适用场景

*   **视频/音频转码**。
*   **大数据报表生成**（查询 1 亿行数据聚合）。
*   **机器学习模型训练**。
*   **第三方支付回调**（调用银行接口扣款，银行异步返回结果）。

## 7. 总结
Async Request-Reply 是将 **"傻等"** 变成 **"智等"** 的艺术。
*   **信条**：长任务不应霸占连接，应放入后台异步处理。
*   **心法**：快速响应用户"已收到"，慢慢处理任务，最后通知"已完成"。
