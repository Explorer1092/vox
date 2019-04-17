package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/1/10
 */
@Setter
@Getter
public class NewHomework implements Serializable {

    private static final long serialVersionUID = -8072627509570170297L;

    // 这个private别动
    private String id;
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
    public Boolean includeIntelligentTeaching;                             // 是否包含重点讲练测
    public Boolean remindCorrection;                                       // 是否已推荐巩固

    // 目前扩展字段中有:
    // "isTermEnd" : "false"
    // "changeEndTime" : "true"
    // "basicReviewPackageId" : ""
    public Map<String, String> additions;                                  // 扩展字段

    public Date createAt;                                                  // 作业生成时间
    public Date updateAt;                                                  // 作业更新时间

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

    @JsonIgnore
    public String getBasicReviewPackageId() {
        if (additions == null || additions.isEmpty()) {
            return null;
        }
        return additions.get("basicReviewPackageId");
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
        if (practices == null)
            return Collections.emptyList();
        NewHomeworkPracticeContent newHomeworkPracticeContent = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (newHomeworkPracticeContent == null)
            return Collections.emptyList();
        return newHomeworkPracticeContent.processNewHomeworkQuestion(false);
    }

    /**
     * 多种作业形式下的题目信息
     *
     * @param types 作业形式列表(为空则查询该作业下所有作业类型题目信息)
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkQuestions(Collection<ObjectiveConfigType> types) {
        if (practices == null) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(types)) {
            types = practices.stream().map(NewHomeworkPracticeContent::getType).collect(Collectors.toList());
        }
        List<NewHomeworkQuestion> questionList = new ArrayList<>();
        for (ObjectiveConfigType type : types) {
            questionList.addAll(findNewHomeworkQuestions(type));
        }
        return questionList;
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
     * 字词讲练
     * @param type
     * @param stoneDataId
     * @return
     */
    public List<NewHomeworkQuestion> findNewHomeworkWordTeachQuestions(ObjectiveConfigType type, String stoneDataId, WordTeachModuleType wordTeachModuleType) {
        List<NewHomeworkQuestion> newHomeworkQuestions = new LinkedList<>();
        if (wordTeachModuleType == null || wordTeachModuleType.equals(WordTeachModuleType.WORDEXERCISE)) {
            newHomeworkQuestions.addAll(practices.stream()
                    .filter(o -> Objects.equals(type, o.getType()))
                    .map(NewHomeworkPracticeContent::getApps)
                    .flatMap(Collection::stream)
                    .filter(o -> Objects.equals(stoneDataId, o.getStoneDataId()))
                    .filter(o -> o.getWordExerciseQuestions() != null && !o.getWordExerciseQuestions().isEmpty())
                    .map(NewHomeworkApp::getWordExerciseQuestions)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }
        if (wordTeachModuleType == null || wordTeachModuleType.equals(WordTeachModuleType.IMAGETEXTRHYME)) {
            newHomeworkQuestions.addAll(practices.stream()
                    .filter(o -> Objects.equals(type, o.getType()))
                    .map(NewHomeworkPracticeContent::getApps)
                    .flatMap(Collection::stream)
                    .filter(o -> Objects.equals(stoneDataId, o.getStoneDataId()))
                    .filter(o -> o.getImageTextRhymeQuestions() != null && !o.getImageTextRhymeQuestions().isEmpty())
                    .map(NewHomeworkApp::getImageTextRhymeQuestions)
                    .flatMap(Collection::stream)
                    .filter(q -> CollectionUtils.isNotEmpty(q.getChapterQuestions()))
                    .map(ImageTextRhymeHomework::getChapterQuestions)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }
        return newHomeworkQuestions;
    }

    /**
     * @param o type of ObjectiveConfigType
     * @return find NewHomeworkPracticeContent by the ObjectiveConfigType
     */
    public NewHomeworkPracticeContent findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType o) {
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
                        List<NewHomeworkQuestion> needIncludeSubjectives = content.processNewHomeworkQuestion(false)
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

    public NewHomework.Location toLocation() {
        NewHomework.Location location = new NewHomework.Location();
        location.id = id;
        location.type = type == null ? NewHomeworkType.Normal : type;
        location.teacherId = (teacherId == null ? 0 : teacherId);
        location.clazzGroupId = (clazzGroupId == null ? 0 : clazzGroupId);
        location.checked = Boolean.TRUE.equals(checked);
        location.createTime = (createAt == null ? 0 : createAt.getTime());
        location.checkedTime = (checkedAt == null ? 0 : checkedAt.getTime());
        location.startTime = (startTime == null ? 0 : startTime.getTime());
        location.endTime = (endTime == null ? 0 : endTime.getTime());
        location.actionId = actionId;
        location.subject = subject;
        location.includeSubjective = Boolean.TRUE.equals(includeSubjective);
        location.includeIntelligentTeaching = Boolean.TRUE.equals(includeIntelligentTeaching);
        location.remindCorrection = Boolean.TRUE.equals(remindCorrection);
        location.duration = (duration == null ? 0 : duration);
        location.disabled = (disabled == null ? false : disabled);
        return location;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {

        private static final long serialVersionUID = -2224669562890518197L;
        private String id;
        private NewHomeworkType type;
        private HomeworkTag homeworkTag;
        private long teacherId;
        private long clazzGroupId;
        private boolean checked;
        private long createTime;
        private long checkedTime;
        private long startTime;
        private long endTime;
        private String actionId;
        private Subject subject;
        private boolean includeSubjective;
        private boolean includeIntelligentTeaching;
        private boolean remindCorrection;
        private Long duration;
        private boolean disabled;
    }

    public List<NewHomeworkQuestionObj> processSubHomeworkResultAnswerIdsByObjectConfigType(Set<ObjectiveConfigType> objectiveConfigTypes) {
        if (practices == null) {
            return Collections.emptyList();
        } else {
            List<NewHomeworkQuestionObj> newHomeworkQuestionObjs = new ArrayList<>();
            for (ObjectiveConfigType objectiveConfigType : objectiveConfigTypes) {
                NewHomeworkPracticeContent practiceContent = findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
                if (practiceContent != null) {
                    doProcessSubHomeworkResultAnswerIdsByType(newHomeworkQuestionObjs, practiceContent);
                }
            }
            return newHomeworkQuestionObjs;
        }

    }

    private void doProcessSubHomeworkResultAnswerIdsByType(List<NewHomeworkQuestionObj> newHomeworkQuestionObjs, NewHomeworkPracticeContent practiceContent) {
        switch (practiceContent.getType()) {
            case BASIC_APP:
            case NATURAL_SPELLING:
            case LS_KNOWLEDGE_REVIEW:
                for (NewHomeworkApp app : practiceContent.getApps()) {
                    for (NewHomeworkQuestion question : app.getQuestions()) {
                        NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Arrays.asList(SafeConverter.toString(app.getCategoryId()), app.getLessonId()), question.getQuestionId());
                        newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                    }
                }
                break;
            case READING:
            case LEVEL_READINGS:
                for (NewHomeworkApp reading : practiceContent.getApps()) {
                    if (reading.getQuestions() != null) {
                        for (NewHomeworkQuestion question : reading.getQuestions()) {
                            NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.singletonList(reading.getPictureBookId()), question.getQuestionId());
                            newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                        }
                    }
                    if (reading.getOralQuestions() != null) {
                        for (NewHomeworkQuestion question : reading.getOralQuestions()) {
                            NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.singletonList(reading.getPictureBookId()), question.getQuestionId());
                            newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                        }
                    }
                }
                break;
            case KEY_POINTS:
                for (NewHomeworkApp keyPoint : practiceContent.getApps()) {
                    for (NewHomeworkQuestion question : keyPoint.getQuestions()) {
                        NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.singletonList(keyPoint.getVideoId()), question.getQuestionId());
                        newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                    }
                }
                break;
            case NEW_READ_RECITE:
            case READ_RECITE_WITH_SCORE:
            case WORD_RECOGNITION_AND_READING:
                for (NewHomeworkApp readRecite : practiceContent.getApps()) {
                    for (NewHomeworkQuestion question : readRecite.getQuestions()) {
                        NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.singletonList(readRecite.getQuestionBoxId()), question.getQuestionId());
                        newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                    }
                }
                break;
            case DUBBING:
            case DUBBING_WITH_SCORE:
                for (NewHomeworkApp dubbing : practiceContent.getApps()) {
                    for (NewHomeworkQuestion question : dubbing.getQuestions()) {
                        NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.singletonList(dubbing.getDubbingId()), question.getQuestionId());
                        newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                    }
                }
                break;

            case DIAGNOSTIC_INTERVENTIONS:
                for (NewHomeworkApp course : practiceContent.getApps()) {
                    for (NewHomeworkQuestion question : course.getQuestions()) {
                        NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.singletonList(course.getCourseId()), question.getQuestionId());
                        newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                    }
                }
                break;
            case OCR_MENTAL_ARITHMETIC:
                break;
            case WORD_TEACH_AND_PRACTICE:
                for (NewHomeworkApp newHomeworkApp : practiceContent.getApps()) {
                    if (CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions())) {
                        for (NewHomeworkQuestion question : newHomeworkApp.getWordExerciseQuestions()) {
                            NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.singletonList(newHomeworkApp.getStoneDataId()), question.getQuestionId());
                            newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(newHomeworkApp.getImageTextRhymeQuestions())) {
                        for (ImageTextRhymeHomework imageTextRhymeHomework : newHomeworkApp.getImageTextRhymeQuestions()) {
                            if (CollectionUtils.isNotEmpty(imageTextRhymeHomework.getChapterQuestions())) {
                                for (NewHomeworkQuestion question : imageTextRhymeHomework.getChapterQuestions()) {
                                    NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.singletonList(newHomeworkApp.getStoneDataId()), question.getQuestionId());
                                    newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                                }
                            }
                        }
                    }
                    if (CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds())) {
                        for (String courseId : newHomeworkApp.getChineseCharacterCultureCourseIds()) {
                            NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.singletonList(newHomeworkApp.getStoneDataId()), courseId);
                            newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                        }
                    }
                }
                break;
            case ORAL_COMMUNICATION:
                break;
            default:
                for (NewHomeworkQuestion question : practiceContent.getQuestions()) {
                    NewHomeworkQuestionObj newHomeworkQuestionObj = new NewHomeworkQuestionObj(this.id, practiceContent.getType(), Collections.emptyList(), question.getQuestionId());
                    newHomeworkQuestionObjs.add(newHomeworkQuestionObj);
                }
        }
    }

    /**
     * 返回结果List<NewHomeworkQuestionObj> 用于组装答题中间结果试题明细数据
     *
     * @return LinkedHashMap
     */
    public List<NewHomeworkQuestionObj> processSubHomeworkResultAnswerIds() {
        List<NewHomeworkQuestionObj> newHomeworkQuestionObjs = new ArrayList<>();
        for (NewHomeworkPracticeContent practiceContent : practices) {
            doProcessSubHomeworkResultAnswerIdsByType(newHomeworkQuestionObjs, practiceContent);
        }
        return newHomeworkQuestionObjs;
    }


    public Map<ObjectiveConfigType, List<NewHomeworkQuestionObj>> processSubHomeworkResultAnswerIds(Set<ObjectiveConfigType> objectiveConfigTypes) {
        Map<ObjectiveConfigType, List<NewHomeworkQuestionObj>> newHomeworkQuestionObjMap = new LinkedHashMap<>();
        for (NewHomeworkPracticeContent practiceContent : practices) {
            if (objectiveConfigTypes.contains(practiceContent.getType())) {
                List<NewHomeworkQuestionObj> newHomeworkQuestionObjs = new ArrayList<>();
                doProcessSubHomeworkResultAnswerIdsByType(newHomeworkQuestionObjs, practiceContent);
                newHomeworkQuestionObjMap.put(practiceContent.getType(), newHomeworkQuestionObjs);
            }
        }
        return newHomeworkQuestionObjMap;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewHomeworkQuestionObj implements Serializable {
        private static final long serialVersionUID = -8555141955029998031L;
        private String hid;                 // 作业id
        private ObjectiveConfigType type;   // 作业形式
        private List<String> joinKey;       // 组合key，基础训练（category+lesson）;绘本（阅读绘本和视频绘本）
        private String questionId;          // 题目id

        public String generateSubHomeworkResultAnswerId(String day, Long userId) {
            SubHomeworkResultAnswer.ID aid = new SubHomeworkResultAnswer.ID();
            aid.setDay(day);
            aid.setHid(getHid());
            aid.setJoinKeys(getJoinKey());
            aid.setType(getType());
            aid.setUserId(SafeConverter.toString(userId));
            aid.setQuestionId(getQuestionId());
            return aid.toString();
        }
    }

    public List<String> getReportShareParts() {
        if (!isHomeworkChecked()) {
            return Collections.emptyList();
        }
        if (additions == null || additions.isEmpty()) {
            return Collections.emptyList();
        }
        String reportShareParts = additions.get("reportShareParts");
        if (reportShareParts == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(reportShareParts.split("_"));
    }
}
