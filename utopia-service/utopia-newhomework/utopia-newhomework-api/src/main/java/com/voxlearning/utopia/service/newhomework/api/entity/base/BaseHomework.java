package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/11/24
 */
@Getter
@Setter
public class BaseHomework implements Serializable {

    private static final long serialVersionUID = 5466037441691992123L;

    public NewHomeworkType type;                                           // 作业类型
    public HomeworkTag homeworkTag;                                        // 作业标签
    public String sourceHomeworkId;                                        // 原作业id，和类题作业类型成对出现
    public SchoolLevel schoolLevel;                                        // 猜猜
    public Subject subject;                                                // 学科
    public String actionId;                                                // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
    public String title;                                                   // 作业名称
    public String des;                                                     // 预留，作业描述

    public Long teacherId;                                                 // 老师id，此处未来可能会变为fromUserId，布置作业的用户角色不会仅仅是老师
    public Long clazzGroupId;                                              // 班组id，有问题问长远

    public List<NewHomeworkPracticeContent> practices;                     // 作业内容
    public Long duration;                                                  // 标准时长（单位：秒）
    public String remark;                                                  // 备注
    public HomeworkSourceType source;                                      // 布置作业来源

    public Boolean checked;                                                // 是否检查
    public Date checkedAt;                                                 // 检查时间
    public HomeworkSourceType checkHomeworkSource;                         // 检查作业的端信息（大数据用）

    public Date startTime;                                                 // 作业起始时间
    public Date endTime;                                                   // 作业结束时间
    public Boolean disabled;                                               // 默认false，删除true
    public Boolean includeSubjective;                                      // 是否包含需要主观作答的试题

    // 目前扩展字段中有:
    // "isTermEnd" : "false"
    // "changeEndTime" : "true"
    public Map<String, String> additions;                                  // 扩展字段

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    @JsonIgnore
    public NewHomeworkType getNewHomeworkType() {
        return type == null ? NewHomeworkType.Normal : type;
    }

    /**
     * 判断是否已检查
     */
    @JsonIgnore
    public boolean isHomeworkChecked() {
        return Boolean.TRUE.equals(checked);
    }

    /**
     * 判断是否已过期
     */
    @JsonIgnore
    public boolean isHomeworkTerminated() {
        return isHomeworkChecked() || System.currentTimeMillis() > endTime.getTime();
    }

    /**
     * 判断是否为期末复习作业
     * 此方法仅适用于2016年的暑期前的那次期末复习
     * 新的期末复习请参考{@link com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag}.
     */
    @JsonIgnore
    @Deprecated
    public boolean isTermEnd() {
        if (additions == null || additions.isEmpty()) {
            return false;
        }
        String s = additions.get("isTermEnd");
        return "true".equalsIgnoreCase(s);
    }

    public List<String> findAllQuestionIds() {
        List<String> result = new LinkedList<>();
        practices.stream().filter(Objects::nonNull).forEach(n -> result.addAll(findQuestionIds(n.getType(), true)));
        return result;
    }

    /**
     * 取某一种作业形式下的所有试题id
     *
     * @param type 作业形式
     * @return List<Qid>
     */

    public List<String> findQuestionIds(ObjectiveConfigType type, Boolean includeReadingOral) {
        NewHomeworkPracticeContent newHomeworkPracticeContent = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (newHomeworkPracticeContent == null)
            return Collections.emptyList();
        return newHomeworkPracticeContent.processNewHomeworkQuestion(includeReadingOral)
                .stream()
                .filter(Objects::nonNull)
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());
    }

    /**
     * 基础作业类型下的所有应用形式
     *
     * @param type 作业形式
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkApp> findNewHomeworkApps(ObjectiveConfigType type) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 某种作业形式下的题目信息
     *
     * @param type 作业形式
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkQuestions(ObjectiveConfigType type) {
        NewHomeworkPracticeContent newHomeworkPracticeContent = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (newHomeworkPracticeContent == null)
            return Collections.emptyList();
        return newHomeworkPracticeContent.processNewHomeworkQuestion(false);
    }

    /**
     * 基础应用作业形式下的题目信息
     *
     * @param type       作业形式
     * @param lessonId   lessonId
     * @param categoryId 练习类型id
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkQuestions(ObjectiveConfigType type, String lessonId, Integer categoryId) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> Objects.equals(lessonId, o.getLessonId()) && Objects.equals(categoryId, o.getCategoryId()))
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 阅读绘本作业形式下的题目信息
     *
     * @param type          作业形式
     * @param pictureBookId 绘本id
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkQuestions(ObjectiveConfigType type, String pictureBookId) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> Objects.equals(pictureBookId, o.getPictureBookId()))
                .filter(o -> o.getQuestions() != null && !o.getQuestions().isEmpty())
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 阅读绘本作业形式下的口语题
     *
     * @param type          作业形式
     * @param pictureBookId 绘本id
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkOralQuestions(ObjectiveConfigType type, String pictureBookId) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> Objects.equals(pictureBookId, o.getPictureBookId()))
                .filter(o -> o.getOralQuestions() != null && !o.getOralQuestions().isEmpty())
                .map(NewHomeworkApp::getOralQuestions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 难重点专项作业形式的题
     *
     * @param type    作业形式
     * @param videoId 视频id
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkKeyPointQuestions(ObjectiveConfigType type, String videoId) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> Objects.equals(videoId, o.getVideoId()))
                .filter(o -> o.getQuestions() != null && !o.getQuestions().isEmpty())
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 课文读背（新）
     *
     * @param type          作业形式
     * @param questionBoxId 题包id
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkReadReciteQuestions(ObjectiveConfigType type, String questionBoxId) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> Objects.equals(questionBoxId, o.getQuestionBoxId()))
                .filter(o -> o.getQuestions() != null && !o.getQuestions().isEmpty())
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 趣味配音获取单个配音的题目
     */
    public List<NewHomeworkQuestion> findNewHomeworkDubbingQuestions(ObjectiveConfigType type, String dubbingId) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> Objects.equals(dubbingId, o.getDubbingId()))
                .filter(o -> o.getQuestions() != null && !o.getQuestions().isEmpty())
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * @param o type of ObjectiveConfigType
     * @return find NewHomeworkPracticeContent by the ObjectiveConfigType
     */
    public NewHomeworkPracticeContent findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType o) {
        if (CollectionUtils.isEmpty(practices)) {
            return null;
        }
        for (NewHomeworkPracticeContent n : practices) {
            if (n.getType() == o) {
                return n;
            }
        }
        return null;
    }

    /**
     * KV形式的作业结构
     *
     * @return LinkedHashMap
     */
    public LinkedHashMap<ObjectiveConfigType, NewHomeworkPracticeContent> findPracticeContents() {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkPracticeContent> resultMap = new LinkedHashMap<>();
        // 每种作业形式只存在一条记录
        for (NewHomeworkPracticeContent content : practices) {
            resultMap.put(content.getType(), content);
        }
        return resultMap;
    }


    public Map<ObjectiveConfigType, Set<String>> findEasyQuestionIds() {
        if (CollectionUtils.isEmpty(practices)) {
            return Collections.emptyMap();
        }
        Map<ObjectiveConfigType, Set<String>> map = new LinkedHashMap<>();
        for (NewHomeworkPracticeContent practice : practices) {
            Set<String> qids = new LinkedHashSet<>();
            map.put(practice.getType(), qids);
            if (CollectionUtils.isNotEmpty(practice.getApps())) {
                for (NewHomeworkApp app : practice.getApps()) {
                    if (CollectionUtils.isNotEmpty(app.getEasyQuestions())) {
                        qids.addAll(app.getEasyQuestions().stream().map(NewHomeworkQuestion::getQuestionId).filter(Objects::nonNull).collect(Collectors.toSet()));
                    }
                }
            }
        }
        return map;
    }
    public Map<ObjectiveConfigType, Set<String>> findHardQuestionIds() {
        if (CollectionUtils.isEmpty(practices)) {
            return Collections.emptyMap();
        }
        Map<ObjectiveConfigType, Set<String>> map = new LinkedHashMap<>();
        for (NewHomeworkPracticeContent practice : practices) {
            Set<String> qids = new LinkedHashSet<>();
            map.put(practice.getType(), qids);
            if (CollectionUtils.isNotEmpty(practice.getApps())) {
                for (NewHomeworkApp app : practice.getApps()) {
                    if (CollectionUtils.isNotEmpty(app.getHardQuestions())) {
                        qids.addAll(app.getHardQuestions().stream().map(NewHomeworkQuestion::getQuestionId).filter(Objects::nonNull).collect(Collectors.toSet()));
                    }
                }
            }
        }
        return map;
    }


    /**
     * 作业中需要主观作答部分Map，仅仅针对应试类型的作业形式
     * 返回结果List<NewHomeworkQuestion>中只有需要主观作答部分的题目
     *
     * @return LinkedHashMap
     */
    public LinkedHashMap<ObjectiveConfigType, List<NewHomeworkQuestion>> findIncludeSubjectiveQuestions() {
        LinkedHashMap<ObjectiveConfigType, List<NewHomeworkQuestion>> resultMap = new LinkedHashMap<>();
        if (Objects.equals(Boolean.TRUE, includeSubjective)) {
            practices.stream()
                    .filter(content -> Objects.equals(Boolean.TRUE, content.getIncludeSubjective()))
                    .forEach(content -> {
                        List<NewHomeworkQuestion> needIncludeSubjectives = content.getQuestions()
                                .stream()
                                .filter(NewHomeworkQuestion::isSubjectiveQuestion)
                                .collect(Collectors.toList());
                        if (needIncludeSubjectives != null && !needIncludeSubjectives.isEmpty()) {
                            resultMap.put(content.getType(), needIncludeSubjectives);
                        }
                    });
        }
        return resultMap;
    }

    /**
     * 作业预计用时（单位分钟，向上取整）
     */
    public long processDurationMinutes() {
        long duration = SafeConverter.toLong(getDuration());
        return (duration + 59) / 60;
    }
}
