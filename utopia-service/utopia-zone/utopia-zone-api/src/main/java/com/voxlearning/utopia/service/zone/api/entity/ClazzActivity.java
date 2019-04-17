package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 班级圈活动
 * @author chensn
 * @date 2018-10-29 20:13
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_activity")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20181030")
public class ClazzActivity implements Serializable {
    private static final long serialVersionUID = -7191218722149632677L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private Integer id;
    /**
     * 活动类型 1.万圣节
     */
    private Integer type;
    /**
     * 活动图片
     */
    private String pic;
    /**
     * 按钮名称
     */
    private String button;
    /**
     * 跳转url模型
     */
    private String targetPattern;
    /**
     * 活动名称
     */
    private String name;
    /**
     * 展示类型  0.纯文本 1.活动排名 2.参与人数
     */
    private Integer showType;
    private String showText;
    private Date startDate;
    private Date endDate;
    private Boolean isShow;
    /**
     * 0.纯文本  1.班级排名 2.班级参与人数
     */
    private Integer contentType;
    private String content;
    private Integer sort;
    /**
     * 是否展示排行榜
     */
    private Boolean isShowRank;
    /**
     * 排行榜名字
     */
    private String rankName;
    /**
     * 排行榜类型
     */
    private Integer rankType;
    private String rankDiscription;
    /**
     * 最少参与人数
     */
    private Long lowActivityNum;


    public String ck_last_activity() {
        return CacheKeyGenerator.generateCacheKey(ClazzActivity.class, "findTheLastActivity");
    }

    public String ck_uesd_activity() {
        return CacheKeyGenerator.generateCacheKey(ClazzActivity.class, "findUsedActivity");
    }

}
