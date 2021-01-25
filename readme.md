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

