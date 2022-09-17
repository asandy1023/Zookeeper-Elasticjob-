# 分散式任務排程系統
## 功能概述
    一般來說，微服務場景下的任務處理，除需要支援不同的任務類型外還需要有分散式任務的排程能力————既能支援任務處理節點的高可用多備份部署，又能實現分散式任務排程
    *(不允許同一個任務在同一時刻被多個節點重複執行)。*

    ElasticJob-Lite的架構說明如下:

    1. 微服務任務排程系統，透過整合 **ElasticJob-Lite** 來定義分散式任務。

    1. 整合 ElasticJob-Lite 的任務排程系統，會將任務定義資訊註冊到分散式協調服務中心 **ZooKeeper** 中，並透過監聽 ZooKeeper 的事件來完成任務的觸發、執行等邏輯

    1. ElasticJob-Console&UI 與ZooKeeper連接，來實現管理 ElasticJob 任務資訊、執行分散式排程節點操作，以及執行log查詢功能



## 前置作業

>Zookeeper 是分散式協調服務

### 架設 ZooKeeper 分散式協調服務

#### 1. 下載 ZooKeeper 安裝套件

(1)透過 *wget* 命令將 ZooKeeper 的安裝套件下載至伺服器的指定目錄 

>```$ wget https://dlcdn.apache.org/zookeeper/zookeeper-3.7.1/apache-zookeeper-3.7.1-bin.tar.gz```

(2)將安裝套件解壓縮至指定目錄
>```$tar -zxvf apache-zookeeper-3.7.1-bin.tar.gz -C /opt/zookeeper/nodel```
>
>```$tar -zxvf apache-zookeeper-3.7.1-bin.tar.gz -C /opt/zookeeper/node2```
>
>```$tar -zxvf apache-zookeeper-3.7.1-bin.tar.gz -C /opt/zookeeper/node3```


#### 2. 安裝 ZooKeeper 叢集 Job R1 Conso

為了保證 ZooKeeper 叢集節點選舉機制的正常運轉，1個 ZooKeeper 叢集 至少需要3個節點。如果條件有限，則可以透過在!台伺服器中同時執行3個節點來組建一個 ZooKeeper 叢集


(1)分別進入ZooKeeper 安裝套件的解壓縮目錄，複製"/conf" 目錄中的

標本報定:

>```$ cp: zoo_sample.cfg zoo.cfg```

(2)編輯 "zoo.cfg" 設定檔

>```
>#此處為 Zookeeper 資料儲存路徑的設定
>
>dataDir=/opt/zookeeper/node1/data
>
>#設定用戶端連接通訊埠。由於部署在1台機器上，所以分別將 nodel、node2、node3 >中的該參數設定為2181、2182、2183 
>
>client Port=2181
>
>#分設定 ZooKeeper 叢集節點組成，其格式為:server.(伺服器編號)-[IP位址]:(Leader選舉通訊埠):(ZooKeeper 伺服器的通訊連接埠)
>
>server.1-127.0.0.1:2888:3888
>
>server.2-127.0.0.1:2889:3889 
>
>server.3=127.0.0.1:2890:3890 
>
>#參照上述設定，依次修改"node2"、"node3" 解壓縮目錄中的 zoo.cfg 設定檔
>```

(3)設定 ZooKeeper 服務節點的編號

在各個節點的 "data" 資料目錄中分別建立名為"myid"的檔案。根據節點的設定，分別在該檔案中寫入1、2、3標誌，以對應步驟(2)中 " zoo. cfg的 *server伺服器編號* "設定

#### 3. 安裝 JDK

完成前面的步驟，實際上就完成了ZooKeeper 叢集的基本設定。但由於 ZooKeeper 是由 Java 撰寫的，所以，要正常執行它還需要在伺服器中安裝JDK

Ubuntu server安裝JDK的命令

>```
>sudo apt-get install openjdk-8-jdk```


#### 4. 執行 ZooKeeper 叢集

分別進入 ZooKeeper 安裝程式解壓縮目錄的"bin"目錄下,執行啟動 zookeeper 的指令稿。命令

>```
>.$./nodel/apache-zookeeper-3.7.0-bin/bin/zkserver.sh start
>
>Starting zookeeper... STARTED


執行成功後,可查看 ZooKeeper status,命令
>```
>$./node1/apache-zookeeper-3.7.0-bin/bin/zkServer.sh status
>
>Mode: follower 
>
>$./node2/apache-zookeeper-3.7.0-bin/bin/zkServer.sh status
>
>Mode: leader
>
>3./node3/apache-zookeeper-3.7.0-bin/bin/zkServer.sh status
>
>Mode: follower

如果讓集執行成功,一個為 leader 節點,剩下的為 follower 節點

### 部署 ElasticJob 的 Console 管理主控台

實現對 ElasticJob 分散式任務的視覺化管理

#### 1. 下載 ElasticJob-UI 的安裝套件 Get the lite tar in 
>```
>$ wget https://github.com/apache/shardingsphere-elasticjob-ui/shardingsphere-elasticjob-ui-distribution/shardingsphere-elasticjob-lite-ui-bin-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-elasticjob-lite-ui-bin.tar.gz
>
>#解壓縮至伺服器中的指定目錄中備用
>$tar -zxvf apache-shardingsphere-${latest.release.version}-shardingsphere-elasticjob-lite-ui-bin.tar.gz -C /opt/zookeeper-ui


#### 2. 增加 MySQL 驅動程式

由於軟體許可的原因，一些資料庫的JDBC 驅動程式不能直接被 ElasticJob 引入，需要手動增加相關的驅動程式。

(1)下載 MySQL 驅動程式
>```
>#MySQL驅動程式下載
>
>$ wget https://cdn.mysql.com//Downloads/Connector-J/mysql-connector-java-8.0.30.tar.gz

(2)解壓縮下載的MySQL 驅動程式，找到"mysql-connector-java-80.30jar" 檔案，將其複製到 ElasticJob-UI安裝路徑的"./ext-lib" 目錄下
>```
>```$tar -zxvf mysql-connector-java-8.0.30.tar.gz -C /opt/zookeeper-ui/ext-lib```

#### 3.啟動 ElasticJob-UI 服務 進入 ElasticJob-UI 安裝檔案的".bin目錄下命令
>```
>cd /opt/zookeeper-ui
>./bin/start.sh
>
>Starting the ShardingSphere-ElasticJob-UI ...
>Please check the STDOUT file: /opt/elasticjob-console/apache
>shardingsphere-elasticjob-3.0.0-RC1-lite-ui-bin/logs/stdout.log

透過查看主控台輸出日誌判斷服務是否政動成功，命令
>
>#tall -f logs/stdout.log
>
>[INFO] 10:52:16.414 [main] o.s.j.e.a.AnnotationMBeanExporter
>Registering beans for JMX exposure on startup [INFO] 10:52:16.428 [main] o.a.coyote.http11>.Http11NioProtocol - Starting ProtocolHandler ("http-nio-8088"] [INFO] 10:52:16.433 [main] o.a>.tomcat.util.net.NioSelector Pool - Using a shared selector for servlet write/read
>[INFO] 10:52:16.491 [main] o.s.b.c.e.t.Tomcat EmbeddedServletContainer -
>Tomcat started on port(s): 8088 (http)
>[INFO 10:52:16.495 [main] o.a.s.elasticjob.lite.ui.Bootstrap - Started Bootstrap in 7.351 seconds >(JVM running for 8.427)

ElasticJob-UI 的服務已經在 8088 通訊埠執行成功


#### 4. 設定 ZooKeeper連接，實現對分散式任務的管理

在ElasticJob-U1 執行成功後，透過瀏覽器打開主控台
>```
>http://10.211.55.12:8088/#/

此時系統會進入登入頁面


### 此專案要先使用 **asandy1023/elasticjob-springboot-starter** 這個專案推到maven私庫中。然後在此專案中引入我們的starter依賴，使用interface @ElasticTask() 來進行操作任務排程
