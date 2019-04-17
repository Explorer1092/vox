package com.voxlearning.utopia.service.newhomework.impl.service.internal.report;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.DubbingRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/8/24
 * \* Time: 上午11:38
 * \* Description:趣味配音作业推介
 * \
 */
@Named
public class DubbingWithScoreRecommendProcessor extends NewHomeworkSpringBean {
    /**
     * 推荐优秀录音
     *
     * @param userMap
     * @param newHomework
     * @param newHomeworkResults
     * @param mapMessage
     */
    public void recommendExcellentVoices(Map<Long, User> userMap, NewHomework newHomework, Collection<NewHomeworkResult> newHomeworkResults, MapMessage mapMessage) {
        DubbingRecommend dubbingRecommend = dubbingRecommendDao.load(newHomework.getId());
        boolean hasDubbingRecommend;
        if (!newHomework.getSubject().equals(Subject.ENGLISH)) {
            return;
        }
        if (dubbingRecommend != null && CollectionUtils.isNotEmpty(dubbingRecommend.getExcellentDubbingStu())) {
            hasDubbingRecommend = true;
            mapMessage.add("hasDubbingRecommend", hasDubbingRecommend);
            mapMessage.add("excellentDubbingStu", dubbingRecommend.getExcellentDubbingStu());
            return;
        }
        hasDubbingRecommend = false;
        List<BaseVoiceRecommend.DubbingWithScore> excellentDubbingStu = loadAllDubbing(userMap, newHomework, newHomeworkResults);
        excellentDubbingStu = excellentDubbingStu.stream().filter(s -> s.getScore() >= AppOralScoreLevel.B.getScore()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(excellentDubbingStu)) {
            mapMessage.add("hasDubbingRecommend", hasDubbingRecommend);
            mapMessage.add("excellentDubbingStu", CollectionUtils.emptyCollection());
            return;
        }
        excellentDubbingStu = excellentDubbingStu.stream().limit(3).collect(Collectors.toList());
        mapMessage.add("hasDubbingRecommend", hasDubbingRecommend);
        mapMessage.add("excellentDubbingStu", excellentDubbingStu);
    }


    /**
     * 加载所有趣味配音的作业-按顺序
     *
     * @param userMap
     * @param newHomework
     * @param newHomeworkResults
     * @return
     */
    List<BaseVoiceRecommend.DubbingWithScore> loadAllDubbing(Map<Long, User> userMap, NewHomework newHomework, Collection<NewHomeworkResult> newHomeworkResults) {
        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING_WITH_SCORE);
        if (newHomeworkPracticeContent == null
                || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
            return Collections.emptyList();
        }
        List<BaseVoiceRecommend.DubbingWithScore> excellentDubbingStu = Lists.newArrayList();
        for (NewHomeworkResult r : newHomeworkResults) {
            if (!r.isFinished() || userMap.get(r.userId) == null) {
                continue;
            }
            Map<String, String> didToHyidMap = newHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(NewHomeworkApp::getDubbingId,
                            (NewHomeworkApp o) ->
                                    new DubbingSyntheticHistory.ID(newHomework.getId(), r.userId, o.getDubbingId()).toString()));
            Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(didToHyidMap.keySet());
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE);
            if (newHomeworkResultAnswer == null || MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                continue;
            }
            for (NewHomeworkApp app : newHomeworkPracticeContent.getApps()) {
                User student = userMap.get(r.userId);
                BaseVoiceRecommend.DubbingWithScore stuDbuInfo = new BaseVoiceRecommend.DubbingWithScore();
                stuDbuInfo.setUserId(r.userId);
                stuDbuInfo.setUserName(student.fetchRealname());
                String dubbingId = app.getDubbingId();
                if (StringUtils.isEmpty(dubbingId)) {
                    continue;
                }
                stuDbuInfo.setDubbingId(dubbingId);
                Dubbing dubbing = dubbingMap.get(dubbingId);
                stuDbuInfo.setCoverUrl(dubbing.getCoverUrl());
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer
                        .getAppAnswers()
                        .get(dubbingId);
                if (newHomeworkResultAppAnswer == null) {
                    continue;
                }
                stuDbuInfo.setVideoName(dubbing.getVideoName());
                if (newHomeworkResultAppAnswer.getDuration() != null) {
                    int duration = new BigDecimal(newHomeworkResultAppAnswer.processDuration())
                            .divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP)
                            .intValue();
                    stuDbuInfo.setDuration(duration);
                } else {
                    stuDbuInfo.setDuration(0);
                }
                if (newHomeworkResultAppAnswer.getScore() != null) {
                    int score = new BigDecimal(newHomeworkResultAppAnswer.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    stuDbuInfo.setScore(score);
                } else {
                    stuDbuInfo.setScore(0);
                }
                if (StringUtils.isNotBlank(newHomeworkResultAppAnswer.getVideoUrl())) {
                    stuDbuInfo.setStudentVideoUrl(newHomeworkResultAppAnswer.getVideoUrl());
                }
                excellentDubbingStu.add(stuDbuInfo);
            }
        }
        if (CollectionUtils.isEmpty(excellentDubbingStu)) {
            return Collections.emptyList();
        }
        Comparator<BaseVoiceRecommend.DubbingWithScore> scoreComp = (o1, o2) -> Integer.compare(SafeConverter.toInt(o2.getScore()), SafeConverter.toInt(o1.getScore()));
        scoreComp.thenComparingLong(o -> SafeConverter.toLong(o.getDuration()));
        excellentDubbingStu = excellentDubbingStu.stream().sorted(scoreComp).collect(Collectors.toList());
        return excellentDubbingStu;
    }


}
