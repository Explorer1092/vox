package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.AgentInvoiceProduct;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * AgentInvoiceProductPersistence
 *
 * @author song.wang
 * @date 2016/9/7
 */
@Named
@UtopiaCacheSupport(AgentInvoiceProduct.class)
public class AgentInvoiceProductPersistence extends AlpsStaticJdbcDao<AgentInvoiceProduct, Long> {
    @Override
    protected void calculateCacheDimensions(AgentInvoiceProduct source, Collection<String> dimensions) {
        dimensions.add(AgentInvoiceProduct.ck_invoiceId(source.getInvoiceId()));
    }

    public List<AgentInvoiceProduct> findByInvoiceId(Long invoiceId){
        Criteria criteria = Criteria.where("INVOICE_ID").is(invoiceId);
        criteria.and("DISABLED").is(false);
        return query(Query.query(criteria));
    }


}
