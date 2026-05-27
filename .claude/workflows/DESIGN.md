# 详细设计: 文章AI解读功能

## 1. PLAN 审阅意见

### 1.1 已修改 PLAN.md 的内容

| 编号 | 问题 | 修正 | 影响 |
|------|------|------|------|
| A1 | AISourceEnum 扩展不必要 | 从修改文件列表移除，AiModelProvider体系通过YAML中id字符串识别模型 | 修改文件从10减到9 |
| A2 | XunFeiAiServiceImpl无需修改 | XunFeiIntegration保留public签名，内部委托给OpenAiChatClient | Chat功能零风险 |
| A3 | 独立Provider过多 | 合并为1个通用OpenAiCompatibleProvider | 新增模型零Java代码 |
| A4 | Token估算缺算法 | 补充: estimatedTokens=中文字符*1.5+英文字母*0.25+英文单词*0.75 | 实现可确定性 |
| A5 | context-window-size模糊 | 明确是单侧500字符，总上下文1000字符 | 实现可确定性 |
| A6 | 文章内容获取方法 | 指定queryDetailArticleInfo避免阅读计数+1副作用 | 正确语义 |
| A7 | test/pre环境YAML | 需完整新增ai-interpret段而非追加 | 部署正确性 |

### 1.2 可扩展性设计的可行性评估

| 扩展点 | PLAN.md预留 | 可行性 | 备注 |
|--------|------------|--------|------|
| 新模型接入 | AiModelProvider+YAML配置 | 可行且优化 | 通用OpenAiCompatibleProvider，新模型仅需1行YAML |
| 新AI场景(摘要/翻译) | AiModelRouter独立注入 | 可行 | 新建Service+Controller: 扩展成本2文件 |
| 提示词版本化 | 常量=>ConfigurationProperties | 可行 | 迁移成本1文件 |
| 健康检查独立化 | 独立方法=>Scheduled | 可行 | 当前内存Map满足v1规模 |
| 功能开关 | ai-interpret.enabled | 已预留 | YAML配置布尔字段 |

## 2. 详细接口设计

### 2.1 AiModelProvider 接口（核心抽象）
位置: `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ai/AiModelProvider.java`

```java
public interface AiModelProvider {
    String getProviderName();       // 提供商名称: "deepseek"
    String getDisplayName();        // 显示名称: "DeepSeek V3"
    int getPriority();              // 优先级(越小越高)
    boolean isAvailable();
    void markUnavailable();
    void markAvailable();
    String chat(String systemPrompt, String userMessage);
    void chatStream(String systemPrompt, String userMessage, StreamCallback callback);
}
```

### 2.2 StreamCallback 回调接口
位置: `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ai/StreamCallback.java`

```java
public interface StreamCallback {
    void onMessage(String content);
    void onComplete(String fullContent);
    void onError(Throwable error);
}
```

### 2.3 AiModelConfig 配置类（YAML绑定）
位置: `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ai/AiModelConfig.java`

YAML配置结构:
```yaml
ai-interpret:
  enabled: true
  max-daily-requests: 50
  models:
    - name: deepseek
      display-name: "DeepSeek V3"
      priority: 0
      api-host: "https://api.deepseek.com/v1/chat/completions"
      api-key: ${DEEPSEEK_API_KEY:sk-xxx}
      model-name: deepseek-chat
      max-tokens: 4096
      timeout-seconds: 30
      enabled: true
    - name: kimi
      display-name: "月之暗面 Kimi"
      priority: 1
      api-host: "https://api.moonshot.cn/v1/chat/completions"
      api-key: ${KIMI_API_KEY:sk-xxx}
      model-name: moonshot-v1-8k
      max-tokens: 4096
      timeout-seconds: 30
      enabled: true
    - name: qwen
      display-name: "通义千问 Turbo"
      priority: 2
      api-host: "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
      api-key: ${QWEN_API_KEY:sk-xxx}
      model-name: qwen-turbo
      max-tokens: 4096
      timeout-seconds: 30
      enabled: true
```

### 2.4 AiModelRouter 路由服务
位置: `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ai/AiModelRouter.java`

```
selectProvider()     → 按priority排序返回第一个available=true的Provider
fallbackToNext(f)    → 标记f不可用 + 选下一个available
hasAvailableProvider() → boolean
listAvailableModels()  → List<ModelInfoVO>
@Scheduled(fixedDelay=300000) checkHealth() → 每5分钟探测标记为不可用的Provider
```

### 2.5 ArticleInterpretService
位置: `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ArticleInterpretService.java`

```java
@Service
public class ArticleInterpretService {
    // 固定系统提示词 — 硬编码，不可配置
    static final String SYSTEM_PROMPT =
        "你是文章专属解读助手，只能基于提供的原文解释选中片段，不准脱离原文，不准编造内容，语言通俗易懂，条理清晰。";

    @Autowired AiModelRouter aiModelRouter;
    @Autowired ArticleDao articleDao;
    @Autowired StringRedisTemplate redisTemplate;

    public void interpret(Long articleId, Long userId,
                          String selectedText, StreamCallback callback);

    private String extractContext(String fullContent, String selectedText);
    private int estimateTokens(String text);   // 1中文字符=1.5token, 1英文单词=0.75token
    private boolean checkDailyLimit(Long userId); // Redis HINCR Key: interpret:limit:{userId}:{date}
}
```

### 2.6 REST Controller
位置: `paicoding-web/src/main/java/com/github/paicoding/forum/web/controller/article/rest/AiInterpretController.java`

```java
@RestController
@RequestMapping(path = "article/api/ai")
public class AiInterpretController {

    @PostMapping(path = "interpret", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter interpret(@RequestBody InterpretReq req, HttpServletRequest request);

    @GetMapping(path = "models")
    public ResVo<List<ModelInfoVO>> listModels();
}
```

### 2.7 DTOs

**InterpretReq** (`paicoding-api/.../vo/ai/InterpretReq.java`):
```java
public class InterpretReq {
    private Long articleId;
    private String selectedText;
    private Integer startPos;
    private Integer endPos;
}
```

**ModelInfoVO** (`paicoding-api/.../vo/ai/ModelInfoVO.java`):
```java
public class ModelInfoVO {
    private String name;
    private String displayName;
    private boolean available;
    private int priority;
}
```

### 2.8 前端组件接口

**ArticleInterpreter.vue Props:**
```typescript
interface Props {
  articleId: number;
}
```
无Events（组件自包含）。

**API 接口层** (`pai-coding-front/src/api/interpretApi.ts`):
```typescript
interface InterpretRequest {
  articleId: number;
  selectedText: string;
  startPos: number;
  endPos: number;
}
interface SseEvent {
  type: 'STREAM' | 'STREAM_END' | 'ERROR';
  content: string;
  message?: string;
}
function interpretArticle(req: InterpretRequest): ReadableStream<SseEvent>;
function fetchAvailableModels(): Promise<ModelInfo[]>;
```

## 3. 组件/类设计

### 3.1 后端类图

```
AiModelProvider <<interface>>
  └── OpenAiCompatibleProvider                         (通用OpenAI兼容实现，OkHttp3 + SSE解析)

AiModelConfig (@ConfigurationProperties "ai-interpret")
  ├── enabled: boolean
  ├── maxDailyRequests: int
  └── models: List<ModelConf>

AiModelRouter (@Component)
  ├── on ApplicationReady → 遍历AiModelConfig.models创建OpenAiCompatibleProvider实例
  ├── selectProvider() → TreeSet按priority排序，返回首个available=true
  ├── fallbackToNext(failed) → 标记failed不可用 + 调用selectProvider()
  ├── @Scheduled(fixedDelay=300000) checkHealth() → 向不可用Provider发最小探测请求
  └── listAvailableModels() → Map为List<ModelInfoVO>

ArticleInterpretService (@Service)
  ├── interpret(articleId, userId, selectedText, callback):
  │    1. checkDailyLimit(userId) → 超限则直接返回错误
  │    2. ArticleDO article = articleDao.getById(articleId)
  │    3. context = extractContext(article.content, selectedText)  // 前后500字
  │    4. Assert estimateTokens(context + selectedText + SYSTEM_PROMPT) <= model.maxTokens
  │    5. userMessage = "请解读以下文章片段:\n\n【上下文】{context}\n\n【选中文本】{selectedText}"
  │    6. aiModelRouter.selectProvider().chatStream(SYSTEM_PROMPT, userMessage, callback)
  │    7. 若异常 → aiModelRouter.fallbackToNext() → 重试
  │    8. 全部失败 → callback.onError()

AiInterpretController
  ├── interpret(req, request):
  │    SseEmitter emitter = new SseEmitter(30_000L)
  │    StreamCallback callback = new SseCallbackAdapter(emitter)
  │    articleInterpretService.interpret(req.articleId, userId, req.selectedText, callback)
  │    return emitter
  └── listModels(): aiModelRouter.listAvailableModels()
```

### 3.2 前端状态机

```
States: idle → selecting → pending → streaming → done | error | timeout

idle:        初始，无选中
selecting:   文本选中中，浮动按钮可见
pending:     请求已发送，等待首个SSE事件
streaming:   接收STREAM事件，逐字追加
done:        收到STREAM_END，显示完整结果+复制按钮
error:       收到ERROR事件或网络异常，显示错误+重试
timeout:     30秒无响应，显示超时+重试
```

状态转换:
- idle → selecting (mouseup事件 + getSelection()非空)
- selecting → idle (click文章空白处)
- selecting → pending (点击"AI解读"按钮)
- pending → streaming (收到首个STREAM事件)
- pending → timeout (30秒无任何SSE事件)
- streaming → done (收到STREAM_END)
- streaming → error (收到ERROR或网络中断)
- error/timeout → pending (点击重试)

### 3.3 前端组件结构

```
ArticleDetailView.vue                                              [修改]
  ├── ArticleDetail.vue                                            [已有]
  └── ArticleInterpreter.vue                                       [新增]
       ├── 文本选中监听 (window.getSelection + mouseup事件)
       ├── FloatingButton ("AI解读"浮动按钮)
       │    ├── 位置计算 (selection.getRangeAt(0).getBoundingClientRect())
       │    └── 防抖 (300ms内重复mouseup不重新渲染)
       └── InterpreterPanel (右侧滑动面板)
            ├── Header: "AI解读" + 选中文本摘要 + 关闭按钮
            ├── Body (状态路由):
            │   ├── LoadingState: el-skeleton animated
            │   ├── StreamingState: MdPreview + 实时更新
            │   ├── DoneState: MdPreview + 复制按钮
            │   ├── ErrorState: el-alert type="error" + 重试按钮
            │   └── TimeoutState: el-alert type="warning" + 重试按钮
            └── Footer: "AI解读基于原文，不编造不杜撰" 免责声明
```

### 3.4 前端 SSE 通信方案

使用 `fetch` + `ReadableStream`（支持POST + 自定义headers）:

```typescript
async function interpretArticle(req: InterpretRequest,
  onMessage: (content: string) => void,
  onComplete: () => void,
  onError: (msg: string) => void
): Promise<void> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 30000);

  try {
    const response = await fetch('/article/api/ai/interpret', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
      signal: controller.signal,
    });
    clearTimeout(timeoutId);

    if (!response.ok) {
      onError(`请求失败: ${response.status}`);
      return;
    }

    const reader = response.body!.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop() || ''; // 保留不完整的行
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const event: SseEvent = JSON.parse(line.slice(6));
          if (event.type === 'STREAM') onMessage(event.content);
          else if (event.type === 'STREAM_END') onComplete();
          else if (event.type === 'ERROR') onError(event.message || '未知错误');
        }
      }
    }
  } catch (e: any) {
    clearTimeout(timeoutId);
    onError(e.name === 'AbortError' ? 'AI响应超时，请稍后重试' : e.message);
  }
}
```

## 4. 需求追溯矩阵

| PRD用户故事/验收条件 | 对应Task编号 | 测试文件 | 测试场景数 | 状态 |
|---------------------|-------------|---------|-----------|------|
| US1: AC1.1-1.3 (正常选中/点击/未选中) | Task 5 | ArticleInterpreter.spec.ts | 3 | ⬜ |
| US1: AC1.4-1.7 (超长/取消/未登录/解读中) | Task 5 | ArticleInterpreter.spec.ts | 4 | ⬜ |
| US2: AC2.1-2.3 (固定提示词/上下文/硬编码) | Task 2 | ArticleInterpretServiceTest.java | 3 | ⬜ |
| US2: AC2.4 (上下文超长截断) | Task 2 | ArticleInterpretServiceTest.java | 1 | ⬜ |
| US3: AC3.1-3.2 (流式返回/解读完成) | Task 6 | InterpreterPanel.spec.ts | 2 | ⬜ |
| US3: AC3.3-3.4 (超时/服务错误) | Task 6 | InterpreterPanel.spec.ts | 2 | ⬜ |
| US3: AC3.5-3.8 (空结果/重试/复制/关闭) | Task 6 | InterpreterPanel.spec.ts | 4 | ⬜ |
| US4: AC4.1-4.3 (多模型/降级/全部不可用) | Task 3 | AiModelRouterTest.java | 3 | ⬜ |
| US4: AC4.4-4.5 (新增模型/健康检查) | Task 1,4 | AiModelConfigTest.java | 2 | ⬜ |
| US5: AC5.1-5.3 (展示位置/移动端/重新选择) | Task 5 | ArticleInterpreter.spec.ts | 3 | ⬜ |

## 5. 任务拆解清单

### Task 1: AiModelProvider 接口与配置体系
- **文件（新增）：**
  - `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ai/AiModelProvider.java`
  - `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ai/StreamCallback.java`
  - `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ai/OpenAiChatClient.java`
  - `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ai/AiModelConfig.java`
- **描述：** 创建统一AiModelProvider接口、StreamCallback回调接口、OpenAiChatClient通用实现（OkHttp3构建请求+SSE流式解析，复用XunFeiIntegration的解析逻辑）、AiModelConfig配置类（@ConfigurationProperties绑定YAML）
- **依赖：** 无
- **被依赖：** Task 2, Task 3, Task 4
- **验收标准：** 接口可编译，OpenAiChatClient正确构建OpenAI兼容请求并解析SSE流式响应
- **验证命令：** `mvn compile -pl paicoding-service -DskipTests=true`
- **测试要求：**
  - 测试文件：`paicoding-service/src/test/java/com/github/paicoding/forum/service/chatai/service/ai/AiModelRouterTest.java`
  - 测试场景：Provider注册/优先级排序/可用性标记/降级选择
  - PRD验收条件映射：US4: AC4.1, AC4.2, AC4.5
- **预估工时：** 2h

### Task 2: 文章解读服务
- **文件（新增）：**
  - `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ArticleInterpretService.java`
- **描述：** 实现ArticleInterpretService: 固定系统提示词常量、上下文提取(前后500字符)、Token估算(中文1.5/英文词0.75)、日限额检查(Redis HINCR+EXPIRE)、构建prompt并委托AiModelRouter调用
- **依赖：** Task 1
- **被依赖：** Task 3
- **验收标准：** 正确获取文章内容、提取上下文、估算token、Redis限额检查
- **验证命令：** `mvn compile -pl paicoding-service -DskipTests=true`
- **测试要求：**
  - 测试文件：`paicoding-service/src/test/java/com/github/paicoding/forum/service/chatai/service/ArticleInterpretServiceTest.java`
  - 测试场景：正常解读/上下文提取/超长截断/空文章/日限额检查/限额超限
  - PRD验收条件映射：US2: AC2.1-2.4
- **预估工时：** 2h

### Task 3: API层 DTO + Controller + Router
- **文件（新增）：**
  - `paicoding-api/src/main/java/com/github/paicoding/forum/api/model/vo/ai/InterpretReq.java`
  - `paicoding-api/src/main/java/com/github/paicoding/forum/api/model/vo/ai/ModelInfoVO.java`
  - `paicoding-service/src/main/java/com/github/paicoding/forum/service/chatai/service/ai/AiModelRouter.java`
  - `paicoding-web/src/main/java/com/github/paicoding/forum/web/controller/article/rest/AiInterpretController.java`
- **描述：** DTOs + AiModelRouter(优先级路由+自动降级+定时健康检查@Scheduled) + AiInterpretController(POST SSE流式+GET模型列表)
- **依赖：** Task 1, Task 2
- **被依赖：** Task 4, Task 7
- **验收标准：** SseEmitter流式响应正确，多模型降级正确，错误码正确(503/429/400)
- **验证命令：** `mvn compile -pl paicoding-web -DskipTests=true`
- **测试要求：**
  - 测试文件：`paicoding-web/src/test/java/com/github/paicoding/forum/test/ai/AiModelRouterTest.java`
  - 测试场景：正常路由/模型降级/全部不可用(503)/定时健康检查
  - PRD验收条件映射：US4: AC4.1-4.5
- **预估工时：** 2h

### Task 4: YAML配置
- **文件（修改）：**
  - `paicoding-web/src/main/resources-env/dev/application-ai.yml`
  - `paicoding-web/src/main/resources-env/test/application-ai.yml`
  - `paicoding-web/src/main/resources-env/pre/application-ai.yml`
  - `paicoding-web/src/main/resources-env/prod/application-ai.yml`
- **描述：** 各环境配置文件添加ai-interpret配置段，含deepseek/kimi/qwen三模型配置，保留现有chatgpt/xunfei不变
- **依赖：** Task 1
- **被依赖：** Task 7
- **验收标准：** 配置格式正确，Spring正确绑定AiModelConfig
- **验证命令：** `mvn compile -pl paicoding-web -DskipTests=true`
- **测试要求：**
  - 测试文件：`paicoding-web/src/test/java/com/github/paicoding/forum/test/ai/AiModelConfigTest.java`
  - 测试场景：配置加载/默认值/占位符解析/enabled开关
  - PRD验收条件映射：US4: AC4.4
- **预估工时：** 1h

### Task 5: 前端 ArticleInterpreter 核心组件
- **文件（新增）：**
  - `pai-coding-front/src/components/ArticleInterpreter.vue`
  - `pai-coding-front/src/components/interpreter/FloatingButton.vue`
  - `pai-coding-front/src/components/interpreter/InterpreterPanel.vue`
  - `pai-coding-front/src/api/interpretApi.ts`
- **文件（修改）：**
  - `pai-coding-front/src/views/article/ArticleDetailView.vue`
- **描述：** ArticleInterpreter组件: mouseup监听→getSelection()获取文本→位置计算(selection.getRangeAt(0).getBoundingClientRect())→防抖300ms→浮动按钮→点击调用interpretApi发起SSE流式请求。集成到ArticleDetailView。
- **依赖：** Task 3 (前后端可并行，API接口已定义)
- **被依赖：** Task 6
- **验收标准：** 选中文本后显示浮动按钮，位置正确，点击后正确发送请求
- **验证命令：** `cd pai-coding-front && npm run type-check`
- **测试要求：**
  - 测试文件：`pai-coding-front/src/components/__tests__/ArticleInterpreter.spec.ts`
  - 测试场景：正常选中+按钮显示/空选中不显示/超长截断/取消选中/位置计算/已登录检查
  - PRD验收条件映射：US1: AC1.1-1.7, US5: AC5.1-5.3
- **预估工时：** 3h

### Task 6: 前端结果展示与异常处理
- **文件（修改）：**
  - `pai-coding-front/src/components/interpreter/InterpreterPanel.vue`
- **描述：** InterpreterPanel状态机完善: loading(skeleton)→streaming(MdPreview实时更新)→done(完整结果+复制按钮)→error(el-alert+重试)→timeout(el-alert+重试)。复制功能(navigator.clipboard.writeText)。30秒超时检测。关闭按钮。移动端底部drawer适配。免责声明。
- **依赖：** Task 5
- **被依赖：** Task 7
- **验收标准：** 所有状态正确展示，Markdown渲染正常，复制可用，移动端自适应
- **验证命令：** `cd pai-coding-front && npm run type-check`
- **测试要求：**
  - 测试文件：`pai-coding-front/src/components/__tests__/InterpreterPanel.spec.ts`
  - 测试场景：流式渲染追加/STREAM_END完成/ERROR错误/超时/空结果/重试按钮/复制/关闭
  - PRD验收条件映射：US3: AC3.1-3.8
- **预估工时：** 2h

### Task 7: 集成接入与端到端验证
- **文件（修改）：**
  - `pai-coding-front/src/views/article/ArticleDetailView.vue` (接入ArticleInterpreter)
- **描述：** ArticleDetailView接入ArticleInterpreter组件，传递articleId。确认后端路由注册，端到端验证: 选中→解读→流式→复制→关闭。
- **依赖：** Task 3, Task 4, Task 6
- **被依赖：** 无
- **验收标准：** 端到端流程完整可用
- **验证命令：** `cd pai-coding-front && npm run build-only` + `mvn compile -pl paicoding-web -DskipTests=true`
- **测试要求：** 无需额外测试（前序Task已覆盖各模块单独测试）
- **预估工时：** 1h
