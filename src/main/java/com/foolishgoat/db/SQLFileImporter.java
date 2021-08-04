package com.foolishgoat.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author kubili
 *
 *
 */
public class SQLFileImporter {

    private static Properties prop = new Properties();

    private static List<DBImporter> fileImporters = new ArrayList<DBImporter>();


    public static void main(String[] args) throws ClassNotFoundException, InterruptedException, IOException {

        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(
                    new FileInputStream(
                            new File(System.getProperty("user.dir")
                                    + File.separator + "config"
                                    + File.separator + "config.properties")));
            if(inputStream == null){
                System.out.println("./config/config.properties ==> 配置文件加载失败！");
                System.exit(0);
            }

            prop.load(inputStream);

            System.out.println("==================================");
            System.out.println("=      您的配置");
            System.out.println("==================================");
            for (Object key : prop.stringPropertyNames()) {
                System.out.println("" + key.toString() + " => " + prop.getProperty(key.toString()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("./config/config.properties ==> 配置文件加载失败！");
            System.exit(0);
        }
        Class.forName(prop.getProperty("db.jdbc.class"));
        String filePath = prop.getProperty("sql.file.path");
        int threadCount = Integer.valueOf(prop.getProperty("thread.count"));
        File file = new File(filePath);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                // 是sql文件，
                if(tempList[i].toString().toUpperCase().endsWith(".SQL")){
                    DBImporter dbi = new DBImporter(prop.getProperty("db.jdbc.url"),prop.getProperty("db.jdbc.user"),prop.getProperty("db.jdbc.psw"));
                    dbi.setFilePath(tempList[i].toString());
                    fileImporters.add(dbi);
                }

            }
            if (tempList[i].isDirectory()) {
                //可以递归获取
                // ...
            }
        }

        boolean finish = false;
        while(!finish){
            int runCount = 0;
            int successCount = 0;
            for(int i = 0;i < fileImporters.size();i++){
                if(fileImporters.get(i).isRunning()){
                    runCount += 1;
                }
                if(fileImporters.get(i).isSuccess()){
                    successCount += 1;
                }
            }
            if(successCount == fileImporters.size()){
                finish = true;
            }else{
                if(runCount != 0){
                    if(runCount < threadCount){
                        for(int i = 0;i < fileImporters.size();i++){
                            if(runCount > 0 && !fileImporters.get(i).isSuccess() && !fileImporters.get(i).isRunning()){
                                runCount --;
                                new Thread(fileImporters.get(i)).start();
                            }
                        }
                    }
                }else{
                    runCount = threadCount;
                    for(int i = 0;i < fileImporters.size();i++){
                        if(runCount > 0 && !fileImporters.get(i).isSuccess() && !fileImporters.get(i).isRunning()){
                            runCount --;
                            new Thread(fileImporters.get(i)).start();
                        }
                    }
                }
            }

            Thread.sleep(1000);
        }

        String logs = "";
        for(int i = 0;i < fileImporters.size();i++){
            logs += fileImporters.get(i).getFilePath() + " ==> " + "执行成功：" + fileImporters.get(i).getCount() + " 执行失败：" + fileImporters.get(i).getErrCount() + "\r\n";
        }
        DBImporter.writeLog("SqlFileImporter",logs);
    }





}
