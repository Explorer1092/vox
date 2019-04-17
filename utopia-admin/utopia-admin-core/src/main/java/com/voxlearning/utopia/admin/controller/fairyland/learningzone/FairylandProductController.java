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

package com.voxlearning.utopia.admin.controller.fairyland.learningzone;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.fairyland.AbstractFairylandController;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import com.voxlearning.utopia.service.vendor.api.FairylandProductService;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductRedirectType;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductStatus;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.client.FairylandProductServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.FairylandServiceClient;
import lombok.Cleanup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author peng
 * @since 16-6-23
 * crm不同平台的产品描述信息
 */
@Controller
@RequestMapping("/opmanager/fairylandProduct")
public class FairylandProductController extends AbstractFairylandController {

    @Inject private CrmImageUploader crmImageUploader;
    @Inject private FairylandServiceClient fairylandServiceClient;
    @Inject private FairylandProductServiceClient fairylandProductServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String fairylandProductIndex(
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String productType,
            Model model) {

        List<FairylandProduct> originalList = fairylandProductServiceClient.getFairylandProductService()
                .loadAllFairylandProductsFromDB()
                .getUninterruptibly();
        List<FairylandProduct> fairylandProducts = FairylandProductService.filterFairylandProducts(originalList, FairyLandPlatform.of(platform), FairylandProductType.of(productType));
        if (CollectionUtils.isNotEmpty(fairylandProducts)) {
            List<FairylandProduct> onlineFairylandProducts = fairylandProducts
                    .stream().filter((product) -> FairylandProductStatus.ONLINE.name().equals(product.getStatus()))
                    .sorted((p1, p2) -> p2.getRank().compareTo(p1.getRank()))
                    .collect(Collectors.toList());
            List<FairylandProduct> offlineFairylandProducts = fairylandProducts
                    .stream().filter((product) -> FairylandProductStatus.OFFLINE.name().equals(product.getStatus()))
                    .sorted((p1, p2) -> p2.getRank().compareTo(p1.getRank()))
                    .collect(Collectors.toList());
            model.addAttribute("onlineFairylandProducts", onlineFairylandProducts);
            model.addAttribute("offlineFairylandProducts", offlineFairylandProducts);
        }
        model.addAttribute("platformTypeMap", FairyLandPlatform.map);
        model.addAttribute("productTypeMap", FairylandProductType.map);
        model.addAttribute("platform", platform);
        model.addAttribute("productType", productType);
        return "/opmanager/fairyland/index";
    }

    @RequestMapping(value = "add.vpage", method = RequestMethod.POST)
    public String addFairylandProduct(Model model) {
        String appKey = getRequestString("appKey");
        String productType = getRequestString("productType");
        String platform = getRequestString("platform");
        String operationMessage = getRequestString("operationMessage");
        Boolean hotFlag = getRequestBool("hotFlag");
        String launchUrl = getRequestString("launchUrl");
        String launchBtnText = getRequestString("launchBtnText");
        Boolean newFlag = getRequestBool("newFlag");
        String productDesc = getRequestString("productDesc");
        String productName = getRequestString("productName");
        String suspendMessage = getRequestString("suspendMessage");
        String backgroundImage = getRequestString("backgroundImage");
        String status = getRequestString("status");
        int rank = getRequestInt("rank");
        String productIcon = getRequestString("productIcon");
        String productRectIcon = getRequestString("productRectIcon");
        String usePlatformDesc = getRequestString("usePlatformDesc");
        int baseUsingNum = getRequestInt("baseUsingNum");
        String redirectTypeStr = getRequestString("redirectType");
        Boolean recommendFlag = getRequestBool("recommendFlag");
        String catalogDesc = getRequestString("catalogDesc");
        String stagingLaunchUrl = getRequestString("stagingLaunchUrl");
        String promptMessage = getRequestString("promptMessage");
        String bannerImage = getRequestString("bannerImage");
        String descImage = getRequestString("descImage");


        try {
            validateParamNotNull("appKey", "appKey");
            validateParamNotNull("productType", "产品类型");
            validateParamNotNull("platform", "平台类型");
            validateParamNotNull("platform", "平台类型");
            validateParamNotNull("redirectType", "跳转类型");
            validateParamNotNullAndLength("productName", "产品名称", 10);
            validateParamLength("productDesc", "副标题描述", 40);
        } catch (Exception e) {
            model.addAttribute("errMsg", "参数验证失败" + e.getMessage());
            model.addAttribute("platformTypeMap", FairyLandPlatform.map);
            model.addAttribute("productTypeMap", FairylandProductType.map);
            model.addAttribute("fairylandProductRedirectTypeMap", FairylandProductRedirectType.map);
            return "/opmanager/fairylandProduct/addOrUpdate";
        }

        List<FairylandProduct> originalList = fairylandProductServiceClient.getFairylandProductService()
                .loadAllFairylandProductsFromDB()
                .getUninterruptibly();
        List<FairylandProduct> fairylandProducts = FairylandProductService.filterFairylandProducts(originalList, FairyLandPlatform.of(platform), FairylandProductType.of(productType));
        FairylandProduct fairylandProduct = fairylandProducts.stream()
                .filter(m -> (appKey.equals(m.getAppKey())))
                .findFirst()
                .orElse(null);
        FairylandProductRedirectType fairylandProductRedirectType = FairylandProductRedirectType.of(redirectTypeStr);

        if (fairylandProduct != null) {
            model.addAttribute("errMsg", "数据已经存在，无法插入");
            model.addAttribute("fairylandProduct", fairylandProduct);
            return "/opmanager/fairyland/addOrUpdate";
        } else {
            fairylandProduct = new FairylandProduct();
        }

        fairylandProduct.setAppKey(appKey);
        fairylandProduct.setProductType(productType);
        fairylandProduct.setPlatform(platform);
        fairylandProduct.setOperationMessage(operationMessage);
        fairylandProduct.setProductType(productType);
        fairylandProduct.setPlatform(platform);
        fairylandProduct.setHotFlag(hotFlag);
        fairylandProduct.setLaunchUrl(launchUrl);
        fairylandProduct.setLaunchBtnText(launchBtnText);
        fairylandProduct.setNewFlag(newFlag);
        fairylandProduct.setProductDesc(productDesc);
        fairylandProduct.setProductType(productType);
        fairylandProduct.setProductName(productName);
        fairylandProduct.setRank(rank);
        fairylandProduct.setProductIcon(productIcon);
        fairylandProduct.setProductRectIcon(productRectIcon);
        fairylandProduct.setBackgroundImage(backgroundImage);
        fairylandProduct.setStatus(status);
        fairylandProduct.setSuspendMessage(suspendMessage);
        fairylandProduct.setUsePlatformDesc(usePlatformDesc);
        fairylandProduct.setBaseUsingNum(baseUsingNum);
        fairylandProduct.setRedirectType(fairylandProductRedirectType);
        fairylandProduct.setRecommendFlag(recommendFlag);
        fairylandProduct.setCatalogDesc(catalogDesc);
        fairylandProduct.setStagingLaunchUrl(stagingLaunchUrl);
        fairylandProduct.setBannerImage(bannerImage);
        fairylandProduct.setDescImage(descImage);
        fairylandProduct.setPromptMessage(promptMessage);
        MapMessage mapMessage = fairylandServiceClient.insertFairylandProduct(fairylandProduct);
        if (mapMessage.isSuccess()) {
            return "redirect:/opmanager/fairylandProduct/index.vpage";
        } else {
            model.addAttribute("errMsg", mapMessage.getInfo());
            model.addAttribute("fairylandProduct", fairylandProduct);
            model.addAttribute("platformTypeMap", FairyLandPlatform.map);
            model.addAttribute("productTypeMap", FairylandProductType.map);
            model.addAttribute("fairylandProductRedirectTypeMap", FairylandProductRedirectType.map);
            return "/opmanager/fairyland/addOrUpdate";
        }

    }

    @RequestMapping(value = "update.vpage", method = RequestMethod.POST)
    public String updateFairylandProduct(Model model) {
        Long id = getRequestLong("fairylandProductId");
        String operationMessage = getRequestString("operationMessage");
        Boolean hotFlag = getRequestBool("hotFlag");
        String launchUrl = getRequestString("launchUrl");
        String stagingLaunchUrl = getRequestString("stagingLaunchUrl");
        String launchBtnText = getRequestString("launchBtnText");
        Boolean newFlag = getRequestBool("newFlag");
        String productDesc = getRequestString("productDesc");
        String productName = getRequestString("productName");
        String suspendMessage = getRequestString("suspendMessage");
        String backgroundImage = getRequestString("backgroundImage");
        String status = getRequestString("status");
        int rank = getRequestInt("rank");
        String productIcon = getRequestString("productIcon");
        String productRectIcon = getRequestString("productRectIcon");
        String usePlatformDesc = getRequestString("usePlatformDesc");
        int baseUsingNum = getRequestInt("baseUsingNum");
        Boolean recommendFlag = getRequestBool("recommendFlag");
        String redirectTypeStr = getRequestString("redirectType");
        FairylandProductRedirectType redirectType = FairylandProductRedirectType.of(redirectTypeStr);
        String catalogDesc = getRequestString("catalogDesc");
        String promptMessage = getRequestString("promptMessage");
        String bannerImage = getRequestString("bannerImage");
        String descImage = getRequestString("descImage");

        try {
            validateParamNotNullAndLength("productName", "产品标题", 10);
            validateParamLength("productDesc", "副标题描述", 40);
            validateParamLength("operationMessage", "运营消息", 15);
        } catch (Exception e) {
            model.addAttribute("errMsg", "参数验证失败" + e.getMessage());
            model.addAttribute("platformTypeMap", FairyLandPlatform.map);
            model.addAttribute("productTypeMap", FairylandProductType.map);
            return "/opmanager/fairylandProduct/addOrUpdate";
        }

        FairylandProduct fairylandProduct = fairylandProductServiceClient.getFairylandProductService()
                .loadFairylandProductFromDB(id)
                .getUninterruptibly();
        if (fairylandProduct == null) {
            model.addAttribute("errMsg", "数据不存在无法更新");
            model.addAttribute("platformTypeMap", FairyLandPlatform.map);
            model.addAttribute("productTypeMap", FairylandProductType.map);
            return "/opmanager/fairyland/addOrUpdate";
        }

        fairylandProduct.setOperationMessage(operationMessage);
        fairylandProduct.setHotFlag(hotFlag);
        fairylandProduct.setLaunchUrl(launchUrl);
        fairylandProduct.setLaunchBtnText(launchBtnText);
        fairylandProduct.setNewFlag(newFlag);
        fairylandProduct.setProductDesc(productDesc);
        fairylandProduct.setProductName(productName);
        fairylandProduct.setRank(rank);
        fairylandProduct.setSuspendMessage(suspendMessage);
        fairylandProduct.setProductIcon(productIcon);
        fairylandProduct.setProductRectIcon(productRectIcon);
        fairylandProduct.setBackgroundImage(backgroundImage);
        fairylandProduct.setStatus(status);
        fairylandProduct.setUsePlatformDesc(usePlatformDesc);
        fairylandProduct.setBaseUsingNum(baseUsingNum);
        fairylandProduct.setRecommendFlag(recommendFlag);
        fairylandProduct.setRedirectType(redirectType);
        fairylandProduct.setCatalogDesc(catalogDesc);
        fairylandProduct.setStagingLaunchUrl(stagingLaunchUrl);
        fairylandProduct.setBannerImage(bannerImage);
        fairylandProduct.setDescImage(descImage);
        fairylandProduct.setPromptMessage(promptMessage);
        MapMessage mapMessage = fairylandServiceClient.updateFairylandProduct(id, fairylandProduct);
        if (mapMessage.isSuccess()) {
            return "redirect:/opmanager/fairylandProduct/index.vpage";
        } else {
            model.addAttribute("fairylandProduct", fairylandProduct);
            model.addAttribute("errMsg", mapMessage.getInfo());
            model.addAttribute("platformTypeMap", FairyLandPlatform.map);
            model.addAttribute("productTypeMap", FairylandProductType.map);
            return "/opmanager/fairyland/addOrUpdate";
        }
    }

    @RequestMapping(value = "addOrUpdateIndex.vpage", method = RequestMethod.GET)
    public String updateFairylandProductIndex(Model model) {

        Long id = getRequestLong("fairylandProductId");
        FairylandProduct fairylandProduct = null;
        if (id > 0) {
            fairylandProduct = fairylandProductServiceClient.getFairylandProductService()
                    .loadFairylandProductFromDB(id)
                    .getUninterruptibly();
        }
        model.addAttribute("fairylandProduct", fairylandProduct);
        model.addAttribute("platformTypeMap", FairyLandPlatform.map);
        model.addAttribute("productTypeMap", FairylandProductType.map);
        model.addAttribute("fairylandProductRedirectTypeMap", FairylandProductRedirectType.map);
        return "/opmanager/fairyland/addOrUpdate";
    }

    @RequestMapping(value = "uploadImage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateImage(@RequestParam MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }
        String originalFileName = file.getOriginalFilename();
        try {
            String prefix = "fairylandProduct";
            @Cleanup InputStream inStream = file.getInputStream();
            String filePath = crmImageUploader.upload(prefix, originalFileName, inStream);
            return MapMessage.successMessage().set("filePath", filePath);
        } catch (Exception ex) {
            return MapMessage.errorMessage("上传图片异常,Error:" + ex.getMessage());
        }
    }

    /**
     * 上下架操作
     */
    @RequestMapping(value = "updateStatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateStatus() {
        Long id = getRequestLong("fairylandProductId");
        if (id == 0) {
            return MapMessage.errorMessage("上传id为空");
        }
        FairylandProduct fairylandProduct = fairylandProductServiceClient.getFairylandProductService()
                .loadFairylandProductFromDB(id)
                .getUninterruptibly();
        if (fairylandProduct == null) {
            return MapMessage.errorMessage("不存在应用");
        } else {
            if (FairylandProductStatus.ONLINE.name().equals(fairylandProduct.getStatus())) {
                fairylandProduct.setStatus(FairylandProductStatus.OFFLINE.name());
                fairylandServiceClient.updateFairylandProduct(id, fairylandProduct);
            } else if (FairylandProductStatus.OFFLINE.name().equals(fairylandProduct.getStatus())) {
                fairylandProduct.setStatus(FairylandProductStatus.ONLINE.name());
                fairylandServiceClient.updateFairylandProduct(id, fairylandProduct);
            }
            return MapMessage.successMessage();
        }
    }

    @RequestMapping(value = "sort.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sort() {
        String appKey = getRequestString("appKey");
        String platform = getRequestString("platform");
        String productType = getRequestString("productType");
        String rankType = getRequestString("rankType");
        if (StringUtils.isEmpty(appKey) ||
                StringUtils.isEmpty(platform) ||
                StringUtils.isEmpty(rankType)) {
            return MapMessage.errorMessage("参数不能为空");
        }

        List<FairylandProduct> onlineFairylandProducts = fairylandProductServiceClient.getFairylandProductService()
                .loadAllFairylandProductsFromDB()
                .getUninterruptibly()
                .stream()
                .filter((product) -> FairylandProductStatus.ONLINE.name().equals(product.getStatus()))
                .filter(p -> platform.equals(p.getPlatform()))
                .filter(p -> StringUtils.isBlank(productType) || productType.equals(p.getProductType()))
                .filter(p -> !SafeConverter.toBoolean(p.getDisabled(), false))
                .sorted((p1, p2) -> p2.getRank().compareTo(p1.getRank()))
                .collect(Collectors.toList());

        List<String> rankServiceTyps = onlineFairylandProducts
                .stream()
                .map(FairylandProduct::getAppKey)
                .collect(Collectors.toList());
        Map<String, FairylandProduct> productMap = onlineFairylandProducts
                .stream()
                .collect(Collectors.toMap(FairylandProduct::getAppKey, f -> f));

        int index = rankServiceTyps.indexOf(appKey);
        int size = rankServiceTyps.size();

        switch (rankType) {
            case "up":
                if (index == 0) return MapMessage.successMessage();
                rankServiceTyps.set(index, rankServiceTyps.get(index - 1));
                rankServiceTyps.set(index - 1, appKey);
                break;
            case "down":
                if (index == size - 1) return MapMessage.successMessage();
                rankServiceTyps.set(index, rankServiceTyps.get(index + 1));
                rankServiceTyps.set(index + 1, appKey);
                break;
            case "top":
                if (index == 0) return MapMessage.successMessage();
                rankServiceTyps.remove(index);
                rankServiceTyps.add(0, appKey);
                break;
            case "end":
                if (index == size - 1) return MapMessage.successMessage();
                rankServiceTyps.remove(index);
                rankServiceTyps.add(size - 1, appKey);
                break;
        }
        int maxRank = size;
        for (String rankServiceTyp : rankServiceTyps) {
            appKey = rankServiceTyp;
            if (productMap.get(appKey).getRank() == maxRank) {
                maxRank--;
                continue;
            }
            FairylandProduct fairylandProduct = productMap.get(appKey);
            fairylandProduct.setRank(maxRank);
            fairylandServiceClient.updateFairylandProduct(fairylandProduct.getId(), fairylandProduct);
            maxRank--;
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateImage() {
        Long id = getRequestLong("fairylandProductId");
        if (id == 0) {
            return MapMessage.errorMessage("上传id为空");
        } else {
            return fairylandServiceClient.deleteFairylandProduct(id);
        }
    }
}
