/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.DigestUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.entity.crm.QiYuMobileRecord;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.crm.client.QiYuMobileRecordServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Haitian Gan on 2018/1/11.
 */
@Named
@ScheduledJobDefinition(
        jobName = "同步七鱼通话记录",
        jobDescription = "同步七鱼通话记录，每30分钟跑一次",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.STAGING, Mode.TEST},
        cronExpression = "0 0/30 * * * ? *"
)
public class AutoSyncQiyuMessageMobileJob extends ScheduledJobWithJournalSupport {

    private static final String CHECK_URL_TPL = "https://qiyukf.com/openapi/ipcc/ipccrecord?appKey=%s&time=%s&checksum=%s";
    private static final String APP_KEY = "f10a2349a4bead156114e00f9084177c";
    private static final String APP_SECRET = "46CDE87EEBC040DA938CC9FB34F90DA8";
    private static final int INTERVAL = 1;
    private static final int SUCCESS_CODE = 200;
    private static final String CK = "qiyu_mobile_start_time";

    @Inject private QiYuMobileRecordServiceClient qiYuMobileRecordServiceClient;
    /**
     * 生成URL，用app_key，app_secret参与鉴权
     * @param body
     * @return
     */
    private String generateUrl(Map<String, Object> body){
        String content = JsonUtils.toJson(body);

        String nonce = DigestUtils.md5Hex(content);
        String time = Long.toString(Calendar.getInstance().getTimeInMillis() / 1000);
        String checkSum = DigestUtils.sha1Hex(APP_SECRET + nonce + time);

        return String.format(CHECK_URL_TPL,APP_KEY,time,checkSum);
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String startDateStr = MapUtils.getString(parameters,"startDate");
        String endDateStr = MapUtils.getString(parameters,"endDate");

        Date endTime = new Date();
        if(!StringUtils.isBlank(endDateStr)) {
            endTime = DateUtils.stringToDate(endDateStr,DateUtils.FORMAT_SQL_DATETIME);
        }
        Date ckValue = CacheSystem.CBS.getCache("storage").load(CK);
        Date cacheStartTime = SafeConverter.toDate(ckValue);
        Date startTime;
        if(StringUtils.isBlank(startDateStr)){
            if (cacheStartTime != null) {
                startTime = cacheStartTime;
            } else {
                startTime = DateUtils.addHours(endTime, -INTERVAL);
            }
        } else {
            startTime = DateUtils.stringToDate(startDateStr,DateUtils.FORMAT_SQL_DATETIME);
        }

        while(startTime.before(endTime)){
            Date tmpEnd = DateUtils.addHours(startTime, INTERVAL);
            if(tmpEnd.after(endTime))
                tmpEnd = endTime;

            Date exportStart = startTime;
            Date exportEnd = tmpEnd;
            startTime = tmpEnd;
            if (!exportData(exportStart,exportEnd)) {
                return;
            }
            CacheSystem.CBS.getCache("storage").set(CK, 0, exportEnd);
            // 接口限制10分钟请求一次
            try {
                Thread.sleep(10 * 60 * 1000L);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private boolean exportData(Date startTime, Date endTime){
        long start = startTime.getTime();
        long end = endTime.getTime();

        Map<String,Object> contentMap = MapUtils.m("startTime", start, "endTime", end);
        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING)
                .post(generateUrl(contentMap))
                .json(contentMap)
                .execute();

        String responseStr = response.getResponseString();
        Map<String,Object> responseMap = JsonUtils.fromJson(responseStr);

        Integer respCde = MapUtils.getInteger(responseMap,"code");
        if(respCde != SUCCESS_CODE){
            logger.error("AutoSyncQiyuMessageMobileJob:session return error!code:{},msg:{}",respCde,MapUtils.getString(responseMap,"message"));
            return false;
        }
        String message = MapUtils.getString(responseMap,"message");
        List<QiYuMobileRecord> records = JsonUtils.fromJsonToList(message, QiYuMobileRecord.class);
        if (records != null && !records.isEmpty()) {
            qiYuMobileRecordServiceClient.getQiYuMobileRecordService().inserts(records);
        }
        return true;
    }
}
