package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework-{}", dynamic = true)
@DocumentCollection(collection = "dubbing_synthetic_history")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20180201")
@UtopiaCachePrefix(prefix = "DubbingSyntheticHistory")
public class DubbingSyntheticHistory implements CacheDimensionDocument {
    private static final long serialVersionUID = 1599465678304575627L;

    @DocumentId
    private String id;      //yyyymm-homeworkId-userId-dubbingId
    private String videoUrl;//视频地址
    private String audioUrl;//音频地址
    private Boolean syntheticSuccess;//是否合成成功默认false
    private String path;//aliyun
    @DocumentCreateTimestamp
    private Date createAt;    //创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;    //更新时间

    @Data
    public static class ID implements Serializable {

        private static final long serialVersionUID = 6878710464886429451L;

        private String homeworkId;
        private Long userId;
        private String dubbingId;

        public ID(String homeworkId, Long userId, String dubbingId) {
            this.homeworkId = homeworkId;
            this.userId = userId;
            this.dubbingId = dubbingId;
        }

        @Override
        public String toString() {
            return homeworkId + "__" + userId + "__" + dubbingId;
        }
    }

    public static String generateCacheKey(String id) {
        return CacheKeyGenerator.generateCacheKey(DubbingSyntheticHistory.class, id);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    public DubbingSyntheticHistory.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        //这个地方不能用StringUtils.split()有bug，两个下划线分割的时候会按一个下划线分割
        String[] segments = id.split("__");
        if (segments.length != 3) return null;
        String homeworkId = segments[0];
        Long userId = SafeConverter.toLong(segments[1]);
        String dubbingId= SafeConverter.toString(segments[2]);
        return new DubbingSyntheticHistory.ID(homeworkId, userId, dubbingId);
    }

    public Boolean isSyntheticSuccess(Date homeworkCreateAt){
        if(homeworkCreateAt.before(NewHomeworkConstants.ALLOW_SHOW_DUBBING_VIDEO_START_TIME)){
            return false;
        }else {
            return syntheticSuccess;
        }
    }
}
