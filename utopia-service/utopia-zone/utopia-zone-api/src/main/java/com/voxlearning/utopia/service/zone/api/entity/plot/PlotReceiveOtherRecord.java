package com.voxlearning.utopia.service.zone.api.entity.plot;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户领取同班同学奖励
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_plot_receive_other_record_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181126")
public class PlotReceiveOtherRecord implements Serializable {
    private static final long serialVersionUID = -2817707488135114925L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id; // activityId_objectid
    private Integer activityId;
    private Long userId;
    private String orderId;   //订单id
    private Boolean isReceive; // 是否领取
    @DocumentCreateTimestamp
    private Date ct;

    public static String cacheKeyFromUserId(Integer activityId,Long userId) {
        return CacheKeyGenerator.generateCacheKey(PlotReceiveOtherRecord.class, new String[]{"activityId","userId"}, new Object[]{activityId,userId});
    }
    public static String cacheKeyFromUserIdAndOrderId(Integer activityId,Long userId,String orderId) {
        return CacheKeyGenerator.generateCacheKey(PlotReceiveOtherRecord.class, new String[]{"activityId","userId","orderId"}, new Object[]{activityId,userId,orderId});
    }
    public void generateId() {
        id = activityId + "_" + RandomUtils.nextObjectId();
    }

}
