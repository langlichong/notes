# Valet Key (代客泊车钥匙模式)

在需要让客户端（如前端、移动端）直接访问云存储资源（如 S3、OSS）时，通过生成**临时的、权限受限的、自动过期的访问凭证**，而非暴露永久的主密钥，从而在保证功能的同时最大限度地降低安全风险。

## 1. 痛点：永久密钥泄露的"核弹级威胁"

在云存储场景中，最危险的做法是把永久访问密钥给客户端：

**灾难场景**：
```javascript
// 前端代码（所有人都能看到）
const OSS = require('ali-oss');
const client = new OSS({
    accessKeyId: 'LTAI4Fxxx...永久密钥',      // ❌ 大错特错！
    accessKeySecret: 'xxxxxxxxxxxxx',        // ❌ 已经泄露
    bucket: 'my-company-files'
});

// 用户可以直接上传文件
client.put('avatar.jpg', file);
```

**后果**：
1.  **任何人都能拿到这个密钥**（通过浏览器开发者工具、反编译 App）。
2.  **黑客可以干任何事**：
    *   删除整个 Bucket 里的所有文件（包括公司核心数据）。
    *   上传恶意文件、病毒、违法内容。
    *   产生天价流量费用（上传几 TB 垃圾数据）。
3.  **密钥一旦泄露，除非重新生成**，否则永久有效，黑客可以持续攻击。

**本质问题**：永久密钥的权限是"上帝模式"，不应该交给不受信任的客户端。

---

## 2. 解决方案：临时钥匙 + 最小权限

我们不给客户端"万能钥匙"，而是给一个**只能开特定房间、且 5 分钟后就失效**的临时卡片。

### 运行流程
```
[前端请求上传头像]
    |
    v
[后端] 调用 OSS STS (Security Token Service) API
    -> 生成临时凭证:
       {
         accessKeyId: "STS.temp...",  // 临时 Key
         accessKeySecret: "temp...",
         securityToken: "CAI...",
         expiration: "2023-12-25T10:15:00Z"  // 5 分钟后过期
       }
    |
    v
[返回给前端]
    |
    v
[前端用临时凭证直接上传到 OSS]
    |
    v
[5 分钟后，临时凭证自动失效，黑客拿到也没用]
```

---

## 3. 实现策略

### 后端生成临时凭证 (Java + 阿里云 OSS)
```java
@RestController
public class UploadController {
    
    @GetMapping("/upload/token")
    public Map<String, String> getUploadToken() {
        // 1. 调用阿里云 STS 服务
        STSAssumeRoleSessionCredentialsProvider provider = 
            CredentialsProviderFactory.newSTSAssumeRoleSessionCredentialsProvider(
                "acs:ram::123456:role/OSSUploadRole",  // RAM 角色
                null
            );
        
        Credentials credentials = provider.getCredentials();
        
        // 2. 返回临时凭证
        return Map.of(
            "accessKeyId", credentials.getAccessKeyId(),
            "accessKeySecret", credentials.getAccessKeySecret(),
            "securityToken", credentials.getSecurityToken(),
            "expiration", credentials.getExpiration().toString()
        );
    }
}
```

### 前端使用临时凭证上传
```javascript
async function uploadAvatar(file) {
    // 1. 从后端获取临时凭证
    const token = await fetch('/upload/token').then(r => r.json());
    
    // 2. 用临时凭证初始化 OSS Client
    const client = new OSS({
        accessKeyId: token.accessKeyId,
        accessKeySecret: token.accessKeySecret,
        stsToken: token.securityToken,
        bucket: 'avatars',
        region: 'oss-cn-hangzhou'
    });
    
    // 3. 上传文件
    await client.put(`users/${userId}/avatar.jpg`, file);
}
```

---

## 4. 权限精细化控制

临时凭证不仅限制了时间，还能限制**权限范围**：

### 示例：只允许上传到特定前缀
```json
{
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["oss:PutObject"],
      "Resource": ["acs:oss:*:*:my-bucket/uploads/user-123/*"]
    }
  ]
}
```

**结果**：
*   用户只能上传到 `uploads/user-123/` 目录。
*   不能删除文件（没有 `DeleteObject` 权限）。
*   不能访问其他用户的目录。

---

## 5. 关键优势

1.  **零信任架构**：即使黑客拿到了临时凭证，也只能在极短时间内做极有限的事。
2.  **审计追踪**：每次生成临时凭证都可以记录日志（谁在什么时候申请了什么权限）。
3.  **按需授权**：不同的场景可以生成不同权限的凭证（上传头像 vs 下载报表）。

---

## 6. 注意事项与挑战

*   **时钟同步**：临时凭证的过期时间是基于服务器时钟的。如果客户端时钟不准（慢了 10 分钟），可能会导致"凭证未过期但实际已失效"的误判。
*   **频繁申请开销**：如果用户需要上传 100 个文件，是申请 1 次凭证还是 100 次？
    *   **建议**：申请 1 次，有效期设为 15 分钟，批量上传。

---

## 7. 适用场景

*   **用户上传头像、文件**。
*   **移动端直传视频到云存储**（避免流量经过应用服务器）。
*   **前端生成报表后直接下载 OSS 文件**。
*   **第三方集成**：给合作伙伴临时访问权限，无需共享主密钥。

## 8. 总结
Valet Key 是云安全的 **"黄金法则"**。
*   **信条**：永远不要把主钥匙交给代客泊车员。
*   **心法**：权限最小化 + 时间最短化 = 风险最小化。
