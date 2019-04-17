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

package com.voxlearning.utopia.service.business.impl.service.rstaff;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.business.api.entity.RSPaperAnalysisReport;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.mapper.rstaff.*;
import com.voxlearning.utopia.service.business.api.entity.*;
import com.voxlearning.utopia.service.business.api.entity.embedded.*;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSAreaHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSSchoolHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.impl.dao.*;
import com.voxlearning.utopia.service.business.impl.service.rstaff.bean.*;
import com.voxlearning.utopia.service.business.impl.service.rstaff.convertor.ResearchStaffConvertor;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ResearchStaffReportServiceImpl {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private RSRegionPatternStatDao rsRegionPatternStatDao;
    @Inject private RSSchoolPatternStatDao rsSchoolPatternStatDao;
    @Inject private RSRegionSkillStatDao rsRegionSkillStatDao;
    @Inject private RSSchoolSkillStatDao rsSchoolSkillStatDao;
    @Inject private RSRegionKnowledgeStatDao rsRegionKnowledgeStatDao;
    @Inject private RSSchoolKnowledgeStatDao rsSchoolKnowledgeStatDao;
    @Inject private RSRegionWeakPointStatDao rsRegionWeakPointStatDao;
    @Inject private RSSchoolWeakPointStatDao rsSchoolWeakPointStatDao;
    @Inject private RSRegionUnitWeakPointStatDao rsRegionUnitWeakPointStatDao;
    @Inject private RSSchoolUnitWeakPointStatDao rsSchoolUnitWeakPointStatDao;
    @Inject private RSRegionSkillMonthlyStatDao rsRegionSkillMonthlyStatDao;
    @Inject private RSSchoolSkillMonthlyStatDao rsSchoolSkillMonthlyStatDao;
    @Inject private RSPaperAnalysisReportDao rsPaperAnalysisReportDao;
    @Inject private RSOralPaperAnalysisReportDao rsOralPaperAnalysisReportDao;
    @Inject private RSEnglishSchoolHomeworkBehaviorStatDao rsEnglishSchoolHomeworkBehaviorStatDao;
    @Inject private RSMathSchoolHomeworkBehaviorStatDao rsMathSchoolHomeworkBehaviorStatDao;
    @Inject private RSEnglishAreaHomeworkBehaviorStatDao rsEnglishAreaHomeworkBehaviorStatDao;
    @Inject private RSMathAreaHomeworkBehaviorStatDao rsMathAreaHomeworkBehaviorStatDao;
    @Inject private PaperLoaderClient paperLoaderClient;

    /**
     * 获得题型数据(采用教研员管理区域架构)
     */
    public ResearchStaffPatternMapper getPatternData(Collection<Long> provinceCodes, Collection<Long> cityCodes,
                                                     Collection<Long> areaCodes, Collection<Long> schoolIds,
                                                     Integer year, Term term) {
        List<ResearchStaffPatternData> patternDatas = new ArrayList<>();
        String avgName = "";
        long schoolCount = 0;
        long validSchoolCount = 0;
        long countyCount = 0;

        // 省教研员情况，返回市级数据
        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            // TODO 先直接把所有区域数据加载进来进行统计，因为数据量少，而且以后可能需要区级数据，先这么着试试
            Set<Long> pCityCodes = getCityCodesForProvinceCodes(provinceCodes);
            List<RSRegionPatternStat> rsRegionPatternStats = rsRegionPatternStatDao.findByCityCodes(pCityCodes, year, term);
            countyCount = rsRegionPatternStats.size();
            if (CollectionUtils.isNotEmpty(rsRegionPatternStats)) {
                patternDatas.addAll(ResearchStaffConvertor.convertToCityResearchStaffPatternData(rsRegionPatternStats));
                avgName = "省平均水平";
                schoolCount += rsSchoolPatternStatDao.countByCityCodes(pCityCodes, year, term);
                validSchoolCount += rsSchoolPatternStatDao.countByCityCodesAndIsValid(pCityCodes, true, year, term);
            }
        }

        // 市教研员情况，返回区级数据
        if (CollectionUtils.isNotEmpty(cityCodes)) {
            List<RSRegionPatternStat> rsRegionPatternStats = rsRegionPatternStatDao.findByCityCodes(cityCodes, year, term);
            if (CollectionUtils.isNotEmpty(rsRegionPatternStats)) {
                patternDatas.addAll(ResearchStaffConvertor.convertRegionStatListToResearchStaffPatternData(rsRegionPatternStats));
                avgName = "市平均水平";
                schoolCount += rsSchoolPatternStatDao.countByCityCodes(cityCodes, year, term);
                validSchoolCount += rsSchoolPatternStatDao.countByCityCodesAndIsValid(cityCodes, true, year, term);
            }
        }

        // 区教研员情况，返回校级数据
        if (CollectionUtils.isNotEmpty(areaCodes)) {
            List<RSSchoolPatternStat> rsSchoolPatternStats = rsSchoolPatternStatDao.findByAreaCodes(areaCodes, year, term);
            if (CollectionUtils.isNotEmpty(rsSchoolPatternStats)) {
                patternDatas.addAll(ResearchStaffConvertor.convertSchoolStatListToResearchStaffPatternData(rsSchoolPatternStats));
                avgName = "区平均水平";
                schoolCount += rsSchoolPatternStatDao.countByAreaCodes(areaCodes, year, term);
                validSchoolCount += rsSchoolPatternStatDao.countByAreaCodesAndIsValid(areaCodes, true, year, term);
            }
        }

        // 街道教研员情况，返回校级数据
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            List<RSSchoolPatternStat> rsSchoolPatternStats = rsSchoolPatternStatDao.findBySchoolIds(schoolIds, year, term);
            if (CollectionUtils.isNotEmpty(rsSchoolPatternStats)) {
                patternDatas.addAll(ResearchStaffConvertor.convertSchoolStatListToResearchStaffPatternData(rsSchoolPatternStats));
                avgName = "街道平均水平";
                schoolCount += rsSchoolPatternStatDao.countBySchoolIds(schoolIds, year, term);
                validSchoolCount += rsSchoolPatternStatDao.countBySchoolIdsAndIsValid(areaCodes, true, year, term);
            }
        }

        if (patternDatas.size() == 0) {
            return null;
        }

        ResearchStaffPatternMapper researchStaffPatternMapper = new ResearchStaffPatternMapper();

        // 题型=>总做题量Map
        Map<String, Long> totalFinishCount = new HashMap<>();
        // 题型=>总做对题数Map
        Map<String, Double> totalCorrectCount = new HashMap<>();
        // 平均水平
        ResearchStaffPatternUnitMapper avgData = new ResearchStaffPatternUnitMapper();
        avgData.setName(avgName);
        researchStaffPatternMapper.getPatternUnitList().add(avgData);
        Map<String, ResearchStaffPatternDetailMapper> avgMap = new HashMap<>();

        // 填入每个题型具体数据
        for (ResearchStaffPatternData patternData : patternDatas) {
            fillPatternUnitMapperData(researchStaffPatternMapper, totalFinishCount, totalCorrectCount, patternData.getName(), patternData.getPatterns());
        }

        // 计算市平均水平
        calPatternAvgStat(patternDatas.size(), totalFinishCount, totalCorrectCount, avgData, avgMap);

        // 对总做题量进行排序，放入rank map中
        Map<String, Long> rankMap = generatePatternRankMap(totalFinishCount);
        researchStaffPatternMapper.setPatternRank(rankMap);

        // 统计学校数及有效学校数
        researchStaffPatternMapper.setSchoolCount(schoolCount);
        researchStaffPatternMapper.setValidSchoolCount(validSchoolCount);
        researchStaffPatternMapper.setCountyCount(countyCount);

        return researchStaffPatternMapper;
    }

    /**
     * 获得知识技能数据(采用教研员管理区域架构)
     */
    public ResearchStaffSkillMapper getSkillData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {

        List<ResearchStaffSkillData> skillDatas = new ArrayList<>();
        String avgName = "";
        long schoolCount = 0;
        long validSchoolCount = 0;
        long countyCount = 0;

        // 省教研员情况，返回市级数据
        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            // TODO 先直接把所有区域数据加载进来进行统计，因为数据量少，而且以后可能需要区级数据，先这么着试试
            Set<Long> pCityCodes = getCityCodesForProvinceCodes(provinceCodes);
            List<RSRegionSkillStat> rsRegionSkillStats = rsRegionSkillStatDao.findByCityCodes(pCityCodes, year, term);
            countyCount = rsRegionSkillStats.size();
            if (CollectionUtils.isNotEmpty(rsRegionSkillStats)) {
                skillDatas.addAll(ResearchStaffConvertor.convertToCityResearchStaffSkillData(rsRegionSkillStats));
                avgName = "省平均水平";
                schoolCount += rsSchoolSkillStatDao.countByCityCodes(pCityCodes, year, term);
                validSchoolCount += rsSchoolSkillStatDao.countByCityCodesAndIsValid(pCityCodes, true, year, term);
            }
        }

        // 市教研员情况，返回区级数据
        if (CollectionUtils.isNotEmpty(cityCodes)) {
            List<RSRegionSkillStat> rsRegionSkillStats = rsRegionSkillStatDao.findByCityCodes(cityCodes, year, term);
            if (CollectionUtils.isNotEmpty(rsRegionSkillStats)) {
                skillDatas.addAll(ResearchStaffConvertor.convertRegionStatListToResearchStaffSkillData(rsRegionSkillStats));
                avgName = "市平均水平";
                schoolCount += rsSchoolSkillStatDao.countByCityCodes(cityCodes, year, term);
                validSchoolCount += rsSchoolSkillStatDao.countByCityCodesAndIsValid(cityCodes, true, year, term);
            }
        }

        // 区教研员情况，返回校级数据
        if (CollectionUtils.isNotEmpty(areaCodes)) {
            List<RSSchoolSkillStat> rsSchoolSkillStats = rsSchoolSkillStatDao.findByAreaCodes(areaCodes, year, term);
            if (CollectionUtils.isNotEmpty(rsSchoolSkillStats)) {
                skillDatas.addAll(ResearchStaffConvertor.convertSchoolStatListToResearchStaffSkillData(rsSchoolSkillStats));
                avgName = "区平均水平";
                schoolCount += rsSchoolSkillStatDao.countByAreaCodes(areaCodes, year, term);
                validSchoolCount += rsSchoolSkillStatDao.countByAreaCodesAndIsValid(areaCodes, true, year, term);
            }
        }

        // 街道教研员情况，返回校级数据
        if (schoolIds.size() > 0) {
            List<RSSchoolSkillStat> rsSchoolSkillStats = rsSchoolSkillStatDao.findBySchoolIds(schoolIds, year, term);
            if (CollectionUtils.isNotEmpty(rsSchoolSkillStats)) {
                skillDatas.addAll(ResearchStaffConvertor.convertSchoolStatListToResearchStaffSkillData(rsSchoolSkillStats));
                avgName = "街道平均水平";
                schoolCount += rsSchoolSkillStatDao.countBySchoolIds(schoolIds, year, term);
                validSchoolCount += rsSchoolSkillStatDao.countBySchoolIdsAndIsValid(schoolIds, true, year, term);
            }
        }

        if (skillDatas.size() == 0) {
            return null;
        }

        ResearchStaffSkillMapper researchStaffSkillMapper = new ResearchStaffSkillMapper();

        // 题型=>总做题量Map
        Map<String, Long> totalFinishCount = new HashMap<>();
        // 题型=>总做对题数Map
        Map<String, Double> totalCorrectCount = new HashMap<>();
        // 题型=>最大做题量
        Map<String, ResearchStaffSkillDetailMapper> maxFinishCount = new HashMap<>();
        // 学生人数
        long studentNum = 0;
        // 平均数据
        ResearchStaffSkillUnitMapper avgData = new ResearchStaffSkillUnitMapper();
        avgData.setName(avgName);
        researchStaffSkillMapper.getSkillUnitList().add(avgData);

        // 填入每个知识技能具体数据
        for (ResearchStaffSkillData skillData : skillDatas) {
            fillSkillUnitMapperData(researchStaffSkillMapper, totalFinishCount, totalCorrectCount, maxFinishCount, skillData.getName(), skillData.getStuNum(), skillData.getSkills());
            studentNum += skillData.getStuNum();
        }

        // 剩余学生数总和
        researchStaffSkillMapper.setTotalStudentCount(studentNum);

        // 计算平均水平
        calSkillAvgStat(totalFinishCount, totalCorrectCount, studentNum, avgData, skillDatas.size());

        // 统计学校数及有效学校数
        researchStaffSkillMapper.setSchoolCount(schoolCount);
        researchStaffSkillMapper.setValidSchoolCount(validSchoolCount);
        researchStaffSkillMapper.setCountyCount(countyCount);

        return researchStaffSkillMapper;
    }


    /**
     * 获得语言知识技能数据(采用教研员管理区域架构)
     */
    public ResearchStaffKnowledgeMapper getKnowledgeData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {

        List<ResearchStaffKnowledgeData> knowledgeDatas = new ArrayList<>();
        String avgName = "";
        long schoolCount = 0;
        long validSchoolCount = 0;
        long countyCount = 0;

        // 省教研员情况，返回市级数据
        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            // TODO 先直接把所有区域数据加载进来进行统计，因为数据量少，而且以后可能需要区级数据，先这么着试试
            Set<Long> pCityCodes = getCityCodesForProvinceCodes(provinceCodes);
            List<RSRegionKnowledgeStat> rsRegionKnowledgeStats = rsRegionKnowledgeStatDao.findByCityCodes(pCityCodes, year, term);
            countyCount = rsRegionKnowledgeStats.size();
            if (CollectionUtils.isNotEmpty(rsRegionKnowledgeStats)) {
                knowledgeDatas.addAll(ResearchStaffConvertor.convertToCityResearchStaffKnowledgeData(rsRegionKnowledgeStats));
                avgName = "省平均水平";
                schoolCount += rsSchoolKnowledgeStatDao.countByCityCodes(pCityCodes, year, term);
                validSchoolCount += rsSchoolKnowledgeStatDao.countByCityCodesAndIsValid(pCityCodes, true, year, term);
            }
        }

        // 市教研员情况，返回区级数据
        if (cityCodes.size() > 0) {
            List<RSRegionKnowledgeStat> rsRegionKnowledgeStats = rsRegionKnowledgeStatDao.findByCityCodes(cityCodes, year, term);
            if (CollectionUtils.isNotEmpty(rsRegionKnowledgeStats)) {
                knowledgeDatas.addAll(ResearchStaffConvertor.convertRegionStatListToResearchStaffKnowledgeData(rsRegionKnowledgeStats));
                avgName = "市平均水平";
                schoolCount += rsSchoolKnowledgeStatDao.countByCityCodes(cityCodes, year, term);
                validSchoolCount += rsSchoolKnowledgeStatDao.countByCityCodesAndIsValid(cityCodes, true, year, term);
            }
        }

        // 区教研员情况，返回校级数据
        if (areaCodes.size() > 0) {
            List<RSSchoolKnowledgeStat> rsSchoolKnowledgeStats = rsSchoolKnowledgeStatDao.findByAreaCodes(areaCodes, year, term);
            if (CollectionUtils.isNotEmpty(rsSchoolKnowledgeStats)) {
                knowledgeDatas.addAll(ResearchStaffConvertor.convertSchoolStatListToResearchStaffKnowledgeData(rsSchoolKnowledgeStats));
                avgName = "区平均水平";
                schoolCount += rsSchoolKnowledgeStatDao.countByAreaCodes(areaCodes, year, term);
                validSchoolCount += rsSchoolKnowledgeStatDao.countByAreaCodesAndIsValid(areaCodes, true, year, term);
            }
        }

        // 街道教研员情况，返回校级数据
        if (schoolIds.size() > 0) {
            List<RSSchoolKnowledgeStat> rsSchoolKnowledgeStats = rsSchoolKnowledgeStatDao.findBySchoolIds(schoolIds, year, term);
            if (CollectionUtils.isNotEmpty(rsSchoolKnowledgeStats)) {
                knowledgeDatas.addAll(ResearchStaffConvertor.convertSchoolStatListToResearchStaffKnowledgeData(rsSchoolKnowledgeStats));
                avgName = "街道平均水平";
                schoolCount += rsSchoolKnowledgeStatDao.countBySchoolIds(schoolIds, year, term);
                validSchoolCount += rsSchoolKnowledgeStatDao.countBySchoolIdsAndIsValid(schoolIds, true, year, term);
            }
        }

        if (knowledgeDatas.size() == 0) {
            return null;
        }

        ResearchStaffKnowledgeMapper researchStaffKnowledgeMapper = new ResearchStaffKnowledgeMapper();

        // 题型=>总做题量Map
        Map<String, Long> totalFinishCount = new HashMap<>();
        // 题型=>总做对题数Map
        Map<String, Double> totalCorrectCount = new HashMap<>();
        // 题型=>最大做题量
        Map<String, ResearchStaffKnowledgeDetailMapper> maxFinishCount = new HashMap<>();
        // 学生人数
        long studentNum = 0;
        // 平均数据
        ResearchStaffKnowledgeUnitMapper avgData = new ResearchStaffKnowledgeUnitMapper();
        avgData.setName(avgName);
        researchStaffKnowledgeMapper.getKnowledgeUnitList().add(avgData);

        // 填入每个知识技能具体数据
        for (ResearchStaffKnowledgeData knowledgeData : knowledgeDatas) {
            fillKnowledgeUnitMapperData(researchStaffKnowledgeMapper,
                    knowledgeData, maxFinishCount, totalFinishCount, totalCorrectCount);
            studentNum += knowledgeData.getStuNum();
        }

        // 剩余学生数总和
        researchStaffKnowledgeMapper.setTotalStudentCount(studentNum);

        // 计算平均水平
        calKnowledgeAvgStat(totalFinishCount, totalCorrectCount, studentNum, avgData, knowledgeDatas.size());

        // 统计学校数及有效学校数
        researchStaffKnowledgeMapper.setSchoolCount(schoolCount);
        researchStaffKnowledgeMapper.setValidSchoolCount(validSchoolCount);
        researchStaffKnowledgeMapper.setCountyCount(countyCount);

        return researchStaffKnowledgeMapper;
    }

    /**
     * 获得薄弱知识点技能数据(采用教研员管理区域架构)
     */
    public List<ResearchStaffWeakPointUnitMapper> getWeakPointData(Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {

        List<ResearchStaffWeakPointData> weakPointDatas = new ArrayList<>();

        // TODO 暂时不支持省教研员情况

        // 市教研员情况，返回区级数据
        if (cityCodes.size() > 0) {
            List<RSRegionWeakPointStat> rsRegionWeakPointStats = rsRegionWeakPointStatDao.findByCityCodes(cityCodes, year, term);
            weakPointDatas.addAll(ResearchStaffConvertor.convertRegionStatListToResearchStaffWeakPointData(rsRegionWeakPointStats));
        }

        // 区教研员情况，返回校级数据
        if (areaCodes.size() > 0) {
            List<RSSchoolWeakPointStat> rsSchoolWeakPointStats = rsSchoolWeakPointStatDao.findByAreaCodes(areaCodes, year, term);
            weakPointDatas.addAll(ResearchStaffConvertor.convertSchoolStatListToResearchStaffWeakPointData(rsSchoolWeakPointStats));
        }

        // 街道教研员情况，返回校级数据
        if (schoolIds.size() > 0) {
            List<RSSchoolWeakPointStat> rsSchoolWeakPointStats = rsSchoolWeakPointStatDao.findBySchoolIds(schoolIds, year, term);
            weakPointDatas.addAll(ResearchStaffConvertor.convertSchoolStatListToResearchStaffWeakPointData(rsSchoolWeakPointStats));
        }

        if (weakPointDatas.size() == 0) {
            return null;
        }

        List<ResearchStaffWeakPointUnitMapper> researchStaffWeakPointUnitMappers
                = new ArrayList<>();

        for (ResearchStaffWeakPointData weakPointData : weakPointDatas) {
            ResearchStaffWeakPointUnitMapper unitMapper = new ResearchStaffWeakPointUnitMapper();
            unitMapper.setName(weakPointData.getName());
            unitMapper.setWord(weakPointData.getWord());
            unitMapper.setTopic(weakPointData.getTopic());
            unitMapper.setGrammar(weakPointData.getGram());

            researchStaffWeakPointUnitMappers.add(unitMapper);
        }

        return researchStaffWeakPointUnitMappers;
    }

    /**
     * 获得单元薄弱知识点技能数据(采用教研员管理区域架构)
     */
    public ResearchStaffUnitWeakPointMapper getUnitWeakPointData(Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {

        List<ResearchStaffUnitWeakPointData> unitWeakPointDatas = new ArrayList<>();

        // TODO 暂时不支持省教研员情况

        // 市教研员情况，返回区级数据
        if (cityCodes.size() > 0) {
            List<RSRegionUnitWeakPointStat> rsRegionUnitWeakPointStats = rsRegionUnitWeakPointStatDao.findByCityCodes(cityCodes, year, term);
            unitWeakPointDatas.addAll(ResearchStaffConvertor
                    .convertRegionStatListToResearchStaffUnitWeakPointData(rsRegionUnitWeakPointStats));
        }

        // 区教研员情况，返回校级数据
        if (areaCodes.size() > 0) {
            List<RSSchoolUnitWeakPointStat> rsSchoolUnitWeakPointStats = rsSchoolUnitWeakPointStatDao.findByAreaCodes(areaCodes, year, term);
            unitWeakPointDatas.addAll(ResearchStaffConvertor
                    .convertSchoolStatListToResearchStaffUnitWeakPointData(rsSchoolUnitWeakPointStats));
        }

        // 街道教研员情况，返回校级数据
        if (schoolIds.size() > 0) {
            List<RSSchoolUnitWeakPointStat> rsSchoolUnitWeakPointStats = rsSchoolUnitWeakPointStatDao.findBySchoolIds(schoolIds, year, term);
            unitWeakPointDatas.addAll(ResearchStaffConvertor
                    .convertSchoolStatListToResearchStaffUnitWeakPointData(rsSchoolUnitWeakPointStats));
        }

        if (unitWeakPointDatas.size() == 0) {
            return null;
        }

        ResearchStaffUnitWeakPointMapper researchStaffUnitWeakPointMapper = new ResearchStaffUnitWeakPointMapper();
        Map<String, List<ResearchStaffUnitWeakPointUnitMapper>> regionWeakPointMap = new HashMap<>();

        // 统计教材出现次数并填充数据
        Map<String, Integer> bookCountMap = new HashMap<>();
        int totalBookCount = 0;
        for (ResearchStaffUnitWeakPointData unitWeakPointData : unitWeakPointDatas) {
            totalBookCount = fillUnitWeakPointUnitMapperData(regionWeakPointMap, bookCountMap, totalBookCount, unitWeakPointData.getName(), unitWeakPointData.getWeakPoint());
        }
        researchStaffUnitWeakPointMapper.setRegionWeakPointMap(regionWeakPointMap);

        // 生成样本教材
        calSelectedBooksForUnitWeakPoint(researchStaffUnitWeakPointMapper, bookCountMap, totalBookCount);

        return researchStaffUnitWeakPointMapper;
    }

    /**
     * 获取月单位语言技能数据(采用教研员管理区域架构)
     */
    public ResearchStaffSkillMonthlyMapper getSkillMonthlyData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {

        List<ResearchStaffSkillMonthlyData> skillMonthlyDatas = new ArrayList<>();

        // 省教研员情况，返回市级数据
        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            // TODO 先直接把所有区域数据加载进来进行统计，因为数据量少，而且以后可能需要区级数据，先这么着试试
            Set<Long> pCityCodes = getCityCodesForProvinceCodes(provinceCodes);
            List<RSRegionSkillMonthlyStat> rsRegionSkillMonthlyStats = rsRegionSkillMonthlyStatDao.findByCityCodes(pCityCodes, year, term);
            if (CollectionUtils.isNotEmpty(rsRegionSkillMonthlyStats)) {
                skillMonthlyDatas.addAll(ResearchStaffConvertor.convertToCityResearchStaffSkillMonthlyData(rsRegionSkillMonthlyStats));
            }
        }

        // 市教研员情况，返回区级数据
        if (cityCodes.size() > 0) {
            List<RSRegionSkillMonthlyStat> rsRegionSkillMonthlyStats = rsRegionSkillMonthlyStatDao.findByCityCodes(cityCodes, year, term);
            skillMonthlyDatas.addAll(ResearchStaffConvertor
                    .convertRegionStatListToResearchStaffSkillMonthlyData(rsRegionSkillMonthlyStats));
        }

        // 区教研员情况，返回校级数据
        if (areaCodes.size() > 0) {
            List<RSSchoolSkillMonthlyStat> rsSchoolSkillMonthlyStats = rsSchoolSkillMonthlyStatDao.findByAreaCodes(areaCodes, year, term);
            skillMonthlyDatas.addAll(ResearchStaffConvertor
                    .convertSchoolStatListToResearchStaffSkillMonthlyData(rsSchoolSkillMonthlyStats));
        }

        // 街道教研员情况，返回校级数据
        if (schoolIds.size() > 0) {
            List<RSSchoolSkillMonthlyStat> rsSchoolSkillMonthlyStats = rsSchoolSkillMonthlyStatDao.findBySchoolIds(schoolIds, year, term);
            skillMonthlyDatas.addAll(ResearchStaffConvertor
                    .convertSchoolStatListToResearchStaffSkillMonthlyData(rsSchoolSkillMonthlyStats));
        }

        if (skillMonthlyDatas.size() == 0) {
            return null;
        }

        ResearchStaffSkillMonthlyMapper researchStaffSkillMonthlyMapper = new ResearchStaffSkillMonthlyMapper();

        initializeSkillMonthlyMapperData(researchStaffSkillMonthlyMapper, year, term);

        for (ResearchStaffSkillMonthlyData skillMonthlyData : skillMonthlyDatas) {
            fillSkillMonthlyUnitMapperData(researchStaffSkillMonthlyMapper, skillMonthlyData.getName(), skillMonthlyData.getSkills());
        }

        return researchStaffSkillMonthlyMapper;
    }

    /**
     * 获得试卷分析报告数据
     */
    public List<RSPaperAnalysisReport> getPaperAnalysisReport(String paperId, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds) {

        List<RSPaperAnalysisReport> rsPaperAnalysisReports = new ArrayList<>();
        if (cityCodes.size() > 0) {
            rsPaperAnalysisReports.addAll(rsPaperAnalysisReportDao.findAreaDataByPaperAndCityCodes(paperId, cityCodes));
        }
        if (areaCodes.size() > 0) {
            rsPaperAnalysisReports.addAll(rsPaperAnalysisReportDao.findSchoolDataByPaperAndAreaCodes(paperId, areaCodes));
        }
        if (schoolIds.size() > 0) {
            rsPaperAnalysisReports.addAll(rsPaperAnalysisReportDao.findSchoolDataByPaperAndSchoolIds(paperId, schoolIds));
        }

        for (RSPaperAnalysisReport rsPaperAnalysisReport : rsPaperAnalysisReports) {
            if (rsPaperAnalysisReport.getWeakPoints() != null &&
                    rsPaperAnalysisReport.getWeakPoints().size() > 6) {
                rsPaperAnalysisReport.setWeakPoints(new ArrayList<>(rsPaperAnalysisReport.getWeakPoints().subList(0, 6)));
            }
        }

        return rsPaperAnalysisReports;
    }

    /**
     * 获得试卷分析报告数据
     */
    public List<RSPaperAnalysisReport> getPaperAnalysisReport(String paperId, Integer regionCode, RegionType regionType) {
        List<RSPaperAnalysisReport> rsPaperAnalysisReports = null;
        if (RegionType.CITY.equals(regionType)) {
            rsPaperAnalysisReports = rsPaperAnalysisReportDao.findAreaDataByPaperAndCityCode(paperId, regionCode);
        } else if (RegionType.COUNTY.equals(regionType)) {
            rsPaperAnalysisReports = rsPaperAnalysisReportDao.findSchoolDataByPaperAndAreaCode(paperId, regionCode);
        }
        if (rsPaperAnalysisReports != null) {
            for (RSPaperAnalysisReport rsPaperAnalysisReport : rsPaperAnalysisReports) {
                if (rsPaperAnalysisReport.getWeakPoints() != null &&
                        rsPaperAnalysisReport.getWeakPoints().size() > 6) {
                    rsPaperAnalysisReport.setWeakPoints(new ArrayList<>(rsPaperAnalysisReport.getWeakPoints().subList(0, 6)));
                }
            }
        } else {
            rsPaperAnalysisReports = Collections.emptyList();
        }
        return rsPaperAnalysisReports;
    }

    public List<RSOralPaperReportMapper> getOralAnalysisReport(Long pushId) {
        List<RSOralPaperReportMapper> results = new LinkedList<>();

        List<RSOralPaperAnalysisReport> reports = rsOralPaperAnalysisReportDao.findByPushId(
                pushId, null, Collections.singletonList("patterns.questions"));

        if (CollectionUtils.isNotEmpty(reports)) {
            for (RSOralPaperAnalysisReport report : reports) {

                RSOralPaperReportMapper reportMapper = new RSOralPaperReportMapper();

                reportMapper.setId(report.getId());
                reportMapper.setSchoolId(report.getSchoolId());
                reportMapper.setSchoolName(report.getSchoolName());
                reportMapper.setPushId(report.getPushId());
                reportMapper.setStudentCount(report.getStudentCount());
                reportMapper.setTotalCount(report.getTotalCount());
                reportMapper.setAvgMScore(0d);
                reportMapper.setAvgTScore(0d);

                Map<String, RSOralPaperReportPatternMapper> patternMappers = new LinkedHashMap<>();
                Map<String, RSOralPaperAnalysisReportPattern> patterns = report.getPatterns();

                if (patterns != null) {
                    patterns.forEach((pstr, pattern) -> {
                        RSOralPaperReportPatternMapper patternMapper = new RSOralPaperReportPatternMapper();

                        patternMapper.setPattern(pstr);
                        patternMapper.setAvgMScore(divide(pattern.getMachineScore(), report.getStudentCount()));
                        patternMapper.setAvgTScore(divide(pattern.getTeacherScore(), report.getStudentCount()));

                        patternMappers.put(pstr, patternMapper);

                        // 总分平均分
                        reportMapper.setAvgMScore(reportMapper.getAvgMScore() + patternMapper.getAvgMScore());
                        reportMapper.setAvgTScore(reportMapper.getAvgTScore() + patternMapper.getAvgTScore());
                    });
                }

                reportMapper.setPatterns(patternMappers);

                results.add(reportMapper);
            }

        }

        return results;
    }

    /**
     * 获取行为数据(采用教研员管理区域架构)
     * 目前只支持获取市级/区级/街道，最多二层结构
     * 若要返回更灵活的样式需重新设计返回对象结构
     */
    public List<ResearchStaffBehaviorDataMapper> getBehaviorData(Long rstaffId, Subject subject, Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {
        if (rstaffId == null) return Collections.emptyList();

        Set<String> paperIds = new HashSet<>();
        if (subject == Subject.ENGLISH || subject == Subject.MATH) {
            // 获得user-book-paper关系
            //系统卷
            List<NewPaper> newPapers = paperLoaderClient.loadPaperAsListByUserIdSubjectId(rstaffId, subject.getId());
            newPapers.forEach(e -> paperIds.add(e.getId()));
        }

        return getBehaviorData(subject, paperIds, provinceCodes, cityCodes, areaCodes, schoolIds, year, term);
    }

    private List<ResearchStaffBehaviorDataMapper> getBehaviorData(Subject subject, Set<String> paperIds, Collection<Long> provinceCodes,
                                                                  Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {

        // fetch behavior data
        List<AbstractRSSchoolHomeworkBehaviorStat> rsSchoolBehaviorDatas = new ArrayList<>();
        List<AbstractRSAreaHomeworkBehaviorStat> rsAreaBehaviorDatas = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            if (subject == Subject.ENGLISH) {
                List<RSEnglishAreaHomeworkBehaviorStat> rsEngHomeworkBehaviorStats = rsEnglishAreaHomeworkBehaviorStatDao.findByProvinceCodes(provinceCodes, year, term);
                rsAreaBehaviorDatas.addAll(rsEngHomeworkBehaviorStats);
            } else if (subject == Subject.MATH) {
                List<RSMathAreaHomeworkBehaviorStat> rsMathHomeworkBehaviorStats = rsMathAreaHomeworkBehaviorStatDao.findByProvinceCodes(provinceCodes, year, term);
                rsAreaBehaviorDatas.addAll(rsMathHomeworkBehaviorStats);
            }
        }

        if (CollectionUtils.isNotEmpty(cityCodes)) {
            if (subject == Subject.ENGLISH) {
                List<RSEnglishSchoolHomeworkBehaviorStat> rsEngSchoolHomeworkBehaviorStats = rsEnglishSchoolHomeworkBehaviorStatDao.findSchoolDataByCityCodes_withoutIdSet(cityCodes, year, term);
                rsSchoolBehaviorDatas.addAll(rsEngSchoolHomeworkBehaviorStats);
            } else if (subject == Subject.MATH) {
                List<RSMathSchoolHomeworkBehaviorStat> rsMathHomeworkBehaviorStats = rsMathSchoolHomeworkBehaviorStatDao.findSchoolDataByCityCodes_withoutIdSet(cityCodes, year, term);
                rsSchoolBehaviorDatas.addAll(rsMathHomeworkBehaviorStats);
            }
        }

        if (CollectionUtils.isNotEmpty(areaCodes)) {
            if (subject == Subject.ENGLISH) {
                List<RSEnglishSchoolHomeworkBehaviorStat> rsEngSchoolHomeworkBehaviorStats = rsEnglishSchoolHomeworkBehaviorStatDao.findSchoolDataByAreaCodes_withoutIdSet(areaCodes, year, term);
                rsSchoolBehaviorDatas.addAll(rsEngSchoolHomeworkBehaviorStats);
            } else if (subject == Subject.MATH) {
                List<RSMathSchoolHomeworkBehaviorStat> rsMathHomeworkBehaviorStats = rsMathSchoolHomeworkBehaviorStatDao.findSchoolDataByAreaCodes_withoutIdSet(areaCodes, year, term);
                rsSchoolBehaviorDatas.addAll(rsMathHomeworkBehaviorStats);
            }
        }

        if (CollectionUtils.isNotEmpty(schoolIds)) {
            List<String> ids = new ArrayList<>();
            for (Long schoolId : schoolIds) {
                ids.add(schoolId.toString());
            }
            if (subject == Subject.ENGLISH) {
                List<RSEnglishSchoolHomeworkBehaviorStat> rsEngSchoolHomeworkBehaviorStats = rsEnglishSchoolHomeworkBehaviorStatDao.findBySchoolIds_withoutIdSet(ids, year, term);
                rsSchoolBehaviorDatas.addAll(rsEngSchoolHomeworkBehaviorStats);
            } else if (subject == Subject.MATH) {
                List<RSMathSchoolHomeworkBehaviorStat> rsMathHomeworkBehaviorStats = rsMathSchoolHomeworkBehaviorStatDao.findBySchoolIds_withoutIdSet(ids, year, term);
                rsSchoolBehaviorDatas.addAll(rsMathHomeworkBehaviorStats);
            }
        }

        // 统考统计数据结构初始化
        Map<Long, Set<Long>> codeTeacherNumMap = new HashMap<>();
        Map<Long, Set<Long>> codeStuNumMap = new HashMap<>();
        Map<Long, Integer> codeTeacherTimeMap = new HashMap<>();
        Map<Long, Integer> codeStuTimeMap = new HashMap<>();

        // 统考数据汇总，目前只有英语有统考数据  update 也有数学统考数据了
        if (subject == Subject.ENGLISH || subject == Subject.MATH) {
            SchoolYear schoolYear = SchoolYear.newInstance();
            DateRange dateRange = schoolYear.currentTermDateRange();

            Set<Long> allSchoolIds = new HashSet<>();
            for (AbstractRSSchoolHomeworkBehaviorStat researchStaffBehaviorData : rsSchoolBehaviorDatas) {
                allSchoolIds.add(Long.valueOf(researchStaffBehaviorData.getSchoolId()));
            }

            // TODO 当省教研员开放组卷的时候，这里的schoolId需要把省下的school id列出来，从而过滤掉非该省试卷
//            countExamBehaviorData(subject, paperIds, dateRange, allSchoolIds, codeTeacherNumMap, codeStuNumMap, codeTeacherTimeMap, codeStuTimeMap);
        }

        List<ResearchStaffBehaviorDataMapper> ret = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            fillBehaviorResultInCityUnit(subject, rsAreaBehaviorDatas, ret);
        }

        if (CollectionUtils.isNotEmpty(cityCodes)) {
            fillBehaviorResultInAreaUnit(subject, rsSchoolBehaviorDatas, ret);
        }

        if (CollectionUtils.isNotEmpty(areaCodes)) {
            fillBehaviorResultInAreaUnit(subject, rsSchoolBehaviorDatas, ret);
        }

        if (CollectionUtils.isNotEmpty(schoolIds)) {
            fillBehaviorResultInSchoolUnit(subject, rsSchoolBehaviorDatas, ret);
        }

        return ret;
    }

    /**
     * ***********************************************以下是私有方法***************************************************
     */

    private void fillBehaviorResultInCityUnit(Subject subject, List<AbstractRSAreaHomeworkBehaviorStat> rsBehaviorDatas, List<ResearchStaffBehaviorDataMapper> ret) {
        Map<Long, ResearchStaffBehaviorDataMapper> cityMap = new HashMap<>();
        for (AbstractRSAreaHomeworkBehaviorStat rsBehaviorData : rsBehaviorDatas) {
            Long ccode = rsBehaviorData.getCcode();
            ResearchStaffBehaviorDataMapper cityMapper = cityMap.get(ccode);
            if (cityMapper == null) {
                cityMapper = new ResearchStaffBehaviorDataMapper();
                cityMapper.setName(rsBehaviorData.getCityName());
                cityMapper.setChildBehaviorData(new ArrayList<>());
                cityMap.put(ccode, cityMapper);
            }

            if (subject == Subject.ENGLISH || subject == Subject.MATH) {
                ResearchStaffBehaviorDataMapper childMapper = generateBehaviorDataMapperForCounty(
                        rsBehaviorData);
                cityMapper.getChildBehaviorData().add(childMapper);
            }

            cityMapper.setSchoolNum(cityMapper.getSchoolNum() + rsBehaviorData.getSchoolNum());
            cityMapper.setHomeworkStuNum(cityMapper.getHomeworkStuNum() + rsBehaviorData.getStuNum());
            cityMapper.setHomeworkStuTime(cityMapper.getHomeworkStuTime() + rsBehaviorData.getStuTimes());
            cityMapper.setHomeworkTeacherNum(cityMapper.getHomeworkTeacherNum() + rsBehaviorData.getTeacherNum());
            cityMapper.setHomeworkTeacherTime(cityMapper.getHomeworkTeacherTime() + rsBehaviorData.getTeacherTimes());

            // TODO 设置省教研员的统考数据
        }

        for (ResearchStaffBehaviorDataMapper mapper : cityMap.values()) {
            if (subject == Subject.MATH || CollectionUtils.isNotEmpty(mapper.getChildBehaviorData())) {
                ret.add(mapper);
            }
        }
    }

    private void fillBehaviorResultInAreaUnit(Subject subject, List<AbstractRSSchoolHomeworkBehaviorStat> rsBehaviorDatas, List<ResearchStaffBehaviorDataMapper> ret) {
        Map<Long, ResearchStaffBehaviorDataMapper> map = new HashMap<>();
        for (AbstractRSSchoolHomeworkBehaviorStat rsBehaviorData : rsBehaviorDatas) {
            Long acode = rsBehaviorData.getAcode();
            ResearchStaffBehaviorDataMapper areaMapper = map.get(acode);
            if (areaMapper == null) {
                areaMapper = new ResearchStaffBehaviorDataMapper();
                areaMapper.setName(rsBehaviorData.getAreaName());
                areaMapper.setChildBehaviorData(new ArrayList<>());
                map.put(acode, areaMapper);
            }

            if (subject == Subject.ENGLISH || subject == Subject.MATH) {
                ResearchStaffBehaviorDataMapper childMapper = new ResearchStaffBehaviorDataMapper();

                childMapper.setName(rsBehaviorData.getSchoolName());
                childMapper.setHomeworkStuNum(SafeConverter.toInt(rsBehaviorData.getStuNum()));
                childMapper.setHomeworkStuTime(SafeConverter.toLong(rsBehaviorData.getStuTimes()));
                childMapper.setHomeworkTeacherNum(SafeConverter.toInt(rsBehaviorData.getTeacherNum()));
                childMapper.setHomeworkTeacherTime(SafeConverter.toLong(rsBehaviorData.getTeacherTimes()));


                areaMapper.getChildBehaviorData().add(childMapper);
            }

            summarizeBehaviorData(rsBehaviorData, areaMapper);

        }

        for (ResearchStaffBehaviorDataMapper mapper : map.values()) {
            if (subject == Subject.MATH || CollectionUtils.isNotEmpty(mapper.getChildBehaviorData())) {
                ret.add(mapper);
            }
        }
    }

    private void fillBehaviorResultInSchoolUnit(Subject subject, List<AbstractRSSchoolHomeworkBehaviorStat> rsBehaviorDatas, List<ResearchStaffBehaviorDataMapper> ret) {
        ResearchStaffBehaviorDataMapper mapper = new ResearchStaffBehaviorDataMapper();

        List<ResearchStaffBehaviorDataMapper> childMappers = new ArrayList<>();
        for (AbstractRSSchoolHomeworkBehaviorStat rsBehaviorData : rsBehaviorDatas) {

            ResearchStaffBehaviorDataMapper childMapper = new ResearchStaffBehaviorDataMapper();

            childMapper.setName(rsBehaviorData.getSchoolName());
            childMapper.setHomeworkStuNum(SafeConverter.toInt(rsBehaviorData.getStuNum()));
            childMapper.setHomeworkStuTime(SafeConverter.toLong(rsBehaviorData.getStuTimes()));
            childMapper.setHomeworkTeacherNum(SafeConverter.toInt(rsBehaviorData.getTeacherNum()));
            childMapper.setHomeworkTeacherTime(SafeConverter.toLong(rsBehaviorData.getTeacherTimes()));


            if (subject == Subject.ENGLISH || subject == Subject.MATH) {
                childMappers.add(childMapper);
            }

            summarizeBehaviorData(rsBehaviorData, mapper);
        }

        if (childMappers.size() > 0) {
            mapper.setName("");
            mapper.setChildBehaviorData(childMappers);
            ret.add(mapper);
        }
    }

    private void summarizeBehaviorData(AbstractRSSchoolHomeworkBehaviorStat rsBehaviorData, ResearchStaffBehaviorDataMapper mapper) {
        mapper.setHomeworkStuNum(mapper.getHomeworkStuNum() + rsBehaviorData.getStuNum());
        mapper.setHomeworkStuTime(mapper.getHomeworkStuTime() + rsBehaviorData.getStuTimes());
        mapper.setHomeworkTeacherNum(mapper.getHomeworkTeacherNum() + rsBehaviorData.getTeacherNum());
        mapper.setHomeworkTeacherTime(mapper.getHomeworkTeacherTime() + rsBehaviorData.getTeacherTimes());
        mapper.setSchoolNum(mapper.getSchoolNum() + 1);
    }

    /**
     * 生成以区为单元的行为数据mapper
     * 这里实际上不会生成统考信息，因为以区为单元仅供省教研员使用，目前无组卷功能，等有的时候再生成吧
     */
    private ResearchStaffBehaviorDataMapper generateBehaviorDataMapperForCounty(AbstractRSAreaHomeworkBehaviorStat rsAreaBehaviorData) {
        ResearchStaffBehaviorDataMapper childMapper = new ResearchStaffBehaviorDataMapper();

        childMapper.setName(rsAreaBehaviorData.getAreaName());
        // 一个trick，用school name存储的学校个数
        childMapper.setSchoolNum(SafeConverter.toInt(rsAreaBehaviorData.getSchoolNum()));
        childMapper.setHomeworkStuNum(SafeConverter.toInt(rsAreaBehaviorData.getStuNum()));
        childMapper.setHomeworkStuTime(SafeConverter.toLong(rsAreaBehaviorData.getStuTimes()));
        childMapper.setHomeworkTeacherNum(SafeConverter.toInt(rsAreaBehaviorData.getTeacherNum()));
        childMapper.setHomeworkTeacherTime(SafeConverter.toLong(rsAreaBehaviorData.getTeacherTimes()));

        //TODO 这里需要设置统考数据，目前省教研员还没有这一功能

        return childMapper;
    }

    /**
     * 获取省id下的所有市id
     */
    private Set<Long> getCityCodesForProvinceCodes(Collection<Long> provinceCodes) {
        Set<Long> pCityCodes = new HashSet<>();
        for (Long provinceCode : provinceCodes) {
            Collection<ExRegion> exRegions = raikouSystem.getRegionBuffer().loadChildRegions(provinceCode.intValue());
            for (ExRegion exRegion : exRegions) {
                pCityCodes.add(Long.valueOf(exRegion.getCode()));
            }
        }
        return pCityCodes;
    }

    /**
     * 获取所有学校id，包括区域下学校和直属学校
     */
    private Set<Long> getAllSchoolIds(Collection<Long> schoolIds, Set<Integer> allAreaCodes) {
        Set<Long> allSchoolIds = new HashSet<>();
        if (schoolIds.size() > 0) {
            allSchoolIds.addAll(schoolIds);
        }

        raikouSystem.querySchoolLocations(allAreaCodes)
                .enabled()
                .asList()
                .stream()
                .map(School.Location::getId)
                .forEach(allSchoolIds::add);

        return allSchoolIds;
    }

    /**
     * 获取所有区域code，包括市下属区和直属区
     */
    private Set<Integer> getAllAreaCodes(Collection<Long> cityCodes, Collection<Long> areaCodes) {
        Set<Integer> allAreaCodes = new HashSet<>();
        if (cityCodes.size() > 0) {
            for (Long code : cityCodes) {
                List<Integer> codeList = raikouSystem.getRegionBuffer()
                        .loadChildRegions(code.intValue())
                        .stream()
                        .map(ExRegion::getCode)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                allAreaCodes.addAll(codeList);
            }
        }
        if (areaCodes.size() > 0) {
            allAreaCodes.addAll(areaCodes.stream().map(Long::intValue).collect(Collectors.toList()));
        }
        return allAreaCodes;
    }

    /**
     * 统计校级历史报告数据
     */
    private void countSchoolHistoryReportData(Long id, Long teacherId, List<Long> studentIds, Map<Long, Set<Long>> codeTeacherNumMap,
                                              Map<Long, Set<Long>> codeStuNumMap, Map<Long, Integer> codeTeacherTimeMap, Map<Long, Integer> codeStuTimeMap) {
        // 统计老师人数
        Set<Long> teacherSet = codeTeacherNumMap.get(id);
        if (teacherSet == null) {
            teacherSet = new HashSet<>();
            codeTeacherNumMap.put(id, teacherSet);
        }
        teacherSet.add(teacherId);

        // 统计学生人数
        Set<Long> stuSet = codeStuNumMap.get(id);
        if (stuSet == null) {
            stuSet = new HashSet<>();
            codeStuNumMap.put(id, stuSet);
        }
        if (studentIds != null) {
            stuSet.addAll(studentIds);
        }

        // 统计老师人次
        Integer teacherTime = codeTeacherTimeMap.get(id);
        if (teacherTime == null) {
            teacherTime = 0;
        }
        codeTeacherTimeMap.put(id, teacherTime + 1);

        // 统计学生人次
        Integer stuTime = codeStuTimeMap.get(id);
        if (stuTime == null) {
            stuTime = 0;
        }
        if (studentIds != null) {
            stuTime += studentIds.size();
        }
        codeStuTimeMap.put(id, stuTime);
    }

    /**
     * 初始化按月技能返回Mapper数据结构
     */
    private void initializeSkillMonthlyMapperData(ResearchStaffSkillMonthlyMapper researchStaffSkillMonthlyMapper, Integer year, Term term) {
        ResearchStaffSkillMonthlyUnitMapper listening = new ResearchStaffSkillMonthlyUnitMapper();
        ResearchStaffSkillMonthlyUnitMapper speaking = new ResearchStaffSkillMonthlyUnitMapper();
        ResearchStaffSkillMonthlyUnitMapper reading = new ResearchStaffSkillMonthlyUnitMapper();
        ResearchStaffSkillMonthlyUnitMapper written = new ResearchStaffSkillMonthlyUnitMapper();

        researchStaffSkillMonthlyMapper.setListening(listening);
        researchStaffSkillMonthlyMapper.setSpeaking(speaking);
        researchStaffSkillMonthlyMapper.setReading(reading);
        researchStaffSkillMonthlyMapper.setWritten(written);

        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM");
        if (year == null || term == null) {
            // FIXME 学年按8月10日分隔，所以这里需要特殊处理，8月10日至8月31日统一为之前学期
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            if (calendar.get(Calendar.MONTH) == Calendar.AUGUST && calendar.get(Calendar.DAY_OF_MONTH) >= 10) {
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                currentDate = calendar.getTime();
            }
            SchoolYear schoolYear = SchoolYear.newInstance(currentDate);
            DateRange dateRange = schoolYear.currentTermDateRange();
            startCalendar.setTime(dateRange.getStartDate());
            Date endDate = new Date();
            endCalendar.setTime(endDate);
            endCalendar.add(Calendar.MONTH, 1);
            endCalendar.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            SchoolYear schoolYear = SchoolYear.newInstance(year);
            DateRange dateRange = schoolYear.getDateRangeByTerm(term);
            startCalendar.setTime(dateRange.getStartDate());
            endCalendar.setTime(dateRange.getEndDate());
        }

        while (startCalendar.before(endCalendar)) {
            String m = formatter.format(startCalendar.getTime());

            listening.getMonthlyRate().put(m, new ArrayList<>());
            speaking.getMonthlyRate().put(m, new ArrayList<>());
            reading.getMonthlyRate().put(m, new ArrayList<>());
            written.getMonthlyRate().put(m, new ArrayList<>());

            listening.getMonthlySum().put(m, new ArrayList<>());
            speaking.getMonthlySum().put(m, new ArrayList<>());
            reading.getMonthlySum().put(m, new ArrayList<>());
            written.getMonthlySum().put(m, new ArrayList<>());

            startCalendar.add(Calendar.MONTH, 1);
        }
    }

    /**
     * 填充按月技能单位数据
     */
    private void fillSkillMonthlyUnitMapperData(ResearchStaffSkillMonthlyMapper researchStaffSkillMonthlyMapper, String name, List<RSSkillMonthlyData> rsSkillMonthlyDatas) {
        for (RSSkillMonthlyData rsSkillMonthlyData : rsSkillMonthlyDatas) {
            String type = rsSkillMonthlyData.getType();

            List<RSSkillMonthlyUnitData> rsSkillMonthlyUnitDatas = rsSkillMonthlyData.getMonthList();

            ResearchStaffSkillMonthlyUnitMapper unitMapper = getSkillMonthlyUnitMapperByType(type, researchStaffSkillMonthlyMapper);

            unitMapper.getNames().add(name);
            for (Map.Entry<String, List<Integer>> entry : unitMapper.getMonthlySum().entrySet()) {
                entry.getValue().add(0);
            }
            for (Map.Entry<String, List<Double>> entry : unitMapper.getMonthlyRate().entrySet()) {
                entry.getValue().add(0d);
            }

            for (RSSkillMonthlyUnitData rsSkillMonthlyUnitData : rsSkillMonthlyUnitDatas) {

                if (!unitMapper.getNames().contains(name)) {
                    unitMapper.getNames().add(name);
                }

                String month = rsSkillMonthlyUnitData.getMonth();
                month = month.substring(0, 4) + "-" + month.substring(4);

                List<Integer> sumList = unitMapper.getMonthlySum().get(month);
                if (CollectionUtils.isNotEmpty(sumList)) {
                    sumList.set(sumList.size() - 1, rsSkillMonthlyUnitData.getNum());
                }
                List<Double> rateList = unitMapper.getMonthlyRate().get(month);
                if (CollectionUtils.isNotEmpty(rateList)) {
                    rateList.set(rateList.size() - 1, 100 * rsSkillMonthlyUnitData.getRate());
                }
            }
        }
    }

    /**
     * 根据技能类型获取月单位语言技能mapper对象
     */
    private ResearchStaffSkillMonthlyUnitMapper getSkillMonthlyUnitMapperByType(String type, ResearchStaffSkillMonthlyMapper researchStaffSkillMonthlyMapper) {
        if ("听".equals(type)) {
            return researchStaffSkillMonthlyMapper.getListening();
        } else if ("说".equals(type)) {
            return researchStaffSkillMonthlyMapper.getSpeaking();
        } else if ("读".equals(type)) {
            return researchStaffSkillMonthlyMapper.getReading();
        } else if ("写".equals(type)) {
            return researchStaffSkillMonthlyMapper.getWritten();
        }
        return null;
    }

    /**
     * 填充单元薄弱具体数据
     */
    private int fillUnitWeakPointUnitMapperData(Map<String, List<ResearchStaffUnitWeakPointUnitMapper>> regionWeakPointMap, Map<String, Integer> bookCountMap, int totalBookCount, String name, List<RSLevelData> rsLevelDatas) {
        List<ResearchStaffUnitWeakPointUnitMapper> weakPointList = new ArrayList<>();
        for (RSLevelData rsLevelData : rsLevelDatas) {
            ResearchStaffUnitWeakPointUnitMapper unitMapper = new ResearchStaffUnitWeakPointUnitMapper();

            List<RSBookData> rsBookDatas = rsLevelData.getBooks();

            for (RSBookData rsBookData : rsBookDatas) {
                ResearchStaffUnitWeakPointBookMapper bookMapper = new ResearchStaffUnitWeakPointBookMapper();

                String bookName = rsBookData.getPress();

                bookMapper.setPress(rsBookData.getPress());

                // 2015.1.26 changyuan.liu 合并各单元薄弱知识点
                // bookMapper.setWeakPointTags(rsBookData.getTags());
                Set<String> weakPointTags = new HashSet<>();
                for (String tag : rsBookData.getTags()) {
                    String[] tags = StringUtils.split(tag, ",");
                    Collections.addAll(weakPointTags, tags);
                }
                bookMapper.setWeakPoints(StringUtils.join(weakPointTags, ","));

                unitMapper.getBookList().add(bookMapper);

                totalBookCount++;
                if (bookCountMap.containsKey(bookName)) {
                    bookCountMap.put(bookName, bookCountMap.get(bookName) + 1);
                } else {
                    bookCountMap.put(bookName, 1);
                }
            }

            unitMapper.setClazzLevel(rsLevelData.getClassLevel());

            weakPointList.add(unitMapper);
        }

        regionWeakPointMap.put(name, weakPointList);
        return totalBookCount;
    }

    /**
     * 生成样本教材
     */
    private void calSelectedBooksForUnitWeakPoint(ResearchStaffUnitWeakPointMapper researchStaffUnitWeakPointMapper, Map<String, Integer> bookCountMap, int totalBookCount) {
        for (Map.Entry<String, Integer> entry : bookCountMap.entrySet()) {
            if ((double) entry.getValue() / totalBookCount > 0.3) {
                researchStaffUnitWeakPointMapper.getSelectedPresses().add(entry.getKey());
                if (researchStaffUnitWeakPointMapper.getSelectedPresses().size() > 2) {
                    break;
                }
            }
        }
        // bootCountMap size=1表示只有一本教材被使用
        researchStaffUnitWeakPointMapper.setSamePressFlag(bookCountMap.size() == 1);
    }

    /**
     * 计算语言知识平均数据
     */
    private void calKnowledgeAvgStat(Map<String, Long> totalFinishCount, Map<String, Double> totalCorrectCount, long studentNum, ResearchStaffKnowledgeUnitMapper avgData, long total) {
        ResearchStaffKnowledgeDetailMapper avgWordMapper = new ResearchStaffKnowledgeDetailMapper();
        long finishCount = totalFinishCount.get("word");
        double correctRate = totalCorrectCount.get("word");
        avgWordMapper.setFinishCount((int) (finishCount / total + 0.5));// 四舍五入功能
//        avgWordMapper.setCorrectRate(correctRate / finishCount);
        avgWordMapper.setCorrectRate(divide(correctRate, finishCount));
        avgWordMapper.setCountPerStudent((int) (finishCount / studentNum + 0.5));
        avgData.setWordDetail(avgWordMapper);

        ResearchStaffKnowledgeDetailMapper avgTopicMapper = new ResearchStaffKnowledgeDetailMapper();
        finishCount = totalFinishCount.get("topic");
        correctRate = totalCorrectCount.get("topic");
        avgTopicMapper.setFinishCount((int) (finishCount / total + 0.5));// 四舍五入功能
//        avgTopicMapper.setCorrectRate(correctRate / finishCount);
        avgTopicMapper.setCorrectRate(divide(correctRate, finishCount));
        avgTopicMapper.setCountPerStudent((int) (finishCount / studentNum + 0.5));
        avgData.setTopicDetail(avgTopicMapper);

        ResearchStaffKnowledgeDetailMapper avgGramMapper = new ResearchStaffKnowledgeDetailMapper();
        finishCount = totalFinishCount.get("gram");
        correctRate = totalCorrectCount.get("gram");
        avgGramMapper.setFinishCount((int) (finishCount / total + 0.5));// 四舍五入功能
//        avgGramMapper.setCorrectRate(correctRate / finishCount);
        avgGramMapper.setCorrectRate(divide(correctRate, finishCount));
        avgGramMapper.setCountPerStudent((int) (finishCount / studentNum + 0.5));
        avgData.setGramDetail(avgGramMapper);

        avgData.setStudentCount((int) (studentNum / total + 0.5));
    }

    /**
     * 填充语言知识数据
     */
    private void fillKnowledgeUnitMapperData(ResearchStaffKnowledgeMapper researchStaffKnowledgeMapper,
                                             ResearchStaffKnowledgeData knowledgeData,
                                             Map<String, ResearchStaffKnowledgeDetailMapper> maxFinishCount,
                                             Map<String, Long> totalFinishCount, Map<String, Double> totalCorrectCount) {
        ResearchStaffKnowledgeUnitMapper researchStaffKnowledgeUnitMapper
                = new ResearchStaffKnowledgeUnitMapper();
        researchStaffKnowledgeUnitMapper.setName(knowledgeData.getName());
        researchStaffKnowledgeUnitMapper.setStudentCount(knowledgeData.getStuNum());

        ResearchStaffKnowledgeDetailMapper wordDetail = new ResearchStaffKnowledgeDetailMapper();
        wordDetail.setFinishCount(knowledgeData.getWordNum());
        wordDetail.setCorrectRate(knowledgeData.getWordRate() * 100);
        wordDetail.setCountPerStudent(knowledgeData.getWordPAvg());
        researchStaffKnowledgeUnitMapper.setWordDetail(wordDetail);

        ResearchStaffKnowledgeDetailMapper topicDetail = new ResearchStaffKnowledgeDetailMapper();
        topicDetail.setFinishCount(knowledgeData.getTopicNum());
        topicDetail.setCorrectRate(knowledgeData.getTopicRate() * 100);
        topicDetail.setCountPerStudent(knowledgeData.getTopicPAvg());
        researchStaffKnowledgeUnitMapper.setTopicDetail(topicDetail);

        ResearchStaffKnowledgeDetailMapper gramDetail = new ResearchStaffKnowledgeDetailMapper();
        gramDetail.setFinishCount(knowledgeData.getGramNum());
        gramDetail.setCorrectRate(knowledgeData.getGramRate() * 100);
        gramDetail.setCountPerStudent(knowledgeData.getGramPAvg());
        researchStaffKnowledgeUnitMapper.setGramDetail(gramDetail);

        // 统计做题总量，用于排序及平均做题数
        calIntoTotalFinishCount(totalFinishCount, "word", wordDetail.getFinishCount());
        calIntoTotalFinishCount(totalFinishCount, "topic", topicDetail.getFinishCount());
        calIntoTotalFinishCount(totalFinishCount, "gram", gramDetail.getFinishCount());
        // 统计正确题数，用于求平均正确率
        calIntoTotalCorrectNum(totalCorrectCount, "word", wordDetail.getFinishCount(), wordDetail.getCorrectRate());
        calIntoTotalCorrectNum(totalCorrectCount, "topic", topicDetail.getFinishCount(), topicDetail.getCorrectRate());
        calIntoTotalCorrectNum(totalCorrectCount, "gram", gramDetail.getFinishCount(), gramDetail.getCorrectRate());
        // 计算最大值
        calMaxRSKnowledgeData(maxFinishCount, "word", wordDetail);
        calMaxRSKnowledgeData(maxFinishCount, "topic", topicDetail);
        calMaxRSKnowledgeData(maxFinishCount, "gram", gramDetail);
        // 更新最高学生数
        if (knowledgeData.getStuNum() > researchStaffKnowledgeMapper.getMaxStudentCount()) {
            researchStaffKnowledgeMapper.setMaxStudentCount(knowledgeData.getStuNum());
        }

        researchStaffKnowledgeMapper.getKnowledgeUnitList().add(researchStaffKnowledgeUnitMapper);
    }

    /**
     * 计算做题数最多的语言知识数据
     */
    private void calMaxRSKnowledgeData(Map<String, ResearchStaffKnowledgeDetailMapper> maxFinishCount, String type, ResearchStaffKnowledgeDetailMapper detailMapper) {
        ResearchStaffKnowledgeDetailMapper maxMapper = maxFinishCount.get(type);
        if (maxMapper != null) {
            if (maxMapper.getFinishCount() < detailMapper.getFinishCount()) {
                detailMapper.setRank(1);
                maxMapper.setRank(0);
                maxFinishCount.put(type, detailMapper);
            }
        } else {
            detailMapper.setRank(1);
            maxFinishCount.put(type, detailMapper);
        }
    }

    /**
     * 根据类型set指定知识技能数据
     */
    private void setSkillDetailByType(ResearchStaffSkillUnitMapper researchStaffSkillUnitMapper,
                                      ResearchStaffSkillDetailMapper skillDetail, String type) {
        if ("听".equals(type)) {
            researchStaffSkillUnitMapper.setListening(skillDetail);
        } else if ("说".equals(type)) {
            researchStaffSkillUnitMapper.setSpeaking(skillDetail);
        } else if ("读".equals(type)) {
            researchStaffSkillUnitMapper.setReading(skillDetail);
        } else if ("写".equals(type)) {
            researchStaffSkillUnitMapper.setWritten(skillDetail);
        }
    }

    /**
     * 计算指定技能下做题数最多的知识技能数据
     */
    private void calMaxRSSkillData(Map<String, ResearchStaffSkillDetailMapper> maxFinishCount, RSSkillData rsSkillData, ResearchStaffSkillDetailMapper detailMapper) {
        ResearchStaffSkillDetailMapper maxMapper = maxFinishCount.get(rsSkillData.getType());
        if (maxMapper != null) {
            if (maxMapper.getFinishCount() < detailMapper.getFinishCount()) {
                detailMapper.setRank(1);
                maxMapper.setRank(0);
                maxFinishCount.put(rsSkillData.getType(), detailMapper);
            }
        } else {
            detailMapper.setRank(1);
            maxFinishCount.put(rsSkillData.getType(), detailMapper);
        }
    }

    /**
     * 计算平均数据
     */
    private void calSkillAvgStat(Map<String, Long> totalFinishCount, Map<String, Double> totalCorrectCount, long studentNum, ResearchStaffSkillUnitMapper avgData, int total) {
        for (Map.Entry<String, Long> entry : totalFinishCount.entrySet()) {
            String pattern = entry.getKey();

            ResearchStaffSkillDetailMapper detailMapper
                    = new ResearchStaffSkillDetailMapper();
            detailMapper.setPatternType(pattern);
            detailMapper.setFinishCount((int) (entry.getValue() / total + 0.5));// 四舍五入功能
            detailMapper.setCorrectRate(totalCorrectCount.get(pattern) / entry.getValue());
            detailMapper.setCountPerStudent((int) (entry.getValue() / studentNum + 0.5));

            setSkillDetailByType(avgData, detailMapper, detailMapper.getPatternType());
        }
        avgData.setStudentCount((int) (studentNum / total + 0.5));
    }

    /**
     * 填充具体区/学校知识技能数据
     */
    private void fillSkillUnitMapperData(ResearchStaffSkillMapper researchStaffSkillMapper, Map<String, Long> totalFinishCount, Map<String, Double> totalCorrectCount, Map<String, ResearchStaffSkillDetailMapper> maxFinishCount, String name, Long studentCount, List<RSSkillData> rsSkillDatas) {
        ResearchStaffSkillUnitMapper researchStaffSkillUnitMapper
                = new ResearchStaffSkillUnitMapper();
        researchStaffSkillUnitMapper.setName(name);
        researchStaffSkillUnitMapper.setStudentCount(studentCount);
        for (RSSkillData rsSkillData : rsSkillDatas) {
            ResearchStaffSkillDetailMapper detailMapper
                    = new ResearchStaffSkillDetailMapper();
            detailMapper.setPatternType(rsSkillData.getType());
            detailMapper.setFinishCount(rsSkillData.getNum());
            detailMapper.setCorrectRate(rsSkillData.getRate() * 100);
            detailMapper.setCountPerStudent(rsSkillData.getPavg());
            detailMapper.setRank(0);

            // 统计做题总量，用于排序及平均做题数
            calIntoTotalFinishCount(totalFinishCount, detailMapper.getPatternType(), detailMapper.getFinishCount());
            // 统计正确题数，用于求平均正确率
            calIntoTotalCorrectNum(totalCorrectCount, detailMapper.getPatternType(), detailMapper.getFinishCount(), detailMapper.getCorrectRate());
            setSkillDetailByType(researchStaffSkillUnitMapper, detailMapper, detailMapper.getPatternType());
            // 计算最大值
            calMaxRSSkillData(maxFinishCount, rsSkillData, detailMapper);
        }

        // 更新最高学生数
        if (studentCount > researchStaffSkillMapper.getMaxStudentCount()) {
            researchStaffSkillMapper.setMaxStudentCount(studentCount);
        }

        researchStaffSkillMapper.getSkillUnitList().add(researchStaffSkillUnitMapper);
    }

    /**
     * 填充具体区/学校题型数据
     */
    private void fillPatternUnitMapperData(ResearchStaffPatternMapper researchStaffPatternMapper, Map<String, Long> totalFinishCount, Map<String, Double> totalCorrectCount, String name, List<RSPatternData> patterns) {
        ResearchStaffPatternUnitMapper researchStaffPatternUnitMapper
                = new ResearchStaffPatternUnitMapper();
        researchStaffPatternUnitMapper.setName(name);
        Map<String, ResearchStaffPatternDetailMapper> patternMap
                = new HashMap<>();

        for (RSPatternData rsPatternData : patterns) {
            ResearchStaffPatternDetailMapper detailMapper
                    = new ResearchStaffPatternDetailMapper();
            detailMapper.setCorrectRate(rsPatternData.getRate() * 100);
            detailMapper.setFinishCount(rsPatternData.getNum());
            detailMapper.setPatternType(rsPatternData.getType());
            patternMap.put(detailMapper.getPatternType(), detailMapper);

            // 统计做题总量，用于排序及平均做题数
            calIntoTotalFinishCount(totalFinishCount, detailMapper.getPatternType(), detailMapper.getFinishCount());

            // 统计正确题数，用于求平均正确率
            calIntoTotalCorrectNum(totalCorrectCount, detailMapper.getPatternType(), detailMapper.getFinishCount(), detailMapper.getCorrectRate());
        }
        researchStaffPatternUnitMapper.setPatternMap(patternMap);
        researchStaffPatternMapper.getPatternUnitList().add(researchStaffPatternUnitMapper);
    }

    /**
     * 生成按总做题量对题型进行排序后结果
     */
    private Map<String, Long> generatePatternRankMap(Map<String, Long> totalFinishCount) {
        List<Map.Entry<String, Long>> list = new LinkedList<>(totalFinishCount.entrySet());
        Collections.sort(list, (o1, o2) -> -(o1.getValue()).compareTo(o2.getValue()));

        Map<String, Long> rankMap = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : list) {
            rankMap.put(entry.getKey(), entry.getValue());
        }
        return rankMap;
    }

    /**
     * 计算平均数据
     */
    private void calPatternAvgStat(int total, Map<String, Long> totalFinishCount, Map<String, Double> totalCorrectCount, ResearchStaffPatternUnitMapper avgData, Map<String, ResearchStaffPatternDetailMapper> avgMap) {
        for (Map.Entry<String, Long> entry : totalFinishCount.entrySet()) {
            String pattern = entry.getKey();

            ResearchStaffPatternDetailMapper detailMapper
                    = new ResearchStaffPatternDetailMapper();
            detailMapper.setPatternType(pattern);
            detailMapper.setFinishCount((int) (entry.getValue() / total));
            detailMapper.setCorrectRate(totalCorrectCount.get(pattern) / entry.getValue());
            avgMap.put(pattern, detailMapper);
        }
        avgData.setPatternMap(avgMap);
    }

    /**
     * 将该题型做对题数计入做对题数总数
     */
    private void calIntoTotalCorrectNum(Map<String, Double> totalCorrectCount, String type, int finishCount, double correctRate) {
        Double correctNum = totalCorrectCount.get(type);
        if (correctNum == null) correctNum = 0d;
        correctNum += finishCount * correctRate;
        totalCorrectCount.put(type, correctNum);
    }

    /**
     * 将该题型做题数计入总做题数
     */
    private void calIntoTotalFinishCount(Map<String, Long> totalFinishCount, String type, int finishCount) {
        Long num = totalFinishCount.get(type);
        if (num == null) num = 0L;
        num += finishCount;
        totalFinishCount.put(type, num);
    }

    /**
     * 一个简单的计算a / b的方法
     */
    private double divide(Double a, Integer b) {
        if (a == null || b == null || b == 0) {
            return 0;
        }
        return a / b;
    }

    private double divide(Double a, Long b) {
        if (a == null || b == null || b == 0) {
            return 0;
        }
        return a / b;
    }
}
