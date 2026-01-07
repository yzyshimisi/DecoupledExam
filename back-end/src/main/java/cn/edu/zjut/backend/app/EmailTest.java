package cn.edu.zjut.backend.app;

import cn.edu.zjut.backend.util.EmailSender;

import java.util.List;

public class EmailTest {
    public static void main(String[] args) {

        if(EmailSender.sendMail(List.of("3266509142@qq.com"), "测试主题", "测试内容")){
            System.out.println("abc");
        }
    }
}
