# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
此文件为Claude Code (claude.ai/code) 在此代码库中工作提供指导。

## Project Overview 项目概述

paicoding-springboot3-vue3 is a community forum platform built with Spring Boot 3 (backend) and Vue 3 (frontend). It features a modular monolith architecture with separate admin dashboard (React) and main frontend (Vue). The platform supports article publishing, tutorials, comments, rankings, AI chat, and comprehensive management capabilities.

paicoding-springboot3-vue3 是一个基于 Spring Boot 3（后端）和 Vue 3（前端）构建的社区论坛平台。采用模块化单体架构，包含独立的管理后台（React）和主前端（Vue）。平台支持文章发布、教程、评论、排行榜、AI对话以及全面的管理功能。

## Module Structure 模块结构

```
paicoding-springboot3-vue3/
├── paicoding-api/          # API definitions, DTOs, VOs, enums API定义、DTO、VO、枚举
├── paicoding-core/         # Core utilities and components 核心工具和组件
├── paicoding-service/      # Business logic and database operations 业务逻辑和数据库操作
├── paicoding-web/          # Web entry point, Spring Boot application Web入口，Spring Boot应用
├── pai-coding-front/       # Main frontend (Vue 3 + TypeScript) 主前端（Vue 3 + TypeScript）
└── paicoding-admin/        # Admin dashboard (React 18 + TypeScript) 管理后台（React 18 + TypeScript）
```

## Development Environment Setup 开发环境设置

### Prerequisites 先决条件
- **JDK 17+** (Spring Boot 3 requires Java 17+)
- **Maven 3.5+**
- **MySQL 8.0+**
- **Redis 6.0+** (required for backend)
- **Node.js 16+** (for frontend and admin)
- **npm** or **yarn**

### Database Setup 数据库设置
1. Create database `pai_coding` (or configure name in `paicoding-web/src/main/resources/application.yml`)
2. Tables are automatically created via Liquibase on first startup
3. Configure connection in `paicoding-web/src/main/resources-env/dev/application-dal.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://127.0.0.1:3306/${database.name}?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
       username: root
       password: your_password
   ```

## Common Commands 常用命令

### Backend (Spring Boot) 后端
```bash
# Build all modules (default dev profile) 构建所有模块（默认dev环境）
mvn clean install -DskipTests=true

# Build with specific profile (dev/test/pre/prod) 使用特定环境构建
mvn clean install -DskipTests=true -Pprod

# Run tests 运行测试
mvn test

# Build executable JAR for paicoding-web module 构建可执行JAR（paicoding-web模块）
cd paicoding-web
mvn clean package spring-boot:repackage -Dmaven.test.skip=true -Pdev

# Start the application locally 本地启动应用
cd paicoding-web
mvn spring-boot:run -Pdev
# Or run the main class: QuickForumApplication

# Production deployment script 生产部署脚本
./launch.sh start      # Build and start 构建并启动
./launch.sh restart    # Restart application 重启应用
```

### Main Frontend (Vue 3) 主前端
```bash
cd pai-coding-front

# Install dependencies 安装依赖
npm install

# Start development server 启动开发服务器
npm run dev

# Build for production 构建生产版本
npm run build

# Type checking 类型检查
npm run type-check

# Lint and format 代码检查和格式化
npm run lint
npm run format

# Preview production build 预览生产构建
npm run preview
```

### Admin Dashboard (React) 管理后台
```bash
cd paicoding-admin

# Install dependencies 安装依赖
npm install
# If installation fails, use mirror registry 如果安装失败，使用镜像源
npm install --registry=http://registry.npmmirror.com

# Start development server 启动开发服务器
npm run dev

# Production build 生产构建
npm run build:pro

# Lint and format 代码检查和格式化
npm run lint:eslint
npm run lint:prettier
```

## Architecture Overview 架构概览

### Backend Architecture 后端架构
- **Layered Architecture**: Controller → Service → DAO 分层架构
- **Database**: MySQL with MyBatis-Plus ORM 使用MyBatis-Plus ORM的MySQL
- **Caching**: Redis for session and data caching 使用Redis进行会话和数据缓存
- **Search**: ElasticSearch for article search 使用ElasticSearch进行文章搜索
- **Message Queue**: RabbitMQ for async processing 使用RabbitMQ进行异步处理
- **File Storage**: Aliyun OSS for image uploads 使用阿里云OSS进行图片上传
- **WebSocket**: Real-time chat and notifications 实时聊天和通知

### Frontend Architecture 前端架构
- **Vue 3** with Composition API and `<script setup>` syntax 使用Composition API和`<script setup>`语法
- **TypeScript** for type safety 用于类型安全
- **Pinia** for state management (single global store) 使用Pinia进行状态管理（单一全局存储）
- **Vue Router** with lazy loading 使用Vue Router并支持懒加载
- **Element Plus** UI component library (Chinese locale) Element Plus UI组件库（中文语言环境）
- **Tailwind CSS** for styling 使用Tailwind CSS进行样式设计
- **Axios** with interceptors for authentication 使用带拦截器的Axios进行认证

### Admin Architecture 管理后台架构
- **React 18** with Functional Components and Hooks 使用函数组件和Hooks的React 18
- **Redux** with redux-persist for state management 使用Redux和redux-persist进行状态管理
- **Ant Design 5.x** UI component library Ant Design 5.x UI组件库
- **Vite 3** as build tool 使用Vite 3作为构建工具
- **ECharts** for data visualization 使用ECharts进行数据可视化

## Configuration 配置说明

### Environment Profiles 环境配置
Backend uses Maven profiles for environment-specific configuration: 后端使用Maven profiles进行环境特定配置：
- **dev**: Local development (default) 本地开发（默认）
- **test**: Testing environment 测试环境
- **pre**: Staging environment 预发环境
- **prod**: Production environment 生产环境

Configuration files are in `paicoding-web/src/main/resources-env/{profile}/`. 配置文件位于 `paicoding-web/src/main/resources-env/{profile}/`。

### Frontend Proxy Configuration 前端代理配置
Development server proxies API requests to backend: 开发服务器将API请求代理到后端：
- **Main frontend**: `http://127.0.0.1:8081` (configured in `pai-coding-front/vite.config.ts`) 主前端：`http://127.0.0.1:8081`（配置在 `pai-coding-front/vite.config.ts`）
- **Admin dashboard**: `http://127.0.0.1:8080` (configured in `paicoding-admin/vite.config.ts`) 管理后台：`http://127.0.0.1:8080`（配置在 `paicoding-admin/vite.config.ts`）

### Authentication 认证
- **JWT tokens** stored in sessionStorage (frontend) or localStorage (admin) JWT令牌存储在sessionStorage（前端）或localStorage（管理后台）
- **Token injection** via Axios interceptors 通过Axios拦截器注入令牌
- **Login validation** via `/api/global/info` endpoint 通过 `/api/global/info` 端点进行登录验证

## Important Notes 重要说明

### Development Tips 开发提示
1. **Backend Port**: Default port is 8080, but may change if occupied (auto-find available port in dev) 后端端口：默认8080，如果被占用可能会更改（开发环境自动查找可用端口）
2. **Redis Required**: Backend requires Redis for session management and caching 后端需要Redis进行会话管理和缓存
3. **Database Auto-creation**: Database and tables are created automatically via Liquibase on first startup 数据库和表在首次启动时通过Liquibase自动创建
4. **Frontend-Backend Sync**: Ensure backend is running before starting frontend development server 启动前端开发服务器前确保后端正在运行

### Known Issues 已知问题
1. **Node.js Version**: Admin dashboard requires Node.js 16+ 管理后台需要Node.js 16+
2. **Windows Line Endings**: Use `dos2unix` on shell scripts if needed Windows行尾：如果需要，对shell脚本使用 `dos2unix`
3. **Chinese Environment**: Project uses Chinese as primary language with Chinese documentation 项目使用中文作为主要语言，并提供中文文档

### Deployment 部署
- **Backend**: Use `./deploy.sh prod` to build and deploy to production server 后端：使用 `./deploy.sh prod` 构建并部署到生产服务器
- **Frontend**: Build static files and deploy to Nginx or CDN 前端：构建静态文件并部署到Nginx或CDN
- **Admin**: Build static files and deploy to `/admin` path on server 管理后台：构建静态文件并部署到服务器的 `/admin` 路径

## Related Documentation 相关文档
- [Local Development Tutorial](docs/本地开发环境配置教程.md) 本地开发环境配置教程
- [Environment Setup](docs/安装环境.md) 环境安装
- [Server Startup](docs/服务器启动教程.md) 服务器启动教程
- [Frontend Structure](docs/前端工程结构说明.md) 前端工程结构说明
- [Conventions](docs/约定.md) 项目约定