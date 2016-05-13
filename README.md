# Druid Notice
Druid Notice为Druid增加了自动告警功能。基于插件形式，只需要打开Druid的stat监控，不需要改变其他任何Druid相关依赖和配置。

## How to build

编译Druid Notice需要如下资源:

* Latest stable [Oracle JDK 7](http://www.oracle.com/technetwork/java/)
* Latest stable [Apache Maven](http://maven.apache.org/)

下载代码及编译方法
```
git clone git@github.com:gl2001wl/druid-notice.git
cd druid-notice
mvn -DskipTests=true package
```
 
## Features

Druid Notice需要和Druid运行在同一个JVM容器内，通过访问Druid内部被设置为static的[DruidStatManagerFacade](https://github.com/alibaba/druid/blob/1.0.18/src/main/java/com/alibaba/druid/stat/DruidStatManagerFacade.java)来获得数据库连接池相关监控信息。
同时用户通过配置文件说明需要监控的属性和阈值，触发条件后会通过[JMX](https://docs.oracle.com/javase/tutorial/jmx/)的[MXBean](https://docs.oracle.com/javase/tutorial/jmx/mbeans/mxbeans.html)自动发送JMX的[AttributeChangeNotification](https://docs.oracle.com/javase/7/docs/api/javax/management/AttributeChangeNotification.html)。
用户可自行设置MXBean的Listener通过JMX协议本地或远程获得Notification，来进行相应的告警。

## Quick start

#### 1. 使用Druid作为数据库连接池

关于Druid，请参考[Druid](https://github.com/alibaba/druid)
按照官方文档配置实用Druid并打开stat监控。

#### 2. 配置Druid-Notice

##### 2.1 增加maven依赖

在pom文件中增加：
```
<project>
    ...
    <dependencies>
        ...
        <!-- Druid Notice的主要依赖，JMX Notification的生产者 -->
        <dependency>
            <groupId>com.jd.druid</groupId>
            <artifactId>jmx-producer</artifactId>
            <version>${druid.notice.version}</version>
        </dependency>
        ...
    </dependencies>
</project>
```
当前版本为`1.0-SNAPSHOT`

##### 2.2 添加Druid Notice的配置文件

默认使用classpath下的druidnotice.yaml（关于yaml格式，请参考[yamlbeans](http://yamlbeans.sourceforge.net/)）。
同时也可以通过JVM启动参数`-Ddruid.notice.conf=[yourConfFile.yaml]`来自定义配置文件路径。

##### 2.3 在spring中增加bean初始化

```
<bean id="druidNoticeProducer" class="com.jd.druid.notice.producer.NoticeProducer" init-method="start"/>
```

如果没有使用spring，在启动时调用上面bean的`init-method`即可。
    
## Configuration

#### 1. 样例

Druid Notice使用yaml语法进行配置文件(默认为druidnotice.yaml)设置，样例如下
```
period: 10000
msgQueueSize: 5120
monitorItems:
  - itemKey: datasource
    rules:
    - valueName: ErrorCount
      compareValue: 1
      logic: GREATER
      params:
        yourParam: dsErrCount
    - valueName: NotEmptyWaitCount
      compareValue: 20
      logic: GREATER
      params:
        yourParam: dsWaitCount
  - itemKey: sql
    rules:
    - valueName: ErrorCount
      compareValue: 1
      logic: GREATER
      params:
       yourParam: sqlErrCount
    - valueName: MaxTimespan
      compareValue: 100
      logic: GREATER
      params:
        yourParam: sqlMaxTimeSpan
```

(以上仅为参考，需根据实际情况配置)

#### 2. 参数说明

* **period**
    * Optional: true
    * Default: 1200000
    * About: 每隔多少毫秒(ms)从Druid采集一次监控信息，注意不要设置过于频繁。默认两分钟。
    
* **msgQueueSize**
    * Optional: true
    * Default: Integer.MAX_VALUE
    * About: JMX Notification可以积压的最大消息数量。如果达到最大消息数量，采集线程会wait，直到有空闲队列资源。消费线程在处理完数据后会wait，等待采集线程输入数据，消费线程wait timeout为两分钟，防止队列满后消费和采集线程同时wait。
    
* **itemKey**
    * Optional: false
    * Default: NONE
    * Options: datasource | sql
    * About: 目前只支持Druid中数据库连接池和sql两种监控的告警功能，需要分别进行配置。
    
* **valueName**
    * Optional: false
    * Default: NONE
    * About: Druid相应监控资源的具体监控项目，可以在Druid监控中的JSON接口中查看。
    
* **compareValue**
    * Optional: false
    * Default: NONE
    * About: 监控时实际值所要比较的阈值，**目前只支持Long类型**。
    
* **logic**
    * Optional: false
    * Default: NONE
    * Options: GREATER | LESS | EQUALS
    * About: 实际值和阈值进行比较的逻辑，分别为大于、小于和等于。当满足条件后会发送Notification。
    
* **params**
    * Optional: true
    * Default: NONE    
    * About: 自定义参数，一般当有扩展Notification Listener时使用，自元素使用Map的key value进行配置

#### 3. 累加值的处理

Druid中有些统计值是累加统计的，所以不适合每次采集都重复报警。Druid Notice对于累加统计的值会监控达到阈值的峰值，后续只有当超过峰值后，才会触发告警。
例如采集到ErrorCount为10，那么如果下一次采集到还是10的话不会触发告警，只有当大于10，例如采集到11的时候才会触发一次新的告警。

##### 3.1 Druid Notice中区分累加统计的元素：

* datasource
    * QueryTimeout
    * TransactionQueryTimeout
    * LoginTimeout
    * NotEmptyWaitCount
    * NotEmptyWaitMillis
    * LogicConnectCount
    * LogicCloseCount
    * LogicConnectErrorCount
    * PhysicalConnectCount
    * PhysicalCloseCount
    * PhysicalConnectErrorCount
    * ExecuteCount
    * ErrorCount
    * CommitCount
    * RollbackCount
    * PSCacheAccessCount
    * PSCacheHitCount
    * PSCacheMissCount
    * StartTransactionCount
    * ClobOpenCount
    * BlobOpenCount
* sql
    * ExecuteCount
    * TotalTime
    * MaxTimespan
    * EffectedRowCount
    * FetchRowCount
    * ConcurrentMax
    * ErrorCount
    
## Contact us

gl2001wl@outlook.com