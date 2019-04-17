package com.voxlearning.utopia.service.mizar.api.entity.settlement;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 *
 * @author song.wang
 * @date 2017/6/22
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_school_refund_amortize_data")
@UtopiaCacheRevision("20161208")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class SchoolRefundAmortizeData implements CacheDimensionDocument {

    private static final long serialVersionUID = -1298100071211726435L;
    @DocumentId
    private String id;

    private Long schoolId;          // 学校ID
    private Integer month;          // 当前月份
    private Integer amortizeMonth;  // 退款订单摊销月份
    private Double amortizeAmount;  // 退款订单在摊销月份的摊销金额
    private Integer settlementDay;  // 产生这条数据的结算日期，通常为每个月的最后一天，例如当前月份是5月份，采用哪一天的数据作为5月份的结算数据

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Long createTime;
    @DocumentUpdateTimestamp
    private Long updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"sid", "month"}, new Object[]{schoolId, month})
        };
    }
}
