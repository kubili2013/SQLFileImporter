package com.foolishgoat.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author kubili
 */
public class DBImporter implements Runnable {


    String url;
    String user;
    String psw;
    private Connection conn;
    private PreparedStatement ps;
    private boolean running = false;
    private boolean success = false;
    private String filePath;
    private String error = "";
    private int count = 0;
    private int errCount = 0;

    public DBImporter(String url, String user, String psw) {
        this.url = url;
        this.user = user;
        this.psw = psw;
    }


    @Override
    public void run() {
        running = true;
        success = false;
        // 访问文件
        // readLine
        // 执行
        try {
            //if(isUTF8(filePath)){
                conn = DriverManager.getConnection(url, user, psw);
                fileImport();
                conn.close();
            //}else{
            //    this.setError( "文件未执行，文件编码非UTF-8，有乱码风险！！！！");
            //}
        } catch (SQLException e) {
            this.setError(e.getMessage());
        } catch (IOException e){
            this.setError(e.getMessage());
        }finally {
            success = true;
            running = false;
            String fName = filePath.trim();
            String fileName = fName.substring(fName.lastIndexOf("/")+1);
            fileName = fName.substring(fName.lastIndexOf("\\")+1);
            try {
                String log = this.getFilePath() + " ==> " + "执行成功："
                        + this.getCount() + " 执行失败：" + this.getErrCount()
                        + "\r\n" + this.getError();

                writeLog(fileName,log);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void excuteSql(String sql) {
        try {
            ps = conn.prepareStatement(sql);
            int result = ps.executeUpdate();
            count += result;
            ps.close();
        } catch (SQLException e) {
            this.setError("---------------------------------------------\r\n");
            this.setError(sql + "\r\n");
            this.setError(e.getMessage() + "\r\n");
            this.setError("---------------------------------------------\r\n");
            errCount += 1;
        }
    }

    public void fileImport() throws IOException {
        String enc = null;
        UnicodeInputStream uin = new UnicodeInputStream(new FileInputStream(filePath), enc);
        InputStreamReader in = null;
        enc = uin.getEncoding();
        if(enc == null ){
            in = new InputStreamReader(uin,"UTF-8");
        }else{
            in = new InputStreamReader(uin, enc);
        }

        BufferedReader reader = new BufferedReader(in);
        String sql = "";
        String temp = "";
        while ((temp = reader.readLine()) != null) {
            if(!temp.startsWith("--")){
                if(temp.endsWith(";")){
                    sql = sql + temp;
                    // 执行
                    if(!sql.equals(";") && !sql.equals("")){
                        excuteSql(sql);
                    }
                    sql = "";
                }else{
                    sql = sql + temp;
                }
            }
        }
        reader.close();
        in.close();
        uin.close();
    }

    public boolean isUTF8BOM(String filePath) throws IOException {
        File file = new File(filePath);
        InputStream in= new java.io.FileInputStream(file);
        byte[] b = new byte[3];
        in.read(b);
        in.close();
        if (b[0] == -17 && b[1] == -69 && b[2] == -65){
            return true;
        }
        else{
            return false;
        }

    }

    public static void writeLog(String name,String text) throws IOException {
        // 写入文件
        File file = new File("./log/" + name + ".log");
        if(!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out=new FileOutputStream(file,false);
        out.write(text.getBytes("utf-8"));
        out.close();
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error += error;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getErrCount() {
        return errCount;
    }

    public void setErrCount(int errCount) {
        this.errCount = errCount;
    }
}
