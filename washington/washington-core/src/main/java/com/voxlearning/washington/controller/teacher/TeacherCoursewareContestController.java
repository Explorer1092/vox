package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.business.api.constant.TeacherNewTermActivityCategory;
import com.voxlearning.utopia.entity.activity.TeacherNewTermActivityProgress;
import com.voxlearning.utopia.service.business.consumer.TeacherActivityServiceClient;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareBookInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewarePageInfo;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherResourceRef;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareContestServiceClient;
import com.voxlearning.utopia.service.campaign.client.TeacherResourceServiceClient;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.washington.data.UploadFileType;
import com.voxlearning.washington.data.enums.AwardEnum;
import com.voxlearning.washington.data.errorCode.coursewareErrorCode;
import com.voxlearning.washington.data.utils.Describable;
import com.voxlearning.washington.data.utils.EnumListBuilder;
import com.voxlearning.washington.data.utils.KeyValue;
import com.voxlearning.washington.data.view.*;
import com.voxlearning.washington.service.UploadOssManageUtils;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.Pager;
import com.voxlearning.washington.support.upload.OSSManageUtils;
import lombok.Cleanup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants.CANVASS_VOTE_END_DATE;
import static com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware.Status.PUBLISHED;
import static com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware.Status.REJECTED;

@Controller
@RequestMapping("/courseware/contest")
public class TeacherCoursewareContestController extends AbstractController {

    @Inject
    private TeacherActivityServiceClient teacherActivityServiceClient;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private TeacherCoursewareContestServiceClient teacherCoursewareContestServiceClient;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private TeacherResourceServiceClient teacherResourceServiceClient;

    public static final Integer MAX_NEWEST_COURSEWARE = 6;

    public static final Integer DEFAULT_PAGE_NUM = 0;

    public static final Integer DEFAULT_PAGE_SIZE = 8;

    private static final String EXAMPLE_PATH = "/config/templates/teacher_plan_template_and_product_elect_standard.zip";

    private static final String EXAMPLE_NAME = "首届小学语数外信息化教学设计展示活动资料包.zip";

    public static final String TOPIC_BOOK_ID = "BKC_BOOK_ID";

    public static final String TOPIC_UNIT_ID = "BKC_UNIT_ID";

    public static final String TOPIC_LESSON_ID = "BKC_LESSON_ID";

    public static final String TOPIC_NAME = "话题";

    /*
    * 首页
    * */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        return "teacher_coursewarev2/index";
    }

    /*
    * 规则页
    * */
    @RequestMapping(value = "rule.vpage", method = RequestMethod.GET)
    public String rulePage() {
        return "teacher_coursewarev2/rule";
    }

    /*
    * 规则页
    * */
    @RequestMapping(value = "personalcenter.vpage", method = RequestMethod.GET)
    public String personalcenterPage() {
        return "teacher_coursewarev2/personalcenter";
    }

    /*
    * 作品详情页
    * */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String detailPage() {
        return "teacher_coursewarev2/detail";
    }

    /*
    * 全部作品页
    * */
    @RequestMapping(value = "course.vpage", method = RequestMethod.GET)
    public String coursePage() {
        return "teacher_coursewarev2/course";
    }

    /*
    * 投票页
    * */
    @RequestMapping(value = "vote.vpage", method = RequestMethod.GET)
    public String votePage() {
        return "teacher_coursewarev2/vote";
    }

    /*
    * 排行榜页
    * */
    @RequestMapping(value = "ranking.vpage", method = RequestMethod.GET)
    public String rankingPage() {
        return "teacher_coursewarev2/ranking";
    }

    /*
     * 获奖结果公布页
     * */
    @RequestMapping(value = "award.vpage", method = RequestMethod.GET)
    public String awardPage() {
        return "teacher_coursewarev2/award";
    }

    /*
    * 上传作品页
    * */
    @RequestMapping(value = "upload.vpage", method = RequestMethod.GET)
    public String uploadPage() {
        return "teacher_coursewarev2/upload";
    }

//    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
//    public String index() {
//        return "teacher_courseware/index";
//    }

    @RequestMapping(value = "pc/join.vpage", method = RequestMethod.GET)
    public String pcJoin() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return "";
        }
        TeacherNewTermActivityProgress activityProgress = teacherActivityServiceClient.getRemoteReference().findTeacherActivityProgress(TeacherNewTermActivityCategory.TeacherCoursewareContest.getId(), teacherDetail.getId());
        if (activityProgress == null) {
            return "teacher_courseware/join";
        } else {
            return "";
        }
    }

    @RequestMapping(value = "mobile/join.vpage", method = RequestMethod.GET)
    public String mobileJoin() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return "";
        }
        TeacherNewTermActivityProgress activityProgress = teacherActivityServiceClient.getRemoteReference().findTeacherActivityProgress(TeacherNewTermActivityCategory.TeacherCoursewareContest.getId(), teacherDetail.getId());
        if (activityProgress == null) {
            return "";
        } else {
            return "";
        }
    }

    @RequestMapping(value = "join.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage join() {
        if (TeacherCoursewareConstants.isCloseUpload()) {
            return TeacherCoursewareConstants.CLOSE_UPLOAD_MSG;
        }

        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请登录后再点击报名");
        }
        return  teacherActivityServiceClient.participateActivity(TeacherNewTermActivityCategory.TeacherCoursewareContest.getId(), teacherDetail.getTeacherSchoolId(), teacherDetail.getId());
    }

    @RequestMapping(value = "my_course.vpage", method = RequestMethod.GET)
    public String myCourse() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return "redirect:/";
        }else{
            return "teacher_courseware/my_course";
        }
    }

    @RequestMapping(value = "upload_course.vpage", method = RequestMethod.GET)
    public String uploadCourse() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return "redirect:/";
        }else{
            return "teacher_courseware/upload_course";
        }
    }

    @RequestMapping(value = "myworks/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage myworksList(@RequestParam(name = "pageNum",required = false) Integer pageNum,
                                  @RequestParam(name = "pageSize",required = false) Integer pageSize,
                                  @RequestParam(name = "status",required = false) String status) {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录");
        }

        pageNum = pageNum == null ? DEFAULT_PAGE_NUM : pageNum;
        pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;

        TeacherCoursewareParam teacherCoursewareParam = new TeacherCoursewareParam();
        teacherCoursewareParam.setStatus(status);
        teacherCoursewareParam.setTeacherId(teacherDetail.getId());

        long allNum = teacherCoursewareContestServiceClient.fetchTeacherCoursewareNumByTeacherId(teacherCoursewareParam);

        TeacherCoursewarePageInfo teacherCoursewarePageInfo = new TeacherCoursewarePageInfo();
        teacherCoursewarePageInfo.setPageNum(pageNum);
        teacherCoursewarePageInfo.setPageSize(pageSize);
        teacherCoursewarePageInfo.setStatus(status);
        teacherCoursewarePageInfo.setTeacherId(teacherDetail.getId());

        List<TeacherCourseware> teacherCoursewares = teacherCoursewareContestServiceClient.
                fetchCourseWareListByPage(teacherCoursewarePageInfo).stream()
                .sorted((e1,e2) -> {
                    long t1 = e1.getUpdateTime() != null ? e1.getUpdateTime().getTime() : 0L;
                    long t2 = e2.getUpdateTime() != null ? e2.getUpdateTime().getTime() : 0L;
                    return  t2 > t1 ? 1 : -1;
                }).collect(Collectors.toList());

        List<Map<String, Object>> coursewareListMap = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(teacherCoursewares)) {
            teacherCoursewares.forEach(e -> {
                Map<String, Object> coursewareMap = new HashMap<>();
                NewBookProfile bookProfile = null;
                if (StringUtils.isNotBlank(e.getBookId())) {
                    bookProfile = newContentLoaderClient.loadBook(e.getBookId());
                }
                coursewareMap.put("id", e.getId());
                coursewareMap.put("title", e.getTitle());
                coursewareMap.put("date", DateUtils.dateToString(e.getUpdateTime()));
                coursewareMap.put("status", e.getStatus());
                coursewareMap.put("cover",e.getCoverUrl());
                coursewareMap.put("statusDesc", e.getStatus().getDescription());
                coursewareMap.put("image", bookProfile != null ? bookProfile.getImgUrl() : "");
                coursewareMap.put("score", e.getTotalScore());
                coursewareMap.put("commentNum", e.getCommentNum() != null ? e.getCommentNum() : 0);
                coursewareMap.put("visitNum", e.getVisitNum() != null ? e.getVisitNum() : 0);
                coursewareMap.put("downloadNum", e.getDownloadNum() != null ? e.getDownloadNum() : 0);
                coursewareMap.put("desc",e.getStatus() == REJECTED ? e.getExamineExt() : "");
                coursewareMap.put("awardLevelId",e.getAwardLevelId());
                coursewareMap.put("awardLevelName",e.getAwardLevelName());
                coursewareListMap.add(coursewareMap);
            });
        }

        return MapMessage.successMessage().set("coursewares", coursewareListMap).
                set("totalCount",allNum).set("pageNum",pageNum).set("pageSize",pageSize);
    }

    @RequestMapping(value = "course_detail.vpage", method = RequestMethod.GET)
    public String courseDetail() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return "redirect:/";
        }else{
            return "teacher_courseware/course_detail";
        }
    }

    @RequestMapping(value = "myworks/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage myworksDetail() {
        String id = getRequestString("id");
        Boolean preview = getRequestBool("preview");

        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().setInfo("参数错误");
        }

        TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(id);
        if (teacherCourseware == null) {
            return MapMessage.errorMessage().setInfo("无效的课件ID");
        }

        if (preview){
            MapMessage message = teacherCoursewareContestServiceClient.
                    updateVisitNum(id, SafeConverter.toInt(teacherCourseware.getVisitNum())+1);
            if (!message.isSuccess()){
                return MapMessage.errorMessage().setInfo(message.getInfo());
            }
        }
        if (teacherCourseware == null || (teacherCourseware.getDisabled() != null && teacherCourseware.getDisabled())) {
            return MapMessage.errorMessage().setInfo("课程不存在或者已经被删除").setErrorCode(coursewareErrorCode.COURSEWARE_NOTEXIST_ERROR);
        }

        NewBookProfile bookProfile = null;
        if (StringUtils.isNotBlank(teacherCourseware.getBookId())) {
            bookProfile = newContentLoaderClient.loadBook(teacherCourseware.getBookId());
        }

        NewBookCatalog newBookCatalog = null;
        if (StringUtils.isNotBlank(teacherCourseware.getUnitId())) {
            newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(teacherCourseware.getUnitId());
        }

        Map<String, List<NewBookCatalog>> lessonListMap = newContentLoaderClient.
                loadChildren(Arrays.asList(teacherCourseware.getUnitId()), BookCatalogType.LESSON);

        List<NewBookCatalog> lessonList = lessonListMap.get(teacherCourseware.getUnitId());

        String lessonId = teacherCourseware.getLessonId();

        String lessonName = "";

        // todo 后期改下
        if (lessonList != null){
            for (NewBookCatalog bookCatalog : lessonList){
                if (bookCatalog.getId().equals(lessonId)){
                    lessonName = bookCatalog.getName();
                    break;
                }
            }
        }

        List<NewBookCatalog> newBookCatalogList = new ArrayList<>();
        if (teacherCourseware.getSubject() != null){
            newBookCatalogList = newContentLoaderClient.loadShowSeriesBySubject(teacherCourseware.getSubject());
        }

        // todo 这里也要修改
        String serieName = "";

        if (newBookCatalogList != null && !newBookCatalogList.isEmpty()){
            for (NewBookCatalog catalog : newBookCatalogList){
                if (catalog.getId().equals(teacherCourseware.getSerieId())){
                    serieName = catalog.getName();
                }
            }
        }

        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherCourseware.getTeacherId());

        List<AttachmentView> attachmentViews = AttachmentView.Builder.build(teacherCourseware);

        MapMessage result = MapMessage.successMessage().set("id", teacherCourseware.getId()).
                   set("title", teacherCourseware.getTitle()).
                   set("term", teacherCourseware.getTermType()).
                   set("clazzLevel",teacherCourseware.getClazzLevel()).
                   set("status", teacherCourseware.getStatus()).
                   set("statusDesc", teacherCourseware.getStatus().getDescription()).
                   set("date", DateUtils.dateToString(teacherCourseware.getUpdateTime())).
                   set("image", bookProfile != null ? bookProfile.getImgUrl() : "").
                   set("bookId",teacherCourseware.getBookId()).
                   set("subject",teacherCourseware.getSubject()).
                   set("bookName",bookProfile != null ? bookProfile.getName() : "").
                   set("unitId",teacherCourseware.getUnitId()).
                   set("unitName",newBookCatalog != null ? newBookCatalog.getName() : "").
                   set("description", "null".equals(teacherCourseware.getDescription()) ? "" : teacherCourseware.getDescription()).
                   set("coursewareFileUrl", teacherCourseware.getCoursewareFile()).
                   set("coursewareFileName", teacherCourseware.getCoursewareFileName()).
                   set("lessonId", teacherCourseware.getLessonId()).
                   set("lessonRealName", lessonName).
                   set("coverUrl", teacherCourseware.getCoverUrl()).
                   set("coverName", teacherCourseware.getCoverName()).
                   set("compressedFileUrl", teacherCourseware.getCompressedFileUrl()).
                   set("compressedFileName", teacherCourseware.getCompressedFileName()).
                   set("wordName", teacherCourseware.getWordName()).
                   set("wordUrl", teacherCourseware.getWordUrl()).
                   set("pictureUrlList", teacherCourseware.getPicturePreview()).
                   set("serieId", teacherCourseware.getSerieId()).
                   set("serieName", serieName).
                   set("desc", teacherCourseware.getExamineExt()).
                   set("isUserUpload", teacherCourseware.getIsUserUpload()).
                   set("coursewareFileImages", teacherCourseware.getCoursewareFilePreview()).
                   set("commentNum",teacherCourseware.getCommentNum()).
                   set("visitNum",teacherCourseware.getVisitNum()).
                   set("downloadNum",teacherCourseware.getDownloadNum()).
                   set("teacherName",teacher.fetchRealname()).
                   set("teacherId",teacherCourseware.getTeacherId()).
                   set("schoolName",teacher.getTeacherSchoolName()).
                   set("previewList",attachmentViews).
                   set("awardLevelId",teacherCourseware.getAwardLevelId()).
                   set("awardLevelName",teacherCourseware.getAwardLevelName()).
                   set("awardIntroduction",teacherCourseware.getAwardIntroduction()).
                   set("awardPreview",CollectionUtils.isEmpty(teacherCourseware.getAwardPicturePreview()) ? "" : teacherCourseware.getAwardPicturePreview().get(0)).
                   set("zipFileUrl",teacherCourseware.getZipFileUrl()).
                   set("canvassNum",SafeConverter.toInt(teacherCourseware.getCanvassNum())).
                   set("canvassHelperNum",SafeConverter.toInt(teacherCourseware.getCanvassHelperNum()))
                ;

        User user = currentUser();
        if(user != null) {
            if (!user.isTeacher()) user = null;
        }
        String openId = getRequestContext().getAuthenticatedOpenId();

        result.set("canvassItem", false);

        // 人气作品周榜前三
        if (teacherCourseware.getSubject() != null) {

            result.set("weekPopularityTop3", false);
            result.set("weekPopularityRank", 0);

            // 高分作品月榜前三
            result.set("monthExcellentTop3", false);
            result.set("monthExcellentRank", 0);

            // 是否进入了拉票版
            MapMessage canvass = teacherCoursewareContestServiceClient.loadCanvassData(teacherCourseware.getSubject().name());
            if (canvass.isSuccess()) {
                List<Map<String, Object>> canvassData = (List<Map<String, Object>>) canvass.get("data");
                Map<String, Object> canvassItem = canvassData.stream()
                        .filter(p -> Objects.equals(SafeConverter.toString(p.get("coursewareId")), teacherCourseware.getId()))
                        .findAny().orElse(null);
                if (canvassItem != null) {
                    result.set("canvassItem", true);
                }
            }
        }

        // 分享次数
        result.set("shareNum", teacherCoursewareContestServiceClient.loadCourseShareNum(teacherCourseware.getId()));

        if (user == null && StringUtils.isEmpty(openId)) {
            result.set("surplus", 1);
            result.set("totalSurplus", 1);
        } else {
            Map<String, Integer> surplus = teacherCoursewareContestServiceClient.surplus(user == null ? null : user.getId(), openId, id);
            result.set("surplus", MapUtils.getIntValue(surplus, "surplus", 0));
            result.set("totalSurplus", MapUtils.getIntValue(surplus, "totalSurplus", 0));
        }

        result.add("wechatLogined", StringUtils.isNotEmpty(openId));
        result.add("userLogined", user != null);

        result.add("authed", user != null && Objects.equals(user.getAuthenticationState(), AuthenticationState.SUCCESS.getState()));
        result.add("leftTime", (CANVASS_VOTE_END_DATE.getTime() - System.currentTimeMillis()));

        return result;
    }

    @RequestMapping(value = "teacherinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchTeacherInfo() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录");
        }
        return MapMessage.successMessage().set("subjects", teacherDetail.getSubjects());
    }

    @RequestMapping(value = "booklist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchBooklist() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录").setErrorCode(coursewareErrorCode.NOTEXIST_ERROR);
        }

        int term = getRequestInt("term");
        int clazz = getRequestInt("clazzLevel");
//        String seriesId = getRequestString("serieId");
        String subjectStr = getRequestString("subject");
        Subject subject = Subject.of(subjectStr);

        //新版接口不需要校验老师学科,teacherDetail.getSubjects().contains(subject)
        if (subject == null || term <= 0 || clazz <= 0 || term > 2 || clazz > 6 ) {
            return MapMessage.errorMessage().setInfo("参数错误");
        }

        List<Map<String, Object>> res = new ArrayList<>();
        List<NewBookProfile> bookProfiles = newContentLoaderClient.loadBooksByClassLevelAndTermAndSeriesIdAndBookType(subject, ClazzLevel.parse(clazz), Term.of(term), null, null);
        if (CollectionUtils.isNotEmpty(bookProfiles)) {
            String name = getRequestString("name");
            List<NewBookProfile> bookProfileList = bookProfiles.stream()
                    .filter(e -> StringUtils.isBlank(name) || e.getName().contains(name))
                    .collect(Collectors.toList());
            NewBookProfile topicNewBookProfile = new NewBookProfile();
            topicNewBookProfile.setId(TOPIC_BOOK_ID);
            topicNewBookProfile.setName(TOPIC_NAME);
            bookProfileList.add(topicNewBookProfile);
            if (CollectionUtils.isNotEmpty(bookProfileList)) {
                Map<String, List<NewBookCatalog>> unitListMap = newContentLoaderClient.loadChildren(bookProfileList.stream().map(NewBookProfile::getId).collect(Collectors.toList()), BookCatalogType.UNIT);
                for (NewBookProfile newBookProfile : bookProfileList) {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", newBookProfile.getId());
                    bookMap.put("name", newBookProfile.getName());
                    List<Map<String, String>> unit = new ArrayList<>();
                    List<NewBookCatalog> unitlist = MapUtils.isNotEmpty(unitListMap) && CollectionUtils.isNotEmpty(unitListMap.get(newBookProfile.getId())) ?
                            unitListMap.get(newBookProfile.getId()) : Collections.emptyList();
                    if (CollectionUtils.isEmpty(unitlist)){
                        unitlist = new ArrayList<>();
                        NewBookCatalog unitNewBookProfile = new NewBookCatalog();
                        unitNewBookProfile.setId(TOPIC_UNIT_ID);
                        unitNewBookProfile.setName(TOPIC_NAME);
                        unitlist.add(unitNewBookProfile);
                    }
                    unitlist.forEach(e -> {
                        Map<String, String> unitMap = new HashMap<>();
                        unitMap.put("unitId", e.getId());
                        unitMap.put("unitName", e.getName());
                        unit.add(unitMap);
                    });
                    bookMap.put("unitList", unit);
                    res.add(bookMap);
                }
            }
        }

        return MapMessage.successMessage().set("books", res);
    }

    @RequestMapping(value = "myworks/create.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createCourseware() {
        if (TeacherCoursewareConstants.isCloseUpload()) {
            return TeacherCoursewareConstants.CLOSE_UPLOAD_MSG;
        }

        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录");
        }
        return teacherCoursewareContestServiceClient.createSimpleCourseware(teacherDetail);
    }

    @RequestMapping(value = "myworks/updatebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateMyCoursewareBook() {
        if (TeacherCoursewareConstants.isCloseUpload()) {
            return TeacherCoursewareConstants.CLOSE_UPLOAD_MSG;
        }

        TeacherDetail teacherDetail = currentTeacherDetail();
        String id = getRequestString("id");

        MapMessage validateMessage = validateCoursewareRequest(teacherDetail, id);
        if (validateMessage != null) {
            return validateMessage;
        }

        TeacherCoursewareBookInfo content = new TeacherCoursewareBookInfo();
        String subjectString = getRequestString("subject");
        String clazzLevelStr = getRequestString("clazzLevel");
        String termStr = getRequestString("term");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("lessonId");
//        String serieId = getRequestString("serieId");
        Subject subject = Subject.safeParse(subjectString);
        if (subject == null || StringUtils.isBlank(termStr) || StringUtils.isBlank(clazzLevelStr)) {
            return MapMessage.errorMessage().setInfo("参数错误");
        }
        content.setSubject(subject);

        int term = SafeConverter.toInt(termStr);
        if (term <= 0 || term > 2) {
            return MapMessage.errorMessage().setInfo("参数错误");
        }

        content.setTermType(term);
        int clazz = SafeConverter.toInt(clazzLevelStr);
        if (clazz <= 0 || clazz > 6) {
            return MapMessage.errorMessage().setInfo("参数错误");
        }

        NewBookProfile bookProfile = newContentLoaderClient.loadBook(bookId);
        if (!TOPIC_BOOK_ID.equals(bookId)){
            if (bookProfile == null || subject.getId() != bookProfile.getSubjectId()) {
                return MapMessage.errorMessage().setInfo("参数错误");
            }
        }

        content.setClazzLevel(clazz);
        content.setBookId(bookId);
        content.setUnitId(unitId);
        content.setLessonId(lessonId);
//        content.setSerieId(serieId);

        return teacherCoursewareContestServiceClient.updateBookInfo(id, content);
    }

    @RequestMapping(value = "myworks/updatecontent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateMyCoursewareContent() {
        if (TeacherCoursewareConstants.isCloseUpload()) {
            return TeacherCoursewareConstants.CLOSE_UPLOAD_MSG;
        }

        TeacherDetail teacherDetail = currentTeacherDetail();
        String id = getRequestString("id");
        String status = getRequestString("status");

        MapMessage validateMessage = validateCoursewareRequest(teacherDetail, id);
        if (validateMessage != null) {
            return validateMessage;
        }

        String title = getRequestString("title");
        if (StringUtils.isBlank(title)) {
            return MapMessage.errorMessage().setInfo("参数错误");
        }

        // 如果状态是被驳回,改为未提交
        if (TeacherCourseware.Status.REJECTED.name().equals(status)){
            teacherCoursewareContestServiceClient.updateStatus(id,TeacherCourseware.Status.DRAFT.name());
        }

        String description = getRequestString("description");

        return teacherCoursewareContestServiceClient.updateContent(id, title, description);
    }

    @RequestMapping(value = "myworks/uploadfile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadMyCoursewareFile(@RequestParam(value = "file",required = false) MultipartFile multipartFiles) {
        if (TeacherCoursewareConstants.isCloseUpload()) {
            return TeacherCoursewareConstants.CLOSE_UPLOAD_MSG;
        }

        TeacherDetail teacherDetail = currentTeacherDetail();
        String id = getRequestString("id");

        MapMessage validateMessage = validateCoursewareRequest(teacherDetail, id);
        if (validateMessage != null) {
            return validateMessage;
        }

        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            List<MultipartFile> files = multipartRequest.getFiles("file");
            if (CollectionUtils.isEmpty(files) || files.size() != 1) {
                return MapMessage.errorMessage("检查文件参数");
            }

            MultipartFile file = files.get(0);

            String filename = file.getOriginalFilename();

            String fileType = filename.substring(filename.lastIndexOf(".")+1,filename.length());

            if (!Arrays.asList("jpg", "png", "ppt","pptx","docx","doc","rar","zip","gz","7z").contains(fileType)){
                return MapMessage.errorMessage("文件后缀不对");
            }

            String ossFile = OSSManageUtils.upload(file, "teacher/courseware", fileType);

            if (StringUtils.isBlank(ossFile)) {
                return MapMessage.errorMessage("服务器异常，请稍后上传");
            }

            MapMessage message = new MapMessage();
            if (fileType.contains("ppt")){
                message = teacherCoursewareContestServiceClient.updateFileInfo(id, ossFile, file.getOriginalFilename());
            } else if (fileType.contains("doc")){
                message = teacherCoursewareContestServiceClient.updateWordFileInfo(id, ossFile, file.getOriginalFilename());
            } else if (Arrays.asList("rar","zip","gz","7z").contains(fileType)){
                message = teacherCoursewareContestServiceClient.updateCompressedFileInfo(id, ossFile, file.getOriginalFilename());
            }
            return message.set("url",ossFile);
        } catch (Exception e) {
            logger.error("uploadMyCoursewareFile error.", e);
            return MapMessage.errorMessage("服务器异常，请稍后上传");
        }
    }

    @RequestMapping(value = "updateFileUrl.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateFileUrl() {
        if (TeacherCoursewareConstants.isCloseUpload()) {
            return TeacherCoursewareConstants.CLOSE_UPLOAD_MSG;
        }

        try {
            String id = getRequestString("id");
            String fileUrl = getRequestString("fileUrl");
            String name = getRequestString("name");

            teacherCoursewareContestServiceClient.updateFileUrl(id,fileUrl,name);
            teacherCoursewareContestServiceClient.updateFileInfo(id, fileUrl, name);

            return MapMessage.successMessage();
        } catch (Exception e){
            logger.error("uploadFileUrl error.", e);
            return MapMessage.errorMessage("服务器异常，请稍后上传");
        }

    }

    @RequestMapping(value = "uploadPictures.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadPictureFile(HttpServletRequest request) {
        if (TeacherCoursewareConstants.isCloseUpload()) {
            return TeacherCoursewareConstants.CLOSE_UPLOAD_MSG;
        }

        String id = getRequestString("id");

        try {
            MultiValueMap<String,MultipartFile> multiValuedMap = ((DefaultMultipartHttpServletRequest)request).getMultiFileMap();

            List<MultipartFile> multipartFiles = multiValuedMap.get("file");

            if ( !multipartFiles.isEmpty() ){

                for (MultipartFile multipartFile : multipartFiles){

                    String filename = multipartFile.getOriginalFilename();

                    String fileType = filename.substring(filename.lastIndexOf(".")+1,filename.length());

                    if (!Arrays.asList("jpg", "png").contains(fileType)){
                        return MapMessage.errorMessage("文件后缀不对");
                    }

                    String ossFile = OSSManageUtils.upload(multipartFile, "teacher/courseware/image", fileType);

                    if (StringUtils.isBlank(ossFile)) {
                        return MapMessage.errorMessage("服务器异常，请稍后上传");
                    }

                    teacherCoursewareContestServiceClient.updatePictureFileInfo(id, ossFile, multipartFile.getOriginalFilename());

                }
            }

            TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(id);
            List<Map<String,String>> pictureList = teacherCourseware.getPicturePreview();

            return MapMessage.successMessage().set("pictureUrl",pictureList);
        } catch (Exception e){
            logger.error("uploadPicture error.", e);
            return MapMessage.errorMessage("服务器异常，请稍后上传");
        }

    }

    @RequestMapping(value = "deletePicture.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deletePicture() {
        String pictureUrl = getRequestString("pictureUrl");
        String id = getRequestString("id");
        try {
            TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(id);
            List<Map<String,String>> pictureList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(teacherCourseware.getPicturePreview())){
                pictureList = teacherCourseware.getPicturePreview();
                Iterator<Map<String,String>> pictureIterator = pictureList.iterator();
                while (pictureIterator.hasNext()){
                    Map<String,String> pictureMap = pictureIterator.next();
                    String pictureOosUrl = pictureMap.get("url");
                    if (pictureUrl.equals(pictureOosUrl)){
                        pictureIterator.remove();
                    }
                }
            }
            teacherCoursewareContestServiceClient.deletePictureUrl(id,pictureList);
            return MapMessage.successMessage().set("id",id).set("pictureUrlList",pictureList);
        } catch (Exception e){
            return MapMessage.errorMessage("服务器异常，请稍后删除");
        }
    }

    @RequestMapping(value = "uploadCover.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadCover() {
        if (TeacherCoursewareConstants.isCloseUpload()) {
            return TeacherCoursewareConstants.CLOSE_UPLOAD_MSG;
        }

        TeacherDetail teacherDetail = currentTeacherDetail();
        String id = getRequestString("id");
        // 兼容IE
        String imageName = getRequestString("name");
        String imageUrl = getRequestString("url");
        Boolean isUserUpload = getRequestBool("isUserUpload");

        MapMessage validateMessage = validateCoursewareRequest(teacherDetail, id);
        if (validateMessage != null) {
            return validateMessage;
        }

        MapMessage message = new MapMessage();

        try {

            if (StringUtils.isEmpty(imageUrl)){

                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                List<MultipartFile> files = multipartRequest.getFiles("file");
                if (CollectionUtils.isEmpty(files) || files.size() != 1) {
                    return MapMessage.errorMessage("检查文件参数");
                }

                MultipartFile file = files.get(0);

                String filename = file.getOriginalFilename();

                String fileType = filename.substring(filename.lastIndexOf(".")+1,filename.length());

                if (!Arrays.asList("jpg","png").contains(fileType)){
                    return MapMessage.errorMessage("文件后缀不对");
                }

                String ossFile = OSSManageUtils.upload(file, "teacher/courseware/cover", fileType);

                if (StringUtils.isBlank(ossFile)) {
                    return MapMessage.errorMessage("服务器异常，请稍后上传");
                }

                message = teacherCoursewareContestServiceClient.updateCoverFile(id, ossFile,
                        file.getOriginalFilename(),isUserUpload);
                message.set("url",ossFile);

            } else {
                message = teacherCoursewareContestServiceClient.updateCoverFile(id, imageUrl, imageName,isUserUpload);
                message.set("url",imageUrl);
            }

            return message;
        } catch (Exception e) {
            logger.error("uploadMyCoursewareFile error.", e);
            return MapMessage.errorMessage("服务器异常，请稍后上传");
        }
    }

    @RequestMapping(value = "myworks/commit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage commitMyCourseware() {
        if (TeacherCoursewareConstants.isCloseUpload()) {
            return TeacherCoursewareConstants.CLOSE_UPLOAD_MSG;
        }

        TeacherDetail teacherDetail = currentTeacherDetail();
        String id = getRequestString("id");

        MapMessage validateMessage = validateCoursewareRequest(teacherDetail, id);
        if (validateMessage != null) {
            return validateMessage;
        }
        TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(id);
        if (teacherCourseware.getClazzLevel() == null || teacherCourseware.getClazzLevel() <= 0 ||
                StringUtils.isBlank(teacherCourseware.getUnitId()) || StringUtils.isBlank(teacherCourseware.getBookId()) ||
                StringUtils.isBlank(teacherCourseware.getCoursewareFile()) || StringUtils.isBlank(teacherCourseware.getCoursewareFileName()) ||
                StringUtils.isBlank(teacherCourseware.getTitle()) ||
                teacherCourseware.getTermType() == null || teacherCourseware.getTermType() <= 0) {
            return MapMessage.errorMessage().setInfo("课件内容不全，请检查");
        }
        return teacherCoursewareContestServiceClient.updateToExamining(id);
    }

    @RequestMapping(value = "myworks/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteMyCourseware() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        String id = getRequestString("id");

        MapMessage validateMessage = validateCoursewareRequest(teacherDetail, id);
        if (validateMessage != null) {
            return validateMessage;
        }

        return teacherCoursewareContestServiceClient.deleteCourseware(id);
    }

    /**
     * 获取老师个人信息
     * @return MapMessage
     */
    @RequestMapping(value = "userInfo.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchUserInfo(){
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage().setInfo("请先登录").setErrorCode(coursewareErrorCode.NOTEXIST_ERROR);
        }
        TeacherDetail teacherDetail = currentTeacherDetail();
        try {
            Map<String,Object> userInfo = new HashMap<>();
            userInfo.put("name",teacherDetail.fetchRealname());
            userInfo.put("schoolName",teacherDetail.getTeacherSchoolName());
            userInfo.put("subject",teacherDetail.getSubject().getValue());
            userInfo.put("regionName",teacherDetail.getRootRegionName() + teacherDetail.getCityName()
                    + teacherDetail.getCountyName());
            TeacherCoursewareParam teacherCoursewareParam = new TeacherCoursewareParam();
            Long userId = teacherDetail.getId();
            teacherCoursewareParam.setTeacherId(userId);
            long coursewareNums = teacherCoursewareContestServiceClient
                    .fetchTeacherCoursewareNumByTeacherId(teacherCoursewareParam);
            userInfo.put("productNum", coursewareNums);
            userInfo.put("teacherId",userId);
            return MapMessage.successMessage().set("userInfo",userInfo);
        } catch (Exception e){
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "subjects.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchSubject(){
        try {
            List<Map<String,Object>> subjectList = new ArrayList<>();
            for (int i = 101 ; i < 104 ; i ++ ){
                Map<String,Object> subjectInfo = new HashMap<>();
                subjectInfo.put("id",i);
                subjectInfo.put("name",Subject.fromSubjectId(i).getValue());
                subjectInfo.put("englishName",Subject.fromSubjectId(i).name());
                subjectList.add(subjectInfo);
            }
            return MapMessage.successMessage().set("data",subjectList);
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 根据包班信息返回学科
     * @return
     */
    @RequestMapping(value = "multiClazzInfo.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage multiClazzInfo(){
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage().setInfo("请先登录");
            }
            List<Subject> teacherDetailSubjects = teacherDetail.getSubjects();
            Map<String,Object> subjectList = new HashMap<>();
//            List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(teacherDetail.getId());
//            if (CollectionUtils.isNotEmpty(subTeacherIds)){
//                subjectList.put("name",Subject.CHINESE.name());
//            } else
            // 是包班返回数学,不是包班返回主授学科
            // 上面是原来的逻辑,现在无论什么都返回主授学科,代码先简单注掉,产品可能后期改
            if (teacherDetailSubjects != null && teacherDetailSubjects.size() != 0){
                subjectList.put("name",StringUtils.isEmpty(teacherDetailSubjects.get(0).name())
                        ? Subject.CHINESE.name() : teacherDetailSubjects.get(0).name());
            }
            return MapMessage.successMessage().set("data",subjectList);
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    @RequestMapping(value = "clazzs.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchClazzLevel(){
        try {
            List<Map<String,Object>> clazzList = new ArrayList<>();
            for (int i = 1 ; i < 7 ; i ++ ){
                Map<String,Object> clazzMap = new HashMap<>();
                clazzMap.put("id",i);
                clazzMap.put("name",ClazzLevel.getDescription(i));
                clazzList.add(clazzMap);
            }
            return MapMessage.successMessage().set("data",clazzList);
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    @RequestMapping(value = "terms.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchTerm(){
        try {
            List<Map<String,Object>> list = new ArrayList<>();
            for (int i = 1 ; i < 3 ; i ++ ){
                Map<String,Object> terms = new HashMap<>();
                terms.put("id",i);
                terms.put("name",Term.of(i).getValue());
                list.add(terms);
            }
            return MapMessage.successMessage().set("data",list);
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 查询是否参加过活动
     *
     * @return Y:参加过,N:未参加
     */
    @RequestMapping(value = "joinInfo.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchJoinInfo() {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage().setInfo("请登录后查询是否报名").setErrorCode(coursewareErrorCode.NOTEXIST_ERROR);
            }
            TeacherNewTermActivityProgress progress = teacherActivityServiceClient.
                    loadParticipateActivityInfo(TeacherNewTermActivityCategory.TeacherCoursewareContest.getId(),
                            teacherDetail.getId());
            return progress == null ? MapMessage.successMessage().set("data","N") : MapMessage.successMessage().set("data","Y");
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 获取课程信息列表
     *
     * @param unitId 单元 ID
     * @return 课程信息列表
     */
    @RequestMapping(value = "lessions.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchLessionList(@RequestParam(name = "unitId") String unitId){
        try {
            Map<String, List<NewBookCatalog>> lessionListMap = newContentLoaderClient.
                    loadChildren(Arrays.asList(unitId), BookCatalogType.LESSON);
            List<NewBookCatalog> lessionlist = lessionListMap.get(unitId);
            List<LessionView> lessionViews = LessionView.Builder.build(lessionlist);
            return MapMessage.successMessage().set("data",lessionViews);
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 获取最新作品列表
     *
     * @param num 展示个数
     * @return 最新作品列表
     */
    @RequestMapping(value = "newest.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchNewestCreateInfo(@RequestParam(name = "limitNum") Integer num){
        try {
            List<TeacherCourseware> teacherCoursewareList = teacherCoursewareContestServiceClient.fetchNewestInfo(num == null ? MAX_NEWEST_COURSEWARE : num);
            List<TeacherCoursewarView> dtoList = TeacherCoursewarView.Builder.build(teacherCoursewareList);
            for (TeacherCoursewarView dto : dtoList){
                TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(dto.getTeacherId());
                if (teacher != null){
                    dto.setTeacherName(teacher.fetchRealname());
                    dto.setSchoolName(teacher.getTeacherSchoolName());
                }
            }
            return MapMessage.successMessage().set("data",dtoList);
        } catch (Exception e) {
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 获取教材版本列表
     *
     * @param subjectId 学科 ID
     * @return 教材版本列表信息
     */
    @RequestMapping(value = "series.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage querySeries(@RequestParam(name = "subjectId") Integer subjectId) {
        try {
            Subject subject = Subject.fromSubjectId(subjectId);
            List<NewBookCatalog> newBookCatalogList = newContentLoaderClient.loadShowSeriesBySubject(subject);
            List<NewBookProfile> bookProfiles = newContentLoaderClient.loadBooks(subject);
            List<SeriesView> seriesViewList = SeriesView.Builder.build(newBookCatalogList,bookProfiles);
            return MapMessage.successMessage().set("data",seriesViewList);
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 下载教案模板及作品评选标准
     *
     * @param response
     */
    @RequestMapping(value = "downloadExample.vpage", method = RequestMethod.GET)
    public void downloadExample(HttpServletResponse response) {
        try {
            Resource resource = new ClassPathResource(EXAMPLE_PATH);
            if (!resource.exists()) {
                logger.error("example is not exists");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            HttpRequestContextUtils.currentRequestContext().downloadFile(EXAMPLE_NAME,
                    "application/x-zip-compressed",
                    outStream.toByteArray());
        } catch (Exception ex) {
            logger.error("download example is failed", ex);
        }
    }

    private MapMessage validateCoursewareRequest(TeacherDetail teacherDetail, String id) {

        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录");
        }

        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().setInfo("参数错误");
        }

        TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(id);
        if (teacherCourseware == null || (teacherCourseware.getDisabled() != null && teacherCourseware.getDisabled())) {
            return MapMessage.errorMessage().setInfo("课程不存在或者已经被删除");
        }

        if (!teacherDetail.getId().equals(teacherCourseware.getTeacherId())) {
            return MapMessage.errorMessage().setInfo("课程错误");
        }

        if (teacherCourseware.getStatus() == TeacherCourseware.Status.EXAMINING || teacherCourseware.getStatus() == TeacherCourseware.Status.PUBLISHED) {
            return MapMessage.errorMessage().setInfo("课程审核中或者已发布不能删除或者修改");
        }

        return null;
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

        MapMessage signatureResult = UploadOssManageUtils.getSignature(uploadFileType, "washington", getResponse());
        if (signatureResult != null)
            return MapMessage.successMessage().add("data", signatureResult);
        return MapMessage.errorMessage();
    }

    /**
     * 获取全部作品列表
     * @param clazzLevel 年级
     * @param subject 学科
     * @param pageNum 第几页
     * @param pageSize 每页多少条数据
     * @param orderMode 排序规则,1 是按评分排序,2 是按最新时间排序
     * @param topThree 是否是查询前三名
     * @return
     */
    @RequestMapping(value = "allCourses.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchAllPublishedCourse(@RequestParam(name = "clazzLevel",required = false) Integer clazzLevel,
                                              @RequestParam(name = "subject",required = false) Integer subject,
                                              @RequestParam(name = "pageNum",required = false) Integer pageNum,
                                              @RequestParam(name = "pageSize",required = false) Integer pageSize,
                                              @RequestParam(name = "orderMode",required = false) Integer orderMode,
                                              @RequestParam(name = "topThree",required = false) Boolean topThree,
                                              @RequestParam(name = "awardLevelId",required = false) Integer awardLevelId){
        try {
            pageNum = pageNum == null ? DEFAULT_PAGE_NUM + 1 : pageNum + 1;
            pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;

            // 入参校验
            if ( null == orderMode ){
                return MapMessage.errorMessage().setInfo("排序参数为空");
            }
            // 从 buffer 获取所有数据
            List<TeacherCourseware> allTeacherCoursewareList = teacherCoursewareContestServiceClient.loadTeacherCoursewareAll();
            // 只要已发布的数据
            allTeacherCoursewareList = allTeacherCoursewareList.stream()
                    .filter(e->e.getStatus() == PUBLISHED)
                    .collect(Collectors.toList());

            // 过滤数据:
            // 1、原始数据是 zip ,ppt 未被解出来;2、word 文件非 v.17xueba.com;3、没有打包 zip 文件的
            allTeacherCoursewareList = allTeacherCoursewareList.stream().
                    filter(e-> null != e.getWordUrl() && e.getWordUrl().toLowerCase().contains("v.17xueba.com")).
                    filter(e-> null != e.getZipFileUrl() && !"".equals(e.getZipFileUrl())).
                    filter(e->
                        (( null != e.getCoursewareFileName()
                                && e.getCoursewareFileName().toLowerCase().contains("zip")
                                && e.getPptCoursewareFile() != null)
                                || e.getCoursewareFileName().toLowerCase().contains("ppt")
                                || ( null != e.getCoursewareFileName()
                                && e.getCoursewareFileName().toLowerCase().contains("rar")
                                && e.getPptCoursewareFile() != null))
                    ).
                    collect(Collectors.toList());

            // 关键字
            String keyword = getRequestString("keyword");
            if (StringUtils.isNoneBlank(keyword)) {
                allTeacherCoursewareList = allTeacherCoursewareList.stream().
                        filter(p -> (p.getTitle().toLowerCase().contains(keyword.toLowerCase())) || (StringUtils.equals(p.getTeacherName(), keyword)))
                        .collect(Collectors.toList());
            }

            // 0 是全部年级
            if (null != clazzLevel && clazzLevel != 0){
                allTeacherCoursewareList = allTeacherCoursewareList.stream().
                        filter(e->clazzLevel.equals(e.getClazzLevel())).collect(Collectors.toList());
            }

            // 0 是全部学科
            if (null != subject && subject != 0){
                allTeacherCoursewareList = allTeacherCoursewareList.stream().
                        filter(e->e.getSubject().getId() == subject).collect(Collectors.toList());
            }

            // 0 是不选奖项
            if (null != awardLevelId && awardLevelId != 0){
                allTeacherCoursewareList = allTeacherCoursewareList.stream().
                        filter(e-> Objects.equals(awardLevelId, e.getAwardLevelId()))
                        .collect(Collectors.toList());
            }

            // 此处是为了查排名前三用的, 优先有获过奖的
            final boolean onlyTopTree = null != topThree && topThree;

            // 1 是按评分排序,2 是按最新时间排序
            if (orderMode == 2){
                Collections.sort(allTeacherCoursewareList, (o1, o2) -> {
                    if (onlyTopTree) {
                        int o1AwardLvl = SafeConverter.toInt(o1.getAwardLevelId());
                        if (o1AwardLvl <= 0) {
                            o1AwardLvl = 20;
                        }
                        int o2AwardLvl = SafeConverter.toInt(o2.getAwardLevelId());
                        if (o2AwardLvl <= 0) {
                            o2AwardLvl = 20;
                        }

                        if (o1AwardLvl != o2AwardLvl) {
                            return Integer.compare(o1AwardLvl, o2AwardLvl);
                        } else {
                            return Long.compare(o2.getCreateTime().getTime(), o1.getCreateTime().getTime());
                        }
                    }

                    return Long.compare(o2.getExamineUpdateTime().getTime(), o1.getExamineUpdateTime().getTime());
                });
            } else if (orderMode == 1){
                Collections.sort(allTeacherCoursewareList, (o1, o2) -> {
                            int o1Score = 0;
                            if (SafeConverter.toInt(o1.getCommentNum()) >= 3) {
                                o1Score = SafeConverter.toInt(o1.getTotalScore());
                            }

                            int o2Score = 0;
                            if (SafeConverter.toInt(o2.getCommentNum()) >= 3) {
                                o2Score = SafeConverter.toInt(o2.getTotalScore());
                            }

                            if (onlyTopTree) {
                                int o1AwardLvl = SafeConverter.toInt(o1.getAwardLevelId());
                                if (o1AwardLvl <= 0) {
                                    o1AwardLvl = 20;
                                }
                                int o2AwardLvl = SafeConverter.toInt(o2.getAwardLevelId());
                                if (o2AwardLvl <= 0) {
                                    o2AwardLvl = 20;
                                }

                                if (o1AwardLvl != o2AwardLvl) {
                                    return Integer.compare(o1AwardLvl, o2AwardLvl);
                                } else {
                                    return Integer.compare(o2Score, o1Score);
                                }
                            }

                            return Integer.compare(o2Score, o1Score);
                        }
                );
            }

            // 内存分页
            List<TeacherCourseware> retData = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(allTeacherCoursewareList)){
                Pager<TeacherCourseware> pager = Pager.create(allTeacherCoursewareList, pageSize);
                retData = pager.getPagedList(pageNum);
            }

            // 转换视图模型
            List<TeacherCoursewarView> dtoList = TeacherCoursewarView.Builder.buildForAll(retData);
            if (CollectionUtils.isNotEmpty(dtoList)){

                for (TeacherCoursewarView dto : dtoList){
                    TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(dto.getTeacherId());

                    if (teacher != null){
                        dto.setTeacherName(teacher.fetchRealname());
                        dto.setSchoolName(teacher.getTeacherSchoolName());
                    }

                    if (StringUtils.isNotBlank(dto.getSeriesId())) {
                        NewBookProfile bookProfile = null;
                        bookProfile = newContentLoaderClient.loadBook(dto.getSeriesId());
                        dto.setBookName(bookProfile == null ? "" : bookProfile.getName());
                    }

//                    Integer weekPopularityRank = teacherCoursewareContestServiceClient.getWeekPopularityTopRank(dto.getSubject(), dto.getCourseId());
//                    if (weekPopularityRank > 0) {
//                        dto.setWeekPopularityTop3(true);
//                        dto.setWeekPopularityRank(weekPopularityRank);
//                    } else {
                        dto.setWeekPopularityTop3(false);
                        dto.setWeekPopularityRank(0);
//                    }

                    // 高分作品月榜前三
//                    Integer monthExcellentRank = teacherCoursewareContestServiceClient.getMonthExcellentTopRank(dto.getSubject(), dto.getCourseId());
//                    if (monthExcellentRank > 0) {
//                        dto.setMonthExcellentTop3(true);
//                        dto.setMonthExcellentRank(monthExcellentRank);
//                    } else {
                        dto.setMonthExcellentTop3(false);
                        dto.setMonthExcellentRank(0);
//                    }
                }
            }

            return MapMessage.successMessage().add("data", dtoList)
                    .set("pageNum",pageNum)
                    .set("pageSize",pageSize)
                    .set("total", allTeacherCoursewareList.size());
        } catch (Exception e){
            logger.error("exception occurs when fetchAllPublishedCourse.", e);
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 领取资源接口
     * @return 领取信息
     */
    @RequestMapping(value = "fetchZipResource.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchCourseResource(){
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录").setErrorCode(coursewareErrorCode.COURSEWARE_NOTEXIST_ERROR);
        }
        Long teacherId = teacherDetail.getId();
        String courseId = getRequestString("courseId");
        String zipUrl = getRequestString("zipUrl");
        String title = getRequestString("title");
        MapMessage message = teacherResourceServiceClient.saveCoursewareResource(teacherId,courseId,title,zipUrl);
        return message;
    }

    /**
     * 查看是否领取过资源接口
     * @return true 领取过,false 未领取过
     */
    @RequestMapping(value = "receivedResource.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage receivedResource(){
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return MapMessage.errorMessage().setInfo("请先登录").setErrorCode(coursewareErrorCode.NOTEXIST_ERROR);
        }
        Long teacherId = user.getId();
        String courseId = getRequestString("courseId");
        try {
            Set<String> coursewareResources = teacherResourceServiceClient.loadCoursewareResourceIdByUserId(teacherId);
            if (coursewareResources.contains(courseId)){
                return MapMessage.successMessage().set("data",true);
            } else {
                return MapMessage.successMessage().set("data",false);
            }
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 查看已领取资源接口
     * @return 已领取的资源列表
     */
    @RequestMapping(value = "personalResources.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage personalResources(){
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage().setInfo("请先登录").setErrorCode(coursewareErrorCode.COURSEWARE_NOTEXIST_ERROR);
            }
            Long teacherId = teacherDetail.getId();
            List<TeacherResourceRef> teacherResourceRefList = teacherResourceServiceClient.loadTeacherResourceByUserId(teacherId);
            List<ResourceView> resourceViewList = new ArrayList<>();
            teacherResourceRefList.stream().forEach(e->resourceViewList.add(ResourceView.Builder.build(e)));
            return MapMessage.successMessage().set("data",resourceViewList);
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 获取奖项枚举接口
     * @return
     */
    @RequestMapping(value = "awardsLevels.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAwardsLevel(){
        try {
            List<KeyValue<String, String>> keyValues = new ArrayList<>();
            keyValues = EnumListBuilder.build(AwardEnum.values(),
                    (Describable<AwardEnum>) i -> i.desc);
            return MapMessage.successMessage().set("data",keyValues);
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    /**
     * 上传曾获奖项接口
     *
     * @return 获奖信息
     */
    @RequestMapping(value = "uploadAwards.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadAwards(HttpServletRequest request) {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            String id = getRequestString("id");
            String awardLevelName = getRequestString("awardLevelName");
            Integer awardLevelId = getRequestInt("awardLevelId");
            String awardIntroduction = getRequestString("awardIntroduction");
            String awardUrl = getRequestString("awardUrl");
            String awardName = getRequestString("awardName");
            MultiValueMap<String,MultipartFile> multiValuedMap = ((DefaultMultipartHttpServletRequest)request).getMultiFileMap();

            List<MultipartFile> multipartFiles = multiValuedMap.get("file");

            // awardLevelId == -1 是不用传奖项
            if ( awardLevelId != -1 && "".equals(awardIntroduction)){
                return MapMessage.errorMessage().setInfo("没有奖项介绍");
            } else if (awardLevelId != -1 && (null == multipartFiles && "".equals(awardUrl))){
                return MapMessage.errorMessage().setInfo("没有奖项图片");
            }

            if ( null != multipartFiles && !multipartFiles.isEmpty() && "".equals(awardUrl) ){

                // 现在只有一张图片,产品后期可能会改,所以这里先留着
                for (MultipartFile multipartFile : multipartFiles){

                    String filename = multipartFile.getOriginalFilename();

                    String fileType = filename.substring(filename.lastIndexOf(".")+1,filename.length());

                    if (!Arrays.asList("jpg", "png").contains(fileType)){
                        return MapMessage.errorMessage("文件后缀不对");
                    }

                    String ossFile = OSSManageUtils.upload(multipartFile, "teacher/courseware/award", fileType);

                    if (StringUtils.isBlank(ossFile)) {
                        return MapMessage.errorMessage("服务器异常，请稍后上传");
                    }

                    teacherCoursewareContestServiceClient.updateAwardInfo(id, ossFile, multipartFile.getOriginalFilename(),awardLevelName,awardLevelId,awardIntroduction);
                }
            } else if ( null == multipartFiles && !"".equals(awardUrl)){
                teacherCoursewareContestServiceClient.updateAwardInfo(id, awardUrl, awardName,awardLevelName,awardLevelId,awardIntroduction);
            }
            else if ( null == multipartFiles && "".equals(awardUrl) ){
                teacherCoursewareContestServiceClient.updateAwardInfo(id, "", "",awardLevelName,awardLevelId,awardIntroduction);
            }
            TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(id);
            List<Map<String,String>> pictureList = teacherCourseware.getAwardPicturePreview();
            return MapMessage.successMessage().set("data",CollectionUtils.isEmpty(pictureList) ? new HashMap<>() : pictureList.get(0));
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo(e.getMessage());
        }
    }

    // 根据当前时间返回截止时间
    private Date createEndTime(){
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date time = sdf.parse("2018-11-20");
            if (time.after(now) || time.equals(now)){
                return sdf.parse("2018-11-20");
            } else {
                return sdf.parse("2018-12-20");
            }
        } catch (Exception e){
            // do nothing
            return null;
        }
    }

}
