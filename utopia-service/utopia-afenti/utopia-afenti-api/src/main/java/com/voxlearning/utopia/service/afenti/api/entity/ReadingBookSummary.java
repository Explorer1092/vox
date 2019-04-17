package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * 绘本阅读统计(阅读量)摘要 、 实体
 * @author haitian.gan
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "reading_book_summary")
@UtopiaCacheRevision("20180313")
public class ReadingBookSummary implements CacheDimensionDocument{

    private static final long serialVersionUID = -8464405958384959168L;
    @DocumentId private String id;

    @DocumentField("bookid")     private String bookId;         // 绘本ID
    @DocumentField("usage_date") private String usageDate;      // 统计时间
    @DocumentField("hw_count")   private Long hwCount;          // 作业阅读量
    @DocumentField("app_count")  private Long appCount;         // 增值阅读量

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
