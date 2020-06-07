
### DI
#### 寻找获取Bean 入口
- `AbstractBeanFactory # getBean()`
    - 在Spring 中如果Bean 定义单例，则容器在创建之前先从缓存中查找，以确保容器中只有一个实例化对象。
    - 原型 bean，则容器每次创建一个新的实例对象， 

- `AbstractAutowireCapableBeanFactory # createBean()` 实例化Bean
    - `doCreateBean` 核心入口
    
    - `AbstractAutowireCapableBeanFactory # createBean()` 真正创建Bean 方法。
        - 【实例化阶段】`1、createBeanInstance 生成 Bean 包含的 Java 对象实例`
        - 【依赖注入阶段】`2、populateBean 对Bean 属性的依赖注入进行处理`
        
 
 ##### 属性转换
 - `BeanDefinitionValueResolver#resolveValueIfNecessary`
 ##### 属性依赖注入
 - `AbstractNestablePropertyAccessor#setPropertyValue(AbstractNestablePropertyAccessor.PropertyTokenHolder, PropertyValue)`
  