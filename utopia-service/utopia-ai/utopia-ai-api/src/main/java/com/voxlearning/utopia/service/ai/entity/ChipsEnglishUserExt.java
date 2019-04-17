package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.ai.constant.ChipsEnglishLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 薯条用户额外信息  大数据写入
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_chips_english_user_ext")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 10800)
@UtopiaCacheRevision("20180918")
public class ChipsEnglishUserExt implements Serializable {

    @DocumentId
    private Long id;                 //用户id
    @DocumentField(value = "wxcode")
    @Deprecated
    private String wxCode;           //微信号
    @DocumentField(value = "studyduration")
    @Deprecated
    private String studyDuration;   //学习年限
    @DocumentField(value = "buycompetitor")
    @Deprecated
    private Boolean buyCompetitor;   //购买竞品
    @DocumentField(value = "level")
    @Deprecated
    private ChipsEnglishLevel level; //定级
    @DocumentField(value = "showplay")
    @Deprecated
    private Boolean showPlay;        //是否显示电子教材
    @DocumentField(value = "province")
    private String province;         //省份
    @DocumentField(value = "chipsconsume")
    private BigDecimal chipsConsume; //薯条总消费
    @DocumentField(value = "jztconsume")
    private BigDecimal jztConsume;   //家长通总消费
    @DocumentField(value = "lastactive")
    private String lastActive;         //最后活跃时间
    @DocumentField(value = "updatetime")
    private String updateTime;
    @DocumentUpdateTimestamp
    @DocumentField(value = "updateDate")
    private Date updateDate;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishUserExt.class, id);
    }

}
