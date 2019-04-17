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

package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignAward;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.VendorService;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.impl.dao.*;
import com.voxlearning.utopia.service.vendor.impl.listener.InternalVendorQueueSender;
import com.voxlearning.utopia.service.vendor.impl.loader.VendorLoaderImpl;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorAppsPersistence;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorAppsResgRefPersistence;
import com.voxlearning.utopia.service.vendor.impl.push.PushProducer;
import com.voxlearning.utopia.service.vendor.impl.queue.MyselfStudyQueueProducer;
import com.voxlearning.utopia.service.vendor.impl.queue.VendorQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.TravelAmerica;

/**
 * Default {@link VendorService} implementation.
 *
 * @author Xiaohai Zhang
 * @since Feb 10, 2015
 */
@Named
@Service(interfaceClass = VendorService.class)
@ExposeService(interfaceClass = VendorService.class)
public class VendorServiceImpl extends SpringContainerSupport implements VendorService {

    @Inject
    private CampaignServiceClient campaignServiceClient;

    @Inject
    private AppParentSignRecordDao appParentSignRecordDao;
    @Inject
    private InternalVendorQueueSender internalVendorQueueSender;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private VendorAppRewardHistoryPersistence vendorAppRewardHistoryPersistence;
    @Inject
    private VendorAppsOrderPersistence vendorAppsOrderPersistence;
    @Inject
    private VendorAppsPersistence vendorAppsPersistence;
    @Inject
    private VendorAppsResgRefPersistence vendorAppsResgRefPersistence;
    @Inject
    private VendorAppsUserRefDao vendorAppsUserRefDao;
    @Inject
    private VendorLoaderImpl vendorLoader;
    @Inject
    private VendorNotifyPersistence vendorNotifyPersistence;
    @Inject
    private VendorPersistence vendorPersistence;
    @Inject
    private VendorResgContentPersistence vendorResgContentPersistence;
    @Inject
    private VendorResgPersistence vendorResgPersistence;

    @Inject
    private PushProducer pushProducer;
    @Inject
    private MyselfStudyQueueProducer myselfStudyQueueProducer;
    @Inject
    private VendorQueueProducer vendorQueueProducer;

    @Inject private VendorAppsServiceImpl vendorAppsService;
    @Inject private VendorResgContentServiceImpl vendorResgContentService;

    @Override
    public MapMessage deleteVendor(Long id) {
        if (id == null) {
            return MapMessage.errorMessage();
        }
        int rows = vendorPersistence.disable(id);
        return new MapMessage().setSuccess(rows > 0);
    }

    @Override
    public MapMessage saveAppVendor(Long vendorId, String cname, String ename, String sname, String address, String webSite, String logoUrl, String contact1Name, String contact1Tel, String contact1Mob, String contact1Email, String contact2Name, String contact2Tel, String contact2Mob, String contact2Email) {
        Vendor vendor;
        if (vendorId > 0) {
            vendor = vendorLoader.loadVendorsIncludeDisabled().get(vendorId);
            if (vendor != null && vendor.isDisabledTrue()) {
                vendor = null;
            }
            if (vendor == null) {
                return MapMessage.errorMessage();
            }
        } else {
            vendor = new Vendor();
            vendor.setDisabled(false);
        }
        vendor.setCname(cname);
        vendor.setEname(ename);
        vendor.setShortName(sname);
        vendor.setAddress(address);
        vendor.setWebSite(webSite);
        vendor.setLogoUrl(logoUrl);
        vendor.setContact1Name(contact1Name);
        vendor.setContact1Tel(contact1Tel);
        vendor.setContact1Mob(contact1Mob);
        vendor.setContact1Email(contact1Email);
        vendor.setContact2Name(contact2Name);
        vendor.setContact2Tel(contact2Tel);
        vendor.setContact2Mob(contact2Mob);
        vendor.setContact2Email(contact2Email);

        Long id;
        if (vendor.getId() != null) {
            vendorPersistence.replace(vendor);
            id = vendor.getId();
        } else {
            vendorPersistence.insert(vendor);
            id = vendor.getId();
        }
        return MapMessage.successMessage().add("id", id);
    }

    @Override
    public MapMessage persistVendorApp(VendorApps vendorApp) {
        if (vendorApp == null) {
            return MapMessage.errorMessage();
        }
        if (vendorApp.getId() == null) {
            vendorAppsPersistence.insert(vendorApp);
            return MapMessage.successMessage().add("id", vendorApp.getId());
        } else {
            boolean ret = vendorAppsPersistence.replace(vendorApp) != null;
            return new MapMessage().setSuccess(ret).add("id", vendorApp.getId());
        }
    }

    @Override
    public MapMessage deleteVendorApp(Long id) {
        if (id == null) {
            return MapMessage.errorMessage();
        }
        int rows = vendorAppsPersistence.disable(id);
        return new MapMessage().setSuccess(rows > 0);
    }

    @Override
    public MapMessage persistVenderAppUserRef(VendorAppsUserRef ref) {
        if (ref == null) {
            return MapMessage.errorMessage();
        }
        try {
            vendorAppsUserRefDao.insert(ref);
            return MapMessage.successMessage().add("ref", ref);
        } catch (Exception ex) {
            logger.error("inser vendorAppsUserRef error, ref is {}, ex is {}", JsonUtils.toJson(ref), ex.getMessage());
            return MapMessage.errorMessage();
//            // 偶尔出现DBDuplicateKeyException，有的话就重新查询一遍
//            String a = ref.getAppKey();
//            String s = ref.getSessionKey();
//            Long u = ref.getUserId();
//
//            vendorAppsUserRefDao.getCache().delete(VendorAppsUserRef.ck_user(u));
//            List<VendorAppsUserRef> refs = vendorAppsUserRefPersistence.findVendorAppsUserRefList(u);
//            ref = refs.stream().filter(p -> Objects.equals(p.getSessionKey(), s) && Objects.equals(p.getAppKey(), a))
//                    .findFirst()
//                    .orElse(null);
//
//            if (ref == null) {
//                return MapMessage.errorMessage();
//            } else {
//                return MapMessage.successMessage().add("ref", ref);
//            }
        }
    }

    @Override
    public MapMessage expireSessionKey(String appKey, Long userId, String sessionKey) {
        if (StringUtils.isBlank(appKey) || userId == null || StringUtils.isBlank(sessionKey)) {
            return MapMessage.errorMessage();
        }
        if (!VendorAppsUserRef.isLegalSessionKey(sessionKey)) {
            return MapMessage.errorMessage();
        }
        long rows = vendorAppsUserRefDao.updateSessionKey(appKey, userId, sessionKey);
        if (rows > 0) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    @Override
    public MapMessage saveVendorAppResg(Long appId, String resgList) {
        if (appId <= 0) {
            return MapMessage.errorMessage();
        }
        vendorAppsResgRefPersistence.deleteByAppId(appId);
        VendorApps app = vendorAppsService.getVendorAppsBuffer().loadById(appId);
        if (app == null) {
            return MapMessage.errorMessage();
        }
        String[] resgIds = StringUtils.split(resgList, ",");
        for (String resgId : resgIds) {
            if (StringUtils.isEmpty(resgId)) {
                continue;
            }
            VendorAppsResgRef appsResgRef = new VendorAppsResgRef();
            appsResgRef.setAppId(appId);
            appsResgRef.setAppKey(app.getAppKey());
            appsResgRef.setResgId(Long.valueOf(resgId));
            vendorAppsResgRefPersistence.insert(appsResgRef);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage saveVendorResg(Long resgId, String cname, String ename, String description) {
        VendorResg resg;
        if (resgId != null && resgId > 0) {
            resg = vendorLoader.loadVendorResgsIncludeDisabled().get(resgId);
            if (resg == null) {
                return MapMessage.errorMessage();
            }
        } else {
            resg = new VendorResg();
        }
        resg.setCname(cname);
        resg.setEname(ename);
        resg.setDescription(description);
        resg.setDisabled(Boolean.TRUE);

        if (resg.getId() != null) {
            resg.setId(resgId);
            vendorResgPersistence.replace(resg);
        } else {
            vendorResgPersistence.insert(resg);
            resgId = resg.getId();
        }
        return MapMessage.successMessage().add("id", resgId);
    }

    @Override
    public MapMessage deleteVendorResgs(Long resgId) {
        if (resgId == null) {
            return MapMessage.successMessage();
        }
        int rows = 0;
        rows += vendorResgContentPersistence.deleteByResgId(resgId);
        rows += vendorAppsResgRefPersistence.deleteByResgId(resgId);
        rows += vendorResgPersistence.delete(resgId);
        return new MapMessage().setSuccess(rows > 0);
    }

    @Override
    public MapMessage deleteVendorResgContent(Long id) {
        if (id == null) {
            return MapMessage.errorMessage();
        }
        return new MapMessage().setSuccess(vendorResgContentPersistence.remove(id));
    }

    @Override
    public MapMessage saveVendorResgContent(Long resgContentId, Long resgId, String resName) {
        VendorResgContent resgContent;
        if (resgContentId > 0) {
            final Long cid = resgContentId;
            resgContent = vendorResgContentService.loadAllVendorResgContentsFromDB()
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> Objects.equals(cid, e.getId()))
                    .findFirst()
                    .orElse(null);
        } else {
            resgContent = new VendorResgContent();
        }

        resgContent.setResgId(resgId);
        resgContent.setResName(resName);

        if (resgContentId > 0) {
            resgContent.setId(resgContentId);
            vendorResgContentPersistence.replace(resgContent);
        } else {
            vendorResgContentPersistence.insert(resgContent);
            resgContentId = resgContent.getId();
        }

        return MapMessage.successMessage().add("id", resgContentId);
    }

    @Override
    public MapMessage persistVendorAppOrder(VendorAppsOrder order) {
        if (order == null) {
            return MapMessage.errorMessage();
        }
        vendorAppsOrderPersistence.insert(order);
        return MapMessage.successMessage().add("id", order.getId());
    }

    @Override
    public MapMessage updateVendorAppOrder(VendorAppsOrder order) {
        if (order == null || order.getId() == null) {
            return MapMessage.errorMessage();
        }
        VendorAppsOrder modified = vendorAppsOrderPersistence.replace(order);
        return new MapMessage().setSuccess(modified != null);
    }

    @Override
    public MapMessage finishPaymnet(String orderId) {
        if (orderId == null) {
            return MapMessage.errorMessage();
        }
        int rows = vendorAppsOrderPersistence.finishPaymnet(orderId);
        return new MapMessage().setSuccess(rows > 0);
    }

    @Override
    public MapMessage persistVendorAppRewardHistory(VendorAppRewardHistory history) {
        if (history == null) {
            return MapMessage.errorMessage();
        }
        vendorAppRewardHistoryPersistence.insert(history);
        Long id = history.getId();
        return MapMessage.successMessage().add("id", id);
    }

    @Override
    public void sendCampaignAward(String appKey, Long userId, CampaignType campaignType, Integer diamondAmount) {
        // 目前只有走遍美国有奖励
        if (userId == null || StringUtils.isBlank(appKey) || !TravelAmerica.name().equals(appKey) || diamondAmount == null || diamondAmount < 1) {
            return;
        }

        // 获取用户SESSION KEY
        VendorAppsUserRef userRef = vendorLoader.loadVendorAppUserRef(appKey, userId);
        if (userRef == null) {
            return;
        }

        List<Map<String, Object>> awardList = new ArrayList<>();
        Map<String, Object> awardItem = new HashMap<>();
        awardItem.put("type", "diamond");
        awardItem.put("amount", diamondAmount);
        awardList.add(awardItem);

        // 保存给用户的奖励
        CampaignAward award = new CampaignAward();
        award.setUserId(userId);
        award.setCampaignId(campaignType.getId());
        award.setAward(JsonUtils.toJson(awardList));
        award.setAwardStatus(1);
        award = campaignServiceClient.getCampaignService().$insertCampaignAward(award);
        Long awardId = award.getId();
        award.setId(awardId);

        // 发送通知消息到走遍美国
        sendTravelAmericaGiftsNotify(appKey, userRef.getSessionKey(), award);
    }

    @Override
    public MapMessage sendHttpNotify(String appKey, String targetUrl, Map<String, Object> params) {
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(targetUrl) || params == null) {
            return MapMessage.errorMessage();
        }

        VendorNotify notify = new VendorNotify();
        notify.setAppKey(appKey);
        notify.setTargetUrl(targetUrl);
        notify.setNotify(JsonUtils.toJson(params));
        vendorNotifyPersistence.insert(notify);
        Long notifyId = notify.getId();

        internalVendorQueueSender.sendHttpNotify(notifyId, appKey, targetUrl, params);

        return MapMessage.successMessage().add("id", notifyId);
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    private void sendTravelAmericaGiftsNotify(String appKey, String sessionKey, CampaignAward award) {
        String sendUrl = "http://travelusa.dev.dreamz.cn/user/addGifts";
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            sendUrl = "http://travelusa.app.dreamz.cn/user/addGifts";
        }
        sendGiftsNotify(appKey, sessionKey, award, sendUrl);
    }

    private void sendGiftsNotify(String appKey, String sessionKey, CampaignAward award, String targetUrl) {
        if (award == null) {
            return;
        }

        CampaignType campaignType = CampaignType.of(award.getCampaignId());
        if (campaignType == null) {
            return;
        }

        VendorApps apps = vendorAppsService.getVendorAppsBuffer().loadByAk(appKey);
        if (apps == null) {
            return;
        }

        List awardList = JsonUtils.fromJsonToList(award.getAward(), Map.class);
        if (awardList == null || awardList.size() == 0) {
            return;
        }
        String awardContent = JsonUtils.toJson(awardList);

        // 计算Sig
        Map<String, String> sigParams = new HashMap<>();
        sigParams.put("campaign_id", String.valueOf(award.getCampaignId()));
        sigParams.put("session_key", sessionKey);
        sigParams.put("campaign_name", campaignType.getName());
        sigParams.put("award", awardContent);
        sigParams.put("award_id", String.valueOf(award.getId()));
        sigParams.put("award_start_time", campaignType.getAwardStartTime());
        sigParams.put("award_end_time", campaignType.getAwardEndTime());
        sigParams.put("app_key", appKey);

        String sig = DigestSignUtils.signMd5(sigParams, apps.getSecretKey());

        // 发送消息通知
        Map<String, Object> messageParams = new HashMap<>();
        messageParams.put("campaign_id", String.valueOf(award.getCampaignId()));
        messageParams.put("session_key", sessionKey);
        messageParams.put("campaign_name", campaignType.getName());
        messageParams.put("award", awardContent);
        messageParams.put("award_id", String.valueOf(award.getId()));
        messageParams.put("award_start_time", campaignType.getAwardStartTime());
        messageParams.put("award_end_time", campaignType.getAwardEndTime());
        messageParams.put("sig", sig);

        sendHttpNotify(appKey, targetUrl, messageParams);
    }

    @Override
    public Map<Long, Set<Long>> studentBindAppParentMap(List<Long> studentIds) {
        Map<Long, List<StudentParentRef>> studentParentMap = studentLoaderClient.loadStudentParentRefs(studentIds);
        List<Long> parentIds = new ArrayList<>();
        for (Long studentId : studentParentMap.keySet()) {
            for (StudentParentRef studentParentRef : studentParentMap.get(studentId)) {
                parentIds.add(studentParentRef.getParentId());
            }
        }
        //查询绑定app的家长
        Map<Long, VendorAppsUserRef> vendorAppsUserRefMap = vendorLoader.loadVendorAppUserRefs("17Parent", parentIds);
        Map<Long, Set<Long>> studentAppRefMap = new HashMap<>();
        for (Long studentId : studentParentMap.keySet()) {
            for (StudentParentRef studentParentRef : studentParentMap.get(studentId)) {
                Long parentId = studentParentRef.getParentId();
                VendorAppsUserRef appsUserRef = vendorAppsUserRefMap.get(parentId);
                if (appsUserRef != null) {
                    if (studentAppRefMap.get(studentId) != null) {
                        studentAppRefMap.get(studentId).add(parentId);
                    } else {
                        Set<Long> pids = new HashSet<>();
                        pids.add(parentId);
                        studentAppRefMap.put(studentId, pids);
                    }
                }
            }
        }
        return studentAppRefMap;
    }

    @Override
    public MapMessage updateOrInsertAppParentSignRecord(Long userId) {
        return appParentSignRecordDao.updateOrInsertAppParentSignRecord(userId);
    }

    @Override
    public void sendVendorMessage(Message message) {
        if (message != null) {
            vendorQueueProducer.getProducer().produce(message);
        }
    }

    @Override
    public void sendJpushMessage(Message message) {
        if (message != null) {
            pushProducer.getJpushProducer().produce(message);
        }
    }

    @Override
    public void sendMyselfstudyMessage(Message message) {
        if (message != null) {
            myselfStudyQueueProducer.getProducer().produce(message);
        }
    }
}
