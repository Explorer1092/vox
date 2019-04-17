package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping(value = "/parentMobile/jzt/vacation")
@Slf4j
public class MobileJztVacationHomeworkController extends AbstractMobileParentController {

    @Inject
    private VacationHomeworkReportLoaderClient vacationHomeworkReportLoaderClient;

    @RequestMapping(value = "personalreadingdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalReadingDetail() {
        User user = currentParent();
        if (user == null) {
            logger.error("Parent is null");
            return MapMessage.errorMessage("Parent is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        String homeworkId = getRequestString("homeworkId");
        String readingId = getRequestString("readingId");
        String objectiveConfigTypeStr = getRequestParameter("objectiveConfigType", ObjectiveConfigType.LEVEL_READINGS.name());
        if (StringUtils.isBlank(homeworkId) || StringUtils.isBlank(readingId)) {
            logger.error("homeworkId or readingId is blank");
            return MapMessage.errorMessage("homeworkId or readingId is null");
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        if (objectiveConfigType == null) {
            return MapMessage.errorMessage("作业形式错误");
        }
        try {
            return vacationHomeworkReportLoaderClient.personalReadingDetail(homeworkId, readingId, objectiveConfigType);
        } catch (Exception e) {
            logger.error("get personal reading detail failed : hid of {},reading id of {}", homeworkId, readingId, e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 趣味配音个人二级详情页面
     */
    @RequestMapping(value = "personaldubbingdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalDubbingDetail() {
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("请用家长帐号登录");
        }
        String homeworkId = getRequestString("homeworkId");
        String dubbingId = getRequestString("dubbingId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }

        if (StringUtils.isBlank(dubbingId)) {
            return MapMessage.errorMessage("配音id为空");
        }
        try{
            return vacationHomeworkReportLoaderClient.personalDubbingDetail(homeworkId, dubbingId);
        }catch (Exception ex){
            logger.error("Failed to load personalDubbingDetail homeworkId:{},studentId{},dubbingId{}", homeworkId, dubbingId, ex);
            return MapMessage.errorMessage("获取趣味配音个人二级详情异常");
        }
    }


    @RequestMapping(value = "detailsbaseapp.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage detailsBaseApp() {
        String homeworkId = getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            logger.error("homeworkId is blank");
            return MapMessage.errorMessage("homeworkId is blank");
        }
        String categoryId = getRequestString("categoryId");
        if (StringUtils.isBlank(categoryId)) {
            logger.error("categoryId is blank");
            return MapMessage.errorMessage("categoryId is blank");
        }
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isBlank(lessonId)) {
            logger.error("lessonId is blank");
            return MapMessage.errorMessage("lessonId is blank");
        }
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.BASIC_APP.name()));
        if (objectiveConfigType == null) {
            logger.error("objectiveConfigType is null");
            return MapMessage.errorMessage("objectiveConfigType is null");
        }
        try {
            return vacationHomeworkReportLoaderClient.reportDetailsBaseApp(homeworkId, categoryId, lessonId, objectiveConfigType);
        } catch (Exception e) {

            logger.error("get details from base app failed : hid of {},categoryId of {},lessonId of {},objectiveConfigType of {}", homeworkId, categoryId, lessonId, objectiveConfigType, e);
            return MapMessage.errorMessage();
        }

    }

    /**
     * 课文读背(打分)
     * @return
     */
    @RequestMapping(value = "personalreadrecitewithscore.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalReadReciteWithScore() {
        String hid = getRequestString("hid");
        String questionBoxId = getRequestString("questionBoxId");
        Long sid = getRequestLong("sid");
        if (StringUtils.isAnyBlank(hid) || sid == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage mapMessage = vacationHomeworkReportLoaderClient.personalReadReciteWithScore(hid, questionBoxId, sid);
        mapMessage.putAll(
                MapUtils.m(
                        "questionUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", "")),
                        "completedUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt,
                                MapUtils.m(
                                        "homeworkId", hid,
                                        "type", ""))
                ));
        return mapMessage;
    }


    @RequestMapping(value = "/package/report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getStudentPackageReport() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登入帐号");
        }
        String packageId = getRequestString("packageId");
        Long studentId = getRequestLong("sid");
        if (StringUtils.isBlank(packageId) || studentId == 0)
            return MapMessage.errorMessage("参数错误 packageId {} sid {}", packageId, studentId).setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
        MapMessage mapMessage;
        try {
            mapMessage = vacationHomeworkReportLoaderClient.studentPackageReport(packageId, studentId);
        } catch (Exception e) {
            logger.error("get student package report failed : packageId of {},studentId of {}", packageId, studentId, e);
            return MapMessage.errorMessage();
        }
        return mapMessage;
    }

    @RequestMapping(value = "detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage vacationReportDetailInformation() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登入帐号");
        }
        String homeworkId = this.getRequestString("homeworkId");
        if (StringUtils.isBlank(homeworkId)) {
            logger.error("homeworkId is blank");
            return MapMessage.errorMessage("homeworkId is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_NORMAL_RETURN);
        }
        MapMessage mapMessage;
        try {
            mapMessage = vacationHomeworkReportLoaderClient.vacationReportDetailInformation(homeworkId);
        } catch (Exception e) {
            logger.error("fetch vacation homework info failed : id of {}", homeworkId, e);
            mapMessage = MapMessage.errorMessage();
        }

        if (mapMessage.isSuccess()) {
            long userId = SafeConverter.toLong(mapMessage.get("userId"));
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            if (studentDetail != null) {
                String subject = SafeConverter.toString(mapMessage.get("subject"));
                String orderProductServiceType = subject.equals("CHINESE") ? OrderProductServiceType.AfentiChinese.name() : (subject.equals("MATH") ? OrderProductServiceType.AfentiMath.name() : OrderProductServiceType.AfentiExam.name());
                List<String> serviceTypes = new LinkedList<>();
                serviceTypes.add(orderProductServiceType);
                Map<String, String> userUseNumDescMap = businessVendorServiceClient.fetchUserUseNumDesc(serviceTypes, studentDetail);
                String afentiDesc = "";
                if (userUseNumDescMap.containsKey(orderProductServiceType)) {
                    afentiDesc = userUseNumDescMap.get(orderProductServiceType);
                }
                String openAppUrl = UrlUtils.buildUrlQuery("/parentMobile/ucenter/shoppinginfo.vpage",
                        MapUtils.m("sid", userId,
                                "productType", orderProductServiceType,
                                "refer", 240002));
                mapMessage.putAll(MapUtils.m(
                        "afentiDesc", afentiDesc,
                        "openAppUrl", openAppUrl,
                        "showAfentiGuide", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "VacationHW", "AfentiGuide")
                ));
            }
        }
        mapMessage.putAll(MapUtils.m(
                "questionUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "homeworkId", homeworkId,
                                "type", "")),
                "completedUrl", UrlUtils.buildUrlQuery("/flash/loader/vacation/homework/questions/answer" + Constants.AntiHijackExt,
                        MapUtils.m(
                                "homeworkId", homeworkId,
                                "objectiveConfigType", ""))
        ));
        return mapMessage;
    }
}
