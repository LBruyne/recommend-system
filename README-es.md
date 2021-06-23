# Elastic Search使用手册

## 用途说明

### ElasticSearch介绍

Elasticsearch是一个基于Lucene的搜索服务器。

它提供了一个分布式多用户能力的全文搜索引擎，基于RESTful Web接口。

官网：https://www.elastic.co

### ES使用目的

对内，ES是一个分布式存储数据库；对外，ES是一个搜索引擎

1. 倒排索引及Token的概念
2. analyze分析过程：字符过滤，字符处理（分词），分词过滤

> 例如：Eating an apple a day & keeps the doctor away
> 字符过滤: & 过滤掉
> 字符处理: 用标准分词器，以空格和标点符号分割
> 分词过滤: 变小写
> 最后建立索引 eating an apple a day keeps the doctor away

3. 存储并索引：
只有被analyze后生成的索引存储下来后才能被搜索到，单纯的存储不analyze的数据不能被搜索，但可以读取出来展示给用户

## 安装方法

不同平台下安装的方法是相似的，这里以MacOS下的安装流程进行示例。Windows和其他Linux版本下的安装方法可以参考相关网站实现。欢迎在这里添加其他平台下的安装方法。

### Mac下

#### 使用Homebrew安装

Homebrew是Mac下的包管理工具，类似于Linux下的apt和apt-get。

```shell
brew install elasticsearch
```

添加环境变量后可以方便的启动

使用一条命令就可完成安装流程。但是这样的安装方法亲测可能会出现问题，导致之后启动应用失败。

#### 使用源码进行安装

从网络上应该可以找到指定版本的ElasticSearch压缩包或源码文件，将其安装到本地。项目中使用安装的是最新的7.13版本

这里我将源码放在了/usr/local目录下。

可以看到这里有es的脚本文件，那么使用
```shell
bash elasticsearch 
```
即可启动应用。

浏览器访问localhost:9200端口查看返回判断启动是否正常

## 配置集群

ES一共有两种运行模式，分别是集群模式和伪集群模式。

集群模式很好理解，就是正常运行的情况下，多台主机上分别配置ES客户端，然后进行通信即可。而伪集群模式，就是在自己的本机上运行多个ES进程，分别对应不同的端口，假装集群的运行。

由于ES的每个分片都要有重复(replicate)，而且不能和原始分片位于同一个节点上（否则会不健康），所以需要集群进行协同。

### 伪集群模式配置

- 解压多份源代码，然后分别配置
- 在config.yml文件中配置cluster-name, node-name(唯一), network.host, http.port, transport.tcp.port, http.cors.enabled, http.cors.allow-origin
discovery.seed_hosts, cluster.initial_master_nodes
- 分别启动
