package com.voxlearning.utopia.admin.controller.coursewarecontest;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.constant.UploadFileType;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.admin.dao.CrmTeacherCoursewareDao;
import com.voxlearning.utopia.admin.util.UploadOssManageUtils;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewarePageInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareContestServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/crm/courseware/contest")
public class CrmTeacherCoursewareContestController extends CrmAbstractController {

    @Inject
    private TeacherCoursewareContestServiceClient teacherCoursewareContestServiceClient;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private CrmTeacherCoursewareDao coursewareDao;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Inject protected SmsServiceClient smsServiceClient;

    @Inject protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject protected VendorLoaderClient vendorLoaderClient;

    @Inject private AppMessageServiceClient appMessageServiceClient;

    @Inject private TeacherLoaderClient teacherLoader;

    private static final String PASS_CONTENT = "尊敬的老师，您上传的教学设计作品已经成功通过初选，进入线上展示环节，" +
            "接受大众老师对作品的点评、下载，请密切关注作品的得分情况，并分享给身边的老师为您的作品助力哦！";

    private static final String OLD_PASS_CONTENT = "尊敬的老师，您上传的教学设计作品已经成功通过初选，10月20日起将进入线上展示环节，" +
            "接受大众老师对作品的点评、下载，请密切关注作品的得分情况，并分享给身边的老师为您的作品助力哦！";

    private static final String LINK = "前往活动官网：www.17zuoye.com";

    private static final String REJECT_CONTENT = "尊敬的老师，您好！您上传的教学设计作品因暂未通过审核被退回到个人中心，" +
            "请查阅具体原因，修正后再次提交！感谢您的支持！";

    private static final String PASS_TITLE = "教学设计作品审核通过通知";

    private static final String REJECT_TITLE = "教学设计作品退回通知";

    @RequestMapping(value = "examine/workslist.vpage",method = RequestMethod.GET)
    public String fetchExamineWorksList(Model model) {

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }

        String title = getRequestString("title");
        int clazzLevel = getRequestInt("clazzLevel");
        int term = getRequestInt("term");

        String userId = getRequestParameter("userId", "");
        Long userIdLong = SafeConverter.toLong(userId);
        String startDateStr = getRequestString("startDate");
        Date startDate = getDateFromString(startDateStr, DateUtils.FORMAT_SQL_DATE);
        String endDateStr = getRequestString("endDate");
        Date endDate = getDateFromString(endDateStr, DateUtils.FORMAT_SQL_DATE);

        String examineStatusStr = getRequestParameter("examineStatus", "WAITING");
        TeacherCourseware.ExamineStatus examineStatus = TeacherCourseware.ExamineStatus.valueOf(examineStatusStr);
        List<TeacherCourseware> teacherCoursewareList = coursewareDao.findExamStatus(examineStatus).stream()
                .filter(e -> StringUtils.isNoneBlank(userId) || examineStatus == TeacherCourseware.ExamineStatus.WAITING || e.getExaminer().equals(adminUser.getAdminUserName()))
                .filter(e -> StringUtils.isBlank(title) || e.getTitle().contains(title))
                .filter(e -> clazzLevel == 0 || (e.getClazzLevel() != null && e.getClazzLevel() == clazzLevel))
                .filter(e -> term == 0 || (e.getTermType() != null && e.getTermType() == term))
                .filter(e -> startDate == null || startDate.before(e.getUpdateTime()))
                .filter(e -> endDate == null || endDate.after(e.getUpdateTime()))
                .filter(e -> StringUtils.isEmpty(userId) || Objects.equals(e.getTeacherId(), userIdLong))
                .sorted((e1, e2)->{
                    long t1 = e1.getExamineUpdateTime() == null ? 0L : e1.getExamineUpdateTime().getTime();
                    long t2 = e2.getExamineUpdateTime() == null ? 0L : e2.getExamineUpdateTime().getTime();
                    return t1 > t2 ? 1 : -1;
                }).collect(Collectors.toList());

        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(teacherCoursewareList)) {
            List<Long> teacherIds = teacherCoursewareList.stream().map(TeacherCourseware::getTeacherId).collect(Collectors.toList());
            Map<Long, Teacher> teacherMap = new HashMap<>();
            for(int i = 0; i < teacherIds.size(); i += 200) {
                teacherMap.putAll(teacherLoaderClient.loadTeachers(teacherIds.subList(i, Math.min(teacherIds.size(), i + 200))));
            }
            teacherCoursewareList.forEach(e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", e.getId());
                map.put("title", e.getTitle());
                map.put("teacher", (teacherMap.get(e.getTeacherId()) != null ?  teacherMap.get(e.getTeacherId()).fetchRealname() : "") + "("
                 + e.getTeacherId() + ")");
                map.put("examineStatusDesc", e.getExamineStatus().getDescription());
                map.put("examineStatus", e.getExamineStatus().name());
                map.put("examiner", e.getExaminer());
                map.put("date", DateUtils.dateToString(e.getUpdateTime()));
                list.add(map);
            });

        }

        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 10);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(list, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("title", title);
        model.addAttribute("userId", userId);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("clazzLevel", clazzLevel);
        model.addAttribute("term", term);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("examineStatus", examineStatusStr);
        model.addAttribute("examineStatusList", TeacherCourseware.ExamineStatus.values());
        return "coursewarecontest/index";
    }

    private static Date getDateFromString(String dateString, String pattern) {
        try {
            return DateUtils.parseDate(dateString, pattern);
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping(value = "workslist.vpage",method = RequestMethod.GET)
    public String fetchWorksList(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "";
        }

        String title = getRequestString("title");
        int clazzLevel = getRequestInt("clazzLevel");
        int term = getRequestInt("term");


        String startDateStr = getRequestString("startDate");
        Date startDate = getDateFromString(startDateStr, DateUtils.FORMAT_SQL_DATE);
        String endDateStr = getRequestString("endDate");
        Date endDate = getDateFromString(endDateStr, DateUtils.FORMAT_SQL_DATE);

        String examineStatusStr = getRequestParameter("examineStatus", "WAITING");
        TeacherCourseware.ExamineStatus examineStatus = TeacherCourseware.ExamineStatus.valueOf(examineStatusStr);
        List<TeacherCourseware> teacherCoursewareList = coursewareDao.findExamStatus(examineStatus).stream()
                .filter(e -> StringUtils.isBlank(title) || e.getTitle().contains(title))
                .filter(e -> clazzLevel == 0 || (e.getClazzLevel() != null && e.getClazzLevel() == clazzLevel))
                .filter(e -> term == 0 || (e.getClazzLevel() != null && e.getClazzLevel() == clazzLevel))
                .filter(e -> startDate == null || startDate.before(e.getUpdateTime()))
                .filter(e -> endDate == null || endDate.after(e.getUpdateTime()))
                .sorted((e1, e2)->{
                    long t1 = e1.getExamineUpdateTime() == null ? 0L : e1.getExamineUpdateTime().getTime();
                    long t2 = e2.getExamineUpdateTime() == null ? 0L : e2.getExamineUpdateTime().getTime();
                    return t1 > t2 ? 1 : -1;
                }).collect(Collectors.toList());

        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(teacherCoursewareList)) {
            List<Long> teacherIds = teacherCoursewareList.stream().map(TeacherCourseware::getTeacherId).collect(Collectors.toList());
            Map<Long, Teacher> teacherMap = new HashMap<>();
            for(int i = 0; i < teacherIds.size(); i += 200) {
                teacherMap.putAll(teacherLoaderClient.loadTeachers(teacherIds.subList(i, Math.min(teacherIds.size(), i + 200))));
            }
            teacherCoursewareList.forEach(e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", e.getId());
                map.put("title", e.getTitle());
                map.put("teacher", (teacherMap.get(e.getTeacherId()) != null ?  teacherMap.get(e.getTeacherId()).fetchRealname() : "") + "("
                        + e.getTeacherId() + ")");
                map.put("examineStatusDesc", e.getExamineStatus().getDescription());
                map.put("examineStatus", e.getExamineStatus().name());
                map.put("examiner", e.getExaminer());
                map.put("date", DateUtils.dateToString(e.getUpdateTime()));
                list.add(map);
            });

        }

        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 10);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(list, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("title", title);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("clazzLevel", clazzLevel);
        model.addAttribute("term", term);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("examineStatus", examineStatusStr);
        model.addAttribute("examineStatusList", TeacherCourseware.ExamineStatus.values());
        return "coursewarecontest/list";
    }

    @RequestMapping(value = "getworks.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getworks() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        return teacherCoursewareContestServiceClient.updateCourseToExamine(id, adminUser.getAdminUserName());
    }

    @RequestMapping(value = "works/detail.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage worksDetail() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(id);
        if (teacherCourseware == null || Boolean.TRUE.equals(teacherCourseware.getDisabled())) {
            return MapMessage.errorMessage("数据不存在");
        }

        NewBookProfile bookProfile = null;
        if (StringUtils.isNotBlank(teacherCourseware.getBookId())) {
            bookProfile = newContentLoaderClient.loadBook(teacherCourseware.getBookId());
        }

        NewBookCatalog newBookCatalog = null;
        if (StringUtils.isNotBlank(teacherCourseware.getUnitId())) {
            newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(teacherCourseware.getUnitId());
        }

        NewBookCatalog lessonInfo = null;
        if (StringUtils.isNotBlank(teacherCourseware.getLessonId())) {
            lessonInfo = newContentLoaderClient.loadBookCatalogByCatalogId(teacherCourseware.getLessonId());
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherCourseware.getTeacherId());
        MapMessage result = MapMessage.successMessage().set("id", teacherCourseware.getId()).
                set("title", teacherCourseware.getTitle()).
                set("term", teacherCourseware.getTermType()).
                set("clazzLevel", ClazzLevel.parse(teacherCourseware.getClazzLevel()) != null ?
                         ClazzLevel.parse(teacherCourseware.getClazzLevel()).getDescription() : "").
                set("status", teacherCourseware.getExamineStatus()).
                set("statusDesc", teacherCourseware.getExamineStatus().getDescription()).
                set("date", DateUtils.dateToString(teacherCourseware.getUpdateTime())).
                set("image", bookProfile != null ? bookProfile.getImgUrl() : "").
                set("subject",teacherCourseware.getSubject().getValue()).
                set("bookName",bookProfile != null ? bookProfile.getName() : "").
                set("bookId",newBookCatalog != null ? newBookCatalog.getId() : "").
                set("description", "null".equals(teacherCourseware.getDescription()) ? "无" : teacherCourseware.getDescription()).
                set("lessonName", lessonInfo == null ? "" : lessonInfo.getName()).
                set("coverUrl", teacherCourseware.getCoverUrl()).
                set("pictureUrlList", teacherCourseware.getPicturePreview()).
                set("wordImageUrl", teacherCourseware.getWordFilePreview()).
                set("coursewareFile", teacherCourseware.getCoursewareFile()).
                set("coursewareFileName", teacherCourseware.getCoursewareFileName()).
                set("coursewareFileImages", teacherCourseware.getCoursewareFilePreview()).
                set("pptCoursewareFileName", teacherCourseware.getPptCoursewareFileName()).
                set("unitName",newBookCatalog != null ? newBookCatalog.getName() : "").
                set("teacherId",teacherDetail.getUserIntegral().getUserId()).
                set("teacherName",teacherDetail.fetchRealname()).
                set("wordUrl",teacherCourseware.getWordUrl()).
                set("wordName", teacherCourseware.getWordName()).
                set("awardLevelId",teacherCourseware.getAwardLevelId()).
                set("awardLevelName",teacherCourseware.getAwardLevelName()).
                set("awardPreview",CollectionUtils.isEmpty(teacherCourseware.getAwardPicturePreview()) ? new HashMap<>() : teacherCourseware.getAwardPicturePreview().get(0)).
                set("awardIntroduction",teacherCourseware.getAwardIntroduction()).
                set("version",bookProfile !=null ? bookProfile.getShortPublisher() : "");
        return result;
    }


    @RequestMapping(value = "examineworks.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage examinWorks() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(id);
        if (teacherCourseware == null || Boolean.TRUE.equals(teacherCourseware.getDisabled())) {
            return MapMessage.errorMessage("数据不存在");
        }

        /*if (!adminUser.getAdminUserName().equals(teacherCourseware.getExaminer())) {
            return MapMessage.errorMessage("该课件已经被其他人领取了");
        }*/

        boolean pass = getRequestBool("pass");
        String desc = getRequestString("description");
        if (!pass && StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("驳回原因未填写");
        }

        MapMessage message = teacherCoursewareContestServiceClient.examineCourseToEnd(id, adminUser.getAdminUserName(), pass, desc);

        try {
            if (message.isSuccess()){
                Date now = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date octoberTwenty = simpleDateFormat.parse("2018-10-20");
                if (now.before(octoberTwenty)){
                    sendMassage(teacherCourseware.getTeacherId(),pass,
                            pass ? OLD_PASS_CONTENT : REJECT_CONTENT);
                } else {
                    sendMassage(teacherCourseware.getTeacherId(),pass,
                            pass ? PASS_CONTENT : REJECT_CONTENT);
                }
            }
        } catch (Exception e){
            logger.error("send info failed:{},error:{}",id,e.getMessage());
        }
        return message;
    }

    private void sendMassage(Long teacherId,boolean pass,String messageContent){

        // 发送 APP 消息
        AppMessage msg = new AppMessage();
        msg.setUserId(teacherId);
        msg.setMessageType(TeacherMessageType.ACTIVIY.getType());
        msg.setContent(messageContent + LINK);
        msg.setTitle(pass ? PASS_TITLE : REJECT_TITLE);
        msg.setCreateTime(new Date().getTime());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);

        // 发送 pc 消息
        String append = "前往活动官网：<a href=\"http://www.17zuoye.com\" class=\"w-blue\" target=\"_blank\">www.17zuoye.com</a>";
        teacherLoader.sendTeacherMessage(teacherId, messageContent + append);

        // 发送 push
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("s", TeacherMessageType.ACTIVIY.name());
        jpushExtInfo.put("key", "j");
        jpushExtInfo.put("t", "h5");

        TeacherDetail td = teacherLoader.loadTeacherDetail(teacherId);
        appMessageServiceClient.sendAppJpushMessageByIds(
                messageContent + LINK,
                AppMessageUtils.getMessageSource("17Teacher", td),
                Collections.singletonList(teacherId),
                jpushExtInfo);

        // 发送短信
        // 获取手机号
        String mobileNumber = sensitiveUserDataServiceClient.loadUserMobile(teacherId);
        // 发送短信
        if(!StringUtils.isEmpty(mobileNumber)){
            smsServiceClient.createSmsMessage(mobileNumber).
                    content(messageContent + LINK).type(SmsType.TEACHER_OPERATION_JOB.name()).send();
        }

    }

    @StorageClientLocation(storage = "plat-doc-content")
    private StorageClient fileStorageClient;

    private static final Integer PAGE_SIZE = 50;

    @RequestMapping(value = "converr.vpage",method = RequestMethod.GET)
    public void converFilef() throws Exception{
        TeacherCoursewareParam teacherCoursewareParam = new TeacherCoursewareParam();
        long allNum = teacherCoursewareContestServiceClient.
                fetchTeacherCoursewareNum(teacherCoursewareParam);
        long pageNum = allNum/PAGE_SIZE + 1;
        for ( int i = 0 ; i < pageNum ; i ++){
            TeacherCoursewarePageInfo teacherCoursewarePageInfo = new TeacherCoursewarePageInfo();
            teacherCoursewarePageInfo.setPageNum(i);
            teacherCoursewarePageInfo.setPageSize(PAGE_SIZE);
            List<TeacherCourseware> teacherCoursewareList =
                    teacherCoursewareContestServiceClient.fetchAllCourseWareListByPage(teacherCoursewarePageInfo);
            if (null != teacherCoursewareList && teacherCoursewareList.size() !=0){
                for (int j = 0;j<teacherCoursewareList.size();j++){
                    TeacherCourseware teacherCourseware = teacherCoursewareList.get(j);
                    String wordUrl = teacherCourseware.getWordUrl();
                    logger.info("old wordUrl :{}", wordUrl);
                    String wordName = teacherCourseware.getWordName();
                    String coursewareId = teacherCourseware.getId();
                    if (null != wordUrl){
                        URL url = null;
                        url = new URL(wordUrl);
                        InputStream input = null;
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        input = conn.getInputStream();
                        String fileName = wordUrl.contains("docx") ? System.currentTimeMillis() + ".docx"
                                : System.currentTimeMillis() + ".doc";
                        String filePath = "test/" + System.currentTimeMillis();
                        String newFilePath = "";
                        try {
                            newFilePath = fileStorageClient.upload(input,fileName,filePath);
                        } catch (Exception e){
                            continue;
                        }
                        String fullPath = "http://v.17xueba.com/" + newFilePath;
                        logger.info("new wordUrl :{}", fullPath);
                        if (!"".equals(newFilePath) && !"null".equals(newFilePath)){
                            teacherCoursewareContestServiceClient.updateNewWordFile(coursewareId,fullPath);
                        }
                    }
                }
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "updateFileName.vpage", method = RequestMethod.POST)
    public MapMessage updateFileName() throws IOException {
        String id = getRequestString("coursewareId");
        String pptName = getRequestString("updatePptName");
        String docName = getRequestString("updateDocName");

        return teacherCoursewareContestServiceClient.updateResourceName(id, pptName, docName);
    }

    @ResponseBody
    @RequestMapping(value = "resavefile.vpage", method = RequestMethod.POST)
    public MapMessage resaveFile(@RequestParam(value = "doc", required = false) MultipartFile file) throws IOException {
        String id = getRequestString("coursewareId");
        String pptUrl = getRequestString("pptUrl");
        String pptName = getRequestString("pptName");

        if (file == null && StringUtils.isEmpty(pptUrl)) {
            return MapMessage.errorMessage("教学课件和教学设计不可同时为空");
        }

        try {
            String fullOssFile = "";
            String originalName = "";
            if (file != null) {
                originalName = file.getOriginalFilename();
                String fileType = originalName.substring(originalName.lastIndexOf(".") + 1);

                if (!Arrays.asList("jpg", "png", "ppt", "pptx", "docx", "doc", "rar", "zip", "gz", "7z").contains(fileType)) {
                    return MapMessage.errorMessage("文件后缀不对,请重新选择后再上传");
                }

                String docfileName = System.currentTimeMillis() + RandomUtils.randomNumeric(5) + "." + fileType;
                String docFielUrl = fileStorageClient.upload(file.getInputStream(), docfileName, "courseware");
                if (StringUtils.isBlank(docFielUrl)) {
                    return MapMessage.errorMessage("服务器异常，请稍后上传");
                }
                fullOssFile = "https://v.17xueba.com/" + docFielUrl;
            }

            MapMessage mapMessage = MapMessage.errorMessage();
            if (StringUtils.isNotEmpty(fullOssFile)) {
                mapMessage = teacherCoursewareContestServiceClient.updateWordFileInfo(id, fullOssFile, originalName);
                if (!mapMessage.isSuccess()) {
                    return mapMessage;
                }
            }
            if (StringUtils.isNotEmpty(pptUrl)) {
                mapMessage = teacherCoursewareContestServiceClient.updateFileUrl(id, pptUrl, pptName);
                if (!mapMessage.isSuccess()) {
                    return mapMessage;
                }
                mapMessage = teacherCoursewareContestServiceClient.updateFileInfo(id, pptUrl, pptName);
            }
            return mapMessage;
        } catch (Exception e) {
            return MapMessage.errorMessage("服务器异常");
        }
    }

    @RequestMapping(value = "getsignature.vpage")
    @ResponseBody
    public MapMessage getSignature(HttpServletRequest request) {
        String ext = getRequestString("ext");
        UploadFileType uploadFileType;
        if (ext != null) {
            uploadFileType = UploadFileType.of(ext);
            if (uploadFileType.equals(UploadFileType.unsupported)) {
                return MapMessage.errorMessage("不支持的数据类型");
            }
        } else uploadFileType = UploadFileType.unsupported;

        MapMessage signatureResult = UploadOssManageUtils.getSignature(uploadFileType, "admin", getResponse());
        if (signatureResult != null)
            return MapMessage.successMessage().add("data", signatureResult);
        return MapMessage.errorMessage();
    }
}
