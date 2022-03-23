# Spring Bean 生命周期

## Bean 创建阶段

1. Instantiation: This is where everything starts for a bean. Spring instantiates bean objects just like we would manually create a Java object instance.
2. Populating Properties: After instantiating objects, Spring scans the beans that implement Aware interfaces and starts setting relevant properties.
3. Pre-Initialization: Spring’s BeanPostProcessors get into action in this phase. The postProcessBeforeInitialization() methods do their job. Also, @PostConstruct annotated methods run right after them.
4. AfterPropertiesSet: Spring executes the afterPropertiesSet() methods of the beans which implement InitializingBean.
5. Custom Initialization: Spring triggers the initialization methods that we defined in the initMethod attribute of our @Beanannotations.
6. Post-Initialization: Spring’s BeanPostProcessors are in action for the second time. This phase triggers the postProcessAfterInitialization() methods


## Bean 销毁阶段
1. Pre-Destroy: Spring triggers@PreDestroy annotated methods in this phase.
2. Destroy: Spring executes the destroy() methods of DisposableBean implementations.
3. Custom Destruction: We can define custom destruction hooks with the destroyMethod attribute in the @Bean annotation and Spring runs them in the last phase.

## Hooking Into the Bean Lifecycle
### Using Spring’s Interfaces
  1. InitializingBean
    ```
      @Component
      class MySpringBean implements InitializingBean {

       @Override
       public void afterPropertiesSet() {
         //...
       }

      }
    ```
  2. DisposableBean
  ```
    @Component
    class MySpringBean implements DisposableBean {

      @Override
      public void destroy() {
        //...
      }

    }
  ```
### Using JSR-250 Annotations
  ```
  @Component
  class MySpringBean {

      @PostConstruct
      public void postConstruct() {
        //...
      }

      @PreDestroy
      public void preDestroy() {
        //...
      }

  }
  ```
### Using Attributes of the @Bean Annotation
```
  @Configuration
  class MySpringConfiguration {

   @Bean(initMethod = "onInitialize", destroyMethod = "onDestroy")
   public MySpringBean mySpringBean() {
     return new MySpringBean();
   }
  }

  Notes: We should note that if we have a public method named close() or shutdown() in our bean,
  then it is automatically triggered with a destruction callback by default:

  @Component
  class MySpringBean {

    public void close() {
      //...
    }

  }

```
### Using BeanPostProcessor
```

Notes: We should pay attention that Spring's BeanPostProcessors are executed for each bean defined in the spring context.

  class MyBeanPostProcessor implements BeanPostProcessor {

      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {
        //...
        return bean;
      }

      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
        //...
        return bean;
      }

  }
```
### Using Aware Interfaces
```
  @Component
  class MySpringBean implements BeanNameAware, ApplicationContextAware {

      @Override
      public void setBeanName(String name) {
        //...
      }

      @Override
      public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        //...
      }

  }
```
## 使用场景（hook to bean  Lifecycle）
### Acquiring Bean Properties(如运行期获取bean的名称 可以让bean实现 BeanNameAware接口)
### Dynamically Changing Spring Bean Instances（如运行时修改或重新创建bean）
```
   Demo:  Let’s create an IpToLocationService service which is capable of
   dynamically updating IpDatabaseRepository to the latest version on-demand.

  @Service
  class IpToLocationService implements BeanFactoryAware {

    DefaultListableBeanFactory listableBeanFactory;
    IpDatabaseRepository ipDatabaseRepository;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
      listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
      updateIpDatabase();
    }

    public void updateIpDatabase(){
      String updateUrl = "https://download.acme.com/ip-database-latest.mdb";

      AbstractBeanDefinition definition = BeanDefinitionBuilder
          .genericBeanDefinition(IpDatabaseRepository.class)
          .addPropertyValue("file", updateUrl)
          .getBeanDefinition();

      listableBeanFactory
          .registerBeanDefinition("ipDatabaseRepository", definition);

      ipDatabaseRepository = listableBeanFactory
          .getBean(IpDatabaseRepository.class);
    }
  }
```
### Accessing Beans From the Outside of the Spring Context
```
 1、For example, we may want to inject the BeanFactory into a non-Spring class
to be able to access Spring beans or configurations inside that class.
The integration between Spring and the Quartz library is a good example
to show this use case:

ApplicationContextAware 接口来访问 bean 工厂，
并使用 bean 工厂来自动装配 Job bean 中的依赖项，
该 Job bean 最初不是由 Spring 管理的.

class AutowireCapableJobFactory
    extends SpringBeanJobFactory implements ApplicationContextAware {

  private AutowireCapableBeanFactory beanFactory;

  @Override
  public void setApplicationContext(final ApplicationContext context) {
    beanFactory = context.getAutowireCapableBeanFactory();
  }

  @Override
  protected Object createJobInstance(final TriggerFiredBundle bundle)
      throws Exception {
    final Object job = super.createJobInstance(bundle);
    beanFactory.autowireBean(job);
    return job;
  }

}

2、Also, a common Spring - Jersey integration is another clear example of this:

 By marking Jersey’s ResourceConfig as a Spring @Configuration,
 we inject the ApplicationContext and lookup all the beans
 which are annotated by Jersey’s @Path, to easily register them
 on application startup。

@Configuration
class JerseyConfig extends ResourceConfig {

  @Autowired
  private ApplicationContext applicationContext;

  @PostConstruct
  public void registerResources() {
    applicationContext.getBeansWithAnnotation(Path.class).values()
      .forEach(this::register);
  }

}

```
## 执行顺序
```
--- setBeanName executed ---
--- setApplicationContext executed ---
--- postProcessBeforeInitialization executed ---
--- @PostConstruct executed ---
--- afterPropertiesSet executed ---
--- init-method executed ---
--- postProcessAfterInitialization executed ---
...
--- @PreDestroy executed ---
--- destroy executed ---
--- destroy-method executed ---
```
