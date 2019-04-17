package com.voxlearning.utopia.admin.controller.equator.babyeagle;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.BabyEagleSubject;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.BabyEagleTerm;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.BabyEagleType;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.GiftStatus;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.SinologyCourseType;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleClassHour;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleClassHourTemplet;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleCourseInfo;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleCourseKind;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.BabyEagleTeacher;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.StudentLearnCourseRecord;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.StudentLearnCourseRecordForChinaCulture;
import com.voxlearning.utopia.service.wonderland.api.entity.babyeagle.StudentLearnInfo;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleChinaCultureLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleChinaCultureServiceClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleServiceClient;
import com.voxlearning.utopia.service.wonderland.client.WonderlandServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by muzi.qiu on 2017/6/28.
 */

@RequestMapping(value = "/equator/babyeagle")
@Controller
public class BabyeagleManagementController extends AbstractEquatorController {
    @Inject
    private BabyEagleLoaderClient babyEagleLoaderClient;
    @Inject
    private BabyEagleServiceClient babyEagleServiceClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private BabyEagleChinaCultureLoaderClient babyEagleChinaCultureLoaderClient;
    @Inject
    private BabyEagleChinaCultureServiceClient babyEagleChinaCultureServiceClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private UserOrderServiceClient userOrderServiceClient;
    @Inject
    private WonderlandServiceClient wonderlandServiceClient;

    @RequestMapping(value = "courseinfoindex.vpage", method = RequestMethod.GET)
    public String getcourseinfolists(Model model) {
        List<BabyEagleCourseKind> courseKinds = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleCourseKindFromDB().getUninterruptibly();
        sortCourseKindList(courseKinds);
        model.addAttribute("courseKinds", courseKinds);

        String courseKindId = getRequestString("kindId");

        if (StringUtils.isBlank(courseKindId))
            return "equator/babyeagle/courseinfoindex";

        // 获取课程种类
        AlpsFuture<BabyEagleCourseKind> kindAlps = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleCourseKindInfoFromDB(courseKindId);
        // 获取课程种类对应的课程内容列表
        AlpsFuture<List<BabyEagleCourseInfo>> babyEagleCourseInfosAlps = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleCourseInfoByKindIdFromDB(courseKindId);
        // 获取所有老师列表
        AlpsFuture<List<BabyEagleTeacher>> teachersAlps = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleTeachersFromDB();

        BabyEagleCourseKind kind = kindAlps.getUninterruptibly();
        model.addAttribute("courseKind", kind);

        List<BabyEagleCourseInfo> babyEagleCourseInfos = babyEagleCourseInfosAlps.getUninterruptibly();
        List<BabyEagleTeacher> teachersList = teachersAlps.getUninterruptibly();
        Map<String, BabyEagleTeacher> teachersMap = teachersList.stream().collect(Collectors.toMap(BabyEagleTeacher::getId, t -> t));

        // 获取所有课程对应的课时列表Map
        Map<String, AlpsFuture<List<BabyEagleClassHour>>> babyEagleClassHoursAlpsMap = new HashMap<>();
        for (BabyEagleCourseInfo courseInfo : babyEagleCourseInfos) {
            babyEagleClassHoursAlpsMap.put(courseInfo.getId(), babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleClassHourByCourseIdFromDB(courseInfo.getId()));
        }
        // 整理所有课程对应的课时列表
        Map<String, List<BabyEagleClassHour>> babyEagleClassHoursMap = new HashMap<>();
        for (String courseId : babyEagleClassHoursAlpsMap.keySet()) {
            babyEagleClassHoursMap.put(courseId, babyEagleClassHoursAlpsMap.get(courseId).getUninterruptibly());
        }

        List<MapMessage> resultList = new ArrayList<>();

        for (BabyEagleCourseInfo info : babyEagleCourseInfos) {
            List<MapMessage> classHourLists = new ArrayList<>();
            List<BabyEagleClassHour> thisBabyEagleClassHours = babyEagleClassHoursMap.get(info.getId());
            if (CollectionUtils.isNotEmpty(thisBabyEagleClassHours)) {
                thisBabyEagleClassHours = thisBabyEagleClassHours.stream().sorted(Comparator.comparing(BabyEagleClassHour::getStartTime)).collect(Collectors.toList());
                thisBabyEagleClassHours.forEach(hour -> classHourLists.add(new MapMessage()
                        .set("id", hour.getId())
                        .set("courseId", hour.getCourseId())
                        .set("talkFunCourseId", hour.getTalkFunCourseId())
                        .set("startTime", hour.getStartTime())
                        .set("endTime", hour.getEndTime())
                        .set("liveUv", hour.getLiveUv())
                        .set("pbUv", hour.getPbUv())
                        .set("isFinish", hour.isFinish())
                        .set("canPlayBack", hour.canPlayBack())
                        .set("playBackUrl", hour.getPlayBackUrl() == null ? "" : hour.getPlayBackUrl())
                        .set("isUpdatedStudentViewRecord", hour.getIsUpdatedStudentViewRecord())
                        .set("teacherId", hour.getTeacherId())
                        .set("teacherTalkFunId", teachersMap.containsKey(hour.getTeacherId()) ? teachersMap.get(hour.getTeacherId()).getBid() : "")
                        .set("teacherName", teachersMap.containsKey(hour.getTeacherId()) ? teachersMap.get(hour.getTeacherId()).getName() : "")
                        .set("startTimeLong", hour.getStartTime().getTime())
                        .set("isExpire", hour.isExpire())
                ));
            }

            MapMessage result = new MapMessage()
                    .set("courseName", info.getCourseName())
                    .set("courseTypeName", StringUtils.isNotBlank(info.getSinologyCourseType()) ? SinologyCourseType.valueOf(info.getSinologyCourseType()).getTypeName() : "")
                    .set("id", info.getId())
                    .set("kindId", info.getKindId())
                    .set("mode", info.getMode())
                    .set("onlineStatus", fetchOnlineMode() == info.getMode())//上线状态还是线下状态
                    .set("recommendOrder", info.fetchBannerRecommendOrder())
                    .set("isBannerRecommend", info.isBannerRecommend())
                    .set("courseIntro", info.fetchIntroduction())
                    .set("iconUrl", info.fetchImgUrl())
                    .set("sinologyType", info.getSinologyCourseType())
                    .set("classHourLists", classHourLists);

            // 是否已经有开始的课时(课时提前5分钟开始)
            if (CollectionUtils.isNotEmpty(thisBabyEagleClassHours)) {
                result.set("hasClassHours", Boolean.TRUE)
                        .set("hasStarted", thisBabyEagleClassHours.get(0).getStartTime().before(DateUtils.addMinutes(new Date(), 5)) ? Boolean.TRUE : Boolean.FALSE)
                        .set("sort", classHourLists.get(0).get("startTimeLong"));
                resultList.add(result);
            } else {
                result.set("hasClassHours", Boolean.FALSE)
                        .set("hasStarted", Boolean.FALSE)
                        .set("sort", Long.MAX_VALUE);
                resultList.add(result);
            }
        }

        sortCourseInfoList(resultList);
        model.addAttribute("courseInfoList", resultList);
        Mode currentMode = RuntimeMode.current();
        List<Mode> mode = new ArrayList<Mode>();
        if (currentMode.equals(Mode.PRODUCTION)) {
            mode.add(Mode.PRODUCTION);
        } else if (currentMode.equals(Mode.STAGING)) {
            mode.add(Mode.STAGING);
        } else {
            mode.add(Mode.UNIT_TEST);
            mode.add(Mode.DEVELOPMENT);
            mode.add(Mode.TEST);
        }
        model.addAttribute("runModeList", mode);

        List<BabyEagleClassHourTemplet> templets = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleClassHourTemplet().getUninterruptibly();
        model.addAttribute("classhourTemplets", templets);
        model.addAttribute("teachers", teachersList.stream().filter(teacher -> teacher.fetchType().equals(kind.getBabyEagleSubject().getBabyEagleType().name())).collect(Collectors.toList()));
        model.addAttribute("sinologyCourseTypes", Arrays.asList(SinologyCourseType.values()));
        return "equator/babyeagle/courseinfoindex";
    }


    @RequestMapping(value = "addcourseinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage addcourseinfo() {
        ///////////////////////////////////////////////////
        // 这里临时处理国学堂push发送，绕过新权限开通
        Long studentId = getRequestLong("studentId");
        boolean isPush = getRequestBool("isPush");
        if (isPush) {
            if (studentId == 0)
                return MapMessage.errorMessage("用户Id有误");
            String title = getRequestString("title");
            String content = getRequestString("content");
            String url = getRequestString("url");
            if (StringUtils.isBlank(title) || StringUtils.isBlank(content))
                return MapMessage.errorMessage("缺少题目或内容信息");
            postMessage(studentId, title, content, url);
            return MapMessage.successMessage("对[" + studentId + "]用户发送消息成功!");
        }
        //////////////////////////////////////////////

        String kindId = getRequestString("kindId");
        boolean onlineStatus = getRequestBool("onlineStatus");
        String coursename = getRequestString("courseName");

        if (kindId.equals("")) {
            return MapMessage.errorMessage("请您先选择课程种类对课程信息进行筛选！");
        }

        if (StringUtils.isBlank(coursename)) {
            return MapMessage.errorMessage("请输入课程内容名称");
        }
        Mode mode;
        if (onlineStatus) {
            mode = fetchOnlineMode();
        } else {
            mode = fetchNotOnlineMode();
        }

        BabyEagleCourseInfo babyEagleCourseInfo = new BabyEagleCourseInfo();
        babyEagleCourseInfo.setKindId(kindId);
        babyEagleCourseInfo.setMode(mode);
        babyEagleCourseInfo.setCourseName(coursename);

        return babyEagleServiceClient.getRemoteReference().addBabyEagleCourseInfo(babyEagleCourseInfo);
    }

    @RequestMapping(value = "updatecourseinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updatecourseinfo() {
        String id = getRequestString("courseinfoId");
        String kindId = getRequestString("courseKind");
        boolean onlineStatus = getRequestBool("updateOnlineStatus");
        String coursename = getRequestString("courseName");
        String introduction = getRequestString("courseIntro");
        Integer recommenrOrder = getRequestInt("recommend");
        String iconUrl = getRequestString("iconUrl");
        String sinologyType = getRequestString("sinologyType");

        if (StringUtils.isBlank(kindId)) {
            return MapMessage.errorMessage("请您先选择课程种类对课程信息进行筛选！");
        }

        if (StringUtils.isBlank(coursename)) {
            return MapMessage.errorMessage("请输入课程内容名称");
        }

        // 查询该课程下的所有课时
        List<BabyEagleClassHour> babyEagleClassHourList = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleClassHourByCourseIdFromDB(id).getUninterruptibly();

        Mode mode;
        if (onlineStatus) {
            if (CollectionUtils.isEmpty(babyEagleClassHourList))
                return MapMessage.errorMessage("该课程下没有课时不能上线");
            mode = fetchOnlineMode();
        } else {
            mode = fetchNotOnlineMode();
        }

        BabyEagleCourseInfo babyEagleCourseInfo = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleCourseInfoFromDB(id).getUninterruptibly();
        if (babyEagleCourseInfo == null)
            return MapMessage.errorMessage("课程内容不存在");

        String oldCourseName = babyEagleCourseInfo.getCourseName();

        babyEagleCourseInfo.setKindId(kindId);
        babyEagleCourseInfo.setMode(mode);
        babyEagleCourseInfo.setCourseName(coursename);
        babyEagleCourseInfo.setIntroduction(introduction);
        babyEagleCourseInfo.setBannerRecommendOrder(recommenrOrder);

        BabyEagleCourseKind courseKind = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleCourseKindInfoFromDB(kindId).getUninterruptibly();
        BabyEagleSubject subjectKind = courseKind.getBabyEagleSubject();

        if (subjectKind.getBabyEagleType() == BabyEagleType.ChinaCulture && subjectKind.getSubjectName().equals("国学精品课一期")) {
            babyEagleCourseInfo.setSinologyCourseType(sinologyType);
        }

        if (iconUrl != null) {
            babyEagleCourseInfo.setImgUrl(iconUrl);
        }

        MapMessage resultMap = babyEagleServiceClient.getRemoteReference().updateBabyEagleCourseInfo(babyEagleCourseInfo);
        if (resultMap.isSuccess() && !oldCourseName.equals(coursename) && CollectionUtils.isNotEmpty(babyEagleClassHourList)) {
            babyEagleClassHourList.forEach(classHour -> {
                babyEagleServiceClient.getRemoteReference().updateClassHour(classHour);
            });

        }

        return resultMap;
    }

    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadPhoto() {
        String path = getRequestString("path");
        if (StringUtils.isEmpty(path)) return MapMessage.errorMessage("请填写上传路径");
        if (!(getRequest() instanceof MultipartHttpServletRequest)) return MapMessage.errorMessage("上传失败");

        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            MultipartFile inputFile = multipartRequest.getFile("file");
            if (inputFile != null && !inputFile.isEmpty()) {
                String fileName = AdminOssManageUtils.upload(inputFile, path);
                return MapMessage.successMessage(fileName);
            }
        } catch (Exception ignored) {
        }
        return MapMessage.errorMessage("上传失败");
    }

    @RequestMapping(value = "deletecourseinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteCourseInfo() {
        String courseid = getRequestString("courseinfoId");
        if (StringUtils.isBlank(courseid)) {
            return MapMessage.errorMessage("id参数为空");
        }

        BabyEagleCourseInfo courseInfo = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleCourseInfoFromDB(courseid).getUninterruptibly();
        if (courseInfo == null) {
            return MapMessage.errorMessage("课程信息不存在");
        }

        String desc = getRequestString("deletedesc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("删除原因不能为空");
        }

        List<BabyEagleClassHour> classHourList = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleClassHourByCourseIdFromDB(courseid).getUninterruptibly();
        if (CollectionUtils.isNotEmpty(classHourList)) {
            // 确保没有已开始的课时
            BabyEagleClassHour startedClassHour = classHourList.stream().filter(classHour -> classHour.getStartTime() != null && classHour.getStartTime().before(DateUtils.addMinutes(new Date(), 5))).findFirst().orElse(null);
            if (startedClassHour != null)
                return MapMessage.errorMessage("课程第一节课时已经开始，不能删除");

            // 课时全部未开始时，一并删除
            classHourList.forEach(classHour -> {
                babyEagleServiceClient.getRemoteReference().deleteClassHour(classHour.getId());
            });

        }

        return babyEagleServiceClient.getRemoteReference().deleteBabyEagleCourseInfo(courseid);
    }

    @RequestMapping(value = "addclasshourbymodel.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage addClassHourByModel() {
        String teacherId = getRequestString("teacherId");
        String courseId = getRequestString("courseId");
        String templetId = getRequestString("templetId");
        String date1 = getRequestString("date1");
        String date2 = getRequestString("date2");
        String date3 = getRequestString("date3");
        String date4 = getRequestString("date4");
        String date5 = getRequestString("date5");

        List<String> dates = new ArrayList<>();
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);
        dates.add(date5);
        BabyEagleClassHourTemplet templet = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleClassHourTemplet(templetId).getUninterruptibly();
        List<String> classHours = templet.getClassHourList();

        if (classHours == null) {
            return MapMessage.errorMessage().setInfo("该课时设定模板没有设置时间");
        }

        int success = 0;
        int fail = 0;
        String info = "";
        try {
            for (String classhour : classHours) {
                String[] times = classhour.split("-");
                String starttime = times[0];
                String endtime = times[1];

                for (String date : dates) {
                    if (date.equals("")) continue;
                    String startTime1 = date + " " + starttime;
                    String endTime1 = date + " " + endtime;

                    BabyEagleClassHour classHour = new BabyEagleClassHour();
                    classHour.setCourseId(courseId);
                    classHour.setTeacherId(teacherId);
                    classHour.setStartTime(DateUtils.stringToDate(startTime1, "yyyy-MM-dd HH:mm"));
                    classHour.setEndTime(DateUtils.stringToDate(endTime1, "yyyy-MM-dd HH:mm"));
                    MapMessage mapMessage = babyEagleServiceClient.getRemoteReference().addClassHour(classHour);

                    if (mapMessage.isSuccess()) {
                        success = success + 1;
                    } else {
                        info = info + " " + mapMessage.getInfo();
                        fail = fail + 1;
                    }

                }
            }
        } catch (Exception e) {
            return new MapMessage().setInfo("成功添加了" + success + "个课时" + fail + "个课时添加失败了\n" + info);
        }
        return new MapMessage().setInfo("成功添加了" + success + "个课时" + fail + "个课时添加失败了\n" + info);

    }

    @RequestMapping(value = "addclasshour.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage addClassHour() {
        String teacherId = getRequestString("teacherId");
        String courseId = getRequestString("courseId");
        Date startTime = DateUtils.stringToDate(getRequestString("startTime"), "yyyy/MM/dd HH:mm");
        Date endTime = DateUtils.stringToDate(getRequestString("endTime"), "yyyy/MM/dd HH:mm");

        BabyEagleClassHour classHour = new BabyEagleClassHour();
        classHour.setCourseId(courseId);
        classHour.setTeacherId(teacherId);
        classHour.setStartTime(startTime);
        classHour.setEndTime(endTime);

        return babyEagleServiceClient.getRemoteReference().addClassHour(classHour);
    }

    @RequestMapping(value = "editclasshour.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage editClassHour() {
        String classhourId = getRequestString("classhourId");

        BabyEagleClassHour classHour = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleClassHourFromDB(classhourId).getUninterruptibly();

        if (classHour == null) {
            return MapMessage.errorMessage().setInfo("不存在此课时");
        }

        String updateType = getRequestString("updateType");
        if (updateType.equals("local")) {
            Boolean isExpire = getRequestBool("isExpire");
            classHour.setIsExpire(isExpire);
            return babyEagleServiceClient.getRemoteReference().updateClassHourForLocal(classHour);
        } else {
            String teacherId = getRequestString("teacherId");
            Date startTime = DateUtils.stringToDate(getRequestString("startTime"), "yyyy/MM/dd HH:mm");
            Date endTime = DateUtils.stringToDate(getRequestString("endTime"), "yyyy/MM/dd HH:mm");

            classHour.setTeacherId(teacherId);
            classHour.setStartTime(startTime);
            classHour.setEndTime(endTime);

            return babyEagleServiceClient.getRemoteReference().updateClassHour(classHour);
        }

    }

    @RequestMapping(value = "deleteclasshour.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteClasshour() {
        String classHourId = getRequestString("classhourId");
        if (StringUtils.isBlank(classHourId)) {
            return MapMessage.errorMessage("id参数为空");
        }

        BabyEagleClassHour classHour = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleClassHourFromDB(classHourId).getUninterruptibly();
        if (classHour == null) {
            return MapMessage.errorMessage("课时不存在");
        }

        if (classHour.getStartTime() != null && classHour.getStartTime().before(DateUtils.addMinutes(new Date(), 5)))
            return MapMessage.errorMessage("课时已经开始，不能删除");

        String desc = getRequestString("deletedesc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("删除原因不能为空");
        }

        return babyEagleServiceClient.getRemoteReference().deleteClassHour(classHourId);
    }

    @RequestMapping(value = "teacherindex.vpage", method = RequestMethod.GET)
    public String teacherIndex(Model model) {

        List<BabyEagleTeacher> teachers = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleTeachersFromDB().getUninterruptibly();
        teachers.forEach(teacher -> {
            if (StringUtils.isBlank(teacher.getType()))
                teacher.setType(BabyEagleType.BaseSchool.name());
        });
        model.addAttribute("teachers", teachers);

        return "equator/babyeagle/teacherinfo";
    }

    @RequestMapping(value = "addteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addTeacher() {
        String teacherName = getRequestString("teacherName");
        BabyEagleType teacherType = BabyEagleType.safeParse(getRequestString("teacherType"));
        String teacherEmail = getRequestString("teacherEmail");
        String teacherPasswd1 = getRequestString("teacherPasswd1");
        String teacherPasswd2 = getRequestString("teacherPasswd2");
        String teacherIntro = getRequestString("teacherIntro");
        if (StringUtils.isBlank(teacherName)) {
            return MapMessage.errorMessage("姓名不能为空");
        }
        if (teacherName.length() > 12) {
            return MapMessage.errorMessage("姓名长度不能超过12");
        }
        if (StringUtils.isBlank(teacherPasswd1)) {
            return MapMessage.errorMessage("密码不能为空");
        }
        if (!StringUtils.equals(teacherPasswd1, teacherPasswd2)) {
            return MapMessage.errorMessage("两次输入密码不同");
        }
        if (StringUtils.isNotBlank(teacherEmail) && teacherEmail.length() > 30) {
            return MapMessage.errorMessage("邮件长度不能超过30");
        }
        if (StringUtils.isNotBlank(teacherIntro) && teacherIntro.length() > 40) {
            return MapMessage.errorMessage("简介不能超过40");
        }

        BabyEagleTeacher teacher = new BabyEagleTeacher();
        teacher.setName(teacherName);
        teacher.setType(teacherType.name());
        teacher.setPassword(teacherPasswd1);
        teacher.setEmail(teacherEmail);
        teacher.setIntro(teacherIntro);

        return babyEagleServiceClient.getRemoteReference().addBabyEagleTeacherInfo(teacher);
    }

    @RequestMapping(value = "updateteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateTeacher() {
        String id = getRequestString("id");
        String name = getRequestString("name");
        String passwd1 = getRequestString("passwd1");
        String passwd2 = getRequestString("passwd2");

        String email = getRequestString("email");
        String intro = getRequestString("intro");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数id不能为空");
        }
        if (StringUtils.isBlank(name)) {
            return MapMessage.errorMessage("姓名不能为空");
        }
        if (name.length() > 12) {
            return MapMessage.errorMessage("姓名长度不能超过12");
        }
        if (!StringUtils.equals(passwd1, passwd2)) {
            return MapMessage.errorMessage("两次输入的密码必须相同,如果不想修改,请不要填写");
        }

        BabyEagleTeacher teacher = new BabyEagleTeacher();
        teacher.setId(id);
        if (StringUtils.isNotBlank(name)) {
            teacher.setName(name);
        }
        if (StringUtils.isNotBlank(passwd1)) {
            teacher.setPassword(passwd1);
        }
        if (StringUtils.isNotBlank(email)) {
            teacher.setEmail(email);
        }
        if (StringUtils.isNotBlank(intro)) {
            teacher.setIntro(intro);
        }

        return babyEagleServiceClient.getRemoteReference().updateBabyEagleTeacherInfo(teacher);
    }

    @RequestMapping(value = "deleteteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTteacher() {
        String id = getRequestString("teacherId");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("id参数为空");
        }
        String desc = getRequestString("desc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("删除原因不能为空");
        }
        BabyEagleTeacher babyEagleTeacher = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleTeacherInfoFromDB(id).getUninterruptibly();
        if (babyEagleTeacher == null) {
            return MapMessage.errorMessage("指定老师信息不存在");
        }

        return babyEagleServiceClient.getRemoteReference().deleteBabyEagleTeacherInfo(id);
    }

    @RequestMapping(value = "/coursekindindex.vpage", method = RequestMethod.GET)
    public String coursekindindex(Model model) {
        List<BabyEagleCourseKind> courseKindList = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleCourseKindFromDB().getUninterruptibly();
        List<ClazzLevel> clazzLevels = Arrays.stream(ClazzLevel.values()).filter(p -> (p.getLevel() >= 1 && p.getLevel() <= 6) || p == ClazzLevel.PRIVATE_GRADE).collect(Collectors.toList());
        //课程列表按照学科 年级 学期3个维度进行排序
        sortCourseKindList(courseKindList);

        List<Map<String, Object>> courseKindMapList = new LinkedList<>();
        for (BabyEagleCourseKind babyEagleCourseKind : courseKindList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", babyEagleCourseKind.getId());
            map.put("babyEagleSubject", babyEagleCourseKind.getBabyEagleSubject());
            map.put("clazzLevel", babyEagleCourseKind.getClazzLevel());
            map.put("babyEagleTerm", babyEagleCourseKind.getBabyEagleTerm());
            map.put("mode", babyEagleCourseKind.getMode());
            map.put("onlineStatus", fetchOnlineMode() == babyEagleCourseKind.getMode());//上线状态还是线下状态
            courseKindMapList.add(map);
        }

        Mode currentMode = RuntimeMode.current();
        List<Mode> mode = new ArrayList<>();
        if (currentMode.equals(Mode.PRODUCTION)) {
            mode.add(Mode.PRODUCTION);
        } else if (currentMode.equals(Mode.STAGING)) {
            mode.add(Mode.STAGING);
        } else {
            mode.add(Mode.UNIT_TEST);
            mode.add(Mode.DEVELOPMENT);
            mode.add(Mode.TEST);
        }

        model.addAttribute("courseKindList", courseKindMapList);
        model.addAttribute("babyeagleSubject", new ArrayList<>(Arrays.asList(BabyEagleSubject.values())));
        model.addAttribute("classLevels", clazzLevels);
        model.addAttribute("babyeagleTermList", new ArrayList<>(Arrays.asList(BabyEagleTerm.values())));
        model.addAttribute("runModeList", mode);

        return "equator/babyeagle/coursekindindex";
    }

    @RequestMapping(value = "addcoursekind.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage addcoursekind() {
        BabyEagleSubject subject = BabyEagleSubject.valueOf(getRequestString("subject"));
        ClazzLevel level = ClazzLevel.valueOf(getRequestString("clazzLevel"));
        BabyEagleTerm term = BabyEagleTerm.valueOf(getRequestString("term"));
        boolean onlineStatus = getRequestBool("onlineStatus");
        Mode mode;
        if (onlineStatus) {
            mode = fetchOnlineMode();
        } else {
            mode = fetchNotOnlineMode();
        }

        if (mode == null) {
            return MapMessage.errorMessage("参数有误！");
        }

        //语数英对应的是1-6年级上下学期
        if (Arrays.asList(BabyEagleSubject.CHINESE, BabyEagleSubject.MATH, BabyEagleSubject.ENGLISH).contains(subject)) {
            if (!Arrays.stream(ClazzLevel.values()).filter(l -> l.getLevel() >= 1 && l.getLevel() <= 6).collect(Collectors.toList()).contains(level)) {
                return MapMessage.errorMessage("语文数学英语只能选择1-6年级上下学期");
            }
            if (term == BabyEagleTerm.ALL_TERM) {
                return MapMessage.errorMessage("语文数学英语只能选择1-6年级上下学期");
            }
        } else if (subject == BabyEagleSubject.ENCYCLOPEDIA || subject == BabyEagleSubject.ENCYCLOPEDIA_HIGH) {
            //校验百科对应的是全年级全学期
            if (level != ClazzLevel.PRIVATE_GRADE || term != BabyEagleTerm.ALL_TERM) {
                return MapMessage.errorMessage("百科对应的是全年级全学期");
            }
        }

        BabyEagleCourseKind courseKind = new BabyEagleCourseKind();
        courseKind.setBabyEagleSubject(subject);
        courseKind.setBabyEagleTerm(term);
        courseKind.setClazzLevel(level);
        courseKind.setMode(mode);

        return babyEagleServiceClient.getRemoteReference().addBabyEagleCourseKindInfo(courseKind);
    }

    @RequestMapping(value = "updatecoursekind.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage updatecoursekind() {
        String id = getRequestString("id");
        boolean onlineStatus = getRequestBool("onlineStatus");
        Mode mode;
        if (onlineStatus) {
            mode = fetchOnlineMode();
        } else {
            mode = fetchNotOnlineMode();
        }

        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("id参数有误！");
        }
        if (mode == null) {
            return MapMessage.errorMessage("mode参数有误！");
        }

        BabyEagleCourseKind courseKind = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleCourseKindInfoFromDB(id).getUninterruptibly();
        if (courseKind == null)
            return MapMessage.errorMessage("课程种类不存在");

        courseKind.setMode(mode);

        return babyEagleServiceClient.getRemoteReference().updateBabyEagleCourseKindInfo(courseKind);
    }

    @RequestMapping(value = "studentlearninfo.vpage", method = RequestMethod.GET)
    public String getStudentlearninfo(Model model) {
        Long studentId = getRequestLong("studentId");

        if (studentId == 0)
            return "equator/babyeagle/studentlearninfo";

        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) {
            model.addAttribute("studentId", "");
            model.addAttribute("studentName", "");
        } else {
            model.addAttribute("studentId", studentId);
            model.addAttribute("studentName", student.fetchRealname());

        }

        // 获取听课卡数量
        AlpsFuture<Long> timesCardCountAlps = babyEagleLoaderClient.getBabyEagleLoader().getStudentTimesCardTotal(studentId);

        // 获取国学体验券数量(3.22将原国学券改为国学课体验券)
        AlpsFuture<Long> sinologyCardCountAlps = babyEagleChinaCultureLoaderClient.getRemoteReference().getStudentSinologyCardTotal(studentId);

        // 获取学习信息
        AlpsFuture<StudentLearnInfo> learnInfoAlps = babyEagleLoaderClient.getBabyEagleLoader().loadStudentLearnInfo(studentId);

        // 获取学生购买过的产品
        Set<OrderProductServiceType> orderProductServiceTypes = userOrderLoaderClient.loadUserActivatedProductList(studentId)
                .stream()
                .filter(p -> p.getServiceEndTime() != null && p.getServiceEndTime().getTime() > System.currentTimeMillis())
                .map(o -> OrderProductServiceType.safeParse(o.getProductServiceType()))
                .collect(Collectors.toSet());

        // 是否开通国学精品课程
        Boolean isSinologyVip = orderProductServiceTypes.contains(OrderProductServiceType.EagletSinologyClassRoom);

        // 获取学生听课列表
        AlpsFuture<List<StudentLearnCourseRecord>> courseRecordListAlps = babyEagleLoaderClient.getBabyEagleLoader().loadStudentLearnCourseRecordList(studentId);
        AlpsFuture<List<StudentLearnCourseRecordForChinaCulture>> courseRecordChinaCultureList = babyEagleChinaCultureLoaderClient.getRemoteReference().loadStudentLearnCourseRecordList(studentId);

        Long timesCardCount = timesCardCountAlps.getUninterruptibly();
        Long sinologyCardCount = sinologyCardCountAlps.getUninterruptibly();
        StudentLearnInfo learnInfo = learnInfoAlps.getUninterruptibly();
        model.addAttribute("learnInfo", learnInfo);
        model.addAttribute("timesCardCount", timesCardCount);
        model.addAttribute("sinologyCardCount", sinologyCardCount);
        model.addAttribute("isSinologyVip", isSinologyVip);

        List<StudentLearnCourseRecord> learnCourseRecords = courseRecordListAlps.getUninterruptibly();
        List<StudentLearnCourseRecordForChinaCulture> learnCourseRecordForChinaCultures = courseRecordChinaCultureList.getUninterruptibly();
        //国学听课记录
        if (CollectionUtils.isNotEmpty(learnCourseRecordForChinaCultures)) {
            // 按照购买时间排序
            learnCourseRecordForChinaCultures = learnCourseRecordForChinaCultures.stream().sorted(Comparator.comparing(StudentLearnCourseRecordForChinaCulture::getCt)).collect(Collectors.toList());

            List<String> courseIds = learnCourseRecordForChinaCultures.stream().map(StudentLearnCourseRecordForChinaCulture::getCourseId).collect(Collectors.toList());

            AlpsFuture<Map<String, BabyEagleCourseInfo>> babyEagleCourseInfoAlpsFuture = babyEagleLoaderClient.getBabyEagleLoader().loadBabyEagleCourseInfosFromBuffer(courseIds);
            AlpsFuture<List<BabyEagleCourseKind>> babyEagleCourseKinds = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleCourseKindFromDB();
            Map<String, BabyEagleCourseInfo> idInfoMap = babyEagleCourseInfoAlpsFuture.getUninterruptibly();
            Map<String, BabyEagleCourseKind> courseKindMap = babyEagleCourseKinds.getUninterruptibly()
                    .stream().collect(Collectors.toMap(BabyEagleCourseKind::getId, Function.identity()));

            List<MapMessage> chinaculturelearnrecordViews = new ArrayList<>();
            learnCourseRecordForChinaCultures.forEach(record -> chinaculturelearnrecordViews.add(new MapMessage()
                            .set("id", record.getId())
                            .set("courseKind", courseKindMap.get(record.getKindId()))
                            .set("courseInfo", idInfoMap.get(record.getCourseId()))
                            .set("classHourId", record.getClassHourId())
                            .set("duration", record.getDuration())
                            .set("giftStatus", record.getGiftStatus())
                            .set("ct", record.getCt())
                    )
            );

            model.addAttribute("ChinaCulturelearnCourseRecords", chinaculturelearnrecordViews);
        } else {
            model.addAttribute("ChinaCulturelearnCourseRecords", new ArrayList<MapMessage>());
        }
        //普通课程听课记录
        if (CollectionUtils.isNotEmpty(learnCourseRecords)) {
            // 按照购买时间排序
            learnCourseRecords = learnCourseRecords.stream().sorted(Comparator.comparing(StudentLearnCourseRecord::getCt)).collect(Collectors.toList());

            List<String> courseIds = learnCourseRecords.stream().map(StudentLearnCourseRecord::getCourseId).collect(Collectors.toList());

            AlpsFuture<Map<String, BabyEagleCourseInfo>> babyEagleCourseInfoAlpsFuture = babyEagleLoaderClient.getBabyEagleLoader().loadBabyEagleCourseInfosFromBuffer(courseIds);
            AlpsFuture<List<BabyEagleCourseKind>> babyEagleCourseKinds = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleCourseKindFromDB();

            Map<String, BabyEagleCourseInfo> idInfoMap = babyEagleCourseInfoAlpsFuture.getUninterruptibly();
            Map<String, BabyEagleCourseKind> courseKindMap = babyEagleCourseKinds.getUninterruptibly()
                    .stream().collect(Collectors.toMap(BabyEagleCourseKind::getId, Function.identity()));


            List<MapMessage> learnRecordsview = new ArrayList<>();
            learnCourseRecords.forEach(record -> learnRecordsview.add(new MapMessage()
                            .set("id", record.getId())
                            .set("courseKind", courseKindMap.get(record.getKindId()))
                            .set("courseInfo", idInfoMap.get(record.getCourseId()))
                            .set("classHourId", record.getClassHourId())
                            .set("replaceClassHourId", record.getReplaceClassHourId() == null ? "" : record.getReplaceClassHourId())
                            .set("isLive", record.getIsLive())
                            .set("duration", record.getDuration())
                            .set("starNum", record.getStarNum())
                            .set("giftStatus", record.getGiftStatus())
                            .set("ct", record.getCt())
                            .set("liveUrl", record.getLiveUrl())
                            .set("playbackUrl", record.getPlaybackUrl())
                    )
            );

            model.addAttribute("learnCourseRecords", learnRecordsview);
        } else {
            model.addAttribute("learnCourseRecords", new ArrayList<MapMessage>());
        }
        model.addAttribute("giftStatusList", Arrays.asList(GiftStatus.values()));
        model.addAttribute("isLiveList", Arrays.asList(Boolean.TRUE, Boolean.FALSE));
        return "equator/babyeagle/studentlearninfo";
    }

    @RequestMapping(value = "addtimesCard.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addtimesCard() {
        Integer timesCard = getRequestInt("addtimesCard");
        Long studentId = getRequestLong("studentId");

        if (timesCard > 100) {
            return MapMessage.errorMessage("单次增加听课卡数量不能超过100");
        }

        if (timesCard > 0) {
            return babyEagleServiceClient.getRemoteReference().addTimesCard(studentId, new Long(timesCard));
        } else {
            return MapMessage.successMessage("增加听课卡失败，请输入正数！");
        }
    }

    @RequestMapping(value = "deductiontimescard.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deductiontimesCard() {
        Integer timesCard = getRequestInt("addtimesCard");
        Long studentId = getRequestLong("studentId");

        if (timesCard >= 0) {
            return MapMessage.errorMessage("扣除听课卡失败，请输入负数！");
        } else {
            // 扣除听课卡逻辑
            MapMessage timesCardChangeMap = wonderlandServiceClient.getWonderlandService().changeWonderlandTimesCard(studentId, OrderProductServiceType.ValueAddedLiveTimesCard.name(), timesCard);
            if (!timesCardChangeMap.isSuccess()) {
                return timesCardChangeMap;
            }

            // 用户订单消耗更新
            try {
                MapMessage mapMessage = userOrderServiceClient.consumeItemCard(studentId, OrderProductServiceType.ValueAddedLiveTimesCard, (0 - timesCard), "0");
                if (!mapMessage.isSuccess()) {
                    logger.warn("userOrderServiceClient.consumeItemCard 扣除听课卡记录 ValueAddedLiveTimesCard: {},{},{}", studentId, (0 - timesCard), "0");
                }
            } catch (Exception e) {
                logger.warn("userOrderServiceClient.consumeItemCard 扣除听课卡记录 ValueAddedLiveTimesCard: {},{},{}", studentId, (0 - timesCard), "0");
            }
            return MapMessage.successMessage("扣除听课卡成功");
        }
    }

    @RequestMapping(value = "deletelearnrecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deletelearnrecord() {
        String recordId = getRequestString("recordId");
        String desc = getRequestString("desc");
        String type = getRequestString("type");

        if (desc == null || desc.equals("")) {
            return MapMessage.errorMessage("删除原因不能够为空");
        }

        if (type.equals("culture")) {
            StudentLearnCourseRecordForChinaCulture record = babyEagleChinaCultureLoaderClient.getRemoteReference().getStudentLearnCourseRecord(recordId).getUninterruptibly();
            if (record == null) {
                return MapMessage.errorMessage("不存在指定记录");
            }
            return babyEagleChinaCultureServiceClient.getRemoteReference().deleteStudentLearnCourseRecord(recordId);
        } else {
            StudentLearnCourseRecord record = babyEagleLoaderClient.getBabyEagleLoader().getStudentLearnCourseRecord(recordId).getUninterruptibly();
            if (record == null) {
                return MapMessage.errorMessage("不存在指定记录");
            }
            return babyEagleServiceClient.getRemoteReference().deleteStudentLearnCourseRecord(recordId);
        }
    }

    @RequestMapping(value = "editlearnrecord.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage editLearnRecord() {
        String recordId = getRequestString("recordId");
        Boolean isLive = getRequestBool("isLive");
        Integer duration = getRequestInt("duration");
        Integer starNum = getRequestInt("starNum");
        GiftStatus giftStatus = GiftStatus.valueOf(getRequestString("giftStatus"));

        StudentLearnCourseRecord learnCourseRecord = babyEagleLoaderClient.getBabyEagleLoader().getStudentLearnCourseRecord(recordId).getUninterruptibly();
        if (learnCourseRecord == null)
            return MapMessage.errorMessage("不存在指定记录");

        learnCourseRecord.setIsLive(isLive);
        learnCourseRecord.setDuration(duration);
        learnCourseRecord.setStarNum(starNum);
        learnCourseRecord.setGiftStatus(giftStatus);

        return babyEagleServiceClient.getRemoteReference().updateStudentLearnCourseRecord(learnCourseRecord);
    }

    @RequestMapping(value = "studentlearnrecordbyclasshour.vpage", method = RequestMethod.GET)
    public String getLearnRecordsbyClassHour(Model model) {
        String classHourId = getRequestString("classHourId");
        String courseId = getRequestString("courseId");

        BabyEagleCourseInfo courseInfo = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleCourseInfoFromDB(courseId).getUninterruptibly();
        BabyEagleClassHour classHour = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleClassHourFromDB(classHourId).getUninterruptibly();
        BabyEagleCourseKind courseKind = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleCourseKindInfoFromDB(courseInfo.getKindId()).getUninterruptibly();
        List<BabyEagleTeacher> teacherList = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleTeachersFromDB().getUninterruptibly();
        Map<String, BabyEagleTeacher> teacherMap = teacherList.stream().collect(Collectors.toMap(BabyEagleTeacher::getId, Function.identity()));

        model.addAttribute("courseName", courseInfo.getCourseName());
        model.addAttribute("courseKind", courseKind.getBabyEagleSubject().getSubjectName() + " " + courseKind.getClazzLevel().getDescription() + " " + courseKind.getBabyEagleTerm().getTermName());
        model.addAttribute("classHourId", classHourId);
        model.addAttribute("StartTime", classHour.getStartTime());
        model.addAttribute("EndTime", classHour.getEndTime());
        model.addAttribute("TeacherName", teacherMap.containsKey(classHour.getTeacherId()) ? teacherMap.get(classHour.getTeacherId()).getName() : "");

        List<MapMessage> classHourLists = new ArrayList<>();
        List<BabyEagleClassHour> babyEagleClassHours = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleClassHourByCourseIdFromDB(courseId).getUninterruptibly();
        Map<String, BabyEagleClassHour> classHourMap = babyEagleClassHours.stream().collect(Collectors.toMap(BabyEagleClassHour::getId, c -> c));

        // 课时列表
        babyEagleClassHours.stream().sorted(Comparator.comparing(BabyEagleClassHour::getStartTime)).forEach(hour -> classHourLists.add(new MapMessage()
                .set("id", hour.getId())
                .set("courseId", hour.getCourseId())
                .set("talkFunCourseId", hour.getTalkFunCourseId())
                .set("startTime", hour.getStartTime())
                .set("endTime", hour.getEndTime())
                .set("liveUv", hour.getLiveUv())
                .set("pbUv", hour.getPbUv())
                .set("canPlayBack", hour.getCanPlayBack() == null ? false : hour.getCanPlayBack())
                .set("playBackUrl", hour.getPlayBackUrl() == null ? "" : hour.getPlayBackUrl())
                .set("isUpdatedStudentViewRecord", hour.getIsUpdatedStudentViewRecord())
                .set("teacherId", hour.getTeacherId())
                .set("teacherName", teacherMap.containsKey(hour.getTeacherId()) ? teacherMap.get(hour.getTeacherId()).getName() : "")
                .set("teacherTalkFunId", teacherMap.containsKey(hour.getTeacherId()) ? teacherMap.get(hour.getTeacherId()).getBid() : "")
        ));
        model.addAttribute("classHourLists", classHourLists);

        if (courseKind.getBabyEagleSubject().getBabyEagleType() == BabyEagleType.BaseSchool) {
            // 购课记录列表
            List<StudentLearnCourseRecord> learnCourseRecords = babyEagleLoaderClient.getBabyEagleLoader().loadStudentLearnCourseRecordListByClassHourId(classHourId).getUninterruptibly();
            model.addAttribute("recordSize", learnCourseRecords.size());
            if (learnCourseRecords.size() > 100) {
                // 显示两天之内的购课详情
//                learnCourseRecords = learnCourseRecords.stream().filter(record -> record.getCt().after(DateUtils.addDays(new Date(), -2))).collect(Collectors.toList());
                // 显示最近100条
                learnCourseRecords = learnCourseRecords.subList(0, 100);
            }

            List<Long> studentIds = new ArrayList<>();
            learnCourseRecords.forEach(l -> studentIds.add(l.getStudentId()));
            Map<Long, Student> studentDetails = studentLoaderClient.loadStudents(studentIds);

            List<MapMessage> learnRecordsView = new ArrayList<>();
            learnCourseRecords.stream().sorted(Comparator.comparing(StudentLearnCourseRecord::getCt)).forEach(record -> learnRecordsView.add(new MapMessage()
                    .set("id", record.getId())
                    .set("studentId", record.getStudentId())
                    .set("replaceClassHour", classHourMap.containsKey(record.getReplaceClassHourId()) ? classHourMap.get(record.getReplaceClassHourId()) : null)
                    .set("isLive", record.getIsLive())
                    .set("duration", record.getDuration())
                    .set("starNum", record.getStarNum())
                    .set("giftStatus", record.getGiftStatus())
                    .set("ct", record.getCt())
                    .set("studentName", studentDetails.containsKey(record.getStudentId()) ? studentDetails.get(record.getStudentId()).fetchRealname() : "")
            ));
            model.addAttribute("studentLearnRecords", learnRecordsView);
        }
        //国学课程记录
        else if (courseKind.getBabyEagleSubject().getBabyEagleType() == BabyEagleType.ChinaCulture) {
            // 购课记录列表
            List<StudentLearnCourseRecordForChinaCulture> chinaculturelearnCourseRecords = babyEagleChinaCultureLoaderClient.getRemoteReference().loadStudentLearnCourseRecordListByClassHourId(classHourId).getUninterruptibly();
            model.addAttribute("chinaculturerecordSize", chinaculturelearnCourseRecords.size());
            if (chinaculturelearnCourseRecords.size() > 100) {
                // 显示两天之内的购课详情
//                chinaculturelearnCourseRecords = chinaculturelearnCourseRecords.stream().filter(record -> record.getCt().after(DateUtils.addDays(new Date(), -2))).collect(Collectors.toList());
                // 显示最近100条
                chinaculturelearnCourseRecords = chinaculturelearnCourseRecords.subList(0, 100);
            }

            List<Long> studentIds = new ArrayList<>();
            chinaculturelearnCourseRecords.forEach(l -> studentIds.add(l.getStudentId()));
            Map<Long, Student> studentDetails = studentLoaderClient.loadStudents(studentIds);

            List<MapMessage> chinaculturelearnRecordsView = new ArrayList<>();
            chinaculturelearnCourseRecords.stream().sorted(Comparator.comparing(StudentLearnCourseRecordForChinaCulture::getCt)).forEach(record -> chinaculturelearnRecordsView.add(new MapMessage()
                    .set("id", record.getId())
                    .set("studentId", record.getStudentId())
                    .set("duration", record.getDuration())
                    .set("giftStatus", record.getGiftStatus().getStatusName())
                    .set("ct", record.getCt())
                    .set("studentName", studentDetails.containsKey(record.getStudentId()) ? studentDetails.get(record.getStudentId()).fetchRealname() : "")
            ));
            model.addAttribute("chinaculturestudentLearnRecords", chinaculturelearnRecordsView);
        }

        return "equator/babyeagle/classhourlearnrecord";
    }

    @RequestMapping(value = "addreplaceclasshour.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage addreplaceClassHour() {
        String recordId = getRequestString("recordId");
        String replaceClassHourId = getRequestString("replaceClassHourId");

        StudentLearnCourseRecord learnCourseRecord = babyEagleLoaderClient.getBabyEagleLoader().getStudentLearnCourseRecord(recordId).getUninterruptibly();
        if (learnCourseRecord == null)
            return MapMessage.errorMessage("不存在指定记录");

        learnCourseRecord.setReplaceClassHourId(replaceClassHourId);

        return babyEagleServiceClient.getRemoteReference().updateStudentLearnCourseRecord(learnCourseRecord);
    }

    @RequestMapping(value = "classhourmodel.vpage", method = RequestMethod.GET)
    public String classhourmodel(Model model) {
        List<BabyEagleClassHourTemplet> babyEagleClassHourTempletss = babyEagleLoaderClient.getBabyEagleLoader().loadAllBabyEagleClassHourTemplet().getUninterruptibly();
        model.addAttribute("classhourmodelList", babyEagleClassHourTempletss);
        return "equator/babyeagle/classhourmodel";
    }

    @RequestMapping(value = "addclasshourmodel.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage addclasshourmodel() {
        String name = getRequestString("modelname");

        BabyEagleClassHourTemplet templet = new BabyEagleClassHourTemplet();
        templet.setTempletName(name);
        return babyEagleServiceClient.getRemoteReference().addClassHourTemplet(templet);
    }

    @RequestMapping(value = "deleteclasshourmodel.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteclasshourmodel() {
        String templetId = getRequestString("templetId");

        BabyEagleClassHourTemplet templet = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleClassHourTemplet(templetId).getUninterruptibly();

        if (templet == null) {
            return MapMessage.errorMessage().setInfo("没有此课时模板");
        }
        return babyEagleServiceClient.getRemoteReference().delClassHourTemplet(templetId);
    }

    @RequestMapping(value = "addmodelclasshour.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage addmodelclasshour() throws ParseException {
        String modelId = getRequestString("modelId");
        String startTime = getRequestString("startTime");
        String endTime = getRequestString("endTime");

        if (startTime.equals("") || endTime.equals("")) {
            return MapMessage.errorMessage().setInfo("缺少开始时间或结束时间");
        }
        String time = startTime + "-" + endTime;
        BabyEagleClassHourTemplet templet = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleClassHourTemplet(modelId).getUninterruptibly();

        if (templet == null) {
            return MapMessage.errorMessage().setInfo("不存在此模板");
        }

        List<String> classhourList = templet.getClassHourList();

        if (classhourList == null) {
            classhourList = new ArrayList<>();
        }
        classhourList.add(time);
        sortModelclassHours(classhourList);
        templet.setClassHourList(classhourList);

        return babyEagleServiceClient.getRemoteReference().updateClassHourTemplet(templet);
    }

    @RequestMapping(value = "deletemodelclasshour.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deletemodelclasshour() {
        String modelId = getRequestString("modelId");
        String startTime = getRequestString("startTime");
        String endTime = getRequestString("endTime");

        String time = startTime + "-" + endTime;
        BabyEagleClassHourTemplet templet = babyEagleLoaderClient.getBabyEagleLoader().getBabyEagleClassHourTemplet(modelId).getUninterruptibly();
        List<String> classhourlists = templet.getClassHourList();
        if (classhourlists.remove(time)) {
            return babyEagleServiceClient.getRemoteReference().updateClassHourTemplet(templet);
        } else {
            return MapMessage.errorMessage().setInfo("失败了！");
        }
    }

    //针对学科种类 学科内容,当前环境是测试时：test表示为上线状态,develop表示为下线状态
    //当前环境是staging或production时：production表示为线上状态,staging表示为线下状态
    public Mode fetchOnlineMode() {//获取当前环境对应的线上状态
        Mode currentMode = RuntimeMode.current();
        //当前环境是staging或production时
        if (currentMode == Mode.STAGING || currentMode == Mode.PRODUCTION) {
            return Mode.PRODUCTION;
        } else {
            return Mode.TEST;
        }
    }

    public Mode fetchNotOnlineMode() {//获取当前环境对应的线下状态
        Mode currentMode = RuntimeMode.current();
        if (currentMode == Mode.STAGING || currentMode == Mode.PRODUCTION) {
            return Mode.STAGING;
        } else {
            return Mode.DEVELOPMENT;
        }
    }

    private void sortCourseKindList(List<BabyEagleCourseKind> courseKindList) {
        if (CollectionUtils.isNotEmpty(courseKindList)) {
            HashMap<ClazzLevel, Integer> clazzLevelHashMap = new HashMap<>();
            HashMap<BabyEagleSubject, Integer> subjectHashMap = new HashMap<>();
            HashMap<BabyEagleTerm, Integer> termHashMap = new HashMap<>();
            List<ClazzLevel> clazzLevels = Arrays.stream(ClazzLevel.values()).filter(p -> (p.getLevel() >= 1 && p.getLevel() <= 6) || p == ClazzLevel.PRIVATE_GRADE).collect(Collectors.toList());
            for (int i = 0; i < clazzLevels.size(); i++) {
                clazzLevelHashMap.put(clazzLevels.get(i), (i + 1) * 1000);
            }
            subjectHashMap.put(BabyEagleSubject.ENCYCLOPEDIA, 100);
            subjectHashMap.put(BabyEagleSubject.ENCYCLOPEDIA_HIGH, 200);
            subjectHashMap.put(BabyEagleSubject.ENGLISH, 300);
            subjectHashMap.put(BabyEagleSubject.CHINESE, 400);
            subjectHashMap.put(BabyEagleSubject.MATH, 500);
            termHashMap.put(BabyEagleTerm.LAST_TERM, 10);
            termHashMap.put(BabyEagleTerm.NEXT_TERM, 20);
            termHashMap.put(BabyEagleTerm.ALL_TERM, 30);
            courseKindList.sort(((o1, o2) -> {
                int o1SubjectInt = SafeConverter.toInt(subjectHashMap.get(o1.getBabyEagleSubject()));
                int o1TermInt = SafeConverter.toInt(termHashMap.get(o1.getBabyEagleTerm()));
                int o1Level = SafeConverter.toInt(clazzLevelHashMap.get(o1.getClazzLevel()));

                int o2SubjectInt = SafeConverter.toInt(subjectHashMap.get(o2.getBabyEagleSubject()));
                int o2TermInt = SafeConverter.toInt(termHashMap.get(o2.getBabyEagleTerm()));
                int o2Level = SafeConverter.toInt(clazzLevelHashMap.get(o2.getClazzLevel()));
                return o1SubjectInt + o1TermInt + o1Level - o2SubjectInt - o2TermInt - o2Level;
            }));
        }
    }

    private void sortCourseInfoList(List<MapMessage> courseInfoList) {
        if (CollectionUtils.isNotEmpty(courseInfoList)) {
            courseInfoList.sort(((o1, o2) -> {
                Integer recommendOrder1 = SafeConverter.toInt(o1.get("recommendOrder"));
                Integer recommendOrder2 = SafeConverter.toInt(o2.get("recommendOrder"));
                Long firstStartTime1 = SafeConverter.toLong(o1.get("sort"));
                Long firstStartTime2 = (Long) o2.get("sort");
                if (!recommendOrder1.equals(recommendOrder2)) {
                    return recommendOrder2.compareTo(recommendOrder1);
                } else {
                    return firstStartTime1.compareTo(firstStartTime2);
                }
            }));
        }
    }

    private void postMessage(Long studentId, String title, String content, String url) {
        AppMessage message = new AppMessage();
        message.setUserId(studentId);
        message.setMessageType(StudentAppPushType.COMMON_REMIND.getType());
        if (StringUtils.isNotBlank(title))
            message.setTitle(title);
        if (StringUtils.isNotBlank(content))
            message.setContent(content);
        if (StringUtils.isNotBlank(url))
            message.setLinkUrl(url);
        message.setLinkType(1);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
    }

    private void sortModelclassHours(List<String> classHours) {
        if (CollectionUtils.isNotEmpty(classHours)) {
            classHours.sort(((o1, o2) -> {
                String[] t1 = o1.split("-");
                String start1 = t1[0];

                String[] t2 = o2.split("-");
                String start2 = t2[0];
                return start1.compareTo(start2);
            }));
        }
    }
}