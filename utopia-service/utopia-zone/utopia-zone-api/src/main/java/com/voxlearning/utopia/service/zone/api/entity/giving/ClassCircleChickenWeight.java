package com.voxlearning.utopia.service.zone.api.entity.giving;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 活动记录
 * @author chensn
 * @date 2018-10-30 16:02
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_chicken_weight")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20181110")
public class ClassCircleChickenWeight implements Serializable {

    @DocumentId
    private String id;

    /**key 值 1：烤箱 2：托盘  3：鸡  4：助力次数**/
    private Map<String,Integer> configs;


}


