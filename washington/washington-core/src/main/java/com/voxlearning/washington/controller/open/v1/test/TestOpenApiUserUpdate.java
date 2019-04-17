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
public class TestOpenApiUserUpdate {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Teacher";
            final String sessionKey = "6893ba03e656d063d6b52cd4b91f665e";
            String realName = "测试真名";
            String nickName = "测试昵称1";
            String avatarFile = "e:\\avatar-335423101-5397e964a310e52d1d51cc2c.jpg";
//            String avatarData = readBase64ImgFile(avatarFile);

            // 计算SIG
            Map paramMap = new HashMap();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
//            paramMap.put("real_name", realName);
//            paramMap.put("nick_name", nickName);
//            paramMap.put("avatar_dat", avatarData);


            String secretKey = "gvUKQN1EFXKp";

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://www.testing.17zuoye.net/v1/teacher/clazz/applications/list.vpage";
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
        while(readLen > 0) {
            outStream.write(buffer, 0, readLen);
            readLen = inputStream.read(buffer);
        }
        inputStream.close();

        return Base64.encodeBase64String(outStream.toByteArray());
    }

}
