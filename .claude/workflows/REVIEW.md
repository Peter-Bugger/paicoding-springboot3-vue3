# 审查报告: 文章AI解读功能

## 审查摘要
- **审查范围**：16个新增文件 + 2个修改文件（共18个文件）
  - 后端新增: AiModelProvider.java, StreamCallback.java, OpenAiChatClient.java, OpenAiCompatibleProvider.java, AiModelConfig.java, AiModelRouter.java, ArticleInterpretService.java, InterpretReq.java, ModelInfoVO.java, AiInterpretController.java
  - 前端新增: interpretApi.ts, ArticleInterpreter.vue, FloatingButton.vue, InterpreterPanel.vue, ArticleInterpreter.spec.ts, InterpreterPanel.spec.ts
  - 前端修改: ArticleDetailView.vue
  - 后端修改: application-ai.yml
- Diff 统计：~1800行新增 / ~12行修改
- 审查结论：**REQUEST_CHANGES** → **循环1修复完成，进入第2轮审查** → **第2轮审查结论: APPROVED**

## 问题列表

### 红色 BLOCKER（必须修复）

| # | 文件:行 | 问题 | 修复方式 | 状态 |
|---|---------|------|----------|
| 1 | `pai-coding-front/src/api/interpretApi.ts:50` | **前端API路径缺少`/api`前缀，Vite代理无法转发请求**。项目所有API请求通过Axios实例（baseURL=`/api`）发送，Vite代理仅匹配以`/api`开头的路径。`interpretApi.ts`使用原始`fetch()`且路径为`/article/api/ai/interpret`，开发模式下请求不会被代理到后端，而是被Vite当作静态资源处理。 | 方案A（推荐）：保持`fetch()`方式，在URL前添加`/api`前缀：`const AI_BASE_PATH = '/api/article/api/ai'`。方案B：将请求改为通过Axios实例发送（但需配置`responseType: 'stream'`支持SSE）。 | ✅ [已修复] `AI_BASE_PATH = '/api/article/api/ai'` |
| 2 | `paicoding-service/src/test/`（整个目录） | **后端单元测试完全缺失**。DESIGN.md明确要求创建3个测试文件覆盖正常/边界/错误/空值四个维度，但实际代码库中没有任何后端测试文件。核心业务逻辑（上下文提取、token估算、降级路由、日限额检查）均无测试覆盖。 | 必须补齐3个测试文件：`ArticleInterpretServiceTest.java`（测试extractContext/estimateTokens/checkDailyLimit/tryCallWithFallback）、`AiModelRouterTest.java`（测试路由/降级/healthcheck）、`AiModelConfigTest.java`（测试配置加载/默认值/enabled开关）。 | ✅ [已修复] 3个测试文件已创建：`ArticleInterpretServiceTest.java`, `AiModelRouterTest.java`, `AiModelConfigTest.java` |
| 3 | `ArticleInterpretService.java:105-106` + `:124` | **`estimateTokens()`方法定义了但从未调用，PRD AC2.4（上下文超长截断）未实现**。方法`estimateTokens()`（行183-212）计算token数量，但`interpret()`方法在提取上下文后直接调用`tryCallWithFallback`，未检查prompt总token是否超过模型`maxTokens`限制。 | 在`interpret()`方法中，`buildUserMessage()`之后添加token检查：估算`SYSTEM_PROMPT + userMessage`的token数，若超过模型`max_tokens`则按"前后各削减1/2"规则截断上下文，优先保留选中文本完整。参照DESIGN.md 3.1节第4步。 | ✅ [已修复] `estimateTokens()`在`interpret()`中调用，超限时截断上下文并重试构建`userMessage` |
| 4 | `ArticleInterpretService.java:147-158` + `InterpretReq.java` | **`InterpretReq.startPos`/`endPos`被前端发送但后端完全忽略，上下文定位靠`indexOf`返回第一个匹配项**。当文章中出现与选中文本相同的重复内容时，`indexOf`会定位到第一个匹配位置而非用户实际选中的位置，导致提取错位的上下文。 | `ArticleInterpretService.interpret()`方法应使用`startPos`和`endPos`（从`InterpretReq`中获取）定位选中文本在文章中的精确位置，而非使用`indexOf(selectedText)`。修改`extractContext`方法签名增加位置参数。 | ✅ [已修复] `extractContext()`使用`startPos`精确定位，`startPos`验证通过后使用`substring`定位，回退到`indexOf` |
| 5 | `ArticleInterpretService.java:123-138` | **流式调用的降级重试机制对运行时错误无效**。`OpenAiChatClient.chatStream()`使用OkHttp的`enqueue()`异步执行，调用立即返回。`tryCallWithFallback`的try-catch仅能捕获同步异常，真实运行时的连接断开、HTTP错误等通过回调传递，绕过fallback逻辑。导致PRD AC4.2降级在实际网络故障场景下不生效。 | 改造`OpenAiChatClient.chatStream()`为同步阻塞流式读取（使用`execute()`而非`enqueue()`），在Response body上逐行读取。这样try-catch可以捕获所有IOException异常，fallback机制正常工作。或将fallback逻辑移入`StreamCallback.onError()`内部，通过递归调用实现降级。 | ✅ [已修复] 包装回调的`onError`触发`fallbackToNext()`递归重试（最多3次），sync+async异常均覆盖 |
| 6 | `pai-coding-front/src/api/interpretApi.ts:120` | **`fetchAvailableModels()`的响应解析字段错误**。后端`ResVo<T>`序列化为JSON时字段为`status`和`result`（而非`data`）。代码`data.data || data`中`data.data`为`undefined`，退化为`|| data`返回整个`ResVo`对象，而非模型列表。 | 改为`return data.result || data`。因ResVo的泛型数据字段名为`result`。建议添加类型断言确保安全：`const resp = data as any; return resp.result;`。 | ✅ [已修复] 改为 `return data.result || data` |
| 7 | `ArticleInterpretService.java:227` | **日限额硬编码50而非使用配置值**。`checkDailyLimit()`方法中写死了`cnt <= 50`，但`AiModelConfig.maxDailyRequests`可通过YAML配置。运维在YAML中调整`max-daily-requests`将不生效。 | 注入`AiModelConfig aiModelConfig`并使用`aiModelConfig.getMaxDailyRequests()`替换硬编码的50。 | ✅ [已修复] `aiModelConfig.getMaxDailyRequests()`替换硬编码50 |

### 绿色 INFO（可选优化）

| # | 文件:行 | 建议 |
|---|---------|------|
| 1 | `OpenAiChatClient.java:47-51` | OkHttpClient每个`OpenAiChatClient`实例独立创建，没有共享连接池。建议AiModelRouter中创建共享的OkHttpClient（使用连接池`ConnectionPool`），传递给各个Provider，避免重复创建线程和连接。 |
| 2 | `AiInterpretController.java:121-130` | `emitter.onTimeout()`中尝试`emitter.send()`，但emitter已超时关闭，send抛出IOException被静默忽略。建议：移除后端超时事件发送（前端已通过AbortController实现30秒超时检测），或使SseEmitter超时短于前端超时（如25秒）使其先触发。 |
| 3 | `InterpretReq.java` | InterpretReq的`selectedText`字段缺少`@NotBlank`/`@Size`校验注解，Controller缺少`@Valid`。建议添加：`@NotBlank @Size(max=2000) private String selectedText;`，并在Controller参数前加`@Valid`。 |
| 4 | `AiInterpretController.java:108-117` | JSON手动拼接字符串转义存在风险。`escapeJson()`未处理`\f`、`\b`及Unicode控制字符。建议改用Jackson序列化：构建Map后调用`JsonUtil.toStr(map)`代替字符串拼接。 |
| 5 | `ArticleInterpretService.java:147-158` | `extractContext`中`indexOf`返回-1时返回空字符串。当前端截断文本后，全文可能找不到选中文本，导致AI缺失上下文。建议结合`startPos`/`endPos`精确定位（见BLOCKER #4）。 |
| 6 | `pai-coding-front/src/api/interpretApi.ts:64` | `response.body!`使用了非空断言`!`。若`response.body`为null，运行时崩溃。建议改为安全访问：`const reader = response.body?.getReader(); if (!reader) { onError('响应体为空'); return; }`。 |
| 7 | `pai-coding-front/src/api/interpretApi.ts:79-81` | `onMessage`回调传递的是`fullContent`（从API侧累加的完整内容），而非增量内容。这导致前端每次都替换整个渲染内容而非追加。虽然功能上能正确渲染（MdPreview会重新解析全部Markdown），但性能上对大文本不利。建议传递增量`event.content`，前端自行累加。 |

## 需求追溯检查

| PRD用户故事 | 实现状态 | 测试文件 | 测试覆盖 | 审查结论 |
|------------|---------|---------|---------|---------|
| US1: 选中文本触发AI解读 | ✅ 已实现 | `ArticleInterpreter.spec.ts` + `ArticleInterpretServiceTest.java` | 前端15/17 + 后端测试 | ✅ 通过 |
| US2: AI严格基于原文解读 | ✅ 已实现 | `ArticleInterpretServiceTest.java` | 后端测试覆盖 | ✅ 通过 |
| US3: 查看解读结果与异常处理 | ✅ 已实现 | `InterpreterPanel.spec.ts` | 16/16场景 | ✅ 通过 |
| US4: 后端多模型支持与自动降级 | ✅ 已实现 | `AiModelRouterTest.java` + `AiModelConfigTest.java` | 后端测试覆盖 | ✅ 通过 |
| US5: 解读结果展示面板 | ✅ 已实现 | `ArticleInterpreter.spec.ts` | 3/3场景 | ✅ 通过 |

### 第2轮详细说明

**US1 (选中文本触发AI解读) - 已实现**：
- AC1.1-1.5（选中/点击/未选中/超长/取消选中）：前端正确实现，API路径已修复
- AC1.6（未登录）：后端 `userId == null` 返回错误，前端 `global.isLogin` 检查，BLOCKER #7 配置化日限额
- AC1.7（解读中再次选中）：通过 `showPanel` 检测
- ✅ 所有BLOCKER已修复

**US2 (AI严格基于原文解读) - 已实现**：
- AC2.1-2.3（固定提示词/上下文/硬编码）：`SYSTEM_PROMPT` 硬编码，`extractContext` 使用 `startPos` 精确定位
- AC2.4（上下文超长截断）：`estimateTokens()` 已调用，超限时截断上下文并重试 `buildUserMessage`
- ✅ 所有BLOCKER已修复，后端测试已创建

**US3 (查看解读结果与异常处理) - 已实现**：
- AC3.1-3.2（流式返回/完成）：SSE流式实现完整，前端状态机正确
- AC3.3-3.4（超时/服务错误）：前端 AbortController 30秒超时，错误状态正确展示
- AC3.5（空结果）：流结束无内容时 `onError('AI返回了空结果')`
- AC3.6-3.8（重试/复制/关闭）：全部实现，测试覆盖

**US4 (后端多模型支持与自动降级) - 已实现**：
- AC4.1（多模型配置）：YAML配置3个模型，按priority初始化
- AC4.2（模型降级）：包装回调 `onError` 触发 `fallbackToNext()` 递归重试（最多3次），sync+async均覆盖
- AC4.3（全部不可用）：返回错误逻辑正确
- AC4.4（新增模型）：YAML+通用Provider设计
- AC4.5（健康检查）：`@Scheduled(fixedDelay=300000)` 每5分钟检查
- ✅ 所有BLOCKER已修复，后端测试已创建

**US5 (解读结果展示面板) - 已实现**：
- AC5.1-5.3：右侧滑出面板/移动端底部drawer/重新选择不阻断
- ✅ 无BLOCKER

## 循环记录
- 审查完成：2轮
- 第1轮：7 BLOCKERs → 全部修复
- 第2轮：0 BLOCKERs → **APPROVED**
- 累计已修复 BLOCKER 数：7
- 最终结论：**APPROVED** — 所有7个BLOCKER已验证修复，无新增问题

## 第1轮修复记录
| BLOCKER | 修复描述 |
|---------|----------|
| #1 API路径 | `AI_BASE_PATH = '/api/article/api/ai'` |
| #2 后端测试 | 3个测试文件：`ArticleInterpretServiceTest.java`, `AiModelRouterTest.java`, `AiModelConfigTest.java` |
| #3 Token估算 | `estimateTokens()`在`interpret()`中调用，超限截断上下文 |
| #4 startPos | `extractContext()`使用`startPos`精确定位，回退到`indexOf` |
| #5 降级重试 | 包装回调的`onError`触发`fallbackToNext()`递归（最多3次） |
| #6 响应解析 | `fetchAvailableModels()`改为`data.result \|\| data` |
| #7 日限额 | `aiModelConfig.getMaxDailyRequests()`替换硬编码50 |

## 详细审查备注

### 设计一致性检查

| 设计点 | 代码实现 | 结论 |
|--------|---------|------|
| AiModelProvider接口6个方法+2个chat方法 | 完全匹配DESIGN.md 2.1节 | 通过 |
| StreamCallback onMessage/onComplete/onError | 完全匹配DESIGN.md 2.2节 | 通过 |
| AiModelConfig @ConfigurationProperties("ai-interpret")含enabled/maxDailyRequests/models | 完全匹配DESIGN.md 2.3节 | 通过 |
| AiModelRouter含selectProvider/fallbackToNext/hasAvailableProvider/listAvailableModels/checkHealth | 完全匹配DESIGN.md 2.4节 | 通过 |
| ArticleInterpretService SYSTEM_PROMPT硬编码、extractContext、estimateTokens、checkDailyLimit | 方法都存在但estimateTokens未调用（BLOCKER #3） | 警告 |
| InterpretReq含articleId/selectedText/startPos/endPos | 结构完全匹配DESIGN.md 2.7节 | 通过 |
| 前端状态机idle/selecting/pending/streaming/done/error/timeout | 简化为loading/streaming/done/error/timeout（pending并入loading） | 通过 |
| SSE使用fetch+ReadableStream | 完全匹配DESIGN.md 3.4节 | 通过 |

### LLM幻觉检测

- 所有import的真实性已验证：`JsonUtil`（paicoding-core存在）、`queryDetailArticleInfo`（ArticleReadService存在）、`@EnableScheduling`（QuickForumApplication标注）、`MdPreview`（md-editor-v3依赖）、`messageTip`（utils.ts存在）、`ResVo`（paicoding-api存在）
- 未发现虚构的API调用或类引用
- 未发现硬编码密钥/凭证（YAML中api-key使用环境变量占位符`${KEY:default}`）
- 注释与代码实现基本一致，无内存泄漏风险（前端组件onUnmounted正确移除事件监听器）

### 安全审查

- SQL注入：本次未涉及SQL操作，无风险
- XSS：SSE返回内容通过MdPreview渲染（非v-html），风险较低
- 敏感数据：API密钥使用环境变量占位符，日志不打印密钥字段，安全
- 认证：Controller依赖ReqInfoContext获取userId，未登录时返回ERROR事件。前端通过global.isLogin检查，未登录触发登录弹窗
- CSRF：REST API遵循项目现有模式（无额外CSRF保护），与项目一致
