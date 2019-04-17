package com.voxlearning.utopia.service.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import com.voxlearning.utopia.service.ai.data.ChipsUserCourseMapper;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import lombok.Data;

import java.util.Date;


@Data
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_USER_COURSE")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190220")
public class ChipsUserCourse extends LongIdEntityWithDisabledField {
    @UtopiaSqlColumn(name = "USER_ID")
    private Long userId;
    @UtopiaSqlColumn(name = "ORDER_ID")
    private String orderId;
    @UtopiaSqlColumn(name = "PRODUCT_ID")
    private String productId;
    @UtopiaSqlColumn(name = "PRODUCT_ITEM_ID")
    private String productItemId;
    @UtopiaSqlColumn(name = "ORIGINAL_PRODUCT_ID")
    private String originalProductId;
    @UtopiaSqlColumn(name = "ORIGINAL_PRODUCT_ITEM_ID")
    private String originalProductItemId;
    @UtopiaSqlColumn(name = "SERVICE_BEGIN_DATE")
    private Date serviceBeginDate;
    @UtopiaSqlColumn(name = "SERVICE_END_DATE")
    private Date serviceEndDate;
    @UtopiaSqlColumn(name = "OPERATION")
    private Operation operation;
    @UtopiaSqlColumn(name = "ACTIVE")
    private Boolean active; //是否激活

    public enum Operation {
        CREATE, CHANGE
    }

    @JsonIgnore
    public String genUserOrderId() {
        UserOrder userOrder = new UserOrder();
        userOrder.setId(this.getOrderId());
        userOrder.setUserId(this.getUserId());
        return userOrder.genUserOrderId();
    }

    //缓存key
    public static String ck_user_id(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ChipsUserCourse.class, "UID", userId);
    }

    public static ChipsUserCourse initNewCourse(Long userId, String orderId, String productId, String productItemId, Date beginDate, Date endDate) {
        ChipsUserCourse chipsUserCourse = new ChipsUserCourse();
        chipsUserCourse.setUserId(userId);
        chipsUserCourse.setOrderId(orderId);
        chipsUserCourse.setProductId(productId);
        chipsUserCourse.setProductItemId(productItemId);
        chipsUserCourse.setOriginalProductItemId(productItemId);
        chipsUserCourse.setOriginalProductId(productId);
        chipsUserCourse.setServiceBeginDate(beginDate);
        chipsUserCourse.setServiceEndDate(endDate);
        chipsUserCourse.setDisabled(false);
        chipsUserCourse.setOperation(Operation.CREATE);
        chipsUserCourse.setActive(true);
        chipsUserCourse.setCreateTime(new Date());
        chipsUserCourse.setUpdateTime(new Date());
        return chipsUserCourse;
    }

}
