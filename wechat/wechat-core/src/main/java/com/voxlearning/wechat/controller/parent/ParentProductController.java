package com.voxlearning.wechat.controller.parent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.wechat.controller.AbstractParentWebController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author Xin Xin
 * @since 11/2/15
 */
@Controller
@RequestMapping(value = "/parent/product")
public class ParentProductController extends AbstractParentWebController {

    @RequestMapping(value = "/list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        // 跳转到家长app的列表页 目前共用一套页面逻辑 这里的cookie是前端在切换孩子的时候放进去的，有效期1个小时
        String sidStr = getRequestContext().getCookieManager().getCookie("ssid", "");
        Long sid = 0L;
        if (StringUtils.isNotBlank(sidStr)) {
            sid = SafeConverter.toLong(sidStr);
        } else {
            // 查询家长默认第一个孩子
            List<User> children = studentLoaderClient.loadParentStudents(getRequestContext().getUserId());
            if (CollectionUtils.isNotEmpty(children)) {
                sid = children.get(0).getId();
            }
        }
        return "redirect:" + ProductConfig.getMainSiteBaseUrl() + "/parentMobile/ucenter/shoppinginfolist.vpage?sid=" + sid;
    }

    // 映射到家长app产品购买页面
    @RequestMapping(value = "/shoppinginfo.vpage", method = RequestMethod.GET)
    public String shoppingInfo() {
        String productType = getRequestString("productType");
        long sid = getRequestLong("sid");
        if (sid == 0l) {
            List<User> children = studentLoaderClient.loadParentStudents(getRequestContext().getUserId());
            if (CollectionUtils.isNotEmpty(children)) {
                sid = children.get(0).getId();
            }
        }
        if (StringUtils.isEmpty(productType)) {
            // 参数不正确无法进入详情页，直接跳回到列表页
            return "redirect:" + ProductConfig.getMainSiteBaseUrl() + "/parentMobile/ucenter/shoppinginfolist.vpage?sid=" + sid;
        }

        // 洛亚传说 三国 关付费 挂公告
        if (StringUtils.equals(productType, A17ZYSPG.name()) || StringUtils.equals(productType, SanguoDmz.name())
                || StringUtils.equals(productType, PetsWar.name()) || StringUtils.equals(productType, TravelAmerica.name())) {
            String url = ProductConfig.getMainSiteBaseUrl() + "/project/appoffline.vpage?type=" + productType;
            return "redirect:" + url;
        }
        // 跳转到app
        return "redirect:" + ProductConfig.getMainSiteBaseUrl() + "/parentMobile/ucenter/shoppinginfo.vpage?sid=" + sid + "&productType=" + productType;

    }

    //FIXME:这里是不是已经废弃了？现有的支付购买已经是走家长app的h5页面了 2017-03-02
    @RequestMapping(value = "/info-{type}.vpage", method = RequestMethod.GET)
    public String productInfo(@PathVariable String type, Model model) {
        String productType;
        if (StringUtils.isEmpty(type)) {
            return redirectWithMsg("未知产品类型", model);
        }
        try {
            productType = transferProductType(type);
            if (StringUtils.isBlank(productType)) {
                return redirectWithMsg("未知产品类型", model);
            }
            Long sid = getRequestLong("sid");
            if (sid == 0L) {
                // 查询家长默认第一个孩子
                List<User> children = studentLoaderClient.loadParentStudents(getRequestContext().getUserId());
                if (CollectionUtils.isNotEmpty(children)) {
                    sid = children.get(0).getId();
                }
            }

            // 洛亚传说 三国 关付费 挂公告
            if (StringUtils.equals(productType, A17ZYSPG.name()) || StringUtils.equals(productType, SanguoDmz.name())
                    || StringUtils.equals(productType, TravelAmerica.name()) || StringUtils.equals(productType, PetsWar.name())) {
                String url = ProductConfig.getMainSiteBaseUrl() + "/project/appoffline.vpage?type=" + productType;
                return "redirect:" + url;
            }

            // 跳转到app
            return "redirect:" + ProductConfig.getMainSiteBaseUrl() + "/parentMobile/ucenter/shoppinginfo.vpage?sid=" + sid + "&productType=" + productType;
        } catch (Exception ex) {
            logger.error("Get {} info page failed", type, ex);
            return redirectWithMsg("查询产品详情失败", model);
        }
    }

    private String transferProductType(String type) {
        String productType;
        switch (type) {
            case "afenti":
                productType = OrderProductServiceType.AfentiExam.name();
                break;
            case "america":
                productType = OrderProductServiceType.TravelAmerica.name();
                break;
            case "walker":
                productType = OrderProductServiceType.Walker.name();
                break;
            case "sanguodmz":
                productType = OrderProductServiceType.SanguoDmz.name();
                break;
            case "fairyland":
                productType = OrderProductServiceType.A17ZYSPG.name();
                break;
            case "stem":
                productType = OrderProductServiceType.Stem101.name();
                break;
            case "pets":
                productType = OrderProductServiceType.PetsWar.name();
                break;
            case "wukongshizi":
                productType = OrderProductServiceType.WukongShizi.name();
                break;
            case "wkongpinyin":
                productType = OrderProductServiceType.WukongPinyin.name();
                break;
            default:
                return null;
        }
        return productType;
    }

    /**
     * FIXME Task#18426 趣味数学挑战赛活动期间使用 By Wyc 2016-02-25
     *
     * @param type trial-试用版 / standard-标准版
     */
    @RequestMapping(value = "globalmath-{type}.vpage", method = RequestMethod.GET)
    public String globalMathInfo(@PathVariable String type, Model model) {
        return "redirect:/"; //产品已下线
    }

    /**
     * Task#18426 By Wyc 2016-02-25
     * 参考 RedirectController.goApps app_key=GlobalMath 方法去生成试用版报名的session_key
     */
    @RequestMapping(value = "globalmath-trial.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage globalMathTrialSignUp(Model model) {
        return MapMessage.errorMessage("产品已下线");
    }
}
