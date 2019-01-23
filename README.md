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
package com.netpan.dao.basedao;

import com.netpan.dao.conn.HdfsConn;
import com.netpan.entity.File;
import com.netpan.entity.User;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.springframework.stereotype.Repository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Repository("hdfsDao")
public class HdfsDao {
    private final String basePath = "/OnlineDisk/";

    /**
     * 获得在hdfs中的目录
     *
     * @param file
     * @param user
     * @return
     */
    private String formatPathMethod(User user, File file) {
        return basePath + user.getName() + file.getPath();
    }

    /**
     * 上传文件
     *
     * @param inputStream
     * @param file
     * @param user
     */
    public void put(InputStream inputStream, File file, User user) {
        try {
            String formatPath = formatPathMethod(user, file);
            OutputStream outputStream = HdfsConn.getFileSystem().create(new Path(formatPath), new Progressable() {
                @Override
                public void progress() {
                    //System.out.println("upload OK");
                }
            });
            IOUtils.copyBytes(inputStream, outputStream, 2048, true);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建文件夹
     *
     * @param file
     * @param user
     */
    public void mkDir(File file, User user) {
        try {
            String formatPath = formatPathMethod(user, file);
            if (!HdfsConn.getFileSystem().exists(new Path(formatPath))) {
                HdfsConn.getFileSystem().mkdirs(new Path(formatPath));
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件或目录
     *
     * @param file
     * @param user
     */
    public void delete(File file, User user) {
        try {
            String formatPath = formatPathMethod(user, file);
            if (HdfsConn.getFileSystem().exists(new Path(formatPath))) {
                HdfsConn.getFileSystem().delete(new Path(formatPath), true);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重命名文件，未使用
     *
     * @param file
     * @param user
     * @param newname
     */
    public void rename(File file, User user, String newname) {
        try {
            String formatPath = formatPathMethod(user, file);
            file.setName(newname);
            String newformatPath = formatPathMethod(user, file);
            if (HdfsConn.getFileSystem().exists(new Path(formatPath))) {
                HdfsConn.getFileSystem().rename(new Path(formatPath), new Path(newformatPath));
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载文件
     *
     * @param user
     * @param file
     * @param local
     */
    public boolean download(User user, File file, String local) {
        try {
            String formatPath = formatPathMethod(user, file);
            if (HdfsConn.getFileSystem().exists(new Path(formatPath))) {
                FSDataInputStream inputStream = HdfsConn.getFileSystem().open(new Path(formatPath));
                OutputStream outputStream = new FileOutputStream(local);
                IOUtils.copyBytes(inputStream, outputStream, 4096, true);
                System.out.println(local);
                return true;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 复制或者移动文件或者目录
     *
     * @param user
     * @param sourceFile
     * @param destFile
     * @param flag
     */
    public void copyOrMove(User user, File sourceFile, File destFile, boolean flag) {
        try {
            String sourceFormatPath = formatPathMethod(user, sourceFile);
            String destFormatPath = formatPathMethod(user, destFile);
            FileUtil.copy(HdfsConn.getFileSystem(), new Path(sourceFormatPath), HdfsConn.getFileSystem(), new Path(destFormatPath), flag, true, HdfsConn.getConfiguration());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

---
#### HbaseConn
```java
package com.netpan.dao.basedao;

import com.netpan.dao.conn.HbaseConn;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository("hbaseDao")
public class HbaseDao {
    /**
     * 计数器
     * @param tableName
     * @param rowKey
     * @param family
     * @param column
     * @param range
     * @return long
     * @throws IOException
     */
    public long incrCounter(String tableName, String rowKey,  String family, String column, long range) {
    	long count = 0l;
		try {
			Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
			count = table.incrementColumnValue(Bytes.toBytes(rowKey), Bytes.toBytes(family), Bytes.toBytes(column), range);
			table.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
    }

    /**
     * 插入或修改一条数据，针对列族中有一个列，value为long类型
     * @category put 'tableName','rowKey','familyName:columnName'
     * @param tableName
     * @param rowKey
     * @param family
     * @param column
     * @param value
     * @return boolean
     * @throws IOException
     */
    public void updateOneData(String tableName, String rowKey, String family, String column, long value) {
		try {
			Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
			Put put = new Put(Bytes.toBytes(rowKey));
	    	put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
	        table.put(put);
	        table.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * 插入或修改一条数据，针对列族中有一个列，value为String类型
     * @category put 'tableName','rowKey','familyName:columnName'
     * @param tableName
     * @param rowKey
     * @param family
     * @param column
     * @param value
     * @return boolean
     * @throws IOException
     */
    public void updateOneData(String tableName, String rowKey, String family, String column, String value) {
		try {
			Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
			Put put = new Put(Bytes.toBytes(rowKey));
	    	put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
	        table.put(put);
	        table.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * 插入或修改一条数据，针对列族中有一个列，value为boolean类型
     * @category put 'tableName','rowKey','familyName:columnName'
     * @param tableName
     * @param rowKey
     * @param family
     * @param column
     * @param value
     * @return boolean
     * @throws IOException
     */
    public void updateOneData(String tableName, long rowKey, String family, String column, boolean value) {
		try {
			Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
			Put put = new Put(Bytes.toBytes(rowKey));
	    	put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
	        table.put(put);
	        table.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * 插入或修改一条数据，针对列族中有一个列，value为boolean类型
     * @category put 'tableName','rowKey','familyName:columnName'
     * @param tableName
     * @param rowKey
     * @param family
     * @param column
     * @param value
     * @return boolean
     * @throws IOException
     */
    public void updateOneData(String tableName, long rowKey, String family, long column, String value) {
		try {
			Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
			Put put = new Put(Bytes.toBytes(rowKey));
	    	put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
	        table.put(put);
	        table.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * 插入或修改一条数据，针对列族中有一个列，value为long类型
     * @category put 'tableName','rowKey','familyName:columnName'
     * @param tableName
     * @param rowKey
     * @param family
     * @param column
     * @param value
     * @return boolean
     * @throws IOException
     */
    public void updateOneData(String tableName, long rowKey, String family, String column, long value) {
		try {
			Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
			Put put = new Put(Bytes.toBytes(rowKey));
	    	put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
	        table.put(put);
	        table.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * 插入或修改一条数据，针对列族中有一个列，value为string类型
     * @category put 'tableName','rowKey','familyName:columnName'
     * @param tableName
     * @param rowKey
     * @param family
     * @param column
     * @param value
     * @return boolean
     * @throws IOException
     */
    public void updateOneData(String tableName, long rowKey, String family, String column, String value) {
		try {
			Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
			Put put = new Put(Bytes.toBytes(rowKey));
	    	put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
	        table.put(put);
	        table.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * 一次性插入或修改多条数据,针对列族中有多个列
     * @category put 'tableName','rowKey','familyName:columnName'
     * @param tableName
     * @param rowKey
     * @param family
     * @param column
     * @param value
     * @return boolean
     * @throws IOException
     */
    public void updateMoreData(String tableName, long rowKey,  String family, String[] column, String[] value) {
    	try {
	        Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
	        Put put = new Put(Bytes.toBytes(rowKey));
	        for (int i = 0; i < column.length; i++) {
	    		put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column[i]), Bytes.toBytes(value[i]));
	        }
	        table.put(put);
	        table.close();
	    } catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * 获得一行数据，行健为long类型
     * @category get 'tableName','rowKey'
     * @param tableName
     * @param rowKey
     * @return Result||null
     * @throws IOException
     */
    public Result getResultByRow(String tableName, long rowKey) {
    	Result result = null;
    	try {
	        Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
	        Get get = new Get(Bytes.toBytes(rowKey));
	        result = table.get(get);
	        table.close();
    	} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }

    /**
     * 获得一行数据，行健为字符串类型
     * @category get 'tableName','rowKey'
     * @param tableName
     * @param rowKey
     * @return Result||null
     * @throws IOException
     */
    public Result getResultByRow(String tableName, String rowKey) {
    	Result result = null;
    	try {
	        Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
	        Get get = new Get(Bytes.toBytes(rowKey));
	        result = table.get(get);
	        table.close();
    	} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }

    /**
     * 按照一定规则扫描表（或者无规则）
     * @param tableName
     * @param filter
     * @return
     */
    public ResultScanner getResultScannerByFilter(String tableName, Filter filter) {
    	ResultScanner resultScanner = null;
    	try {
    		Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
    		Scan scan = new Scan();
			if (filter != null) {
				scan.setFilter(filter);
			}
    		resultScanner = table.getScanner(scan);
    		table.close();
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	return resultScanner;
    }

    /**
     * 删除某一行的数据，行健为String类型
     * @category deleteall 'tableName','rowKey'
     * @param tableName
     * @param rowKey
     * @throws IOException
     */
    public void deleteDataByRow(String tableName, String rowKey) {
    	try {
	        Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
	        Delete deleteAll = new Delete(Bytes.toBytes(rowKey));
	        table.delete(deleteAll);
	        table.close();
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * 删除某一行的数据，行健为long类型
     * @category deleteall 'tableName','rowKey'
     * @param tableName
     * @param rowKey
     * @throws IOException
     */
    public void deleteDataByRow(String tableName, long rowKey) {
    	try {
	        Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
	        Delete deleteAll = new Delete(Bytes.toBytes(rowKey));
	        table.delete(deleteAll);
	        table.close();
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * 删除某一行中某一列的数据，行健为long类型，列名为long类型
     * @category delete 'tableName','rowKey','falilyName:columnName'
     * @param tableName
     * @param rowKey
     * @param falilyName
     * @param columnName
     */
    public void deleteDataByColumn(String tableName, long rowKey, String falilyName, long columnName) {
    	try {
	        Table table = HbaseConn.getConn().getTable(TableName.valueOf(tableName));
	        Delete deleteColumn = new Delete(Bytes.toBytes(rowKey));
	        deleteColumn.addColumns(Bytes.toBytes(falilyName), Bytes.toBytes(columnName));
	        table.delete(deleteColumn);
	        table.close();
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
```


