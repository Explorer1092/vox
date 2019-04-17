package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.OralStarScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationAnswerResult;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationQuestionResult;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationSummaryResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.OralCommunicationService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.VoxScoreLevelHelper;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.stone.data.*;
import com.voxlearning.utopia.service.question.api.entity.stone.data.oralpractice.*;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/11/22
 * \* Time: 8:04 PM
 * \* Description: 口语交际从内容库获取学生答题需要的数据
 * \
 */
@Named
@Service(interfaceClass = OralCommunicationService.class)
@ExposeService(interfaceClass = OralCommunicationService.class)
public class OralCommunicationServiceImpl implements OralCommunicationService {

    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;

    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;

    @Inject
    private VoxScoreLevelHelper voxScoreLevelHelper;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Override
    public List<Map> getHomeworkSummaryGroupByType(String homeworkId, String objectiveConfigType) {
        ObjectiveConfigType objectiveType = ObjectiveConfigType.of(objectiveConfigType);
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return Lists.newArrayList();
        }
        List<String> stoneIds = newHomework.findNewHomeworkApps(objectiveType).stream().map(NewHomeworkApp::getStoneDataId).collect(Collectors.toList());
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(stoneIds);
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            return Lists.newArrayList();
        }
        Map<OralCommunicationContentType, List<StoneBufferedData>> groupResult = stoneBufferedDataList.stream()
                .collect(Collectors.groupingBy(s -> {
                    if (s.getOralPracticeConversion() != null) {
                        return OralCommunicationContentType.INTERACTIVE_CONVERSATION;
                    }
                    if (s.getInteractivePictureBook() != null) {
                        return OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK;
                    }
                    if (s.getInteractiveVideo() != null) {
                        return OralCommunicationContentType.INTERACTIVE_VIDEO;
                    }
                    return null;
                }, Collectors.toList()));

        List<Map> result = Lists.newArrayList();
        groupResult.forEach((k, v) -> {
            if (k.equals(OralCommunicationContentType.INTERACTIVE_CONVERSATION)) {
                result.add(MapUtils.m(
                        "type", OralCommunicationContentType.INTERACTIVE_CONVERSATION,
                        "typeName", OralCommunicationContentType.INTERACTIVE_CONVERSATION.getName(),
                        "topics", v.stream().map(s -> s.getOralPracticeConversion().getTopicName()).collect(Collectors.toList()
                        )));
            }
            if (k.equals(OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK)) {
                result.add(MapUtils.m(
                        "type", OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK,
                        "typeName", OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK.getName(),
                        "topics", v.stream().map(s -> s.getInteractivePictureBook().getTopicName()).collect(Collectors.toList()
                        )));
            }
            if (k.equals(OralCommunicationContentType.INTERACTIVE_VIDEO)) {
                result.add(MapUtils.m(
                        "type", OralCommunicationContentType.INTERACTIVE_VIDEO,
                        "typeName", OralCommunicationContentType.INTERACTIVE_VIDEO.getName(),
                        "topics", v.stream().map(s -> s.getInteractiveVideo().getTopicName()).collect(Collectors.toList()
                        )));
            }
        });
        return result;
    }

    @Override
    public List<OralCommunicationSummaryResult> getHomeworkStoneInfo(String homeworkId, Long studentId, String objectiveConfigType) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        List<OralCommunicationSummaryResult> resultList = Lists.newArrayList();
        if (StringUtils.isEmpty(homeworkId) || StringUtils.isEmpty(objectiveConfigType)) {
            return resultList;
        }
        ObjectiveConfigType objectiveType = ObjectiveConfigType.of(objectiveConfigType);
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return resultList;
        }
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (newHomeworkResult != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(objectiveType) != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(objectiveType).getAppAnswers())) {
            appAnswerMap = newHomeworkResult.getPractices().get(objectiveType).getAppAnswers();
        }
        List<String> stoneIds = newHomework.findNewHomeworkApps(objectiveType).stream().map(NewHomeworkApp::getStoneDataId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(stoneIds)) {
            return resultList;
        }
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(stoneIds);
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            return resultList;
        }
        Map<String, StoneBufferedData> stoneSourceDataMap = stoneBufferedDataList
                .stream()
                .collect(Collectors.toMap(StoneBufferedData::getId, Function.identity()));
        //构建题包数据
        for (String stoneId : stoneIds) {
            StoneBufferedData bufferedData = stoneSourceDataMap.get(stoneId);
            if (bufferedData == null) {
                continue;
            }
            OralCommunicationSummaryResult itemData = buildOralCommunicationSummaryResult(bufferedData);
            if (itemData == null) {
                continue;
            }
            resultList.add(itemData);
            if (MapUtils.isEmpty(appAnswerMap) || appAnswerMap.get(stoneId) == null) {
                continue;
            }
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.get(stoneId);
            itemData.setIsFinished(appAnswer != null && appAnswer.isFinished());
            if (itemData.getIsFinished()) {
                Double score = SafeConverter.toDouble(appAnswer.getScore());
                List<Map<String, Object>> oralCommunicationScoreLevels = voxScoreLevelHelper.loadVoxOralCommunicationTotalLevel(studentDetail);
                OralStarScoreLevel oralStartScoreLevel = OralStarScoreLevel.of(NewHomeworkUtils.processScoreLevel(
                        oralCommunicationScoreLevels,
                        score));
                if (oralStartScoreLevel == null) {
                    continue;
                }
                itemData.setLevel(oralStartScoreLevel);
                itemData.setStar(oralStartScoreLevel.getStartCount());
            }
        }
        return resultList;
    }

    /**
     * 获取学生答题时候的题目详情
     *
     * @param stoneId
     * @return
     */
    @Override
    public OralCommunicationQuestionResult getHomeworkStonDetaiInfo(String stoneId) {
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(Collections.singleton(stoneId));
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            return null;
        }
        StoneBufferedData bufferedData = stoneBufferedDataList.get(0);
        return buildOralCommunicationDetailResult(bufferedData);
    }

    /**
     * 获取口语交际答题结果 -- answer接口
     *
     * @param newHomework
     * @param newHomeworkResult
     * @param studentId
     * @param stoneId
     * @return
     */
    @Override
    public OralCommunicationQuestionResult getHomeworkStoneAnswerInfo(NewHomework newHomework, NewHomeworkResult newHomeworkResult, Long studentId, String stoneId) {
        OralCommunicationQuestionResult questionResult = new OralCommunicationQuestionResult();
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(Collections.singleton(stoneId));
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            return questionResult;
        }
        StoneBufferedData bufferedData = stoneBufferedDataList.get(0);
        if (bufferedData == null) {
            return questionResult;
        }
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (newHomeworkResult != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION) != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION).getAppAnswers())) {
            appAnswerMap = newHomeworkResult.getPractices().get(ObjectiveConfigType.ORAL_COMMUNICATION).getAppAnswers();
        }
        if (MapUtils.isEmpty(appAnswerMap) || appAnswerMap.get(stoneId) == null) {
            return questionResult;
        }
        Map<String, SubHomeworkProcessResult> homeworkProcessResultMap;
        NewHomeworkResultAppAnswer appAnswer = appAnswerMap.get(stoneId);
        if (appAnswer == null) {
            return questionResult;
        }
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        List<String> dialogIdList = Lists.newArrayList();
        if (bufferedData.getOralPracticeConversion() != null && CollectionUtils.isNotEmpty(bufferedData.getOralPracticeConversion().getTopics())) {
            dialogIdList = bufferedData.getOralPracticeConversion().getTopics()
                    .stream()
                    .filter(t -> CollectionUtils.isNotEmpty(t.getContents()))
                    .map(Topic::getContents)
                    .flatMap(List::stream)
                    .filter(c -> CollectionUtils.isNotEmpty(c.getDialogs()))
                    .map(OralContent::getDialogs)
                    .flatMap(List::stream)
                    .filter(Objects::nonNull)
                    .filter(d -> SafeConverter.toInt(d.getRequiredAnswer()) == 1)
                    .map(Dialog::getUuid)
                    .collect(Collectors.toList());

        }
        if (bufferedData.getInteractiveVideo() != null && CollectionUtils.isNotEmpty(bufferedData.getInteractiveVideo().getContents())) {
            dialogIdList = bufferedData.getInteractiveVideo().getContents()
                    .stream()
                    .filter(v -> "record".equals(v.getContentType()))
                    .map(VideoContent::getUuid)
                    .collect(Collectors.toList());
        }
        if (bufferedData.getInteractivePictureBook() != null && CollectionUtils.isNotEmpty(bufferedData.getInteractivePictureBook().getPages())) {
            dialogIdList = bufferedData.getInteractivePictureBook().getPages().stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getSections()))
                    .map(InteractivePictureBook.Page::getSections)
                    .flatMap(List::stream)
                    .filter(s -> CollectionUtils.isNotEmpty(s.getQuestions()))
                    .map(InteractivePictureBook.Section::getQuestions)
                    .flatMap(List::stream)
                    .filter(q -> "record".equals(q.getContentType()))
                    .map(InteractivePictureBook.Question::getUuid)
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(dialogIdList)) {
            return questionResult;
        }
        dialogIdList.forEach(d -> {
            SubHomeworkResultAnswer.ID aid = new SubHomeworkResultAnswer.ID();
            aid.setDay(day);
            aid.setHid(newHomework.getId());
            aid.setJoinKeys(Collections.singleton(stoneId));
            aid.setType(ObjectiveConfigType.ORAL_COMMUNICATION);
            aid.setUserId(SafeConverter.toString(studentId));
            aid.setQuestionId(d);
            subHomeworkResultAnswerIds.add(aid.toString());
        });

        if (CollectionUtils.isEmpty(subHomeworkResultAnswerIds)) {
            return questionResult;
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        if (MapUtils.isEmpty(subHomeworkResultAnswerMap)) {
            return questionResult;
        }
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                .stream()
                .filter(Objects::nonNull)
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toList());
        Map<String, SubHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(newHomeworkProcessResultIds);
        if (MapUtils.isEmpty(newHomeworkProcessResultMap)) {
            return questionResult;
        }
        homeworkProcessResultMap = newHomeworkProcessResultMap.values()
                .stream()
                .collect(Collectors.toMap(SubHomeworkProcessResult::getDialogId, Function.identity()));
        if (appAnswer.getStoneType() == OralCommunicationContentType.INTERACTIVE_CONVERSATION) {
            OralCommunicationAnswerResult communicationAnswerResult = buildAnswerResult(bufferedData, OralCommunicationContentType.INTERACTIVE_CONVERSATION, appAnswer.getRoleTopicId(), homeworkProcessResultMap);
            questionResult.setStoneId(communicationAnswerResult.getStoneId());
            questionResult.setStoneType(communicationAnswerResult.getStoneType());
            questionResult.setThumbUrl(communicationAnswerResult.getThumbUrl());
            questionResult.setTopicName(communicationAnswerResult.getTopicName());
            questionResult.setTopicDesc(communicationAnswerResult.getTopicDesc());
            questionResult.setVideoSeconds(communicationAnswerResult.getVideoSeconds());
            questionResult.setVideoUrl(communicationAnswerResult.getVideoUrl());
            questionResult.setKeyWords(communicationAnswerResult.getKeyWords());
            questionResult.setKeySentences(communicationAnswerResult.getKeySentences());
            questionResult.setQuestionContent(MapUtils.m("first_dialog", communicationAnswerResult.getFirstDialog(),
                    "roles", communicationAnswerResult.getRoles(),
                    "topics", communicationAnswerResult.getTopics()
            ));
            return questionResult;
        }
        if (appAnswer.getStoneType() == OralCommunicationContentType.INTERACTIVE_VIDEO) {
            OralCommunicationAnswerResult communicationAnswerResult = buildAnswerResult(bufferedData, OralCommunicationContentType.INTERACTIVE_VIDEO, "", homeworkProcessResultMap);
            questionResult.setStoneId(communicationAnswerResult.getStoneId());
            questionResult.setStoneType(communicationAnswerResult.getStoneType());
            questionResult.setThumbUrl(communicationAnswerResult.getThumbUrl());
            questionResult.setTopicName(communicationAnswerResult.getTopicName());
            questionResult.setTopicDesc(communicationAnswerResult.getTopicDesc());
            questionResult.setVideoSeconds(communicationAnswerResult.getVideoSeconds());
            questionResult.setVideoUrl(communicationAnswerResult.getVideoUrl());
            questionResult.setKeyWords(communicationAnswerResult.getKeyWords());
            questionResult.setKeySentences(communicationAnswerResult.getKeySentences());
            questionResult.setQuestionContent(MapUtils.m("video_contents", communicationAnswerResult.getVideoContents()));
            return questionResult;
        }
        if (appAnswer.getStoneType() == OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK) {
            OralCommunicationAnswerResult communicationAnswerResult = buildAnswerResult(bufferedData, OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK, "", homeworkProcessResultMap);
            questionResult.setStoneId(communicationAnswerResult.getStoneId());
            questionResult.setStoneType(communicationAnswerResult.getStoneType());
            questionResult.setThumbUrl(communicationAnswerResult.getThumbUrl());
            questionResult.setTopicName(communicationAnswerResult.getTopicName());
            questionResult.setTopicDesc(communicationAnswerResult.getTopicDesc());
            questionResult.setVideoSeconds(communicationAnswerResult.getVideoSeconds());
            questionResult.setVideoUrl(communicationAnswerResult.getVideoUrl());
            questionResult.setKeyWords(communicationAnswerResult.getKeyWords());
            questionResult.setKeySentences(communicationAnswerResult.getKeySentences());
            questionResult.setQuestionContent(MapUtils.m("picture_books",
                    MapUtils.m("screen_mode", communicationAnswerResult.getScreenMode()
                            , "pages", communicationAnswerResult.getPages())));
            return questionResult;
        }
        return questionResult;
    }

    /**
     * 构建返回题包summary数据
     *
     * @param bufferedData
     * @return
     */
    private OralCommunicationSummaryResult buildOralCommunicationSummaryResult(StoneBufferedData bufferedData) {
        if (bufferedData == null) {
            return null;
        }
        OralCommunicationSummaryResult result = new OralCommunicationSummaryResult();
        //人机交互
        if (bufferedData.getOralPracticeConversion() != null) {
            OralPracticeConversion oralPracticeConversion = bufferedData.getOralPracticeConversion();
            result.setStoneId(bufferedData.getId());
            result.setStoneType(OralCommunicationContentType.INTERACTIVE_CONVERSATION);
            result.setStoneTypeName(OralCommunicationContentType.INTERACTIVE_CONVERSATION.getName());
            result.setTopicTrans(oralPracticeConversion.getTopicTrans());
            result.setTopicName(oralPracticeConversion.getTopicName());
            result.setThumbUrl(oralPracticeConversion.getThumbUrl());
            return result;
        }
        //互动视频
        if (bufferedData.getInteractiveVideo() != null) {
            InteractiveVideo interactiveVideo = bufferedData.getInteractiveVideo();
            result.setStoneId(bufferedData.getId());
            result.setStoneType(OralCommunicationContentType.INTERACTIVE_VIDEO);
            result.setStoneTypeName(OralCommunicationContentType.INTERACTIVE_VIDEO.getName());
            result.setTopicTrans(interactiveVideo.getTopicTrans());
            result.setTopicName(interactiveVideo.getTopicName());
            result.setThumbUrl(interactiveVideo.getThumbUrl());
            return result;
        }
        //互动绘本
        if (bufferedData.getInteractivePictureBook() != null) {
            InteractivePictureBook pictureBook = bufferedData.getInteractivePictureBook();
            result.setStoneId(bufferedData.getId());
            result.setStoneType(OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK);
            result.setStoneTypeName(OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK.getName());
            result.setTopicTrans(pictureBook.getTopicTrans());
            result.setTopicName(pictureBook.getTopicName());
            result.setThumbUrl(pictureBook.getThumbUrl());
            return result;
        }
        return result;
    }


    private OralCommunicationQuestionResult buildOralCommunicationDetailResult(StoneBufferedData bufferedData) {
        if (bufferedData == null) {
            return null;
        }
        OralCommunicationQuestionResult questionResult = null;
        //人机交互
        if (bufferedData.getOralPracticeConversion() != null) {
            OralPracticeConversion oralPracticeConversion = bufferedData.getOralPracticeConversion();
            questionResult = new OralCommunicationQuestionResult();
            questionResult.setStoneId(bufferedData.getId());
            questionResult.setStoneType(OralCommunicationContentType.INTERACTIVE_CONVERSATION);
            questionResult.setThumbUrl(oralPracticeConversion.getCoverUrl());
            questionResult.setTopicDesc(oralPracticeConversion.getTopicDesc());
            questionResult.setTopicTrans(oralPracticeConversion.getTopicTrans());
            questionResult.setTopicName(oralPracticeConversion.getTopicName());
            questionResult.setTopicDesc(oralPracticeConversion.getTopicDesc());
            questionResult.setVideoSeconds(SafeConverter.toInt(oralPracticeConversion.getVideoSeconds()));
            questionResult.setVideoUrl(oralPracticeConversion.getVideoUrl());
            if (CollectionUtils.isNotEmpty(oralPracticeConversion.getKeySentences())) {
                questionResult.setKeySentences(oralPracticeConversion.getKeySentences()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(oralPracticeConversion.getKeyWords())) {
                questionResult.setKeyWords(oralPracticeConversion.getKeyWords()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            questionResult.setQuestionContent(MapUtils.m("first_dialog", oralPracticeConversion.getFirstDialogs(),
                    "tip_of_choosing_roles", oralPracticeConversion.getTipOfChoosingRoles(),
                    "topics", oralPracticeConversion.getTopics(),
                    "roles", oralPracticeConversion.getRoles()
            ));
            return questionResult;
        }
        //互动视频
        if (bufferedData.getInteractiveVideo() != null) {
            InteractiveVideo interactiveVideo = bufferedData.getInteractiveVideo();
            questionResult = new OralCommunicationQuestionResult();
            questionResult.setStoneId(bufferedData.getId());
            questionResult.setStoneType(OralCommunicationContentType.INTERACTIVE_VIDEO);
            questionResult.setThumbUrl(interactiveVideo.getCoverUrl());
            questionResult.setTopicDesc(interactiveVideo.getTopicDesc());
            questionResult.setTopicTrans(interactiveVideo.getTopicTrans());
            questionResult.setTopicName(interactiveVideo.getTopicName());
            questionResult.setTopicDesc(interactiveVideo.getTopicDesc());
            questionResult.setVideoSeconds(SafeConverter.toInt(interactiveVideo.getVideoSeconds()));
            questionResult.setVideoUrl(interactiveVideo.getVideoUrl());
            if (CollectionUtils.isNotEmpty(interactiveVideo.getKeySentences())) {
                questionResult.setKeySentences(interactiveVideo.getKeySentences()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(interactiveVideo.getKeyWords())) {
                questionResult.setKeyWords(interactiveVideo.getKeyWords()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            questionResult.setQuestionContent(MapUtils.m("video_contents", interactiveVideo.getContents()));
            return questionResult;
        }
        //互动绘本
        if (bufferedData.getInteractivePictureBook() != null) {
            InteractivePictureBook pictureBook = bufferedData.getInteractivePictureBook();
            questionResult = new OralCommunicationQuestionResult();
            questionResult.setStoneId(bufferedData.getId());
            questionResult.setStoneType(OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK);
            questionResult.setThumbUrl(pictureBook.getCoverUrl());
            questionResult.setTopicDesc(pictureBook.getTopicDesc());
            questionResult.setTopicTrans(pictureBook.getTopicTrans());
            questionResult.setTopicName(pictureBook.getTopicName());
            questionResult.setTopicDesc(pictureBook.getTopicDesc());
            questionResult.setVideoSeconds(SafeConverter.toInt(pictureBook.getVideoSeconds()));
            questionResult.setVideoUrl(pictureBook.getVideoUrl());
            if (CollectionUtils.isNotEmpty(pictureBook.getKeySentences())) {
                questionResult.setKeySentences(pictureBook.getKeySentences()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(pictureBook.getKeyWords())) {
                questionResult.setKeyWords(pictureBook.getKeyWords()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            Map<String, Object> pictureBooksMap = Maps.newLinkedHashMap();
            pictureBooksMap.put("screen_mode", pictureBook.getScreenMode());
            pictureBooksMap.put("pages", pictureBook.getPages());
            questionResult.setQuestionContent(MapUtils.m("picture_books", pictureBooksMap));
            return questionResult;
        }
        return questionResult;
    }

    /**
     * build 学生的题目&答案
     *
     * @param bufferedData
     * @param stoneType
     * @param roleTopicId
     * @return
     */
    private OralCommunicationAnswerResult buildAnswerResult(StoneBufferedData bufferedData, OralCommunicationContentType stoneType, String roleTopicId, Map<String, SubHomeworkProcessResult> homeworkProcessResultMap) {
        OralCommunicationAnswerResult result = new OralCommunicationAnswerResult();
        if (bufferedData == null || stoneType == null) {
            return result;
        }
        if (stoneType.equals(OralCommunicationContentType.INTERACTIVE_CONVERSATION)) {
            if (StringUtils.isEmpty(roleTopicId) || bufferedData.getOralPracticeConversion() == null) {
                return result;
            }
            OralPracticeConversion oralPracticeConversion = bufferedData.getOralPracticeConversion();
            result.setStoneId(bufferedData.getId());
            result.setStoneType(OralCommunicationContentType.INTERACTIVE_CONVERSATION);
            result.setThumbUrl(oralPracticeConversion.getThumbUrl());
            result.setTopicName(oralPracticeConversion.getTopicName());
            result.setTopicDesc(oralPracticeConversion.getTopicDesc());
            result.setVideoSeconds(SafeConverter.toInt(oralPracticeConversion.getVideoSeconds()));
            result.setVideoUrl(oralPracticeConversion.getVideoUrl());
            if (CollectionUtils.isNotEmpty(oralPracticeConversion.getFirstDialogs())) {
                result.setFirstDialog(oralPracticeConversion.getFirstDialogs().stream().map(d -> {
                            return
                                    MapUtils.m("role_pic", d.getRolePic(),
                                            "sentence_trans", d.getSentenceTrans(),
                                            "sentence", d.getSentence(),
                                            "show_at_right", d.getShowAtRight(),
                                            "start_at", d.getStartAt(),
                                            "end_at", d.getEndAt(),
                                            "key_words", d.getKeyWords()
                                    );
                        }
                ).collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(oralPracticeConversion.getKeySentences())) {
                result.setKeySentences(oralPracticeConversion.getKeySentences()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(oralPracticeConversion.getKeyWords())) {
                result.setKeyWords(oralPracticeConversion.getKeyWords()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            if (CollectionUtils.isEmpty(oralPracticeConversion.getTopics())) {
                return result;
            }

            Topic topic = oralPracticeConversion.getTopics().stream()
                    .filter(t -> CollectionUtils.isNotEmpty(t.getContents()))
                    .filter(t -> roleTopicId.equals(t.getUuid()))
                    .findFirst().orElse(null);
            if (topic == null) {
                return result;
            }
            OralPracticeConversion.Role role = bufferedData.getOralPracticeConversion().getRoles().stream()
                    .filter(r -> r.getRoleName().equals(topic.getRoleName()))
                    .findFirst().orElse(null);
            if (role != null) {
                result.setRoles(Lists.newArrayList(
                        MapUtils.m("role_name", role.getRoleName(),
                                "role_pic", role.getRolePic(),
                                "role_intro", role.getRoleIntro()
                        )));
            }
            List<Map<String, Object>> topicList = Lists.newArrayList();
            Map<String, Object> userAnswerTopicMap = Maps.newLinkedHashMap();
            List<Map<String, Object>> topicContentList = Lists.newArrayList();
            topicList.add(userAnswerTopicMap);
            userAnswerTopicMap.put("uuid", roleTopicId);
            userAnswerTopicMap.put("role_name", topic.getRoleName());
            userAnswerTopicMap.put("topic_name", topic.getTopicName());
            userAnswerTopicMap.put("img_url", topic.getImgUrl());
            userAnswerTopicMap.put("contents", topicContentList);
            List<OralContent> oralContentList = topic.getContents();
            if (CollectionUtils.isEmpty(oralContentList)) {
                return result;
            }
            for (OralContent oralContent : oralContentList) {

                Map<String, Object> contentItemMap = Maps.newLinkedHashMap();
                topicContentList.add(contentItemMap);
                contentItemMap.put("content_video_url", oralContent.getContentVideoUrl());
                contentItemMap.put("content_pic_url", oralContent.getContentPicUrl());
                List<Map<String, Object>> dialogList = Lists.newArrayList();
                if (CollectionUtils.isEmpty(oralContent.getDialogs())) {
                    continue;
                }
                oralContent.getDialogs().forEach(c ->
                        dialogList.add(MapUtils.m(
                                "uuid", c.getUuid(),
                                "sentence_trans", c.getSentenceTrans(),
                                "sentence", c.getSentence(),
                                "key_words", c.getKeyWords(),
                                "audio_url", c.getAudioUrl(),
                                "audio_duration", c.getAudioDuration(),
                                "required_answer", c.getRequiredAnswer(),
                                "ex_sentence", c.getExSentence(),
                                "ex_sentence_trans", c.getExSentenceTrans(),
                                "ex_sentence_audio", c.getExSentenceAudio(),
                                "ex_key_words", c.getExKeyWords(),
                                "student_audio", homeworkProcessResultMap.get(c.getUuid()) != null
                                        && CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(c.getUuid()).getOralDetails().get(0))
                                        && homeworkProcessResultMap.get(c.getUuid()).getOralDetails().get(0).get(0) != null
                                        ? homeworkProcessResultMap.get(c.getUuid()).getOralDetails().get(0).get(0).getAudio() : null,
                                "star", homeworkProcessResultMap.get(c.getUuid()) != null
                                        && CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(c.getUuid()).getOralDetails().get(0))
                                        && homeworkProcessResultMap.get(c.getUuid()).getOralDetails().get(0).get(0) != null
                                        ? homeworkProcessResultMap.get(c.getUuid()).getOralDetails().get(0).get(0).getStar() : null,
                                "student_duration", homeworkProcessResultMap.get(c.getUuid()) != null
                                        && CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(c.getUuid()).getOralDetails().get(0))
                                        && homeworkProcessResultMap.get(c.getUuid()).getOralDetails().get(0).get(0) != null
                                        ? homeworkProcessResultMap.get(c.getUuid()).getOralDetails().get(0).get(0).getDuration() : null
                        ))
                );
                contentItemMap.put("dialogs", dialogList);
            }
            userAnswerTopicMap.put("contents", topicContentList);
            result.setTopics(topicList);
            return result;
        }
        if (stoneType.equals(OralCommunicationContentType.INTERACTIVE_VIDEO) && bufferedData.getInteractiveVideo() != null) {
            InteractiveVideo interactiveVideo = bufferedData.getInteractiveVideo();
            result.setStoneId(bufferedData.getId());
            result.setStoneType(OralCommunicationContentType.INTERACTIVE_VIDEO);
            result.setThumbUrl(interactiveVideo.getThumbUrl());
            result.setTopicName(interactiveVideo.getTopicName());
            result.setTopicDesc(interactiveVideo.getTopicDesc());
            result.setVideoSeconds(SafeConverter.toInt(interactiveVideo.getVideoSeconds()));
            result.setVideoUrl(interactiveVideo.getVideoUrl());
            if (CollectionUtils.isNotEmpty(interactiveVideo.getKeySentences())) {
                result.setKeySentences(interactiveVideo.getKeySentences()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(interactiveVideo.getKeyWords())) {
                result.setKeyWords(interactiveVideo.getKeyWords()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            List<Map<String, Object>> videoContentList = Lists.newArrayList();
            List<VideoContent> videoContents = interactiveVideo.getContents();
            if (CollectionUtils.isEmpty(videoContents)) {
                return result;
            }
            videoContents.stream().forEachOrdered(v -> videoContentList.add(MapUtils.m(
                    "uuid", v.getUuid(),
                    "time_point", v.getTimePoint(),
                    "content_audio", v.getContentAudio(),
                    "content_text", v.getContentText(),
                    "options", v.getOptions(),
                    "content_type", v.getContentType(),
                    "example", v.getExample(),
                    "example_trans", v.getExampleTrans(),
                    "example_audio", v.getExampleAudio(),
                    "student_audio", "record".equals(v.getContentType()) &&
                            homeworkProcessResultMap.get(v.getUuid()) != null
                            && CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(v.getUuid()).getOralDetails().get(0))
                            && homeworkProcessResultMap.get(v.getUuid()).getOralDetails().get(0).get(0) != null
                            ? homeworkProcessResultMap.get(v.getUuid()).getOralDetails().get(0).get(0).getAudio() : null,
                    "star", "record".equals(v.getContentType()) &&
                            homeworkProcessResultMap.get(v.getUuid()) != null
                            && CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(v.getUuid()).getOralDetails().get(0))
                            && homeworkProcessResultMap.get(v.getUuid()).getOralDetails().get(0).get(0) != null
                            ? homeworkProcessResultMap.get(v.getUuid()).getOralDetails().get(0).get(0).getStar() : null,
                    "student_duration", "record".equals(v.getContentType()) &&
                            homeworkProcessResultMap.get(v.getUuid()) != null
                            && CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(v.getUuid()).getOralDetails().get(0))
                            && homeworkProcessResultMap.get(v.getUuid()).getOralDetails().get(0).get(0) != null
                            ? homeworkProcessResultMap.get(v.getUuid()).getOralDetails().get(0).get(0).getDuration() : null
            )));
            result.setVideoContents(videoContentList);
            return result;
        }

        if (stoneType.equals(OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK) && bufferedData.getInteractivePictureBook() != null) {
            InteractivePictureBook interactivePictureBook = bufferedData.getInteractivePictureBook();
            result.setStoneId(bufferedData.getId());
            result.setStoneType(OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK);
            result.setThumbUrl(interactivePictureBook.getThumbUrl());
            result.setTopicName(interactivePictureBook.getTopicName());
            result.setTopicDesc(interactivePictureBook.getTopicDesc());
            result.setVideoSeconds(SafeConverter.toInt(interactivePictureBook.getVideoSeconds()));
            result.setVideoUrl(interactivePictureBook.getVideoUrl());
            result.setScreenMode(interactivePictureBook.getScreenMode());
            if (CollectionUtils.isNotEmpty(interactivePictureBook.getKeySentences())) {
                result.setKeySentences(interactivePictureBook.getKeySentences()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(interactivePictureBook.getKeyWords())) {
                result.setKeyWords(interactivePictureBook.getKeyWords()
                        .stream().map(s -> MapUtils.m("trans", s.getTrans(), "text", s.getText(), "audio_url", s.getAudioUrl()))
                        .collect(Collectors.toList()));
            }
            List<Map<String, Object>> pages = Lists.newArrayList();
            if (CollectionUtils.isEmpty(interactivePictureBook.getPages())) {
                return result;
            }
            for (InteractivePictureBook.Page p : interactivePictureBook.getPages()) {
                Map<String, Object> pageItem = Maps.newLinkedHashMap();
                pageItem.put("image_url", p.getImageUrl());
                List<Map<String, Object>> sections = Lists.newArrayList();
                if (CollectionUtils.isEmpty(p.getSections())) {
                    pageItem.put("sections", sections);
                    pages.add(pageItem);
                    continue;
                }
                for (InteractivePictureBook.Section s : p.getSections()) {
                    Map<String, Object> section = Maps.newLinkedHashMap();
                    section.put("paragraph", s.getParagraph());
                    section.put("post_hotspots", s.getPostHotSpots());
                    section.put("last_hotspots", s.getLastHotSpots());
                    if (CollectionUtils.isEmpty(s.getQuestions())) {
                        sections.add(section);
                        continue;
                    }
                    List<Map<String, Object>> questions = Lists.newArrayList();
                    for (InteractivePictureBook.Question q : s.getQuestions()) {
                        questions.add(MapUtils.m(
                                "uuid", q.getUuid(),
                                "content_audio", q.getContentAudio(),
                                "content_text", q.getContentText(),
                                "key_words", q.getKeyWords(),
                                "content_text_frame", q.getContentTextFrame(),
                                "options", q.getOptions(),
                                "content_type", q.getContentType(),
                                "example", q.getExample(),
                                "example_trans", q.getExampleTrans(),
                                "example_audio", q.getExampleAudio(),
                                "student_audio", "record".equals(q.getContentType()) &&
                                        homeworkProcessResultMap.get(q.getUuid()) != null
                                        && CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(q.getUuid()).getOralDetails().get(0))
                                        && homeworkProcessResultMap.get(q.getUuid()).getOralDetails().get(0).get(0) != null
                                        ? homeworkProcessResultMap.get(q.getUuid()).getOralDetails().get(0).get(0).getAudio() : null,
                                "star", "record".equals(q.getContentType()) &&
                                        homeworkProcessResultMap.get(q.getUuid()) != null
                                        && CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(q.getUuid()).getOralDetails().get(0))
                                        && homeworkProcessResultMap.get(q.getUuid()).getOralDetails().get(0).get(0) != null
                                        ? homeworkProcessResultMap.get(q.getUuid()).getOralDetails().get(0).get(0).getStar() : null,
                                "student_duration", "record".equals(q.getContentType()) &&
                                        homeworkProcessResultMap.get(q.getUuid()) != null
                                        && CollectionUtils.isNotEmpty(homeworkProcessResultMap.get(q.getUuid()).getOralDetails().get(0))
                                        && homeworkProcessResultMap.get(q.getUuid()).getOralDetails().get(0).get(0) != null
                                        ? homeworkProcessResultMap.get(q.getUuid()).getOralDetails().get(0).get(0).getDuration() : null
                        ));
                    }
                    section.put("questions", questions);
                    sections.add(section);
                }
                pageItem.put("sections", sections);
                pages.add(pageItem);
            }
            result.setPages(pages);
            return result;
        }
        return result;
    }


}
