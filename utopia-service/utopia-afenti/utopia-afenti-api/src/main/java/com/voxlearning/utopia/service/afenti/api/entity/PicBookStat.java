package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@DocumentConnection(configName = "mongo-newworld")
@DocumentDatabase(database = "vox-picbook")
@DocumentCollection(collection = "vox_picbook_stat")
@EqualsAndHashCode(of = "id")
@UtopiaCacheRevision("20180321")
public class PicBookStat implements CacheDimensionDocument{

    private static final long serialVersionUID = 8500139390475929487L;

    @DocumentId private String id;
    private String bookId;              // 绘本ID
    private Integer sales;              // 销量
    private Date lastBuyTime;           // 最近一次购买时间
    private Double weight;              // 权重

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }


}
