# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
此文件为Claude Code (claude.ai/code) 在此代码库中工作提供指导。

## Project Overview 项目概述

This is the frontend for "技术派" (Tech Pai), a community platform built with Vue 3 + TypeScript. It connects to a Spring Boot 3 backend. Key technologies:
这是"技术派" (Tech Pai) 社区平台的前端，使用Vue 3 + TypeScript构建。它连接到一个Spring Boot 3后端。关键技术：

- **Vue 3** with Composition API and `<script setup>` syntax (Vue 3，使用Composition API和`<script setup>`语法)
- **TypeScript** for type safety (用于类型安全)
- **Element Plus** UI component library (Chinese locale configured) (Element Plus UI组件库，配置为中文语言环境)
- **Tailwind CSS** for styling with custom width utilities (`navBarMDInput`, `navBarInput`) (用于样式设计，带有自定义宽度工具类)
- **Pinia** for state management (single global store) (用于状态管理，单一全局存储)
- **Vue Router** with route-based code splitting (带有基于路由的代码分割)
- **Axios** with interceptors for authentication and error handling (带有认证和错误处理拦截器)
- **Vite** as build tool with proxy configuration for local development (作为构建工具，带有本地开发代理配置)

## Development Environment 开发环境

### Prerequisites 先决条件
- Node.js (version not specified, but project uses `type: "module"`) (版本未指定，但项目使用 `type: "module"`)
- npm (package-lock.json indicates npm) (package-lock.json表明使用npm)

### Setup 安装
```bash
npm install
```

### Common Commands 常用命令
```bash
# Start development server with hot reload (启动开发服务器，带热重载)
npm run dev

# Build for production (runs type check first) (构建生产版本，先运行类型检查)
npm run build

# Build only (skip type check) (仅构建，跳过类型检查)
npm run build-only

# Run TypeScript type checking (运行TypeScript类型检查)
npm run type-check

# Lint code with ESLint and auto-fix (使用ESLint检查代码并自动修复)
npm run lint

# Format code with Prettier (src/ directory only) (使用Prettier格式化代码，仅src/目录)
npm run format

# Preview production build locally (本地预览生产构建)
npm run preview
```

### Development Server Proxy 开发服务器代理
The Vite dev server proxies API requests to `http://127.0.0.1:8081` (local Spring Boot backend). Configuration in `vite.config.ts`:
Vite开发服务器将API请求代理到 `http://127.0.0.1:8081`（本地Spring Boot后端）。配置在 `vite.config.ts` 中：
- `/api` → `http://127.0.0.1:8081` (path rewrite removes `/api` prefix) (路径重写移除 `/api` 前缀)
- WebSocket connections use `ws/api` path (WebSocket连接使用 `ws/api` 路径)

To change backend URL, update `vite.config.ts` server.proxy configuration.
要更改后端URL，请更新 `vite.config.ts` 中的server.proxy配置。

## Project Architecture 项目架构

### Source Structure (`src/`) 源代码结构
```
src/
├── assets/           # CSS files (main.css, index.css) CSS文件
├── components/       # Reusable Vue components organized by feature 可复用的Vue组件，按功能组织
│   ├── article/      # Article-related components 文章相关组件
│   ├── column/       # Column/Series components 专栏/系列组件
│   ├── comment/      # Comment components 评论组件
│   ├── dialog/       # Modal dialogs (LoginDialog) 模态对话框
│   ├── icons/        # SVG icon components SVG图标组件
│   ├── layout/       # Layout components (HeaderBar, Footer, SideImage) 布局组件
│   ├── notice/       # Notification components 通知组件
│   └── side/         # Sidebar widgets 侧边栏小部件
├── constants/        # Application constants and enums 应用常量和枚举
│   ├── element-plus-constants/  # Element Plus specific constants Element Plus特定常量
│   └── *.ts          # Various constant files (UserHomeTabTypeConstants, etc.) 各种常量文件
├── http/             # API communication layer API通信层
│   ├── URL.ts        # All API endpoint definitions 所有API端点定义
│   ├── BackendRequests.ts       # Axios instance with interceptors 带有拦截器的Axios实例
│   └── ResponseTypes/           # TypeScript interfaces for API responses API响应的TypeScript接口
├── router/           # Vue Router configuration (index.ts) Vue Router配置
├── stores/           # Pinia store (global.ts for shared application state) Pinia存储（用于共享应用状态的global.ts）
├── util/             # Utility functions (utils.ts) 工具函数
└── views/            # Route-level page components 路由级页面组件
```

### Key Architectural Patterns 关键架构模式

#### API Layer API层
- **`src/http/URL.ts`**: Centralized endpoint definitions. Base URL is `/api` (proxied to backend). 集中式端点定义。基础URL是 `/api`（代理到后端）。
- **`src/http/BackendRequests.ts`**: Axios instance with: 带有以下功能的Axios实例：
  - Request interceptor adds JWT token from `sessionStorage`/`localStorage` 请求拦截器从 `sessionStorage`/`localStorage` 添加JWT令牌
  - Response interceptor handles authentication errors (code > 900) 响应拦截器处理认证错误（code > 900）
  - Helper functions: `doGet`, `doPost`, `doFilePost`, `doPut`, `doDelete` 辅助函数
  - Special `mockLoginXML` for simulating WeChat authentication 用于模拟微信认证的特殊 `mockLoginXML`
- **Response Types**: TypeScript interfaces in `src/http/ResponseTypes/` define API response structures. 响应类型：`src/http/ResponseTypes/` 中的TypeScript接口定义API响应结构。

#### State Management 状态管理
- Single Pinia store at `src/stores/global.ts` manages global application state. `src/stores/global.ts` 中的单一Pinia存储管理全局应用状态。
- Store holds `GlobalResponse` data (user info, site info, login status). 存储包含 `GlobalResponse` 数据（用户信息、站点信息、登录状态）。
- Store is initialized in router guards and used across components. 存储在路由守卫中初始化并在组件间使用。
- Helper function `getGlobalStore()` ensures Pinia is installed before use. 辅助函数 `getGlobalStore()` 确保在使用前安装Pinia。

#### Routing 路由
- **`src/router/index.ts`**: Defines all routes with lazy loading for most views. 定义所有路由，大多数视图使用懒加载。
- Route guards check `meta.loginRequired` and validate login status via global store. 路由守卫检查 `meta.loginRequired` 并通过全局存储验证登录状态。
- Dynamic routes: `/article/detail/:articleId`, `/user/:userId/:typeName`, `/column/:columnId/:sectionId` 动态路由
- Nested routes under `/tools` for tool pages. `/tools` 下的嵌套路由用于工具页面。

#### Authentication 认证
- JWT tokens stored in `sessionStorage` (primary) or `localStorage` (fallback). JWT令牌存储在 `sessionStorage`（主要）或 `localStorage`（备用）。
- Token name retrieved via `getTokenName()` utility. 令牌名称通过 `getTokenName()` 工具函数获取。
- Interceptor adds `Authorization` header with token value. 拦截器添加带有令牌值的 `Authorization` 头。
- Login status checked via `/api/global/info` endpoint in router guards. 在路由守卫中通过 `/api/global/info` 端点检查登录状态。
- Mock login available via XML requests to simulate WeChat OAuth. 通过XML请求模拟微信OAuth的模拟登录。

#### Styling 样式
- **Tailwind CSS** with custom configuration in `tailwind.config.js`. 带有 `tailwind.config.js` 中自定义配置的Tailwind CSS。
- **Element Plus** components with Chinese localization (`zh-cn`). 带有中文本地化（`zh-cn`）的Element Plus组件。
- Custom CSS in `src/assets/main.css` and `src/index.css`. 自定义CSS在 `src/assets/main.css` 和 `src/index.css` 中。

#### Component Organization 组件组织
- Components are feature-organized within `src/components/`. 组件在 `src/components/` 内按功能组织。
- Layout components (`HeaderBar`, `Footer`, `SideImage`) provide consistent page structure. 布局组件提供一致的页面结构。
- Page-level components in `src/views/` correspond to routes. `src/views/` 中的页面级组件对应路由。
- Reusable UI components (cards, lists, dialogs) are in appropriate feature directories. 可复用的UI组件（卡片、列表、对话框）在相应的功能目录中。

## Configuration Files 配置文件

### `vite.config.ts`
- Aliases `@` to `./src` 别名 `@` 指向 `./src`
- Optimizes dependencies (includes `md-editor-v3`) 优化依赖项（包含 `md-editor-v3`）
- Proxy configuration for development 开发代理配置
- Vue and JSX plugins enabled 启用Vue和JSX插件

### `tailwind.config.js`
- Content includes `index.html` and all Vue/JS/TS files in `src` 内容包括 `index.html` 和 `src` 中所有Vue/JS/TS文件
- Custom width utilities defined 定义了自定义宽度工具类
- No additional plugins 没有额外的插件

### TypeScript Configurations TypeScript配置
- `tsconfig.json`: Base configuration 基础配置
- `tsconfig.app.json`: App-specific settings 应用特定设置
- `tsconfig.node.json`: Node-specific settings Node特定设置

## Development Notes 开发说明

### API Base URL Configuration API基础URL配置
The backend base URL is configured in two places: 后端基础URL在两个位置配置：
1. **Development**: `vite.config.ts` server.proxy target (`http://127.0.0.1:8081`) 开发环境：`vite.config.ts` server.proxy目标（`http://127.0.0.1:8081`）
2. **Production**: Axios base URL is `/api` (relative, should be same origin as frontend) 生产环境：Axios基础URL是 `/api`（相对路径，应与前端同源）

To change backend location, update both: 要更改后端位置，请更新两者：
- `vite.config.ts` proxy target for development 开发环境的 `vite.config.ts` 代理目标
- Build configuration for production (may need different approach) 生产环境的构建配置（可能需要不同的方法）

### Custom Skills 自定义技能

- **`/exec-plan`**: Execute multi-step plans (PRO.MD) step by step with automated verification. 按步骤执行 PRO.MD 计划，每步自动验证。
  - `/exec-plan` — 从第一个未完成的步骤开始执行
  - `/exec-plan resume` — 从中断处继续
  - `/exec-plan status` — 查看当前进度

### Authentication Flow 认证流程
1. User logs in via `LoginDialog` component 用户通过 `LoginDialog` 组件登录
2. Token stored in `sessionStorage` with key from `getTokenName()` 令牌使用来自 `getTokenName()` 的键存储在 `sessionStorage` 中
3. Subsequent API requests include token in `Authorization` header 后续API请求在 `Authorization` 头中包含令牌
4. Router guards check login status for protected routes 路由守卫检查受保护路由的登录状态
5. Token validation errors (code > 900) trigger logout prompt 令牌验证错误（code > 900）触发注销提示

### Global Store Usage 全局存储使用
The global store contains site-wide information (user, categories, etc.). It's populated on app initialization and route changes. Components access it via:
全局存储包含全站信息（用户、分类等）。它在应用初始化和路由更改时填充。组件通过以下方式访问：
```typescript
import { useGlobalStore } from '@/stores/global'
const globalStore = useGlobalStore()
```

### Code Style 代码风格
- ESLint with Vue 3 and TypeScript rules 使用Vue 3和TypeScript规则的ESLint
- Prettier for formatting (run `npm run format`) 用于格式化的Prettier（运行 `npm run format`）
- Vue 3 Composition API with `<script setup>` syntax 使用 `<script setup>` 语法的Vue 3 Composition API
- TypeScript strict mode enabled 启用TypeScript严格模式

### Asset Management 资产管理
- Static assets in `public/` directory 静态资源在 `public/` 目录中
- CSS imports in `main.ts` and component `<style>` blocks CSS导入在 `main.ts` 和组件 `<style>` 块中
- SVG components in `src/components/icons/` SVG组件在 `src/components/icons/` 中