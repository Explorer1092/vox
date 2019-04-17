package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.*;
import com.voxlearning.utopia.admin.service.chips.ClazzManagerService;
import com.voxlearning.utopia.service.ai.api.ChipsActiveService;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.data.AIUserInfoWithScore;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.alps.calendar.DateUtils.dateToString;

/**
 * @author guangqing
 * @since 2018/8/21
 */
@Controller
@RequestMapping("/chips/chips/clazz") // TODO 这里比较恶心,以为迁移的问题。
public class ClazzManagerController extends AbstractAdminSystemController {

    @Inject
    private ClazzManagerService clazzManagerService;

    @ImportService(interfaceClass = ChipsActiveService.class)
    private ChipsActiveService chipsActiveService;

    private static final int pageSize = 20;

    /**
     * 班级总览列表页
     */
    @RequestMapping(value = "summary/list.vpage", method = RequestMethod.GET)
    public String summaryList(Model model) {
        String clazzName = getRequestString("clazzName");
        String clazzTeacher = getRequestString("clazzTeacher");
        String productId = getRequestString("product");
        String[] productTypes = getRequest().getParameterValues("productType");
        List<OrderProduct> productList;
        if (productTypes == null) {//从班级总览进入，此时没有传productType 给个默认值
            productList = clazzManagerService.loadOrderProductByProductType(Collections.singletonList("2"));
            model.addAttribute("productTypeList", clazzManagerService.buildProductTypeList(Collections.singletonList("2")));
        } else {
            productList = clazzManagerService.loadOrderProductByProductType(Arrays.asList(productTypes));
            model.addAttribute("productTypeList", clazzManagerService.buildProductTypeList(Arrays.asList(productTypes)));
        }
        List<ClazzCrmPojo> clazzPojoList = clazzManagerService.selectClazzCrmPojo(clazzName, clazzTeacher, productId);
        model.addAttribute("clazzPojoList", clazzPojoList);
        model.addAttribute("teacherOptionList", clazzManagerService.buildTeacherSelectOptionList(clazzTeacher));
        model.addAttribute("productOptionList", clazzManagerService.buildProductSelectOptionList(productList, productId));
        model.addAttribute("clazzName", clazzName);
        return "chips/clazz/summary";
    }

    private void addProductClazzSelectOption(Model model, String productId, Long clazzId) {
        model.addAttribute("productOptionList", clazzManagerService.buildProductSelectOptionList(productId));
        model.addAttribute("clazzOptionList", clazzManagerService.buildClazzSelectOptionList(productId, clazzId));
    }

    /**
     * 创建班级页面
     */
    @RequestMapping(value = "create.vpage", method = RequestMethod.GET)
    public String clazzCreate(Model model) {
        model.addAttribute("teacherOptionList", clazzManagerService.buildTeacherSelectOptionList(null));
        model.addAttribute("productOptionList", clazzManagerService.buildProductSelectOptionList(null));
        model.addAttribute("clazzTypeOptionList", ChipsEnglishClass.Type.values());
        return "chips/clazz/clazzCreate";
    }

    /**
     * 编辑班级页面
     */
    @RequestMapping(value = "modify.vpage", method = RequestMethod.GET)
    public String clazzModify(Model model) {
        Long clazzId = getRequestLong("clazzId");
        ClazzCrmPojo pojo = clazzManagerService.selectClazzById(clazzId);
        model.addAttribute("teacherOptionList", clazzManagerService.buildTeacherSelectOptionList(pojo == null ? null : pojo.getClazzTeacherName()));
        model.addAttribute("productOptionList", clazzManagerService.buildProductSelectOptionList(pojo == null ? null : pojo.getProductId()));
        model.addAttribute("clazz", pojo == null ? new ClazzCrmPojo() : pojo);
        model.addAttribute("clazzTypeOptionList", ChipsEnglishClass.Type.values());
        return "chips/clazz/clazzModify";
    }


    /**
     * 合并班级页面
     */
    @RequestMapping(value = "combine.vpage", method = RequestMethod.GET)
    public String clazzCombine(Model model) {
        String productId = getRequestString("productId");
        Long clazzId = getRequestLong("clazzId");
        List<SelectOption> aimClazzList = clazzManagerService.buildAimClazzSelectOptionList(productId, clazzId);
        model.addAttribute("aimClazzList", aimClazzList);
        ClazzCrmPojo pojo = clazzManagerService.selectClazzById(clazzId);
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("clazzName", pojo == null ? "" : pojo.getClazzName());
        return "chips/clazz/clazzCombine";
    }

    /**
     * 新建和编辑班级时的保存url
     */
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzSave() {
        Long clazzId = getRequestLong("clazzId");
        String clazzName = getRequestString("clazzName");
        String clazzTeacherName = getRequestString("clazzTeacherName");
        String productId = getRequestString("productId");
        Integer userLimitation = getRequestInt("userLimitation", 0);

        String clazzType = getRequestString("clazzType");

        MapMessage message = clazzManagerService.saveOrUpdateChipsEnglishClass(clazzId, clazzName, clazzTeacherName, productId, userLimitation, clazzType);
        if (message.isSuccess()) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage().setInfo("数据存储失败");
        }
    }

    /**
     * 更改班级下的产品
     */
    @RequestMapping(value = "changeIndex.vpage", method = RequestMethod.GET)
    public String clazzChangeProductIndex(Model model) {
        Long clazzId = getRequestLong("clazzId");
        ClazzCrmPojo pojo = clazzManagerService.selectClazzById(clazzId);
        model.addAttribute("productOptionList", clazzManagerService.buildProductSelectOptionList(null));
        model.addAttribute("clazz", pojo == null ? new ClazzCrmPojo() : pojo);
        return "chips/clazz/clazzChangeProduct";
    }

    @RequestMapping(value = "changeQuery.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clazzChangeProductQuery() {
        Long clazzId = getRequestLong("clazzId");
        ClazzCrmPojo pojo = clazzManagerService.selectClazzById(clazzId);
        MapMessage message = MapMessage.successMessage();
        message.add("pojo", pojo);
//        String productType = getRequestString("productType");
//        if (StringUtils.isBlank(productType)) {
//            productType = "2,3";
//        }
        String productType = "3";
        String[] split = productType.split(",");
        message.add("productTypeList", clazzManagerService.buildProductTypeList2(Arrays.asList(split)));
        List<SelectOption> productList = clazzManagerService.buildProdctListByType(Arrays.asList(split));
        message.add("toProductList", productList);
        return message;
    }

    /**
     * 保存更换的班级下的产品
     */
    @RequestMapping(value = "updateChangeProduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateChangeProduct() {
        AuthCurrentAdminUser currentAdminUser = getCurrentAdminUser();
        if (currentAdminUser == null) {
            return MapMessage.errorMessage().setInfo("on login user");
        }
        String originProductId = getRequestString("originProductId");
        Long clazzId = getRequestLong("clazzId");
        String productId = getRequestString("productId");
        MapMessage checkMessage = checkChangeProduct(originProductId, productId);
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }
        MapMessage message = clazzManagerService.updateChipsEnglishClassProduct(clazzId, productId);
        clazzManagerService.insertChipsEnglishClassUpdateLog(currentAdminUser.getAdminUserName(), clazzId, originProductId, productId);
        if (message.isSuccess()) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage().setInfo("数据存储失败");
        }
    }

    /**
     * 更换产品时进行价格校验，只有价格相同的才能更换
     *
     * @param originProductId
     * @param productId
     * @return
     */
    private MapMessage checkChangeProduct(String originProductId, String productId) {
        if (StringUtils.isBlank(originProductId) || StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("有产品不存在,原产品:" + originProductId + " ;目标产品: " + productId);
        }
        if (originProductId.equals(productId)) {
            return MapMessage.errorMessage("不能更换成相同的产品");
        }
        OrderProduct originProduct = userOrderLoaderClient.loadOrderProductById(originProductId);
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        if (originProduct == null || originProduct.getPrice() == null || product == null || product.getPrice() == null) {
            return MapMessage.errorMessage("获取产品对应的价格失败");
        }
        if (originProduct.getPrice().compareTo(product.getPrice()) == 0) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("价格不同，不能更换为此产品");
    }

    /**
     * 更换产品时进行价格校验，只有价格相同的才能更换
     *
     * @param originProductId
     * @param productId
     * @return
     */
    private MapMessage checkUserChangeProduct(String originProductId, String productId, Long originClazzId, Long clazzId) {
        if (StringUtils.isBlank(originProductId) || StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("有产品不存在,原产品:" + originProductId + " ;目标产品: " + productId);
        }
        if (originClazzId == null || originClazzId == 0l || clazzId == null || clazzId == 0l) {
            return MapMessage.errorMessage("有班级不存在,原班级:" + originClazzId + " ;目标班级: " + clazzId);
        }
        if (originProductId.equals(productId) && originClazzId.equals(clazzId)) {
            return MapMessage.errorMessage("不能更换成相同的产品下的相同班级");
        }
        if (originProductId.equals(productId)) {
            return MapMessage.successMessage();
        }
        OrderProduct originProduct = userOrderLoaderClient.loadOrderProductById(originProductId);
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        if (originProduct == null || originProduct.getPrice() == null || product == null || product.getPrice() == null) {
            return MapMessage.errorMessage("获取产品对应的价格失败");
        }
        if (originProduct.getPrice().compareTo(product.getPrice()) == 0) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("价格不同，不能更换为此产品");
    }

    /**
     * 合并班级的保存url
     */
    @RequestMapping(value = "mergeSave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzMergeSave() {
        Long clazzId = getRequestLong("clazzId");
        Long aimClazzId = getRequestLong("aimClazzId");
        boolean overFlow = clazzManagerService.isUserLimitationOverFlow(clazzId, aimClazzId);
        if (overFlow) {
            return MapMessage.errorMessage().add("info", "overFlow " + aimClazzId + " clazz userLimitation");
        }
        MapMessage message = clazzManagerService.mergeChipsEnglishClazz(clazzId, aimClazzId);
        if (message.isSuccess()) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage().add("info", "班级合并失败");
    }

    /**
     * 每个班级的基础信息页面
     */
    @RequestMapping(value = "manager/basicInfo.vpage", method = RequestMethod.GET)
    public String clazzBasicInfo(Model model) {
        Long clazzId = getRequestLong("clazzId");
        String productId = getRequestString("productId");
        int dataType = getRequestInt("dataType");   // 数据类型  0：今日数据   1：最新数据
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        if (dataType == 0) {
            List<ClazzCrmBasicPojo> clazzBasicPojoList = clazzManagerService.buildClazzCrmBasicPojo(clazzId, productId);
            model.addAttribute("basicList", clazzBasicPojoList);
        } else {
            List<ClazzCrmBasicPojo> clazzBasicPojoList = clazzManagerService.buildLatestClazzCrmBasicPojo(clazzId, productId);
            model.addAttribute("basicList", clazzBasicPojoList);
        }
        ClazzCrmBasicPojoV2 pojoV2 = clazzManagerService.buildClazzCrmBasicPojoV2(clazzId);
        model.addAttribute("basicInfo", pojoV2);
        model.addAttribute("clazz", clazzCrmPojo == null ? new ClazzCrmPojo() : clazzCrmPojo);
        model.addAttribute("productName", clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getProductName()) ? clazzCrmPojo.getProductName() : "");
        model.addAttribute("productId", productId);
        model.addAttribute("clazzName", clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "");
        model.addAttribute("dataType", dataType);
        // 合并一次查询
        Map<String, Integer> serviceRemindCountMap = chipsActiveService.obtainAllActiveServiceRemained(clazzId);

        model.addAttribute("activeServiceRemained", serviceRemindCountMap.get(ChipsActiveServiceType.SERVICE.name()));
        model.addAttribute("remindRemained", serviceRemindCountMap.get(ChipsActiveServiceType.REMIND.name()));
        model.addAttribute("bindingRemained", serviceRemindCountMap.get(ChipsActiveServiceType.BINDING.name()));
        model.addAttribute("instructionRemained", serviceRemindCountMap.get(ChipsActiveServiceType.USEINSTRUCTION.name()));
        model.addAttribute("renewRemained", serviceRemindCountMap.get(ChipsActiveServiceType.RENEWREMIND.name()));
        addProductClazzSelectOption(model, productId, clazzId);
        return "chips/clazz/basicInfo";
    }

    /**
     * 每个班级下的用户成绩
     */
    @RequestMapping(value = "manager/userScore.vpage", method = RequestMethod.GET)
    public String userScore(Model model) {
        int pageNumber = getRequestInt("pageNumber", 1);
        String productId = getRequestString("productId");
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String grading = getRequestString("grading");
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        List<Long> userIdList = clazzManagerService.pageUserList(clazzId, userId, grading);
        int total = userIdList == null ? 0 : userIdList.size();
        Pageable pageable = new PageRequest(pageNumber - 1, pageSize);
        Page<Long> pageData = PageableUtils.listToPage(userIdList == null ? new ArrayList<>() : userIdList, pageable);
        userIdList = pageData.getContent();
        model.addAttribute("pageData", pageData);
        Map<Long, List<AIUserInfoWithScore>> userIdScoreMap = clazzManagerService.buildAllUserScoreByClazz(clazzId, userIdList);
        model.addAttribute("clazz", clazzCrmPojo == null ? new ClazzCrmPojo() : clazzCrmPojo);
        model.addAttribute("productName", clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getProductName()) ? clazzCrmPojo.getProductName() : "");
        model.addAttribute("clazzName", clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "");
        model.addAttribute("userId", userId == 0L ? "" : userId);
        model.addAttribute("gradingOptionList", clazzManagerService.buildGradingSelectOptionList(grading));
        int lessonCount = clazzManagerService.buildMaxUnitCount(productId);
        model.addAttribute("lessonTitleList", clazzManagerService.buildUserScoreLessonTitle2(lessonCount));
        model.addAttribute("clazzAveScoreList", clazzManagerService.buildClazzAvgScore(clazzId, productId, lessonCount));
        model.addAttribute("userScoreList", clazzManagerService.buildUserScore(userIdList, userIdScoreMap, lessonCount, clazzId));
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("productId", productId);
        model.addAttribute("total", total);
        addProductClazzSelectOption(model, productId, clazzId);
        return "chips/clazz/userScore";
    }

    @RequestMapping(value = "manager/operationInfo.vpage", method = RequestMethod.GET)
    public String operationInfo(Model model) {
        int pageNumber = getRequestInt("pageNumber", 1);
        String productId = getRequestString("productId");
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String grading = getRequestString("grading");
        Double jztConsumerMin = getRequestDouble("jztConsumerMin", -1.0);
        Double jztConsumerMax = getRequestDouble("jztConsumerMax", -1.0);
        double serviceScoreMin = getRequestDouble("serviceScoreMin", 0);
        double serviceScoreMax = getRequestDouble("serviceScoreMax", 0);
        int wxAdd = getRequestInt("wxAdd", 2);
        int epWxAdd = getRequestInt("epWxAdd", 2);
        int wxCodeShowType = getRequestInt("wxCodeShowType", 2);
        int wxNickName = getRequestInt("wxNickName", 2);
        int wxLogin = getRequestInt("wxLogin", 2);
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        List<Long> userIdList = clazzManagerService.buildOperatingUserIdListNew(clazzId, userId, grading,
                jztConsumerMin == -1.0 ? null : jztConsumerMin, jztConsumerMax == -1.0 ? null : jztConsumerMax,
                serviceScoreMin, serviceScoreMax, wxAdd, epWxAdd, wxLogin, wxCodeShowType, wxNickName);
        int total = userIdList == null ? 0 : userIdList.size();
        Pageable pageable = new PageRequest(pageNumber - 1, pageSize);
        Page<Long> pageData = PageableUtils.listToPage(userIdList == null ? new ArrayList<>() : userIdList, pageable);
        userIdList = pageData.getContent();
        model.addAttribute("pageData", pageData);
        model.addAttribute("clazz", clazzCrmPojo == null ? new ClazzCrmPojo() : clazzCrmPojo);
        model.addAttribute("productName", clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getProductName()) ? clazzCrmPojo.getProductName() : "");
        model.addAttribute("clazzName", clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "");
        model.addAttribute("gradingOptionList", clazzManagerService.buildGradingSelectOptionList(grading));
        model.addAttribute("userId", userId == 0L ? "" : userId);
        model.addAttribute("clazzId", clazzId == 0L ? "" : clazzId);
        model.addAttribute("jztConsumerMin", jztConsumerMin == -1.0 ? "" : jztConsumerMin);
        model.addAttribute("jztConsumerMax", jztConsumerMax == -1.0 ? "" : jztConsumerMax);
        model.addAttribute("serviceScoreMin", jztConsumerMin == 0 ? "" : serviceScoreMin);
        model.addAttribute("serviceScoreMax", jztConsumerMax == 0 ? "" : serviceScoreMax);
        model.addAttribute("wxAdd", wxAdd);
        model.addAttribute("epWxAdd", epWxAdd);
        model.addAttribute("wxCodeShowType", wxCodeShowType);
        model.addAttribute("wxNickName", wxNickName);
        model.addAttribute("wxLogin", wxLogin);
        model.addAttribute("operatingList", clazzManagerService.buildOperatingListNew(clazzId, userIdList, productId));
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("productId", productId);
        model.addAttribute("total", total);
        addProductClazzSelectOption(model, productId, clazzId);
        return "chips/clazz/operationInfo";
    }

    @RequestMapping(value = "userInfoModify.vpage", method = RequestMethod.GET)
    public String userOperatingInfoModify(Model model) {
        Long userId = getRequestLong("userId");
        Long clazzId = getRequestLong("clazzId");
        ClazzCrmPojo pojo = clazzManagerService.selectClazzById(clazzId);
        ClazzCrmOperatingPojo operatingPojo = clazzManagerService.selectWechatNumberInGroupDuration(userId, clazzId);
        model.addAttribute("clazz", pojo == null ? new ClazzCrmPojo() : pojo);
        model.addAttribute("joinedGroupOptionList", clazzManagerService.buildJoinGroupSelectOptionList(operatingPojo == null || operatingPojo.getJoinedGroup() == null ? null : operatingPojo.getJoinedGroup()));//是否进群下拉
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("userId", userId);
        model.addAttribute("operatingPojo", operatingPojo);
        return "chips/clazz/userOperatingInfoModify";
    }

    @RequestMapping(value = "userInfoClazzChange.vpage", method = RequestMethod.GET)
    public String userInfoClazzChange(Model model) {
        Long clazzId = getRequestLong("clazzId");
        ClazzCrmPojo pojo = clazzManagerService.selectClazzById(clazzId);
        model.addAttribute("productOptionList", clazzManagerService.buildProductSelectOptionList(null));
        model.addAttribute("clazz", pojo == null ? new ClazzCrmPojo() : pojo);

        return "chips/clazz/userChangeClazz";
    }

    @RequestMapping(value = "queryClazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryClazzByProduct() {
        String productId = getRequestString("productId");
        List<SelectOption> optionList = clazzManagerService.buildClazzSelectOptionDescTeacherList(productId, null);
        MapMessage message = MapMessage.successMessage();
        message.add("clazzOptionList", optionList);
        return message;
    }

    @RequestMapping(value = "updateUserChangeProduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUserChangeProduct() {
        AuthCurrentAdminUser currentAdminUser = getCurrentAdminUser();
        if (currentAdminUser == null) {
            return MapMessage.errorMessage().setInfo("on login user");
        }
        String originProductId = getRequestString("originProductId");
        String productId = getRequestString("productId");
        Long originClazzId = getRequestLong("originClazzId");
        Long clazzId = getRequestLong("clazzId");
        long userId = getRequestLong("userId");
        MapMessage checkMessage = checkUserChangeProduct(originProductId, productId, originClazzId, clazzId);
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }
        MapMessage message = clazzManagerService.updateChipsEnglishClassProduct(userId,originClazzId, clazzId,originProductId, productId);
        clazzManagerService.insertUserChipsEnglishClassUpdateLog(currentAdminUser.getAdminUserName(), userId, originClazzId, clazzId, originProductId, productId);
        if (message.isSuccess()) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage().setInfo(StringUtils.isNotBlank(message.getInfo()) ? message.getInfo() : "数据存储失败");
        }
    }


    @RequestMapping(value = "manager/generalInfo.vpage", method = RequestMethod.GET)
    public String generalInfo(Model model) {
        int pageNumber = getRequestInt("pageNumber", 1);
        String productId = getRequestString("productId");
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String grading = getRequestString("grading");
        double jztConsumerMin = getRequestDouble("jztConsumerMin", -1.0);
        double jztConsumerMax = getRequestDouble("jztConsumerMax", -1.0);
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        List<Long> userIdList = clazzManagerService.buildOperatingUserIdList(clazzId, userId, grading,
                jztConsumerMin == -1.0 ? null : jztConsumerMin, jztConsumerMax == -1.0 ? null : jztConsumerMax);
        int total = userIdList == null ? 0 : userIdList.size();
        Pageable pageable = new PageRequest(pageNumber - 1, pageSize);
        Page<Long> pageData = PageableUtils.listToPage(userIdList == null ? new ArrayList<>() : userIdList, pageable);
        userIdList = pageData.getContent();
        model.addAttribute("pageData", pageData);
        model.addAttribute("clazz", clazzCrmPojo == null ? new ClazzCrmPojo() : clazzCrmPojo);
        model.addAttribute("productName", clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getProductName()) ? clazzCrmPojo.getProductName() : "");
        model.addAttribute("clazzName", clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "");
        model.addAttribute("gradingOptionList", clazzManagerService.buildGradingSelectOptionList(grading));
        model.addAttribute("userId", userId == 0L ? "" : userId);
        model.addAttribute("clazzId", clazzId == 0L ? "" : clazzId);
        model.addAttribute("jztConsumerMin", jztConsumerMin == -1.0 ? "" : jztConsumerMin);
        model.addAttribute("jztConsumerMax", jztConsumerMax == -1.0 ? "" : jztConsumerMax);
        model.addAttribute("operatingList", clazzManagerService.buildOperatingList(clazzId, userIdList));
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("productId", productId);
        model.addAttribute("total", total);
        addProductClazzSelectOption(model, productId, clazzId);
        return "chips/clazz/generalInfo";
    }

    @RequestMapping(value = "userInfoModifySave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userOperatingInfoModifySave() {
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String joinStr = getRequest().getParameter("joinedGroup");
        String durationStr = getRequest().getParameter("duration");
        String wechatNumber = getRequestString("wechatNumber");
        Boolean joinedGroup = StringUtils.isBlank(joinStr) ? null : ConversionUtils.toBool(joinStr);
        return clazzManagerService.saveUserModifyInfo(clazzId, userId, wechatNumber, joinedGroup, durationStr);
    }

    @RequestMapping(value = "saveETextBook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveETextBook() {
        String userIds = getRequestString("showPlay");
        boolean isCheck = getRequestBool("isCheck");
        List<Long> userIdList = parseUserIds(userIds);
        if (CollectionUtils.isEmpty(userIdList)) {
            return MapMessage.successMessage();
        }
        return clazzManagerService.saveShowPlay(userIdList, isCheck);
    }

    @RequestMapping(value = "saveWxAddStatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveWxAddStatus() {
        Long userId = getRequestLong("userId");
        boolean wxAddStatus = getRequestBool("wxAddStatus");
        if (userId == 0L) {
            return MapMessage.successMessage();
        }
        return clazzManagerService.saveWxAddStatus(userId, wxAddStatus);
    }

    private List<Long> parseUserIds(String userIds) {
        if (StringUtils.isBlank(userIds)) {
            return null;
        }
        String[] userArr = userIds.split(",");
        List<Long> list = new ArrayList<>();
        for (String str : userArr) {
            long userId = SafeConverter.toLong(str);
            if (userId == 0L) {
                continue;
            }
            list.add(userId);
        }
        return list;
    }

    @RequestMapping(value = "operationInfoExport.vpage", method = RequestMethod.GET)
    public void operationInfoExport() {
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String grading = getRequestString("grading");
        double jztConsumerMin = getRequestDouble("jztConsumerMin", -1.0);
        double jztConsumerMax = getRequestDouble("jztConsumerMax", -1.0);
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        List<Long> userIdList = clazzManagerService.buildOperatingUserIdList(clazzId, userId, grading,
                jztConsumerMin == -1.0 ? null : jztConsumerMin, jztConsumerMax == -1.0 ? null : jztConsumerMax);
        List<ClazzCrmOperatingPojo> pojoList = clazzManagerService.buildOperatingList(clazzId, userIdList);
        HSSFWorkbook hssfWorkbook = clazzManagerService.createoperationInfoExportResult(pojoList);
        String clazzName = (clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "") + "_综合信息";
        String filename = clazzName + "_" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

    @RequestMapping(value = "operationInfoNewExport.vpage", method = RequestMethod.GET)
    public void operationInfoNewExport() {
        String productId = getRequestString("productId");
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String grading = getRequestString("grading");
        Double jztConsumerMin = getRequestDouble("jztConsumerMin", -1.0);
        Double jztConsumerMax = getRequestDouble("jztConsumerMax", -1.0);
        double serviceScoreMin = getRequestDouble("serviceScoreMin", 0);
        double serviceScoreMax = getRequestDouble("serviceScoreMax", 0);
        int wxAdd = getRequestInt("wxAdd", 2);
        int epWxAdd = getRequestInt("epWxAdd", 2);
        int wxCodeShowType = getRequestInt("wxCodeShowType", 2);
        int wxNickName = getRequestInt("wxNickName", 2);
        int wxLogin = getRequestInt("wxLogin", 2);
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        List<Long> userIdList = clazzManagerService.buildOperatingUserIdListNew(clazzId, userId, grading,
                jztConsumerMin == -1.0 ? null : jztConsumerMin, jztConsumerMax == -1.0 ? null : jztConsumerMax,
                serviceScoreMin, serviceScoreMax, wxAdd, epWxAdd, wxLogin, wxCodeShowType, wxNickName);
        List<ClazzCrmOperatingPojo> pojoList = clazzManagerService.buildOperatingListNew(clazzId, userIdList, productId);
        HSSFWorkbook hssfWorkbook = clazzManagerService.createoperationInfoExportResultNew(pojoList);
        String clazzName = (clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "") + "_运营信息";
        String filename = clazzName + "_" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

    /**
     * 问卷导出
     */
    @RequestMapping(value = "questionnaireInfoExport.vpage", method = RequestMethod.GET)
    public void questionnaireInfoExport() {
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String grading = getRequestString("grading");
        double jztConsumerMin = getRequestDouble("jztConsumerMin", -1.0);
        double jztConsumerMax = getRequestDouble("jztConsumerMax", -1.0);
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        List<Long> userIdList = clazzManagerService.buildOperatingUserIdList(clazzId, userId, grading,
                jztConsumerMin == -1.0 ? null : jztConsumerMin, jztConsumerMax == -1.0 ? null : jztConsumerMax);
        List<ClazzCrmQuestionnairePojo> pojoList = clazzManagerService.buildQuestionnaireList(userIdList);
        HSSFWorkbook hssfWorkbook = clazzManagerService.createQuestionnaireInfoExportResult(pojoList);
        String clazzName = (clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "") + "_问卷信息";
        String filename = clazzName + "_" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

    /**
     * 问卷导出
     */
    @RequestMapping(value = "oralTestScheduleExport.vpage", method = RequestMethod.GET)
    public void oralTestScheduleExport() {
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String grading = getRequestString("grading");
        double jztConsumerMin = getRequestDouble("jztConsumerMin", -1.0);
        double jztConsumerMax = getRequestDouble("jztConsumerMax", -1.0);
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        List<Long> userIdList = clazzManagerService.buildOperatingUserIdList(clazzId, userId, grading,
                jztConsumerMin == -1.0 ? null : jztConsumerMin, jztConsumerMax == -1.0 ? null : jztConsumerMax);
        List<ClazzCrmOralTestPojo> pojoList = clazzManagerService.buildOralTestList(clazzId, userIdList);
        HSSFWorkbook hssfWorkbook = clazzManagerService.createOralTestExportResult(pojoList);
        String clazzName = (clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "") + "_口语测试";
        String filename = clazzName + "_" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

    /**
     * 邮寄地址导出
     */
    @RequestMapping(value = "mailAddressExport.vpage", method = RequestMethod.GET)
    public void mailAddressExport() {
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String grading = getRequestString("grading");
        double jztConsumerMin = getRequestDouble("jztConsumerMin", -1.0);
        double jztConsumerMax = getRequestDouble("jztConsumerMax", -1.0);
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        List<Long> userIdList = clazzManagerService.buildOperatingUserIdList(clazzId, userId, grading,
                jztConsumerMin == -1.0 ? null : jztConsumerMin, jztConsumerMax == -1.0 ? null : jztConsumerMax);
        List<ClazzCrmMailAddressPojo> pojoList = clazzManagerService.buildMailAddressList(userIdList);
        HSSFWorkbook hssfWorkbook = clazzManagerService.createMailAddressExportResult(pojoList);
        String clazzName = (clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "") + "_邮寄地址";
        String filename = clazzName + "_" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }


    @RequestMapping(value = "userScoreExport.vpage", method = RequestMethod.GET)
    public void userScoreExport() {
        Long clazzId = getRequestLong("clazzId");
        Long userId = getRequestLong("userId");
        String grading = getRequestString("grading");
        String productId = getRequestString("productId");
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        List<Long> userIdList = clazzManagerService.pageUserList(clazzId, userId, grading);
        Map<Long, List<AIUserInfoWithScore>> userIdScoreMap = clazzManagerService.buildAllUserScoreByClazz(clazzId, userIdList);
        int lessonCount = clazzManagerService.buildMaxUnitCount(productId);
        List<String> titleList = clazzManagerService.buildUserScoreLessonTitle(lessonCount);
        List<ClazzCrmUserScorePojo> avgList = clazzManagerService.buildClazzAvgScore(clazzId, productId, lessonCount);
        List<ClazzCrmUserScorePojo> userList = clazzManagerService.buildUserScore(userIdList, userIdScoreMap, lessonCount, clazzId);
        HSSFWorkbook hssfWorkbook = clazzManagerService.createUserScoreExportResult(titleList, avgList, userList);
        String clazzName = (clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "") + "_用户成绩";
        String filename = clazzName + "_" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

    @RequestMapping(value = "rank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rankListData() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("没有登录");
        }
        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("班级为空");
        }
        StoneUnitData unit = clazzManagerService.loadTodayStudyUnit(clazzId);
        if (unit == null) {
            return MapMessage.errorMessage("今日没有要上课的课程");
        }
        return clazzManagerService.rankListData(clazzId, unit);
    }

    @RequestMapping(value = "productList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage productList() {
        String productType = getRequestString("productType");
        MapMessage message = MapMessage.successMessage();
        if (StringUtils.isBlank(productType)) {
            List<SelectOption> productList = clazzManagerService.buildProductSelectOptionList(null);
            message.add("productList", productList);
            return message;
        }
        String[] split = productType.split(",");
        List<SelectOption> productList = clazzManagerService.buildProdctListByType(Arrays.asList(split));
        message.add("productList", productList);
        return message;
    }

    @RequestMapping(value = "addrExport.vpage", method = RequestMethod.GET)
    public void userMailAddressExport() {
        String productId = getRequestString("product");
        List<Long> userIdList = clazzManagerService.loadUserIdByProduct(productId);
        List<ClazzCrmMailExportPojo> mailExportPojoList = clazzManagerService.buildMailPojoList(userIdList);
        HSSFWorkbook hssfWorkbook = clazzManagerService.createMailExportResult(mailExportPojoList);
        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
        String productName = (orderProduct != null && StringUtils.isNotBlank(orderProduct.getName()) ? orderProduct.getName() : "") + "_邮寄地址";
        String filename = productName + "_" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }


    @RequestMapping(value = "saveEpWxAddStatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveEpWxAddStatus() {
        Long userId = getRequestLong("userId");
        boolean epWxAddStatus = getRequestBool("epWxAddStatus");
        if (userId == 0L) {
            return MapMessage.successMessage();
        }
        return clazzManagerService.saveEpWxAddStatus(userId, epWxAddStatus);
    }

    @RequestMapping(value = "energyExport.vpage", method = RequestMethod.GET)
    public void energyExport() {
        Long clazzId = getRequestLong("clazzId");
        String productId = getRequestString("productId");
        List<Long> userIdList = clazzManagerService.pageUserList(clazzId, null, null);
        List<List<String>> engeryList = clazzManagerService.exportEngery(userIdList, productId);
        ClazzCrmPojo clazzCrmPojo = clazzManagerService.selectClazzById(clazzId);
        HSSFWorkbook hssfWorkbook = clazzManagerService.createEngeryExportResult(engeryList);
        String clazzName = (clazzCrmPojo != null && StringUtils.isNotBlank(clazzCrmPojo.getClazzName()) ? clazzCrmPojo.getClazzName() : "") + "_能量榜";
        String filename = clazzName + "_" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

}
