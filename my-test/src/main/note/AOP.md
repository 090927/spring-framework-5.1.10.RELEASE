### AOP
#### 标签解析。
- `标签解析` `DefaultBeanDefinitionDocumentReader # parseBeanDefinitions()`
- `IOC 标签解析` `BeanDefinitionParserDelegate # parseCustomElement()`
    - 通过 `AOP` 命名空间uri，找到 AOP 标签处理器。`AopNamespaceHandler` 

##### `<aop:aspectj-autoproxy/>` 标签对应的解析类 `AspectJAutoProxyBeanDefinitionParser`
- 最终将解析后标签注册在 `AnnotationAwareAspectJAutoProxyCreator`


##### 开启 AOP 权限
- `EnableAspectJAutoProxy`

#### 入口（代理类创建）,
- Bean 实例化后置处理 
- `AbstractAutowireCapableBeanFactory # initializeBean()`
    - 【核心】 `AbstractAutoProxyCreator # postProcessAfterInitialization()` 
    
- `ProxyFactory` 代理工厂
    - `JdkDynamicAopProxy` JDK 代理
    - `CglibAopProxy` CGLib 代理
    
- `BeanFactoryUtis` 工具类
    - `beanNamesForTypeIncludingAncestors` 获取容器中，某个类型Bean，BeanName。
  
##### 核心类
- AbstractAutoProxyCreator `专为含有 Advisor Bean 处理代理创建核心抽象类`
  - AspectJAwareAdvisorAutoProxyCreator `支持 AspectJ 的 Advisor 代理类创建`