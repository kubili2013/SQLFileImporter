# SQL 文件导入工具

## 自己组织文件目录
```
    - config
        - config.properties
    - lib
        - jdbc*
    - log
    - sql
    - sql-file-importer-1.0-SNAPSHOT.jar

```
## 编译成 jar
`mvn clean deploy`
## 配置文件
```$xslt
# jdbc 数据库连接 jar，默认放在 lib 里面
db.jdbc.class=com.mysql.cj.jdbc.Driver
# 数据库连接地址
db.jdbc.url=jdbc:mysql://127.0.0.1:3306/test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
# 数据库用户名
db.jdbc.user=homestead
# 数据库密码
db.jdbc.psw=secret
sql.file.path=E:/src/java/SQLFileImporter/sql
thread.count=2

```
## 执行命令
```
java -jar -Djava.ext.dirs=./lib sql-file-importer-1.0-SNAPSHOT.jar
```
