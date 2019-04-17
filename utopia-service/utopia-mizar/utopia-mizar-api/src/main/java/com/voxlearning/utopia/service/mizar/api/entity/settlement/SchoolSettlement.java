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
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * SchoolSettlement
 *
 * @author song.wang
 * @date 2017/6/22
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_school_settlement")
@UtopiaCacheRevision("20161208")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class SchoolSettlement implements CacheDimensionDocument {

    private static final long serialVersionUID = 2876478158645123134L;
    @DocumentId
    private String id;

    private Long schoolId;                         // 学校ID
    private String schoolName;
    private Integer month;                         // 当前月份
    private Double totalAmount;                    // 本月交易额
    private Double refundAmount;                   // 本月退款金额
    private Integer orderCount;                    // 本月订单数
    private Integer refundOrderCount;              // 退单数
    private Double basicSettlementAmount;          // 本月应结算的交易基数
    private Double tmOrderAmortizeAmount;          // 本月交易且已消费金额
    private Double btmOrderAmortizeAmount;         // 历史交易本月已消费金额
    private Double tmOneTimeOrderExpenditure;      // 本月一次性消费交易金额
//    private Integer tmHwCount;                     // 本校老师本月布置作业次数
    private Double paymentRate;                    // 提成比例
    private Double payment;                        // 本月提成
    private Double refundAmortizeAmount;           // 退款摊销金额
    private Integer settlementDay;                 // 产生这条数据的结算日期，通常为每个月的最后一天，例如当前月份是5月份，采用哪一天的数据作为5月份的结算数据

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

    public void initData(){
        this.totalAmount = 0d;
        this.refundAmount = 0d;
        this.orderCount = 0;
        this.refundOrderCount = 0;
        this.basicSettlementAmount = 0d;
        this.tmOrderAmortizeAmount = 0d;
        this.btmOrderAmortizeAmount = 0d;
        this.tmOneTimeOrderExpenditure = 0d;
        this.payment = 0d;
        this.paymentRate = 0d;
        this.refundAmortizeAmount = 0d;
        this.disabled = false;
    }

}
