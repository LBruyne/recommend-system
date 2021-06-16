# recommend-system

基于Spark和ElasticSearch搭建的商铺推荐系统

## 系统介绍

### 整体架构

系统整体架构如下：

![img.png](assets/framework.png)

### 技术栈

- JDK1.8
- SpringBoot：后端开发框架
- ElasticSearch + Spark：推荐算法实现
- Canal：数据增量中间件

### 开启应用

- 配置MySQL
- 配置ElasticSearch
- 根据Resource中的DDL和DML文件，进行数据库的新建

## 推荐系统

### V1.0

- 主要基于经纬度计算距离，结合简单评分机制进行推送
- 评分推荐主要是基于距离和评分进行加权计算  
- 主要是为了形成逻辑闭环

## 搜索系统

### V1.0

- 首先根据输入的关键词在数据库里模糊匹配
- 根据上述推荐算法进行排序返回
- 对keyword搜索的商品结果，进行tag的聚类，然后返回所有的tag供用户进一步选择
- 二次筛选：用户添加tag、orderBy、category等进一步搜索的条件，则结合新的条件重新搜索和聚类