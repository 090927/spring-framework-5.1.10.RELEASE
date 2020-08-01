### Spring 事件 
- `ApplicationEvent`
- `AbstractApplicationEventMulticaster` 提供注册、添加、删除 `ApplicationListener` 方法。

##### 事件发布
- `SimpleApplicationEventMulticaster#multicastEvent()` 事件发布。
- `AbstractApplicationContext`#  `publishEvent()` 底层还是调用 `SimpleApplicationEventMulticaster` 进行事件广播。