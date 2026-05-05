# 前端界面全面视觉优化计划

## 设计方向

**冷调·现代极客风** — 从现有的暖橙色系转向冷色调科技感风格：
- **配色**: 深海军蓝/钢青色为基调，冷灰为辅助，电光蓝/青为强调色，橙色降级为点缀
- **字体**: JetBrains Mono（代码/数字/统计）+ Noto Sans SC（正文）+ ZCOOL QingKe HuangYou（装饰性标题）
- **氛围**: 锐利、干净、有呼吸感，大面积留白配合克制的高亮色
- **动效**: 滚动驱动、页面转场、微交互相结合

## 阶段总览

| 阶段 | 内容 | 预计步数 |
|------|------|---------|
| P0 | ✅ 设计系统重构（配色/字体/变量/Tailwind） | 8-10 |
| P1 | ✅ 布局组件重设计（Header/Footer/Sidebar） | 6-8 |
| P2 | ✅ 首页全面优化 | 8-10 |
| P3 | ✅ 文章详情页优化 | 6-8 |
| P4 | ✅ 专栏系统优化 | 4-6 |
| P5 | ✅ 用户中心优化 | 6-8 |
| P6 | ✅ 其余页面统一优化 | 8-10 |
| P7 | ✅ 暗色模式适配（独立阶段） | 8-10 |
| P8 | ✅ 动效与微交互增强 | 6-8 |

---

## P0：设计系统重构

### 目标
建立全新的冷色调设计令牌体系，统一字体方案，重构 Tailwind 配置。

### 0.1 安装新字体依赖
```bash
npm install @fontsource/jetbrains-mono @fontsource/noto-sans-sc @fontsource/zcool-qingke-huangyou
```

### 0.2 重构 CSS 变量体系
- 文件：`src/assets/base.css`
- 将 `--pai-*` 变量体系从暖色改为冷色调
- 新配色方案：
  ```css
  :root {
    /* 主色调 - 冷调科技蓝 */
    --pai-brand-1-normal: #2d7cf6;       /* 电光蓝 */
    --pai-brand-2-hover: #4a8ff7;
    --pai-brand-3-click: #1a5fc7;
    --pai-brand-4-disable: #8ab4f8;
    --pai-brand-5-bak: #1a5fc7;
    --pai-brand-6-mq: #f85959;           /* 保留红色提示 */
    --pai-brand-7-light: rgba(45, 124, 246, 0.12);
    --pai-brand-8-light: rgba(45, 124, 246, 0.25);

    /* 灰色系 - 冷灰 */
    --pai-color-fff-normal: #ffffff;
    --pai-color-999-gray: #8c8f9c;
    --pai-color-3-gray: #6b7084;
    --pai-color-4-gray: #484d5e;
    --pai-color-5-gray: #d0d3dd;
    --pai-color-6-gray: #f0f2f6;
    --pai-color-7-gray: #e4e7ef;
    --pai-color-3-black: #1e2029;

    /* 背景色 - 冷底 */
    --pai-bg-white-fff: #ffffff;
    --pai-bg-normal-1: #e8edf9;
    --pai-bg-light-1: #f4f6fa;
    --pai-bg-light-2: #eef1f7;
    --pai-bg-dark-1: #1a1d27;
    --pai-bg-dark-2: #12141c;
    --pai-bg-dark-3: #0a0b10;

    /* 字体 */
    --pai-font-2: #484d5e;

    /* 边框 */
    --pai-border-color-1: #d6dae6;
    --pai-hr-color-1: #e8ebf2;

    /* 阴影 - 冷调 */
    --card-shadow: 0 2px 8px rgba(26, 29, 39, 0.06), 0 1px 2px rgba(26, 29, 39, 0.04);
  }
  ```
- 保留橙色 `#ff6900` 作为点缀色/橙色标签等场景（降级到 `<10%` 使用频率）

### 0.3 更新 Tailwind 配置
- 文件：`tailwind.config.js`
- 修改 `colors`：`warm` → `cool` 色板
- 添加 `fontFamily`：`mono-cn`（JetBrains Mono）、`display-cn`（ZCOOL QingKe HuangYou）
- 更新 `boxShadow` 为冷色调阴影
- 添加滚动驱动动画关键帧

### 0.4 更新 `base.css` 中的字体系统
- 文件：`src/assets/base.css`
- 添加 `body` 字体栈：`'Noto Sans SC', system-ui, -apple-system, sans-serif`
- 装饰性标题字体：`'ZCOOL QingKe HuangYou', cursive`
- 代码字体：`'JetBrains Mono', 'Source Code Pro', monospace`

### 0.5 清理冗余 CSS
- 删除 `src/assets/static/css/components/` 下的独立组件 CSS（共 5 个）
- 删除 `src/assets/static/css/views/` 下的视图 CSS（共 10 个）
- 功能已迁移至对应组件的 `<style scoped>` 或 Tailwind

### 0.6 更新 `main.ts`
- 导入新字体 CSS
- 确保 Element Plus 主题配置与新色板一致

### ✅ P0 验证
```bash
npm run type-check     # 类型检查通过
npm run build-only     # 构建成功
npm run lint           # 无 ESLint 错误
grep -r "var(--pai-brand-1-normal)" src/  # 确认旧色引用已更新（部分保留橙色的合理引用仍需存在）
```

---

## P1：布局组件重设计

### 目标
重新设计 HeaderBar、Footer、Sidebar 系列，奠定全站统一的布局基调。

### 1.1 HeaderBar 重设计
- 背景：白色 → 半透明毛玻璃效果（`backdrop-filter: blur(12px)`）
- Logo + 导航文字更新配色
- 搜索框样式冷调化
- 用户头像/通知区域调整
- 固定导航栏在滚动时的阴影过渡动画
- 写文章按钮样式更新

### 1.2 Footer 重设计
- 简化内容，冷色背景
- 添加社交/版权信息样式更新

### 1.3 ✅ SideImage / SideBar 组件重设计
- 统一边栏卡片样式（圆角、阴影、间距）
- SideAbout、SideArticles、SideColumn、SideJoin、SideNotice、SidePDF、SideRank、SideSubscribe 逐一优化

### 1.4 ✅ LoginDialog 重设计
- 模态框样式冷调化
- 表单元素样式更新
- 动效优化

### ✅ P1 验证 ✅
```bash
npm run type-check     # 类型检查通过
npm run build-only     # 构建成功
npm run lint           # 0 errors
# 手动检查：HeaderBar 显示正常，Footer 显示正常，登录弹窗可打开
```

---

## P2：首页全面优化

### 目标
首页是流量入口，需要最显著的设计提升。

### 2.1 HomeView 整体布局
- 页面背景色更新
- 顶部装饰条样式调整
- 骨架屏 Loading 状态优化

### 2.2 NavBar（分类导航）重设计
- 冷色调分类标签样式
- 选中态动画（下划线滑动或背景过渡）

### 2.3 RecommendArticle（推荐文章/Hero）重设计
- 卡片式推荐布局，冷色阴影
- 大标题使用 ZCOOL QingKe HuangYou 装饰字体
- 图片懒加载 + 入场动画
- 可考虑滚动视差效果

### 2.4 ArticleCard 重设计
- 卡片样式全面更新（冷色调、新阴影、新圆角）
- 封面图比例/样式调整
- 标签、标题、摘要、统计信息样式更新
- hover 状态增强（scale + shadow 组合动画）
- 批量入场 staggered 动画（animation-delay）

### 2.5 ArticleList 和分页组件
- 列表布局样式统一
- 分页器 Element Plus 主题色更新

### 2.6 首页侧边栏
- SideBar 组件样式统一

### ✅ P2 验证
```bash
npm run type-check     # 类型检查通过
npm run build-only     # 构建成功
# 手动检查：首页完整渲染、分类切换正常、分页正常、推荐区域正常
```

---

## P3：文章详情页优化

### 目标
阅读体验是博客核心，优化文章内容排版和交互。

### 3.1 ArticleDetailView
- 页面布局冷调化
- 文章标题区域增强字体表现
- 作者信息区域样式更新

### 3.2 ArticleDetail 组件
- 文章内容区域排版优化（标题层级、引用块、代码块等）
- 代码块使用 JetBrains Mono
- 阅读进度条
- 目录/锚点导航样式

### 3.3 Comment 组件系列重设计
- CommentAction、CommentItem、SubCommentAction
- 评论区卡片样式冷调化
- 评论输入框样式
- 子评论缩进和层级样式

### 3.4 SideRecommendBar
- 推荐文章列表样式更新

### ✅ P3 验证
```bash
npm run type-check     # 类型检查通过
npm run build-only     # 构建成功
# 手动检查：文章详情正常渲染、评论加载/提交正常
```

---

## P4：专栏系统优化

### 目标
教程/专栏区域风格统一。

### 4.1 ✅ ColumnView / ColumnList
- ✅ 专栏卡片样式冷调优化
- ✅ 列表/网格布局

### 4.2 ✅ ColumnCard
- ✅ 卡片设计统一

### 4.3 ✅ ColumnDetailView / ColumnMenu / ColumnSideBar / ColumnArticleDetail
- ✅ 专栏详情布局
- ✅ 侧边目录样式
- ✅ 文章内导航

### ✅ P4 验证 ✅
```bash
npm run type-check     # ✅ 类型检查通过
npm run build-only     # ✅ 构建成功
npm run lint           # ✅ 0 errors, 63 warnings (pre-existing)
# 手动检查：专栏列表页、专栏详情页渲染正常
```

---

## P5：用户中心优化

### 目标
个人主页视觉升级。

### 5.1 ✅ UserHomeView / UserHomeInfo
- ✅ 用户主页布局重构（暗色渐变封面、头像、统计数字）
- ✅ 个人信息卡片冷调化
- ✅ 编辑弹窗样式更新

### 5.2 ✅ UserCard / UserFollowedCard
- ✅ 用户卡片样式全面更新（冷色调、统计数据、关注按钮）
- ✅ 关注列表卡片设计

### 5.3 ✅ UserHomeNavBar / 子页面
- ✅ Tabs 导航样式冷调化
- ✅ el-tab 激活态蓝色高亮
- ✅ 分页器居中

### 5.4 ✅ UserAchievement / UserHistory / UserFollowedList
- ✅ 成就卡片用 SVG 替换 base64 图标
- ✅ 创作历程添加进度条可视化
- ✅ 各 tab 页面样式统一

### ✅ P5 验证 ✅
```bash
npm run type-check     # ✅ 类型检查通过
npm run build-only     # ✅ 构建成功
npm run lint           # ✅ 0 errors, 50 warnings (pre-existing)
# 手动检查：用户主页各 tab 渲染正常，编辑弹窗功能正常
```

---

## P6：其余页面统一优化

### 目标
覆盖所有剩余页面。

### 6.1 ✅ TagArticlesView / ArticleTagView
- ✅ 标签文章列表空状态占位

### 6.2 ✅ ChatView
- ✅ AI 聊天页面冷调化（气泡、输入框、头部）
- ✅ 清理无用 import，简化代码

### 6.3 ✅ ArticleEditView
- ✅ 编辑器页面布局冷调化
- ✅ 发布弹窗样式更新

### 6.4 ✅ NoticeView 系列
- ✅ 通知列表页面样式统一（冷色调 tabs、卡片）
- ✅ 橙色高亮 → 蓝色高亮

### 6.5 ✅ AboutView / PlanView / ToolsView
- ✅ 关于页面冷调化（信息卡片、列表样式）
- ✅ ToolsView 菜单样式冷调化
- ✅ PlanView 与全局背景统一

### ✅ P6 验证 ✅
```bash
npm run type-check     # ✅ 类型检查通过
npm run build-only     # ✅ 构建成功
npm run lint           # ✅ 0 errors, 38 warnings (pre-existing)
# 手动检查：各路由页面渲染正常，AI 对话功能正常
```

---

## P7：暗色模式适配

### 目标
在所有页面完成亮色模式优化后，统一适配暗色模式。

### 7.1 暗色 CSS 变量定义
- 在 `base.css` 的 `.dark` 选择器中定义完整暗色变量
- 暗色背景深蓝黑系：`#0f111a` / `#161822` / `#1e2030`
- 暗色文字：`#e1e3eb` / `#b0b4c4`
- 暗色边框：`#2a2d3a`

### 7.2 Element Plus 暗色主题同步
- 确认 `main.ts` 中暗色 CSS 导入正确
- 覆盖 Element Plus 暗色变量以匹配冷调设计

### 7.3 逐组件暗色适配
- 按照 P1-P6 的顺序，逐一确认每个组件在 `.dark` 模式下表现
- 添加必要的 `dark:` Tailwind 类

### ✅ P7 验证 ✅
```bash
npm run type-check     # ✅ 类型检查通过
npm run build-only     # ✅ 构建成功
npm run lint           # ✅ 0 errors, 38 warnings (pre-existing)
# 手动检查：切换暗色模式，检查所有页面
```

---

## P8：动效与微交互增强

### 目标
在所有页面视觉完成后，注入丰富的动效层。

### 8.1 页面入场动画系统
- 路由切换时 fade-in + slide-up 过渡
- 基于 `vue-router` 的导航守卫触发动画

### 8.2 滚动驱动动画
- 文章卡片列表 staggered 入场
- 滚动到可视区域触发 (IntersectionObserver)
- 滚动进度条

### 8.3 Hover 微交互增强
- 卡片 hover 浮动+阴影增强
- 按钮点击波纹效应
- 链接 hover 下划线动画
- 点赞/收藏图标弹跳动画

### 8.4 首页 Hero 区域增强
- 背景几何图案/网格（纯 CSS）
- 推荐文字入场动画序列

### 8.5 页面过渡
- 使用 Vue `<Transition>` 包裹路由视图
- 定义简洁的页面切换动效

### ✅ P8 验证 ✅
```bash
npm run type-check     # ✅ 类型检查通过
npm run build-only     # ✅ 构建成功
npm run lint           # ✅ 0 errors, 38 warnings (pre-existing)
# 手动检查：页面入场动画流畅、卡片入场动画正常、Hero 几何背景显示
# 手动检查：点赞图标弹跳动画、阅读进度条、链接下划线动画
```

---

## 最终验收标准

### 自动化检查
```bash
npm run type-check     # ✅ 0 errors
npm run build-only     # ✅ 构建成功
npm run lint           # ✅ 0 errors, 0 warnings
```

### 人工验收清单
- [ ] 亮色模式所有页面浏览正常
- [ ] 冷调配色在所有页面一致
- [ ] 字体系统在所有页面正确渲染
- [ ] 导航/布局在所有视图一致
- [ ] 首页各区块完整（分类导航、推荐、文章列表、分页）
- [ ] 文章详情阅读体验良好
- [ ] 评论区功能完整美观
- [ ] 专栏系统导航清晰
- [ ] 用户中心各 tab 正常
- [ ] 暗色模式所有页面正常
- [ ] 页面入场动效流畅
- [ ] 移动端响应式正常
- [ ] 交互反馈（hover/click）一致

---

## 执行说明

### 一键执行
```bash
# 从 P0 开始依次执行
/exec-plan
```

### 中断后恢复
```bash
# 从中断处继续
/exec-plan resume
```

### 查看进度
```bash
/exec-plan status
```

### 每阶段后自动验证
每个阶段完成后运行：
```bash
npm run type-check && npm run build-only && npm run lint
```