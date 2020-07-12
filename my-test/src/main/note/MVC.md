### 入口
- `DispatcherServlet #init()` -> `HttpServletBean # init()`

### Controller 和 URL 对应关系
- `AbstractDetectingUrlHandlerMapping #detectHandlers()`

#### WEB 容器初始化
- `ContextLoaderListener # contextInitialized()`

##### 加载容器类型
- `读取-》 ContextLoader.properties 文件`

#### Web 自动装配
- `SpringServletContainerInitializer` 是 -》`ServletContainerInitializer` 实现类