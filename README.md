# Tutu Backend · 图片协同管理平台

> 一个基于 Spring Boot 的图片素材库 / 图床 + 空间管理系统：支持图片上传与审核、缩略图与 webp 自动转换、以图搜图、按颜色搜索、AI 扩图，以及面向私有空间的多维度容量统计分析。

![Java](https://img.shields.io/badge/Java-1.8-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.6-brightgreen)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.15-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1)
![Redis](https://img.shields.io/badge/Redis-Session%20Store-DC382D)
![Caffeine](https://img.shields.io/badge/Cache-Caffeine%203.1.8-6DB33F)
![COS](https://img.shields.io/badge/Storage-Tencent%20COS-0052D9)
![Knife4j](https://img.shields.io/badge/Docs-Knife4j%204.4.0-ff69b4)
![License](https://img.shields.io/badge/License-Private-lightgrey)

- 仓库地址：<https://github.com/A-lioth/tutu>
- 后端工程：`tutu-backend`（Maven `com.alioth:tutu-backend:0.0.1-SNAPSHOT`）
- 主类：`com.alioth.tutubackend.TutuBackendApplication`
- 规模：90 个 Java 文件、约 5707 行 Java 代码、3 个 Mapper XML、99 个受控文件

---

## 项目简介

**Tutu** 是一个图片协同管理平台，核心解决的问题是「把散落在本地和网上的图片，变成可检索、可分类、可审核、可配额管理的素材资产」。

系统提供两个层次的图库：

| 层次 | 说明 | 判定方式 |
| --- | --- | --- |
| 公共图库 | 所有用户共享的公开素材库，仅展示审核通过的图片 | `picture.spaceId` 为空 |
| 私有空间 | 用户自建的空间，容量与数量受空间级别限制，仅空间归属者（及管理员）可见可操作 | `picture.spaceId` 非空 |

项目在功能设计上参考了鱼皮（yupi）的「云图库」教学项目，但**代码为本人独立重构实现**：包名为 `com.alioth.tutubackend`（参考项目为 `com.yupi.yupicturebackend`），实体、DTO/VO 分层、权限模型、上传链路、分析模块均为自行编写与重组。全仓库 48 次提交按「用户 → 权限 → 文件存储 → 图片 → 缓存 → 空间 → AI → 分析」的顺序逐步演进，可用于观察一个完整业务系统的搭建轨迹。

---

## 功能特性

### 用户与权限

- 用户注册 / 登录 / 登出 / 获取当前登录用户（Session 登录态存放在 Redis，Cookie 有效期 30 天）
- 用户密码加盐 MD5 摘要存储，登录态与业务数据分离（会话中只存用户快照，每次请求回查数据库）
- 管理员对用户的增删改查与分页查询
- 自定义 `@AuthCheck(mustRole = ...)` 注解 + AOP 环绕切面完成接口级角色校验，注解声明式、零业务侵入
- 全局跨域配置（`CorsConfig`），允许携带 Cookie

### 图片管理

- **文件上传**（`multipart/form-data`）与 **URL 上传**（服务端拉取远端图片），两条链路共用一套模板方法
- **批量抓取上传**：基于 jsoup 解析 Bing 图片搜索结果页，批量下载并入库，单次最多 30 张
- **图片审核**：`0` 待审核 / `1` 通过 / `2` 拒绝；管理员上传自动过审，普通用户上传与编辑后重新进入待审核
- **缩略图**：上传时由腾讯云 COS 图片处理自动生成 128×128 缩略图
- **自动转 webp**：上传时通过 `imageMogr2/format/webp` 规则转码，减小体积并统一格式
- **主色调提取**：由 COS 返回图片平均色（`picColor`），用于按颜色搜索
- 图片编辑、批量编辑（批量改分类 / 标签 / 按 `{序号}` 规则批量重命名）
- 图片删除时同步释放空间额度，并**异步清理** COS 上的原图与缩略图（同一 URL 被多条记录引用时跳过清理）
- 按编辑时间范围查询（`startEditTime` / `endEditTime`）
- 标签 / 分类选项接口（预置热门标签与常用分类）

### 检索能力

- **以图搜图**：以 360 搜图接口为数据源，先用缩略图取回结果页地址，再翻页拉取相似图片列表
- **按颜色搜索**：在指定空间内按 RGB 欧氏距离计算颜色相似度并降序取前 12 张（`ColorSimilarUtils`）
- 多字段模糊搜索（名称 / 简介）、标签 JSON 数组匹配、分类精确匹配、分页与自定义排序

### 空间管理

- 空间创建 / 删除 / 编辑 / 更新，以及管理员的全量分页查询
- **空间级别与配额**（`SpaceLevelEnum`）：

| 级别 | value | 图片数量上限 | 总容量上限 |
| --- | --- | --- | --- |
| 普通版 | 0 | 100 张 | 100 MB |
| 专业版 | 1 | 1000 张 | 1000 MB |
| 旗舰版 | 2 | 10000 张 | 10000 MB |

- 同一用户仅允许创建一个空间（`userId.intern()` 加锁 + 事务内二次校验）
- 上传前校验空间归属与剩余额度，上传后在同一事务内累加 `totalSize` / `totalCount`；删除图片时回退额度
- 空间权限校验抽为独立方法（`checkSpaceAuth`），提交 `f3b49ca refactor(space): 重构空间权限校验逻辑`
- 历史设计中曾有「私有空间 / 团队空间」的类型划分（见文末问题记录第 2 条），当前实现以空间级别区分容量额度

### 空间分析

- **使用情况分析**：已用容量 / 已用数量 / 上限 / 使用率百分比
- **分类分析**：按 `category` 分组统计数量与总大小（无分类归入「未分类」）
- **标签分析**：解析 `tags` JSON 数组并扁平统计，按使用次数降序
- **大小分析**：按 `<100KB`、`100KB-500KB`、`500KB-1MB`、`>1MB` 四档分桶统计
- **用户行为分析**：按日 / 周 / 月三个时间维度统计上传趋势
- **空间使用排行 TopN**：按 `totalSize` 降序取前 N 个空间（仅管理员）
- 所有分析接口统一支持三种查询范围：`queryAll`（全空间）、`queryPublic`（公共图库）、`spaceId`（指定私有空间）

### AI 能力

- **阿里云 AI 图像扩图**（`api/aliyunai`）：创建异步扩图任务 + 按 `taskId` 轮询任务状态
- 扩图结果通过 URL 上传接口回流入库（`UrlPictureUpload#getOriginFilename` 对 OSS 结果链接做了专门的文件名解析）

---

## 技术栈

| 分类 | 技术 | 版本 | 用途 |
| --- | --- | --- | --- |
| 语言 | Java | 1.8 | 开发语言（源码与目标版本均为 1.8） |
| 框架 | Spring Boot | 2.7.6 | 应用基础框架（注意：**不是 3.x**，仍为 `javax.*` 命名空间） |
| Web | spring-boot-starter-web | 2.7.6 | MVC、内嵌 Tomcat、文件上传 |
| 切面 | spring-boot-starter-aop | 2.7.6 | `@AuthCheck` 权限切面 |
| ORM | MyBatis-Plus | 3.5.15 | 单表 CRUD、`QueryWrapper` 条件构造、逻辑删除 |
| 分页 | mybatis-plus-jsqlparser-4.9 | 3.5.15 | 分页插件依赖的 SQL 解析器 |
| 数据库 | MySQL（`mysql-connector-j`） | 8.x | 业务数据持久化，库名 `tutu` |
| 缓存 | Redis + spring-boot-starter-data-redis | 2.7.6 | 分布式缓存、Session 存储 |
| 会话 | spring-session-data-redis | 2.7.6 | 登录态存 Redis，Cookie 30 天过期 |
| 本地缓存 | Caffeine | 3.1.8 | 图片列表一级缓存（5 分钟过期） |
| HTML 解析 | jsoup | 1.15.3 | 解析 Bing 图片搜索结果页，用于批量抓图 |
| 对象存储 | 腾讯云 COS `cos_api` | 5.6.227 | 图片存储、图片处理（webp / 缩略图 / 元信息），bucket `tutu-1393238311`，地域 `ap-beijing` |
| 工具库 | Hutool | 5.8.38 | HTTP、JSON、文件、日期、随机数等工具 |
| 简化代码 | Lombok | 由 Boot 管理 | `@Data`、`@Slf4j` |
| 接口文档 | Knife4j（OpenAPI2 / Swagger2） | 4.4.0 | 接口文档，扫描 `com.alioth.tutubackend.controller` |
| AI | 阿里云百炼图像扩图 API | - | 异步扩图任务创建与查询 |
| 构建 | Maven | 3.8.1 编译插件 | 依赖管理与打包 |

---

## 项目结构

```
tutu-backend
├── pom.xml
├── sql/create_sql.sql                     # 建库建表脚本（含增量 ALTER 语句）
└── src/main
    ├── java/com/alioth/tutubackend
    │   ├── TutuBackendApplication.java     # 启动类
    │   ├── annotation/
    │   │   └── AuthCheck.java              # 自定义权限校验注解（mustRole 指定所需角色）
    │   ├── aop/
    │   │   └── AuthInterceptor.java        # 切面拦截 @AuthCheck，校验当前登录用户角色
    │   ├── api/
    │   │   ├── aliyunai/                   # 阿里云 AI 扩图 API 封装
    │   │   │   ├── ALiYunAiApi.java        # 创建任务 / 查询任务
    │   │   │   └── model/                  # CreateOutPaintingTaskRequest/Response、GetOutPaintingTaskResponse
    │   │   └── imagesearch/                # 以图搜图第三方 API 门面
    │   │       ├── ImageSearchApiFacade.java   # 门面：URL → 结果页 → 图片列表
    │   │       ├── model/ImageSearchResult.java
    │   │       └── sub/                    # GetImagePageUrlApi、GetImageListApi
    │   ├── common/                         # BaseResponse、PageRequest、DeleteRequest、ResultUtils
    │   ├── config/
    │   │   ├── CorsConfig.java             # 全局跨域
    │   │   ├── CosClientConfig.java        # COS 客户端 Bean 与配置绑定
    │   │   ├── JsonConfig.java             # Long 序列化为 String，避免前端精度丢失
    │   │   └── MybatisPlusConfig.java      # Mapper 扫描 + 分页插件
    │   ├── constant/UserConstant.java      # 登录态键、admin/user 角色常量
    │   ├── controller/                     # 5 个 Controller
    │   │   ├── UserController.java
    │   │   ├── PictureController.java
    │   │   ├── SpaceController.java
    │   │   ├── SpaceAnalyzeController.java
    │   │   └── FileController.java
    │   ├── exception/
    │   │   ├── BusinessException.java
    │   │   ├── ErrorCode.java              # 业务错误码枚举
    │   │   ├── ThrowUtils.java             # 条件抛异常的语法糖
    │   │   └── GlobalExceptionHandler.java # @RestControllerAdvice 全局收敛
    │   ├── manager/
    │   │   ├── CosManager.java             # COS 上传 / 下载 / 删除封装（含图片处理规则）
    │   │   ├── FileManager.java            # @Deprecated，早期文件处理实现，已被 upload 包取代
    │   │   └── upload/                     # 模板方法模式
    │   │       ├── PictureUploadTemplate.java  # 抽象模板：校验 → 建临时文件 → 处理 → 上传 → 封装 → 清理
    │   │       ├── FilePictureUpload.java      # 本地文件实现
    │   │       └── UrlPictureUpload.java       # URL 实现
    │   ├── mapper/                         # PictureMapper、SpaceMapper、UserMapper
    │   ├── model/
    │   │   ├── entity/                     # Picture、Space、User
    │   │   ├── enums/                      # PictureReviewStatusEnum、SpaceLevelEnum、UserRoleEnum
    │   │   ├── vo/                         # PictureVO、PictureTagCategory、SpaceVO、UserVO、UserLoginVO
    │   │   │   └── space/analyze/          # 5 个分析响应 VO
    │   │   └── dto/
    │   │       ├── file/UploadPictureResult.java
    │   │       ├── picture/                # 10 个图片 DTO
    │   │       ├── space/                  # 5 个空间 DTO（含 SpaceLevel）
    │   │       ├── space/analyze/          # 7 个分析请求 DTO
    │   │       └── user/                   # 5 个用户 DTO
    │   ├── service/                        # 4 个 Service 接口 + impl/ 4 个实现
    │   │   ├── UserService.java / impl/UserServiceImpl.java
    │   │   ├── PictureService.java / impl/PictureServiceImpl.java
    │   │   ├── SpaceService.java / impl/SpaceServiceImpl.java
    │   │   └── SpaceAnalyzeService.java / impl/SpaceAnalyzeServiceImpl.java
    │   └── utils/ColorSimilarUtils.java    # 颜色相似度计算（按颜色搜索）
    └── resources/
        ├── application.yml                 # 端口、数据源、Redis、Session、MyBatis-Plus、Knife4j
        ├── application-local.yml           # 本地私密配置（已被 .gitignore 忽略，不入库）
        └── mapper/                         # PictureMapper.xml、SpaceMapper.xml、UserMapper.xml
```

---

## 核心设计说明

### 1. 统一响应体

所有接口返回 `BaseResponse<T>`，固定三字段 `code` / `data` / `message`，由 `ResultUtils` 提供 `success` 与 `error` 静态工厂。业务成功时 `code = 0`，失败时 `code` 取自 `ErrorCode` 枚举：

| 错误码 | 枚举 | 含义 |
| --- | --- | --- |
| 0 | `SUCCESS` | ok |
| 40000 | `PARAMS_ERROR` | 请求参数错误 |
| 40100 | `NOT_LOGIN_ERROR` | 未登录 |
| 40101 | `NO_AUTH_ERROR` | 无权限 |
| 40300 | `FORBIDDEN_ERROR` | 禁止访问 |
| 40400 | `NOT_FOUND_ERROR` | 请求数据不存在 |
| 50000 | `SYSTEM_ERROR` | 系统内部异常 |
| 50001 | `OPERATION_ERROR` | 操作失败 |

### 2. 全局异常处理

`GlobalExceptionHandler` 用 `@RestControllerAdvice` 统一捕获两类异常：

- `BusinessException` → 按其自带 `code` 与 `message` 原样返回，业务语义不丢失；
- `RuntimeException` → 统一降级为 `SYSTEM_ERROR / 系统错误`，避免堆栈信息泄漏到响应体（日志侧保留完整堆栈）。

配合 `ThrowUtils.throwIf(condition, errorCode, message)` 的写法，业务校验可以一行完成，无需到处写 `if (...) throw ...`。

### 3. AOP 权限校验

```java
@PostMapping("/review")
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
public BaseResponse<Boolean> pictureReview(...) { ... }
```

`AuthInterceptor` 通过 `@Around("@annotation(authCheck)")` 拦截所有标注了 `@AuthCheck` 的方法：从 `RequestContextHolder` 取当前请求 → 由 `UserService#getLoginUser` 取登录用户 → 比对 `UserRoleEnum`。`mustRole` 为空时直接放行，声明了 `admin` 时要求登录用户必须是管理员。控制器代码中因此没有任何权限分支。

### 4. 模板方法模式的上传链路

图片有两个来源（本地 `MultipartFile` 与远端 `URL`），二者的差异只在于「校验」「取文件名」「落地临时文件」三步。因此抽出抽象模板 `PictureUploadTemplate`：

```
uploadPicture(inputSource, uploadPathPrefix)   ← final，定义不可变骨架
  1. validPicture(inputSource)                 ← abstract
  2. 生成 uuid + 日期 + 后缀的上传路径
  3. 创建临时文件 → processFile(inputSource, file) ← abstract
  4. cosManager.putPictureObject()（webp + 缩略图 + 元信息）
  5. buildResult()（含 webp / 缩略图 / 原图两种分支）
  6. finally deleteTempFile()                   ← 保证临时文件不泄漏
```

`FilePictureUpload` 与 `UrlPictureUpload` 只实现三个钩子方法；调用方按 `inputSource` 的类型（`String` 即 URL）选择实现，业务层无需关心存储细节。这也是提交 `c1a4247 refactor(picture): 重构图片上传功能使用模板方法模式` 的动机。

### 5. 缓存策略（两级缓存）

`POST /picture/list/page/vo/cache` 实现了 Caffeine + Redis 的两级缓存：

1. 以请求 DTO 的 JSON 串做 MD5，拼成 `tutu:listPictureVOByPage:{hash}` 作为缓存键（同一查询条件命中同一键）；
2. **L1 Caffeine**：`initialCapacity=1024`、`maximumSize=10000`、`expireAfterWrite=5min`，命中直接返回；
3. **L2 Redis**：未命中 L1 时查 Redis，命中后**回填 L1**；
4. 两级都未命中才查库，随后写 Redis（过期时间 `300 + random(0,300)` 秒，即 5~10 分钟随机，防缓存雪崩）并写 L1。

### 6. 逻辑删除

全表统一使用 `isDelete` 字段（`1` 已删除 / `0` 未删除），由 `application.yml` 中的 MyBatis-Plus 全局配置驱动，实体上以 `@TableLogic` 标注：

```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDelete
      logic-delete-value: 1
      logic-not-delete-value: 0
```

业务代码调用 `removeById` 即完成「软删除」，无需手写 UPDATE。

### 7. 空间权限与额度

权限分为三层，逐层收窄：

| 层级 | 位置 | 规则 |
| --- | --- | --- |
| 接口级 | `@AuthCheck` + AOP | 是否管理员 |
| 资源级（空间） | `SpaceService#checkSpaceAuth` | 空间归属者本人或管理员 |
| 资源级（图片） | `PictureService#checkPictureAuth` | 公共图库：本人或管理员；私有空间：仅本人 |

额度控制依赖事务：上传时在同一事务内 `saveOrUpdate(picture)` 并执行 `totalSize = totalSize + ?`、`totalCount = totalCount + 1`（用 `setSql` 做原子自增，避免读改写竞态）；删除时反向扣减。任一环节失败整体回滚。

### 8. 其他工程细节

- `JsonConfig` 将所有 `Long` 序列化为 `String`，规避前端 JavaScript `Number` 精度丢失（雪花 ID 场景必需）。
- `map-underscore-to-camel-case: false`：数据库列名直接采用驼峰命名，与实体字段一一对应，无需下划线转换。
- `spring.servlet.multipart.max-file-size: 10MB` 为容器级上限，业务层（`FilePictureUpload`）再收紧到 2MB 并限定后缀 `jpeg/jpg/png/webp`。
- 批量抓图、文件清理等耗时操作使用 `@Async` / `@Transactional` 分离，避免阻塞主流程。

---

## 接口一览

统一前缀：`http://localhost:8123/api`（`server.port=8123`，`context-path=/api`）。下表路径均为**相对 context-path 的控制器路径**，实际访问需加上 `/api` 前缀，例如 `POST http://localhost:8123/api/picture/upload`。

### UserController · `/user`

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/user/register` | 用户注册 | 公开 |
| POST | `/user/login` | 用户登录，登录态写入 Session | 公开 |
| GET | `/user/get/login` | 获取当前登录用户 | 登录 |
| POST | `/user/logout` | 用户登出 | 登录 |
| POST | `/user/add` | 新增用户（默认密码） | 管理员 |
| GET | `/user/get` | 根据 id 获取用户 | 管理员 |
| GET | `/user/get/vo` | 根据 id 获取用户 VO | 管理员 |
| POST | `/user/update` | 更新用户信息 | 管理员 |
| POST | `/user/delete` | 删除用户（逻辑删除） | 管理员 |
| POST | `/user/list/page/vo` | 分页获取用户 VO 列表 | 管理员 |

### PictureController · `/picture`

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/picture/upload` | 文件上传图片（可传 id 覆盖重传） | 登录 |
| POST | `/picture/upload/url` | 通过 URL 上传图片 | 登录 |
| POST | `/picture/upload/batch` | 批量抓取上传（jsoup 解析搜索结果页） | 管理员 |
| POST | `/picture/delete` | 删除图片（异步清理 COS 文件） | 本人 / 管理员 |
| POST | `/picture/update` | 修改图片（走审核填充） | 管理员 |
| GET | `/picture/get` | 根据 id 获取图片实体 | 管理员 |
| GET | `/picture/get/vo` | 根据 id 获取图片 VO（含上传者信息） | 登录 / 空间校验 |
| POST | `/picture/list/page` | 分页获取图片列表（实体） | 管理员 |
| POST | `/picture/list/page/vo` | 分页获取图片 VO 列表 | 登录 / 空间校验 |
| POST | `/picture/list/page/vo/cache` | 分页获取图片 VO 列表（Caffeine + Redis 缓存） | 公开 |
| POST | `/picture/edit` | 编辑图片（分类 / 标签 / 名称 / 简介） | 本人 / 管理员 |
| POST | `/picture/edit/batch` | 批量编辑（分类 / 标签 / `{序号}` 重命名） | 空间归属者 |
| POST | `/picture/review` | 图片审核（待审核 / 通过 / 拒绝） | 管理员 |
| GET | `/picture/tag_category` | 获取预置标签与分类选项 | 公开 |
| POST | `/picture/search/picture` | 以图搜图（返回相似图片列表） | 公开 |
| POST | `/picture/search/color` | 按主色调搜索空间内图片 | 空间归属者 |
| POST | `/picture/out_painting/create_task` | 创建阿里云 AI 扩图任务 | 图片可见者 |
| GET | `/picture/out_painting/get_task` | 查询 AI 扩图任务状态 | 公开 |

> `POST /picture/list/page/vo` 与 `/cache` 版本均限制 `pageSize ≤ 20` 以限制爬虫；非管理员在公共图库范围内只能查到 `reviewStatus = 1`（通过）的图片。

### SpaceController · `/space`

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/space/add` | 创建空间（每用户限一个） | 登录 |
| POST | `/space/delete` | 删除空间 | 本人 / 管理员 |
| POST | `/space/update` | 修改空间（按级别自动填充配额） | 管理员 |
| POST | `/space/edit` | 编辑空间名称等信息 | 本人 / 管理员 |
| GET | `/space/get` | 根据 id 获取空间实体 | 管理员 |
| GET | `/space/get/vo` | 根据 id 获取空间 VO（含创建者信息） | 公开 |
| POST | `/space/list/page` | 分页获取空间列表（实体） | 管理员 |
| POST | `/space/list/page/vo` | 分页获取空间 VO 列表（`pageSize ≤ 20`） | 公开 |
| GET | `/space/list/level` | 获取全部空间级别选项（value/text/maxCount/maxSize） | 公开 |

### SpaceAnalyzeController · `/space/analyze`

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/space/analyze/usage` | 空间使用情况分析（容量 / 数量 / 使用率） | 空间归属者 / 管理员 |
| POST | `/space/analyze/category` | 图片分类分析（数量 + 总大小） | 空间归属者 / 管理员 |
| POST | `/space/analyze/tag` | 标签使用分析（按次数降序） | 空间归属者 / 管理员 |
| POST | `/space/analyze/size` | 图片大小分析（四档分桶） | 空间归属者 / 管理员 |
| POST | `/space/analyze/user` | 用户行为分析（日 / 周 / 月维度） | 空间归属者 / 管理员 |
| POST | `/space/analyze/topN` | 空间使用排行 TopN | 管理员 |

> 以上 6 个接口的请求体都继承自 `SpaceAnalyzeRequest`，通过 `spaceId` / `queryPublic` / `queryAll` 三选一确定分析范围；`queryAll` 与 `queryPublic` 仅管理员可用。

### FileController · `/file`

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/file/test/upload` | COS 上传测试（上传至 `/test/` 目录） | 管理员 |
| GET | `/file/test/download/` | COS 下载测试（以流的形式返回对象） | 管理员 |

---

## 快速开始

### 环境要求

| 依赖 | 版本 / 说明 |
| --- | --- |
| JDK | 1.8（源码与目标版本均为 1.8） |
| Maven | 3.6+ |
| MySQL | 8.x，本地 `localhost:3306`，库名 `tutu` |
| Redis | 本地 `127.0.0.1:6379`（登录态与缓存依赖，必须启动） |
| 腾讯云 COS | 需自备 bucket，开启图片处理（数据万象）能力 |
| 阿里云百炼 | 可选，仅 AI 扩图功能需要 API Key |

### 1. 初始化数据库

```bash
mysql -u root -p < sql/create_sql.sql
```

脚本会创建数据库 `tutu` 与 `user`、`picture`、`space` 三张表。注意：脚本中的 `ALTER TABLE picture ...` 为**增量语句**，仅在首次初始化时执行一次；重复执行会因列已存在而报错，需要重新建库或手动跳过。

### 2. 配置

`src/main/resources/application.yml` 已入库，包含端口、数据源、Redis、Session、MyBatis-Plus、Knife4j 等公共配置。**私密配置请放在 `src/main/resources/application-local.yml`**（该文件已被 `.gitignore` 忽略，不会提交）：

```yaml
# application-local.yml 示例（请勿提交真实密钥）
cos:
  client:
    host: https://YOUR_BUCKET.cos.ap-beijing.myqcloud.com
    secretId: YOUR_SECRET_ID
    secretKey: YOUR_SECRET_KEY
    region: ap-beijing
    bucket: YOUR_BUCKET

ALiYun:
  apiKey: YOUR_API_KEY
```

数据源口令同样应外置，示例：

```yaml
spring:
  datasource:
    username: root
    password: YOUR_DB_PASSWORD
```

### 3. 启动

```bash
# 方式一：直接运行（推荐）
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package
java -jar target/tutu-backend-0.0.1-SNAPSHOT.jar
```

> 说明：当前 `pom.xml` 中 `spring-boot-maven-plugin` 配置了 `<skip>true</skip>`，因此 `mvn package` 产出的 jar **不是可执行的 fat jar**。若要使用 `java -jar` 方式启动，需先移除该 `skip` 配置（或改为 `false`）再打包；日常开发直接用 `mvn spring-boot:run` 即可。

### 4. 接口文档

启动后访问 Knife4j 文档页：

```
http://localhost:8123/api/doc.html
```

文档分组按包扫描 `com.alioth.tutubackend.controller`，5 个 Controller 的接口会自动归集；页面支持在线调试（需先登录以携带 Session Cookie）。

### 5. 冒烟测试建议顺序

1. `POST /api/user/register` 注册账号（账号 ≥ 4 位，密码 ≥ 8 位）
2. `POST /api/user/login` 登录，浏览器保留 Cookie
3. `GET /api/user/get/login` 验证登录态
4. `POST /api/picture/upload` 上传一张图片（≤ 2MB，`jpeg/jpg/png/webp`）
5. `POST /api/picture/list/page/vo` 查看公共图库列表
6. `POST /api/space/add` 创建空间 → `POST /api/picture/upload`（带 `spaceId`）→ `POST /api/space/analyze/usage` 验证额度统计

---

## 数据库设计简述

库名 `tutu`，字符集 `utf8mb4`，排序规则 `utf8mb4_unicode_ci`。三张核心表，主键均为 `bigint` 雪花 ID（实体侧 `@TableId(type = IdType.ASSIGN_ID)`）。

### `user` — 用户表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键，自增声明 + 雪花 ID 赋值 |
| `userAccount` | varchar(256) | 账号，唯一索引 `uk_userAccount` |
| `userPassword` | varchar(512) | 加盐 MD5 摘要（非明文） |
| `userName` | varchar(256) | 昵称，索引 `idx_userName` |
| `userAvatar` | varchar(1024) | 头像地址 |
| `userProfile` | varchar(512) | 简介 |
| `userRole` | varchar(256) | 角色：`user` / `admin`，默认 `user` |
| `editTime` / `createTime` / `updateTime` | datetime | 时间戳，`updateTime` 由 `ON UPDATE CURRENT_TIMESTAMP` 自动维护 |
| `isDelete` | tinyint | 逻辑删除标记，默认 0 |

### `picture` — 图片表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `url` | varchar(512) | 图片访问地址（COS 域名 + 路径） |
| `thumbnailUrl` | varchar(512) | 缩略图地址（后加列） |
| `name` | varchar(128) | 图片名称，索引 `idx_name` |
| `introduction` | varchar(512) | 简介，索引 `idx_introduction` |
| `category` | varchar(64) | 分类，索引 `idx_category` |
| `tags` | varchar(512) | 标签，JSON 数组字符串，索引 `idx_tags` |
| `picSize` | bigint | 体积（字节） |
| `picWidth` / `picHeight` | int | 宽高（像素） |
| `picScale` | double | 宽高比，保留两位小数 |
| `picFormat` | varchar(32) | 格式（如 `webp`） |
| `picColor` | varchar(16) | 主色调（后加列，用于按颜色搜索） |
| `userId` | bigint | 创建者 ID，索引 `idx_userId` |
| `spaceId` | bigint | 空间 ID（后加列 + `idx_spaceId`）；为空表示公共图库 |
| `reviewStatus` | int | 审核状态：0 待审核 / 1 通过 / 2 拒绝，索引 `idx_reviewStatus` |
| `reviewMessage` | varchar(512) | 审核意见 |
| `reviewerId` | bigint | 审核人 ID |
| `reviewTime` | datetime | 审核时间 |
| `createTime` / `editTime` / `updateTime` | datetime | 时间戳 |
| `isDelete` | tinyint | 逻辑删除标记 |

### `space` — 空间表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `spaceName` | varchar(128) | 空间名称，索引 `idx_spaceName` |
| `spaceLevel` | int | 空间级别：0 普通版 / 1 专业版 / 2 旗舰版，索引 `idx_spaceLevel` |
| `maxSize` | bigint | 图片总容量上限（按级别自动填充） |
| `maxCount` | bigint | 图片数量上限（按级别自动填充） |
| `totalSize` | bigint | 当前已用容量（上传 +、删除 −） |
| `totalCount` | bigint | 当前图片数量 |
| `userId` | bigint | 创建者 ID，索引 `idx_userId` |
| `createTime` / `editTime` / `updateTime` | datetime | 时间戳 |
| `isDelete` | tinyint | 逻辑删除标记 |

### 表关系

```
user 1 ──< space 1 ──< picture
  └────< picture（spaceId 为空 = 公共图库）
```

- 一个用户最多一个空间（`SpaceServiceImpl#addSpace` 中加锁 + 事务校验保证）。
- 一张图片要么属于公共图库（`spaceId = null`），要么属于某个空间；空间删除不影响图片行，但图片行的空间归属需要人工处理（当前实现未做级联）。

---

## 开发过程问题记录

> 以下清单为前后端联调阶段记录的问题与修复情况，**原文保留**：带 `~~删除线~~` 的条目表示已修复，未加删除线者为仍待解决的问题。

### Bug
1. ~~空间命名后仍为`默认空间`~~
---
2. ~~空间类型 `（）` 显示为空~~
   >（后续 `0` 为私有空间，`1` 为团队空间）
---
3. ~~我的空间内无 `创建图片` `批量编辑` 按钮~~
    > 注释 `spaceDetailPage` 中的 `v-if`
---
4. ~~非管理员无法通过 `文件上传` 保存图片~~
---
5. ~~非管理员通过 `URL上传` 保存图片提示空间不存在，初步调试发现请求体中 `id` 为空~~
---
6. ~~在空间内上传一张图片后，`totalSize` 未增加，再次上传图片时提示空间已满~~
---
7. ~~图片详情页无 `编辑` `删除` 按钮~~
   >`PictureDetailPage` 注释 `v-if`
---
8. ~~空间详情页图片组件无 `编辑` `删除` 按钮~~
   >`PictureList` 注释 `v-if`
---
9. `以图搜图` 搜索无结果
---

> 补充说明：第 1~8 条已修复（多为前端页面 `v-if` 注释、空间 `id` 传参缺失、空间额度未累加等前后端契约问题）；第 9 条以图搜图仍待排查，当前实现依赖 360 搜图的页面与接口结构，返回结果为空时接口会直接返回空列表。

---

## 注意事项与安全提示

1. **数据库口令不要入库。** `src/main/resources/application.yml` 目前以明文方式保存 `spring.datasource.password`，这是历史遗留问题。生产环境或开源前应改为环境变量（`${DB_PASSWORD}`）或迁移到 `application-local.yml`。
2. **云存储密钥与 AI Key 必须外置。** 腾讯云 `secretId` / `secretKey` 与阿里云 `apiKey` 统一放在 `application-local.yml`，该文件已被 `.gitignore` 忽略（`.gitignore` 首行即 `application-local.yml`），请勿将其改为强制入库，也不要在 README、Issue、日志中贴出真实值。
3. **README 中的示例一律使用占位符**：`YOUR_SECRET_ID`、`YOUR_SECRET_KEY`、`YOUR_API_KEY`、`YOUR_DB_PASSWORD`、`YOUR_BUCKET`。
4. **建议的密钥管理方式**：本地用 `application-local.yml`（不提交）；CI/CD 用环境变量注入；轮换策略上，一旦密钥曾以明文进入过版本历史，应视为已泄漏并立即在云控制台重置。
5. **文件大小限制是双层叠加的**：容器级 `multipart.max-file-size=10MB`，业务级限制 2MB；URL 上传额外要求 HTTP/HTTPS 协议、`Content-Type` 为图片类型、`Content-Length ≤ 2MB`。
6. **以图搜图依赖第三方站点**，接口结构变化会导致功能失效（对应问题记录第 9 条），生产中应做降级与超时控制（当前单次请求超时 5 秒）。
7. **缓存一致性**：图片增删改后目前**不会主动失效** `listPictureVOByPage` 缓存，最长会有 5~10 分钟的脏读窗口；对一致性要求高的场景需补上删除缓存的逻辑。
8. **逻辑删除**：所有删除操作均为软删除，数据库中仍保留行；若需物理清理，请自行编写归档/清理任务。

---

## 作者与仓库

- 作者：**Alioth**（A-lioth）
- 仓库：<https://github.com/A-lioth/tutu>
- 后端工程：`tutu-backend`（分支 `main`）
- 提交历史：48 次提交，按「用户认证 → 权限 → 对象存储 → 图片 → 缓存 → 空间 → AI → 分析」顺序演进

欢迎 Star、提 Issue 或 PR。若本项目对你的学习或工作有帮助，欢迎交流指正。
