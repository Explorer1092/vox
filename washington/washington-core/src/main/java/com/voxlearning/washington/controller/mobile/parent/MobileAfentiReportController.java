package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2016/5/12.
 * 阿分题相关数据格式
 */
@Controller
@RequestMapping(value = "/parentMobile/afenti")
public class MobileAfentiReportController extends AbstractMobileController {

    // 阿芬提家长小报告实验
    @RequestMapping(value = "/loginreport.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loginReport() {
        Long parentId = currentUserId();
        // 双号不显示
        if (parentId == null) {
            return MapMessage.errorMessage();
        }
        Long studentId = getRequestLong("sid");
        if (studentId == 0) {
            return MapMessage.errorMessage();
        }
        String productType = getRequestString("productType");
        OrderProductServiceType productServiceType = OrderProductServiceType.safeParse(productType);
        if(productServiceType == null || productServiceType == OrderProductServiceType.Unknown){
            return MapMessage.errorMessage("错误的类型");
        }
        return afentiActivityServiceClient.loadLoginReportData(studentId, productServiceType);
    }
}
