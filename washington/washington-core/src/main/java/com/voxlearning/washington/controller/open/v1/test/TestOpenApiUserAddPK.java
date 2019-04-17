/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 14-6-11.
 */
public class TestOpenApiUserAddPK {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "OPEN_API_TEST_APP";
            final String sessionKey = "a4623619070069e70cdb0c0945776cfa24a3f7689850c867";
            final String pkVltality = "1";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            paramMap.put("pk_vitality", pkVltality);

            String secretKey = "Abxc1923";

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://localhost:8080/v1/user/pk/add.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString());

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
