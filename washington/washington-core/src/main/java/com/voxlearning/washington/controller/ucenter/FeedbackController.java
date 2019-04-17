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

package com.voxlearning.washington.controller.ucenter;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.misc.UserAppeal;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.feedback.api.entities.ExamFeedback;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.feedback.client.AppealServiceClient;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingTeacher;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.upload.CommonImageUploader;
import lombok.Cleanup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;

@Controller
@RequestMapping("/ucenter")
public class FeedbackController extends AbstractController {

    @Inject private CommonImageUploader commonImageUploader;

    @Inject private AppealServiceClient appealServiceClient;
    @Inject private FeedbackServiceClient feedbackServiceClient;

    private Map getStudentFeedbackTypes() {

        Map<String, Object> cache = new LinkedHashMap<>();

        Map<String, Object> map;

        map = new LinkedHashMap<>();
        cache.put("账号与班级", map);
        map.put("忘记账号", new ArrayList<>());
        map.put("忘记密码", new ArrayList<>());
        map.put("班级问题", new ArrayList<>());
        map.put("其他", new ArrayList<>());

        map = new LinkedHashMap<>();
        cache.put("英语基础作业", map);
        map.put("_text", "英语基础作业（背单词/跟读/句子）");
        map.put("加载过程中卡住", new ArrayList<>());
        map.put("作业白屏", new ArrayList<>());
        map.put("作业黑屏", new ArrayList<>());
        map.put("作业无法打开", new ArrayList<>());
        map.put("作业过程卡住", new ArrayList<>());
        map.put("作业反应慢", new ArrayList<>());
        map.put("作业内容错误", new ArrayList<>());
        map.put("作业不方便操作", new ArrayList<>());
        map.put("录音问题", new ArrayList<>());
        map.put("语音作业Go无法通过", new ArrayList<>());
        map.put("语音分析/打分问题", new ArrayList<>());
        map.put("其他", new ArrayList<>());

        map = new LinkedHashMap<>();
        cache.put("英语同步试题", map);
        map.put("_text", "英语同步试题（选择题/填空题/归类题/阅读题等");
        map.put("作业无法打开", new ArrayList<>());
        map.put("作业过程卡住", new ArrayList<>());
        map.put("作业反应慢", new ArrayList<>());
        map.put("作业内容错误", new ArrayList<>());
        map.put("没有声音/没有图片", new ArrayList<>());
        map.put("作业不方便操作", new ArrayList<>());
        map.put("其他", new ArrayList<>());

        map = new LinkedHashMap<>();
        cache.put("英语阅读应用", map);
        map.put("作业无法打开", new ArrayList<>());
        map.put("作业过程卡住", new ArrayList<>());
        map.put("作业反应慢", new ArrayList<>());
        map.put("作业内容错误", new ArrayList<>());
        map.put("没有声音/没有图片", new ArrayList<>());
        map.put("作业不方便操作", new ArrayList<>());
        map.put("其他", new ArrayList<>());

        map = new LinkedHashMap<>();
        cache.put("数学基础作业", map);
        map.put("_text", "数学基础作业（口算练习）");
        map.put("作业无法打开", new ArrayList<>());
        map.put("作业过程卡住", new ArrayList<>());
        map.put("作业反应慢", new ArrayList<>());
        map.put("作业内容错误", new ArrayList<>());
        map.put("作业不方便操作", new ArrayList<>());
        map.put("其他", new ArrayList<>());

        map = new LinkedHashMap<>();
        cache.put("数学同步试题", map);
        map.put("_text", "数学同步试题（选择题/填空题/判断题等）");
        map.put("没有图片", new ArrayList<>());
        map.put("作业无法打开", new ArrayList<>());
        map.put("作业过程卡住", new ArrayList<>());
        map.put("作业反应慢", new ArrayList<>());
        map.put("题目错误", new ArrayList<>());
        map.put("题目显示不全", new ArrayList<>());
        map.put("不方便操作", new ArrayList<>());
        map.put("其他", new ArrayList<>());

//        map = new LinkedHashMap<>();
//        cache.put("PK问题", map);
//        map.put("卡住/慢", new ArrayList<>());
//        map.put("升级问题", new ArrayList<>());
//        map.put("PK商城", new ArrayList<>());
//        map.put("排行榜", new ArrayList<>());
//        map.put("其他", new ArrayList<>());

//        map = new LinkedHashMap<>();
//        cache.put("通天塔问题", map);
//        map.put("卡住/慢", new ArrayList<>());
//        map.put("精力问题", new ArrayList<>());
//        map.put("BOSS战", new ArrayList<>());
//        map.put("其他", new ArrayList<>());
//
//        map = new LinkedHashMap<>();
//        cache.put("走遍美国问题", map);
//        map.put("加载卡/慢", new ArrayList<>());
//        map.put("GO不能点击", new ArrayList<>());
//        map.put("无法购买钻石", new ArrayList<>());
//        map.put("点亮VIP问题", new ArrayList<>());
//        map.put("其他", new ArrayList<>());

//        map = new LinkedHashMap<>();
//        cache.put("趣味数学问题", map);
//        map.put("加载卡/慢", new ArrayList<>());
//        map.put("START不能点击", new ArrayList<>());
//        map.put("其他", new ArrayList<>());
//
//        map = new LinkedHashMap<>();
//        cache.put("爱儿优问题", map);
//        map.put("加载卡/慢", new ArrayList<>());
//        map.put("无法进入应用", new ArrayList<>());
//        map.put("视频无法播放", new ArrayList<>());
//        map.put("其他", new ArrayList<>());

//        map = new LinkedHashMap<>();
//        cache.put("进击的三国问题", map);
//        map.put("加载卡/慢", new ArrayList<>());
//        map.put("无法进入应用", new ArrayList<>());
//        map.put("视频无法播放", new ArrayList<>());
//        map.put("其他", new ArrayList<>());
//
//        map = new LinkedHashMap<>();
//        cache.put("洛亚传说问题", map);
//        map.put("加载卡/慢", new ArrayList<>());
//        map.put("副本战斗卡慢", new ArrayList<>());
//        map.put("金币不够用", new ArrayList<>());
//        map.put("无法进入应用", new ArrayList<>());
//        map.put("任务太多，太累", new ArrayList<>());
//        map.put("其他", new ArrayList<>());

//        map = new LinkedHashMap<>();
//        cache.put("沃克大冒险问题", map);
//        map.put("加载卡/慢", new ArrayList<>());
//        map.put("游戏过程卡住", new ArrayList<>());
//        map.put("不方便操作", new ArrayList<>());
//        map.put("太贵了", new ArrayList<>());
//        map.put("其他", new ArrayList<>());
//
//        map = new LinkedHashMap<>();
//        cache.put("宠物大乱斗问题", map);
//        map.put("加载卡/慢", new ArrayList<>());
//        map.put("无法进入应用", new ArrayList<>());
//        map.put("视频无法播放", new ArrayList<>());
//        map.put("其他", new ArrayList<>());

//        cache.put("大爆料问题", new ArrayList<>());
        cache.put("奖品中心问题", new ArrayList<>());

        //'银币问题': [], 先不显示这个了，免得孩子总是问银币问题......
//        map = new LinkedHashMap<>();
//        cache.put("课外练习", map);
//        map.put("_text", "课外练习（阿分题/冒险岛/单词达人）");

        cache.put("改进建议", new ArrayList<>());
        cache.put("其他问题", new ArrayList<>());

        return cache;
    }

    //普通的feedback提交
    @RequestMapping(value = "feedback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage feedbackPost() {

        try {

            UserFeedback userFeedback = new UserFeedback();

            userFeedback.setContactSensitivePhone(sensitiveUserDataServiceClient.encodeMobile(getRequestParameter("contactPhone", "")));
            userFeedback.setContactSensitiveQq(sensitiveUserDataServiceClient.encodeQq(getRequestParameter("contactQq", "")));
            userFeedback.setContent(StringHelper.cleanXSS(getRequestParameter("content", "")));
            // fix data too large exception
            if (StringUtils.isNoneBlank(userFeedback.getContent()) && userFeedback.getContent().length() > 150) {
                userFeedback.setContent(userFeedback.getContent().substring(0, 150));
            }
            // 作业ID
            userFeedback.setExtStr1(getRequestParameter("extStr1", ""));
            // 应试题ID
            userFeedback.setExtStr2(getRequestParameter("extStr2", ""));
            userFeedback.setFeedbackType(StringHelper.cleanXSS(getRequestParameter("feedbackType", "")));
            userFeedback.setFeedbackSubType1(StringHelper.filterEmojiForMysql(StringHelper.cleanXSS(getRequestParameter("feedbackSubType1", ""))));
            userFeedback.setFeedbackSubType2(StringHelper.filterEmojiForMysql(StringHelper.cleanXSS(getRequestParameter("feedbackSubType2", ""))));
            userFeedback.setIp(getWebRequestContext().getRealRemoteAddr());
            //userFeedback.address 需要在service端设置。washington没有IPSeeker?
            userFeedback.setPracticeType(getRequestInt("practiceType", 0));
            userFeedback.setPracticeName(getRequestParameter("practiceName", ""));
            userFeedback.setRefUrl(getRequestParameter("refUrl", ""));

            userFeedback.setUserId((currentUserId() != null) ? currentUserId() : 0);
            userFeedback.setUserType((currentUserId() != null) ? currentUser().fetchUserType().getType() : 0);
            userFeedback.setRealName((currentUserId() != null) ? currentUser().getProfile().getRealname() : "");

            if (currentUser() != null && currentUser().isStudent()) {
                StudentParent parent = parentLoaderClient.loadStudentKeyParent(currentUserId());
                if (parent != null) {
                    User user = parent.getParentUser();
                    if (user != null) {
                        userFeedback.setStudentKeyParentId(user.getId());
                    }
                }
            }
            //选择其他时，添加数据收集选项【TAG_ID:92，TAG:数据收集】
            boolean dataCollectionSelect = getRequestBool("dataCollectionSelect");
            if ("其他".equals(userFeedback.getFeedbackSubType1()) && dataCollectionSelect) {
                userFeedback.setTag("数据收集");
                userFeedback.setTagId(92L);
            }

            // 这里加错题反馈的
            if (("英语同步试题".equals(userFeedback.getFeedbackType()) && "作业内容错误".equals(userFeedback.getFeedbackSubType1()))
                    || ("数学同步试题".equals(userFeedback.getFeedbackType()) && "题目错误".equals(userFeedback.getFeedbackSubType1()))) {
                feedbackServiceClient.getFeedbackService().sendExamFeedback(userFeedback.getUserId(), userFeedback.getContent(), ExamFeedback.fetchExamFeedbackType(7), getRequestParameter("extStr2", ""));
            }
            return feedbackServiceClient.getFeedbackService().saveFeedback(userFeedback);

        } catch (Exception e) {
            logger.error("feedbackPost " + e.getMessage(), e);
            return MapMessage.errorMessage("留言失败,请稍后再试");
        }
    }

    //feedback提交之后，还允许补充联系方式
    @RequestMapping(value = "feedback-contact.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage feedbackPostContact() {
        Long feedbackId = getRequestLong("feedbackId");
        String contactQq = getRequestParameter("contactQq", "");
        String contactPhone = getRequestParameter("contactPhone", "");

        return feedbackServiceClient.getFeedbackService().updateFeedbackContact(currentUserId(), feedbackId, contactQq, contactPhone);
    }

    // 老师反馈页面
    @RequestMapping(value = "teacherfeedback.vpage", method = RequestMethod.GET)
    public String teacherFeedbackPage(Model model) {
        String title = getRequestString("title");
        if (StringUtils.isNotBlank(title)) {
            model.addAttribute("title", title);
        }
        String feedbackType = getRequestString("feedbackType");
        Teacher teacher = currentTeacher();
        if (StringUtils.isNotBlank(feedbackType)) {
            model.addAttribute("feedbackType", feedbackType);
        } else {
            if (teacher != null && teacher.getKtwelve() == Ktwelve.JUNIOR_SCHOOL) {
                model.addAttribute("feedbackType", "中学老师PC端问题");
            }
        }
        // 是否显示申诉入口
        if (teacher != null) {
            PossibleCheatingTeacher cheatingTeacher = newHomeworkLoaderClient.loadPossibleCheatingTeacherByTeacherId(teacher.getId());
            if (cheatingTeacher != null && cheatingTeacher.getStatus() != CheatingTeacherStatus.WHITE) {
                model.addAttribute("showCheating", true);
            }

            boolean showFake = false;
            CrmTeacherSummary summary = crmSummaryLoaderClient.loadTeacherSummary(teacher.getId());
            if (summary != null && summary.getFakeTeacher() && CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(summary.getValidationType())) {
                showFake = true;
            }

            if (teacherLoaderClient.isFakeTeacher(teacher.getId())) {
                showFake = true;
            }

            model.addAttribute("showFake", showFake);

        }
        return "ucenter/teacherfeedback";
    }

    //默认的feedback页面
    @RequestMapping(value = "feedback.vpage", method = RequestMethod.GET)
    public String feedbackPage(Model model) {

        Map<String, Object> postData = new HashMap<>();
        postData.put("refUrl", getRequestParameter("refUrl", ""));
        postData.put("homeworkType", getRequestParameter("homeworkType", ""));
        postData.put("practiceType", getRequestLong("practiceType", 0L));
        postData.put("practiceName", getRequestParameter("practiceName", ""));
        postData.put("extStr1", getRequestParameter("extStr1", ""));
        postData.put("extStr2", getRequestParameter("extStr2", ""));

        long practiceType = Long.parseLong(postData.get("practiceType").toString());

        if (StringUtils.isNotBlank(postData.get("practiceName").toString())
                && practiceType != 0L) {
            if ("english".equals(postData.get("homeworkType"))) {
                if (practiceType == 40L)
                    postData.put("practiceName", "exam");
                else {
                    PracticeType entity = practiceLoaderClient.loadPractice(practiceType);
                    postData.put("practiceName", (entity == null) ? null : entity.getPracticeName());
                }
            } else if ("math".equals(postData.get("homeworkType"))) {
                PracticeType entity = practiceLoaderClient.loadPractice(practiceType);
                postData.put("practiceName", (entity == null) ? null : entity.getPracticeName());
            } else {
                postData.put("practiceName", postData.get("homeworkType").toString());
            }
        }

        model.addAttribute("feedbackTypes", getStudentFeedbackTypes());
        model.addAttribute("postData", postData);
        return "ucenter/feedback.inner";
    }

    //重置密码的反馈页面
    @RequestMapping(value = "feedback-resetpwd.vpage", method = RequestMethod.GET)
    public String feedbackPageResetpwd() {
        return "ucenter/feedback-resetpwd";
    }


    // 老师申诉页面 h5
    @RequestMapping(value = "appeal.vpage", method = RequestMethod.GET)
    public String appealIndex(Model model) {
        String type = getRequestString("type");
        model.addAttribute("type", type);
        return "ucenter/appeal/index";
    }

    // 老师申诉提交内容
    @RequestMapping(value = "saveappeal.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAppeal() {
        try {
            String type = getRequestString("type");     // 申诉类型
            String reason = getRequestString("reason"); // 申诉原因
            String fileName = getRequestString("fileName"); // 上传的文件名
            if (StringUtils.isBlank(type) || StringUtils.isBlank(reason) || StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("提交失败，请完善申诉信息。");
            }
            // 保存信息
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage("请登录账号重试");
            }
            UserAppeal.Type appealType = UserAppeal.Type.valueOf(type);
            // 是否允许申诉
            if (appealType == UserAppeal.Type.CHEATING) {
                PossibleCheatingTeacher cheatingTeacher = newHomeworkLoaderClient.loadPossibleCheatingTeacherByTeacherId(teacherDetail.getId());
                if (cheatingTeacher == null || cheatingTeacher.getStatus() == CheatingTeacherStatus.WHITE) {
                    return MapMessage.errorMessage("对不起，您不满足申诉条件");
                }
            }
            if (appealType == UserAppeal.Type.FAKE) {

                boolean fakeTeacher = false;
                CrmTeacherSummary summary = crmSummaryLoaderClient.loadTeacherSummary(teacherDetail.getId());
                if (summary != null && summary.getFakeTeacher() && CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(summary.getValidationType())) {
                    fakeTeacher = true;
                }

                if (teacherLoaderClient.isFakeTeacher(teacherDetail.getId())) {
                    fakeTeacher = true;
                }

                if (!fakeTeacher) {
                    return MapMessage.errorMessage("对不起，您不满足申诉条件");
                }
            }
            UserAppeal appeal = new UserAppeal();
            appeal.setStatus(UserAppeal.Status.WAIT);
            appeal.setAname(teacherDetail.getCountyName());
            appeal.setCname(teacherDetail.getCityName());
            appeal.setPname(teacherDetail.getRootRegionName());
            appeal.setSchoolId(teacherDetail.getTeacherSchoolId());
            appeal.setSchoolName(teacherDetail.getTeacherSchoolName());
            appeal.setFileName(fileName);
            appeal.setType(UserAppeal.Type.valueOf(type));
            appeal.setReason(reason);
            appeal.setUserId(teacherDetail.getId());
            appeal.setUserName(teacherDetail.fetchRealname());
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            return builder.keyPrefix("FeedbackController.saveAppeal")
                    .keys(appeal.getUserId())
                    .callback(() -> appealServiceClient.getAppealService().saveUserAppeal(appeal).getUninterruptibly())
                    .build()
                    .execute();
        } catch (Exception ex) {
            logger.error("teacher appeal error, id {}, error {}", currentUserId(), ex.getMessage());
            return MapMessage.errorMessage("提交失败，请重试");
        }
    }

    // 老师申诉上传照片
    @RequestMapping(value = "appealupload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage appealUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("请选择上传的文件！");
        }
        try {
            List<String> IMG_SUFFIX = Arrays.asList("bmp", "gif", "jpeg", "jpg", "png"); // 此处维护上传图片的后缀
            String ext = StringUtils.substringAfterLast(file.getOriginalFilename(), ".").toLowerCase();
            if (!IMG_SUFFIX.contains(ext)) {
                return MapMessage.errorMessage("图片格式只能是" + Arrays.toString(IMG_SUFFIX.toArray()));
            }
            String prefix = "appeal-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + currentUserId();
            String originalFileName = file.getOriginalFilename();
            @Cleanup InputStream inStream = file.getInputStream();
            String fileName = commonImageUploader.upload(prefix, originalFileName, inStream);
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("上传失败，请重试");
            }
            return MapMessage.successMessage().add("fileName", fileName);
        } catch (Exception ex) {
            logger.error("Failed to upload img, ex={}", ex);
            return MapMessage.errorMessage("上传素材失败：" + ex.getMessage());
        }
    }
}
