/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.student.wintermission;

import com.voxlearning.washington.controller.babel.AbstractBabelController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 已下线，跳转到学生首页
 *
 * @author Sadi.Wan
 * @since 2014/12/15.
 */
@Deprecated
@Controller
@RequestMapping(value = "student/wintermission")
public class StudentWinterMissionController extends AbstractBabelController {

    //winterMissionIndex
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String winterMissionIndex(Model model) {
        return "redirect:/student/index.vpage";

//        UserMissionDayCount userMissionDayCount = winterMissionLoaderClient.loadUserMissionDayCount(currentUserId());
//        if (null == userMissionDayCount) {
//            logger.error("UserMissionDayCount_LOAD_FAILED,USER:{}", currentUserId());
//            return "redirect:/";
//        }
//        switch (WinterMissionUtil.getMyMissionOpenStatus(currentStudentDetail())) {
//            case NOT_OPEN_YET:
//            case ALREADY_CLOSED:
//                logger.error("UserMissionDayCount_TRIED_TO_ACCESS_WINTER_MISSION_WHEN_ITS_CLOSED,USER:{}", currentUserId());
//                return "redirect:/";
//            case OPEN_ON_NO_VH:
//                if (!winterMissionLoaderClient.checkNoHw(currentStudentDetail().getClazzId())) {
//                    return "redirect:/";
//                }
//                break;
//        }
//        model.addAttribute("finishedDay", SafeConverter.toInt(userMissionDayCount.getDmc()));
//        Calendar calculate = Calendar.getInstance();
//        calculate.set(Calendar.HOUR_OF_DAY, 0);
//        calculate.set(Calendar.MINUTE, 0);
//        calculate.set(Calendar.SECOND, 0);
//        calculate.set(Calendar.MILLISECOND, 0);
//        model.addAttribute("showStart", userMissionDayCount.getLfTime().before(calculate.getTime()));
//        return "/studentv3/wintermission/index";
    }

    //startMission
    @RequestMapping(value = "startMission.vpage", method = RequestMethod.GET)
    public String startMission() {
        return "redirect:/student/index.vpage";
//        return "/studentv3/wintermission/startmission";
    }

    @RequestMapping(value = "initInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String initInfo() {
        return "redirect:/student/index.vpage";

//        InitInfoResponse initInfoResponse = new InitInfoResponse();
//        try {
//
//            switch (WinterMissionUtil.getMyMissionOpenStatus(currentStudentDetail())) {
//                case NOT_OPEN_YET:
//                    initInfoResponse.failReason = "MISSION_NOT_OPEN";
//                    return initInfoResponse.toResponse();
//                case ALREADY_CLOSED:
//                    initInfoResponse.failReason = "MISSION_ALREADY_CLOSED";
//                    return initInfoResponse.toResponse();
//                case OPEN_ON_NO_VH:
//                    //FIXME  对于非虚拟班 && 非银座小学的学生，查询从 2014/12 至2014/2/10 是否有寒假作业，是 则不可做任务
//                    if (!winterMissionLoaderClient.checkNoHw(currentStudentDetail().getClazzId())) {
//                        initInfoResponse.failReason = "MISSION_ALREADY_CLOSED";
//                        return initInfoResponse.toResponse();
//                    }
//                    break;
//            }
//
//            UserMissionDayCount userMissionDayCount = winterMissionLoaderClient.loadUserMissionDayCount(currentUserId());
//            if (null == userMissionDayCount) {
//                initInfoResponse.failReason = "NO_USER_RECORD";
//                return initInfoResponse.toResponse();
//            }
//            if (SafeConverter.toInt(userMissionDayCount.getDmc()) >= WinterMissionConstant.WINTER_MISSION_MAX_DAY) {
//                initInfoResponse.failReason = "ALL_MISSION_FINISHED";
//                return initInfoResponse.toResponse();
//            }
//            Calendar calculate = Calendar.getInstance();
//            calculate.set(Calendar.HOUR_OF_DAY, 0);
//            calculate.set(Calendar.MINUTE, 0);
//            calculate.set(Calendar.SECOND, 0);
//            calculate.set(Calendar.MILLISECOND, 0);
//
//            if (userMissionDayCount.getLfTime().after(calculate.getTime())) {
//                initInfoResponse.failReason = "ALREADY_DONE";
//                return initInfoResponse.toResponse();
//            }
//            Role pkRole = pkLoaderClient.loadRole(currentUserId());
//            if (null == pkRole) {
//                initInfoResponse.failReason = "NO_PK_ROLE";
//                return initInfoResponse.toResponse();
//            }
//
//            Long bookId = getDefaultBookId();
//            initInfoResponse.userId = String.valueOf(currentUserId());
//            initInfoResponse.bookId = bookId.toString();
//            int randomType = RandomUtils.nextInt(1, 2);
//            //3年级以下的只做应用
//            if (null == currentStudentDetail().getClazzLevelAsInteger() || currentStudentDetail().getClazzLevelAsInteger() < 3) {
//                randomType = WinterMissionConstant.WINTER_MISSION_PRACTICE_APP;
//            }
//            switch (randomType) {
//                case WinterMissionConstant.WINTER_MISSION_PRACTICE_EXAM:
//                    List<Question> eqList = Collections.emptyList();
//                    try {
//                        eqList = buildExamQuestion(bookId, "winterMission", WinterMissionConstant.WINTER_MISSION_EXAM_COUNT, false);
//                    } catch (Exception e) {
//
//                    }
//                    if (CollectionUtils.isNotEmpty(eqList)) {
//                        for (Question q : eqList) {
//                            EnglishExamQuestion eeq = new EnglishExamQuestion();
//                            eeq.fillFrom(q);
//                            initInfoResponse.examQuestionList.add(eeq);
//                            if (initInfoResponse.examQuestionList.size() == WinterMissionConstant.WINTER_MISSION_EXAM_COUNT) {
//                                break;
//                            }
//                        }
//                    }
//                    if (initInfoResponse.examQuestionList.size() < WinterMissionConstant.WINTER_MISSION_EXAM_COUNT) {
//                        initInfoResponse.appQuestionList = buildAppQuestion(bookId);
//                        initInfoResponse.questionType = WinterMissionConstant.WINTER_MISSION_PRACTICE_APP;
//                    } else {
//                        initInfoResponse.questionType = randomType;
//                    }
//                    initInfoResponse.success = true;
//                    break;
//                case WinterMissionConstant.WINTER_MISSION_PRACTICE_APP:
//                    initInfoResponse.appQuestionList = buildAppQuestion(bookId);
//                    initInfoResponse.success = true;
//                    initInfoResponse.questionType = randomType;
//                    break;
//                default:
//                    break;
//            }
//
//        } catch (Exception e) {
//            logger.error("winterMission_initInfo_Exception:", e);
//        }
//        return initInfoResponse.toResponse();
    }
//
//    private List<EnglishAppQuestion> buildAppQuestion(long bookId) {
//        List<String> wordList = new ArrayList<>();
//        try {
//            PsrPrimaryAppEnContent appPsrRs = utopiaPsrServiceClient.getPsrPrimaryAppEn("winterMission", currentUserId(), (currentStudentDetail().getCityCode() == null || 0 == currentStudentDetail().getCityCode()) ? 110000 : currentStudentDetail().getCityCode(), bookId, -1L, WinterMissionConstant.WINTER_MISSION_APP_COUNT, "");
//            if (appPsrRs.getErrorContent().equals("success")) {
//                for (PsrPrimaryAppEnItem enItem : appPsrRs.getAppEnList()) {
//                    wordList.add(enItem.getEid());
//                }
//            }
//        } catch (Exception e) {
//            logger.error("PSR getPsrPrimaryAppEn FAILED with parameter:(cityCode:{},bookId:{},unitId:-1l).trace:{}", currentStudentDetail().getCityCode(), bookId, e.getMessage(), e);
//        }
//        //组织好推题知识点对应的句子
//        Map<Sentence, Unit> psrSentence = this.loadSentenceFromWordListAndBook(wordList, bookId, WinterMissionConstant.WINTER_MISSION_APP_COUNT);
//        int sliceListSize = new BigDecimal(psrSentence.size()).divide(new BigDecimal(3), 0, BigDecimal.ROUND_UP).intValue();
//        List<Map<Sentence, Unit>> sliceListMap = new ArrayList<>(sliceListSize);
//        Map<Sentence, Unit> cursorMap = new LinkedHashMap<>();
//        for (Map.Entry<Sentence, Unit> entry : psrSentence.entrySet()) {
//            cursorMap.put(entry.getKey(), entry.getValue());
//            if (cursorMap.size() == sliceListSize) {
//                sliceListMap.add(cursorMap);
//                cursorMap = new LinkedHashMap<>();
//            }
//        }
//        if (!cursorMap.isEmpty()) {
//            sliceListMap.add(cursorMap);
//        }
//        String cdnUrl = getCdnBaseUrlStaticSharedWithSep();
//
//        int randomTypeLength = 3;
//        BabelEnglishFlashGameConfig[] babelEnglishFlashGameConfigs = new BabelEnglishFlashGameConfig[randomTypeLength];
//        RandomUtils.randomPickFew(
//                new ArrayList<>(BabelEnglishFlashGameConfig.getConfigs()),
//                randomTypeLength,
//                babelEnglishFlashGameConfigs
//        );
//
//        int cursor = 0;
//        List<EnglishAppQuestion> appQuestionList = new ArrayList<>();
//        for (Map<Sentence, Unit> sentenceUnitMapSlice : sliceListMap) {
//            BabelEnglishFlashGameConfig babelEnglishFlashGameConfig = babelEnglishFlashGameConfigs[cursor++];
//            PracticeType englishPractice = practiceLoaderClient.loadNamedPractice(babelEnglishFlashGameConfig.getName());
//            MapMessage mapMessage = flashGameServiceClient.loadDataFromSentenceList(cdnUrl, new ArrayList<>(sentenceUnitMapSlice.keySet()), englishPractice, Ktwelve.PRIMARY_SCHOOL, null, false);
//            EnglishAppQuestion appQ = new EnglishAppQuestion();
//            for (Map.Entry<Sentence, Unit> stcEntry : sentenceUnitMapSlice.entrySet()) {
//                UnitLessonId ul = new UnitLessonId();
//                ul.unitId = String.valueOf(stcEntry.getValue().getId());
//                ul.lessonId = String.valueOf(stcEntry.getKey().getLessonId());
//                appQ.unitLessonIdList.add(ul);
//            }
//            appQ.content = JsonUtils.toJson(mapMessage);
//            appQ.appType = babelEnglishFlashGameConfig.getCategoryName();
//            appQ.practiceId = babelEnglishFlashGameConfig.getId();
//            appQuestionList.add(appQ);
//        }
//        return appQuestionList;
//    }

    @RequestMapping(value = "saveResult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String saveResult() {
        return "redirect:/student/index.vpage";

//        SaveResultRequest req;
//        try {
//            req = SaveResultRequest.parseRequest(getRequestParameter("data", ""));
//        } catch (Exception e) {
//            SaveResultResponse resp = new SaveResultResponse();
//            resp.failReason = "ILLEGAL_PARAMETER";
//            return resp.toResponse();
//        }
//
//        SaveResultResponse resp = new SaveResultResponse();
//
//        switch (WinterMissionUtil.getMyMissionOpenStatus(currentStudentDetail())) {
//            case NOT_OPEN_YET:
//                resp.failReason = "MISSION_NOT_OPEN";
//                return resp.toResponse();
//            case ALREADY_CLOSED:
//                resp.failReason = "MISSION_ALREADY_CLOSED";
//                return resp.toResponse();
//            case OPEN_ON_NO_VH:
//                if (!winterMissionLoaderClient.checkNoHw(currentStudentDetail().getClazzId())) {
//                    resp.failReason = "MISSION_ALREADY_CLOSED";
//                    return resp.toResponse();
//                }
//                break;
//        }
//
//        Role pkRole = pkLoaderClient.loadRole(currentUserId());
//        if (null == pkRole) {
//            logger.warn("USER{} tried to saveResult with NO_PK_ROLE", currentUserId());
//            resp.failReason = "NO_PK_ROLE";
//            return resp.toResponse();
//        }
//
//        if (null == pkRole.getGender() || Gender.NOT_SURE == pkRole.getGender()) {
//            logger.warn("USER{} tried to saveResult with NO_PK_ROLE_GENDER", currentUserId());
//            resp.success = false;
//            resp.failReason = "NO_PK_ROLE_GENDER";
//            return resp.toResponse();
//        }
//
//        double correctRate = 0d;
//        switch (req.questionType) {
//            case WinterMissionConstant.WINTER_MISSION_PRACTICE_APP:
//                if (CollectionUtils.isEmpty(req.appResultList)) {
//                    logger.warn("USER{} tried to saveResult of ENGLISH_APP WITH EMPTY_APP_RESULT", currentUserId());
//                    resp.failReason = "ILLEGAL_PARAMETER_EMPTY_APP_RESULT";
//                    return resp.toResponse();
//                }
//                int correctCount = 0;
//                long bookId = NumberUtils.toLong(req.bookId);
//                List<EnglishAppResult> appResultList = new ArrayList<>(req.appResultList.size());
//                Date nowDate = new Date();
//                long clazzId = null != currentStudentDetail().getClazzId() ? currentStudentDetail().getClazzId() : 0L;
//                for (EnglishAppQuestionResult apRs : req.appResultList) {
//                    EnglishAppResult rs = new EnglishAppResult();
//                    rs.setApp_id("winterMission");
//                    rs.setAtag(apRs.correct);
//                    rs.setBook_id(bookId);
//                    rs.setClass_id(clazzId);
//                    rs.setCmt_time(nowDate);
//                    rs.setCmt_timelen((long) apRs.finishTime);
//                    rs.setEk_list(StringUtils.indexOf(apRs.word, "#") >= 0 ? StringUtils.substring(apRs.word, StringUtils.indexOf(apRs.word, "#") + 1) : apRs.word);
//                    rs.setLesson_id(NumberUtils.toLong(apRs.lessonId));
//                    rs.setPractice_id((long) apRs.practiceId);
//                    rs.setUid(currentUserId());
//                    rs.setUnit_id(NumberUtils.toLong(apRs.unitId));
//                    appResultList.add(rs);
//                    if (apRs.correct) {
//                        correctCount++;
//                    }
//                }
//                if (CollectionUtils.isNotEmpty(appResultList)) {
//                    homeworkQueueClient.saveEnglishAppResults(appResultList);
//                }
//                correctRate = new BigDecimal(correctCount).divide(new BigDecimal(req.appResultList.size()), 2, BigDecimal.ROUND_UP).doubleValue();
//                break;
//            case WinterMissionConstant.WINTER_MISSION_PRACTICE_EXAM:
//                correctRate = req.examCorrectRate;
//                if (correctRate < 0d || correctRate > 1d) {
//                    logger.warn("USER{} tried to saveResult of ENGLISH_EXAM WITH ILLEGAL_CORRECT_RATE of {}", currentUserId(), req.examCorrectRate);
//                    resp.failReason = "ILLEGAL_CORRECT_RATE";
//                    return resp.toResponse();
//                }
//                break;
//            default:
//                logger.warn("USER{} tried to saveResult WITH ILLEGAL_QUESTION_TYPE of {}", currentUserId(), req.questionType);
//                resp.failReason = "ILLEGAL_QUESTION_TYPE";
//                return resp.toResponse();
//        }
//        resp = winterMissionServiceClient.saveMissionResult(currentUserId(), req.questionType, correctRate);
//        if (resp.pkSuitGet) {
//            Set<String> fashionIdSet = null;
//            switch (pkRole.getGender()) {
//                case MALE:
//                    fashionIdSet = WinterMissionConstant.WINTER_MISSION_MALE_PK_PRIZE;
//                    break;
//                case FEMALE:
//                    fashionIdSet = WinterMissionConstant.WINTER_MISSION_FEMALE_PK_PRIZE;
//                    break;
//                default:
//                    break;
//            }
//            MapMessage addPkRs = pkServiceClient.addFashions(currentUserId(), fashionIdSet, null);
//            if (!addPkRs.isSuccess()) {
//                logger.error("USER{} ADD_FASHION_FAILED,fashionId:{}.Trying to rollBack userMissionCount.", currentUserId(), fashionIdSet);
//                winterMissionServiceClient.resetUserMissionRecordToPreviousDay(currentUserId());
//                resp.pkSuitGet = false;
//                resp.success = false;
//                resp.failReason = "ADD_PK_FAILED";
//            }
//        }
//        return resp.toResponse();
    }

//    private Long getDefaultBookId() {
//        String bookCacheKey = "WINTER_MISSION_USER_BOOK:" + currentUserId();
//        CacheObject<Long> fromCache = winterMissionCacheClient.getWinterMissionCacheSystem().CBS.flushable.get(bookCacheKey);
//        if (null != fromCache && null != fromCache.getValue()) {
//            return fromCache.getValue();
//        }
//        Long bookId = null;
//        ClazzTeacher ct = teacherLoaderClient.loadClazzTeacher(currentStudentDetail().getClazzId(), Subject.ENGLISH);
//        Teacher myTeacher = ct == null ? null : ct.getTeacher();
//        if (null != myTeacher) {
//            MapMessage msg = businessTeacherServiceClient.fetchRecommendedBook(myTeacher, Collections.singletonList(currentStudentDetail().getClazzId()));
//            if (null != msg && msg.isSuccess()) {
//                bookId = ((Book) msg.get("book")).getId();
//            }
//        }
//        if (null != bookId) {
//            Map<Sentence, Unit> mp = getAllSentenceOrdrBySentence(bookId);
//            if (null != mp && mp.size() >= WinterMissionConstant.WINTER_MISSION_APP_COUNT) {
//                winterMissionCacheClient.getWinterMissionCacheSystem().CBS.flushable.add(bookCacheKey, 86400 * 3, bookId);
//                return bookId;
//            }
//        }
//        int regionCode = (currentStudentDetail().getCityCode() == null || 0 == currentStudentDetail().getCityCode()) ? 110000 : currentStudentDetail().getCityCode();
//        int clazzLevel = null == currentStudentDetail().getClazz() ? 1 : currentStudentDetail().getClazzLevel().getLevel();
//        if (clazzLevel > 6) {
//            clazzLevel = 6;
//        }
//        bookId = contentLoaderClient.getExtension().initializeClazzBook(Subject.ENGLISH, clazzLevel, regionCode, regionServiceClient);
//        if (null != bookId) {
//            winterMissionCacheClient.getWinterMissionCacheSystem().CBS.flushable.add(bookCacheKey, 86400 * 3, bookId);
//        }
//        return bookId;
//    }
}
