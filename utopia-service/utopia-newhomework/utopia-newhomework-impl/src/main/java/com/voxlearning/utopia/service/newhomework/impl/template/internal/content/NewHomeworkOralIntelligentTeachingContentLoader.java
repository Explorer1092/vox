package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.athena.api.cuotizhenduan.entity.EnglishIntelligentDiagnosisHugePak;
import com.voxlearning.athena.api.cuotizhenduan.entity.EnglishIntelligentDiagnosisPak;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.WrongQuestionDiagnosisLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.video.MicroVideoTask;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 口语讲练测
 * @author: Mr_VanGogh
 * @date: 2018/7/12 下午4:22
 */
@Named
public class NewHomeworkOralIntelligentTeachingContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject
    private WrongQuestionDiagnosisLoaderClient wrongQuestionDiagnosisLoaderClient;
    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        try {
            List<Map<String, Object>> content = new ArrayList<>();
            TeacherDetail teacher = mapper.getTeacher();
            String unitId = mapper.getUnitId();
            String bookId = mapper.getBookId();
            // 获取题包
            Map<String, List<EnglishIntelligentDiagnosisHugePak>> intelligentDiagnosisPakMap = new HashMap<>();
            try {
                intelligentDiagnosisPakMap = wrongQuestionDiagnosisLoaderClient.getCuotizhenduanLoader().loadEnglishIntelligentDiagnosisQuestionHugePaksV2(Collections.singletonList(unitId), "ORAL");
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
                Map<String, MicroVideoTask> allMicroVideoTaskMap = intelDiagnosisClient.loadMicroVideoTaskByIdsIncludeDisabled(allCourseIds);
                //所有的知识点素材信息
                Set<String> allKpMaterialIds = allMicroVideoTaskMap.values().stream().map(MicroVideoTask::getKpMaterialId).collect(Collectors.toSet());
                Map<String, KnowledgePointMaterial> allKnowledgePointMaterialMap = intelDiagnosisClient.loadKnowledgePointMaterialByIdsIncludeDisabled(allKpMaterialIds);

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
                        Map<String, MicroVideoTask> microVideoTaskMap = new HashMap<>();
                        courseIds.forEach(c -> {
                            MicroVideoTask microVideoTask = allMicroVideoTaskMap.get(c);
                            if (microVideoTask != null) {
                                microVideoTaskMap.put(c, microVideoTask);
                            }
                        });

                        if (MapUtils.isNotEmpty(normalQuestionInfoMap) && MapUtils.isNotEmpty(microVideoTaskMap)) {
                            //知识点素材信息
                            Set<String> kpMaterialIds = microVideoTaskMap.values().stream().map(MicroVideoTask::getKpMaterialId).collect(Collectors.toSet());
                            Map<String, KnowledgePointMaterial> knowledgePointMaterialMap = new HashMap<>();
                            kpMaterialIds.forEach(k -> {
                                KnowledgePointMaterial knowledgePointMaterial = allKnowledgePointMaterialMap.get(k);
                                if (knowledgePointMaterial != null) {
                                    knowledgePointMaterialMap.put(k, knowledgePointMaterial);
                                }
                            });
                            //教材信息
                            EmbedBook book = new EmbedBook();
                            book.setBookId(bookId);
                            book.setUnitId(unitId);

                            for (EnglishIntelligentDiagnosisPak englishIntelligentDiagnosisPak : smallPackageList) {
                                //题包信息
                                Map<String, Object> sectionPakMap = new LinkedHashMap<>();
                                // 视频课程信息
                                MicroVideoTask microVideoTask = microVideoTaskMap.get(englishIntelligentDiagnosisPak.getCourseId());
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
                                sectionPakMap.put("courseName", microVideoTask != null ? microVideoTask.getName() : null);
                                sectionPakMap.put("videoUrl", microVideoTask != null && knowledgePointMaterialMap.get(microVideoTask.getKpMaterialId()) != null ? knowledgePointMaterialMap.get(microVideoTask.getKpMaterialId()).getVideoUrl() : null);
                                sectionPakMap.put("thumbnail", microVideoTask != null && knowledgePointMaterialMap.get(microVideoTask.getKpMaterialId()) != null ? knowledgePointMaterialMap.get(microVideoTask.getKpMaterialId()).getImageUrl() : null);
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
        } catch (Exception e) {
            logger.error("Failed to load NewHomeworkOralIntelligentTeachingContent, mapper:{}", mapper, e);
            return Collections.emptyList();
        }
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
}
