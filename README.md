### 武汉融合通信 SipServlet

> 主要功能点为 `跨域呼叫`，使用 `XMS` 作为媒体服务器

### 部署过程

#### Linux

CentOS 7.2 （CentOS-7-x86_64-DVD-1511.iso）

GNOME Desktop（Compatibility Libraries and Development Tools）

安装 `lrzsz` 后可以使用 `rz` 命令进行上传

```sh
rpm -ivh lrzsz-0.12.20-36.el7.x86_64.rpm
```

关闭防火墙

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
mv mysql-tab mysql
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
FLUSH PRIVILEGES ;
```

#### JDK 1.7

```shell
rpm -qa | grep java
rpm -e --nodeps 所有上面列出的条目
```

```sh
rpm -ivh jdk-7u79-linux-x64.rpm
```

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

```sh
java -version
javac
```

#### xmlstarlet、tmux 可选

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
tar -xvzf tmux-2.1.tar.gz
cd tmux-2.1
LDFLAGS="-L/usr/local/lib -Wl,-rpath=/usr/local/lib" ./configure --prefix=/usr/local
make && make install
```

#### JBoss & SipServlet

把 `mss-3.1.633-jboss-as-7.2.0.Final.zip` 放在 `/root` 下

```shell
unzip mss-3.1.633-jboss-as-7.2.0.Final.zip
```

`change-ip-sip-servlet.sh` 放在 `mss` 目录下

```shell
chmod 777 change-ip-sip-servlet.sh
./change-ip-sip-servlet.sh
```

```shell
mkdir env && cd env
```

```shell
echo "realm 10.109.246.93" > sysstr.env
```

```shell
vim jdbc.properties
jdbc.driverClass=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://10.109.246.143:3306/my_sip_app_test
jdbc.userName=zlren
jdbc.password=Lab2016!
```

```shell
cd /root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/
java -cp ../../modules/system/layers/base/org/picketbox/main/picketbox-4.0.15.Final.jar org.jboss.security.auth.callback.RFC2617Digest admin sip-servlets secret
```

```shell
echo "admin=<hash>" > sip-servlets-users.properties
```

在 `configuration` 下

```shell
mkdir ca && cd ca
keytool -genkeypair -alias myserver -keyalg RSA -keysize 1024 -keypass secret -validity 365 -storetype jks -keystore myserver.jks -storepass secret -v -dname "CN=James Smith, OU=Engineering, O=My Company, L=My City, S=My State, C=US"
```

```shell
vim /root/mss-3.1.633-jboss-as-7.2.0.Final/bin/run.sh
./standalone.sh -Djavax.net.ssl.keyStorePassword=secret -Dgov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Disabled -Djavax.net.ssl.keyStore=/root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/ca/myserver.jks -Djavax.net.ssl.trustStorePassword=secret -Djavax.net.ssl.trustStore=/root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/ca/myserver.jks
```

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

```shell
cd /root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/deployments/
rm -rf *
放入war包
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

修改配置文件

```shell
vim /root/mss-3.1.633-jboss-as-7.2.0.Final/bin/standalone.sh
```

`export JBOSS_HOME` 的后面

```shell
# Dialogic additions
export APPSERVER_PLATFORM="TELESTAX"
export DIALOGIC_DEMO_PROPERTY_FILE=${JBOSS_HOME}/standalone/configuration/dlgc_demos.properties
export DLG_PROPERTY_FILE=${JBOSS_HOME}/standalone/configuration/dlgc_JSR309.properties
```

`dlgc_demos.properties` 和 `dlgc_JSR309.properties` 放在 `configuration` 下

```shell
vim /root/mss-3.1.633-jboss-as-7.2.0.Final/standalone/configuration/dlgc_JSR309.properties
```

```shell
connector.sip.address=xx
connector.sip.port=5080
mediaserver.1.sip.address=xx
mediaserver.1.sip.port=5060
```

#### 启动

```shell
cd /root/mss-3.1.633-jboss-as-7.2.0.Final/bin
./run.sh
```
