### AOP
#### 入口,
- `BeanPostProcessor`
-  bean 实例化、依赖注入完成后。 
    - `AbstractAutowireCapableBeanFactory # initializeBean()`
    
- `ProxyFactory` 代理工厂
    - `JdkDynamicAopProxy` JDK 代理
    - `CglibAopProxy` CGLib 代理