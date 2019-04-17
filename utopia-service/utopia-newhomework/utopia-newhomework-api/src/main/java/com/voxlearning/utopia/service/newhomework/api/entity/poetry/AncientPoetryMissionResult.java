package com.voxlearning.utopia.service.newhomework.api.entity.poetry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-poetry")
@DocumentCollection(collection = "poetry_mission_result")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20190412")
public class AncientPoetryMissionResult implements Serializable {
    private static final long serialVersionUID = 5835448161900983370L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                                                   // activityId-missionId-studentId
    private Long studentId;                                              // 学生ID
    private String activityId;                                           // 活动ID
    private String missionId;                                            // 关卡ID
    private List<Date> modelFinishAt;                                    // 各个模块完成时间
    private List<String> studentAudioUrls;                               // 学生每日朗读音频地址url
    private LinkedHashMap<String, AncientPoetryProcessResult> answers;
    private Double star;
    private Date finishAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    // 以下字段只有家长助力关卡用到
    private List<String> parentAudioUrls;                                // 家长诵读音频url
    private Long parentId;                                               // 助力家长ID

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AncientPoetryMissionResult.class, id);
    }

    public static String generateId(String activityId, String missionId, Long studentId, boolean isParentMission) {
        if (isParentMission) {
            return StringUtils.join(activityId, "-", studentId);
        } else {
            return StringUtils.join(activityId, "-", missionId, "-", studentId);
        }
    }

    /**
     * 关卡是否完成
     * @return
     */
    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }

    /**
     * 家长助力关卡是否完成
     * @return
     */
    @JsonIgnore
    public boolean isParentMissionFinished() {
        return CollectionUtils.isNotEmpty(parentAudioUrls) && finishAt != null;
    }
}
