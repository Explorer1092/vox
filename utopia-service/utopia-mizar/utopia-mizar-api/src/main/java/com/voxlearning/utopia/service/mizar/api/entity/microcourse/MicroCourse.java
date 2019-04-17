package com.voxlearning.utopia.service.mizar.api.entity.microcourse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
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

/**
 * 微课堂-课程实体
 * Created by Wang Yuechen on 2016/12/08.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "vox_micro_course")
@UtopiaCacheRevision("20170308")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MicroCourse implements CacheDimensionDocument {

    private static final long serialVersionUID = 1L;

    @DocumentId private String id;
    private String name;        // 课程名称
    private String category;    // 课程分类
    private Double price;       // 课程价格
    private String btnContent;  // 按钮文字
    private Boolean payAll;     // 支持按课程购买
    private String tip;         // 备注提示
    private String qqTip;       // 加群提示
    private String qqUrl;       // 加群链接
    private Integer status;     // 课程状态, Enum MicroCourseStatus


    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;
    private Boolean disabled;   // 删除状态


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey("CAT", category)
        };
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

}
