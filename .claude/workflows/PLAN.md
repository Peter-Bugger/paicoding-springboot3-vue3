# 技术方案: 私信会话清除功能

## 1. 需求分析

### 1.1 功能需求
- F1: 用户清除私信会话（仅对自己可见，对方不受影响）
- F2: 清除后会话从列表消失，未读数归零，消息历史对当前用户隐藏
- F3: 清除后对方发新消息，会话重新出现（当前用户侧恢复），保留全部历史
- F4: 当前用户主动重新发起会话，已清除的会话恢复（复用原 conversation_id）
- F5: REST API `DELETE /api/msg/conversations/{conversationId}`

### 1.2 非功能需求
- 清除操作是软清除（逻辑标记），不物理删除数据
- 清除操作仅影响当前用户，不影响对方
- 支持后续扩展"回收站"（保留 cleared_at 字段）

### 1.3 边界条件
- 清除后立即通过 `/messages/{id}` 直接访问 → 返回 403
- 清除后对方发消息 → 会话复活，当前用户重新可见
- 清除后当前用户主动发起会话 → 复用已有 conversation_id
- 未读数统计排除已清除的会话
- 对方浏览器 WebSocket 不感知清除事件（清除是本地操作）

---

## 2. 技术选型

### 2.1 方案
- **cleared_at 字段**：放在 `conversation_member` 表，记录清除时间
- **清除标记**：复用现有 `is_deleted=1` 字段，清除时同时设置 `is_deleted=1`, `cleared_at=NOW()`, `unread_count=0`
- **复活策略**：`getOrCreatePrivateConversation` 两步走 — 先查正常，再查已清除
- **查询过滤**：所有会话列表/消息查询增加 `is_deleted=0` 校验

### 2.2 新增依赖
- **后端**：无新增
- **前端**：无新增（已有 `doDelete`）

---

## 3. 架构设计

### 3.1 整体流程

```
[用户 A 清除会话]
  DELETE /msg/api/conversations/{convId}
  → 校验成员关系 + is_deleted=0
  → 更新 conversation_member: is_deleted=1, cleared_at=NOW(), unread_count=0
  → A 的会话列表不再显示该会话
  → A 不能再查看该会话消息
  → B 不受影响

[用户 B 发送消息给 A (A 已清除)]
  POST /msg/api/send {toUserId: A}
  → getOrCreatePrivateConversation 先查活跃 -> 未找到
  → 再查 A 已清除的记录 -> 找到 -> 恢复 A 的 is_deleted=0, cleared_at=NULL
  → 新建消息
  → A 收到 WS 推送，会话在 A 的列表中重新出现

[用户 A 主动给 B 发消息 (A 已清除)]
  POST /msg/api/start 或 send
  → getOrCreatePrivateConversation 先查活跃 -> 未找到
  → 再查已清除 -> 找到 -> 恢复
  → 新建消息或返回会话ID
```

### 3.2 模块划分

```
paicoding-api (修改)
  model/vo/msg/ConversationVO.java     ← 新增 clearedAt 字段 (可选，向前端透传)

paicoding-service (修改)
  msg/repository/entity/ConversationMemberDO.java  ← 新增 clearedAt 字段
  msg/repository/dao/ConversationMemberDAO.java    ← 新增 listDeletedByUserId()
  msg/service/MsgService.java                      ← 新增 clearConversation() 接口
  msg/service/impl/MsgServiceImpl.java             ← 实现清除 + 复活逻辑

paicoding-web (修改)
  controller/msg/rest/MsgController.java           ← 新增 DELETE endpoint
  resources/liquibase/changelog/002_clear_msg.xml  ← 新增 changelog
  resources/liquibase/master.xml                   ← 新增 include

pai-coding-front (修改)
  stores/message.ts                                ← 新增 removeConversation action
  http/MessageRequests.ts                          ← 新增 clearConversation API
  views/messages/ConversationView.vue              ← 清除按钮 + 清除后跳转
  components/message/MessageHeader.vue             ← 新增 clear emit + 清除按钮 UI
```

---

## 4. 数据流设计

### 4.1 清除流程

```
前端: 用户点击清除 → el-MessageBox 确认 → doDelete /msg/api/conversations/{id}
后端: MsgController.deleteConversation()
  → msgService.clearConversation(userId, conversationId)
  → 校验成员关系 (member != null)
  → 校验 is_deleted=0 (防止重复清除)
  → 更新: is_deleted=1, cleared_at=NOW(), unread_count=0
  → 返回 true
前端: 成功后 → messageStore.removeConversation(id) + router.push('/messages')
```

### 4.2 消息查询过滤

```
listConversations():
  → ConversationMemberDAO.listByUserId() 已过滤 is_deleted=0
  → 清除的会话自动不出现 ✓

listMessages():
  → 增加校验: member.getIsDeleted() == 1 → 抛出 "会话已被清除"
  → 清除后无法查看历史消息 ✓

queryUnreadTotal():
  → ConversationMemberDAO.countUnreadByUser() 已过滤 is_deleted=0
  → 清除的会话不计入未读 ✓
```

### 4.3 复活流程

```
getOrCreatePrivateConversation(A, B):
  Step 1: 查询 A 所有活跃会话 (is_deleted=0)
    查找与 B 共同的私有会话 → 找到直接返回
  Step 2 (新增): 查询 A 所有已清除会话 (is_deleted=1)
    查找与 B 共同的私有会话 → 找到则复活:
      is_deleted=0, cleared_at=NULL, unread_count=0
      返回会话ID
  Step 3: 创建新会话 → 返回新ID
```

---

## 5. 接口/组件设计

### 5.1 新增 REST API

| 方法 | 路径 | 说明 | 请求参数 | 响应 |
|------|------|------|----------|------|
| DELETE | `/msg/api/conversations/{conversationId}` | 清除会话(当前用户) | 无 | `ResVo<Boolean>` |

### 5.2 修改的 REST API (内部逻辑变更)

| 方法 | 路径 | 变更说明 |
|------|------|----------|
| POST | `/msg/api/send` | `getOrCreatePrivateConversation` 增加清除项查找 |
| POST | `/msg/api/start` | `getOrCreatePrivateConversation` 增加清除项查找 |
| GET | `/msg/api/messages/{conversationId}` | 增加 `is_deleted=0` 校验, 返回403 |

### 5.3 前端组件变更

**MessageHeader.vue**
- 新增 emit: `clear`
- 右侧新增清除按钮 (自定义 button 样式, 符合项目约定)
- 清除点击不直接执行, 交由父组件处理确认弹窗

**ConversationView.vue**
- `handleClear()`: el-MessageBox.confirm → 调用 clearConversation API → messageStore.removeConversation → router.push('/messages')

### 5.4 WebSocket 推送
清除操作不需要 WebSocket 推送（仅影响清除者自己, 对方完全不受影响）。

---

## 6. 数据库变更

### 6.1 Liquibase changelog 002

```sql
ALTER TABLE `conversation_member`
ADD COLUMN `cleared_at` DATETIME NULL COMMENT '清除时间(用户清除会话时记录)' AFTER `is_deleted`;
```

### 6.2 ConversationMemberDO 变更

新增字段:
```java
/**
 * 清除时间
 */
private Date clearedAt;
```

### 6.3 索引说明
现有索引 `idx_cm_user_deleted_time` (user_id, is_deleted, update_time) 已覆盖已清除记录的查询需求, 无需新增索引。

---

## 7. 影响范围

| 模块 | 变更类型 | 文件数 | 说明 |
|------|----------|--------|------|
| paicoding-api | 修改 | 1 | `ConversationMemberDO.java` 新增 clearedAt 字段 |
| paicoding-service | 修改 | 4 | `MsgService.java` + `MsgServiceImpl.java` + `ConversationMemberDAO.java` |
| paicoding-web | 修改 | 3 | `MsgController.java` + 新 changelog + `master.xml` |
| pai-coding-front | 修改 | 4 | `MessageRequests.ts` + `message.ts` + `ConversationView.vue` + `MessageHeader.vue` |
| 数据库 | 新增列 | 1 changelog | `conversation_member.cleared_at` |
| 第三方依赖 | 无 | 0 | - |

### 具体文件变更

```
修改:
  paicoding-service/src/main/java/com/github/paicoding/forum/service/msg/repository/entity/ConversationMemberDO.java
  paicoding-service/src/main/java/com/github/paicoding/forum/service/msg/repository/dao/ConversationMemberDAO.java
  paicoding-service/src/main/java/com/github/paicoding/forum/service/msg/service/MsgService.java
  paicoding-service/src/main/java/com/github/paicoding/forum/service/msg/service/impl/MsgServiceImpl.java
  paicoding-web/src/main/java/com/github/paicoding/forum/web/controller/msg/rest/MsgController.java
  paicoding-web/src/main/resources/liquibase/master.xml
  pai-coding-front/src/http/MessageRequests.ts
  pai-coding-front/src/stores/message.ts
  pai-coding-front/src/views/messages/ConversationView.vue
  pai-coding-front/src/components/message/MessageHeader.vue

新增:
  paicoding-web/src/main/resources/liquibase/changelog/002_clear_msg.xml
```

---

## 8. 任务拆解

### Task 1: 数据库变更 + DO 层

**文件**:
- 新增: `paicoding-web/src/main/resources/liquibase/changelog/002_clear_msg.xml`
- 修改: `paicoding-web/src/main/resources/liquibase/master.xml` (新增 include)
- 修改: `ConversationMemberDO.java` (新增 clearedAt 字段)

**验证**: mvn compile 通过

### Task 2: 后端 DAO + Service 层

**文件**:
- 修改: `ConversationMemberDAO.java` — 新增 `listDeletedByUserId()` 方法
- 修改: `MsgService.java` — 新增 `clearConversation()` 接口方法
- 修改: `MsgServiceImpl.java` — 实现 clearConversation + 修改 getOrCreatePrivateConversation + 修改 listMessages 增加校验

**验证**: mvn compile 通过

### Task 3: 后端 Controller 层

**文件**:
- 修改: `MsgController.java` — 新增 DELETE endpoint, 统一异常处理

**验证**: mvn compile 通过

### Task 4: 前端 API + Store

**文件**:
- 修改: `MessageRequests.ts` — 新增 `clearConversation()` 函数
- 修改: `message.ts` — 新增 `removeConversation()` action

**验证**: npm run type-check 通过

### Task 5: 前端 UI

**文件**:
- 修改: `MessageHeader.vue` — 新增 clear emit + 清除按钮
- 修改: `ConversationView.vue` — 监听 clear emit + 确认弹窗 + 调用 API + 跳转

**验证**: npm run build-only 通过

### Task 6: 集成验证

- 前端 type-check + build 通过
- 后端 mvn compile 通过
- 清除后会话从列表消失
- 清除后 URL 直接访问返回 403
- 清除后对方发消息, 会话重新出现
- 清除后主动发起会话, 复用原 conversation_id
- 清除后未读数不计入总数

---

## 9. 风险评估与回退方案

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|----------|
| 清除后未读数不一致 | 低 | 中 | 清除时同步置零; countUnreadByUser 已过滤 is_deleted |
| 复活时误恢复已物理删除的会话 | 低 | 中 | 同时校验 conversation.deleted=0 |
| 并发清除/发送导致复活冲突 | 低 | 高 | synchronized 方法保证串行 |
| 前端误清除 | 低 | 低 | 清除前弹窗确认 |

**回退方案**:
1. 删除 `002_clear_msg.xml` changelog 可回滚数据库变更
2. 移除 Controller DELETE endpoint 可禁用清除功能
3. 清除后可通过管理员直接修改 `is_deleted=0` 恢复
