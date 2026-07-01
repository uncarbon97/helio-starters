本组件基于[mzt-biz-log](https://github.com/mouzt/mzt-biz-log)二次开发

## 相对于上游项目的主要变更

- 拆分为`helium-core-bizlog`与`helium-starter-bizlog`，前者主要放置注解、上下文
- 删除`@EnableLogRecord`注解、`LogRecordConfigureSelector`选择器，统一使用自动装配类`HeliumBizLogAutoConfiguration`
- `ILogRecordService`更名为`ILogRecordDataService`
- `IOperatorGetService#getUser`更名为`getOperator`
- `beans`文件夹更名为`model`
- `LogRecord`POJO更名为`LogRecordModel`，避免与注解重名
- `@DIffLogIgnore`注解订正为`@DiffLogIgnore`
- `@LogRecord`注解属性：
  - 新增`namespace`，默认值为空文本，且支持组合注解使用
  - `type`更名为`bizType`，默认值为空文本
  - `subType`更名为`behavior`，默认值为空文本
  - `bizNo`指定默认值为空文本
  - `success`指定默认值为空文本
- 内嵌`CodeVariableType`至`LogRecordModel`
- 在使用`DiffLogAllFields`注解时，如果字段上没有`@DiffLogField`注解，使用字段上的`@Schema`注解的`description`属性
- 原有`LogRecordContext`是基于ThreadLocal的，重构为支持虚拟线程传递的上下文
