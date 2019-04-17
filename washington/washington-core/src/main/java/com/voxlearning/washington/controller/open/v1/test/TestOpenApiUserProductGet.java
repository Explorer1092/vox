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
public class TestOpenApiUserProductGet {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "KaplanPicaro";
            String secretKey = "1YEG9Cm4GhbQ";
            final String sessionKey = "0a1c94eb233d532a7d53a5d9f6448e8d";

            // 计算SIG
            Map paramMap = new HashMap();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);



            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://localhost:8080/v1/user/product/get.vpage";
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
