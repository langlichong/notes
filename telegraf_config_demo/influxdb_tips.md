## influxdb: 
### 数据模型
	- "series" : measurement + tags + fields 
	- measurement: 类似关系数据库的表 ，一般包含 name, 一个或多个fields 再加上有或没有的 tags 
	- field: 由 data values + timestamp 构成
	- tags: 是key-value对，一般是为data提供metadata
   _influxdb将fields(data values)与tags(metadata)分开主要是为查询与聚合
		 查询时支持按tags的filter，group,order操作_
	
	### 模型构建注意：
		由于与传统RDBMS区别，需要决策如何将数据拆分成measurements 与 tags 
		及如何在field中表示数据值
### 特点
		使用line protocol 进行数据摄取，极大的提高了高写入的时序数据存储问题
		retention policy: 数据存留时长
		checks and notification 机制（alerting system）: https://www.influxdata.com/blog/influxdbs-checks-and-notifications-system/
		       Kapacitor组件
		       alert_database ：内建的用于存储告警信息
		        alerting 与 notifications feature： 
				enable alerting in InfluxDB, you need to configure a notification endpoint and define the alert rules
				在写入数据时候生成告警 ，支持 thresholds, time duration, and statistical functions
				告警数据可以写入不同的Endpoints: 如邮件、
### 时序数据库选择
[选择因子](https://www.timescale.com/blog/what-is-a-time-series-database/#tips-for-choosing-a-time-series-database)
