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

* 启动服务注册中心eureka server,访问 http://localhost:8761/eureka/

## module-service-hi 一个可以注册到服务注册中心的服务

* pom 文件引入相关依赖

```
	<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
        	<groupId>org.springframework.boot</groupId>
        	<artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
        	<groupId>org.springframework.boot</groupId>
        	<artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
        	<groupId>org.springframework.cloud</groupId>
        	<artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
	<dependency>
        	<groupId>org.springframework.cloud</groupId>
        	<artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
```
```
** eureka-client - 可注册的客户端依赖 **
** starter-web - web 服务 **
** hystrix - 熔断器相关组件 **
** actuator - 监控相关组件 **
```
* 定义一个 controller 类添加服务业务逻辑
```
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@RestController
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
public class ServiceHiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceHiApplication.class, args);
	}
	
	@Value("${server.port}")
	String port;
	
	@RequestMapping("/hi")
	@HystrixCommand(fallbackMethod="hiError")
	public String sayHi(@RequestParam(value="name", defaultValue="fujian") String name) {
		return "hi, " + name + ", i am from port:" + port;
	}
	
	public String hiError(String name) {
		return "hi, " + name + ", error happens";
	}
}

```
```
@EnableEurekaClient - 表明这是一个可注册的客户端
@EnableDiscoveryClient - 作用同上
@EnableHystrix/@EnableCircuitBreaker - 使其具有熔断器功能
@EnableHystrixDashboard - 使其具有熔断器dashboard功能
@RestController - 这是一个cotroller
```
* application.yml 相关配置信息
```
server:
  port: 8762
  
spring:
  application:
    name: service-hi

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/ #服务注册到该地址的服务注册中心

management: #该设置允许该服务可以被http访问，如查看服务 actuator 等信息
  endpoints:
    web:
      exposure:
        include: "*"
      cors:
        allowed-origins: "*"
        allowed-methods: "*"
```
* 启动该服务，访问 http://localhost:8762/hi?name=fujian 可以直接访问该服务，在 http://localhost:8761/ 中可以看到该服务已注册到注册中心

## module-service-ribbon 访问服务的客户端

* pom 文件中相关依赖

```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
		</dependency>
```
* 访问服务的逻辑
```
@Service
public class HiService {

	@Autowired
	RestTemplate restTemplate;
	
	@HystrixCommand(fallbackMethod="hiError")
	public String sayhi(String name) {
		return restTemplate.getForObject("http://SERVICE-HI/hi?name="+name, String.class);
	}
	
	public String hiError(String name) {
		return "hi," + name + ",sorry,error!";
	}
}

```
```
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableHystrix
public class ServiceRibbonApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRibbonApplication.class, args);
	}
	
	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
```
首先看方法上的 @HystrixCommand 注释，该注释表明调用该方法服务不可用时交由 fallbackMethod 指向的方法处理。然后 sayhi 方法内使用 restTemplate 去访问 service-hi 服务，由于该服务已在注册中心注册，所以访问 SERVICE-HI 唯一标识便可访问到已注册的服务。restTemplate 在启动类中通过@Bean 实例化，通过 @LoadBalanced 使其具有负载均衡功能，即当通过该模块访问一个服务集群时会均衡分发请求。

* application.yml 配置
```
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
      
server:
  port: 8764

spring:
  application:
    name: service-ribbon

```
## module-service-feign 访问服务的客户端

* pom文件引入相关依赖
```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-openfeign</artifactId>
		</dependency>
```

* 业务逻辑
```
@FeignClient(value="service-hi", fallback=HiServiceHystrix.class)
public interface HiSerivceInterface {

	@GetMapping("/hi")
	public String sayHi(@RequestParam(value="name") String name);
}
```
```
@Component
public class HiServiceHystrix implements HiSerivceInterface {

	@Override
	public String sayHi(String name) {
		return "sorry," + name + ", it has a error";
	}

}
```
@FeignClient的 value 将 /hi 访问转递给 service-hi 服务，假如访问服务失败则请求交由 HiServiceHystrix 处理，可以看到 HiServiceHystrix 实现了 HiSerivceInterface 接口。

* application.yml 配置文件
```
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
      
server:
  port: 8765
  
spring:
  application:
    name: service-feign
    
feign:
  hystrix:
    enabled: true
```






