package com.voxlearning.utopia.mizar.controller.biz;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.service.data.MizarDataService;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 经营罗盘-门店概况 Controller
 * Created by yuechen.wang on 16/9/6.
 */
@Controller
@RequestMapping(value = "/biz/report")
public class BusinessReportController extends AbstractMizarController {

    @Inject private MizarDataService mizarDataService;

    // 基本信息首页列表
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {

        List<String> shopIds = currentUserShop();
        // 生效门店
        List<Map<String, Object>> userShop = mizarLoaderClient.loadShopByIds(shopIds).values().stream()
                .map(MizarShop::simpleInfo).collect(Collectors.toList());
        model.addAttribute("shop", userShop);
        String shopId = getRequestString("shopId");
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        model.addAttribute("shopId", shopId);
        Integer start = mizarDataService.parseDateParam("startDate", startDate, -8, model);
        Integer end = mizarDataService.parseDateParam("endDate", endDate, -1, model);
        if (start == null || end == null || start > end) {
            model.addAttribute("error", "开始时间不得早于结束时间");
            return "biz/report";
        }
        try {
            List<String> shopList;
            if (StringUtils.isBlank(shopId) || "all".equals(shopId)) {
                shopList = currentUserShop();
            } else {
                shopList = Collections.singletonList(shopId);
            }
            if (!currentUserShop().containsAll(shopList)) {
                model.addAttribute("error", "无效的机构信息");
            }
            MapMessage message = mizarDataService.queryShopData(shopList, start, end);
            if (message.isSuccess()) {
                model.addAttribute("result", message.get("result"));
            } else {
                model.addAttribute("error", message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("Failed load shop data: id={}, start={}, end={}", shopId, startDate, endDate, ex);
            model.addAttribute("error", "查询数据异常:" + ex.getMessage());
        }
        return "biz/report";
    }

}
