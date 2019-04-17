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

package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.athena.api.recom.entity.paks.KnowledgePointQuestionInfo;
import com.voxlearning.athena.api.recom.entity.paks.KnowledgePointQuestionPackage;
import com.voxlearning.athena.api.recom.entity.wrapper.ClazzKnowledgePointPackageWrapper;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.mapper.assign.KnowledgePointQuestionBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.assign.KnowledgeReviewBO;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.AthenaHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 类NewHomeworkKnowledgeReviewContentLoader的实现，小学数学 知识点查漏补缺推题接口
 *
 * @author zhangbin
 * @since 2017/1/5 18:07
 */
@Named
public class NewHomeworkKnowledgeReviewContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject
    private AthenaHomeworkLoaderClient athenaHomeworkLoaderClient;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.KNOWLEDGE_REVIEW;
    }

    @Override
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        TeacherDetail teacher = mapper.getTeacher();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();

        //最后一个section和这个section前面的所有section
        List<String> moreSectionIds = getMoreSectionIds(unitId, mapper.getSectionIds());

        Map<Long, Collection<String>> groupBookCatalogIds = new HashMap<>();
        if (CollectionUtils.isNotEmpty(mapper.getGroupIds())) {
            for (Long groupId : mapper.getGroupIds()) {
                groupBookCatalogIds.put(groupId, moreSectionIds);
            }
        }
        Integer subjectId = teacher.getSubject().getId();
        List<ClazzKnowledgePointPackageWrapper> clazzKnowledgePointPackageWrapper = new ArrayList<>();
        try {
            clazzKnowledgePointPackageWrapper = athenaHomeworkLoaderClient.getAthenaHomeworkLoader()
                    .loadKnowledgePointQuestionPaks(groupBookCatalogIds, bookId, subjectId);
        } catch (Exception ex) {
            logger.error("newHomeworkKnowledgeReviewContent call athena error:", ex);
        }

        //新题库-题型
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        //试题类型白名单
        List<Integer> contentTypeList = teacher.getSubject() == Subject.ENGLISH ? QuestionConstants.englishExamIncludeContentTypeIds :
                teacher.getSubject() == Subject.MATH ? QuestionConstants.homeworkMathIncludeContentTypeIds : QuestionConstants
                        .examChineseIncludeContentTypeIds;
        //老师使用次数
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(),
                teacher.getId(), bookId);
        //处理题包
        if (clazzKnowledgePointPackageWrapper != null) {
            processPackageContent(content, clazzKnowledgePointPackageWrapper, bookId, unitId, contentTypeMap, teacherAssignmentRecord, contentTypeList, mapper.getCurrentPageNum());
        }
        return content;
    }

    private void processPackageContent(List<Map<String, Object>> content, List<ClazzKnowledgePointPackageWrapper> clazzKnowledgePointPackageWrapperList,
                                       String bookId, String unitId, Map<Integer, NewContentType> contentTypeMap,
                                       TeacherAssignmentRecord teacherAssignmentRecord, List<Integer> contentTypeList, Integer currentPageNum) {
        if (CollectionUtils.isNotEmpty(clazzKnowledgePointPackageWrapperList)) {
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);

            //获取所有有数据的课时集合
            Set<String> sectionIdSet = clazzKnowledgePointPackageWrapperList.stream()
                    .filter(Objects::nonNull)
                    .filter(wrapper -> MapUtils.isNotEmpty(wrapper.getQuestionPackageMap()))
                    .map(wrapper -> wrapper.getQuestionPackageMap().keySet())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            if (CollectionUtils.isNotEmpty(sectionIdSet)) {
                List<NewBookCatalog> allSections = getUnitSections(unitId);
                List<String> sortedSectionIds = allSections.stream()
                        .filter(Objects::nonNull)
                        .filter(section -> section.getId() != null)
                        .filter(section -> sectionIdSet.contains(section.getId()))
                        .map(NewBookCatalog::getId)
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(sortedSectionIds)) {
                    List<KnowledgeReviewBO> knowledgeReviewBOList = new ArrayList<>();
                    Map<String, NewBookCatalog> sectionMap = newContentLoaderClient.loadBookCatalogByCatalogIds(sortedSectionIds);
                    //分页，每页取两个section
                    int sectionSize = sortedSectionIds.size();
                    for (int i = (currentPageNum - 1) * 2; i < currentPageNum * 2 && i < sectionSize; i++) {
                        KnowledgeReviewBO knowledgeReviewBO = new KnowledgeReviewBO();
                        String sectionId = sortedSectionIds.get(i);
                        if (StringUtils.isNotBlank(sectionId)) {
                            knowledgeReviewBO.setSectionId(sectionId);
                            if (sectionMap.get(sectionId) != null) {
                                book.setSectionId(sectionId);
                                knowledgeReviewBO.setSectionName(sectionMap.get(sectionId).getName());
                                List<KnowledgePointQuestionBO> knowledgePointQuestionBOList = new ArrayList<>();

                                //按班级分组并按照班级排序
                                Comparator<ClazzKnowledgePointPackageWrapper> comp = Comparator.comparing(ClazzKnowledgePointPackageWrapper
                                        ::getClazzGroupId);
                                List<ClazzKnowledgePointPackageWrapper> sortedClazzGroupWrapperList = clazzKnowledgePointPackageWrapperList
                                        .stream()
                                        .sorted(comp)
                                        .collect(Collectors.toList());
                                for (ClazzKnowledgePointPackageWrapper clazzKnowledgePointPackageWrapper : sortedClazzGroupWrapperList) {
                                    KnowledgePointQuestionBO knowledgePointQuestionBO = new KnowledgePointQuestionBO();
                                    Long groupId = SafeConverter.toLong(clazzKnowledgePointPackageWrapper.getClazzGroupId());
                                    knowledgePointQuestionBO.setGroupId(groupId);
                                    //获取班级名称
                                    Map<Long, GroupMapper> groupMap = groupLoaderClient.loadGroups(Collections.singletonList(groupId), false);
                                    String clazzName = "";
                                    if (groupMap.get(groupId) != null) {
                                        Long clazzId = groupMap.get(groupId).getClazzId();
                                        Map<Long, Clazz> classMap = raikouSDK.getClazzClient()
                                                .getClazzLoaderClient()
                                                .loadClazzs(Collections.singleton(clazzId))
                                                .stream()
                                                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
                                        Clazz clazz = classMap.get(clazzId);
                                        if (clazz != null) {
                                            clazzName = clazz.formalizeClazzName();
                                            knowledgePointQuestionBO.setGroupName(clazzName);
                                        }
                                    }

                                    Map<String, KnowledgePointQuestionPackage> questionPackageMap = clazzKnowledgePointPackageWrapper
                                            .getQuestionPackageMap();
                                    KnowledgePointQuestionPackage knowledgePointQuestionPackage = questionPackageMap.get(sectionId);
                                    if (knowledgePointQuestionPackage != null) {
                                        //获取当前请求页的DocIds
                                        Set<String> allQuestionDocIdSet = new HashSet<>();
                                        if (CollectionUtils.isNotEmpty(knowledgePointQuestionPackage.getKnowledgePointQuestionInfos())) {
                                            //当前课时
                                            knowledgePointQuestionPackage.getKnowledgePointQuestionInfos()
                                                    .stream()
                                                    .filter(e -> e.getDocId() != null)
                                                    .forEach(e -> allQuestionDocIdSet.add(e.getDocId()));
                                        }
                                        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionDocIdSet)
                                                .stream()
                                                .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
                                        Map<String, NewQuestion> docIdQuestionMap = allQuestionMap.values()
                                                .stream()
                                                .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
                                        //总的使用次数
                                        Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader
                                                .loadTotalAssignmentRecordByContentType
                                                        (Subject.MATH, allQuestionMap.keySet(), HomeworkContentType.QUESTION);

                                        knowledgePointQuestionBO.setId(knowledgePointQuestionPackage.getId() == null ? RandomUtils.randomNumeric(5) :
                                                knowledgePointQuestionPackage.getId());
                                        knowledgePointQuestionBO.setName(knowledgePointQuestionPackage.getName() == null ? clazzName + " 知识点查缺补漏" :
                                                knowledgePointQuestionPackage.getName());

                                        //知识点
                                        if (CollectionUtils.isNotEmpty(knowledgePointQuestionPackage.getKnowledgePoints())) {
                                            List<Map<String, Object>> knowledgePointList = new ArrayList<>();
                                            Map<String, String> kpMap = loadKpName(knowledgePointQuestionPackage.getKnowledgePoints());
                                            kpMap.forEach((id, name) -> knowledgePointList.add(MapUtils.m("id", id, "name", name)));
                                            knowledgePointQuestionBO.setKnowledgePointList(knowledgePointList);
                                            knowledgePointQuestionBO.setKnowledgePointNum(knowledgePointList.size());
                                        }

                                        List<Map<String, Object>> questionMapList = new ArrayList<>();
                                        List<KnowledgePointQuestionInfo> knowledgePointQuestionInfoList = knowledgePointQuestionPackage
                                                .getKnowledgePointQuestionInfos();
                                        if (CollectionUtils.isNotEmpty(knowledgePointQuestionInfoList)) {
                                            for (KnowledgePointQuestionInfo knowledgePointQuestionInfo : knowledgePointQuestionInfoList) {
                                                String docId = knowledgePointQuestionInfo.getDocId();
                                                NewQuestion newQuestion = docIdQuestionMap.get(docId);
                                                Map<String, Object> kpIdMap = new HashMap<>();
                                                kpIdMap.put("kpId", knowledgePointQuestionInfo.getKnowledgePointId());
                                                if (newQuestion != null
                                                        && contentTypeList.contains(newQuestion.getContentTypeId())
                                                        && newQuestion.supportOnlineAnswer()
                                                        && !Objects.equals(newQuestion.getNotFitMobile(), 1)) {
                                                    Map<String, Object> question = NewHomeworkContentDecorator.decorateNewQuestion(newQuestion,
                                                            contentTypeMap, totalAssignmentRecordMap,
                                                            teacherAssignmentRecord, book);
                                                    question.putAll(kpIdMap);
                                                    questionMapList.add(question);
                                                }
                                            }
                                        }
                                        knowledgePointQuestionBO.setQuestionNum(questionMapList.size());
                                        Long seconds = questionMapList
                                                .stream()
                                                .mapToInt(e -> SafeConverter.toInt(e.get("seconds")))
                                                .summaryStatistics()
                                                .getSum();
                                        knowledgePointQuestionBO.setSeconds(seconds);
                                        knowledgePointQuestionBO.setQuestions(questionMapList);
                                    }
                                    if (CollectionUtils.isNotEmpty(knowledgePointQuestionBO.getQuestions())) {
                                        knowledgePointQuestionBOList.add(knowledgePointQuestionBO);
                                    }
                                }
                                if (CollectionUtils.isNotEmpty(knowledgePointQuestionBOList)) {
                                    knowledgeReviewBO.setKnowledgePointQuestionBOList(knowledgePointQuestionBOList);
                                    knowledgeReviewBOList.add(knowledgeReviewBO);
                                }
                            }
                        }
                    }
                    content.add(MapUtils.m("type", "package", "totalPages", Math.ceil((double) sectionSize / 2), "currentPageNum", currentPageNum,
                            "packages", knowledgeReviewBOList));
                }
            }
        }
    }

    /**
     * 默认展示老师已选的最后一个section和这个section前面的所有section
     *
     * @param unitId             单元id
     * @param selectedSectionIds 已选课时ids
     */
    private List<String> getMoreSectionIds(String unitId, List<String> selectedSectionIds) {
        List<NewBookCatalog> allSections = getUnitSections(unitId);
        List<String> allSectionIds = allSections.stream()
                .filter(Objects::nonNull)
                .filter(section -> section.getId() != null)
                .map(NewBookCatalog::getId)
                .collect(Collectors.toList());

        //获取最后一个选中的section下标
        List<Integer> selectedSectionIdIndex = selectedSectionIds.stream()
                .map(allSectionIds::indexOf)
                .collect(Collectors.toList());
        int maxSectionIdIndex = selectedSectionIdIndex.stream()
                .mapToInt((e) -> e)
                .summaryStatistics()
                .getMin();

        if (maxSectionIdIndex < 0) {
            return allSectionIds;
        }
        return allSectionIds.subList(maxSectionIdIndex, allSectionIds.size());
    }

    /**
     * 根据unitId 获取该单元下有序section
     *
     * @param unitId 单元id
     */
    private List<NewBookCatalog> getUnitSections(String unitId) {
        // 获取unit下的lesson
        Map<String, List<NewBookCatalog>> unitLessonMap = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON);
        Set<String> lessonIds = unitLessonMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(NewBookCatalog::getId)
                .collect(Collectors.toSet());
        //获取lesson下的section
        Map<String, List<NewBookCatalog>> lessonSectionMap = newContentLoaderClient.loadChildren(lessonIds, BookCatalogType.SECTION);

        //按section降序
        Comparator<NewBookCatalog> comparator = (e1, e2) -> Integer.compare(SafeConverter.toInt(e2.getRank()), SafeConverter.toInt(e1.getRank()));
        List<NewBookCatalog> lessonsList = unitLessonMap.get(unitId)
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
        return lessonsList.stream()
                .filter(lesson -> lessonSectionMap.get(lesson.getId()) != null)
                .flatMap(lesson -> lessonSectionMap.get(lesson.getId())
                        .stream()
                        .sorted(comparator))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> content = previewSpecialExam(contentIdList);
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "content", content
        );
    }
}
