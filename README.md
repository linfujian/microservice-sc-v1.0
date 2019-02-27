# microservice-sc-v1.0
this is a microservice platform based on spring cloud

前言
这是一个基于springcloud的分布式微服务架构，版本v1.0是无业务逻辑的架构。在后续会不断优化框架使其满足一些底层业务需求。

项目结构
parent
├─.settings
├─module-config-client ##6.配置读取客户端，可在1.服务注册中心注册，该服务中的配置信息可以读取5.配置服务中心中的配置信息,并能够通过消息总线bus来保证读取的配置信息为最新
│ 
├─module-config-server ##5.配置服务中心，可在1.服务注册中心注册，可以访问本地或者远程git仓库的配置文件信息
│ 
├─module-eureka-server ##1.eureka服务注册中心
│ 
├─module-service-feign ##3.2.需要在1.注册中心注册,并可以访问2.已注册服务的客户端(feign实现)
│ 
├─module-service-gateway ##4.2.路由转发服务,作用同4.1.比较主流的一种路由分发服务实现
│ 
├─module-service-hi ##2.需要在1.注册中心注册的服务
│ 
├─module-service-ribbon ##3.1.需要在1.注册中心注册,并可以访问2.已注册服务的客户端(ribbon实现)
│ 
├─module-service-zuul ##4.1.路由转发服务，负责将客户的请求根据url路径分发到不同的服务访问客户端(3.1 or 3.2)
