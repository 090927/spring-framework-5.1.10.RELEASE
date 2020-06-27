### AOP
#### 标签解析。
- `IOC 标签解析` `BeanDefinitionParserDelegate # parseCustomElement()`
    - 通过 `AOP` 命名空间uri，找到 AOP 标签处理器。`AopNamespaceHandler` 

##### `<aop:aspectj-autoproxy/>` 标签对应的解析类 `AspectJAutoProxyBeanDefinitionParser`
- 最终将解析后标签注册在 `AnnotationAwareAspectJAutoProxyCreator`

#### 入口,
- Bean 实例化后置处理 
- `AbstractAutowireCapableBeanFactory # initializeBean()`
    - 核心 `AbstractAutoProxyCreator # postProcessAfterInitialization()` 
    
- `ProxyFactory` 代理工厂
    - `JdkDynamicAopProxy` JDK 代理
    - `CglibAopProxy` CGLib 代理
    
- `BeanFactoryUtis` 工具类
    - `beanNamesForTypeIncludingAncestors` 获取容器中，某个类型Bean，BeanName。