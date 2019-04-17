package com.voxlearning.utopia.service.mizar.api.entity.groupon;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by xiang.lv on 2016/10/17.
 * 商品专题
 *
 * @author xiang.lv
 * @date 2016/10/17   19:57
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_special_topic")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161017")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)

public class SpecialTopic implements Serializable {
    private static final long serialVersionUID = -2313077256732202395L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    private String name;                            // 专题名称
    private String position;                        // 专题位置 SpecialTopicPosition
    private Integer orderIndex;                     // 专题优先级 0~9,大值放前面
    private String coverImg;                        // 封面图
    private String detailImg;                       // 专题头图
    private Date  startTime;                         // 开始时间
    private Date  endTime;                           // 结束时间

    private String type;                            //外链outer_url或都详情to_detail

    @DocumentCreateTimestamp
    private Date createAt;                          // 创建时间
    
    @DocumentUpdateTimestamp
    private Date updateAt;                          // 更新时间
    private List<String> grouponGoodsIdList;        // 商品id 列表
    private String url;                             // 专题跳转url
    private String status;                          // 状态，ONLINE OFFLINE

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(SpecialTopic.class, id);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(SpecialTopic.class, "ALL");
    }
    public static String ck_active() {
        return CacheKeyGenerator.generateCacheKey(SpecialTopic.class, "active");
    }
}
