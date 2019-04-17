package com.voxlearning.utopia.service.newhomework.api.entity.outside;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-outside")
@DocumentCollection(collection = "outside_reading")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20181114")
public class OutsideReading implements Serializable {
    private static final long serialVersionUID = -8794260367395130536L;

    @DocumentId
    private String id;

    private Long teacherId;                             // 老师id
    private Long clazzGroupId;                          // 班组id
    public Subject subject;                             // 学科
    private String actionId;                            // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
    private OutsideReadingPractice practices;           // 阅读内容
    private Date endTime;                               // 阅读截止时间
    private Integer planDays;                           // 计划天数
    private Boolean disabled;                           // 默认false，删除true

    @DocumentCreateTimestamp
    private Date createAt;                              // 阅读生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                              // 阅读更新时间


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(OutsideReading.class, id);
    }

    public static String ck_clazzGroupId(Long clazzGroupId) {
        return CacheKeyGenerator.generateCacheKey(
                OutsideReading.class,
                new String[]{"CG"},
                new Object[]{clazzGroupId}
        );
    }

    /**
     * 获取关卡列表
     */
    public List<OutsideReadingMission> findMissions() {
        if (practices != null && CollectionUtils.isNotEmpty(practices.getMissions())) {
            return practices.getMissions();
        }
        return Collections.emptyList();
    }

    public String findBookId() {
        return practices != null ? practices.getBookId() : null;
    }

    /**
     * 根据missionId获取mission
     */
    public OutsideReadingMission findMissionById(String missionId) {
        if (practices == null || CollectionUtils.isEmpty(practices.getMissions())) {
            return null;
        }
        Map<String, OutsideReadingMission> missionMap = practices.getMissions()
                .stream()
                .collect(Collectors.toMap(OutsideReadingMission::getMissionId, o -> o, (o1, o2) -> o1));
        return missionMap.get(missionId);
    }

    /**
     * 获取关卡包含的客观题IDs
     */
    public List<String> findObjectiveQuestionIds(String missionId) {
        OutsideReadingMission mission = findMissionById(missionId);
        if (mission == null || CollectionUtils.isEmpty(mission.getQuestionIds())) {
            return Collections.emptyList();
        }
        return mission.getQuestionIds();
    }

    /**
     * 获取关卡包含的主观题IDs
     */
    public List<String> findSubjectiveQuestionIds(String missionId) {
        OutsideReadingMission mission = findMissionById(missionId);
        if (mission == null || CollectionUtils.isEmpty(mission.getSubjectiveQuestionIds())) {
            return Collections.emptyList();
        }
        return mission.getSubjectiveQuestionIds();
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    @JsonIgnore
    public LinkedHashMap<String, OutsideReadingMission> getMissionMap() {
        LinkedHashMap<String, OutsideReadingMission> missionMap = new LinkedHashMap<>();
        if (practices != null && CollectionUtils.isNotEmpty(practices.getMissions())) {
            for (OutsideReadingMission orm : getPractices().getMissions()) {
                missionMap.put(orm.getMissionId(), orm);
            }
        }
        return missionMap;
    }

    /**
     * 获取图书主观题ID列表
     */
    public List<String> findAllSubjectiveQuestionIds() {
        if (practices == null || CollectionUtils.isEmpty(practices.getMissions())) {
            return new LinkedList<>();
        }

        return practices.getMissions()
                .stream()
                .flatMap(o -> o.getSubjectiveQuestionIds().stream())
                .collect(Collectors.toList());
    }

    /**
     * 判断是否已过期
     */
    @JsonIgnore
    public boolean isTerminated() {
        return System.currentTimeMillis() > endTime.getTime();
    }
}
