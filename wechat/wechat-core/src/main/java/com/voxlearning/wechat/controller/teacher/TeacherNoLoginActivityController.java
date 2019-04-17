package com.voxlearning.wechat.controller.teacher;

import com.voxlearning.wechat.constants.WechatInfoCode;
import com.voxlearning.wechat.controller.AbstractTeacherWebController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Summer Yang on 2016/8/25.
 */
@Controller
@RequestMapping(value = "/activity")
public class TeacherNoLoginActivityController extends AbstractTeacherWebController {

    /* 2016教师节活动 分享页面 */
    @RequestMapping(value = "teachersdayshare.vpage", method = RequestMethod.GET)
    public String teachersDayShare(Model model) {
//        try {
//            Long teacherId = getRequestLong("teacherId");
//            Long clazzId = getRequestLong("clazzId");
//            if (teacherId == 0 || clazzId == 0L) {
//                model.addAttribute("shareList", new ArrayList<>());
//                return "teacher/activity/teachersdayshare";
//            }
//            List<Map<String, Object>> shareList = atomicLockManager.wrapAtomic(teachersDayBlessActivityService)
//                    .expirationInSeconds(30)
//                    .keyPrefix("TEACHERS_DAY_SHARE_LIST")
//                    .keys(teacherId)
//                    .proxy()
//                    .loadTeacherShareList(teacherId, clazzId);
//            model.addAttribute("shareList", shareList);
//            // 获取JS API 需要的参数MAP
//            WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(WechatType.TEACHER));
//            Map<String, Object> jsApiMap = new TreeMap<>();
//            jsApiMap.put("signature", wxConfig.sha1Sign());
//            jsApiMap.put("appId", ProductConfig.get(WechatType.TEACHER.getAppId()));
//            jsApiMap.put("noncestr", wxConfig.getNonce());
//            jsApiMap.put("timestamp", wxConfig.getTimestamp());
//            model.addAttribute("ret", jsApiMap);
//            return "teacher/activity/teachersdayshare";
//        } catch (CannotAcquireLockException ex) {
//            //ticket过期后,加锁获取,加锁失败抛此异常
//            return infoPage("调取微信接口失败,请返回重试", "/activity/teachersdayshare.vpage", model);
//        }
        return infoPage(WechatInfoCode.TEACHER_ACTIVITY_DOWN, model);
    }
}
