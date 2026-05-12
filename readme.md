# 企业文档管理系统

基于 JDK 17 + Spring Boot 3 + Vue 3 + MySQL 的企业文档管理系统

## 项目结构

```
enterprise_doc/
├── backend/           # 后端 Spring Boot 项目
│   ├── src/main/java/com/enterprise/doc/
│   │   ├── common/       # 通用类（返回结果、分页等）
│   │   ├── config/       # 配置类
│   │   ├── controller/   # 控制器
│   │   ├── dto/          # 数据传输对象
│   │   ├── entity/       # 实体类
│   │   ├── exception/    # 异常处理
│   │   ├── mapper/       # MyBatis Mapper
│   │   ├── security/     # 安全认证
│   │   ├── service/      # 业务逻辑
│   │   ├── util/         # 工具类
│   │   └── vo/           # 视图对象
│   ├── sql/              # 数据库脚本
│   └── pom.xml
├── frontend/          # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/         # API 接口
│   │   ├── router/      # 路由配置
│   │   ├── stores/      # Pinia 状态管理
│   │   ├── utils/       # 工具类
│   │   ├── views/       # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
└── readme.md
```

## 功能特性

### 核心功能
- **用户系统**：用户注册、登录、JWT 认证
- **文库管理**：
  - 公共文库（全部文库）- 所有人可见
  - 个人文库 - 仅创建者可见
  - 支持创建、编辑、删除文库
- **文件夹管理**：多层级文件夹，支持创建、重命名、删除
- **文档管理**：
  - 文档上传、下载、重命名、移动、删除
  - 版本控制
  - 浏览/下载次数统计
- **文档预览**：
  - 图片：jpg、png、gif、webp 等
  - PDF：直接在线预览
  - Markdown：在线渲染预览
  - 纯文本：txt、csv 等
  - 视频/音频：在线播放
  - Word/Excel/PPT：需要下载后使用对应软件打开
- **文档分享**：
  - 生成分享链接
  - 支持设置访问密码
  - 支持设置有效期（永久/1天/7天/30天）
  - 可取消分享

### 支持的文件类型
- 文档类：Word (doc/docx)、Excel (xls/xlsx)、PPT (ppt/pptx)、PDF
- 表格类：CSV
- 文本类：TXT、Markdown (md)、XML、JSON、HTML
- 图片类：JPG、PNG、GIF、BMP、WebP、SVG
- 视频类：MP4、WebM、OGG
- 音频类：MP3、WAV
- 压缩包：ZIP、RAR、7z

## 技术栈

### 后端
- JDK 17
- Spring Boot 3.2.5
- Spring Security 6 + JWT
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis（可选，用于缓存）
- Hutool 工具库
- Knife4j (Swagger UI)

### 前端
- Vue 3.4
- Vue Router 4
- Pinia 2
- Element Plus 2
- Axios
- Vite 5
- Marked (Markdown 渲染)
- DOMPurify (XSS 防护)

## 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+ (可选)

## 快速开始

### 1. 初始化数据库

在 MySQL 中执行后端 SQL 脚本：

```bash
mysql -u root -p < backend/sql/init.sql
```

或者手动执行 `backend/sql/init.sql` 文件内容。

默认管理员账号：
- 用户名：`admin`
- 密码：`admin123`

### 2. 配置后端

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/enterprise_doc?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 如有密码
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080/api` 启动。

Swagger 文档：`http://localhost:8080/api/doc.html`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:3000` 启动。

## API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户

### 文库接口
- `GET /api/library/public` - 公共文库列表
- `GET /api/library/personal` - 个人文库列表
- `POST /api/library` - 创建文库
- `PUT /api/library/{id}` - 更新文库
- `DELETE /api/library/{id}` - 删除文库
- `GET /api/library/{id}` - 获取文库详情

### 文件夹接口
- `GET /api/folder/library/{id}` - 获取文库所有文件夹（树形）
- `POST /api/folder` - 创建文件夹
- `PUT /api/folder/{id}/rename` - 重命名文件夹
- `DELETE /api/folder/{id}` - 删除文件夹

### 文档接口
- `GET /api/document/list` - 文档列表
- `POST /api/document/upload` - 上传文档
- `GET /api/document/{id}` - 文档详情
- `GET /api/document/{id}/download` - 下载文档
- `PUT /api/document/{id}/rename` - 重命名文档
- `PUT /api/document/{id}/move` - 移动文档
- `DELETE /api/document/{id}` - 删除文档
- `POST /api/document/{id}/update` - 更新文档版本

### 分享接口
- `POST /api/share` - 创建分享
- `DELETE /api/share/{id}` - 取消分享
- `POST /api/share/verify` - 验证分享
- `GET /api/share/document/{code}` - 获取分享文档

### 文件预览
- `GET /api/doc/view/{path}` - 预览文件
- `GET /api/doc/view/{path}?download=true` - 下载文件

## 目录说明

### 文件存储
上传的文件默认存储在 `backend/uploads` 目录，按日期分目录存储：
```
uploads/
└── 2024/
    └── 01/
        └── 15/
            └── {uuid}.{ext}
```

## 开发计划

- [ ] 集成 LibreOffice/OnlyOffice 实现 Office 文档在线预览和编辑
- [ ] 文档版本历史管理
- [ ] 文档协作编辑
- [ ] 全文搜索
- [ ] 文档回收站
- [ ] 更细粒度的权限控制
- [ ] 操作日志
- [ ] 文件分片上传/断点续传

## License

MIT License
