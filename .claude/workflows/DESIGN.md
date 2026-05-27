# 详细设计: 私信功能 (Private Messaging)

---

## 1. PLAN 审阅意见

### 1.1 SENIOR-DEV AMENDMENTS

**[SENIOR-DEV AMENDMENT-01: 数据模型缺少部分核心字段]**
PLAN.md 的 `conversation_member` 表缺少 `is_top`（置顶标记）字段。虽然 S2（会话置顶）是 Should Have，但建表时预留该字段（默认 0）可避免后续 DDL 迁移。同样，`conversation` 表缺少 `deleted` 标记以支持软删除。DESIGN.md 已补充。

**[SENIOR-DEV AMENDMENT-02: 未详述 XSS 过滤方案]**
PLAN.md 提到"消息内容做 XSS 过滤"，但未指定具体实现。项目中无现成的 HTML 过滤工具类。MVP 方案：后端 Service 层使用 Spring 的 `org.springframework.web.util.HtmlUtils.htmlEscape()` 对消息内容编码，前端 `MessageInput.vue` 也做一次编码。

**[SENIOR-DEV AMENDMENT-03: 缺少 WebSocket 全局连接管理策略]**
PLAN.md 未说明如何管理 WebSocket 连接的生命周期。AI 聊天（`ChatView.vue`）的连接绑定在组件生命周期上，但私信需要在全局（所有页面）保持长连接。DESIGN.md 设计了一个 `useMessageWs.ts` composable，在 `App.vue` 登录后初始化，实现跨页面持久连接。

**[SENIOR-DEV AMENDMENT-04: 发私信入口的校验流程不完整]**
PLAN.md 提到"自己不能给自己发私信"，但未说明前端如何获取当前用户 ID 进行比较。需要在 `UserHomeInfo.vue` 和 `CommentAction.vue` 中注入 `globalStore`，比较 `global.user.userId` 与目标 `userId`。

**[SENIOR-DEV AMENDMENT-05: 后端 API 路径前缀规范]**
PLAN.md 使用 `/api/msg/*` 作为 API 路径。经核查现有代码，多数 Controller 使用不带 `/api` 前缀的路径（如 `notice/api`），配合 Vite 代理和 Axios baseURL 工作。但 `GlobalInfoController` 使用 `api/global` 模式。为保持一致性，本设计采用 `api/msg` 前缀，前端 URL 以 `/api/msg/` 开头。

### 1.2 潜在风险提示

- `conversation` 表的 `last_message_id` 和 `last_message_time` 是反范式冗余字段，需要在发送消息、删除消息时保持一致性。建议用 Service 层保证更新原子性。
- WebSocket 连接数：每个登录用户持有一个 STOMP 连接，10 万用户即 10 万连接。单节点建议配置 `server.tomcat.max-connections=5000`，后续考虑多节点 + Redis 广播。
- 消息表增长：按日活 1 万、人均 5 条/天计算，月增约 150 万行。MVP 暂不分表，但需监控并在 3 个月内准备归档策略。

## 2. 数据模型详细设计

### 2.1 完整 DDL

```sql
-- Table 1: conversation (会话表)
CREATE TABLE `conversation` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `conversation_type` VARCHAR(20)     NOT NULL DEFAULT 'PRIVATE' COMMENT '会话类型: PRIVATE-一对一, GROUP-群聊(预留)',
    `initiator_id`      BIGINT          NOT NULL DEFAULT 0       COMMENT '会话发起者用户ID',
    `last_message_id`   BIGINT          NOT NULL DEFAULT 0       COMMENT '最后一条消息ID(冗余,加速列表查询)',
    `last_message_time` DATETIME        NULL                     COMMENT '最后一条消息时间(冗余)',
    `deleted`           TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_last_msg_time` (`last_message_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- Table 2: conversation_member (会话成员表)
CREATE TABLE `conversation_member` (
    `id`                    BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `conversation_id`       BIGINT      NOT NULL DEFAULT 0       COMMENT '会话ID',
    `user_id`               BIGINT      NOT NULL DEFAULT 0       COMMENT '用户ID',
    `unread_count`          INT         NOT NULL DEFAULT 0       COMMENT '该用户在此会话中的未读消息数',
    `last_read_message_id`  BIGINT      NOT NULL DEFAULT 0       COMMENT '该用户最后已读的消息ID',
    `is_deleted`            TINYINT     NOT NULL DEFAULT 0       COMMENT '软删除标记: 0-未删除, 1-已删除',
    `is_top`                TINYINT     NOT NULL DEFAULT 0       COMMENT '置顶标记: 0-未置顶, 1-已置顶(预留, S2)',
    `create_time`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `update_time`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_cm_conv_user` (`conversation_id`, `user_id`) COMMENT '一个用户在一个会话中只有一条成员记录',
    INDEX `idx_cm_user_deleted_time` (`user_id`, `is_deleted`, `update_time` DESC) COMMENT '加速查询用户的会话列表')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话成员表';

-- Table 3: message (消息表)
CREATE TABLE `message` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `conversation_id`   BIGINT          NOT NULL DEFAULT 0       COMMENT '所属会话ID',
    `sender_id`         BIGINT          NOT NULL DEFAULT 0       COMMENT '发送者用户ID',
    `message_type`      VARCHAR(20)     NOT NULL DEFAULT 'TEXT'  COMMENT '消息类型: TEXT-文本, IMAGE/FILE/VIDEO(预留)',
    `content`           TEXT            NOT NULL                 COMMENT '消息正文',
    `referenced_msg_id` BIGINT          NULL                     COMMENT '引用的消息ID(预留,可为null)',
    `attachment`        JSON            NULL                     COMMENT '附件信息(预留,可为null)',
    `status`            VARCHAR(20)     NOT NULL DEFAULT 'SENT'  COMMENT '消息状态: SENDING-发送中, SENT-已发送, DELIVERED-已送达, READ-已读',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_msg_conversation_time` (`conversation_id`, `create_time` DESC) COMMENT '加速查询某个会话的消息列表',
    INDEX `idx_msg_sender_time` (`sender_id`, `create_time` DESC) COMMENT '加速查询某个用户发送的消息',
    INDEX `idx_msg_status` (`status`) COMMENT '加速按状态查询')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
```

### 2.2 索引设计理由

| 索引 | 理由 |
|------|------|
| `idx_conversation_last_msg_time` | 会话列表按最后消息时间倒序排列，该索引覆盖排序需求 |
| `idx_cm_conv_user` (UNIQUE) | 防止同一用户在同一会话中出现重复成员记录，同时加速成员查询 |
| `idx_cm_user_deleted_time` | 用户消息页面需要查询该用户参与的所有未删除会话 |
| `idx_msg_conversation_time` | 核心查询：按会话翻页加载历史消息（每页 20 条） |
| `idx_msg_sender_time` | 辅助索引：用于统计数据或未来扩展 |
| `idx_msg_status` | 用于批量更新已读状态 |

### 2.3 MyBatis-Plus Entity 类

所有 Entity 遵循现有 `BaseDO` 模式（包含 id, createTime, updateTime）：

**ConversationDO.java**
- 包路径: `paicoding-service/src/main/java/.../msg/repository/entity/ConversationDO.java`
- `@TableName("conversation")`
- 字段: conversationType, initiatorId, lastMessageId, lastMessageTime, deleted

**ConversationMemberDO.java**
- 包路径: `paicoding-service/src/main/java/.../msg/repository/entity/ConversationMemberDO.java`
- `@TableName("conversation_member")`
- 字段: conversationId, userId, unreadCount, lastReadMessageId, isDeleted, isTop

**MessageDO.java**
- 包路径: `paicoding-service/src/main/java/.../msg/repository/entity/MessageDO.java`
- `@TableName("message")`
- 字段: conversationId, senderId, messageType, content, referencedMsgId, attachment, status

### 2.4 扩展预留字段用法说明

| 字段 | 预留用途 | 当前 MVP 行为 | 未来启用方式 |
|------|----------|--------------|-------------|
| conversation.conversation_type | 群聊支持 | 固定为 PRIVATE | 增加 GROUP 枚举值 |
| message.referenced_msg_id | 消息引用/回复 | 始终为 NULL | 发送 API 增加可选参数 |
| message.attachment | 文件/图片分享 | 始终为 NULL | 发送 API 增加文件上传 |
| message.message_type | 消息类型扩展 | 固定为 TEXT | 扩展 IMAGE/FILE/VIDEO 枚举值 |
| conversation_member.is_top | 会话置顶 | 始终为 0 (预留 S2) | 新增置顶 API |

## 3. API 接口详细设计

### 3.1 REST API 端点

#### 3.1.1 发送消息
| 属性 | 值 |
|------|-----|
| URL | POST /api/msg/send |
| 认证 | 需要登录 (@Permission(role = UserRole.LOGIN)) |
| Request Body | { "toUserId": 123, "content": "你好" } |
| Response (result) | { "conversationId": 456, "messageId": 789 } |
| 校验 | toUserId 不能等于当前用户；content 长度 1~5000；XSS 过滤 |

**请求参数说明：**
- toUserId (Long, required): 目标用户ID
- content (String, required): 消息内容，1~5000 字符，前后端均做 XSS 编码

**响应示例：**
```json
{
  "status": { "code": 0, "msg": "success" },
  "result": { "conversationId": 456, "messageId": 789 },
  "global": { ... }
}
```

#### 3.1.2 获取会话列表
| 属性 | 值 |
|------|-----|
| URL | GET /api/msg/conversations |
| 认证 | 需要登录 |
| Query Params | page=1&pageSize=20 |
| Response (result) | PageListVo<ConversationVO> |

**ConversationVO 结构：**
```json
{
  "conversationId": 456,
  "conversationType": "PRIVATE",
  "targetUser": {
    "userId": 123,
    "userName": "张三",
    "photo": "https://..."
  },
  "lastMessage": {
    "content": "你好吗？",
    "createTime": "2026-05-08 14:30:00",
    "status": "SENT",
    "fromUserId": 123
  },
  "unreadCount": 3,
  "isTop": 0,
  "createTime": "2026-05-08T10:00:00"
}
```

#### 3.1.3 获取会话历史消息
| 属性 | 值 |
|------|-----|
| URL | GET /api/msg/messages/{conversationId} |
| 认证 | 需要登录 |
| Query Params | page=1&pageSize=20 |
| Response (result) | PageListVo<MessageVO> |
| 权限 | 必须是该会话的成员 |

**MessageVO 结构：**
```json
{
  "messageId": 789,
  "conversationId": 456,
  "fromUserId": 123,
  "messageType": "TEXT",
  "content": "你好",
  "referencedMsgId": null,
  "attachment": null,
  "status": "SENT",
  "createTime": "2026-05-08 14:30:00"
}
```

#### 3.1.4 标记会话为已读
| 属性 | 值 |
|------|-----|
| URL | PUT /api/msg/read/{conversationId} |
| 认证 | 需要登录 |
| Request Body | 无 |
| Response (result) | { "success": true } |
| 权限 | 必须是该会话的成员 |

**处理逻辑：**
1. 查询该会话中所有 sender_id != currentUserId AND status != READ 的消息
2. 批量更新这些消息的 status 为 READ
3. 重置当前用户的 conversation_member.unread_count 为 0
4. 更新 conversation_member.last_read_message_id
5. 通过 WebSocket 推送已读状态给会话中的其他成员

#### 3.1.5 获取未读消息总数
| 属性 | 值 |
|------|-----|
| URL | GET /api/msg/unread-count |
| 认证 | 需要登录 |
| Query Params | 无 |
| Response (result) | { "totalUnread": 5 } |

**说明：** 用于前端周期性校准未读数。初始未读数已在 /api/global/info 中返回（privateMsgNum 字段）。

#### 3.1.6 创建或获取会话 (发私信入口专用)
| 属性 | 值 |
|------|-----|
| URL | POST /api/msg/start |
| 认证 | 需要登录 |
| Request Body | { "toUserId": 123 } |
| Response (result) | { "conversationId": 456 } |
| 校验 | toUserId 不能等于当前用户 |

**说明：** 点击发私信按钮时调用。后端查找两个用户之间是否已有 PRIVATE 会话，有则返回已有 conversationId，无则创建新会话并添加成员。

### 3.2 错误码定义

| 场景 | StatusEnum | 说明 |
|------|-----------|------|
| 目标用户不存在 | ILLEGAL_ARGUMENTS_MIXED | toUserId 对应的用户不存在 |
| 不能给自己发消息 | FORBID_ERROR | 自己不能给自己发私信 |
| 消息内容超长 | ILLEGAL_ARGUMENTS_MIXED | content > 5000 字符 |
| 消息内容为空 | ILLEGAL_ARGUMENTS_MIXED | content 为空或仅空白 |
| 非会话成员访问 | FORBID_ERROR | 尝试访问非自己参与的会话 |
| 会话不存在 | RECORDS_NOT_EXISTS | conversationId 无效 |
| 未登录调用 | (由 @Permission 拦截器自动处理) | 自动返回需要登录的提示 |

### 3.3 WebSocket 消息通道

#### 3.3.1 连接端点

| 端点 | 说明 |
|------|------|
| /msg/{session} | STOMP over WebSocket 连接端点 |
| Application prefix: /app | 客户端发送消息的前缀（本 MVP 以 REST 为主） |
| Simple Broker: /msg | 服务端推送消息的 broker 前缀 |

#### 3.3.2 服务端 -> 客户端消息

**通道1: /user/msg/new --- 推送新消息**
```json
{
  "type": "NEW_MESSAGE",
  "payload": {
    "conversationId": 456,
    "messageId": 789,
    "fromUserId": 123,
    "fromUserName": "张三",
    "fromUserPhoto": "https://...",
    "content": "你好",
    "messageType": "TEXT",
    "createTime": "2026-05-08 14:30:00"
  }
}
```

**通道2: /user/msg/read --- 推送已读状态**
```json
{
  "type": "READ_STATUS",
  "payload": {
    "conversationId": 456,
    "messageId": 789,
    "readByUserId": 123,
    "readTime": "2026-05-08 14:35:00"
  }
}
```

#### 3.3.3 消息体格式定义

**WsMsgPushVO.java** - 包路径: paicoding-api/.../api/model/vo/msg/WsMsgPushVO.java
- 字段: String type (NEW_MESSAGE | READ_STATUS), Object payload
- NewMessagePayload: conversationId, messageId, fromUserId, fromUserName, fromUserPhoto, content, messageType, createTime
- ReadStatusPayload: conversationId, messageId, readByUserId, readTime

## 4. 后端代码结构

### 4.1 模块文件清单

```
paicoding-api
+-- src/main/java/com/github/paicoding/forum/api/model/enums/msg/
|   +-- ConversationTypeEnum.java
|   +-- MessageTypeEnum.java
|   +-- MessageStatusEnum.java
+-- src/main/java/com/github/paicoding/forum/api/model/vo/msg/
    +-- ConversationVO.java
    +-- MessageVO.java
    +-- SendMsgReq.java
    +-- SendMsgRes.java
    +-- StartConvReq.java
    +-- StartConvRes.java
    +-- UnreadCountRes.java
    +-- WsMsgPushVO.java

paicoding-service
+-- src/main/java/com/github/paicoding/forum/service/msg/
    +-- service/MsgService.java
    +-- service/impl/MsgServiceImpl.java
    +-- helper/MsgPushHelper.java
    +-- repository/entity/ConversationDO.java
    +-- repository/entity/ConversationMemberDO.java
    +-- repository/entity/MessageDO.java
    +-- repository/mapper/ConversationMapper.java
    +-- repository/mapper/ConversationMemberMapper.java
    +-- repository/mapper/MessageMapper.java
    +-- repository/dao/ConversationDAO.java
    +-- repository/dao/ConversationMemberDAO.java
    +-- repository/dao/MessageDAO.java

paicoding-web
+-- src/main/java/com/github/paicoding/forum/web/controller/msg/
|   +-- rest/MsgController.java
|   +-- stomp/WsMsgConfig.java
|   +-- stomp/MsgHandshakeHandler.java
|   +-- stomp/MsgHandshakeInterceptor.java
+-- src/main/java/.../web/global/vo/GlobalVo.java [修改]
+-- src/main/java/.../web/global/GlobalInitService.java [修改]
+-- src/main/resources/liquibase/master.xml [修改]
+-- src/main/resources/liquibase/changelog/001_private_msg.xml [新增]
```

### 4.2 依赖注入关系

```
MsgController
  +-- MsgService (interface) <-- MsgServiceImpl
  |     +-- ConversationDAO
  |     +-- ConversationMemberDAO
  |     +-- MessageDAO
  |     +-- MsgPushHelper
  |           +-- SimpMessagingTemplate
  +-- UserService

GlobalInitService
  +-- NotifyService (existing)
  +-- MsgService (新增) <-- queryUnreadTotal()
```

### 4.3 Service 层核心方法伪代码

**MsgService 接口：**
```java
public interface MsgService {
    SendMsgRes send(Long currentUserId, SendMsgReq req);
    PageListVo<ConversationVO> listConversations(Long userId, PageParam page);
    PageListVo<MessageVO> listMessages(Long userId, Long conversationId, PageParam page);
    void markAsRead(Long userId, Long conversationId);
    int queryUnreadTotal(Long userId);
    Long getOrCreatePrivateConversation(Long currentUserId, Long targetUserId);
}
```

**MsgServiceImpl.send() 伪代码：**
```java
@Transactional
public SendMsgRes send(Long currentUserId, SendMsgReq req) {
    // 1. 校验
    Assert.isTrue(!Objects.equals(currentUserId, req.getToUserId()), "不能给自己发消息");
    Assert.isTrue(StringUtils.hasText(req.getContent()), "内容不能为空");
    String safeContent = HtmlUtils.htmlEscape(req.getContent().trim());
    safeContent = StringUtils.truncate(safeContent, 5000);
    
    // 2. 查找或创建会话
    Long conversationId = getOrCreatePrivateConversation(currentUserId, req.getToUserId());
    
    // 3. 保存消息
    MessageDO msg = new MessageDO().setConversationId(conversationId)
        .setSenderId(currentUserId).setMessageType("TEXT")
        .setContent(safeContent).setStatus("SENT");
    messageDAO.save(msg);
    
    // 4. 更新会话冗余字段
    conversationDAO.lambdaUpdate()
        .eq(ConversationDO::getId, conversationId)
        .set(ConversationDO::getLastMessageId, msg.getId())
        .set(ConversationDO::getLastMessageTime, msg.getCreateTime())
        .update();
    
    // 5. 递增接收方未读数
    conversationMemberDAO.lambdaUpdate()
        .eq(ConversationMemberDO::getConversationId, conversationId)
        .eq(ConversationMemberDO::getUserId, req.getToUserId())
        .setSql("unread_count = unread_count + 1").update();
    
    // 6. WebSocket 推送
    msgPushHelper.pushNewMessage(req.getToUserId(), msg);
    return new SendMsgRes(conversationId, msg.getId());
}
```

**getOrCreatePrivateConversation() 伪代码：**
```java
@Transactional
public Long getOrCreatePrivateConversation(Long userA, Long userB) {
    List<ConversationMemberDO> membersOfA = conversationMemberDAO.lambdaQuery()
        .eq(ConversationMemberDO::getUserId, userA)
        .eq(ConversationMemberDO::getIsDeleted, 0)
        .list();
    Set<Long> convIdsOfA = membersOfA.stream()
        .map(ConversationMemberDO::getConversationId)
        .collect(Collectors.toSet());
    if (!convIdsOfA.isEmpty()) {
        List<ConversationMemberDO> membersOfB = conversationMemberDAO.lambdaQuery()
            .eq(ConversationMemberDO::getUserId, userB)
            .eq(ConversationMemberDO::getIsDeleted, 0)
            .in(ConversationMemberDO::getConversationId, convIdsOfA)
            .list();
        for (ConversationMemberDO mb : membersOfB) {
            ConversationDO conv = conversationDAO.getById(mb.getConversationId());
            if (conv != null && "PRIVATE".equals(conv.getConversationType())) {
                return conv.getId();
            }
        }
    }
    // 未找到，创建新会话
    ConversationDO conv = new ConversationDO()
        .setConversationType("PRIVATE").setInitiatorId(userA);
    conversationDAO.save(conv);
    conversationMemberDAO.save(new ConversationMemberDO()
        .setConversationId(conv.getId()).setUserId(userA));
    conversationMemberDAO.save(new ConversationMemberDO()
        .setConversationId(conv.getId()).setUserId(userB));
    return conv.getId();
}
```

**MsgServiceImpl.markAsRead() 伪代码：**
```java
@Transactional
public void markAsRead(Long userId, Long conversationId) {
    ConversationMemberDO member = conversationMemberDAO.lambdaQuery()
        .eq(ConversationMemberDO::getConversationId, conversationId)
        .eq(ConversationMemberDO::getUserId, userId).one();
    Assert.notNull(member, "非会话成员");
    messageDAO.lambdaUpdate()
        .eq(MessageDO::getConversationId, conversationId)
        .ne(MessageDO::getSenderId, userId)
        .ne(MessageDO::getStatus, "READ")
        .set(MessageDO::getStatus, "READ").update();
    MessageDO lastMsg = messageDAO.lambdaQuery()
        .eq(MessageDO::getConversationId, conversationId)
        .orderByDesc(MessageDO::getId).last("limit 1").one();
    conversationMemberDAO.lambdaUpdate()
        .eq(ConversationMemberDO::getId, member.getId())
        .set(ConversationMemberDO::getUnreadCount, 0)
        .set(lastMsg != null, ConversationMemberDO::getLastReadMessageId,
            lastMsg != null ? lastMsg.getId() : null)
        .update();
    if (lastMsg != null && !lastMsg.getSenderId().equals(userId)) {
        msgPushHelper.pushReadStatus(lastMsg.getSenderId(), conversationId,
            lastMsg.getId(), userId);
    }
}
```

## 5. 前端代码结构

### 5.1 组件树

```
App.vue                                    [修改] 初始化 WS 连接
  +-- HeaderBar.vue                        [修改] 增加消息导航入口 + 未读徽标
  |     +-- button: /messages + badge: store.unreadTotal
  +-- MessageListView.vue                  [新增] /messages 路由
  |     +-- ConversationItem.vue * N       [新增] 会话列表单项
  |     |     Props: { item: ConversationItem }
  |     |     Events: @click -> router.push(/messages/:id)
  |     +-- ElEmpty (空状态)
  +-- ConversationView.vue                 [新增] /messages/:conversationId 路由
  |     +-- MessageHeader.vue              [新增] 顶部联系人信息
  |     |     Props: { targetUser: SimpleUserInfo }
  |     |     Events: @click -> router.push(/user/:id)
  |     +-- MessageBubble.vue * N          [新增] 消息气泡
  |     |     Props: { message: MessageItem, isOwn: boolean }
  |     +-- MessageInput.vue               [新增] 输入框 + 发送按钮
  |           Props: { maxLength: 5000, loading: boolean }
  |           Emits: @send(content: string)
  +-- UserHomeInfo.vue                     [修改] 增加发私信按钮
  +-- CommentAction.vue                    [修改] 增加发私信入口
```

### 5.2 路由配置

在 router/index.ts 中添加：
```typescript
{
  path: "/messages",
  name: "messages",
  component: () => import("@/views/messages/MessageListView.vue"),
  meta: { loginRequired: true }
},
{
  path: "/messages/:conversationId",
  name: "conversation",
  component: () => import("@/views/messages/ConversationView.vue"),
  meta: { loginRequired: true }
}
```

### 5.3 Store 设计

**message.ts** (新增 Pinia Store)
- State: unreadTotal, conversations[], currentConversationId, wsConnected
- Actions: fetchUnreadCount, fetchConversations, incrementUnread, resetUnread, addMessage, updateMessageStatus, handleNewMessage, handleReadStatus, setWsConnected
- 更新 pageTitle: document.title = unread > 0 ? "(N) 派编程" : "派编程"

**GlobalResponse 扩展 (CommonResponseType.ts)：**
- 新增字段: privateMsgNum: number | null
- defaultGlobalResponse 新增: privateMsgNum: null

### 5.4 API 层

**URL.ts 追加：**
```typescript
export const MSG_SEND_URL = "/api/msg/send";
export const MSG_START_URL = "/api/msg/start";
export const MSG_CONVERSATIONS_URL = "/api/msg/conversations";
export const MSG_MESSAGES_URL = "/api/msg/messages";
export const MSG_READ_URL = "/api/msg/read";
export const MSG_UNREAD_COUNT_URL = "/api/msg/unread-count";
```

**MessageRequests.ts** (新增)：封装 6 个请求函数 (sendMessage, startConversation, fetchConversations, fetchMessages, markConversationRead, fetchUnreadCount)

### 5.5 WebSocket 连接管理

**useMessageWs.ts** (新增 composable)
- 使用 @stomp/stompjs 的 Client 类
- connect(): 从 cookie 获取 sessionId -> 创建 STOMP 连接 -> 订阅 /user/msg/new 和 /user/msg/read
- disconnect(): 断开连接
- 指数退避重连 (Client 内置 reconnectDelay)
- 内部维护单例，避免重复连接
- 在 App.vue 中初始化: watch globalStore.global.isLogin -> connect/disconnect

### 5.6 关键页面交互说明

**ConversationView.vue：**
- onMounted: 获取 conversationId, fetchMessages(), markConversationRead()
- 顶部: MessageHeader (返回 + 对方头像/昵称)
- 消息区域: 按时间正序, isOwn 区分左右气泡
- 底部: MessageInput, @send 调用 sendMessage()
- 自动滚动到底部, 上拉加载更多
- WebSocket 收到新消息追加到列表

**MessageListView.vue：**
- onMounted: fetchConversations(1)
- 循环 ConversationItem, 支持点击跳转
- WebSocket 接收: 更新对应会话项 (移到顶部, 更新摘要, 更新未读数)
- 空状态: ElEmpty 组件

## 6. 任务拆解清单

### T1: 创建 Liquibase 变更集
- **标题:** 创建私信功能数据库表
- **依赖:** 无
- **涉及文件:**
  - [新建] paicoding-web/src/main/resources/liquibase/changelog/001_private_msg.xml
  - [新建] paicoding-web/src/main/resources/liquibase/data/init_schema_260508.sql
  - [修改] paicoding-web/src/main/resources/liquibase/master.xml
- **实现要点:** 按 DDL 创建 3 张表, 使用 sqlFile 方式, master.xml 追加 include
- **验证命令:** cd paicoding-web && mvn compile
- **预估复杂度:** S

### T2: 创建 API 层枚举和 DTO/VO
- **标题:** 创建消息功能枚举、请求/响应 DTO
- **依赖:** 无
- **涉及文件:**
  - [新建] paicoding-api/.../enums/msg/ConversationTypeEnum.java
  - [新建] paicoding-api/.../enums/msg/MessageTypeEnum.java
  - [新建] paicoding-api/.../enums/msg/MessageStatusEnum.java
  - [新建] paicoding-api/.../vo/msg/SendMsgReq.java
  - [新建] paicoding-api/.../vo/msg/SendMsgRes.java
  - [新建] paicoding-api/.../vo/msg/StartConvReq.java
  - [新建] paicoding-api/.../vo/msg/StartConvRes.java
  - [新建] paicoding-api/.../vo/msg/ConversationVO.java
  - [新建] paicoding-api/.../vo/msg/MessageVO.java
  - [新建] paicoding-api/.../vo/msg/UnreadCountRes.java
  - [新建] paicoding-api/.../vo/msg/WsMsgPushVO.java
- **实现要点:** 所有 VO 使用 @Data + @Accessors(chain = true), 枚举参照 NotifyTypeEnum 模式
- **验证命令:** cd paicoding-api && mvn compile
- **预估复杂度:** S

### T3: 创建数据访问层 (DO + Mapper + DAO)
- **标题:** 创建消息模块的持久层代码
- **依赖:** T1, T2
- **涉及文件:**
  - [新建] paicoding-service/.../msg/repository/entity/ConversationDO.java
  - [新建] paicoding-service/.../msg/repository/entity/ConversationMemberDO.java
  - [新建] paicoding-service/.../msg/repository/entity/MessageDO.java
  - [新建] paicoding-service/.../msg/repository/mapper/ConversationMapper.java
  - [新建] paicoding-service/.../msg/repository/mapper/ConversationMemberMapper.java
  - [新建] paicoding-service/.../msg/repository/mapper/MessageMapper.java
  - [新建] paicoding-service/.../msg/repository/dao/ConversationDAO.java
  - [新建] paicoding-service/.../msg/repository/dao/ConversationMemberDAO.java
  - [新建] paicoding-service/.../msg/repository/dao/MessageDAO.java
- **实现要点:** BaseDO 继承, BaseMapper 继承, ServiceImpl 继承, DAO 提供 listByUserId/listByConversation/countUnreadByUser
- **验证命令:** cd paicoding-service && mvn compile
- **预估复杂度:** M

### T4: 创建 Service 业务逻辑层
- **标题:** 实现私信核心业务逻辑
- **依赖:** T3
- **涉及文件:**
  - [新建] paicoding-service/.../msg/service/MsgService.java
  - [新建] paicoding-service/.../msg/service/impl/MsgServiceImpl.java
  - [新建] paicoding-service/.../msg/helper/MsgPushHelper.java
- **实现要点:** @Transactional send/list/markAsRead/queryUnreadTotal/getOrCreatePrivateConversation, HtmlUtils.htmlEscape XSS 过滤, SimpMessagingTemplate 推送
- **验证命令:** cd paicoding-service && mvn compile
- **预估复杂度:** M

### T5: 创建 WebSocket 配置
- **标题:** 配置私信功能的 STOMP WebSocket 端点
- **依赖:** 无
- **涉及文件:**
  - [新建] paicoding-web/.../controller/msg/stomp/WsMsgConfig.java
  - [新建] paicoding-web/.../controller/msg/stomp/MsgHandshakeHandler.java
  - [新建] paicoding-web/.../controller/msg/stomp/MsgHandshakeInterceptor.java
- **实现要点:** @EnableWebSocketMessageBroker, broker /msg, endpoint /msg/{session}, handshake 验证 session, 与 WsChatConfig 隔离
- **验证命令:** cd paicoding-web && mvn compile
- **预估复杂度:** M

### T6: 创建 REST Controller 并扩展全局信息
- **标题:** 实现私信 REST API 和全局信息集成
- **依赖:** T4, T5
- **涉及文件:**
  - [新建] paicoding-web/.../controller/msg/rest/MsgController.java
  - [修改] paicoding-web/.../web/global/vo/GlobalVo.java
  - [修改] paicoding-web/.../web/global/GlobalInitService.java
- **实现要点:** @RestController @RequestMapping("api/msg"), 6 个端点, 全部 @Permission(LOGIN), GlobalVo 新增 privateMsgNum, GlobalInitService 注入 MsgService
- **验证命令:** cd paicoding-web && mvn compile
- **预估复杂度:** M

### T7: 创建前端类型定义和 API 层
- **标题:** 定义前端 TypeScript 接口和 API 请求函数
- **依赖:** 无
- **涉及文件:**
  - [新建] pai-coding-front/src/http/ResponseTypes/MsgTypes.ts
  - [新建] pai-coding-front/src/http/MessageRequests.ts
  - [修改] pai-coding-front/src/http/URL.ts
  - [修改] pai-coding-front/src/http/ResponseTypes/CommonResponseType.ts
- **实现要点:** 定义 ConversationItem/MessageItem/WsPushPayload 等接口, 新增 MSG_* URL 常量, 封装 6 个请求函数, GlobalResponse 扩展 privateMsgNum
- **验证命令:** cd pai-coding-front && npm run type-check
- **预估复杂度:** S

### T8: 创建 Pinia Store 和 WebSocket Composable
- **标题:** 实现前端消息状态管理和 WebSocket 连接管理
- **依赖:** T7
- **涉及文件:**
  - [新建] pai-coding-front/src/stores/message.ts
  - [新建] pai-coding-front/src/composables/useMessageWs.ts
- **实现要点:** Pinia store 管理 unreadTotal/conversations/wsConnected, useMessageWs 使用 @stomp/stompjs 连接 /msg/{session}, 订阅 /user/msg/new 和 /user/msg/read
- **验证命令:** cd pai-coding-front && npm run type-check
- **预估复杂度:** M

### T9: 创建前端消息页面和组件
- **标题:** 开发消息列表页、会话详情页和消息相关组件
- **依赖:** T7, T8
- **涉及文件:**
  - [新建] pai-coding-front/src/views/messages/MessageListView.vue
  - [新建] pai-coding-front/src/views/messages/ConversationView.vue
  - [新建] pai-coding-front/src/components/message/ConversationItem.vue
  - [新建] pai-coding-front/src/components/message/MessageBubble.vue
  - [新建] pai-coding-front/src/components/message/MessageInput.vue
  - [新建] pai-coding-front/src/components/message/MessageHeader.vue
- **实现要点:** 会话列表/消息详情/发消息完整交互, 消息气泡区分左右, Enter/Ctrl+Enter 发送, 字符计数, 上拉加载更多, 自动滚动到底部, 暗色模式使用 --pai-* 变量
- **验证命令:** cd pai-coding-front && npm run build
- **预估复杂度:** XL

### T10: 集成路由和导航栏
- **标题:** 添加消息路由、导航栏消息入口和未读徽标
- **依赖:** T8, T9
- **涉及文件:**
  - [修改] pai-coding-front/src/router/index.ts
  - [修改] pai-coding-front/src/components/layout/HeaderBar.vue
  - [修改] pai-coding-front/src/App.vue
- **实现要点:** 添加 /messages 和 /messages/:conversationId 路由(懒加载+loginRequired), HeaderBar 增加消息图标+徽标, App.vue 初始化 WS 连接和未读数拉取
- **验证命令:** cd pai-coding-front && npm run build
- **预估复杂度:** M

### T11: 实现发私信入口
- **标题:** 在用户主页和文章评论区添加发私信按钮
- **依赖:** T9, T10
- **涉及文件:**
  - [修改] pai-coding-front/src/views/user/UserHomeInfo.vue
  - [修改] pai-coding-front/src/components/comment/CommentAction.vue
- **实现要点:** 仅在 userId != currentUserId 时显示, 点击调用 POST /api/msg/start 获取 conversationId 后跳转, 未登录弹出登录框, 使用 startConversation API
- **验证命令:** cd pai-coding-front && npm run build
- **预估复杂度:** M

### T12: 集成测试和细节完善
- **标题:** 端到端流程验证、暗色模式适配、Lint 检查
- **依赖:** T1 至 T11
- **涉及文件:** 无新增文件，仅验证和微调
- **实现要点:** 后端编译 mvn compile, 前端 type-check+build+lint, 人工验证完整流程(双浏览器互发/已读/未读数/暗色/移动端/自我发送拦截/未登录拦截)
- **验证命令:**
  ```bash
  cd pai-coding-front && npm run type-check && npm run build && npm run lint
  cd paicoding-web && mvn test
  ```
- **预估复杂度:** M

## 7. 可扩展性设计

### 7.1 群聊预留

| 步骤 | 变更内容 |
|------|----------|
| 1 | ConversationTypeEnum 新增 GROUP 枚举值 |
| 2 | ConversationMemberDAO 支持批量插入 2+ 成员 |
| 3 | MsgService.send() 群聊时递增除发送者外所有成员 unread_count |
| 4 | MessageHeader.vue 展示群名称, ConversationItem.vue 展示群头像 |
| 5 | MessageBubble.vue 增加发送者名称/头像 |

### 7.2 消息引用预留

| 步骤 | 变更内容 |
|------|----------|
| 1 | SendMsgReq 新增可选字段 referencedMsgId |
| 2 | MessageBubble.vue 增加 referencedMsg prop, 渲染引用块 |
| 3 | MessageVO 增加 referencedMsg 字段 |
| 4 | 输入框增加引用交互 |

### 7.3 文件分享预留

| 步骤 | 变更内容 |
|------|----------|
| 1 | message_type 增加 IMAGE/FILE 枚举 |
| 2 | 发送 API 增加文件上传端点 |
| 3 | attachment JSON 字段写入文件元数据 |
| 4 | MessageBubble.vue 根据 message_type 渲染图片预览/下载链接 |

### 7.4 数据库迁移策略

- MVP: 001_private_msg.xml 创建 3 张表
- 后续: 002_private_msg_group.xml 追加字段
  - 群聊: conversation.group_avatar, conversation.group_name
  - 置顶: 使用已预留 is_top 字段 (无需 DDL)
  - 归档: message_archive 表或按月分表
- 回滚: 移除 master.xml 中的 include
- 清理: is_deleted=1 软删除, 定时任务物理删除 180 天前记录

---

> 文档版本: v1.0 | 最后更新: 2026-05-08 | 状态: 待实现