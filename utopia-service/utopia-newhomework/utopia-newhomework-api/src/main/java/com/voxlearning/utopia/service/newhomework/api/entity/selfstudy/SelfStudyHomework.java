package com.voxlearning.utopia.service.newhomework.api.entity.selfstudy;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 1、这个数据目前只查1个月的，入口在阿包那，他写了一个索引，俺就直接按月份库了。
 * 2、自学作业是系统生成，并没有老师直接布置，所以自学作业没有teacherId。
 * 3、自学作业的id生成规则也有所区别，有点像假期作业。
 * 4、自学作业中的题，所有的题目默认给100分，只显示对错个数，无分值计算。
 *
 * @author xuesong.zhang
 * @since 2017/1/22
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "self_study_homework")
@DocumentIndexes({
        @DocumentIndex(def = "{'type':1}", background = true),
        @DocumentIndex(def = "{'sourceHomeworkId':1}", background = true),
        @DocumentIndex(def = "{'studentId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'createAt':-1}", background = true)
})
// 缓存4天
@UtopiaCacheExpiration(345600)
@UtopiaCacheRevision("20181130")
public class SelfStudyHomework extends BaseHomework implements Serializable {

    private static final long serialVersionUID = -6397061427800796268L;

    @DocumentId
    private String id;
    private Long studentId;             // 学生id
    @DocumentCreateTimestamp
    private Date createAt;              // 作业生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;              // 作业更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SelfStudyHomework.class, id);
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -3526286033287411550L;

        private String month;
        private String randomId = RandomUtils.nextObjectId();
        private Long studentId;

        public ID(String month, Long studentId) {
            this.month = month;
            this.studentId = studentId;
        }

        @Override
        public String toString() {
            return month + "_" + randomId + "_" + studentId;
        }
    }

    public SelfStudyHomework.ID parseID() {
        if (StringUtils.isBlank(id)) return null;
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 3) return null;
        String month = segments[0];
        String randomId = segments[1];
        Long studentId = SafeConverter.toLong(segments[2]);
        return new SelfStudyHomework.ID(month, randomId, studentId);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {

        private static final long serialVersionUID = -2022721167485126699L;

        private String id;
        private NewHomeworkType type;
        private String sourceHomeworkId;
        private Long studentId;
        private long clazzGroupId;
        private long createTime;
        private Subject subject;
        private Long duration;
    }

    public SelfStudyHomework.Location toLocation() {
        SelfStudyHomework.Location location = new SelfStudyHomework.Location();
        location.id = id;
        location.type = type == null ? NewHomeworkType.selfstudy : type;
        location.sourceHomeworkId = sourceHomeworkId;
        location.studentId = (studentId == null ? 0 : studentId);
        location.clazzGroupId = (clazzGroupId == null ? 0 : clazzGroupId);
        location.createTime = (createAt == null ? 0 : createAt.getTime());
        location.subject = subject;
        location.duration = (duration == null ? 0 : duration);
        return location;
    }

    /**
     * 获取指定作业形式下selfStudyHomework的原作业错题IDs
     * @return
     * @param type
     */
    public List<String> findSelfStudyNewHomeworkQuestionIds(ObjectiveConfigType type) {
        if (CollectionUtils.isEmpty(practices)) {
            return Collections.emptyList();
        }

        List<String> questionIdList = new ArrayList<>();
        NewHomeworkPracticeContent n = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.equals(type) || ObjectiveConfigType.ORAL_INTERVENTIONS.equals(type)) {
            if (CollectionUtils.isNotEmpty(n.getApps())) {
                n.getApps().forEach(p -> {
                    if (!NewHomeworkApp.DiagnosisSource.OcrDiagnosis.equals(p.getDiagnosisSource())) {
                        p.getErrorQuestions().stream().filter(Objects::nonNull).forEach(q -> questionIdList.add(q.getErrorQuestionId()));
                    }
                });
            }
        } else {
            if (CollectionUtils.isNotEmpty(n.getQuestions())) {
                n.getQuestions().forEach(q -> questionIdList.add(q.getQuestionId()));
            }
        }
        return questionIdList;
    }

    /**
     * 获取selfStudyHomework的原作业错题IDs
     * @return
     */
    public List<String> findSelfStudyNewHomeworkQuestionIds() {
        List<String> ids = new ArrayList<>();
        for(NewHomeworkPracticeContent npc : practices){
            ids.addAll(findSelfStudyNewHomeworkQuestionIds(npc.getType()));
        }
        return ids;
    }


    /**
     * 获取巩固学习或发音矫正课程对应原题list
     * @param type
     * @return Map<课程ID, 原题list>
     */
    public Map<String, List<ErrorQuestion>> findAppErrorQuestionsMap(ObjectiveConfigType type) {
        NewHomeworkPracticeContent n = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (n != null && CollectionUtils.isNotEmpty(n.getApps())) {
            return n.getApps().stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getErrorQuestions()))
                    .collect(Collectors.toMap(NewHomeworkApp::getCourseId, NewHomeworkApp::getErrorQuestions));
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * 获取英语巩固学习或发音矫正题包id对应原题list
     * @param type
     * @return Map<题包ID, 原题list>
     */
    public Map<String, List<ErrorQuestion>> findQuestionBoxAppErrorQuestionsMap(ObjectiveConfigType type) {
        NewHomeworkPracticeContent n = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (n != null && CollectionUtils.isNotEmpty(n.getApps())) {
            return n.getApps().stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getErrorQuestions()))
                    .collect(Collectors.toMap(NewHomeworkApp::getQuestionBoxId, NewHomeworkApp::getErrorQuestions));
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * 查询DIAGNOSTIC_INTERVENTIONS原题ID对应的课程ID
     * @return <原题ID, 课程ID>
     */
    public Map<String, String> findAppErrorQuestionCourseMap(NewHomeworkApp.DiagnosisSource diagnosisSource) {
        NewHomeworkPracticeContent n = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS);
        if (n != null && CollectionUtils.isNotEmpty(n.getApps())) {

            Map<String, String> errorQuestionCourseMap = new HashMap<>();
            n.getApps().stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getErrorQuestions()) && CollectionUtils.isNotEmpty(p.getQuestions()))
                    .filter(p -> (NewHomeworkApp.DiagnosisSource.SyncDiagnosis.equals(diagnosisSource) ? p.getDiagnosisSource() == null : Boolean.FALSE)
                            || p.getDiagnosisSource().equals(diagnosisSource))
                    .forEach(e -> e.getErrorQuestions().forEach(q -> errorQuestionCourseMap.put(q.getErrorQuestionId(), e.getCourseId())));
            return errorQuestionCourseMap;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * 获取诊断课程课程ID对应的后测题列表
     * @return Map<课程ID, 后测题list>
     * @param objectiveConfigType
     */
    public Map<String, List<NewHomeworkQuestion>> findCourseAppQuestionsMap(ObjectiveConfigType objectiveConfigType) {
        NewHomeworkPracticeContent n = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(objectiveConfigType);
        if (n != null && CollectionUtils.isNotEmpty(n.getApps())) {
            return n.getApps().stream()
                    .filter(p -> p.getCourseId() != null && CollectionUtils.isNotEmpty(p.getQuestions()))
                    .collect(Collectors.toMap(NewHomeworkApp::getCourseId, NewHomeworkApp::getQuestions));
        } else {
            return Collections.emptyMap();
        }
    }

    public Map<ObjectiveConfigType, Set<String>> findConfigTypeCourseIdMap() {
        Map<ObjectiveConfigType, Set<String>> configTypeCourseIdMap = new HashMap<>();
        for (NewHomeworkPracticeContent content : practices) {
            if (NewHomeworkConstants.COURSE_APP_CONFIGTYPE.contains(content.getType())) {
                configTypeCourseIdMap.put(content.getType(), content.getApps().stream().map(NewHomeworkApp::getCourseId).collect(Collectors.toSet()));
            }
        }
        return configTypeCourseIdMap;
    }

}
