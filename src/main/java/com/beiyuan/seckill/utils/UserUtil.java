package com.beiyuan.seckill.utils;

import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.vo.RespBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 生成多个数据库中User工具类
 * @author: beiyuan
 * @date: 2023/5/9  20:53
 */
public class UserUtil {
    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        //System.out.println(MD5Util.inputPassToFromPass("123456"));
        //因为redis中存的用户ticket只保存30分钟，所以过了之后要重新生成
       createUser(5000);
    }
    private static void createUser(int count) throws SQLException, ClassNotFoundException, IOException {
        //创建用户
        List<User> users=new ArrayList<>(count);
        String basePassword="123456";
        for(int i=0;i<count;i++){
            User user=new User();
            user.setId(13000000000L+i);
            user.setNickname("user_"+i);
            user.setSalt("1a2b3c4d");
            user.setPassword(MD5Util.inputPassToDBPass("123456",user.getSalt()));
            user.setRegisterDate(new Timestamp(new Date().getTime()));
            user.setLoginCount(1);
            users.add(user);
        }
        System.out.println("created user");
        //插入数据库
        Connection conn=getConnection();
        String sql="insert into t_user(login_count,nickname,register_date,salt,password,id) values(?,?,?,?,?,?)";

        PreparedStatement pstmt=conn.prepareStatement(sql);
        for(int i=0;i<users.size();i++){
            User user=users.get(i);
            pstmt.setInt(1,user.getLoginCount());
            pstmt.setString(2,user.getNickname());
            pstmt.setTimestamp(3,user.getRegisterDate());
            pstmt.setString(4,user.getSalt());
            pstmt.setString(5,""+user.getPassword());
            pstmt.setLong(6,user.getId());
            pstmt.addBatch();//添加批处理
        }
        pstmt.executeBatch();
        pstmt.clearParameters();
        conn.close();
        System.out.println("inserted to db");
        //登陆，拿到userTicket
        String urlString="http://localhost:8080/login/doLogin";
        File file=new File("C:\\Users\\beilinanju\\Desktop\\config.txt");
        if(file.exists()){
            file.delete();
        }
        RandomAccessFile raf=new RandomAccessFile(file, "rw");
        raf.seek(0);
        for(int i=0;i<users.size();i++){
            User user=users.get(i);
            URL url=new URL(urlString);
            HttpURLConnection co=(HttpURLConnection)url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            //请求
            OutputStream out=co.getOutputStream();
            String params="mobile="+user.getId()+"&password="+MD5Util.inputPassToFromPass(basePassword);
            out.write(params.getBytes());   //填充参数
            out.flush(); //发送请求
            //响应
            InputStream in=co.getInputStream();//获取响应输入流
            ByteArrayOutputStream bout=new ByteArrayOutputStream();
            byte[]buff=new byte[1024];
            int len=0;
            while ((len=in.read(buff))>=0){
                bout.write(buff,0,len);//将响应输入流转到输出流bout
            }
            in.close();
            bout.close();
            String response=new String(bout.toByteArray());
            ObjectMapper mapper=new ObjectMapper();
            System.out.println(response);
            RespBean respBean=mapper.readValue(response,RespBean.class);//将字符串转化为RespBean
            String userTicket=(String)respBean.getData();
            System.out.println("receive userTicket:"+userTicket);
            //写入到配置文件
            String row =user.getId()+","+userTicket;
            raf.seek(raf.length());//指针指向文件末尾
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());//换行
            System.out.println("writed to file: "+user.getId());
        }

    }
    private static Connection getConnection() throws ClassNotFoundException, SQLException {
        String url="jdbc:mysql://100.65.134.13:3306/seckill?useUnicode=true&" +
                "characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&serverTimezone=GMT%2B8";
        String username="root";
        String password="beiyuan3721";
        String driver="com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }
}
