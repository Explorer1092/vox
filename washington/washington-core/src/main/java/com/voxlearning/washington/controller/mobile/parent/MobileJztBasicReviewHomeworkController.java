package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping(value = "/parentMobile/jzt/basicreview/report")
@Slf4j
public class MobileJztBasicReviewHomeworkController extends AbstractMobileParentController {
    @Inject
    private BasicReviewHomeworkReportLoaderClient basicReviewHomeworkReportLoaderClient;

    /**
     * 学生个人有基础的学科 h5
     * @return
     */
    @RequestMapping(value = "subjects.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchSubjectsToPersonal() {
        long userId = getRequestLong("studentId");
        if (userId <= 0) {
            return MapMessage.errorMessage("学生ID不正确");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("用户登入信息失效");
        }
        try {
            return basicReviewHomeworkReportLoaderClient.fetchSubjectsToPersonal(userId);
        } catch (Exception e) {
            logger.error("/parentMobile/jzt/basicreview/report/subjects failed : userId {}", userId, e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 个人关卡信息 h5
     * @return
     */
    @RequestMapping(value = "stagelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchStageListToPersonal() {
        User parent = currentParent();
        String packageId = getRequestString("packageId");
        long userId = getRequestLong("studentId");
        if (parent == null || StringUtils.isAnyBlank(packageId) || userId <= 0) {
            return MapMessage.errorMessage("老师未登入，或者参数作业PACKAGE_ID不存在,学生ID不正确");
        }
        try {
            return basicReviewHomeworkReportLoaderClient.fetchStageListToPersonal(packageId, userId);
        } catch (Exception e) {
            logger.error("/parentMobile/jzt/basicreview/report/stagelist failed : packageId {},userId {}", packageId, userId, e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 个人一个卡片作业的报告 h5
     * @return
     */
    @RequestMapping(value = "detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchReportToPersonal() {
        String packageId = getRequestString("packageId");
        String homeworkId = getRequestString("homeworkId");
        long userId = getRequestLong("studentId");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请用户登入");
        }

        if (StringUtils.isAnyBlank(homeworkId, packageId) || userId <= 0) {
            return MapMessage.errorMessage("参数作业PACKAGE_ID,HOMEWOKR_ID不存在,学生ID不正确");
        }
        //return MapMessage.errorMessage("国庆假期练习无法查看报告");
        try {
            MapMessage mapMessage = basicReviewHomeworkReportLoaderClient.fetchReportToPersonal(packageId, homeworkId, userId, parent);
            if (mapMessage.isSuccess()) {
                mapMessage.put("teacherImageUrl", getUserAvatarImgUrl(SafeConverter.toString(mapMessage.get("teacherImageUrl"))));
            }
            return mapMessage;
        } catch (Exception e) {
            logger.error("/parentMobile/jzt/basicreview/report/detail failed:packageId {},userId {},homeworkId {}", packageId, userId, homeworkId, e);
            return MapMessage.errorMessage();
        }
    }

}
