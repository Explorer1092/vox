package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by Summer Yang on 2016/7/26.
 */
@Named
@CacheBean(type = RewardLogistics.class)
public class RewardLogisticsPersistence extends StaticMySQLPersistence<RewardLogistics, Long> {

    @Override
    protected void calculateCacheDimensions(RewardLogistics document, Collection<String> dimensions) {
        dimensions.add(RewardLogistics.ck_id(document.getId()));
        dimensions.add(RewardLogistics.ck_receiver_type(document.getReceiverId(), document.getType()));
    }

    public List<RewardLogistics> loadRewardLogisticsBySchoolIdAndType(Long schoolId, RewardLogistics.Type type) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId)
                .and("TYPE").is(type);
        return query(Query.query(criteria));
    }

    public RewardLogistics loadRewardLogisticsBySchoolIdAndMonthAndType(Long schoolId, String month, RewardLogistics.Type type) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId)
                .and("MONTH").is(month)
                .and("TYPE").is(type);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public List<RewardLogistics> loadRewardLogisticsByMonthAndType(String currentMonth, RewardLogistics.Type type) {
        Criteria criteria = Criteria.where("MONTH").is(currentMonth)
                .and("TYPE").is(type);
        return query(Query.query(criteria));
    }

    public List<RewardLogistics> loadByMonth(String month) {
        Criteria criteria = Criteria.where("MONTH").is(month);
        return query(Query.query(criteria));
    }

    public List<RewardLogistics> loadRewardLogisticsByReceiverAndType(Long receiverId, RewardLogistics.Type type) {
        Criteria criteria = Criteria.where("RECEIVER_ID").is(receiverId)
                .and("TYPE").is(type)
                .and("CREATE_DATETIME").gte(DateUtils.addYears(new Date(), -1));
        return query(Query.query(criteria));
    }

    public RewardLogistics loadByReceiverIdAndTypeAndMonth(Long receiverId, RewardLogistics.Type type, String month) {
        Criteria criteria = Criteria.where("RECEIVER_ID").is(receiverId)
                .and("TYPE").is(type)
                .and("MONTH").is(month);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public Page<RewardLogistics> find(Pageable pageable, Criteria criteria) {
        Objects.requireNonNull(pageable);
        Objects.requireNonNull(criteria);

        Query query = Query.query(criteria).with(pageable);
        List<RewardLogistics> content = query(query);
        long total = count(Query.query(criteria));
        return new PageImpl<>(content, pageable, total);
    }
}
