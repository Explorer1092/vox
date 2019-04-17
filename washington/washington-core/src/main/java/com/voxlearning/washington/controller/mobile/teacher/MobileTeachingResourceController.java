package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.business.api.constant.TeachingResourceUserType;
import com.voxlearning.utopia.business.api.util.TeachingResourceUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.api.entity.NewTeacherResource;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.entity.TeachingResourceCollect;
import com.voxlearning.utopia.service.business.api.mapper.NewTeacherResourceWrapper;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import com.voxlearning.utopia.service.business.consumer.NewTeacherResourceServiceClient;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskPrivilegeServiceClient;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceLoaderClient;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceServiceClient;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityService;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourse;
import com.voxlearning.utopia.service.campaign.api.mapper.CourseInvitation;
import com.voxlearning.utopia.service.campaign.api.mapper.YiqiJTCourseListMapper;
import com.voxlearning.utopia.service.campaign.api.mapper.YiqiJTCourseMapper;
import com.voxlearning.utopia.service.campaign.client.YiqiJTServiceClient;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.controller.oauth.AbstractMobileTeacherOauthController;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.mapper.wechat.WechatOAuthUserInfo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.alps.core.util.StringUtils.isEmpty;
import static com.voxlearning.alps.lang.convert.SafeConverter.toBoolean;
import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;
import static com.voxlearning.alps.lang.util.MapMessage.errorMessage;
import static com.voxlearning.alps.lang.util.MapMessage.successMessage;

/**
 * 教学资源 Controller
 * Created by haitian.gan on 2017/8/3.
 */
@Named
@RequestMapping("/teacherMobile/teachingres/")
public class MobileTeachingResourceController extends AbstractMobileTeacherOauthController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private TeachingResourceLoaderClient teachingResLoader;
    @Inject private TeachingResourceServiceClient teachingResService;

    @ImportService(interfaceClass = TeacherActivityService.class)
    private TeacherActivityService tchActSrv; // TODO:这个坑记得找空修
    @Inject private YiqiJTServiceClient yiqiJTServiceClient;
    @Inject private TeacherTaskPrivilegeServiceClient teacherPrivilegeServiceClient;
    @Inject
    NewTeacherResourceServiceClient teacherResourceServiceClient;

    /**
     * 获得推荐的资源列表
     */
    @RequestMapping("/featured.vpage")
    @ResponseBody
    public MapMessage loadFeaturedResource() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        int page = getRequestInt("page", 1);
        int pageSize = getRequestInt("pageSize", 10);
        String grade = getRequestString("grade");
        int source = getRequestInt("source", 0);
        page = page <= 0 ? 1 : page;

        List<TeachingResourceRaw> featuredList = loadResources(teacher, true)
                .stream()
                .filter(t -> Objects.equals(t.getSource(), source))
                .filter(t -> toBoolean(t.getFeaturing()))
                .filter(t -> isEmpty(grade) || ArrayUtils.contains(t.getGrade().split(","), grade))
                .filter(t ->
                        t.getVisitLimited() == null || t.getVisitLimited() == TeachingResourceUserType.All ||
                                (t.getVisitLimited() == TeachingResourceUserType.Unauthorized && teacher.fetchCertificationState() != AuthenticationState.SUCCESS) ||
                                (t.getVisitLimited() == TeachingResourceUserType.Authorized && teacher.fetchCertificationState() == AuthenticationState.SUCCESS)
                )
                // 按上线时间倒序
                .sorted((t1, t2) -> t2.getOnlineAt().compareTo(t1.getOnlineAt()))
                .collect(Collectors.toList());

        // 获得老师教的年级列表
        Set<String> clazzLvlList = getTeacherClassesLvlList(teacher.getId());

        PageRequest pageable = new PageRequest(page - 1, pageSize);
        Page<TeachingResourceRaw> resources = PageableUtils.listToPage(featuredList, pageable);
        List<TeachingResourceRaw> content = resources.getContent();
        teachingResLoader.fillReadCollectCount(content);

        return successMessage().add("result", content)
                .add("totalPage", resources.getTotalPages())
                .add("hasNext", resources.hasNext())
                .add("gradeOptions", clazzLvlList)
                .add("source", source);
    }

    /**
     * 获得资源列表，带筛选条件
     */
    @RequestMapping("/resources.vpage")
    @ResponseBody
    public MapMessage loadResources() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String category = getRequestString("category");
        String grade = getRequestString("grade");
        String name = getRequestString("name");
        int source = getRequestInt("source", 0);

        int page = getRequestInt("page", 1);
        int pageSize = getRequestInt("pageSize", 10);
        page = page <= 0 ? 1 : page;

        if (StringUtils.isNotBlank(name)) {
            teachingResService.addHotSearch(name);
        }
        List<TeachingResourceRaw> resources = loadResources(teacher, true);
        List<TeachingResourceRaw> result = resources
                .stream()
                .filter(t -> Objects.equals(t.getSource(), source))
                .filter(t -> isEmpty(category) || Objects.equals(t.getCategory(), category))
                .filter(t -> isEmpty(grade) || ArrayUtils.contains(t.getGrade().split(","), grade))
                .filter(t -> StringUtils.isEmpty(name) || t.getName().contains(name))
                .filter(t -> t.getVisitLimited() == null || t.getVisitLimited() == TeachingResourceUserType.All ||
                        (t.getVisitLimited() == TeachingResourceUserType.Unauthorized && teacher.fetchCertificationState() != AuthenticationState.SUCCESS) ||
                        (t.getVisitLimited() == TeachingResourceUserType.Authorized && teacher.fetchCertificationState() == AuthenticationState.SUCCESS)
                )
                .sorted((t1, t2) -> {
                    // 先按置顶排序值排，如果相同再按上线时间来
                    Integer t1Do = SafeConverter.toInt(t1.getDisplayOrder());
                    Integer t2Do = SafeConverter.toInt(t2.getDisplayOrder());

                    int compareResult = t2Do.compareTo(t1Do);
                    if (compareResult != 0) {
                        return compareResult;
                    } else {
                        return t2.getOnlineAt().compareTo(t1.getOnlineAt());
                    }
                })
                .collect(Collectors.toList());

        // 获得老师教的年级列表
        Set<String> clazzLvlList = getTeacherClassesLvlList(teacher.getId());

        PageRequest pageable = new PageRequest(page - 1, pageSize);
        Page<TeachingResourceRaw> resultPage = PageableUtils.listToPage(result, pageable);
        List<TeachingResourceRaw> content = resultPage.getContent();
        teachingResLoader.fillReadCollectCount(content);

        MapMessage resultMsg = successMessage();
        resultMsg.add("result", content);
        resultMsg.add("totalPage", resultPage.getTotalPages());
        resultMsg.add("hasNext", resultPage.hasNext());
        resultMsg.add("gradeOptions", clazzLvlList);
        resultMsg.add("source", source);
        return resultMsg;
    }

    /**
     * 资源详情页
     */
    @RequestMapping("/resource_detail.vpage")
    @ResponseBody
    public MapMessage loadResourceDetail() {
        // 详情页会分享出去，不能进行身份判断
        Teacher teacher = currentTeacher();
        //if (teacher == null)
        //    return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);

        String resourceId = getRequestString("resourceId");
        int source = getRequestInt("source", 0);

        TeachingResource resource = teachingResLoader.loadResource(resourceId);
        if (resource == null) {
            return errorMessage("资源不存在!");
        }

        // 资源ID 收藏ID
        Map<String, String> collect = new HashMap<>();
        if (teacher != null) {
            collect = teachingResLoader.loadCollectByUserId(teacher.getId()).stream()
                    .filter(i -> TeachingResourceUtils.isTeachingResource(i.getResourceId()))
                    .collect(Collectors.toMap(TeachingResourceCollect::getResourceId, TeachingResourceCollect::getId, (o1, o2) -> o2));
        }

        teachingResService.addReadCount(resourceId);

        TeacherResourceTask task = null;
        String leftTimeExpr = "";
        Integer level = null;
        Long total = null, times = null;
        Boolean shareParent = false;
        if (teacher != null) {
            task = teachingResLoader.loadTeacherTask(teacher.getId(), resource.getId());
            if (task != null) {
                leftTimeExpr = task.getLeftTimeExpr();
            }

            TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
            level = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getNewLevel());

            MapMessage privilegeMessage = teacherPrivilegeServiceClient.getCoursewareDownloadTimes(teacher.getId());
            times = MapUtils.getLong(privilegeMessage, "times");
            total = MapUtils.getLong(privilegeMessage, "total");

            shareParent = teacherResourceServiceClient.getRemoteReference().getShareParent(teacher.getId(), resourceId);
        }
        String vipLevel = TeacherExtAttribute.NewLevel.getNewLevelByLevel(level).getValue();

        // 任务字典
        TeachingResourceTask taskDict = TeachingResourceTask.parse(resource.getTask());

        return successMessage().add("resource", resource)
                .add("collected", collect.get(resourceId) != null)
                .add("delCollectId", collect.get(resourceId))
                .add("subject", teacher != null && teacher.getSubject() == Subject.ENGLISH ? "english" : "")
                .add("source", source)
                .add("task", task)
                .add("isFree", taskDict == TeachingResourceTask.FREE)
                .add("taskDesc", taskDict.getDesc())
                .add("leftTime", leftTimeExpr)
                .add("privilegeCount", total)
                .add("privilegeSurplusCount", times)
                .add("vipLevel", vipLevel)
                .add("teacherAuthState", teacher != null && Objects.equals(teacher.getAuthenticationState(), 1))
                .add("share_parent", shareParent);
    }

    /**
     * 获得任务列表
     */
    @RequestMapping("/tasks.vpage")
    @ResponseBody
    public MapMessage loadTasks() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String status = getRequestString("status");
        Map<String, TeachingResourceRaw> resourceMap = teachingResLoader.getTeachingResourceBuffer().loadResourceRawMap();

        // 转成前台方便取值的格式
        Function<TeacherResourceTask, Map<String, Object>> transToMapFunc = t -> {
            // 如果找不到对应的资源信息，直接忽略处理
            TeachingResourceRaw resource = resourceMap.get(t.getResourceId());
            if (resource == null) {
                return null;
            }

            TeachingResourceTask task = TeachingResourceTask.parse(t.getTask());

            Map<String, Object> mapper = new HashMap<>();
            mapper.put("resourceId", resource.getId());
            mapper.put("image", resource.getImage());
            mapper.put("taskDesc", task.getDesc());
            mapper.put("resourceName", resource.getName());
            mapper.put("leftTime", t.getLeftTimeExpr());

            // 拉新任务无需查看任务进度
            mapper.put("displayProgress", !TeachingResourceTask.RECRUIT_NEW.name().equals(task.name()));

            return mapper;
        };

        List<Map<String, Object>> result = teachingResLoader.loadTasksByStatus(teacher.getId(), status)
                .stream()
                // 按照更新时间倒序排列
                .sorted((t1, t2) -> t2.getUpdateAt().compareTo(t1.getUpdateAt()))
                .map(transToMapFunc)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return successMessage().add("result", result);
    }

    /**
     * 领取任务
     */
    @RequestMapping("/receive_task.vpage")
    @ResponseBody
    public MapMessage receiveTask() {
        boolean auth = false;// 认证状态
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "请登录老师账号");

            String haveType = getRequestParameter("haveType", TeachingResourceUtils.HAVE_TYPE_ORDINARY);

            String resourceId = getRequestString("resourceId");
            TeachingResource resource = teachingResLoader.loadResource(resourceId);

            Validate.notNull(resource, "资源不存在!");
            Validate.isTrue(resource.getOnline(), "资源已下线!");
            if (Objects.equals(haveType, TeachingResourceUtils.HAVE_TYPE_ORDINARY)) {
                AuthenticationState state = teacher.fetchCertificationState();
                if (resource.getReceiveLimited() != null && resource.getReceiveLimited() == TeachingResourceUserType.Authorized) {
                    Validate.isTrue(state == AuthenticationState.SUCCESS, "用户未认证！");
                }

                if (resource.getReceiveLimited() != null && resource.getReceiveLimited() == TeachingResourceUserType.Unauthorized) {
                    Validate.isTrue(state != AuthenticationState.SUCCESS, "任务领取受限！");
                }
            }
            return teachingResService.receiveTask(teacher.getId(), resource.getId(), haveType);
        } catch (Exception e) {
            return errorMessage(e.getMessage()).add("auth", auth);
        }
    }

    /**
     * 获得我拥有的资源列表
     */
    @RequestMapping("/my_resources.vpage")
    @ResponseBody
    public MapMessage getResourceList() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        int page = getRequestInt("page", 1);
        int pageSize = getRequestInt("pageSize", 10000); // 因为pc版可能没改造, 没传 pageSize, 所以默认大一点
        page = page <= 0 ? 1 : page;

        // 所有资源的Map对照
        Map<String, TeachingResourceRaw> resourceMap = teachingResLoader.getTeachingResourceBuffer().loadResourceRawMap();

        List<TeachingResourceRaw> allResourceRawList = new ArrayList<>(resourceMap.values());
        List<NewTeacherResourceWrapper> newResourceMap = teacherResourceServiceClient.getTeacherResourceBuffer().getNativeBuffer().dump().getData();
        for (NewTeacherResourceWrapper wrapper : newResourceMap) {
            for (NewTeacherResource.File file : wrapper.getFileList()) {
                TeachingResourceRaw itemRaw = new TeachingResourceRaw();
                itemRaw.setId(wrapper.getId());
                itemRaw.setName(file.getFileName());
                itemRaw.setFileUrl(file.getFileUrl());
                itemRaw.setOnlineAt(wrapper.getFirstOnlineTime());
                allResourceRawList.add(itemRaw);
            }
        }

        Map<String, List<TeachingResourceRaw>> allResourceRaw = allResourceRawList.stream().collect(Collectors.groupingBy(TeachingResourceRaw::getId));

        List<TeachingResourceRaw> myResources = teachingResLoader.loadTasksByStatus(teacher.getId(), TeacherResourceTask.Status.FINISH.name())
                .stream()
                .sorted((o1, o2) -> o2.getUpdateAt().compareTo(o1.getUpdateAt()))
                .map(t -> {
                    List<TeachingResourceRaw> teachingResourceRaws = allResourceRaw.get(t.getResourceId());
                    if (teachingResourceRaws != null) {
                        for (TeachingResourceRaw teachingResourceRaw : teachingResourceRaws) {
                            teachingResourceRaw.setOnlineAt(t.getUpdateAt()); // 前端显示的领取日期取得是这个字段,这里重新 set 一下任务的完成时间当作领取日期
                        }
                    }
                    return teachingResourceRaws;
                })
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        PageRequest pageable = new PageRequest(page - 1, pageSize);
        Page<TeachingResourceRaw> myResourcesPage = PageableUtils.listToPage(myResources, pageable);

        return successMessage().add("sourceList", myResourcesPage.getContent())
                .add("totalPage", myResourcesPage.getTotalPages())
                .add("hasNext", myResourcesPage.hasNext());
    }

    @RequestMapping(value = "/myresources.vpage")
    public String myresources() {
        // 校验登录状态
        Teacher teacher = currentTeacher();
        if (teacher == null)
            return "redirect:" + ProductConfig.getUcenterUrl();

        return "/teacherv3/resource/mysource";
    }

    /**
     * 获取任务的进度
     *
     * @return
     */
    @RequestMapping(value = "/progress.vpage")
    @ResponseBody
    public MapMessage loadProgressDetail() {
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "请登录老师账号!");

            String resourceId = getRequestString("resourceId");
            Validate.notEmpty(resourceId, "资源id不能为空!");

            Map<String, Object> progress = teachingResLoader.loadTaskProgress(teacher.getId(), resourceId);
            return successMessage().add("progress", progress);

        } catch (Exception e) {
            return errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/add_read_count.vpage")
    @ResponseBody
    public MapMessage addReadCount() {
        return MapMessage.successMessage("接口已下线");
    }

    @ResponseBody
    @RequestMapping("/my_collect.vpage")
    public MapMessage loadTeacherCollect() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        int page = getRequestInt("page", 1);
        int pageSize = getRequestInt("pageSize", 30);
        int source = getRequestInt("source", 0);
        page = page <= 0 ? 1 : page;

        String category = getRequestString("category");
        List<TeachingResourceCollect> collectList = teachingResLoader.loadCollectByUserId(teacher.getId());
        Map<String, TeachingResourceRaw> resourceMap = loadResources(teacher, false).stream().collect(Collectors.toMap(TeachingResourceRaw::getId, Function.identity(), (o1, o2) -> o1));

        // FIXME 临时策略, 把新老资源都封装成 TeachingResourceRaw
        List<TeachingResourceRaw> newTeachingResourceRaw = teacherResourceServiceClient.getTeacherResourceBuffer().getNativeBuffer().dump().getData().stream().map(i -> {
            TeachingResourceRaw resourceRaw = new TeachingResourceRaw();
            resourceRaw.setCategory(TeachingResource.Category.NEW_COURSEWARE.name());
            resourceRaw.setId(i.getId());
            resourceRaw.setName(i.getTitle());
            resourceRaw.setImage(i.getImage());
            resourceRaw.setAppImage(i.getAppImage());
            resourceRaw.setSource(0); // 非江西版
            resourceRaw.setIsCourse(false);
            return resourceRaw;
        }).collect(Collectors.toList());

        // ID 是 ObjectId 不会重复
        for (TeachingResourceRaw raw : newTeachingResourceRaw) {
            resourceMap.put(raw.getId(), raw);
        }

        Stream<TeachingResourceRaw> teachingResourceRawStream = collectList.stream().map(item -> {
            TeachingResourceRaw raw = resourceMap.get(item.getResourceId());
            if (Objects.equals(TeachingResource.Category.NEW_COURSEWARE.name(), item.getCategory())) {
                return raw;
            } else {
                // 有脏数据 get 不出来会导致 setDelCollectId NPE
                if (raw == null) {
                    return null;
                }
                raw.setDelCollectId(item.getId()); // 保留收藏ID 为删除提供便利(利用主键删除) 0319 补充：现在看当初的做法有点傻.
                return raw;
            }
        }).filter(i -> i != null && Objects.equals(i.getSource(), source));

        if (StringUtils.isNotBlank(category)) {
            // 前端只传老课件分类,但是后端要考虑新课件数据
            if (Objects.equals(category, TeachingResource.Category.SYNC_COURSEWARE.name())) {
                teachingResourceRawStream = teachingResourceRawStream.filter(item -> Objects.equals(item.getCategory(), category)
                        || Objects.equals(item.getCategory(), TeachingResource.Category.NEW_COURSEWARE.name()));
            } else {
                teachingResourceRawStream = teachingResourceRawStream.filter(item -> Objects.equals(item.getCategory(), category));
            }
        }
        List<TeachingResourceRaw> myCollect = teachingResourceRawStream.collect(Collectors.toList());

        PageRequest pageable = new PageRequest(page - 1, pageSize);
        Page<TeachingResourceRaw> resultPage = PageableUtils.listToPage(myCollect, pageable);
        List<TeachingResourceRaw> content = resultPage.getContent();
        teachingResLoader.fillReadCollectCount(content);

        // 填充新结构的阅读、收藏量
        for (TeachingResourceRaw resourceRaw : content) {
            if (Objects.equals(TeachingResource.Category.NEW_COURSEWARE.name(), resourceRaw.getCategory())) {
                NewTeacherResource resource = teacherResourceServiceClient.getRemoteReference().loadDetailById(resourceRaw.getId());
                if (resource != null) {
                    resourceRaw.setReadCount(resource.getReadCount());
                    resourceRaw.setCollectCount(resource.getCollectCount());
                }
            }
        }

        return MapMessage.successMessage().add("data", content)
                .add("totalPage", resultPage.getTotalPages())
                .add("hasNext", resultPage.hasNext())
                .add("source", source);
    }

    @ResponseBody
    @RequestMapping(value = "/add_collect.vpage", method = RequestMethod.POST)
    public MapMessage addTeacherCollect() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String resourceId = getRequestString("id");
        String category = getRequestString("category");
        if (StringUtils.isBlank(resourceId) || StringUtils.isBlank(category)) {
            return MapMessage.errorMessage("id、category 不可为空");
        }

        MapMessage mapMessage = teachingResService.addCollect(teacher.getId(), category, resourceId);
        return mapMessage;
    }

    @ResponseBody
    @RequestMapping(value = "/del_collect.vpage", method = RequestMethod.POST)
    public MapMessage delCollect() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String resourceId = getRequestString("id");
        String category = getRequestString("category");
        String delCollectId = getRequestString("delCollectId");
        if (StringUtils.isBlank(resourceId) || StringUtils.isBlank(category) || StringUtils.isBlank(delCollectId)) {
            return MapMessage.errorMessage("id、category、delCollectId 不可为空");
        }
        MapMessage mapMessage = teachingResService.disableCollect(teacher.getId(), resourceId, category, delCollectId);
        return mapMessage;
    }

    @ResponseBody
    @RequestMapping(value = "/hot_word.vpage", method = RequestMethod.GET)
    public MapMessage getHotWord() {
        //List<String> hotWord = teachingResLoader.getHotWord();
        List<String> hotWord = new ArrayList<>();
        hotWord.add("开学");
        hotWord.add("课件");
        hotWord.add("绘本");
        hotWord.add("阅读");
        hotWord.add("资源");
        hotWord.add("人教版");
        return MapMessage.successMessage().add("data", hotWord);
    }

    @ResponseBody
    @RequestMapping(value = "/teacher_info.vpage", method = RequestMethod.GET)
    public MapMessage teacherInfo() {
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage("未登录");
        }
        return MapMessage.successMessage()
                .add("teacherId", teacherDetail.getId())
                .add("teacherName", teacherDetail.fetchRealname())
                .add("authState", teacherDetail.getAuthenticationState() == AuthenticationState.SUCCESS.getState());
    }

    /**
     * ------------------------------- 一起讲堂 --------------------------------------------
     */

    @RequestMapping(value = "/17jt/course.vpage")
    @ResponseBody
    public MapMessage load17JTCourse() {
        try {
            Teacher teacher = currentTeacher();

            long courseId = getRequestInt("courseId");
            Validate.isTrue(courseId > 0, "课程id为空!");

            YiqiJTCourseMapper mapper = yiqiJTServiceClient.load17JTCourseMapper(courseId);

            if (mapper == null) {
                return MapMessage.errorMessage("课程不存在!");
            }

            boolean bought = true;
            long price = mapper.getPrice();

            MapMessage resultMsg = MapMessage.successMessage();
            resultMsg.add("details", mapper);

            if (price > 0) {
                if (teacher != null) {
                    Date buyTime = yiqiJTServiceClient.loadCourseBuyTime(teacher.getId(), courseId);
                    bought = buyTime != null;

                    resultMsg.add("buyTime", buyTime);
                    resultMsg.add("bought", bought);

                    // 是否在有效期内
                    boolean inValidPeriod = Optional.ofNullable(buyTime)
                            .map(t -> DateUtils.addSeconds(t, toInt(mapper.getActiveTime())))
                            .map(t -> (new Date()).before(t))
                            .orElse(false);
                    resultMsg.add("inValidPeriod", inValidPeriod);

                    // 邀请记录
                    CourseInvitation courseInvitation = tchActSrv.loadCourseInvitation(teacher.getId(), courseId);
                    resultMsg.add("invitation", courseInvitation);

                    // NONE 未开始 ING 进行中 SUCCESS 成功 EXPIRE 已过期
                    if (courseInvitation == null) {
                        resultMsg.add("invitationStatus", "NONE");
                    } else if (courseInvitation.getHelpers().size() >= 3) {
                        resultMsg.add("invitationStatus", "SUCCESS");
                    } else if (courseInvitation.fetchExpire()) {
                        resultMsg.add("invitationStatus", "EXPIRE");
                    } else {
                        resultMsg.add("invitationStatus", "ING");
                    }
                    resultMsg.add("countdown", courseInvitation != null ? courseInvitation.fetchCountdown() : 0);
                } else {
                    resultMsg.add("buyTime", new Date());
                    resultMsg.add("bought", false);
                    resultMsg.add("inValidPeriod", false);
                }
            } else {
                resultMsg.add("invitationStatus", "NONE");

                //免费课程
                resultMsg.add("buyTime", new Date());
                resultMsg.add("bought", bought);
                resultMsg.add("inValidPeriod", true);
                mapper.setAttendNum(SafeConverter.toLong(mapper.getReadCount())); // 免费课程的兑换量展示为阅读量
            }

            Date now = new Date();
            // 查看是否开启了
            mapper.setOpen(Optional.ofNullable(mapper.getOpenTime()).map((now)::after).orElse(false));

            if (bought) {
                Date videoExpTime = DateUtils.addSeconds(now, 1800);
                mapper.setUrl(yiqiJTServiceClient.wrapAuth(mapper.getUrl(), videoExpTime)); // url添加鉴权参数
            }

            String unit = "学豆";
            if (teacher != null && teacher.isPrimarySchool()) {//默认是学豆，如果园丁豆则修改
                unit = "园丁豆";
                price = price / 10;
                mapper.setPrice(price);
            }
            resultMsg.add("unit", unit);

            String courseIdString = String.valueOf(courseId); // 收藏时把课程ID看成资源ID
            TeachingResourceCollect collect = null;

            Long total = null, times = null;
            if (teacher != null) {
                collect = teachingResLoader.loadCollectByUserId(teacher.getId()).stream()
                        .filter(i -> Objects.equals(i.getCategory(), "YIQI_JIANGTANG"))
                        .filter(i -> Objects.equals(i.getResourceId(), courseIdString))
                        .findFirst().orElse(null);

                MapMessage privilegeMessage = teacherPrivilegeServiceClient.get17ClassTimes(teacher.getId());
                times = MapUtils.getLong(privilegeMessage, "times");
                total = MapUtils.getLong(privilegeMessage, "total");
            }
            yiqiJTServiceClient.add17JTReadCount(courseId);

            resultMsg.add("collected", collect != null);
            resultMsg.add("delCollectId", collect != null ? collect.getId() : "");

            resultMsg.add("privilegeCount", total);
            resultMsg.add("privilegeSurplusCount", times);
            resultMsg.add("teacherId", teacher != null ? teacher.getId() : null);
            return resultMsg;
        } catch (Exception e) {
            e.printStackTrace();
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/17jt/course_list.vpage")
    @ResponseBody
    public MapMessage load17JTCoursesList() {
        try {
            List<YiqiJTCourseListMapper> courseList = yiqiJTServiceClient.load17JTCourseListMapper();
            return MapMessage.successMessage().add("courseList", courseList);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/17jt/buy_course.vpage")
    @ResponseBody
    public MapMessage buy17JTCourse() {
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "未登录!");

            long courseId = getRequestInt("courseId");
            Validate.isTrue(courseId > 0, "课程id为空!");
            String haveType = getRequestParameter("haveType", TeachingResourceUtils.HAVE_TYPE_ORDINARY);

            MapMessage privilegeMessage = teacherPrivilegeServiceClient.get17ClassTimes(teacher.getId());
            // 校验特权次数,如果够就减一个,如果不够,就返回失败,null 表示特权不限次数
            Long total = MapUtils.getLong(privilegeMessage, "total");
            Long times = MapUtils.getLong(privilegeMessage, "times");
            Long typeId = MapUtils.getLong(privilegeMessage, "id");

            if (Objects.equals(haveType, TeachingResourceUtils.HAVE_TYPE_ORDINARY)) {
                int price = Optional.ofNullable(yiqiJTServiceClient.loadCourseById(courseId).getPrice()).orElse(tchActSrv.QIYIJI_PRICE);
                String unit = "学豆";
                if (teacher.isPrimarySchool()) {
                    price = price / 10;
                    unit = "园丁豆";
                }

                long integral = Optional.ofNullable(currentTeacherDetail())
                        .map(TeacherDetail::getUserIntegral)
                        .map(UserIntegral::getUsable)
                        .orElse(0L);
                Validate.isTrue(integral >= price, "您的" + unit + "不够" + price + "个哦，请去布置作业获得更多的" + unit + "吧");
                MapMessage mapMessage = tchActSrv.buy17JTCourse(teacher.getId(), courseId);
                if (!mapMessage.isSuccess()) {
                    return mapMessage;
                }
                tchActSrv.cancelCourseInvitation(teacher.getId(), courseId);
                mapMessage.add("privilegeCount", total);
                mapMessage.add("privilegeSurplusCount", times);
                return mapMessage;
            } else {
                if (!privilegeMessage.isSuccess()) {
                    return privilegeMessage;
                }
                if (times != null) {
                    if (times <= 0) {
                        return MapMessage.errorMessage("兑换次数已经用完啦~");
                    } else {
                        MapMessage mapMessage = teacherPrivilegeServiceClient.cousumerPrivilege(teacher.getId(), typeId, "[兑换新讲堂资源]-" + courseId);
                        if (!mapMessage.isSuccess()) {
                            return mapMessage;
                        }
                    }
                }
                MapMessage mapMessage = tchActSrv.unlockJTCourse(teacher.getId(), courseId);
                if (!mapMessage.isSuccess()) {
                    return mapMessage;
                }
                tchActSrv.cancelCourseInvitation(teacher.getId(), courseId);
                mapMessage.add("privilegeCount", total);
                mapMessage.add("privilegeSurplusCount", times != null ? times - 1 : null);
                return mapMessage;
            }
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/17jt/unlock_course.vpage")
    @ResponseBody
    public MapMessage unlock17JTCourse() {
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "未登录!");

            long courseId = getRequestInt("courseId");
            Validate.isTrue(courseId > 0, "课程id为空!");

            return tchActSrv.unlockJTCourse(teacher.getId(), courseId);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/17jt/invitation.vpage")
    @ResponseBody
    public MapMessage invitation() {
        try {
            Teacher teacher = currentTeacher();
            Validate.notNull(teacher, "未登录!");

            long courseId = getRequestInt("courseId");
            Validate.isTrue(courseId > 0, "课程id为空!");

            return tchActSrv.startCourseInvitation(teacher.getId(), courseId);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/17jt/invitation_detail.vpage")
    @ResponseBody
    public MapMessage invitationDetail() {
        long teacherId = getRequestLong("teacherId");
        long courseId = getRequestLong("courseId");

        CourseInvitation courseInvitation = tchActSrv.loadCourseInvitation(teacherId, courseId);

        MapMessage resultMsg = MapMessage.successMessage();
        resultMsg.add("invitation", courseInvitation);

        // NONE 未开始 ING 进行中 SUCCESS 成功 EXPIRE 已过期
        if (courseInvitation == null) {
            resultMsg.add("invitationStatus", "EXPIRE");
        } else if (courseInvitation.getHelpers().size() >= 3) {
            resultMsg.add("invitationStatus", "SUCCESS");
        } else if (courseInvitation.fetchExpire()) {
            resultMsg.add("invitationStatus", "EXPIRE");
        } else {
            resultMsg.add("invitationStatus", "ING");
        }
        resultMsg.add("countdown", courseInvitation != null ? courseInvitation.fetchCountdown() : 0);

        String openId = getWebRequestContext().getAuthenticatedOpenId();
        boolean helped = false;

        if (courseInvitation != null && CollectionUtils.isNotEmpty(courseInvitation.getHelpers())) {
            for (CourseInvitation.Helper helper : courseInvitation.getHelpers()) {
                if (Objects.equals(helper.getOpenId(), openId)) {
                    helped = true;
                    break;
                }
            }
        }
        resultMsg.put("helped", helped);

        YiqiJTCourse course = yiqiJTServiceClient.loadCourseById(courseId);
        resultMsg.add("course", course);
        resultMsg.add("logined", canGetWechatUserInfo());

        return resultMsg;
    }

    @RequestMapping(value = "/17jt/helper.vpage")
    @ResponseBody
    public MapMessage helper() {
        try {
            String openId = getWebRequestContext().getAuthenticatedOpenId();
            if (StringUtils.isEmpty(openId)) {
                return MapMessage.errorMessage("未登录");
            }

            long teacherId = getRequestInt("teacherId");
            Validate.isTrue(teacherId > 0, "teacherId 为空!");

            long courseId = getRequestInt("courseId");
            Validate.isTrue(courseId > 0, "courseId 为空!");

            String nickName = "", imgUrl = "";
            WechatOAuthUserInfo userInfo = getWechatOauthUserInfo(openId);
            if (userInfo != null) {
                nickName = userInfo.getNickName();
                imgUrl = userInfo.getHeadImgUrl();
            } else {
                return MapMessage.errorMessage("获取到用户信息失败");
            }

            return tchActSrv.helperCourseInvitation(openId, nickName, imgUrl, teacherId, courseId);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 获得老师所属学科(包班制是算上主副账号一起)覆盖的所有已上线资源
     *
     * @param teacher
     * @return
     */
    private List<TeachingResourceRaw> loadResources(Teacher teacher, boolean filterOnline) {
        // 考虑包班制的情况，把主副账号的学科都查出来
        List<String> allSubNames = teacherLoaderClient.loadSubTeacherIds(teacher.getId())
                .stream()
                .map(tId -> teacherLoaderClient.loadTeacher(tId).getSubject())
                .filter(Objects::nonNull)
                .map(Enum::name)
                .collect(Collectors.toList());

        if (teacher.getSubject() != null)
            allSubNames.add(teacher.getSubject().name());

        // 获得老师名下所有班级的ClazzLevel列表(String)
        Set<String> clazzLvlList = getTeacherClassesLvlList(teacher.getId());

        List<TeachingResourceRaw> resources = loadAllResourcesRaw(teacher.getId());

        List<TeachingResourceRaw> yiqiJiangtang = teachingResLoader.getYQJTRaws();
        resources.addAll(yiqiJiangtang);

        Stream<TeachingResourceRaw> stream = resources
                .stream()
                .filter(TeachingResourceUtils.filterCategory)
                .filter(r -> {
                    // 看资源所属学科和老师所有学科的交集
                    List<String> resourceSubNames = Arrays.asList(r.getSubject().split(","));
                    Collection<String> interSubject = CollectionUtils.intersection(allSubNames, resourceSubNames);
                    return !CollectionUtils.isEmpty(interSubject);
                })
                // 过滤年级选项，取资源和老师班级的交集
                .filter(r -> {
                    List<String> resourceGrades = Arrays.asList(r.getGrade().split(","));
                    Collection<String> interClazzLvl = CollectionUtils.intersection(resourceGrades, clazzLvlList);
                    return !CollectionUtils.isEmpty(interClazzLvl);
                })
                .filter(t -> t.getOnlineAt() != null)
                .peek(t -> {
                    // 旧数据的问题, 如果没有设置来源就默认是平台
                    if (t.getSource() == null) {
                        t.setSource(TeachingResource.SOURCE_PLATFORM);
                    }
                });
        if (filterOnline) {
            stream = stream.filter(TeachingResourceRaw::getOnline);
        }
        return stream.collect(Collectors.toList());
    }

    private List<TeachingResourceRaw> loadAllResourcesRaw(Long teacherId) {
        List<TeacherResourceTask> teacherResourceTasks = teachingResLoader.loadTeacherTasks(teacherId);
        Map<String, TeacherResourceTask> taskMap = teacherResourceTasks.stream().collect(Collectors.toMap(TeacherResourceTask::getResourceId, Function.identity(), (o1, o2) -> o1));

        List<TeachingResourceRaw> resources = teachingResLoader.getTeachingResourceBuffer()
                .loadTeachingResourceRaw().stream()
                .filter(i -> i.getOnline() != null)
                .collect(Collectors.toList());
        resources = resources.stream()
                .filter(i -> TeachingResourceUtils.category.contains(i.getCategory()))
                .peek(itemRaw -> {
                    if (Objects.equals(itemRaw.getCategory(), TeachingResource.Category.WEEK_WELFARE.name()) && teacherId != null) {
                        TeacherResourceTask task = taskMap.get(itemRaw.getId());
                        if (task != null) {
                            itemRaw.setTaskStatus(task.getStatus());
                        }
                    }
                }).collect(Collectors.toList());
        return resources;
    }

    Set<String> getTeacherClassesLvlList(Long mainTeacherId) {
        return getTeacherClazz(mainTeacherId)
                .stream()
                .map(clazz -> clazz.getClazzLevel().getLevel())
                .map(String::valueOf)
                .collect(Collectors.toSet());
    }

    List<Clazz> getTeacherClazz(Long mainTeacherId) {
        List<Long> teacherIds = teacherLoaderClient.loadSubTeacherIds(mainTeacherId);
        HashSet<Long> teacherIdSet = new HashSet<>(teacherIds);
        teacherIdSet.add(mainTeacherId);

        return teacherLoaderClient.loadTeachersClazzIds(teacherIdSet)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(cId -> raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(cId))
                .filter(Objects::nonNull)
                .filter(Clazz::isPublicClazz)
                .filter(clazz -> !clazz.isTerminalClazz())
                .distinct()
                .sorted(Comparator.comparingInt(o -> o.getClazzLevel().getLevel()))
                .collect(Collectors.toList());
    }
}
