package com.voxlearning.utopia.mizar.controller.basic;


import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarGoodsController;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarAuditStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 活动管理 Controller
 * Created by yuechen.wang on 16/9/6.
 */
@Controller
@RequestMapping(value = "/basic/activity")
public class ActivityManageController extends AbstractMizarGoodsController {

    // 基本信息首页列表
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        int page = getRequestInt("pageIndex", 1);
        if (page < 1) page = 1;
        Pageable pageable = new PageRequest(page - 1, 10);
        MizarAuthUser currentUser = getCurrentUser();
        Page<MizarShopGoods> activityPage = new PageImpl<>(Collections.emptyList());
        List<String> types = Arrays.asList(MizarShopGoods.familyActivityType(), MizarShopGoods.usTalkType());
        // 运营人员
        if (currentUser.isOperator()) {
            activityPage = mizarLoaderClient.loadPageByGoodsType(pageable, null, types);
        } else if (currentUser.isShopOwner()) {
            // FIXME 此处预留了以后机构主使用的case，这次暂时没有测试功能。。。
            // 机构主只能看到自己名下的活动
            List<MizarShopGoods> userShop = mizarLoaderClient.loadShopGoodsByShop(currentUserShop()).values()
                    .stream().flatMap(List::stream)
                    .filter(t -> t.isFamilyActivity() || t.isUSTalkActivity())
                    .collect(Collectors.toList());
            activityPage = PageableUtils.listToPage(userShop, pageable);
        }
        model.addAttribute("pageIndex", page);
        model.addAttribute("totalPages", activityPage.getTotalPages());
        model.addAttribute("activityList", mapActivityList(activityPage.getContent()));
        return "basic/activity/activitylist";
    }

    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        String goodsId = getRequestString("gid");
        MizarShopGoods activity = mizarLoaderClient.loadShopGoodsById(goodsId);
        if (StringUtils.isNotBlank(goodsId) && activity == null) {
            return "redirect: /basic/activity/index.vpage";
        }
        if (activity != null) {
            // 传入的不是亲子活动的类型
            // 兼容 USTalk
            if (!activity.isFamilyActivity() && !activity.isUSTalkActivity()) {
                return "redirect: /basic/activity/index.vpage";
            }
            // 机构主需要校验这个活动是否有编辑权限
            if (getCurrentUser().isShopOwner() && activity.getShopId() != null && !currentUserShop().contains(activity.getShopId())) {
                return "redirect: /basic/activity/index.vpage";
            }
        }
        if (getCurrentUser().isShopOwner()) {
            List<String> shopIds = currentUserShop();
            List<Map<String, Object>> userShop = mizarLoaderClient.loadShopByIds(shopIds).values()
                    .stream().map(MizarShop::simpleInfo).collect(Collectors.toList());
            model.addAttribute("userShop", userShop);
        }
        boolean editable = activity == null || (activity.getStatus() != null && activity.getStatus() != MizarGoodsStatus.ONLINE);
        model.addAttribute("isNew", activity == null);
        model.addAttribute("editable", editable);
        model.addAttribute("activity", activity);
        if (editable) return "basic/activity/activitydetail";
        return "basic/activity/activityview";
    }

    @RequestMapping(value = "addactivity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addActivity() {
        try {
            MizarShopGoods activity = getRequestFamilyActivity();
            MapMessage validMsg = validateActivity(activity);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            MizarAuthUser currentUser = getCurrentUser();
            activity.setId(null);

            MizarEntityChangeRecord record = new MizarEntityChangeRecord();
            record.setAuditStatus(MizarAuditStatus.PENDING.name());
            record.setEntityType(MizarEntityType.FAMILY_ACTIVITY.getCode());
            record.setApplicant(currentUser.getRealName());
            record.setApplicantId(currentUser.getUserId());
            String content = JSON.toJSONString(activity);
            record.setContent(content);
            record.setDesc(activity.getGoodsName());
            // 保存record
            return mizarChangeRecordServiceClient.saveChangeRecord(record);
        } catch (Exception ex) {
            logger.error("Save Mizar goods failed.", ex);
            return MapMessage.errorMessage("保存课程失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "editactivity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editActivity() {
        String goodsId = getRequestString("gid");
        try {
            MizarShopGoods activity = mizarLoaderClient.loadShopGoodsById(goodsId);
            // 传入的不是亲子活动的shopId
            // 兼容USTalk
            if (activity == null || (!activity.isFamilyActivity() && !activity.isUSTalkActivity())) {
                return MapMessage.errorMessage("无效的活动：" + goodsId);
            }
            if (activity.getStatus() != null && MizarGoodsStatus.ONLINE == activity.getStatus()) {
                return MapMessage.errorMessage("该活动已经上线，不允许编辑");
            }
            MizarShopGoods change = getRequestFamilyActivity();
            MapMessage validMsg = validateActivity(change);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }

            MizarShopGoods diff = BeanUtils.getInstance().beanDiff(activity, change);
            String content = JSON.toJSONString(diff);
            MizarAuthUser currentUser = getCurrentUser();

            // 保存之前先要判断一下是否有针对这个记录的更新
            MizarEntityChangeRecord record = mizarChangeRecordLoaderClient.loadByTarget(goodsId, MizarEntityType.FAMILY_ACTIVITY.getCode());
            if (record != null && MizarAuditStatus.PENDING.name().equals(record.getAuditStatus())) {
                return MapMessage.errorMessage("已经有一条变更记录等待审核!");
            }
            if (record == null) {
                record = new MizarEntityChangeRecord();
                record.setAuditStatus(MizarAuditStatus.PENDING.name());
                record.setEntityType(MizarEntityType.FAMILY_ACTIVITY.getCode());
                record.setApplicant(currentUser.getRealName());
                record.setApplicantId(currentUser.getUserId());
            }
            record.setAuditStatus(MizarAuditStatus.PENDING.name());
            record.setApplicant(currentUser.getRealName());
            record.setApplicantId(currentUser.getUserId());
            record.setTargetId(goodsId);
            record.setContent(content);
            record.setDesc(activity.getGoodsName());
            return mizarChangeRecordServiceClient.saveChangeRecord(record);
        } catch (Exception ex) {
            logger.error("Save Mizar goods failed.", ex);
            return MapMessage.errorMessage("编辑课程保存失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "changestatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeGoodsStatus() {
        String goodsId = getRequestString("gid");
        String status = getRequestString("status");
        try {
            MizarGoodsStatus goodsStatus = MizarGoodsStatus.parse(status);
            MizarShopGoods activity = mizarLoaderClient.loadShopGoodsById(goodsId);
            if (goodsStatus == null || activity == null) {
                return MapMessage.errorMessage("无效的参数");
            }
            // 传入的不是亲子活动的类型
            // 兼容USTalk
            if (!activity.isFamilyActivity() && !activity.isUSTalkActivity()) {
                return MapMessage.errorMessage("无效的亲子活动：" + goodsId);
            }
            activity.setStatus(goodsStatus);
            return mizarServiceClient.saveMizarShopGoods(activity);
        } catch (Exception ex) {
            logger.error("Failed change status, gid={}, status={}", goodsId, status, ex);
            return MapMessage.errorMessage("状态变更失败：" + ex.getMessage());
        }
    }

    /**
     * ueditor  配置
     *
     * @return
     */
    @RequestMapping(value = "ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        String action = getRequestString("action");
        switch (action) {
            case "config":
                return MapMessage.successMessage()
                        .add("imageActionName", "uploadimage")
                        .add("imageFieldName", "upfile")
                        .add("imageInsertAlign", "none")
                        .add("imageMaxSize", 2048000)
                        .add("imageUrlPrefix", "");
            case "uploadimage":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");
                if (imgFile.isEmpty()) {
                    return MapMessage.errorMessage("没有文件上传");
                }
                String originalFileName = imgFile.getOriginalFilename();
                try {
                    String url = $uploadFile("upfile");
                    return MapMessage.successMessage()
                            .add("url", url)
                            .add("title", imgFile.getName())
                            .add("state", "SUCCESS")
                            .add("original", originalFileName);
                } catch (Exception ex) {
                    logger.error("上传图片异常： " + ex.getMessage(), ex);
                    return MapMessage.errorMessage("上传图片异常： " + ex.getMessage());
                }
            default:
                return MapMessage.successMessage();
        }
    }

    private List<Map<String, Object>> mapActivityList(List<MizarShopGoods> activities) {
        if (CollectionUtils.isEmpty(activities)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> activityList = new ArrayList<>();
        for (MizarShopGoods activity : activities) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", activity.getId());
            info.put("title", activity.getTitle());
            info.put("desc", activity.getDesc());
            info.put("status", activity.getStatus() == null ? "离线" : activity.getStatus().getDesc());
            info.put("goodsType", SafeConverter.toString(activity.getGoodsType()));
            activityList.add(info);
        }
        return activityList;
    }
}
