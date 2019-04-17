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
@DocumentTable(table = "VOX_PRODUCT_TAG")
@UtopiaCacheRevision("20181112")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ProductTag extends AbstractDatabaseEntityWithDisabledField implements Serializable {

    @UtopiaSqlColumn private Long parentId;
    @UtopiaSqlColumn private Integer parentType;
    @UtopiaSqlColumn private String name;
    @UtopiaSqlColumn private Integer displayOrder;

    public enum ParentType{
        CATEGPRY(1),
        SET(2);
        private Integer type;
        ParentType(Integer type) {
            this.type = type;
        }
        public Integer getType() {
            return type;
        }
    }
}
