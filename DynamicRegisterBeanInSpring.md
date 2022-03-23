## dynamically register bean with spring” or “dynamically add the bean to spring-context” (at run time)

If the client code needs to register objects which are not managed by Spring container, then we will need to work with an instance of BeanDefinition.

```
 A Spring application can register a BeanDefinition by using the following method of BeanDefinitionRegistry:
 
 void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
```
### GenericBeanDefinition
```
 
 public class MyBean {
  private Date date;
  public void doSomething () {
      System.out.println("from my bean, date: " + date);
  }
  public void setDate (Date date) {
      this.date = date;
  }
}


  public class GenericBeanDefinitionExample {
  public static void main (String[] args) {
      DefaultListableBeanFactory context =
                new DefaultListableBeanFactory();
      GenericBeanDefinition gbd = new GenericBeanDefinition();
      gbd.setBeanClass(MyBean.class);
      MutablePropertyValues mpv = new MutablePropertyValues();
      mpv.add("date", new Date());
      //alternatively we can use:
      // gbd.getPropertyValues().addPropertyValue("date", new Date());
      gbd.setPropertyValues(mpv);
      context.registerBeanDefinition("myBeanName", gbd);
      MyBean bean = context.getBean(MyBean.class);
      bean.doSomething();
  }
 }

```

### BeanDefinitionBuilder
### BeanFactoryPostProcessor
### BeanDefinitionRegistryPostProcessor
