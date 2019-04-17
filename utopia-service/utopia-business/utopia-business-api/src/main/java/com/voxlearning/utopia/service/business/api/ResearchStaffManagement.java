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

package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.business.api.entity.RSPaperAnalysisReport;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20160308")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
@CyclopsMonitor("utopia")
public interface ResearchStaffManagement extends IPingable {

    // ========================================================================
    // RSPaperAnalysisReport
    // ========================================================================

    RSPaperAnalysisReport saveRSPaperAnalysisReport(RSPaperAnalysisReport report);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    void saveRSPaperAnalysisReports(Collection<RSPaperAnalysisReport> reports);

    void updateRSPaperAnalysisReportStatisticData(String id, int stuNum, int finishNum, int questionNum,
                                                  int correctNum, int listeningScore, int writtenScore,
                                                  List<String> weakPoints, Date curTime);

    void updateRSPaperAnalysisReportFlagReportTime(String id, Date time);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    RSPaperAnalysisReport findFlagRSPaperAnalysisReport();

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS)
    List<RSPaperAnalysisReport> findRSPaperAnalysisReportListByPaperIds(Set<String> paperIds);
}
