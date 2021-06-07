* blr19c-common 库包含实用程序类、Map/Stream实现、发送企业微信/邮件、ip/ip所在地获取、可操作定时任务、mybatis-plus分离mapper实现等等。
* 如果没有使用数据源,请尝试:
* > @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)

* The blr19c-common library contains utility classes, Map/Stream implementation, sending enterprise
        WeChat, IP/ip address acquisition,Operable timed tasks, mybatis-plus separation implementation, and so on.  
* If not used DataSource  
* @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
> 
        <dependency>
            <groupId>com.blr19c</groupId>
            <artifactId>common</artifactId>
            <version>1.2.2</version>
        </dependency>
