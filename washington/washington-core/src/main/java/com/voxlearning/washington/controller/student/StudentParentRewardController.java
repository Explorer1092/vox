/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;

/**
 * 家长奖励学生端
 *
 * @author RuiBao
 * @version 0.1
 * @since 1/9/2015
 */
@Controller
@RequestMapping("/student/parentreward")
public class StudentParentRewardController extends AbstractController {

    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;

    // 家长奖励 -- 学生端首页 -- 显示进行中的任务
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/";
//        Long studentId = currentUserId();
//        String type = getRequestString("type");
//        if (StringUtils.equals(type, "card")) { // 如果是点击卡片进来的，记录缓存
//            asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
//                    .StudentParentRewardCacheManager_turnOff(studentId)
//                    .awaitUninterruptibly();
//        }
//        boolean hasParentBindWechat = CollectionUtils.isNotEmpty(studentLoaderClient.loadStudentParentRefs(studentId));
//        if (hasParentBindWechat) {
//            Pageable pageable = new PageRequest(0, 10);
//            Page<Mission> missionPage = missionLoaderClient.loadStudentMissions(studentId)
//                    .filter(t -> t.getState() == MissionState.ONGOING)
//                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
//                    .toPage(pageable);
//            List<MissionMapper> mapperList = missionPage.getContent()
//                    .stream()
//                    .map(t -> missionLoaderClient.transformMission(t, studentId, UserType.STUDENT))
//                    .collect(Collectors.toList());
//            Page<MissionMapper> missions = new PageImpl<>(mapperList, pageable, missionPage.getTotalElements());
//            model.addAttribute("missions", missions);
//        }
//        model.addAttribute("hasParentBindWechat", hasParentBindWechat);
//        model.addAttribute("canMakeWish", !asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
//                .StudentWishCreationCacheManager_wishMadeThisWeek(studentId)
//                .getUninterruptibly());
//        return "/studentv3/parentreward/index";
    }

    // 家长奖励 -- 显示进行中的和已经完成的任务
    @RequestMapping(value = "footprint.vpage", method = RequestMethod.GET)
    public String footprint(Model model) {
        return "redirect:/";
//        Long studentId = currentUserId();
//        /*Map<Long, Set<Long>> bindWeChatMap = wechatServiceClient.studentBindWechatParentMap(Collections.singletonList(studentId));
//        boolean hasParentBindWechat = CollectionUtils.isNotEmpty(bindWeChatMap.get(studentId));*/
//        boolean hasParentBindWechat = CollectionUtils.isNotEmpty(studentLoaderClient.loadStudentParentRefs(studentId));
//        if (hasParentBindWechat) {
//            Pageable pageable = new PageRequest(0, 10);
//            Page<Mission> missionPage = missionLoaderClient.loadStudentMissions(studentId)
//                    .filter(t -> t.getState() != MissionState.WISH)
//                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
//                    .toPage(pageable);
//            List<MissionMapper> mapperList = missionPage.getContent()
//                    .stream()
//                    .map(t -> missionLoaderClient.transformMission(t, studentId, UserType.STUDENT))
//                    .collect(Collectors.toList());
//            Page<MissionMapper> missions = new PageImpl<>(mapperList, pageable, missionPage.getTotalElements());
//            model.addAttribute("missions", missions);
//        }
//        model.addAttribute("hasParentBindWechat", hasParentBindWechat);
//        return "/studentv3/parentreward/footprint";
    }

    // 家长奖励 -- 分页显示任务
    @RequestMapping(value = "more.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage page() {
        return MapMessage.errorMessage("家长奖励已经下线");
//        Long studentId = currentUserId();
//        int page = ConversionUtils.toInt(getRequest().getParameter("currentPage"), 1);
//        String type = getRequestString("type");
//        Map<Long, Set<Long>> bindWeChatMap = wechatServiceClient.studentBindWechatParentMap(Collections.singletonList(studentId));
//        if (CollectionUtils.isEmpty(bindWeChatMap.get(studentId))) return MapMessage.errorMessage("请绑定家长微信");
//
//        Pageable pageable = new PageRequest(page - 1, 10);
//        Page<Mission> missionPage;
//        if (StringUtils.equals("ALL", type)) {
//            missionPage = missionLoaderClient.loadStudentMissions(studentId)
//                    .filter(t -> t.getState() != MissionState.WISH)
//                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
//                    .toPage(pageable);
//        } else {
//            missionPage = missionLoaderClient.loadStudentMissions(studentId)
//                    .filter(t -> t.getState() == MissionState.ONGOING)
//                    .sorted((o1, o2) -> Long.compare(o2.getMissionTime(), o1.getMissionTime()))
//                    .toPage(pageable);
//        }
//        List<MissionMapper> mapperList = missionPage.getContent()
//                .stream()
//                .map(t -> missionLoaderClient.transformMission(t, studentId, UserType.STUDENT))
//                .collect(Collectors.toList());
//        Page<MissionMapper> missions = new PageImpl<>(mapperList, pageable, missionPage.getTotalElements());
//        return MapMessage.successMessage().add("missions", missions);
    }

    // 家长奖励 -- 学生填写心愿(学生许愿都是自定义类型的)
    @RequestMapping(value = "makewish.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage makeWish() {
        return MapMessage.errorMessage("家长奖励已经下线");
//        Long studentId = currentUserId();
//        String wish = StringUtils.filterEmojiForMysql(getRequestString("wish"));
//        if (StringUtils.isBlank(wish))
//            return MapMessage.errorMessage("请填写你的愿望");
//
//        if (StringUtils.length(wish) > 200)
//            return MapMessage.errorMessage("请输入200以内的字符");
//
//        try {
//            return atomicLockManager.wrapAtomic(businessStudentServiceClient)
//                    .keyPrefix("MAKE_WISH_OR_MISSION").keys(studentId).proxy()
//                    .studentMakeWish(studentId, WishType.CUSTOMIZE, wish);
//        } catch (CannotAcquireLockException ex) {
//            return MapMessage.successMessage("正在处理，请不要重复提交");
//        } catch (Exception ex) {
//            logger.error("STUDENT {} MAKE WISH FAILED", studentId, ex);
//            return MapMessage.errorMessage("提交失败");
//        }
    }

    // 家长鼓励 -- 学生提醒家长干啥啥
    @RequestMapping(value = "remindsendnotice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentSendWechatNotice() {
        return MapMessage.errorMessage("家长奖励已经下线");
//        Long studentId = currentUserId();
//        Long missionId = getRequestLong("missionId");
//        WechatNoticeType template = WechatNoticeType.of(getRequestString("template"));
//        if (template != TEMPLATE_REMIND_UPDATE_PROGRESS && template != TEMPLATE_REMIND_REWARD)
//            return MapMessage.errorMessage("发送失败");
//        if (asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
//                .StudentMissionNoticeCacheManager_sendToday(studentId, missionId, template.name())
//                .getUninterruptibly())
//            return MapMessage.errorMessage("一天一次");
//
//        try {
//            // 发送家长消息
//            atomicLockManager.wrapAtomic(businessStudentServiceClient)
//                    .proxy()
//                    .postParentMessage(studentId, missionId, template.name());
//        } catch (Exception ex) {
//            if (ex instanceof DuplicatedOperationException) {
//                return MapMessage.successMessage("正在处理，请不要重复提交");
//            }
//            logger.error(ex.getMessage(), ex);
//            return MapMessage.errorMessage("发送失败");
//        }
//
//        return businessStudentServiceClient.studentSendWechatNotice(currentUserId(), missionId, template.name());
    }

    // 家长鼓励 -- 上传任务照片
    @RequestMapping(value = "missionpicture.vpage", method = RequestMethod.POST)
    public String missionPicture(MultipartFile filedata, Model model) {
        return "redirect:/";
//        User student = currentUser();
//        if (!User.isStudentUser(student)) {
//            model.addAttribute("error", "请登录");
//            return "/studentv3/parentreward/updataphotoline";
//        }
//
//        if (filedata.isEmpty()) {
//            model.addAttribute("error", "请选择照片");
//            return "/studentv3/parentreward/updataphotoline";
//        }
//
//        Long missionId = getRequestLong("missionId", -1);
//        Mission mission = missionLoaderClient.loadMission(missionId);
//        if (mission == null || !Objects.equals(student.getId(), mission.getStudentId())) {
//            model.addAttribute("error", "上传失败");
//            return "/studentv3/parentreward/updataphotoline";
//        }
//
//        String filename_ori = mission.getImg();
//        String filename;
//        try {
//            filename = missionPictureUploader.upload(missionId, filedata);
//        } catch (Exception ex) {
//            logger.warn("Upload mission picture failed. studentId {}, missionId {}", student.getId(), missionId, ex);
//            model.addAttribute("error", "上传失败");
//            return "/studentv3/parentreward/updataphotoline";
//        }
//        if (filename == null) {
//            model.addAttribute("error", "上传失败");
//            return "/studentv3/parentreward/updataphotoline";
//        }
//
//        if (businessStudentServiceClient.updateMissionImg(missionId, filename)) {
//            missionPictureUploader.delete(filename_ori);
//            model.addAttribute("missionId", missionId);
//            model.addAttribute("filename", filename);
//            return "/studentv3/parentreward/updataphotoline";
//        } else {
//            model.addAttribute("error", "上传失败");
//            return "/studentv3/parentreward/updataphotoline";
//        }
    }

    // 家长鼓励 -- 查看完成详情 -- 暂未开放
    @RequestMapping(value = "checkdetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studentCheckDetail() {
        return MapMessage.errorMessage("家长奖励已经下线");
//        Long missionId = getRequestLong("missionId");
//        return businessStudentServiceClient.studentCheckDetail(currentUserId(), missionId);
    }
}
