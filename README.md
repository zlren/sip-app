### 融合通信 SipServlet

主要功能点为 `跨域呼叫`，使用 `XMS` 作为媒体服务器，并提供一定的网络丢包容错能力

> 跨域呼叫：两个不同 SIP 服务器的用户可以互相通话

### 所需安装文件

- Linux 镜像
  - CentOS-7-x86_64-DVD-1511.iso
  - PowerMedia-3.3.16104-1.c7.x86_64.iso
- 安装包
  - lrzsz-0.12.20-36.el7.x86_64.rpm
  - mysql-advanced-5.6.24-linux-glibc2.5-x86_64.tar.gz
  - jdk-7u79-linux-x64.rpm
  - xmlstarlet-1.5.0-1.el6.rf.x86_64.rpm
  - ncurses-devel-5.9-13.20130511.el7.x86_64.rpm
  - libevent-2.0.22-stable.tar.gz
  - mss-3.1.633-jboss-as-7.2.0.Final.zip
- 配置文件
  - change-ip-sip-servlet.sh
  - dlgc_demos.properties
  - dlgc_JSR309.properties

> 最后三个配置文件在项目的 `env` 文件夹中

### 部署过程

#### Linux

- SipServlet
  - CentOS 7.2 （CentOS-7-x86_64-DVD-1511.iso）
  - GNOME Desktop（Compatibility Libraries & Development Tools）
- XMS 3.3
  - 16104.rc1

安装 `lrzsz` 后可以使用 `rz` 命令

进行文件上传

```sh
rpm -ivh lrzsz-0.12.20-36.el7.x86_64.rpm
```

关闭并禁用防火墙

```sh
systemctl stop firewalld.service
systemctl disable firewalld.service
```

#### MySql 5.6

```sh
rpm -qa | grep mariadb
rpm -e --nodeps xxx
```

```shell
rm /etc/my.cnf
```

```sh
groupadd mysql
useradd -g mysql mysql
```

把 `mysql-advanced-5.6.24-linux-glibc2.5-x86_64.tar.gz` 放在 `/usr/local` 下

```sh
tar -zxvf mysql-advanced-5.6.24-linux-glibc2.5-x86_64.tar.gz
mv mysql<tab> mysql
```

```sh
vim /etc/my.cnf
```

```shell
[mysql]
default-character-set=utf8
socket=/var/lib/mysql/mysql.sock

[mysqld]
character-set-server=utf8
skip-name-resolve
lower_case_table_names=1
port = 3306
socket=/var/lib/mysql/mysql.sock
basedir=/usr/local/mysql
datadir=/usr/local/mysql/data
max_connections=200
character-set-server=utf8
default-storage-engine=INNODB
lower_case_table_names=1
max_allowed_packet=16M

[client]
default-character-set=utf8
```

```sh
cd /usr/local/mysql
chown -R mysql:mysql ./
./scripts/mysql_install_db --user=mysql
chown -R mysql:mysql data
```

```sh
chown 777 /etc/my.cnf
cp ./support-files/mysql.server /etc/rc.d/init.d/mysqld
chmod +x /etc/rc.d/init.d/mysqld
chkconfig --add mysqld
chkconfig --list mysqld
```

```sh
service mysqld start
```

```sh
vim /etc/profile
export PATH=$PATH:/usr/local/mysql/bin
source /etc/profile
```

```sql
mysql -u root -p
use mysql;
update user set password=password('123456') where user='root'and host='localhost';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123456' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

> 安装完成后的 mysql 用户名和密码分别是 root 和 123456

#### JDK 1.7

移除自带的 java 相关的组件（如果是最小化安装的话这一步不用做）

```shell
rpm -qa | grep java
rpm -e --nodeps 所有上面列出的条目，空格分隔
```

安装 jdk

```sh
rpm -ivh jdk-7u79-linux-x64.rpm
```

配置环境变量

```sh
vim /etc/profile
```

```sh
export JAVA_HOME=/usr/java/jdk1.7.0_79
export CLASSPATH=.:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
export PATH=$PATH:$JAVA_HOME/bin
```

```sh
source /etc/profile
```

验证

```sh
java -version
javac
```

#### xmlstarlet、Tmux （可选）

```shell
rpm -ivh xmlstarlet-1.5.0-1.el6.rf.x86_64.rpm
rpm -ivh ncurses-devel-5.9-13.20130511.el7.x86_64.rpm
```

```shell
tar -xvzf libevent-2.0.22-stable.tar.gz
cd libevent-2.0.22-stable
./configure --prefix=/usr/local && make && make install
```

```shell
cd ..
tar -xvzf tmux-2.1.tar.gz
cd tmux-2.1
LDFLAGS="-L/usr/local/lib -Wl,-rpath=/usr/local/lib" ./configure --prefix=/usr/local
make && make install
```

#### JBoss & SipServlet

把 `mss-3.1.633-jboss-as-7.2.0.Final.zip` 放在 `/root` 下并解压

```shell
unzip mss-3.1.633-jboss-as-7.2.0.Final.zip
```

将脚本 `change-ip-sip-servlet.sh` 放在 `mss` 目录下，赋予执行权限后执行脚本

```sh
chmod 777 change-ip-sip-servlet.sh
./change-ip-sip-servlet.sh
```

新建 env 文件夹，用于放置配置文件

```shell
mkdir env && cd env
```

配置文件 `sysstr.env`

```bash
echo "realm 10.109.246.93" > sysstr.env
```

配置文件 `jdbc.properties`

```sh
vim jdbc.properties
```

```bash
jdbc.driverClass=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://10.109.246.143:3306/my_sip_app_test
jdbc.userName=zlren
jdbc.password=Lab2016!
```

配置文件 `heart.properties`

```bash
vim heart.properties
```

```properties
enable=1 # 0 disable
cycle=3 # 心跳发送周期
timeout=8 # 超时时间
check=3 # 检测并释放资源
```

不太懂这句是做什么

```shell
cd /root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/
java -cp ../../modules/system/layers/base/org/picketbox/main/picketbox-4.0.15.Final.jar org.jboss.security.auth.callback.RFC2617Digest admin sip-servlets secret
```

这句可以不用做

```shell
echo "admin=<hash>" > sip-servlets-users.properties
```

在 `configuration` 下，生成证书

```shell
cd /root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/
mkdir ca && cd ca
keytool -genkeypair -alias myserver -keyalg RSA -keysize 1024 -keypass secret -validity 365 -storetype jks -keystore myserver.jks -storepass secret -v -dname "CN=James Smith, OU=Engineering, O=My Company, L=My City, S=My State, C=US"
```

修改启动脚本

```shell
vim /root/mss-3.1.633-jboss-as-7.2.0.Final/bin/run.sh
./standalone.sh -Djavax.net.ssl.keyStorePassword=secret -Dgov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Disabled -Djavax.net.ssl.keyStore=/root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/ca/myserver.jks -Djavax.net.ssl.trustStorePassword=secret -Djavax.net.ssl.trustStore=/root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/ca/myserver.jks
```

配置 `https`、`wss` 和 `5083`

```shell
vim /root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/standalone-sip.xml
```

搜索 `urn:jboss:domain:web:1.4` ，注释的那一段后面

```shell
<connector name="https" protocol="HTTP/1.1" scheme="https" socket-binding="https" secure="true">
<ssl protocol="TLSv1,TLSv1.1,TLSv1.2" certificate-key-file="/root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/ca/myserver.jks" certificate-file="/root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/ca/myserver.jks" password="secret"/>
</connector>
```

下面这两句添加到类似位置

```shell
<connector name="sip-wss" protocol="SIP/2.0" scheme="sip" socket-binding="sip-wss"/>
<socket-binding name="sip-wss" port="5083"/>
```

部署 `war` 包

```shell
cd /root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/deployments/
rm -rf *
放入 war 包
```

修改 `dar` 文件

```shell
vim /root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/dars/mobicents-dar.properties
```

```shell
REGISTER: ("com.zczg.app.SipAppP2PApplication.SipAppP2PServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
INVITE: ("com.zczg.app.SipAppP2PApplication.SipAppP2PServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
INFO: ("com.zczg.app.SipAppP2PApplication.SipAppP2PServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
MESSAGE: ("com.zczg.app.SipAppP2PApplication.SipAppP2PServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
# RESPONSE: ("com.zczg.app.SipAppP2PApplication.SipAppP2PServlet","DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
ALL: ("com.zczg.app.SipAppP2PApplication","DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
# CANCEL: ("com.zczg.app.SipAppP2PApplication.SipAppP2PServlet","DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
```

#### XMS

修改配置文件

```shell
vim /root/mss-3.1.633-jboss-as-7.2.0.Final/bin/standalone.sh
```

在 `export JBOSS_HOME` 的后面添加

```shell
# Dialogic additions
export APPSERVER_PLATFORM="TELESTAX"
export DIALOGIC_DEMO_PROPERTY_FILE=${JBOSS_HOME}/standalone/configuration/dlgc_demos.properties
export DLG_PROPERTY_FILE=${JBOSS_HOME}/standalone/configuration/dlgc_JSR309.properties
```

将 `dlgc_demos.properties` 和 `dlgc_JSR309.properties` 放在 `configuration` 下

```shell
vim /root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/dlgc_JSR309.properties
```

```shell
connector.sip.address=${SipServerIP}
connector.sip.port=5080
mediaserver.1.sip.address=${XmsIP}
mediaserver.1.sip.port=5060
```

### 启动

```shell
cd /root/mss-3.1.633-jboss-as-7.2.0.Final/bin
./run.sh
```

> 附 x-Lite 的 SIP 账户配置

![](https://ws1.sinaimg.cn/large/006tNc79ly1fliihsfthxj30i60h1wfy.jpg)