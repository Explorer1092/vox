package com.voxlearning.utopia.service.newhomework.impl.template.internal.content.termreview;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.recom.entity.paks.AthenaReviewPackage;
import com.voxlearning.athena.api.recom.entity.paks.AthenaReviewPackageQuestion;
import com.voxlearning.athena.api.recom.entity.paks.AthenaReviewPackageType;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.mapper.assign.TermReviewCommonBO;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.template.TermReviewContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.TermReview;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisVariant;
import com.voxlearning.utopia.service.question.api.mapper.review.ChineseReview;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhangbin
 * @since 2017/11/9
 */

abstract public class AbstractTermReviewContentLoader extends TermReviewContentLoaderTemplate {

    @Override
    public MapMessage loadNewContent(Teacher teacher,
                                     List<Long> groupIds,
                                     String bookId,
                                     TermReviewContentType termReviewContentType) {
        AthenaReviewPackageType athenaReviewPackageType = null;
        TermReview.ChineseModule chineseModule = null;
        List<TermReviewCommonBO> termReviewCommonBOList;

        // 语文数据调用内容接口，英语、数学调用大数据接口
        Subject subject = getBookSubject(teacher, bookId);
        if (!Subject.CHINESE.equals(subject)) {
            for (AthenaReviewPackageType reviewPackageType : AthenaReviewPackageType.values()) {
                String athenaName = termReviewContentType.getAthenaName();
                AthenaReviewPackageType type = null;
                try {
                    type = AthenaReviewPackageType.valueOf(athenaName);
                } catch (Exception ignore) {
                }
                if (reviewPackageType.equals(type)) {
                    athenaReviewPackageType = type;
                    break;
                }
            }
            termReviewCommonBOList = loadEnglishOrMathContent(
                    athenaReviewPackageType,
                    termReviewContentType,
                    teacher,
                    bookId);
        } else {
            for (TermReview.ChineseModule module : TermReview.ChineseModule.values()) {
                String athenaName = termReviewContentType.getAthenaName();
                TermReview.ChineseModule m = null;
                try {
                    m = TermReview.ChineseModule.valueOf(athenaName);
                } catch (Exception ignore) {
                }
                if (module.equals(m)) {
                    chineseModule = m;
                    break;
                }
            }
            termReviewCommonBOList = loadChineseContent(
                    chineseModule,
                    termReviewContentType,
                    teacher,
                    bookId);
        }
        return MapMessage.successMessage().add("packages", termReviewCommonBOList);
    }


    private List<TermReviewCommonBO> loadChineseContent(TermReview.ChineseModule chineseModule,
                                                        TermReviewContentType termReviewContentType,
                                                        Teacher teacher,
                                                        String bookId) {
        List<ChineseReview> chineseReviewList;
        try {
            chineseReviewList = termReviewLoaderClient.loadChineseReviews(bookId, chineseModule);
        } catch (Exception ex) {
            logger.error("TermReviewContentLoader call teachingObjective error:", ex);
            return Collections.emptyList();
        }

        List<TermReviewCommonBO> termReviewCommonBOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(chineseReviewList)) {
            //所有的questionIds
            Set<String> allQuestionsIdSet = new HashSet<>();
            for (ChineseReview chineseReview : chineseReviewList) {
                List<String> questionIdList = chineseReview.getContents()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(TermReview.ChineseContent::getQuestionId)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(questionIdList)) {
                    questionIdList.stream()
                            .filter(Objects::nonNull)
                            .forEach(allQuestionsIdSet::add);
                }
            }

            Map<String, NewQuestion> idQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionsIdSet)
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
            Map<String, NewQuestion> docIdQuestionMap = idQuestionMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));

            //新题库-题型
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            //老师使用次数
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(
                    teacher.getSubject(),
                    teacher.getId(),
                    bookId
            );
            for (ChineseReview chineseReview : chineseReviewList) {
                TermReviewCommonBO termReviewCommonBO = new TermReviewCommonBO();
                String packageId = chineseReview.getPackageId();
                termReviewCommonBO.setId(packageId);
                termReviewCommonBO.setName(chineseReview.getName());
                termReviewCommonBO.setObjectiveConfigType(termReviewContentType.getObjectiveConfigType());
                termReviewCommonBO.setTypeName(termReviewContentType.getObjectiveConfigType().getValue());
                Integer teacherAssignTimes = 0;
                if (teacherAssignmentRecord != null
                        && MapUtils.isNotEmpty(teacherAssignmentRecord.getPackageInfo())
                        && teacherAssignmentRecord.getPackageInfo().get(packageId) != null) {
                    teacherAssignTimes = teacherAssignmentRecord.getPackageInfo().get(packageId);
                }
                termReviewCommonBO.setShowAssigned(teacherAssignTimes > 0);

                Map<String, String> questionIdUnitIdMap = new HashMap<>();
                chineseReview.getContents()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(e -> questionIdUnitIdMap.put(e.getQuestionId(), e.getCatalogId()));

                List<String> questionIdList = chineseReview.getContents()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(TermReview.ChineseContent::getQuestionId)
                        .collect(Collectors.toList());
                List<Map<String, Object>> questionMapList = loadQuestions(
                        bookId,
                        Subject.CHINESE,
                        questionIdList,
                        docIdQuestionMap,
                        idQuestionMap,
                        questionIdUnitIdMap,
                        contentTypeMap,
                        teacherAssignmentRecord,
                        null,
                        null,
                        null,
                         null
                );
                Long seconds = questionMapList
                        .stream()
                        .mapToInt(e -> SafeConverter.toInt(e.get("seconds")))
                        .summaryStatistics()
                        .getSum();
                termReviewCommonBO.setQuestionNum(questionMapList.size());
                termReviewCommonBO.setSeconds(seconds);
                termReviewCommonBO.setQuestions(questionMapList);
                if (CollectionUtils.isNotEmpty(questionMapList)) {
                    termReviewCommonBOList.add(termReviewCommonBO);
                }
            }
        }
        return termReviewCommonBOList;
    }

    private List<TermReviewCommonBO> loadEnglishOrMathContent(AthenaReviewPackageType athenaReviewPackageType,
                                                              TermReviewContentType termReviewContentType,
                                                              Teacher teacher,
                                                              String bookId) {
        List<AthenaReviewPackage> athenaReviewPackageList;
        try {
            athenaReviewPackageList = athenaReviewLoaderClient
                    .getAthenaReviewLoader()
                    .getPackage(Collections.singletonList(athenaReviewPackageType), bookId, teacher.getId());
        } catch (Exception ex) {
            logger.error("TermReviewContentLoader call athena error:", ex);
            return Collections.emptyList();
        }

        List<TermReviewCommonBO> termReviewCommonBOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(athenaReviewPackageList)) {
            //所有的DocIds
            Set<String> allQuestionDocIdSet = new HashSet<>();
            for (AthenaReviewPackage athenaReviewPackage : athenaReviewPackageList) {
                List<String> docIdList = athenaReviewPackage.getQuestions()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(AthenaReviewPackageQuestion::getDocId)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(docIdList)) {
                    docIdList.stream()
                            .filter(Objects::nonNull)
                            .forEach(allQuestionDocIdSet::add);
                }
            }

            Map<String, NewQuestion> idQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionDocIdSet)
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
            Map<String, NewQuestion> docIdQuestionMap = idQuestionMap.values()
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));

            //新题库-题型
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            //老师使用次数
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(
                    teacher.getSubject(),
                    teacher.getId(),
                    bookId
            );

            Subject subject;
            NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
            if (newBookProfile == null || newBookProfile.getSubjectId() == null) {
                subject = teacher.getSubject();
            } else {
                subject = Subject.fromSubjectId(newBookProfile.getSubjectId());
            }
            for (AthenaReviewPackage athenaReviewPackage : athenaReviewPackageList) {
                TermReviewCommonBO termReviewCommonBO = new TermReviewCommonBO();
                String packageId = athenaReviewPackage.getId();
                termReviewCommonBO.setId(packageId);
                termReviewCommonBO.setName(athenaReviewPackage.getName());
                termReviewCommonBO.setObjectiveConfigType(termReviewContentType.getObjectiveConfigType());
                termReviewCommonBO.setTypeName(termReviewContentType.getObjectiveConfigType().getValue());
                Integer teacherAssignTimes = 0;
                if (teacherAssignmentRecord != null
                        && MapUtils.isNotEmpty(teacherAssignmentRecord.getPackageInfo())
                        && teacherAssignmentRecord.getPackageInfo().get(packageId) != null) {
                    teacherAssignTimes = teacherAssignmentRecord.getPackageInfo().get(packageId);
                }
                termReviewCommonBO.setShowAssigned(teacherAssignTimes > 0);

                Map<String, String> docIdUnitIdMap = new HashMap<>();
                athenaReviewPackage.getQuestions()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(e -> docIdUnitIdMap.put(e.getDocId(), e.getBookCatalogId()));
                Map<String, AthenaReviewPackageQuestion> questionDocIdsMap = athenaReviewPackage.getQuestions()
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(AthenaReviewPackageQuestion::getDocId, Function.identity()));
                List<String> docIdList = athenaReviewPackage.getQuestions()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(AthenaReviewPackageQuestion::getDocId)
                        .collect(Collectors.toList());
                Map<String, IntelDiagnosisVariant> intelDiagnosisVariantMap = new HashMap<>();
                Set<String> variantIdSet = athenaReviewPackage.getQuestions().stream()
                        .filter(p -> p != null && StringUtils.isNotEmpty(p.getVariantId()))
                        .map(AthenaReviewPackageQuestion::getVariantId)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(variantIdSet)) {
                    intelDiagnosisVariantMap = intelDiagnosisClient.loadIntelDiagnosisVariantByIdIncludeDisabled(variantIdSet);
                }
                Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = new HashMap<>();
                Set<String> courseIdSet = athenaReviewPackage.getQuestions().stream()
                        .filter(p -> p != null && StringUtils.isNotEmpty(p.getCourseId()))
                        .map(AthenaReviewPackageQuestion::getCourseId)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(courseIdSet)) {
                    intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIdSet);
                }
                Map<String, NewQuestion> similarQuestionMap = new HashMap<>();
                Set<String> similarQuestionIds = athenaReviewPackage.getQuestions().stream()
                        .filter(p -> p != null && StringUtils.isNotEmpty(p.getSimilarQid()))
                        .map(AthenaReviewPackageQuestion::getSimilarQid)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(similarQuestionIds)) {
                    List<NewQuestion> similarQuestionList = questionLoaderClient.loadQuestionByDocIds(similarQuestionIds);
                    if (CollectionUtils.isNotEmpty(similarQuestionList)) {
                        similarQuestionMap = similarQuestionList.stream().collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
                    }
                }
                List<Map<String, Object>> questionMapList = loadQuestions(
                        bookId,
                        subject,
                        docIdList,
                        docIdQuestionMap,
                        idQuestionMap,
                        docIdUnitIdMap,
                        contentTypeMap,
                        teacherAssignmentRecord,
                        questionDocIdsMap,
                        intelDiagnosisVariantMap,
                        intelDiagnosisCourseMap,
                        similarQuestionMap
                );
                Long seconds = questionMapList
                        .stream()
                        .mapToInt(e -> SafeConverter.toInt(e.get("seconds")))
                        .summaryStatistics()
                        .getSum();
                termReviewCommonBO.setQuestionNum(questionMapList.size());
                termReviewCommonBO.setSeconds(seconds);
                termReviewCommonBO.setQuestions(questionMapList);
                if (CollectionUtils.isNotEmpty(questionMapList)) {
                    termReviewCommonBOList.add(termReviewCommonBO);
                }
            }
        }
        return termReviewCommonBOList;
    }

    private List<Map<String, Object>> loadQuestions(String bookId,
                                                    Subject subject,
                                                    List<String> idList,
                                                    Map<String, NewQuestion> docIdQuestionMap,
                                                    Map<String, NewQuestion> idQuestionMap,
                                                    Map<String, String> idUnitIdMap,
                                                    Map<Integer, NewContentType> contentTypeMap,
                                                    TeacherAssignmentRecord teacherAssignmentRecord,
                                                    Map<String,AthenaReviewPackageQuestion> questionDocIdsMap,
                                                    Map<String, IntelDiagnosisVariant> intelDiagnosisVariantMap,
                                                    Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap,
                                                    Map<String, NewQuestion> similarQuestionMap
                                                    ) {
        List<Map<String, Object>> questionMapList = new ArrayList<>();

        //试题类型白名单
        List<Integer> contentTypeList = Subject.ENGLISH.equals(subject)
                ? QuestionConstants.englishExamIncludeContentTypeIds
                : Subject.MATH.equals(subject)
                ? QuestionConstants.homeworkMathIncludeContentTypeIds
                : QuestionConstants.examChineseIncludeContentTypeIds;

        if (CollectionUtils.isNotEmpty(idList)) {
            for (String id : idList) {
                NewQuestion newQuestion = Subject.CHINESE.equals(subject)
                        ? idQuestionMap.get(id)
                        : docIdQuestionMap.get(id);
                if (newQuestion != null
                        && contentTypeList.contains(newQuestion.getContentTypeId())
                        && newQuestion.supportOnlineAnswer()
                        && !Objects.equals(newQuestion.getNotFitMobile(), 1)) {
                    EmbedBook book = new EmbedBook();
                    book.setBookId(bookId);
                    book.setUnitId(idUnitIdMap.get(id));
                    Map<String, Object> question = NewHomeworkContentDecorator.decorateNewQuestion(
                            newQuestion,
                            contentTypeMap,
                            Collections.emptyMap(),
                            teacherAssignmentRecord,
                            book
                    );
                    if (MapUtils.isNotEmpty(questionDocIdsMap) && questionDocIdsMap.get(id) != null) {
                        AthenaReviewPackageQuestion packageQuestion = questionDocIdsMap.get(id);
                        question.put("variantId", SafeConverter.toString(packageQuestion.getVariantId()));
                        if (StringUtils.isNotEmpty(packageQuestion.getVariantId())) {
                            if (MapUtils.isNotEmpty(intelDiagnosisVariantMap) && MapUtils.isNotEmpty(intelDiagnosisVariantMap)) {
                                IntelDiagnosisVariant intelDiagnosisVariant = intelDiagnosisVariantMap.get(packageQuestion.getVariantId());
                                question.put("variantName", intelDiagnosisVariant != null ? intelDiagnosisVariant.getCoreMission() : "");
                            }
                        }
                        if (StringUtils.isNotEmpty(packageQuestion.getCourseId())) {
                            question.put("courseId", packageQuestion.getCourseId());
                            if (MapUtils.isNotEmpty(intelDiagnosisCourseMap) && MapUtils.isNotEmpty(intelDiagnosisCourseMap)) {
                                IntelDiagnosisCourse intelDiagnosisCourse = intelDiagnosisCourseMap.get(packageQuestion.getCourseId());
                                question.put("courseName", intelDiagnosisCourse == null ? "" : intelDiagnosisCourse.getName());
                            }
                        }
                        String similarQid = packageQuestion.getSimilarQid();
                        if (MapUtils.isNotEmpty(similarQuestionMap) && StringUtils.isNotEmpty(similarQid)) {
                            NewQuestion postQuestion = similarQuestionMap.get(similarQid);
                            if (postQuestion != null) {
                                Map<String, Object> postMap = NewHomeworkContentDecorator.decorateNewQuestion(postQuestion, new HashMap<>(), new HashMap<>(), null, book);
                                question.put("postQuestions", Collections.singleton(postMap));
                            }
                        }
                    }
                    question.put("sourceType", getTermReviewContentType());
                    questionMapList.add(question);
                }
            }
        }
        return questionMapList;
    }

}
