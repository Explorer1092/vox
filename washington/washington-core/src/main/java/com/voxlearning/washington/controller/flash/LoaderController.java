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

package com.voxlearning.washington.controller.flash;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.support.flash.FlashVars;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.AllowUserTokenTypes;


@Controller
@RequestMapping("/flash/loader")
@Slf4j
public class LoaderController extends AbstractFlashLoaderController {

    private String displayFlashLoader(Model model) {
        return "flash/loader";
    }

    // 说明： userId=0的时候，表示试用。会跳过结果保存的步骤。具体实现请参考 AbstractScoreCalculatorTemplate
    @RequestMapping(value = "selfstudy-{type}-{userId}-{bookId}-{unitId}-{lessonId}.vpage", method = RequestMethod.GET)
    public String selfstudy(@PathVariable("type") Long type,
                            @PathVariable("userId") Long userId,
                            @PathVariable("bookId") Long bookId,
                            @PathVariable("unitId") Long unitId,
                            @PathVariable("lessonId") Long lessonId,
                            Model model) {
        String qids = getRequestString("qids");
        model.addAllAttributes(englishSelfStudy(type, userId, bookId, unitId, lessonId));
        model.addAttribute("fromModule", getRequestString("fromModule"));
        model.addAttribute("selfstudy", "true");
        return displayFlashLoader(model);
    }

    @RequestMapping(value = "newselfstudy.vpage", method = RequestMethod.GET)
    public String newSelfstudy(Model model) {

        String pictureBookId = getRequestString("pictureBookId");
        if (StringUtils.isNotBlank(pictureBookId)) {
            model.addAllAttributes(newPictureBookSelfStudy(currentUserId(), pictureBookId));
            model.addAttribute("fromModule", getRequestString("fromModule"));
            model.addAttribute("selfstudy", "true");
        } else {
            String qids = getRequestString("qids");
            String lessonId = getRequestString("lessonId");
            Long practiceId = getRequestLong("practiceId");
            String bookId = getRequestString("bookId");
            model.addAllAttributes(newSelfStudy(practiceId, currentUserId(), lessonId, qids, bookId));
            model.addAttribute("fromModule", getRequestString("fromModule"));
            model.addAttribute("selfstudy", "true");
        }
        return displayFlashLoader(model);
    }

    /**
     * 提供给第三方使用，如戴特等
     */
    @RequestMapping(value = "newselfstudywiththirdparty.vpage", method = RequestMethod.GET)
    public String newSelfstudyDaiTe(Model model) {

        String pictureBookId = getRequestString("pictureBookId");
        if (StringUtils.isNotBlank(pictureBookId)) {
            model.addAllAttributes(newPictureBookSelfStudy(currentUserId(), pictureBookId));
            model.addAttribute("fromModule", getRequestString("fromModule"));
            model.addAttribute("selfstudy", "true");
        } else {
            String qids = getRequestString("qids");
            String lessonId = getRequestString("lessonId");
            Long practiceId = getRequestLong("practiceId");
            String bookId = getRequestString("bookId");
            model.addAllAttributes(newSelfStudy(practiceId, currentUserId(), lessonId, qids, bookId));
            model.addAttribute("fromModule", getRequestString("fromModule"));
            model.addAttribute("selfstudy", "true");
        }
        return "flash/loaderwiththirdparty";
    }

    @RequestMapping(value = "newselfstudymobile.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newSelfstudyMobile(Model model) {
        String pictureBookId = getRequestString("pictureBookId");
        if (StringUtils.isNotBlank(pictureBookId)) {
            Map data = newPictureBookSelfStudy(currentUserId(), pictureBookId);
            data.put("fromModule", getRequestString("fromModule"));
            data.put("selfstudy", "true");
            return MapMessage.successMessage().add("data", data);
        } else {
            String qids = getRequestString("qids");
            String lessonId = getRequestString("lessonId");
            Long practiceId = getRequestLong("practiceId");
            String bookId = getRequestString("bookId");
            Map data = newSelfStudy(practiceId, currentUserId(), lessonId, qids, bookId);
            data.put("fromModule", getRequestString("fromModule"));
            data.put("selfstudy", "true");
            return MapMessage.successMessage().add("data", data);
        }
    }

    @RequestMapping(value = "homework.vpage", method = RequestMethod.GET)
    public String homework(Model model) {
        if (currentUserId() == null || !getWebRequestContext().isCurrentUserStudent())
            return "redirect:/";
        Long type = getRequestLong("type");
        Long bookId = getRequestLong("bookId");
        Long unitId = getRequestLong("unitId");
        Long lessonId = getRequestLong("lessonId");
        String hid = getRequestParameter("hid", "0");
        StudentDetail studentDetail = currentStudentDetail();

        if (studentDetail != null && studentDetail.getClazz() != null) {
            model.addAllAttributes(englishHomework(studentDetail, type, bookId, unitId, lessonId, hid));
            return displayFlashLoader(model);
        } else {
            //爬虫爬内容太厉害了，不用输出日志了。
            //log.error("LoaderController.homework can not get current student, need login");
            return "redirect:/";
        }
    }

    @RequestMapping(value = "newhomework.vpage", method = RequestMethod.GET)
    public String newHomework(Model model) {
        if (currentUserId() == null || !getWebRequestContext().isCurrentUserStudent())
            return "redirect:/";
        Long practiceId = getRequestLong("practiceId");
        String lessonId = getRequestString("lessonId");
        String hid = getRequestParameter("hid", "0");
        String pictureBookId = getRequestString("pictureBookId");
        String homeworkType = getRequestString("newHomeworkType");
        String objectiveConfigType = getRequestParameter("objectiveConfigType", StringUtils.isNotBlank(pictureBookId) ? ObjectiveConfigType.READING.name() : ObjectiveConfigType.BASIC_APP.name());
        StudentDetail studentDetail = currentStudentDetail();
        //在套壳的情况下会出现参数异常，一般情况下判断这俩就行
        if (practiceId == 0L || "0".equals(hid)) return "redirect:/";

        if ((studentDetail != null && studentDetail.getClazz() != null)
                || (studentDetail != null && AllowUserTokenTypes.contains(NewHomeworkType.valueOf(homeworkType)))) {
            // 绘本和flash游戏区别在lessonId和pictureBookId那个是空串
            model.addAllAttributes(newHomework(studentDetail, practiceId, lessonId, pictureBookId, hid, homeworkType, objectiveConfigType));
            return displayFlashLoader(model);
        } else {
            //爬虫爬内容太厉害了，不用输出日志了。
            //log.error("LoaderController.homework can not get current student, need login");
            return "redirect:/";
        }
    }

    @RequestMapping(value = "newhomeworkmobile.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage newHomeworkMobile(Model model) {
        User user = getHomeworkUser();
        if (user == null || user.fetchUserType() != UserType.STUDENT) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long practiceId = getRequestLong("practiceId");
        String lessonId = getRequestString("lessonId");
        String hid = getRequestParameter("hid", "0");
        String pictureBookId = getRequestString("pictureBookId");
        String homeworkType = getRequestString("newHomeworkType");
        String objectiveConfigType = getRequestParameter("objectiveConfigType", StringUtils.isNotBlank(pictureBookId) ? ObjectiveConfigType.READING.name() : ObjectiveConfigType.BASIC_APP.name());
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());

        if ((studentDetail != null && studentDetail.getClazz() != null) || (studentDetail != null && AllowUserTokenTypes.contains(NewHomeworkType.valueOf(homeworkType)))) {

            Map data = newHomework(studentDetail, practiceId, lessonId, pictureBookId, hid, homeworkType, objectiveConfigType);
            if (MapUtils.isNotEmpty(data)) {
                data.put("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            }
            return MapMessage.successMessage().add("data", data);
        } else {
            //爬虫爬内容太厉害了，不用输出日志了。
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
    }

    @RequestMapping(value = "englishvacationhomework.vpage", method = RequestMethod.GET)
    public String englishVacationHomework(Model model) {
        return "redirect:/";
    }

    /**
     * PK
     */
    @RequestMapping(value = "pk-{homeworkId}-{clazzId}.vpage", method = RequestMethod.GET)
    public String pk(HttpServletRequest request, Model model, @PathVariable("homeworkId") String homeworkId,
                     @PathVariable("clazzId") Long clazzId) {

        String file = "PkSystemMain";
        model.addAttribute("file", file);

        FlashVars vars = new FlashVars(request);
        vars.add("classId", clazzId);
        vars.add("homeworkId", homeworkId);
        vars.add("serverDomain", HttpRequestContextUtils.getWebAppBaseUrl(request) + "/");
        String flashVars = vars.getJsonParam();
        model.addAttribute("type", -1);
        model.addAttribute("file", "PkSystemMain");
        model.addAttribute("flashVars", flashVars);

        return displayFlashLoader(model);
    }

    // 说明： userId=0的时候，表示试用。会跳过结果保存的步骤。具体实现请参考 AbstractScoreCalculatorTemplate
    @RequestMapping(value = "mathselfstudy-{type}-{userId}-{bookId}-{unitId}-{lessonId}-{pointId}.vpage", method = RequestMethod.GET)
    public String mathselfstudy(@PathVariable("type") Long type,
                                @PathVariable("userId") Long userId,
                                @PathVariable("bookId") Long bookId,
                                @PathVariable("unitId") Long unitId,
                                @PathVariable("lessonId") Long lessonId,
                                @PathVariable("pointId") Long pointId,
                                @RequestParam(value = "dataType", required = false, defaultValue = "1") String dataType,
                                Model model) {

        return "redirect:/";

    }

    @RequestMapping(value = "mathhomework.vpage", method = RequestMethod.GET)
    public String mathhomework(@RequestParam("type") Long type,
                               @RequestParam("userId") Long userId,
                               @RequestParam("bookId") Long bookId,
                               @RequestParam("unitId") Long unitId,
                               @RequestParam("lessonId") Long lessonId,
                               @RequestParam("pointId") Long pointId,
                               @RequestParam("questionNum") Integer questionNum,
                               @RequestParam("hid") String hid,
                               @RequestParam(value = "cid", required = false, defaultValue = "0") String cid,
                               @RequestParam(value = "dataType", required = false, defaultValue = "1") String dataType,
                               @RequestParam(value = "homeworkType", required = false, defaultValue = "MATH") String homeworkType,
                               Model model) {
        return "redirect:/";
    }

    @RequestMapping(value = "mathvacationhomework.vpage", method = RequestMethod.GET)
    public String mathVacationhHomework(Model model) {
        return "redirect:/";
    }

    @RequestMapping(value = "chineseselfstudy-{type}-{userId}-{bookId}-{unitId}-{lessonId}.vpage", method = RequestMethod.GET)
    public String chineseSelfstudy(@PathVariable("type") Long type,
                                   @PathVariable("userId") Long userId,
                                   @PathVariable("bookId") Long bookId,
                                   @PathVariable("unitId") Long unitId,
                                   @PathVariable("lessonId") Long lessonId,
                                   Model model) {

        return "redirect:/";

    }

    @RequestMapping(value = "chinesehomework.vpage", method = RequestMethod.GET)
    public String chineseHomework(@RequestParam("type") Long type,
                                  @RequestParam("userId") Long userId,
                                  @RequestParam("bookId") Long bookId,
                                  @RequestParam("unitId") Long unitId,
                                  @RequestParam("lessonId") Long lessonId,
                                  @RequestParam("hid") Long hid,
                                  @RequestParam(value = "cid", required = false, defaultValue = "0") String cid,
                                  Model model) {
        return "redirect:/";
    }

    @RequestMapping(value = "studenthomework-{homeworkId}.vpage", method = RequestMethod.GET)
    public String studentHomeworkCorrect(@PathVariable("homeworkId") String homeworkId, Model model) {
        if (currentUserId() == null) {
            return "redirect:/";
        }
        return "";
    }


    @RequestMapping(value = "h5/homework.vpage", method = RequestMethod.GET)
    public String h5Homework(Model model) {
        Long practiceId = getRequestLong("practiceId");
        Long bookId = getRequestLong("bookId");
        Long unitId = getRequestLong("unitId");
        Long lessonId = getRequestLong("lessonId");
        Long pointId = getRequestLong("pointId");
        Integer questionNum = getRequestInt("questionNum");
        String hid = getRequestString("hid");
        Long cid = getRequestLong("cid");
        String dataType = getRequestString("dataType");
        String homeworkType = getRequestString("homeworkType");
        StudyType studyType = StudyType.of(getRequestString("studyType"));
        Subject subject = Subject.of(getRequestString("subject"));
        User student = currentStudent();
        if (student != null) {
            PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
            Map<String, Object> param = new LinkedHashMap<>();
            //gameType和practiceType取的是同一个值，以后新FLASH应用统一取gameType,但为了兼容老的FLASH应用，所以practiceType也保留
            param.put("gameType", String.valueOf(practiceId));
            param.put("practiceType", String.valueOf(practiceId));
            param.put("userId", String.valueOf(student.getId()));
            param.put("hid", hid);
            param.put("bookId", String.valueOf(bookId));
            param.put("unitId", String.valueOf(unitId));
            param.put("lessonId", String.valueOf(lessonId));
            param.put("pointId", String.valueOf(pointId));
            param.put("dataType", dataType);
            param.put("studyType", studyType.name());
            param.put("cid", String.valueOf(cid));
            param.put("questionNum", String.valueOf(questionNum));
            param.put("homeworkType", homeworkType);
            Map queryParams = MiscUtils.map("param", JsonUtils.toJson(param),
                    "practiceName", practiceType.getFilename(),
                    "subject", subject,
                    "pointId", pointId,
                    "amount", questionNum);
            String gameDataURL = UrlUtils.buildUrlQuery("appdata/obtain" + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(), queryParams); //10表示出题个数
            String completedUrl = "flash/" + practiceType.getFilename() + "/process" + Constants.AntiHijackExt;
            model.addAttribute("gameDataURL", gameDataURL);
            model.addAttribute("param", JsonUtils.toJson(param));
            model.addAttribute("subject", subject);
            model.addAttribute("completedUrl", completedUrl);
            model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
            return "studentv3/practice/homework";
        }
        return "redirect:/login.vpage";
    }
}
