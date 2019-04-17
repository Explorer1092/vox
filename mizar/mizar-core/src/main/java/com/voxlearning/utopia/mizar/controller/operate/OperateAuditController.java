package com.voxlearning.utopia.mizar.controller.operate;


import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.audit.MizarAuditContext;
import com.voxlearning.utopia.mizar.audit.MizarAuditProcessor;
import com.voxlearning.utopia.mizar.audit.MizarAuditProcessorFactory;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarAuditStatus;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarChangeRecordLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运营使用，审核信息 Controller
 * Created by yuechen.wang on 16/10/10.
 */
@Controller
@RequestMapping(value = "/operate/audit")
public class OperateAuditController extends AbstractMizarController {

    @Inject private MizarChangeRecordLoaderClient mizarChangeRecordLoaderClient;
    @Inject private MizarAuditProcessorFactory mizarAuditProcessorFactory;

    // 基本信息首页列表
    @RequestMapping(value = "index.vpage")
    public String index(Model model) {
        String type = getRequestString("type");
        String status = getRequestString("status");
        String token = getRequestString("token");
        String applicant = getRequestString("applicant");//申请人
        Integer pageNum = getRequestInt("page", 1); // 当前页

        if (StringUtils.isBlank(status)) status = MizarAuditStatus.PENDING.name();
        List<MizarEntityChangeRecord> recordList = new ArrayList<>();
        // 如果有人员的话，优先根据人员查询，然后过滤状态
        if (StringUtils.isNotBlank(token)) {
            MizarUser user = mizarUserLoaderClient.loadUserByToken(token);
            if (user != null) {
                recordList = mizarChangeRecordLoaderClient.loadByApplicant(user.getId());
            }
        } else {
            recordList = mizarChangeRecordLoaderClient.loadByStatus(status);
        }
        // 过滤类型
        if (StringUtils.isNotBlank(type)) {
            recordList = recordList.stream()
                    .filter(r -> type.equals(r.getEntityType()))
                    .collect(Collectors.toList());
        }
        //申请人,根据名称或id匹配
        if (StringUtils.isNotBlank(applicant)) {
            recordList = recordList.stream()
                    .filter(r -> StringUtils.contains(r.getApplicant(), applicant) || StringUtils.contains(r.getApplicantId(), applicant))
                    .collect(Collectors.toList());
        }
        //不可以看到自己提交的申请
        recordList = recordList.stream()
                .filter(r -> !StringUtils.equals(r.getApplicantId(), currentUserId()))
                .collect(Collectors.toList());
        model.addAttribute("auditList", splitList(recordList, 10));
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("token", token);
        model.addAttribute("page", pageNum);
        model.addAttribute("applicant", applicant);
        return "operate/audit/index";
    }

    @RequestMapping(value = "auditbrand.vpage", method = RequestMethod.GET)
    public String auditBrand(Model model) {
        String id = getRequestString("id");
        String view = getRequestString("view");
        String targetId = getRequestString("targetId");
        MizarEntityChangeRecord record = mizarChangeRecordLoaderClient.load(id);
        MizarBrand oldBrand = null;//原来的品牌信息
        if (StringUtils.isBlank(record.getTargetId())) {
            //新增的情况
            oldBrand = new MizarBrand();
        } else {
            String brandId = record.getTargetId();
            oldBrand = mizarLoaderClient.loadBrandById(brandId);
        }
        MizarBrand newBrand = JSON.parseObject(record.getContent(), MizarBrand.class);//新的待审核的机构信息
        model.addAttribute("analysis", BeanUtils.getInstance().analyse(oldBrand, newBrand));
        model.addAttribute("record", record);
        if(StringUtils.isNotBlank(view)){
            //标识,只能查看,不能审核
            model.addAttribute("view", view);
        }
        // 自己不能审核，以及非PENDING状态不能审核
        model.addAttribute("available", !currentUserId().equals(record.getApplicantId()) && MizarAuditStatus.PENDING.name().equals(record.getAuditStatus()));
        return "operate/audit/auditbrand";
    }

    @RequestMapping(value = "auditshop.vpage", method = RequestMethod.GET)
    public String shopDetail(Model model) {
        String id = getRequestString("id");
        String view = getRequestString("view");
        String targetId = getRequestString("targetId");
        MizarEntityChangeRecord record = mizarChangeRecordLoaderClient.load(id);
        if (record == null) {
            return redirect("/operate/audit/index.vpage");
        }
        MizarShop oldShop;//原来的机构信息
        if (StringUtils.isBlank(record.getTargetId())) {
            //新增的情况
            oldShop = new MizarShop();
        } else {
            String shopId = record.getTargetId();
            oldShop = mizarLoaderClient.loadShopById(shopId);
        }
        MizarShop newShop = JSON.parseObject(record.getContent(), MizarShop.class);//新的待审核的机构信息
        model.addAttribute("analysis", BeanUtils.getInstance().analyse(oldShop, newShop));
        model.addAttribute("record", record);
        if (StringUtils.isNotBlank(view)) {
            //标识,只能查看,不能审核
            model.addAttribute("view", view);
        }
        // 自己不能审核，以及非PENDING状态不能审核
        model.addAttribute("available", !currentUserId().equals(record.getApplicantId()) && MizarAuditStatus.PENDING.name().equals(record.getAuditStatus()));
        return "operate/audit/auditshop";
    }

    // 课程详情页面
    @RequestMapping(value = "goodsinfo.vpage", method = RequestMethod.GET)
    public String goodsInfo(Model model) {
        String goodsId = getRequestString("gid");
        String recordId = getRequestString("rid");
        MizarEntityChangeRecord record = mizarChangeRecordLoaderClient.load(recordId);
        if (record == null) {
            return "redirect: index.vpage";
        }
        MizarShopGoods goods = null;
        // 没传gid的话当做新增处理
        if (StringUtils.isNotBlank(record.getTargetId()) && StringUtils.isNotBlank(goodsId)) {
            if (record.getTargetId().equals(goodsId)) {
                goods = mizarLoaderClient.loadShopGoodsById(goodsId);
            }
        }
        if (goods == null) goods = new MizarShopGoods();
        MizarShopGoods change = JSON.parseObject(record.getContent(), MizarShopGoods.class);
        model.addAttribute("analysis", BeanUtils.getInstance().analyse(goods, change));
        // 门店信息
        String shopId = goods.getShopId() != null ? goods.getShopId() : change.getShopId();
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop != null) model.addAllAttributes(shop.simpleInfo());
        model.addAttribute("gid", goodsId);
        model.addAttribute("rid", recordId);
        // 自己不能审核，以及非PENDING状态不能审核
        model.addAttribute("available", !currentUserId().equals(record.getApplicantId()) && MizarAuditStatus.PENDING.name().equals(record.getAuditStatus()));
        return "operate/audit/auditgoods";
    }

    // 亲子活动详情页面
    @RequestMapping(value = "activityinfo.vpage", method = RequestMethod.GET)
    public String activityInfo(Model model) {
        String goodsId = getRequestString("gid");
        String recordId = getRequestString("rid");
        MizarEntityChangeRecord record = mizarChangeRecordLoaderClient.load(recordId);
        if (record == null) {
            return "redirect: index.vpage";
        }
        MizarShopGoods activity = null;
        // 没传gid的话当做新增处理
        if (StringUtils.isNotBlank(record.getTargetId()) && StringUtils.isNotBlank(goodsId)) {
            if (record.getTargetId().equals(goodsId)) {
                activity = mizarLoaderClient.loadShopGoodsById(goodsId);
            }
        }
        if (activity == null) {
            activity = new MizarShopGoods();
        } else if (!activity.isFamilyActivity() && !activity.isUSTalkActivity()) {
            return "redirect: index.vpage";
        }
        MizarShopGoods change = JSON.parseObject(record.getContent(), MizarShopGoods.class);

        model.addAttribute("analysis", BeanUtils.getInstance().analyse(activity, change));
        model.addAttribute("gid", goodsId);
        model.addAttribute("rid", recordId);
        // 自己不能审核，以及非PENDING状态不能审核
        model.addAttribute("available", !currentUserId().equals(record.getApplicantId()) && MizarAuditStatus.PENDING.name().equals(record.getAuditStatus()));
        return "operate/audit/auditactivity";
    }


    // 审核
    @RequestMapping(value = "approve.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveChange() {
        String recordId = getRequestString("rid");
        MizarEntityChangeRecord record = mizarChangeRecordLoaderClient.load(recordId);
        if (record == null) {
            return MapMessage.errorMessage("无效的申请记录！");
        }
        if (!MizarAuditStatus.PENDING.name().equals(record.getAuditStatus())) {
            return MapMessage.errorMessage("该记录已经被处理！");
        }
        MizarAuditContext context = new MizarAuditContext(getCurrentUser(), record);
        MizarAuditProcessor processor = mizarAuditProcessorFactory.getProcessor(context);
        if (processor == null) {
            return MapMessage.errorMessage("无效的变更类型！");
        }
        return processor.approve();
    }

    @RequestMapping(value = "reject.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rejectChange() {
        String recordId = getRequestString("rid");
        String note = getRequestString("note");
        if (StringUtils.isBlank(note)) {
            return MapMessage.errorMessage("请填写驳回原因！");
        }
        MizarEntityChangeRecord record = mizarChangeRecordLoaderClient.load(recordId);
        if (record == null) {
            return MapMessage.errorMessage("无效的申请记录！");
        }
        if (!MizarAuditStatus.PENDING.name().equals(record.getAuditStatus())) {
            return MapMessage.errorMessage("该记录已经被处理！");
        }
        MizarAuditContext context = new MizarAuditContext(getCurrentUser(), record);
        context.setProcessNotes(note);
        MizarAuditProcessor processor = mizarAuditProcessorFactory.getProcessor(context);
        if (processor == null) {
            return MapMessage.errorMessage("无效的变更类型！");
        }
        return processor.reject();
    }
}
