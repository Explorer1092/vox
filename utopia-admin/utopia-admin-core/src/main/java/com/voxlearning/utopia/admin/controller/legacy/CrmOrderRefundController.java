package com.voxlearning.utopia.admin.controller.legacy;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.admin.service.legacy.CrmRefundService;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.order.api.constants.RefundHistoryStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderRefundHistory;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by Summer on 2017/11/2.
 */
@Controller
@RequestMapping("/legacy/order")
@NoArgsConstructor
@SuppressWarnings("deprecation")
public class CrmOrderRefundController extends AbstractAdminLegacyController {
    @Inject private CrmRefundService crmRefundService;

    @RequestMapping(value = "main.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String main(Model model) {
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 50);
        String outTradeId = getRequestString("outTradeId");
        String orderId = getRequestString("orderId");
        String status = getRequestParameter("status", RefundHistoryStatus.SUBMIT.name());
        String payMethod = getRequestParameter("payMethod", PaymentConstants.PaymentGatewayName_Wechat_StudentApp);
        Long userId = getRequestLong("userId");

        Page<OrderRefundHistory> pageContent = userOrderLoaderClient.loadRefundHistoryByPage(pageable, outTradeId, orderId, payMethod, status, userId);
        List<Map<String,Object>> refundMapperList = buildOrderRefundHistoryMapper(pageContent);

//        model.addAttribute("pageContent", pageContent);
        model.addAttribute("refundMapperList", refundMapperList);
        model.addAttribute("currentPage", pageContent.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", pageContent.getTotalPages());
        model.addAttribute("hasPrev", pageContent.hasPrevious());
        model.addAttribute("hasNext", pageContent.hasNext());
        model.addAttribute("orderId", orderId);
        model.addAttribute("outTradeId", outTradeId);
        model.addAttribute("payMethod", payMethod);
        model.addAttribute("status", status);
        model.addAttribute("refundStatus", RefundHistoryStatus.values());
        return "legacy/order/main";
    }

    private List<Map<String,Object>> buildOrderRefundHistoryMapper(Page<OrderRefundHistory> pageContent) {
        List<OrderRefundHistory> orderRefundHistoryList = pageContent.getContent();
        List<Map<String,Object>> refundList = new LinkedList<>();
        for(OrderRefundHistory temp : orderRefundHistoryList){
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("id",temp.getId());
            map.put("orderId",temp.getOrderId());
            map.put("oaOrderId",temp.getOaOrderId());
            map.put("userId",temp.getUserId());
            map.put("refundFee",temp.getRefundFee());
            map.put("status",temp.getStatus());
            map.put("code",temp.getCode());
            map.put("comment",temp.getComment());
            map.put("payMethod",temp.getPayMethod());
            map.put("createDatetime",temp.getCreateDatetime());
            map.put("updateDatetime",temp.getUpdateDatetime());
            //失败的订单并且小于201811月份的设置为灰色，并且不能点击
            Date tempDate = DateUtils.stringToDate("2018-11-01 00:00:00");
            map.put("buttonFlag","blue");
            if(temp.getStatus() == RefundHistoryStatus.FAIL) {
                if (tempDate.after(temp.getCreateDatetime())) {
                    map.put("buttonFlag", "gray");
                }
            }
            refundList.add(map);
        }
        return refundList;
    }

    // 支付宝 微信 批量退款
    @RequestMapping(value = "batchrefund.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchRefund() {
        String ids = getRequestString("ids");
        String comment = getRequestString("comment");
        if (StringUtils.isBlank(ids) || StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return AtomicLockManager.instance().wrapAtomic(crmRefundService)
                    .keyPrefix("taskService.batchRefund")
                    .keys(getCurrentAdminUser().getAdminUserName())
                    .proxy().batchRefundUserOrder(getCurrentAdminUser(), ids, comment);
        } catch (CannotAcquireLockException ex) {
            logger.error("CRM batch refund error npc {}: DUPLICATED OPERATION");
            return MapMessage.errorMessage("请稍后重试");
        }
    }

    // 手动修改任务
    @RequestMapping(value = "manualrefund.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage manualRefund() {
        String id = getRequestString("id");
        String oaOrderId = getRequestString("oaOrderId");
        String comment = getRequestString("comment");
        if (StringUtils.isBlank(id) || StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return AtomicLockManager.instance().wrapAtomic(crmRefundService)
                    .keyPrefix("taskService.batchRefund")
                    .keys(getCurrentAdminUser().getAdminUserName())
                    .proxy().manualRefund(getCurrentAdminUser(), id, comment,oaOrderId);
        } catch (CannotAcquireLockException ex) {
            logger.error("CRM refund error npc {}: DUPLICATED OPERATION");
            return MapMessage.errorMessage("请稍后重试");
        }
    }


}
