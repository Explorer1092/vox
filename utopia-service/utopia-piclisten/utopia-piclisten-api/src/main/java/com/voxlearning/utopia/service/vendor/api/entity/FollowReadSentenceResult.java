package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 跟读打分结果
 *
 * @author jiangpeng
 * @since 2017-03-10 下午3:20
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "follow_read_sentence_result_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 30)
public class FollowReadSentenceResult implements CacheDimensionDocument {


    private static final long serialVersionUID = -5899323083106481631L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    private String picListenId;
    private String unitId;
    private Long sentenceId;
    private Long studentId;
    private Integer recordTime; // 录音时长 毫秒
    private String audioUrl;
    private ScoreResult scoreResult;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
            newCacheKey(id),newCacheKey("SID", studentId)
        };
    }


    @DocumentFieldIgnore
    public Double fetchSentenceScore(){
        return scoreResult == null ? 0:scoreResult.getScore();
    }


    @Getter
    @Setter
    @ToString
    public static class ScoreResult implements Serializable {

        private static final long serialVersionUID = -1910085499151387864L;
        private String sample;
        private String usertext;
        private Float begin;
        private Float end;
        private Double score;
        private Float fluency;
        private Float integrity;
        private Float pronunciation;
        private List<Word> words;
    }


    @Getter
    @Setter
    @ToString
    public static class Word implements Serializable {
        private static final long serialVersionUID = -5885353634019902732L;
        private String text;
        private Integer type;
        private Float score;
        private List<Map<String, Object>> subwords;
    }

    @Getter
    @Setter
    public static class UselessWrapper implements Serializable {

        private static final long serialVersionUID = 8611728307454861338L;
        private String version;
        private List<ScoreResult> lines;
    }



    public FollowReadSentenceResult generateId(){
        Objects.requireNonNull(studentId);
        id = studentId + "-" + RandomUtils.nextObjectId();
        return this;
    }
}
