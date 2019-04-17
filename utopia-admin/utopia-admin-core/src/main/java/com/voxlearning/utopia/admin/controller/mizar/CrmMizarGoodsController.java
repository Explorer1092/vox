package com.voxlearning.utopia.admin.controller.mizar;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/mizar/goods")
public class CrmMizarGoodsController extends CrmMizarAbstractController {

    private static final String BannerField = "banner";
    private static final String DetailField = "detail";

    private static final List<String> PhotoType = Arrays.asList(BannerField, DetailField);
    private static final int GOODS_PAGE_SIZE = 10;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String goodsIndex(Model model) {
        int page = getRequestInt("page", 1);
        page = page <= 0 ? 1 : page;
        String goodsName = getRequestString("goods");
        String shopId = getRequestString("sid");
        Pageable pageable = new PageRequest(page - 1, GOODS_PAGE_SIZE);
        Page<MizarShopGoods> mizarShopGoods;
        if (StringUtils.isBlank(shopId)) {
            mizarShopGoods = mizarLoaderClient.loadShopGoodsByPage(pageable, goodsName);
        } else {
            List<MizarShopGoods> mizarShops = mizarLoaderClient.loadShopGoodsByShop(shopId);
            mizarShops = mizarShops == null ? new ArrayList<>() : mizarShops;
            mizarShopGoods = PageableUtils.listToPage(mizarShops, pageable);
        }
        model.addAttribute("goodsList", mizarShopGoods.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", mizarShopGoods.getTotalPages());
        model.addAttribute("hasPrev", mizarShopGoods.hasPrevious());
        model.addAttribute("hasNext", mizarShopGoods.hasNext());
        model.addAttribute("goods", goodsName);
        model.addAttribute("sid", shopId);
        return "mizar/goods/goodsindex";
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String goodsInfo(Model model) {
        String goodsId = getRequestString("gid");
        String shopId = getRequestString("sid");
        MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(goodsId);
        if (StringUtils.isNotBlank(goodsId) && goods == null) {
            getAlertMessageManager().addMessageError("无效的课程ID : " + goodsId);
            return "mizar/goods/goodsindex";
        }
        model.addAttribute("sid", shopId);
        model.addAttribute("gid", goodsId);
        model.addAttribute("new", goods == null);
        model.addAttribute("goods", goods);
        return "mizar/goods/goodsinfo";
    }

    @RequestMapping(value = "savegoods.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveGoods() {
        String goodsId = getRequestString("gid");
        String shopId = getRequestString("sid"); // 机构ID
        String goodsName = getRequestString("goodsName"); // 课程名称
        String title = getRequestString("title"); // 课程标题
        String desc = getRequestString("desc"); // 课程简介
        String goodsHours = getRequestString("goodsHours"); // 课时
        String duration = getRequestString("duration"); // 时长
        String goodsTime = getRequestString("goodsTime"); // 上课时间
        String target = getRequestString("target"); // 年龄段
        String category = getRequestString("category"); // 课程分类
        String audition = getRequestString("audition"); // 试听
        Double price = getRequestDouble("price", 0D); // 课程现价
        Double originalPrice = getRequestDouble("originalPrice", 0D); // 课程原价
        String appointGift = getRequestString("appointGift"); // 预约礼
        String welcomeGift = getRequestString("welcomeGift"); // 到店礼
        String tags = getRequestString("tags"); // 课程标签
        Boolean recommended = getRequestBool("recommended"); // 是否推荐到首页
        String redirectUrl = getRequestString("redirectUrl"); // 跳转链接
        try {
            MizarShop shop = mizarLoaderClient.loadShopById(shopId);
            if (shop == null) {
                return MapMessage.errorMessage("无效的机构ID" + shopId);
            }
            MizarShopGoods goods = null;
            if (StringUtils.isNotBlank(goodsId)) {
                goods = mizarLoaderClient.loadShopGoodsById(goodsId);
            }
            if (goods == null) {
                goods = new MizarShopGoods();
            }
            goods.setShopId(shopId);
            goods.setGoodsName(goodsName);
            goods.setTitle(title);
            goods.setDesc(desc);
            goods.setGoodsHours(goodsHours);
            goods.setDuration(duration);
            goods.setGoodsTime(goodsTime);
            goods.setTarget(target);
            goods.setCategory(category);
            goods.setAudition(audition);
            goods.setPrice(price);
            goods.setOriginalPrice(originalPrice);
            goods.setAppointGift(appointGift);
            goods.setWelcomeGift(welcomeGift);
            goods.setTags(splitString(tags));
            goods.setRecommended(recommended);
            goods.setRedirectUrl(redirectUrl);
//            goods.setStatus(MizarGoodsStatus.OFFLINE);
            return mizarServiceClient.saveMizarShopGoods(goods);
        } catch (Exception ex) {
            logger.error("Save Mizar goods failed.", ex);
            return MapMessage.errorMessage("保存课程失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "approvegoods.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveGoods() {
        String goodsId = getRequestString("gid");
        try {
            MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(goodsId);
            if (goods == null) {
                return MapMessage.errorMessage("无效的课程信息");
            }
            if (!MizarGoodsStatus.PENDING.equals(goods.getStatus())) {
                return MapMessage.errorMessage("上线申请已被处理或者撤销");
            }
            goods.setStatus(MizarGoodsStatus.ONLINE);
            return mizarServiceClient.saveMizarShopGoods(goods);
        } catch (Exception ex) {
            logger.error("Save Mizar goods failed.", ex);
            return MapMessage.errorMessage("保存课程失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadGoodsPhoto() {
        String field = getRequestString("field");
        String goodsId = getRequestString("gid");
        String desc = getRequestString("desc");
        if (!PhotoType.contains(field)) {
            return MapMessage.errorMessage("上传类型错误");
        }
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请填写描述");
        }
        try {
            MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(goodsId);
            if (goods == null) {
                return MapMessage.errorMessage("无效的课程信息");
            }
            // 上传文件
            String fileName = uploadPhoto("file");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("图片上传失败");
            }
            switch (field) {
                case BannerField:
                    List<String> bannerPhoto = goods.getBannerPhoto() == null ? new ArrayList<>() : goods.getBannerPhoto();
                    bannerPhoto.add(fileName);
                    goods.setBannerPhoto(bannerPhoto);
                    break;
                case DetailField:
                    List<String> details = goods.getDetail() == null ? new ArrayList<>() : goods.getDetail();
                    details.add(fileName);
                    goods.setDetail(details);
                    break;
                default:
                    return MapMessage.errorMessage("上传类型错误");
            }
            return mizarServiceClient.saveMizarShopGoods(goods);
        } catch (Exception ex) {
            logger.error("Upload Mizar goods photo failed, goods={}, type={}", goodsId, field, ex);
            return MapMessage.errorMessage("上传失败");
        }
    }

    @RequestMapping(value = "deletephoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteGoodsPhoto() {
        String field = getRequestString("field");
        String goodsId = getRequestString("gid");
        String fileName = getRequestString("file");
        if (!PhotoType.contains(field)) {
            return MapMessage.errorMessage("图片类型错误");
        }
        try {
            MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(goodsId);
            if (goods == null) {
                return MapMessage.errorMessage("无效的课程信息");
            }
            // 删除文件
            switch (field) {
                case BannerField:
                    List<String> bannerPhoto = goods.getBannerPhoto();
                    if (CollectionUtils.isEmpty(bannerPhoto) || !bannerPhoto.contains(fileName)) {
                        return MapMessage.errorMessage("无效的图片信息");
                    }
                    bannerPhoto.removeIf(fileName::equals);
                    goods.setBannerPhoto(bannerPhoto);
                    break;
                case DetailField:
                    List<String> details = goods.getDetail();
                    if (CollectionUtils.isEmpty(details) || !details.contains(fileName)) {
                        return MapMessage.errorMessage("无效的图片信息");
                    }
                    details.removeIf(fileName::equals);
                    goods.setDetail(details);
                    break;
                default:
                    return MapMessage.errorMessage("图片类型错误");
            }
            MapMessage msg = mizarServiceClient.saveMizarShopGoods(goods);
            if (msg.isSuccess()) {
                deletePhoto(fileName);
            }
            return msg;
        } catch (Exception ex) {
            logger.error("Delete Mizar goods photo failed, goods={}, type={}, file={}", goodsId, field, fileName, ex);
            return MapMessage.errorMessage("删除失败：" + ex.getMessage());
        }
    }

}
