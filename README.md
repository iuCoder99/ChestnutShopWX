# 项目经历
## 栗子商城
**担任角色**：后端核心开发（全栈适配）
**技术栈**：Java 17、Spring Boot 3.x、MySQL、MyBatis/MyBatis-Plus、Redis、Shiro、Netty、Caffeine、RocketMQ、Maven
**工具链**：Lombok、MapStruct、Apache Commons、Jackson

**项目描述**：独立耗时2个月从零搭建前后端分离B2C电商单体项目，覆盖商品、购物车、订单、权限、在线客服等核心业务；完成数据库设计、接口开发、性能优化全流程开发

### 核心职责与成果
1. 核心业务开发：基于Spring Boot 3.x完成电商全模块后端开发，独立实现商品管理、购物车、订单管理等核心接口，保障模块稳定联动。
2. 权限安全设计：采用 Shiro + Redis +双JWT Token 实现用户认证与权限管控，完成账号封禁、无感续期功能，提升系统安全性。
3. 数据层优化：使用 MyBatis-Plus 简化单表操作，自定义 ResultMap 实现多表查询；通过 SQL 索引I优化，降低数据库 IO 开销，提升数据查询效率。
4. 多级缓存方案：结合Caffeine 本地缓存+Redis 分布式缓存，针对热点数据做缓存优化；通过布隆过滤器、逻辑过期解决缓存穿透／击穿/雪崩问题。
5. 并发与消息处理：自定义线程池处理异步定时任务；基于Redisson 实现缓存数据一致性；使用Netty搭建在线客服系统；接入RocketMQ 实现订单、消息等业务异步解耦。
6. 效率提升：集成Lombok、MapStruct 等工具简化开发；独立完成数据库表设计、接口文档编写，具备全流程项目开发能力。


本仓库存储项目后端代码
