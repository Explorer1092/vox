package com.voxlearning.utopia.admin.controller.ailesson;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.service.chips.ClazzManagerService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserLoader;
import com.voxlearning.utopia.service.ai.data.AIUserInfoDetail;
import com.voxlearning.utopia.service.ai.data.AIUserInfoWithScore;
import com.voxlearning.utopia.service.ai.data.AiUserOperationInfo;
import com.voxlearning.utopia.service.ai.data.ChipsUserMailInfo;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author xuan.zhu
 * @date 2018/8/22 10:52
 * 薯条英语-用户管理
 */
@Controller
@RequestMapping("/chips/user/ai")
public class AiUserController extends AbstractAdminSystemController {

    private static final int pageSize = 20;

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @ImportService(interfaceClass = ChipsEnglishUserLoader.class)
    private ChipsEnglishUserLoader chipsEnglishUserLoader;
    @Inject
    private ClazzManagerService clazzManagerService;

    /**
     * 用户管理首页
     *
     * @return
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String indexPage() {
        return "ailesson/user_manager_index";
    }

    /**
     * 获取薯条英语全部产品
     *
     * @return
     */
    @RequestMapping(value = "/allChipEnglishProduct.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage obtainChipEnglishProductItems() {
        MapMessage mapMessage = MapMessage.successMessage();
        String productType = getRequestString("productType");
        List<OrderProduct> productList;
        if (StringUtils.isBlank(productType)) {
            productList = clazzManagerService.loadOrderProductByProductType(Collections.singletonList("2"));
            mapMessage.add("productTypeList", clazzManagerService.buildProductTypeList(Collections.singletonList("2")));
        } else {
            String[] split = productType.split(",");
            productList = clazzManagerService.loadOrderProductByProductType(Arrays.asList(split));
            mapMessage.add("productTypeList", clazzManagerService.buildProductTypeList(Arrays.asList(split)));
        }
        mapMessage.add("orderProductList", productList);
        return mapMessage;
    }

    /**
     * 根据 产品id 获取所有的班级
     *
     * @return
     */
    @RequestMapping(value = "/class/obtainByProductItemId.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage obtainClassByProductItemId() {
        String orderProduceId = getRequestString("orderProduceId");
        if (StringUtils.isBlank(orderProduceId)) {
            return MapMessage.errorMessage("参数异常");
        }

        return MapMessage.successMessage()
                .add("chipsEnglishClassList", chipsEnglishClazzService.selectChipsEnglishClassByProductId(orderProduceId));
    }

    /**
     * 用户筛选
     *
     * @return
     */
    @RequestMapping(value = "/filterUser.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage filterUser() {

        long classId = getRequestLong("classId");
        String productId = getRequestString("productId");
        String userId = getRequestString("userId");
        int minCost = getRequestInt("minCost");
        int maxCost = getRequestInt("maxCost");

        int pageNumber = getRequestInt("pageNumber", 1);

        MapMessage mapMessage = chipsEnglishUserLoader.loadSimpleUserByClassId(productId, classId, userId, minCost, maxCost, pageNumber);

        return mapMessage;
    }


    /**
     * 用户管理 - 用户详情
     *
     * @return
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String detailPage(Model model) {
        String userId = getRequestString("userId");
        model.addAttribute("userId", userId);
        return "ailesson/user_manager_detail";
    }

    /**
     * 用户详细信息
     *
     * @return
     */
    @RequestMapping(value = "/userInfoDetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userInfoDetail() {
        long userId = getRequestLong("userId");
        AIUserInfoDetail aiUserInfoDetail = chipsEnglishUserLoader.loadUserDetailByUserId(userId, getCurrentAdminUser().getAdminUserName());
        String phone = aiUserInfoDetail == null ? "" : aiUserInfoDetail.getPhone();
        addAdminLog("queryUserInfoDetailWithPhone", userId, StringUtils.mobileObscure(phone), "crm", "ID:" + userId + ", phone:" + phone);
        return aiUserInfoDetail == null ?
                MapMessage.errorMessage("不存在id为 " + userId + " 的用户") :
                MapMessage.successMessage().add("userInfo", aiUserInfoDetail);
    }

    /**
     * 用户成绩列表
     *
     * @return
     */
    @RequestMapping(value = "/userScoreList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userScoreList() {
        MapMessage mapMessage = MapMessage.successMessage();
        long userId = getRequestLong("userId");
        String productId = getRequestString("productId");

        List<AIUserInfoWithScore> scoreList = chipsEnglishUserLoader.loadUserAllClassScore(userId, productId);

        mapMessage.add("scoreList", scoreList);
        return mapMessage;
    }


    /**
     * 用户单元成绩运营日志编辑
     *
     * @return
     */
    @RequestMapping(value = "/unitResultOperationLogEdit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage testAddUser() {
        MapMessage mapMessage = MapMessage.successMessage();

        Long userId = getRequestLong("userId");
        String unitId = getRequestString("unitId");
        String operationLog = getRequestString("operationLog");

        chipsEnglishUserLoader.editUnitResultOperationLog(userId, unitId, operationLog);

        return mapMessage.setSuccess(Boolean.TRUE);
    }


    /**
     * 用户运营信息
     *
     * @return
     */
    @RequestMapping(value = "/userOperationInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userOperationInfo() {
        MapMessage mapMessage = MapMessage.successMessage();
        long userId = getRequestLong("userId");

        List<AiUserOperationInfo> operationList = chipsEnglishUserLoader.loadUserOperationInfoList(userId);

        mapMessage.add("operationList", operationList);
        return mapMessage;
    }

    /**
     * 用户邮寄信息获取
     *
     * @return
     */
    @RequestMapping(value = "/userMailInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userMailInfo() {
        MapMessage mapMessage = MapMessage.successMessage();
        long userId = getRequestLong("userId");

        ChipsUserMailInfo mailInfo = chipsEnglishUserLoader.loadUserMailInfo(userId);

        mapMessage.add("mailInfo", mailInfo);
        return mapMessage;
    }

    /**
     * 用户邮寄信息获取更新
     *
     * @return
     */
    @RequestMapping(value = "/userMailInfoSave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userMailInfoSave() {
        long userId = getRequestLong("userId");
        String editType = getRequestString("editType");
        String value = getRequestString("value");

        boolean result = chipsEnglishUserLoader.updateUserMailInfo(userId, editType, value);

        return result ? MapMessage.successMessage() : MapMessage.errorMessage("保存失败！");
    }


    /**
     * 用户微信号更新保存
     *
     * @return
     */
    @RequestMapping(value = "/userWxNumSave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userWxNumSave() {
        long userId = getRequestLong("userId");
        String wxNum = getRequestString("wxNum");

        boolean result = chipsEnglishUserLoader.updateUserWxNum(userId, wxNum);

        return result ? MapMessage.successMessage() : MapMessage.errorMessage("保存失败！");
    }


    /**
     * 用户微信昵称更新保存
     *
     * @return
     */
    @RequestMapping(value = "/userWxNameSave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userWxNameSave() {
        long userId = getRequestLong("userId");
        String wxName = getRequestString("wxName");

        boolean result = chipsEnglishUserLoader.updateUserWxName(userId, wxName);

        return result ? MapMessage.successMessage() : MapMessage.errorMessage("保存失败！");
    }
}
