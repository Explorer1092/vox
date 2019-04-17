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

package com.voxlearning.utopia.service.surl;

import com.voxlearning.alps.http.client.execute.GET;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.logger.LoggerFactory;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xin.xin
 * @since 9/30/15
 */
public class ServerTest {
    private static final Logger logger = LoggerFactory.getLogger(ServerTest.class);

    public static void main(String[] args) {
        String address = "http://localhost:8080/yeyIJf";

        CountDownLatch countDownLatch = new CountDownLatch(1);

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        logger.info("init thread pool >>>>>>>>>>>");
        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                try {
                    GET get = HttpRequestExecutor.defaultInstance()
                            .get(address)
                            .headers(new BasicHeader("User-Agent", "User-Agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"));

                    logger.info("send http request >>>>>>>>>>");
                    countDownLatch.await();

                    Long begin = System.currentTimeMillis();
                    get.execute();
                    Long end = System.currentTimeMillis();

                    logger.info("send http request end >>>>>>>>>>");
                    logger.info("spend {} millis.", (end - begin));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        countDownLatch.countDown();
        logger.info("start >>>>>>>>>>");
    }
}
