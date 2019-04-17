package com.voxlearning.utopia.admin.service.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.data.*;
import com.voxlearning.utopia.admin.util.UserInfoSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.*;
import com.voxlearning.utopia.service.ai.cache.UserScoreRankCache;
import com.voxlearning.utopia.service.ai.cache.UserShareVideoRankCache;
import com.voxlearning.utopia.service.ai.client.AiChipsEnglishConfigServiceClient;
import com.voxlearning.utopia.service.ai.client.AiLoaderClient;
import com.voxlearning.utopia.service.ai.client.AiOrderProductServiceClient;
import com.voxlearning.utopia.service.ai.constant.ChipsEnglishLevel;
import com.voxlearning.utopia.service.ai.constant.ChipsEnglishTeacher;
import com.voxlearning.utopia.service.ai.constant.PageViewType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wonderland.api.entity.WonderlandPromotionChannel;
import com.voxlearning.utopia.service.wonderland.client.WonderlandPromotionChannelServiceClient;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author guangqing
 * @since 2018/8/21
 */
@Service
public class ClazzManagerService {

    private static final String[] units = {"", "十", "百", "千"};
    private static final char[] numArray = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'};
    private static Long[] TEST_USER = {265026L, 262870L};
    private static Long[] ONLINE_USER = {215602147L, 225303466L, 222080759L, 222207511L, 223122061L, 225492190L};
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private AiLoaderClient aiLoaderClient;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private WonderlandPromotionChannelServiceClient wonderlandPromotionChannelServiceClient;

    @Inject
    private AiOrderProductServiceClient orderProductServiceClient;

    @ImportService(interfaceClass = ChipsUserOralScheduleService.class)
    private ChipsUserOralScheduleService chipsUserOralScheduleService;

    //    @ImportService(interfaceClass = WechatLoader.class)
//    private WechatLoader wechatLoader;
    @ImportService(interfaceClass = ChipsActiveService.class)
    private ChipsActiveService chipsActiveService;

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @ImportService(interfaceClass = ChipsEnglishUserLoader.class)
    private ChipsEnglishUserLoader chipsEnglishUserLoader;

    @Inject
    private AiChipsEnglishConfigServiceClient chipsEnglishConfigServiceClient;

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    @ImportService(interfaceClass = ChipsUserPageViewLogService.class)
    private ChipsUserPageViewLogService chipsUserPageViewLogService;

    @ImportService(interfaceClass = ChipsTaskLoader.class)
    private ChipsTaskLoader chipsTaskLoader;

    public List<ClazzCrmPojo> selectClazzCrmPojo(String clazzName, String clazzTeacherName, String productId) {
        if (StringUtils.isBlank(clazzName) && StringUtils.isBlank(clazzTeacherName) && StringUtils.isBlank(productId)) {
            return null;
        }
        List<ChipsEnglishClass> chipsClazzList;
        if (StringUtils.isBlank(clazzTeacherName) && StringUtils.isBlank(productId)) {
            chipsClazzList = chipsEnglishClazzService.selectAllChipsEnglishClass();
        } else {
            chipsClazzList = chipsEnglishClazzService.selectChipsEnglishClassByProductIdTeacherName(productId, clazzTeacherName);
        }
        if (StringUtils.isNotBlank(clazzName)) {
            //根据班名进行过滤
            chipsClazzList = filterByClazzName(chipsClazzList, clazzName);
        }
        if (CollectionUtils.isEmpty(chipsClazzList)) {
            return null;
        }
        return buildClazzCrmPojoAttr(chipsClazzList);
    }

    /**
     * 构建基础信息展示对象 --list对象
     */
    private List<ClazzCrmPojo> buildClazzCrmPojoAttr(List<ChipsEnglishClass> clazzList) {
        if (CollectionUtils.isEmpty(clazzList)) {
            return null;
        }
        List<ClazzCrmPojo> list = new ArrayList<>();
        for (ChipsEnglishClass chipsClazz : clazzList) {
            if (chipsClazz == null) {
                continue;
            }
            ClazzCrmPojo pojo = buildClazzCrmPojoAttr(chipsClazz);
            list.add(pojo);
        }
        return list;
    }

    /**
     * 构建基础信息展示对象 --单个对象
     */
    private ClazzCrmPojo buildClazzCrmPojoAttr(ChipsEnglishClass chipsClazz) {
        if (chipsClazz == null) {
            return null;
        }
        ClazzCrmPojo pojo = new ClazzCrmPojo();
        pojo.setClazzId(chipsClazz.getId());
        pojo.setClazzName(chipsClazz.getName());
        pojo.setClazzTeacherName(chipsClazz.getTeacher());
        pojo.setUserLimitation(chipsClazz.getUserLimit());
        OrderProduct product = getOrderProductById(chipsClazz.getProductId());
        pojo.setProductId(product == null ? null : product.getId());
        pojo.setProductName(product == null ? "" : product.getName());
        List<String> bookNameList = getBookName(product);
        pojo.setBookName(bookNameToString(bookNameList));
        pojo.setCreateTime(DateUtils.dateToString(chipsClazz.getCreateTime(), "yyyy/MM/dd"));
        pojo.setUserCount(getUserCount(chipsClazz));
        pojo.setType(chipsClazz.getType() != null ? chipsClazz.getType().name() : "");
        pojo.setTypeDesc(chipsClazz.getType() != null ? chipsClazz.getType().getDescription() : "");
        return pojo;
    }

    private String bookNameToString(List<String> bookNameList) {
        if (CollectionUtils.isEmpty(bookNameList)) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bookNameList.size(); i++) {
                if (i == 0) {
                    sb.append(bookNameList.get(i));
                } else {
                    sb.append(",").append(bookNameList.get(i));
                }
            }
            return sb.toString();
        }
    }

    private int getUserCount(ChipsEnglishClass chipsClazz) {
        List<ChipsEnglishClassUserRef> userList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(chipsClazz.getId());
        return CollectionUtils.isEmpty(userList) ? 0 : userList.size();
    }

    private List<String> getBookName(OrderProduct product) {
        if (product == null || StringUtils.isBlank(product.getId())) {
            return null;
        }
        List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
        if (CollectionUtils.isEmpty(orderProductItemList)) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (OrderProductItem item : orderProductItemList) {
            if (item == null || item.getName() == null) {
                continue;
            }
            list.add(item.getName());
        }
        return list;
    }

    public MapMessage saveOrUpdateChipsEnglishClass(Long clazzId, String clazzName, String teacherName, String productId, Integer userLimitation, String type) {
        ChipsEnglishClass clazz = new ChipsEnglishClass();
        clazz.setId(clazzId);
        clazz.setName(clazzName);
        clazz.setTeacher(teacherName);
        clazz.setProductId(productId);
        clazz.setUserLimit(userLimitation);
        clazz.setDisabled(false);
        clazz.setType(ChipsEnglishClass.Type.safeOf(type));
        return chipsEnglishClazzService.saveOrUpdateChipsEnglishClass(clazz);
    }

    private OrderProduct getOrderProductById(String productId) {
        if (StringUtils.isBlank(productId)) {
            return null;
        }
        return userOrderLoaderClient.loadOrderProductById(productId);
    }

    private List<ChipsEnglishClass> filterByClazzName(List<ChipsEnglishClass> clazzList, String reg) {
        if (CollectionUtils.isEmpty(clazzList) || StringUtils.isBlank(reg)) {
            return clazzList;
        }
        return clazzList.stream().filter(c -> c != null && match(c.getName(), reg)).collect(Collectors.toList());
    }

    private static boolean match(String clazzName, String reg) {
        if (StringUtils.isBlank(clazzName) || StringUtils.isBlank(reg)) {
            return false;
        }
        Pattern p = Pattern.compile(reg.replaceAll("\\*", "\\.\\*"));
        Matcher m = p.matcher(clazzName);
        return m.matches();
    }

    public List<SelectOption> buildProdctListByType(List<String> typeList) {
        List<OrderProduct> productList = orderProductServiceClient.getRemoteReference().loadProductByType(typeList);
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }
        List<SelectOption> optionList = new ArrayList<>();
        productList.forEach(p -> {
            SelectOption op = new SelectOption();
            op.setDesc(p.getName());
            op.setValue(p.getId());
            op.setSelected(false);
            optionList.add(op);
        });
        return optionList;
    }

    public List<OrderProduct> loadOrderProductByProductType(List<String> typeList) {
        return orderProductServiceClient.getRemoteReference().loadProductByType(typeList);
    }

    public List<SelectOption> buildProductTypeList(List<String> typeList) {
        SelectOption o1 = new SelectOption("1", "已完结");
        SelectOption o2 = new SelectOption("2", "当前");
        SelectOption o3 = new SelectOption("3", "未开始");
        List<SelectOption> list = new ArrayList<>();
        o1.setSelected(CollectionUtils.isNotEmpty(typeList) && typeList.contains("1"));
        o2.setSelected(CollectionUtils.isNotEmpty(typeList) && typeList.contains("2"));
        o3.setSelected(CollectionUtils.isNotEmpty(typeList) && typeList.contains("3"));
        list.add(o1);
        list.add(o2);
        list.add(o3);
        return list;
    }


    public List<SelectOption> buildProductTypeList2(List<String> typeList) {
//        SelectOption o1 = new SelectOption("1", "已完结");
        SelectOption o2 = new SelectOption("2", "当前");
        SelectOption o3 = new SelectOption("3", "未开始");
        List<SelectOption> list = new ArrayList<>();
//        o1.setSelected(CollectionUtils.isNotEmpty(typeList) && typeList.contains("1"));
        o2.setSelected(CollectionUtils.isNotEmpty(typeList) && typeList.contains("2"));
        o3.setSelected(CollectionUtils.isNotEmpty(typeList) && typeList.contains("3"));
//        list.add(o1);
        list.add(o2);
        list.add(o3);
        return list;
    }

    public List<SelectOption> buildProductSelectOptionList(List<OrderProduct> productList, String selectedValue) {
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }
        List<SelectOption> optionList = new ArrayList<>();
        productList.forEach(p -> {
            SelectOption op = new SelectOption();
            op.setDesc(p.getName());
            op.setValue(p.getId());
            op.setSelected(StringUtils.isNotBlank(selectedValue) && p.getId().equals(selectedValue));
            optionList.add(op);
        });
        return optionList;
    }

    /**
     * 构建产品下拉框
     */
    public List<SelectOption> buildProductSelectOptionList(String selectedValue) {
        List<OrderProduct> productList = userOrderLoaderClient.loadAllOrderProductIncludeOfflineForCrm().stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish).collect(Collectors.toList());
        return buildProductSelectOptionList(productList, selectedValue);
    }

    /**
     * 构建产品下拉框
     */
    public List<SelectOption> buildClazzSelectOptionList(String productId, Long clazzId) {
        if (StringUtils.isBlank(productId)) {
            return Collections.emptyList();
        }
        List<ChipsEnglishClass> clazzList = chipsEnglishClazzService.selectChipsEnglishClassByProductId(productId);
        if (CollectionUtils.isEmpty(clazzList)) {
            return Collections.emptyList();
        }

        List<SelectOption> optionList = new ArrayList<>();
        clazzList.forEach(p -> {
            SelectOption op = new SelectOption();
            op.setDesc(p.getName());
            op.setValue(p.getId());
            op.setSelected(clazzId != null && (p.getId() + "").equals(clazzId + ""));
            optionList.add(op);
        });
        return optionList;
    }

    public List<SelectOption> buildClazzSelectOptionDescTeacherList(String productId, Long clazzId) {
        if (StringUtils.isBlank(productId)) {
            return Collections.emptyList();
        }
        List<ChipsEnglishClass> clazzList = chipsEnglishClazzService.selectChipsEnglishClassByProductId(productId);
        if (CollectionUtils.isEmpty(clazzList)) {
            return Collections.emptyList();
        }

        List<SelectOption> optionList = new ArrayList<>();
        clazzList.forEach(p -> {
            SelectOption op = new SelectOption();
            op.setDesc(p.getTeacher());
            op.setValue(p.getId());
            op.setSelected(clazzId != null && (p.getId() + "").equals(clazzId + ""));
            optionList.add(op);
        });
        return optionList;
    }

    /**
     * 是否进微信群，下拉
     *
     * @param selectedValue 值有是，否，空
     */
    public List<SelectOption> buildJoinGroupSelectOptionList(Boolean selectedValue) {
        List<SelectOption> optionList = new ArrayList<>();
        SelectOption opY = new SelectOption();
        opY.setDesc("是");
        opY.setValue(true);
        opY.setSelected(selectedValue != null && Boolean.TRUE.equals(selectedValue));
        optionList.add(opY);
        SelectOption opN = new SelectOption();
        opN.setDesc("否");
        opN.setValue(false);
        opN.setSelected(selectedValue != null && Boolean.FALSE.equals(selectedValue));
        optionList.add(opN);
        return optionList;
    }

    public MapMessage saveUserModifyInfo(Long clazzId, Long userId, String wechatNumber, Boolean joinedGroup, String duration) {
        return chipsEnglishClazzService.saveUserRefExt(clazzId, userId, wechatNumber, joinedGroup, duration);
    }

    public MapMessage saveShowPlay(List<Long> userIdList, boolean isChecked) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return MapMessage.successMessage();
        }
        return chipsEnglishClazzService.insertOrUpdateUserExt(userIdList, isChecked);
    }

    public MapMessage saveWxAddStatus(Long userId, boolean wxAddStatus) {
        if (userId == null) {
            return MapMessage.successMessage();
        }
        return chipsEnglishClazzService.insertOrUpdateUserExtWxAddStatus(userId, wxAddStatus);
    }
    public MapMessage saveEpWxAddStatus(Long userId, boolean epWxAddStatus) {
        if (userId == null) {
            return MapMessage.successMessage();
        }
        return chipsEnglishClazzService.insertOrUpdateUserExtEpWxAddStatus(userId, epWxAddStatus);
    }

    public ClazzCrmOperatingPojo selectWechatNumberInGroupDuration(Long userId, Long clazzId) {
        if (userId == null || userId == 0L || clazzId == null || clazzId == 0L) {
            return null;
        }
        ChipsEnglishClassUserRef userRef = chipsEnglishClazzService.selectChipsEnglishClassUserRefByUserId(userId, clazzId);
//        ChipsEnglishUserExt userExt = chipsEnglishClazzService.selectChipsEnglishUserExtByUserId(userId);
        ChipsEnglishUserExtSplit userExtSplit = chipsEnglishClazzService.selectChipsEnglishUserExtSplitByUserId(userId);
        ClazzCrmOperatingPojo pojo = new ClazzCrmOperatingPojo();
        if (userRef != null) {
            pojo.setJoinedGroup(userRef.getInGroup());
        }
        if (userExtSplit != null) {
            if (userExtSplit.getStudyDuration() != null) {
                pojo.setDuration(userExtSplit.getStudyDuration());
            }
            pojo.setWechatNumber(SafeConverter.toString(userExtSplit.getWxCode(), ""));
        }
        return pojo;
    }

    public List<SelectOption> buildGradingSelectOptionList(String selectedValue) {
        List<SelectOption> optionList = new ArrayList<>();
        for (ChipsEnglishLevel level : ChipsEnglishLevel.values()) {
            SelectOption op = new SelectOption();
            op.setValue(level.getDescription());
            op.setDesc(level.getDescription());
            op.setSelected(StringUtils.isNotBlank(selectedValue) && level.getDescription().equals(selectedValue));
            optionList.add(op);
        }
        return optionList;
    }

    /**
     * 构建班主任下拉框
     */
    public List<SelectOption> buildTeacherSelectOptionList(String selectedValue) {
        List<SelectOption> optionList = new ArrayList<>();
        for (ChipsEnglishTeacher chipsEnglishTeacher : ChipsEnglishTeacher.values()) {
            SelectOption op = new SelectOption();
            op.setValue(chipsEnglishTeacher.name());
            op.setDesc(chipsEnglishTeacher.name());
            op.setSelected(StringUtils.isNotBlank(selectedValue) && chipsEnglishTeacher.name().equals(selectedValue));
            optionList.add(op);
        }
        return optionList;
    }

    public ClazzCrmPojo selectClazzById(Long clazzId) {
        if (clazzId == null) {
            return null;
        }
        ChipsEnglishClass chipsClazz = chipsEnglishClazzService.selectChipsEnglishClassById(clazzId);
        return buildClazzCrmPojoAttr(chipsClazz);
    }

    public List<SelectOption> buildAimClazzSelectOptionList(String productId, Long clazzId) {
        if (StringUtils.isBlank(productId)) {
            return null;
        }
        List<ChipsEnglishClass> chipsClazzList = chipsEnglishClazzService.selectChipsEnglishClassByProductId(productId);
        if (CollectionUtils.isEmpty(chipsClazzList)) {
            return null;
        }
        List<SelectOption> optionList = new ArrayList<>();
        for (ChipsEnglishClass clazz : chipsClazzList) {
            if (clazz == null || clazz.getId() == null || clazz.getId().equals(clazzId)) {
                continue;
            }
            SelectOption op = new SelectOption();
            op.setValue(clazz.getId() + "");
            op.setDesc(clazz.getName());
            op.setSelected(false);
            optionList.add(op);
        }
        return optionList;
    }

    public MapMessage mergeChipsEnglishClazz(Long clazzId, Long aimClazzId) {
        return chipsEnglishClazzService.mergeChipsEnglishClass(clazzId, aimClazzId);
    }

    public boolean isUserLimitationOverFlow(Long clazzId, Long aimClazzId) {
        if (clazzId == null || aimClazzId == null) {
            return false;
        }
        ChipsEnglishClass clazz = chipsEnglishClazzService.selectChipsEnglishClassById(clazzId);
        ChipsEnglishClass aimClazz = chipsEnglishClazzService.selectChipsEnglishClassById(aimClazzId);
        if (clazz == null || aimClazz == null) {
            return false;
        }
        int clazzUserCount = getUserCount(clazz);
        int aimClazzUserCount = getUserCount(aimClazz);
        return aimClazz.getUserLimit() < (clazzUserCount + aimClazzUserCount);
    }

    private String formatRate(Integer numerator, Integer denominator) {
        if (numerator == null || numerator == 0 || denominator == null || denominator == 0) {
            return "0.00";
        }
        double val = new BigDecimal(numerator).divide(new BigDecimal(denominator), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return val + "";
    }

    public int buildMaxUnitCount(String productId) {
        List<OrderProductItem> productItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        if (CollectionUtils.isEmpty(productItemList)) {
            return 0;
        }
        int count = 0;
        for (OrderProductItem item : productItemList) {
            List<StoneUnitData> stoneUnitData = chipsEnglishContentLoader.fetchUnitListExcludeTrialV2(item.getAppItemId());
            if (CollectionUtils.isEmpty(stoneUnitData)) {
                continue;
            }
            if (stoneUnitData.size() > count) {
                count = stoneUnitData.size();
            }
        }
        return count;
    }

    public ClazzCrmBasicPojoV2 buildClazzCrmBasicPojoV2(Long clazzId) {
        if (clazzId == null || clazzId == 0L) {
            return null;
        }
        List<ChipsClassStatistics> list = chipsEnglishClazzService.selectChipsClassStatisticsByClazzId(clazzId);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        ChipsEnglishClass clazz = chipsEnglishClazzService.selectChipsEnglishClassById(clazzId);
        int clazzPaidCount = chipsEnglishClazzService.calRenewCount(clazz);
        int totalPaidCount = calTotalPaidCount(clazz);
        ClazzCrmBasicPojoV2 pojoV2 = ClazzCrmBasicPojoV2.valueOf(list.get(0), clazzPaidCount, totalPaidCount);
        return pojoV2;
    }

    private int calTotalPaidCount(ChipsEnglishClass clazz) {
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return 0;
        }
        List<ChipsEnglishClass> clazzList = chipsEnglishClazzService.selectChipsEnglishClassByProductId(clazz.getProductId());
        int totalCount = 0;
        for (ChipsEnglishClass c : clazzList) {
            totalCount += chipsEnglishClazzService.calRenewCount(c);
        }
        return totalCount;
    }

    /**
     * 构建班级下的基础信息tab的前端展示数据
     */
    public List<ClazzCrmBasicPojo> buildClazzCrmBasicPojo(Long clazzId, String productId) {
        if (clazzId == null || clazzId == 0L || StringUtils.isBlank(productId)) {
            return Collections.emptyList();
        }
        List<OrderProductItem> productItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        if (CollectionUtils.isEmpty(productItemList)) {
            return Collections.emptyList();
        }
        Map<String, ChipsEnglishClassStatistics> unitToClazzStatMap = unitToClazzStatMap(chipsEnglishClazzService.selectChipsEnglishClassStatisticsByClazzId(clazzId));
        if (MapUtils.isEmpty(unitToClazzStatMap)) {
            return Collections.emptyList();
        }
        return productItemList.stream().filter(item -> item != null && StringUtils.isNotEmpty(item.getAppItemId())).flatMap(item -> {
            List<StoneUnitData> unitDataList = chipsEnglishContentLoader.fetchUnitListExcludeTrialV2(item.getAppItemId());
            //过滤掉unitDateList 中的null元素
            Optional<List<StoneUnitData>> unitDataOptional = Optional.ofNullable(unitDataList).map(l -> l.stream().filter(u -> u != null && StringUtils.isNotBlank(u.getId()) && unitToClazzStatMap.get(u.getId()) != null).collect(Collectors.toList()));
            return unitDataOptional.map(l -> Stream.iterate(0, i -> i + 1).limit(l.size())
                    .map(i -> buildClazzCrmBasicPojo(i + 1, l.get(i).getId(),
                            unitToClazzStatMap.get(l.get(i).getId())))).orElse(Stream.empty());
        }).collect(Collectors.toList());
    }


    /**
     * 构建班级下的基础信息tab的前端展示数据（对应基础信息页面 最新数据 选项）
     */
    public List<ClazzCrmBasicPojo> buildLatestClazzCrmBasicPojo(Long clazzId, String productId) {
        if (clazzId == null || clazzId == 0L || StringUtils.isBlank(productId)) {
            return Collections.emptyList();
        }
        List<OrderProductItem> productItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        if (CollectionUtils.isEmpty(productItemList)) {
            return Collections.emptyList();
        }
        Map<String, ChipsEnglishClassStatisticsLatest> unitToClazzStatMap = unitToClazzStatLatestMap(chipsEnglishClazzService.selectChipsEnglishClassStatisticsLatestByClazzId(clazzId));
        if (MapUtils.isEmpty(unitToClazzStatMap)) {
            return Collections.emptyList();
        }
        return productItemList.stream().filter(item -> item != null && StringUtils.isNotEmpty(item.getAppItemId())).flatMap(item -> {
            List<StoneUnitData> unitDataList = chipsEnglishContentLoader.fetchUnitListExcludeTrialV2(item.getAppItemId());
            //过滤掉unitDateList 中的null元素
            Optional<List<StoneUnitData>> unitDataOptional = Optional.ofNullable(unitDataList)
                    .map(l -> l.stream()
                            .filter(u -> u != null && StringUtils.isNotBlank(u.getId()) && unitToClazzStatMap.get(u.getId()) != null)
                            .collect(Collectors.toList()));
            return unitDataOptional.map(
                    l -> Stream.iterate(0, i -> i + 1)
                            .limit(l.size())
                            .map(i -> buildLatestClazzCrmBasicPojo(i + 1, l.get(i).getId(), unitToClazzStatMap.get(l.get(i).getId()))))
                    .orElse(Stream.empty());
        }).collect(Collectors.toList());
    }

    private String getUnitName(String unitId) {
        if (StringUtils.isBlank(unitId)) {
            return null;
        }
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(unitId));
        if (MapUtils.isEmpty(stoneDataMap)) {
            return null;
        }
        StoneData stoneData = stoneDataMap.get(unitId);
        if (stoneData == null) {
            return null;
        }
        StoneUnitData stoneUnitData = StoneUnitData.newInstance(stoneData);
        if (stoneUnitData.getJsonData() == null) {
            return null;
        }
        return stoneUnitData.getJsonData().getName();
    }

    private ClazzCrmBasicPojo buildClazzCrmBasicPojo(int lessonNum, String unitId, ChipsEnglishClassStatistics clazzStatistics) {
        return ClazzCrmBasicPojo.valueOf(lessonNum, unitId, getUnitName(unitId), clazzStatistics);
    }

    private ClazzCrmBasicPojo buildLatestClazzCrmBasicPojo(int lessonNum, String unitId, ChipsEnglishClassStatisticsLatest clazzStatisticsLatest) {
        return ClazzCrmBasicPojo.valueOf(lessonNum, unitId, getUnitName(unitId), clazzStatisticsLatest);
    }

    private Map<String, ChipsEnglishClassStatistics> unitToClazzStatMap(List<ChipsEnglishClassStatistics> clazzStatisticsList) {
        if (CollectionUtils.isEmpty(clazzStatisticsList)) {
            return null;
        }
        return clazzStatisticsList.stream().filter(s -> s != null && StringUtils.isNotEmpty(s.getUnitId()))
                .collect(Collectors.toMap(ChipsEnglishClassStatistics::getUnitId, Function.identity()));
    }


    private Map<String, ChipsEnglishClassStatisticsLatest> unitToClazzStatLatestMap(List<ChipsEnglishClassStatisticsLatest> clazzStatisticsList) {
        if (CollectionUtils.isEmpty(clazzStatisticsList)) {
            return null;
        }
        return clazzStatisticsList.stream().filter(s -> s != null && StringUtils.isNotEmpty(s.getUnitId()))
                .collect(Collectors.toMap(ChipsEnglishClassStatisticsLatest::getUnitId, Function.identity()));
    }

    public List<String[]> buildUserScoreLessonTitle2(int count) {
        List<String[]> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String num = formatInteger(i);
            String[] arr = new String[2];
            arr[0] = "第" + num + "课";
            arr[1] = "Day " + i + "";
            list.add(arr);
        }
        return list;
    }

    public List<String> buildUserScoreLessonTitle(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String num = formatInteger(i);
            list.add("第" + num + "课(Day" + i + ")");
        }
        return list;
    }

    private List<Long> filterGrading(List<Long> userList, String grading) {
        if (StringUtils.isBlank(grading)) {
            return userList;
        }
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }
        Map<Long, ChipsEnglishUserExtSplit> idToExtMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userList);
        if (MapUtils.isEmpty(idToExtMap)) {
            return null;
        }
        List<Long> list = new ArrayList<>();
        for (Long userId : userList) {
            ChipsEnglishUserExtSplit userExt = idToExtMap.get(userId);
            if (userExt == null || userExt.getId() == null || userExt.getLevel() == null) {
                continue;
            }
            if (userExt.getLevel().getDescription().equals(grading)) {
                list.add(userExt.getId());
            }
        }
        return list;
    }

    public List<Long> pageUserList(Long clazzId, Long userId, String gradIng) {
        if (clazzId == null || clazzId == 0L) {
            return null;
        }
        List<Long> userIdList = chipsEnglishClazzService.selectAllUserByClazzId(clazzId);
        List<Long> filteredUserList = filterGrading(userIdList, gradIng);
        if (CollectionUtils.isEmpty(filteredUserList)) {
            return null;
        }
        if (userId != null && userId != 0) {
            if (!filteredUserList.contains(userId)) {
                return null;
            } else {
                List<Long> list = new ArrayList<>();
                list.add(userId);
                return list;
            }
        }
        return filteredUserList;
    }

    public List<ClazzCrmUserScorePojo> buildUserScore(List<Long> userIdList, Map<Long, List<AIUserInfoWithScore>> userIdToScoreMap, int lessonCount, Long clazzId) {
        if (MapUtils.isEmpty(userIdToScoreMap)) {
            return null;
        }
        Map<Long, Integer> servicedCountMap = chipsActiveService.isActiveServiced(userIdList, clazzId);
        List<ClazzCrmUserScorePojo> list = new ArrayList<>();
        for (Long id : userIdList) {
            List<AIUserInfoWithScore> scoreList = userIdToScoreMap.get(id);
            List<ClazzCrmUserScorePojo> pojoList = buildUserScore(scoreList, lessonCount, servicedCountMap.get(id));
            if (pojoList == null) {
                continue;
            }
            list.addAll(pojoList);
        }
        return list;
    }


    public List<ClazzCrmUserScorePojo> buildClazzAvgScore(Long clazzId, String productId, int lessonCount) {
        if (clazzId == null || clazzId == 0L) {
            return buildClazzAvgScoreEmpty(lessonCount);
        }
        Map<String, ChipsEnglishClassStatistics> unitToClazzStatMap = unitToClazzStatMap(chipsEnglishClazzService.selectChipsEnglishClassStatisticsByClazzId(clazzId));
        if (MapUtils.isEmpty(unitToClazzStatMap)) {
            return buildClazzAvgScoreEmpty(lessonCount);
        }
        List<OrderProductItem> productItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        if (CollectionUtils.isEmpty(productItemList)) {
            return new ArrayList<>();
        }
        List<ClazzCrmUserScorePojo> list = new ArrayList<>();
        for (OrderProductItem item : productItemList) {
            List<StoneUnitData> stoneUnitData = chipsEnglishContentLoader.fetchUnitListExcludeTrialV2(item.getAppItemId());
            ClazzCrmUserScorePojo pojo = new ClazzCrmUserScorePojo();
            List<String> avgLessonList = new ArrayList<>();
            for (StoneUnitData aStoneUnitData : stoneUnitData) {
                String unitId = aStoneUnitData.getId();
                ChipsEnglishClassStatistics clazzStat = unitToClazzStatMap.get(unitId);
                if (clazzStat == null || clazzStat.getClassScore() == null) {
                    avgLessonList.add("--");
                } else {
                    avgLessonList.add(formatRate(clazzStat.getClassScore(), clazzStat.getClassFinishNum()));
                }
            }
            pojo.setUserName("班级平均");
            pojo.setLessonScoreList(avgLessonList);
            list.add(pojo);
        }
        return list;
    }

    private List<ClazzCrmUserScorePojo> buildClazzAvgScoreEmpty(int lessonCount) {
        List<ClazzCrmUserScorePojo> list = new ArrayList<>();
        ClazzCrmUserScorePojo pojo = new ClazzCrmUserScorePojo();
        List<String> avgLessonList = new ArrayList<>();
        for (int i = 0; i < lessonCount; i++) {
            avgLessonList.add("--");
        }
        pojo.setUserName("班级平均");
        pojo.setLessonScoreList(avgLessonList);
        list.add(pojo);
        return list;
    }

    private List<ClazzCrmUserScorePojo> buildUserScore(List<AIUserInfoWithScore> scoreList, int lessonCount, Integer servicedCount) {
        if (CollectionUtils.isEmpty(scoreList)) {
            return null;
        }
        List<ClazzCrmUserScorePojo> list = new ArrayList<>();
        for (AIUserInfoWithScore userInfo : scoreList) {
            ClazzCrmUserScorePojo pojo = new ClazzCrmUserScorePojo();
            pojo.setUserId(userInfo.getId());
            pojo.setUserName(userInfo.getName());
            pojo.setProductName(userInfo.getProductName());
            pojo.setBookId(userInfo.getBookId());
            pojo.setProductItemName(userInfo.getProductItemName());
            List<AIUserUnitScore> unitScoreList = userInfo.getScoreLis();
            pojo.setLessonScoreList(toScoreListOrderByRank(unitScoreList, lessonCount));
            pojo.setCompleteRate(finishCount(unitScoreList) + " / " + (unitScoreList == null ? 0 : unitScoreList.size()));
            pojo.setServicedRate(servicedCount + " / " + (unitScoreList == null ? 0 : unitScoreList.size()));
            pojo.setGrading(userInfo.getLevel() == null ? "" : userInfo.getLevel().getDescription());
            pojo.setShowPlay(userInfo.getShowPlay() == null ? false : userInfo.getShowPlay());
            list.add(pojo);
        }
        return list;
    }

    private int finishCount(List<AIUserUnitScore> unitScoreList) {
        if (CollectionUtils.isEmpty(unitScoreList)) {
            return 0;
        }
        int count = 0;
        for (AIUserUnitScore score : unitScoreList) {
            if (score != null && score.getFinished() != null && score.getFinished()) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param consumeMin -1
     * @param consumeMax -1 代表没有这个过滤条件
     */
    private boolean filterConsumeJZT(ChipsEnglishUserExt userExt, Double consumeMin, Double consumeMax) {
        if (consumeMin == null && consumeMax == null) {
            return true;
        }
        if (userExt == null || userExt.getJztConsume() == null) {
            return false;
        }
        if (consumeMin == null) {
            return userExt.getJztConsume().doubleValue() <= consumeMax;
        }
        if (consumeMax == null) {
            return userExt.getJztConsume().doubleValue() >= consumeMin;
        }
        return userExt.getJztConsume().doubleValue() >= consumeMin && userExt.getJztConsume().doubleValue() <= consumeMax;
    }

    private List<String> toScoreListOrderByRank(List<AIUserUnitScore> unitScoreList, int lessonCount) {
        if (CollectionUtils.isEmpty(unitScoreList)) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < lessonCount; i++) {
                list.add("");
            }
            return list;
        }
        unitScoreList.sort(Comparator.comparing(AIUserUnitScore::getRank));
        List<String> list = new ArrayList<>();
        for (AIUserUnitScore unitScore : unitScoreList) {
            if (unitScore == null || unitScore.getScore() == null || unitScore.getScore() == -1) {
                list.add("--");
            } else {
                list.add(unitScore.getScore() + "");
            }
        }
        if (list.size() < lessonCount) {
            int count = lessonCount - list.size();
            for (int i = 0; i < count; i++) {
                list.add("");
            }
        }
        return list;
    }

    public Map<Long, List<AIUserInfoWithScore>> buildAllUserScoreByClazz(Long clazzId, List<Long> userIdList) {
        if (clazzId == null || clazzId == 0L) {
            return null;
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            return null;
        }
        Map<Long, List<AIUserInfoWithScore>> userIdToScoreMap = chipsEnglishUserLoader.loadClassSingleUserInfoWithScore(clazzId, userIdList);
        if (MapUtils.isEmpty(userIdToScoreMap)) {
            return null;
        }
        return userIdToScoreMap;
    }

    public List<Long> buildOperatingUserIdList(Long clazzId, Long userId, String gradIng, Double consumeMin, Double consumeMax) {
        if (clazzId == null || 0L == clazzId) {
            return null;
        }
        List<Long> userIdList = chipsEnglishClazzService.selectAllUserByClazzId(clazzId);
        if (userId != null && userId != 0L) {
            if (CollectionUtils.isEmpty(userIdList) || !userIdList.contains(userId)) {
                return null;
            }
            ChipsEnglishUserExt userExt = chipsEnglishClazzService.selectChipsEnglishUserExtByUserId(userId);
            ChipsEnglishUserExtSplit userExtSplit = chipsEnglishClazzService.selectChipsEnglishUserExtSplitByUserId(userId);
            boolean flag = filterGradingConsumeJZT(userExt, userExtSplit, gradIng, consumeMin, consumeMax);
            if (!flag) {
                return null;
            }
            List<Long> list = new ArrayList<>();
            list.add(userId);
            return list;
        }
        List<ChipsEnglishUserExt> userExtList = chipsEnglishClazzService.selectChipsEnglishUserExtByUserIds(userIdList);
        Map<Long, ChipsEnglishUserExt> userExtMap = userIdToUserExtMap(userExtList);
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        return filterGradingConsumeJZT(userIdList, userExtMap, userExtSplitMap, gradIng, consumeMin, consumeMax);
    }

    public List<ClazzCrmOperatingPojo> buildOperatingList(Long clazzId, List<Long> userIdList) {
        List<ChipsEnglishUserExt> userExtList = chipsEnglishClazzService.selectChipsEnglishUserExtByUserIds(userIdList);
        Map<Long, ChipsEnglishUserExt> userExtMap = userIdToUserExtMap(userExtList);
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazzId);
        Map<Long, ChipsEnglishClassUserRef> userRefMap = userIdToUserRefMap(userRefList);
        return buildOperatingList(userIdList, userExtSplitMap, userExtMap, userRefMap);
    }

    public List<ClazzCrmOralTestPojo> buildOralTestList(Long clazzId,List<Long> userIdList) {
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        if (MapUtils.isEmpty(userExtSplitMap)) {
            return Collections.emptyList();
        }
        Map<Long, User> userIdToUserMap = userLoaderClient.loadUsers(userIdList);
        List<ChipsUserOralTestSchedule> scheduleList = chipsUserOralScheduleService.loadByClazzId(clazzId);
        return scheduleList.stream().filter(e -> e.getUserId() != null).filter(e -> userIdToUserMap.get(e.getUserId()) != null)
                .map(e -> {
                    ClazzCrmOralTestPojo pojo = new ClazzCrmOralTestPojo();
                    pojo.setId(e.getUserId());
                    pojo.setName(getUserName(userIdToUserMap.get(e.getUserId())));
                    pojo.setTestDay(formatTestDay(e.getTestBeginTime()));
                    pojo.setTestRegion(formatTestRegion(e.getTestBeginTime(), e.getTestEndTime()));
                    pojo.setUpdateTime(e.getUpdateTime());
                    return pojo;
                }) .collect(Collectors.toList());
    }

    public List<ClazzCrmMailAddressPojo> buildMailAddressList(List<Long> userIdList) {
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        if (MapUtils.isEmpty(userExtSplitMap)) {
            return Collections.emptyList();
        }
        Map<Long, User> userIdToUserMap = userLoaderClient.loadUsers(userIdList);
        return userExtSplitMap.values().stream().map(e -> {
            ClazzCrmMailAddressPojo pojo = new ClazzCrmMailAddressPojo();
            pojo.setId(e.getId());
            pojo.setName(getUserName(userIdToUserMap.get(e.getId())));
            pojo.setRecipientName(e.getRecipientName());
            pojo.setRecipientTel(e.getRecipientTel());
            pojo.setRecipientAddr(e.getRecipientAddr());
            pojo.setCourseLevel(e.getCourseLevel());
            pojo.setUpdateTime(e.getUpdateTime());
            return pojo;
        }).collect(Collectors.toList());
    }


    private String formatTestDay(Date date) {
        if (date == null) {
            return "无";
        }
        return DateUtils.dateToString(date, "MM月dd日");
    }

    private String formatTestRegion(Date beginTime, Date endTime) {
        if (beginTime == null || endTime == null) {
            return "无";
        }
        String begin = DateUtils.dateToString(beginTime, "HH:mm");
        String end = DateUtils.dateToString(endTime, "HH:mm");
        return begin + "-" + end;
    }



    public List<ClazzCrmQuestionnairePojo> buildQuestionnaireList(List<Long> userIdList) {
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        if (MapUtils.isEmpty(userExtSplitMap)) {
            return Collections.emptyList();
        }

        Map<Long, User> userIdToUserMap = userLoaderClient.loadUsers(userIdList);

        return userExtSplitMap.values()
                .stream()
                .filter(ChipsEnglishUserExtSplit::isFillQuestionnaire)
                .map(split -> {
                    ClazzCrmQuestionnairePojo clazzCrmQuestionnairePojo = new ClazzCrmQuestionnairePojo();
                    Long userId = split.getId();
                    clazzCrmQuestionnairePojo.setId(userId);
                    clazzCrmQuestionnairePojo.setName(getUserName(userIdToUserMap.get(userId)));
                    clazzCrmQuestionnairePojo.setGrade(split.getGrade());
                    clazzCrmQuestionnairePojo.setStudyDuration(split.getStudyDuration());
//                    clazzCrmQuestionnairePojo.setInterest(split.getInterest());
//                    clazzCrmQuestionnairePojo.setMentor(split.getMentor());
                    clazzCrmQuestionnairePojo.setWeekPoints(split.getWeekPoints());
                    clazzCrmQuestionnairePojo.setOtherExtraRegistration(split.getOtherExtraRegistration());
                    clazzCrmQuestionnairePojo.setRecentlyScore(split.getRecentlyScore());
                    clazzCrmQuestionnairePojo.setExpect(split.getExpect());
                    clazzCrmQuestionnairePojo.setUpdateTime(split.getUpdateTime());
                    return clazzCrmQuestionnairePojo;
                })
                .collect(Collectors.toList());
    }

    public List<ClazzCrmOperatingPojo> buildOperatingListNew(Long clazzId, List<Long> userIdList, String productId) {
        List<ChipsEnglishUserExt> userExtList = chipsEnglishClazzService.selectChipsEnglishUserExtByUserIds(userIdList);
        Map<Long, ChipsEnglishUserExt> userExtMap = userIdToUserExtMap(userExtList);
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazzId);
        Map<Long, ChipsEnglishClassUserRef> userRefMap = userIdToUserRefMap(userRefList);
        return buildOperatingListNew(userIdList, userExtSplitMap, userExtMap, userRefMap, productId);
    }

    private Map<Long, Long> userArticleViewCount(List<Long> userIdList) {
        List<ChipsUserPageViewLog> logList = chipsUserPageViewLogService.loadChipsUserPageViewLogByType(userIdList, PageViewType.STUDY_INFORMATION);
//        Map<Long, List<ChipsUserPageViewLog>> collect = logList.stream().collect(Collectors.groupingBy(ChipsUserPageViewLog::getUserId));
        Map<Long, Long> map = logList.stream().collect(Collectors.groupingBy(ChipsUserPageViewLog::getUserId, Collectors.counting()));
        return map;
    }

    public List<Long> buildOperatingUserIdListNew(Long clazzId, Long userId, String gradIng,
                                                  Double consumeMin, Double consumeMax, Double serviceScoreMin, Double serviceScoreMax,
                                                  int wxAdd, int epWxAdd, int wxLogin, int wxCodeShowType, int wxNickName) {
        if (clazzId == null || 0L == clazzId) {
            return null;
        }
        List<Long> userIdList = chipsEnglishClazzService.selectAllUserByClazzId(clazzId);
        Map<Long, Boolean> registeredInWeChatMap = chipsActiveService.registeredInWeChatSubscription(userIdList);
        if (userId != null && userId != 0L) {
            if (CollectionUtils.isEmpty(userIdList) || !userIdList.contains(userId)) {
                return null;
            }
            ChipsEnglishUserExt userExt = chipsEnglishClazzService.selectChipsEnglishUserExtByUserId(userId);
            ChipsEnglishUserExtSplit userExtSplit = chipsEnglishClazzService.selectChipsEnglishUserExtSplitByUserId(userId);
            boolean flag = filterGradingConsumeJZTServiceScore(userId, userExt, userExtSplit, gradIng,
                    consumeMin, consumeMax, serviceScoreMin, serviceScoreMax, wxAdd,epWxAdd, wxLogin, wxCodeShowType, wxNickName, registeredInWeChatMap);
            if (!flag) {
                return null;
            }
            List<Long> list = new ArrayList<>();
            list.add(userId);
            return list;
        }
        List<ChipsEnglishUserExt> userExtList = chipsEnglishClazzService.selectChipsEnglishUserExtByUserIds(userIdList);
        Map<Long, ChipsEnglishUserExt> userExtMap = userIdToUserExtMap(userExtList);
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        return filterGradingConsumeJZTServiceScore(userIdList, userExtMap, userExtSplitMap, gradIng,
                consumeMin, consumeMax, serviceScoreMin, serviceScoreMax, wxAdd, epWxAdd, wxLogin, wxCodeShowType, wxNickName, registeredInWeChatMap);
    }

    private boolean filterGradingConsumeJZTServiceScore(Long userId, ChipsEnglishUserExt userExt, ChipsEnglishUserExtSplit userExtSplit,
                                                        String gradIng, Double consumeMin, Double consumeMax,
                                                        double serviceScoreMin, double serviceScoreMax,
                                                        int wxAdd,int epWxAdd, int wxLogin, int wxCodeShowType, int wxNickName, Map<Long, Boolean> registeredInWeChatMap) {
        // 等级 与 家长通消费筛选
        boolean flag = filterGradingConsumeJZT(userExt, userExtSplit, gradIng, consumeMin, consumeMax);
        if (!flag) {
            return false;
        }

        // 需要筛选微信号是否填写
        if (wxCodeShowType != 2) {
            if (userExtSplit == null) {
                return false;
            }
            //已填写筛选
            if (wxCodeShowType == 1 && StringUtils.isEmpty(userExtSplit.getWxCode())) {
                return false;
            }
            // 未填写筛选
            if (wxCodeShowType == 0 && StringUtils.isNotEmpty(userExtSplit.getWxCode())) {
                return false;
            }
        }

        // 需要筛选微信号昵称填写
        if (wxNickName != 2) {
            if (userExtSplit == null) {
                return false;
            }
            //已填写筛选
            if (wxNickName == 1 && StringUtils.isEmpty(userExtSplit.getWxName())) {
                return false;
            }
            // 未填写筛选
            if (wxNickName == 0 && StringUtils.isNotEmpty(userExtSplit.getWxName())) {
                return false;
            }
        }

        // 是否登录微信筛选
        if (wxLogin != 2) {
            // 登录筛选
            if (wxLogin == 1 && (!registeredInWeChatMap.containsKey(userId) || !registeredInWeChatMap.get(userId))) {
                return false;
            }
            // 未登录筛选
            if (wxLogin == 0 && (registeredInWeChatMap.containsKey(userId) && registeredInWeChatMap.get(userId))) {
                return false;
            }
        }

        // 是否加微信筛选
        if (wxAdd != 2) {
            // 加微信筛选
            if (wxAdd == 1 && (userExtSplit == null || userExtSplit.getWeAdd() == null || !userExtSplit.getWeAdd())) {
                return false;
            }
            // 未加微信筛选
            if (wxAdd == 0 && (userExtSplit != null && userExtSplit.getWeAdd() != null && userExtSplit.getWeAdd())) {
                return false;
            }
        }

        // 是否加微信筛选
        if (epWxAdd != 2) {
            // 加微信筛选
            if (epWxAdd == 1 && (userExtSplit == null || userExtSplit.getEpWxAdd() == null || !userExtSplit.getEpWxAdd())) {
                return false;
            }
            // 未加微信筛选
            if (epWxAdd == 0 && (userExtSplit != null && userExtSplit.getEpWxAdd() != null && userExtSplit.getEpWxAdd())) {
                return false;
            }
        }
        int serviceScore = userExtSplit != null && userExtSplit.getServiceScore() != null ? userExtSplit.getServiceScore() : 0;
        if (serviceScoreMax == 0 && serviceScoreMin >= serviceScoreMax) {
            return serviceScore >= serviceScoreMin;
        }
        return serviceScore >= serviceScoreMin && serviceScore <= serviceScoreMax;
    }

    private List<Long> filterGradingConsumeJZTServiceScore(List<Long> userIdList,
                                                           Map<Long, ChipsEnglishUserExt> userExtMap,
                                                           Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap,
                                                           String gradIng,
                                                           Double consumeMin, Double consumeMax, double serviceScoreMin, double serviceScoreMax,
                                                           int wxAdd, int epWxAdd, int wxLogin, int wxCodeShowType, int wxNickName, Map<Long, Boolean> registeredInWeChatMap) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return userIdList;
        }
        List<Long> list = new ArrayList<>();
        for (Long userId : userIdList) {
            ChipsEnglishUserExt userExt = userExtMap.get(userId);
            ChipsEnglishUserExtSplit userExtSplit = userExtSplitMap.get(userId);
            boolean flag = filterGradingConsumeJZTServiceScore(userId, userExt, userExtSplit, gradIng,
                    consumeMin, consumeMax, serviceScoreMin, serviceScoreMax, wxAdd, epWxAdd, wxLogin, wxCodeShowType, wxNickName, registeredInWeChatMap);
            if (flag) {
                list.add(userId);
            }
        }
        return list;
    }


    /**
     * 过滤grading
     */
    private boolean filterGradingConsumeJZT(ChipsEnglishUserExt userExt,
                                            ChipsEnglishUserExtSplit userExtSplit,
                                            String gradIng,
                                            Double consumeMin,
                                            Double consumeMax) {
        boolean flagJZT = filterConsumeJZT(userExt, consumeMin, consumeMax);
        return flagJZT && filterGrading(userExtSplit, gradIng);
    }

    /**
     * 过滤grading
     */
    private boolean filterGrading(ChipsEnglishUserExtSplit userExt, String gradIng) {
        if (StringUtils.isBlank(gradIng)) {
            return true;
        }
        if (userExt == null || userExt.getLevel() == null) {
            return false;
        }
        return userExt.getLevel().getDescription().equals(gradIng);
    }

    private List<Long> filterGradingConsumeJZT(List<Long> userIdList,
                                               Map<Long, ChipsEnglishUserExt> userExtMap,
                                               Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap,
                                               String gradIng,
                                               Double consumeMin,
                                               Double consumeMax) {
        if (CollectionUtils.isEmpty(userIdList) || MapUtils.isEmpty(userExtMap)) {
            return userIdList;
        }
        List<Long> list = new ArrayList<>();
        for (Long userId : userIdList) {
            ChipsEnglishUserExt userExt = userExtMap.get(userId);
            ChipsEnglishUserExtSplit userExtSplit = userExtSplitMap.get(userId);
            boolean flag = filterGradingConsumeJZT(userExt, userExtSplit, gradIng, consumeMin, consumeMax);
            if (flag) {
                list.add(userId);
            }
        }
        return list;
    }

    /**
     * 电话,是否登录公众号
     */
    private List<ClazzCrmOperatingPojo> buildOperatingList(List<Long> userIdList, Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap,
                                                           Map<Long, ChipsEnglishUserExt> userExtMap, Map<Long, ChipsEnglishClassUserRef> userRefMap) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return null;
        }
        Map<Long, User> userIdToUserMap = userLoaderClient.loadUsers(userIdList);
        List<ClazzCrmOperatingPojo> list = new ArrayList<>();
        Map<Long, WonderlandPromotionChannel> channelMap = queryAllWonderlandPromotionChannel();
        Map<Long, Boolean> registeredInWeChatMap = chipsActiveService.registeredInWeChatSubscription(userIdList);
        for (Long userId : userIdList) {
            String userName = getUserName(userIdToUserMap, userId);
            ChipsEnglishUserExt userExt = userExtMap.get(userId);
            ChipsEnglishUserExtSplit userExtSplit = userExtSplitMap.get(userId);
            ChipsEnglishClassUserRef userRef = userRefMap.get(userId);
            ClazzCrmOperatingPojo pojo = getClazzCrmOperatingPojo(userExt, userExtSplit, userRef, userName, userId, channelMap, registeredInWeChatMap.get(userId));
            list.add(pojo);
        }
        return list;
    }

    private List<ClazzCrmOperatingPojo> buildOperatingListNew(List<Long> userIdList, Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap,
                                                              Map<Long, ChipsEnglishUserExt> userExtMap,
                                                              Map<Long, ChipsEnglishClassUserRef> userRefMap, String productId) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return null;
        }

        Map<Long, User> userIdToUserMap = userLoaderClient.loadUsers(userIdList);
        List<ClazzCrmOperatingPojo> list = new ArrayList<>();
        Map<Long, WonderlandPromotionChannel> channelMap = queryAllWonderlandPromotionChannel();
        Map<Long, Boolean> registeredInWeChatMap = chipsActiveService.registeredInWeChatSubscription(userIdList);

        Map<Long, List<ScoreSimpleInfo>> scoreInfoMap = chipsEnglishUserLoader.loadUserResultSimpleInfo(productId, userIdList);
        Map<Long, Long> viewCountMap = userArticleViewCount(userIdList);
        for (Long userId : userIdList) {
            String userName = getUserName(userIdToUserMap, userId);
            ChipsEnglishUserExt userExt = userExtMap.get(userId);
            ChipsEnglishUserExtSplit userExtSplit = userExtSplitMap.get(userId);
            ChipsEnglishClassUserRef userRef = userRefMap.get(userId);
            ClazzCrmOperatingPojo pojo = getClazzCrmOperatingPojo(userExt, userExtSplit, userRef, userName, userId, channelMap, registeredInWeChatMap.get(userId));
            pojo.setWxAdd(userExtSplit != null ? userExtSplit.getWeAdd() : Boolean.FALSE);
            pojo.setEpWxAdd(userExtSplit != null ? userExtSplit.getEpWxAdd() : Boolean.FALSE);
            pojo.setScoreSimpleInfos(scoreInfoMap.getOrDefault(userId, new ArrayList<>()));
            pojo.setArticleViewNum(viewCountMap.get(userId) == null ? 0L : viewCountMap.get(userId));
            list.add(pojo);
        }
        return list;
    }

    private ClazzCrmOperatingPojo getClazzCrmOperatingPojo(ChipsEnglishUserExt userExt, ChipsEnglishUserExtSplit userExtSplit, ChipsEnglishClassUserRef userRef, String userName, Long userId,
                                                           Map<Long, WonderlandPromotionChannel> channelMap, Boolean registeredInWeChat) {
        ClazzCrmOperatingPojo pojo = new ClazzCrmOperatingPojo();
        pojo.setUserId(userId);
        pojo.setUserName(userName == null ? "" : userName);
        pojo.setRegisteredInWeChatSubscription(registeredInWeChat);
        if (userRef != null) {
            pojo.setRegisterDate(DateUtils.dateToString(userRef.getCreateTime(), "yyyy/MM/dd"));
            pojo.setBuyFrom(processOrderReffer(userRef.getOrderRef(), channelMap));
            pojo.setJoinedGroup(userRef.getInGroup());
        }
        if (userExt != null) {
            pojo.setConsumption_Fries(userExt.getChipsConsume() == null ? null : userExt.getChipsConsume().doubleValue());
            pojo.setConsumption_JZT(userExt.getJztConsume() == null ? null : userExt.getJztConsume().doubleValue());
            pojo.setProvince(userExt.getProvince() == null ? "" : userExt.getProvince());
            pojo.setLatestActive(userExt.getLastActive());
        }
        if (userExtSplit != null) {
            pojo.setWechatNumber(userExtSplit.getWxCode() == null ? "" : userExtSplit.getWxCode());
            pojo.setDuration(userExtSplit.getStudyDuration());
            pojo.setServiceScore(userExtSplit.getServiceScore());
            pojo.setWxName(userExtSplit.getWxName());
        }
        pojo.setRecommandTime(getRecommmendCount(userId));
        pojo.setPurchaseTimes(getPurchaseTimes(userId));
        return pojo;
    }

    private int getRecommmendCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        List<ChipEnglishInvitation> list = aiLoaderClient.getRemoteReference().loadInvitationByInviterId(userId);
        return CollectionUtils.isEmpty(list) ? 0 : list.size();
    }

    private int getPurchaseTimes(Long userId) {
        if (userId == null) {
            return 0;
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClazzService.selectAllChipsEnglishClassUserRefByUserId(userId);
        return CollectionUtils.isEmpty(userRefList) ? 0 : userRefList.size();
    }

    private String getUserName(Map<Long, User> userIdToUserMap, Long userId) {
        if (userId == null || MapUtils.isEmpty(userIdToUserMap)) {
            return null;
        }
        User user = userIdToUserMap.get(userId);
        return getUserName(user);
    }

    private String getUserName(User user) {
        if (user == null) {
            return null;
        }
        UserProfile userProfile = user.getProfile();
        return userProfile == null ? "" : userProfile.getNickName() != null ? userProfile.getNickName() : "";
    }

    private Map<Long, ChipsEnglishUserExt> userIdToUserExtMap(List<ChipsEnglishUserExt> userExtList) {
        if (CollectionUtils.isEmpty(userExtList)) {
            return new HashMap<>();
        }
        Map<Long, ChipsEnglishUserExt> map = new HashMap<>();
        userExtList.forEach(e -> map.put(e.getId(), e));
        return map;
    }

    private Map<Long, ChipsEnglishClassUserRef> userIdToUserRefMap(List<ChipsEnglishClassUserRef> userRefList) {
        if (CollectionUtils.isEmpty(userRefList)) {
            return new HashMap<>();
        }
        Map<Long, ChipsEnglishClassUserRef> map = new HashMap<>();
        userRefList.forEach(e -> map.put(e.getUserId(), e));
        return map;
    }

    /**
     * 将1---9999之间的数转换成对应的中文
     */
    private String formatInteger(int num) {
        if (num < 0 || num > 9999) {
            return num + "";
        }
        char[] val = String.valueOf(num).toCharArray();
        int len = val.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String m = val[i] + "";
            int n = Integer.valueOf(m);
            boolean isZero = n == 0;
            String unit = units[(len - 1) - i];
            if (isZero) {
                if ('0' != val[i - 1]) {
                    //只有当当前val[i]的下一个值val[i-1]不为0才输出零
                    sb.append(numArray[n]);
                }
            } else {
                sb.append(numArray[n]);
                sb.append(unit);
            }
        }
        String str = sb.toString();
        if (str.startsWith("一十")) {
            str = str.substring(1);
        }
        if (str.endsWith("十零")) {
            str = str.substring(0, str.length() - 1);
        }
        if (str.endsWith("百零")) {
            str = str.substring(0, str.length() - 1);
        }
        if (str.endsWith("千零")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }


    public HSSFWorkbook createUserScoreExportResult(List<String> titleList, List<ClazzCrmUserScorePojo> avgList, List<ClazzCrmUserScorePojo> userList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("成绩");
        HSSFCellStyle borderStyle = getHssfCellStyle(hssfWorkbook);
        int column = 6 + (titleList == null ? 0 : titleList.size());
        for (int col = 0; col < column; col++) {
            hssfSheet.setColumnWidth(col, 256 * 14);
        }
        hssfSheet.setColumnWidth(0, 256 * 16);
        hssfSheet.setColumnWidth(1, 256 * 16);
        hssfSheet.setColumnWidth(2, 256 * 26);
        HSSFRow firstRow = createRow(hssfSheet, 0, column, borderStyle);
        int colNum = 0;
        setCellValue(firstRow, colNum++, borderStyle, "姓名(Name)");
        setCellValue(firstRow, colNum++, borderStyle, "用户ID(Use Id)");
        setCellValue(firstRow, colNum++, borderStyle, "课程(Book)");
        if (CollectionUtils.isNotEmpty(titleList)) {
            for (String title : titleList) {
                setCellValue(firstRow, colNum++, borderStyle, title);
            }
        }
        setCellValue(firstRow, colNum++, borderStyle, "完成率(Complete/Lesson)");
        setCellValue(firstRow, colNum++, borderStyle, "定级(Grading)");
        setCellValue(firstRow, colNum, borderStyle, "发放电子教材(E-Book)");
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(avgList)) {
            for (ClazzCrmUserScorePojo pojo : avgList) {
                colNum = 0;
                HSSFRow row = createRow(hssfSheet, rowNum++, column, borderStyle);
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getUserName(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getUserId(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getProductItemName(), ""));
                List<String> lessonScoreList = pojo.getLessonScoreList();
                if (CollectionUtils.isNotEmpty(lessonScoreList)) {
                    for (String score : lessonScoreList) {
                        setCellValue(row, colNum++, borderStyle, SafeConverter.toString(score, ""));
                    }
                }
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getCompleteRate(), ""));
                setCellValue(row, colNum, borderStyle, SafeConverter.toString(pojo.getGrading(), ""));
            }
        }
        if (CollectionUtils.isNotEmpty(userList)) {
            for (ClazzCrmUserScorePojo pojo : userList) {
                colNum = 0;
                HSSFRow row = createRow(hssfSheet, rowNum++, column, borderStyle);
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getUserName(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getUserId(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getProductItemName(), ""));
                List<String> lessonScoreList = pojo.getLessonScoreList();
                if (CollectionUtils.isNotEmpty(lessonScoreList)) {
                    for (String score : lessonScoreList) {
                        setCellValue(row, colNum++, borderStyle, SafeConverter.toString(score, ""));
                    }
                }
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getCompleteRate(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getGrading(), ""));
                setCellValue(row, colNum, borderStyle, SafeConverter.toString(pojo.getShowPlay() == null || !pojo.getShowPlay() ? "否" : "是", ""));
            }

        }
        return hssfWorkbook;
    }

    public HSSFWorkbook createoperationInfoExportResultNew(List<ClazzCrmOperatingPojo> pojoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("运营信息");
        int column = 12;
        for (int col = 0; col < column; col++) {
            hssfSheet.setColumnWidth(col, 256 * 16);
        }
        HSSFCellStyle borderStyle = getHssfCellStyle(hssfWorkbook);
        HSSFRow firstRow = createRow(hssfSheet, 0, column, borderStyle);
        int colNum = 0;
        setCellValue(firstRow, colNum++, borderStyle, "姓名");
        setCellValue(firstRow, colNum++, borderStyle, "用户ID");
        setCellValue(firstRow, colNum++, borderStyle, "是否加微信");
        setCellValue(firstRow, colNum++, borderStyle, "是否加企业微信");
        setCellValue(firstRow, colNum++, borderStyle, "微信号");
        setCellValue(firstRow, colNum++, borderStyle, "微信昵称");
        setCellValue(firstRow, colNum++, borderStyle, "是否登录公众号");
        setCellValue(firstRow, colNum++, borderStyle, "用户活跃");
        setCellValue(firstRow, colNum++, borderStyle, "服务价值");
        setCellValue(firstRow, colNum++, borderStyle, "家长通消费");
        setCellValue(firstRow, colNum++, borderStyle, "完成率");
        setCellValue(firstRow, colNum++, borderStyle, "最新成绩");
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(pojoList)) {
            for (ClazzCrmOperatingPojo pojo : pojoList) {
                colNum = 0;
                HSSFRow row = createRow(hssfSheet, rowNum++, column, borderStyle);
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getUserName(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getUserId(), ""));
                setCellValue(row, colNum++, borderStyle, pojo.getWxAdd() == null || !pojo.getWxAdd() ? "否" : "是");
                setCellValue(row, colNum++, borderStyle, pojo.getEpWxAdd() == null || !pojo.getEpWxAdd() ? "否" : "是");
                setCellValue(row, colNum++, borderStyle, StringUtils.isBlank(pojo.getWechatNumber()) ? "未填写" : pojo.getWechatNumber());
                setCellValue(row, colNum++, borderStyle, StringUtils.isBlank(pojo.getWxName()) ? "未填写" : pojo.getWxName());
                setCellValue(row, colNum++, borderStyle, pojo.getRegisteredInWeChatSubscription() == null || !pojo.getRegisteredInWeChatSubscription() ? "否" : "是");
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getArticleViewNum(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getServiceScore(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getConsumption_JZT(), ""));
                setCellValue(row, colNum++, borderStyle,buildCompleteRate(pojo.getScoreSimpleInfos()));
                setCellValue(row, colNum++, borderStyle, recentlyScoreToString(pojo.getScoreSimpleInfos()));
            }
        }
        return hssfWorkbook;
    }

    private String buildCompleteRate(List<ScoreSimpleInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();

        list.forEach(e -> {
            sb.append(e.getFinishedNum() + "/" + e.getTotalNum());
            sb.append("\n");
        });
        return sb.toString();
    }

    private String recentlyScoreToString(List<ScoreSimpleInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();

        list.forEach(e -> {
            String s = e.getRecentlyScore() == -1 ? "未完成" : e.getRecentlyScore() + "";
            sb.append(s);
            sb.append("\n");
        });
        return sb.toString();
    }


    public HSSFWorkbook createoperationInfoExportResult(List<ClazzCrmOperatingPojo> pojoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("运营信息");
        int column = 14;
        for (int col = 0; col < column; col++) {
            hssfSheet.setColumnWidth(col, 256 * 16);
        }
        HSSFCellStyle borderStyle = getHssfCellStyle(hssfWorkbook);
        HSSFRow firstRow = createRow(hssfSheet, 0, column, borderStyle);
        int colNum = 0;
        setCellValue(firstRow, colNum++, borderStyle, "姓名(Name)");
        setCellValue(firstRow, colNum++, borderStyle, "用户ID(User Id)");
        setCellValue(firstRow, colNum++, borderStyle, "报名日期(Register Date)");
        setCellValue(firstRow, colNum++, borderStyle, "购课次数(Purchase Times)");
        setCellValue(firstRow, colNum++, borderStyle, "薯条总消费(Consumption/Fries)");
        setCellValue(firstRow, colNum++, borderStyle, "家长通过消费(Consumption/JZT)");
        setCellValue(firstRow, colNum++, borderStyle, "订单来源(Buy From)");
        setCellValue(firstRow, colNum++, borderStyle, "省份(Province)");
        setCellValue(firstRow, colNum++, borderStyle, "是否登录公众号(Registered in Wechat Subscription)");
        setCellValue(firstRow, colNum++, borderStyle, "微信号(Wechat Account)");
        setCellValue(firstRow, colNum++, borderStyle, "是否进群(Joined Group)");
        setCellValue(firstRow, colNum++, borderStyle, "学习年限(Duration)");
        setCellValue(firstRow, colNum++, borderStyle, "最后活跃(Latest Active)");
        setCellValue(firstRow, colNum, borderStyle, "服务价值(Service Value)");
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(pojoList)) {
            for (ClazzCrmOperatingPojo pojo : pojoList) {
                colNum = 0;
                HSSFRow row = createRow(hssfSheet, rowNum++, column, borderStyle);
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getUserName(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getUserId(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getRegisterDate(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getPurchaseTimes(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getConsumption_Fries(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getConsumption_JZT(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getBuyFrom(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getProvince(), ""));
                setCellValue(row, colNum++, borderStyle, pojo.getRegisteredInWeChatSubscription() == null || !pojo.getRegisteredInWeChatSubscription() ? "否" : "是");
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getWechatNumber(), ""));
                setCellValue(row, colNum++, borderStyle, pojo.getJoinedGroup() == null || !pojo.getJoinedGroup() ? "否" : "是");
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getDuration(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getLatestActive(), ""));
                setCellValue(row, colNum, borderStyle, SafeConverter.toString(pojo.getServiceScore(), ""));
            }
        }
        return hssfWorkbook;
    }

    public HSSFWorkbook createQuestionnaireInfoExportResult(List<ClazzCrmQuestionnairePojo> pojoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("用户问卷报告");
        int column = 10;
        for (int col = 0; col < 10; col++) {
            hssfSheet.setColumnWidth(col, 256 * 16);
        }
        HSSFCellStyle borderStyle = getHssfCellStyle(hssfWorkbook);
        HSSFRow firstRow = createRow(hssfSheet, 0, column, borderStyle);
        int colNum = 0;
        setCellValue(firstRow, colNum++, borderStyle, "用户ID");
        setCellValue(firstRow, colNum++, borderStyle, "姓名");
        setCellValue(firstRow, colNum++, borderStyle, "年级");
        setCellValue(firstRow, colNum++, borderStyle, "学习年限");
//        setCellValue(firstRow, colNum++, borderStyle, "兴趣");
//        setCellValue(firstRow, colNum++, borderStyle, "谁辅导孩子");
        setCellValue(firstRow, colNum++, borderStyle, "英语薄弱点");
        setCellValue(firstRow, colNum++, borderStyle, "其他课外报名");
        setCellValue(firstRow, colNum++, borderStyle, "最近成绩");
        setCellValue(firstRow, colNum++, borderStyle, "对薯条的期待");
        setCellValue(firstRow, colNum, borderStyle, "填写时间");

        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(pojoList)) {
            for (ClazzCrmQuestionnairePojo pojo : pojoList) {
                colNum = 0;
                HSSFRow row = createRow(hssfSheet, rowNum++, column, borderStyle);
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getId(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getName(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getGrade(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getStudyDuration(), ""));
//                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getInterest(), ""));
//                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getMentor(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getWeekPoints(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getOtherExtraRegistration(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getRecentlyScore(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getExpect(), ""));
                Date date = pojo.getUpdateTime();
                setCellValue(row, colNum, borderStyle, date == null ? "无" : DateUtils.dateToString(date));
            }
        }
        return hssfWorkbook;
    }

    public HSSFWorkbook createOralTestExportResult(List<ClazzCrmOralTestPojo> pojoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("口语测试");
        int column = 5;
        for (int col = 0; col < 5; col++) {
            hssfSheet.setColumnWidth(col, 256 * 16);
        }
        HSSFCellStyle borderStyle = getHssfCellStyle(hssfWorkbook);
        HSSFRow firstRow = createRow(hssfSheet, 0, column, borderStyle);
        int colNum = 0;
        setCellValue(firstRow, colNum++, borderStyle, "用户ID");
        setCellValue(firstRow, colNum++, borderStyle, "姓名");
        setCellValue(firstRow, colNum++, borderStyle, "口测日期");
        setCellValue(firstRow, colNum++, borderStyle, "口测时间段");
        setCellValue(firstRow, colNum, borderStyle, "填写时间");

        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(pojoList)) {
            for (ClazzCrmOralTestPojo pojo : pojoList) {
                colNum = 0;
                HSSFRow row = createRow(hssfSheet, rowNum++, column, borderStyle);
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getId(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getName(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getTestDay(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getTestRegion(), ""));
                Date date = pojo.getUpdateTime();
                setCellValue(row, colNum, borderStyle, date == null ? "无" : DateUtils.dateToString(date));
            }
        }
        return hssfWorkbook;
    }
    public HSSFWorkbook createMailAddressExportResult(List<ClazzCrmMailAddressPojo> pojoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("口语测试");
        int column = 7;
        for (int col = 0; col < 7; col++) {
            hssfSheet.setColumnWidth(col, 256 * 16);
        }
        HSSFCellStyle borderStyle = getHssfCellStyle(hssfWorkbook);
        HSSFRow firstRow = createRow(hssfSheet, 0, column, borderStyle);
        int colNum = 0;
        setCellValue(firstRow, colNum++, borderStyle, "用户ID");
        setCellValue(firstRow, colNum++, borderStyle, "姓名");
        setCellValue(firstRow, colNum++, borderStyle, "收货人姓名");
        setCellValue(firstRow, colNum++, borderStyle, "收货人地址");
        setCellValue(firstRow, colNum++, borderStyle, "收货人电话");
        setCellValue(firstRow, colNum++, borderStyle, "后续课程级别");
        setCellValue(firstRow, colNum, borderStyle, "填写时间");

        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(pojoList)) {
            for (ClazzCrmMailAddressPojo pojo : pojoList) {
                colNum = 0;
                HSSFRow row = createRow(hssfSheet, rowNum++, column, borderStyle);
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getId(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getName(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getRecipientName(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getRecipientTel(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getRecipientAddr(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getCourseLevel(), ""));
                Date date = pojo.getUpdateTime();
                setCellValue(row, colNum, borderStyle, date == null ? "无" : DateUtils.dateToString(date));
            }
        }
        return hssfWorkbook;
    }

    @NotNull
    private HSSFCellStyle getHssfCellStyle(HSSFWorkbook hssfWorkbook) {
        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);
        borderStyle.setAlignment(HorizontalAlignment.LEFT);
        HSSFFont titleFont = hssfWorkbook.createFont();
        titleFont.setFontHeightInPoints((short) 12);
        borderStyle.setFont(titleFont);
        borderStyle.setWrapText(false);
        return borderStyle;
    }


    private void setCellValue(HSSFRow row, int column, CellStyle style, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    private HSSFRow createRow(HSSFSheet sheet, int rowNum, int column, CellStyle style) {
        HSSFRow row = sheet.createRow(rowNum);
        for (int i = 0; i <= column; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
        }
        return row;
    }

    private Map<Long, WonderlandPromotionChannel> queryAllWonderlandPromotionChannel() {
        Map<Long, WonderlandPromotionChannel> map = wonderlandPromotionChannelServiceClient.getWonderlandPromotionChannelService().queryAll()
                .stream().collect(Collectors.toMap(WonderlandPromotionChannel::getId, Function.identity()));
        if (map == null) {
            return new HashMap<>();
        }
        return map;
    }

    private String processOrderReffer(String orderRefferer, Map<Long, WonderlandPromotionChannel> channelMap) {
        String[] channelIds = StringUtils.split(orderRefferer, ",");
        StringBuilder refer = new StringBuilder();
        if (channelIds == null) {
            return refer.toString();
        }
        for (String channel : channelIds) {
            WonderlandPromotionChannel realChannel = channelMap.get(SafeConverter.toLong(channel));
            refer.append(realChannel == null ? channel : realChannel.getDescription()).append("|");
        }
        return StringUtils.substring(refer.toString(), 0, refer.length() - 1);
    }

    private List<Long> selectUserIdFromUserRef(Long clazzId) {
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazzId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return null;
        }
        List<Long> list = new ArrayList<>();
        for (ChipsEnglishClassUserRef userRef : userRefList) {
            list.add(userRef.getUserId());
        }
        return list;
    }

    public StoneUnitData loadTodayStudyUnit(Long clazzId) {
        String unitId = chipsEnglishContentLoader.loadCurrentUnitId(clazzId);
        if (StringUtils.isBlank(unitId)) {
            return null;
        }
        Map<String, StoneData> unitDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(unitId));
        return Optional.ofNullable(unitDataMap).filter(MapUtils::isNotEmpty).map(m -> m.get(unitId)).map(StoneUnitData::newInstance).orElse(null);
    }

    public MapMessage rankListData(Long clazzId, StoneUnitData unit) {
        List<AIUserUnitResultPlan> aiUserUnitResultPlans = aiLoaderClient.getRemoteReference().loadUnitStudyPlan(unit.getId());
        List<Long> userIdList = selectUserIdFromUserRef(clazzId);
        aiUserUnitResultPlans = filterByUsers(aiUserUnitResultPlans, userIdList);
        Map<Long, UserBean> userInfoMap = buildUserDisplayInfo(aiUserUnitResultPlans);
        int gradeA = aiUserUnitResultPlans == null ? 0 : aiUserUnitResultPlans.stream().filter(e -> e.getGrade() == AIUserUnitResultPlan.Grade.A).collect(Collectors.toList()).size();
        int gradeB = aiUserUnitResultPlans == null ? 0 : aiUserUnitResultPlans.stream().filter(e -> e.getGrade() == AIUserUnitResultPlan.Grade.B).collect(Collectors.toList()).size();
        int gradeC = aiUserUnitResultPlans == null ? 0 : aiUserUnitResultPlans.stream().filter(e -> e.getGrade() == AIUserUnitResultPlan.Grade.C).collect(Collectors.toList()).size();
        //生成成绩排行榜，并存储
        List<ChipsRank> scoreRankList = genarateScoreRanking(aiUserUnitResultPlans, userInfoMap);
        UserScoreRankCache.save(scoreRankList, clazzId + "", unit.getId());
        List<ChipsRank> useVideoRankList = genarateShareVideoRanking(unit.getId(), clazzId, userInfoMap);
        UserShareVideoRankCache.save(useVideoRankList, clazzId + "", unit.getId());
        List<String> userVideoRankStr = new ArrayList<>();
        useVideoRankList.forEach(e -> userVideoRankStr.add(e.getUserName() + "(" + e.getUserId() + ")"));
        ChipsEnglishClass clazz = chipsEnglishClazzService.selectChipsEnglishClassById(clazzId);
        aiLoaderClient.getRemoteReference().sendChipsCourseDailyRankTemplateMessage(unit.getId(), clazz, Arrays.asList(RuntimeMode.lt(Mode.STAGING) ? TEST_USER : ONLINE_USER), aiUserUnitResultPlans == null ? 0 : aiUserUnitResultPlans.size());
        return MapMessage.successMessage("排行榜已经生成，请看微信模板消息，课程：" + (unit.getJsonData() == null ? "" : unit.getJsonData().getName()) +
                "<br/>成绩汇总 成绩A:" + gradeA + "  成绩B:" + gradeB + "  成绩C:" + gradeC +
                "<br/>视频榜单：" + JsonUtils.toJson(userVideoRankStr));
    }

    /**
     * 分析次数在10次以上的
     */
    private List<ChipsRank> genarateShareVideoRanking(String unitId, Long clazzId, Map<Long, UserBean> userInfoMap) {
        List<ChipsRank> chipsRankList = filterChipsRank(clazzId, unitId);
        if (CollectionUtils.isEmpty(chipsRankList)) {
            return Collections.emptyList();
        }
        Map<Integer, List<ChipsRank>> numberChipsRankMap = new HashMap<>();
        List<Integer> numberList = new ArrayList<>();
        for (ChipsRank rank : chipsRankList) {
            if (rank == null || rank.getNumber() == null) {
                continue;
            }
            List<ChipsRank> rankList = numberChipsRankMap.get(rank.getNumber());
            if (rankList == null) {
                numberList.add(rank.getNumber());
                rankList = new ArrayList<>();
                numberChipsRankMap.put(rank.getNumber(), rankList);
            }
            rankList.add(rank);
        }
        numberList.sort(Comparator.comparing(Integer::intValue).reversed());//排序之前应该就是有序的了
        int rank = 0;
        List<ChipsRank> resultList = new ArrayList<>();
        for (Integer num : numberList) {
            if (++rank > 10) {
                break;
            }
            List<ChipsRank> rankList = numberChipsRankMap.get(num);
            for (ChipsRank chipsRank : rankList) {
                resultList.add(getChipsRank(userInfoMap, rank, chipsRank));
            }
        }
        return resultList;
    }

    @NotNull
    private ChipsRank getChipsRank(Map<Long, UserBean> userInfoMap, int rank, ChipsRank rankOld) {
        ChipsRank chipsRank = new ChipsRank();
        chipsRank.setRank(rank);
        chipsRank.setUserId(rankOld.getUserId());
        chipsRank.setNumber(rankOld.getNumber());
        chipsRank.setUserName(Optional.ofNullable(userInfoMap.get(rankOld.getUserId())).map(UserBean::getName).orElse("**"));
        chipsRank.setImage(Optional.ofNullable(userInfoMap.get(rankOld.getUserId())).map(UserBean::getImage).orElse(""));
        return chipsRank;
    }

    /**
     * @return 一个userId只会有一条记录
     */
    private List<ChipsRank> filterChipsRank(Long clazzId, String unitId) {
        ChipsEnglishPageContentConfig userVideoBlackList = chipsEnglishConfigServiceClient.loadChipsEnglishConfigByName("userVideoBlackList");
        List<Long> userBlackList = userVideoBlackList == null ? null : JsonUtils.fromJsonToList(userVideoBlackList.getValue(), Long.class);
        return aiLoaderClient.getRemoteReference().loadShareVideoRanking(clazzId + "", unitId).stream()
                .filter(e -> CollectionUtils.isEmpty(userBlackList) || !userBlackList.contains(e.getUserId()))
                .filter(e -> e.getNumber() != null && e.getNumber().compareTo(10) > 0)
                .sorted((e1, e2) -> e2.getNumber().compareTo(e1.getNumber()))
                .collect(Collectors.toList());
    }

    private List<AIUserUnitResultPlan> filterByUsers(List<AIUserUnitResultPlan> aiUserUnitResultPlans, List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return null;
        }
        if (CollectionUtils.isEmpty(aiUserUnitResultPlans)) {
            return null;
        }
        List<Long> userBlackList = Optional.ofNullable(chipsEnglishConfigServiceClient.loadChipsEnglishConfigByName("chipsScoreBlackList"))
                .filter(e -> e.getDisabled() == null || !e.getDisabled())
                .map(e -> JsonUtils.fromJsonToList(e.getValue(), Long.class))
                .orElse(null);
        return aiUserUnitResultPlans.stream()
                .filter(e -> CollectionUtils.isNotEmpty(userIdList) && userIdList.contains(e.getUserId()))
                .filter(e -> CollectionUtils.isEmpty(userBlackList) || !userBlackList.contains(e.getUserId()))
                .collect(Collectors.toList());

    }

    private Map<Long, UserBean> buildUserDisplayInfo(List<AIUserUnitResultPlan> aiUserUnitResultPlans) {
        Map<Long, UserBean> userInfoMap = new HashMap<>();
        if (CollectionUtils.isEmpty(aiUserUnitResultPlans)) {
            return userInfoMap;
        }
        List<Long> userIds = aiUserUnitResultPlans.stream().map(AIUserUnitResultPlan::getUserId).collect(Collectors.toList());
        Map<Long, User> extMap = getUserExtInfo(userIds);
        if (MapUtils.isEmpty(extMap)) {
            return userInfoMap;
        }
        userIds.forEach(e -> {
            UserBean userBean = new UserBean();
            userBean.setImage(Optional.ofNullable(extMap.get(e)).map(UserInfoSupport::getUserRoleImage).orElse(""));
            userBean.setName(Optional.ofNullable(extMap.get(e)).map(User::getProfile).filter(e1 -> StringUtils.isNotBlank(e1.getNickName())).map(UserProfile::getNickName).orElse("**"));
            userInfoMap.put(e, userBean);
        });
        return userInfoMap;
    }

    private Map<Long, User> getUserExtInfo(List<Long> userIds) {
        Map<Long, User> map = new HashMap<>();
        if (CollectionUtils.isEmpty(userIds)) {
            return map;
        }
        for (int i = 0; i < userIds.size(); i += 200) {
            Map<Long, User> extMap = userLoaderClient.loadUsers(userIds.subList(i, Math.min(userIds.size(), i + 200)));
            if (MapUtils.isEmpty(extMap)) {
                continue;
            }
            map.putAll(extMap);
        }
        return map;
    }

    /**
     * 取最大的10个分数对应的用户，且总数不超过400个
     */
    private List<ChipsRank> genarateScoreRanking(List<AIUserUnitResultPlan> aiUserUnitResultPlans, Map<Long, UserBean> userInfoMap) {
        Map<Integer, List<AIUserUnitResultPlan>> scoreMap = new HashMap<>();//每个分数对应的ResultPlan
        List<Integer> scoreList = new ArrayList<>();//分数
        if (aiUserUnitResultPlans != null) {
            aiUserUnitResultPlans.forEach(e -> {
                if (e.getScore() == null || e.getScore().compareTo(75) <= 0) {
                    return;
                }
                List<AIUserUnitResultPlan> userUnitResultPlanList = scoreMap.get(e.getScore());
                if (userUnitResultPlanList == null) {
                    scoreList.add(e.getScore());
                    userUnitResultPlanList = new ArrayList<>();
                    scoreMap.put(e.getScore(), userUnitResultPlanList);
                }
                userUnitResultPlanList.add(e);
            });
        }
        scoreList.sort(Comparator.comparing(Integer::intValue).reversed());
        List<ChipsRank> rankList = new ArrayList<>();
        int rank = 0;
        Set<Long> userSet = new HashSet<>();
        for (Integer score : scoreList) {
            if (++rank > 10) {
                break;
            }
            for (AIUserUnitResultPlan plan : scoreMap.get(score)) {
                if (userSet.contains(plan.getUserId()) || userSet.size() > 400) {
                    continue;
                }
                userSet.add(plan.getUserId());
                rankList.add(getChipsRank(userInfoMap, rank, plan));
            }
        }
        return rankList;
    }

    @NotNull
    private ChipsRank getChipsRank(Map<Long, UserBean> userInfoMap, int rank, AIUserUnitResultPlan unitResultPlan) {
        ChipsRank chipsRank = new ChipsRank();
        chipsRank.setNumber(unitResultPlan.getScore());
        chipsRank.setUserId(unitResultPlan.getUserId());
        chipsRank.setUserName(Optional.ofNullable(userInfoMap.get(unitResultPlan.getUserId())).map(UserBean::getName).orElse("**"));
        chipsRank.setImage(Optional.ofNullable(userInfoMap.get(unitResultPlan.getUserId())).map(UserBean::getImage).orElse(""));
        chipsRank.setRank(rank);
        return chipsRank;
    }

    public List<ClazzCrmMailExportPojo> buildMailPojoList(List<Long> userIdList) {
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        if (CollectionUtils.isEmpty(userIdList)) {
            return null;
        }
        List<ClazzCrmMailExportPojo> list = new ArrayList<>();
        for (Long userId : userIdList) {
            ChipsEnglishUserExtSplit userExtSplit = userExtSplitMap.get(userId);
            ClazzCrmMailExportPojo pojo = buildMailPojoList(userExtSplit, userId);
            list.add(pojo);
        }
        return list;
    }

    private ClazzCrmMailExportPojo buildMailPojoList(ChipsEnglishUserExtSplit userExtSplit, Long userId) {
        ClazzCrmMailExportPojo pojo = new ClazzCrmMailExportPojo();
        pojo.setUserId(userId);
        if (userExtSplit != null) {
            pojo.setRecipientAddr(userExtSplit.getRecipientAddr() == null ? "" : userExtSplit.getRecipientAddr());
            pojo.setRecipientTel(userExtSplit.getRecipientTel() == null ? "" : userExtSplit.getRecipientTel());
            pojo.setRecipientName(userExtSplit.getRecipientName() == null ? "" : userExtSplit.getRecipientName());
        }
        return pojo;
    }

    public HSSFWorkbook createMailExportResult(List<ClazzCrmMailExportPojo> pojoList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("邮寄地址");
        int column = 4;
        for (int col = 0; col < column; col++) {
            hssfSheet.setColumnWidth(col, 256 * 16);
        }
        HSSFCellStyle borderStyle = getHssfCellStyle(hssfWorkbook);
        HSSFRow firstRow = createRow(hssfSheet, 0, column, borderStyle);
        int colNum = 0;
        setCellValue(firstRow, colNum++, borderStyle, "用户ID");
        setCellValue(firstRow, colNum++, borderStyle, "收货人姓名");
        setCellValue(firstRow, colNum++, borderStyle, "收货人地址");
        setCellValue(firstRow, colNum, borderStyle, "收货人电话");
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(pojoList)) {
            for (ClazzCrmMailExportPojo pojo : pojoList) {
                colNum = 0;
                HSSFRow row = createRow(hssfSheet, rowNum++, column, borderStyle);
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getUserId(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getRecipientName(), ""));
                setCellValue(row, colNum++, borderStyle, SafeConverter.toString(pojo.getRecipientAddr(), ""));
                setCellValue(row, colNum, borderStyle, SafeConverter.toString(pojo.getRecipientTel(), ""));
            }
        }
        return hssfWorkbook;
    }

    private List<ChipsEnglishClass> loadClazzByProduct(String productId) {
        List<ChipsEnglishClass> clazzList = chipsEnglishClazzService.selectChipsEnglishClassByProductId(productId);
        if (CollectionUtils.isEmpty(clazzList)) {
            return Collections.emptyList();
        }
        return clazzList;
    }

    public List<Long> loadUserIdByProduct(String productId) {
        List<ChipsEnglishClass> clazzList = loadClazzByProduct(productId);
        List<Long> list = new ArrayList<>();
        clazzList.forEach(c -> {
            List<Long> userList = chipsEnglishClazzService.selectAllUserByClazzId(c.getId());
            if (CollectionUtils.isEmpty(userList)) {
                return;
            }
            list.addAll(userList);
        });
        return list;
    }

    public MapMessage updateChipsEnglishClassProduct(Long clazzId, String productId) {
        return chipsEnglishClazzService.updateChipsEnglishClassProduct(clazzId, productId);
    }
    public MapMessage updateChipsEnglishClassProduct(Long userId,Long originClazzId, Long clazzId,String originProductId, String productId) {
        return chipsEnglishClazzService.updateUserClazzAndUserCourse(userId, originClazzId, clazzId, originProductId, productId);
    }

    public MapMessage insertChipsEnglishClassUpdateLog(String userName, Long clazzId, String originProductId, String productId) {
        return chipsEnglishClazzService.insertChipsEnglishClassUpdateLog(buildChipsEnglishClassUpdateLog(userName, clazzId, originProductId, productId));
    }

    /**
     *
     * @param userName 操作用户
     * @param userId 更换班级用户id
     * @param clazzId
     * @param originProductId
     * @param productId
     * @return
     */
    public MapMessage insertUserChipsEnglishClassUpdateLog(String userName,Long userId,Long originClazzId, Long clazzId, String originProductId, String productId) {
        ChipsEnglishClassUpdateLog log = new ChipsEnglishClassUpdateLog();
        log.setUser(userName);
        log.setOperation("用户换班");
        log.setContent(buildUserClazzProductChangeLogContent(userId, originClazzId, clazzId, originProductId, productId));
        return chipsEnglishClazzService.insertChipsEnglishClassUpdateLog(log);
    }
    public ChipsEnglishClassUpdateLog buildChipsEnglishClassUpdateLog(String userName, Long clazzId, String originProductId, String productId) {
        ChipsEnglishClassUpdateLog log = new ChipsEnglishClassUpdateLog();
        log.setUser(userName);
        log.setOperation("班级更换产品");
        log.setContent(buildClazzProductChangeLogContent(clazzId, originProductId, productId));
        return log;
    }

    private String buildClazzProductChangeLogContent(Long clazzId, String originProductId, String productId) {
        OrderProduct originProduct = userOrderLoaderClient.loadOrderProductById(originProductId);
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        ChipsEnglishClass clazz = chipsEnglishClazzService.selectChipsEnglishClassById(clazzId);
        StringBuffer sb = new StringBuffer();
        sb.append("班级:").append(clazz == null ? "" : clazz.getName()).append("(").append(clazzId).append(")从产品 ").append(originProduct == null ? "" : originProduct.getName()).append("(").append(originProductId)
                .append(")更换为产品 ").append(product == null ? "" : product.getName()).append("(").append(productId).append(")");
        return sb.toString();
    }

    private String buildUserClazzProductChangeLogContent(Long userId, Long originClazzId, Long clazzId, String originProductId, String productId) {
        OrderProduct originProduct = userOrderLoaderClient.loadOrderProductById(originProductId);
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        ChipsEnglishClass originClazz = chipsEnglishClazzService.selectChipsEnglishClassById(originClazzId);
        ChipsEnglishClass clazz = chipsEnglishClazzService.selectChipsEnglishClassById(clazzId);
        StringBuffer sb = new StringBuffer();
        sb.append("用户:").append(userId).append(";从产品").append(originProduct == null ? "" : originProduct.getName()).append("(").append(originProductId)
                .append(")的班级:").append(originClazz == null ? "" : originClazz.getName()).append("(").append(originClazzId)
                .append(")更换为产品 ").append(product == null ? "" : product.getName()).append("(").append(productId).append(")")
                .append(")的班级:").append(clazz == null ? "" : clazz.getName()).append("(").append(clazzId).append(")");
        return sb.toString();
    }

    public   List<List<String>> exportEngery(List<Long> userIdList,String productId) {
        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        if (CollectionUtils.isEmpty(itemList)) {
            return Collections.emptyList();
        }
        String bookId= Optional.ofNullable(itemList.get(0)).map(e -> e.getAppItemId()).orElse(null);
        return chipsTaskLoader.calUserUnitEngeryByBook(userIdList, bookId);
    }


    public HSSFWorkbook createEngeryExportResult(List<List<String>> engeryList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("能量榜");
        HSSFCellStyle borderStyle = getHssfCellStyle(hssfWorkbook);
        if (CollectionUtils.isNotEmpty(engeryList)) {
            List<String> titleList = engeryList.get(0);
            for (int col = 0; col < titleList.size(); col++) {
                hssfSheet.setColumnWidth(col, 256 * 14);
            }
            int rowNum = 0;
            for(int i = 0 ; i < engeryList.size() ; i++) {
                List<String> list = engeryList.get(i);
                HSSFRow row = createRow(hssfSheet, rowNum++, 0, borderStyle);
                int colNum = 0;
                for (String data : list) {
                    setCellValue(row, colNum++, borderStyle, data);
                }
            }
        }
        return hssfWorkbook;
    }


    @Getter
    @Setter
    private class UserBean {
        private String name;
        private String image;
    }

}
