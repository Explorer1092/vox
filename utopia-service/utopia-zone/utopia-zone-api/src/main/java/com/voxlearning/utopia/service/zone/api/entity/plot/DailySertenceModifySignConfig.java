package com.voxlearning.utopia.service.zone.api.entity.plot;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.zone.api.entity.WeekDailySentence;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 剧情每日奖励
 * @author dongfeng.xue
 * @date 2018-11-09
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_clazz_modify_sign_config")
public class DailySertenceModifySignConfig implements Serializable {
    private static final long serialVersionUID = -719121472149632677L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private Integer id;

    /** 1、活动 2、配音 3、绘本 4、小U语文 5、小U数学 6、小U英语**/
    private Integer signType;

    private String content;

    private Boolean isShow;

    /**跳转地址**/
    private String url;

    public static String ck_DailySertenceModifySignConfig() {
        return CacheKeyGenerator.generateCacheKey(DailySertenceModifySignConfig.class, "DailySertenceModifySignConfig");
    }
}
