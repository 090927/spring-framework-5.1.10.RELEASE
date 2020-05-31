## IOC

### BeanFactory
- `DefaultListableBeanFactory`

### Web IOC 容器
- `DispatcherServlet` -> `HttpServletBean # init()`

### 基于 XML 的IOC 容器初始化

#### 【 定位阶段 】（Spring 定义需要读取那些bean ）BeanDefinition 的 resource 定位
##### 基于 XML 的IOC容器初始化入口 `ClassPathXmlApplicationContext`
- `创建容器` -> `AbstractRefreshableApplicationContext#refreshBeanFactory()`
    -  载入bean定义 -> `AbstractXmlApplicationContext # loadBeanDefinitions()`  

#### 【 加载阶段 】
- 载入配置路径 -> `AbstractXmlApplicationContext # loadBeanDefinitions()`
    - `开始读取配置内容` -> `XmlBeanDefinitionReader #loadBeanDefinitions()` 
    - `doLoadDocument` 准备文档对象。
    - `registerBeanDefinitions`

#### 【 注册阶段 】
- `DefaultBeanDefinitionDocumentReader#registerBeanDefinitions` 
    - `parseBeanDefinitions` 从跟路径解析 DOM 文档对象。
-  解析 `BeanDefinitionHolder`  
    - `BeanDefinitionParserDelegate#parseBeanDefinitionElement(Element)` 
-  将 `BeanDefinitionHolder` 注册 IOC
    -  `BeanDefinitionReaderUtils#registerBeanDefinition`
- 将 `BeanDefinition` 注册 IOC
    - `DefaultListableBeanFactory#registerBeanDefinition`


#### 基于 XML 的IOC容器初始化入口。
- `ClassPathXmlApplicationContext` 构造方法
    - 获取文件路径
    - 创建Spring 容器加载
    - 规定 IOC 容器启动流程 ``AbstractApplication # refresh()``   
- `AbstractBeanDefinitionReader` 获取bean 资源加载器。


### 定位 Bean 扫描
- 在Spring 中管理注解的bean，定义的容器 `AnnotationConfigWebApplication`、`AnnotationConfigApplication`

#### Spring 对注解的处理分两种方式
- 直接将注解 bean 注册容器：
    - 可以在初始化容器是注册；
    - 在容器创建之后手动调用注册方式向容器注册，然后通过手动方式刷新容器使容器对注册的注解 bean 进行处理。
- 通过扫描指定包及其子包下的所有类处理
    - 在初始化注解容器是指定要自动扫描的路径，
    - 容器创建之后想指定路径动态添加 注册bean，则需要手动调用容器扫描的方法手动刷新容器。使容器对所注册的 Bean 进行处理

#### 注解Bean，注册到IOC容器。
- 实现类 `AnnotatedBeanDefinitionReader # doRegisterBean()`
- 具体流程
    - 解析作用域，元数据。
        - `{@link AnnotationScopeMetadataResolver#resolveScopeMetadata(BeanDefinition)}`
    - 处理注解 bean 定义的通用注解。
        - `{@link AnnotationConfigUtils#processCommonDefinitionAnnotations(AnnotatedBeanDefinition)}`
    - 根据注解 bean 定义类中配置的作用域，创建相应的 代理对象 
        - `{@link AnnotationConfigUtils#applyScopedProxyMode(ScopeMetadata, BeanDefinitionHolder, BeanDefinitionRegistry)}`
    - 向IOC 容器注册 Bean 类定义对象 
        - `{@link BeanDefinitionReaderUtils#registerBeanDefinition(BeanDefinitionHolder, BeanDefinitionRegistry)}`

#### 扫描指定包并解析为 BeanDefinition。
- `AnnotatedBeanDefinitionReader # scan()`
- 调用类路径 Bean 定义扫描器入口方法  `ClassPathBeanDefinitionScanner #scan()`