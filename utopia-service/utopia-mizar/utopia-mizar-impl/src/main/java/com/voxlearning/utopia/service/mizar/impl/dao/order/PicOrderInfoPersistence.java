package com.voxlearning.utopia.service.mizar.impl.dao.order;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.mizar.api.entity.order.PicOrderInfo;

import javax.inject.Named;
import java.util.*;

/**
 * Created by jiang wei on 2017/3/7.
 */
@Named
public class PicOrderInfoPersistence extends AlpsStaticJdbcDao<PicOrderInfo, String> {
    /**
     * Calculate cache dimensions based on specified document.
     *
     * @param document   the non null document.
     * @param dimensions put calculated result into this
     */
    @Override
    protected void calculateCacheDimensions(PicOrderInfo document, Collection<String> dimensions) {

    }


    public List<PicOrderInfo> getOrderCount(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }
        return RoutingPolicyExecutorBuilder.getInstance()
                .<List<PicOrderInfo>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    List<PicOrderInfo> resultList = new ArrayList<>();
                    Criteria criteria = Criteria.where("ORDER_CREATE_TIME").gte(startDate).lt(endDate);
//                    criteria.and("ORDER_CREATE_TIME").lte(endDate);
                    Query query = Query.query(criteria);
                    resultList.addAll(query(query));
                    return resultList;
                })
                .execute();
    }

    public Page<PicOrderInfo> getCurrentDayOrderDetailByPage(Date currentDate, Pageable pageable, String regex) {
        if (currentDate == null) {
            return null;
        }
        Date currentDateEnd = DateUtils.calculateDateDay(currentDate, 1);
        return RoutingPolicyExecutorBuilder.getInstance()
                .<Page<PicOrderInfo>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    List<PicOrderInfo> resultList = new ArrayList<>();
                    Criteria criteria = Criteria.where("ORDER_CREATE_TIME").gte(currentDate).lt(currentDateEnd);
                    criteria = criteria.and("DISABLED").is(Boolean.FALSE);
                    criteria = criteria.and("PRODUCT_NAME").like("%" + regex + "%");
                    Query query = Query.query(criteria);
                    return new PageImpl<>(query(query.with(pageable)), pageable, count(query));
                })
                .execute();
    }

    public List<PicOrderInfo> downloadCurrentDayOrderDetail(Date currentDate) {
        if (currentDate == null) {
            return Collections.emptyList();
        }
        Date currentDateEnd = DateUtils.calculateDateDay(currentDate, 1);
        return RoutingPolicyExecutorBuilder.getInstance()
                .<List<PicOrderInfo>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    List<PicOrderInfo> resultList = new ArrayList<>();
                    Criteria criteria = Criteria.where("ORDER_CREATE_TIME").gte(currentDate).lt(currentDateEnd);
                    criteria = criteria.and("DISABLED").is(Boolean.FALSE);
                    Query query = Query.query(criteria);
                    resultList.addAll(query(query));
                    return resultList;
                })
                .execute();
    }


    public void insertOrderInfo(PicOrderInfo picOrderInfo) {
        if (picOrderInfo == null) {
            return;
        }
        upsert(picOrderInfo);
    }
}
