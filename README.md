# 复式记账系统 (Double-Entry Accounting System)

一个基于 Spring Boot 的复式记账系统，支持账户管理、交易录入、余额调整和财务报表生成。

## 📋 目录

- [技术栈](#技术栈)
- [功能特性](#功能特性)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [API 接口](#api-接口)
- [使用指南](#使用指南)
- [项目结构](#项目结构)

## 🛠 技术栈

### 后端
- **Spring Boot 3.3.4** - 应用框架
- **Spring Data JPA** - 数据持久化
- **Spring Security** - 安全认证
- **MySQL 8.0+** - 数据库
- **MapStruct 1.5.5** - DTO 映射
- **Lombok 1.18.34** - 代码简化
- **Java 17** - 编程语言

### 前端
- **原生 HTML/CSS/JavaScript** - 前端界面
- **RESTful API** - 前后端通信

## ✨ 功能特性

### 1. 账户管理
- ✅ 创建账户（支持 5 种账户类型：资产、负债、权益、收入、费用）
- ✅ 账户列表查询
- ✅ 账户余额调整（仅限资产类和负债类账户）
- ✅ 账户删除
- ✅ 账户余额可视化显示

### 2. 交易管理
- ✅ 创建双式记账交易（支持多条分录）
- ✅ 交易列表查询（分页）
- ✅ 交易核对（标记为已清算/未清算）
- ✅ 自动更新账户余额
- ✅ 支持商品/服务关联

### 3. 财务报表
- ✅ **资产负债表** - 显示资产、负债、权益
- ✅ **损益表** - 显示收入、费用、净利润
- ✅ **试算平衡表** - 验证借贷平衡

### 4. 商品/货币管理
- ✅ 创建商品/服务/货币
- ✅ 商品列表查询

### 5. 用户认证
- ✅ 用户注册
- ✅ 用户登录
- ✅ 基于 Session 的认证

## 📦 环境要求

- **JDK 17+** - Java 开发工具包
- **Maven 3.6+** - 项目构建工具
- **MySQL 8.0+** - 数据库服务器
- **浏览器** - Chrome、Firefox、Edge 等现代浏览器

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd SystemDesignProj
```

### 2. 创建数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE gnucash_like CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置数据库连接

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gnucash_like?useSSL=false&useUnicode=true&serverTimezone=Asia/Shanghai
    username: root          # 修改为你的 MySQL 用户名
    password: your_password # 修改为你的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 4. 编译项目

```bash
mvn clean compile
```

### 5. 运行项目

**方式一：使用 Maven**

```bash
mvn spring-boot:run
```

**方式二：使用 IDE**

1. 在 IntelliJ IDEA 或 Eclipse 中打开项目
2. 找到 `src/main/java/org/example/Main.java`
3. 运行 `main` 方法

### 6. 访问系统

启动成功后，打开浏览器访问：

- **前端页面**: http://localhost:8080/
- **登录页面**: ·
- **注册页面**: http://localhost:8080/register.html

默认端口为 `8080`，如果端口被占用，可以在 `application.yml` 中修改：

```yaml
server:
  port: 8080  # 修改为你想要的端口
```

## ⚙️ 配置说明

### application.yml 主要配置项

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gnucash_like
    username: root
    password: your_password
  
  jpa:
    hibernate:
      ddl-auto: update  # 自动更新数据库表结构
    show-sql: true      # 显示 SQL 语句（开发环境）

logging:
  level:
    org.example.accounting: debug  # 应用日志级别
```

### 数据库表结构

系统使用 JPA 的 `ddl-auto: update` 自动创建表结构，主要表包括：

- `accounts` - 账户表
- `transactions` - 交易表
- `splits` - 分录表
- `commodities` - 商品/货币表
- `app_users` - 用户表

## 📡 API 接口

### 账户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/accounts` | 获取所有账户列表 |
| POST | `/api/accounts` | 创建账户 |
| PUT | `/api/accounts/{id}/balance` | 调整账户余额 |
| DELETE | `/api/accounts/{id}` | 删除账户 |

### 交易管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/transactions` | 创建交易 |
| GET | `/api/transactions` | 分页查询交易 |
| GET | `/api/transactions/reconcile` | 获取待核对交易 |

### 报表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/reports/balance-sheet` | 资产负债表 |
| GET | `/api/reports/income-statement` | 损益表 |
| GET | `/api/reports/trial-balance` | 试算平衡表 |

### 商品管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/commodities` | 获取所有商品 |
| POST | `/api/commodities` | 创建商品 |

### 用户认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |

## 📖 使用指南

### 1. 首次使用

1. **注册账号**
   - 访问 http://localhost:8080/register.html
   - 填写用户名和密码完成注册

2. **登录系统**
   - 访问 http://localhost:8080/login.html
   - 使用注册的账号登录

3. **创建货币/商品**
   - 在"商品管理"页面创建货币（如：CNY、USD）

4. **创建账户**
   - 在"账户管理"页面创建账户
   - 选择账户类型（资产、负债、权益、收入、费用）
   - 填写账户代码和名称

### 2. 录入交易

1. 切换到"录入交易"标签页
2. 填写交易日期、描述等信息
3. 添加至少两条分录（借贷平衡）：
   - 例如：借：水电支出 1000，贷：中国银行 1000
4. 点击"创建交易"提交

**注意**：系统会自动验证借贷平衡，借贷总额必须相等。

### 3. 调整账户余额

1. 切换到"调整余额"标签页
2. 选择账户（仅显示资产类和负债类账户）
3. 输入调整金额（正数增加，负数减少）
4. 点击"调整余额"提交

**注意**：调整余额会自动创建对应的调整交易。

### 4. 查看报表

- **资产负债表**：显示资产、负债、权益的余额
- **损益表**：显示收入、费用的汇总和净利润
- **试算平衡表**：验证所有账户的借贷是否平衡

## 📁 项目结构

```
SystemDesignProj/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── Main.java                    # 应用启动入口
│   │   │   └── accounting/
│   │   │       ├── config/                  # 配置类
│   │   │       │   ├── SecurityConfig.java  # 安全配置
│   │   │       │   ├── WebMvcConfig.java    # Web MVC 配置
│   │   │       │   └── DataInitializer.java # 数据初始化
│   │   │       ├── controller/              # REST 控制器
│   │   │       │   ├── AccountController.java
│   │   │       │   ├── TransactionController.java
│   │   │       │   ├── ReportController.java
│   │   │       │   ├── CommodityController.java
│   │   │       │   └── AuthController.java
│   │   │       ├── domain/                  # 实体类
│   │   │       │   ├── Account.java
│   │   │       │   ├── Transaction.java
│   │   │       │   ├── Split.java
│   │   │       │   └── ...
│   │   │       ├── repository/              # 数据访问层
│   │   │       ├── service/                 # 业务逻辑层
│   │   │       ├── dto/                     # 数据传输对象
│   │   │       ├── mapper/                  # MapStruct 映射器
│   │   │       └── exception/               # 异常处理
│   │   └── resources/
│   │       ├── application.yml              # 应用配置
│   │       ├── static/                      # 静态资源（前端）
│   │       │   ├── index.html              # 主页面
│   │       │   ├── login.html              # 登录页
│   │       │   └── register.html           # 注册页
│   │       └── db/migration/               # 数据库迁移脚本
│   └── test/                                # 测试代码
├── pom.xml                                  # Maven 配置
└── README.md                                # 项目说明文档
```

## 🔍 常见问题

### Q: 启动时提示数据库连接失败？

**A**: 检查以下几点：
1. MySQL 服务是否已启动
2. `application.yml` 中的数据库连接信息是否正确
3. 数据库 `gnucash_like` 是否已创建
4. 用户名和密码是否正确

### Q: 账户余额显示不正确？

**A**: 
1. 检查交易分录是否正确（借贷是否平衡）
2. 确认账户类型和借贷方向是否正确
3. 查看后端日志中的余额计算日志

### Q: 如何重置数据库？

**A**: 
1. 删除数据库：`DROP DATABASE gnucash_like;`
2. 重新创建数据库：`CREATE DATABASE gnucash_like CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
3. 重启应用，JPA 会自动创建表结构

### Q: 前端页面显示异常？

**A**: 
1. 检查浏览器控制台是否有 JavaScript 错误
2. 确认后端服务是否正常运行
3. 检查 API 请求是否成功（Network 标签页）

## 📝 开发说明

### 运行测试

```bash
mvn test
```

### 打包项目

```bash
mvn clean package
```

打包后的 JAR 文件位于 `target/SystemDesignProj-1.0-SNAPSHOT.jar`

### 运行打包后的 JAR

```bash
java -jar target/SystemDesignProj-1.0-SNAPSHOT.jar
```

## 📄 许可证

本项目仅供学习和研究使用。

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

---

**注意**：本系统为教学项目，生产环境使用前请进行充分的安全评估和性能优化。

