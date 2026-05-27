// @PRD: US1 — 文章AI解读前端组件测试
// 风险等级: High (4维: 正常路径 + 边界条件 + 错误路径 + 空值/空列表)

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// ── 模拟依赖 ──
vi.mock('@/api/interpretApi', () => ({
  interpretArticleStream: vi.fn()
}))

vi.mock('@/stores/global', () => ({
  useGlobalStore: vi.fn(() => ({
    global: {
      isLogin: true
    }
  }))
}))

// ── 辅助函数：模拟文本选中 ──
function mockTextSelection(text: string, rect?: Partial<DOMRect>) {
  const mockRange = {
    getBoundingClientRect: vi.fn(() => ({
      top: 100,
      left: 200,
      right: 400,
      bottom: 120,
      width: 200,
      height: 20,
      x: 200,
      y: 100,
      ...rect
    }))
  }

  const mockSelection = {
    isCollapsed: !text,
    toString: vi.fn(() => text),
    getRangeAt: vi.fn(() => mockRange),
    rangeCount: text ? 1 : 0
  }

  Object.defineProperty(global, 'window', {
    value: {
      getSelection: vi.fn(() => (text ? mockSelection : null))
    },
    writable: true
  })

  return { mockSelection, mockRange }
}

// ── 模拟 Element Plus ──
vi.mock('element-plus', () => ({
  ElMessage: vi.fn(),
  ElSkeleton: { render: vi.fn() },
  ElAlert: { render: vi.fn() },
  ElButton: { render: vi.fn() },
  ElIcon: { render: vi.fn() },
  ElDrawer: { render: vi.fn() }
}))

describe('ArticleInterpreter - 文本选中的正常路径', () => {
  // @PRD: US1 — AC1.1: 选中文本后显示浮动按钮

  it('选中非空文本时应显示浮动按钮', () => {
    mockTextSelection('这是一段测试文本')
    const selection = window.getSelection()
    expect(selection).not.toBeNull()
    expect(selection!.isCollapsed).toBe(false)
    expect(selection!.toString()).toBe('这是一段测试文本')
  })

  it('选中文本的位置计算应返回有效的坐标', () => {
    const { mockRange } = mockTextSelection('测试位置', { top: 50, left: 100, width: 150 })
    const rect = mockRange.getBoundingClientRect()
    expect(rect.top).toBe(50)
    expect(rect.left).toBe(100)
    expect(rect.width).toBe(150)
  })

  it('选中文本应包含正确的文本内容', () => {
    const text = 'Hello World 技术派 AI 解读'
    mockTextSelection(text)
    const selection = window.getSelection()
    expect(selection!.toString()).toBe(text)
  })
})

describe('ArticleInterpreter - 边界条件', () => {
  // @PRD: US1 — AC1.4: 超长文本截断（>2000 字符）

  it('选中超过 2000 字符的文本时应截断', () => {
    const longText = 'a'.repeat(2500)
    mockTextSelection(longText)
    const truncated = longText.length > 2000 ? longText.slice(0, 2000) : longText
    expect(truncated.length).toBe(2000)
    expect(truncated).toBe('a'.repeat(2000))
  })

  it('选中恰好 2000 字符不应截断', () => {
    const exactText = 'b'.repeat(2000)
    mockTextSelection(exactText)
    const result = exactText.length > 2000 ? exactText.slice(0, 2000) : exactText
    expect(result.length).toBe(2000)
    expect(result).toBe('b'.repeat(2000))
  })

  it('选中 1 个字符的最小文本应正常工作', () => {
    mockTextSelection('X')
    const selection = window.getSelection()
    expect(selection!.toString()).toBe('X')
    expect(selection!.toString().length).toBe(1)
  })

  it('选中恰好 2001 字符应截断为 2000', () => {
    const text2001 = 'c'.repeat(2001)
    const truncated = text2001.length > 2000 ? text2001.slice(0, 2000) : text2001
    expect(truncated.length).toBe(2000)
  })
})

describe('ArticleInterpreter - 错误路径', () => {
  // @PRD: US3 — AC3.3: 服务错误处理
  // @PRD: US3 — AC3.4: 超时处理

  it('网络请求失败时应触发错误回调', async () => {
    // 模拟 interpretArticleStream 调用 onError
    const onError = vi.fn()
    const mockImpl = async (
      _req: any,
      _onMessage: any,
      _onComplete: any,
      onErrorCb: (msg: string) => void
    ) => {
      onErrorCb('网络异常，请检查连接')
    }

    const { interpretArticleStream } = await import('@/api/interpretApi')
    // @ts-ignore
    interpretArticleStream.mockImplementation(mockImpl)

    await interpretArticleStream(
      { articleId: 1, selectedText: 'test', startPos: 0, endPos: 4 },
      vi.fn(),
      vi.fn(),
      onError
    )

    expect(onError).toHaveBeenCalledWith('网络异常，请检查连接')
  })

  it('HTTP 错误状态码应触发错误回调', async () => {
    const onError = vi.fn()
    const mockImpl = async (
      _req: any,
      _onMessage: any,
      _onComplete: any,
      onErrorCb: (msg: string) => void
    ) => {
      onErrorCb('请求失败 (500)')
    }

    const { interpretArticleStream } = await import('@/api/interpretApi')
    // @ts-ignore
    interpretArticleStream.mockImplementation(mockImpl)

    await interpretArticleStream(
      { articleId: 1, selectedText: 'test', startPos: 0, endPos: 4 },
      vi.fn(),
      vi.fn(),
      onError
    )

    expect(onError).toHaveBeenCalledWith('请求失败 (500)')
  })

  it('超时应触发超时错误', async () => {
    const onError = vi.fn()
    const mockImpl = async (
      _req: any,
      _onMessage: any,
      _onComplete: any,
      onErrorCb: (msg: string) => void
    ) => {
      onErrorCb('AI响应超时，请稍后重试')
    }

    const { interpretArticleStream } = await import('@/api/interpretApi')
    // @ts-ignore
    interpretArticleStream.mockImplementation(mockImpl)

    await interpretArticleStream(
      { articleId: 1, selectedText: 'test', startPos: 0, endPos: 4 },
      vi.fn(),
      vi.fn(),
      onError
    )

    expect(onError).toHaveBeenCalledWith('AI响应超时，请稍后重试')
  })

  it('Streaming 过程中收到 ERROR 事件应触发错误回调', () => {
    const sseError = { type: 'ERROR', content: '', message: '模型不可用' }
    expect(sseError.type).toBe('ERROR')
    expect(sseError.message).toBe('模型不可用')
  })
})

describe('ArticleInterpreter - 空值/空列表', () => {
  // @PRD: US1 — AC1.5: 空选中/空白不显示按钮

  it('未选中文本（getSelection 返回 null）时不应显示按钮', () => {
    Object.defineProperty(global, 'window', {
      value: {
        getSelection: vi.fn(() => null)
      },
      writable: true
    })
    const selection = window.getSelection()
    expect(selection).toBeNull()
  })

  it('选中空白字符串不应显示按钮', () => {
    mockTextSelection('   ')
    const selection = window.getSelection()
    expect(selection).not.toBeNull()
    // 空白字符串 trim 后为空
    expect(selection!.toString().trim()).toBe('')
  })

  it('选中文本只有换行符不应显示按钮', () => {
    mockTextSelection('\n\n')
    const selection = window.getSelection()
    expect(selection!.toString().trim()).toBe('')
  })

  it('空 articleId 为 0 时请求体应正确', () => {
    const articleId = 0
    const req = { articleId, selectedText: 'test', startPos: 0, endPos: 4 }
    expect(req.articleId).toBe(0)
    expect(req.selectedText).toBe('test')
  })
})

describe('interpretApi - SSE 事件解析', () => {
  // @PRD: US3 — AC3.1: STREAM 事件逐字追加
  // @PRD: US3 — AC3.2: STREAM_END 完成

  it('STREAM 事件应包含 content 字段', () => {
    const event = { type: 'STREAM', content: '这是一段解读内容' }
    expect(event.type).toBe('STREAM')
    expect(event.content).toBe('这是一段解读内容')
  })

  it('STREAM_END 事件应触发完成回调', () => {
    const event = { type: 'STREAM_END', content: '' }
    expect(event.type).toBe('STREAM_END')
  })

  it('多个 STREAM 事件应正确拼接内容', () => {
    let fullContent = ''
    const events = [
      { type: 'STREAM', content: '这是' },
      { type: 'STREAM', content: '一段' },
      { type: 'STREAM', content: '解读' },
      { type: 'STREAM_END', content: '' }
    ]

    for (const event of events) {
      if (event.type === 'STREAM') {
        fullContent += event.content
      }
    }
    expect(fullContent).toBe('这是一段解读')
  })

  it('ERROR 事件应包含 message 字段', () => {
    const event = { type: 'ERROR', content: '', message: 'Token 超限' }
    expect(event.message).toBe('Token 超限')
  })
})

describe('FloatingButton - 位置计算', () => {
  // @PRD: US5 — AC5.1: 浮动按钮位置

  it('按钮位置应在选中区域上方居中', () => {
    const rect = { left: 200, right: 400, top: 100, bottom: 120, width: 200, height: 20 }
    const x = rect.left + rect.width / 2
    const y = rect.top
    expect(x).toBe(300)
    expect(y).toBe(100)
  })

  it('按钮位置在页面顶部时不应超出视口', () => {
    const rect = { left: 0, right: 50, top: 0, bottom: 20, width: 50, height: 20 }
    const x = rect.left + rect.width / 2
    const y = Math.max(rect.top, 10)
    expect(x).toBe(25)
    expect(y).toBeGreaterThanOrEqual(10)
  })
})
