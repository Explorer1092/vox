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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.business.api.entity.RSPaperAnalysisReport;
import com.voxlearning.utopia.business.api.mapper.ResearchInfo;
import com.voxlearning.utopia.mapper.rstaff.*;
import com.voxlearning.utopia.service.business.api.entity.BizMarketingSchoolData;
import com.voxlearning.utopia.service.region.api.constant.RegionType;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface ResearchStaffService extends IPingable {
    // ResearchStaffService

    //时间点查询专用->  endDate其实总是为null
    ResearchInfo getSchoolStatisticData(Subject subject, String date, String endDate, Integer code, Integer regionType, Long schoolId);

    ResearchInfo getSchoolStatisticDataBySchoolIds(Subject subject, String date, String endDate, Integer code, Integer regionType, Collection<Long> schoolIds);
    //时间点查询专用<-

    //分时查询专用->
    ResearchInfo getSchoolStatisticDataByDateAndRegionListAndSchool(Subject subject, String date, String endDate, Integer code, Integer regionType, List<Integer> areaCodeList, Long schoolId);

    ResearchInfo getSchoolStatisticDataByDateAndRegionListAndSchoolList(Subject subject, String date, String endDate, Integer code, Integer regionType, List<Integer> areaCodeList, Collection<Long> schoolId);
    //分时查询专用<-

//    ResearchInfo getCrazyDetaData(String startDate, String endDate, List<Integer> regionIdList, Long schoolId);

    List<BizMarketingSchoolData> getGroupbyTeacherIdFinderToSchoolStatistic(List<BizMarketingSchoolData> schoolStatistics);

    List<Map<String, Object>> findInviteHistoryByUserId(Long rstaffId);

    List<Map<String, Object>> findInviteHistoryByUserId(Long rstaffId, Subject subject, Boolean isSuccessful, Date startDate, Date endDate, boolean needStuCount);

    String validMarketJobTaskRunSuccessOrFaild();

    ///////////////////////////////TODO 需要重构，先列出必须的保留方法////////////////////////////////////////////////////////////

    // 以下方法建议放在loader中

    // 试卷及报告使用
    List<RSPaperAnalysisReport> getPaperAnalysisReport(String paperId, Integer regionCode, RegionType regionType);

    List<RSPaperAnalysisReport> getPaperAnalysisReport(String paperId, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds);

    List<RSOralPaperReportMapper> getOralAnalysisReport(Long pushId);

    // 大数据
    ResearchStaffPatternMapper getPatternData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term);

    ResearchStaffSkillMapper getSkillData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term);

    ResearchStaffKnowledgeMapper getKnowledgeData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term);

    List<ResearchStaffWeakPointUnitMapper> getWeakPointData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term);

    ResearchStaffUnitWeakPointMapper getUnitWeakPointData(Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term);

    ResearchStaffSkillMonthlyMapper getSkillMonthlyData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term);

    List<ResearchStaffBehaviorDataMapper> getBehaviorData(Long rstaffId, Subject subject, Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term);
}
