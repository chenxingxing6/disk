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

    public static void main(String[] args) {
        HbaseConn hbaseConn = new HbaseConn();
        hbaseConn.getAllTables();

    }
}
