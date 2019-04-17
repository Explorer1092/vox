package com.voxlearning.utopia.service.mizar.api.entity.order;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.ObjectIdEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by jiang wei on 2017/3/7.
 */
@Getter
@Setter
@DocumentTable(table = "VOX_PIC_ORDER_INFO")
@DocumentConnection(configName = "order")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170307")
public class PicOrderInfo extends ObjectIdEntityWithDisabledField {


    private static final long serialVersionUID = 5659824100385511327L;


    @UtopiaSqlColumn private String orderId;
    @UtopiaSqlColumn private Long userId;
    @UtopiaSqlColumn private String productName;
    @UtopiaSqlColumn private String paymentStatus;
    @UtopiaSqlColumn private String bookId;
    @UtopiaSqlColumn private BigDecimal payAmount;
    @UtopiaSqlColumn private Date serviceStartTime;
    @UtopiaSqlColumn private Date serviceEndTime;
    @UtopiaSqlColumn private Date orderCreateTime;

}
