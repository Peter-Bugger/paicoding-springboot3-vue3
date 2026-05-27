# 技术方案: 文章AI解读功能

## 1. 需求分析

### 功能需求

1. **文本选中触发**: 用户在文章详情页用鼠选中任意文本（1~2000字符），在选中区域附近显示浮动「AI解读」按钮
2. **流式解读返回**: 点击按钮后，前端通过REST API发起请求，后端调用AI大模型流式返回解读结果，前端以Markdown格式渲染展示
3. **严格基于原文**: 后端硬编码系统提示词，基于选中文本+前后各500字上下文+文章标题，约束AI不准脱离原文
4. **多模型自动降级**: 支持至少5个模型提供商，按优先级自动选择，失败时自动切换到下一个
5. **解读结果面板**: 右侧滑动面板展示结果，支持复制、重试、关闭；移动端自适应为底部抽屉式

### 非功能需求

- 首字响应 P95 < 3秒，完整响应 P95 < 5秒
- 解读成功率 > 99%（含自动降级）
- 并发支持 >= 50 req/s
- 用户日调用限制 50次/天
- 超时 30秒
- 浏览器兼容 Chrome/Firefox/Safari/Edge 最新2版本

### 边界条件

| 边界条件 | 预期行为 |
|----------|----------|
| 选中文本为空 | 不显示AI解读按钮 |
| 选中文本超长(>2000) | 前端截断到2000字符+提示 |
| 上下文+prompt超token | 后端截断上下文（优先保留选中文本） |
| 用户未登录 | 弹出登录提示 |
| 解读进行中再次选中 | 提示「AI正在解读中」 |
| 所有模型不可用 | 返回503 |
| 网络中断 | 显示已接收内容 + 连接中断提示 |
| 限流触发(>50次/天) | 返回429 |
| 特殊字符 | 正确转义处理 |
| 文章内容为空 | 返回400 |

### 用户优先级选择：[稳健交付]（待用户确认）


### PRD 验收条件摘要

| PRD用户故事 | Given/When/Then 验收条件 | 覆盖章节 |
|------------|-------------------------|----------|
| US1(AC1.1-AC1.2) | 选中文本>0且<=2000 -> 显示浮动按钮；点击发送请求 | Section 3, Section 5 |
| US1(AC1.3) | 未选中 -> 不显示按钮 | Section 3 |
| US1(AC1.4) | 选中超2000 -> 截断+提示+仍显示按钮 | Section 3 |
| US1(AC1.5) | 取消选中 -> 按钮消失 | Section 3 |
| US1(AC1.6) | 未登录 -> 弹出登录提示 | Section 5(Permission) |
| US1(AC1.7) | 解读进行中 -> 提示 | Section 3 |
| US2(AC2.1-AC2.3) | 后端构建请求 -> 使用硬编码提示词 | Section 3, Section 5 |
| US2(AC2.2) | 构建请求 -> 选中文本+前后500字+标题 | Section 4 |
| US2(AC2.4) | 超token限制 -> 优先保留选中文本截断 | Section 3 |
| US3(AC3.1-AC3.2) | 正常流式返回 -> Markdown逐字渲染+复制按钮 | Section 5 |
| US3(AC3.3) | 30秒超时 -> 提示+重试按钮 | Section 5 |
| US3(AC3.4-AC3.6) | 错误/空结果 -> 提示+重试按钮 | Section 5 |
| US3(AC3.7) | 点击复制 -> 复制到剪贴板+toast | Section 3 |
| US3(AC3.8) | 关闭面板 -> 面板关闭，不清除选中 | Section 3 |
| US4(AC4.1-AC4.3) | 多模型配置 -> 按优先级选择，失败切换 | Section 3 |
| US4(AC4.4) | 新增配置 -> 自动加入可用池 | Section 3 |
| US4(AC4.5) | 健康检查 -> 多次失败标记冷却5min | Section 3 |
| US5(AC5.1) | 展示结果 -> 右侧滑动面板 | Section 3 |
| US5(AC5.2) | 移动端 -> 底部抽屉式 | Section 3 |
| US5(AC5.3) | 面板展示中选中新段落 -> 按钮再次出现 | Section 3 |

### 非功能需求清单

| 类别 | 需求 | 目标值 | 说明 |
|------|------|--------|------|
| 性能 | API首字响应时间 | P95 < 3秒 | 从请求到第一个SSE token返回 |
| 性能 | 完整响应时间 | P95 < 5秒 | 从请求到STREAM_END事件 |
| 可靠性 | 解读成功率 | > 99% | 含自动降级后的成功率 |
| 并发 | 并发解读请求 | >= 50 req/s | 正常业务峰值 |
| 限流 | 用户日调用限制 | 50次/天 | Redis计数器实现 |
| 超时 | 单次请求超时 | 30秒 | 含模型降级重试时间 |
| 可用性 | 模型可用率 | > 99.5% | 至少1个模型在线 |
| 浏览器 | 兼容性 | Chrome/Firefox/Safari/Edge 最新2版本 | 含移动端响应式 |
| 安全 | 输入校验 | 前后端双重校验 | selectedText长度、articleId存在性 |
| 安全 | 认证 | Permission(UserRole.LOGIN) | 仅登录用户可调用解读API |

## 2. 技术选型

### 框架/库/工具及理由

| 技术 | 用途 | 理由 |
|------|------|------|
| Spring Boot 3 + SseEmitter | REST SSE流式响应 | 项目已有，无需新增依赖；Spring原生SSE支持 |
| OkHttp3 | HTTP客户端调用AI API | 项目已有（XunFeiIntegration），支持异步回调 |
| md-editor-v3 | Markdown渲染 | 项目已有（ArticleDetail.vue），复用 |
| Redis + 现有计数模式 | 用户日调用限流 | 复用AbsChatService中的HINCR + EXPIRE模式 |
| Element Plus | UI组件 | 项目已有UI库 |
| Jackson (JsonUtil) | JSON序列化 | 项目已有核心工具类 |

### 是否需要新增依赖

**无需新增任何依赖**。

### 技术权衡：[方案B - 适度抽象]（默认推荐，待用户确认）

**方案B（推荐）**: 抽象出统一的 AiModelProvider 接口，各模型通过配置+实现类注册。文章解读作为独立的使用场景，通过 AiModelRouter 自动路由到可用模型。

**选择理由**:
1. 项目现有基础设施（XunFeiIntegration的OpenAI兼容调用模式）天然适合提取为通用层
2. 产品要求至少3个新模型提供商 + 现有模型，适度抽象是合理规模
3. 不影响现有Chat对话功能（XunFeiAiServiceImpl可继续使用提取后的OpenAiChatClient）
4. 相比方案A，新模型仅需实现接口 + YAML配置
5. 相比方案C，改动量可控，风险低，符合稳健交付策略

## 3. 架构设计

### 整体架构



### 模块划分

| 模块 | 包路径 | 职责 |
|------|--------|------|
| paicoding-api | ...api.model.vo.articleinterpret | InterpretReq, SseEventVo, ModelInfoVO |
| paicoding-api | ...api.model.enums.ai | 扩展AISourceEnum（新增DEEP_SEEK, KIMI, TONG_YI） |
| paicoding-service | ...service.articleinterpret | ArticleInterpretService, ArticleInterpretLimitService |
| paicoding-service | ...service.chatai.service.impl.provider | AiModelProvider接口 + 各模型实现 |
| paicoding-service | ...service.chatai.service.impl | OpenAiChatClient（从XunFeiIntegration提取） |
| paicoding-service | ...service.chatai.service.impl | AiModelRouter, AiModelProviderConfig |
| paicoding-web | ...web.controller.article.rest | ArticleInterpretRestController |
| pai-coding-front | src/components/article/ | ArticleInterpreter.vue（新增） |
| pai-coding-front | src/components/article/ | ArticleDetail.vue（修改：集成interpreter） |
| pai-coding-front | src/http/ | 新增API URL常量 + 响应类型定义 |

### 模块间依赖关系




## 4. 数据流设计

### 核心数据流转路径

前端选中文本 -> mouseup -> getSelection() -> 判断长度
  - 空/仅空白 -> 不显示按钮
  - >2000字符 -> 截断 + 提示
  - 1~2000字符 -> 显示浮动按钮

点击 AI解读 按钮 -> 检查登录状态 -> POST /article/api/ai/interpret
  body: {articleId, selectedText, startPos, endPos}

后端处理链:
  ArticleInterpretRestController
    - Permission(UserRole.LOGIN) 认证检查
    - 参数校验: articleId存在, selectedText非空
    - 调用 interpretService.interpret(req)

  ArticleInterpretService.interpret()
    - 日限流检查: Redis HINCR
    - 获取文章内容: articleReadService.queryDetailArticleInfo(articleId) [SENIOR-DEV AMENDMENT: 使用queryDetailArticleInfo避免触发阅读计数+1等副作用]
    - 提取上下文: 选中位置前后各500字符
    - Token截断: 如超限, 优先保留选中文本
    [SENIOR-DEV AMENDMENT: Token估算公式: estimatedTokens = 中文字符数*1.5 + 英文字母数*0.25 + 英文单词数*0.75。截断时先保留选中文本完整，对前后上下文各削减1/2，重复直到估算token数低于maxToken限制]
    - 构建Prompt: system(硬编码) + user(标题+上下文+选中文本)
    - 调用 aiModelRouter.streamInterpret()

  AiModelRouter.streamInterpret()
    - 获取可用模型列表（按YAML配置优先级）
    - 过滤冷却中的模型（5min内失败过2次）
    - 尝试Provider，失败则fallback下一个
    - 全部失败则回调ERROR

  SseEmitter事件流:
    data: {"type":"STREAM","content":"文本片段..."}
    data: {"type":"STREAM_END","content":"完整解读文本"}
    data: {"type":"ERROR","message":"错误信息"}

### 状态管理方案

前端状态（ArticleInterpreter.vue组件局部状态，不使用全局store）：

- selectedText / startPos / endPos: 选中状态
- showButton / buttonPosition / panelVisible: UI状态  
- status: idle / loading / streaming / complete / error / timeout
- interpretedContent: 累积的解读文本
- errorMessage: 错误信息

### 关键数据结构

```java
// InterpretReq - paicoding-api
public class InterpretReq {
    private Long articleId;
    private String selectedText;
    private Integer startPos;
    private Integer endPos;
}

// SseEventVo - paicoding-api
public class SseEventVo {
    private String type;    // STREAM / STREAM_END / ERROR
    private String content;
    private String message;
}

// ModelInfoVO - paicoding-api
public class ModelInfoVO {
    private String id;
    private String name;
    private boolean available;
}

// AiModelProvider 接口 - paicoding-service
public interface AiModelProvider {
    String getId();
    String getName();
    boolean isAvailable();
    void markFailure();
    int getPriority();
    void streamCall(String systemPrompt, String userMessage,
                    OpenAiChatClient.StreamCallback callback);
}

// AiModelProviderConfig - paicoding-service
@ConfigurationProperties(prefix = "ai-interpret")
public class AiModelProviderConfig {
    private List<ModelProviderConf> providers;
    // ModelProviderConf: {id, name, apiHost, apiKey, model, maxToken, timeout, priority}
}
```

## 5. 接口设计

### 5.1 后端 API

#### POST /article/api/ai/interpret - 文章AI解读(流式)

方法: POST
路径: /article/api/ai/interpret
认证: 需要登录 (@Permission(UserRole.LOGIN))
Content-Type: application/json
Accept: text/event-stream

请求体:
```json
{
  "articleId": 123,
  "selectedText": "选中需要解读的文本内容",
  "startPos": 100,
  "endPos": 200
}
```

字段说明:
- articleId (Long, required): 文章ID
- selectedText (String, required): 用户选中的文本，1~2000字符
- startPos (Integer, required): 起始字符偏移
- endPos (Integer, required): 结束字符偏移

SSE事件流响应 (text/event-stream):
```
data: {"type":"STREAM","content":"这是解读的第一..."}
data: {"type":"STREAM","content":"这是解读的后续内容..."}
data: {"type":"STREAM_END","content":"完整解读文本内容(Markdown格式)"}
data: {"type":"ERROR","message":"今日解读次数已用完(50/50)"}
```

SSE事件类型:

| type | content字段 | message字段 | 说明 |
|------|------------|-------------|------|
| STREAM | 本次返回的文本片段 | - | 流式内容块 |
| STREAM_END | 完整的解读文本(Markdown) | - | 解读完成 |
| ERROR | - | 错误描述信息 | 发生错误 |

错误码:

| HTTP状态码 | 场景 | message示例 |
|-----------|------|-------------|
| 400 | 参数校验失败 | 文章内容为空 / 文章不存在 / 选中文本不能为空 |
| 401 | 未登录 | 请先登录 |
| 429 | 日限额用完 | 今日解读次数已用完(50/50) |
| 503 | 所有模型不可用 | AI服务暂不可用，请稍后重试 |
| 500 | 服务内部异常 | 解读失败，AI服务暂不可用 |

#### GET /article/api/ai/models - 获取可用模型列表

方法: GET
路径: /article/api/ai/models
认证: 不需要登录
响应体:
```json
[
  {"id": "deepseek-chat", "name": "DeepSeek", "available": true},
  {"id": "moonshot-v1", "name": "Kimi", "available": true},
  {"id": "qwen-max", "name": "通义千问", "available": false}
]
```

### 5.2 前端组件

#### ArticleInterpreter.vue

组件结构:
- FloatingButton: 浮动AI解读按钮，基于选中区域计算绝对定位
  - 显示条件: selectedText长度>0 且 status非loading/streaming
  - loading状态: 显示AI正在解读中提示
- InterpretationPanel: 解读结果面板
  - 桌面端: el-drawer (右侧滑出, size=480px)
  - 移动端: el-drawer (底部滑出, direction=btt, size=60%)
  - 加载态: el-skeleton 骨架屏
  - 流式态: md-editor-v3 MdPreview 组件渲染
  - 完成态: 复制按钮 + 解读完成标记
  - 错误态: el-result error显示 + 重试按钮
  - 空结果: AI暂时无法解读此内容提示

Props:
```typescript
interface ArticleInterpreterProps {
  articleId: number
  articleTitle: string
  articleContent: string
}
```

Events: 组件使用内部状态管理，需要登录时调用 inject 的 showLoginDialog

SSE消费方式:
```typescript
// 使用 fetch + ReadableStream (EventSource不支持POST请求体)
const response = await fetch('/api/article/api/ai/interpret', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
  body: JSON.stringify({ articleId, selectedText, startPos, endPos })
})
const reader = response.body.getReader()
const decoder = new TextDecoder()
// 逐行解析 SSE data: {...} 事件
// 超时: AbortController + 30秒 timeout
```

### 5.3 硬编码系统提示词

```java
// ArticleInterpretService 中的常量
private static final String SYSTEM_PROMPT =
    "你是文章专属解读助手，只能基于提供的原文解释选中片段，" +
    "不准脱离原文，不准编造内容，语言通俗易懂，条理清晰。";

private static final String USER_PROMPT_TEMPLATE =
    "文章标题: %s

" +
    "上下文（选中片段前后各500字符）：
%s

" +
    "请解读以下原文片段：
%s";
```


## 6. 影响范围（基于代码库实际搜索验证）

### 6.1 新增文件（共17个）
[SENIOR-DEV AMENDMENT: 合并3个独立Provider为1个通用OpenAiCompatibleProvider，新增ArticleInterpretConstants + 2个提取的DTO类]

| 序号 | 文件路径 | 说明 |
|------|----------|------|
| 1 | paicoding-api/.../vo/articleinterpret/InterpretReq.java | 解读请求DTO |
| 2 | paicoding-api/.../vo/articleinterpret/SseEventVo.java | SSE事件VO |
| 3 | paicoding-api/.../vo/articleinterpret/ModelInfoVO.java | 模型信息VO |
| 4 | paicoding-service/.../articleinterpret/ArticleInterpretService.java | 解读核心服务 |
| 5 | paicoding-service/.../articleinterpret/ArticleInterpretLimitService.java | 日限流服务 |
| 6 | paicoding-service/.../chatai/service/impl/OpenAiChatClient.java | OpenAI通用客户端 |
| 7 | paicoding-service/.../chatai/service/impl/AiModelProvider.java | 模型提供者接口 |
| 8 | paicoding-service/.../chatai/service/impl/AiModelRouter.java | 模型路由器 |
| 9 | paicoding-service/.../chatai/service/impl/AiModelProviderConfig.java | 模型配置类 |
| 10 | paicoding-service/.../chatai/service/impl/provider/DeepSeekModelProvider.java | DeepSeek适配 |
| 11 | paicoding-service/.../chatai/service/impl/provider/KimiModelProvider.java | Kimi适配 |
| 12 | paicoding-service/.../chatai/service/impl/provider/TongyiModelProvider.java | 通义千问适配 |
| 13 | paicoding-service/.../chatai/service/impl/provider/XunFeiModelProvider.java | 讯飞适配(包装现有服务) |
| 14 | paicoding-web/.../article/rest/ArticleInterpretRestController.java | 解读REST控制器 |
| 15 | pai-coding-front/src/components/article/ArticleInterpreter.vue | 前端解读组件 |
| 16 | pai-coding-front/src/http/ResponseTypes/InterpretResponseType.ts | 前端TS类型定义 |

### 6.2 修改文件（共9个，已通过代码库搜索验证存在）
[SENIOR-DEV AMENDMENT: 从原10个减少到9个——移除了AISourceEnum修改。AiModelProvider体系通过YAML配置中的provider id（字符串）识别模型，不依赖AISourceEnum枚举。]

| 序号 | 文件路径 | 修改内容 | 验证状态 |
|------|----------|----------|----------|
| 1 | paicoding-api/.../enums/ai/AISourceEnum.java | 新增枚举: DEEP_SEEK, KIMI, TONG_YI | 已确认: 54行, 4个枚举值 |
| 2 | paicoding-service/.../xunfei/XunFeiIntegration.java | 提取公共逻辑到OpenAiChatClient | 已确认: 301行 |
| 3 | paicoding-service/.../xunfei/XunFeiAiServiceImpl.java | 适配提取后的OpenAiChatClient | 已确认: 85行 |
| 4 | paicoding-web/.../dev/application-ai.yml | 新增 ai-interpret 配置块 | 已确认: 55行 |
| 5 | paicoding-web/.../test/application-ai.yml | 同上 | 已确认 |
| 6 | paicoding-web/.../pre/application-ai.yml | 同上 | 已确认 |
| 7 | paicoding-web/.../prod/application-ai.yml | 同上 | 已确认 |
| 8 | pai-coding-front/src/components/article/ArticleDetail.vue | 集成ArticleInterpreter组件 | 已确认: 509行 |
| 9 | pai-coding-front/src/http/URL.ts | 新增2个API URL常量 | 已确认: 112行 |
| 10 | pai-coding-front/src/views/ArticleDetailView.vue | 传递articleContent prop | 已确认: 206行 |

### 6.3 数据库变更

**无需数据库变更**（确认）

- 用户日限流：使用Redis计数器（复用AbsChatService中的HINCR模式）
- 无解读历史记录存储需求（PRD v1 Won't Have）
- 模型配置：YAML配置文件

### 6.4 配置变更

[SENIOR-DEV AMENDMENT: 经验证，test和pre环境的application-ai.yml当前不含ai.maxNum和ai.source配置段（与dev/prod不同），这两个环境的ai-interpret配置段需完整新增。dev和prod环境仅需在末尾追加。]

在 application-ai.yml 新增:

```yaml
ai-interpret:
  daily-limit: 50
  timeout: 30
  context-window-size: 500  # 前后各500字符 [SENIOR-DEV AMENDMENT: 明确是每侧500，即总上下文1000字符]
  cooldown-seconds: 300
  cooldown-threshold: 2
  providers:
    - id: "deepseek-chat"
      name: "DeepSeek"
      apiHost: "https://api.deepseek.com/v1/chat/completions"
      apiKey: ""
      model: "deepseek-chat"
      maxToken: 4096
      timeout: 300
    - id: "moonshot-v1"
      name: "Kimi"
      apiHost: "https://api.moonshot.cn/v1/chat/completions"
      apiKey: ""
      model: "moonshot-v1-8k"
      maxToken: 4096
      timeout: 300
    - id: "qwen-max"
      name: "通义千问"
      apiHost: "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
      apiKey: ""
      model: "qwen-max"
      maxToken: 4096
      timeout: 300
```


## 7. 风险评估

| 风险 | 影响 | 概率 | 缓解策略 | 回退方案 |
|------|------|------|----------|----------|
| AI API调用失败 | 高 | 中 | 多模型自动降级fallback | 返回友好错误提示+重试按钮 |
| SSE流传输中断 | 中 | 低 | AbortController 30秒超时；已接收内容保留 | 降级为同步返回完整结果 |
| 大文章token超限 | 低 | 中 | 后端token估算+逐级截断 | 仍返回基于部分上下文的解读 |
| 并发请求费用暴增 | 高 | 低 | Redis日限流50次 | 关闭功能配置开关 |
| XunFeiIntegration提取破坏Chat | 高 | 低 | 保留原public方法签名不变，内部委托 | git revert回退 |

### 回退方案

- **功能级回退**: ai-interpret.enabled: false 配置开关，运维可关闭解读功能而不影响其他模块
- **代码级回退**: 改动集中在独立文件/模块，可通过git revert精确回退
- **Chat功能保护**: XunFeiIntegration改造采用委托模式，保留原有public方法签名不变

## 8. 可扩展性考量

### 用户关注的变化方向：[待用户确认，默认a+b]

- a) **新增模型提供商（只需改YAML配置）** - 通过AiModelProvider接口 + YAML配置实现
- b) **新增AI使用场景（除解读外，摘要、翻译等）** - AiModelRouter可复用于任何AI调用场景

### 当前设计预留的扩展点

**扩展点1: 新模型提供商接入** - 预留方式: AiModelProvider接口
- 新增模型：实现 AiModelProvider 接口 + YAML配置一行
- 改动: 1个实现类 + 1个YAML条目

**扩展点2: 新AI使用场景** - 预留方式: AiModelRouter + OpenAiChatClient 独立于业务
- 新增场景（如摘要/翻译）：新建Service注入AiModelRouter并构建prompt
- 改动: 2个文件（1 Service + 1 Controller）+ 1个前端组件

**扩展点3: 系统提示词版本化** - 预留方式: 提示词常量独立定义
- 当前硬编码于 ArticleInterpretService 常量（v1要求硬编码）
- 如需可配置：改为 ConfigurationProperties 读取YAML

**扩展点4: 模型健康检查独立化** - 预留方式: AiModelRouter 中独立方法
- 当前在Router内实现简单失败计数+冷却
- 如需独立：提取为 ModelHealthChecker + Scheduled定时任务

### 为扩展做的取舍

| 当前简化 | 理由 | 如需扩展，改动范围 |
|----------|------|-------------------|
| 提示词硬编码 | 产品要求v1不可外部配置 | ConfigurationProperties: 1文件 |
| 模型失败冷却用内存Map | v1规模小 | Redis: 1文件(AiModelRouter) |
| 无数据库表 | v1无历史记录需求 | 新增表+Service+Controller: 4文件 |
| 无管理后台 | 不在v1范围 | 独立功能: 6文件(前后端) |
