package com.voxlearning.washington.controller.open.v1.homework;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLivecastLoader;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.service.LiveCastGenerateDataService;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkLivecastService;
import com.voxlearning.utopia.service.question.api.DubbingLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.net.message.exam.SaveNewHomeworkResultRequest;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.REQ_HOMEWORK_ID;

@Controller
@RequestMapping("/v1/newhomework")
public class NewHomeworkApiController extends AbstractApiController {

    @Getter
    @ImportService(interfaceClass = NewHomeworkLivecastLoader.class)
    private NewHomeworkLivecastLoader newHomeworkLivecastLoader;

    @Getter
    @ImportService(interfaceClass = NewHomeworkLivecastService.class)
    private NewHomeworkLivecastService newHomeworkLivecastService;

    @Getter
    @ImportService(interfaceClass = LiveCastGenerateDataService.class)
    private LiveCastGenerateDataService liveCastGenerateDataService;

    @Getter
    @ImportService(interfaceClass = DubbingLoader.class)
    private DubbingLoader dubbingLoader;

    @RequestMapping(value = "/do.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage doNewHomework() {
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            validateRequired(REQ_OBJECTIVE_CONFIG_TYPE, "作业形式");
            validateRequired(REQ_LEARNING_TYPE, "学习类型");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_HOMEWORK_ID, REQ_OBJECTIVE_CONFIG_TYPE, REQ_LEARNING_TYPE, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String objectiveConfigType = getRequestString(REQ_OBJECTIVE_CONFIG_TYPE);
        String learningType = getRequestString(REQ_LEARNING_TYPE);
        StudyType studyType = StudyType.of(learningType);

        // 小U接入趣味配音
        if (learningType.startsWith("va-")) {
            String requestUrl = getVARequestDomain(learningType) + "/v1/newhomework/do.api";
            Map<Object, Object> params = new LinkedHashMap<>();
            params.put(REQ_HOMEWORK_ID, homeworkId);
            params.put(REQ_OBJECTIVE_CONFIG_TYPE, objectiveConfigType);
            params.put(REQ_LEARNING_TYPE, learningType);
            params.put(REQ_STUDENT_ID, getRequestLong(REQ_STUDENT_ID));
            return forwardVARequest(requestUrl, params);
        }

        if (studyType == null) {
            return failMessage("未知的学习类型");
        }
        if (studyType != StudyType.homework && studyType != StudyType.vacationHomework && studyType != StudyType.livecastHomework) {
            return failMessage("不支持的学习类型");
        }

        if (studyType == StudyType.homework) {
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            if (newHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else if (studyType == StudyType.vacationHomework) {
            VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
            if (vacationHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else {
            LiveCastHomework liveCastHomework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
            if (liveCastHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        }

        Long studentId = getApiStudentId();
        if (studentId == null) {
            return failMessage("学生id为空");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("学生id错误");
        }
        if (ObjectiveConfigType.DUBBING.name().equals(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(objectiveConfigType)) {
            List<DubbingSummaryResult> dubbingSummaryResults;
            if (studyType == StudyType.homework) {
                dubbingSummaryResults = dubbingHomeworkServiceClient.getDubbingSummerInfo(homeworkId, studentId, objectiveConfigType);
            } else if (studyType == StudyType.vacationHomework) {
                dubbingSummaryResults = dubbingHomeworkServiceClient.getVacationDubbingSummerInfo(homeworkId, studentId, objectiveConfigType);
            } else {
                dubbingSummaryResults = dubbingHomeworkServiceClient.getLiveCastDubbingSummerInfo(homeworkId, studentId);
            }

            if (CollectionUtils.isEmpty(dubbingSummaryResults)) {
                return failMessage("作业内容为空");
            }
            List<Map<String, Object>> dubbingList = new ArrayList<>();
            for (DubbingSummaryResult summaryResult : dubbingSummaryResults) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(RES_RESULT_DUBBING_ID, summaryResult.getDubbingId());
                map.put(RES_RESULT_DUBBING_IS_SONG, summaryResult.getIsSong());
                map.put(RES_RESULT_DUBBING_NAME, summaryResult.getDubbingName());
                map.put(RES_RESULT_DUBBING_COVER_IMG, summaryResult.getCoverUrl());
                map.put(RES_RESULT_DUBBING_SUMMARY, summaryResult.getVideoSummary());
                map.put(RES_RESULT_DUBBING_VIDEO_URL, summaryResult.getVideoUrl());
                map.put(RES_RESULT_DUBBING_SYNTHETIC, summaryResult.getSynthetic());
                map.put(RES_RESULT_DUBBING_SKIP_UPLOAD_VIDEO, summaryResult.getSkipUploadVideo());
                map.put(RES_RESULT_DUBBING_IS_FINISHED, summaryResult.getFinished());
                map.put(RES_RESULT_DUBBING_SCORE, summaryResult.getScore());
                map.put(RES_CLAZZ_LEVEL, summaryResult.getLevel());
                map.put(RES_CLAZZ_LEVEL_NAME, summaryResult.getClazzLevel());
                List<Map<String, Object>> keyWords = summaryResult.getKeyWords();
                if (CollectionUtils.isNotEmpty(keyWords)) {
                    List<Map<String, Object>> keyWordList = new ArrayList<>();
                    for (Map keyWord : keyWords) {
                        String audioUrl = SafeConverter.toString(keyWord.get("audioUrl"));
                        if (StringUtils.isNotEmpty(audioUrl)) {
                            audioUrl = getCdnBaseUrlStaticSharedWithSep() + audioUrl;
                        }
                        keyWordList.add(MapUtils.m(
                                RES_RESULT_DUBBING_KEY_WORD_CHINESE, SafeConverter.toString(keyWord.get("chineseWord")),
                                RES_RESULT_DUBBING_KEY_WORD_ENGLISH, SafeConverter.toString(keyWord.get("englishWord")),
                                RES_RESULT_DUBBING_KEY_WORD_AUDIO_URL, audioUrl)
                        );
                    }
                    map.put(RES_RESULT_DUBBING_KEY_WORD_LIST, keyWordList);
                }
                List<Map<String, Object>> keyGrammars = summaryResult.getKeyGrammars();
                if (CollectionUtils.isNotEmpty(keyGrammars)) {
                    List<Map<String, Object>> keyGrammarList = new ArrayList<>();
                    for (Map keyGrammar : keyGrammars) {
                        keyGrammarList.add(MapUtils.m(
                                RES_RESULT_DUBBING_KEY_GRAMMAR_NAME, SafeConverter.toString(keyGrammar.get("grammarName")),
                                RES_RESULT_DUBBING_KEY_GRAMMAR_EXAMPLE, SafeConverter.toString(keyGrammar.get("exampleSentence"))
                        ));
                    }
                    map.put(RES_RESULT_DUBBING_KEY_GRAMMAR_LIST, keyGrammarList);
                }
                map.put(RES_RESULT_DUBBING_TOPIC_LIST, summaryResult.getTopics());
                dubbingList.add(map);
            }

            return successMessage()
                    .add(RES_RESULT_DUBBING_LIST, dubbingList)
                    .add(RES_RESULT_DUBBING_LEGACY_HOMEWORK, ObjectiveConfigType.DUBBING.name().equals(objectiveConfigType))
                    .add(RES_RESULT_DUBBING_CDN_URL_LIST, dubbingCdnUrlList())
                    .add(RES_RESULT_DUBBING_VOX8_SENTENCE_SCORE_LEVELS, newHomeworkContentServiceClient.loadVoxSentenceScoreLevels(studentDetail))
                    .add(RES_RESULT_DUBBING_VOX8_SONG_SCORE_LEVELS, newHomeworkContentServiceClient.loadVoxSongScoreLevels(studentDetail));
        } else if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.name().equals(objectiveConfigType)) {
            if (studyType == StudyType.homework) {
                NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
                if (newHomework == null) {
                    return failMessage("作业不存在，或者作业已被老师删除");
                }
                NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
                if (newHomeworkPracticeContent == null) {
                    return failMessage("作业内容错误");
                }
                NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, false);
                boolean finished = newHomeworkResult != null
                        && newHomeworkResult.getPractices() != null
                        && newHomeworkResult.getPractices().get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC) != null
                        && newHomeworkResult.getPractices().get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC).getFinishAt() != null;
                return successMessage()
                        .add(RES_WORK_BOOK_NAME, newHomeworkPracticeContent.getWorkBookName())
                        .add(RES_HOMEWORK_DETAIL, newHomeworkPracticeContent.getHomeworkDetail())
                        .add(RES_FINISHED, finished);
            } else {
                return failMessage("不支持的作业类型");
            }
        } else if (ObjectiveConfigType.ORAL_COMMUNICATION.name().equals(objectiveConfigType)) {
            if (studyType != StudyType.homework) {
                return failMessage("不支持的作业类型");
            }
            List<OralCommunicationSummaryResult> summaryInfoList = oralCommunicationClient.getHomeworkStoneInfo(homeworkId, studentId, objectiveConfigType);
            List<Map<String, Object>> stoneSubjectList = Lists.newArrayList();
            stoneSubjectList.add(MapUtils.m("main_desc", "学习目标", "item_label", "重点句型与词汇"));
            stoneSubjectList.add(MapUtils.m("main_desc", "交际练习", "item_label", "人机对话，有趣交流"));
            return successMessage()
                    .add(RES_RESULT_STONE_SUBJECT, stoneSubjectList)
                    .add(RES_RESULT_STONE_LIST, summaryInfoList)
                    .add(REQ_ORAL_COMMUNICATION_SINGLE_SCORE, newHomeworkContentServiceClient.loadVoxOralCommunicationSingleLevel(studentDetail))
                    .add(REQ_ORAL_COMMUNICATION_CDN_LIST, dubbingCdnUrlList());

        }
        return failMessage("不支持的作业形式");
    }

    @RequestMapping(value = "/dubbing/questions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage loadNewHomeworkDubbingQuestions() {
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            validateRequired(REQ_OBJECTIVE_CONFIG_TYPE, "作业形式");
            validateRequired(REQ_DUBBING_ID, "配音id");
            validateRequired(REQ_LEARNING_TYPE, "学习类型");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_HOMEWORK_ID, REQ_OBJECTIVE_CONFIG_TYPE, REQ_DUBBING_ID, REQ_LEARNING_TYPE, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String objectiveConfigType = getRequestString(REQ_OBJECTIVE_CONFIG_TYPE);
        String dubbingId = getRequestString(REQ_DUBBING_ID);
        String learningType = getRequestString(REQ_LEARNING_TYPE);
        StudyType studyType = StudyType.of(learningType);
        String model = getRequestString(REQ_MODEL);
        String systemVersion = getRequestString(REQ_SYSTEM_VERSION);

        // 小U接入趣味配音
        if (learningType.startsWith("va-")) {
            String requestUrl = getVARequestDomain(learningType) + "/v1/newhomework/dubbing/questions.api";
            Map<Object, Object> params = new LinkedHashMap<>();
            params.put(REQ_HOMEWORK_ID, homeworkId);
            params.put(REQ_OBJECTIVE_CONFIG_TYPE, objectiveConfigType);
            params.put(REQ_DUBBING_ID, dubbingId);
            params.put(REQ_LEARNING_TYPE, learningType);
            params.put(REQ_STUDENT_ID, getRequestLong(REQ_STUDENT_ID));
            return forwardVARequest(requestUrl, params);
        }

        if (studyType == null) {
            return failMessage("未知的学习类型");
        }
        if (studyType != StudyType.homework && studyType != StudyType.vacationHomework && studyType != StudyType.livecastHomework) {
            return failMessage("不支持的学习类型");
        }

        Long studentId = getApiStudentId();
        if (studentId == null) {
            return failMessage("学生id为空");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("学生id错误");
        }

        if (studyType == StudyType.homework) {
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            if (newHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else if (studyType == StudyType.vacationHomework) {
            VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
            if (vacationHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else {
            LiveCastHomework liveCastHomework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
            if (liveCastHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        }
        HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
        request.setHomeworkId(homeworkId);
        request.setObjectiveConfigType(ObjectiveConfigType.of(objectiveConfigType));
        request.setVideoId(dubbingId);
        if (ObjectiveConfigType.DUBBING.name().equals(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(objectiveConfigType)) {
            Map<String, Object> questionMap;
            if (studyType == StudyType.homework) {
                questionMap = newHomeworkLoaderClient.loadHomeworkQuestions(request);
            } else if (studyType == StudyType.vacationHomework) {
                questionMap = vacationHomeworkLoaderClient.loadHomeworkQuestions(homeworkId, ObjectiveConfigType.of(objectiveConfigType), null, null, dubbingId, null);
            } else {
                questionMap = liveCastGenerateDataService.loadHomeworkQuestions(homeworkId, ObjectiveConfigType.of(objectiveConfigType), null, null, dubbingId);
            }
            if (MapUtils.isEmpty(questionMap)) {
                return failMessage("获取题目信息失败");
            }
            MapMessage message = successMessage();
            Map<String, Object> dubbingInfoMap = new LinkedHashMap<>();
            dubbingInfoMap.put(RES_RESULT_DUBBING_ID, SafeConverter.toString(questionMap.get("dubbingId")));
            dubbingInfoMap.put(RES_RESULT_DUBBING_IS_SONG, SafeConverter.toBoolean(questionMap.get("isSong")));
            dubbingInfoMap.put(RES_RESULT_DUBBING_NAME, SafeConverter.toString(questionMap.get("dubbingName")));
            dubbingInfoMap.put(RES_RESULT_DUBBING_VIDEO_URL, SafeConverter.toString(questionMap.get("videoUrl")));
            dubbingInfoMap.put(RES_RESULT_DUBBING_BACKGROUND_VIDEO_URL, SafeConverter.toString(questionMap.get("backgroundMusicUrl")));
            dubbingInfoMap.put(RES_RESULT_DUBBING_COVER_IMG, SafeConverter.toString(questionMap.get("coverImgUrl")));
            List<Map<String, Object>> sentences = (List<Map<String, Object>>) questionMap.get("sentenceList");
            if (CollectionUtils.isNotEmpty(sentences)) {
                List<Map<String, Object>> sentenceList = new ArrayList<>();
                for (Map<String, Object> sentence : sentences) {
                    List<Map<String, Object>> keyWordList = (List<Map<String, Object>>) sentence.get("keyWordList");
                    if (CollectionUtils.isNotEmpty(keyWordList)) {
                        for (Map<String, Object> keyWord : keyWordList) {
                            String dubbingKeyWordAudioUrl = SafeConverter.toString(keyWord.get("dubbing_key_word_audio_url"));
                            if (StringUtils.isNotEmpty(dubbingKeyWordAudioUrl)) {
                                keyWord.put("dubbing_key_word_audio_url", getCdnBaseUrlStaticSharedWithSep() + dubbingKeyWordAudioUrl);
                            }
                        }
                    }
                    sentenceList.add(MapUtils.m(
                            RES_RESULT_SENTENCE_CHINESE_CONTENT, SafeConverter.toString(sentence.get("sentenceChineseContent")),
                            RES_RESULT_SENTENCE_ENGLISH_CONTENT, SafeConverter.toString(sentence.get("sentenceEnglishContent")),
                            RES_RESULT_SENTENCE_VIDEO_START, SafeConverter.toString(sentence.get("sentenceVideoStart")),
                            RES_RESULT_SENTENCE_VIDEO_END, SafeConverter.toString(sentence.get("sentenceVideoEnd")),
                            RES_RESULT_QUESTION_ID, SafeConverter.toString(sentence.get("questionId")),
                            RES_RESULT_DUBBING_KEY_WORD_LIST, keyWordList)
                    );
                }
                dubbingInfoMap.put(RES_RESULT_SENTENCE_LIST, sentenceList);
            }
            dubbingInfoMap.put(RES_RESULT_DUBBING_LEGACY_HOMEWORK, ObjectiveConfigType.DUBBING.name().equals(objectiveConfigType));
            message.add(RES_RESULT_DUBBING_INFO, dubbingInfoMap);
            message.add(RES_RESULT_DUBBING_ASYNCHRONOUS_SYNTHETIC, grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "DubbingSynthetic", "WhiteList"));
            message.add(RES_RESULT_DUBBING_SOFT_DECODING, dubbingSoftDecoding(model, systemVersion));
            return message;
        }
        return failMessage("不支持的作业形式");
    }

    /**
     * 口语交际 ： 学生端 - 获取题目接口
     * @return
     */
    @RequestMapping(value = "/oralcommunication/questions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage loadNewHomeworkOralCommunicationQuestions() {
       try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            validateRequired(REQ_OBJECTIVE_CONFIG_TYPE, "作业形式");
            validateRequired(REQ_STONE_ID, "情景包id");
            validateRequired(REQ_LEARNING_TYPE, "学习类型");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_HOMEWORK_ID, REQ_OBJECTIVE_CONFIG_TYPE, REQ_STONE_ID, REQ_LEARNING_TYPE, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String objectiveConfigType = getRequestString(REQ_OBJECTIVE_CONFIG_TYPE);
        String stoneId = getRequestString(REQ_STONE_ID);
        String learningType = getRequestString(REQ_LEARNING_TYPE);
        StudyType studyType = StudyType.of(learningType);
        if (studyType == null) {
            return failMessage("未知的学习类型");
        }
        if (studyType != StudyType.homework && studyType != StudyType.vacationHomework && studyType != StudyType.livecastHomework) {
            return failMessage("不支持的学习类型");
        }

        Long studentId = getApiStudentId();
        if (studentId == null) {
            return failMessage("学生id为空");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("学生id错误");
        }

        if (studyType == StudyType.homework) {
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            if (newHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else if (studyType == StudyType.vacationHomework) {
            VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
            if (vacationHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else {
            LiveCastHomework liveCastHomework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
            if (liveCastHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        }
        if (ObjectiveConfigType.ORAL_COMMUNICATION.name().equals(objectiveConfigType)) {
            return successMessage()
                    .add(RES_RESULT_PACKAGE_INFO, oralCommunicationClient.getHomeworkStonDetaiInfo(stoneId));
        }
        return failMessage("不支持的作业形式");
    }

    @RequestMapping(value = "/dubbing/questions/answer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadNewHomeworkDubbingQuestionsAnswer() {
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            validateRequired(REQ_OBJECTIVE_CONFIG_TYPE, "作业形式");
            validateRequired(REQ_DUBBING_ID, "配音id");
            validateRequired(REQ_LEARNING_TYPE, "学习类型");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_HOMEWORK_ID, REQ_OBJECTIVE_CONFIG_TYPE, REQ_DUBBING_ID, REQ_LEARNING_TYPE, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String objectiveConfigType = getRequestString(REQ_OBJECTIVE_CONFIG_TYPE);
        String dubbingId = getRequestString(REQ_DUBBING_ID);
        String learningType = getRequestString(REQ_LEARNING_TYPE);
        StudyType studyType = StudyType.of(learningType);

        // 小U接入趣味配音
        if (learningType.startsWith("va-")) {
            String requestUrl = getVARequestDomain(learningType) + "/v1/newhomework/dubbing/questions/answer.api";
            Map<Object, Object> params = new LinkedHashMap<>();
            params.put(REQ_HOMEWORK_ID, homeworkId);
            params.put(REQ_OBJECTIVE_CONFIG_TYPE, objectiveConfigType);
            params.put(REQ_DUBBING_ID, dubbingId);
            params.put(REQ_LEARNING_TYPE, learningType);
            params.put(REQ_STUDENT_ID, getRequestLong(REQ_STUDENT_ID));
            return forwardVARequest(requestUrl, params);
        }

        if (studyType == null) {
            return failMessage("未知的学习类型");
        }
        if (studyType != StudyType.homework && studyType != StudyType.vacationHomework && studyType != StudyType.livecastHomework) {
            return failMessage("不支持的学习类型");
        }

        if (studyType == StudyType.homework) {
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            if (newHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else if (studyType == StudyType.vacationHomework) {
            VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
            if (vacationHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else {
            LiveCastHomework liveCastHomework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
            if (liveCastHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        }

        Long studentId = getApiStudentId();
        if (studentId == null) {
            return failMessage("学生id为空");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("学生id错误");
        }
        Dubbing dubbing = dubbingLoader.loadDubbingByIdIncludeDisabled(dubbingId);
        if (ObjectiveConfigType.DUBBING.name().equals(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(objectiveConfigType)) {
            MapMessage message = successMessage();
            Map<String, Object> result;
            HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
            request.setHomeworkId(homeworkId);
            request.setObjectiveConfigType(ObjectiveConfigType.of(objectiveConfigType));
            request.setStudentId(studentDetail.getId());
            request.setVideoId(dubbingId);

            if (studyType == StudyType.homework) {
                result = newHomeworkLoaderClient.loadQuestionAnswer(request);
            } else if (studyType == StudyType.vacationHomework) {
                result = vacationHomeworkLoaderClient.loadQuestionAnswer(ObjectiveConfigType.of(objectiveConfigType), homeworkId, null, null, dubbingId, null);
            } else {
                result = liveCastGenerateDataService.loadHomeworkQuestionsAnswer(ObjectiveConfigType.DUBBING, homeworkId, studentDetail.getId(), null, null, dubbingId);
            }

            if (studyType == StudyType.homework || studyType == StudyType.vacationHomework) {
                String shareUrl = ProductConfig.getMainSiteBaseUrl() + UrlUtils.buildUrlQuery("/view/mobile/student/junior/dubbing_share/dubbing_with_score", MiscUtils.m("homeworkId", homeworkId, "dubbingId", dubbingId, "studentId", studentId));
                String shareContent = studentDetail.fetchRealnameIfBlankId() + "小朋友为《" + dubbing.getVideoName() + "》视频配音啦，快来听听吧！";
                String shareTitle = "邀请你一起听" + studentDetail.fetchRealnameIfBlankId() + "小朋友的优秀配音";
                String shareDubbingImg = dubbing.getCoverUrl();

                String sys = getRequestString(REQ_SYS);
                String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
                String errorVersion = RuntimeMode.isProduction() ? "3.0.6.2045" : "3.0.6.2036";
                if (StringUtils.equalsIgnoreCase(sys, "android") && StringUtils.equals(appVersion, errorVersion)) {
                    shareDubbingImg = "";
                }

                message.add(RES_RESULT_SENTENCE_LIST, result.get("sentenceList"))
                        .add(RES_RESULT_DUBBING_SHARE_URL, shareUrl)
                        .add(RES_RESULT_DUBBING_SHARE_TITLE, shareTitle)
                        .add(RES_RESULT_DUBBING_SHARE_CONTENT, shareContent)
                        .add(RES_RESULT_DUBBING_SHARE_IMG, shareDubbingImg)
                        .add(RES_RESULT_DUBBING_SKIP_UPLOAD_VIDEO, SafeConverter.toBoolean(result.get("skipUploadVideo")));
            }

            if (MapUtils.isEmpty(result)) {
                return failMessage("获取答案失败");
            }
            message.add(RES_RESULT_DUBBING_ID, SafeConverter.toString(result.get("dubbingId")));
            message.add(RES_RESULT_DUBBING_NAME, SafeConverter.toString(result.get("dubbingName")));
            message.add(RES_RESULT_DUBBING_COVER_IMG, SafeConverter.toString(result.get("coverImgUrl")));
            message.add(RES_RESULT_DUBBING_VIDEO_URL, SafeConverter.toString(result.get("dubbingVideoUrl")));
            message.add(RES_RESULT_SENTENCE_COUNT, SafeConverter.toString(result.get("sentenceCount")));
            return message;
        }
        return failMessage("不支持的作业形式");
    }

    /**
     * 学生端 - 获取口语交际结果页接口
     * @return
     */
    @RequestMapping(value = "/oralcommunication/questions/answer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadNewHomeworkOralCommunicationQuestionsAnswer() {
        try {
            validateRequired(REQ_HOMEWORK_ID, "作业id");
            validateRequired(REQ_OBJECTIVE_CONFIG_TYPE, "作业形式");
            validateRequired(REQ_STONE_ID, "情景包id");
            validateRequired(REQ_LEARNING_TYPE, "学习类型");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_HOMEWORK_ID, REQ_OBJECTIVE_CONFIG_TYPE, REQ_STONE_ID, REQ_LEARNING_TYPE, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkId = getRequestString(REQ_HOMEWORK_ID);
        String objectiveConfigType = getRequestString(REQ_OBJECTIVE_CONFIG_TYPE);
        String stoneId = getRequestString(REQ_STONE_ID);
        String learningType = getRequestString(REQ_LEARNING_TYPE);
        StudyType studyType = StudyType.of(learningType);
        if (studyType == null) {
            return failMessage("未知的学习类型");
        }
        if (studyType != StudyType.homework && studyType != StudyType.vacationHomework && studyType != StudyType.livecastHomework) {
            return failMessage("不支持的学习类型");
        }

        if (studyType == StudyType.homework) {
            NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
            if (newHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else if (studyType == StudyType.vacationHomework) {
            VacationHomework vacationHomework = vacationHomeworkLoaderClient.loadVacationHomeworkById(homeworkId);
            if (vacationHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        } else {
            LiveCastHomework liveCastHomework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
            if (liveCastHomework == null) {
                return failMessage("作业不存在，或者作业已被老师删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            }
        }

        Long studentId = getApiStudentId();
        if (studentId == null) {
            return failMessage("学生id为空");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return failMessage("学生id错误");
        }
        if (ObjectiveConfigType.ORAL_COMMUNICATION.name().equals(objectiveConfigType)) {
            Map<String, Object> result = successMessage();
            MapMessage message = successMessage();
            if (studyType == StudyType.homework) {
                HomeworkQuestionAnswerRequest request = new HomeworkQuestionAnswerRequest();
                request.setHomeworkId(homeworkId);
                request.setObjectiveConfigType(ObjectiveConfigType.of(objectiveConfigType));
                request.setStudentId(studentDetail.getId());
                request.setStoneDataId(stoneId);
                Map<String, Object> serverResult = newHomeworkLoaderClient.loadQuestionAnswer(request);
                if (MapUtils.isNotEmpty(serverResult) && serverResult.containsKey("result")) {
                    return successMessage()
                            .add(RES_RESULT_PACKAGE_INFO, serverResult.get("result"));
                }
            } else if (studyType == StudyType.vacationHomework) {
                result = vacationHomeworkLoaderClient.loadQuestionAnswer(ObjectiveConfigType.of(objectiveConfigType), homeworkId, null, null, stoneId, null);
            } else {
                result = liveCastGenerateDataService.loadHomeworkQuestionsAnswer(ObjectiveConfigType.of(objectiveConfigType), homeworkId, studentDetail.getId(), null, null, stoneId);
            }
            if (MapUtils.isNotEmpty(result)) {
                message.putAll(result);
            }
            return message;
        }
        return failMessage("不支持的作业形式");
    }

    @RequestMapping(value = "/batch/processresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage batchProcessNewHomeworkResult() {
        try {
            validateRequired(REQ_HOMEWORK_RESULT_DATA, "答题数据");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_HOMEWORK_RESULT_DATA, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String homeworkResultData = getRequestString(REQ_HOMEWORK_RESULT_DATA);
        Map<String, Object> resultMap = JsonUtils.fromJson(homeworkResultData);
        if (MapUtils.isEmpty(resultMap)) {
            return failMessage("提交结果数据异常");
        }

        Long studentId = getApiStudentId();
        if (studentId == null) {
            return failMessage("学生id为空");
        }
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) {
            return failMessage("学生id错误");
        }

        SaveNewHomeworkResultRequest request = new SaveNewHomeworkResultRequest();
        request.setHomeworkId(SafeConverter.toString(resultMap.get(REQ_HOMEWORK_ID)));
        request.setObjectiveConfigType(SafeConverter.toString(resultMap.get(REQ_OBJECTIVE_CONFIG_TYPE)));
        request.setLearningType(SafeConverter.toString(resultMap.get(REQ_LEARNING_TYPE)));
        request.setSkipUploadVideo(SafeConverter.toBoolean(resultMap.get(REQ_SKIP_UPLAOD_VIDEO), false));
        request.setConsumeTime(SafeConverter.toLong(resultMap.get(REQ_CONSUME_TIME)));
        request.setDubbingId(SafeConverter.toString(resultMap.get(REQ_DUBBING_ID)));
        request.setVideoUrl(SafeConverter.toString(resultMap.get(REQ_DUBBING_VIDEO_URL)));
        request.setStoneId(SafeConverter.toString(resultMap.get(REQ_STONE_ID)));
        request.setStoneType(SafeConverter.toString(resultMap.get(REQ_STONE_TYPE)));
        request.setTopicRoleId(SafeConverter.toString(resultMap.get(REQ_TOPIC_ROLE_ID)));
        List<Map<String, Object>> answers = (List<Map<String, Object>>) resultMap.get(REQ_STUDENT_HOMEWORK_ANSWERS);

        // 小U接入趣味配音
        if (request.getLearningType().startsWith("va-")) {
            String requestUrl = getVARequestDomain(request.getLearningType()) + "/v1/newhomework/batch/processresult.api";
            Map<Object, Object> params = new LinkedHashMap<>();
            params.put(REQ_HOMEWORK_RESULT_DATA, homeworkResultData);
            params.put(REQ_STUDENT_ID, getRequestLong(REQ_STUDENT_ID));
            return forwardVARequest(requestUrl, params);
        }

        if (CollectionUtils.isNotEmpty(answers)) {
            List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
            for (Map<String, Object> answer : answers) {
                StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                studentHomeworkAnswer.setQuestionId(SafeConverter.toString(answer.get(REQ_QUESTION_ID)));
                studentHomeworkAnswer.setDialogId(SafeConverter.toString(answer.get(REQ_ORAL_COMMUNICATION_DIALOG_ID)));
                studentHomeworkAnswer.setDurationMilliseconds(SafeConverter.toLong(answer.get(REQ_DURATION_MILLISECONDS)));
                studentHomeworkAnswer.setVoiceEngineType(VoiceEngineType.of(SafeConverter.toString(resultMap.get(REQ_VOICE_ENGINE_TYPE))));
                studentHomeworkAnswer.setVoiceCoefficient(SafeConverter.toString(resultMap.get(REQ_VOICE_COEFFICIENT)));
                studentHomeworkAnswer.setVoiceMode(SafeConverter.toString(resultMap.get(REQ_VOICE_MODE)));
                studentHomeworkAnswer.setVoiceScoringMode(SafeConverter.toString(resultMap.get(REQ_VOICE_SCORING_MODE)));
                studentHomeworkAnswer.setSentenceType(SafeConverter.toInt(resultMap.get(REQ_SENTENCE_TYPE)));
                if (CollectionUtils.isNotEmpty((List) answer.get(REQ_ORAL_SCORE_DETAILS))) {
                    List<HashMap<String, Object>> oralList = (List<HashMap<String, Object>>) answer.get(REQ_ORAL_SCORE_DETAILS);
                    List<NewHomeworkProcessResult.OralDetail> oralDetails = Lists.newArrayList();

                    HashMap<String, Object> oralMap = (HashMap<String, Object>) ((List) oralList.get(0)).get(0);
                    NewHomeworkProcessResult.OralDetail oralDetail = new NewHomeworkProcessResult.OralDetail();
                    oralDetail.setAudio(SafeConverter.toString(oralMap.get(RES_RESULT_DUBBING_ORAL_AUDIO)));
                    oralDetail.setMacScore(SafeConverter.toInt(oralMap.get(RES_RESULT_DUBBING_ORAL_MAC_SCORE)));
                    oralDetail.setBusinessLevel(SafeConverter.toFloat(oralMap.get(RES_RESULT_DUBBING_ORAL_BUSINESS_LEVEL)));
                    oralDetail.setStandardScore(SafeConverter.toInt(oralMap.get(RES_RESULT_DUBBING_ORAL_STANDARD_SCORE)));
                    oralDetail.setFluency(SafeConverter.toInt(oralMap.get(RES_RESULT_DUBBING_ORAL_FLUENCY)));
                    oralDetail.setIntegrity(SafeConverter.toInt(oralMap.get(RES_RESULT_DUBBING_ORAL_INTEGRITY)));
                    oralDetail.setPronunciation(SafeConverter.toInt(oralMap.get(RES_RESULT_DUBBING_ORAL_PRONUNCIATION)));
                    oralDetail.setKeyStandardScore(SafeConverter.toDouble(oralMap.get(REQ_KEY_STANDARD_SCORE)));
                    oralDetail.setIsHasKeyWords(SafeConverter.toBoolean(oralMap.get(REQ_IS_HAS_KEY_WORDS)));
                    oralDetail.setStar(SafeConverter.toInt(oralMap.get(REQ_STAR)));
                    oralDetail.setDuration(SafeConverter.toLong(oralMap.get(REQ_DURATION)));
                    oralDetails.add(oralDetail);

                    List<List<NewHomeworkProcessResult.OralDetail>> list = new ArrayList<>();
                    list.add(oralDetails);

                    studentHomeworkAnswer.setOralScoreDetails(list);
                } else {
                    //口语交际如果没有答题结果，则过滤掉这条答题数据
                    if (ObjectiveConfigType.ORAL_COMMUNICATION.name().equals(request.getObjectiveConfigType())) {
                        continue;
                    }
                }
                if (CollectionUtils.isNotEmpty((List<List<String>>) answer.get(REQ_FILE_URLS))) {
                    studentHomeworkAnswer.setFileUrls((List<List<String>>) answer.get(REQ_FILE_URLS));
                }
                studentHomeworkAnswers.add(studentHomeworkAnswer);
            }
            if (CollectionUtils.isEmpty(studentHomeworkAnswers)) {
                return failMessage("提交结果数据异常");
            }
            request.setStudentHomeworkAnswers(studentHomeworkAnswers);
        }
        List<Map<String, Object>> imageDetails = (List<Map<String, Object>>) resultMap.get(REQ_OCR_MENTAL_IMAGE_DETAILS);
        if (CollectionUtils.isNotEmpty(imageDetails)) {
            List<OcrMentalImageDetail> ocrMentalImageDetails = new ArrayList<>();
            for (Map<String, Object> imageDetail : imageDetails) {
                OcrMentalImageDetail ocrMentalImageDetail = JsonUtils.fromJson(JsonUtils.toJson(imageDetail), OcrMentalImageDetail.class);
                if (ocrMentalImageDetail != null) {
                    ocrMentalImageDetails.add(ocrMentalImageDetail);
                }
            }
            request.setOcrMentalImageDetails(ocrMentalImageDetails);
        }
        if (!ObjectiveConfigType.DUBBING.name().equals(request.getObjectiveConfigType())
                && !ObjectiveConfigType.DUBBING_WITH_SCORE.name().equals(request.getObjectiveConfigType())
                && !ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.name().equals(request.getObjectiveConfigType())
                && !ObjectiveConfigType.ORAL_COMMUNICATION.name().equals(request.getObjectiveConfigType())
                && !ObjectiveConfigType.OCR_DICTATION.name().equals(request.getObjectiveConfigType())
                ) {
            return failMessage("不支持的作业形式");
        }
        try {
            StudyType studyType = StudyType.of(request.getLearningType());
            MapMessage message;
            if (StudyType.livecastHomework == studyType) {
                message = processLiveCastHomeworkResult(student, request, studyType);
            } else {
                message = homeworkResultProcessor.processSaveNewHomeworkResultRequest(student, request, getRequest(), getWebRequestContext());
            }
            if (message.isSuccess()) {
                if (ObjectiveConfigType.ORAL_COMMUNICATION.name().equals(request.getObjectiveConfigType()) && message.get("result") != null) {
                    MapMessage result;
                    Map serverResult = (Map) message.get("result");
                    if (serverResult != null && serverResult.get("NewHomeworkOralCommunication") != null) {
                        logger.error("batchProcessNewHomeworkResult_info request: {}", JsonUtils.toJson(request));
                    }
                    Map oralMessage = (Map) serverResult.get("oral_communication_result");
                    if (oralMessage == null) {
                        return failMessage("提交结果数据异常");
                    }
                    result = MapMessage.of(oralMessage);
                    result.add(RES_RESULT, RES_RESULT_SUCCESS);
                    return result;
                }
                return successMessage();
            } else {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", studentId,
                        "mod1", request.getHomeworkId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK,
                        "mod3", "error message",
                        "errorInfo", message.getInfo(),
                        "errorCode", message.getErrorCode(),
                        "objectiveConfigType", request.getObjectiveConfigType(),
                        "homeworkResultData", JsonUtils.toJson(homeworkResultData),
                        "op", "student homework result"
                ));
                Map serverResult = (Map) message.get("result");
                if (serverResult != null && serverResult.get("NewHomeworkOralCommunication") != null) {
                    logger.error("batchProcessNewHomeworkResult_info request: {}", JsonUtils.toJson(request));
                }
                return failMessage(message.getInfo()).setErrorCode(message.getErrorCode());
            }
        } catch (Exception e) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", studentId,
                    "mod1", request.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK,
                    "mod3", "catch exception",
                    "objectiveConfigType", request.getObjectiveConfigType(),
                    "homeworkResultData", JsonUtils.toJson(homeworkResultData),
                    "op", "student homework result"
            ));
            logger.error("batchProcessNewHomeworkResult : request : {} ", JsonUtils.toJson(request));
            return failMessage("提交结果数据异常");
        }
    }

    private MapMessage processLiveCastHomeworkResult(Student student, SaveNewHomeworkResultRequest result, StudyType studyType) {
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(result.getObjectiveConfigType());
        LiveCastHomeworkResultContext context = new LiveCastHomeworkResultContext();
        context.setUserId(student.getId());
        context.setUser(student);
        context.setHomeworkId(result.getHomeworkId());
        context.setLearningType(studyType);
        context.setObjectiveConfigType(objectiveConfigType);
        context.setBookId(result.getBookId());
        context.setUnitId(result.getUnitId());
        context.setUnitGroupId(result.getUnitGroupId());
        context.setLessonId(result.getLessonId());
        context.setSectionId(result.getSectionId());
        context.setPracticeId(result.getPracticeId());
        context.setPictureBookId(result.getPictureBookId());
        context.setDubbingId(result.getDubbingId());
        context.setVideoUrl(result.getVideoUrl());
        context.setDurations(result.getDurations());
        context.setClientType(result.getClientType());
        context.setClientName(result.getClientName());
        context.setIpImei(StringUtils.isNotBlank(result.getIpImei()) ? result.getIpImei() : getWebRequestContext().getRealRemoteAddress());
        context.setUserAgent(getRequest().getHeader("User-Agent"));
        context.setStudentHomeworkAnswers(result.getStudentHomeworkAnswers());
        context.setConsumeTime(result.getConsumeTime());
        return newHomeworkLivecastService.processorHomeworkResult(context);
    }

    private Long getApiStudentId() {
        User user = getApiRequestUser();
        if (user == null) {
            return null;
        }
        if (user.fetchUserType() == UserType.PARENT) {
            return getRequestLong(REQ_STUDENT_ID);
        } else {
            return user.getId();
        }
    }

    private boolean dubbingSoftDecoding(String model, String systemVersion) {
        if (StringUtils.isBlank(model) || StringUtils.isBlank(systemVersion)) {
            return false;
        }
        String configContent = getPageBlockContentGenerator().getPageBlockContentHtml("student_homework", "dubbing_soft_decoding");
        if (StringUtils.isBlank(configContent)) {
            return false;
        }
        configContent = configContent.replace("\r", "").replace("\n", "").replace("\t", "");
        List<String> configList = JsonUtils.fromJsonToList(configContent, String.class);
        return !CollectionUtils.isEmpty(configList) && configList.contains(model + ":" + systemVersion);
    }

    private List<String> dubbingCdnUrlList() {
        String configContent = getPageBlockContentGenerator().getPageBlockContentHtml("student_homework", "dubbing_cdn_url_list");
        configContent = configContent.replace("\r", "").replace("\n", "").replace("\t", "");
        if (StringUtils.isBlank(configContent)) {
            return Collections.emptyList();
        }
        return JsonUtils.fromJsonToList(configContent, String.class);
    }

    private String getVARequestDomain(String learningType) {
        learningType = learningType.replace("va-", "");
        String domain = "test.17zuoye.net";
        if (RuntimeMode.isStaging()) {
            domain = "staging.17zuoye.net";
        } else if (RuntimeMode.isProduction()) {
            domain = "17zuoye.com";
        }
        return "http://" + learningType + "." + domain;
    }

    private MapMessage forwardVARequest(String url, Map<Object, Object> params) {
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .post(url)
                .addParameter(params)
                .socketTimeout(10000)
                .execute();
        if (response != null && response.getStatusCode() == 200) {
            Map<String, Object> map = JsonUtils.fromJson(response.getResponseString());
            if (MapUtils.isNotEmpty(map) && map.containsKey("result")) {
                return MapMessage.of(map);
            } else {
                return failMessage("获取数据失败");
            }
        } else {
            logger.error("调用:{}失败, response:{}", url, response != null ? response.getResponseString() : "");
            return failMessage("获取数据失败");
        }
    }
}
