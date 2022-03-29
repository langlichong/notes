# HttpSecurity工作原理

1. 使用了什么设计模式

2. authorizeRequests()啥意思

3. and()啥意思，跟xml中的闭合标签一致？，有and()与没有有啥区别？

4. WebSecurity 与 HttpSecurity 区别？

5. antmatcher中多个不同url书写顺序有何影响？

6. ExpressionInterceptUrlRegistry是怎么工作的？

   authorizeRequests() 所创建的 RequestMatcher 是有顺序的？？

7. DefaultSecurityFilterChain ？

8. HttpSecurity 的dsl如何理解？多个authorizeRequests有啥区别？？

   ```java
   @Override
       protected void configure(HttpSecurity http) throws Exception {
           http.servletApi()
           .and().csrf().disable()
     .authorizeRequests().filterSecurityInterceptorOncePerRequest(false)
   .and()           .anonymous().disable().sessionManagement()
   .and() .formLogin().disable()        .authorizeRequests().anyRequest().authenticated()
   .and().headers().frameOptions().sameOrigin().and()
   .authorizeRequests().antMatchers("/error").permitAll().antMatchers("/**").authenticated()
   .and().addFilterBefore(new RestOpenAmSpringFilter(openAmAuthenticationManager, restSsoClient), UsernamePasswordAuthenticationFilter.class)      .exceptionHandling().authenticationEntryPoint(openAmFilterEntryPoint) ;
   }
   ```

9.  配置Demo

   ```
   public static class ApiWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
   
       @Override
       protected void configure(HttpSecurity http) throws Exception {
           http
                   .csrf().disable()
                   .addFilterBefore(new RestOpenAmSpringFilter(openAmAuthenticationManager, restSsoClient), UsernamePasswordAuthenticationFilter.class)
                   .exceptionHandling()
                   .authenticationEntryPoint((request, response, authException) -> {
                       log.debug("Authentication required.");
   
                       response.setHeader("X-Authenticate", properties.getLoginUrl());
                       response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                   }).and()
   
                   .antMatcher("/api/**")
                   .authorizeRequests().anyRequest()
                   .authenticated()
           ;
       }
   }
   ```

   

10. 