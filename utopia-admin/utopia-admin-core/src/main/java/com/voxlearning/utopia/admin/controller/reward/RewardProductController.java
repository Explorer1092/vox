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

package com.voxlearning.utopia.admin.controller.reward;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.athena.SearchEngineServiceClient;
import com.voxlearning.utopia.admin.data.RewardProductMapper;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.action.api.support.PrivilegeOrigin;
import com.voxlearning.utopia.service.action.api.support.PrivilegeType;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeManagerClient;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.api.filter.RewardCategoryFilter;
import com.voxlearning.utopia.service.reward.api.filter.RewardTagFilter;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.TobyDressMapper;
import com.voxlearning.utopia.service.reward.constant.*;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;
import static com.voxlearning.utopia.api.constant.PopupType.DEFAULT_AD;
import static com.voxlearning.utopia.service.reward.entity.RewardCategory.SubCategory.COUPON;
import static com.voxlearning.utopia.service.reward.entity.RewardCategory.SubCategory.COURSE_WARE;

/**
 * Created by XiaoPeng.Yang on 14-7-16.
 */
@Controller
@RequestMapping("/reward/product")
@Slf4j
public class RewardProductController extends RewardAbstractController {

    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject private SearchEngineServiceClient searchEngineServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private RewardLoaderClient rewardLoaderClient;
    @Inject private PrivilegeManagerClient privilegeManagerClient;
    @Inject private RewardCenterClient rewardCenterClient;

    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @Deprecated
    @RequestMapping(value = "productlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    private String categoryList(Model model) {

        Integer pageNumber = getRequestInt("pageNumber", 1);
        long tagId = getRequestLong("tagId", 0L);
        long categoryId = getRequestLong("categoryId", 0L);
        String productType = getRequestParameter("productType", "");
        String productName = getRequestParameter("productName", "");
        String onlined = getRequestParameter("onlined", "true");
        String schoolVisible = getRequestParameter("schoolVisible", "");
        String displayTerminal = getRequestParameter("displayTerminal", "");
        Long productId = getRequestLong("productId");

        Pageable pageable = new PageRequest(pageNumber - 1, 10);

        List<RewardProduct> rewardProductList = crmRewardService.$loadRewardProducts()
                .stream()
                .sorted((o1, o2) -> {
                    long c1 = SafeConverter.toLong(o1.getStudentOrderValue());
                    long c2 = SafeConverter.toLong(o2.getStudentOrderValue());
                    if (c1 == c2) {
                        return o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
                    } else {
                        return Long.compare(c2, c1);
                    }
                })
                .collect(Collectors.toList());
        Stream<RewardProduct> stream = rewardProductList.stream();

        if (categoryId != 0) {
            Set<Long> productIds = crmRewardService.$findRewardProductCategoryRefsByCategoryId(categoryId)
                    .stream()
                    .map(RewardProductCategoryRef::getProductId)
                    .collect(Collectors.toSet());
            stream = stream.filter(e -> productIds.contains(e.getId()));
        }
        if (tagId != 0) {
            Set<Long> productIds = crmRewardService.$findRewardProductTagRefsByTagId(tagId)
                    .stream()
                    .map(RewardProductTagRef::getProductId)
                    .collect(Collectors.toSet());
            stream = stream.filter(e -> productIds.contains(e.getId()));
        }
        if (StringUtils.isNotBlank(productName)) {
            stream = stream.filter(e -> e.getProductName() != null)
                    .filter(e -> e.getProductName().contains(productName));
        }

        if (productId != 0)
            stream = stream.filter(e -> Objects.equals(e.getId(), productId));

        if (StringUtils.isNotBlank(productType)) {
            stream = stream.filter(e -> StringUtils.equals(productType, e.getProductType()));
        }
        if (StringUtils.isNotBlank(onlined)) {
            stream = stream.filter(e -> {
                boolean b1 = Boolean.parseBoolean(onlined);
                boolean b2 = SafeConverter.toBoolean(e.getOnlined());
                return b1 == b2;
            });
        }

        if (StringUtils.isNotBlank(displayTerminal)) {
            stream = stream.filter(e -> StringUtils.isBlank(e.getDisplayTerminal())
                    || e.getDisplayTerminal().contains(displayTerminal));
        }

        if (StringUtils.isNotBlank(schoolVisible)) {
            switch (schoolVisible) {
                case "1":
                    stream = stream.filter(e -> SafeConverter.toBoolean(e.getPrimarySchoolVisible()));
                    break;
                case "2":
                    stream = stream.filter(e -> SafeConverter.toBoolean(e.getJuniorSchoolVisible()));
                    break;
                default:
                    break;
            }
        }
        rewardProductList = stream.collect(Collectors.toList());

        Map<Long, Integer> productInventory = new HashMap<>();
        // 获取图片
        List<RewardImage> images = crmRewardService.$loadRewardImages();
        List<Map<String, Object>> indexList = new ArrayList<>();
        int inventory;
        List<RewardSku> rewardSkus;
        for (RewardProduct product : rewardProductList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("productName", product.getProductName());
            map.put("productType", product.getProductType());
            // 显示兑换积分
            map.put("priceS", product.getPriceOldS());
            map.put("priceT", product.getPriceOldT());
            map.put("soldQuantity", product.getSoldQuantity());
            map.put("wishQuantity", product.getWishQuantity());
            map.put("studentVisible", product.getStudentVisible());
            map.put("teacherVisible", product.getTeacherVisible());
            map.put("onlined", product.getOnlined());
            map.put("studentOrderValue", product.getStudentOrderValue());
            map.put("teacherOrderValue", product.getTeacherOrderValue());
            map.put("buyingPrice", product.getBuyingPrice());
            map.put("remarks", product.getRemarks());

            rewardSkus = crmRewardService.$findRewardSkusByProductId(product.getId());
            if (rewardSkus != null) {
                inventory = rewardSkus.stream()
                        .mapToInt(RewardSku::getInventorySellable)
                        .sum();
                map.put("inventory", inventory);
            }

            if (!RewardProductType.JPZX_SHIWU.equals(product.getProductType())) {
                productInventory.put(product.getId(), 0);
            }
            List<RewardImage> productImages = images.stream()
                    .filter(e -> e.getProductId() != null)
                    .filter(e -> Objects.equals(e.getProductId(), product.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(productImages)) {
                String imgUrl = productImages.get(0).getLocation();
                if (imgUrl.toLowerCase().startsWith("http")) {
                    map.put("img", imgUrl);
                } else {
                    String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
                    map.put("img", prePath + "/gridfs/" + imgUrl);
                }
            }
            indexList.add(map);
        }

        Page<Map<String, Object>> productPage = PageableUtils.listToPage(indexList, pageable);
        if (CollectionUtils.isNotEmpty(productPage.getContent())) {
            for (Map<String, Object> bean : productPage.getContent()) {
                Long proId = SafeConverter.toLong(bean.get("id"));
                if (proId == null) {
                    continue;
                }
                Integer proInventory = productInventory.get(proId);
                if (proInventory == null) {
                    continue;
                }
                Map<Long, List<RewardCouponDetail>> map = rewardLoaderClient.loadProductRewardCouponDetails(Arrays.asList(proId));
                if (MapUtils.isEmpty(map) || CollectionUtils.isEmpty(map.get(proId))) {
                    continue;
                }
                proInventory = map.get(proId).stream().filter(e -> !Boolean.TRUE.equals(e.getExchanged())).collect(Collectors.toList()).size();
                bean.put("inventory", proInventory);
            }
        }

        model.addAttribute("productPage", productPage);
        model.addAttribute("pageNumber", pageNumber);

        List<RewardCategory> categorys = crmRewardService.$loadRewardCategories();
        List<RewardTag> tags = crmRewardService.$loadRewardTags();
        model.addAttribute("categorys", categorys);
        model.addAttribute("tags", tags);
        model.addAttribute("types", RewardProductType.values());

        model.addAttribute("productType", productType);
        model.addAttribute("tagId", tagId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("productName", productName);
        model.addAttribute("onlined", onlined);
        model.addAttribute("schoolVisible", schoolVisible);
        model.addAttribute("displayTerminal", displayTerminal);
        model.addAttribute("productId", productId);

        return "reward/product/productlist";
    }

    /**
     * 添加
     */
    @Deprecated
    @RequestMapping(value = "addproduct.vpage", method = RequestMethod.GET)
    private String preAddProduct(Model model) {
        List<RewardTag> allTags = crmRewardService.$loadRewardTags();
        List<RewardTag> oneLevelTags = RewardTagFilter.filter(allTags, RewardTagLevel.ONE_LEVEL, null);
        List<RewardTag> twoLevelTags = RewardTagFilter.filter(allTags, RewardTagLevel.TWO_LEVEL, null);

        model.addAttribute("oneLevelTags", oneLevelTags);
        model.addAttribute("twoLevelTags", twoLevelTags);
        model.addAttribute("types", RewardProductType.values());
        model.addAttribute("couponResourceTypes", RewardCouponResource.values());
        model.addAttribute("saleGroup", RewardOrderSaleGroup.values());
        return "reward/product/addproduct";
    }

    /**
     * 编辑
     */
    @Deprecated
    @RequestMapping(value = "editproduct.vpage", method = RequestMethod.GET)
    private String preEditProduct(Model model) {
        Long productId = getRequestLong("productId");
        if (productId == 0L) {
            return "reward/product/productlist";
        }

        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        List<RewardProductCategoryRef> categoryRefs = crmRewardService.$findRewardProductCategoryRefsByProductId(productId);
        List<RewardProductTagRef> tagRefs = crmRewardService.$findRewardProductTagRefsByProductId(productId);
        List<RewardSku> skus = crmRewardService.$findRewardSkusByProductId(productId);

        model.addAttribute("product", product);
        model.addAttribute("categoryRefs", categoryRefs);
        model.addAttribute("couponResourceTypes", RewardCouponResource.values());
        model.addAttribute("tagRefs", tagRefs);
        model.addAttribute("skus", skus);

        List<RewardTag> allTags = crmRewardService.$loadRewardTags();
        List<RewardTag> oneLevelTags = RewardTagFilter.filter(allTags, RewardTagLevel.ONE_LEVEL, null);
        List<RewardTag> twoLevelTags = RewardTagFilter.filter(allTags, RewardTagLevel.TWO_LEVEL, null);
        model.addAttribute("oneLevelTags", oneLevelTags);
        model.addAttribute("twoLevelTags", twoLevelTags);
        model.addAttribute("types", RewardProductType.values());
        model.addAttribute("saleGroup", RewardOrderSaleGroup.values());

        // 加载兑换券数据
        RewardCoupon coupon = crmRewardService.loadRewardCouponByPID(productId);
        if (coupon != null) {
            model.addAttribute("sendSms", coupon.getSendSms());
            model.addAttribute("sendMsg", coupon.getSendMsg());
            model.addAttribute("msgTpl", coupon.getMsgTpl());
            model.addAttribute("smsTpl", coupon.getSmsTpl());
        }

        return "reward/product/addproduct";
    }

    @RequestMapping(value = "getcategorys.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage getCategorys() {
        String productType = getRequestParameter("productType", "");

        RewardProductType rewardProductType;
        try {
            rewardProductType = RewardProductType.valueOf(productType);
        } catch (IllegalArgumentException ex) {
            return MapMessage.errorMessage("参数错误");
        }

        List<RewardCategory> allCategories = crmRewardService.$loadRewardCategories();
        List<RewardCategory> categories = RewardCategoryFilter.filter(allCategories, rewardProductType, null);
        return MapMessage.successMessage().add("categories", categories);
    }

    @RequestMapping(value = "headwears.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getHeadWears() {
        // 只显示可兑换的那些
        List<Map<String, String>> headWears = privilegeManagerClient.getPrivilegeManager()
                .loadAllPrivilegesFromDB()
                .getUninterruptibly()
                .stream()
                .filter(p -> p.getOrigin() == PrivilegeOrigin.EXCHANGE)
                .map(p -> {
                    Map<String, String> pMap = new HashMap<>();
                    pMap.put("id", p.getId());
                    pMap.put("name", p.getName());
                    return pMap;
                })
                .collect(Collectors.toList());

        return MapMessage.successMessage().add("headwears", headWears);
    }

    @RequestMapping(value = "tobyDressList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTobyDresses() {
        // 只显示可兑换的那些
        TobyDressMapper mapper = rewardCenterClient.loadTobyDress();

        return MapMessage.successMessage().add("tobyDressList", mapper.getTobyDressList());
    }

    @Deprecated
    @RequestMapping(value = "addproduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addProduct(@RequestBody final RewardProductMapper mapper) {
        RewardProduct rewardProduct = RewardProductMapper.convert(mapper);
        MapMessage resultMsg = MapMessage.successMessage();

        try {
            String operation = rewardProduct.getId() != null ? "编辑奖品" : "添加奖品";
            String comment = getCurrentAdminUser().getAdminUserName() + "设置进货价为: " + rewardProduct.getBuyingPrice();
            addAdminLog(operation, rewardProduct.getId(), comment);
            resultMsg = rewardManagementClient.addRewardProduct(rewardProduct, mapper.getCategoryIds(), mapper.getTagIds(), mapper.getSkus());
        } catch (Exception ex) {
            logger.error("Failed to add reward product", ex);
            return MapMessage.errorMessage("编辑失败！");
        }

        if (!resultMsg.isSuccess())
            return resultMsg;

        Long productId = SafeConverter.toLong(resultMsg.get("productId"));

        // 如果是课件或者是兑换券类型，需要生成兑换券数据
        String categoryCode = mapper.getCategoryCode();
        if (Arrays.asList(COUPON.name(), COURSE_WARE.name()).contains(categoryCode)) {

            RewardCoupon newCoupon = RewardProductMapper.extractCoupon(mapper);
            newCoupon.setProductId(productId);
            // 学豆扣除备注设置成和产品名字一样
            newCoupon.setIntegralComment(mapper.getProductName());

            RewardCoupon existCoupon = crmRewardService.loadRewardCouponByPID(productId);
            if (existCoupon != null)
                newCoupon.setId(existCoupon.getId());

            resultMsg = rewardManagementClient.addRewardCoupon(newCoupon);
            resultMsg.add("productId", productId);
        }

        return resultMsg;
    }

    @Deprecated
    @RequestMapping(value = "updownlined.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upDownLined() {
        long productId = getRequestLong("productId");
        if (productId <= 0) {
            return MapMessage.errorMessage();
        }
        boolean onLined = getRequestBool("onLined");

        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        if (product == null) {
            return MapMessage.errorMessage();
        }
        product.setOnlined(onLined);
        if (onLined) {
            product.setOnlineDatetime(new Date());
        } else {
            product.setOfflineDatetime(new Date());
        }

        product = crmRewardService.$upsertRewardProduct(product);
        if (product != null) {
            return MapMessage.successMessage().setInfo("操作成功");
        } else {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }


    @Deprecated
    @RequestMapping(value = "updateordervalue.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateOrderValue() {
        long productId = getRequestLong("productId");
        if (productId <= 0) {
            return MapMessage.errorMessage();
        }

        String valueType = getRequestString("valueType");
        Integer orderValue = getRequestInt("orderValue");

        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        if (product == null) {
            return MapMessage.errorMessage();
        }

        if (valueType.contains("student")) {
            product.setStudentOrderValue(orderValue);
        } else {
            product.setTeacherOrderValue(orderValue);
        }

        product.setUpdateDatetime(new Date());
        product = crmRewardService.$upsertRewardProduct(product);
        if (product != null) {
            return MapMessage.successMessage().setInfo("操作成功");
        } else {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }

    //首页推荐设置列表页
    @RequestMapping(value = "listrewardindex.vpage", method = RequestMethod.GET)
    public String listRewardIndex(Model model) {
        Collection<RewardIndex> indexes = crmRewardService.$loadRewardIndices();
        model.addAttribute("indexList", indexes);
        return "reward/product/listrewardindex";
    }

    //添加首页推荐奖品
    @RequestMapping(value = "addindexproduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addIndexProduct() {
        Long productId = getRequestLong("productId");
        Integer displayOrder = getRequestInt("displayOrder");
        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        if (product == null) {
            return MapMessage.errorMessage().setInfo("奖品不存在");
        }
        RewardIndex indexProduct = new RewardIndex();
        indexProduct.setProductId(productId);
        indexProduct.setDisplayOrder(displayOrder);
        indexProduct = crmRewardService.$upsertRewardIndex(indexProduct);
        if (indexProduct == null) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage().setInfo("操作成功");
    }

    @RequestMapping(value = "deleteindexproduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteIndexProduct() {
        Long indexId = getRequestLong("indexId");
        boolean ret = crmRewardService.$removeRewardIndex(indexId);
        if (ret) {
            return MapMessage.successMessage().setInfo("操作成功");
        } else {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }

    //上传图片 奖品图片
    @Deprecated
    @RequestMapping(value = "uploadproductimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadProductImage(MultipartFile files, @RequestParam("productId") Long productId) {
        if (files.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }

        String relateAttr = getRequestString("relateAttr");
        String relateValue = getRequestString("relateValue");

        String originalFileName = files.getOriginalFilename();

        try {
            String ext = StringUtils.substringAfterLast(originalFileName, ".");
            ext = StringUtils.defaultString(ext).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(ext);

            String filePath = AdminOssManageUtils.upload(files, "reward");
            if (StringUtils.isBlank(filePath)) {
                return MapMessage.errorMessage("图片上传失败");
            }

            RewardImage image = new RewardImage();
            image.setProductId(productId);
            image.setLocation(filePath);
            image.setRelateAttr(relateAttr);
            image.setRelateValue(relateValue);
            image = crmRewardService.$upsertRewardImage(image);

            if (image == null) {
                return MapMessage.errorMessage();
            }

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.warn("upload file failed", ex);
            return MapMessage.errorMessage("图片上传失败");
        }
    }

    //上传奖品描述图片
    @Deprecated
    @RequestMapping(value = "uploaddescriptionimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadProductDescriptionImage(MultipartFile imgFile, @RequestParam("productId") Long productId) {
        MapMessage mapMessage = new MapMessage();
        if (imgFile.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }
        String originalFileName = imgFile.getOriginalFilename();
        String prePath = RuntimeMode.isUsingProductionData() ? "http://cdn-portrait.17zuoye.cn/" : "http://www.test.17zuoye.net";
        String prefix = "rpdi-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + productId;
        try {
            @Cleanup InputStream inStream = imgFile.getInputStream();
            String filename = crmImageUploader.upload(prefix, originalFileName, inStream);
            mapMessage.add("url", prePath + "/gridfs/" + filename);
            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            log.error("上传奖品图片异常： " + ex.getMessage());
        }
        return mapMessage;

    }

    //上传图片
    @Deprecated
    @RequestMapping(value = "productimagelist.vpage", method = RequestMethod.GET)
    public String productImageList(Model model) {
        String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
        Long productId = getRequestLong("productId");
        List<RewardImage> images = crmRewardService.$loadRewardImages().stream()
                .filter(e -> e.getProductId() != null)
                .filter(e -> Objects.equals(e.getProductId(), productId))
                .collect(Collectors.toList());
        model.addAttribute("images", images);
        model.addAttribute("productId", productId);
        model.addAttribute("prePath", prePath);
        return "reward/product/productimagelist";
    }

    //删除图片
    @Deprecated
    @RequestMapping(value = "deleteproductimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteProductImage() {
        Long imageId = getRequestLong("imageId");
        RewardImage image = crmRewardService.$loadRewardImage(imageId);
        if (image == null) {
            return MapMessage.errorMessage("奖品图片{}不存在", imageId);
        }
        try {
            crmImageUploader.deletePhotoByFilename(image.getLocation());
            crmRewardService.$removeRewardImage(imageId);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("删除奖品图片异常", ex);
            return MapMessage.errorMessage("删除失败");
        }
    }

    @RequestMapping(value = "producttarget.vpage", method = RequestMethod.GET)
    public String productTarget(Model model) {
        Long productId = getRequestLong("productId");
        // 不能直接用getProduct那个方法，否则下线的情况也会被拦住
        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        if (product == null) {
            model.addAttribute("error", "无效的产品!");
            return "reward/product/producttarget";
        }

        // 记录是第几页过来的
        int pageNumber = getRequestInt("fromPage");
        model.addAttribute("fromPage", pageNumber);

        List<Set<String>> labels = new ArrayList<>();
        List<RewardProductTarget> targets;
        List<Integer> regions = new ArrayList<>();

        Map<Integer, List<RewardProductTarget>> targetMap = rewardLoaderClient.loadRewardTargetGroupByType(productId);
        // 默认是全部区域
        int type = RewardProductTargetType.TARGET_TYPE_ALL.getType();
        if ((targets = targetMap.get(RewardProductTargetType.TARGET_TYPE_REGION.getType())) != null) {
            type = RewardProductTargetType.TARGET_TYPE_REGION.getType();
            regions = targets.stream().map(c -> SafeConverter.toInt(c.getTargetStr()))
                    .collect(Collectors.toList());
        }

        Arrays.stream(RewardProductTargetType.values())
                .forEach(t -> model.addAttribute("has_" + t.getType(), targetMap.containsKey(t.getType())));

        model.addAttribute("targetType", type);
        model.addAttribute("targetRegion", JsonUtils.toJson(crmRegionService.buildRegionTree(regions)));
        model.addAttribute("labelTree", JsonUtils.toJson(searchEngineServiceClient.getLabelTree()));
        model.addAttribute("targetLabel", labels);

        model.addAttribute("product", product);
        return "reward/product/producttarget";
    }

    /**
     * 保存商品的投放区域
     *
     * @return
     */
    @RequestMapping(value = "saveregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetRegion() {

        Long productId = getRequestLong("productId");
        Integer type = getRequestInt("type");
        String regions = getRequestString("regionList");

        if (RewardProductTargetType.of(type) != RewardProductTargetType.TARGET_TYPE_REGION) {
            return MapMessage.errorMessage("无效的参数！");
        }

        if (StringUtils.isBlank(regions)) {
            return MapMessage.errorMessage("选择地区不能为空！");
        }

        try {
            List<String> regionList = Arrays.asList(regions.split(","));
            return rewardServiceClient.saveProductTargets(productId, type, regionList, false);
        } catch (Exception ex) {
            logger.error("保存投放地区失败! id={},type={}, ex={}", productId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放地区失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "saveids.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetIds() {
        Long productId = getRequestLong("productId");
        Integer type = getRequestInt("type");
        String targetIds = getRequestString("targetIds");
        Boolean append = getRequestBool("append");
        RewardProductTargetType targetType = RewardProductTargetType.of(type);

        if (targetType != RewardProductTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }

        if (StringUtils.isBlank(targetIds)) {
            return MapMessage.errorMessage("请输入有效的内容！");
        }

        try {
            // 没有校验用户输入是否符合规范
            List<String> targetList = Arrays.stream(targetIds.split(DEFAULT_LINE_SEPARATOR))
                    .map(t -> t.replaceAll("\\s", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            return rewardServiceClient.saveProductTargets(productId, type, targetList, append);
        } catch (Exception ex) {
            logger.error("保存投放用户失败:id={},type={},ex={}", productId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放用户失败:" + ex.getMessage(), ex);
        }
    }

    /**
     * 清空投放对象
     *
     * @return
     */
    @RequestMapping(value = "cleartargets.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearTargets() {
        Long productId = getRequestLong("productId");
        Integer type = getRequestInt("type");

        RewardProductTargetType targetType = RewardProductTargetType.of(type);
        if (targetType != RewardProductTargetType.TARGET_TYPE_REGION
                && targetType != RewardProductTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }

        try {
            return rewardServiceClient.clearProductTargets(productId, type);
        } catch (Exception ex) {
            logger.error("清空投放对象失败:id={},type={},ex={}", productId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("清空投放对象失败:" + ex.getMessage(), ex);
        }
    }

    @Deprecated
    @RequestMapping(value = "couponimportindex.vpage", method = RequestMethod.GET)
    public String couponImportIndex(Model model) {
        Long productId = getRequestLong("productId");
        if (productId == 0) {
            return "reward/product/productlist";
        }
        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        List<RewardCouponDetail> detailList = rewardLoaderClient.getRewardCouponDetailLoader().loadProductRewardCouponDetails(productId);
        model.addAttribute("product", product);
        model.addAttribute("couponSize", detailList.size());
        return "reward/product/coupondetail";
    }

    @Deprecated
    @RequestMapping(value = "importcouponno.vpage", method = RequestMethod.POST)
    public String importCouponNo(@RequestParam String couponNo, @RequestParam Long productId, Model model) {
        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        model.addAttribute("product", product);
        if (StringUtils.isEmpty(couponNo) || productId == null || productId == 0) {
            getAlertMessageManager().addMessageInfo("请输入兑换码，一行一条");
            return "reward/product/coupondetail";
        }
        String[] couponNos = couponNo.trim().split("\n");
        List<String> lstSuccess = new ArrayList<>();
        List<String> lstFailed = new ArrayList<>();
        for (String no : couponNos) {
            String realNo = no.replaceAll(" ", "").trim();
            try {
                if (StringUtils.isNotBlank(realNo)) {
                    RewardCouponDetail detail = new RewardCouponDetail();
                    detail.setCouponNo(realNo);
                    detail.setProductId(productId);
                    if (rewardManagementClient.persistRewardCouponDetail(detail).isSuccess()) {
                        lstSuccess.add(realNo);
                    } else
                        lstFailed.add(realNo);
                } else {
                    lstFailed.add(realNo);
                }
            } catch (Exception ex) {
                lstFailed.add(realNo);
            }
        }
        List<RewardCouponDetail> detailList = rewardLoaderClient.getRewardCouponDetailLoader().loadProductRewardCouponDetails(productId);
        model.addAttribute("couponSize", detailList.size());
        model.addAttribute("successSize", lstSuccess.size());
        model.addAttribute("wrongSize", lstFailed.size());
        return "reward/product/coupondetail";
    }

    @RequestMapping(value = "generatecouponno.vpage", method = RequestMethod.POST)
    public String generateCouponNo(@RequestParam String prefix, @RequestParam Long productId,
                                   @RequestParam Integer num, Model model) {
        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        model.addAttribute("product", product);
        if (StringUtils.isEmpty(prefix) || productId == null || num == 0) {
            getAlertMessageManager().addMessageInfo("请正确输入参数");
            return "reward/product/coupondetail";
        }
        List<String> lstSuccess = new ArrayList<>();
        List<String> lstFailed = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            final String couponNo = prefix + generateRandomCouponNo();
            try {
                List<RewardCouponDetail> details = rewardLoaderClient.getRewardCouponDetailLoader().loadProductRewardCouponDetails(productId);
                details = details.stream().filter(source -> StringUtils.equals(source.getCouponNo(), couponNo))
                        .collect(Collectors.toList());

                RewardCouponDetail coupon = MiscUtils.firstElement(details);
                if (coupon != null) {
                    lstFailed.add(couponNo);
                    continue;
                }
                RewardCouponDetail detail = new RewardCouponDetail();
                detail.setCouponNo(couponNo);
                detail.setProductId(productId);
                rewardManagementClient.persistRewardCouponDetail(detail);
                lstSuccess.add(couponNo);
            } catch (Exception ex) {
                lstFailed.add(couponNo);
            }
        }
        List<RewardCouponDetail> detailList = rewardLoaderClient.getRewardCouponDetailLoader().loadProductRewardCouponDetails(productId);
        model.addAttribute("couponSize", detailList.size());
        model.addAttribute("successSize", lstSuccess.size());
        model.addAttribute("wrongSize", lstFailed.size());
        return "reward/product/coupondetail";
    }

    /**
     * 生成兑换码 10位数字
     *
     * @return
     */
    private String generateRandomCouponNo() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int temp = RandomUtils.nextInt(0, 99);
            if (temp < 10) {
                builder.append("0" + temp);
            } else {
                builder.append(temp);
            }

        }
        return builder.toString();
    }

    @RequestMapping(value = "downloadexchangedata.vpage", method = RequestMethod.GET)
    public void downloadExchangeData(HttpServletResponse response) {
        Long productId = getRequestLong("productId", 0L);
        List<RewardCouponDetail> couponDetails = rewardLoaderClient.getRewardCouponDetailLoader().loadProductRewardCouponDetails(productId);

        couponDetails = couponDetails.stream().filter(RewardCouponDetail::getExchanged).collect(Collectors.toList());

        HSSFWorkbook hssfWorkbook = convertToExchangeHSS(couponDetails);
        String filename = "已兑换数据-奖品ID" + productId + "-" + DateUtils.dateToString(new Date()) + ".xls";
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException ignored) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                log.error("download exchange coupon data exception!");
            }
        }
    }

    private HSSFWorkbook convertToExchangeHSS(List<RewardCouponDetail> couponDetails) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow firstRow = hssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("用户ID");
        firstRow.createCell(1).setCellValue("用户姓名");
        firstRow.createCell(2).setCellValue("学校");
        firstRow.createCell(3).setCellValue("班级");
        firstRow.createCell(4).setCellValue("报名手机号");
        firstRow.createCell(5).setCellValue("家长ID");
        firstRow.createCell(6).setCellValue("家长姓名");
        firstRow.createCell(7).setCellValue("家长手机号");
        firstRow.createCell(8).setCellValue("兑换时间");

        int rowNum = 1;
        for (RewardCouponDetail couponDetail : couponDetails) {
            User user = userLoaderClient.loadUser(couponDetail.getUserId());
            StudentParent sp = parentLoaderClient.loadStudentKeyParent(user.getId());
            User keyParent = sp == null ? null : sp.getParentUser();
            School school = asyncStudentServiceClient.getAsyncStudentService()
                    .loadStudentSchool(user.getId())
                    .getUninterruptibly();
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());

            HSSFRow hssfRow = hssfSheet.createRow(rowNum++);
            hssfRow.createCell(0).setCellValue(couponDetail.getUserId());
            hssfRow.createCell(1).setCellValue(user.fetchRealname());
            if (school != null) {
                hssfRow.createCell(2).setCellValue(school.getCname());
            }
            if (clazz != null) {
                hssfRow.createCell(3).setCellValue(clazz.formalizeClazzName());
            }
            hssfRow.createCell(4).setCellValue(couponDetail.getSensitiveMobile());
            if (keyParent != null) {
                hssfRow.createCell(5).setCellValue(keyParent.getId());
                hssfRow.createCell(6).setCellValue(keyParent.fetchRealname());
                //FIXME: 以后有需要再说
                //hssfRow.createCell(7).setCellValue(userLoaderClient.loadUserAuthentication(keyParent.getId()).getSensitiveMobile());
                hssfRow.createCell(7).setCellValue("(TODO)");
            }
            hssfRow.createCell(8).setCellValue(DateUtils.dateToString(couponDetail.getExchangedDate()));
        }
        hssfSheet.setColumnWidth(0, 300 * 15);
        hssfSheet.setColumnWidth(1, 300 * 15);
        hssfSheet.setColumnWidth(2, 600 * 15);
        hssfSheet.setColumnWidth(3, 300 * 15);
        hssfSheet.setColumnWidth(4, 300 * 15);
        hssfSheet.setColumnWidth(5, 300 * 15);
        hssfSheet.setColumnWidth(6, 300 * 15);
        hssfSheet.setColumnWidth(7, 300 * 15);
        hssfSheet.setColumnWidth(8, 300 * 15);
        return hssfWorkbook;
    }

    @RequestMapping(value = "importuseddata.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage importUsedData() {
        String userIds = getRequestParameter("userIds", "");
        Long productId = getRequestLong("productId");
        if (StringUtils.isEmpty(userIds)) {
            return MapMessage.errorMessage("请输入用户ID，一行一条");
        }
        if (productId == null) {
            return MapMessage.errorMessage("产品ID错误");
        }
        String[] idArray = userIds.trim().split("\n");
        List<Long> userIdList = new ArrayList<>();
        for (String id : idArray) {
            String realId = id.replaceAll(" ", "");
            if (realId != null && realId.length() > 0) {
                Long userId = Long.parseLong(realId);
                userIdList.add(userId);
                String content = "好消息！你的家长已经同意体验贝乐英语，你可以领取 1000 学豆奖励了！" +
                        "<a href=\"http://17zuoye.com/reward/order/myexperience.vpage\">点击这里，申请领取 1000 学豆奖励</a>";
                userPopupServiceClient.createPopup(userId)
                        .content(content)
                        .type(DEFAULT_AD)
                        .category(LOWER_RIGHT)
                        .unique(true)
                        .create();
            }
        }
        try {
            String sql = "UPDATE VOX_REWARD_COUPON_DETAIL SET USED=TRUE,USED_DATE=NOW() WHERE PRODUCT_ID=:productId AND USER_ID IN (:userIds)";
            utopiaSqlReward.withSql(sql).useParams(MiscUtils.map("productId", productId).add("userIds", userIdList)).executeUpdate();
            String key = CacheKeyGenerator.generateCacheKey(RewardCouponDetail.class, "productId", productId);
            adminCacheSystem.CBS.flushable.delete(key);
        } catch (Exception ex) {
            log.error("CRM导入兑换卷已使用数据失败");
        }
        addAdminLog("管理员" + getCurrentAdminUser().getAdminUserName() + "导入了虚拟兑换卷ID：" + productId + "的已使用数据" + userIdList.size() + "条");
        return MapMessage.successMessage("导入成功");
    }

    @RequestMapping(value = "headwearlist.vpage", method = RequestMethod.GET)
    public String headWears(Model model) {
        // 获取全部的特权
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 30);

        List<Privilege> headWears = privilegeManagerClient.getPrivilegeManager()
                .loadAllPrivilegesFromDB()
                .getUninterruptibly()
                .stream()
                .sorted((p1, p2) -> p2.getCreateTime().compareTo(p1.getCreateTime()))
                .collect(Collectors.toList());

        // 处理图片的路径，拼接上gridfs的前缀
        headWears.forEach(h -> {
            String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
            h.setImg(prePath + "/gridfs/" + h.getImg());
        });

        Page<Privilege> headwearsPage = PageableUtils.listToPage(headWears, pageable);
        model.addAttribute("headWearsPage", headwearsPage);
        model.addAttribute("currentPage", headwearsPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", headwearsPage.getTotalPages());
        model.addAttribute("hasPrev", headwearsPage.hasPrevious());
        model.addAttribute("hasNext", headwearsPage.hasNext());

        // 来源
        model.addAttribute("origins", Arrays.stream(PrivilegeOrigin.values())
                .map(p -> p.name()).collect(Collectors.toList()));

        // 类型
        model.addAttribute("type", Arrays.stream(PrivilegeType.values())
                .map(p -> p.name()).collect(Collectors.toList()));

        return "reward/product/headwears";
    }

    @RequestMapping(value = "headwear.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getHeadWear() {
        String id = getRequestString("headWearId");
        Privilege headwear = privilegeManagerClient.getPrivilegeManager()
                .loadAllPrivilegesFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .orElse(null);
        if (headwear == null)
            return MapMessage.errorMessage("头饰不存在!");

        return MapMessage.successMessage().add("headwear", headwear);
    }

    @Deprecated
    @RequestMapping(value = "headwear.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateHeadWear() {
        String id = getRequestString("id");
        String name = getRequestString("name");
        String code = getRequestString("code");
        String origin = getRequestString("origin");
        String type = getRequestString("type");
        Boolean displayInCenter = getRequestBool("displayInCenter");
        String acquireCondition = getRequestString("acquireCondition");

        if (StringUtils.isEmpty(id))
            id = null;

        final String theId = id;
        Privilege existHeadWear = privilegeManagerClient.getPrivilegeManager()
                .loadAllPrivilegesFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> Objects.equals(e.getId(), theId))
                .findFirst()
                .orElse(null);
        if (existHeadWear == null) {
            existHeadWear = new Privilege();
        }

        existHeadWear.setId(id);
        existHeadWear.setName(name);
        existHeadWear.setCode(code);
        existHeadWear.setType(PrivilegeType.valueOf(type));
        existHeadWear.setOrigin(PrivilegeOrigin.valueOf(origin));
        existHeadWear.setDisplayInCenter(displayInCenter);
        existHeadWear.setAcquireCondition(acquireCondition);

        Privilege upserted = privilegeManagerClient.getPrivilegeManager()
                .upsertPrivilege(existHeadWear)
                .getUninterruptibly();
        if (upserted == null) {
            return MapMessage.errorMessage("保存头饰失败!");
        }
        return MapMessage.successMessage().add("id", upserted.getId());
    }

    @RequestMapping(value = "uploadheadwearimg.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SneakyThrows
    public MapMessage uploadHeadWearImg(MultipartFile file) {
        String id = getRequestString("headWearId");
        Privilege headwear = privilegeManagerClient.getPrivilegeManager()
                .loadAllPrivilegesFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .orElse(null);
        if (headwear == null)
            return MapMessage.errorMessage("头饰不存在!");

        String originalFileName = file.getOriginalFilename();
        String prefix = "hw-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + id;

        try (InputStream inStream = file.getInputStream()) {
            String filename = crmImageUploader.upload(prefix, originalFileName, inStream);

            headwear.setImg(filename);
            privilegeManagerClient.getPrivilegeManager().upsertPrivilege(headwear).awaitUninterruptibly();
            return MapMessage.successMessage();
        }
    }

}
