# 审查报告: 私信功能全栈 Review

## 审查摘要
- 审查范围：后端 MsgController + MsgServiceImpl，前端 ConversationView + MessageListView + useMessageWs + store/message
- 审查结论：**APPROVED** (2 轮修复：代码质量 + 功能缺陷)
- Diff 统计：+N / -M 行

## 问题列表

### 🔴 BLOCKER（必须修复） — 全部已修复 ✅

1. `ConversationView.vue:235` — 发送失败时状态误标为 SENT ✅ 改为 FAILED
2. `MsgServiceImpl.java:71-73` — 超长消息静默截断 ✅ 改为抛异常
3. `MsgServiceImpl.java:128-133` — 会话列表内存分页 ✅ DB 层 LIMIT/OFFSET

### 🟢 INFO（可选优化） — 全部已修复 ✅

1. `MessageListView.vue:56` — store 响应式断连 ✅ watch 同步
2. `MsgController / MsgServiceImpl` — 重复校验 ✅ 保留防御性二次校验 + 注释
3. `ConversationView.vue:225` — conversationId 覆盖 ✅ 仅变化时更新
4. `MsgController.java:133` — markAsRead 返回冗余 ✅ ResVo<Boolean>

### 🔴 功能缺陷（运行时问题） — 全部已修复 ✅

1. `useMessageWs.ts:92` — WebSocket 无自动重连 ✅ 指数退避重连（1s→2s→4s→…→30s）
2. `MsgServiceImpl.java:getOrCreatePrivateConversation` — 并发创建重复会话 ✅ synchronized 锁
3. `MsgServiceImpl.java:send` — 移除防御性校验的回归 ✅ 恢复并标注
4. `MsgServiceImpl.java:send` — 消息状态流缺失 DELIVERED ✅ WebSocket 推送后标记

## 循环记录
- 修复轮次：2/3（代码质量 + 功能缺陷）
- 累计修复：11 项（3 BLOCKER + 4 INFO + 4 FUNCTIONAL）
