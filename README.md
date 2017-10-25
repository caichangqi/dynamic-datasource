# 动态数据源，读写分离
## spring boot集成
maven依赖
```xml
        <dependency>
            <groupId>com.yongle.dshelper</groupId>
            <artifactId>dshelper-spring-boot-starter</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```
spring boot配置文件(application.yml)
```yaml
ds-helper:
  read-data-source-poll-pattern: 1
  write:
    url: jdbc:mysql://ip:port/crm_test?useUnicode=true&characterEncoding=UTF-8&useSSL=false
    username: username
    password: password
    initial-size: 10
    max-active: 50
  read:
    - url: jdbc:mysql://ip:port/crm_dev?useUnicode=true&characterEncoding=UTF-8&useSSL=false
      username: username
      password: password
      initial-size: 10
      max-active: 50
    - url: jdbc:mysql://ip:port/crm_dev?useUnicode=true&characterEncoding=UTF-8&useSSL=false
      username: username
      password: password
      initial-size: 10
      max-active: 50
```
`read-data-source-poll-pattern`：读数据源轮询模式，1循环，其他随机

`write`：写数据源

`read`：读数据源，支持配置多个，可以一写多读

连接池的具体属性请参考`DruidDataSource`配置

**注：集成spring boot的方式必须使用Druid数据源**

## spring集成
待续