package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.intelligentTeaching;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserRef;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserProgress;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.QuestionPackage;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserRefDao;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.IntelligentTeachingServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题包处理
 *
 * @author Wenlong Meng
 * @since Feb 20,2019
 */
@Named("IntelliagentTeaching.QuestionSubProcessor")
@SubType({
        ObjectiveConfigType.INTELLIGENT_TEACHING
})
public class QuestionPackageProcessor implements HomeworkProcessor {

    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private IntelligentTeachingServiceImpl intelligentTeachingService;
    @Inject
    protected HomeworkUserRefDao homeworkUserRefDao;
    @Inject
    private HomeworkLoader homeworkLoader;
    @Inject
    private HomeworkResultLoader homeworkResultLoader;

    /**
     * 题包
     *
     * @param hc args
     */
    @Override
    public void process(HomeworkContext hc) {
        Map<String, Object> data = hc.getData();
        HomeworkParam homeworkParam = hc.getHomeworkParam();
        Long studentId = homeworkParam.getStudentId();
        // 默认单元
        String unitId = hc.getUnitId();

        // 课时
        List<NewBookCatalog> sections = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.SECTION)
                .getOrDefault(unitId, Collections.emptyList());

        // 进度
        List<UserProgress> userProgresses = hc.getProgress() == null ? Collections.EMPTY_LIST : hc.getProgress().getUserProgresses();
        Map<String, List<UserProgress>> sectionMap = ObjectUtils.get(() ->
                userProgresses.stream()
                        .filter(u -> u.getSectionId() != null && u.getCourse() != null && unitId.equals(u.getUnitId()))
                        .collect(Collectors.groupingBy(UserProgress::getSectionId)), new HashMap<>());
        // 返回课时列表
        data.put("sections", sections.stream().map(catalog ->{
                    List<IntelDiagnosisCourse> courses = intelligentTeachingService.loadCoursesBySectionId(catalog.getId());
                    if(ObjectUtils.anyBlank(courses)){
                        return null;
                    }
                    return MapUtils.m("id", catalog.getId(),
                            "name", catalog.getName(),
                            "count", Math.min(ObjectUtils.get(() -> sectionMap.get(catalog.getId()).size(), 0), courses.size()),
                            "totalCount",  courses.size());
                }
        ).filter(Objects::nonNull).collect(Collectors.toList()));

        //题包
        QuestionPackage questionPackage = buildQuestionPackage(hc);
        int status = -1;//表示无题
        String homeworkId = null;
        if(questionPackage != null){
            UserProgress userProgress = userProgresses.stream().filter(p->p.getBookId().equals(homeworkParam.getBookId())&&p.getUnitId().equals(unitId)).findFirst().orElse(null);
            String doHomeworkId = ObjectUtils.get(()->(String)userProgress.getExtInfo().get("homeworkId"));
            if(!ObjectUtils.anyBlank(doHomeworkId)){
                status = 2;
                homeworkId = doHomeworkId;
            }else{
                homeworkId = questionPackage.getId();
                HomeworkResult homeworkResult = homeworkResultLoader.loadHomeworkResult(homeworkId, studentId);
                if(homeworkResult == null){
                    List<HomeworkUserRef> homeworkUserRefs = homeworkUserRefDao.loadHomeworkUserRef(studentId);
                    if(CollectionUtils.isNotEmpty(homeworkUserRefs) && homeworkUserRefs.stream().anyMatch(hu->hu.getHomeworkId().equals(questionPackage.getId()))){
                        status = 1;
                    }else{
                        status = 0;
                    }
                }else if(homeworkResult.getFinished() == Boolean.TRUE){
                    status = 2;
                }
            }
        }

        data.put("questionBoxes", MapUtils.m("status", status, "homeworkId", homeworkId));
        hc.setData(data);
    }

    /**
     * 组题包
     *
     * @param hc
     * @return
     */
    private QuestionPackage buildQuestionPackage(HomeworkContext hc){
        HomeworkParam param = hc.getHomeworkParam();
        String unitId = hc.getUnitId();
        String questionPackageId = HomeworkUtil.generatorDayID(param.getBizType(), unitId);
        //从缓存获取题包
        QuestionPackage questionPackage = HomeWorkCache.load(CacheKey.BOX, questionPackageId);
        if(questionPackage != null){
            return questionPackage;
        }

        // 组题包
        List<String> questionIds = new ArrayList<>();
        List<NewBookCatalog> sections = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.SECTION)
                .getOrDefault(unitId, Collections.emptyList());
        sections.forEach(catalog ->{
            List<IntelDiagnosisCourse> courses = intelligentTeachingService.loadCoursesBySectionId(catalog.getId());
            if(ObjectUtils.anyBlank(courses)){
                return;
            }
            courses.forEach(c->{
                if(ObjectUtils.anyBlank(c.getQuestionIds())){
                    return;
                }
                String qid = intelligentTeachingService.loadQuestionIdByIDQId(c.getQuestionIds().get(0));
                if(StringUtils.isNotEmpty(qid)){
                    questionIds.add(qid);
                }else{
                    LoggerUtils.info("loadQuestionIdByIDQId", c, qid);
                }
            });
        });
        if(ObjectUtils.anyBlank(questionIds)){
            return null;
        }
        questionPackage = new QuestionPackage();
        questionPackage.setName("BASE");
        questionPackage.setUnitId(unitId);
        questionPackage.setBizType(param.getBizType());
        questionPackage.setObjectiveConfigType(ObjectiveConfigType.EXAM.name());
        questionPackage.setId(questionPackageId);
        questionPackage.setQuestonIds(questionIds);
        questionPackage.setDuration(questionIds.size() * 17L);
        HomeWorkCache.set(24 * 60 * 60, questionPackage, CacheKey.BOX, questionPackageId);
        return questionPackage;
    }

}
