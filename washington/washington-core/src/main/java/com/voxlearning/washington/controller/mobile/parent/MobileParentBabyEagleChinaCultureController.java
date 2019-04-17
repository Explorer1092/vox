package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.ParentBabyEagleChinaCultureService;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleChinaCultureLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/5
 */
@Controller
@RequestMapping(value = "/parentMobile/baby_eagle")
public class MobileParentBabyEagleChinaCultureController extends AbstractMobileParentController {


    @Inject
    private BabyEagleChinaCultureLoaderClient babyEagleChinaCultureLoaderClient;
    @ImportService(interfaceClass = ParentBabyEagleChinaCultureService.class)
    private ParentBabyEagleChinaCultureService parentBabyEagleChinaCultureService;


    // 课程列表页
    @RequestMapping(value = "course_list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage getCourseList() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        MapMessage mapMessage;
        if (studentDetail.getClazz() == null) {
            return MapMessage.successMessage().add("has_clazz", Boolean.FALSE);
        }
        //正式课程列表
        try {
            AlpsFuture<MapMessage> courseListAlps = babyEagleChinaCultureLoaderClient.getRemoteReference().fetchCourseList(studentId);
            mapMessage = courseListAlps.getUninterruptibly().add("has_clazz", Boolean.TRUE);
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }

        UserOrder userOrder = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.EagletSinologyClassRoom.name(), studentId)
                .stream()
                .findFirst()
                .orElse(null);
        if (userOrder != null) {
            mapMessage.add("order_id", userOrder.getId());
        }
        //分享打卡记录
        List<String> courseIds = parentBabyEagleChinaCultureService.getShareRecordsByStudentIds(Collections.singleton(studentId)).get(studentId);
        mapMessage.add("course_ids", CollectionUtils.isEmpty(courseIds) ? new ArrayList<>() : courseIds);
        MapMessage message = babyEagleChinaCultureLoaderClient.getRemoteReference().fetchIndexBannerRecommendCourse(studentId).getUninterruptibly();
        //试学课程是否在上课时间范围内
        Boolean inTryCoursePeriod = Boolean.FALSE;
        String countDownText = "";
        String tryCourseStartTime = "";
        //试学课程列表
        List<Map<String, Object>> bannerCourseList = (List<Map<String, Object>>) message.get("bannerRecommendCourseList");
        if (CollectionUtils.isNotEmpty(bannerCourseList)) {
            Date currentDate = new Date();
            Map<String, Object> bannerCourseMap = bannerCourseList
                    .stream()
                    .sorted((o1, o2) -> DateUtils.stringToDate(SafeConverter.toString(o2.get("startTime"))).compareTo(DateUtils.stringToDate(SafeConverter.toString(o1.get("startTime")))))
                    .findFirst()
                    .orElse(null);
            if (MapUtils.isNotEmpty(bannerCourseMap)) {
                Date bannerCourseEndTime = (Date) bannerCourseMap.get("endTime");
                Date bannerCourseStartTime = (Date) bannerCourseMap.get("startTime");
                if (bannerCourseEndTime != null && currentDate.before(bannerCourseEndTime)) {
                    if (bannerCourseStartTime != null && bannerCourseStartTime.compareTo(new Date()) <= 0) {
                        countDownText = "直播中";
                    }
                    inTryCoursePeriod = Boolean.TRUE;
                }
                tryCourseStartTime = DateUtils.dateToString(bannerCourseStartTime, "MM月dd日 HH:mm");
                if (bannerCourseStartTime != null && bannerCourseStartTime.after(new Date())) {
                    countDownText = "{0}天{1}小时{2}分";
                    long minDiff = DateUtils.minuteDiff(bannerCourseStartTime, new Date());
                    long dayDiff = minDiff / 60 / 24;
                    long hourDiff = minDiff / 60 % 60 % 24;
                    minDiff = minDiff % 60;
                    countDownText = MessageFormat.format(countDownText, dayDiff, hourDiff, minDiff);
                }
            }
        }
        mapMessage.add("in_try_course_period", inTryCoursePeriod)
                .add("try_course_start_time", tryCourseStartTime)
                .add("count_down_text", countDownText);
        return mapMessage;
    }

    // 获取收货地址
    @RequestMapping(value = "getAddress.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getAddress() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        String orderId = getRequestString("order_id");
        if (StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage();
        }
        String fixId = orderId + "_" + studentId % 100;
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(fixId);
        Map<String, Object> map = JsonUtils.fromJson(userOrder.getExtAttributes());
        String address = "";
        if (MapUtils.isNotEmpty(map)) {
            if (StringUtils.isNotBlank(SafeConverter.toString(map.get("address")))) {
                address = SafeConverter.toString(map.get("address"));
            }
        }
        return MapMessage.successMessage().add("address", address);
    }

    // 填写收货地址
    @RequestMapping(value = "address.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage address() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        String orderId = getRequestString("order_id");
        String address = getRequestString("address");
        if (StringUtils.isBlank(orderId) || StringUtils.isBlank(address)) {
            return MapMessage.errorMessage();
        }
        String fixId = orderId + "_" + studentId % 100;
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(fixId);
        if (userOrder == null) {
            return MapMessage.errorMessage("没有相关的订单信息");
        }
        String extAttributes = userOrder.getExtAttributes();
        Map<String, Object> extAttr = new HashMap<>();
        if (StringUtils.isBlank(extAttributes)) {
            extAttr.put("address", address);
        } else {
            extAttr = JsonUtils.fromJson(extAttributes);
            extAttr.put("address", address);
        }
        String extAttrJson = JsonUtils.toJson(extAttr);
        return userOrderService.updateUserOrderExtAttributes(userOrder, extAttrJson);
    }


    // 分享页信息
    @RequestMapping(value = "getShareInfo.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getShareInfo() {
        long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        String orderId = getRequestString("order_id");
        if (StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage();
        }
        String fixId = orderId + "_" + studentId % 100;
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(fixId);
        if (userOrder == null) {
            return MapMessage.errorMessage("没有相关的订单信息");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("student_img", getUserAvatarImgUrl(studentDetail.fetchImageUrl()));
        returnMap.put("student_name", studentDetail.fetchRealnameIfBlankId());
        returnMap.put("join_days", DateUtils.dayDiff(new Date(), userOrder.getCreateDatetime()));

        return MapMessage.successMessage().add("share_info", returnMap);
    }


    // 分享打卡
    @RequestMapping(value = "shareRecord.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage shareRecord() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        String course_id = getRequestString("course_id");
        if (StringUtils.isBlank(course_id)) {
            return MapMessage.errorMessage();
        }
        parentBabyEagleChinaCultureService.insertShareRecordByStudentIds(studentId, course_id);
        return MapMessage.successMessage();
    }


    @RequestMapping(value = "getShareRecord.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getShareRecords() {
        String sids = getRequestString("sids");
        List<Long> sidList = JsonUtils.fromJsonToList(sids, Long.class);
        if (CollectionUtils.isEmpty(sidList)) {
            return MapMessage.errorMessage();
        }
        Map<Long, List<String>> shareRecordsByStudentIds = parentBabyEagleChinaCultureService.getShareRecordsByStudentIds(sidList);
        return MapMessage.successMessage().add("shareRecordMap", shareRecordsByStudentIds);
    }
}


