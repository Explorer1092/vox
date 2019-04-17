package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.TimestampTouchableEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 薯条班级
 */

@Data
@DocumentConnection(configName = "hs_chipsenglish")
@DocumentTable(table = "VOX_CHIPS_ACTIVITY_TRANSACTION_FLOW")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190306")
public class ChipsActivityTransactionFlow extends TimestampTouchableEntity {

    private static final long serialVersionUID = 3731712173319854811L;

    @UtopiaSqlColumn(
            name = "ID",
            primaryKey = true,
            primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC
    )
    @DocumentId(
            autoGenerator = DocumentIdAutoGenerator.AUTO_INC
    )
    private Long id;
    @UtopiaSqlColumn(name = "USER_ID")
    private Long userId;
    @UtopiaSqlColumn(name = "ACTIVITY_TYPE")
    private String activityType;
    @UtopiaSqlColumn(name = "AMOUNT")
    private Double amount;
    @UtopiaSqlColumn(name = "OPERATION")
    private Integer operation;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ChipsActivityTransactionFlow.class, id);
    }
}
