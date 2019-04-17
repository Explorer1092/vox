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

package com.voxlearning.utopia.agent.service.log;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.persist.AgentOperationLogPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentOperationLog;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alex on 14-8-7.
 */
@Named
public class AsyncLogService extends SpringContainerSupport {

    @Inject
    AgentOperationLogPersistence agentOperationLogPersistence;

    private BlockingQueue<AgentOperationLog> blockingQueue;
    private boolean runningFlag;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        // 最大缓存3000条日志信息
        blockingQueue = new ArrayBlockingQueue<>(10000);
        runningFlag = true;
//        new AsyncLoggerWriter().start();
    }

    public void logLogin(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "Login", requestUrl, result, notes);
    }

    public void logLogout(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "Logout", requestUrl, result, notes);
    }

    public void logKpiModified(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "MdfKpi", requestUrl, result, notes);
    }

    public void logProductModified(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "MdfProduct", requestUrl, result, notes);
    }

    public void logUserModified(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "MdfUser", requestUrl, result, notes);
    }

    public void logGroupModified(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "MdfGroup", requestUrl, result, notes);
    }

    public void logSysPathModified(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "MdfSysPath", requestUrl, result, notes);
    }

    public void logResetPassword(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "ResetPassword", requestUrl, result, notes);
    }

    public void logOrder(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "Order", requestUrl, result, notes);
    }

    public void logUserKpiModified(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "MdfUserKpi", requestUrl, result, notes);
    }

    public void logSendNotify(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "SendNotify", requestUrl, result, notes);
    }

    public void logTask(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "Task", requestUrl, result, notes);
    }

    public void logMarketFeeConfirmed(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "ConfirmMarketFee", requestUrl, result, notes);
    }

    public void logBatchRegTeacher(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "BatchRegTeacher", requestUrl, result, notes);
    }

    public void logBatchRegSchool(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "logBatchRegSchool", requestUrl, result, notes);
    }

    public void logDownloadNameList(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "DownloadNameList", requestUrl, result, notes);
    }

    public void logWorkRecordRemoved(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "WorkRecord", requestUrl, result, notes);
    }

    public void logResetTeacherPasswd(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "ResetTeacherPasswd", requestUrl, result, notes);
    }

    public void logUserAction(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "UserAction", requestUrl, result, notes);
    }

    // add kpiConfigLog
    public void logKpiConfig(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "KpiConfig", requestUrl, result, notes);
    }

    // add dict operation Log
    public void logDictOperation(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "DictOp", requestUrl, result, notes);
    }

    // add dict operation Log
    public void logClearDeviceIdOperation(AuthCurrentUser operator, String requestUrl, String result, String notes) {
        addlogInfo(operator, "DeviceIdOp", requestUrl, result, notes);
    }


    public void addlogInfo(AuthCurrentUser operator, String operationType, String requestUrl, String result, String notes) {

//        AgentOperationLog logInfo = new AgentOperationLog();
//        logInfo.setOperatorId(operator.getUserId());
//        logInfo.setOperatorName(operator.getRealName());
//        logInfo.setOperationType(operationType);
//        logInfo.setActionUrl(requestUrl);
//        if (result != null && result.length() > 200) {
//            logInfo.setOperationResult(result.substring(0, 199));
//        } else {
//            logInfo.setOperationResult(result);
//        }
//
//        if (notes != null && notes.length() > 200) {
//            logInfo.setOperationNotes(notes.substring(0, 199));
//        } else {
//            logInfo.setOperationNotes(notes);
//        }
//
//        if (blockingQueue.remainingCapacity() > 0) {
//            blockingQueue.add(logInfo);
//        } else {
//            logger.warn("The logging collection queue is full with size " + blockingQueue.size());
//            // logger.info("the following operation was lost:" + logInfo.toString());
//        }
    }

//    private class AsyncLoggerWriter extends Thread {
//        public void run() {
//
//            while (runningFlag) {
//                try {
//                    AgentOperationLog logItem = blockingQueue.poll(10, TimeUnit.SECONDS);
//                    if (logItem != null) {
//                        agentOperationLogPersistence.insert(logItem);
//                    }
//                    sleep(10);
//                } catch (InterruptedException e) {
//                    // ignore this exception
//                } catch (Exception e) {
//                    logger.warn("Logging error happened", e);
//                }
//            }
//        }
//    }

    @Override
    public void destroy() throws Exception {
        runningFlag = false;
    }
}