那 Spring Boot 有何魔法？
自动配置、起步依赖、Actuator、命令行界面(CLI) 是 Spring Boot 最重要的 4 大核心特性，
其中 CLI 是 Spring Boot 的可选特性，虽然它功能强大，但也引入了一套不太常规的开发模型，因而这个系列的文章仅关注其它 3 种特性。

一、抛砖引玉：探索Spring IoC容器
如果有看过 SpringApplication.run()方法的源码，Spring Boot 冗长无比的启动流程一定会让你抓狂，透过现象看本质，
SpringApplication 只是将一个典型的 Spring 应用的启动流程进行了扩展，因此，透彻理解 Spring 容器是打开 Spring Boot 大门的一把钥匙。

1.1、Spring IoC容器
可以把 Spring IoC 容器比作一间餐馆，当你来到餐馆，通常会直接招呼服务员：点菜！至于菜的原料是什么？如何用原料把菜做出来？可能你根本就不关心。
IoC 容器也是一样，你只需要告诉它需要某个bean，它就把对应的实例（instance）扔给你，至于这个bean是否依赖其他组件，怎样完成它的初始化，根本就不需要你关心。

作为餐馆，想要做出菜肴，得知道菜的原料和菜谱，同样地，IoC 容器想要管理各个业务对象以及它们之间的依赖关系，需要通过某种途径来记录和管理这些信息。
BeanDefinition对象就承担了这个责任：容器中的每一个 bean 都会有一个对应的 BeanDefinition 实例，该实例负责保存bean对象的所有必要信息，
包括 bean 对象的 class 类型、是否是抽象类、构造方法和参数、其它属性等等。当客户端向容器请求相应对象时，容器就会通过这些信息为客户端返回一个完整可用的 bean 实例。

原材料已经准备好（把 BeanDefinition 看着原料），开始做菜吧，等等，你还需要一份菜谱， BeanDefinitionRegistry和 BeanFactory就是这份菜谱，
BeanDefinitionRegistry 抽象出 bean 的注册逻辑，而 BeanFactory 则抽象出了 bean 的管理逻辑，而各个 BeanFactory 的实现类就具体承担了 bean 的注册以及管理工作。

DefaultListableBeanFactory作为一个比较通用的 BeanFactory 实现，它同时也实现了 BeanDefinitionRegistry 接口，
因此它就承担了 Bean 的注册管理工作。从图中也可以看出，BeanFactory 接口中主要包含 getBean、containBean、getType、getAliases 等管理 bean 的方法，
而 BeanDefinitionRegistry 接口则包含 registerBeanDefinition、removeBeanDefinition、getBeanDefinition 等注册管理 BeanDefinition 的方法.

模拟 BeanFactory 底层是如何工作的:
```
  // 默认容器实现
  DefaultListableBeanFactory beanRegistry = new DefaultListableBeanFactory();
  // 根据业务对象构造相应的
  BeanDefinitionAbstractBeanDefinition definition = new RootBeanDefinition(Business.class,true);
  // 将bean定义注册到容器中
  beanRegistry.registerBeanDefinition("beanName",definition);
  // 如果有多个bean，还可以指定各个bean之间的依赖关系
  // 然后可以从容器中获取这个bean的实例
  // 注意：这里的beanRegistry其实实现了BeanFactory接口，所以可以强转，
  // 单纯的BeanDefinitionRegistry是无法强制转换到BeanFactory类型的
  BeanFactory container = (BeanFactory)beanRegistry;Business business = (Business)container.getBean("beanName");
```
这段代码仅为了说明 BeanFactory 底层的大致工作流程，实际情况会更加复杂，
比如 bean 之间的依赖关系可能定义在外部配置文件(XML/Properties)中、也可能是注解方式。Spring IoC 容器的整个工作流程大致可以分为两个阶段.

①、容器启动阶段
容器启动时，会通过某种途径加载 ConfigurationMetaData。除了代码方式比较直接外，在大部分情况下，容器需要依赖某些工具类，
比如： BeanDefinitionReader，BeanDefinitionReader 会对加载的 ConfigurationMetaData进行解析和分析，并将分析后的信息组装为相应的 BeanDefinition，
最后把这些保存了 bean 定义的 BeanDefinition，注册到相应的 BeanDefinitionRegistry，这样容器的启动工作就完成了。这个阶段主要完成一些准备性工作，
更侧重于 bean 对象管理信息的收集，当然一些验证性或者辅助性的工作也在这一阶段完成。

来看一个简单的例子吧，过往，所有的 bean 都定义在 XML 配置文件中，下面的代码将模拟 BeanFactory 如何从配置文件中加载 bean 的定义以及依赖关系:
```
  // 通常为BeanDefinitionRegistry的实现类，这里以DeFaultListabeBeanFactory为例
  BeanDefinitionRegistry beanRegistry = new DefaultListableBeanFactory();
  // XmlBeanDefinitionReader实现了BeanDefinitionReader接口，用于解析XML文件
  XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReaderImpl(beanRegistry);
  // 加载配置文件beanDefinitionReader.loadBeanDefinitions("classpath:spring-bean.xml");
  // 从容器中获取bean实例
  BeanFactory container = (BeanFactory)beanRegistry;
  Business business = (Business)container.getBean("beanName");
```

②、Bean的实例化阶段
经过第一阶段，所有 bean 定义都通过 BeanDefinition 的方式注册到 BeanDefinitionRegistry 中，当某个请求通过容器的 getBean 方法请求某个对象，
或者因为依赖关系容器需要隐式的调用 getBean 时，就会触发第二阶段的活动：容器会首先检查所请求的对象之前是否已经实例化完成。如果没有，
则会根据注册的 BeanDefinition 所提供的信息实例化被请求对象，并为其注入依赖。当该对象装配完毕后，容器会立即将其返回给请求方法使用。

BeanFactory 只是 Spring IoC 容器的一种实现，如果没有特殊指定，它采用采用延迟初始化策略：只有当访问容器中的某个对象时，才
对该对象进行初始化和依赖注入操作。而在实际场景下，我们更多的使用另外一种类型的容器： ApplicationContext，它构建在 BeanFactory 之上，
属于更高级的容器，除了具有 BeanFactory 的所有能力之外，还提供对事件监听机制以及国际化的支持等。它管理的 bean，在容器启动时全部完成初始化和依赖注入操作。

1.2、Spring容器扩展机制
IoC 容器负责管理容器中所有bean的生命周期，而在 bean 生命周期的不同阶段，Spring 提供了不同的扩展点来改变 bean 的命运。
在容器的启动阶段， BeanFactoryPostProcessor允许我们在容器实例化相应对象之前，对注册到容器的 BeanDefinition 所保存的信息做一些额外的操作，
比如修改 bean 定义的某些属性或者增加其他信息等。

如果要自定义扩展类，通常需要实现 org.springframework.beans.factory.config.BeanFactoryPostProcessor接口，与此同时，
因为容器中可能有多个BeanFactoryPostProcessor，可能还需要实现 org.springframework.core.Ordered接口，以保证BeanFactoryPostProcessor按照顺序执行。
Spring提供了为数不多的BeanFactoryPostProcessor实现，我们以 PropertyPlaceholderConfigurer来说明其大致的工作流程。

在Spring项目的XML配置文件中，经常可以看到许多配置项的值使用占位符，而将占位符所代表的值单独配置到独立的properties文件，
这样可以将散落在不同XML文件中的配置集中管理，而且也方便运维根据不同的环境进行配置不同的值。这个非常实用的功能就是由PropertyPlaceholderConfigurer负责实现的。

根据前文，当BeanFactory在第一阶段加载完所有配置信息时，BeanFactory中保存的对象的属性还是以占位符方式存在的，比如 ${jdbc.mysql.url}。
当PropertyPlaceholderConfigurer作为BeanFactoryPostProcessor被应用时，它会使用properties配置文件中的值来替换相应的BeanDefinition中占位符所表示的属性值。
当需要实例化bean时，bean定义中的属性值就已经被替换成我们配置的值。当然其实现比上面描述的要复杂一些，这里仅说明其大致工作原理，更详细的实现可以参考其源码。

与之相似的，还有 BeanPostProcessor，其存在于对象实例化阶段。跟BeanFactoryPostProcessor类似，它会处理容器内所有符合条件并且已经实例化后的对象。
简单的对比，BeanFactoryPostProcessor处理bean的定义，而BeanPostProcessor则处理bean完成实例化后的对象。BeanPostProcessor定义了两个接口.
![](assets/markdown-img-paste-2021113011445967.png)

postProcessBeforeInitialization()方法与 postProcessAfterInitialization()分别对应图中前置处理和后置处理两个步骤将执行的方法。
这两个方法中都传入了bean对象实例的引用，为扩展容器的对象实例化过程提供了很大便利，在这儿几乎可以对传入的实例执行任何操作。
注解、AOP等功能的实现均大量使用了 BeanPostProcessor，比如有一个自定义注解，你完全可以实现BeanPostProcessor的接口，
在其中判断bean对象的脑袋上是否有该注解，如果有，你可以对这个bean实例执行任何操作.

JavaConfig 与 常用Annotation:
@ComponentScan:
  ```
    @ComponentScan注解对应XML配置形式中的 <context:component-scan>元素，表示启用组件扫描，Spring会自动扫描所有通过注解配置的bean，
    然后将其注册到IOC容器中。我们可以通过 basePackages等属性来指定 @ComponentScan自动扫描的范围，如果不指定，
    默认从声明 @ComponentScan所在类的 package进行扫描。正因为如此，SpringBoot的启动类都默认在 src/main/java下.
  ```
@Import: 导入配置类，类似xml配置时期的<import xxx />
```
  @Configuration public class MoonBookConfiguration {
     @Bean
     public BookService bookService() {
         return new BookServiceImpl();
     }
   }

   现在有另外一个配置类，比如： MoonUserConfiguration，这个配置类中有一个bean依赖于 MoonBookConfiguration中的bookService，
   如何将这两个bean组合在一起？借助 @Import即可：
      @Configuration// 可以同时导入多个配置类，比如：@Import({A.class,B.class})
      @Import(MoonBookConfiguration.class)
      public class MoonUserConfiguration {
        @Bean
        public UserService userService(BookService bookService) {
            return new BookServiceImpl(bookService);
        }
      }

  需要注意的是，在4.2之前， @Import注解只支持导入配置类，但是在4.2之后，它支持导入普通类，并将这个类作为一个bean的定义注册到IOC容器中
```
@Conditional: 可以说是spring boot 自动配置的基础
```
  在Spring里可以很方便的编写你自己的条件类，所要做的就是实现 Condition接口，并覆盖它的 matches()方法。
  举个例子，下面的简单条件类表示只有在 Classpath里存在 JdbcTemplate类时才生效：
  public class JdbcTemplateCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        try {
        conditionContext.getClassLoader().loadClass("org.springframework.jdbc.core.JdbcTemplate");
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
  }

  当你用Java来声明bean的时候，可以使用这个自定义条件类：
  @Conditional(JdbcTemplateCondition.class)
  @Service
  public MyService service() {}

  这个例子中只有当 JdbcTemplateCondition类的条件成立时才会创建MyService这个bean。也就是说MyService这bean的创建条件是 classpath里面包含 JdbcTemplate，否则这个bean的声明就会被忽略掉

```
@ConfigurationProperties与@EnableConfigurationProperties ：
```
 当某些属性的值需要配置的时候，我们一般会在 application.properties文件中新建配置项，然后在bean中使用 @Value注解来获取配置的值.
 @Value("jdbc.mysql.url")
 public String url;

使用 @Value注解注入的属性通常都比较简单，如果同一个配置在多个地方使用，也存在不方便维护的问题（考虑下，如果有几十个地方在使用某个配置，而现在你想改下名字，你改怎么做？）。
对于更为复杂的配置，Spring Boot提供了更优雅的实现方式，那就是 @ConfigurationProperties注解。我们可以通过下面的方式来改写上面的代码:
  //  还可以通过@PropertySource("classpath:jdbc.properties")来指定配置文件
  // 前缀=jdbc.mysql，会在配置文件中寻找jdbc.mysql.*的配置项
  @ConfigurationProperties("jdbc.mysql")
  @Component
  pulic class JdbcConfig {
    public String url;
    public String username;
    public String password;
  }
    @Configuration
    public class HikariDataSourceConfiguration {

    @AutoWired
    public JdbcConfig config;

    @Bean
    public HikariDataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.url);
        hikariConfig.setUsername(config.username);
        hikariConfig.setPassword(config.password);
        // 省略部分代码
        return new HikariDataSource(hikariConfig);
    }}

  @EnableConfigurationProperties注解表示对 @ConfigurationProperties的内嵌支持，默认会将对应Properties Class作为bean注入的IOC容器中，即在相应的Properties类上不用加 @Component注解。

```

SpringFactoriesLoader(削铁如泥):
```
JVM提供了3种类加载器： BootstrapClassLoader、 ExtClassLoader、 AppClassLoader分别加载Java核心类库、扩展类库以及应用的类路径( CLASSPATH)下的类库。
JVM通过双亲委派模型进行类的加载，我们也可以通过继承 java.lang.classloader实现自己的类加载器。

但双亲委派模型并不能解决所有的类加载器问题，比如，Java 提供了很多服务提供者接口( ServiceProviderInterface，SPI)，允许第三方为这些接口提供实现。
常见的 SPI 有 JDBC、JNDI、JAXP 等，这些SPI的接口由核心类库提供，却由第三方实现，这样就存在一个问题：SPI 的接口是 Java 核心库的一部分，
是由BootstrapClassLoader加载的；SPI实现的Java类一般是由AppClassLoader来加载的。BootstrapClassLoader是无法找到 SPI 的实现类的，
因为它只加载Java的核心库。它也不能代理给AppClassLoader，因为它是最顶层的类加载器。也就是说，双亲委派模型并不能解决这个问题。
```
SPI 与  ServiceLoader：
Java提供了很多服务提供者接口（Service Provide Interface，SPI），允许第三方为这些接口提供实现。常见的有JDBC、JNDI、JAXP、JBI等。
这些SPI的接口由Java核心库提供，而这些SPI的实现则是作为Java的依赖jar包被包含到ClALLPATH里。SPI接口中的代码经常需要加载第三方提供的具体实现。
有趣的是，SPI接口是Java核心库，它是由BootstrapClassLoader来加载。SPI的实现类则是由AppClassLoader来加载的。
依照双亲委派模型和可见性，BootstrapClassLoader是无法获取到AppClassLoader加载的类，
```

类加载器除了加载class外，还有一个非常重要功能，就是加载资源，它可以从jar包中读取任何资源文件，比如， ClassLoader.getResources(Stringname)方法就是用于读取jar包中的资源文件。

从 CLASSPATH下的每个Jar包中搜寻所有 META-INF/spring.factories配置文件，然后将解析properties文件，找到指定名称的配置后返回。
需要注意的是，其实这里不仅仅是会去ClassPath路径下查找，会扫描所有路径下的Jar包，只不过这个文件只会在Classpath下的jar包中。
执行 loadFactoryNames(EnableAutoConfiguration.class,classLoader)后，得到对应的一组 @Configuration类，我们就可以通过反射实例化这些类然后注入到IOC容器中，
最后容器里就有了一系列标注了 @Configuration的JavaConfig形式的配置类。
这就是 SpringFactoriesLoader，它本质上属于Spring框架私有的一种扩展方案，类似于SPI，Spring Boot在Spring基础上的很多核心功能都是基于此。

Spring事件监听：
    Java提供了实现事件监听机制的两个基础类：自定义事件类型扩展自 java.util.EventObject、事件的监听器扩展自 java.util.EventListener。
    Spring的ApplicationContext容器内部中的所有事件类型均继承自 org.springframework.context.AppliationEvent，
    容器中的所有监听器都实现 org.springframework.context.ApplicationListener接口，并且以bean的形式注册在容器中。
    一旦在容器内发布ApplicationEvent及其子类型的事件，注册到容器的ApplicationListener就会对这些事件进行处理。
    ApplicationContext接口继承了ApplicationEventPublisher接口，该接口提供了 void publishEvent(ApplicationEventevent)方法定义，不难看出，ApplicationContext容器担当的就是事件发布者的角色。
    如果我们业务需要在容器内部发布事件，只需要为其注入（@AutoWired）ApplicationEventPublisher后调用其publishEvent，或者直接使用ApplicationContext的内部方法publishEvent也可以。
