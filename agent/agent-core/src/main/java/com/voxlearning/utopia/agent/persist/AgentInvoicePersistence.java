package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.constants.AgentLogisticsStatus;
import com.voxlearning.utopia.agent.persist.entity.AgentInvoice;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * AgentInvoicePersistence
 *
 * @author song.wang
 * @date 2016/9/7
 */
@Named
@UtopiaCacheSupport(AgentInvoice.class)
public class AgentInvoicePersistence extends AlpsStaticJdbcDao<AgentInvoice, Long> {
    @Override
    protected void calculateCacheDimensions(AgentInvoice source, Collection<String> dimensions) {

    }

    public List<AgentInvoice> findByStatus(AgentLogisticsStatus logisticsStatus){
        Criteria criteria = Criteria.where("LOGISTICS_STATUS").is(logisticsStatus);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }
    public List<AgentInvoice> findInvoiceList(Long invoiceId, String logisticsId, AgentLogisticsStatus logisticsStatus, Date startDate, Date endDate){

        Criteria criteria = new Criteria();
        if(invoiceId != null && invoiceId != 0){
            criteria.and("ID").is(invoiceId);
        }
        if(StringUtils.isNotBlank(logisticsId)){
            criteria.and("LOGISTICS_ID").is(logisticsId);
        }
        if(logisticsStatus != null){
            criteria.and("LOGISTICS_STATUS").is(logisticsStatus);
        }
        if(startDate != null || endDate != null){
            criteria.and("CREATE_DATETIME");
            if(startDate != null){
                criteria.gte(startDate);
            }
            if(endDate != null){
                criteria.lt(endDate);
            }
        }
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }
}
