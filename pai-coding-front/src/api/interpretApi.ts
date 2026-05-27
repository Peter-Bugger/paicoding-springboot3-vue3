// @PRD: US1 — 选中文本后发起AI解读请求，支持流式返回
// @PRD: US3 — SSE事件流式渲染，STREAM事件逐字追加，STREAM_END完成，ERROR错误处理

// SSE事件类型
export interface SseEvent {
  type: 'STREAM' | 'STREAM_END' | 'ERROR';
  content: string;
  message?: string;
}

// 请求体
export interface InterpretRequest {
  articleId: number;
  selectedText: string;
  startPos: number;
  endPos: number;
}

// 模型信息
export interface ModelInfo {
  name: string;
  displayName: string;
  available: boolean;
  priority: number;
}

// @PRD: US1 — Vite代理只匹配 /api 前缀，开发模式需要 /api 前缀
const AI_BASE_PATH = '/api/article/api/ai';

// 30秒超时
const SSE_TIMEOUT_MS = 600000;

// 从 sessionStorage / localStorage 获取 JWT 令牌
function getAuthToken(): string | null {
  const token = sessionStorage.getItem('Authorization') || localStorage.getItem('Authorization');
  return token || null;
}

/**
 * 流式解读请求 - 使用 fetch + ReadableStream 消费 SSE
 * 支持 30 秒超时、错误处理和完成回调
 * @PRD: US1 — AC1.4: 超长文本截断（2000 字符）
 * @PRD: US3 — AC3.1: 流式返回，逐字追加
 * @PRD: US3 — AC3.4: 超时处理
 * @PRD: US3 — AC3.5: 空结果处理
 */
export async function interpretArticleStream(
  req: InterpretRequest,
  onMessage: (content: string) => void,
  onComplete: (fullContent: string) => void,
  onError: (message: string) => void
): Promise<void> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), SSE_TIMEOUT_MS);

  try {
    const token = getAuthToken();
    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    if (token) {
      headers['Authorization'] = token;
    }

    const response = await fetch(`${AI_BASE_PATH}/interpret`, {
      method: 'POST',
      headers,
      body: JSON.stringify(req),
      signal: controller.signal,
      credentials: 'include',
    });

    clearTimeout(timeoutId);

    if (!response.ok) {
      onError(`请求失败 (${response.status})`);
      return;
    }

    const reader = response.body!.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    let fullContent = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';
      for (const line of lines) {
        if (line.startsWith('data:')) {
          try {
            // Spring SseEmitter 格式为 data:{json}（无空格），
            // 使用 trim() 兼容有/无空格两种情况
            const event: SseEvent = JSON.parse(line.slice(5).trim());
            if (event.type === 'STREAM') {
              fullContent += event.content;
              onMessage(fullContent);
            } else if (event.type === 'STREAM_END') {
              onComplete(fullContent);
              return;
            } else if (event.type === 'ERROR') {
              onError(event.message || 'AI解读失败');
              return;
            }
          } catch {
            // 忽略解析错误的行
          }
        }
      }
    }
    // 流结束但没有收到 STREAM_END
    if (fullContent) {
      onComplete(fullContent);
    } else {
      onError('AI返回了空结果');
    }
  } catch (e: any) {
    clearTimeout(timeoutId);
    if (e.name === 'AbortError') {
      onError('AI响应超时，请稍后重试');
    } else {
      onError(e.message || '网络异常，请检查连接');
    }
  }
}

/**
 * 获取可用模型列表
 * @PRD: US4 — AC4.1: 多模型列表返回
 */
export async function fetchAvailableModels(): Promise<ModelInfo[]> {
  const response = await fetch(`${AI_BASE_PATH}/models`);
  if (!response.ok) throw new Error('获取模型列表失败');
  const data = await response.json();
  // 响应格式: { status: { code: 0, msg: "ok" }, result: [...] }  ResVo<T> 的 result 字段
  return data.result || data;
}
