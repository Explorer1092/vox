package com.voxlearning.washington.controller.mobile.parent.test;


import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;

/**
 * Created by malong on 2016/5/12.
 */
public class TestMobileParentSignController {
    public static void main(String args[]) {
        try {
            System.out.println("Test Start......");

            String apiURL = "http://localhost:8081/parentMobile/parent/sign.vpage";

            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            System.out.println(post.execute().getResponseString());

            System.out.println("Test End......");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
