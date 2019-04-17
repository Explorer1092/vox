/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.business.api.entity.RSPaperAnalysisReport;
import com.voxlearning.utopia.service.business.api.ResearchStaffManagement;
import com.voxlearning.utopia.service.business.impl.dao.RSPaperAnalysisReportDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Spring
@Named
@Service(interfaceClass = ResearchStaffManagement.class)
@ExposeService(interfaceClass = ResearchStaffManagement.class)
public class ResearchStaffManagementImpl extends SpringContainerSupport implements ResearchStaffManagement {

    @Inject private RSPaperAnalysisReportDao rsPaperAnalysisReportDao;

    @Override
    public RSPaperAnalysisReport saveRSPaperAnalysisReport(RSPaperAnalysisReport report) {
        return rsPaperAnalysisReportDao.save(report);
    }

    @Override
    public void saveRSPaperAnalysisReports(Collection<RSPaperAnalysisReport> reports) {
        rsPaperAnalysisReportDao.save(reports);
    }

    @Override
    public void updateRSPaperAnalysisReportStatisticData(String id, int stuNum, int finishNum,
                                                         int questionNum, int correctNum, int listeningScore,
                                                         int writtenScore, List<String> weakPoints, Date curTime) {
        rsPaperAnalysisReportDao.updateStatisticData(id, stuNum, finishNum, questionNum,
                correctNum, listeningScore, writtenScore, weakPoints, curTime);
    }

    @Override
    public void updateRSPaperAnalysisReportFlagReportTime(String id, Date time) {
        rsPaperAnalysisReportDao.updateFlagReportTime(id, time);
    }

    @Override
    public RSPaperAnalysisReport findFlagRSPaperAnalysisReport() {
        return rsPaperAnalysisReportDao.findFlagReport();
    }

    @Override
    public List<RSPaperAnalysisReport> findRSPaperAnalysisReportListByPaperIds(Set<String> paperIds) {
        return rsPaperAnalysisReportDao.findByPaperIds(paperIds);
    }
}
