package com.voxlearning.utopia.service.mizar.api.entity.shop;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer on 16/8/17.
 */
@Getter
@Setter
@DocumentTable(table = "VOX_O2O_RESERVE_RECORD")
@NoArgsConstructor
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160817")
@DocumentConnection(configName = "hs_misc")
public class MizarReserveRecord extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -8362786989454192497L;
    @UtopiaSqlColumn private Long parentId;
    @UtopiaSqlColumn private String callName;
    @UtopiaSqlColumn private Long studentId;
    @UtopiaSqlColumn private String studentName;
    @UtopiaSqlColumn private String mobile;
    @UtopiaSqlColumn private String email;
    @UtopiaSqlColumn private Long schoolId;
    @UtopiaSqlColumn private String shopId;
    @UtopiaSqlColumn private String shopGoodsId;
    @UtopiaSqlColumn private MizarReserveRecord.Status status;        // 预约状态
    @UtopiaSqlColumn private String notes;
    @UtopiaSqlColumn private Boolean disabled;
    @UtopiaSqlColumn private String orderId;
    @UtopiaSqlColumn private String address;
    @UtopiaSqlColumn private Integer age;
    @UtopiaSqlColumn private String school;
    @UtopiaSqlColumn private Integer clazzLevel;
    @UtopiaSqlColumn private String schoolArea;
    @UtopiaSqlColumn private Integer regionId;


    private static Map<Status, String> statusMap = new HashMap<>();

    static {
        statusMap.put(Status.New, "未接触");
        statusMap.put(Status.Payment, "已支付");
        statusMap.put(Status.Attach, "已联系");
        statusMap.put(Status.Access, "已到课");
        statusMap.put(Status.Success, "付费");
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(MizarReserveRecord.class, id);
    }

    public static String ck_parentId(Long parentId) {
        return CacheKeyGenerator.generateCacheKey(MizarReserveRecord.class, "parentId", parentId);
    }

    public static String ck_mobileAndShopId(String mobile, String shopId) {
        return CacheKeyGenerator.generateCacheKey(MizarReserveRecord.class, new String[]{"M", "SID"},
                new Object[]{mobile, shopId});
    }

    public static String ck_mobileAndgoodsId(String mobile, String goodsId) {
        return CacheKeyGenerator.generateCacheKey(MizarReserveRecord.class, new String[]{"M", "GID"},
                new Object[]{mobile, goodsId});
    }

    public static String ck_shopId(String shopId) {
        return CacheKeyGenerator.generateCacheKey(MizarReserveRecord.class, "shopId", shopId);
    }

    public static String ck_goodsId(String shopGoodsId) {
        return CacheKeyGenerator.generateCacheKey(MizarReserveRecord.class, "GID", shopGoodsId);
    }

    public static String ck_schoolIdAndShopId(Long schoolId, String shopId) {
        return CacheKeyGenerator.generateCacheKey(MizarReserveRecord.class, new String[]{"schoolId", "shopId"}, new Object[]{schoolId, shopId});
    }

    public enum Status {
        New,        // 未接触
        Payment,    // 已支付
        Success,    // 付费
        Attach,     // 已联系
        Access,     // 到课
    }

    public String fetchStatus() {
        if (statusMap.containsKey(status)) {
            return statusMap.get(status);
        }
        return "状态异常";
    }

    public static MizarReserveRecord.Status parseStatus(String status) {
        try {
            return MizarReserveRecord.Status.valueOf(status);
        } catch (Exception ignored) {
            return MizarReserveRecord.Status.New;
        }
    }

    public static Map<MizarReserveRecord.Status, String> fetchStatusMap() {
        return statusMap;
    }

    public static MizarReserveRecord mockInstance() {
        MizarReserveRecord mock = new MizarReserveRecord();
        mock.setParentId(1L);
        mock.setCallName("A");
        mock.setEmail("A");
        mock.setMobile("122");
        mock.setShopId("XXX");
        mock.setShopGoodsId("XXX");
        mock.setStatus(Status.New);
        return mock;
    }
}