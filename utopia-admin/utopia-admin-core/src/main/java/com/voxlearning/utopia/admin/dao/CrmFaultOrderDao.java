package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.admin.entity.CrmFaultOrder;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/2/17
 * Time: 14:14
 * crm用户问题跟踪工单
 */
@Named
public class CrmFaultOrderDao extends AlpsStaticMongoDao<CrmFaultOrder,String> {

    public void calculateCacheDimensions(CrmFaultOrder crmFaultOrder, Collection<String> ids){}

    /**
     * 读取工单记录
     * @param pageable 分页信息
     * @param userId  用户id
     * @param startDate 创建时间起
     * @param endDate   创建时间止
     * @param faultType 故障类型
     * @param status   工单状态
     * @return
     */
    public Page<CrmFaultOrder> findByPage(Pageable pageable, Long userId, Date startDate, Date endDate,
                                          Integer faultType, Integer status) {
        Criteria criteria = new Criteria();
        if (userId != null && userId != 0L) {
            criteria = criteria.and("userId").is(userId);
        }
        if (status!=null && status >= 0) {
            criteria = criteria.and("status").is(status);
        }

        if (faultType!=null && faultType > 0) {
            criteria = criteria.and("faultType").is(faultType);
        }

        if (startDate != null && endDate != null) {
            criteria = criteria.and("createTime").gte(startDate).lte(endDate);
        } else {
            if (startDate != null) {
                criteria = criteria.and("createTime").gte(startDate);
            }
            if (endDate != null) {
                criteria = criteria.and("createTime").lte(endDate);
            }
        }


        Query query = Query.query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "createTime"));
        return new PageImpl<>(query(query.with(pageable)), pageable, count(query));
    }
}
