package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignselfstudy;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.context.AssignSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.COURSE_APP_CONFIGTYPE;

/**
 * @author xuesong.zhang
 * @since 2017/1/22
 */
@Named
public class AHSS_ProcessHomeworkContent extends SpringContainerSupport implements AssignSelfStudyHomeworkTask {

    @Inject private NewContentLoaderClient newContentLoaderClient;

    @Override
    public void execute(AssignSelfStudyHomeworkContext context) {
        HomeworkSource homeworkSource = context.getSource();
        try {
            //处理作业内容
            processPractices(context, homeworkSource);
            //处理课本相关信息
            processBooks(context, homeworkSource);
            if (homeworkSource.containsKey("remark")) {
                context.setRemark(SafeConverter.toString(homeworkSource.get("remark")));
            }
            if (homeworkSource.containsKey("des")) {
                context.setDes(SafeConverter.toString(homeworkSource.get("des")));
            }
        } catch (Exception ex) {
            context.errorResponse("SelfStudyHomework content is error homeworkSource:{}, errorInfo {}", JsonUtils.toJson(homeworkSource), ex);
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT);
            context.setTerminateTask(true);
        }
    }

    private void processPractices(AssignSelfStudyHomeworkContext context, HomeworkSource homeworkSource) {
        String practiceJson = JsonUtils.toJson(homeworkSource.get("practices"));
        Map<String, Object> practiceMap = JsonUtils.fromJson(practiceJson);
        if (MapUtils.isNotEmpty(practiceMap)) {
            for (String key : practiceMap.keySet()) {
                Map<String, Object> practice = JsonUtils.fromJson(JsonUtils.toJson(practiceMap.get(key)));
                List<HwQuestion> questions = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("questions")), HwQuestion.class);
                ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.valueOf(key);
                if (COURSE_APP_CONFIGTYPE.contains(objectiveConfigType)) {
                    context = processHomeworkAppContent(context, questions, objectiveConfigType);
                } else {
                    context = processHomeworkContent(context, questions, objectiveConfigType);
                }
                if (context.isTerminateTask()) {
                    return;
                }
            }
            if (CollectionUtils.isEmpty(context.getPractices())) {
                context.errorResponse("SelfStudyHomework practices is empty homeowrkSource:{}", JsonUtils.toJson(homeworkSource));
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT);
                context.setTerminateTask(true);
            }
        }
    }

    private void processBooks(AssignSelfStudyHomeworkContext context, HomeworkSource homeworkSource) {
        Map<String, List<Map>> practicesBooksMap = JsonUtils.fromJson(JsonUtils.toJson(homeworkSource.get("books")), Map.class);

        for (String objectiveConfigTypeStr : practicesBooksMap.keySet()) {
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
            List<NewHomeworkBookInfo> bookInfos = new ArrayList<>();
            List<Map> books = JsonUtils.fromJsonToList(JsonUtils.toJson(practicesBooksMap.get(objectiveConfigTypeStr)), Map.class);
            Set<String> catalogIdSet = new HashSet<>();
            if (CollectionUtils.isNotEmpty(books)) {
                books.forEach(book -> {
                    catalogIdSet.add(SafeConverter.toString(book.get("bookId")));
                    catalogIdSet.add(SafeConverter.toString(book.get("unitId")));
                });
            }
            Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(catalogIdSet);
            for (Map book : books) {
                NewHomeworkBookInfo bookInfo = new NewHomeworkBookInfo();
                if (book.containsKey("bookId")) {
                    String bookId = SafeConverter.toString(book.get("bookId"));
                    bookInfo.setBookId(bookId);
                    if (newBookCatalogMap.get(bookId) != null) {
                        bookInfo.setBookName(newBookCatalogMap.get(bookId).getName());
                    }
                }
                if (book.containsKey("unitId")) {
                    String unitId = SafeConverter.toString(book.get("unitId"));
                    bookInfo.setUnitId(unitId);
                    NewBookCatalog newBookCatalog = newBookCatalogMap.get(unitId);
                    if (newBookCatalog != null) {
                        bookInfo.setUnitName(newBookCatalog.getName());
                    }
                }

                if (book.containsKey("includeQuestions")) {
                    bookInfo.setQuestions(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("includeQuestions")), String.class));
                }

                if (book.containsKey("includePictureBooks")) {
                    bookInfo.setPictureBooks(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("includePictureBooks")), String.class));
                }
                bookInfos.add(bookInfo);
            }
            if (CollectionUtils.isNotEmpty(bookInfos)) {
                context.getPracticesBooksMap().put(objectiveConfigType, bookInfos);
            }
        }
    }

    private AssignSelfStudyHomeworkContext processHomeworkContent(AssignSelfStudyHomeworkContext context, List<HwQuestion> questions, ObjectiveConfigType objectiveConfigType) {
        List<NewHomeworkQuestion> newHomeworkQuestions = new ArrayList<>();
        //该作业类型是否有主观答题
        Boolean isIncludeSubjective = false;
        Set<String> similarQuestionIds = new HashSet<>();
        for (HwQuestion question : questions) {
            String similarQuestionId = question.getSimilarQuestionId();
            if(similarQuestionIds.contains(similarQuestionId))continue;
            NewHomeworkQuestion nhq = new NewHomeworkQuestion();
            nhq.setQuestionId(question.getQuestionId());
            nhq.setSeconds(question.getSeconds());
            nhq.setSubmitWay(question.getSubmitWay());
            nhq.setSimilarQuestionId(similarQuestionId);
            if (CollectionUtils.isNotEmpty(nhq.getSubmitWay()) && nhq.isSubjectiveQuestion()) {
                context.setIncludeSubjective(true);
                isIncludeSubjective = true;
            }
            // 每道题都是100分
            nhq.setScore(100D);
            newHomeworkQuestions.add(nhq);
            similarQuestionIds.add(similarQuestionId);
        }
        if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
            NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
            nhpc.setType(objectiveConfigType);
            nhpc.setQuestions(newHomeworkQuestions);
            nhpc.setIncludeSubjective(isIncludeSubjective);
            context.getPractices().add(nhpc);
        }
        return context;
    }

    @Data
    private static class HwQuestion implements Serializable {
        private static final long serialVersionUID = -8100960491373503073L;
        private String questionId; //题id
        private String kpId; //知识点id
        private Integer seconds;
        private List<List<Integer>> submitWay;
        private String courseId; //课程ID
        private String errorCause;  //错因
        private Integer courseOrder;  //课程顺序
        private String similarQuestionId;
        private String experimentId;            //实验ID
        private String experimentGroupId;       //实验组ID
        private String questionBoxId;           //原作业题包ID
        private NewHomeworkApp.DiagnosisSource diagnosisSource; //诊断来源
    }

    private AssignSelfStudyHomeworkContext processHomeworkAppContent(AssignSelfStudyHomeworkContext context, List<HwQuestion> questions, ObjectiveConfigType objectiveConfigType) {

        Map<String, List<HwQuestion>> courseQuestionMap = questions.stream().collect(Collectors.groupingBy(HwQuestion::getCourseId));
        Map<String, List<ErrorQuestion>> courseErrorQuestionMap =  new HashMap<>();
        Map<String, List<ErrorKpoint>> courseErrorKpointMap = new HashMap<>();
        Map<String, List<NewHomeworkQuestion>> courseSimilarQuestionMap = new HashMap<>();
        for(List<HwQuestion> hqs : courseQuestionMap.values()){
            Set<String> errorkeySet = new HashSet<>(); //排重原题
            Set<String> similarkeySet = new HashSet<>(); //排重类题
            Set<String> pointkeySet = new HashSet<>(); //排重原题知识点（纸质拍照）
            for(HwQuestion hw : hqs){
                String courseId = hw.getCourseId();
                if(StringUtils.isNoneBlank(hw.getQuestionId())){
                    List<ErrorQuestion> eqs = courseErrorQuestionMap.get(courseId);
                    if(eqs == null){
                        eqs = new ArrayList<>();
                    }
                    if(!errorkeySet.contains(hw.getQuestionId())){
                        errorkeySet.add(hw.getQuestionId());
                        eqs.add(new ErrorQuestion(hw.getQuestionId(), hw.getErrorCause()));
                        courseErrorQuestionMap.put(courseId, eqs);
                    }
                }

                if(StringUtils.isNoneBlank(hw.getKpId())){
                    List<ErrorKpoint> ekps = courseErrorKpointMap.get(courseId);
                    if(ekps == null){
                        ekps = new ArrayList<>();
                    }
                    if(!pointkeySet.contains(hw.getKpId())){
                        pointkeySet.add(hw.getKpId());
                        ekps.add(new ErrorKpoint(hw.getKpId(), hw.getErrorCause()));
                        courseErrorKpointMap.put(courseId, ekps);
                    }
                }

                if (!ObjectiveConfigType.ORAL_INTERVENTIONS.equals(objectiveConfigType)) {
                    List<NewHomeworkQuestion> nqs =courseSimilarQuestionMap.get(courseId);
                    if(nqs == null){
                        nqs = new ArrayList<>();
                    }
                    if(!similarkeySet.contains(hw.getSimilarQuestionId())){
                        similarkeySet.add(hw.getSimilarQuestionId());
                        NewHomeworkQuestion nq = new NewHomeworkQuestion();
                        nq.setSeconds(hw.getSeconds());
                        nq.setSubmitWay(hw.getSubmitWay());
                        nq.setQuestionId(hw.getSimilarQuestionId());
                        nq.setScore(100D);
                        if (nq.isSubjectiveQuestion()) {
                            context.setIncludeSubjective(true);
                        }
                        nqs.add(nq);
                        courseSimilarQuestionMap.put(courseId, nqs);
                    }
                }
            }
        }

        List<NewHomeworkApp> apps = Lists.newLinkedList();
        for (Map.Entry<String, List<HwQuestion>> courseEntry : courseQuestionMap.entrySet()) {
            String courseId = courseEntry.getKey();
            //优先保留实验
            List<HwQuestion> hws = courseEntry.getValue().stream().sorted(Comparator.comparingInt(h-> NewHomeworkApp.DiagnosisSource.OcrDiagnosis.equals(h.getDiagnosisSource())? 0 : 1)).collect(Collectors.toList());
            HwQuestion hw = hws.stream().filter(o -> o.getExperimentId() != null).findFirst().orElse(null);
            if (hw == null) {
                hw = hws.get(0);
            }
            NewHomeworkApp nha = new NewHomeworkApp();
            nha.setCourseId(courseId);
            nha.setCourseOrder(hw.getCourseOrder());
            nha.setErrorQuestions(courseErrorQuestionMap.get(courseId));
            nha.setErrorKpoints(courseErrorKpointMap.get(courseId));
            nha.setQuestions(courseSimilarQuestionMap.get(courseId));
            nha.setExperimentId(hw.getExperimentId());
            nha.setExperimentGroupId(hw.getExperimentGroupId());
            nha.setQuestionBoxId(hw.getQuestionBoxId());
            nha.setDiagnosisSource(hw.getDiagnosisSource());
            apps.add(nha);
        }

        if (!apps.isEmpty()) {
            NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
            nhpc.setType(objectiveConfigType);
            nhpc.setIncludeSubjective(context.isIncludeSubjective());
            nhpc.setApps(apps);
            context.getPractices().add(nhpc);
        }
        return context;
    }

}
