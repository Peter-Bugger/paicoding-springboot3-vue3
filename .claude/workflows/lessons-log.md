# 经验日志

> 项目: {{PROJECT_NAME}} | 初始化: {{INIT_DATE}}
> 质量门槛和条目模板由全局层定义，见 `~/.claude/skills/multi-agent-dev/project-init-template/lessons-log.md`

## 质量门槛（追加前必过）
- [ ] **可操作**：能转化为具体的行为改变？
- [ ] **可泛化**：不局限于当前bug/feature？
- [ ] **作用域判定**：`[project]` / `[stack: xxx]` / `[global]`
- 三个都 ✅ 才追加。

## 条目模板
```
### YYYY-MM-DD | 类型 | 作用域
- ✅ 做得好的
- 💡 新认知
- 🔧 流程改进点
- 🎯 建议推广到
- 📌 已提炼为模式 #N
```

> 被提炼进模式后，删除本条。超过30条后旧条目归档到 `lessons-archive.md`（不加载到上下文）。

## 条目

### 2026-05-08 | 流程改进 | [project]
- 🔧 **流程改进点**: 在现有的 @EnableWebSocketMessageBroker 配置上追加 broker 前缀（`/msg`），而非创建重复的 @EnableWebSocketMessageBroker 配置。两个 WS 功能（AI 聊天 + 私信）共用一个 STOMP 基础设施，避免启动冲突。
- ✅ **做得好的**: REVIEW 阶段使用 `grep` 检查 @EnableWebSocketMessageBroker 出现次数 + 验证 WsChatConfig 包含 "/msg" 前缀，捕获了本可能到集成阶段才暴露的冲突。
- 💡 **新认知**: ConcurrentHashMap 存储 userId->sessionId 映射适合单节点 MVP，扩展到多节点时需替换为 Redis 广播（SimpMessagingTemplate 支持外部 broker）。
- 🎯 **建议推广到**: Spring Boot + STOMP 项目

### 2026-05-08 | 技术实践 | [project]
- 🔧 **流程改进点**: 会话/消息列表 API 采用"先批量查询、后构建 VO"的 4 步模式（查成员 ID -> 批量查会话 -> 批量查最后消息 -> 批量查用户），避免循环内 N+1 查询。
- ✅ **做得好的**: REVIEW 阶段特别关注了 listConversations 方法的循环模式，确认全部使用 IN 批量查询替代了循环内单条查询。
- 💡 **新认知**: MyBatis-Plus 的 lambdaQuery().in() 配合 stream().collect(Collectors.toMap()) 是构建 VO 批量映射的高效组合。
- 🎯 **建议推广到**: 所有列表类 API 实现

### 2026-05-08 | 技术实践 | [project]
- 🔧 **流程改进点**: 跨模块的 userId-sessionId 映射应定义在 service 层（`paicoding-service`），从 web 层（`paicoding-web`）的手持拦截器中填充，保持依赖方向正确（web -> service）。
- ✅ **做得好的**: REVIEW 中确认了映射变量定义在 MsgPushHelper（service 模块）、填充在 MsgHandshakeInterceptor（web 模块），无跨模块非法依赖。
- 💡 **新认知**: `static final ConcurrentHashMap` 是模块间共享状态的最轻量方式，适合 MVP 阶段。

### 2026-05-08 | 流程改进 | [project]
- 🔧 **流程改进点**: 集成验证阶段应遵循"后端编译 -> 前端 type-check -> 后端打包 -> 前端 lint"的固定顺序。后端先编译可快速暴露 Java 语法/依赖问题，再跑前端步骤可避免来回切换上下文。
- ✅ **做得好的**: 所有 4 个硬门槛步骤（mvn compile, npm run type-check/build-only, mvn package, npm run lint）一次性全部通过，无需进入修复循环。
- 💡 **新认知**: 集成验证中 `mvn package` 不仅验证编译，还验证 jar 打包和 spring-boot repackage 阶段（包括 test compile），能暴露 maven-plugin 配置问题。比单纯 `mvn compile` 更全面。
- 🎯 **建议推广到**: 所有包含 Spring Boot 后端的项目集成验证。

---
*统计: 7条 | 已提炼: 0条 | 活跃: 7条 | 归档阈值: 30条*

### 2026-05-27 | 新发现 | [stack: SpringBoot3]
- 💡 **新认知**: Spring Boot 3 + surefire 默认使用 JUnit 5（Jupiter）引擎。测试类若使用 JUnit 4 注解（`@org.junit.Test`、`@RunWith`、`@Before`），surefire 无法发现测试用例（Tests run: 0），除非添加 junit-vintage-engine 依赖。
- 🔧 **流程改进点**: 在 Spring Boot 3 项目的新测试中应直接使用 JUnit 5 注解（`@org.junit.jupiter.api.Test`、`@ExtendWith`、`@BeforeEach`），避免集成时才发现测试不运行。
- ✅ **做得好的**: 集成阶段通过排查 surefire 输出 + 检查编译后的 class 文件存在性，准确定位了根因是 JUnit 版本不匹配而非测试代码问题。

### 2026-05-27 | 新发现 | [stack: Vue3]
- 💡 **新认知**: Vue 3 模板中，`v-else-if` 指令不能跨越非 HTML 元素包装器（如 `<Transition>`）与前面的 `v-if` 配对。编译器报错 `v-else/v-else-if has no adjacent v-if or v-else-if`。
- 🔧 **流程改进点**: 当需要在不同包装器中分别使用 v-if/v-else 时，改为在每个元素上使用完整的条件 v-if（如 `v-if="!isMobile && visible"`），而非依赖 v-else-if 链。

### 2026-05-27 | 新发现 | [project]
- 💡 **新认知**: 多模块 Maven 项目中，`mvn test -pl <module> -Dtest=<TestClass>` 需要加 `-am`（also-make）标志，才能让 reactor 构建顺序中包含依赖模块并编译其中的新增源文件。不加 `-am` 时，依赖模块的新 class 不会被重新编译，导致测试编译失败。
- 🔧 **流程改进点**: 在多模块 Maven 项目中执行选择性测试时，始终使用 `-am` 标志。
