package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.statistics.PaymentDataSummary;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Alex on 15-1-16.
 */
@Named
public class PaymentDataSummaryDao extends StaticMongoDao<PaymentDataSummary, String> {

    @Override
    protected void calculateCacheDimensions(PaymentDataSummary source, Collection<String> dimensions) {
    }

    /**
     * 查询某段时间内相关区域的数据
     */
    public List<PaymentDataSummary> findPaymentDataSummaryWithRegionSort(Integer startDate, Integer endDate, Set<Integer> regions) {
        if (CollectionUtils.isEmpty(regions)) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.where("region_code").in(regions)
                .and("date").gte(startDate).lte(endDate)
                .and("status").is(1);
        Find find = Find.find(filter);
        find.with(new Sort(Sort.Direction.ASC, "region_code"));
        find.with(new Sort(Sort.Direction.DESC, "date"));
        return __find_OTF(find);
    }

    /**
     * 查询某段时间内相关区域的数据,降序排列
     */
    public List<PaymentDataSummary> findPaymentDataSummaryWithIncomeSort(Integer startDate, Integer endDate, Set<Integer> regions) {

        if (CollectionUtils.isEmpty(regions)) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.where("region_code").in(regions)
                .and("date").gte(startDate).lte(endDate)
                .and("status").is(1);
        Find find = Find.find(filter);
        find.with(new Sort(Sort.Direction.DESC, "total_order_amount"));
        return __find_OTF(find);
    }

}
