package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_user_order_ext")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190113")
public class ChipsUserOrderExt implements Serializable {

    @DocumentId
    private String id;//订单id
    private Long userId;
    private Long inviter;
    private String saleStaffId;//地推人员的id

    private String groupShoppingCode; //拼团码
    private Boolean sponsor;//是否是发起人
    private String newGroupCode;//新的拼团码

    private OrderStatus status;
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;

    public ChipsUserOrderExt(String id, Long userId) {
        this.id = id;
        this.userId = userId;
        this.createDate = new Date();
        this.updateDate = new Date();
        this.status = OrderStatus.CREATE;
    }

    public enum OrderStatus {
        CREATE, PAYED, REFUND
    }
}
