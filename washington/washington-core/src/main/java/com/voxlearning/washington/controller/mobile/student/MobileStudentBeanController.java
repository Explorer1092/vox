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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.clazz.cache.ClazzCache;
import com.voxlearning.utopia.service.integral.api.constants.BeanBuilder;
import com.voxlearning.utopia.service.integral.api.constants.EnumBeanFeatures;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.Integral;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeManagerClient;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.mapper.LoadRewardProductContext;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetailPagination;
import com.voxlearning.utopia.service.reward.util.GrayFuncMngCallback;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.api.mapper.BeanGuideMapper;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.mobile.student.headline.helper.MobileStudentClazzHelper;
import com.voxlearning.washington.controller.open.v1.util.AppHomeworkCardFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/18
 * Time: 11:01
 * 学生学豆成长api
 */
@Controller
@RequestMapping(value = "/studentMobile/bean")
public class MobileStudentBeanController extends AbstractMobileController {

    // inject in alphabetical order
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private MobileStudentClazzHelper mobileStudentClazzHelper;
    @Inject private IntegralLoaderClient integralLoaderClient;
    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;
    @Inject private RewardLoaderClient rewardLoaderClient;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject private PrivilegeManagerClient privilegeManagerClient;

    private final int defaultVisitorSize = 4;

    private GrayFuncMngCallback grayFuncMngCallBack = (user, func1, func2) -> {
        if (user instanceof StudentDetail) {
            return grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable((StudentDetail) user, func1, func2, false);
        } else if (user instanceof TeacherDetail) {
            return grayFunctionManagerClient.getTeacherGrayFunctionManager()
                    .isWebGrayFunctionAvailable((TeacherDetail) user, func1, func2, false);
        }

        return false;
    };

    /**
     * 我的学豆页面
     */
    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage index() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long userId = currentUserId();
        try {
            // 获取学生当前豆子信息
            Integral integral = integralLoaderClient.getIntegralLoader().loadIntegral(userId);
            int total = integral == null ? 0 : SafeConverter.toInt(integral.getTotalIntegral());   // 豆子总数
            int usable = integral == null ? 0 : SafeConverter.toInt(integral.getUsableIntegral()); // 豆子余额

            int increased = 0;          // 距离上次登录新增豆子数
            int increasedOfWeek = 0;    // 本周新增豆子数

            Integer currentLevel = null; // 当前等级
            Integer beforeLevel = null;  // 上一等级
            boolean upgraded = false;    // 是否升级
            int beans4upgrade = total < BeanBuilder.TOTAL_MAXIMUM ? BeanBuilder.safeGetNext(EnumBeanFeatures.TOTAL, total) : 0;    // 升级需要的豆子

            // 新增豆子
            List<IntegralHistory> integralLogs = integralHistoryLoaderClient.getIntegralHistoryLoader().loadUserIntegralHistories(userId);
            // 过滤消耗和奖品中心的学豆
            if (CollectionUtils.isNotEmpty(integralLogs)) {
                integralLogs = integralLogs.stream()
                        .filter(history -> !IntegralType.isTotalIntegralIngoreType(history.getIntegralType()))
                        .filter(history -> history.getIntegralValue() > 0)
                        .collect(Collectors.toList());

                increased = getIncreasedNum(integralLogs);
                increasedOfWeek = getIncreasedNumOfWeek(integralLogs);
            }

            currentLevel = BeanBuilder.getLevel(EnumBeanFeatures.TOTAL, total);
            beforeLevel = BeanBuilder.getLevel(EnumBeanFeatures.TOTAL, total - increased);
            upgraded = !Objects.equals(currentLevel, beforeLevel);

            return MapMessage.successMessage().add(RES_RESULT, RES_RESULT_SUCCESS)
                    .add("total", total)
                    .add("usable", usable)
                    .add("increased", increased)
                    .add("increasedOfWeek", increasedOfWeek)
                    .add("upgraded", upgraded)
                    .add("currentLevel", currentLevel)
                    .add("beforeLevel", beforeLevel)
                    .add("beans4upgrade", beans4upgrade);
        } catch (Exception e) {
            logger.error("Failed to load student integral beans, student={}", userId, e);
            return MapMessage.errorMessage().add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE).add(RES_MESSAGE, e.getMessage());
        }
    }

    /**
     * 同学学豆页面
     */
    @RequestMapping(value = "/guest.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage guest() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long userId = getRequestLong("userId");
        try {
            // 检查用户信息
            User student = userLoaderClient.loadUser(userId, UserType.STUDENT);
            if (student == null || student.isDisabledTrue()) {
                return MapMessage.errorMessage().add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE)
                        .add(RES_MESSAGE, "该同学已离开班级，去看看其他人的学豆树吧");
            }
            // 获取学生当前豆子信息
            Integral integral = integralLoaderClient.getIntegralLoader().loadIntegral(userId);
            int total = integral == null ? 0 : SafeConverter.toInt(integral.getTotalIntegral());   // 豆子总数
            int usable = integral == null ? 0 : SafeConverter.toInt(integral.getUsableIntegral()); // 豆子余额
            Integer currentLevel = BeanBuilder.getLevel(EnumBeanFeatures.TOTAL, total);            // 当前等级
            int beans4upgrade = total < BeanBuilder.TOTAL_MAXIMUM ? BeanBuilder.safeGetNext(EnumBeanFeatures.TOTAL, total) : 0;    // 升级需要的豆子

            Date monday = WeekRange.current().getStartDate();
            // 本周新增豆子数
            int increasedOfWeek = integralHistoryLoaderClient.getIntegralHistoryLoader().loadUserIntegralHistories(userId)
                    .stream()
                    .filter(history -> history.getCreatetime().after(monday))
                    // 过滤消耗和奖品中心的学豆
                    .filter(history -> !Objects.equals(history.getIntegralType(), IntegralType.奖品相关.getType()))
                    .filter(history -> history.getIntegralValue() > 0)
                    .mapToInt(IntegralHistory::getIntegralValue).sum();

            return MapMessage.successMessage().add(RES_RESULT, RES_RESULT_SUCCESS)
                    .add("userId", userId)
                    .add("userName", student.fetchRealname())
                    .add("total", total)
                    .add("usable", usable)
                    .add("increasedOfWeek", increasedOfWeek)
                    .add("currentLevel", currentLevel)
                    .add("beans4upgrade", beans4upgrade);
        } catch (Exception e) {
            logger.error("Failed to load student integral beans as guest, student={}", userId, e);
            return MapMessage.errorMessage().add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE).add(RES_MESSAGE, e.getMessage());
        }

    }

    /**
     * 推荐奖品
     */
    @RequestMapping(value = "/products.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage products() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long userId = currentUserId();
        User currentUser = currentUser();
        try {
            // 获取学生当前豆子信息
            Integral integral = integralLoaderClient.getIntegralLoader().loadIntegral(userId);
            // 豆子余额
            int usable = integral == null ? 0 : SafeConverter.toInt(integral.getUsableIntegral());
            List<RewardProductDetail> productDetails = loadAffordableProducts(currentUser, usable, RewardProductType.JPZX_TIYAN);

            // 如果没有可兑换的商品 则读取用户马上可以兑换的商品（4个）
            if (CollectionUtils.isEmpty(productDetails)) {
                List<RewardProductDetail> products = loadProducts(currentUser, RewardProductType.JPZX_TIYAN);
                if (CollectionUtils.isEmpty(products)) {
                    return MapMessage.successMessage().add(RES_RESULT, RES_RESULT_SUCCESS)
                            .add("products", null);
                }
                // 读取4个
                productDetails = products.subList(0, products.size() > 4 ? 4 : products.size());
            }

            List<Map<String, Object>> products = productDetails.stream().map(product -> {
                Map<String, Object> mProduct = new HashMap<>();
                mProduct.put("id", product.getId());
                mProduct.put("name", product.getProductName());
                mProduct.put("image", product.getImage());
                mProduct.put("price", product.getPrice());
                mProduct.put("discountPrice", product.getDiscountPrice());
                return mProduct;
            }).collect(Collectors.toList());

            return MapMessage.successMessage().add(RES_RESULT, RES_RESULT_SUCCESS)
                    .add("products", products);
        } catch (Exception e) {
            logger.error("Failed to load student bean products, student={}", userId, e);
            return MapMessage.errorMessage().add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE).add(RES_MESSAGE, e.getMessage());
        }
    }

    /**
     * 同学互访(大雾)
     * 随机展示4个所在group内学生头像+头饰+姓名；
     * 点击“换一换”再随机出4个，前后出现的学生不重复，一轮过后再重复
     */
    @RequestMapping(value = "/visitors.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage visitors() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long userId = currentUserId();
        StudentDetail student = currentStudentDetail();
        try {
            List<Long> selected = selectClassmates(userId, student.getClazzId());
            if (CollectionUtils.isNotEmpty(selected)) Collections.shuffle(selected); // 打乱，假装是随机的
            Map<Long, User> userMap = asyncUserServiceClient.loadUsersWithCache(selected);
            Map<Long, StudentInfo> infoMap = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfos(selected);
            List<Map<String, Object>> visitors = selected.stream().map(uid -> {
                Map<String, Object> info = new HashMap<>();
                User user = userMap.get(uid);
                StudentInfo studentInfo = infoMap.get(uid);
                info.put("userId", uid);
                info.put("userName", user == null ? null : user.fetchRealname());
                info.put("avatar", user == null ? null : user.fetchImageUrl());
                if (studentInfo != null) {
                    Privilege privilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(studentInfo.getHeadWearId());
                    info.put("headWear", privilege == null ? null : privilege.getImg());
                }
                return info;
            }).collect(Collectors.toList());
            return MapMessage.successMessage().add(RES_RESULT, RES_RESULT_SUCCESS)
                    .add("visitors", visitors);
        } catch (Exception e) {
            logger.error("Failed to load student bean visitors, student={}", userId, e);
            return MapMessage.errorMessage().add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE).add(RES_MESSAGE, e.getMessage());
        }
    }

    /**
     * 学豆攻略页面
     */
    @RequestMapping(value = "/guide.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage guide() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long userId = currentUserId();
        StudentDetail student = currentStudentDetail();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String sys = getRequestString(REQ_SYS);
        try {
            List<BeanGuideMapper> guideList = buildGuideCardList(student, ver, sys);
            return MapMessage.successMessage().add(RES_RESULT, RES_RESULT_SUCCESS)
                    .add("guides", guideList);
        } catch (Exception e) {
            logger.error("Failed to load student bean guide, student={}", userId, e);
            return MapMessage.errorMessage().add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE).add(RES_MESSAGE, e.getMessage());
        }
    }

    /**
     * 如果距离上一次进入此页面，无升级、且有获得>0的学豆，则展示吃豆子的动画状态，学豆数+X；
     * 此处，如果距离上次进入此页面超过7天，则取7天内获得的学豆数；
     */
    private int getIncreasedNum(List<IntegralHistory> integralLogs) {
        if (CollectionUtils.isEmpty(integralLogs)) {
            return 0;
        }
        Long userId = currentUserId();
        String cacheKey = "APPLICATION_STD_BEAN_LAST_VISITED_" + userId;
        CacheObject<Long> cacheObject = washingtonCacheSystem.CBS.flushable.get(cacheKey);
        int count = 0;
        //超过7天 则取7天内获得的学豆数
        if (cacheObject == null || cacheObject.getValue() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            count = integralLogs.stream()
                    .filter(integralHistory -> integralHistory.getCreatetime().after(calendar.getTime()))
                    .mapToInt(IntegralHistory::getIntegralValue).sum();

        } else {
            count = integralLogs.stream()
                    .filter(integralHistory -> integralHistory.getCreatetime().getTime() > cacheObject.getValue())
                    .mapToInt(IntegralHistory::getIntegralValue).sum();
        }

        washingtonCacheSystem.CBS.flushable.set(cacheKey, 7 * 24 * 60 * 60, System.currentTimeMillis());
        return count;
    }

    /**
     * 以自然周统计，显示当周累计获得的学豆数
     */
    private int getIncreasedNumOfWeek(List<IntegralHistory> integralLogs) {
        Date monday = WeekRange.current().getStartDate();
        return integralLogs.stream()
                .filter(integralHistory -> integralHistory.getCreatetime().after(monday))
                .mapToInt(IntegralHistory::getIntegralValue).sum();
    }

    /**
     * 获得一个用户可负担得起的商品详情列表
     *
     * @param user     用户信息
     * @param integral 用户学豆数
     * @param type     商品类型
     */
    private List<RewardProductDetail> loadAffordableProducts(
            User user,
            Integer integral,
            RewardProductType type) {

        if (type == null || integral == null)
            return Collections.emptyList();

        LoadRewardProductContext context = new LoadRewardProductContext();
        // 默认用排序值排序
        context.orderBy = "studentOrderValue";
        context.loadPage = "all";
        context.twoLevelTagId = 0L;
        context.categoryId = 0L;
        context.productType = type.name();
        context.pageNumber = 0;
        context.pageSize = Integer.MAX_VALUE;

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(user, context,
                u -> asyncUserServiceClient.getAsyncUserService()
                        .loadUserSchool(u)
                        .getUninterruptibly(),
                grayFuncMngCallBack);
        List<RewardProductDetail> allProducts = pagination.getContent();

        return allProducts.stream()
                .filter(p -> p.getDiscountPrice() <= integral)
                .collect(Collectors.toList());
    }

    /**
     * 获得商品列表 orderby price asc
     *
     * @param user 用户信息
     * @param type 商品类型
     */
    private List<RewardProductDetail> loadProducts(
            User user,
            RewardProductType type) {

        if (type == null)
            return Collections.emptyList();

        LoadRewardProductContext context = new LoadRewardProductContext();
        // 默认用排序值排序
        context.orderBy = "price";
        context.loadPage = "all";
        context.twoLevelTagId = 0L;
        context.categoryId = 0L;
        context.productType = type.name();
        context.pageNumber = 0;
        context.pageSize = Integer.MAX_VALUE;

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(user, context,
                u -> asyncUserServiceClient.getAsyncUserService()
                        .loadUserSchool(u)
                        .getUninterruptibly(),
                grayFuncMngCallBack);
        return pagination.getContent();
    }

    private List<Long> selectClassmates(Long userId, Long clazzId) {
        if (userId == null || clazzId == null) {
            return Collections.emptyList();
        }
        List<Long> classmates = mobileStudentClazzHelper.getCacheClassmates(userId, clazzId)
                .stream().map(User::getId)
                .filter(t -> !userId.equals(t))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(classmates)) return Collections.emptyList();

        // 取前四个出来
        List<Long> selected = new ArrayList<>();
        if (classmates.size() <= defaultVisitorSize) return classmates; // 只有这么几个就不折腾了

        // 读取上次加载到的列表的位置
        String cacheKey = "LastLoadClassmateIndex:" + userId;
        int index = SafeConverter.toInt(ClazzCache.getClazzCache().load(cacheKey));
        if (index < 0) index = 0;
        int size = classmates.size();
        Long[] classmateArray = new Long[size];
        classmates.toArray(classmateArray);
        for (int i = 0; i < defaultVisitorSize; ++i) {
            selected.add(classmateArray[index++ % size]);
        }
        // 缓存当前加载到的列表的位置
        ClazzCache.getClazzCache().set(cacheKey, 1800, index % size);
        return selected;
    }

    @SuppressWarnings("unchecked")
    private List<BeanGuideMapper> buildGuideCardList(StudentDetail student, String ver, String sys) {
        Map<String, Object> appIndexData = businessStudentServiceClient.loadStudentAppIndexData(student, ver, sys);
        if (MapUtils.isEmpty(appIndexData)) {
            return Collections.emptyList();
        }
        Map<String, Object> configMap = AppHomeworkCardFilter.getMappingInfo(getPageBlockContentGenerator());
        List<BeanGuideMapper> guideList = new LinkedList<>();
        // 完成作业
        BeanGuideMapper homeworkGuide = BeanGuideMapper.homework();
        List<Map<String, Object>> homeworkCards = (List<Map<String, Object>>) appIndexData.get("homeworkCards");
        if (CollectionUtils.isNotEmpty(homeworkCards)) {
            // 找出一条未完成作业
            homeworkGuide.setData(convertData(homeworkCards.get(0), configMap, ver, sys));
            homeworkGuide.setEnable(true);
        }
        guideList.add(homeworkGuide);

        // FIXME 暂时不要测验了。。以后再有再说
//        // 完成测验
//        BeanGuideMapper examGuide = BeanGuideMapper.exam();
//        List<Map<String, Object>> examCards = (List<Map<String, Object>>) appIndexData.get("enterableNewExamCards");
//        if (CollectionUtils.isNotEmpty(examCards)) {
//            // 找出一条未完成测验
//            examGuide.setData(convertData(examCards.get(0), configMap, ver, sys));
//            examGuide.setEnable(true);
//        }
//        guideList.add(examGuide);
        return guideList;
    }

    private Map<String, Object> convertData(Map<String, Object> data, Map<String, Object> configMap, String ver, String sys) {
        String homeworkType = SafeConverter.toString(data.get("homeworkType"));
        List<String> types = JsonUtils.fromJsonToList(JsonUtils.toJson(data.get("types")), String.class);
        String desc = SafeConverter.toString(data.get("desc"));
        Map<String, Object> homeworkCardMap = new HashMap<>();
        homeworkCardMap.put(RES_HOMEWORK_CARD_TYPE, homeworkType);
        homeworkCardMap.put(RES_HOMEWORK_CARD_DESC, desc);
        homeworkCardMap.put(RES_HOMEWORK_MAKE_UP_FLAG, false);
        homeworkCardMap.put(RES_HOMEWORK_ID, data.get("homeworkId"));
        homeworkCardMap.put(RES_HOMEWORK_END_DATE, data.get("endDate"));
        if (data.get("finishCount") != null) {
            homeworkCardMap.put(RES_HOMEWORK_FINISH_COUNT, data.get("finishCount"));
        }
        if (data.get("homeworkCount") != null) {
            homeworkCardMap.put(RES_HOMEWORK_COUNT, data.get("homeworkCount"));
        }
        // 配置中命中了：此app的版本支持当前作业类型
        AppHomeworkCardFilter.HomeworkCardInfo info = AppHomeworkCardFilter.generateHomeworkCardInfo(configMap, homeworkType, types, ver, sys);
        boolean supported = info.getSupportType() == AppHomeworkCardFilter.HomeworkCardSupportType.SUPPORTED;
        homeworkCardMap.put(RES_HOMEWORK_CARD_SUPPORT_FLAG, supported);
        if (!supported && info.getSupportType() != null) {
            homeworkCardMap.put(RES_NOT_SUPPORT_HOMEWORK_LINK_PARAM, buildNotSupportParams(SafeConverter.toString(data.get("homeworkId")), homeworkType, desc, info.getSupportType().name(), info.getNoSupportObjectiveConfigType()));
        }
        homeworkCardMap.put(RES_HOMEWORK_CARD_SOURCE, info.getSourceType());
        homeworkCardMap.put(RES_HOMEWORK_CARD_VARIETY, info.getHomeworkOrQuiz());

        return homeworkCardMap;
    }

    private String buildNotSupportParams(String homeworkId, String homeworkType, String homeworkDesc, String cardSupportType, String noSupportObjectiveConfigType) {
        Map<String, Object> notSupportParam = new HashMap<>();
        notSupportParam.put(RES_HOMEWORK_ID, homeworkId);
        notSupportParam.put(RES_HOMEWORK_TYPE, homeworkType);
        notSupportParam.put(RES_HOMEWORK_CARD_DESC, homeworkDesc);
        notSupportParam.put(RES_HOMEWORK_CARD_SUPPORT_TYPE, cardSupportType);
        notSupportParam.put(RES_IMG_DOMAIN, getCdnBaseUrlStaticSharedWithSep());
        notSupportParam.put(RES_NO_SUPPORT_OBJECTIVE_CONFIG_TYPE, noSupportObjectiveConfigType);
        return JsonUtils.toJson(notSupportParam);
    }

}
