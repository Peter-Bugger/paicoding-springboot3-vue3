# 产品需求文档（PRD）—— 私信功能

## 1. 文档信息

| 属性 | 值 |
|------|-----|
| 文档版本 | v1.0 |
| 创建日期 | 2026-05-08 |
| 产品名称 | 派编程社区私信系统 |
| 目标版本 | MVP 1.0 |

---

## 2. 用户故事（User Stories）

### US-01: 发起新会话并发送第一条消息

**作为** 社区注册用户
**我想要** 从另一个用户的个人主页或文章评论区找到"发私信"按钮，点击后进入私信对话界面并发送消息
**以便于** 我能够与对方进行一对一的私下交流

**验收条件：**

- [ ] AC-01.1: 在用户个人主页（`/user/:userId`）上，登录用户可以看到"发私信"按钮（条件：目标用户不是自己）
- [ ] AC-01.2: 在文章详情页评论区中，每个评论用户的头像旁有"发私信"入口（悬停显示）
- [ ] AC-01.3: 点击"发私信"后，跳转到私信对话页面，页面顶部展示对方头像、昵称
- [ ] AC-01.4: 页面底部有消息输入框和发送按钮
- [ ] AC-01.5: 输入消息后点击发送（或按 Enter 键），消息立即在对话区域显示
- [ ] AC-01.6: 对方实时收到 WebSocket 推送的新消息
- [ ] AC-01.7: 如果用户未登录，"发私信"按钮点击后弹出登录对话框

---

### US-02: 查看会话列表

**作为** 社区注册用户
**我想要** 在一个统一的"消息"页面看到我参与的所有私信会话列表
**以便于** 我能够快速找到并进入某个会话继续对话

**验收条件：**

- [ ] AC-02.1: 全局导航栏有"消息"入口（显示未读消息总数徽标）
- [ ] AC-02.2: 点击"消息"进入消息列表页面（路由：`/messages`）
- [ ] AC-02.3: 会话列表每个条目展示：对方头像、昵称、最后一条消息摘要（截断 30 字符）、最后消息时间（相对时间如"3分钟前"）
- [ ] AC-02.4: 有未读消息的会话显示未读计数红点/数字
- [ ] AC-02.5: 会话列表按最后消息时间倒序排列（最新消息的会话在最上面）
- [ ] AC-02.6: 点击某个会话，进入该会话的对话详情页面
- [ ] AC-02.7: 会话列表为空时，显示空状态提示"暂无消息"

---

### US-03: 在会话中收发消息

**作为** 社区注册用户
**我想要** 在会话详情页中查看历史消息、发送新消息，并且实时收到对方的新消息
**以便于** 我能够与对方进行流畅的即时对话

**验收条件：**

- [ ] AC-03.1: 会话详情页路由：`/messages/:conversationId`
- [ ] AC-03.2: 页面顶部固定显示对方头像、昵称（点击可跳转到对方个人主页）
- [ ] AC-03.3: 历史消息按时间正序排列（最早在上，最新在下），进入页面时自动滚动到底部
- [ ] AC-03.4: 支持上拉加载更多历史消息（分页，每页 20 条）
- [ ] AC-03.5: 我发送的消息靠右显示（蓝色气泡），对方的消息靠左显示（灰色气泡）
- [ ] AC-03.6: 每条消息显示发送时间（当天显示 HH:mm，非当天显示 MM-DD HH:mm）
- [ ] AC-03.7: 输入框支持多行文本（最多 5000 字符），右下角显示字符计数
- [ ] AC-03.8: 按 Ctrl+Enter 换行，按 Enter 发送消息；输入框为空时 Enter 无效
- [ ] AC-03.9: 通过 WebSocket 实时接收对方发送的新消息，无需手动刷新
- [ ] AC-03.10: 进入会话后，该会话的未读消息自动标记为已读
- [ ] AC-03.11: 页面失去焦点时收到新消息，页面标题显示闪烁提醒（如"[新消息] 派编程"）

---

### US-04: 未读消息计数和提醒

**作为** 社区注册用户
**我想要** 在网站任何页面都能看到未读私信的数量提醒
**以便于** 我不会错过任何来自其他用户的消息

**验收条件：**

- [ ] AC-04.1: 全局导航栏"消息"入口右侧显示未读消息总数（红点/数字徽标），0 条时不显示
- [ ] AC-04.2: 浏览器页面标题在未读消息 > 0 时显示 `(N) 派编程`
- [ ] AC-04.3: 当通过 WebSocket 收到新消息时，未读计数实时更新，无需刷新页面
- [ ] AC-04.4: 用户阅读消息后，未读计数实时减少
- [ ] AC-04.5: 页面初始加载时，通过 API 获取当前未读消息总数（与通知未读数的获取方式一致，合并到 global/info 接口中）

---

### US-05: 消息已读状态

**作为** 消息发送者
**我想要** 知道我发送的消息是否已被对方阅读
**以便于** 我确认对方已经看到我的消息

**验收条件：**

- [ ] AC-05.1: 我发送的消息下方显示状态标记：发送中（时钟图标）→ 已发送（单勾）→ 已读（双勾）
- [ ] AC-05.2: 当对方进入会话页面时，该会话中所有对方发送给我的消息自动标记为已读
- [ ] AC-05.3: 已读状态通过 WebSocket 实时推送给消息发送者
- [ ] AC-05.4: 在会话列表中，最后一条消息（我发送的）也显示已读/未读状态

---

## 3. 核心流程

### 3.1 发送私信流程图

```
用户A进入用户B主页
       │
       ▼
 点击"发私信"按钮
       │
       ▼
 检查登录状态 ────── 未登录 ──→ 弹出登录对话框
       │
      已登录
       │
       ▼
 创建/获取 Conversation
  (两个用户之间的会话)
       │
       ▼
 跳转到 /messages/:conversationId
       │
       ▼
 加载历史消息 (API)
       │
       ▼
 建立 WebSocket 连接
  (订阅 /user/msg/new)
       │
       ▼
 用户输入消息 → 按 Enter 发送
       │
       ▼
 前端 → POST /api/msg/send
       │
       ▼
 后端 → 保存 Message → MySQL
       │
       ▼
 后端 → SimpMessagingTemplate 
        推送至 /user/{userB}/msg/new
       │
       ▼
 用户B前端 → 收到 WebSocket 消息
   → 更新会话列表/新增消息气泡
   → 更新未读计数
       │
       ▼
 用户A前端 → 更新消息为"已发送"状态
       │
       ▼
 用户B进入会话 → PUT /api/msg/read
   → 消息标记已读 → 推送已读状态给用户A
```

### 3.2 未读计数更新流程

```
WebSocket 收到新消息
       │
       ▼
 Pinia Store 更新全局未读总数
       │
       ├──→ HeaderBar 导航栏徽标更新
       │
       ├──→ 页面标题更新 (document.title)
       │
       └──→ 会话列表页（若当前在列表页）更新对应会话未读数
```

### 3.3 会话列表数据流

```
进入 /messages 页面
       │
       ▼
 GET /api/msg/conversations?page=1&pageSize=20
       │
       ▼
 返回: [{conversationId, targetUser: {id, name, photo}, 
         lastMessage: {content, time, isRead}, unreadCount}]
       │
       ▼
 渲染会话列表
       │
       ▼
 WebSocket 连接 → 收到新消息
       │
       ▼
 更新对应会话项（移到列表顶部，更新摘要和未读数）
```

---

## 4. 功能规格

### 4.1 页面清单

| 页面/组件 | 路由 | 说明 |
|-----------|------|------|
| 消息列表页 | `/messages` | 展示所有会话列表 |
| 会话详情页 | `/messages/:conversationId` | 与特定用户的私信对话 |
| HeaderBar 消息入口 | 全局 | 导航栏消息图标 + 未读徽标 |
| "发私信"按钮 | 分散在多处 | 用户主页、文章评论区等 |

### 4.2 前端组件树（建议）

```
views/
├── messages/
│   ├── MessageListView.vue        # 消息列表页
│   └── ConversationView.vue       # 会话详情页
components/
├── message/
│   ├── ConversationItem.vue       # 会话列表单项
│   ├── MessageBubble.vue          # 消息气泡组件
│   ├── MessageInput.vue           # 消息输入框组件
│   └── MessageHeader.vue          # 会话顶部联系人信息
```

### 4.3 API 设计

#### 4.3.1 REST API

| 方法 | 路径 | 说明 | 请求参数 | 响应 |
|------|------|------|----------|------|
| POST | `/api/msg/send` | 发送消息 | `{ toUserId: Long, content: String }` | `{ conversationId: Long, messageId: Long }` |
| GET | `/api/msg/conversations` | 获取会话列表 | `?page=1&pageSize=20` | `PageListVo<ConversationDTO>` |
| GET | `/api/msg/messages/:conversationId` | 获取会话历史消息 | `?page=1&pageSize=20` | `PageListVo<MessageDTO>` |
| PUT | `/api/msg/read/:conversationId` | 标记会话为已读 | 无 | `{ success: true }` |
| GET | `/api/msg/unread-count` | 获取未读消息总数 | 无 | `{ totalUnread: 0 }` |

#### 4.3.2 WebSocket

| 端点 | 方向 | 说明 |
|------|------|------|
| `/msg/{session}` | 客户端→服务端 | 建立私信 WebSocket 连接（STOMP over WebSocket） |
| `/user/msg/new` | 服务端→客户端 | 推送新消息给目标用户 |
| `/user/msg/read` | 服务端→客户端 | 推送消息已读状态给发送者 |
| `/app/msg/send` | 客户端→服务端 | 通过 WebSocket 发送消息（备选方案，MVP 仍以 REST API 为主） |

WebSocket 消息体格式：
```json
{
  "type": "NEW_MESSAGE | READ_STATUS | CONVERSATION_UPDATE",
  "payload": {
    "conversationId": 123,
    "messageId": 456,
    "fromUserId": 789,
    "content": "消息文本",
    "createTime": "2026-05-08 14:30:00"
  }
}
```

### 4.4 数据模型设计

#### conversation 表（会话表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| conversation_type | VARCHAR(20) | 会话类型：PRIVATE（一对一）、GROUP（群聊，预留） |
| initiator_id | BIGINT | 会话发起者用户ID |
| last_message_id | BIGINT | 最后一条消息ID（冗余，加速列表查询） |
| last_message_time | DATETIME | 最后一条消息时间（冗余） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### conversation_member 表（会话成员表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| conversation_id | BIGINT | 会话ID |
| user_id | BIGINT | 用户ID |
| unread_count | INT | 该用户在此会话中的未读消息数 |
| last_read_message_id | BIGINT | 该用户最后已读的消息ID |
| is_deleted | TINYINT | 软删除标记（用户删除会话时置 1） |
| create_time | DATETIME | 加入时间 |
| update_time | DATETIME | 更新时间 |

#### message 表（消息表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| conversation_id | BIGINT | 所属会话ID |
| sender_id | BIGINT | 发送者用户ID |
| message_type | VARCHAR(20) | 消息类型：TEXT（本版本）、IMAGE/FILE/VIDEO（预留） |
| content | TEXT | 消息正文 |
| referenced_msg_id | BIGINT | 引用的消息ID（预留，可为 null） |
| attachment | JSON | 附件信息（预留，可为 null） |
| status | VARCHAR(20) | 消息状态：SENDING/SENT/DELIVERED/READ |
| create_time | DATETIME | 发送时间 |
| update_time | DATETIME | 更新时间（状态变更时） |

#### 索引建议

```sql
-- conversation 表
CREATE INDEX idx_conversation_last_msg_time ON conversation(last_message_time DESC);

-- conversation_member 表
CREATE UNIQUE INDEX idx_cm_conv_user ON conversation_member(conversation_id, user_id);
CREATE INDEX idx_cm_user_deleted_time ON conversation_member(user_id, is_deleted, update_time DESC);

-- message 表
CREATE INDEX idx_msg_conversation_time ON message(conversation_id, create_time DESC);
CREATE INDEX idx_msg_sender ON message(sender_id, create_time DESC);
```

### 4.5 页面交互规格

#### 消息列表页（MessageListView）

- 顶部标题栏：标题"消息"，右侧可有关闭/返回按钮
- 搜索框（Could Have，UI 预留，功能暂不实现）
- 会话列表区域：可滚动，下拉刷新（可选）
- 空状态：图标 + "暂无消息，去逛逛社区吧" 文案
- 点击会话项 → 跳转到 `/messages/:conversationId`
- 左滑会话项（移动端）→ 显示"删除"按钮

#### 会话详情页（ConversationView）

- 顶部固定栏：返回按钮 + 对方头像 + 昵称（可点击跳转到对方主页）
- 消息区域：flex 布局，自适应高度，overflow-y: auto
- 消息气泡样式：
  - 自己的消息：`justify-content: flex-end`，气泡背景色为品牌色/浅蓝，右下角显示已读/未读状态
  - 对方的消息：`justify-content: flex-start`，气泡背景色为浅灰
- 时间标签：在消息之间间隔 > 5 分钟时显示时间分割线
- 底部固定栏：多行 textarea + 发送按钮
- 加载状态：进入页面时显示骨架屏，历史消息加载中显示 loading
- 错误状态：网络异常时显示"消息加载失败，点击重试"

#### "发私信"入口

- 用户主页：在用户信息卡片中，"关注"按钮旁边显示"发私信"按钮（outlined 样式）
- 评论区：鼠标悬停在评论者头像上时，tooltip 中显示"发私信"链接
- 这些入口需要判断：目标用户不是当前登录用户本人

---

## 5. 非功能性需求

### 5.1 性能

| 指标 | 要求 |
|------|------|
| WebSocket 消息延迟 | P99 < 500ms（发送→推送→接收端渲染） |
| API 响应时间 | P99 < 200ms（会话列表/消息列表） |
| 历史消息翻页 | 每页 20 条，加载时间 < 200ms |
| 并发 WebSocket 连接 | 单节点支持 5000 并发连接 |
| 前端首屏加载 | 消息列表页 Lighthouse Performance Score > 80 |

### 5.2 安全

| 要求 | 说明 |
|------|------|
| 认证 | 所有 API 和 WebSocket 连接必须验证登录状态，复用现有 `@Permission(role = UserRole.LOGIN)` 和 `AuthHandshakeInterceptor` |
| 授权 | 用户只能查看自己参与的会话和消息；API 层面校验 `conversation_id` 的成员权限 |
| 输入校验 | 消息内容做 XSS 过滤（前端 + 后端），长度限制 5000 字符 |
| 频率限制 | MVP 阶段不做硬性频率限制，但后端记录发送频率日志以便后续评估 |
| 数据隔离 | 软删除的会话，其他用户不可见（通过 `is_deleted` 字段过滤） |

### 5.3 可靠性

| 要求 | 说明 |
|------|------|
| 消息不丢失 | 消息先写入 MySQL，成功后通过 WebSocket 推送；如果 WebSocket 推送失败，消息已持久化，用户在刷新或下次进入时可看到 |
| WebSocket 断线重连 | 前端实现自动重连（指数退避：1s/2s/4s/8s/16s，最大重试 5 次） |
| 未读计数一致性 | 以服务端数据库为准；前端乐观更新 + 页面 `visibilitychange` 时拉取校准 |
| 重复消息去重 | 服务端基于 `sender_id + conversation_id + content + create_time(分钟级)` 做去重 |

### 5.4 可扩展性

| 要求 | 说明 |
|------|------|
| 数据模型扩展 | `conversation_type`、`message_type`、`attachment`、`referenced_msg_id` 等字段已预留 |
| 消息通道隔离 | 私信 WebSocket 使用独立 broker 前缀 `/msg`，与 AI 聊天 `/chat` 隔离 |
| 前端组件化 | 消息气泡、输入框、会话列表项等组件独立封装，支持未来群聊等场景复用 |
| API 版本管理 | API 路径不含版本号（沿用项目风格），后续可通过请求头控制版本 |

### 5.5 兼容性

| 要求 | 说明 |
|------|------|
| 浏览器 | 支持 Chrome/Firefox/Edge/Safari 最近 2 个大版本 |
| 移动端 | 消息列表和会话页面支持响应式布局，移动端适配（同项目现有标准） |
| 暗色模式 | 所有私信相关页面和组件必须支持暗色/亮色主题切换（使用 `--pai-*` CSS 变量） |

### 5.6 可用性

| 要求 | 说明 |
|------|------|
| 键盘操作 | Enter 发送，Ctrl+Enter 换行；Tab 键可聚焦到输入框和发送按钮 |
| 屏幕阅读器 | 消息区域使用 `role="log"`，新消息使用 `aria-live="polite"` |
| 加载状态 | 所有异步操作有 loading 指示器 |
| 错误处理 | 网络错误时显示 toast 提示，不丢失用户已输入的内容 |
| 空状态 | 无会话、无消息时显示友好的空状态提示 |

---

## 6. 与现有系统的集成点

| 集成点 | 方式 | 说明 |
|--------|------|------|
| 全局未读计数 | 扩展 `GlobalInitService.globalAttr()` | 在 `GlobalVo` 中新增 `privateMsgNum` 字段，与 `msgNum`（通知未读）并列 |
| 全局信息接口 | 扩展 `GLOBAL_INFO_URL` 响应 | 在 `CommonResponse.global` 中增加 `privateMsgNum` |
| 导航栏 | 修改 `HeaderBar.vue` | 导航栏增加"消息"入口，展示未读徽标 |
| WebSocket 基础设施 | 新增 `WsMsgConfig.java` | 独立配置，复用现有的 `SimpMessagingTemplate`，使用独立的 broker 和目标路径 |
| 通知系统 | 可选集成（Should Have） | 收到私信时创建 `NotifyMsgDO` 记录，类型暂用 `SYSTEM` 或新增 `PRIVATE_MSG` 枚举值 |
| 用户服务 | 复用 `UserService` / `UserRelationService` | 获取用户信息、查询用户关系 |
| 认证 | 复用 `LoginService.SESSION_KEY` | WebSocket 握手时读取 session 验证身份 |

---

## 7. 外部依赖与影响

| 依赖项 | 状态 | 说明 |
|--------|------|------|
| WebSocket (STOMP) | 已有 | 使用独立的端点 `/msg/{session}`，复用 `SimpMessagingTemplate` |
| MySQL | 已有 | 新增 3 张表：`conversation`、`conversation_member`、`message` |
| Redis | 已有 | 可选用于在线状态缓存、消息去重 |
| RabbitMQ | 已有 | 可选用于异步处理消息已读/未读状态更新 |
| 前端 stompjs | 已有 | 已在 `ChatView.vue` 中使用，私信模块复用相同库 |
| Liquibase | 已有 | 新增 ChangeLog 文件管理表结构变更 |
