package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.meta.Subject;
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
@DocumentTable(table = "VOX_AFENTI_QUIZ_STAT_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161012")
public class AfentiQuizStat implements CacheDimensionDocument {
    private static final long serialVersionUID = 2662113155467337766L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC) private Long id;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    private Long userId;
    private String newBookId;
    private String newUnitId;
    private Integer silver; // 学豆奖励
    private Integer score; // 分数
    private Subject subject; // 学科

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"UID"}, new Object[]{userId}, new Object[]{null}),
        };
    }
}
