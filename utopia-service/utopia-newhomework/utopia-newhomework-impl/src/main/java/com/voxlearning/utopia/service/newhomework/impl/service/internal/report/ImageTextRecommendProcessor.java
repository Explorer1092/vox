package com.voxlearning.utopia.service.newhomework.impl.service.internal.report;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ImageTextRecommend;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultAnswerDao;
import com.voxlearning.utopia.service.newhomework.impl.support.ImageTextRhymeStarCalculator;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ImageTextRecommendProcessor extends NewHomeworkSpringBean {

    @Inject private SubHomeworkResultAnswerDao subHomeworkResultAnswerDao;
    @Inject private ImageTextRhymeStarCalculator imageTextRhymeStarCalculator;

    /**
     * 推荐图文入韵优秀录音
     */
    public void recommendExcellentVoices(Map<Long, User> userMap, NewHomework newHomework, Collection<NewHomeworkResult> newHomeworkResults, MapMessage mapMessage) {
        ImageTextRecommend imageTextRecommend = imageTextRecommendDao.load(newHomework.getId());
        if (!newHomework.getSubject().equals(Subject.CHINESE)) {
            return;
        }
        if (imageTextRecommend != null && CollectionUtils.isNotEmpty(imageTextRecommend.getImageTextList())) {
            mapMessage.add("hasImageTextRecommend", true);
            mapMessage.add("imageTextList", imageTextRecommend.getImageTextList());
            return;
        }
        List<BaseVoiceRecommend.ImageText> imageTexts = loadAllImageTexts(userMap, newHomework, newHomeworkResults);
        if (CollectionUtils.isEmpty(imageTexts)) {
            mapMessage.add("hasImageTextRecommend", false);
            mapMessage.add("imageTextList", Collections.emptyList());
            return;
        }
        imageTexts = imageTexts.stream().limit(3).collect(Collectors.toList());
        mapMessage.add("hasImageTextRecommend", false);
        mapMessage.add("imageTextList", imageTexts);
    }

    private List<BaseVoiceRecommend.ImageText> loadAllImageTexts(Map<Long, User> userMap, NewHomework newHomework, Collection<NewHomeworkResult> newHomeworkResults) {
        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE);
        if (newHomeworkPracticeContent == null || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
            return Collections.emptyList();
        }
        List<BaseVoiceRecommend.ImageText> imageTexts = new ArrayList<>();
        Map<String, List<ImageTextRhymeHomework>> stoneIdImageTextHomeworksMap = new LinkedHashMap<>();
        for (NewHomeworkApp newHomeworkApp : newHomeworkPracticeContent.getApps()) {
            stoneIdImageTextHomeworksMap.put(newHomeworkApp.getStoneDataId(), newHomeworkApp.getImageTextRhymeQuestions());
        }
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        for (NewHomeworkResult r : newHomeworkResults) {
            if (!r.isFinished() || userMap.get(r.userId) == null) {
                continue;
            }
            Long userId = r.getUserId();
            NewHomeworkResultAnswer resultAnswer = r.getPractices().get(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE);
            if (resultAnswer != null && MapUtils.isNotEmpty(resultAnswer.getAppAnswers())) {
                for (Map.Entry<String, List<ImageTextRhymeHomework>> entry : stoneIdImageTextHomeworksMap.entrySet()) {
                    String stoneId = entry.getKey();
                    List<ImageTextRhymeHomework> imageTextRhymeHomeworks = entry.getValue();
                    if (CollectionUtils.isEmpty(imageTextRhymeHomeworks)) {
                        continue;
                    }
                    for (ImageTextRhymeHomework imageTextRhymeHomework : imageTextRhymeHomeworks) {
                        String imgUrl = imageTextRhymeHomework.getImageUrl() != null ? imageTextRhymeHomework.getImageUrl() : NewHomeworkConstants.WORD_TEACH_IMAGE_TEXT_RHYME_DEFAULT_IMG;
                        List<Double> scoreList = new ArrayList<>();
                        String studentName = userMap.get(r.getUserId()).fetchRealname();
                        for (NewHomeworkQuestion question : imageTextRhymeHomework.getChapterQuestions()) {
                            String answerId = new NewHomework.NewHomeworkQuestionObj(newHomework.getId(), ObjectiveConfigType.WORD_TEACH_AND_PRACTICE, Collections.singletonList(stoneId), question.getQuestionId())
                                    .generateSubHomeworkResultAnswerId(day, r.getUserId());
                            SubHomeworkResultAnswer answer = subHomeworkResultAnswerDao.load(answerId);
                            if (answer != null) {
                                String processId = answer.getProcessId();
                                NewHomeworkProcessResult processResult = newHomeworkProcessResultLoader.load(newHomework.getId(), processId);
                                if (processResult != null) {
                                    double score = SafeConverter.toDouble(processResult.getActualScore());
                                    scoreList.add(score);
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(scoreList)) {
                            double totalScore = 0;
                            for (Double s : scoreList) {
                                totalScore += s;
                            }
                            int score = new BigDecimal(totalScore).divide(new BigDecimal(scoreList.size()), 2, BigDecimal.ROUND_DOWN).intValue();
                            int star = imageTextRhymeStarCalculator.calculateImageTextRhymeStar(score);
                            VoiceRecommend.ImageText recommendImageText = new VoiceRecommend.ImageText();
                            recommendImageText.setStudentId(userId);
                            recommendImageText.setStudentName(studentName);
                            recommendImageText.setStoneId(stoneId);
                            recommendImageText.setChapterId(imageTextRhymeHomework.getChapterId());
                            recommendImageText.setCoverImageUrl(imgUrl);
                            recommendImageText.setStar(star);
                            recommendImageText.setScore(score);
                            recommendImageText.setFlashvarsUrl((UrlUtils.buildUrlQuery("/exam/flash/student/imagetextrhyme/detail"
                                    + Constants.AntiHijackExt, MiscUtils.m("homeworkId", newHomework.getId(),
                                    "studentId", userId, "stoneDataId", stoneId, "chapterId", imageTextRhymeHomework.getChapterId()))));
                            imageTexts.add(recommendImageText);
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(imageTexts)) {
            return Collections.emptyList();
        }
        Comparator<BaseVoiceRecommend.ImageText> scoreComp = (o1, o2) -> Integer.compare(SafeConverter.toInt(o2.getScore()), SafeConverter.toInt(o1.getScore()));
        return imageTexts.stream().sorted(scoreComp).collect(Collectors.toList());
    }
}
