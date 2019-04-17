package com.voxlearning.utopia.service.mizar.api.entity.shop;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Summer Yang on 2016/8/26.
 * 目前只用于查询 没有和shop关联
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_trade_area")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161020")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarTradeArea implements Serializable {

    private static final long serialVersionUID = 8121653890465230622L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    @DocumentField("region_code") private Integer regionCode;                // 所属地区code  acode
    @DocumentField("region_name") private String regionName;                 // 所属地区名称   aname
    @DocumentField("trade_area") private String tradeArea;                   // 商圈名称

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

}
