# 
需要添加腾讯的maven仓库到setting.xml中

# 添加私服配置
找到 Maven 所使用的配置文件，一般在 ~/.m2/settings.xml 中，在 settings.xml 中加入如下配置：
```
<profiles>
     <profile>
       <id>nexus</id>
       <repositories>
           <repository>
               <id>central</id>
               <url>http://repo1.maven.org/maven2</url>
               <releases>
                   <enabled>true</enabled>
               </releases>
               <snapshots>
                   <enabled>true</enabled>
               </snapshots>
           </repository>
       </repositories>
       <pluginRepositories>
           <pluginRepository>
               <id>central</id>
               <url>http://repo1.maven.org/maven2</url>
               <releases>
                   <enabled>true</enabled>
               </releases>
               <snapshots>
                   <enabled>true</enabled>
               </snapshots>
           </pluginRepository>
       </pluginRepositories>
   </profile>
   <profile>
       <id>qcloud-repo</id>
       <repositories>
           <repository>
               <id>qcloud-central</id>
               <name>qcloud mirror central</name>
               <url>http://mirrors.cloud.tencent.com/nexus/repository/maven-public/</url>
               <snapshots>
                   <enabled>true</enabled>
               </snapshots>
               <releases>
                   <enabled>true</enabled>
               </releases>
           </repository>
           </repositories>
       <pluginRepositories>
           <pluginRepository>
               <id>qcloud-plugin-central</id>
               <url>http://mirrors.cloud.tencent.com/nexus/repository/maven-public/</url>
               <snapshots>
                   <enabled>true</enabled>
               </snapshots>
               <releases>
                   <enabled>true</enabled>
               </releases>
           </pluginRepository>
       </pluginRepositories>
   </profile>
 </profiles>

 <activeProfiles>
   <activeProfile>nexus</activeProfile>
   <activeProfile>qcloud-repo</activeProfile>
</activeProfiles>

```
# 验证配置是否成功
在命令行执行如下命令
```
mvn help:effective-settings
```
- 查看执行结果，没有错误表明 setting.xml 格式正确。
- profiles 中包含 qcloud-repo ，则表明 qcloud-repo 私服已经加入到
- profiles 中；activeProfiles 中包含 qcloud-repo，则表明 qcloud-repo 私服已经激活成功。可以通过mvn help:effective-settings | grep 'qcloud-repo'命令检查。

# 添加依赖
```
<dependency>
  <groupId>com.seelyn</groupId>
  <artifactId>tdmq-spring-boot-starter</artifactId>
  <version>1.0.4</version>
</dependency>
```
# 添加TDMQ连接配置
```
tdmq.service-url=pulsar://host:6000/
tdmq.listener-name=custom:XXX
tdmq.authentication-token=xxx
tdmq.io-threads=10
tdmq.listener-threads=10
tdmq.enable_tcp_no_delay=false
tdmq.keep_alive_interval_sec=20
tdmq.connection_timeout_sec=10
tdmq.operation_timeout_sec=15
tdmq.starting_backoff_interval_ms=100
tdmq.max_backoff_interval_sec=10
tdmq.batch-threads=-1 # 批量接收消息的线程数，-1表示不限制，为订阅数量
```
# 快速开始
## 单条消息接收
```
@Component
@TdmqHandler(topics = {@TdmqTopic(topic = "topic")})
public class TestHandler implements TdmqListener<String> {

    @Override
    public void received(Consumer<String> consumer, Message<String> message) throws MessageRedeliverException {

    }
}
```
@TdmqTopic(topic = "topic", tags = "test") 可以使用Spring的EL表达式，
例如@TdmqTopic(topic = "${topic}", tags = "${test}")
@TdmqHandler是必须的，注解到实现了TdmqListener接口的类上
TdmqListener<T>,T可以是任意类型，除了集合类型外

## 多条消息接收
```
@Component
@TdmqHandler(topics = {@TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-12.topic}")})
public class DemoHandler12 implements TdmqBatchListener<String> {

    @Override
    public void received(Consumer<String> consumer, Messages<String> messages) throws MessageRedeliverException {
        System.out.println("DemoHandler12:" + messages.size());
    }
}
```
messages 为多条消息的迭代器，多线程来处理需要配置文件
```
tdmq.batch-threads=-1 # 实现TdmqBatchListener的数量，配置数量也可以比实现接口数量多或少。都可以
```

## 接收List消息
```
@Component
@TdmqHandler(topics = {@TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-11.topic}")})
public class DemoHandler11 extends ListBaseBytesListener<String> {
    @Override
    protected void receive(Consumer<byte[]> consumer, Message<byte[]> message, List<String> data) throws MessageRedeliverException {
        System.out.println("DemoHandler11:" + data.size());
    }
}
```
类继承ListBaseBytesListener，可以接收List集合消息

## 接收bytes编码传输的对象消息
```
@Component
@TdmqHandler(topics = {@TdmqTopic(topic = "${eqxiu.scs.mns.topics.content-todo-11.topic}")})
public class DemoHandler11 extends ObjectBaseBytesListener<String> {

    @Override
    protected void receive(Consumer<byte[]> consumer, Message<byte[]> message, String data) throws MessageRedeliverException {
        System.out.println("DemoHandler11:" + data);
    }
}
```
类继承ObjectBaseBytesListener，可以接收除集合以外的任意消息

## 发送消息
```
// 发送字符串消息
@Autoware
StringTdmqTemplate stringTdmqTemplate;

// 发送List对象消息
@Autoware
ListBaseBytesTemplate listBaseBytesTemplate;

// 发送除集合以外的对象消息
@Autoware
ObjectBaseBytesTemplate objectBaseBytesTemplate;

// 发送任意消息
@Autoware
TdmqTemplate tdmqTemplate;
```
