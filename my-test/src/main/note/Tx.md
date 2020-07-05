#### 事务
- `TxNamespaceHandler` 事务命名空间处理类。

#### 事务拦截器 [核心]
- `TransactionInterceptor#invoke(MethodInvocation)`

#### `@EnableTransactionManagement` 开启事务管理器。
- `TransactionManagementConfigurationSelector` 
    - 最终是向容器注册，`InfrastructureAdvisorAutoProxyCreator`