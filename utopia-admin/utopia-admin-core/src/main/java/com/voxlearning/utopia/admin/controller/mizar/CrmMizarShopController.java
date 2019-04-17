package com.voxlearning.utopia.admin.controller.mizar;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mizar/shop")
public class CrmMizarShopController extends CrmMizarAbstractController {

    private static final int SHOP_PAGE_SIZE = 10;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        int page = getRequestInt("page", 1);
        page = page <= 0 ? 1 : page;
        String shopName = getRequestString("shop");
        String brandId = getRequestString("bid");
        // 处理shopName
        String escapeName = "";
        if (StringUtils.isNotBlank(shopName)) {
            escapeName = StringRegexUtils.escapeExprSpecialWord(shopName);
        }
        Pageable pageable = new PageRequest(page - 1, SHOP_PAGE_SIZE);
        Page<MizarShop> mizarBrands;
        if (StringUtils.isBlank(brandId)) {
            mizarBrands = mizarLoaderClient.loadShopByPage(pageable, escapeName);
        } else {
            List<MizarShop> mizarShops = mizarLoaderClient.loadShopByBrand(brandId);
            mizarShops = mizarShops == null ? new ArrayList<>() : mizarShops;
            mizarBrands = PageableUtils.listToPage(mizarShops, pageable);
        }
        model.addAttribute("shopList", mizarBrands.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", mizarBrands.getTotalPages());
        model.addAttribute("hasPrev", mizarBrands.hasPrevious());
        model.addAttribute("hasNext", mizarBrands.hasNext());
        model.addAttribute("shop", shopName);
        model.addAttribute("bid", brandId);
        return "mizar/shop/shopindex";
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String shopInfo(Model model) {
        String shopId = getRequestString("sid");
        String brandId = getRequestString("bid");
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (StringUtils.isNotBlank(shopId) && shop == null) {
            getAlertMessageManager().addMessageError("无效的机构ID : " + shopId);
            return "mizar/shop/shopindex";
        }
        model.addAttribute("sid", shopId);
        model.addAttribute("bid", brandId);
        model.addAttribute("new", shop == null);
        model.addAttribute("shop", shop);
        Set<String> firstCategory = mizarLoaderClient.loadAllCategory().stream()
                .map(t -> SafeConverter.toString(t.get("firstCategory"))).collect(Collectors.toSet());
        model.addAttribute("categoryList", firstCategory);
        return "mizar/shop/shopinfo";
    }

    @RequestMapping(value = "saveshop.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveShop() {
        String shopId = getRequestString("sid");
        String brandId = getRequestString("bid"); // 品牌ID
        String fullName = getRequestString("fullName"); // 机构全称
        Boolean isVip = getRequestBool("vip"); //
        String shortName = getRequestString("shortName"); // 机构简称
        String introduction = getRequestString("intro"); // 机构介绍
        String shopType = getRequestString("shopType"); // 机构类型
        Integer regionCode = getRequestInt("region"); // 所属地区编码
        String tradeArea = getRequestString("area"); // 所属商圈
        String address = getRequestString("address"); // 详细地址
        Double longitude = getRequestDouble("longitude", 0D); // GPS经度
        Double latitude = getRequestDouble("latitude", 0D); // GPS纬度
        Boolean baiduGps = getRequestBool("baiduGps"); // 是否BaiduGps信息
        String contactPhone = getRequestString("phone"); // 联系方式
        String firstCategory = getRequestString("firstcat"); // 一级分类
        String secondCategory = getRequestString("secondcat"); // 二级分类
        String matchGrade = getRequestString("matchGrade"); // 支持的年级
        Integer cooperationLevel = requestInteger("cooperationLevel");     // 合作等级
        Integer type = requestInteger("type");                            // 线上机构或线下机构
        Integer adjustScore = requestInteger("adjustScore");             // 人工打分
        String shopStatus = getRequestString("shopStatus");             // 审核状态
        String welcomeGift = getRequestString("welcomeGift"); // 到店礼
        try {
            MizarBrand brand = mizarLoaderClient.loadBrandById(brandId);
            if (brand == null) {
                return MapMessage.errorMessage("无效的品牌ID");
            }
            MizarShop shop = null;
            if (StringUtils.isNotBlank(shopId)) {
                shop = mizarLoaderClient.loadShopById(shopId);
            }
            if (shop == null) {
                shop = new MizarShop();
            }
            shop.setBrandId(brandId);
            shop.setFullName(fullName);
            shop.setShortName(shortName);
            shop.setVip(isVip);
            shop.setIntroduction(introduction);
            shop.setShopType(shopType);
            shop.setRegionCode(regionCode);
            shop.setTradeArea(tradeArea);
            shop.setAddress(address);
            shop.setLongitude(longitude);
            shop.setLatitude(latitude);
            shop.setBaiduGps(baiduGps);
            shop.setContactPhone(splitString(contactPhone));
            shop.setFirstCategory(splitString(firstCategory));
            shop.setSecondCategory(splitString(secondCategory));
            shop.setMatchGrade(matchGrade);
            shop.setCooperationLevel(cooperationLevel);
            shop.setAdjustScore(adjustScore);
            shop.setType(type);
            shop.setShopStatus(shopStatus);
            shop.setWelcomeGift(welcomeGift);
            return mizarServiceClient.saveMizarShop(shop);
        } catch (Exception ex) {
            logger.error("Save Mizar shop failed.", ex);
            return MapMessage.errorMessage("保存机构失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadShopPhoto() {
        String shopId = getRequestString("sid");
        String desc = getRequestString("desc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请填写描述");
        }
        try {
            MizarShop shop = mizarLoaderClient.loadShopById(shopId);
            if (shopId == null) {
                return MapMessage.errorMessage("无效的机构信息");
            }
            // 上传文件
            String fileName = uploadPhoto("file");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("文件上传失败");
            }
            List<String> photo = shop.getPhoto() == null ? new ArrayList<>() : shop.getPhoto();
            photo.add(fileName);
            shop.setPhoto(photo);
            return mizarServiceClient.saveMizarShop(shop);
        } catch (Exception ex) {
            logger.error("Upload Mizar shop photo failed, shop={}", shopId, ex);
            return MapMessage.errorMessage("上传失败");
        }
    }

    @RequestMapping(value = "deletephoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteShopPhoto() {
        String shopId = getRequestString("sid");
        String fileName = getRequestString("file");
        if (StringUtils.isBlank(fileName)) {
            return MapMessage.errorMessage("无效的图片信息");
        }
        try {
            MizarShop shop = mizarLoaderClient.loadShopById(shopId);
            if (shopId == null) {
                return MapMessage.errorMessage("无效的机构信息");
            }
            // 删除文件
            List<String> photo = shop.getPhoto();
            if (CollectionUtils.isEmpty(photo) || !photo.contains(fileName)) {
                return MapMessage.errorMessage("无效的图片信息");
            }
            photo.removeIf(file -> StringUtils.equals(file, fileName));
            shop.setPhoto(photo);
            MapMessage msg = mizarServiceClient.saveMizarShop(shop);
            if (msg.isSuccess()) {
                deletePhoto(fileName);
            }
            return msg;
        } catch (Exception ex) {
            logger.error("Delete Mizar shop photo failed, shop={}, file={}", shopId, fileName, ex);
            return MapMessage.errorMessage("图片删除失败：" + ex.getMessage());
        }
    }



}
