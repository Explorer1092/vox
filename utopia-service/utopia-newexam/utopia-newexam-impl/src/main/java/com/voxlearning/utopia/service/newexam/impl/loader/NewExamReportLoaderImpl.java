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

package com.voxlearning.utopia.service.newexam.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.newexam.api.constant.ExamReportAnswerStatType;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.newexam.api.entity.*;
import com.voxlearning.utopia.service.newexam.api.loader.NewExamReportLoader;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamReportForClazz;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamReportForStudent;
import com.voxlearning.utopia.service.newexam.api.mapper.report.*;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.*;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalPrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalQuestion;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamStudentQuestion;
import com.voxlearning.utopia.service.newexam.impl.queue.NewExamQueueProducer;
import com.voxlearning.utopia.service.newexam.impl.service.internal.report.NewExamReportProcessor;
import com.voxlearning.utopia.service.newexam.impl.service.internal.student.work.DoNewExamProcess;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamPaperHelper;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.newexam.impl.template.exam.report.*;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType.*;


@Named
@Service(interfaceClass = NewExamReportLoader.class)
@ExposeService(interfaceClass = NewExamReportLoader.class)
public class NewExamReportLoaderImpl extends NewExamSpringBean implements NewExamReportLoader {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject
    private ClazzQuestionAnswerHandlerFactory clazzQuestionAnswerHandlerFactory;
    @Inject
    private SingleQuestionAnswerHandlerFactory singleQuestionAnswerHandlerFactory;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private DoNewExamProcess doNewExamProcess;
    @Inject
    private NewExamReportProcessor newExamReportProcessor;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    private PersonalQuestionAnswerHandlerFactory personalQuestionAnswerHandlerFactory;
    @Inject
    private NewExamQueueProducer newExamQueueProducer;
    @Inject
    private VendorServiceClient vendorServiceClient;

    @Override
    public MapMessage pageUnifyExamList(Long teacherId, Long clazzId, Subject subject, Integer iDisplayLength, Integer iDisplayStart) {
        return newExamReportProcessor.pageUnifyExamList(teacherId, clazzId, subject, iDisplayLength, iDisplayStart);
    }

    @Override
    public MapMessage newPageUnifyExamList(Teacher teacher, Long clazzId, Integer iDisplayLength, Integer iDisplayStart) {
        return newExamReportProcessor.newPageUnifyExamList(teacher, clazzId, teacher.getSubject(), iDisplayLength, iDisplayStart);
    }

    @Override
    public MapMessage pageUnitTestList(Teacher teacher, Subject subject, List<Long> groupIds, Integer iDisplayLength, Integer iDisplayStart) {
        return newExamReportProcessor.pageUnitTestList(teacher, subject, groupIds, iDisplayLength, iDisplayStart);
    }

    @Override
    public MapMessage crmUnifyExamList(Long teacherId, Long clazzId, Subject subject, Long groupId) {
        return newExamReportProcessor.crmUnifyExamList(teacherId, clazzId, subject, groupId);
    }

    @Override
    public MapMessage examDetailForClazz(Teacher teacher, String newExamId, Long clazzId) {
        return newExamReportProcessor.examDetailForClazz(teacher, newExamId, clazzId);
    }

    @Override
    public MapMessage examDetailForStudent(Teacher teacher, String newExamId, Long clazzId) {
        return newExamReportProcessor.examDetailForStudent(teacher, newExamId, clazzId);
    }

    @Override
    public MapMessage independentExamDetailForClazz(Teacher teacher, String newExamId) {
        return newExamReportProcessor.independentExamDetail(teacher, newExamId, "clazz");
    }

    @Override
    public MapMessage independentExamDetailForStudent(Teacher teacher, String newExamId) {
        return newExamReportProcessor.independentExamDetail(teacher, newExamId, "student");
    }

    @Override
    public MapMessage fetchTeacherClazzInfo(Teacher teacher) {
        try {
            Set<Long> tids = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(tids);
            Map<Long, List<Clazz>> clazzMap = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(tids);
            Map<Subject, List<Map<String, Object>>> resultMap = new LinkedHashMap<>();
            for (Map.Entry<Long, List<Clazz>> entry : clazzMap.entrySet()) {
                List<Map<String, Object>> clazzList = entry.getValue()
                        .stream()
                        .filter(c -> !c.isTerminalClazz())
                        .sorted(new Clazz.ClazzLevelAndNameComparator())
                        .map(c -> MapUtils.m(
                                "clazzId", c.getId(),
                                "className", c.formalizeClazzName()
                        ))
                        .collect(Collectors.toList());
                resultMap.put(teacherMap.get(entry.getKey()).getSubject(), clazzList);
            }
            //按照学科排序
            Subject[] subjects1 = new Subject[]{Subject.ENGLISH, Subject.MATH, Subject.CHINESE};
            List<Map<String, Object>> subjects = new LinkedList<>();
            for (Subject s : subjects1) {
                if (resultMap.containsKey(s)) {
                    subjects.add(
                            MapUtils.m("subject", s,
                                    "subjectName", s.getValue(),
                                    "clazzList", resultMap.get(s)));
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.put("clazz_list", subjects);
            return mapMessage;
        } catch (Exception e) {
            logger.error("new exam report app fetch Teacher ClazzInfo failed : tid {}", teacher.getId());
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage fetchNewExamSingleQuestionDimension(Teacher teacher, String newExamId, Long clazzId, String paperId, String questionId, int subIndex) {
        try {
            if (StringUtils.isAnyBlank(newExamId, paperId, questionId) || clazzId <= 0L || subIndex < 0) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("参数有误");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("考试不存在");
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            }
            List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
            Set<String> paperIds = papers.stream().map(NewExam.EmbedPaper::getPaperId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(paperIds) || (!paperIds.contains(paperId))) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("试卷不属于考试");
            }
            NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId);
            if (newPaper == null) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("试卷不存在");
            }
            if (CollectionUtils.isEmpty(newPaper.getQuestions())) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("试卷不存在题目");
            }
            List<String> qids = newPaper.getQuestions()
                    .stream()
                    .map(XxBaseQuestion::getId)
                    .collect(Collectors.toList());
            if (!qids.contains(questionId)) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("题目不属于试卷");
            }
            Map<String, NewQuestion> questionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids);
            if (!questionMap.containsKey(questionId)) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("题目不存在");
            }
            NewQuestion newQuestion = questionMap.get(questionId);
            if (newQuestion.getContent() == null) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("小题目不存在");
            }
            List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
            if (CollectionUtils.isEmpty(subContents) || subContents.size() <= subIndex) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("小题目不存在");
            }
            NewQuestionsSubContents newQuestionsSubContents = subContents.get(subIndex);
            ExamReportAnswerStatType answerStatType = ExamReportAnswerStatType.get(newQuestionsSubContents.getSubContentTypeId());
            if (answerStatType == null) {
                return MapMessage.errorMessage("题型报告不支持");
            }
            SingleQuestionAnswerHandlerTemplate template = singleQuestionAnswerHandlerFactory.getTemplate(answerStatType);
            if (template == null) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("模板缺失");
            }

            Map<Long, User> userMap = newExamReportProcessor.loadClazzStudents(teacher, newExam.getSubject(), clazzId);
            if (MapUtils.isEmpty(userMap)) {
                logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId());
                return MapMessage.errorMessage("班级不存在学生");
            }

            String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
            List<String> newExamResultIds = userMap.keySet()
                    .stream()
                    .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                    .collect(Collectors.toList());

            List<String> processIds = newExamResultDao.loads(newExamResultIds)
                    .values().stream()
                    .filter(o -> MapUtils.isNotEmpty(o.getAnswers()) && Objects.equals(o.getPaperId(), paperId) && o.getAnswers().containsKey(newQuestion.getDocId()))
                    .map(o -> o.getAnswers().get(newQuestion.getDocId()))
                    .collect(Collectors.toList());
            Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(processIds);

            NewExamSingleSubQuestion singleSubQuestion = getNewExamSingleSubQuestion(newExam, newPaper, newQuestion, newQuestionsSubContents, answerStatType, template, userMap, newExamProcessResultMap, subIndex);

            //为了不改壳传参，正确显示小题对应的题号，取出所有题计算标号
            Map<String, Integer> subIndexRankMap = new HashMap<>();
            int rank = 0;
            for (String qid : qids) {
                NewQuestion question = questionMap.get(qid);
                if (question != null) {
                    for (int i = 0; i < question.getContent().getSubContents().size(); i++) {
                        subIndexRankMap.put(StringUtils.join(Arrays.asList(question.getId(), i), "#"), rank);
                        rank++;
                    }
                }
            }
            Integer subIndexRank = subIndexRankMap.get(StringUtils.join(Arrays.asList(newQuestion.getId(), subIndex), "#"));
            if (subIndexRank != null) {
                singleSubQuestion.setSubIndexRank(subIndexRank);
            } else {
                singleSubQuestion.setSubIndexRank(subIndex);
            }

            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("correctStartTime", DateUtils.dateToString(newExam.getExamStopAt(), "MM-dd HH:mm"));
            mapMessage.add("correctStopTime", DateUtils.dateToString(newExam.getCorrectStopAt(), "MM-dd HH:mm"));
            mapMessage.put("endCorrect", !newExam.canCorrect());
            mapMessage.add("singleSubQuestion", singleSubQuestion);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch NewExam Single Question Dimension failed : newExamId {},clazzId {} ,paperId {},questionId {},subIndex {},tid {}", newExamId, clazzId, questionId, subIndex, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 封装子小问数据
     *
     * @return
     */
    private NewExamSingleSubQuestion getNewExamSingleSubQuestion(NewExam newExam, NewPaper newPaper, NewQuestion newQuestion, NewQuestionsSubContents newQuestionsSubContents, ExamReportAnswerStatType answerStatType,
                                                                 SingleQuestionAnswerHandlerTemplate template, Map<Long, User> userMap, Map<String, NewExamProcessResult> newExamProcessResultMap, int subIndex) {
        NewExamSingleSubQuestion singleSubQuestion = new NewExamSingleSubQuestion();
        //計算标准分
        Map<String, Double> questionScoreMapByQid = newPaper.getQuestionScoreMapByQid();
        double standardScore = new BigDecimal(SafeConverter.toDouble(questionScoreMapByQid.getOrDefault(newQuestion.getId(), 0.0)))
                .divide(new BigDecimal(newQuestion.getContent().getSubContents().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        //设置小题标准分
        singleSubQuestion.setStandardScore(NewExamPaperHelper.simple(standardScore));

        NewExamSinglePrepareQuestionContext newExamSinglePrepareQuestionContext = new NewExamSinglePrepareQuestionContext(answerStatType,
                singleSubQuestion, newQuestionsSubContents, newExam, newExamProcessResultMap, subIndex);

        //准备答案统计类型
        template.prepareAnswerType(newExamSinglePrepareQuestionContext);
        Map<Long, NewExamProcessResult> userProcessResultMap = newExamProcessResultMap.values().stream().collect(Collectors.toMap(NewExamProcessResult::getUserId, Function.identity(), (o1, o2) -> o2));
        for (User user : userMap.values()) {
            NewExamProcessResult examProcessResult = userProcessResultMap.get(user.getId());
            double score = 0d;
            if (examProcessResult != null) {
                //根据DOC_ID过滤成绩(改题的情况过滤老的做题数据)
                if (!Objects.equals(examProcessResult.getQuestionDocId(), newQuestion.getDocId())) {
                    continue;
                }
                //成绩
                List<Double> subScore = examProcessResult.processSubScore();
                if (subScore.size() <= subIndex) {
                    continue;
                }
                score = SafeConverter.toDouble(subScore.get(subIndex));
                singleSubQuestion.setTotalScore(score + singleSubQuestion.getTotalScore());

                //几人做对几人做错
                List<Boolean> subGrasp = examProcessResult.getSubGrasp().get(subIndex);
                boolean subQuestionGrasp = isSubQuestionGrasp(newQuestionsSubContents, subGrasp);
                if (subQuestionGrasp) {
                    singleSubQuestion.setRightCount(singleSubQuestion.getRightCount() + 1);
                } else {
                    if (CollectionUtils.isNotEmpty(examProcessResult.getUserAnswers()) && examProcessResult.getUserAnswers().size() >= subIndex
                            && !examProcessResult.getUserAnswers().get(subIndex).stream().allMatch(""::equals)) {
                        singleSubQuestion.setWrongCount(singleSubQuestion.getWrongCount() + 1);
                    }
                }
            }

            NewExamSingleQuestionContext newExamSingleQuestionContext = new NewExamSingleQuestionContext(user.getId(),
                    userMap.get(user.getId()).fetchRealnameIfBlankId(), NewExamPaperHelper.simple(score),
                    examProcessResult, subIndex, singleSubQuestion, newQuestionsSubContents, newExamSinglePrepareQuestionContext.getTop3Answer());
            //统计答案详情
            template.statAnswerTypeDetail(newExamSingleQuestionContext);
        }

        //得分率和平均分
        int doCount = singleSubQuestion.getWrongCount() + singleSubQuestion.getRightCount();
        if (doCount > 0) {
            double averScore = new BigDecimal(singleSubQuestion.getTotalScore()).divide(new BigDecimal(doCount), 5, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (singleSubQuestion.getStandardScore() >= averScore && averScore > 0) {
                double rate = new BigDecimal(100 * averScore).divide(new BigDecimal(singleSubQuestion.getStandardScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                singleSubQuestion.setRate(rate);
            }
        }

        if (CollectionUtils.isNotEmpty(singleSubQuestion.getAnswerStudentsList())) {
            //过滤掉没有学生的项
            List<NewExamSingleSubQuestion.StudentAnswer> answerStudents = singleSubQuestion.getAnswerStudentsList().stream().filter(o -> CollectionUtils.isNotEmpty(o.getStudents())).collect(Collectors.toList());
            singleSubQuestion.setAnswerStudentsList(answerStudents);
            //学生列表按照拼音首字母排序
            singleSubQuestion.getAnswerStudentsList().forEach(o -> o.getStudents().sort(NewExamSingleSubQuestion.BaseStudent::compareTo));
        }
        return singleSubQuestion;
    }

    @Override
    public MapMessage fetchNewExamPaperInfo(String newExamId, Long clazzId) {
        try {
            if (StringUtils.isBlank(newExamId) || clazzId <= 0L) {
                logger.error("fetch NewExam PaperInfo failed : newExamId {},clazzId {} ", newExamId, clazzId);
                return MapMessage.errorMessage("参数有误");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("fetch NewExam PaperInfo failed : newExamId {},clazzId {} ", newExamId, clazzId);
                return MapMessage.errorMessage("考试不存在");
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                logger.error("fetch NewExam PaperInfo failed : newExamId {},clazzId {} ", newExamId, clazzId);
                return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            }
            List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
            List<String> paperIds = papers.stream().map(NewExam.EmbedPaper::getPaperId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(paperIds)) {
                logger.error("fetch NewExam PaperInfo failed : newExamId {},clazzId {} ", newExamId, clazzId);
                return MapMessage.errorMessage("试卷ID不存在");
            }
            Map<String, NewPaper> newPaperMap = loadNewPapersByDocIdsIncludeDisable(paperIds);
            if (newPaperMap.isEmpty()) {
                logger.error("fetch NewExam PaperInfo failed : newExamId {},clazzId {} ", newExamId, clazzId);
                return MapMessage.errorMessage("试卷不存在或者已经下架");
            }
            //是否包含口语
            List<String> contentTypes = newExam.getContentTypes() != null ? newExam.getContentTypes() : Collections.emptyList();
            boolean hasOral = contentTypes.contains("ORAL");
            //多份试卷的
            List<NewExamH5PaperInfo> newExamPaperInfos = new LinkedList<>();
            for (NewExam.EmbedPaper paper : papers) {
                String paperId = paper.getPaperId();
                if (newPaperMap.containsKey(paperId)) {
                    NewExamH5PaperInfo newExamPaperInfo = new NewExamH5PaperInfo();
                    newExamPaperInfo.setPaperId(paperId);
                    newExamPaperInfo.setPaperName(paper.getPaperName());
                    newExamPaperInfos.add(newExamPaperInfo);
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("clazzName", clazz.formalizeClazzName());
            mapMessage.add("newExamName", newExam.getName());
            mapMessage.add("correctStartTime", DateUtils.dateToString(newExam.getExamStopAt(), "MM-dd HH:mm"));
            mapMessage.add("correctStopTime", DateUtils.dateToString(newExam.getCorrectStopAt(), "MM-dd HH:mm"));
            mapMessage.add("hasOral", hasOral);
            mapMessage.add("newExamPaperInfos", newExamPaperInfos);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch NewExam PaperInfo failed : newExamId {},clazzId {} ", newExamId, clazzId, e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage fetchPaperInfo(List<String> paperIds) {
        Map<String, NewPaper> newPaperMap = loadNewPapersByDocIdsIncludeDisable(paperIds);
        if (newPaperMap.isEmpty()) {
            logger.error("fetch PaperInfo failed : paperIds {} ", paperIds);
            return MapMessage.errorMessage("试卷不存在或者已经下架");
        }
        List<NewExamH5PaperInfo> newExamPaperInfos = new LinkedList<>();
        for (NewPaper paper : newPaperMap.values()) {
            NewExamH5PaperInfo newExamPaperInfo = new NewExamH5PaperInfo();
            newExamPaperInfo.setPaperId(paper.getId());
            newExamPaperInfo.setPaperName(paper.getTitle());
            newExamPaperInfos.add(newExamPaperInfo);
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("newExamPaperInfos", newExamPaperInfos);
        return mapMessage;
    }


    @Override
    public MapMessage fetchNewExamPaperQuestionInfo(String newExamId, Long sid) {
        try {
            if (StringUtils.isAnyBlank(newExamId) || sid < 0L) {
                logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {} ,sid {}", newExamId, sid);
                return MapMessage.errorMessage("参数有误");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {} ,sid {}", newExamId, sid);
                return MapMessage.errorMessage("考试不存在");
            }
            String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
            String newExamRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), sid.toString()).toString();
            NewExamResult newExamResult = newExamResultDao.load(newExamRegistrationId);
            if (newExamResult == null) {
                logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {} ,sid {}", newExamId, sid);
                return MapMessage.errorMessage("学生没有参与考试");
            }
            if (newExamResult.getPaperId() == null) {
                logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {} ,sid {}", newExamId, sid);
                return MapMessage.errorMessage("试卷ID不存在");
            }
            String paperId = newExamResult.getPaperId();
            //TODO 不想重复代码，所以一个newExam有重复查询的，
            return fetchNewExamPaperQuestionInfo(newExamId, paperId);
        } catch (Exception e) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {} ,sid {}", newExamId, sid, e);
            return MapMessage.errorMessage();
        }
    }


    @Override
    public MapMessage fetchNewExamPaperQuestionInfo(String newExamId, String paperId) {
        try {
            if (StringUtils.isAnyBlank(newExamId, paperId)) {
                logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {} ,paperId {}", newExamId, paperId);
                return MapMessage.errorMessage("参数有误");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {} ,paperId {}", newExamId, paperId);
                return MapMessage.errorMessage("考试不存在");
            }
            List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
            Set<String> paperIds = papers.stream().map(NewExam.EmbedPaper::getPaperId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(paperIds) || (!paperIds.contains(paperId))) {
                logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {} ,paperId {}", newExamId, paperId);
                return MapMessage.errorMessage("试卷不属于考试");
            }
            return fetchPaperQuestionInfo(paperId);
        } catch (Exception e) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {} ,paperId {}", newExamId, paperId, e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage fetchPaperQuestionInfo(String paperId) {
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId);
        if (newPaper == null) {
            logger.error("fetch NewExam Paper QuestionInfo failed : paperId {}", paperId);
            return MapMessage.errorMessage("试卷不存在");
        }
        Set<String> qids = newPaper.getQuestions()
                .stream()
                .map(XxBaseQuestion::getId)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids);
        List<NewExamThemeForSub> themeForSubs = new LinkedList<>();
        int index = 1;//按照小题的顺序号
        for (NewPaperParts parts : newPaper.getParts()) {
            //一个模块的对于数据结构
            NewExamThemeForSub s = new NewExamThemeForSub();
            s.setDesc(parts.getTitle());
            themeForSubs.add(s);
            if (CollectionUtils.isNotEmpty(parts.getQuestions())) {
                for (NewPaperQuestion question : parts.getQuestions()) {
                    if (newQuestionMap.containsKey(question.getId())) {
                        NewQuestion newQuestion = newQuestionMap.get(question.getId());
                        if (newQuestion.getContent() == null)
                            continue;
                        if (CollectionUtils.isEmpty(newQuestion.getContent().getSubContents()))
                            continue;
                        double standardScore = new BigDecimal(SafeConverter.toDouble(question.getScore(), 0.0))
                                .divide(new BigDecimal(newQuestion.getContent().getSubContents().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        int subIndex = 0;//题里面第几小题
                        for (NewQuestionsSubContents ignored : newQuestion.getContent().getSubContents()) {
                            //小题的数据结构
                            NewExamThemeForSub.SubQuestion subQuestion = new NewExamThemeForSub.SubQuestion();
                            subQuestion.setQid(question.getId());
                            subQuestion.setIndex(index);
                            subQuestion.setSubIndex(subIndex);
                            subQuestion.setStandardScore(standardScore);
                            s.getSubQuestions().add(subQuestion);
                            subIndex++;
                            index++;
                        }
                    }
                }
            }
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("themeForSubs", themeForSubs);
        return mapMessage;
    }


    @Override
    public MapMessage fetchNewExamPaperQuestionAnswerInfo(Teacher teacher, String newExamId, Long clazzId, String paperId) {
        if (StringUtils.isAnyBlank(newExamId, paperId) || clazzId <= 0L) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("参数有误");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("考试不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }
        List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
        Set<String> paperIds = papers.stream().map(NewExam.EmbedPaper::getPaperId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(paperIds) || (!paperIds.contains(paperId))) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("试卷不属于考试");
        }
        SchoolLevel schoolLevel = newExam.getSchoolLevel();
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId, schoolLevel);
        if (newPaper == null) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("试卷不存在");
        }
        Set<String> qids = newPaper.getQuestions()
                .stream()
                .map(XxBaseQuestion::getId)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids, schoolLevel);
        Map<Long, User> userMap = newExamReportProcessor.loadClazzStudents(teacher, newExam.getSubject(), clazzId);
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamResultIds = userMap.keySet()
                .stream()
                .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                .collect(Collectors.toList());
        Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamResultIds);
        int submitNum = 0;
        int jointNum = 0;
        for (NewExamResult newExamResult : newExamResultMap.values()) {
            if (Objects.equals(newExamResult.getPaperId(), paperId)) {
                jointNum++;
                if (newExamResult.getSubmitAt() != null) {
                    submitNum++;
                }
            }
        }
        List<String> processIds = newExamResultMap.values()
                .stream()
                .filter(o -> Objects.equals(o.getPaperId(), paperId))
                .filter(o -> MapUtils.isNotEmpty(o.getAnswers()))
                .map(o -> o.getAnswers().values())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(processIds);

        Map<String, Double> questionScoreMapByQid = newPaper.getQuestionScoreMapByQid();
        Map<String, NewExamDetailH5ToQuestion> questionMap = new LinkedHashMap<>();
        //DOC_ID 来记录成绩
        Map<String, NewExamDetailH5ToQuestion> questionDocIdMap = new LinkedHashMap<>();
        //分析模块部分
        int index = 1;//小题的顺序号

        //subject 用于多答案支持
        Subject subject = newExam.getSubject();
        switch (subject) {
            case JENGLISH:
                subject = Subject.ENGLISH;
                break;
            case JMATH:
                subject = Subject.MATH;
                break;
            case JCHINESE:
                subject = Subject.CHINESE;
                break;
            default:
                break;
        }
        for (NewPaperParts parts : newPaper.getParts()) {
            if (CollectionUtils.isNotEmpty(parts.getQuestions())) {
                for (NewPaperQuestion question : parts.getQuestions()) {
                    if (newQuestionMap.containsKey(question.getId())) {
                        NewQuestion newQuestion = newQuestionMap.get(question.getId());
                        //1.每个题的结果，包含多个小题
                        NewExamDetailH5ToQuestion newExamDetailToQuestion = new NewExamDetailH5ToQuestion();
                        questionDocIdMap.put(newQuestion.getDocId(), newExamDetailToQuestion);
                        questionMap.put(question.getId(), newExamDetailToQuestion);
                        double standardScore = new BigDecimal(SafeConverter.toDouble(questionScoreMapByQid.getOrDefault(newQuestion.getId(), 0.0)))
                                .divide(new BigDecimal(newQuestion.getContent().getSubContents().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        int subIndex = 1;
                        for (NewQuestionsSubContents newQuestionsSubContents : newQuestion.getContent().getSubContents()) {
                            NewExamQuestionType newExamQuestionType = getNewExamQuestionType(newQuestionsSubContents);
                            if (newExamQuestionType == null) {
                                index++;
                                subIndex++;
                                continue;
                            }
                            ClazzQuestionAnswerHandlerTemplate template = clazzQuestionAnswerHandlerFactory.getTemplate(newExamQuestionType);
                            if (template == null) {
                                index++;
                                subIndex++;
                                continue;
                            }
                            NewExamClazzPrepareQuestionContext newExamClazzPrepareQuestionContext = new NewExamClazzPrepareQuestionContext(
                                    newQuestionsSubContents,
                                    standardScore,
                                    subIndex,
                                    index,
                                    question,
                                    newExamQuestionType,
                                    newExamDetailToQuestion,
                                    subject);
                            //对于不同类型的题形成对于的结构和基础数据，放入newExamDetailToQuestion中
                            template.prepareSubQuestion(newExamClazzPrepareQuestionContext);
                            index++;
                            subIndex++;
                        }
                        newExamDetailToQuestion.setQid(question.getId());
                    }
                }
            }
        }
        Map<String, NewQuestion> newQuestionMap1 = newQuestionMap.values().stream().collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
        for (NewExamProcessResult p : newExamProcessResultMap.values()) {
            if (!Objects.equals(paperId, p.getPaperDocId()))
                continue;
            if (!newQuestionMap1.containsKey(p.getQuestionDocId()))
                continue;
            NewQuestion newQuestion = newQuestionMap1.get(p.getQuestionDocId());
            if (newQuestion == null)
                continue;
            if (questionDocIdMap.containsKey(p.getQuestionDocId())) {
                NewExamDetailH5ToQuestion question = questionDocIdMap.get(p.getQuestionDocId());
                List<Double> subScore = p.processSubScore();
                if (question.getSubQuestions().size() == subScore.size()) {
                    int _index = 0;
                    for (NewExamDetailH5ToQuestion.SubQuestion subQuestion : question.getSubQuestions()) {
                        double score = SafeConverter.toDouble(subScore.get(_index));
                        subQuestion.setTotalScore(subQuestion.getTotalScore() + score);
                        subQuestion.setNum(subQuestion.getNum() + 1);
                        if (p.getSubGrasp() != null &&
                                p.getSubGrasp().size() > _index) {
                            List<Boolean> baleen = p.getSubGrasp().get(_index);
                            boolean b = baleen.stream().allMatch(SafeConverter::toBoolean);
                            //学生作答信息结构
                            NewExamDetailH5ToQuestion.StudentAnswer studentAnswer = new NewExamDetailH5ToQuestion.StudentAnswer();
                            studentAnswer.setUserId(p.getUserId());
                            studentAnswer.setUserName(userMap.containsKey(p.getUserId()) ? userMap.get(p.getUserId()).fetchRealnameIfBlankId() : "");
                            studentAnswer.setScore(score);
                            NewExamClazzQuestionContext newExamClazzQuestionContext = new NewExamClazzQuestionContext(_index,
                                    studentAnswer,
                                    subQuestion,
                                    p,
                                    b,
                                    baleen,
                                    newQuestion);
                            ClazzQuestionAnswerHandlerTemplate template = clazzQuestionAnswerHandlerFactory.getTemplate(subQuestion.getNewExamQuestionType());
                            if (template != null) {
                                template.processSubQuestion(newExamClazzQuestionContext);
                            }
                        }
                        _index++;
                    }
                }
            }
        }
        questionMap.values()
                .stream()
                .map(NewExamDetailH5ToQuestion::getSubQuestions)
                .flatMap(Collection::stream)
                .filter(o -> o.getNum() > 0)
                .forEach(o -> {
                    o.setAverScore(new BigDecimal(o.getTotalScore()).divide(new BigDecimal(o.getNum()), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    if (o.getStandardScore() > 0) {
                        o.setRate(new BigDecimal(o.getAverScore() * 100).divide(new BigDecimal(o.getStandardScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    }
                    o.setXuanZeAnswerMap(null);
                });
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("endCorrect", !newExam.canCorrect());
        mapMessage.put("questionMap", questionMap);
        mapMessage.add("submitNum", submitNum);
        mapMessage.add("jointNum", jointNum);
        return mapMessage;
    }

    @Override
    public MapMessage paperClazzAnswerDetail(Teacher teacher, String newExamId, Long clazzId, String paperId) {
        if (StringUtils.isAnyBlank(newExamId, paperId) || clazzId <= 0L) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("参数有误");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("考试不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }
        List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
        Set<String> paperIds = papers.stream().map(NewExam.EmbedPaper::getPaperId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(paperIds) || (!paperIds.contains(paperId))) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("试卷不属于考试");
        }
        SchoolLevel schoolLevel = newExam.getSchoolLevel();
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId, schoolLevel);
        if (newPaper == null) {
            logger.error("fetch NewExam Paper QuestionInfo failed : newExamId {},clazzId {} ,paperId {},tid {}", newExamId, clazzId, paperId, teacher.getId());
            return MapMessage.errorMessage("试卷不存在");
        }
        Set<String> qids = newPaper.getQuestions()
                .stream()
                .map(XxBaseQuestion::getId)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids, schoolLevel);
        Map<Long, User> userMap = newExamReportProcessor.loadClazzStudents(teacher, newExam.getSubject(), clazzId);
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamResultIds = userMap.keySet()
                .stream()
                .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                .collect(Collectors.toList());
        Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamResultIds);
        int submitNum = 0;
        int jointNum = 0;
        for (NewExamResult newExamResult : newExamResultMap.values()) {
            if (Objects.equals(newExamResult.getPaperId(), paperId)) {
                jointNum++;
                if (newExamResult.getSubmitAt() != null) {
                    submitNum++;
                }
            }
        }
        List<String> processIds = newExamResultMap.values()
                .stream()
                .filter(o -> Objects.equals(o.getPaperId(), paperId))
                .filter(o -> MapUtils.isNotEmpty(o.getAnswers()))
                .map(o -> o.getAnswers().values())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(processIds);

        Map<String, NewExamWebQuestionDetail> questionMap = new LinkedHashMap<>();
        for (NewPaperParts parts : newPaper.getParts()) {
            if (CollectionUtils.isEmpty(parts.getQuestions())) {
                continue;
            }

            for (NewPaperQuestion question : parts.getQuestions()) {
                NewQuestion newQuestion = newQuestionMap.get(question.getId());
                if (newQuestion == null) {
                    continue;
                }
                NewExamWebQuestionDetail webQuestionDetail = new NewExamWebQuestionDetail();
                List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                for (int i = 0; i < subContents.size(); i++) {
                    NewQuestionsSubContents newQuestionsSubContents = subContents.get(i);
                    ExamReportAnswerStatType answerStatType = ExamReportAnswerStatType.get(newQuestionsSubContents.getSubContentTypeId());
                    SingleQuestionAnswerHandlerTemplate template = singleQuestionAnswerHandlerFactory.getTemplate(answerStatType);
                    Map<String, NewExamProcessResult> thisQuestionProcessResultMap = Maps.filterValues(newExamProcessResultMap, p -> p != null && p.getQuestionDocId().equals(newQuestion.getDocId()));
                    NewExamSingleSubQuestion newExamSingleSubQuestion = getNewExamSingleSubQuestion(newExam, newPaper, newQuestion, newQuestionsSubContents, answerStatType, template, userMap, thisQuestionProcessResultMap, i);
                    webQuestionDetail.getSubQuestions().add(newExamSingleSubQuestion);
                }
                webQuestionDetail.setQid(newQuestion.getId());
                questionMap.put(question.getId(), webQuestionDetail);
            }
        }

        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("endCorrect", !newExam.canCorrect());
        mapMessage.put("questionMap", questionMap);
        mapMessage.add("submitNum", submitNum);
        mapMessage.add("jointNum", jointNum);
        return mapMessage;
    }


    @Override
    public MapMessage fetchNewExamPaperQuestionPersonalAnswerInfo(String newExamId, Long userId) {

        if (StringUtils.isAnyBlank(newExamId)) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("参数有误");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("考试不存在");
        }
        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        if (student == null) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("学生ID错误");
        }
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        String newExamRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), userId.toString()).toString();
        NewExamResult newExamResult = newExamResultDao.load(newExamRegistrationId);
        if (newExamResult == null) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("学生没有参与考试");
        }
        SchoolLevel schoolLevel = newExam.getSchoolLevel();
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(newExamResult.getPaperId(), schoolLevel);
        if (newPaper == null) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("试卷不存在");
        }
        Set<String> qids = newPaper.getQuestions()
                .stream()
                .map(XxBaseQuestion::getId)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids, schoolLevel);
        LinkedHashMap<String, String> answers = newExamResult.getAnswers();
        Map<String, NewExamProcessResult> newExamProcessResultMap = MapUtils.isNotEmpty(answers) ? newExamProcessResultDao.loads(answers.values()) : Collections.emptyMap();
        Map<String, Double> questionScoreMapByQid = newPaper.getQuestionScoreMapByQid();
        Map<String, NewExamPersonalQuestion> questionMap = new LinkedHashMap<>();
        Map<String, NewExamPersonalQuestion> questionDocMap = new LinkedHashMap<>();
        //subject 用于多答案
        Subject subject = newExam.getSubject();
        switch (subject) {
            case JENGLISH:
                subject = Subject.ENGLISH;
                break;
            case JMATH:
                subject = Subject.MATH;
                break;
            case JCHINESE:
                subject = Subject.CHINESE;
                break;
            default:
                break;
        }
        //分析模块部分
        int index = 1;//小题的顺序号
        for (NewPaperParts parts : newPaper.getParts()) {
            if (CollectionUtils.isNotEmpty(parts.getQuestions())) {
                for (NewPaperQuestion question : parts.getQuestions()) {
                    if (newQuestionMap.containsKey(question.getId())) {
                        NewQuestion newQuestion = newQuestionMap.get(question.getId());
                        //1题
                        NewExamPersonalQuestion newExamDetailToQuestion = new NewExamPersonalQuestion();
                        questionMap.put(question.getId(), newExamDetailToQuestion);
                        questionDocMap.put(newQuestion.getDocId(), newExamDetailToQuestion);
                        double standardScore = NewExamPaperHelper.simple(NewExamPaperHelper.simple(new BigDecimal(SafeConverter.toDouble(questionScoreMapByQid.getOrDefault(newQuestion.getId(), 0.0)))
                                .divide(new BigDecimal(newQuestion.getContent().getSubContents().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue()));
                        for (NewQuestionsSubContents newQuestionsSubContents : newQuestion.getContent().getSubContents()) {
                            NewExamQuestionType newExamQuestionType = getNewExamQuestionType(newQuestionsSubContents);
                            if (newExamQuestionType == null) {
                                index++;
                                continue;
                            }
                            NewExamPersonalPrepareQuestionContext newExamPersonalPrepareQuestionContext = new NewExamPersonalPrepareQuestionContext(newQuestionsSubContents,
                                    newExamDetailToQuestion,
                                    newExamQuestionType,
                                    question,
                                    index,
                                    standardScore,
                                    subject);
                            PersonalQuestionAnswerHandlerTemplate template = personalQuestionAnswerHandlerFactory.getTemplate(newExamQuestionType);
                            if (template == null) {
                                index++;
                                continue;
                            }
                            template.prepareSubQuestion(newExamPersonalPrepareQuestionContext);
                            index++;
                        }
                        newExamDetailToQuestion.setQid(question.getId());
                    }
                }
            }
        }
        Map<String, NewQuestion> newQuestionMap1 = newQuestionMap.values().stream().collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
        //分析数据：根据 questionMap key 得到数据结构
        for (NewExamProcessResult p : newExamProcessResultMap.values()) {
            if (!newQuestionMap1.containsKey(p.getQuestionDocId()))
                continue;
            NewQuestion newQuestion = newQuestionMap1.get(p.getQuestionDocId());
            if (newQuestion == null)
                continue;
            if (questionDocMap.containsKey(p.getQuestionDocId())) {
                NewExamPersonalQuestion question = questionDocMap.get(p.getQuestionDocId());
                List<Double> subScore = p.processSubScore();
                if (question.getSubQuestions().size() == subScore.size()) {
                    //小题号
                    int _index = 0;
                    for (NewExamPersonalQuestion.NewExamPersonalSubQuestion subQuestion : question.getSubQuestions()) {
                        double score = SafeConverter.toDouble(subScore.get(_index));
                        subQuestion.setPersonalScore(NewExamPaperHelper.simple(score));
                        subQuestion.setHasAnswer(true);
                        if (p.getSubGrasp() != null &&
                                p.getSubGrasp().size() > _index) {
                            List<Boolean> baleen = p.getSubGrasp().get(_index);
                            boolean b = baleen.stream().allMatch(SafeConverter::toBoolean);
                            NewExamPersonalQuestionContext newExamPersonalQuestionContext = new NewExamPersonalQuestionContext(p,
                                    _index,
                                    baleen,
                                    subQuestion,
                                    b,
                                    newQuestion);
                            PersonalQuestionAnswerHandlerTemplate template = personalQuestionAnswerHandlerFactory.getTemplate(subQuestion.getNewExamQuestionType());
                            if (template == null) {
                                continue;
                            }
                            template.processSubQuestion(newExamPersonalQuestionContext);
                        }
                        _index++;
                    }
                }
            }
        }
        MapMessage mapMessage = MapMessage.successMessage();
        String newExamName = getReportExamName(newExam, newPaper);
        //显示等级或者分数
        double score = newExamResult.processScore(SafeConverter.toInt(newPaper.getTotalScore()));
        int gradeType = SafeConverter.toInt(newExam.getGradeType());
        if (gradeType == 0) {
            newExamName = newExamName + " (满分 " + newPaper.getTotalScore() + "分)";
        } else {
            double rate = 0;
            if (newPaper.getTotalScore() != null && newPaper.getTotalScore() > 0) {
                rate = new BigDecimal(score * 100).divide(new BigDecimal(newPaper.getTotalScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            NewExam.EmbedRank embedRank = newExam.processScoreLevel(rate);
            mapMessage.add("embedRank", embedRank == null ? "无等级" : embedRank.getRankName());
        }
        mapMessage.add("newExamName", newExamName);
        mapMessage.add("userName", student.fetchRealnameIfBlankId());
        mapMessage.add("questionMap", questionMap);
        mapMessage.add("gradeType", gradeType);
        mapMessage.add("score", NewExamPaperHelper.simple(score));
        DecimalFormat df = new DecimalFormat("###.##");
        mapMessage.add("scoreStr", df.format(score));
        return mapMessage;
    }

    //根据小题得到相对类型
    private NewExamQuestionType getNewExamQuestionType(NewQuestionsSubContents newQuestionsSubContents) {
        // 口语小题的题型
        if (questionContentTypeLoaderClient.isOral(Collections.singletonList(newQuestionsSubContents.getSubContentTypeId()))) {
            return Oral;
        }
        //选择题
        // TODO questionContentTypeLoaderClient::isChoice
        if (questionContentTypeLoaderClient.isChoice(Collections.singletonList(newQuestionsSubContents.getSubContentTypeId()))) {
            return Choice;
        }
        //填空
        // TODO questionContentTypeLoaderClient::isBlank
        if (questionContentTypeLoaderClient.isBlank(Collections.singletonList(newQuestionsSubContents.getSubContentTypeId()))) {
            return Blank;
        }
        return null;
    }


    //TODO 题库数据
    private Map<String, NewPaper> loadNewPapersByDocIdsIncludeDisable(List<String> paperIds) {
        Map<String, NewPaper> map = new LinkedHashMap<>();
        for (String paperId : paperIds) {
            NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId);
            if (newPaper != null) {
                map.put(newPaper.getDocId(), newPaper);
            }
        }
        return map;
    }


    @Override
    public MapMessage fetchNewExamPaperStudentAnswerInfo(String newExamId, Long userId) {

        if (StringUtils.isAnyBlank(newExamId)) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("参数有误");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("考试不存在");
        }
        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        if (student == null) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("学生ID错误");
        }
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        String newExamRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), userId.toString()).toString();
        NewExamResult newExamResult = newExamResultDao.load(newExamRegistrationId);
        if (newExamResult == null) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("学生没有参与考试");
        }
        SchoolLevel schoolLevel = newExam.getSchoolLevel();
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(newExamResult.getPaperId(), schoolLevel);
        if (newPaper == null) {
            logger.error("fetch NewExam Paper Question Personal AnswerInfo failed : newExamId {},userId {}", newExamId, userId);
            return MapMessage.errorMessage("试卷不存在");
        }
        Set<String> qids = newPaper.getQuestions()
                .stream()
                .map(XxBaseQuestion::getId)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids, schoolLevel);
        LinkedHashMap<String, String> answers = newExamResult.getAnswers();
        Map<String, NewExamProcessResult> newExamProcessResultMap = MapUtils.isNotEmpty(answers) ? newExamProcessResultDao.loads(answers.values()) : Collections.emptyMap();
        Map<String, NewExamProcessResult> questionProcessResultMap = newExamProcessResultMap.values().stream().collect(Collectors.toMap(NewExamProcessResult::getQuestionDocId, Function.identity()));
        Map<String, Double> questionScoreMapByQid = newPaper.getQuestionScoreMapByQid();
        Map<String, NewExamStudentQuestion> questionMap = new LinkedHashMap<>();

        int index = 0;//小题的顺序号
        for (NewPaperParts parts : newPaper.getParts()) {
            if (CollectionUtils.isEmpty(parts.getQuestions())) {
                continue;
            }
            for (NewPaperQuestion question : parts.getQuestions()) {
                NewQuestion newQuestion = newQuestionMap.get(question.getId());
                if (newQuestion == null) {
                    continue;
                }

                NewExamStudentQuestion examStudentQuestion = new NewExamStudentQuestion();
                NewExamProcessResult processResult = questionProcessResultMap.get(newQuestion.getDocId());
                examStudentQuestion.setUserAnswer(processResult != null ? processResult.getUserAnswers() : null);
                examStudentQuestion.setSubGrasp(processResult != null ? processResult.getSubGrasp() : null);
                questionMap.put(question.getId(), examStudentQuestion);
                List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                double standardScore = NewExamPaperHelper.simple(NewExamPaperHelper.simple(new BigDecimal(SafeConverter.toDouble(questionScoreMapByQid.getOrDefault(newQuestion.getId(), 0.0)))
                        .divide(new BigDecimal(subContents.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue()));
                int questionSubIndex = 0;
                for (int i = index; i < subContents.size(); i++) {
                    NewQuestionsSubContents newQuestionsSubContent = subContents.get(i);
                    ExamReportAnswerStatType answerStatType = ExamReportAnswerStatType.get(newQuestionsSubContent.getSubContentTypeId());
                    if (answerStatType == null) {
                        continue;
                    }

                    NewExamStudentQuestion.SubQuestion subQuestion = new NewExamStudentQuestion.SubQuestion();
                    subQuestion.setExamQuestionType(answerStatType.equals(ExamReportAnswerStatType.ORAL) ? ExamReportAnswerStatType.ORAL.name() : "OTHERS");
                    subQuestion.setIndex(index + 1);
                    subQuestion.setQid(question.getId());
                    subQuestion.setStandardScore(standardScore);
                    if (processResult != null) {
                        if (processResult.getSubScore() != null && questionSubIndex < processResult.getSubScore().size()) {
                            double score = SafeConverter.toDouble(processResult.getSubScore().get(questionSubIndex));
                            subQuestion.setUserScore(NewExamPaperHelper.simple(score));
                        }
                        if (processResult.getSubGrasp() != null && questionSubIndex < processResult.getSubGrasp().size()) {
                            List<Boolean> subGrasp = processResult.getSubGrasp().get(questionSubIndex);
                            subQuestion.setGrasp(isSubQuestionGrasp(newQuestionsSubContent, subGrasp));
                        }
                    }

                    if (answerStatType.equals(ExamReportAnswerStatType.ORAL)) {
                        if (newQuestionsSubContent.getOralDict() != null) {
                            subQuestion.setReferenceAnswers(newQuestionsSubContent.getOralDict().getAnswers());
                        }
                        //口语
                        if (processResult != null && processResult.getOralDetails() != null && processResult.getOralDetails().size() > questionSubIndex) {
                            List<NewExamProcessResult.OralDetail> oralDetails = processResult.getOralDetails().get(questionSubIndex);
                            if (oralDetails != null) {
                                List<String> voiceUrlList = oralDetails.stream().map(NewExamProcessResult.OralDetail::getAudio).filter(Objects::nonNull).collect(Collectors.toList());
                                subQuestion.setVoiceUrlList(voiceUrlList);
                            }
                        }
                    }

                    examStudentQuestion.getSubQuestions().add(subQuestion);
                    questionSubIndex++;
                }
                examStudentQuestion.setQid(question.getId());
            }
        }

        MapMessage mapMessage = MapMessage.successMessage();
        String newExamName = getReportExamName(newExam, newPaper);
        //显示等级或者分数
        double score = newExamResult.processScore(SafeConverter.toInt(newPaper.getTotalScore()));
        int gradeType = SafeConverter.toInt(newExam.getGradeType());
        if (gradeType == 0) {
            newExamName = newExamName + " (满分 " + newPaper.getTotalScore() + "分)";
        } else {
            double rate = 0;
            if (newPaper.getTotalScore() != null && newPaper.getTotalScore() > 0) {
                rate = new BigDecimal(score * 100).divide(new BigDecimal(newPaper.getTotalScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            NewExam.EmbedRank embedRank = newExam.processScoreLevel(rate);
            mapMessage.add("embedRank", embedRank == null ? "无等级" : embedRank.getRankName());
        }
        mapMessage.add("newExamName", newExamName);
        mapMessage.add("userName", student.fetchRealnameIfBlankId());
        mapMessage.add("questionMap", questionMap);
        mapMessage.add("gradeType", gradeType);
        mapMessage.add("score", NewExamPaperHelper.simple(score));
        DecimalFormat df = new DecimalFormat("###.##");
        mapMessage.add("scoreStr", df.format(score));
        return mapMessage;
    }

    public boolean isSubQuestionGrasp(NewQuestionsSubContents newQuestionsSubContent, List<Boolean> subGrasp) {
        boolean grasp = subGrasp.stream().allMatch(SafeConverter::toBoolean);
        if (newQuestionsSubContent.getSubContentTypeId().equals(QuestionConstants.LianXianTi_V2)) {
            grasp = grasp && subGrasp.size() == newQuestionsSubContent.getAnswers().size();
        }
        return grasp;
    }

    /**
     * 考试报告考试名拼接
     *
     * @param newExam
     * @param newPaper
     * @return
     */
    private String getReportExamName(NewExam newExam, NewPaper newPaper) {
        String newExamName = newExam.getName();
        List<NewExam.EmbedPaper> papers = newExam.getPapers();
        if (CollectionUtils.isNotEmpty(papers) && papers.size() > 1) {
            for (NewExam.EmbedPaper paper : papers) {
                if (Objects.equals(paper.getPaperId(), newPaper.getDocId())) {
                    newExamName = newExamName + "-" + paper.getPaperName();
                    break;
                }
            }
        }
        return newExamName;
    }

    //班级每题信息的接口
    //1.Paper 数据结构 theme == 》 question == 》 subQuestion
    //2.newExamProcess信息
    //3.数据后处理
    @Override
    public MapMessage fetchNewExamQuestionReport(Teacher teacher, String newExamId, Long clazzId) {
        try {
            if (StringUtils.isBlank(newExamId) || clazzId <= 0) {
                logger.error("fetch NewExam Question Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("参数有误");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("fetch NewExam Question Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("考试不存在");
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            }
            List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
            List<String> paperIds = papers.stream().map(NewExam.EmbedPaper::getPaperId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(paperIds)) {
                logger.error("fetch NewExam Question Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("试卷ID不存在");
            }
            Map<String, NewPaper> newPaperMap = loadNewPapersByDocIdsIncludeDisable(paperIds);
            if (newPaperMap.isEmpty()) {
                logger.error("fetch NewExam Question Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("试卷不存在或者已经下架");
            }

            Map<Long, User> userMap = newExamReportProcessor.loadClazzStudents(teacher, newExam.getSubject(), clazzId);
            String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
            List<String> newExamRegistrationIds = new LinkedList<>();
            for (Long userId : userMap.keySet()) {
                NewExamRegistration.ID newExamRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), userId.toString());
                newExamRegistrationIds.add(newExamRegistrationId.toString());
            }
            Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamRegistrationIds);
            List<String> processIds = new LinkedList<>();

            //班级题分析结构类
            NewExamClazzQuestions newExamClazzQuestions = new NewExamClazzQuestions();
            Map<String, NewExamPaperInfo> newExamPaperInfoMap = new LinkedHashMap<>();
            // 初始化试卷名字信息，对应下拉框
            for (NewExam.EmbedPaper paper : papers) {
                String paperId = paper.getPaperId();
                if (newPaperMap.containsKey(paperId)) {
                    //试卷
                    NewExamPaperInfo newExamPaperInfo = new NewExamPaperInfo();
                    //模块
                    List<NewExamDetailToTheme> themes = new LinkedList<>();
                    newExamPaperInfo.setPaperId(paperId);
                    newExamPaperInfoMap.put(paperId, newExamPaperInfo);
                    newExamPaperInfo.setPaperName(paper.getPaperName());
                    newExamClazzQuestions.getNewExamPaperInfos().add(newExamPaperInfo);
                    newExamPaperInfo.setThemes(themes);
                    newExamClazzQuestions.getThemes().put(paperId, themes);
                }
            }
            //各个试卷完成的人数；参与人数；提交人数
            for (Map.Entry<String, NewExamResult> entry : newExamResultMap.entrySet()) {
                String paperId = NewExamPaperHelper.fetchPaperId(entry.getValue(), newExam, entry.getValue().getUserId());
                if (newExamPaperInfoMap.containsKey(paperId)) {
                    if (MapUtils.isNotEmpty(entry.getValue().getAnswers())) {
                        processIds.addAll(entry.getValue().getAnswers().values());
                    }
                    NewExamPaperInfo newExamPaperInfo = newExamPaperInfoMap.get(paperId);
                    newExamPaperInfo.setJoinNum(1 + newExamPaperInfo.getJoinNum());
                    if (entry.getValue().getSubmitAt() != null) {
                        newExamPaperInfo.setSubmitNum(1 + newExamPaperInfo.getSubmitNum());
                    }
                }
            }
            Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(processIds);
            Map<String, List<NewExamDetailToTheme>> themeMap = newExamClazzQuestions.getThemes();
            // 题信息统计
            handlerNewExamDetailToQuestion(paperIds, newExamProcessResultMap, newPaperMap, themeMap);
            //是否包含口语
            List<String> contentTypes = newExam.getContentTypes() != null ? newExam.getContentTypes() : Collections.emptyList();
            boolean hasOral = contentTypes.contains("ORAL");
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("newExamName", newExam.getName());
            mapMessage.add("clazzName", clazz.formalizeClazzName());
            mapMessage.add("hasOral", hasOral);
            mapMessage.add("examEndTime", DateUtils.dateToString(newExam.getExamStopAt(), "MM-dd HH:mm"));
            mapMessage.add("examCorrectEndTime", DateUtils.dateToString(newExam.getCorrectStopAt(), "MM-dd HH:mm"));
            mapMessage.add("newExamPaperInfos", newExamClazzQuestions.getNewExamPaperInfos());
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch NewExam Question Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    //班级学生参与考试信息接口
    @Override
    public MapMessage fetchNewExamAttendanceReport(Teacher teacher, String newExamId, Long clazzId) {
        try {
            if (StringUtils.isBlank(newExamId) || clazzId <= 0) {
                logger.error("fetch NewExam Attendance Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("参数有误");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("fetch NewExam Attendance Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("考试不存在");
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            }
            Map<Long, User> userMap = newExamReportProcessor.loadClazzStudents(teacher, newExam.getSubject(), clazzId);
            String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
            List<String> newExamRegistrationIds = new LinkedList<>();
            for (Long userId : userMap.keySet()) {
                NewExamRegistration.ID newExamRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), userId.toString());
                newExamRegistrationIds.add(newExamRegistrationId.toString());
            }
            Map<Long, NewExamRegistration> newExamRegistrationMap = newExamRegistrationDao.loads(newExamRegistrationIds)
                    .values()
                    .stream()
                    .filter(o -> !SafeConverter.toBoolean(o.getBeenCanceled()))
                    .collect(Collectors.toMap(NewExamRegistration::getUserId, Function.identity()));

            //考试考勤
            AttendanceStudentV1 attendanceStudents = new AttendanceStudentV1();
            for (User u : userMap.values()) {
                NewExamStudent student = new NewExamStudent();
                student.setUserId(u.getId());
                student.setUserName(u.fetchRealnameIfBlankId());
                //是否参与考试
                if (newExamRegistrationMap.containsKey(u.getId())) {
                    NewExamRegistration n = newExamRegistrationMap.get(u.getId());
                    //是否交卷
                    if (n.getSubmitAt() != null) {
                        attendanceStudents.getSubmitStudents().add(student);
                    } else {
                        attendanceStudents.getUnSubmitStudents().add(student);
                    }
                } else {
                    attendanceStudents.getUnJoinStudents().add(student);
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("submitStudents", attendanceStudents.getSubmitStudents());
            mapMessage.add("unJoinStudents", attendanceStudents.getUnJoinStudents());
            mapMessage.add("unSubmitStudents", attendanceStudents.getUnSubmitStudents());
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch NewExam Attendance Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    //学生表格信息接口
    @Override
    public MapMessage fetchNewExamStudentReport(Teacher teacher, String newExamId, Long clazzId) {
        try {
            if (StringUtils.isBlank(newExamId) || clazzId <= 0) {
                logger.error("fetch NewExam Student Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("参数有误");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("fetch NewExam Student Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("考试不存在");
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            }
            List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
            List<String> paperIds = papers.stream().map(NewExam.EmbedPaper::getPaperId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(paperIds)) {
                logger.error("fetch NewExam Student Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("试卷ID不存在");
            }
            Map<String, NewPaper> newPaperMap = loadNewPapersByDocIdsIncludeDisable(paperIds);
            if (newPaperMap.isEmpty()) {
                logger.error("fetch NewExam Student Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("试卷不存在或者已经下架");
            }
            Map<Long, User> userMap = newExamReportProcessor.loadClazzStudents(teacher, newExam.getSubject(), clazzId);
            String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
            List<String> newExamRegistrationIds = new LinkedList<>();
            for (Long userId : userMap.keySet()) {
                NewExamRegistration.ID newExamRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), userId.toString());
                newExamRegistrationIds.add(newExamRegistrationId.toString());
            }
            Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamRegistrationIds);


            // 初始化试卷名字信息，对应下拉框
            Map<String, String> newExamPaperInfoMap = new LinkedHashMap<>();
            for (NewExam.EmbedPaper paper : papers) {
                String paperId = paper.getPaperId();
                if (newPaperMap.containsKey(paperId)) {
                    NewExamPaperInfo newExamPaperInfo = new NewExamPaperInfo();
                    newExamPaperInfo.setPaperId(paperId);
                    newExamPaperInfo.setPaperName(paper.getPaperName());
                    newExamPaperInfoMap.put(paperId, newExamPaperInfo.getPaperName());
                }
            }
            boolean issue = newExam.getResultIssueAt().before(new Date());
            // 学生分析
            NewExamStudentV1 newExamStudentV1 = new NewExamStudentV1();
            //TODO 待优化，直接user 循环
            handlerNewExamDetailToStudents(newExam, newExamPaperInfoMap, newExamResultMap, newPaperMap, newExamStudentV1, userMap);
            Integer totalScore = newPaperMap.values().iterator().next().getTotalScore();
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("issue", issue);
            mapMessage.add("issueTime", DateUtils.dateToString(newExam.getResultIssueAt(), "yyyy-MM-dd HH:mm"));
            mapMessage.add("newExamName", newExam.getName());
            mapMessage.add("single", newPaperMap.size() == 1);
            mapMessage.add("students", newExamStudentV1.getNewExamDetailToStudents());
            mapMessage.add("studentNum", userMap.size());
            mapMessage.add("paperTotalScore", totalScore);
            mapMessage.add("joinNum", newExamStudentV1.getJoinNum());
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch NewExam Student Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }


    @Override
    public MapMessage fetchNewExamStatisticsReport(Teacher teacher, String newExamId, Long clazzId) {
        try {
            if (StringUtils.isBlank(newExamId) || clazzId <= 0) {
                logger.error("fetch NewExam Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("参数有误");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("fetch NewExam Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("考试不存在");
            }
            NewExamStatistics newExamStatistics = new NewExamStatistics();
            newExamStatistics.setIssued(newExam.getResultIssueAt().before(new Date()));
            //考试未发布成绩的时候，不显示统计信息
            if (!newExamStatistics.isIssued()) {
                newExamStatistics.setIssueTime(DateUtils.dateToString(newExam.getResultIssueAt()));
                MapMessage mapMessage = MapMessage.successMessage();
                newExamStatistics.setIssueTime(DateUtils.dateToString(newExam.getResultIssueAt()));
                newExamStatistics.setIssueText("试卷分析将在 " + DateUtils.dateToString(newExam.getResultIssueAt()) + "之后开放");
                mapMessage.add("newExamStatistics", newExamStatistics);
                return mapMessage;
            }
            List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
            List<String> paperIds = papers.stream().map(NewExam.EmbedPaper::getPaperId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(paperIds)) {
                logger.error("fetch NewExam Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("试卷ID不存在");
            }
            Map<String, NewPaper> newPaperMap = loadNewPapersByDocIdsIncludeDisable(paperIds);
            if (newPaperMap.isEmpty()) {
                logger.error("fetch NewExam Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId());
                return MapMessage.errorMessage("试卷不存在或者已经下架");
            }
            Map<Long, User> userMap = newExamReportProcessor.loadClazzStudents(teacher, newExam.getSubject(), clazzId);

            String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
            List<String> newExamRegistrationIds = new LinkedList<>();
            for (Long userId : userMap.keySet()) {
                NewExamRegistration.ID newExamRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), userId.toString());
                newExamRegistrationIds.add(newExamRegistrationId.toString());
            }
            Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamRegistrationIds);
            List<String> processIds = newExamResultMap.values()
                    .stream()
                    .filter(o -> MapUtils.isNotEmpty(o.getAnswers()))
                    .map(o -> o.getAnswers().values())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(processIds);


            //paperId==>模块
            for (NewExam.EmbedPaper embedPaper : papers) {
                String paperId = embedPaper.getPaperId();
                if (newPaperMap.containsKey(paperId)) {
                    NewExamStatistics.Paper paper = new NewExamStatistics.Paper();
                    paper.setPaperId(paperId);
                    paper.setPaperName(embedPaper.getPaperName());
                    NewPaper newPaper = newPaperMap.get(paperId);
                    //各模块成绩初始化 moduleIndex 模块ID
                    int moduleIndex = 0;
                    for (NewPaperParts parts : newPaper.getParts()) {
                        NewExamStatistics.ModuleAchievement moduleAchievement = new NewExamStatistics.ModuleAchievement();
                        moduleAchievement.setDesc(parts.getTitle());
                        moduleAchievement.setStandardScore(SafeConverter.toFloat(parts.getScore()));
                        //因为process 的partId 最初的没设计好，是String类型
                        paper.getModuleAchievementMap().put(moduleIndex + "", moduleAchievement);
                        paper.getModuleAchievements().add(moduleAchievement);
                        moduleIndex++;
                    }
                    newExamStatistics.getPapers().add(paper);
                }
            }

            NewExamStatisticContext newExamStatisticContext = new NewExamStatisticContext(userMap,
                    newExamResultMap,
                    newExamProcessResultMap,
                    newPaperMap,
                    newExamStatistics,//试卷分析
                    newExam
            );
            //总成绩分析
            handlerAchievementAnalysisPart(newExamStatisticContext);
            //成绩分布
            handlerScoreDistributionPart(newExamStatisticContext);
            //题目成绩分析
            handlerModuleAchievementPart(newExamStatisticContext);
            MapMessage mapMessage = MapMessage.successMessage();

            //fixme mjx 目前只有数学展示报告入口
            if (Subject.MATH.equals(newExam.getSubject())) {
                newExamStatistics.setExamReportUrl(UrlUtils.buildUrlQuery("/view/newexamv2/report", MapUtils.map("exam_id", newExamId, "class_id", clazzId)));
            } else {
                newExamStatistics.setExamReportUrl("");
            }
            mapMessage.add("newExamStatistics", newExamStatistics);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch NewExam Report failed : newExamId {},clazzId {},tid {}", newExamId, clazzId, teacher.getId(), e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public TamExamInfo fetchTamExamInfo(String newExamId) {
        if (StringUtils.isBlank(newExamId)) {
            return null;
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return null;
        }
        if (newExam.getGroupId() == null) {
            return null;
        }
        if (newExam.getExamType() != NewExamType.independent) {
            return null;
        }
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newExam.getGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamRegistrationIds = userMap.values()
                .stream()
                .map(o -> new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), o.getId().toString()).toString())
                .collect(Collectors.toList());
        Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamRegistrationIds);
        double totalScore = 0;
        int submitNum = 0;
        for (NewExamResult r : newExamResultMap.values()) {
            if (r.getSubmitAt() == null)
                continue;
            submitNum++;
            totalScore += r.processScore(100);
        }
        TamExamInfo result = new TamExamInfo(newExamId);

        if (submitNum != 0) {
            result.setSubmitNum(submitNum);
            if (totalScore > 0) {
                double avgScore = new BigDecimal(totalScore).divide(new BigDecimal(submitNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                result.setAvgScore(avgScore);
            }
        }

        return result;
    }

    //1.试卷模块数据初始化
    //2.NewExamProcessResult 数据处理
    //3.数据后处理
    private void handlerModuleAchievementPart(NewExamStatisticContext newExamStatisticContext) {
        Map<String, NewExamProcessResult> newExamProcessResultMap = newExamStatisticContext.getNewExamProcessResultMap();
        NewExamStatistics newExamStatistics = newExamStatisticContext.getNewExamStatistics();


        //paperMap paperId ==>qid==>模块的一个链接
        Map<String, NewExamStatistics.Paper> paperMap = newExamStatistics.getPapers()
                .stream()
                .collect(Collectors.toMap(NewExamStatistics.Paper::getPaperId, Function.identity()));


        // NewExamProcessResult 根据 paperId 和信息去取相应的模块。然后将数据填入
        // 这里的newProcessResult 没有根据题过滤，如果试卷有改题的情况，有不一致
        for (NewExamProcessResult n : newExamProcessResultMap.values()) {
            if (!paperMap.containsKey(n.getPaperDocId())) continue;
            NewExamStatistics.Paper moduleAchievementPart = paperMap.get(n.getPaperDocId());
            // qid 和模块对于关系
            Map<String, NewExamStatistics.ModuleAchievement> map = moduleAchievementPart.getModuleAchievementMap();
            if (!map.containsKey(n.getPartId())) continue;
            moduleAchievementPart.getUserIds().add(n.getUserId());
            NewExamStatistics.ModuleAchievement moduleAchievement = map.get(n.getPartId());
            moduleAchievement.getUserIds().add(n.getUserId());
            moduleAchievement.setTotalScore(moduleAchievement.getTotalScore() + SafeConverter.toDouble(n.getCorrectScore() != null ? n.getCorrectScore() : n.getScore()));
        }

        paperMap.values().forEach(moduleAchievementPart -> {
            if (moduleAchievementPart.getUserIds().size() > 0) {
                moduleAchievementPart.getModuleAchievements()
                        .stream()
                        .filter(p -> p.getUserIds().size() > 0)
                        .forEach(p -> {
                            //模块平均成绩
                            p.setAverScore(NewExamPaperHelper.simple(new BigDecimal(p.getTotalScore()).divide(new BigDecimal(p.getUserIds().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue()));
                            //得分率
                            p.setRate(NewExamPaperHelper.simple(new BigDecimal(p.getAverScore() * 100).divide(new BigDecimal(p.getStandardScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue()));
                            p.setUserIds(null);
                        });
                //参与人数
                moduleAchievementPart.setJoinNum(moduleAchievementPart.getUserIds().size());
                moduleAchievementPart.setUserIds(null);
            }
            moduleAchievementPart.setModuleAchievementMap(null);
        });
    }

    //1.分数成绩初始化
    //2.查询newExamResult，newExamResult分数处理
    //3.数据后处理

    private void handlerScoreDistributionPart(NewExamStatisticContext newExamStatisticContext) {
        Map<String, NewExamResult> newExamResultMap = newExamStatisticContext.getNewExamResultMap();
        Map<String, NewPaper> newPaperMap = newExamStatisticContext.getNewPaperMap();
        NewExamStatistics newExamStatistics = newExamStatisticContext.getNewExamStatistics();
        NewExam newExam = newExamStatisticContext.getNewExam();
        List<NewExam.EmbedRank> scoreHierarchies = newExam.getRanks();
        if (scoreHierarchies == null) {
            scoreHierarchies = NewExam.EmbedRank.defaultRanks;
        }
        //获得分解的比例
        //0.85,0.75.0.60,0
        Set<Double> keys = new LinkedHashSet<>();

        //scoreDistributionPart 成绩分布
        NewExamStatistics.ScoreDistributionPart scoreDistributionPart = newExamStatistics.getScoreDistributionPart();
        //设置个分布区间 第一个比较特殊的区间
        boolean flag = true;
        for (NewExam.EmbedRank scoreHierarchy : scoreHierarchies) {
            double key = new BigDecimal(SafeConverter.toInt(scoreHierarchy.getBottom())).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            keys.add(key);
            NewExamStatistics.ScoreDistribution scoreDistribution = new NewExamStatistics.ScoreDistribution();
            String scoreDesc;
            if (flag) {
                scoreDesc = "[" + scoreHierarchy.getBottom() + "%," + scoreHierarchy.getTop() + "%]";
                flag = false;
            } else {
                scoreDesc = "[" + scoreHierarchy.getBottom() + "%," + scoreHierarchy.getTop() + "%)";
            }
            scoreDistribution.setScoreDesc(scoreDesc);
            scoreDistribution.setDecs(scoreHierarchy.getRankName());
            scoreDistribution.setMax(scoreHierarchy.getTop());
            scoreDistribution.setMin(scoreHierarchy.getBottom());
            scoreDistributionPart.getScoreDistributionMap().put(key, scoreDistribution);
            scoreDistributionPart.getScoreDistributions().add(scoreDistribution);
        }
        int totalScore = SafeConverter.toInt(newPaperMap.values().iterator().next().getTotalScore());

        //计算各个等级类型的学生人数统计
        for (NewExamResult newExamResult : newExamResultMap.values()) {
            double score = SafeConverter.toDouble(newExamResult.getCorrectScore() != null ? newExamResult.getCorrectScore() : newExamResult.getScore());
            scoreDistributionPart.setJoinNum(scoreDistributionPart.getJoinNum() + 1);
            double va = score / totalScore;
            for (Double key : keys) {
                if (va >= key) {
                    NewExamStatistics.ScoreDistribution s = scoreDistributionPart.getScoreDistributionMap().get(key);
                    s.setNum(s.getNum() + 1);
                    break;
                }

            }
        }
        //计算各个等级类型占比
        if (scoreDistributionPart.getJoinNum() > 0) {
            List<NewExamStatistics.ScoreDistribution> scoreDistributions = scoreDistributionPart.getScoreDistributions();
            for (NewExamStatistics.ScoreDistribution s : scoreDistributions) {
                if (s.getNum() == 0) continue;
                s.setRate(NewExamPaperHelper.simple(new BigDecimal(s.getNum() * 100).divide(new BigDecimal(scoreDistributionPart.getJoinNum()), 2, BigDecimal.ROUND_HALF_UP).doubleValue()));
            }
        }
        scoreDistributionPart.setScoreDistributionMap(null);
    }

    //1.取到各个学生的成绩
    //2.成绩处理，平均分.最大分.最小分.得分率.方差
    private void handlerAchievementAnalysisPart(NewExamStatisticContext newExamStatisticContext) {
        Map<String, NewExamResult> newExamResultMap = newExamStatisticContext.getNewExamResultMap();
        NewExam newExam = newExamStatisticContext.getNewExam();
        Map<String, NewPaper> newPaperMap = newExamStatisticContext.getNewPaperMap();
        NewPaper newPaper = newPaperMap.values().iterator().next();
        NewExamStatistics newExamStatistics = newExamStatisticContext.getNewExamStatistics();
        //achievementAnalysisPart 总成绩分析数据
        NewExamStatistics.AchievementAnalysisPart achievementAnalysisPart = newExamStatistics.getAchievementAnalysisPart();
        achievementAnalysisPart.setStandardScore(SafeConverter.toInt(newPaper.getTotalScore()));

        //最低分
        Double minScore = null;
        //最高分
        double maxScore = 0.0;
        //总分
        double totalScore = 0.0;
        for (NewExamResult newExamResult : newExamResultMap.values()) {
            String paperId = NewExamPaperHelper.fetchPaperId(newExamResult, newExam, newExamResult.getUserId());
            if (!newPaperMap.containsKey(paperId))
                continue;
            double score = newExamResult.processScore(SafeConverter.toInt(newPaper.getTotalScore()));
            score = new BigDecimal(score).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            achievementAnalysisPart.getScoreList().add(score);

            if (minScore == null || score < minScore) {
                minScore = score;
            }
            if (maxScore < score) {
                maxScore = score;
            }
            totalScore += score;
        }

        if (achievementAnalysisPart.getScoreList().size() > 0) {
            achievementAnalysisPart.setJoinNum(achievementAnalysisPart.getScoreList().size());

            //平均分
            if (totalScore > 0) {
                achievementAnalysisPart.setAverScore(NewExamPaperHelper.simple(new BigDecimal(totalScore).divide(new BigDecimal(achievementAnalysisPart.getJoinNum()), 2, BigDecimal.ROUND_HALF_UP).doubleValue()));
            }
            //计算方差和平均差
            double totalVarianceScore = 0.0;
            for (double s : achievementAnalysisPart.getScoreList()) {
                totalVarianceScore += Math.pow(s - achievementAnalysisPart.getAverScore(), 2);
            }
            if (totalVarianceScore > 0) {
                achievementAnalysisPart.setVarianceScore(
                        NewExamPaperHelper.simple(new BigDecimal(totalVarianceScore)
                                .divide(new BigDecimal(achievementAnalysisPart.getJoinNum()), 2, BigDecimal.ROUND_HALF_UP)
                                .doubleValue()));
                achievementAnalysisPart.setStandardDeviationScore(NewExamPaperHelper.simple(new BigDecimal(Math.sqrt(achievementAnalysisPart.getVarianceScore())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
            }
            minScore = minScore == null ? 0.0 : NewExamPaperHelper.simple(minScore);
            achievementAnalysisPart.setMinScore(minScore);
            achievementAnalysisPart.setMaxScore(NewExamPaperHelper.simple(maxScore));
            if (achievementAnalysisPart.getStandardScore() > 0) {
                achievementAnalysisPart.setScoreRate(NewExamPaperHelper.simple(new BigDecimal(achievementAnalysisPart.getAverScore() * 100).divide(new BigDecimal(achievementAnalysisPart.getStandardScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue()));
            }
        }
    }

    private void handlerNewExamDetailToQuestion(List<String> paperIds,
                                                Map<String, NewExamProcessResult> newExamProcessResultMap,
                                                Map<String, NewPaper> newPaperMap,
                                                Map<String, List<NewExamDetailToTheme>> themeMap) {
        Set<String> qids = newPaperMap.values()
                .stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getQuestions()))
                .map(NewPaper::getQuestions)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(XxBaseQuestion::getId)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids);
        //初始化各个试卷各个模块每小题每个小题的信息
        for (String paperId : paperIds) {
            if (newPaperMap.containsKey(paperId)) {
                NewPaper newPaper = newPaperMap.get(paperId);
                Map<String, Double> questionScoreMapByQid = newPaper.getQuestionScoreMapByQid();
                //小题号
                int index = 1;
                for (NewPaperParts newPaperParts : newPaper.getParts()) {
                    //各个模块
                    NewExamDetailToTheme theme = new NewExamDetailToTheme();
                    theme.setDesc(newPaperParts.getTitle());
                    if (newPaperParts.getQuestions() != null) {
                        //各个题
                        for (NewPaperQuestion newPaperQuestion : newPaperParts.getQuestions()) {
                            if (newQuestionMap.containsKey(newPaperQuestion.getId())) {
                                NewQuestion newQuestion = newQuestionMap.get(newPaperQuestion.getId());
                                double standardScore = NewExamPaperHelper.simple(new BigDecimal(SafeConverter.toDouble(questionScoreMapByQid.getOrDefault(newQuestion.getId(), 0.0)))
                                        .divide(new BigDecimal(newQuestion.getContent().getSubContents().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                                if (newQuestion.getContent() != null && newQuestion.getContent().getSubContents() != null) {
                                    NewExamDetailToTheme.NewExamDetailToQuestion newExamDetailToQuestion = new NewExamDetailToTheme.NewExamDetailToQuestion();
                                    newExamDetailToQuestion.setDocId(newQuestion.getDocId());
                                    //小题号
                                    int subIndex = 0;
                                    //各个小题
                                    for (NewQuestionsSubContents ignored : newQuestion.getContent().getSubContents()) {
                                        NewExamDetailToTheme.NewExamDetailToSubQuestion subQuestion = new NewExamDetailToTheme.NewExamDetailToSubQuestion();
                                        newExamDetailToQuestion.getSubQuestions().add(subQuestion);
                                        subQuestion.setIndex(index);
                                        subQuestion.setSubIndex(subIndex);
                                        subQuestion.setStandardScore(standardScore);
                                        subQuestion.setQid(newPaperQuestion.getId());
                                        index++;
                                        subIndex++;
                                    }
                                    newExamDetailToQuestion.setQid(newPaperQuestion.getId());
                                    theme.getNewExamQuestionDetailToQuestions().add(newExamDetailToQuestion);
                                }
                            }
                        }
                    }
                    if (themeMap.containsKey(paperId)) {
                        themeMap.get(paperId).add(theme);
                    }
                }
            }
        }
        // paperId==>qid==>模块题
        Map<String, Map<String, NewExamDetailToTheme.NewExamDetailToQuestion>> mapMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<NewExamDetailToTheme>> entry : themeMap.entrySet()) {
            mapMap.put(entry.getKey(),
                    entry.getValue()
                            .stream()
                            .map(NewExamDetailToTheme::getNewExamQuestionDetailToQuestions).flatMap(Collection::stream)
                            .collect(Collectors.toMap(NewExamDetailToTheme.NewExamDetailToQuestion::getDocId, Function.identity())));
        }
        //newExamProcessResult 根据 paperId 和qid 拿到对于的question结构部分，然后将newExamProcessResult将数据填入
        for (NewExamProcessResult newExamProcessResult : newExamProcessResultMap.values()) {
            if (themeMap.containsKey(newExamProcessResult.getPaperDocId())) {
                List<NewExamDetailToTheme> themes = themeMap.get(newExamProcessResult.getPaperDocId());
                int moduleIndex = SafeConverter.toInt(newExamProcessResult.getPartId());
                if (themes.size() <= moduleIndex) {
                    continue;
                }
                NewExamDetailToTheme theme = themes.get(moduleIndex);
                Map<String, NewExamDetailToTheme.NewExamDetailToQuestion> map = mapMap.get(newExamProcessResult.getPaperDocId());
                if (map.containsKey(newExamProcessResult.getQuestionDocId())) {
                    double score = SafeConverter.toDouble(newExamProcessResult.processScore());
                    theme.setTotalScore(score + theme.getTotalScore());
                    long duration = SafeConverter.toLong(newExamProcessResult.getDurationMilliseconds());
                    theme.setTotalDuration(duration + theme.getTotalDuration());
                    theme.getUserIds().add(newExamProcessResult.getUserId());
                    //1取题
                    NewExamDetailToTheme.NewExamDetailToQuestion newExamDetailToQuestion = map.get(newExamProcessResult.getQuestionDocId());
                    newExamDetailToQuestion.setTotalScore(score + newExamDetailToQuestion.getTotalScore());
                    newExamDetailToQuestion.setTotalDuration(duration + newExamDetailToQuestion.getTotalDuration());
                    NewQuestion newQuestion = newQuestionMap.get(newExamDetailToQuestion.getQid());
                    if (newQuestion == null) continue;
                    int i = 0;
                    List<Double> subScore = newExamProcessResult.processSubScore();
                    if (CollectionUtils.isNotEmpty(subScore) && subScore.size() == newExamDetailToQuestion.getSubQuestions().size()) {
                        //2小题
                        for (NewExamDetailToTheme.NewExamDetailToSubQuestion subQuestion : newExamDetailToQuestion.getSubQuestions()) {
                            subQuestion.setNum(subQuestion.getNum() + 1);
                            subQuestion.setTotalScore(subQuestion.getTotalScore() + SafeConverter.toDouble(subScore.get(i)));
                            i++;
                        }
                    }
                }
            }
        }
        //每个模块的平均分和平均时间
        themeMap.values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(o ->
                        {
                            for (NewExamDetailToTheme.NewExamDetailToQuestion question : o.getNewExamQuestionDetailToQuestions()) {
                                // 每小题分数后处理
                                //小题的得分率
                                List<NewExamDetailToTheme.NewExamDetailToSubQuestion> subQuestions = question.getSubQuestions();
                                if (CollectionUtils.isNotEmpty(subQuestions)) {
                                    for (NewExamDetailToTheme.NewExamDetailToSubQuestion subQuestion : subQuestions) {
                                        if (subQuestion.getNum() > 0) {
                                            double averScore = new BigDecimal(subQuestion.getTotalScore()).divide(new BigDecimal(subQuestion.getNum()), 5, BigDecimal.ROUND_HALF_UP).doubleValue();
                                            subQuestion.setAverScore(new BigDecimal(averScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                                            if (subQuestion.getStandardScore() > 0) {
                                                subQuestion.setRate(new BigDecimal(averScore * 100).divide(new BigDecimal(subQuestion.getStandardScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                                            }
                                        }
                                    }
                                }
                                ///end
                            }
                            if (o.getUserIds().size() > 0) {
                                double totalScore = o.getTotalScore();
                                long totalDuration = o.getTotalDuration();
                                double s = new BigDecimal(totalScore).divide(new BigDecimal(o.getUserIds().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                int t = new BigDecimal(totalDuration).divide(new BigDecimal(1000 * o.getUserIds().size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                                o.setAverScore(NewExamPaperHelper.simple(s));
                                o.setAverDuration(t);
                            }
                        }
                );

        // 去除题结构，拿出小题的结构,因为前段并不需要题结构
        for (Map.Entry<String, List<NewExamDetailToTheme>> entry : themeMap.entrySet()) {
            if (CollectionUtils.isNotEmpty(entry.getValue())) {
                for (NewExamDetailToTheme theme : entry.getValue()) {
                    for (NewExamDetailToTheme.NewExamDetailToQuestion question : theme.getNewExamQuestionDetailToQuestions()) {
                        theme.getSubQuestions().addAll(question.getSubQuestions());
                    }
                    theme.setNewExamQuestionDetailToQuestions(null);
                }
            }
        }

    }


    //模块的成绩组合
    //1.取得模块的questionId 对应得到processId
    //2.processId得NewExamProcess 分数处理
    private void handlerNewExamDetailToStudents(NewExam newExam, Map<String, String> newExamPaperInfoMap, Map<String, NewExamResult> newExamResultMap, Map<String, NewPaper> newPaperMap, NewExamStudentV1 newExamDetail, Map<Long, User> userMap) {
        for (User user : userMap.values()) {
            NewExamDetailToStudent student = new NewExamDetailToStudent();
            student.setUserId(user.getId());
            student.setUserName(user.fetchRealnameIfBlankId());
            newExamDetail.getNewExamDetailToStudentMap().put(user.getId(), student);
        }
        int defaultTime = SafeConverter.toInt(newExam.getDurationMinutes()) * 60;
        DecimalFormat df = new DecimalFormat("###.##");
        for (NewExamResult newExamResult : newExamResultMap.values()) {
            if (newExamDetail.getNewExamDetailToStudentMap().containsKey(newExamResult.getUserId())) {
                newExamDetail.setJoinNum(1 + newExamDetail.getJoinNum());
                String paperId = newExamResult.obtainPaperId(newExam.obtainRandomPaperId());
                NewExamDetailToStudent student = newExamDetail.getNewExamDetailToStudentMap().get(newExamResult.getUserId());
                //学生所考试卷不存在
                if (!newPaperMap.containsKey(paperId)) {
                    student.setFlag(false);
                    continue;
                }
                NewPaper newPaper = newPaperMap.get(paperId);
                double score = newExamResult.processScore(SafeConverter.toInt(newPaper.getTotalScore()));
                score = new BigDecimal(score).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                score = NewExamPaperHelper.simple(score);
                int duration = new BigDecimal(SafeConverter.toLong(newExamResult.getDurationMilliseconds()))
                        .divide(new BigDecimal(1000), BigDecimal.ROUND_HALF_UP)
                        .intValue();
                if (defaultTime < duration) {
                    duration = defaultTime;
                }
                int minutes = duration / 60;
                int second = duration % 60;
                String durationStr = minutes != 0 ? minutes + "分" + second + "秒" : second + "秒";
                student.setDuration(duration);
                student.setDurationStr(durationStr);
                student.setScoreStr(df.format(score));
                student.setScore(score);
                student.setBegin(true);
                student.setPaperId(newExamResult.getPaperId());
                student.setFinished(newExamResult.getFinishAt() != null);
                student.setParameters(MapUtils.m(
                        "newexam_id", newExam.getId(),
                        "examDetailUrl", "/flash/loader/newexam/view.vpage",
                        "student_id", newExamResult.getUserId()
                ));
                student.setPaperName(newExamPaperInfoMap.getOrDefault(paperId, ""));
            }
        }
        //学生成绩后处理：分数从大到小排序，试卷从小到大排序
        newExamDetail.setNewExamDetailToStudents(newExamDetail.getNewExamDetailToStudentMap()
                .values()
                .stream()
                .filter(NewExamDetailToStudent::isFlag)
                .sorted((o1, o2) -> {
                    int compare = Double.compare(o2.getScore(), o1.getScore());
                    if (compare != 0) {
                        return compare;
                    }
                    return Integer.compare(o1.getDuration(), o2.getDuration());
                })
                .collect(Collectors.toList()));
        newExamDetail.setNewExamDetailToStudentMap(null);
    }


    @Override
    public MapMessage independentExamDetailForParent(String newExamId, Long studentId) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("测试不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }
//        NewPaper newPaper = paperLoaderClient.loadPaperByDocid(newExam.getPaperId());
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(newExam.fetchPaperId(studentId));
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在");
        }
        Long groupId = newExam.getGroupId();
        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            return MapMessage.errorMessage("班组不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
        }
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(groupId)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
        if (!userMap.containsKey(studentId)) {
            return MapMessage.errorMessage("不在考试时所属班组，无法查看考试报告");
        }
        User student = userMap.get(studentId);
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamResultIds = userMap.keySet()
                .stream()
                .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                .collect(Collectors.toList());
        Map<String, NewExamResult> newExamResults = newExamResultDao.loads(newExamResultIds);
        // 学生信息
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("imgUrl", student.fetchImageUrl());
        mapMessage.add("studentName", student.fetchRealnameIfBlankId());
        // 考试说明
        mapMessage = generateExamTypeInfo(mapMessage, newPaper);
        // 考试信息
        mapMessage.add("examStartAt", DateUtils.dateToString(newExam.getExamStartAt(), "yyyy年MM月dd日HH:mm"));
        mapMessage.add("examStopAt", DateUtils.dateToString(newExam.getExamStopAt(), "yyyy年MM月dd日HH:mm"));
        mapMessage.add("examContent", newExam.getName());
        mapMessage.add("examQuestionCount", newPaper.getQuestions().size());
        mapMessage.add("examDurationMinutes", newExam.getDurationMinutes());
        boolean showScore = true;
        String errorInfo = "";
        mapMessage.add("fullScore", SafeConverter.toInt(newPaper.getTotalScore()));
        Date now = new Date();
        if (now.before(newExam.getExamStartAt())) {
            showScore = false;
            errorInfo = "考试还未开始";
            // 班组总体信息
            mapMessage.add("avgScore", "--");
            mapMessage.add("minScore", "--");
            mapMessage.add("maxScore", "--");
        } else {
            // 班组总体信息
            mapMessage = newExamReportProcessor.independentExamGeneralViewDate(mapMessage, userMap, newExamResults);
            String newExamResultId = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), studentId.toString()).toString();
            NewExamResult newExamResult = newExamResults.get(newExamResultId);
            if (newExamResult == null) {
                showScore = false;
                if (now.before(newExam.getExamStopAt())) {
                    errorInfo = "未提交，请家长督促完成此次在线考试";
                } else {
                    errorInfo = "考试已截止，学生未提交";
                }
            } else {
                // 孩子成绩
                mapMessage.add("score", new BigDecimal(SafeConverter.toDouble(newExamResult.getScore())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                List<String> errorQuestionIds = Collections.emptyList();
                if (newExamResult.getAnswers() != null) {
                    // 错题
                    Collection<String> processIds = newExamResult.getAnswers().values();
                    Map<String, NewExamProcessResult> processResultMap = newExamProcessResultDao.loads(processIds);
                    errorQuestionIds = processResultMap.values().stream()
                            .filter(processResult -> !SafeConverter.toBoolean(processResult.getGrasp()))
                            .map(NewExamProcessResult::getQuestionId)
                            .collect(Collectors.toList());
                }
                mapMessage.add("errorQuestionIds", errorQuestionIds);
            }
        }
        mapMessage.add("showScore", showScore);
        if (!showScore) {
            mapMessage.add("errorInfo", errorInfo);
        }
        mapMessage.add("subject", newExam.getSubject());
        return mapMessage;
    }

    private MapMessage generateExamTypeInfo(MapMessage mapMessage, NewPaper newPaper) {
        int star = 0;
        String examType = "";
        String examTypeName = "";
        String description = "";
        NewPaper.ExtraQuestionType extraQuestionType = newPaper.getExtraQuestionType();
        if (extraQuestionType != null) {
            switch (extraQuestionType) {
                case WORD:
                    star = 5;
                    examTypeName = "单词";
                    description = "词汇是小学英语学习的基础和根基，在考试中具有举足轻重的地位";
                    break;
                case GRAMMAR:
                    star = 4;
                    examTypeName = "语法";
                    description = "句式语法对学生的阅读、写作成绩和听说水平都有着重大的影响";
                    break;
                case LISTEN:
                    star = 3;
                    examTypeName = "听力";
                    description = "所有语言的学习都始于听，没有输入就没有输出。听力在考试中所占比重越来越大";
                    break;
                case ORAL:
                    star = 2;
                    examTypeName = "口语";
                    description = "交流是学习语言之目的，口语水平测试会越来越重要地影响学生整体英语水平评价";
                    break;
                default:
                    break;
            }
            examType = extraQuestionType.name();
        }
        mapMessage.add("examType", examType);
        mapMessage.add("examTypeName", examTypeName);
        mapMessage.add("star", star);
        mapMessage.add("description", description);
        return mapMessage;
    }

    @Override
    public MapMessage independentExamDetailForShare(String newExamId) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("测试不存在");
        }
//        NewPaper newPaper = paperLoaderClient.loadPaperByDocid(newExam.getPaperId());
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(newExam.obtainRandomPaperId());
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在");
        }
        Long groupId = newExam.getGroupId();
        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            return MapMessage.errorMessage("班组不存在");
        }
        Long clazzId = groupMapper.getClazzId();
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在");
        }
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(groupId)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamResultIds = userMap.keySet()
                .stream()
                .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                .collect(Collectors.toList());
        Map<String, NewExamResult> newExamResults = newExamResultDao.loads(newExamResultIds);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("clazzName", clazz.formalizeClazzName());
        // 考试说明
        mapMessage = generateExamTypeInfo(mapMessage, newPaper);
        // 考试信息
        mapMessage.add("examStartAt", DateUtils.dateToString(newExam.getExamStartAt(), "yyyy年MM月dd日HH:mm"));
        mapMessage.add("examStopAt", DateUtils.dateToString(newExam.getExamStopAt(), "yyyy年MM月dd日HH:mm"));
        mapMessage.add("examContent", newExam.getName());
        mapMessage.add("examQuestionCount", newPaper.getQuestions().size());
        mapMessage.add("examDurationMinutes", newExam.getDurationMinutes());
        mapMessage.add("fullScore", SafeConverter.toInt(newPaper.getTotalScore()));
        mapMessage = newExamReportProcessor.independentExamGeneralViewDate(mapMessage, userMap, newExamResults);
        Date now = new Date();
        if (now.before(newExam.getExamStartAt())) {
            // 班组总体信息
            mapMessage.put("avgScore", "--");
            mapMessage.put("minScore", "--");
            mapMessage.put("maxScore", "--");
        }
        mapMessage.add("subject", newExam.getSubject());
        return mapMessage;
    }

    @Override
    public MapMessage loadNewExamParentReport(String newExamId, Long studentId) {
        if (StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("考试id不能为空");
        }
        if (studentId == null) {
            return MapMessage.errorMessage("学生id不能为空");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("无效的考试id");
        }
        if (newExam.getResultIssueAt().before(new Date())) {
            return MapMessage.errorMessage("未到成绩发布时间，无法查看考试结果");
        }
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamRegistration.ID studentRegistrationId = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), studentId.toString());
        NewExamRegistration studentRegistration = newExamRegistrationDao.load(studentRegistrationId.toString());
        if (studentRegistration == null || Objects.equals(true, studentRegistration.getBeenCanceled())) {
            return MapMessage.errorMessage("该学生未报名");
        }
        double studentScore = SafeConverter.toDouble(studentRegistration.getScore());
        double highScore = 0;
        double averageScore = 0;
        double sumScore = 0;
        int allStudentCount = 0;
        int belowStudentCount = 0;
        List<String> newExamRegistrationIds = newExamRegistrationDao.findByNewExam(newExam);
        Map<String, NewExamRegistration> newExamRegistrationMap = newExamRegistrationDao.loads(newExamRegistrationIds);
        if (MapUtils.isNotEmpty(newExamRegistrationMap)) {
            for (NewExamRegistration newExamRegistration : newExamRegistrationMap.values()) {
                if (Objects.equals(false, newExamRegistration.getBeenCanceled())) {
                    double score = SafeConverter.toDouble(newExamRegistration.getScore());
                    highScore = highScore < score ? score : highScore;
                    sumScore += score;
                    allStudentCount++;
                    if (score < studentScore) {
                        belowStudentCount++;
                    }
                }
            }
            if (allStudentCount != 0) {
                averageScore = sumScore / allStudentCount;
            }
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("score", studentScore);
        mapMessage.put("highScore", highScore);
        mapMessage.put("averageScore", averageScore);
        mapMessage.put("allStudentCount", allStudentCount);
        mapMessage.put("belowStudentCount", belowStudentCount);
        return mapMessage;
    }


    @Override
    public List<Map<String, Object>> getRegionStatistic(String examId, ExRegion exRegion, String paperId) {
        if (exRegion == null) {
            return Collections.emptyList();
        }
        List<RptMockNewExamCounty> rptMockNewExamCounties = rptMockNewExamCountyDao.loadRegions(examId, exRegion.getCityCode());
        if (CollectionUtils.isEmpty(rptMockNewExamCounties)) {
            return Collections.emptyList();
        }
        if (StringUtils.isNotBlank(paperId)) {
            rptMockNewExamCounties = rptMockNewExamCounties
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(e -> paperId.equals(e.getPaperDocId()))
                    .collect(Collectors.toList());
        }
        List<Map<String, Object>> results = new ArrayList<>(rptMockNewExamCounties.size());
        for (RptMockNewExamCounty rptMockNewExamCounty : rptMockNewExamCounties) {
            Map<String, Object> countyMap = new LinkedHashMap<>();
            countyMap.put("examId", rptMockNewExamCounty.getExamId());
            countyMap.put("paperId", rptMockNewExamCounty.getPaperDocId());
            countyMap.put("countyId", rptMockNewExamCounty.getCountyId());
            countyMap.put("countyName", rptMockNewExamCounty.getCountyName());
            countyMap.put("realStuNum", rptMockNewExamCounty.getRealStuNum());
            countyMap.put("actStuNum", rptMockNewExamCounty.getActStuNum());
            Double avgScore = rptMockNewExamCounty.getAvgScore();
            if (avgScore == null || avgScore <= 0) {
                avgScore = 0D;
            }
            String avgScoreStr = String.format("%.2f", avgScore);
            countyMap.put("avgScore", avgScoreStr.equals("0.00") ? "0" : avgScoreStr);
            Double avgDuration = rptMockNewExamCounty.getAvgDuration() == null ? 0 : rptMockNewExamCounty.getAvgDuration() / 1000;
            int minute = (int) (avgDuration / 60);
            int second = (int) (avgDuration - minute * 60);
            countyMap.put("avgDuration", minute + "分" + second + "秒");
            countyMap.put("maxScore", rptMockNewExamCounty.getMaxScore());
            countyMap.put("minScore", rptMockNewExamCounty.getMinScore());
            String rankJson = rptMockNewExamCounty.getRankJson();
            if (StringUtils.isNotBlank(rankJson)) {
                rankJson = rankJson.replaceAll("'", "\"");
            }
            List<Map> ranks = JsonUtils.fromJsonToList(rankJson, Map.class);
            if (CollectionUtils.isNotEmpty(ranks)) {
                for (Map rank : ranks) {
                    double rate = SafeConverter.toDouble(rank.get("ranks_rat"));
                    rank.put("ranks_rat", (int) Math.round(rate * 100) + "%");
                }
            }
            countyMap.put("ranks", ranks);
            String partJson = rptMockNewExamCounty.getPartJson();
            if (StringUtils.isNotBlank(partJson)) {
                partJson = partJson.replaceAll("'", "\"");
            }
            List<Map> parts = JsonUtils.fromJsonToList(partJson, Map.class);
            if (CollectionUtils.isNotEmpty(parts)) {
                Comparator<Map> comparator = Comparator.comparingInt(e -> SafeConverter.toInt(e.get("part_id")));
                parts = parts.stream()
                        .filter(Objects::nonNull)
                        .filter(e -> e.get("part_id") != null)
                        .sorted(comparator)
                        .collect(Collectors.toList());
                for (Map part : parts) {
                    int rate = SafeConverter.toInt(part.get("part_id"));
                    part.put("part_id", rate + 1);
                    part.put("part_name", SafeConverter.toString(part.get("part_name")));
                    Double partAvgScore = SafeConverter.toDouble(part.get("part_avg_score"));
                    if (partAvgScore <= 0) {
                        partAvgScore = 0D;
                    }
                    String partAvgScoreStr = String.format("%.2f", partAvgScore);
                    part.put("part_avg_score", partAvgScoreStr.equals("0.00") ? "0" : partAvgScoreStr);
                    Double partAvgCorrectScore = SafeConverter.toDouble(part.get("part_avg_correctscore"));
                    if (partAvgCorrectScore <= 0) {
                        partAvgCorrectScore = 0D;
                    }
                    String partAvgCorrectScoreStr = String.format("%.2f", partAvgCorrectScore);
                    part.put("part_avg_correctscore", partAvgCorrectScoreStr.equals("0.00") ? "0" : partAvgCorrectScoreStr);
                }
            }
            countyMap.put("parts", parts);
            results.add(countyMap);
        }
        return results;
    }

    @Override
    public List<Map<String, Object>> getSchoolStatistic(String examId, String paperId, ExRegion exRegion, String countyId) {
        if (exRegion == null) {
            return Collections.emptyList();
        }
        List<RptMockNewExamSchool> rptMockNewExamSchools = rptMockNewExamSchoolDao.loadSchools(examId, SafeConverter.toInt(countyId));
        if (CollectionUtils.isEmpty(rptMockNewExamSchools)) {
            return Collections.emptyList();
        }
        if (StringUtils.isNotBlank(paperId)) {
            rptMockNewExamSchools = rptMockNewExamSchools
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(e -> paperId.equals(e.getPaperDocId()))
                    .collect(Collectors.toList());
        }
        List<Map<String, Object>> results = new ArrayList<>(rptMockNewExamSchools.size());
        for (RptMockNewExamSchool rptMockNewExamSchool : rptMockNewExamSchools) {
            Map<String, Object> schoolMap = new LinkedHashMap<>();
            schoolMap.put("examId", rptMockNewExamSchool.getExamId());
            schoolMap.put("paperId", rptMockNewExamSchool.getPaperDocId());
            schoolMap.put("schoolId", rptMockNewExamSchool.getSchoolId());
            schoolMap.put("schoolName", rptMockNewExamSchool.getSchoolName());
            schoolMap.put("realStuNum", rptMockNewExamSchool.getRealStuNum());
            schoolMap.put("actStuNum", rptMockNewExamSchool.getActStuNum());
            Double avgScore = rptMockNewExamSchool.getAvgScore();
            if (avgScore == null || avgScore <= 0) {
                avgScore = 0D;
            }
            String avgScoreStr = String.format("%.2f", avgScore);
            schoolMap.put("avgScore", avgScoreStr.equals("0.00") ? "0" : avgScoreStr);
            Double avgDuration = rptMockNewExamSchool.getAvgDuration() == null ? 0 : rptMockNewExamSchool.getAvgDuration() / 1000;
            int minute = (int) (avgDuration / 60);
            int second = (int) (avgDuration - minute * 60);
            schoolMap.put("avgDuration", minute + "分" + second + "秒");
            schoolMap.put("maxScore", rptMockNewExamSchool.getMaxScore());
            schoolMap.put("minScore", rptMockNewExamSchool.getMinScore());
            String rankJson = rptMockNewExamSchool.getRankJson();
            if (StringUtils.isNotBlank(rankJson)) {
                rankJson = rankJson.replaceAll("'", "\"");
            }
            List<Map> ranks = JsonUtils.fromJsonToList(rankJson, Map.class);
            if (CollectionUtils.isNotEmpty(ranks)) {
                for (Map rank : ranks) {
                    double rate = SafeConverter.toDouble(rank.get("ranks_rat"));
                    rank.put("ranks_rat", (int) Math.round(rate * 100) + "%");
                }
            }
            schoolMap.put("ranks", ranks);
            String partJson = rptMockNewExamSchool.getPartJson();
            if (StringUtils.isNotBlank(partJson)) {
                partJson = partJson.replaceAll("'", "\"");
            }
            List<Map> parts = JsonUtils.fromJsonToList(partJson, Map.class);
            if (CollectionUtils.isNotEmpty(parts)) {
                Comparator<Map> comparator = Comparator.comparingInt(e -> SafeConverter.toInt(e.get("part_id")));
                parts = parts.stream()
                        .filter(e -> e.get("part_id") != null)
                        .sorted(comparator)
                        .collect(Collectors.toList());
                for (Map part : parts) {
                    int rate = SafeConverter.toInt(part.get("part_id"));
                    part.put("part_id", rate + 1);
                    part.put("part_name", SafeConverter.toString(part.get("part_name")));
                    Double partAvgScore = SafeConverter.toDouble(part.get("part_avg_score"));
                    if (partAvgScore <= 0) {
                        partAvgScore = 0D;
                    }
                    String partAvgScoreStr = String.format("%.2f", partAvgScore);
                    part.put("part_avg_score", partAvgScoreStr.equals("0.00") ? "0" : partAvgScoreStr);
                    Double partAvgCorrectScore = SafeConverter.toDouble(part.get("part_avg_correctscore"));
                    if (partAvgCorrectScore <= 0) {
                        partAvgCorrectScore = 0D;
                    }
                    String partAvgCorrectScoreStr = String.format("%.2f", partAvgCorrectScore);
                    part.put("part_avg_correctscore", partAvgCorrectScoreStr.equals("0.00") ? "0" : partAvgCorrectScoreStr);
                }
            }
            schoolMap.put("parts", parts);
            results.add(schoolMap);
        }
        return results;
    }

    @Override
    public List<Map<String, Object>> getClassStatistic(String examId, String paperId, ExRegion exRegion, String schoolId) {
        if (exRegion == null) {
            return Collections.emptyList();
        }
        List<RptMockNewExamClazz> rptMockNewExamClazzes = rptMockNewExamClazzDao.loadClasses(examId, SafeConverter.toInt(schoolId));
        if (CollectionUtils.isEmpty(rptMockNewExamClazzes)) {
            return Collections.emptyList();
        }
        if (StringUtils.isNotBlank(paperId)) {
            rptMockNewExamClazzes = rptMockNewExamClazzes
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(e -> paperId.equals(e.getPaperDocId()))
                    .collect(Collectors.toList());
        }
        List<Map<String, Object>> results = new ArrayList<>(rptMockNewExamClazzes.size());
        for (RptMockNewExamClazz rptMockNewExamClazz : rptMockNewExamClazzes) {
            Map<String, Object> clazzMap = new LinkedHashMap<>();
            clazzMap.put("examId", rptMockNewExamClazz.getExamId());
            clazzMap.put("paperId", rptMockNewExamClazz.getPaperDocId());
            clazzMap.put("clazzId", rptMockNewExamClazz.getClassId());
            clazzMap.put("clazzName", rptMockNewExamClazz.getClassName());
            clazzMap.put("realStuNum", rptMockNewExamClazz.getRealStuNum());
            clazzMap.put("actStuNum", rptMockNewExamClazz.getActStuNum());
            Double avgScore = rptMockNewExamClazz.getAvgScore();
            if (avgScore == null || avgScore <= 0) {
                avgScore = 0D;
            }
            String avgScoreStr = String.format("%.2f", avgScore);
            clazzMap.put("avgScore", avgScoreStr.equals("0.00") ? "0" : avgScoreStr);
            Double avgDuration = rptMockNewExamClazz.getAvgDuration() == null ? 0 : rptMockNewExamClazz.getAvgDuration() / 1000;
            int minute = (int) (avgDuration / 60);
            int second = (int) (avgDuration - minute * 60);
            clazzMap.put("avgDuration", minute + "分" + second + "秒");
            clazzMap.put("maxScore", rptMockNewExamClazz.getMaxScore());
            clazzMap.put("minScore", rptMockNewExamClazz.getMinScore());
            String rankJson = rptMockNewExamClazz.getRankJson();
            if (StringUtils.isNotBlank(rankJson)) {
                rankJson = rankJson.replaceAll("'", "\"");
            }
            List<Map> ranks = JsonUtils.fromJsonToList(rankJson, Map.class);
            if (CollectionUtils.isNotEmpty(ranks)) {
                for (Map rank : ranks) {
                    double rate = SafeConverter.toDouble(rank.get("ranks_rat"));
                    rank.put("ranks_rat", (int) Math.round(rate * 100) + "%");
                }
            }
            clazzMap.put("ranks", ranks);
            String partJson = rptMockNewExamClazz.getPartJson();
            if (StringUtils.isNotBlank(partJson)) {
                partJson = partJson.replaceAll("'", "\"");
            }
            List<Map> parts = JsonUtils.fromJsonToList(partJson, Map.class);
            if (CollectionUtils.isNotEmpty(parts)) {
                Comparator<Map> comparator = Comparator.comparingInt(e -> SafeConverter.toInt(e.get("part_id")));
                parts = parts.stream()
                        .filter(e -> e.get("part_id") != null)
                        .sorted(comparator)
                        .collect(Collectors.toList());
                for (Map part : parts) {
                    int rate = SafeConverter.toInt(part.get("part_id"));
                    part.put("part_id", rate + 1);
                    part.put("part_name", SafeConverter.toString(part.get("part_name")));
                    Double partAvgScore = SafeConverter.toDouble(part.get("part_avg_score"));
                    if (partAvgScore <= 0) {
                        partAvgScore = 0D;
                    }
                    String partAvgScoreStr = String.format("%.2f", partAvgScore);
                    part.put("part_avg_score", partAvgScoreStr.equals("0.00") ? "0" : partAvgScoreStr);
                    Double partAvgCorrectScore = SafeConverter.toDouble(part.get("part_avg_correctscore"));
                    if (partAvgCorrectScore <= 0) {
                        partAvgCorrectScore = 0D;
                    }
                    String partAvgCorrectScoreStr = String.format("%.2f", partAvgCorrectScore);
                    part.put("part_avg_correctscore", partAvgCorrectScoreStr.equals("0.00") ? "0" : partAvgCorrectScoreStr);
                }
            }
            clazzMap.put("parts", parts);
            results.add(clazzMap);
        }
        return results;
    }

    @Override
    public List<RptMockNewExamStudent> getStudentAchievement(String examId) {
        return rptMockNewExamStudentDao.loadStudents(examId);
    }

    @Override
    public List<RptMockNewExamStudent> getStudentAchievement(ExRegion region, String examId) {
        List<RptMockNewExamStudent> rptMockNewExamStudents = rptMockNewExamStudentDao.loadStudents(examId);
        if (CollectionUtils.isEmpty(rptMockNewExamStudents) || region == null) {
            return Collections.emptyList();
        }
        return rptMockNewExamStudents
                .stream()
                .filter(student -> (
                        (student.getProvinceId() != null && student.getProvinceId().equals(region.getProvinceCode()))
                                || (student.getCityId() != null && student.getCityId().equals(region.getCityCode()))
                                || (student.getCountyId() != null && student.getCountyId().equals(region.getCountyCode()))
                ))
                .collect(Collectors.toList());
    }

    public List<RptMockNewExamStudent> getStudentAchievement(String examId, Integer clazzId) {
        List<RptMockNewExamStudent> rptMockNewExamStudents = rptMockNewExamStudentDao.loadGroupStudents(examId, clazzId);
        return rptMockNewExamStudents;
    }

    @Override
    public NewExamReportForClazz crmReceiveNewExamReportForClazz(Teacher teacher, String newExamId, Long clazzId) {
        NewExamReportForClazz newExamReportForClazz = new NewExamReportForClazz();
        newExamReportForClazz.setExamId(newExamId);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            newExamReportForClazz.setDescription("班级不存在");
            newExamReportForClazz.setSuccess(false);
            return newExamReportForClazz;
        }
        School school = raikouSystem.loadSchool(clazz.getSchoolId());
        if (school == null) {
            newExamReportForClazz.setDescription("学校不存在");
            newExamReportForClazz.setSuccess(false);
            return newExamReportForClazz;
        }

        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            newExamReportForClazz.setDescription("测试不存在，请联系客服：400-160-1717。");
            newExamReportForClazz.setSuccess(false);
            return newExamReportForClazz;
        }

        NewPaper newPaper = tikuStrategy.loadPaperByDocid(newExam.obtainRandomPaperId());
        if (newPaper == null) {
            newExamReportForClazz.setDescription("newPaper is null");
            newExamReportForClazz.setSuccess(false);
            return newExamReportForClazz;
        } else {
            newExamReportForClazz.setPaperId(newPaper.getId());
            newExamReportForClazz.setTotalNum(SafeConverter.toInt(newPaper.getTotalNum()));
        }
        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
        if (!doNewExamProcess.haveNewExamPermission(exRegion, newExam, school, clazz, Collections.singletonList(teacher.getSubject()))) {
            newExamReportForClazz.setDescription("您无权限查看本次测试");
            newExamReportForClazz.setSuccess(false);
            return newExamReportForClazz;
        }
        Map<Long, User> userMap = newExamReportProcessor.loadClazzStudents(teacher, newExam.getSubject(), clazzId);

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamResultIds = userMap.keySet()
                .stream()
                .map(o -> new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), o.toString()).toString())
                .collect(Collectors.toList());
        Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamResultIds);

        if (!MapUtils.isNotEmpty(newExamResultMap)) {
            newExamReportForClazz.setDescription("没有人参与考试");
            newExamReportForClazz.setSuccess(false);
            return newExamReportForClazz;
        }
        int joinExamNum = 0;                       //参加的学生个数
        int finishedExamNum = 0;                   //完成的学生个数
        int totalStudentNum = userMap.size();      //班级的学生
        for (NewExamResult newExamResult : newExamResultMap.values()) {
            if (newExamResult.getUserId() != null && userMap.containsKey(newExamResult.getUserId())) {
                joinExamNum++;
                if (newExamResult.getFinishAt() != null) {
                    finishedExamNum++;
                }
                NewExamReportForClazz.CrmStudentNewExamReport crmStudentNewExamReport = new NewExamReportForClazz.CrmStudentNewExamReport();
                crmStudentNewExamReport.setNewExamResultId(newExamResult.getId());
                crmStudentNewExamReport.setUserId(newExamResult.getUserId());
                crmStudentNewExamReport.setPaperId(StringUtils.isBlank(newExamResult.getPaperId()) ? "" : newExamResult.getPaperId());
                crmStudentNewExamReport.setUserName(userMap.get(newExamResult.getUserId()).fetchRealnameIfBlankId());
                if (newExamResult.getCreateAt() != null) {
                    crmStudentNewExamReport.setCreateAt(DateUtils.dateToString(newExamResult.getCreateAt()));
                }
                if (newExamResult.getFinishAt() != null) {
                    crmStudentNewExamReport.setFinishAt(DateUtils.dateToString(newExamResult.getFinishAt()));
                }
                if (newExamResult.getSubmitAt() != null) {
                    crmStudentNewExamReport.setSubmitAt(DateUtils.dateToString(newExamResult.getSubmitAt()));
                }
                crmStudentNewExamReport.setScore(SafeConverter.toDouble(newExamResult.getScore()));
                crmStudentNewExamReport.setCorrectScore(SafeConverter.toDouble(newExamResult.getCorrectScore()));
                if (newExamResult.getCorrectAt() != null) {
                    crmStudentNewExamReport.setCorrectAt(DateUtils.dateToString(newExamResult.getCorrectAt()));
                }
                crmStudentNewExamReport.setDurationSeconds(new BigDecimal(SafeConverter.toLong(newExamResult.getDurationMilliseconds())).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).longValue());
                crmStudentNewExamReport.setClientType(SafeConverter.toString(newExamResult.getClientType()));
                crmStudentNewExamReport.setClientName(SafeConverter.toString(newExamResult.getClientName()));
                crmStudentNewExamReport.setDetail(JsonUtils.toJson(newExamResult));
                newExamReportForClazz.getCrmStudentNewExamReports().add(crmStudentNewExamReport);
            }
        }
        newExamReportForClazz.setFinishedExamNum(finishedExamNum);
        newExamReportForClazz.setTotalStudentNum(totalStudentNum);
        newExamReportForClazz.setJoinExamNum(joinExamNum);
        newExamReportForClazz.setSuccess(true);
        return newExamReportForClazz;
    }

    private void handleOralDetail(List<NewExamReportForStudent.Audio> audios, List<NewExamProcessResult.OralDetail> oralDetails) {
        for (NewExamProcessResult.OralDetail oralDetail : oralDetails) {
            NewExamReportForStudent.Audio audio = new NewExamReportForStudent.Audio();
            String voiceUrl = oralDetail.getAudio();
            if (StringUtils.isNotEmpty(voiceUrl)) {
                audio.setAudioUrl(voiceUrl);
                audio.setAudioInfo(oralDetail.getMacScore() + "/" + oralDetail.getFluency() + "/" + oralDetail.getIntegrity() + "/" + oralDetail.getPronunciation());
                audios.add(audio);
            }
        }
    }


    @Override
    public NewExamReportForStudent crmReceiveNewExamReportForStudent(String newExamResultId) {
        NewExamReportForStudent newExamReportForStudent = new NewExamReportForStudent();
        NewExamResult newExamResult = newExamResultDao.load(newExamResultId);
        if (newExamResult == null) {
            newExamReportForStudent.setDescription("newExamResult is null");
            newExamReportForStudent.setSuccess(false);
            return newExamReportForStudent;
        }
        NewExam newExam = newExamLoaderClient.load(newExamResult.getNewExamId());
        if (newExam == null) {
            newExamReportForStudent.setDescription("newExam is null");
            newExamReportForStudent.setSuccess(false);
            return newExamReportForStudent;
        }
        NewPaper newPaper = tikuStrategy.loadPaperByDocid(newExam.obtainRandomPaperId());
        if (newPaper == null) {
            newExamReportForStudent.setDescription("newPaper is null");
            newExamReportForStudent.setSuccess(false);
            return newExamReportForStudent;
        }
        LinkedHashMap<String, String> answers = newExamResult.getAnswers();
        if (MapUtils.isEmpty(answers)) {
            newExamReportForStudent.setDescription("newExam answers is empty");
            newExamReportForStudent.setSuccess(false);
            return newExamReportForStudent;
        }
        List<NewPaperQuestion> questions = newPaper.getQuestions();
        if (questions == null) {
            newExamReportForStudent.setDescription("newPaper's questions is empty");
            newExamReportForStudent.setSuccess(false);
            return newExamReportForStudent;
        } else {
            int totalQuestionNum = questions.size();//一共的题数量
            Set<String> questionIds = questions.stream()
                    .filter(Objects::nonNull)
                    .map(XxBaseQuestion::getId)
                    .collect(Collectors.toSet());
            int finishQuestionNum = 0;//完成题数
            int processNum = 0;
            List<String> newExamProcessIds = new LinkedList<>(answers.values());
            Map<String, NewExamProcessResult> newExamProcessResultMap = newExamProcessResultDao.loads(newExamProcessIds);
            for (NewExamProcessResult n : newExamProcessResultMap.values()) {
                NewExamReportForStudent.CrmNewExamProcessDetail crmNewExamProcessDetail = new NewExamReportForStudent.CrmNewExamProcessDetail();
                boolean inPaper = true;
                processNum++;
                if (questionIds.contains(n.getQuestionId())) {
                    finishQuestionNum++;
                    inPaper = false;
                }
                crmNewExamProcessDetail.setInPaper(inPaper);
                crmNewExamProcessDetail.setNewExamProcessId(n.getId());
                if (n.getCreateAt() != null) {
                    crmNewExamProcessDetail.setCreateAt(DateUtils.dateToString(n.getCreateAt()));
                }
                if (n.getUpdateAt() != null) {
                    crmNewExamProcessDetail.setUpdateAt(DateUtils.dateToString(n.getUpdateAt()));
                }
                crmNewExamProcessDetail.setQuestionDocId(n.getQuestionDocId());
                crmNewExamProcessDetail.setStandardScore(n.getStandardScore());
                crmNewExamProcessDetail.setScore(n.getScore());
                crmNewExamProcessDetail.setGrasp(SafeConverter.toBoolean(n.getGrasp()));
                crmNewExamProcessDetail.setSubGrasp(JsonUtils.toJson(n.getSubGrasp()));
                crmNewExamProcessDetail.setDurationSeconds(new BigDecimal(SafeConverter.toLong(n.getDurationMilliseconds())).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).longValue());
                crmNewExamProcessDetail.setClientType(n.getClientType());
                crmNewExamProcessDetail.setClientName(n.getClientName());
                crmNewExamProcessDetail.setCorrectScore(n.getCorrectScore());
                if (n.getCorrectAt() != null) {
                    crmNewExamProcessDetail.setCorrectAt(DateUtils.dateToString(n.getCorrectAt()));
                }
                crmNewExamProcessDetail.setUserAnswers(JsonUtils.toJson(n.getUserAnswers()));
                List<NewExamProcessResult.OralDetail> oralDetails = new LinkedList<>();
                if (n.getOralDetails() != null) {
                    oralDetails = n.getOralDetails()
                            .stream()
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
                handleOralDetail(crmNewExamProcessDetail.getAudios(), oralDetails);


                crmNewExamProcessDetail.setOralDetails(n.getOralDetails());
                crmNewExamProcessDetail.setDetail(JsonUtils.toJson(n));
                newExamReportForStudent.getCrmNewExamProcessDetails().add(crmNewExamProcessDetail);
            }
            newExamReportForStudent.setTotalQuestionNum(totalQuestionNum);
            newExamReportForStudent.setFinishQuestionNum(finishQuestionNum);
            newExamReportForStudent.setUserId(newExamResult.getUserId());
            newExamReportForStudent.setProcessNum(processNum);
        }
        newExamReportForStudent.setSuccess(true);
        return newExamReportForStudent;
    }

    /**
     * 学生查看考试详情打点(pc&app)
     *
     * @param examId
     * @param userId
     * @param actionRefer
     */
    @Override
    public void studentViewExamReportKafka(String examId, Long userId, String actionRefer) {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("examId", examId);
        messageMap.put("userId", userId);
        messageMap.put("actionRefer", actionRefer);
        messageMap.put("actionTimeStamp", System.currentTimeMillis());
        Message message = Message.newMessage();
        message.withPlainTextBody(JsonUtils.toJson(messageMap));
        newExamQueueProducer.getStudentViewReport().produce(message);
    }

    //学生表格信息接口
    @Override
    public MapMessage shareReport(String newExamId, Long clazzId) {
        try {
            if (StringUtils.isBlank(newExamId) || clazzId <= 0) {
                logger.error("share NewExam Report failed : newExamId {},clazzId {}", newExamId, clazzId);
                return MapMessage.errorMessage("参数有误");
            }
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam == null) {
                logger.error("share NewExam Report failed : newExamId {},clazzId {}", newExamId, clazzId);
                return MapMessage.errorMessage("考试不存在");
            }
            if (!SchoolLevel.JUNIOR.equals(newExam.getSchoolLevel())) {
                return MapMessage.errorMessage("此报告数据只支持小学业务");
            }
            Date currentDate = new Date();
            if (newExam.getResultIssueAt().after(currentDate)) {
                logger.error("share NewExam Report failed : newExamId {},clazzId {}", newExamId, clazzId);
                return MapMessage.errorMessage("考试成绩未发布");
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz == null) {
                return MapMessage.errorMessage("班级不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            }
            List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
            List<String> paperIds = papers.stream().map(NewExam.EmbedPaper::getPaperId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(paperIds)) {
                logger.error("share NewExam Report failed : newExamId {},clazzId {}", newExamId, clazzId);
                return MapMessage.errorMessage("试卷ID不存在");
            }
            Map<String, NewPaper> newPaperMap = loadNewPapersByDocIdsIncludeDisable(paperIds);
            if (newPaperMap.isEmpty()) {
                logger.error("share NewExam Report failed : newExamId {},clazzId {}", newExamId, clazzId);
                return MapMessage.errorMessage("试卷不存在或者已经下架");
            }
            List<String> newExamResultIds = newExamResultDao.findByNewExamAndClazzId(newExam, clazzId);
            Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(newExamResultIds);
            Map<String, Integer> levelCountMap = new LinkedHashMap<>();
            for (NewExamResult result : newExamResultMap.values()) {
                if (result.getSubmitAt() != null) {
                    NewPaper newPaper = newPaperMap.get(result.getPaperId());
                    if (newPaper == null) {
                        logger.error("share NewExam Report failed : newExamId {},clazzId {}", newExamId, clazzId);
                        return MapMessage.errorMessage("试卷不存在或者已经下架");
                    }
                    double rate = 0;
                    if (newPaper.getTotalScore() != null && newPaper.getTotalScore() > 0) {
                        //显示等级或者分数
                        double score = result.processScore(SafeConverter.toInt(newPaper.getTotalScore()));
                        rate = new BigDecimal(score * 100).divide(new BigDecimal(newPaper.getTotalScore()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                    NewExam.EmbedRank embedRank = newExam.processScoreLevel(rate);

                    Integer levelCount = levelCountMap.get(embedRank.getRankName());
                    if (levelCount == null) {
                        levelCountMap.put(embedRank.getRankName(), 1);
                    } else {
                        levelCountMap.put(embedRank.getRankName(), levelCount + 1);
                    }
                }
            }

            List<String> colors = Arrays.asList("#FFD150", "#40B3FF", "#52D8C5", "#FF6802", "#7E6CD8", "#98D170", "#6DBBEA", "#FFAB17", "#EF95B0", "#E0577A");
            List<Map<String, Object>> echartData = new ArrayList<>();
            int i = 0;
            for (NewExam.EmbedRank embedRank : newExam.getRanks()) {
                if (levelCountMap.containsKey(embedRank.getRankName())) {
                    echartData.add(MapUtils.m("color", colors.get(i), "num", levelCountMap.get(embedRank.getRankName()), "rank", embedRank.getRankName()));
                    i++;
                }
            }

            Date bigDateReportDate = newExam.getResultIssueAt();
            if (!NewExamType.independent.equals(newExam.getExamType())) {
                bigDateReportDate = DateUtils.addHours(newExam.getResultIssueAt(), 24);
            }
            return MapMessage.successMessage()
                    .add("echartData", echartData)
                    .add("echartDesc", "等级分布")
                    .add("echartTitle", clazz.formalizeClazzName())
                    .add("echartType", "pie")
                    .add("joinCount", newExamResultMap.size())
                    .add("reportComment", bigDateReportDate.after(currentDate) ? "详细报告正在生成中，请" + DateUtils.dateToString(bigDateReportDate, DateUtils.FORMAT_SQL_DATETIME) + "后再来分享吧~" : "")
                    .add("subject", newExam.getSubject().name());
        } catch (Exception e) {
            logger.error("share NewExam Report failed : newExamId {},clazzId {}", newExamId, clazzId, e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage loadUnitTestDetail(List<String> newExamIds) {
        Map<String, NewExam> newExamMap = newExamLoaderClient.loads(newExamIds);
        if (MapUtils.isEmpty(newExamMap)) {
            return MapMessage.errorMessage("考试id错误");
        }
        List<Long> groupIds = newExamMap.values().stream().map(NewExam::getGroupId).collect(Collectors.toList());
        Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(groupIds, true);
        Set<Long> studentIds = groupMapperMap.values().stream()
                .map(GroupMapper::getStudents)
                .flatMap(Collection::stream)
                .map(GroupMapper.GroupUser::getId)
                .collect(Collectors.toSet());
        Map<Long, Set<Long>> studentBindAppParentMap = vendorServiceClient.studentBindAppParentMap(new ArrayList<>(studentIds));
        Set<Long> clazzIds = groupMapperMap.values().stream()
                .map(GroupMapper::getClazzId)
                .collect(Collectors.toSet());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        List<Map<String, Object>> clazzList = new ArrayList<>();
        for (GroupMapper groupMapper : groupMapperMap.values()) {
            Clazz clazz = clazzMap.get(groupMapper.getClazzId());
            if (clazz == null) {
                continue;
            }
            List<GroupMapper.GroupUser> students = groupMapper.getStudents();
            long parentCount = students.stream()
                    .map(GroupMapper.GroupUser::getId)
                    .filter(studentId -> CollectionUtils.isNotEmpty(studentBindAppParentMap.get(studentId)))
                    .count();
            clazzList.add(MapUtils.m("clazzGroupId", groupMapper.getClazzId() + "_" + groupMapper.getId(), "clazzName", clazz.formalizeClazzName(), "parentCount", parentCount));
        }

        Set<String> paperIds = newExamMap.values()
                .stream()
                .map(NewExam::getPapers)
                .flatMap(Collection::stream)
                .map(NewExam.EmbedPaper::getPaperId)
                .collect(Collectors.toSet());
        Map<String, NewPaper> newPaperMap = paperLoaderClient.loadNewPapersByDocIds0(paperIds);

        List<Map> unitPaperTypes = new ArrayList<>();
        List<Map> classPaperTypes = new ArrayList<>();
        for (NewPaper newPaper : newPaperMap.values()) {
            Map<String, Object> contents;
            if (newPaper.getPaperTypes().contains(25)) {
                contents = MapUtils.m("title", newPaper.getTitle(),
                        "minutes", newPaper.getExamTime() / 3,
                        "limitMinutes", newPaper.getExamTime(),
                        "questionNum", newPaper.getQuestions().size());
                unitPaperTypes.add(contents);
            }
            if (newPaper.getPaperTypes().contains(26)) {
                contents = MapUtils.m("title", newPaper.getTitle(),
                        "minutes", newPaper.getExamTime() / 3,
                        "limitMinutes", newPaper.getExamTime(),
                        "questionNum", newPaper.getQuestions().size());
                classPaperTypes.add(contents);
            }
        }
        List<Map<String, Object>> practices = new ArrayList<>();
        Map<String, Object> paperTypeMap;
        if (CollectionUtils.isNotEmpty(unitPaperTypes)) {
            paperTypeMap = MapUtils.m("paperType", 25,
                    "typeName", "单元检测",
                    "contents", unitPaperTypes);
            practices.add(paperTypeMap);
        }
        if (CollectionUtils.isNotEmpty(classPaperTypes)) {
            paperTypeMap = MapUtils.m("paperType", 26,
                    "typeName", "课时小测",
                    "contents", classPaperTypes);
            practices.add(paperTypeMap);
        }

        NewExam newExam = newExamMap.values().iterator().next();
        String examDate = DateUtils.dateToString(newExam.getExamStartAt(), "MM.dd");
        String shareExamDate = DateUtils.dateToString(newExam.getCreatedAt(), "MM月dd日");
        String endDate = DateUtils.dateToString(newExam.getExamStopAt(), "yyyy-MM-dd HH:mm");
        Subject subject = newExam.getSubject();

        return MapMessage.successMessage()
                .add("shareExamDate", shareExamDate)
                .add("examDate", examDate)
                .add("endDate", endDate)
                .add("clazzList", clazzList)
                .add("practices", practices)
                .add("newExamIds", newExamIds)
                .add("subject", subject)
                .add("subjectName", subject.getValue());
    }

    @Override
    public MapMessage loadUnitTestAdjustDetail(String examId, Long teacherId) {
        NewExam newExam = newExamLoaderClient.load(examId);
        if (newExam == null) {
            return MapMessage.errorMessage("考试不存在");
        }
        // 权限检查
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newExam.getGroupId())) {
            return MapMessage.errorMessage("没有权限调整此考试");
        }
        NewExam.EmbedPaper embedPaper = newExam.getPapers().iterator().next();
        NewPaper newPaper = paperLoaderClient.loadPaperByDocid(embedPaper.getPaperId());
        Map<String, Object> contents = new HashMap<>();
        if (newPaper.getPaperTypes().contains(25)) {
            contents = MapUtils.m("paperType", 25,
                    "typeName", "单元检测",
                    "title", newPaper.getTitle(),
                    "minutes", newPaper.getExamTime() / 3,
                    "limitMinutes", newPaper.getExamTime(),
                    "questionNum", newPaper.getQuestions().size());
        }
        if (newPaper.getPaperTypes().contains(26)) {
            contents = MapUtils.m("paperType", 26,
                    "typeName", "课时小测",
                    "title", newPaper.getTitle(),
                    "minutes", newPaper.getExamTime() / 3,
                    "limitMinutes", newPaper.getExamTime(),
                    "questionNum", newPaper.getQuestions().size());
        }

        Long groupId = newExam.getGroupId();
        GroupMapper groupMapper = groupLoaderClient.loadGroups(Collections.singleton(groupId), true).get(groupId);
        Long clazzId = groupMapper.getClazzId();
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId);

        Date endDate = new Date(System.currentTimeMillis() + 300000);
        return MapMessage.successMessage()
                .add("practices", contents)
                .add("clazz", clazz.formalizeClazzName())
                .add("startDateTime", DateUtils.dateToString(newExam.getExamStartAt(), DateUtils.FORMAT_SQL_DATE))
                .add("endDateTime", DateUtils.dateToString(newExam.getExamStopAt()))
                .add("currentDate", DateUtils.dateToString(endDate, DateUtils.FORMAT_SQL_DATE))
                .add("nowEndTime", DateUtils.dateToString(endDate, "HH:mm"));
    }

    // TODO: 2019/4/4 代码需要优化
    @Override
    public MapMessage fetchUnitTestTeacherClazzInfo(Teacher teacher) {
        try {
            Set<Long> tids = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(tids);
            Map<Long, List<Clazz>> clazzMap = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(tids);
            Map<Subject, List<Map<String, Object>>> resultMap = new LinkedHashMap<>();

            List<Clazz> clazzs = clazzMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .sorted(new Clazz.ClazzLevelAndNameComparator()).collect(Collectors.toList());
            Map<Long, List<GroupMapper>> groupMaps = groupLoaderClient.loadClazzGroups(clazzs.stream().map(Clazz::getId).collect(Collectors.toList()));
            List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByTeacherIds(tids)
                    .stream()
                    .map(GroupTeacherTuple::getGroupId)
                    .collect(Collectors.toList());
            Map<Long, Long> clazzIdGroupIdMap = new LinkedHashMap<>();
            for (Clazz clazz : clazzs) {
                List<GroupMapper> groupMapperList = groupMaps.get(clazz.getId());
                if (CollectionUtils.isNotEmpty(groupMapperList)) {
                    groupMapperList.forEach(groupMapper -> {
                        if (teacherGroupIds.contains(groupMapper.getId())) {
                            clazzIdGroupIdMap.put(clazz.getId(), groupMapper.getId());
                        }
                    });
                }
            }

            for (Map.Entry<Long, List<Clazz>> entry : clazzMap.entrySet()) {
                List<Map<String, Object>> clazzList = entry.getValue()
                        .stream()
                        .filter(c -> !c.isTerminalClazz())
                        .sorted(new Clazz.ClazzLevelAndNameComparator())
                        .map(c -> MapUtils.m(
                                "clazzId", c.getId(),
                                "groupId", clazzIdGroupIdMap.get(c.getId()),
                                "clazzName", c.formalizeClazzName()
                        ))
                        .collect(Collectors.toList());
                resultMap.put(teacherMap.get(entry.getKey()).getSubject(), clazzList);
            }

            //按照学科排序
            Subject[] subjects1 = new Subject[]{Subject.MATH};
            List<Map<String, Object>> subjects = new LinkedList<>();
            for (Subject s : subjects1) {
                if (resultMap.containsKey(s)) {
                    subjects.add(
                            MapUtils.m("subject", s,
                                    "subjectName", s.getValue(),
                                    "clazzList", resultMap.get(s)));
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.put("clazz_list", subjects);
            return mapMessage;
        } catch (Exception e) {
            logger.error("new exam report app fetch Teacher ClazzInfo failed : tid {}", teacher.getId());
            return MapMessage.errorMessage();
        }
    }
}
