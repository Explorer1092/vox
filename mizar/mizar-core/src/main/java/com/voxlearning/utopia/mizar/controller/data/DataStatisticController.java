package com.voxlearning.utopia.mizar.controller.data;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.service.data.MizarDataService;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarRatingLoaderClient;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 经营概况-经营数据 Controller
 * Created by yuechen.wang on 16/9/6.
 */
@Controller
@RequestMapping(value = "/data/statistic")
public class DataStatisticController extends AbstractMizarController {

    @Inject private MizarRatingLoaderClient mizarRatingLoaderClient;
    @Inject private MizarDataService mizarDataService;

    // 基本信息首页列表
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String shopToken = getRequestString("shopToken");
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        model.addAttribute("shopToken", shopToken);
        Integer start = mizarDataService.parseDateParam("startDate", startDate, -8, model);
        Integer end = mizarDataService.parseDateParam("endDate", endDate, -1, model);
        if (start == null || end == null || start > end) {
            model.addAttribute("error", "开始时间不得早于结束时间");
            return "data/statistic";
        }
        try {
            List<String> shopList = new ArrayList<>();
            // 输入的是shopId
            if (ObjectId.isValid(shopToken)) {
                MizarShop shop = mizarLoaderClient.loadShopById(shopToken);
                if (shop != null) shopList.add(shop.getId());
            } else if (StringUtils.isNotBlank(shopToken)) {
                shopList = mizarRatingLoaderClient.loadShopByName(shopToken, -1)
                        .stream().map(MizarShop::getId).distinct().collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(shopList)) {
                return "data/statistic";
            }
            MapMessage message = mizarDataService.queryShopData(shopList, start, end);
            if (message.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> result = (List<Map<String, Object>>) message.get("result");
                model.addAttribute("result", splitList(result, 10));
                model.addAttribute("reserveTotal", result.stream().mapToInt(t -> SafeConverter.toInt(t.get("reserveCnt"))).sum());
                model.addAttribute("pvTotal", result.stream().mapToLong(t -> SafeConverter.toLong(t.get("showPv"))).sum());
            } else {
                model.addAttribute("error", message.getInfo());
            }
        } catch (Exception ex) {
            logger.error("Failed load shop data: token={}, start={}, end={}", shopToken, startDate, endDate, ex);
            model.addAttribute("error", "查询数据异常:" + ex.getMessage());
        }
        return "data/statistic";
    }

}
