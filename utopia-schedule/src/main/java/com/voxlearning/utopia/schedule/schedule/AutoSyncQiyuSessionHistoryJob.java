///*
// * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
// *
// * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
// *
// * NOTICE: All information contained herein is, and remains the property of
// * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
// * and technical concepts contained herein are proprietary to Shanghai Sunny
// * Education, Inc. and its suppliers and may be covered by patents, patents
// * in process, and are protected by trade secret or copyright law. Dissemination
// * of this information or reproduction of this material is strictly forbidden
// * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
// */
//
//package com.voxlearning.utopia.schedule.schedule;
//
//import com.voxlearning.alps.annotation.common.Mode;
//import com.voxlearning.alps.calendar.DateUtils;
//import com.voxlearning.alps.core.util.*;
//import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
//import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
//import com.voxlearning.alps.lang.convert.SafeConverter;
//import com.voxlearning.alps.lang.mapper.json.JsonUtils;
//import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
//import com.voxlearning.alps.spi.core.HttpClientType;
//import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
//import com.voxlearning.utopia.entity.crm.QiYuSessionRecord;
//import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
//import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
//import com.voxlearning.utopia.service.crm.client.QiYuSessionRecordServiceClient;
//import net.lingala.zip4j.core.ZipFile;
//import net.lingala.zip4j.exception.ZipException;
//import net.lingala.zip4j.model.FileHeader;
//
//import javax.inject.Inject;
//import javax.inject.Named;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * Created by Haitian Gan on 2018/1/11.
// */
//@Named
//@ScheduledJobDefinition(
//        jobName = "同步七鱼客服数据从2018年9月1号",
//        jobDescription = "同步七鱼客服2018年9月1号聊天记录，2018-12-1 3:45执行一次",
//        disabled = {Mode.DEVELOPMENT, Mode.STAGING, Mode.UNIT_TEST, Mode.TEST},
//        cronExpression = "0 45 3 4 12 ? 2018-2018"
//)
//public class AutoSyncQiyuSessionHistoryJob extends ScheduledJobWithJournalSupport {
//
//    private static final String AUTH_URL_TPL = "https://qiyukf.com/openapi/export/session?appKey=%s&time=%s&checksum=%s";
//    private static final String CHECK_URL_TPL = "https://qiyukf.com/openapi/export/session/check?appKey=%s&time=%s&checksum=%s";
//    private static final String APP_KEY = "f10a2349a4bead156114e00f9084177c";
//    private static final String APP_SECRET = "46CDE87EEBC040DA938CC9FB34F90DA8";
//    private static final String RESOURCE_PATH = "/tmp/qiyuhistory";
//    private static final int INTERVAL = 1;
//    private static final int SUCCESS_CODE = 200;
//
//    @Inject private QiYuSessionRecordServiceClient qiYuSessionRecordServiceClient;
//
//    /**
//     * 生成URL，用app_key，app_secret参与鉴权
//     * @param url
//     * @param body
//     * @return
//     */
//    private String generateUrl(String url,Map<String,Object> body){
//        String content = JsonUtils.toJson(body);
//
//        String nonce = DigestUtils.md5Hex(content);
//        String time = Long.toString(Calendar.getInstance().getTimeInMillis() / 1000);
//        String checkSum = DigestUtils.sha1Hex(APP_SECRET + nonce + time);
//
//        return String.format(url,APP_KEY,time,checkSum);
//    }
//
//    @Override
//    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
//                                       long startTimestamp,
//                                       Map<String, Object> parameters,
//                                       ISimpleProgressMonitor progressMonitor) throws Exception {
//
//
//        Date startTime = DateUtils.stringToDate("2018-09-11 00:00:00");
//        Date endTime = DateUtils.stringToDate("2018-11-16 00:00:00");
//        while(startTime.before(endTime)){
//            Date tmpEnd = DateUtils.addDays(startTime, INTERVAL);
//            if(tmpEnd.after(endTime))
//                tmpEnd = endTime;
//            Date exportStart = startTime;
//            Date exportEnd = tmpEnd;
//            startTime = tmpEnd;
//            if (!exportData(exportStart, exportEnd)) {
//                return;
//            }
//            try {
//                Thread.sleep(10000L);
//            } catch (InterruptedException e) {
//                return;
//            }
//        }
//
//    }
//
//    private boolean downloadAndSave(String fileName,String httpUrl){
//        AlpsHttpResponse resp = HttpRequestExecutor.instance(HttpClientType.POOLING)
//                .get(httpUrl)
//                .execute();
//
//        byte[] respData = resp.getOriginalResponse();
//        String dest = RESOURCE_PATH + "/" + fileName;
//        File destFile = new File(dest);
//        if (!destFile.exists()) {
//            destFile.mkdirs();
//        }
//        try {
//            String zipFile = RESOURCE_PATH + "/" + fileName + ".zip";
//            IOUtils.write(respData, new FileOutputStream(zipFile));
//            // 解压
//            String filePath = unZipFile(zipFile, dest, APP_KEY.substring(0, 12));
//            if (filePath == null) {
//                return false;
//            }
//            // 解析
//            List<String> sessionMessages = FileUtils.readLines(new File(dest + "/" + filePath + "/session.txt"), Charset.defaultCharset());
//            List<String> messages = FileUtils.readLines(new File(dest + "/" + filePath + "/message.txt"), Charset.defaultCharset());
//            // 移除最后一行，最后一行是无效数据
//            sessionMessages.remove(sessionMessages.size() - 1);
//            messages.remove(messages.size() - 1);
//            // message 信息按sessionId 进行分组 进行统计每个会话的消息数量
//            // Files.readAllLines(Paths.get(""))
//            List<Map<String, Object>> messageList = new ArrayList<>();
//            for (String string : messages) {
//                Map<String, Object> messageMap = JsonUtils.fromJson(string);
//                // 不统计机器人客服
//                if (SafeConverter.toInt(messageMap.get("from")) == 0 && SafeConverter.toInt(messageMap.get("staffId")) <= 0) {
//                    continue;
//                }
//                messageList.add(MapUtils.m("sessionId", messageMap.get("sessionId"), "time", messageMap.get("time"), "from", messageMap.get("from")));
//            }
//            Map<Long, List<Map<String, Object>>> messageMap = messageList.stream().collect(Collectors.groupingBy(m -> SafeConverter.toLong(m.get("sessionId"))));
//
//            List<QiYuSessionRecord> records = new ArrayList<>();
//            for (String s : sessionMessages) {
//                if (StringUtils.isNoneBlank(s)) {
//                    QiYuSessionRecord qiYuSessionRecord = JsonUtils.fromJson(s, QiYuSessionRecord.class);
//                    // 过滤掉机器人
//                    if (qiYuSessionRecord.getStaffId() != null && qiYuSessionRecord.getStaffId() <= 0) {
//                        continue;
//                    }
//
//                    // 只保留客服会话
//                    if (qiYuSessionRecord.getInteraction() != null && qiYuSessionRecord.getInteraction() != 0) {
//                        continue;
//                    }
//                    // 过滤掉排队超时和留言的会话
//                    if (qiYuSessionRecord.getSType() != null && qiYuSessionRecord.getSType() != 0) {
//                        continue;
//                    }
//                    // 当前会话下的所有发的消息
//                    List<Map<String, Object>> sessionMessageList = messageMap.get(SafeConverter.toLong(qiYuSessionRecord.getId()));
//                    int staffMessageNum = 0;
//                    int userMessageNum = 0;
//                    if (sessionMessageList != null) {
//                        // 按消息发送时间排序
//                        sessionMessageList.sort((Comparator.comparingLong(o -> SafeConverter.toLong(o.get("time")))));
//                        Long userTime = 0L;
//                        Long staffTime = 0L;
//                        // 上一次发送消息的用户类型0客服 1访客
//                        int lastFrom = 0;
//                        // 有效的回复次数
//                        int replayNum = 0;
//                        // 有效回复的总时间
//                        int replayTotalTime = 0;
//                        for (Map<String, Object> map : sessionMessageList) {
//                            int from = SafeConverter.toInt(map.get("from"));
//                            if (from == 0) {
//                                staffMessageNum++;
//                                // 如果上次是访客发送
//                                if (lastFrom == 1) {
//                                    staffTime = SafeConverter.toLong(map.get("time"));
//                                }
//                            } else {
//                                userMessageNum++;
//                                // 如果上次是客服发送
//                                if (lastFrom == 0) {
//                                    userTime = SafeConverter.toLong(map.get("time"));
//                                }
//                            }
//                            // 如果客服发送的时间在访客发送的时间之后并且两次发送身份不一致，认为是一次有效服务
//                            if (staffTime > userTime && lastFrom != from) {
//                                replayNum++;
//                                replayTotalTime += staffTime - userTime;
//                            }
//                            lastFrom = from;
//                        }
//                        if (replayNum > 0) {
//                            qiYuSessionRecord.setReplayAvgTime(replayTotalTime/1000/replayNum);
//                        }
//                    }
//                    qiYuSessionRecord.setStaffMessageNum(staffMessageNum);
//                    qiYuSessionRecord.setUserMessageNum(userMessageNum);
//                    records.add(qiYuSessionRecord);
//                    // 一次入库200个
//                    if (records.size()%200 == 0) {
//                        qiYuSessionRecordServiceClient.getQiYuSessionRecordService().inserts(records);
//                        records.clear();
//                    }
//                }
//            }
//
//            if (!records.isEmpty()) {
//                qiYuSessionRecordServiceClient.getQiYuSessionRecordService().inserts(records);
//            }
//            return true;
//        } catch (IOException e) {
//            logger.error("downloadqiyu message error: {}", e);
//            return false;
//        } catch (ZipException e) {
//            logger.error("unzipqiyu error: {}", e);
//            return false;
//        } finally {
//            // 删除文件
//            try {
//                FileUtils.deleteDirectory(new File(dest));
//            } catch (IOException e) {
//                logger.error("delete qiyu error: {}", e);
//            }
//        }
//    }
//
//    private String unZipFile(String zipFile, String dest, String password) throws ZipException {
//        ZipFile zFile = new ZipFile(zipFile);
//        File file = new File(dest);
//        if (!file.exists()) {
//            file.mkdirs();
//        }
//        if (file.isDirectory()) {
//            zFile.setPassword(password);
//            List<FileHeader> fileHeaders = zFile.getFileHeaders();
//            zFile.extractAll(dest);
//            return fileHeaders.get(fileHeaders.size() - 1).getFileName();
//        }
//        return null;
//    }
//
//    private boolean exportData(Date startTime, Date endTime){
//        String start = Long.toString(startTime.getTime());
//        String end = Long.toString(endTime.getTime());
//
//        Map<String,Object> contentMap = MapUtils.m("start",start,"end",end);
//        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING)
//                .post(generateUrl(AUTH_URL_TPL,contentMap))
//                .json(contentMap)
//                .execute();
//
//        String responseStr = response.getResponseString();
//        Map<String,Object> responseMap = JsonUtils.fromJson(responseStr);
//
//        Integer respCde = MapUtils.getInteger(responseMap,"code");
//        if(respCde != SUCCESS_CODE){
//            logger.error("AutoSyncQiyuMessageJob:session return error!code:{},msg:{}",respCde,MapUtils.getString(responseMap,"message"));
//            return false;
//        }
//
//        String key = MapUtils.getString(responseMap,"message");
//        if(StringUtils.isBlank(key)){
//            logger.error("AutoSyncQiyuMessageJob:the key is missing!");
//            return false;
//        }
//
//        while(true){
//
//            try {
//                Thread.sleep(5000L);
//            } catch (InterruptedException e) {
//                break;
//            }
//
//            contentMap = MapUtils.m("key",key);
//            response = HttpRequestExecutor.instance(HttpClientType.POOLING)
//                    .post(generateUrl(CHECK_URL_TPL,contentMap))
//                    .json(contentMap)
//                    .execute();
//
//            int checkCode = response.getStatusCode();
//            if(checkCode != SUCCESS_CODE){
//                logger.error("AutoSyncQiyuMessageJob:check return error!code:{},detail:{}",checkCode,response.getResponseString());
//                break;
//            }
//
//            responseStr = response.getResponseString();
//            responseMap = JsonUtils.fromJson(responseStr);
//            respCde = MapUtils.getInteger(responseMap,"code");
//
//            if(respCde == null){
//                logger.error("AutoSyncQiyuMessageJob:check response error2!detail:{}",responseStr);
//                break;
//            } else if(respCde == SUCCESS_CODE){
//                String messageUrl = MapUtils.getString(responseMap,"message");
//                // 下载
//                if (downloadAndSave(DateUtils.dateToString(startTime, DateUtils.FORMAT_SQL_DATE) + "-" + DateUtils.dateToString(endTime, DateUtils.FORMAT_SQL_DATE), messageUrl)) {
//                    logger.info("AutoSyncQiyuMessageJob:export msg job is finished!start:{},end:{},url:{}",
//                            DateUtils.dateToString(startTime,"MMdd"),
//                            DateUtils.dateToString(endTime,"MMdd"),
//                            messageUrl);
//                    return true;
//                }
//                break;
//            }
//        }
//        return false;
//    }
//
//}
