package com.voxlearning.utopia.service.newhomework.api.entity.livecast;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-livecast-homework")
@DocumentCollection(collection = "livecast_homework_process_result_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170608")
public class LiveCastHomeworkProcessResult extends BaseHomeworkProcessResult implements CacheDimensionDocument {

    private static final long serialVersionUID = 2032619024877718L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;                      // 创建时间
    @DocumentUpdateTimestamp
    private Date updateAt;                      // 修改时间
    private String correctionImg;               // 批改图片
    private String correctionVoice;             // 批改语音
    private Double percentage;                  // 百分比信息


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"time", "randomId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -3090715369256694080L;

        private String randomId = RandomUtils.nextObjectId();
        private String time;

        public ID(Date createTime) {
            this.time = Long.toString(createTime.getTime());
        }

        @Override
        public String toString() {
            return randomId + "-" + time;
        }
    }
}
