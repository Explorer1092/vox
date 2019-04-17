package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Ruib
 * @since 2016/10/12
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_QUIZ_RESULT_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161013")
public class AfentiQuizResult implements CacheDimensionDocument {
    private static final long serialVersionUID = -6240455554735758252L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC) private Long id;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    private Long userId;                // 用户ID
    private String newBookId;           // 课本ID
    private String newUnitId;           // 单元ID
    private String knowledgePoint;      // 知识点
    private String examId;              // 题目ID
    private Integer rightNum;           // 正确数
    private Integer errorNum;           // 错误数
    private Subject subject;             // 学科

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"UID", "NBID"}, new Object[]{userId, newBookId}, new Object[]{null, ""})
        };
    }

    public int increaseErrorNum() {
        errorNum = SafeConverter.toInt(errorNum) + 1;
        return errorNum;
    }
}
