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

package com.voxlearning.utopia.service.business.impl.service.rstaff.convertor;

import com.voxlearning.utopia.service.business.api.entity.RSEnglishAreaHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.RSEnglishSchoolHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.RSMathAreaHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.RSMathSchoolHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.RSRegionKnowledgeStat;
import com.voxlearning.utopia.service.business.api.entity.RSRegionPatternStat;
import com.voxlearning.utopia.service.business.api.entity.RSRegionSkillMonthlyStat;
import com.voxlearning.utopia.service.business.api.entity.RSRegionSkillStat;
import com.voxlearning.utopia.service.business.api.entity.RSRegionUnitWeakPointStat;
import com.voxlearning.utopia.service.business.api.entity.RSRegionWeakPointStat;
import com.voxlearning.utopia.service.business.api.entity.RSSchoolKnowledgeStat;
import com.voxlearning.utopia.service.business.api.entity.RSSchoolPatternStat;
import com.voxlearning.utopia.service.business.api.entity.RSSchoolSkillMonthlyStat;
import com.voxlearning.utopia.service.business.api.entity.RSSchoolSkillStat;
import com.voxlearning.utopia.service.business.api.entity.RSSchoolUnitWeakPointStat;
import com.voxlearning.utopia.service.business.api.entity.RSSchoolWeakPointStat;
import com.voxlearning.utopia.service.business.api.entity.embedded.RSPatternData;
import com.voxlearning.utopia.service.business.api.entity.embedded.RSSkillData;
import com.voxlearning.utopia.service.business.api.entity.embedded.RSSkillMonthlyData;
import com.voxlearning.utopia.service.business.api.entity.embedded.RSSkillMonthlyUnitData;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSAreaHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.api.entity.extended.AbstractRSSchoolHomeworkBehaviorStat;
import com.voxlearning.utopia.service.business.impl.service.rstaff.bean.ResearchStaffKnowledgeData;
import com.voxlearning.utopia.service.business.impl.service.rstaff.bean.ResearchStaffPatternData;
import com.voxlearning.utopia.service.business.impl.service.rstaff.bean.ResearchStaffSkillData;
import com.voxlearning.utopia.service.business.impl.service.rstaff.bean.ResearchStaffSkillMonthlyData;
import com.voxlearning.utopia.service.business.impl.service.rstaff.bean.ResearchStaffUnitWeakPointData;
import com.voxlearning.utopia.service.business.impl.service.rstaff.bean.ResearchStaffWeakPointData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Changyuan on 2015/1/28.
 */
public class ResearchStaffConvertor {

    /* pattern data convertor */
    public static ResearchStaffPatternData convertToResearchStaffPatternData(RSRegionPatternStat rsRegionPatternStat) {
        ResearchStaffPatternData data = new ResearchStaffPatternData();
        data.setName(rsRegionPatternStat.getAreaName());
        data.setPatterns(rsRegionPatternStat.getPatterns());
        return data;
    }

    public static ResearchStaffPatternData convertToResearchStaffPatternData(RSSchoolPatternStat rsSchoolPatternStat) {
        ResearchStaffPatternData data = new ResearchStaffPatternData();
        data.setName(rsSchoolPatternStat.getSchoolName());
        data.setPatterns(rsSchoolPatternStat.getPatterns());
        return data;
    }

    public static List<ResearchStaffPatternData> convertRegionStatListToResearchStaffPatternData(Collection<RSRegionPatternStat> rsRegionPatternStats) {
        List<ResearchStaffPatternData> ret = new ArrayList<>();
        for (RSRegionPatternStat stat : rsRegionPatternStats) {
            ret.add(convertToResearchStaffPatternData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffPatternData> convertSchoolStatListToResearchStaffPatternData(Collection<RSSchoolPatternStat> rsSchoolPatternStats) {
        List<ResearchStaffPatternData> ret = new ArrayList<>();
        for (RSSchoolPatternStat stat : rsSchoolPatternStats) {
            ret.add(convertToResearchStaffPatternData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffPatternData> convertToCityResearchStaffPatternData(Collection<RSRegionPatternStat> rsRegionPatternStats) {
        Map<Long, ResearchStaffPatternData> map = new LinkedHashMap<>();
        for (RSRegionPatternStat rsRegionPatternStat : rsRegionPatternStats) {
            if (rsRegionPatternStat.getPatterns() == null) {
                rsRegionPatternStat.setPatterns(new ArrayList<>());
            }
            Long cityCode = rsRegionPatternStat.getCcode();
            if (map.containsKey(cityCode)) {
                ResearchStaffPatternData researchStaffPatternData = map.get(cityCode);
                List<RSPatternData> prevPatterns = researchStaffPatternData.getPatterns();
                Map<String, RSPatternData> patternDataMap = prevPatterns.stream()
                        .collect(Collectors.toMap(RSPatternData::getType, Function.identity()));
                for (RSPatternData rsPatternData : rsRegionPatternStat.getPatterns()) {
                    if (patternDataMap.containsKey(rsPatternData.getType())) {
                        RSPatternData prevPatternData = patternDataMap.get(rsPatternData.getType());
                        prevPatternData.setRate(
                                (rsPatternData.getNum() * rsPatternData.getRate() + prevPatternData.getNum() * prevPatternData.getRate())
                                / (rsPatternData.getNum() + prevPatternData.getNum()));
                        prevPatternData.setNum(rsPatternData.getNum() + prevPatternData.getNum());
                    } else {
                        patternDataMap.put(rsPatternData.getType(), rsPatternData);
                        prevPatterns.add(rsPatternData);
                    }
                }
            } else {
                ResearchStaffPatternData researchStaffPatternData = new ResearchStaffPatternData();
                researchStaffPatternData.setName(rsRegionPatternStat.getCityName());
                researchStaffPatternData.setPatterns(rsRegionPatternStat.getPatterns());
                map.put(cityCode, researchStaffPatternData);
            }
        }
        return new ArrayList<>(map.values());
    }

    /* skill data convertor */
    public static ResearchStaffSkillData convertToResearchStaffSkillData(RSRegionSkillStat rsRegionSkillStat) {
        ResearchStaffSkillData data = new ResearchStaffSkillData();
        data.setName(rsRegionSkillStat.getAreaName());
        data.setStuNum(rsRegionSkillStat.getStuNum());
        data.setSkills(rsRegionSkillStat.getSkills());
        return data;
    }

    public static ResearchStaffSkillData convertToResearchStaffSkillData(RSSchoolSkillStat rsSchoolSkillStat) {
        ResearchStaffSkillData data = new ResearchStaffSkillData();
        data.setName(rsSchoolSkillStat.getSchoolName());
        data.setStuNum(rsSchoolSkillStat.getStuNum());
        data.setSkills(rsSchoolSkillStat.getSkills());
        return data;
    }

    public static List<ResearchStaffSkillData> convertRegionStatListToResearchStaffSkillData(Collection<RSRegionSkillStat> rsRegionSkillStats) {
        List<ResearchStaffSkillData> ret = new ArrayList<>();
        for (RSRegionSkillStat stat : rsRegionSkillStats) {
            ret.add(convertToResearchStaffSkillData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffSkillData> convertSchoolStatListToResearchStaffSkillData(Collection<RSSchoolSkillStat> rsSchoolSkillStats) {
        List<ResearchStaffSkillData> ret = new ArrayList<>();
        for (RSSchoolSkillStat stat : rsSchoolSkillStats) {
            ret.add(convertToResearchStaffSkillData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffSkillData> convertToCityResearchStaffSkillData(Collection<RSRegionSkillStat> rsRegionSkillStats) {
        Map<Long, ResearchStaffSkillData> map = new LinkedHashMap<>();
        for (RSRegionSkillStat rsRegionSkillStat : rsRegionSkillStats) {
            if (rsRegionSkillStat.getSkills() == null) {
                rsRegionSkillStat.setSkills(new ArrayList<>());
            }
            Long cityCode = rsRegionSkillStat.getCcode();
            if (map.containsKey(cityCode)) {
                ResearchStaffSkillData researchStaffSkillData = map.get(cityCode);

                // 更新学生数量
                researchStaffSkillData.setStuNum(researchStaffSkillData.getStuNum() + rsRegionSkillStat.getStuNum());

                // 更新各种技能数据
                List<RSSkillData> prevSkills = researchStaffSkillData.getSkills();
                Map<String, RSSkillData> skillDataMap = prevSkills.stream()
                        .collect(Collectors.toMap(RSSkillData::getType, Function.identity()));
                for (RSSkillData rsSkillData : rsRegionSkillStat.getSkills()) {
                    RSSkillData prevSkillData = skillDataMap.get(rsSkillData.getType());
                    if (prevSkillData != null) {
                        prevSkillData.setRate(
                                (rsSkillData.getNum() * rsSkillData.getRate() + prevSkillData.getNum() * prevSkillData.getRate())
                                        / (rsSkillData.getNum() + prevSkillData.getNum()));
                        prevSkillData.setNum(rsSkillData.getNum() + prevSkillData.getNum());
                        prevSkillData.setPavg((int)(prevSkillData.getNum() / researchStaffSkillData.getStuNum() + 0.5));
                    } else {
                        prevSkills.add(rsSkillData);
                        skillDataMap.put(rsSkillData.getType(), rsSkillData);
                        rsSkillData.setPavg((int)(rsSkillData.getNum() / researchStaffSkillData.getStuNum() + 0.5));
                    }
                }
            } else {
                ResearchStaffSkillData researchStaffSkillData = new ResearchStaffSkillData();
                researchStaffSkillData.setName(rsRegionSkillStat.getCityName());
                researchStaffSkillData.setStuNum(rsRegionSkillStat.getStuNum());
                researchStaffSkillData.setSkills(rsRegionSkillStat.getSkills());
                map.put(cityCode, researchStaffSkillData);
            }
        }
        return new ArrayList<>(map.values());
    }

    /* knowledge data convertor */
    public static ResearchStaffKnowledgeData convertToResearchStaffKnowledgeData(RSRegionKnowledgeStat rsRegionKnowledgeStat) {
        ResearchStaffKnowledgeData data = new ResearchStaffKnowledgeData();

        data.setName(rsRegionKnowledgeStat.getAreaName());
        data.setStuNum(rsRegionKnowledgeStat.getStuNum());

        data.setWordNum(rsRegionKnowledgeStat.getWordNum());
        data.setWordRate(rsRegionKnowledgeStat.getWordRate());
        data.setWordPAvg(rsRegionKnowledgeStat.getWordPAvg());

        data.setTopicNum(rsRegionKnowledgeStat.getTopicNum());
        data.setTopicRate(rsRegionKnowledgeStat.getTopicRate());
        data.setTopicPAvg(rsRegionKnowledgeStat.getTopicPAvg());

        data.setGramNum(rsRegionKnowledgeStat.getGramNum());
        data.setGramRate(rsRegionKnowledgeStat.getGramRate());
        data.setGramPAvg(rsRegionKnowledgeStat.getGramPAvg());

        return data;
    }

    public static ResearchStaffKnowledgeData convertToResearchStaffKnowledgeData(RSSchoolKnowledgeStat rsSchoolKnowledgeStat) {
        ResearchStaffKnowledgeData data = new ResearchStaffKnowledgeData();

        data.setName(rsSchoolKnowledgeStat.getSchoolName());
        data.setStuNum(rsSchoolKnowledgeStat.getStuNum());

        data.setWordNum(rsSchoolKnowledgeStat.getWordNum());
        data.setWordRate(rsSchoolKnowledgeStat.getWordRate());
        data.setWordPAvg(rsSchoolKnowledgeStat.getWordPAvg());

        data.setTopicNum(rsSchoolKnowledgeStat.getTopicNum());
        data.setTopicRate(rsSchoolKnowledgeStat.getTopicRate());
        data.setTopicPAvg(rsSchoolKnowledgeStat.getTopicPAvg());

        data.setGramNum(rsSchoolKnowledgeStat.getGramNum());
        data.setGramRate(rsSchoolKnowledgeStat.getGramRate());
        data.setGramPAvg(rsSchoolKnowledgeStat.getGramPAvg());

        return data;
    }

    public static List<ResearchStaffKnowledgeData> convertRegionStatListToResearchStaffKnowledgeData(Collection<RSRegionKnowledgeStat> rsRegionKnowledgeStats) {
        List<ResearchStaffKnowledgeData> ret = new ArrayList<>();
        for (RSRegionKnowledgeStat stat : rsRegionKnowledgeStats) {
            ret.add(convertToResearchStaffKnowledgeData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffKnowledgeData> convertSchoolStatListToResearchStaffKnowledgeData(Collection<RSSchoolKnowledgeStat> rsSchoolKnowledgeStats) {
        List<ResearchStaffKnowledgeData> ret = new ArrayList<>();
        for (RSSchoolKnowledgeStat stat : rsSchoolKnowledgeStats) {
            ret.add(convertToResearchStaffKnowledgeData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffKnowledgeData> convertToCityResearchStaffKnowledgeData(Collection<RSRegionKnowledgeStat> rsRegionKnowledgeStats) {
        Map<Long, ResearchStaffKnowledgeData> map = new LinkedHashMap<>();
        for (RSRegionKnowledgeStat rsRegionKnowledgeStat : rsRegionKnowledgeStats) {
            Long cityCode = rsRegionKnowledgeStat.getCcode();
            if (map.containsKey(cityCode)) {
                ResearchStaffKnowledgeData researchStaffKnowledgeData = map.get(cityCode);
                researchStaffKnowledgeData.setStuNum(researchStaffKnowledgeData.getStuNum() + rsRegionKnowledgeStat.getStuNum());
                researchStaffKnowledgeData.setWordRate(calRate(researchStaffKnowledgeData.getWordNum(), researchStaffKnowledgeData.getWordRate(),
                        rsRegionKnowledgeStat.getWordNum(), rsRegionKnowledgeStat.getWordRate()));
                researchStaffKnowledgeData.setWordNum(researchStaffKnowledgeData.getWordNum() + rsRegionKnowledgeStat.getWordNum());
                researchStaffKnowledgeData.setWordPAvg((int)((double)researchStaffKnowledgeData.getWordNum() / researchStaffKnowledgeData.getStuNum() + 0.5));
                researchStaffKnowledgeData.setGramRate(calRate(researchStaffKnowledgeData.getGramNum(), researchStaffKnowledgeData.getGramRate(),
                        rsRegionKnowledgeStat.getGramNum(), rsRegionKnowledgeStat.getGramRate()));
                researchStaffKnowledgeData.setGramNum(researchStaffKnowledgeData.getGramNum() + rsRegionKnowledgeStat.getGramNum());
                researchStaffKnowledgeData.setGramPAvg((int) ((double) researchStaffKnowledgeData.getGramNum() / researchStaffKnowledgeData.getStuNum() + 0.5));
                researchStaffKnowledgeData.setTopicRate(calRate(researchStaffKnowledgeData.getTopicNum(), researchStaffKnowledgeData.getTopicRate(),
                        rsRegionKnowledgeStat.getTopicNum(), rsRegionKnowledgeStat.getTopicRate()));
                researchStaffKnowledgeData.setTopicNum(researchStaffKnowledgeData.getTopicNum() + rsRegionKnowledgeStat.getTopicNum());
                researchStaffKnowledgeData.setTopicPAvg((int) ((double) researchStaffKnowledgeData.getTopicNum() / researchStaffKnowledgeData.getStuNum() + 0.5));
            } else {
                ResearchStaffKnowledgeData researchStaffKnowledgeData = new ResearchStaffKnowledgeData();
                researchStaffKnowledgeData.setName(rsRegionKnowledgeStat.getCityName());
                researchStaffKnowledgeData.setStuNum(rsRegionKnowledgeStat.getStuNum());
                researchStaffKnowledgeData.setWordNum(rsRegionKnowledgeStat.getWordNum());
                researchStaffKnowledgeData.setWordRate(rsRegionKnowledgeStat.getWordRate());
                researchStaffKnowledgeData.setWordPAvg(rsRegionKnowledgeStat.getWordPAvg());
                researchStaffKnowledgeData.setGramNum(rsRegionKnowledgeStat.getGramNum());
                researchStaffKnowledgeData.setGramRate(rsRegionKnowledgeStat.getGramRate());
                researchStaffKnowledgeData.setGramPAvg(rsRegionKnowledgeStat.getGramPAvg());
                researchStaffKnowledgeData.setTopicNum(rsRegionKnowledgeStat.getTopicNum());
                researchStaffKnowledgeData.setTopicRate(rsRegionKnowledgeStat.getTopicRate());
                researchStaffKnowledgeData.setTopicPAvg(rsRegionKnowledgeStat.getTopicPAvg());
                map.put(cityCode, researchStaffKnowledgeData);
            }
        }
        return new ArrayList<>(map.values());
    }

    /* weak point data convertor */
    public static ResearchStaffWeakPointData convertToResearchStaffWeakPointData(RSRegionWeakPointStat rsRegionWeakPointStat) {
        ResearchStaffWeakPointData data = new ResearchStaffWeakPointData();
        data.setName(rsRegionWeakPointStat.getAreaName());
        data.setWord(rsRegionWeakPointStat.getWord());
        data.setTopic(rsRegionWeakPointStat.getTopic());
        data.setGram(rsRegionWeakPointStat.getGram());
        return data;
    }

    public static ResearchStaffWeakPointData convertToResearchStaffWeakPointData(RSSchoolWeakPointStat rsSchoolWeakPointStat) {
        ResearchStaffWeakPointData data = new ResearchStaffWeakPointData();
        data.setName(rsSchoolWeakPointStat.getSchoolName());
        data.setWord(rsSchoolWeakPointStat.getWord());
        data.setTopic(rsSchoolWeakPointStat.getTopic());
        data.setGram(rsSchoolWeakPointStat.getGram());
        return data;
    }

    public static List<ResearchStaffWeakPointData> convertRegionStatListToResearchStaffWeakPointData(Collection<RSRegionWeakPointStat> rsRegionWeakPointStats) {
        List<ResearchStaffWeakPointData> ret = new ArrayList<>();
        for (RSRegionWeakPointStat stat : rsRegionWeakPointStats) {
            ret.add(convertToResearchStaffWeakPointData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffWeakPointData> convertSchoolStatListToResearchStaffWeakPointData(Collection<RSSchoolWeakPointStat> rsSchoolWeakPointStats) {
        List<ResearchStaffWeakPointData> ret = new ArrayList<>();
        for (RSSchoolWeakPointStat stat : rsSchoolWeakPointStats) {
            ret.add(convertToResearchStaffWeakPointData(stat));
        }
        return ret;
    }

    /* unit weak point data convertor */
    public static ResearchStaffUnitWeakPointData convertToResearchStaffUnitWeakPointData(RSRegionUnitWeakPointStat rsRegionUnitWeakPointStat) {
        ResearchStaffUnitWeakPointData data = new ResearchStaffUnitWeakPointData();
        data.setName(rsRegionUnitWeakPointStat.getAreaName());
        data.setWeakPoint(rsRegionUnitWeakPointStat.getWeakPoint());
        return data;
    }

    public static ResearchStaffUnitWeakPointData convertToResearchStaffUnitWeakPointData(RSSchoolUnitWeakPointStat rsSchoolUnitWeakPointStat) {
        ResearchStaffUnitWeakPointData data = new ResearchStaffUnitWeakPointData();
        data.setName(rsSchoolUnitWeakPointStat.getSchoolName());
        data.setWeakPoint(rsSchoolUnitWeakPointStat.getWeakPoint());
        return data;
    }

    public static List<ResearchStaffUnitWeakPointData> convertRegionStatListToResearchStaffUnitWeakPointData(Collection<RSRegionUnitWeakPointStat> rsRegionUnitWeakPointStats) {
        List<ResearchStaffUnitWeakPointData> ret = new ArrayList<>();
        for (RSRegionUnitWeakPointStat stat : rsRegionUnitWeakPointStats) {
            ret.add(convertToResearchStaffUnitWeakPointData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffUnitWeakPointData> convertSchoolStatListToResearchStaffUnitWeakPointData(Collection<RSSchoolUnitWeakPointStat> rsSchoolUnitWeakPointStats) {
        List<ResearchStaffUnitWeakPointData> ret = new ArrayList<>();
        for (RSSchoolUnitWeakPointStat stat : rsSchoolUnitWeakPointStats) {
            ret.add(convertToResearchStaffUnitWeakPointData(stat));
        }
        return ret;
    }

    /* skill monthly data convertor */
    public static ResearchStaffSkillMonthlyData convertToResearchStaffSkillMonthlyData(RSRegionSkillMonthlyStat rsRegionSkillMonthlyStat) {
        ResearchStaffSkillMonthlyData data = new ResearchStaffSkillMonthlyData();
        data.setName(rsRegionSkillMonthlyStat.getAreaName());
        data.setSkills(rsRegionSkillMonthlyStat.getSkills());
        return data;
    }

    public static ResearchStaffSkillMonthlyData convertToResearchStaffSkillMonthlyData(RSSchoolSkillMonthlyStat rsSchoolSkillMonthlyStat) {
        ResearchStaffSkillMonthlyData data = new ResearchStaffSkillMonthlyData();
        data.setName(rsSchoolSkillMonthlyStat.getSchoolName());
        data.setSkills(rsSchoolSkillMonthlyStat.getSkills());
        return data;
    }

    public static List<ResearchStaffSkillMonthlyData> convertRegionStatListToResearchStaffSkillMonthlyData(Collection<RSRegionSkillMonthlyStat> rsRegionSkillMonthlyStats) {
        List<ResearchStaffSkillMonthlyData> ret = new ArrayList<>();
        for (RSRegionSkillMonthlyStat stat : rsRegionSkillMonthlyStats) {
            ret.add(convertToResearchStaffSkillMonthlyData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffSkillMonthlyData> convertSchoolStatListToResearchStaffSkillMonthlyData(Collection<RSSchoolSkillMonthlyStat> rsSchoolSkillMonthlyStats) {
        List<ResearchStaffSkillMonthlyData> ret = new ArrayList<>();
        for (RSSchoolSkillMonthlyStat stat : rsSchoolSkillMonthlyStats) {
            ret.add(convertToResearchStaffSkillMonthlyData(stat));
        }
        return ret;
    }

    public static List<ResearchStaffSkillMonthlyData> convertToCityResearchStaffSkillMonthlyData(Collection<RSRegionSkillMonthlyStat> rsRegionSkillMonthlyStats) {
        Map<Long, ResearchStaffSkillMonthlyData> map = new LinkedHashMap<>();
        for (RSRegionSkillMonthlyStat rsRegionSkillMonthlyStat : rsRegionSkillMonthlyStats) {
            Long cityCode = rsRegionSkillMonthlyStat.getCcode();
            if (map.containsKey(cityCode)) {
                ResearchStaffSkillMonthlyData researchStaffSkillMonthlyData = map.get(cityCode);
                List<RSSkillMonthlyData> prevSkills = researchStaffSkillMonthlyData.getSkills();
                Map<String, RSSkillMonthlyData> skillDataMap = prevSkills.stream()
                        .collect(Collectors.toMap(RSSkillMonthlyData::getType, Function.identity()));
                for (RSSkillMonthlyData stat : rsRegionSkillMonthlyStat.getSkills()) {
                    if (skillDataMap.containsKey(stat.getType())) {
                        List<RSSkillMonthlyUnitData> prevMonthList = skillDataMap.get(stat.getType()).getMonthList();
                        List<RSSkillMonthlyUnitData> monthList = stat.getMonthList();
                        int i = 0;
                        int j = 0;
                        while (i < prevMonthList.size() && j < monthList.size()) {
                            RSSkillMonthlyUnitData prevMonthUnit = prevMonthList.get(i);
                            RSSkillMonthlyUnitData monthUnit = monthList.get(j);
                            if (prevMonthUnit.getMonth().equals(monthUnit.getMonth())) {
                                i++;
                                j++;
                                prevMonthUnit.setRate(calRate(prevMonthUnit.getNum(), prevMonthUnit.getRate(), monthUnit.getNum(), monthUnit.getRate()));
                                prevMonthUnit.setNum(prevMonthUnit.getNum() + monthUnit.getNum());
                            } else if (prevMonthUnit.getMonth().compareTo(monthUnit.getMonth()) < 0) {
                                i++;
                            } else {
                                prevMonthList.add(i, monthUnit);
                                i++;
                                j++;
                            }
                        }
                    } else {
                        skillDataMap.put(stat.getType(), stat);
                        prevSkills.add(stat);
                    }
                }
            } else {
                ResearchStaffSkillMonthlyData researchStaffSkillMonthlyData = new ResearchStaffSkillMonthlyData();
                researchStaffSkillMonthlyData.setName(rsRegionSkillMonthlyStat.getCityName());
                researchStaffSkillMonthlyData.setSkills(rsRegionSkillMonthlyStat.getSkills());
                map.put(cityCode, researchStaffSkillMonthlyData);
            }
        }
        return new ArrayList<>(map.values());
    }

    /* behavior data convertor */
    public static List<AbstractRSSchoolHomeworkBehaviorStat> convertEnglishSchoolStatListToResearchStaffBehaviorData(Collection<RSEnglishSchoolHomeworkBehaviorStat> rsEngSchoolHomeworkBehaviorStats) {
        List<AbstractRSSchoolHomeworkBehaviorStat> ret = new ArrayList<>();
        for (RSEnglishSchoolHomeworkBehaviorStat rsEngSchoolHomeworkBehaviorStat : rsEngSchoolHomeworkBehaviorStats) {
            ret.add(rsEngSchoolHomeworkBehaviorStat);
        }
        return ret;
    }

    public static List<AbstractRSSchoolHomeworkBehaviorStat> convertMathSchoolStatListToResearchStaffBehaviorData(Collection<RSMathSchoolHomeworkBehaviorStat> rsMathHomeworkBehaviorStats) {
        List<AbstractRSSchoolHomeworkBehaviorStat> ret = new ArrayList<>();
        for (RSMathSchoolHomeworkBehaviorStat rsMathHomeworkBehaviorStat : rsMathHomeworkBehaviorStats) {
            ret.add(rsMathHomeworkBehaviorStat);
        }
        return ret;
    }

    public static List<AbstractRSAreaHomeworkBehaviorStat> convertEnglishAreaStatListToResearchStaffBehaviorData(Collection<RSEnglishAreaHomeworkBehaviorStat> rsEngHomeworkBehaviorStats) {
        List<AbstractRSAreaHomeworkBehaviorStat> ret = new ArrayList<>();
        for (RSEnglishAreaHomeworkBehaviorStat rsEngSchoolHomeworkBehaviorStat : rsEngHomeworkBehaviorStats) {
            ret.add(rsEngSchoolHomeworkBehaviorStat);
        }
        return ret;
    }

    public static List<AbstractRSAreaHomeworkBehaviorStat> convertMathAreaStatListToResearchStaffBehaviorData(Collection<RSMathAreaHomeworkBehaviorStat> rsMathHomeworkBehaviorStats) {
        List<AbstractRSAreaHomeworkBehaviorStat> ret = new ArrayList<>();
        for (RSMathAreaHomeworkBehaviorStat rsMathHomeworkBehaviorStat : rsMathHomeworkBehaviorStats) {
            ret.add(rsMathHomeworkBehaviorStat);
        }
        return ret;
    }



    private static double calRate(int num1, double rate1, int num2, double rate2) {
        return (num1 + num2) != 0 ? (num1 * rate1 + num2 * rate2) / (num1 + num2) : 0;
    }
}
