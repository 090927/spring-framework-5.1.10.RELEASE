### AOP
#### 入口,
- Bean 实例化后置处理 
- `AbstractAutowireCapableBeanFactory # initializeBean()`
    - 核心 `AbstractAutoProxyCreator # postProcessAfterInitialization()` 
    
- `ProxyFactory` 代理工厂
    - `JdkDynamicAopProxy` JDK 代理
    - `CglibAopProxy` CGLib 代理