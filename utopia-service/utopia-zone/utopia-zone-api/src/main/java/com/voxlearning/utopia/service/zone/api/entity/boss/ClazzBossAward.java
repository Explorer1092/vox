package com.voxlearning.utopia.service.zone.api.entity.boss;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author : kai.sun
 * @version : 2018-11-05
 * @description :
 **/

@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_clazz_boss_award")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20181030")
public class ClazzBossAward implements Serializable {

    private static final long serialVersionUID = -2088866323490733002L;

    public static int SELF = 0,CLAZZ=1;

    /** activityId + selfOrClazz +  type*/
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    @DocumentField("self_or_clazz")
    private Integer selfOrClazz; //维度的奖励 0：个人奖励 1：班级奖励 2 购买小u奖励

    @DocumentField("target_value")
    private Double targetValue;

    //类型 1 2 3
    @DocumentField
    private Integer type;

    @DocumentField("activity_id")
    private Integer activityId; //活.动id

    @DocumentField("box_pic")
    private String boxPic; //外面奖品图片

    @DocumentField("name")
    private String name; //外面名称

    @DocumentField
    private List<AwardDetail> awards;

    //生成主键id
    public void generateId() {
        id =activityId +"_"+ selfOrClazz + "_" +type;
    }

    //缓存key
    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ClazzBossAward.class, id);
    }

    public static String ck_clazzBossAwardList() {
        return CacheKeyGenerator.generateCacheKey(ClazzBossAward.class, "clazzBossAwardList");
    }

    public static String ck_clazzBossAwardList(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(ClazzBossAward.class, new String[]{"activityId"}, new Object[]{activityId});
    }

    public static String ck_clazzBossAwardList(Integer activityId,Integer selfOrClazz) {
        return CacheKeyGenerator.generateCacheKey(ClazzBossAward.class, new String[]{"activityId","selfOrClazz"}, new Object[]{activityId,selfOrClazz});
    }

}

