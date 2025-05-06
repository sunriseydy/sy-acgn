# 多语言

## 实现方式

1. 定义 `Interface`： [`Key`](../lib/src/interfaces/KeyInterface.kt)，包含三个属性：`moduleName`(模块枚举), `key`(键),
   `meaning`(含义)
2. 定义各个需要多语言的类型：`EnumKey`，`Message`以及`MessageException`，并指定 `key` 属性的规则
3. 定义各个模块(`ModuleName`)的 `Interface`，例如：`CommonModule`，`AnimeModule`
4. 定义多语言工具对象 [`LocalizationTool`](../lib/src/tools/LocalizationTool.kt)，用于多语言的加载、存储和获取
5. 在服务启动时从多语言定义文件加载多语言：[`Localization.kt`](../server/src/plugins/Localization.kt) 和 [
   `server/resources/data/localization`](../server/resources/data/localization)

## 类型

### 模块

```kotlin
interface CommonModule : Key {
    override val moduleName get() = ModuleName.COMMON
}
```

### 消息 `Message`

#### 键规则 `Message.getMessageKey(enum: Message)`

`message.${LEVEL}.${MODULE_NAME}.${ENUM_NAME}`

level(`MessageLevel`):

* `ERROR`
* `WARNING`
* `INFO`

### 枚举 `EnumKey`

#### 键规则 `EnumKey.getEnumKey(enum: EnumKey)`

`enum.${MODULE_NAME}.${EnumClassName}.${ENUM_NAME}`