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

package com.voxlearning.washington.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.ThreadUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.content.api.WordStockLoader;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.content.api.mapper.VersionedEnglishWordStocks;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Named
public class LocalEnglishWordStockBuffer extends SpringContainerSupport {

    @ImportService(interfaceClass = WordStockLoader.class)
    private WordStockLoader wordStockLoader;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Map<Long, WordStock> buffer = new HashMap<>();


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!initialized.get()) {
                    VersionedEnglishWordStocks data;
                    try {
                        data = wordStockLoader.loadVersionedEnglishWordStocks(0);
                    } catch (Exception ignored) {
                        data = null;
                    }
                    if (data == null) {
                        ThreadUtils.sleepCurrentThread(5000);
                        continue;
                    }
                    data.getEnglishWordStocks().forEach(e -> buffer.put(e.getId(), e));
                    initialized.set(true);
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public WordStock loadWordStock(Long id) {
        if (id == null) {
            return null;
        }
        if (!initialized.get()) {
            return wordStockLoader.loadWordStock(id);
        } else {
            return buffer.get(id);
        }
    }
}
