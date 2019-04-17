package com.voxlearning.utopia.service.business.impl.loader;


import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.business.api.SchoolMasterDataLoader;
import com.voxlearning.utopia.business.api.entity.ClassStudySitutation;
import com.voxlearning.utopia.business.api.entity.KnowledgeAbilityAnalysis;
import com.voxlearning.utopia.business.api.entity.SchoolReportSituation;
import com.voxlearning.utopia.service.business.base.AbstractSchoolMasterDataLoader;
import com.voxlearning.utopia.service.business.impl.dao.ClassStudySitutationDao;
import com.voxlearning.utopia.service.business.impl.dao.KnowledgeAbilityAnalysisDao;
import com.voxlearning.utopia.service.business.impl.dao.SchoolReportSituationDao;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * /**
 *
 * @author fugui.chang
 * @since 2016-9-27
 */
@Named
@Service(interfaceClass = SchoolMasterDataLoader.class)
@ExposeService(interfaceClass = SchoolMasterDataLoader.class)
public class SchoolMasterDataLoaderImpl extends AbstractSchoolMasterDataLoader {
    @Inject
    private SchoolReportSituationDao schoolReportSituationDao;
    @Inject
    private ClassStudySitutationDao classStudySitutationDao;
    @Inject
    private KnowledgeAbilityAnalysisDao knowledgeAbilityAnalysisDao;


    @Override
    public Map<String, List<Map<String, Object>>> getKnowledgeAbilityAnalysisData(School school, Long schoolId, String subject, Long beginDt, Long endDt) {

        if (schoolId == null || StringUtils.isBlank(subject) || beginDt == null || endDt == null) {
            return Collections.emptyMap();
        }
        List<KnowledgeAbilityAnalysis> knowledgeAbilityAnalysises = knowledgeAbilityAnalysisDao.loadBySchoolIdSubjectDt(schoolId, subject, beginDt, endDt);
        Map<String, List<Map<String, Object>>> knowledgeAbilityAnalysisData = new LinkedHashMap<>();
        knowledgeAbilityAnalysises.sort((o1, o2) -> o1.getGrade() - o2.getGrade());
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        if (!school.isJuniorSchool()) {
            for (KnowledgeAbilityAnalysis knowledgeAbilityAnalysis : knowledgeAbilityAnalysises) {
                if (knowledgeAbilityAnalysisData.containsKey(knowledgeAbilityAnalysis.getGrade().toString())) {
                    List<Map<String, Object>> l = knowledgeAbilityAnalysisData.get(knowledgeAbilityAnalysis.getGrade().toString());
                    KnowledgeAbilityAnalysis.PrimaryAbility primaryAbility = JsonUtils.fromJson(knowledgeAbilityAnalysis.getPrimaryAbility(), KnowledgeAbilityAnalysis.PrimaryAbility.class);
                    if (primaryAbility != null) {
                        if (map.containsKey(knowledgeAbilityAnalysis.getGrade().toString() + knowledgeAbilityAnalysis.getYearmonth())) {
                            Map<String, Object> m = map.get(knowledgeAbilityAnalysis.getGrade().toString() + knowledgeAbilityAnalysis.getYearmonth());
                            if (primaryAbility.getListenRate() != null) {
                                m.put("listen", primaryAbility.getListenRate() + SafeConverter.toDouble(m.get("listen")));
                            }
                            if (primaryAbility.getSpeakRate() != null) {
                                m.put("speak", primaryAbility.getSpeakRate() + SafeConverter.toDouble(m.get("speak")));
                            }
                            if (primaryAbility.getReadRate() != null) {
                                m.put("read", primaryAbility.getReadRate() + SafeConverter.toDouble(m.get("read")));
                            }
                            if (primaryAbility.getWriteRate() != null) {
                                m.put("write", primaryAbility.getWriteRate() + SafeConverter.toDouble(m.get("write")));
                            }
                        } else {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("time", knowledgeAbilityAnalysis.getYearmonth());
                            m.put("listen", SafeConverter.toDouble(primaryAbility.getListenRate()));
                            m.put("speak", SafeConverter.toDouble(primaryAbility.getSpeakRate()));
                            m.put("read", SafeConverter.toDouble(primaryAbility.getReadRate()));
                            m.put("write", SafeConverter.toDouble(primaryAbility.getWriteRate()));
                            l.add(m);
                            map.put(knowledgeAbilityAnalysis.getGrade().toString() + knowledgeAbilityAnalysis.getYearmonth(), m);
                        }
                    }
                } else {
                    KnowledgeAbilityAnalysis.PrimaryAbility primaryAbility = JsonUtils.fromJson(knowledgeAbilityAnalysis.getPrimaryAbility(), KnowledgeAbilityAnalysis.PrimaryAbility.class);
                    if (primaryAbility != null) {
                        List<Map<String, Object>> l = new LinkedList<>();
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("time", knowledgeAbilityAnalysis.getYearmonth());
                        m.put("listen", SafeConverter.toDouble(primaryAbility.getListenRate()));
                        m.put("speak", SafeConverter.toDouble(primaryAbility.getSpeakRate()));
                        m.put("read", SafeConverter.toDouble(primaryAbility.getReadRate()));
                        m.put("write", SafeConverter.toDouble(primaryAbility.getWriteRate()));
                        l.add(m);
                        map.put(knowledgeAbilityAnalysis.getGrade().toString() + knowledgeAbilityAnalysis.getYearmonth(), m);
                        knowledgeAbilityAnalysisData.put(knowledgeAbilityAnalysis.getGrade().toString(), l);
                    }
                }
            }
        } else {
            for (KnowledgeAbilityAnalysis knowledgeAbilityAnalysis : knowledgeAbilityAnalysises) {
                if (knowledgeAbilityAnalysisData.containsKey(knowledgeAbilityAnalysis.getGrade().toString())) {
                    List<Map<String, Object>> l = knowledgeAbilityAnalysisData.get(knowledgeAbilityAnalysis.getGrade().toString());
                    KnowledgeAbilityAnalysis.MiddleAbility middleAbility = JsonUtils.fromJson(knowledgeAbilityAnalysis.getMiddleAbility(), KnowledgeAbilityAnalysis.MiddleAbility.class);
                    if (middleAbility != null) {
                        if (map.containsKey(knowledgeAbilityAnalysis.getGrade().toString() + knowledgeAbilityAnalysis.getYearmonth())) {
                            Map<String, Object> m = map.get(knowledgeAbilityAnalysis.getGrade().toString() + knowledgeAbilityAnalysis.getYearmonth());
                            if (middleAbility.getListenRate() != null) {
                                m.put("listen", middleAbility.getListenRate() + SafeConverter.toDouble(m.get("listen")));
                            }
                            if (middleAbility.getOralRate() != null) {
                                m.put("oral", middleAbility.getOralRate() + SafeConverter.toDouble(m.get("oral")));
                            }
                            if (middleAbility.getWordRate() != null) {
                                m.put("word", middleAbility.getWordRate() + SafeConverter.toDouble(m.get("word")));
                            }
                            if (middleAbility.getReadingRate() != null) {
                                m.put("read", middleAbility.getReadingRate() + SafeConverter.toDouble(m.get("read")));
                            }
                            if (middleAbility.getGrammarRate() != null) {
                                m.put("grammar", middleAbility.getGrammarRate() + SafeConverter.toDouble(m.get("grammar")));
                            }
                        } else {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("time", knowledgeAbilityAnalysis.getYearmonth());
                            m.put("listen", SafeConverter.toDouble(middleAbility.getListenRate()));
                            m.put("oral", SafeConverter.toDouble(middleAbility.getOralRate()));
                            m.put("word", SafeConverter.toDouble(middleAbility.getWordRate()));
                            m.put("read", SafeConverter.toDouble(middleAbility.getReadingRate()));
                            m.put("grammar", SafeConverter.toDouble(middleAbility.getGrammarRate()));
                            map.put(knowledgeAbilityAnalysis.getGrade().toString() + knowledgeAbilityAnalysis.getYearmonth(), m);
                            l.add(m);
                        }
                    }
                } else {
                    KnowledgeAbilityAnalysis.MiddleAbility middleAbility = JsonUtils.fromJson(knowledgeAbilityAnalysis.getMiddleAbility(), KnowledgeAbilityAnalysis.MiddleAbility.class);
                    if (middleAbility != null) {
                        List<Map<String, Object>> l = new LinkedList<>();
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("time", knowledgeAbilityAnalysis.getYearmonth());
                        m.put("listen", SafeConverter.toDouble(middleAbility.getListenRate()));
                        m.put("oral", SafeConverter.toDouble(middleAbility.getOralRate()));
                        m.put("word", SafeConverter.toDouble(middleAbility.getWordRate()));
                        m.put("read", SafeConverter.toDouble(middleAbility.getReadingRate()));
                        m.put("grammar", SafeConverter.toDouble(middleAbility.getGrammarRate()));
                        map.put(knowledgeAbilityAnalysis.getGrade().toString() + knowledgeAbilityAnalysis.getYearmonth(), m);
                        l.add(m);
                        knowledgeAbilityAnalysisData.put(knowledgeAbilityAnalysis.getGrade().toString(), l);
                    }
                }
            }
        }
        for (List<Map<String, Object>> n : knowledgeAbilityAnalysisData.values()) {
            n.sort((o1, o2) -> Long.compare(SafeConverter.toLong(o2.get("time")),SafeConverter.toLong(o1.get("time"))));
        }
        return knowledgeAbilityAnalysisData;
    }

    @Override
    public List<Map<String, Object>> getSchoolSitutaion(Long schoolId, Long yearchmonth) {
        if (schoolId == null || yearchmonth == null) {
            return Collections.emptyList();
        }
        List<SchoolReportSituation> schoolReportSituations = schoolReportSituationDao.loadSchoolReportSituationBySchoolIdAndDt(schoolId, yearchmonth);
        if (CollectionUtils.isEmpty(schoolReportSituations)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> schoolSitutationInfoList = new ArrayList<>();
        Long total_auth_use_tea_num_total = 0L;
        Long total_month_sasc = 0L;
        for (SchoolReportSituation tempschoolReportSituation : schoolReportSituations) {
            Map<String, Object> map = new HashMap<>();
            if (StringUtils.isBlank(tempschoolReportSituation.getSubject())) {
                continue;
            }
            if (tempschoolReportSituation.getSubject().equals("ALL")) {
                total_auth_use_tea_num_total = tempschoolReportSituation.getAuth_use_tea_num_total();
                total_month_sasc = tempschoolReportSituation.getMonth_sasc();
            } else {
                map.put("subject", Subject.of(tempschoolReportSituation.getSubject()).getValue());
                if (tempschoolReportSituation.getAuth_use_tea_num_total() == null) {
                    map.put("auth_use_tea_num_total", "暂无数据");
                } else {
                    map.put("auth_use_tea_num_total", tempschoolReportSituation.getAuth_use_tea_num_total());
                }
                if (tempschoolReportSituation.getMonth_sasc() == null) {
                    map.put("month_sasc", "暂无数据");
                } else {
                    map.put("month_sasc", tempschoolReportSituation.getMonth_sasc());
                }
                schoolSitutationInfoList.add(map);
            }
        }
        if (CollectionUtils.isNotEmpty(schoolReportSituations)) {
            Map<String, Object> map = new HashMap<>();
            map.put("subject", "总计");
            map.put("auth_use_tea_num_total", total_auth_use_tea_num_total);
            map.put("month_sasc", total_month_sasc);
            schoolSitutationInfoList.add(map);
        }
        return schoolSitutationInfoList;
    }

    @Override
    public Map<String, List<ClassStudySitutation>> loadClassStudySitutationBySchoolIdDtSubjectData(Long schoolId, Long dt, String subject) {
        if (schoolId == null || dt == null || subject == null) {
            return Collections.emptyMap();
        }
        List<ClassStudySitutation> classStudySitutations = classStudySitutationDao.loadClassStudySitutationBySchoolIdDtSubject(schoolId, dt, subject);
        if (classStudySitutations != null) {
            classStudySitutations.sort((o1, o2) -> SafeConverter.toInt(o1.getGrade()) - SafeConverter.toInt(o2.getGrade()));
        }
        return classStudySitutations != null ?
                classStudySitutations
                        .stream()
                        .collect(Collectors
                                .groupingBy(ClassStudySitutation::getGrade)) :
                Collections.emptyMap();
    }

}