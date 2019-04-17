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
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.MapUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-outside")
@DocumentCollection(collection = "outside_reading_result")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20181114")
public class OutsideReadingResult implements Serializable {
    private static final long serialVersionUID = 8351226536096473565L;

    @DocumentId
    private String id;                                                  // readingId + studentId

    private String readingId;                                           // 阅读id
    private String bookId;                                              // 教材id
    private Long studentId;                                             // 学生id
    private Map<String, OutsideReadingMissionResult> missionResults;    // 关卡答题结果
    private Date finishAt;
    @DocumentCreateTimestamp
    private Date createAt;                                              // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                                              // 更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(OutsideReadingResult.class, id);
    }

    public static String generateId(String readingId, Long studentId) {
        return readingId + "-" + studentId;
    }

    /**
     * 获取具体关卡做题结果
     * @param missionId 关卡ID
     * @return
     */
    @JsonIgnore
    public OutsideReadingMissionResult getMissionResult(String missionId) {
        if (MapUtils.isNotEmpty(missionResults)) {
            return missionResults.get(missionId);
        }
        return null;
    }

    /**
     * 获取missionResults
     * @return
     */
    @JsonIgnore
    public Map<String, OutsideReadingMissionResult> getNotNullMissionResults() {
        if (missionResults != null) {
            return missionResults;
        }
        return new LinkedHashMap<>();
    }

    /**
     * 课外阅读是否完成
     * @return
     */
    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }

    /**
     * 获取指定关卡answers
     * @param missionId
     * @return
     */
    @JsonIgnore
    public LinkedHashMap<String, String> getMissionAnswers(String missionId) {
        OutsideReadingMissionResult missionResult = getMissionResult(missionId);
        if (missionResult != null && MapUtils.isNotEmpty(missionResult.getAnswers())) {
            return missionResult.getAnswers();
        }
        return new LinkedHashMap<>();
    }
}
