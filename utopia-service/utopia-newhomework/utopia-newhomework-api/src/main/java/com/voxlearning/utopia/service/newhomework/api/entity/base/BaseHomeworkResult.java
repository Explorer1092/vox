package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2016/11/24
 */
@Getter
@Setter
@ToString
public class BaseHomeworkResult implements Serializable {

    private static final long serialVersionUID = 2885019177487447835L;

    public String homeworkId;
    public SchoolLevel schoolLevel;
    public Subject subject;                                                        // 学科
    public String actionId;                                                        // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
    public Long clazzGroupId;                                                      // 班组id，有问题问长远
    public Long userId;                                                            // 用户id，根据大作业的趋势，以后做题的会变成各种角色
    public Date finishAt;
    public String comment;                                                         // 作业评语
    public String audioComment;                                                    // 作业音频评语
    public Integer rewardIntegral;                                                 // 奖励学豆
    public Integer integral;                                                       // 完成作业奖励学豆
    public Integer energy;                                                         // 完成作业奖励能量
    public Integer credit;                                                         // 完成作业奖励学分
    public Date userStartAt;                                                       // 开始做作业的时间

    public LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices;  // 做作业的内容<作业形式, <试题id, 作业明细id(homework_process的id)>>


    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }


    @JsonIgnore
    public boolean isFinishedOfObjectiveConfigType(ObjectiveConfigType objectiveConfigType) {
        return practices != null && practices.containsKey(objectiveConfigType) && practices.get(objectiveConfigType).isFinished();
    }

    /**
     * @param includeAppReading 是否包含Base_app  READING 类型 , if flag is true,then add the base_app
     * @return 获取全部的homeworkProcessIds
     */
    public List<String> findAllHomeworkProcessIds(boolean includeAppReading) {
        if (practices == null || practices.isEmpty())
            return Collections.emptyList();
        List<String> result = new LinkedList<>();
        practices.keySet()
                .stream()
                .filter(o -> includeAppReading || (
                        o != ObjectiveConfigType.BASIC_APP
                                && o != ObjectiveConfigType.READING
                                && o != ObjectiveConfigType.KEY_POINTS
                                && o != ObjectiveConfigType.LS_KNOWLEDGE_REVIEW
                                && o != ObjectiveConfigType.NEW_READ_RECITE
                                && o != ObjectiveConfigType.READ_RECITE_WITH_SCORE
                                && o != ObjectiveConfigType.NATURAL_SPELLING))
                .forEach(o -> result.addAll(findHomeworkProcessIdsByObjectiveConfigType(o)));
        return result;
    }

    /**
     * @param categoryId 练习类型
     * @param lessonId   我是一个很奇怪的属性
     * @return homework_process的id
     */
    public List<String> findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(String categoryId, String lessonId, ObjectiveConfigType objectiveConfigType) {

        if ((practices == null || practices.isEmpty()) || (!practices.containsKey(objectiveConfigType))) {
            return Collections.emptyList();
        } else {
            NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(objectiveConfigType);
            if (Objects.isNull(newHomeworkResultAnswer)) {
                return Collections.emptyList();
            }
            if ((newHomeworkResultAnswer.getAppAnswers() == null || newHomeworkResultAnswer.getAppAnswers().isEmpty()) || !newHomeworkResultAnswer.getAppAnswers().keySet().contains(categoryId + "-" + lessonId)) {
                return Collections.emptyList();
            } else {
                List<String> result = Collections.emptyList();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(categoryId + "-" + lessonId);
                if (newHomeworkResultAppAnswer.getAnswers() != null && !newHomeworkResultAppAnswer.getAnswers().isEmpty()) {
                    result = new LinkedList<>(newHomeworkResultAppAnswer.getAnswers().values());
                }
                return result;
            }
        }
    }

    /**
     * @param videoId 视频绘本ID
     * @return homework_process的id
     */
    public List<String> findHomeworkProcessIdsForKeyPointsByVideoId(String videoId) {

        if ((practices == null || practices.isEmpty()) || (!practices.containsKey(ObjectiveConfigType.KEY_POINTS))) {
            return Collections.emptyList();
        } else {
            NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(ObjectiveConfigType.KEY_POINTS);
            if (Objects.isNull(newHomeworkResultAnswer)) {
                return Collections.emptyList();
            }
            if ((newHomeworkResultAnswer.getAppAnswers() == null || newHomeworkResultAnswer.getAppAnswers().isEmpty())) {
                return Collections.emptyList();
            } else if (newHomeworkResultAnswer.getAppAnswers().containsKey(videoId)) {
                List<String> result = Collections.emptyList();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(videoId);
                if (newHomeworkResultAppAnswer.getAnswers() != null && !newHomeworkResultAppAnswer.getAnswers().isEmpty()) {
                    result = new LinkedList<>(newHomeworkResultAppAnswer.getAnswers().values());
                }
                return result;
            } else {
                return StringUtils.isEmpty(videoId) ? new LinkedList<>(newHomeworkResultAnswer.processAnswers().values()) : Collections.emptyList();
            }
        }
    }

    /**
     * ！！！这里会把answer&oralAnswer里的数据都返回
     * @param videoId 动画视频、情景口语ID
     * @return homework_process的id
     */
    public List<String> findHomeworkProcessIdsForVideoQuestionByVideoId(String videoId, ObjectiveConfigType objectiveConfigType) {

        if (MapUtils.isEmpty(practices) || !practices.containsKey(objectiveConfigType)) {
            return Collections.emptyList();
        } else {
            NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(objectiveConfigType);
            if (Objects.isNull(newHomeworkResultAnswer)) {
                return Collections.emptyList();
            }
            if (MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                return Collections.emptyList();
            } else if (newHomeworkResultAnswer.getAppAnswers().containsKey(videoId)) {
                List<String> result = new LinkedList<>();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(videoId);
                if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
                    result.addAll(newHomeworkResultAppAnswer.getAnswers().values());
                }
                if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getOralAnswers())) {
                    result.addAll(newHomeworkResultAppAnswer.getOralAnswers().values());
                }
                return result;
            } else {
                return new LinkedList<>(newHomeworkResultAnswer.processAnswers().values());
            }
        }
    }


    /**
     * @param questionBoxId 题包ID
     * @return homework_process的id
     */
    public List<String> findHomeworkProcessIdsForReadReciteByQuestionBoxId(String questionBoxId) {

        if (MapUtils.isEmpty(practices) || (!practices.containsKey(ObjectiveConfigType.NEW_READ_RECITE))) {
            return Collections.emptyList();
        } else {
            NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(ObjectiveConfigType.NEW_READ_RECITE);
            if (Objects.isNull(newHomeworkResultAnswer)) {
                return Collections.emptyList();
            }
            if (MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                return Collections.emptyList();
            } else if (newHomeworkResultAnswer.getAppAnswers().containsKey(questionBoxId)) {
                List<String> result = Collections.emptyList();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
                if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
                    result = new LinkedList<>(newHomeworkResultAppAnswer.getAnswers().values());
                }
                return result;
            } else {
                return new LinkedList<>(newHomeworkResultAnswer.processAnswers().values());
            }
        }
    }

    /**
     * @param questionBoxId 课文读背（打分）题包ID
     * @return homework_process的id
     */
    public List<String> findHomeworkProcessIdsForReadReciteWithScoreByQuestionBoxId(String questionBoxId, ObjectiveConfigType objectiveConfigType) {
        if (MapUtils.isEmpty(practices) || (!practices.containsKey(objectiveConfigType))) {
            return Collections.emptyList();
        } else {
            NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(objectiveConfigType);
            if (Objects.isNull(newHomeworkResultAnswer)) {
                return Collections.emptyList();
            }
            if (MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                return Collections.emptyList();
            } else if (newHomeworkResultAnswer.getAppAnswers().containsKey(questionBoxId)) {
                List<String> result = Collections.emptyList();
                NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(questionBoxId);
                if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
                    result = new LinkedList<>(newHomeworkResultAppAnswer.getAnswers().values());
                }
                return result;
            } else {
                return Collections.emptyList();
            }
        }
    }

    /**
     * 字词讲练
     */
    public List<String> findHomeworkProcessIdsForWordTeachByStoneDataId(String stoneDataId, ObjectiveConfigType objectiveConfigType, WordTeachModuleType wordTeachModuleType) {
        if (MapUtils.isEmpty(practices) || (!practices.containsKey(objectiveConfigType))) {
            return Collections.emptyList();
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(objectiveConfigType);
        if (Objects.isNull(newHomeworkResultAnswer)) {
            return Collections.emptyList();
        }
        if (MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
            return Collections.emptyList();
        }
        if (!newHomeworkResultAnswer.getAppAnswers().containsKey(stoneDataId)) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(stoneDataId);
        if (wordTeachModuleType == null || wordTeachModuleType.equals(WordTeachModuleType.WORDEXERCISE)) {
            if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
                result.addAll(newHomeworkResultAppAnswer.getAnswers().values());
            }
        }
        if (wordTeachModuleType == null || wordTeachModuleType.equals(WordTeachModuleType.IMAGETEXTRHYME)) {
            if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getImageTextRhymeAnswers())) {
                result.addAll(newHomeworkResultAppAnswer.getImageTextRhymeAnswers().values());
            }
        }
        if (wordTeachModuleType == null || wordTeachModuleType.equals(WordTeachModuleType.CHINESECHARACTERCULTURE)) {
            if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getChineseCourses())) {
                result.addAll(newHomeworkResultAppAnswer.getChineseCourses().values());
            }
        }
        return result;
    }

    /**
     * 字词讲练
     */
    public Map<String, List> findHomeworkProcessIdsForWordTeachByModuleType(ObjectiveConfigType objectiveConfigType, WordTeachModuleType wordTeachModuleType) {
        if (MapUtils.isEmpty(practices) || (!practices.containsKey(objectiveConfigType))) {
            return Collections.emptyMap();
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(objectiveConfigType);
        if (Objects.isNull(newHomeworkResultAnswer)) {
            return Collections.emptyMap();
        }
        if (MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers())) {
            return Collections.emptyMap();
        }

        Map<String, List> resultMap = new LinkedHashMap<>();
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
        for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : appAnswers.entrySet()) {
            String stoneDataId = entry.getKey();
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(stoneDataId);
            if (wordTeachModuleType.equals(WordTeachModuleType.WORDEXERCISE)) {
                LinkedList<String> resultIds = new LinkedList<>();
                if (MapUtils.isNotEmpty(newHomeworkResultAppAnswer.getAnswers())) {
                    resultIds.addAll(newHomeworkResultAppAnswer.getAnswers().values());
                    resultMap.put(stoneDataId, resultIds);
                }
            }
        }
        return resultMap;
    }

    /**
     * @param pictureBookId 绘本ID
     * @return homework_process的id
     */
    public List<String> findHomeworkProcessIdsForReading(String pictureBookId, ObjectiveConfigType objectiveConfigType) {

        if ((practices == null || practices.isEmpty()) || (!practices.containsKey(objectiveConfigType))) {
            return Collections.emptyList();
        } else {
            NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(objectiveConfigType);
            if (Objects.isNull(newHomeworkResultAnswer)) {
                return Collections.emptyList();
            }
            if (MapUtils.isEmpty(newHomeworkResultAnswer.getAppAnswers()) || !newHomeworkResultAnswer.getAppAnswers().containsKey(pictureBookId)) {
                return Collections.emptyList();
            } else {
                List<String> result = new ArrayList<>();
                NewHomeworkResultAppAnswer appAnswer = newHomeworkResultAnswer.getAppAnswers().get(pictureBookId);
                if (appAnswer.getAnswers() != null && !appAnswer.getAnswers().isEmpty()) {
                    // 绘本应试题部分
                    result.addAll(appAnswer.getAnswers().values());
                }
                if (MapUtils.isNotEmpty(appAnswer.getOralAnswers())) {
                    // 绘本跟读题部分
                    result.addAll(appAnswer.getOralAnswers().values());
                }
                return result;
            }
        }
    }

    /**
     * 获取某个作业类型下的所有processId
     *
     * @param objectiveConfigType 类型
     * @return 根据类型获得homeworkProcessIds
     */
    public List<String> findHomeworkProcessIdsByObjectiveConfigType(ObjectiveConfigType objectiveConfigType) {
        if ((practices == null || practices.isEmpty()) || (!practices.containsKey(objectiveConfigType))) {
            return Collections.emptyList();
        } else {
            NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(objectiveConfigType);
            if (Objects.isNull(newHomeworkResultAnswer)) {
                return Collections.emptyList();
            }
            LinkedList<String> processAnswers;
            if (objectiveConfigType.equals(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE)) {
                processAnswers = new LinkedList<>(newHomeworkResultAnswer.processWordTeachAnswers());
            } else {
                processAnswers = new LinkedList<>(newHomeworkResultAnswer.processAnswers().values());
            }
            return processAnswers;
        }
    }


    /**
     * 获取作业下的所有作业类型的processId
     *
     * @return 根据类型获得Map<qid, processId>
     */
    public Map<String, String> findHomeworkProcessIdsMap() {
        if (practices == null || practices.isEmpty()) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, String> processIdMap = new LinkedHashMap<>();
        for (NewHomeworkResultAnswer newHomeworkResultAnswer : practices.values()) {
            processIdMap.putAll(newHomeworkResultAnswer.processAnswers());
        }
        return processIdMap;

    }

    /**
     * 获取某个作业类型下的ProcessAnswersMap
     */
    public LinkedHashMap<String, String> findProcessAnswersMap(ObjectiveConfigType objectiveConfigType) {
        if (MapUtils.isEmpty(practices)|| (!practices.containsKey(objectiveConfigType))) {
            return new LinkedHashMap<>();
        } else {
            NewHomeworkResultAnswer newHomeworkResultAnswer = practices.get(objectiveConfigType);
            if (Objects.isNull(newHomeworkResultAnswer)) {
                return new LinkedHashMap<>();
            }
            return newHomeworkResultAnswer.processAnswers();
        }
    }

    public Integer processQuestionCount() {
        int questionCount = 0;
        if (practices != null) {
            questionCount = (int) practices.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> o.processAnswers().values())
                    .filter(Objects::nonNull)
                    .mapToLong(Collection::size)
                    .sum();
        }
        return questionCount;
    }

    /**
     * 只有在作业完成的情况下返回数据，不然返回是NUll，向下取整
     */
    public Integer processScore() {
        if (!isFinished())
            return null;
        Integer score = null;
        Integer totalScore = 0;
        int scoreCount = 0;
        if (practices != null && !practices.isEmpty()) {
            for (ObjectiveConfigType oct : practices.keySet()) {
                if (NewHomeworkConstants.NOT_SHOW_SCORE_TYPE.contains(oct))
                    continue;
                NewHomeworkResultAnswer na = practices.get(oct);
                if (na != null && na.processScore(oct) != null) {
                    scoreCount++;
                    totalScore += na.processScore(oct);
                }
            }
        }
        if (scoreCount > 0) {
            score = new BigDecimal(totalScore).divide(new BigDecimal(scoreCount), 0, BigDecimal.ROUND_DOWN).intValue();
        }
        return score;
    }

    /**
     * 纸质作业使用
     * 只有在作业完成的情况下返回数据，不然返回是NUll，向下取整
     */
    public Integer ocrHomeworkProcessScore() {
        if (!isFinished())
            return null;
        Integer score = null;
        Integer totalScore = 0;
        int scoreCount = 0;
        if (practices != null && !practices.isEmpty()) {
            for (ObjectiveConfigType oct : practices.keySet()) {
                NewHomeworkResultAnswer na = practices.get(oct);
                if (na != null && na.processScore(oct) != null) {
                    scoreCount++;
                    totalScore += na.processScore(oct);
                }
            }
        }
        if (scoreCount > 0) {
            score = new BigDecimal(totalScore).divide(new BigDecimal(scoreCount), 0, BigDecimal.ROUND_DOWN).intValue();
        }
        return score;
    }

    /**
     * 只有在作业完成的情况下返回数据，不然返回是NUll，向上取整返回，单位是秒
     */
    public Long processDuration() {
        if (!isFinished())
            return null;
        Long duration = 0L;
        if (practices != null && !practices.isEmpty()) {
            for (ObjectiveConfigType oct : practices.keySet()) {
                NewHomeworkResultAnswer na = practices.get(oct);
                duration += SafeConverter.toLong(na.processDuration());
            }
        }
        return duration;
    }

}
