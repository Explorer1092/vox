package com.voxlearning.utopia.service.reward.entity.newversion;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by kaibo.he on 18-10-10.
 */
@Getter
@Setter
@ToString
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_PRODUCT_SET")
@UtopiaCacheRevision("20181112")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ProductSet extends AbstractDatabaseEntityWithDisabledField implements Serializable {
    @UtopiaSqlColumn private String name;
    @UtopiaSqlColumn private Integer visible;
    @UtopiaSqlColumn private Boolean display;
    @UtopiaSqlColumn private Integer displayOrder;
}
