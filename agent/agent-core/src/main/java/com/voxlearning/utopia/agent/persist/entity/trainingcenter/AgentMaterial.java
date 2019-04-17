package com.voxlearning.utopia.agent.persist.entity.trainingcenter;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 培训中心素材管理实体
 * @author xianlong.zhang
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_material")
@UtopiaCacheRevision("20180711")
public class AgentMaterial implements CacheDimensionDocument {
    private static final long serialVersionUID = -5651182691756185331L;
    @DocumentId
    private String id;
    private String title;//标题
    private String url;//上传视频url
    private String picUrl;//预览图url
    private Double fileSize;//文件大小   Mb
    private String introduction;//简介
    private Integer videoTime;//缩略图所在视频时间
    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id)       // 在ID上创建索引
        };
    }
}
