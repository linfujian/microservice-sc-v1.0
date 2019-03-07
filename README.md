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
## module-service-zuul 路由转发服务

* pom 相关依赖
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
			<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
		</dependency>
```
* 启动类 ServiceZuulApplication
```
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableZuulProxy
public class ServiceZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceZuulApplication.class, args);
	}
}
```
@EnableZuulProxy 启动zuul功能

* application.yml 中配置路由 path
```
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

server:
  port: 8769
  
spring:
  application:
    name: service-zuul
    
zuul:
  routes:
    api-a:
      path: /api-a/**
      service-id: service-ribbon #请求路径/api-a/ 路由给 service-ribbon 服务
    api-b:
      path: /api-b/**
      service-id: service-feign #请求路径/api-b/ 路由给 service-feign 服务
```

## module-service-gateway 路由服务

* pom 相关依赖
```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-gateway</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
```
* application.yml 相关配置
```
server:
  port: 8081
  
spring:
  application:
    name: service-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: false
          lower-case-service-id: true
      routes: #将请求路径 /demo/** 路由至 service-hi 服务，filter 将请求 /path 前缀去掉
      - id: service-hi
        uri: lb://SERVICE-HI
        predicates:
        - Path=/demo/**
        filters:
        - StripPrefix=1

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## module-config-server 配置服务
该服务提供了实时读取配置信息源的服务，当配置源的配置信息修改后，该服务会实时获取最新的配置信息，从而使读取该配置服务的客户端获取最新的配置信息。本次 demo 主要读取了 [git repository](https://github.com/linfujian/springCloudConfig) 中的配置信息。

* pom 文件依赖
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
			<artifactId>spring-cloud-config-server</artifactId>
		</dependency>
```
* 启动类加相关注解
```
@SpringBootApplication
@EnableEurekaClient
@EnableConfigServer
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
}
```
* application.yml 中配置相关的配置源信息
```
spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/linfujian/springCloudConfig  #git 仓库的 uri
          search-paths:
          - config #搜索路径
      label: master #指定哪一个分支

server:
  port: 8888
      
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## module-config-client 读取配置信息的客户端，一般来说，将配置信息交给配置服务管理的服务都为客户端

* application.yml 相关依赖
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
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-bus-amqp</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
```

* 相关测试逻辑代码，从配置服务中读取 foo 属性
```
@SpringBootApplication
@RestController
@EnableEurekaClient
@EnableDiscoveryClient
@RefreshScope
public class ConfigClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigClientApplication.class, args);
	}
	@Value("${foo}")
	String foo;
	
	@GetMapping("/hi")
	public String readConfigValue() {
		return foo;
	}
}
```
@RefreshScope 对每次方法的调用都会刷新实例,也就是会读取到最新的属性 foo ，可以看下注解注释。

* bootstrap.properties 启动文件，该文件在 application.yml 加载前加载
```
spring.application.name=config-client
spring.cloud.config.label=master
spring.cloud.config.profile=dev
server.port=8882

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
spring.cloud.config.discovery.enabled=true #可以发现 配置服务 功能开启
spring.cloud.config.discovery.serviceId=config-server #指向配置服务的 name
```
* application.yml
```
##rabbitmq 连接 相关配置，作为消息总线
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

spring.cloud.bus.enabled=true
spring.cloud.bus.trace.enabled=true
management.endpoints.web.exposure.include=bus-refresh #该服务可以接受外部 bus-refresh web 请求，来更新客户端读取的配置信息
```
* 消息总线的使用
当你在postman等插件中发送 http://localhost:8882/actuator/bus-refresh POST 请求时，该请求一方面会让8882实例通过 config-server 读取 git repository 最新配置信息；另一方面会发至消息总线，因为其他实例订阅了总线，所以也会收到更新请求去读取最新的配置信息，见下图：

![消息总线更新](https://github.com/linfujian/microservice-sc-v1.0/blob/master/src/main/java/com/github/linfujian/parent/image/messagebus.png)

```
1.web 前端发送 bus-refresh 请求，该请求达到8882实例后，请求 config-server 读取 git repo 获得最新配置信息
2.bus-refresh 请求同时也会发给消息总线，订阅了该总线的8881和8883实例接收到配置更新请求，也去请求 config-server 读取 git repo 获得最新配置信息
```

## 如下是底层比较常用的业务组件

### aop拦截线程池执行子线程，保证上下文在父子线程间传递

```
每个用户登陆系统后，该用户需要异步执行多个方法，方法内涉及到从securityContext和LogContext中读取用户信息
```
抽象出来便是
```
用户登录的主线程需要异步(多子线程)执行多个方法，方法中需要保持用户的上下文信息
```

解决思路：
```
aop拦截子线程的调用，将上下文通过封装的Runnable对象传递给子线程。因为aop是beanPostProcessor后处理器，只能拦截1)bean中的2)方法，所以逆推得 子线程来自 一个2)线程池的submit执行合适一些，而且这个线程池对象是1)一个bean。
```

实现：

1、bean形式存在的线程池
```
@Configuration
public class AsynExecutor {
	
	@Bean
	@Lazy
	public ExecutorService defaultExecutor() {
		return Executors.newCachedThreadPool();
	}

}
```

2、该线程池提供的线程可以通过注解的方式被业务方法体异步使用
```
@Async("defaultExecutor")
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DefaultAsyncThread {

	
}
```

3、标注如上注解的方法在被线程池中线程执行时添加AOP

```
public class DefaultExecutorSubmitAspect {

	@Around("execution(* java.util.concurrent.Executor.*(..))")
	@SneakyThrows
	public Object intercept(ProceedingJoinPoint joinPoint) {
		
		Object[] args = joinPoint.getArgs();
		for(int i =0; i< args.length; i++) {
			args[i] = processArgs(args[i]);
		}
		
		return joinPoint.proceed();
	}
	
	Object processArgs(Object arg) {
		if(arg instanceof Runnable) {
			Runnable r = (Runnable)arg;
			return new RunnableWeave(r);
		}
		if(arg instanceof Callable<?>) {
			Callable<?> c = (Callable<?>)arg;
			return new CallableWeave<>(c);
		}
		if(arg instanceof Collection<?>) {
			Collection<?> cs = (Collection<?>)arg;
			List<Object> collect = cs.stream().map(this::processArgs).collect(Collectors.toList());
			return collect;
		}
		return arg;
	}
}
```

4、将上下文传递给子线程，从而保证 子线程里可以读取用户登录信息(父线程上下文信息)

```
public class RunnableWeave  implements Runnable {

	private final Runnable r;
	private final Map<String, String> MDCContextMap;
	private final SecurityContext securityContext;
	
	public RunnableWeave(Runnable r) {
		this.r = r;
		this.MDCContextMap = MDC.getCopyOfContextMap();
		securityContext = SecurityContextHolder.getContext();
	}
	
	@Override
	public void run() {
		MDC.setContextMap(MDCContextMap);
		SecurityContextHolder.setContext(securityContext);
		r.run();
		SecurityContextHolder.clearContext();
		MDC.setContextMap(new HashMap<>());
	}

}
```
```
public class CallableWeave<V> implements Callable<V> {

	private final Callable<V> c;
	private final Map<String, String> MDCContextMap;
	private final SecurityContext securityContext;
	
	public CallableWeave(Callable<V> c) {
		this.c = c;
		this.MDCContextMap = MDC.getCopyOfContextMap();
		this.securityContext = SecurityContextHolder.getContext();
	}
	@Override
	public V call() throws Exception {
		MDC.setContextMap(MDCContextMap);
		SecurityContextHolder.setContext(securityContext);
		V v = c.call();
		MDC.setContextMap(new HashMap<>());
		SecurityContextHolder.clearContext();
		return v;
	}

}
```










