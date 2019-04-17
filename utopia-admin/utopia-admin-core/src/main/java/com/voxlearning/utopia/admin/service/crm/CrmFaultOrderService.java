package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.dao.CrmFaultOrderDao;
import com.voxlearning.utopia.admin.entity.CrmFaultOrder;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 *
 * User: qianxiaozhi
 * Date: 2017/2/17
 * Time: 15:01
 * crm用户问题跟踪工单
 */

@Named
public class CrmFaultOrderService {

    @Inject
    private CrmFaultOrderDao crmFaultOrderDao;

    /**
     * 新增工单
     *
     * @param crmFaultOrders
     */
    public void save(List<CrmFaultOrder> crmFaultOrders) {
        crmFaultOrderDao.inserts(crmFaultOrders);
    }

    /**
     * 关闭工单
     *
     * @param crmFaultOrder
     */
    public void close(CrmFaultOrder crmFaultOrder) {
        crmFaultOrder.setCloseTime(new Date());
        crmFaultOrderDao.upsert(crmFaultOrder);
    }

    /**
     * 查询工单数据
     *
     * @param userId
     * @param startDate
     * @param endDate
     * @param faultType
     * @param status
     * @param page
     * @param limit
     * @return
     */
    public Page<CrmFaultOrder> findByPage(Long userId, Date startDate, Date endDate,
                                          Integer faultType, Integer status, int page, int limit) {

        //如果开始时间为空 默认为昨天
        if (startDate == null) {
            startDate = DateUtils.addDays(new Date(), -1);
        }

        Pageable pageable = new PageRequest(page, limit);
        return crmFaultOrderDao.findByPage(pageable, userId, startDate, endDate, faultType, status);
    }

    /**
     * 获取工单记录
     *
     * @param id
     */
    public CrmFaultOrder load(String id) {
       return crmFaultOrderDao.load(id);
    }
}
