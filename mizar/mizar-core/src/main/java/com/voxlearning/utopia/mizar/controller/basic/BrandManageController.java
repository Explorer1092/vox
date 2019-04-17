package com.voxlearning.utopia.mizar.controller.basic;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.entity.BrandQueryContext;
import com.voxlearning.utopia.mizar.service.basic.MizarBrandManager;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基本信息-品牌相关 Controller
 * Created by yuechen.wang on 16/9/6.
 */
@Controller
@RequestMapping(value = "/basic/brand")
public class BrandManageController extends AbstractMizarController {

    @Inject private MizarBrandManager mizarBrandManager;

    // 基本信息首页列表
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1));
        Pageable pageable = new PageRequest(pageIndex - 1, PAGE_SIZE);//从0始开始

        // 初始化参数
        BrandQueryContext queryContext = new BrandQueryContext(pageable);
        queryContext.setBrandName(getRequestString("brandName"));

        Page<MizarBrand> brandPage = mizarBrandManager.page(getCurrentUser(), queryContext);

        model.addAllAttributes(queryContext.toParamMap());
        model.addAttribute("brandList", brandPage.getContent());
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("totalPage", Integer.max(1, brandPage.getTotalPages()));
        return "basic/brand/list";
    }

    /**
     * 机构选择品牌时的弹窗
     */
    @RequestMapping(value = "choose.vpage", method = RequestMethod.GET)
    public String listBrand(Model model) {
        int pageIndex = getRequestInt("pageIndex");
        pageIndex = pageIndex <= 0 ? 1 : pageIndex;
        String brandName = getRequestString("brandName");
        String brandId = getRequestString("brandId");
        Pageable pageable = new PageRequest(pageIndex - 1, PAGE_SIZE);
        List<MizarBrand> brandList = new ArrayList<>();
        int totalPage = 1;
        // 市场人员也可以新增机构
        // 此时，能选择的品牌应该在其所管辖的门店品牌之中
        // FIXME 后续可能会支持人员与品牌相关联
        if (getCurrentUser().isBD() || getCurrentUser().isShopOwner()) {
            Set<String> userBrand = mizarLoaderClient.loadShopByIds(currentUserShop()).values()
                    .stream()
                    .filter(shop -> StringUtils.isNotBlank(shop.getBrandId()))
                    .map(MizarShop::getBrandId)
                    .collect(Collectors.toSet());

            brandList = mizarLoaderClient.loadBrandByIds(userBrand).values()
                    .stream()
                    .filter(b -> StringUtils.isBlank(brandId) || brandId.equals(b.getId()))
                    .filter(b -> StringUtils.isBlank(brandName) || StringUtils.contains(b.getBrandName(), brandName))
                    .collect(Collectors.toList());
            totalPage = Integer.max(1, brandList.size() / PAGE_SIZE);

        } else {
            if (StringUtils.isNotBlank(brandId)) {
                MizarBrand brand = mizarLoaderClient.loadBrandById(brandId);
                if (brand != null) {
                    brandList.add(brand);
                    totalPage = 1;
                }
            }
            if (CollectionUtils.isEmpty(brandList)) {
                Page<MizarBrand> mizarBrands = mizarLoaderClient.loadBrandByPage(pageable, brandName);
                brandList = mizarBrands.getContent();
                totalPage = mizarBrands.getTotalPages();
            }
        }
        model.addAttribute("brandList", brandList);
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("brand", brandName);
        model.addAttribute("bid", brandId);
        return "basic/brand/choose";
    }

    @RequestMapping(value = "uploadimg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImg() {
        String teacherIntroduction = requestString("teacherIntroduction");
        try {
            // 上传文件
            MapMessage fileMsg = $uploadFile("file", MAXIMUM_UPLOAD_PHOTO_SIZE);
            if (!fileMsg.isSuccess()) {
                return fileMsg;
            }
            String imgUrl = SafeConverter.toString(fileMsg.get("fileName"));
            if (StringUtils.isBlank(imgUrl)) {
                return MapMessage.errorMessage("图片上传失败");
            }
            fileMsg = MapMessage.successMessage().add("imgUrl", imgUrl);
            if (StringUtils.isNotBlank(teacherIntroduction)) {
                fileMsg = fileMsg.add("teacherIntroduction", teacherIntroduction);
            }
            return fileMsg;
        } catch (Exception ex) {
            logger.error("Upload Mizar goods photo failed.", ex);
            return MapMessage.errorMessage("上传失败");
        }
    }

    @RequestMapping(value = "add.vpage", method = RequestMethod.GET)
    public String addBrand() {
        return "/basic/brand/add";
    }

    // 品牌编辑页面
    @RequestMapping(value = "edit.vpage", method = RequestMethod.GET)
    public String editBrand(Model model) {
        String brandId = requestString("id");
        MizarBrand mizarBrand;
        if (StringUtils.isNotBlank(brandId)) {
            mizarBrand = mizarLoaderClient.loadBrandById(brandId);
            model.addAttribute("mizarBrand", mizarBrand);
            if (CollectionUtils.isNotEmpty(mizarBrand.getFaculty())) {
                model.addAttribute("faculty", JSON.toJSONString(mizarBrand.getFaculty()));
            }
        }
        return "/basic/brand/edit";
    }

    // 品牌详情页面
    @RequestMapping(value = "view.vpage", method = RequestMethod.GET)
    public String viewBrand(Model model) {
        String brandId = requestString("id");
        MizarBrand mizarBrand;
        if (StringUtils.isNotBlank(brandId)) {
            mizarBrand = mizarLoaderClient.loadBrandById(brandId);
            model.addAttribute("mizarBrand", mizarBrand);
        }
        return "/basic/brand/view";
    }

    // 品牌保存
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveBrand() {
        String brandId = "New";
        try {
            // 获取填写内容
            MizarBrand mizarBrand = requestEntity(MizarBrand.class);
            mizarBrand.setShowList(SafeConverter.toBoolean(getRequestBool("showList")));
            mizarBrand.setFaculty(getTeachersInfo());
            mizarBrand.setPoints(requestStringList("points"));

            // 如果ID为空表示为新建
            brandId = mizarBrand.getId();
            if (StringUtils.isBlank(brandId)) {
                return mizarBrandManager.create(mizarBrand, getCurrentUser());
            } else {
                return mizarBrandManager.modify(mizarBrand, getCurrentUser());
            }
        } catch (Exception ex) {
            logger.error("Failed Save Mizar Brand, id={}", brandId, ex);
            return MapMessage.errorMessage("保存品牌失败:" + ex.getMessage());
        }
    }

}
