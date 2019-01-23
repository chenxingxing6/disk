## 分布式网盘系统

这个版本比较干净，整个demo在Hadoop，和Hbase环境搭建好了，可以启动起来。

---
#### 技术选型
> 1.Hadoop  
> 2.Hbase  
> 3.SpringBoot  
> ......

---
#### 系统实现的功能
> 1.用户登录与注册  
> 2.用户网盘管理  
> 3.文件在线浏览功能  
> 4.文件上传与下载  
> ......

---

![avatar](https://raw.githubusercontent.com/chenxingxing6/disk/master/img/1.png)

---
![avatar](https://raw.githubusercontent.com/chenxingxing6/disk/master/img/2.png)

---
![avatar](https://raw.githubusercontent.com/chenxingxing6/disk/master/img/3.png)

---

#### Hbase创建表语句
![avatar](https://raw.githubusercontent.com/chenxingxing6/disk/master/img/4.png)

---

> hbase-daemon.sh start master  ## 启动Hbase  
create 'email_user','user'  
create 'user_id','id'  
create 'gid_disk','gid'
create 'user_file','file'  
create 'file','file'  
create 'follow','name'  
create 'followed','userid'  
create 'share','content'  
create 'shareed','shareid'

---
#### HdfsConn
```java
package com.netpan.dao.conn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class HdfsConn {
    private FileSystem fileSystem = null;
    private Configuration configuration = null;

    private static class SingletonHolder {
        private static final HdfsConn INSTANCE = new HdfsConn();
    }

    private HdfsConn() {
        try {
            configuration = new Configuration();
            configuration.set("fs.defaultFS", "hdfs://localhost:9000/");
            System.setProperty("HADOOP_USER_NAME", "root");
            configuration.set("dfs.permissions", "false");
            fileSystem = FileSystem.get(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileSystem getFileSystem() {
        return SingletonHolder.INSTANCE.fileSystem;
    }

    public static Configuration getConfiguration() {
        return SingletonHolder.INSTANCE.configuration;
    }
}

```

---
#### HbaseConn
```java
package com.netpan.dao.conn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HbaseConn {
    private Connection conn;
    private Table table;
    private Admin admin;

    private static class SingletonHolder {
        private static final HbaseConn INSTANCE = new HbaseConn();
    }

    private HbaseConn() {
        try {
            Configuration hconf = new Configuration();
            Configuration conf = HBaseConfiguration.create(hconf);
            conf.set("hbase.zookeeper.quorum","localhost");  //hbase 服务地址
            conf.set("hbase.zookeeper.property.clientPort","2181"); //端口号
            conn = ConnectionFactory.createConnection(conf);
            admin = conn.getAdmin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取连接
    public static final Connection getConn() {
        return SingletonHolder.INSTANCE.conn;
    }

    // Hbase获取所有的表信息
    public List getAllTables() {
        List<String> tables = null;
        if (admin != null) {
            try {
                HTableDescriptor[] allTable = admin.listTables();
                if (allTable.length > 0)
                    tables = new ArrayList<String>();
                for (HTableDescriptor hTableDescriptor : allTable) {
                    tables.add(hTableDescriptor.getNameAsString());
                    System.out.println(hTableDescriptor.getNameAsString());
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tables;
    }
}


```


