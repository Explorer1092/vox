package com.voxlearning.utopia.mizar.controller.basic;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarChangeRecordLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by alex on 2016/11/11.
 */
@Controller
@RequestMapping(value = "/basic/apply")
public class MyApplyController extends AbstractMizarController {

    @Inject private MizarChangeRecordLoaderClient mizarChangeRecordLoaderClient;

    // 基本信息首页列表
    @RequestMapping(value = "index.vpage")
    public String index(Model model) {
        // 类型
        String type = getRequestString("type");
        // 状态
        String status = getRequestString("status");
        Integer pageNum = getRequestInt("page", 1); // 当前页

        // 申请人查自己的
        MizarAuthUser curUser = getCurrentUser();
        List<MizarEntityChangeRecord> recordList = mizarChangeRecordLoaderClient.loadByApplicant(curUser.getUserId());
        recordList = recordList.stream()
                .filter(p -> StringUtils.isBlank(type) || type.equals(p.getEntityType()))
                .filter(p -> StringUtils.isBlank(status) || status.equals(p.getAuditStatus()))
                .collect(Collectors.toList());
        // 机构主和BD需要过滤掉不是自己名下的门店信息
        if (curUser.isBD() || curUser.isShopOwner()) {
            List<String> userShops = currentUserShop();
            recordList = recordList.stream()
                    .filter(p -> !MizarEntityType.SHOP.getName().equals(p.getEntityType()) || userShops.contains(p.getTargetId()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("auditList", splitList(recordList, 10));
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("page", pageNum);
        return "basic/apply/applylist";
    }

    @RequestMapping(value = "changedetail.vpage", method = RequestMethod.GET)
    public String shopDetail(Model model) {
        MizarAuthUser curUser = getCurrentUser();

        String id = getRequestString("id");
        MizarEntityChangeRecord record = mizarChangeRecordLoaderClient.load(id);
        if (record == null || !Objects.equals(record.getApplicantId(), curUser.getUserId())) {
            return "redirect: index.vpage";
        }
        MizarEntityType entityType = MizarEntityType.of(record.getEntityType());
        if (entityType == null) {
            return "redirect: index.vpage";
        }
        switch (entityType) {
            case BRAND:
                MizarBrand oldBrand = mizarLoaderClient.loadBrandById(record.getTargetId());
                if (oldBrand == null) {
                    oldBrand = new MizarBrand();
                }

                MizarBrand newBrand = JSON.parseObject(record.getContent(), MizarBrand.class);
                model.addAttribute("analysis", BeanUtils.getInstance().analyse(oldBrand, newBrand));
                model.addAttribute("record", record);
                return "basic/apply/brandchangedetail";
            case SHOP:
                // 原机构信息
                MizarShop oldShop = mizarLoaderClient.loadShopById(record.getTargetId());
                if (oldShop == null) {
                    oldShop = new MizarShop();
                }

                MizarShop newShop = JSON.parseObject(record.getContent(), MizarShop.class);//新的待审核的机构信息
                model.addAttribute("analysis", BeanUtils.getInstance().analyse(oldShop, newShop));
                model.addAttribute("record", record);
                return "basic/apply/shopchangedetail";
            case GOODS:
                MizarShopGoods oldGoods = mizarLoaderClient.loadShopGoodsById(record.getTargetId());
                if (oldGoods == null) {
                    oldGoods = new MizarShopGoods();
                }
                MizarShopGoods newGoods = JSON.parseObject(record.getContent(), MizarShopGoods.class);
                model.addAttribute("analysis", BeanUtils.getInstance().analyse(oldGoods, newGoods));

                // 门店信息
                String shopId = oldGoods.getShopId() != null ? oldGoods.getShopId() : newGoods.getShopId();
                MizarShop shop = mizarLoaderClient.loadShopById(shopId);
                if (shop != null) {
                    model.addAllAttributes(shop.simpleInfo());
                }
                return "basic/apply/goodschangedetail";
            case FAMILY_ACTIVITY:
                MizarShopGoods oldActivity = mizarLoaderClient.loadShopGoodsById(record.getTargetId());
                if (oldActivity == null) {
                    oldActivity = new MizarShopGoods();
                }

                MizarShopGoods newActivity = JSON.parseObject(record.getContent(), MizarShopGoods.class);
                model.addAttribute("analysis", BeanUtils.getInstance().analyse(oldActivity, newActivity));
                return "basic/apply/activitychangedetail";
            default:
                return "redirect: index.vpage";
        }
    }
}
