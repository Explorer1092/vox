package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * AgentInvoiceProduct
 *
 * @author song.wang
 * @date 2016/9/6
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_INVOICE_PRODUCT")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20170320")
public class AgentInvoiceProduct extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 6792806355147230491L;

    private Long invoiceId;                  // 发货单ID
    private Integer productType;           // 产品类型
    private Long productId;                // 商品ID
    private Integer productQuantity;       // 商品数量
    private Boolean disabled;              // 禁用

    public static String ck_invoiceId(Long invoiceId) {
        return CacheKeyGenerator.generateCacheKey(AgentInvoiceProduct.class, "invoiceId", invoiceId);
    }

}
