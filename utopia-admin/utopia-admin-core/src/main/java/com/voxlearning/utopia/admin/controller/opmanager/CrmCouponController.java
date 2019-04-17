package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.coupon.api.constants.CouponStatus;
import com.voxlearning.utopia.service.coupon.api.constants.CouponTagType;
import com.voxlearning.utopia.service.coupon.api.constants.CouponTargetType;
import com.voxlearning.utopia.service.coupon.api.constants.CouponType;
import com.voxlearning.utopia.service.coupon.api.entities.Coupon;
import com.voxlearning.utopia.service.coupon.api.entities.CouponProductRef;
import com.voxlearning.utopia.service.coupon.api.entities.CouponTag;
import com.voxlearning.utopia.service.coupon.api.entities.CouponTarget;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2017/3/14.
 */
@Controller("opmanager.CrmCouponController")
@RequestMapping("/opmanager/coupon")
public class CrmCouponController extends OpManagerAbstractController {
    @Inject private CouponLoaderClient couponLoaderClient;
    @Inject private CouponServiceClient couponServiceClient;

    // 列表页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        // 获取全部的礼券
        int page = getRequestInt("page", 1);
        String couponName = getRequestString("couponName");
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 10);
        List<Coupon> coupons = couponLoaderClient.loadAllCoupons();
        if (StringUtils.isNotBlank(couponName)) {
            coupons = coupons.stream().filter(c -> c.getName().contains(couponName)).collect(Collectors.toList());
        }
        // 按照创建时间倒序
//        coupons.sort((a, b) -> a.getCreateDatetime().before(b.getCreateDatetime()) ? 1 : -1);
        Collections.sort(coupons, new Comparator<Coupon>() {
            @Override
            public int compare(Coupon o1, Coupon o2) {
                return o2.getCreateDatetime().compareTo(o1.getCreateDatetime());
            }
        });

        Page<Coupon> couponPage = PageableUtils.listToPage(coupons, pageable);
        model.addAttribute("couponPage", couponPage.getContent());
        model.addAttribute("currentPage", couponPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", couponPage.getTotalPages());
        model.addAttribute("hasPrev", couponPage.hasPrevious());
        model.addAttribute("hasNext", couponPage.hasNext());
        model.addAttribute("couponName", couponName);
        return "opmanager/coupon/index";
    }

    // 详细页面
    @RequestMapping(value = "coupondetail.vpage", method = RequestMethod.GET)
    public String couponDetail(Model model) {
        String couponId = getRequestString("couponId");
        if (StringUtils.isNotBlank(couponId)) {
            Coupon coupon = couponLoaderClient.loadCouponById(couponId);
            if (coupon != null) {
                model.addAttribute("coupon", coupon);
                model.addAttribute("useStartTime",coupon.getUseStartTime());
                model.addAttribute("couponType",coupon.getCouponType());
            }
        }

        model.addAttribute("accountId", couponId);
        model.addAttribute("status", CouponStatus.values());
        model.addAttribute("couponTypes", CouponType.values());
        model.addAttribute("type", getRequestString("type"));
        return "opmanager/coupon/coupondetail";
    }

    // 添加编辑 post
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage save() {
        // 获取参数
        String couponId = getRequestString("couponId");
        String name = getRequestString("name");
        String desc = getRequestString("desc");
        String couponType = getRequestString("couponType");
        int usableCount = getRequestInt("usableCount");
        int totalCount = getRequestInt("totalCount", 0);
        String effectiveDay = getRequestString("effectiveDay");
        String limitDate = getRequestString("limitDate");
        int leftCount = getRequestInt("leftCount");
        double typeValue = getRequestDouble("typeValue", 0);
        String status = getRequestString("status");
        String linkUrl = getRequestString("linkUrl");
        double voucherAmount = getRequestDouble("voucherAmount",0);
        String useStartTime = getRequestString("useStartTime");
        String useEndTime = getRequestString("useEndTime");
        try {
            Coupon coupon;
            if (StringUtils.isBlank(couponId)) {
                coupon = new Coupon();
            } else {
                coupon = couponLoaderClient.loadCouponById(couponId);
                if (coupon == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
            }
            coupon.setName(name);
            coupon.setDesc(desc);
            coupon.setCouponType(CouponType.valueOf(couponType));
            coupon.setUsableCount(usableCount);
            coupon.setTotalCount(totalCount);
            coupon.setLimitDate(DateUtils.stringToDate(limitDate));
            coupon.setLeftCount(leftCount);
            coupon.setTypeValue(new BigDecimal(typeValue));
            coupon.setStatus(CouponStatus.valueOf(status));
            coupon.setLinkUrl(linkUrl);
            coupon.setVoucherAmount(new BigDecimal(voucherAmount));
            if(StringUtils.isNotBlank(effectiveDay)){
                coupon.setEffectiveDay(SafeConverter.toInt(effectiveDay));
            }else{
                coupon.setUseStartTime(DateUtils.stringToDate(useStartTime));
                coupon.setUseEndTime(DateUtils.stringToDate(useEndTime));
            }

            // 保存实体
            MapMessage returnMsg;
            String op = "新建礼券";
            if (StringUtils.isBlank(couponId)) {
                returnMsg = couponServiceClient.$saveCoupon(coupon);
                couponId = SafeConverter.toString(returnMsg.get("id"));
            } else {
                coupon.setUpdateDatetime(new Date());
                returnMsg = couponServiceClient.$updateCoupon(couponId, coupon);
                op = "编辑礼券";
            }
            returnMsg.setInfo(returnMsg.isSuccess() ? "保存成功！" : "保存失败!");
            saveOperationLog(op, null, couponId, returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Save coupon error! id={}, ex={}", couponId, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    // 投放策略
    @RequestMapping(value = "couponconfig.vpage", method = RequestMethod.GET)
    public String couponConfig(Model model) {
        String couponId = getRequestString("couponId");
        model.addAttribute("couponId", couponId);
        Coupon coupon = couponLoaderClient.loadCouponById(couponId);
        if (coupon == null || coupon.isDisabledTrue()) {
            model.addAttribute("error", "无效的信息");
            return "opmanager/coupon/couponconfig";
        }
        model.addAttribute("coupon", coupon);
        generateDetailTargets(couponId, model);
        generateDetailTags(couponId, model);
        generateProducts(couponId, model);
        return "opmanager/coupon/couponconfig";
    }

    @RequestMapping(value = "saveregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetRegion() {
        String couponId = getRequestString("couponId");
        Integer type = getRequestInt("type");
        String regions = getRequestString("regionList");
        if (CouponTargetType.of(type) != CouponTargetType.TARGET_TYPE_REGION) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(regions)) {
            return MapMessage.errorMessage("选择地区不能为空！");
        }
        try {
            List<String> regionList = Arrays.asList(regions.split(","));
            MapMessage returnMsg = couponServiceClient.$saveCouponTargets(couponId, type, regionList);
            addAdminLog("修改礼券投放区域", couponId, returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存投放地区失败! id={},type={}, ex={}", couponId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放地区失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "saveproducts.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveProducts() {
        String couponId = getRequestString("couponId");
        String products = getRequestString("productList");
        if (StringUtils.isBlank(products)) {
            return MapMessage.errorMessage("关联产品不能为空！");
        }
        try {
            List<String> productList = Arrays.asList(products.split(","));
            MapMessage returnMsg = couponServiceClient.$saveCouponProductRefs(couponId, productList);
            addAdminLog("修改礼券关联产品", couponId, returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存礼券关联产品失败! id={}, ex={}", couponId, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存礼券关联产品失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "saveids.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetIds() {
        String couponId = getRequestString("couponId");
        Integer type = getRequestInt("type");
        String targetIds = getRequestString("targetIds");
        CouponTargetType targetType = CouponTargetType.of(type);
        if (targetType != CouponTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(targetIds)) {
            return MapMessage.errorMessage("请输入有效的内容！");
        }
        try {
            // 没有校验用户输入是否符合规范
            List<String> targetList = Arrays.stream(targetIds.split("\n")).map(t -> t.replaceAll("\\s", ""))
                    .filter(StringUtils::isNotBlank).collect(Collectors.toList());
            MapMessage returnMsg = couponServiceClient.$saveCouponTargets(couponId, type, targetList);
            addAdminLog("修改礼券投放对象(" + targetType.getDesc() + ")", couponId, returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存投放用户失败:id={},type={},ex={}", couponId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放用户失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "cleartargets.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearTargets() {
        String couponId = getRequestString("couponId");
        Integer type = getRequestInt("type");
        CouponTargetType targetType = CouponTargetType.of(type);
        if (targetType != CouponTargetType.TARGET_TYPE_REGION
                && targetType != CouponTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        try {
            return couponServiceClient.$clearCouponTargets(couponId, type);
        } catch (Exception ex) {
            logger.error("清空投放对象失败:id={},type={},ex={}", couponId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("清空投放对象失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "savetag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTag() {
        String couponId = getRequestString("couponId");
        String tagType = getRequestString("tagType");
        String tagVal = getRequestString("tagVal");
        String tagComment = getRequestString("tagComment");
        try {

            if (StringUtils.isBlank(couponId) || StringUtils.isBlank(tagType)) {
                return MapMessage.errorMessage("参数异常");
            }
            CouponTagType type = CouponTagType.valueOf(tagType);
            if (type.getValueType() == 2 && StringUtils.isBlank(tagVal)) {
                return MapMessage.errorMessage("参数异常");
            }
            if (type.getValueType() == 1) {
                tagVal = "true";
            }
            // 查出当用户的TagList 暂时全部覆盖了吧。。。
            Map<String, CouponTag> tagMap = couponLoaderClient.loadCouponTagsGroupByType(couponId);
            CouponTag tag = tagMap.get(tagType);
            if (tag == null) {
                tag = new CouponTag();
                tag.setCouponId(couponId);
                tag.setTagType(type);
                tag.setTagValue(tagVal);
                tag.setTagComment(tagComment);
            } else {
                tag.setTagType(type);
                tag.setTagValue(tagVal);
                tag.setTagComment(tagComment);
            }
            MapMessage returnMsg = couponServiceClient.$saveCouponTag(couponId, tag);
            addAdminLog("修改礼券约束:" + tagType, couponId, returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存约束失败：id={},tag={},ex={}", couponId, tagType, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存约束失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "deltag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTag() {
        String couponId = getRequestString("couponId");
        String tagType = getRequestString("tagType");
        try {
            if (StringUtils.isBlank(couponId) || StringUtils.isBlank(tagType)) {
                return MapMessage.errorMessage("参数异常");
            }

            Map<String, CouponTag> tagMap = couponLoaderClient.loadCouponTagsGroupByType(couponId);
            CouponTag tag = tagMap.get(tagType);
            if (tag == null) {
                return MapMessage.errorMessage("Tag已经被删除，请勿重复操作！");
            }
            tag.setDisabled(true);
            MapMessage returnMsg = couponServiceClient.$saveCouponTag(couponId, tag);
            addAdminLog("删除礼券约束:" + tagType, couponId, returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("删除礼券约束失败：id={},tag={},ex={}", couponId, tagType, ex.getMessage(), ex);
            return MapMessage.errorMessage("删除礼券约束失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "sendcoupon.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendCoupon() {
        String couponId = getRequestString("couponId");
        String userIds = getRequestString("userId");
        if (StringUtils.isBlank(couponId) || StringUtils.isBlank(userIds)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            addAdminLog("发送礼券", couponId, "管理员像用户" + userIds + "发放礼券");
            String[] userIdList = userIds.split(",");
            for (String userId : userIdList) {
                Long uid = SafeConverter.toLong(userId);
                if (uid <= 0) {
                    continue;
                }

                couponServiceClient.sendCoupon(couponId, uid, "crm", getCurrentAdminUser().getAdminUserName());
            }

            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("发送失败:" + ex.getMessage(), ex);
        }
    }

    /*************private method******************/


    private void generateDetailTargets(String couponId, Model model) {
        Map<Integer, List<CouponTarget>> targetMap = couponLoaderClient.loadCouponTargetsGroupByType(couponId);
        int type = 5;
        List<Integer> regions = new ArrayList<>();
        if (targetMap.get(CouponTargetType.TARGET_TYPE_REGION.getType()) != null) {
            type = CouponTargetType.TARGET_TYPE_REGION.getType();
            regions = targetMap.get(type).stream().map(ad -> SafeConverter.toInt(ad.getTargetStr())).collect(Collectors.toList());
        }
        List<KeyValuePair<Integer, String>> targetTypes = CouponTargetType.toKeyValuePairs();
        for (KeyValuePair<Integer, String> target : targetTypes) {
            model.addAttribute("has_" + target.getKey(), targetMap.containsKey(target.getKey()));
        }
        model.addAttribute("targetType", type);
        model.addAttribute("targetRegion", JsonUtils.toJson(crmRegionService.buildRegionTree(regions)));
    }

    private void generateProducts(String couponId, Model model) {
        List<CouponProductRef> refs = couponLoaderClient.loadCouponProductRefs(couponId);
        List<String> products = refs.stream().map(CouponProductRef::getProductValue).collect(Collectors.toList());
        model.addAttribute("productTree", JsonUtils.toJson(buildProductTree(products)));
    }

    private void generateDetailTags(String couponId, Model model) {
        // 一个类型的Tag理论上应该只有一条与之对应
        Map<String, CouponTag> tagMap = couponLoaderClient.loadCouponTagsGroupByType(couponId);
        List<CouponTagType> tagList = Arrays.asList(CouponTagType.values());
        model.addAttribute("tagMap", tagMap);
        model.addAttribute("tagList", generateTagInfo(tagList, tagMap.keySet()));
    }

    private List<Map<String, Object>> generateTagInfo(List<CouponTagType> tagList, Set<String> exists) {
        boolean exist = CollectionUtils.isNotEmpty(exists);
        List<Map<String, Object>> results = new ArrayList<>();
        for (CouponTagType tag : tagList) {
            Map<String, Object> info = new HashMap<>();
            info.put("tagName", tag.name());
            info.put("tagDesc", tag.getDesc());
            info.put("exist", exist && exists.contains(tag.name()));
            info.put("tagType", tag.getValueType());
            info.put("instruction", tag.getInstruction());
            results.add(info);
        }
        return results;
    }

    // 获取产品树
    private List<Map<String, Object>> buildProductTree(Collection<String> products) {
        List<Map<String, Object>> allProductTree = buildAllProductTree();
        if (CollectionUtils.isNotEmpty(products)) {
            for (String product : products) {
                Map<String, Object> productInfo = allProductTree.stream().filter(m -> Objects.equals(SafeConverter.toString(m.get("key")), product))
                        .findAny().orElse(null);
                if (productInfo != null) {
                    productInfo.put("selected", Boolean.TRUE);
                } else {
                    for (Map<String, Object> parent : allProductTree) {
                        List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                        Map<String, Object> child = children.stream().filter(m -> Objects.equals(SafeConverter.toString(m.get("key")), product))
                                .findAny().orElse(null);
                        if (child != null) {
                            child.put("selected", Boolean.TRUE);
                        }
                    }
                }
            }
        }
        return allProductTree;
    }

    private List<Map<String, Object>> buildAllProductTree() {
        List<Map<String, Object>> retList = new ArrayList<>();
        List<OrderProduct> allProduct = userOrderLoaderClient.loadAvailableProductForCrm();
        for (OrderProductServiceType orderType : OrderProductServiceType.values()) {
            if (orderType == OrderProductServiceType.Unknown) {
                continue;
            }
            if (orderType.isOrderClosed()) {
                continue;
            }
            Map<String, Object> regionItemMap = new HashMap<>();
            regionItemMap.put("title", orderType.name());
            regionItemMap.put("key", orderType.name());
            // 获取子节点
            List<OrderProduct> productList = allProduct.stream().filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == orderType)
                    .filter(p -> Objects.equals("ONLINE", p.getStatus()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(productList)) {
                regionItemMap.put("children", new ArrayList());
            } else {
                List<Map<String, Object>> children = new ArrayList<>();
                for (OrderProduct product : productList) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("title", product.getName());
                    itemMap.put("key", product.getId());
                    children.add(itemMap);
                }
                regionItemMap.put("children", children);
            }
            retList.add(regionItemMap);
        }
        return retList;
    }
}
