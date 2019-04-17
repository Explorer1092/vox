//package com.voxlearning.washington.controller.open.v1.student;
//
//import com.voxlearning.alps.core.util.StringUtils;
//import com.voxlearning.alps.lang.convert.SafeConverter;
//import com.voxlearning.alps.lang.util.MapMessage;
//import com.voxlearning.alps.lang.util.MiscUtils;
//import com.voxlearning.alps.web.UrlUtils;
//import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
//import com.voxlearning.utopia.api.constant.*;
//import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
//import com.voxlearning.utopia.service.content.api.entity.PracticeType;
//import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
//import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookSummaryResult;
//import com.voxlearning.utopia.service.newhomework.api.entity.ReadReciteSummaryResult;
//import com.voxlearning.utopia.service.newhomework.api.entity.VideoSummaryResult;
//import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
//import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
//import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
//import com.voxlearning.washington.controller.open.AbstractStudentApiController;
//import com.voxlearning.washington.controller.open.ApiConstants;
//import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
//import com.voxlearning.washington.support.flash.FlashVars;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.*;
//
//import static com.voxlearning.washington.controller.open.ApiConstants.*;
//import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.*;
//
///**
// * @author zhangbin
// * @since 2017/7/20 16:04
// */
//
//@Controller
//@RequestMapping(value = "/v1/student")
//@Slf4j
//public class StudentNewHomeworkApiController extends AbstractStudentApiController {
//
//    //做作业
//    @RequestMapping(value = "newhomework/do.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    @ResponseBody
//    public MapMessage newHomeworkForMobile(HttpServletRequest request) {
//        MapMessage resultMap = new MapMessage();
//        try {
//            validateRequired(RES_OBJECTIVE_CONFIG_TYPE, "作业形式");
//            validateRequired(REQ_HOMEWORK_ID, "作业id");
//            validateRequired(RES_LESSON_ID, "课文id");
//            validateRequired(REQ_CATEGORY_ID, "应用id");
//            validateRequired(RES_PRACTICE_ID, "练习ID");
//            validateRequired(RES_PICTURE_BOOK_IDS, "绘本ID");
//            validateRequest(
//                    RES_OBJECTIVE_CONFIG_TYPE,
//                    REQ_PIC_HOMEWORK_ID,
//                    RES_LESSON_ID,
//                    REQ_CATEGORY_ID,
//                    RES_PRACTICE_ID,
//                    RES_PICTURE_BOOK_IDS
//            );
//        } catch (IllegalArgumentException e) {
//            if (e instanceof IllegalVendorUserException) {
//                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
//                resultMap.add(RES_MESSAGE, e.getMessage());
//                return resultMap;
//            }
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, e.getMessage());
//            return resultMap;
//        }
//
//        String objectiveConfigType = getRequestString(RES_OBJECTIVE_CONFIG_TYPE);
//        String homeworkId = getRequestString(REQ_PIC_HOMEWORK_ID);
//        String lessonId = getRequestString(RES_LESSON_ID);
//        String categoryId = getRequestString(REQ_CATEGORY_ID);
//        String practiceId = getRequestString(RES_PRACTICE_ID);
//        String pictureBookIds = getRequestString(RES_PICTURE_BOOK_IDS);
//
//        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
//        if (newHomework == null) {
//            resultMap.add(RES_RESULT, RES_RESULT_HOMEWORK_HAD_DELETE).setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_DISABLED);
//        }
//
//        StudentDetail studentDetail = currentStudentDetail();
//        if ((studentDetail != null && studentDetail.getClazz() != null)
//                || (studentDetail != null && StringUtils.equalsIgnoreCase(newHomework.getType().name(), NewHomeworkType.USTalk.name()))) {
//            FlashVars vars = new FlashVars(request);
//            vars.add("uid", studentDetail.getId());
//            vars.add("hid", homeworkId);
//            vars.add("subject", newHomework.getSubject());
//            vars.add("ipAddress", HttpRequestContextUtils.getWebAppBaseUrl());
//            vars.add("learningType", StudyType.homework);
//            vars.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
//            if (ObjectiveConfigType.BASIC_APP.name().equals(objectiveConfigType)
//                    || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.name().equals(objectiveConfigType)
//                    || ObjectiveConfigType.NATURAL_SPELLING.name().equals(objectiveConfigType)) {
//                boolean unisound8 = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "VoiceEngine", "Unisound8");
//                if(unisound8){
//                    vars.add("unisound8WordscoreLevels", Unisound8WordScoreLevel.levels);
//                    vars.add("unisound8SentencescoreLevels", Unisound8SentenceScoreLevel.levels);
//                }else {
//                    vars.add("scoreLevels", UnisoundScoreLevel.levels);
//                }
//
//                List<PracticeType> practiceTypes = practiceLoaderClient.loadCategoriedIdPractices(SafeConverter.toInt(categoryId));
//
//                List<Map> practices = new ArrayList<>();
//                for (PracticeType practiceType : practiceTypes) {
//                    if (!PracticeCategory.categoryPracticeTypesMap.get(SafeConverter.toInt(categoryId)).contains(practiceType.getId())) {
//                        continue;
//                    }
//                    practices.add(MiscUtils.m(
//                            "appUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomework" + Constants.AntiHijackExt,
//                                    MiscUtils.m(
//                                            "practiceId", practiceType.getId(),
//                                            "hid", homeworkId,
//                                            "lessonId", lessonId,
//                                            "newHomeworkType", newHomework.getType(),
//                                            "objectiveConfigType", objectiveConfigType)),
//                            "appMobileUrl", UrlUtils.buildUrlQuery("/flash/loader/newhomeworkmobile" + Constants.AntiHijackExt,
//                                    MiscUtils.m(
//                                            "practiceId", practiceType.getId(),
//                                            "hid", homeworkId,
//                                            "lessonId", lessonId,
//                                            "newHomeworkType", newHomework.getType(),
//                                            "objectiveConfigType", objectiveConfigType)),
//                            "fileName", practiceType.getFilename(),
//                            "practiceId", practiceType.getId(),
//                            "practiceName", practiceType.getPracticeName(),
//                            "categoryId", practiceType.getCategoryId(),
//                            "categoryName", practiceType.getCategoryName(),
//                            "needRecord", practiceType.getNeedRecord(),
//                            "checked", practiceType.getId().equals(SafeConverter.toLong(practiceId)),
//                            "questionUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt,
//                                    MiscUtils.m(
//                                            "objectiveConfigType", objectiveConfigType,
//                                            "homeworkId", homeworkId,
//                                            "lessonId", lessonId,
//                                            "categoryId", practiceType.getCategoryId())),
//                            "completedUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt,
//                                    MiscUtils.m(
//                                            "objectiveConfigType", objectiveConfigType,
//                                            "homeworkId", homeworkId,
//                                            "lessonId", lessonId,
//                                            "categoryId", practiceType.getCategoryId()))
//                    ));
//                }
//                vars.add("unisound8", unisound8);
//                vars.add("practices", practices);
//                vars.add("objectiveConfigType", objectiveConfigType);
//            } else if (ObjectiveConfigType.READING.name().equals(objectiveConfigType)) {
//                // 其实这个picBookIds可以不用传过来
//                String[] picBookIds = StringUtils.split(pictureBookIds, ",");
//                if (picBookIds == null || picBookIds.length <= 0) {
//                    resultMap.add(RES_RESULT, RES_RESULT_PICTURE_BOOK_DELETE).setErrorCode(ErrorCodeConstants.ERROR_CODE_PICTURE_BOOK_IS_NULL);
//                }
//                List<PictureBookSummaryResult> picBookResult = pictureBookHomeworkServiceClient.getPictureBookSummaryInfo(
//                        homeworkId,
//                        Arrays.asList(picBookIds),
//                        studentDetail.getId());
//                vars.add("practices", picBookResult);
//                vars.add("objectiveConfigType", objectiveConfigType);
//            } else if (ObjectiveConfigType.KEY_POINTS.name().equals(objectiveConfigType)) {
//                List<VideoSummaryResult> videoResults = videoHomeworkServiceClient.getVideoSummaryInfo(homeworkId, studentDetail.getId());
//                vars.add("practices", videoResults);
//            } else if (ObjectiveConfigType.NEW_READ_RECITE.name().equals(objectiveConfigType)) {
//                List<ReadReciteSummaryResult> readReciteSummaryResults = readReciteHomeworkServiceClient.getReadRectieSummaryInfo(homeworkId, studentDetail.getId());
//                vars.add("practices", readReciteSummaryResults);
//            } else {
//                if (ObjectiveConfigType.ORAL_PRACTICE.name().equals(objectiveConfigType)) {
//                    vars.add("oralScoreIntervals", OralScoreInterval.oralScoreIntervals);
//                }
//                vars.add("questionUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt,
//                        MiscUtils.m(
//                                "objectiveConfigType", objectiveConfigType,
//                                "homeworkId", homeworkId
//                        )));
//                vars.add("completedUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt,
//                        MiscUtils.m(
//                                "objectiveConfigType", objectiveConfigType,
//                                "homeworkId", homeworkId
//                        )));
//                vars.add("processResultUrl", "/exam/flash/newhomework/processresult" + Constants.AntiHijackExt);
//            }
//
//            String flashVars = vars.getJsonParam();
//            Map<String, Object> data = new HashMap<>();
//            data.put("flashVars", flashVars);
//            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//            resultMap.add(REQ_FLASHVARS_DATA, data);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_RELOGIN).setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
//        }
//        return resultMap;
//    }
//
//}
