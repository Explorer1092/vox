package com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.IntelligentTeachingService;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.dao.IntelligentDiagnosisQuestionDao;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.dao.VariantRefDao;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 讲练测服务接口
 *
 * @author Wenlong Meng
 * @since Feb 13, 2019
 */
@Named
@ExposeService(interfaceClass = IntelligentTeachingService.class)
public class IntelligentTeachingServiceImpl implements IntelligentTeachingService {

    //Local variabless
    @Inject private VariantRefDao variantRefDao;
    @Inject private IntelligentDiagnosisQuestionDao intelligentDiagnosisQuestionDao;
    @Inject protected IntelDiagnosisClient intelDiagnosisClient;
    //课时->课程缓存
    LoadingCache<String, List<IntelDiagnosisCourse>> section2CoursesCache = CacheBuilder.newBuilder().maximumSize(1717).refreshAfterWrite(7, TimeUnit.DAYS).build(
            new CacheLoader<String, List<IntelDiagnosisCourse>>() {
                @Override
                public List<IntelDiagnosisCourse> load(String id) {
                    return cacheCoursesBySectionId(id);
                }
            });

    //Logic

    /**
     * 根据科目id获取可用教材id列表
     *
     * @param subjectId 科目id
     * @return
     */
    @Override
    public List<String> loadBookIds(Integer subjectId) {
        return variantRefDao.loadBookIds(subjectId);
    }

    /**
     * 根据课时id获取讲练测课程
     *
     * @param sectionId 课时id
     * @return
     */
    @Override
    public List<IntelDiagnosisCourse> loadCoursesBySectionId(String sectionId) {
        return section2CoursesCache.getUnchecked(sectionId);
    }

    /**
     * 缓存课时与课程
     *
     * @param sectionId 课时id
     * @return
     */
    private List<IntelDiagnosisCourse> cacheCoursesBySectionId(String sectionId) {
        List<String> variantIds = variantRefDao.loadVariantIds(sectionId);
        List<List<String>> videfids = variantIds.stream().map(variantId->Arrays.asList(variantId, "IDEF_10200000009966")).collect(Collectors.toList());
        List<IntelDiagnosisCourse> result = intelDiagnosisClient.loadOnlineIntelCourseByVarIdsAndErrorIds(videfids);
        LoggerUtils.info("cacheCoursesBySectionId", sectionId, result != null ? result.size():0);
        return result;
    }

    /**
     * 查询讲练测题id对应题id
     *
     * @param iDQid 讲练测题id
     * @return
     */
    @Override
    public String loadQuestionIdByIDQId(String iDQid) {
        return intelligentDiagnosisQuestionDao.loadQuestionId(iDQid);
    }

    /**
     * 查询题qid查询对应的题zid
     *
     * @param qid 题id
     * @return
     */
    @Override
    public String loadZIdByQId(String qid) {
        return intelligentDiagnosisQuestionDao.loadZIdByQId(qid);
    }
}
