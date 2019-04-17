/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 14-6-11.
 */
public class TestOpenApiUserLogin {
    public static void main(String[] args) {
        teacherLogin();
    }

    private static void teacherLogin() {
        try {
            System.out.println("Test Start...");
            String secretKey = "FP7lk6WDSSXy";

            // 12988885/1
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", "17Teacher");
            paramMap.put("app_product_id", "301");
            paramMap.put("model", "iPhone10,3");

            paramMap.put("user_code", "12988869");
            paramMap.put("passwd", "123456");
            paramMap.put("user_type", "1");

            paramMap.put("uuid", "9ACD2197-2689-48E2-ACF6-462162278185");
            // paramMap.put("uuid", "158CA437-4C3A-471D-BAB2-6A546A5CD2AA");
            paramMap.put("ver", "1.9.4.332");
            paramMap.put("sys", "ios");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);

            String apiURL = "http://localhost:8081/v1/teacher/user/login.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.forEach(post::addParameter);
            System.out.println(post.execute().getResponseString());

            System.out.println("Test End...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void oldTeacher() {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Teacher";
            String secretKey = "gvUKQN1EFXKp";
            final String userCode = "10007";
            String passwd = "726218";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("user_code", userCode);
            paramMap.put("passwd", passwd);


            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://www.testing.17zuoye.net/v1/user/login.api";
            String URL = UrlUtils.buildUrlQuery(apiURL, paramMap);
            System.out.println(HttpRequestExecutor.defaultInstance().post(URL).execute().getResponseString());
            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readBase64ImgFile(String fileName) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        FileInputStream inputStream = new FileInputStream(fileName);
        byte[] buffer = new byte[1024];
        int readLen = inputStream.read(buffer);
        while (readLen > 0) {
            outStream.write(buffer, 0, readLen);
            readLen = inputStream.read(buffer);
        }
        inputStream.close();

        return Base64.encodeBase64String(outStream.toByteArray());
    }

}
