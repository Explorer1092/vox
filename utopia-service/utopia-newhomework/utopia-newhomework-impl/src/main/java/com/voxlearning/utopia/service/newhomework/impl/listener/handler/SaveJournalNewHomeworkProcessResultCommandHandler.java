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

package com.voxlearning.utopia.service.newhomework.impl.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.JournalNewHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveJournalNewHomeworkProcessResultCommand;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.util.List;

@Named
public class SaveJournalNewHomeworkProcessResultCommandHandler {

    @Inject private JournalNewHomeworkProcessResultDao journalNewHomeworkProcessResultDao;

    public void handle(SaveJournalNewHomeworkProcessResultCommand command) throws Exception {
        List<JournalNewHomeworkProcessResult> results = command.getResults();
        if (CollectionUtils.isNotEmpty(results)) {
            // 暂时kibana上报先扔在这里，上报失败不重试，打个点，计算一下失败驴。
            send(results);
            // 2016-10-25 下午停止写入mongo-journal
            // journalNewHomeworkProcessResultDao.saveJournalNewHomeworkProcessResults(results);
        }
    }

    /**
     * 做题结果上报Kibana测试
     */
    private static void send(List<JournalNewHomeworkProcessResult> results) throws Exception {
        URIBuilder builder = new URIBuilder("http://aurora.17zuoye.net/log?env=" + RuntimeMode.getCurrentStage());
        URI uri = builder.build();
        // _type定义
        // 0:单记录未压缩, 1:多记录未压缩, 2:多记录压缩,4:单记录压缩. 默认是 0.如果没有_type 参数，默认为0
        // _l定义
        // 0:emerg, 1:alert, 2:crit, 3:err/error, 4:warn/warning, 5:notice, 6:info, 7:debug . 默认是 info
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .post(uri)
                .addParameter("_c", "mongo-journal:homework_process_result")
                .addParameter("_type", "1")
                .addParameter("_log", JsonUtils.toJson(results))
                .addParameter("_l", "6")
                .execute();
        if (response.hasHttpClientException() || response.getStatusCode() != HttpStatus.SC_OK) {
            // 记录失败情况
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "mod1", JsonUtils.toJson(results),
                    "op", "mongo-journal:homework_process_result"
            ));
//            throw new Exception();
        }
    }
}
