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

package com.voxlearning.utopia.admin.controller.toolkit;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.RecordType;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.cdn.CdnFlushFacade;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.news.client.JxtNewsServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.piclisten.api.PicListenBookDelDevService;
import com.voxlearning.utopia.service.piclisten.api.PicListenBookOrderService;
import com.voxlearning.utopia.service.piclisten.api.entity.PicListenBookUserDev;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.reminder.api.ReminderService;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientCategory;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.cache.UserCache;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsAlbum;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/toolkit")
@NoArgsConstructor
public class ToolKitController extends ToolKitAbstractController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = PicListenBookOrderService.class)
    private PicListenBookOrderService picListenBookOrderService;

    @Inject
    private ActionServiceClient actionServiceClient;
    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject
    private JxtNewsServiceClient jxtNewsServiceClient;

    @ImportService(interfaceClass = PicListenBookDelDevService.class)
    private PicListenBookDelDevService picListenBookDelDevService;
    private PinYinComparator pinYinComparator = new PinYinComparator();
    @ImportService(interfaceClass = ReminderService.class)
    private ReminderService reminderService;

    /**
     * 跳转到工具箱操作页面
     */
    @RequestMapping(value = "toolkit.vpage", method = RequestMethod.GET)
    String toolkit(Model model) {
        Map<String, String> conditionMap = new HashMap<>();
        try {
            conditionMap.put("provinces", getRequestParameter("provinces", "-1"));
            conditionMap.put("citys", getRequestParameter("citys", "-1"));
            conditionMap.put("countys", getRequestParameter("countys", "-1"));
        } catch (Exception ignored) {
        }

        model.addAttribute("provinces", getAllProvincePinYin());
        model.addAttribute("conditionMap", conditionMap);
        List<String> positions = new ArrayList<>();
        for (ReminderPosition position : ReminderPosition.values()) {
            positions.add(position.name());
        }
        model.addAttribute("positions", positions);
        return "toolkit/toolkit";
    }

    private class PinYinComparator implements Comparator<Region> {
        private Map<String, String> regionPinYin = new HashMap<>();

        @Override
        public int compare(Region s1, Region s2) {
            if (s1.getId() == -1) {
                return -1;
            }
            if (s2.getId() == -1) {
                return 1;
            }
            String str1 = this.getPinYin(s1.getName());
            String str2 = this.getPinYin(s2.getName());
            for (int i = 0; i < str1.length() && i < str2.length(); i++) {
                char c1 = str1.charAt(i);
                char c2 = str2.charAt(i);
                if (c1 > c2) {
                    return 1;
                }
                if (c1 < c2) {
                    return -1;
                }
            }
            return Integer.compare(str1.length(), str2.length());
        }

        private String getPinYin(String sin) {
            String rtn = regionPinYin.get(sin);
            if (null == rtn) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < sin.length(); i++) {
                    char c = sin.charAt(i);
                    if ('广' == c) {
                        sb.append("guang3");
                        continue;
                    }
                    String[] py = PinyinHelper.toHanyuPinyinStringArray(c);
                    if (null == py || py.length < 1) {
                        sb.append(c);
                        continue;
                    }
                    if (py.length > 1) {
                        Arrays.sort(py, (str1, str2) -> {
                            for (int i1 = 0; i1 < str1.length() && i1 < str2.length(); i1++) {
                                char c1 = str1.charAt(i1);
                                char c2 = str2.charAt(i1);
                                if (c1 > c2) {
                                    return 1;
                                }
                                if (c1 < c2) {
                                    return -1;
                                }
                            }
                            return Integer.compare(str1.length(), str2.length());
                        });
                    }
                    sb.append(py[0]);
                }
                rtn = sb.toString();
                regionPinYin.put(sin, rtn);
            }
            return rtn;
        }
    }

    private List<Map<String, Object>> getAllProvincePinYin() {
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
        Set<ExRegion> rt = new TreeSet<>(pinYinComparator);
        rt.addAll(regionList);
        List<Map<String, Object>> provinces = new ArrayList<>();
        for (ExRegion region : rt) {
            Map<String, Object> province = new HashMap<>();
            province.put("key", region.getCode());
            province.put("value", region.getName());
            provinces.add(province);
        }
        return provinces;
    }

    @RequestMapping(value = "user/cleanupBindedMobile.vpage", method = RequestMethod.GET)
    String getCleanupMobile() {
        return "toolkit/toolkit";
    }

    // 上传图片至阿里云
    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadPhoto() {
        Integer width = getRequestInt("width", 0);
        Integer height = getRequestInt("height", 0);

        String path = getRequestString("path");
        if (StringUtils.isEmpty(path)) return MapMessage.errorMessage("请填写上传路径");
        if (!(getRequest() instanceof MultipartHttpServletRequest)) return MapMessage.errorMessage("上传失败");

        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            MultipartFile inputFile = multipartRequest.getFile("file");

            MapMessage mapMessage = validateImg(inputFile, width, height);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }

            if (inputFile != null && !inputFile.isEmpty()) {
                String fileName = AdminOssManageUtils.upload(inputFile, path);
                return MapMessage.successMessage(fileName);
            }
        } catch (Exception ignored) {
        }
        return MapMessage.errorMessage("上传失败");
    }

    /**
     * 清除手机号
     */
    @RequestMapping(value = "user/cleanupBindedMobile.vpage", method = RequestMethod.POST)
    String cleanupBindedMobile(@RequestParam(value = "mobile", required = false) String mobile) {
        if (StringUtils.isBlank(mobile)) {
            getAlertMessageManager().addMessageError("手机号不能为空");
            return "toolkit/toolkit";
        }
        if (!MobileRule.isMobile(mobile)) {
            getAlertMessageManager().addMessageError("无效的手机号码");
            return "toolkit/toolkit";
        }
        String reason = getRequestString("reason");
        if (StringUtils.isBlank(reason)) {
            getAlertMessageManager().addMessageError("原因不能为空");
            return "toolkit/toolkit";
        }
        String[] roles = getRequest().getParameterValues("cleanup_role");
        if (roles == null || roles.length == 0) {
            getAlertMessageManager().addMessageError("请选择角色");
            return "toolkit/toolkit";
        }
        for (String role : roles) {
            UserType userType;
            RecordType recordType;
            switch (role) {
                case "teacher":
                    userType = UserType.TEACHER;
                    recordType = RecordType.老师操作;
                    break;
                case "student":
                    userType = UserType.STUDENT;
                    recordType = RecordType.学生操作;
                    break;
                case "parent":
                    userType = UserType.PARENT;
                    recordType = RecordType.家长操作;
                    break;
                default:
                    continue;
            }
            MapMessage message;
            try {
                // 检查手机号
                UserAuthentication ua = userLoaderClient.loadMobileAuthentication(mobile, userType);
                if (ua == null) {
                    getAlertMessageManager().addMessageError(userType.getDescription() + "手机号" + mobile + "错误或不存在，请核实后再操作");
                    message = MapMessage.errorMessage();
                } else {
                    message = userServiceClient.cleanupBindedMobile(getCurrentAdminUser().getAdminUserName(), mobile, userType);
                }
            } catch (Exception ex) {
                logger.error("Failed to cleanup binded mobile '{}/{}'", mobile, userType, ex);
                message = MapMessage.errorMessage();
            }
            if (message.isSuccess()) {
                Long targetId = (Long) message.get("userId");
                addAdminLog("cleanupBindedMobile", targetId, mobile, "清除角色" + role + "，原因：" + reason + "，结果：" + message.isSuccess(), null);
                if (targetId != null) {

                    // 记录 UserServiceRecord
                    UserServiceRecord userServiceRecord = new UserServiceRecord();
                    userServiceRecord.setUserId(targetId);
                    userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                    userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                    userServiceRecord.setOperationContent("管理员清除用户手机号");
                    userServiceRecord.setComments("手机:" + SensitiveLib.encodeMobile(mobile));
                    userServiceClient.saveUserServiceRecord(userServiceRecord);

                    User user = userLoaderClient.loadUser(targetId, userType);
                    if (user != null && !user.isDisabledTrue()) {
                        getAlertMessageManager().addMessageSuccess("已解除" + userType.getDescription() + "手机号" + mobile + "与" + user.getProfile().getRealname() + "(" + targetId + ")的绑定关系。");
                    } else {
                        getAlertMessageManager().addMessageError(userType.getDescription() + "手机号" + mobile + "错误或不存在，请核实后再操作。");
                    }
                } else {
                    getAlertMessageManager().addMessageError(userType.getDescription() + "手机号" + mobile + "错误或不存在，请核实后再操作。");
                }
            }
        }
        return "toolkit/toolkit";
    }

    @RequestMapping(value = "user/cleanupBindedEmail.vpage", method = RequestMethod.GET)
    String getCleanupEmail() {
        return "toolkit/toolkit";
    }

    /**
     * 清除邮箱
     */
    @RequestMapping(value = "user/cleanupBindedEmail.vpage", method = RequestMethod.POST)
    String cleanupBindedEmail(@RequestParam(value = "email", required = false) String email, Model model) {
        if (StringUtils.isBlank(email)) {
            getAlertMessageManager().addMessageError("邮箱不能为空");
        }
        if (!getAlertMessageManager().hasMessageError()) {
            MapMessage message;
            try {
                message = userServiceClient.cleanupBindedEmail(getCurrentAdminUser().getAdminUserName(), email);
            } catch (Exception ex) {
                logger.error("Failed to cleanup binded email '{}'", email, ex);
                message = MapMessage.errorMessage();
            }
            if (!message.isSuccess()) {
                getAlertMessageManager().addMessageError("哈哈，阿娟在清除绑定邮箱" + email + "的时候出错了！");
                return "toolkit/toolkit";
            }
            addAdminLog("cleanupBindedMobile", email);
            getAlertMessageManager().addMessageSuccess("阿娟应该是清除了绑定的邮箱" + email + "。");
        }
        return "toolkit/toolkit";
    }

    /**
     * 根据手机号查询发送的信息
     * FIXME 这里当初是何苦要写三遍呢。。。
     */
    @RequestMapping(value = "user/findMobileMessage.vpage", method = RequestMethod.POST)
    String findSmsMessage(@RequestParam(value = "mobile", required = false) String mobile, Model model) {
        if (StringUtils.isBlank(mobile)) {
            getAlertMessageManager().addMessageError("手机号不能为空");
        }

        if (!getAlertMessageManager().hasMessageError()) {
            List<Map<String, Object>> userSmsMessages = smsLoaderClient.getSmsLoader().loadUserSmsMessage(mobile, 10)
                    .stream()
                    .map(sms -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("createTime", sms.getCreateTime());
                        info.put("smsType", SmsType.of(sms.getSmsType()));
                        info.put("smsContent", sms.getSmsContent());
                        info.put("status", sms.getStatus());
                        info.put("errorCode", sms.getErrorCode());
                        info.put("errorDesc", sms.getErrorDesc());
                        SmsClientType channel = null;
                        try {
                            channel = SmsClientType.valueOf(sms.getSmsChannel());
                        } catch (Exception ignored) {
                        }
                        if (channel == null) {
                            info.put("smsChannel", null);
                            info.put("verification", false);
                        } else {
                            info.put("smsChannel", channel.name());
                            info.put("verification", SmsClientCategory.verification_code == channel.getCategory() || SmsClientCategory.voice_verification_code == channel.getCategory());
                        }
                        info.put("consumed", Boolean.TRUE.equals(sms.getConsumed()));
                        info.put("receiveTime", sms.getReceiveTime());
                        return info;
                    }).collect(Collectors.toList());

            addAdminLog("findSmsMessageByMobile", mobile, (userSmsMessages.size() > 0) ? "查询出" + userSmsMessages.size() + "条" : "无结果集");

            model.addAttribute("smsMessageList", userSmsMessages);
            model.addAttribute("queryMobilMessage_mobile", mobile);
        }

        return "toolkit/toolkit";
    }

    /**
     * 查询用户缓存
     */
    @RequestMapping(value = "user/findUserCache.vpage", method = RequestMethod.POST)
    String findUserCache(@RequestParam(value = "userId", required = false) String userId, Model model) throws Exception {
        if (StringUtils.isBlank(userId)) {
            getAlertMessageManager().addMessageError("用户ID不能为空");
        }

        if (!getAlertMessageManager().hasMessageError()) {
            String key = User.generateCacheKey(conversionService.convert(userId, Long.class));
            Object userCache = UserCache.getUserCache().load(key);
            if (userCache != null) {
                model.addAttribute("userCache", JsonUtils.toJsonPretty(userCache));
            } else {
                model.addAttribute("userCache", "没有数据");
            }
            model.addAttribute("queryUserCache_userId", userId);
        }
        return "toolkit/toolkit";
    }

    @RequestMapping(value = "user/findMobileMessage.vpage", method = RequestMethod.GET)
    String forwardIndex(@RequestParam(value = "mobile", required = false) String mobile, Model model) {
        return "toolkit/toolkit";
    }

    @RequestMapping(value = "cdn/flush.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage flushCdn(@RequestParam String flushCdn_url, @RequestParam String flushCdn_dir) {

        MapMessage message = new MapMessage();

        List<String> url = new ArrayList<>();
        List<String> dir = new ArrayList<>();
        if (!StringUtils.isBlank(flushCdn_url)) {
            url = Arrays.asList(flushCdn_url.split("\n"));
        }
        if (!StringUtils.isBlank(flushCdn_dir)) {
            dir = Arrays.asList(flushCdn_dir.split("\n"));
        }
        List<Map> result = CdnFlushFacade.flushCdn(url, dir);
        message.add("result", result);
        return message;
    }


    @RequestMapping(value = "action.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage action() {
        String t = getRequestString("type");
        if (StringUtils.isBlank(t)) return MapMessage.errorMessage("未知类型的Action");

        try {
            switch (ActionEventType.valueOf(t)) {
                case FinishHomework:
                    return finishHomeworkAction();
                case FinishSelfLearning:
                    return finishSelfLearning();
                case FinishOral:
                    return finishOral();
                case SubmitZoumeiAnswer:
                    return submitAnswer(ActionEventType.SubmitZoumeiAnswer);
                case SubmitAfentiEnglishAnswer:
                    return submitAnswer(ActionEventType.SubmitAfentiEnglishAnswer);
                case SubmitAfentiMathAnswer:
                    return submitAnswer(ActionEventType.SubmitAfentiMathAnswer);
                case SubmitAfentiChineseAnswer:
                    return submitAnswer(ActionEventType.SubmitAfentiChineseAnswer);
                case SaveSelfStudyChineseTextRead:
                    return chineseTextRead();
                case StartSelfStudyEnglishWalkman:
                    return englishWalkMan();
                case ClickSelfStudyEnglishPicListen:
                    return picListen();
                case LookHomeworkReport:
                    return lookHomeworkReport();
                default:
                    break;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常," + ex.getMessage());
        }

        return MapMessage.errorMessage("未知的Action类型");
    }

    @RequestMapping(value = "regionlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> regionList(@RequestParam Integer regionCode) {
        Map<String, Object> message = new HashMap<>();
        if (regionCode == null) {
            return message;
        }
        //返回时增加“全部”选项，小于0的regionCode值都为无效值
        Region regionAll = new Region();
        regionAll.setName("全部");
        regionAll.setCode(-1);

        List<Region> regionList = new ArrayList<>();
        if (regionCode >= 0) {
            regionList.addAll(raikouSystem.getRegionBuffer().loadChildRegions(regionCode));
        }

        Set<Region> rs = new TreeSet<>(pinYinComparator);
        rs.addAll(regionList);
        rs.add(regionAll);
        message.put("regionList", rs);
        return message;
    }

    private MapMessage lookHomeworkReport() {
        Long userId = getRequestLong("userId");
        String dt = getRequestString("date");
        String subject = getRequestString("subject");

        try {
            if (0 == userId) return MapMessage.errorMessage("参数错误");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.from(formatter.parse(dt));
            if (dateTime.isAfter(LocalDateTime.now())) return MapMessage.errorMessage("时间错误");

            ActionEvent event = new ActionEvent();
            event.setTimestamp(dateTime.toInstant(dateTime.atZone(ZoneId.systemDefault()).getOffset()).toEpochMilli());
            event.setType(ActionEventType.LookHomeworkReport);
            event.setUserId(userId);
            event.getAttributes().put("homeworkSubject", subject);
            actionServiceClient.getRemoteReference().sendEvent(event);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private MapMessage picListen() {
        Long userId = getRequestLong("userId");
        String dt = getRequestString("date");

        try {
            if (0 == userId) return MapMessage.errorMessage("参数错误");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.from(formatter.parse(dt));
            if (dateTime.isAfter(LocalDateTime.now())) return MapMessage.errorMessage("时间错误");

            ActionEvent event = new ActionEvent();
            event.setTimestamp(dateTime.toInstant(dateTime.atZone(ZoneId.systemDefault()).getOffset()).toEpochMilli());
            event.setType(ActionEventType.ClickSelfStudyEnglishPicListen);
            event.setUserId(userId);
            actionServiceClient.getRemoteReference().sendEvent(event);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private MapMessage englishWalkMan() {
        Long userId = getRequestLong("userId");
        String dt = getRequestString("date");

        try {
            if (0 == userId) return MapMessage.errorMessage("参数错误");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.from(formatter.parse(dt));
            if (dateTime.isAfter(LocalDateTime.now())) return MapMessage.errorMessage("时间错误");

            ActionEvent event = new ActionEvent();
            event.setTimestamp(dateTime.toInstant(dateTime.atZone(ZoneId.systemDefault()).getOffset()).toEpochMilli());
            event.setType(ActionEventType.StartSelfStudyEnglishWalkman);
            event.setUserId(userId);
            actionServiceClient.getRemoteReference().sendEvent(event);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private MapMessage chineseTextRead() {
        Long userId = getRequestLong("userId");
        String dt = getRequestString("date");
        try {
            if (0 == userId) return MapMessage.errorMessage("参数错误");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.from(formatter.parse(dt));
            if (dateTime.isAfter(LocalDateTime.now())) return MapMessage.errorMessage("时间错误");

            ActionEvent event = new ActionEvent();
            event.setTimestamp(dateTime.toInstant(dateTime.atZone(ZoneId.systemDefault()).getOffset()).toEpochMilli());
            event.setType(ActionEventType.SaveSelfStudyChineseTextRead);
            event.setUserId(userId);
            actionServiceClient.getRemoteReference().sendEvent(event);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private MapMessage finishSelfLearning() {
        Long userId = getRequestLong("userId");
        String dt = getRequestString("date");

        try {
            if (0 == userId) return MapMessage.errorMessage("参数错误");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.from(formatter.parse(dt));
            if (dateTime.isAfter(LocalDateTime.now())) return MapMessage.errorMessage("时间错误");

            ActionEvent event = new ActionEvent();
            event.setTimestamp(dateTime.toInstant(dateTime.atZone(ZoneId.systemDefault()).getOffset()).toEpochMilli());
            event.setType(ActionEventType.FinishSelfLearning);
            event.setUserId(userId);
            actionServiceClient.getRemoteReference().sendEvent(event);

            String day = DayRange.newInstance(event.getTimestamp()).toString();
            actionServiceClient.getRemoteReference()
                    .decreaseDayRangeCount(event.getUserId(), event.getType(), day)
                    .awaitUninterruptibly();

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private MapMessage finishHomeworkAction() {
        Long userId = getRequestLong("userId");
        String s = getRequestString("subject");
        Integer score = getRequestInt("score");
        String dt = getRequestString("date");

        try {
            if (0 == userId || StringUtils.isBlank(s) || 0 == score) return MapMessage.errorMessage("参数错误");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.from(formatter.parse(dt));
            if (dateTime.isAfter(LocalDateTime.now())) return MapMessage.errorMessage("时间错误");

            User user = userLoaderClient.loadUser(userId);
            if (null == user) return MapMessage.errorMessage("用户不存在");
            Subject subject = Subject.safeParse(s);
            if (null == subject) return MapMessage.errorMessage("未知学科");

            ActionEvent event = new ActionEvent();
            event.setTimestamp(dateTime.toInstant(dateTime.atZone(ZoneId.systemDefault()).getOffset()).toEpochMilli());
            event.setType(ActionEventType.FinishHomework);
            event.setUserId(userId);
            event.getAttributes().put("homeworkScore", score);
            event.getAttributes().put("homeworkSubject", subject.name());
            actionServiceClient.getRemoteReference().sendEvent(event);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private MapMessage finishOral() {
        Long userId = getRequestLong("userId");
        String homeworkId = getRequestString("hid");
        String dt = getRequestString("date");

        try {
            if (0 == userId || StringUtils.isBlank(homeworkId) || StringUtils.isBlank(dt))
                return MapMessage.errorMessage("参数错误");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.from(formatter.parse(dt));
            if (dateTime.isAfter(LocalDateTime.now())) return MapMessage.errorMessage("时间错误");

            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (null == newHomework) {
                return MapMessage.errorMessage("作业不存在");
            }

            NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), userId, false);
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(ObjectiveConfigType.BASIC_APP);
            if (null == newHomeworkResultAnswer) {
                return MapMessage.errorMessage("未查到口语答题记录");
            }

            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResultAnswer.getAppAnswers();
            if (MapUtils.isEmpty(appAnswers)) {
                return MapMessage.errorMessage("未查询到口语答案");
            }

            long count = appAnswers.values().stream().filter(t -> {
                Long practiceId = t.getPracticeId();
                PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
                return practiceType != null && practiceType.getNeedRecord() && t.getScore() >= 79.5;//作业报告里的分数四舍五入了,这里>=80分就要记一次所以用79.5,与作业报告一致
            }).count();

            if (count <= 0) {
                return MapMessage.errorMessage("口语分数大于80分的数量为0");
            }

            ActionEvent event = new ActionEvent();
            event.setTimestamp(dateTime.toInstant(dateTime.atZone(ZoneId.systemDefault()).getOffset()).toEpochMilli());
            event.setType(ActionEventType.FinishOral);
            event.setUserId(userId);
            event.getAttributes().put("count", count);

            actionServiceClient.getRemoteReference().sendEvent(event);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private MapMessage submitAnswer(ActionEventType type) {
        Long userId = getRequestLong("userId");
        String dt = getRequestString("date");

        try {
            if (0 == userId || StringUtils.isBlank(dt)) return MapMessage.errorMessage("参数错误");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.from(formatter.parse(dt));
            if (dateTime.isAfter(LocalDateTime.now())) return MapMessage.errorMessage("时间错误");

            ActionEvent event = new ActionEvent();
            event.setTimestamp(dateTime.toInstant(dateTime.atZone(ZoneId.systemDefault()).getOffset()).toEpochMilli());
            event.setUserId(userId);
            event.setType(type);

            actionServiceClient.getRemoteReference().sendEvent(event);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "subscribe.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage subscribe() {
        String albumId = getRequestString("albumId");
        String userIds = getRequestString("userIds");
        if (StringUtils.isBlank(albumId) || StringUtils.isBlank(userIds)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            JxtNewsAlbum jxtNewsAlbum = jxtNewsLoaderClient.getJxtNewsAlbum(albumId);
            if (null == jxtNewsAlbum) {
                return MapMessage.errorMessage("专辑不存在");
            }

            String[] userIdStrs = userIds.split("\n");
            Set<Long> availableUser = new HashSet<>(); //去重
            Set<String> notExistsUser = new HashSet<>();
            Set<String> failUser = new HashSet<>();
            for (String uid : userIdStrs) {
                Long userId = SafeConverter.toLong(uid);
                if (availableUser.contains(userId)) {
                    continue;
                }

                User user = userLoaderClient.loadUser(userId);
                if (null == user || user.getUserType() != UserType.PARENT.getType()) {
                    notExistsUser.add(uid);
                    continue;
                }

                MapMessage message = jxtNewsServiceClient.subAlbum(userId, albumId);
                if (!message.isSuccess()) {
                    failUser.add(uid);
                } else {
                    availableUser.add(userId);
                }
            }

            if (notExistsUser.isEmpty() && failUser.isEmpty()) {
                return MapMessage.successMessage();
            }

            return MapMessage.errorMessage().add("notExist", JsonUtils.toJson(notExistsUser)).add("failUser", JsonUtils.toJson(failUser));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/syncFltrpOrder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage syncFltrpOrder() {
        String ids = getRequestString("ids");
        if (StringUtils.isBlank(ids)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            String[] orderIds = ids.split("\n");
            for (String orderId : orderIds) {
                if (!orderId.contains("_")) {
                    continue;
                }

                if (orderId.length() > orderId.lastIndexOf("_") + 3) {
                    orderId = orderId.substring(0, orderId.lastIndexOf("_") - 1);
                }

                UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
                if (userOrder == null) {
                    continue;
                }
                if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook) {
                    continue;
                }
                if (userOrder.getPaymentStatus() != PaymentStatus.Paid) {
                    continue;
                }
                picListenBookOrderService.synchronizePicListenOrder(userOrder);
            }

            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/refundpiclistenbookorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refundPicListenBookOrder() {
        String ids = getRequestString("ids");
        if (StringUtils.isBlank(ids)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            String[] orderIds = ids.split("\n");
            for (String orderId : orderIds) {
                picListenBookOrderService.notifyThirdPartyCancelOrder(orderId);
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Refund piclistenbook order failed", ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/getRJUserDevList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getRJUserDevList() {
        long userId = getRequestLong("userId");
        if (userId == 0L) {
            return MapMessage.errorMessage();
        }
        List<PicListenBookUserDev.DevInfo> devInfos = picListenBookDelDevService.queryPicDevList(userId);
        return MapMessage.successMessage().add("devList", devInfos);
    }


    @RequestMapping(value = "/delRJUserDevs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delRJUserDevs() {
        long userId = getRequestLong("userId");
        List<String> devIds = JsonUtils.fromJsonToList(getRequestString("devIds"), String.class);
        if (userId == 0L || CollectionUtils.isEmpty(devIds)) {
            return MapMessage.errorMessage();
        }
        return picListenBookDelDevService.delPicDev(userId, devIds);
    }

    @RequestMapping(value = "userremind.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userRemind() {
        Long userId = getRequestLong("userId");
        if (userId == 0) {
            return MapMessage.errorMessage();
        }
        String position = getRequestString("reminderPosition");
        ReminderPosition reminderPosition = ReminderPosition.of(position);
        String type = getRequestString("handleType");
        String remindType = getRequestString("remindType");
        if ("incr".equals(type)) {
            if ("dot".equals(remindType)) {
                reminderService.addUserReminder(userId, reminderPosition);
            } else {
                reminderService.addUserNumberReminder(userId, reminderPosition);
            }
        } else {
            if ("dot".equals(remindType)) {
                reminderService.decrUserReminder(userId, reminderPosition);
            } else {
                reminderService.decrUserNumberReminder(userId, reminderPosition);
            }
        }
        return MapMessage.successMessage();
    }


    private XSSFWorkbook readExcel(String name) {
        HttpServletRequest request = getRequest();
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

    private MapMessage validateImg(MultipartFile file, int width, int height) {
        if (width == 0 || height == 0) {
            return MapMessage.successMessage();
        }
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            int h = image.getHeight();
            int w = image.getWidth();
            if (w != width || h != height) {
                return MapMessage.errorMessage("图片大小不匹配，请重新上传大小为" + width + "*" + height + "的图片！");
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed validate Img, ex={}", ex);
            return MapMessage.errorMessage("图片校验异常！");
        }
    }

}
