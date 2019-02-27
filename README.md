# microservice-sc-v1.0
this is a microservice platform based on spring cloud

## 前言
这是一个基于 springcloud 的分布式微服务架构，版本 v1.0是无业务组件逻辑的架构。在后续会不断优化框架使其满足一些底层业务需求。

## 项目结构
如下 module 项目按照序号依次排序讲解，parent 为父模块，主要是做了 spring-boot 和 spring-cloud 依赖引入和版本管理，spring-boot 版本为2.0.3.RELEASE，spring-cloud 版本为 Finchley.RELEASE.
```
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

```

## module-eureka-server 服务注册中心

* pom文件引入eureka-server依赖

```
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

* 启动类 EurekaServerApplication 中添加激活 Eureka server 相关配置的注解

```
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}
}
```

* application.yml 文件中相关配置

```
server:
  port: 8761 #服务启动后监听端口

eureka:
  instance:
    hostname: localhost #服务实例访问ip
  client:
    register-with-eureka: false #本身为服务注册中心，不需要注册
    fetch-registry: false
    service-url: 
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/ #需要注册到该中心的服务需要访问的地址url

spring:
  application:
    name: eureka-server #该注册中心的名称，该名称唯一，可以被其他服务识别
```
