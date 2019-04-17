/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.ObjectIdEntity;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StudentCenterController.
 *
 * @author Yaoheng Wu
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @since 2012-11-15
 */
@Controller
@RequestMapping("/student/center")
public class StudentCenterController extends AbstractController {
//    private static final int ELEMENTS_PER_PAGE = 10;

    @ImportService(interfaceClass = VerificationService.class)
    private VerificationService verificationService;

    @Inject private FinanceServiceClient financeServiceClient;

    // 2014暑期改版 -- 基本信息
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/center/index.vpage";
    }

    @RequestMapping(value = "information.vpage", method = RequestMethod.GET)
    public String infomation() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/center/information.vpage";
    }

//    // 2014暑期改版 -- 保存我的资料
//    @RequestMapping(value = "saveprofiledata.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage saveProfileData() {
//        User user = currentUser();
//        String strQq = getRequestParameter("qq", "");
//        String strGender = getRequestParameter("gender", "");
//        Integer year = getRequestInt("year", 0);
//        Integer month = getRequestInt("month", 0);
//        Integer day = getRequestInt("day", 0);
//        return userServiceClient.updateUserProfile(user, strQq, year, month, day, strGender);
//    }

    // 2014暑期改版 -- 账号安全
    @RequestMapping(value = "account.vpage", method = RequestMethod.GET)
    public String account() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/center/account.vpage";
    }

    // 2014暑期改版 -- 绑定手机 -- 发送验证码
    // 任务卡片中有调用，无法去掉
    @RequestMapping(value = "sendmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendMobileCode() {
        try {
            String mobile = getRequest().getParameter("mobile");
            return getSmsServiceHelper().sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.STUDENT_VERIFY_MOBILE_CENTER);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    // 2014暑期改版 -- 验证手机
    // 任务卡片中有调用，无法去掉
    @RequestMapping(value = "validatemobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitRebindMobile() {
        try {
            String code = getRequest().getParameter("latestCode");
            MapMessage mapMessage = verificationService.verifyMobile(currentUserId(), code, SmsType.STUDENT_VERIFY_MOBILE_CENTER.name());
            if (mapMessage.isSuccess() && currentUser().fetchUserType() == UserType.STUDENT && studentLoaderClient.isStudentFreezing(currentUser().getId())) {//冻结学生解冻时恢复冻结前班组
                studentServiceClient.freezeStudent(currentUserId(), false);
            }
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("手机验证失败");
        }
    }

    // 2014暑期改版 -- 忘记支付密码，重置支付密码页面
    @RequestMapping(value = "htmlchip/setpaymentpassword.vpage", method = RequestMethod.GET)
    public String setpaymentpassword() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/center/htmlchip/setpaymentpassword.vpage";
    }

    // 2014暑期改版 -- 我的学豆
    @RequestMapping(value = "integral.vpage", method = RequestMethod.GET)
    public String integral() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/center/integral.vpage";
    }

    @RequestMapping(value = "integralchip.vpage", method = RequestMethod.GET)
    public String integralChip(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/center/index.vpage";
    }

    // 2014暑期改版 -- 我的订单
    @RequestMapping(value = "order.vpage", method = RequestMethod.GET)
    public String order(Model model) {
        StudentDetail student = currentStudentDetail();
        //有未支付的订单　价格与产品不相等直接跳过
        Map<String, OrderProduct> productInfoMap = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(student)
                .stream()
                .collect(Collectors.toMap(ObjectIdEntity::getId, s -> s));
        List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderList(student.getId())
                .stream()
                .filter(order -> order.getOrderType() == OrderType.app
                        && order.getOrderStatus() != OrderStatus.Canceled
                        && productInfoMap.containsKey(order.getProductId())
                        && Math.abs(productInfoMap.get(order.getProductId()).getPrice().doubleValue() - order.getOrderPrice().doubleValue()) <= 1e-6)
                .collect(Collectors.toList());
        model.addAttribute("orders", userOrders);
        return "studentv3/center/order";
    }

    // 2014暑期改版 -- 我的充值
    @RequestMapping(value = "recharging.vpage", method = RequestMethod.GET)
    public String recharging(Model model) {
        Finance finance = financeServiceClient.getFinanceService()
                .loadUserFinance(currentUserId())
                .getUninterruptibly();
        model.addAttribute("balance", null == finance ? 0 : finance.getBalance());

        return "studentv3/center/recharging";
    }
}
