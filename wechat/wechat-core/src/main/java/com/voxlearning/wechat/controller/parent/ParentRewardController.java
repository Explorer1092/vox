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

package com.voxlearning.wechat.controller.parent;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.MissionState;
import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.mission.Mission;
import com.voxlearning.utopia.mapper.MissionMapper;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeSnapshot;
import com.voxlearning.wechat.context.WxConfig;
import com.voxlearning.wechat.controller.AbstractParentWebController;
import com.voxlearning.wechat.support.PageBlockContentGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2015/11/5.
 */
@Controller
@RequestMapping(value = "/parent/reward")
public class ParentRewardController extends AbstractParentWebController {

    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Long parentId = getRequestContext().getUserId();
        // 验证是否全部选择了家长角色
        String url = callNameAvailable(parentId);
        if(StringUtils.isNotBlank(url)){
            return "redirect:" + url;
        }
        try {
            //获取学生列表
            List<User> students = studentLoaderClient.loadParentStudents(getRequestContext().getUserId());
            if (students.size() == 0) {
                //跳去绑学生页面
                return "redirect:/parent/ucenter/bindchild.vpage";
            }
            List<Map<String, Object>> stdInfos = mapChildInfos(students);
            model.addAttribute("students", stdInfos);

            //获取孩子id，并返回当前选中学生ID，如果孩子id不在孩子列表中则返回第一个孩子及其位置
            Long studentId = getRequestLong("sid");

            Map<Long, User> studentMap = students.stream().collect(Collectors.toMap(User::getId, Function.identity(), (u, v) -> {
                throw new IllegalStateException("Duplicate key " + u);
            }, LinkedHashMap::new));
            if (0 == studentId || !studentMap.keySet().contains(studentId)) {
                model.addAttribute("currentStd", students.get(0));
            } else {
                model.addAttribute("currentStd", studentMap.get(studentId));
            }
            // 获取JS API 需要的参数MAP
            WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(WechatType.PARENT));
            Map<String, Object> jsApiMap = new TreeMap<>();
            jsApiMap.put("signature", wxConfig.sha1Sign());
            jsApiMap.put("appId", ProductConfig.get(WechatType.PARENT.getAppId()));
            jsApiMap.put("noncestr", wxConfig.getNonce());
            jsApiMap.put("timestamp", wxConfig.getTimestamp());

            model.addAttribute("ret", jsApiMap);

            log();
        } catch (CannotAcquireLockException e) {
            //ticket过期后,加锁获取,加锁失败抛此异常
            return infoPage("调取微信接口失败,请返回重试", null, model);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return redirectWithMsg("系统异常", model);
        }
        return "/parent/reward/index";
    }

    private void log() {
        String _from = getRequestString("_from");
        if (StringUtils.isNotBlank(_from)) {
            // 添加日志
            Map<String, String> log = new HashMap<>();
            log.put("module", "parentReward");
            log.put("op", "ucenter_parentreward_pv_from_" + _from);
            log.put("s0", getRequestContext().getAuthenticatedOpenId());
            super.log(log);
        }
    }

    @RequestMapping(value = "/getmissions.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getMissions() {
        Long studentId = getRequestLong("studentId");
        Integer page = getRequestInt("page");

        if (0 == studentId) {
            return MapMessage.errorMessage();
        }

        try {
            if (page <= 0) page = 1;

            Pageable pageable = new PageRequest(page - 1, 10);
            Page<Mission> missionPage = missionLoaderClient.loadStudentMissions(studentId)
                    .filter(t -> t.getState() == MissionState.ONGOING)
                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
                    .toPage(pageable);
            List<MissionMapper> mapperList = missionPage.getContent()
                    .stream()
                    .map(t -> missionLoaderClient.transformMission(t, studentId, UserType.PARENT))
                    .collect(Collectors.toList());
            Page<MissionMapper> missions = new PageImpl<>(mapperList, pageable, missionPage.getTotalElements());

            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            return MapMessage.successMessage()
                    .add("missions", missions)
                    .add("isGraduate", clazz !=null && clazz.isTerminalClazz());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "getfootprint.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentGetFootPrint() {
        Long studentId = getRequestLong("studentId");
        Integer page = getRequestInt("page");
        if (0 == studentId) {
            return MapMessage.errorMessage();
        }

        try {
            if (page <= 0) page = 1;

            Pageable pageable = new PageRequest(page - 1, 10);
            Page<Mission> missionPage = missionLoaderClient.loadStudentMissions(studentId)
                    .filter(t -> t.getState() != MissionState.WISH)
                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
                    .toPage(pageable);
            List<MissionMapper> mapperList = missionPage.getContent()
                    .stream()
                    .map(t -> missionLoaderClient.transformMission(t, studentId, UserType.PARENT))
                    .collect(Collectors.toList());
            Page<MissionMapper> missions = new PageImpl<>(mapperList, pageable, missionPage.getTotalElements());
            return MapMessage.successMessage().add("missions", missions);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "/setmissions.vpage", method = RequestMethod.GET)
    public String setMissions(Model model) {
        Long studentId = getRequestLong("sid");
        PageBlockContentGenerator generator = getPageBlockContentGenerator();
        String content = generator.getPageBlockContentHtml("WechatParentReward", "promotionBanner");
        boolean arranged = businessStudentServiceClient.isCurrentMonthIntegralMissionArranged(studentId);
        model.addAttribute("promotionBanner", content);
        model.addAttribute("arranged", arranged);
        model.addAttribute("studentId", studentId);

        return "/parent/reward/setmission";
    }

    @RequestMapping(value = "/setmissions.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setMissions() {
        Long studentId = getRequestLong("studentId");
        String missionTask = getRequestString("mission_task");
        String missionReward = getRequestString("mission_reward");
        Integer missionCount = getRequestInt("mission_count");
        Long missionId = getRequestLong("missionId");
        WishType wishType = WishType.of(getRequestString("wish_type"));
        MissionType missionType = MissionType.of(getRequestString("mission_type"));
        Long parentId = getRequestContext().getUserId();

        if (studentId == 0 || wishType == null || missionType == null
                || StringUtils.isBlank(missionTask) || missionCount < 1 || missionCount > 20) {
            return MapMessage.errorMessage("设置任务失败");
        }

        try {
            MapMessage mesg = atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .keyPrefix("MAKE_WISH_OR_MISSION").keys(studentId).proxy()
                    .parentSetMission(parentId, studentId, wishType, missionReward, missionCount, missionTask, missionType, missionId);
            if (!mesg.isSuccess()) {
                return MapMessage.errorMessage("设置任务失败");
            } else {
                return MapMessage.successMessage("设置任务成功");
            }
        } catch (CannotAcquireLockException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("Parent reward set missions faild, parentId is {}, studentId is {},  ex is:{}", parentId, studentId, ex.getMessage());
            return MapMessage.errorMessage("设置任务失败");
        }
    }

    @RequestMapping(value = "/updateprogress.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateProgress() {
        Long missionId = getRequestLong("missionId");
        if (missionId == 0) {
            return MapMessage.errorMessage("更新进度失败");
        }
        try {
            MapMessage mesg = atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .proxy().parentUpdateProgress(getRequestContext().getUserId(), missionId);
            if (!mesg.isSuccess()) {
                return MapMessage.errorMessage(mesg.getInfo());
            } else {
                return MapMessage.successMessage("更新进度成功");
            }
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("Parent {} update mission progress failed.", getRequestContext().getUserId(), ex);
            return MapMessage.errorMessage("更新进度失败");
        }
    }

    @RequestMapping(value = "/doreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doReward() {
        Long missionId = getRequestLong("missionId");
        if (missionId == 0) {
            return MapMessage.errorMessage("发放奖励失败");
        }
        try {
            MapMessage mesg = atomicLockManager.wrapAtomic(businessStudentServiceClient)
                    .proxy().parentUpdateComplete(getRequestContext().getUserId(), missionId);
            if (!mesg.isSuccess()) {
                return MapMessage.errorMessage(mesg.getInfo());
            }
            return MapMessage.successMessage("发放奖励成功");
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("Parent {} update mission complete failed.", getRequestContext().getUserId(), ex);
            return MapMessage.errorMessage("发放奖励失败");
        }
    }

    @RequestMapping(value = "/uploadimg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImg() {
        Long missionId = getRequestLong("missionId");
        String mediaId = getRequestString("mediaId");
        Mission mission = missionLoaderClient.loadMission(missionId);
        if (mission == null) {
            return MapMessage.errorMessage("任务不存在");
        }
        try {
            String filename_ori = mission.getImg();
            String accessToken = tokenHelper.getAccessToken(WechatType.PARENT);
            if (StringUtils.isBlank(accessToken)) {
                return MapMessage.errorMessage("上传失败，请重试");
            }

            byte[] imageArray = wechatPictureUploader.downLoadMediaFromWechat(accessToken, mediaId);
            String filename = wechatPictureUploader.uploadMediaToGFS(missionId, imageArray);
            if (filename == null) {
                logger.warn("Upload mission picture failed. parentId {}, missionId {}, mediaId {}", getRequestContext().getUserId(), missionId, mediaId);
                return MapMessage.errorMessage("上传失败");
            }
            if (businessStudentServiceClient.updateMissionImg(missionId, filename)) {
                wechatPictureUploader.delete(filename_ori);
                return MapMessage.successMessage("上传成功").add("filename", filename);
            } else {
                logger.warn("Update mission picture failed. parentId {}, missionId {}, mediaId {}", getRequestContext().getUserId(), missionId, mediaId);
                return MapMessage.errorMessage("上传失败");
            }
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("Parent {} Update mission picture failed.", getRequestContext().getUserId(), ex);
            return MapMessage.errorMessage("上传失败");
        }
    }

    // 孩子心愿
    @RequestMapping(value = "/notices.vpage", method = RequestMethod.GET)
    public String notices(Model model) {
        Long parentId = getRequestContext().getUserId();
        String openId = getRequestContext().getAuthenticatedOpenId();
        List<WechatNoticeSnapshot> noticeSnaps = new ArrayList<>();
        List<WechatNoticeSnapshot> latestSnaps = wechatLoaderClient.loadWechatNoticeSnapshotByUserId(parentId, false);
        if (latestSnaps != null && !latestSnaps.isEmpty()) {
            noticeSnaps.addAll(latestSnaps);
        }
        List<WechatNoticeSnapshot> historySnaps = wechatLoaderClient.loadWechatNoticeSnapshotByUserId(parentId, true);
        if (historySnaps != null && !historySnaps.isEmpty()) {
            noticeSnaps.addAll(historySnaps);
        }
        if (!noticeSnaps.isEmpty()) {
            noticeSnaps = noticeSnaps.stream().filter(s -> Arrays.asList(39, 41, 42).contains(s.getMessageType())
                    && Objects.equals(openId, s.getOpenId())).collect(Collectors.toList());
            List<Map<String, Object>> newNotices = new ArrayList<>();
            for (WechatNoticeSnapshot snapshot : noticeSnaps) {
                Map<String, Object> newNotice = new HashMap<>();
                Long noticeId = snapshot.getId();
                String key = CacheKeyGenerator.generateCacheKey("WECHAT_PARENT_REWARD_NOTICE_CLICKED", null, new Object[]{noticeId});
                String cacheValue = wechatWebCacheSystem.CBS.persistence.load(key);
                boolean clicked = false;
                if (StringUtils.isNotBlank(cacheValue)) {
                    clicked = true;
                }
                newNotice.put("createTime", DateUtils.dateToString(snapshot.getCreateTime()));
                newNotice.put("clicked", clicked);
                newNotice.put("tip", "查看详情");
                newNotice.put("nid", noticeId);
                newNotice.putAll(genMsgFromTemplateMsg(snapshot.getMessageType(), JsonUtils.fromJson(snapshot.getMessage())));
                newNotices.add(newNotice);
            }
            model.addAttribute("msgs", newNotices);
        }
        return "/parent/reward/notices";
    }

    private Map<String, Object> genMsgFromTemplateMsg(Integer messageType, Map<String, Object> messageMap) {
        String wish = messageMap.get("wish") == null ? "" : messageMap.get("wish").toString();
        String mission = messageMap.get("mission") == null ? "" : messageMap.get("mission").toString();
        String studentId = messageMap.get("studentId").toString();
        String studentName = messageMap.get("studentName") == null ? "您的孩子（" + studentId + "）" : messageMap.get("studentName").toString();
        String content = "";
        String url = "";

        switch (messageType) {
            case 39:
                content = studentName + "许了一个愿望：" + wish;
                if (messageMap.get("missionId") != null) {
                    url = "/parent/reward/setmissions.vpage?sid=" + studentId + "&mid=" + messageMap.get("missionId").toString() + "&wish=" + wish;
                } else {
                    url = "/parent/reward/setmissions.vpage?sid=" + studentId;
                }
                break;
            case 40:
                content = "请为" + studentName + "的愿望（" + wish + ")设置任务目标";
                if (messageMap.get("missionId") != null) {
                    url = "/parent/reward/setmissions.vpage?sid=" + studentId + "&mid=" + messageMap.get("missionId").toString() + "&wish=" + wish;
                } else {
                    url = "/parent/reward/setmissions.vpage?sid=" + studentId;
                }
                break;
            case 41:
                content = studentName + "申请更新一次目标完成进度，目标：" + mission;
                url = "/parent/reward/index.vpage?sid=" + studentId;
                break;
            case 42:
                content = studentName + "已" + mission + "，请发放奖励：" + wish;
                url = "/parent/reward/index.vpage?sid=" + studentId;
                break;
            default:
        }

        return MiscUtils.map("content", content, "url", url, "messageType", messageType);
    }


    @RequestMapping(value = "/cacheclicked.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cacheClicked() {
        Long noticeId = getRequestLong("nid");
        String key = CacheKeyGenerator.generateCacheKey("WECHAT_PARENT_REWARD_NOTICE_CLICKED", null, new Object[]{noticeId});
        wechatWebCacheSystem.CBS.persistence.set(key, 3600 * 24 * 7, "clicked");
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/receive.vpage", method = RequestMethod.GET)
    public String receive(Model model) {
        return "parent/reward/receive";
    }


}
