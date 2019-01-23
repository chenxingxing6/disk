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
```sql
hbase-daemon.sh start master

create 'email_user','user'
create 'user_id','id'
create 'gid_disk','gid'
create 'user_file','file'
create 'file','file'
create 'follow','name'
create 'followed','userid'
create 'share','content'
create 'shareed','shareid'
```

