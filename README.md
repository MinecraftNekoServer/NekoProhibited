# NekoProhibited 违禁词屏蔽插件

一个用于Minecraft服务器的违禁词屏蔽插件，可以智能检测并替换玩家聊天中的违禁词。

## 功能特点

- **数据库存储**: 违禁词存储在SQLite数据库中，便于管理
- **智能检测**: 能够检测并替换各种变体违禁词（如"傻 逼"、"傻·逼"等）
- **严格模式**: 采用严格匹配算法，防止绕过检测
- **实时替换**: 将违禁词替换为"杂鱼"而不是简单拦截消息
- **可配置**: 支持通过配置文件自定义设置

## 配置说明

### 数据库配置
插件支持SQLite和MySQL两种数据库，通过`database.type`参数选择：
- `sqlite`: 使用SQLite数据库（文件存储）
- `mysql`: 使用MySQL数据库（需要先创建数据库）

```yaml
database:
  # 数据库类型 (sqlite, mysql)
  type: sqlite
  # SQLite配置
  sqlite:
    # 数据库文件路径（仅在type为sqlite时使用）
    path: "db/prohibited.db"
  # MySQL配置（仅在type为mysql时使用）
  mysql:
    host: "localhost"
    port: 3306
    database: "neko_prohibited"
    username: "root"
    password: "password"
    # 强制关闭SSL（防止SSL相关错误）
    useSSL: false
```

### 违禁词检测设置
```yaml
prohibited:
  # 是否启用违禁词检测
  enabled: true
  # 检测模式 (strict - 严格模式，会检测变体)
  mode: "strict"
```

## 使用方法

### 使用SQLite（默认）
1. 将插件放入服务器的plugins文件夹
2. 启动服务器，插件会自动创建数据库文件
3. 在数据库中添加违禁词记录
4. 玩家聊天时违禁词将被自动替换为"杂鱼"

### 使用MySQL
1. 在MySQL中创建数据库（如neko_prohibited）
2. 修改config.yml中的database.type为"mysql"
3. 配置MySQL连接信息（host, port, database, username, password）
4. 将插件放入服务器的plugins文件夹
5. 启动服务器，插件会自动创建数据表
6. 在数据库中添加违禁词记录
7. 玩家聊天时违禁词将被自动替换为"杂鱼"

### 使用命令管理违禁词
- `/prohibited add <违禁词> [描述]` - 添加违禁词
- `/prohibited remove <违禁词>` - 删除违禁词
- `/prohibited list` - 列出所有违禁词

只有拥有`nekoprohibited.admin`权限的玩家或OP才能使用这些命令。

## 数据库结构

违禁词存储在`prohibited_words`表中，包含以下字段：
- `id`: 主键，自增ID
- `word`: 违禁词内容
- `description`: 违禁词描述（可选）
- `created_at`: 创建时间

## API接口

### 添加违禁词
```java
plugin.getProhibitedWordManager().addProhibitedWord("违禁词", "描述");
```

### 删除违禁词
```java
plugin.getProhibitedWordManager().removeProhibitedWord("违禁词");
```

### 获取所有违禁词
```java
List<String> words = plugin.getProhibitedWordManager().getOriginalWords();
```

## 注意事项

- 插件会自动创建所需的数据库表结构
- 违禁词检测区分大小写
- 插件支持热重载配置文件
- 使用MySQL时需要先手动创建数据库
- 当配置为MySQL时不会创建SQLite数据库文件