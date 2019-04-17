package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "picture_book_order_cnt_new")
@UtopiaCacheRevision("20181123")
public class PicBookOrderStat implements CacheDimensionDocument{

    private static final long serialVersionUID = -5430959780328463330L;

    @DocumentId private String id;
    @DocumentField("bookid")     private String bookId;         // 绘本ID
    @DocumentField("usage_date") private String usageDate;      // 统计时间
    @DocumentField("order_count")private Long orderCount;       // 订单量
    @DocumentField("series_id")  private String seriesId;       // 系统ID
    @DocumentField("refund_order_count") private Long refundOrderCount; // 退款数量

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
