package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.XiaoUOrderInfoService;
import com.voxlearning.utopia.service.vendor.api.mapper.XiaoUOrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/8
 */
@Controller
@RequestMapping(value = "/parentMobile/sell_xiaoU")
@Slf4j
public class MobileParentSellXiaoUPageController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = XiaoUOrderInfoService.class)
    private XiaoUOrderInfoService xiaoUOrderInfoService;

    private static final Map<OrderProductServiceType, String> PRODUCT_MAP;
    private static final Map<Integer, OrderProductServiceType> SUBJECT_MAP;

    static {
        PRODUCT_MAP = new HashMap<>();
        PRODUCT_MAP.put(OrderProductServiceType.AfentiExam, "小U英语");
        PRODUCT_MAP.put(OrderProductServiceType.AfentiChinese, "小U语文");
        PRODUCT_MAP.put(OrderProductServiceType.AfentiMath, "小U数学");
        PRODUCT_MAP.put(OrderProductServiceType.AfentiExamImproved, "小U英语");
        PRODUCT_MAP.put(OrderProductServiceType.AfentiChineseImproved, "小U语文");
        PRODUCT_MAP.put(OrderProductServiceType.AfentiMathImproved, "小U数学");
        SUBJECT_MAP = new HashMap<>();
        SUBJECT_MAP.put(Subject.CHINESE.getId(), OrderProductServiceType.AfentiChineseImproved);
        SUBJECT_MAP.put(Subject.MATH.getId(), OrderProductServiceType.AfentiMathImproved);
        SUBJECT_MAP.put(Subject.ENGLISH.getId(), OrderProductServiceType.AfentiExamImproved);
    }

    @RequestMapping(value = "/user_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getXiaoUOrderUser() {
        long sid = getRequestLong("sid");
        int subjectId = getRequestInt("subject_id");
        MapMessage mapMessage = MapMessage.successMessage();
        if (sid == 0L || subjectId == 0) {
            mapMessage.add("purchased", Boolean.FALSE);
        } else {
            AppPayMapper appPaidStatus = userOrderLoaderClient.getUserAppPaidStatus(SUBJECT_MAP.get(subjectId).name(), sid);
            mapMessage.add("purchased", appPaidStatus.isActive());
        }
        List<XiaoUOrderInfo> xiaoUOrderInfoList = xiaoUOrderInfoService.getXiaoUOrderInfoList();
        if (CollectionUtils.isEmpty(xiaoUOrderInfoList)) {
            return mapMessage.add("text_list", new ArrayList<>());
        }
        String textTemplate = "{0}{1}家长 购买了{2}";
        List<String> returnTextList = new ArrayList<>();
        List<Long> studentIds = xiaoUOrderInfoList.stream().map(XiaoUOrderInfo::getUserId).collect(Collectors.toList());
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);
        for (XiaoUOrderInfo orderInfo : xiaoUOrderInfoList) {
            StudentDetail studentDetail = studentDetailMap.get(orderInfo.getUserId());
            ExRegion exRegion = raikouSystem.loadRegion(studentDetail.getCityCode());
            String returnText = MessageFormat.format(textTemplate, exRegion.getCityName(), studentDetail.fetchRealnameIfBlankId(), PRODUCT_MAP.get(OrderProductServiceType.safeParse(orderInfo.getOrderProductServiceType())));
            returnTextList.add(returnText);
        }
        return mapMessage.add("text_list", returnTextList);
    }
}
