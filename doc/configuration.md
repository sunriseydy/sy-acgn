# APP 配置

## 静态配置

使用KTOR的配置文件存储无法动态配置的配置项，例如数据库信息。

配置文件：

[`server/resources/application.yaml`](../server/resources/application.yaml)

解析值的逻辑：

`YamlConfig.resolveValue`

1. 如果 value 不以 `$` 字符起始，则直接返回 value
2. 否则，截取 `$` 之后的字符串，如果含有 `:` 字符，则先从系统配置或者系统变量获取值，如果没有，就返回 `:` 之后的默认值
3. 否则，尝试将其作为 key 从已有的配置中获取，获取到就直接返回
4. 否则，如果以 `?` 起始，则视为可以为空。从系统配置或者系统变量获取值，如果值为空，根据是否可以为空标识抛出异常或返回 `null`

```
environment.config.property(DatabaseKey.HOST).getString()
```

## 动态配置

配置项：

[`lib/src/common/config`](../lib/src/common/config)

1. ### 从配置文件获取

使用 KTOR 读取配置文件的方式，先从配置文件中加载配置（[
`server/resources/data/config/config.local.yaml`](../server/resources/data/config/config.local.yaml) || [
`server/resources/data/config/config.yaml`](../server/resources/data/config/config.yaml) ）

2. ### 从数据库获取

从数据库中加载配置，代码位置：

[`server/src/plugins/AppConfig.kt`](../server/src/plugins/AppConfig.kt)