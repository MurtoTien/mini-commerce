# 本地基础服务

本目录用于存放下载的第三方程序，不提交发行包、JAR、日志或运行数据。clone 后需要自行准备。

## ZooKeeper

1. 从 [Apache ZooKeeper 官方下载页](https://zookeeper.apache.org/releases.html)获取二进制发行包（本项目原使用 3.9.7，文件夹名 apache-zookeeper-3.9.7-bin）。
2. 解压到本目录，确认存在 apache-zookeeper-3.9.7-bin/bin/zkServer.sh 和 lib 下的 JAR。保留完整发行包。
3. 使用 JDK 21，在仓库根目录执行：

```bash
bash backend/scripts/zookeeper.sh start
bash backend/scripts/zookeeper.sh status
bash backend/scripts/zookeeper.sh stop
```

使用其他版本时，将其完整发行包也放在本目录，并将 ZOOKEEPER_HOME 设置为对应发行包目录。脚本使用仓库的 config/zookeeper/zoo.cfg，不依赖发行包内的原配置。脚本固定工作目录，数据存入 backend/datas/zookeeper-data，日志存入 backend/datas/zookeeper-logs。路径在启动时定位，不绑定电脑用户名。

单机注册中心的数据不随 Git 迁移；新电脑启动空目录后，Dubbo 应用会重新注册。MySQL 由本机安装方式管理，不存放在本目录。
