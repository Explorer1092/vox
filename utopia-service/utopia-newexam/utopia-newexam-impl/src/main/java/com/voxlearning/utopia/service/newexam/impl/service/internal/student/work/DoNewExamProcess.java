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

package com.voxlearning.utopia.service.newexam.impl.service.internal.student.work;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.OralScoreInterval;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.newexam.api.entity.*;
import com.voxlearning.utopia.service.newexam.impl.dao.StudentExaminationAuthorityDao;
import com.voxlearning.utopia.service.newexam.impl.loader.NewExamProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newexam.impl.loader.NewExamRegistrationLoaderImpl;
import com.voxlearning.utopia.service.newexam.impl.loader.NewExamReportLoaderImpl;
import com.voxlearning.utopia.service.newexam.impl.loader.NewExamResultLoaderImpl;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamPaperHelper;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.question.api.constant.NewExamChoicePaperType;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;


@Named
public class DoNewExamProcess extends NewExamSpringBean {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private NewExamResultLoaderImpl newExamResultLoader;
    @Inject
    private NewExamProcessResultLoaderImpl newExamProcessResultLoader;
    @Inject
    private NewExamRegistrationLoaderImpl newExamRegistrationLoader;
    @Inject
    private StudentExaminationAuthorityDao studentExaminationAuthorityDao;
    @Inject
    private NewExamReportLoaderImpl newExamReportLoader;


    // 学生开始作业首页
    public MapMessage index(String newExamId, Long studentId) {

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);

        if (studentDetail == null) {
            return MapMessage.errorMessage("学生不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_STUDENT_NOT_EXIST);
        }

        if (StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("考试id为空").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }

        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("考试不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }
        if (newExam.getDeletedAt() != null) {
            return MapMessage.errorMessage("考试已删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }
        Date currentDate = new Date();

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamRegistration.ID id = new NewExamRegistration.ID(month, Subject.fromSubjectId(newExam.getSubjectId()), newExam.getId(), studentId.toString());


        boolean examHasEnd = newExam.getExamStopAt().before(currentDate);
        StudentExaminationAuthority studentExaminationAuthority = null;
        if (examHasEnd) {
            //考试时间结束的情况下
            //学生是否有补考重考的权限
            //补考重考权限，考试批改未结束，而且有权限
            studentExaminationAuthority = studentExaminationAuthorityDao.load(id.toString());
            if (newExam.getCorrectStopAt().after(currentDate)
                    && studentExaminationAuthority != null
                    && !SafeConverter.toBoolean(studentExaminationAuthority.getDisabled())) {
                examHasEnd = false;
            }
        }
        if (examHasEnd) {
            return MapMessage.errorMessage("测试时间已结束，无法答题。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_IS_STOP);
        }

        NewExamRegistration newExamRegistration = newExamRegistrationLoader.loadById(id.toString());
        if (newExamRegistration != null) {
            if (newExamRegistration.getSubmitAt() != null) {
                return MapMessage.errorMessage("您已交卷，无法继续答题。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_IS_SUBMIT);
            }
        }

        Long schoolId = studentDetail.getClazz() == null ? null : studentDetail.getClazz().getSchoolId();
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("测试学校不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_SCHOOL_NOT_EXIST);
        }

        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(studentId, false);
        if (groupMappers == null) {
            return MapMessage.errorMessage("您没有加入任何班组。").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
        }
        List<Subject> subjects = groupMappers.stream().map(GroupMapper::getSubject).collect(Collectors.toList());

        ExRegion exRegion = new ExRegion();
        exRegion.setProvinceCode(studentDetail.getRootRegionCode());
        exRegion.setCityCode(studentDetail.getCityCode());
        exRegion.setCountyCode(studentDetail.getStudentSchoolRegionCode());
        if (!haveNewExamPermission(exRegion, newExam, school, studentDetail.getClazz(), subjects)) {
            return MapMessage.errorMessage("您不需要参加本次测试。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_PERMISSION);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("newExamId", newExamId);
        result.put("submitAfterMinutes", SafeConverter.toInt(newExam.getSubmitAfterMinutes()));
        result.put("oralRepeatCount", SafeConverter.toInt(newExam.getOralRepeatCount(), -1));
        result.put("name", newExam.getName());
        Subject subject = Subject.fromSubjectId(newExam.getSubjectId());
        result.put("subject", subject);
        result.put("subjectName", subject.getValue());
        result.put("detail", newExam.getDetail());
        result.put("type", newExam.getExamType());
        result.put("startAt", newExam.getExamStartAt());
        result.put("stopAt", studentExaminationAuthority == null ? newExam.getExamStopAt() : newExam.getCorrectStopAt());
        result.put("examTimes", newExam.getDurationMinutes());
        result.put("publishTime", newExam.getResultIssueAt());
        Long durationTime = newExamRegistration != null && newExamRegistration.getDurationMilliseconds() != null ? newExamRegistration.getDurationMilliseconds() : 0;
        result.put("durationTime", durationTime);
        long remainingTime = newExam.getDurationMinutes() * 60 * 1000 - durationTime;
        //当剩余时间是负数的时候，返回前端一秒时间，用于提交
        if (remainingTime < 1000) {
            remainingTime = 1000;
        }
        result.put("remainingTime", remainingTime);
        result.put("doNewExamUrl", "/flash/loader/newexam/do" + Constants.AntiHijackExt);
        result.put("currentDate", new Date());
        result.put("imageUrl", studentDetail.fetchImageUrl());
        result.put("schoolLevel", newExam.getSchoolLevel());
        boolean continueExam = false;
        if (newExamRegistration != null && newExamRegistration.getStartAt() != null) {
            continueExam = true;
        }
        result.put("continueExam", continueExam);
        return MapMessage.successMessage().add("result", result);
    }


    //获取试卷ID
    private String fetchPaperId(NewExam newExam, StudentDetail studentDetail) {
        String paperId;

        List<String> paperIds = newExam.obtainPaperIds();
        //没有试卷ID
        if (CollectionUtils.isEmpty(paperIds)) {
            return null;
        }
        //单份试卷
        if (paperIds.size() == 1) {
            return paperIds.get(0);
        }
        //多试卷，没有注册，
        //而且是轮流模式
        if (Objects.equals(newExam.getDistribution(), NewExamChoicePaperType.ClassOrder.getDistribution())) {
            Long clazzId = studentDetail.getClazzId();
            //班级Id 缓存
            String key = NewExamPaperHelper.getClassOrderPaperIdKey(clazzId, newExam.getId());
            Long num = newExamCacheClient.cacheSystem.CBS.unflushable.incr(key, 1, 1, UtopiaCacheExpiration.MAX_TTL_IN_SECONDS);
            int index = (int) (num % paperIds.size());
            paperId = paperIds.get(index);
        } else {
            //按照学生ID获取试卷
            if (Objects.equals(newExam.getDistribution(), NewExamChoicePaperType.BasisStudentId.getDistribution())) {
                int index = (int) (studentDetail.getId() % paperIds.size());
                return paperIds.get(index);
            } else {
                //随机获取
                Random random = new Random();
                int nextInt = random.nextInt(paperIds.size());
                return paperIds.get(nextInt);
            }
        }

        return paperId;
    }


    /**
     * 进入考试
     */
    public MapMessage enterExam(String newExamId, StudentDetail studentDetail, String cdnUrl, String clientType, String clientName) {

        if (studentDetail.getClazz() == null) {
            return MapMessage.errorMessage("您未加入班级。").setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("测试不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
        }

//        NewPaper newPaper = paperLoaderClient.loadPaperByDocid(newExam.getPaperId());
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), studentDetail.getId().toString());

        NewExamRegistration newExamRegistration = newExamRegistrationDao.load(id.toString());
        String paperId;
        if (newExamRegistration == null) {
            paperId = fetchPaperId(newExam, studentDetail);
        } else {
            paperId = newExamRegistration.getPaperId();
        }
        if (paperId == null) {
            return MapMessage.errorMessage("试卷ID不存在。").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
        }

        SchoolLevel schoolLevel = newExam.getSchoolLevel();
        NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId, schoolLevel);
        if (newPaper == null) {
            return MapMessage.errorMessage("试卷不存在，请联系客服：400-160-1717。").setErrorCode(ErrorCodeConstants.ERROR_CODE_PAPER_NOT_EXIST);
        }

        Date currentDate = new Date();
        if (newExam.getExamStartAt().after(currentDate)) {
            return MapMessage.errorMessage("测试未开始，请耐心等待。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_START);
        }

        //examHasEnd 考试是否结束
        boolean examHasEnd = newExam.getExamStopAt().before(currentDate);
        if (examHasEnd) {
            //考试时间结束的情况下
            //学生是否有补考重考的权限
            //补考重考权限，考试批改未结束，而且有权限
            StudentExaminationAuthority studentExaminationAuthority = studentExaminationAuthorityDao.load(id.toString());
            if (newExam.getCorrectStopAt().after(currentDate)
                    && studentExaminationAuthority != null
                    && !SafeConverter.toBoolean(studentExaminationAuthority.getDisabled())) {
                examHasEnd = false;
            }
        }
        if (examHasEnd) {
            return MapMessage.errorMessage("测试时间已结束，无法答题。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_IS_STOP);
        }
        List<String> qIds = newPaper.getQuestions().stream().map(NewPaperQuestion::getId).collect(Collectors.toList());
        List<NewQuestion> newQuestions = tikuStrategy.loadQuestionsIncludeDisabledAsList(qIds, schoolLevel);
        List<Integer> contentTypeIds = new ArrayList<>();
        for (NewQuestion newQuestion : newQuestions) {
            contentTypeIds.addAll(newQuestion.getContent().getSubContents().stream().map(NewQuestionsSubContents::getSubContentTypeId).collect(Collectors.toList()));
        }
        boolean isOral = questionContentTypeLoaderClient.isOral(contentTypeIds);

        Map<String, Object> vars = new HashMap<>();
        vars.put("uid", studentDetail.getId());
        vars.put("userName", studentDetail.fetchRealname());
        vars.put("cid", studentDetail.getClazzId());
        vars.put("newExamId", newExamId);
        vars.put("paperId", paperId);
        vars.put("name", newExam.getName());
        vars.put("subject", newExam.processSubject());
        vars.put("isOral", isOral);
        vars.put("learningType", StudyType.examination);
        vars.put("imgDomain", cdnUrl);
        vars.put("completedUrl", "/flash/loader/newexam/questions/answer" + Constants.AntiHijackExt);
        vars.put("paperUrl", "/exam/flash/load/newexam/paper/parts/byid" + Constants.AntiHijackExt);
        vars.put("questionUrl", "/exam/flash/load/newquestion/byids" + Constants.AntiHijackExt);
        vars.put("processResultUrl", "/exam/flash/newexam/processresult" + Constants.AntiHijackExt);
        vars.put("submitUrl", "/exam/flash/newexam/submit" + Constants.AntiHijackExt);
        vars.put("currentDate", new Date());
        vars.put("contentTypes", newExam.getContentTypes());
        vars.put("testCategory", newExam.getTestCategory());
        vars.put("oralRepeatCount", newExam.getOralRepeatCount());
        vars.put("oralScoreIntervals", OralScoreInterval.oralScoreIntervals);

        NewExamResult newExamResult = newExamResultDao.load(id.toString());
        Long clazzGroupId = null;
        Long clazzId = studentDetail.getClazzId();
        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        for (GroupMapper gm : groupMappers) {
            if (Objects.equals(gm.getGroupType(), GroupType.TEACHER_GROUP) && Objects.equals(gm.getSubject(), newExam.processSubject())) {
                clazzGroupId = gm.getId();
                clazzId = gm.getClazzId();
                break;
            }
        }

        // #37121 如果是中学而且没有匹配到对应的老师分组，再匹配一次教学班组
        if (clazzGroupId == null && studentDetail.isJuniorStudent()) {
            for (GroupMapper gm : groupMappers) {
                if (Objects.equals(gm.getGroupType(), GroupType.WALKING_GROUP) && Objects.equals(gm.getSubject(), newExam.processSubject())) {
                    clazzGroupId = gm.getId();
                    clazzId = gm.getClazzId();
                    break;
                }
            }
        }

        if (newExamResult == null) {
            //报名考试如果是第一次开始考试则需要设置开始考试时间
            if (NewExamType.apply == newExam.getExamType()) {
                String groupExamRegistrationId = GroupExamRegistration.generateId(newExam.getCreatedAt(), newExam.getId(), clazzGroupId);
                GroupExamRegistration registration = groupExamRegistrationDao.load(groupExamRegistrationId);
                if (registration == null) {
                    return MapMessage.errorMessage("您未报名此考试").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_REGISTRATION_NOT_EXIST);
                }
            }
            //如果是统考第一次开始考试则初始化报名数据
            if (newExamRegistration == null) {
                newExamRegistration = new NewExamRegistration();
                newExamRegistration.setId(id.toString());
                newExamRegistration.setNewExamId(newExamId);
                newExamRegistration.setSubject(newExam.getSubject());
                newExamRegistration.setExamType(newExam.getExamType());
                newExamRegistration.setClazzId(clazzId);
                newExamRegistration.setClazzGroupId(clazzGroupId);//如果学生所在班没有对应的学科组则不存
                newExamRegistration.setUserId(studentDetail.getId());
                newExamRegistration.setUserName(studentDetail.getProfile().getRealname());
                newExamRegistration.setSchoolId(studentDetail.getClazz().getSchoolId());
                ExRegion exRegion = raikouSystem.loadRegion(studentDetail.getStudentSchoolRegionCode());
                if (exRegion != null) {
                    newExamRegistration.setProvinceId(exRegion.getProvinceCode());
                    newExamRegistration.setCityId(exRegion.getCityCode());
                    newExamRegistration.setRegionId(exRegion.getCountyCode());
                }
                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(studentDetail.getClazz().getSchoolId())
                        .getUninterruptibly();
                if (school != null) {
                    newExamRegistration.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
                }
                newExamRegistration.setClazzLevel(studentDetail.getClazzLevel().getLevel());
                newExamRegistration.setStartAt(currentDate);
                newExamRegistration.setUpdateAt(currentDate);
                newExamRegistration.setClientType(clientType);
                newExamRegistration.setClientName(clientName);
                newExamRegistration.setPaperId(paperId);
                newExamRegistrationDao.insert(newExamRegistration);
            }
            newExamResultDao.initNewExamResult(paperId, id.toString(), newExamId, newExam.getSubject(), studentDetail.getId(), clazzId, clazzGroupId, clientType, clientName);
        } else {
            newExamResultDao.flightRecorderTime(id.toString());
        }

        if (newExamRegistration != null && newExamRegistration.getSubmitAt() != null) {
            return MapMessage.errorMessage("您已交卷，无法继续答题。").setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_IS_SUBMIT);
        }

        return MapMessage.successMessage().add("result", vars);

    }

    //取题答案
    public MapMessage questionAnswer(String newExamId, Long studentId, Boolean includeStandardAnswer) {
        Map<String, Object> questionAnswerMap = new HashMap<>();
        if (StringUtils.isNoneBlank(newExamId)) {
            NewExam newExam = newExamLoaderClient.load(newExamId);
            NewExamResult newExamResult = newExamResultLoader.loadNewExamResult(newExamId, studentId);
//            List<NewPaper> newPapers = paperLoaderClient.loadPaperAsListByDocid(newExam.getPaperId());
//            NewPaper newPaper = null;
//            if (CollectionUtils.isNotEmpty(newPapers)) {
//                newPaper = newPapers.stream().filter(p -> !p.isDeletedTrue()).collect(Collectors.toList()).get(0);
//            }

            SchoolLevel schoolLevel = newExam.getSchoolLevel();
            NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(NewExamPaperHelper.fetchPaperId(newExamResult, newExam, studentId), schoolLevel);
            if (newPaper != null) {
                Map<String, Double> newPaperQuestionScoreMap = newPaper.getQuestionScoreMapByQid();
                Collection<String> qids = newPaperQuestionScoreMap.keySet();
                Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids, schoolLevel);
                Collection<String> resultIds = newExamResult != null && newExamResult.getAnswers() != null ? newExamResult.getAnswers().values() : Collections.emptyList();
                Map<String, NewExamProcessResult> newExamProcessResults = newExamProcessResultLoader.loadByIds(resultIds);
                Map<String, String> answersMap = new HashMap<>();
                if (newExamResult != null && newExamResult.getAnswers() != null) {
                    answersMap = newExamResult.getAnswers();
                }
                for (NewQuestion newQuestion : newQuestionMap.values()) {
                    String qid = newQuestion.getId();
                    String processResultId = answersMap.get(newQuestion.getDocId());
                    NewExamProcessResult processResult = newExamProcessResults.get(processResultId);
                    Map<String, Object> questionAnswer = new HashMap<>();
                    questionAnswer.put("fullScore", newPaperQuestionScoreMap.get(qid));
                    if (includeStandardAnswer) {
                        questionAnswer.put("standardAnswer", newQuestion.getAnswers());
                    }
                    if (processResult != null) {
                        questionAnswer.put("files", processResult.getFiles());
                        questionAnswer.put("oralDetails", processResult.getOralDetails());
                        questionAnswer.put("subMaster", processResult.getSubGrasp());

                        //小题掌握情况
                        List<Boolean> subQuestionMaster = new LinkedList<>();
                        List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                        List<List<Boolean>> subGrasp = processResult.getSubGrasp();
                        for (int subIndex = 0; subIndex < subGrasp.size(); subIndex++) {
                            boolean subQuestionGrasp = false;
                            if (subContents.size() > subIndex) {
                                List<Boolean> subQuestionMasters = subGrasp.get(subIndex);
                                subQuestionGrasp = newExamReportLoader.isSubQuestionGrasp(subContents.get(subIndex), subQuestionMasters);
                            }
                            subQuestionMaster.add(subQuestionGrasp);
                        }
                        questionAnswer.put("subQuestionMaster", subQuestionMaster);
                        questionAnswer.put("master", processResult.getGrasp());
                        List<List<String>> userAnswers = processResult.getUserAnswers();
                        // 脱式计算题，重新赋值userAnswer
                        for (int i = 0; i < subContents.size(); i++) {
                            NewQuestionsSubContents subContent = subContents.get(i);
                            Integer subContentTypeId = subContent.getSubContentTypeId();
                            if (Objects.equals(QuestionConstants.TuoShiJiSuanTi, subContentTypeId)) {
                                List<String> answers = userAnswers.get(i);
                                // 去除后面连续的空行
                                if (CollectionUtils.isNotEmpty(answers) && answers.size() > 1) {
                                    int lastIndex = answers.size();
                                    for (int index = answers.size(); index >= 2; index--) {
                                        if (StringUtils.isNotBlank(answers.get(index - 1))) {
                                            break;
                                        } else {
                                            lastIndex = index - 1;
                                        }
                                    }
                                    answers = new ArrayList<>(answers.subList(0, lastIndex));
                                }
                                userAnswers.set(i, answers);
                            }
                        }
                        questionAnswer.put("userAnswers", userAnswers);
                        questionAnswer.put("score", processResult.processScore());
                        questionAnswer.put("subScore", processResult.processSubScore());
                    }
                    if (includeStandardAnswer) {
                        questionAnswerMap.put(qid, questionAnswer);
                    } else {
                        if (processResult != null) {
                            questionAnswerMap.put(qid, questionAnswer);
                        }
                    }
                }
            }
        }

        return MapMessage.successMessage().add("result", questionAnswerMap);
    }


    public boolean haveNewExamPermission(ExRegion exRegion, NewExam newExam, School school, Clazz clazz, List<Subject> subjects) {
        if (newExam.getExamType() == NewExamType.independent) {
            return true;
        }

        if (!newExam.getClazzLevels().contains(clazz.getClazzLevel().getLevel())) {
            return false;
        }

        if (!newExam.matchStudentSubject(subjects)) {
            return false;
        }

        if (exRegion == null) {
            return false;
        }
        List<Integer> provinceCodes = new ArrayList<>();
        List<Integer> cityCodes = new ArrayList<>();
        List<Integer> regionCodes = new ArrayList<>();
        for (XxBaseRegion xxBaseRegion : newExam.getRegions()) {
            provinceCodes.add(xxBaseRegion.getProvinceId());
            cityCodes.add(xxBaseRegion.getCityId());
            regionCodes.add(xxBaseRegion.getRegionId());
        }
        switch (newExam.getRegionLevel()) {
            case province:
                if (!provinceCodes.contains(exRegion.getProvinceCode())) {
                    return false;
                }
                break;
            case city:
                if (!cityCodes.contains(exRegion.getCityCode())) {
                    return false;
                }
                break;
            case country:
                if (!regionCodes.contains(exRegion.getCountyCode())) {
                    return false;
                }
                break;
            case school:
                if (!newExam.getSchoolIds().contains(school.getId())) {
                    return false;
                }
                break;
            default:
                break;
        }
        return true;
    }
}
