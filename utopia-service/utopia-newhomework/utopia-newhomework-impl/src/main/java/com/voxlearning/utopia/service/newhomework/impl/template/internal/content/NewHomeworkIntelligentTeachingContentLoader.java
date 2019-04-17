package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.api.cuotizhenduan.entity.EnglishIntelligentDiagnosisHugePak;
import com.voxlearning.athena.api.cuotizhenduan.entity.EnglishIntelligentDiagnosisPak;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.WrongQuestionDiagnosisLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisVariant;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 重点讲练测
 * @author: Mr_VanGogh
 * @date: 2018/6/13 下午5:13
 */
@Named
public class NewHomeworkIntelligentTeachingContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject
    private WrongQuestionDiagnosisLoaderClient wrongQuestionDiagnosisLoaderClient;
    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.INTELLIGENT_TEACHING;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        try {
            List<Map<String, Object>> content = new ArrayList<>();
            TeacherDetail teacher = mapper.getTeacher();
            String unitId = mapper.getUnitId();
            String bookId = mapper.getBookId();
            Subject subject = teacher.getSubject();

            switch (subject) {
                case MATH:
                    return processMathContent(content, teacher, unitId, bookId);
                case ENGLISH:
                    return processEnglishContent(content, teacher, unitId, bookId);
                default:
                    return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("Failed to load NewHomeworkIntelligentTeachingContent, mapper:{}", mapper, e);
            return Collections.emptyList();
        }
    }

    /**
     * 数学:组装Content
     */
    private List<Map<String, Object>> processMathContent(List<Map<String, Object>> content, TeacherDetail teacher, String unitId, String bookId) {
        // 获取题包
        MathIntelligentDiagnosisPak intelligentDiagnosisPak = null;
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("unitId", unitId);
            requestMap.put("subjectId", "102");
            requestMap.put("packageType", "SYNC");
            requestMap.put("simulate", 1);
            String url = RuntimeMode.isUsingTestData() ? NewHomeworkConstants.INTELLIGENT_DIAGNOSIS_QUESTION_PAKS_TEST_URL : NewHomeworkConstants.INTELLIGENT_DIAGNOSIS_QUESTION_PAKS_PRODUCT_URL;
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .post(url)
                    .json(requestMap)
                    .contentType("application/json").socketTimeout(3 * 1000)
                    .execute();
            if (response.getStatusCode() == 200 && StringUtils.isNotEmpty(response.getResponseString())) {
                intelligentDiagnosisPak = JsonUtils.fromJson(response.getResponseString(), MathIntelligentDiagnosisPak.class);
            }
        } catch (Exception e) {
            logger.error("newHomeworkIntelligentTeachingContentLoader call athena error:", e);
        }
        if (intelligentDiagnosisPak != null) {
            List<MathIntelligentDiagSectionPak> sectionPaks = intelligentDiagnosisPak.getSectionPaks();
            if (CollectionUtils.isNotEmpty(sectionPaks)) {
                //布置题目信息
                Set<String> questionIds = sectionPaks
                        .stream()
                        .filter(sectionPak -> CollectionUtils.isNotEmpty(sectionPak.getVariantPaks()))
                        .map(MathIntelligentDiagSectionPak::getVariantPaks)
                        .flatMap(Collection::stream)
                        .map(MathIntellVariantPak::getQuestionId)
                        .collect(Collectors.toSet());
                Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(questionIds)
                        .stream()
                        .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
                //课程信息
                Set<String> courseIds = sectionPaks
                        .stream()
                        .filter(sectionPak -> CollectionUtils.isNotEmpty(sectionPak.getVariantPaks()))
                        .map(MathIntelligentDiagSectionPak::getVariantPaks)
                        .flatMap(Collection::stream)
                        .map(MathIntellVariantPak::getCourseId)
                        .collect(Collectors.toSet());
                Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);
                if (MapUtils.isNotEmpty(allQuestionMap) && MapUtils.isNotEmpty(intelDiagnosisCourseMap)) {
                    // 题目总的使用次数
                    Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), allQuestionMap.keySet(), HomeworkContentType.QUESTION);
                    // 老师使用次数
                    TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
                    // 所有题型
                    Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
                    //变式信息
                    Set<String> variantIds = sectionPaks
                            .stream()
                            .filter(sectionPak -> CollectionUtils.isNotEmpty(sectionPak.getVariantPaks()))
                            .map(MathIntelligentDiagSectionPak::getVariantPaks)
                            .flatMap(Collection::stream)
                            .map(MathIntellVariantPak::getVariantId)
                            .collect(Collectors.toSet());
                    Map<String, IntelDiagnosisVariant> intelDiagnosisVariantMap = intelDiagnosisClient.loadIntelDiagnosisVariantByIdIncludeDisabled(variantIds);
                    //后测题信息
                    List<String> postQuestionIds = sectionPaks
                            .stream()
                            .filter(sectionPak -> CollectionUtils.isNotEmpty(sectionPak.getVariantPaks()))
                            .map(MathIntelligentDiagSectionPak::getVariantPaks)
                            .flatMap(Collection::stream)
                            .map(MathIntellVariantPak::getPostQuestionId)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    Map<String, NewQuestion> postQuestionMap = questionLoaderClient.loadQuestionByDocIds(postQuestionIds)
                            .stream()
                            .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
                    //教材信息
                    EmbedBook book = new EmbedBook();
                    book.setBookId(bookId);
                    book.setUnitId(unitId);

                    Map<String, Integer> sectionPackageCountMap = new HashMap<>();
                    for (MathIntelligentDiagSectionPak sectionPak : sectionPaks) {
                        if (CollectionUtils.isNotEmpty(sectionPak.getVariantPaks())) {
                            String sectionId = sectionPak.getSectionId();
                            book.setSectionId(sectionId);
                            //题包信息
                            Map<String, Object> sectionPakMap = new LinkedHashMap<>();
                            int idx = 0;
                            if (sectionPackageCountMap.containsKey(sectionId)) {
                                idx = sectionPackageCountMap.get(sectionId) + 1;
                            }
                            sectionPackageCountMap.put(sectionId, idx);
                            sectionPakMap.put("id", sectionPak.getSectionId() + "-" + (idx + 1));
                            sectionPakMap.put("title", sectionPak.getPackageName());
                            //此题包下的题目信息
                            Map<String, NewQuestion> questionMap = new HashMap<>();
                            sectionPak.getVariantPaks()
                                    .forEach(o -> {
                                        if (allQuestionMap.get(o.getQuestionId()) != null) {
                                            questionMap.put(o.getQuestionId(), allQuestionMap.get(o.getQuestionId()));
                                        }
                                    });
                            //变式信息 questionId + 变式信息
                            Map<String, IntelDiagnosisVariant> variantMap = new HashMap<>();
                            sectionPak.getVariantPaks()
                                    .forEach(o -> {
                                        if (intelDiagnosisVariantMap.get(o.getVariantId()) != null) {
                                            variantMap.put(o.getQuestionId(), intelDiagnosisVariantMap.get(o.getVariantId()));
                                        }
                                    });
                            //对应的课程信息 questionId + course信息
                            Map<String, IntelDiagnosisCourse> courseMap = new HashMap<>();
                            sectionPak.getVariantPaks()
                                    .forEach(o -> {
                                        if (intelDiagnosisCourseMap.get(o.getCourseId()) != null) {
                                            courseMap.put(o.getQuestionId(), intelDiagnosisCourseMap.get(o.getCourseId()));
                                        }
                                    });
                            //对应后测题信息 questionId + List<Map<String, Object>>
                            Map<String, List<Map<String, Object>>> postQuestionInfoMap = new HashMap<>();
                            sectionPak.getVariantPaks()
                                    .forEach(o -> {
                                        List<Map<String, Object>> newQuestions = new ArrayList<>();
                                        o.getPostQuestionId().forEach(q -> {
                                            if (postQuestionMap.get(q) != null) {
                                                NewQuestion newQuestion = postQuestionMap.get(q);
                                                Map<String, Object> postMap = NewHomeworkContentDecorator.decorateNewQuestion(newQuestion, new HashMap<>(), new HashMap<>(), null, book);
                                                newQuestions.add(postMap);
                                            }
                                        });
                                        postQuestionInfoMap.put(o.getQuestionId(), newQuestions);
                                    });
                            //组装Question信息
                            List<Map<String, Object>> questionList = questionMap.values().stream()
                                    .map(q -> {
                                        Map<String, Object> map = NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book);
                                        map.put("variantId", variantMap.get(q.getId()) != null ? variantMap.get(q.getId()).getId() : "");
                                        map.put("variantName", variantMap.get(q.getId()) != null ? variantMap.get(q.getId()).getCoreMission() : "");
                                        map.put("courseId", courseMap.get(q.getId()) != null ? courseMap.get(q.getId()).getId() : "");
                                        map.put("courseName", courseMap.get(q.getId()) != null ? courseMap.get(q.getId()).getName() : "");
                                        map.put("postQuestions", postQuestionInfoMap.get(q.getId()));
                                        return map;
                                    })
                                    .collect(Collectors.toList());
                            if (CollectionUtils.isEmpty(questionList)) {
                                continue;
                            }

                            sectionPakMap.put("questions", questionList);
                            sectionPakMap.put("showAssigned", teacherAssignmentRecord != null && questionMap.values().stream().allMatch(q -> teacherAssignmentRecord.getQuestionInfo().getOrDefault(q.getDocId(), 0) > 0));
                            sectionPakMap.put("seconds", questionMap.values().stream().mapToInt(q -> SafeConverter.toInt(q.getSeconds())).sum());

                            content.add(sectionPakMap);
                        }
                    }
                }
            }
        }
        return content;
    }

    /**
     * 英语:组装Content
     */
    private List<Map<String, Object>> processEnglishContent(List<Map<String, Object>> content, TeacherDetail teacher, String unitId, String bookId) {
        // 获取题包
        Map<String, List<EnglishIntelligentDiagnosisHugePak>> intelligentDiagnosisPakMap = new HashMap<>();
        try {
            intelligentDiagnosisPakMap = wrongQuestionDiagnosisLoaderClient.getCuotizhenduanLoader().loadEnglishIntelligentDiagnosisQuestionHugePaksV2(Collections.singletonList(unitId), "SYNC");
        } catch (Exception e) {
            logger.error("newHomeworkIntelligentTeachingContentLoader call athena error:", e);
        }

        List<EnglishIntelligentDiagnosisHugePak> pakList = new ArrayList<>();
        if (MapUtils.isNotEmpty(intelligentDiagnosisPakMap)) {
            for (List<EnglishIntelligentDiagnosisHugePak> intelligentDiagnosisPakList : intelligentDiagnosisPakMap.values()) {
                pakList.addAll(intelligentDiagnosisPakList);
            }
        }

        if (CollectionUtils.isNotEmpty(pakList)) {
            // 所有的前测题目信息
            List<String> allBeforeQuestionIds = pakList
                    .stream()
                    .map(EnglishIntelligentDiagnosisHugePak::getPakList)
                    .flatMap(Collection::stream)
                    .map(EnglishIntelligentDiagnosisPak::getQuestionIds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            // 所有的后测题目信息
            List<String> allPostQuestionIds = pakList
                    .stream()
                    .map(EnglishIntelligentDiagnosisHugePak::getPakList)
                    .flatMap(Collection::stream)
                    .map(EnglishIntelligentDiagnosisPak::getPostQuestionIds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            // 所有的题目信息
            List<String> allQuestionIds = new ArrayList<>(allBeforeQuestionIds);
            allQuestionIds.addAll(allPostQuestionIds);
            Map<String, NewQuestion> allQuestionInfoMap = questionLoaderClient.loadQuestionByDocIds(allQuestionIds)
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
            //所有的课程信息
            Set<String> allCourseIds = pakList
                    .stream()
                    .map(EnglishIntelligentDiagnosisHugePak::getPakList)
                    .flatMap(Collection::stream)
                    .map(EnglishIntelligentDiagnosisPak::getCourseId)
                    .collect(Collectors.toSet());
            Map<String, IntelDiagnosisCourse> allIntelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(allCourseIds);

            // 题目总的使用次数
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), allQuestionIds, HomeworkContentType.QUESTION);
            // 所有题型
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            // 老师使用次数
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);

            for (EnglishIntelligentDiagnosisHugePak intelligentDiagnosisHugePak : pakList) {
                Map<String, Object> hugePakMap = new LinkedHashMap<>();
                List<Map<String, Object>> smallPackages = new ArrayList<>();
                int questionCount = 0;
                // 小题包信息
                List<EnglishIntelligentDiagnosisPak> smallPackageList = intelligentDiagnosisHugePak.getPakList();
                if (CollectionUtils.isNotEmpty(smallPackageList)) {
                    //前测题信息
                    List<String> questionIds = smallPackageList
                            .stream()
                            .map(EnglishIntelligentDiagnosisPak::getQuestionIds)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    Map<String, NewQuestion> normalQuestionInfoMap = new HashMap<>();
                    questionIds.forEach(q -> {
                        NewQuestion newQuestion = allQuestionInfoMap.get(q);
                        if (newQuestion != null) {
                            normalQuestionInfoMap.put(q, newQuestion);
                        }
                    });
                    //后测题信息
                    List<String> postQuestionIds = smallPackageList
                            .stream()
                            .map(EnglishIntelligentDiagnosisPak::getPostQuestionIds)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    Map<String, NewQuestion> postQuestionInfoMap = new HashMap<>();
                    postQuestionIds.forEach(q -> {
                        NewQuestion newQuestion = allQuestionInfoMap.get(q);
                        if (newQuestion != null) {
                            postQuestionInfoMap.put(q, newQuestion);
                        }
                    });
                    //课程信息
                    Set<String> courseIds = smallPackageList
                            .stream()
                            .map(EnglishIntelligentDiagnosisPak::getCourseId)
                            .collect(Collectors.toSet());
                    Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = new HashMap<>();
                    courseIds.forEach(c -> {
                        IntelDiagnosisCourse intelDiagnosisCourse = allIntelDiagnosisCourseMap.get(c);
                        if (intelDiagnosisCourse != null) {
                            intelDiagnosisCourseMap.put(c, intelDiagnosisCourse);
                        }
                    });

                    if (MapUtils.isNotEmpty(normalQuestionInfoMap) && MapUtils.isNotEmpty(intelDiagnosisCourseMap)) {
                        //教材信息
                        EmbedBook book = new EmbedBook();
                        book.setBookId(bookId);
                        book.setUnitId(unitId);
                        for (EnglishIntelligentDiagnosisPak englishIntelligentDiagnosisPak : smallPackageList) {
                            //题包信息
                            Map<String, Object> sectionPakMap = new LinkedHashMap<>();
                            // 课程信息
                            IntelDiagnosisCourse intelDiagnosisCourse = intelDiagnosisCourseMap.get(englishIntelligentDiagnosisPak.getCourseId());
                            //此题包下的前测题目信息
                            Map<String, NewQuestion> questionMap = new HashMap<>();
                            englishIntelligentDiagnosisPak.getQuestionIds()
                                    .forEach(o -> {
                                        if (normalQuestionInfoMap.get(o) != null) {
                                            questionMap.put(o, normalQuestionInfoMap.get(o));
                                        }
                                    });
                            //组装前测题目信息
                            List<Map<String, Object>> questionList = questionMap.values().stream()
                                    .map(q -> NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book))
                                    .collect(Collectors.toList());
                            if (CollectionUtils.isEmpty(questionList)) {
                                continue;
                            }
                            //此题包下的后测题目信息
                            Map<String, NewQuestion> postQuestionMap = new HashMap<>();
                            englishIntelligentDiagnosisPak.getPostQuestionIds()
                                    .forEach(o -> {
                                        if (postQuestionInfoMap.get(o) != null) {
                                            postQuestionMap.put(o, postQuestionInfoMap.get(o));
                                        }
                                    });
                            //组装后测题目信息
                            List<Map<String, Object>> postQuestionList = postQuestionMap.values().stream()
                                    .map(q -> NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book))
                                    .collect(Collectors.toList());
                            sectionPakMap.put("id", englishIntelligentDiagnosisPak.getPackageId());
                            sectionPakMap.put("title", englishIntelligentDiagnosisPak.getPackageName());
                            sectionPakMap.put("variantId", englishIntelligentDiagnosisPak.getVariantId());
                            sectionPakMap.put("variantName", englishIntelligentDiagnosisPak.getVariantName());
                            sectionPakMap.put("courseId", englishIntelligentDiagnosisPak.getCourseId());
                            sectionPakMap.put("courseName", intelDiagnosisCourse != null ? intelDiagnosisCourse.getName() : null);
                            sectionPakMap.put("questions", questionList);
                            sectionPakMap.put("postQuestions", postQuestionList);
                            sectionPakMap.put("showAssigned", teacherAssignmentRecord != null && questionMap.values().stream().allMatch(q -> teacherAssignmentRecord.getQuestionInfo().getOrDefault(q.getDocId(), 0) > 0));
                            sectionPakMap.put("seconds", questionMap.values().stream().mapToInt(q -> SafeConverter.toInt(q.getSeconds())).sum());
                            smallPackages.add(sectionPakMap);
                            questionCount += SafeConverter.toInt(questionList.size());
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(smallPackages)) {
                    hugePakMap.put("id", intelligentDiagnosisHugePak.getPakId());
                    hugePakMap.put("title", intelligentDiagnosisHugePak.getPakName());
                    hugePakMap.put("description", intelligentDiagnosisHugePak.getDescription());
                    hugePakMap.put("smallPackages", smallPackages);
                    hugePakMap.put("seconds", smallPackages.stream().mapToInt(q -> SafeConverter.toInt(q.get("seconds"))).sum());
                    hugePakMap.put("questionCount", questionCount);
                    // 大题包是否全部布置过
                    boolean showAssigned = true;
                    for (Map<String, Object> smallPackage : smallPackages) {
                        boolean assigned = SafeConverter.toBoolean(smallPackage.get("showAssigned"));
                        if (!assigned) {
                            showAssigned = false;
                            break;
                        }
                    }
                    hugePakMap.put("showAssigned", showAssigned);
                    content.add(hugePakMap);
                }
            }
        }
        return content;
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        return super.previewContent(teacher, bookId, contentIdList);
    }

    @Override
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> contentList = loadContent(mapper);
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        if (CollectionUtils.isNotEmpty(contentList)) {
            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "packages", contentList
            );
        }
        return Collections.emptyMap();
    }

    @Getter
    @Setter
    private static class MathIntelligentDiagnosisPak implements Serializable {
        private static final long serialVersionUID = -8087601098596763182L;

        private String unitId;  //单元id
        private List<MathIntelligentDiagSectionPak> sectionPaks; //题包按照section前后顺序排列
    }

    @Getter
    @Setter
    private static class MathIntelligentDiagSectionPak implements Serializable {
        private static final long serialVersionUID = -7505327884513102297L;

        private String sectionId;   // sectionId
        private String packageName; // 题包名称: section名称+空格+讲练测
        private List<MathIntellVariantPak> variantPaks; //每个变式一道题
    }

    @Getter
    @Setter
    private static class MathIntellVariantPak implements Serializable {
        private static final long serialVersionUID = 3044273412711600738L;

        private String variantId;       // 变式id
        private String questionId;      // 取课程C关联的诊断题目id(前测题)
        private String courseId;        // 课程id
        private List<String> postQuestionId;  //后测题id
    }
}
