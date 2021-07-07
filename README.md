* blr19c-common 常用便捷工具包
    * Excel解析;雪花ID生成;AES加解密;便捷安全的数学运算
    * 带有缓存和限制的Map(按照使用时间和最少使用也可自定义); 
    * 象形Map(不是Map却比Map支持更多功能更好用); 带有更多快捷功能的Stream
    * 获取传入IP地址; 根据IP地址获取地址信息(可精确到区); WebFlux响应式请求; Zip快捷压缩
    * 实现Mybatis-Plus与BaseMapper分离,无需任何配置仅使用实体类即可完成全部操作(并且解决了Mybatis-Plus无法批量插入的问题)
    * 可操作的分布式定时任务(基于SpringBoot-Scheduled实现在运行时可以更改,定时任务触发时间;执行任务;以及添加分布式锁)
    * 内存估算器
    * 快速建立企业微信服务程序(实现文件;视频;音频;Markdown;图片;卡片等消息的快捷发送)
    * 快捷搭建邮件服务并支持使用多邮件服务器,例如下面简单配置即可完成不同服务器邮件分发
        ``` 
            spring:
              multi-server-mail:
                enable: true
                mail:
                  - host: smtp.163.com
                    port: 25
                    username: xxx@163.com
                    password: xxxxx
                    default-encoding: UTF-8
                    #通常要与username相同
                    componentName: xxxxx
                    #指定后缀使用此服务发送
                    suffix-matching:
                      #因为有@所以必须添加单引号
                      - '@163.com'
                      - '@qq.com'
                    primary: true
                  - host: smtp.163.com
                    port: 25
                    username: xxx@163.com
                    password: xxxxxx
                    default-encoding: UTF-8
                    #通常要与username相同
                    componentName: xxxxx
                    #指定后缀使用此服务发送
                    suffix-matching:
                      #因为有@所以必须添加单引号
                      - '@gmail.com'
                      - '@126.com'
                    primary: false        
* 如果没有使用数据源,请尝试:
* > @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
* 看了半天还不知道如何使用?
> 
        <dependency>
            <groupId>com.blr19c</groupId>
            <artifactId>common</artifactId>
            <version>1.3.8</version>
        </dependency>
