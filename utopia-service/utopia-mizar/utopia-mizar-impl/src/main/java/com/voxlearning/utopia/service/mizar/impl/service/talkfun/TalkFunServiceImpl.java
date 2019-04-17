package com.voxlearning.utopia.service.mizar.impl.service.talkfun;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriodRef;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourseUserRef;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.TalkFunCourse;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.mapper.talkfun.TK_CourseData;
import com.voxlearning.utopia.service.mizar.api.mapper.talkfun.TK_CourseReport;
import com.voxlearning.utopia.service.mizar.api.mapper.talkfun.TK_TeacherData;
import com.voxlearning.utopia.service.mizar.api.service.talkfun.TalkFunService;
import com.voxlearning.utopia.service.mizar.api.utils.talkfun.TalkFunCommand;
import com.voxlearning.utopia.service.mizar.impl.dao.microcourse.TalkFunCourseDao;
import com.voxlearning.utopia.service.mizar.impl.loader.MicroCourseLoaderImpl;
import com.voxlearning.utopia.service.mizar.impl.loader.MizarUserLoaderImpl;
import com.voxlearning.utopia.service.mizar.impl.service.MizarUserServiceImpl;
import com.voxlearning.utopia.service.mizar.impl.support.TalkFunHttpUtils;
import com.voxlearning.utopia.service.mizar.talkfun.TalkFunUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.service.mizar.talkfun.TalkFunUtils.*;


/**
 * 欢拓课程管理接口
 *
 * @author yeuchen.wang
 * @date 2016/01/10
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = TalkFunService.class, version = @ServiceVersion(version = "20170227")),
        @ExposeService(interfaceClass = TalkFunService.class, version = @ServiceVersion(version = "20170315"))
})
@SuppressWarnings("unchecked")
public class TalkFunServiceImpl extends SpringContainerSupport implements TalkFunService {

    @Inject private MizarUserLoaderImpl mizarUserLoader;
    @Inject private MicroCourseLoaderImpl microCourseLoader;
    @Inject private MizarUserServiceImpl mizarUserService;
    @Inject private TalkFunCourseDao talkFunCourseDao;

    @Override
    public MapMessage registerCourse(String periodId) {
        // 校验参数
        MapMessage validMsg = validateCourse(periodId, true);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        // 去找到对应的欢拓课程ID
        TalkFunCourse talkFunCourse = talkFunCourseDao.load(periodId);
        boolean exists = talkFunCourse != null && StringUtils.isNotBlank(talkFunCourse.getCourseId());
        TalkFunCommand command = exists ? TalkFunCommand.UPDATE_COURSE : TalkFunCommand.ADD_COURSE;
        // 准备参数
        MicroCoursePeriod period = (MicroCoursePeriod) validMsg.get("period");
        MizarUser user = (MizarUser) validMsg.get("teacher");
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "course_name", period.getTheme()); // 课程名称
        appendParam(params, "start_time", DateUtils.dateToString(period.getStartTime())); // 课程开始时间
        appendParam(params, "end_time", DateUtils.dateToString(period.getEndTime())); // 课程结束时间
        appendParam(params, "account", user.getId()); // 发起直播课程的第三方主播账号
        appendParam(params, "nickname", user.getRealName()); // 主播的昵称
        if (exists) {
            appendParam(params, "course_id", talkFunCourse.getCourseId());
        }
//        appendParam(params,"accountIntro", user.getUserComment()); // 主播的简介

        // 调用请求
        MapMessage retMsg = TalkFunHttpUtils.post(params, command, RuntimeMode.current());
        if (!retMsg.isSuccess()) {
            return retMsg;
        }
        if (exists) return retMsg;
        // 解析返回的参数
        TK_CourseData data = parseReturnData(JsonUtils.toJson(retMsg.get("data")), TK_CourseData.class);
        // 处理返回值
        if (data == null) {
            return MapMessage.errorMessage("解析返回值失败!");
        }
        // 将返回值中的重要信息存入数据库
        TalkFunCourse course = TalkFunCourse.newInstance(data);
        course.setPeriodId(periodId);
        talkFunCourseDao.upsert(course);
        return retMsg;
    }

    @Override
    public MapMessage manualRegisterCourse(String periodId, String funTalkCourse) {
        // 校验参数
        if (StringUtils.isAnyBlank(periodId, funTalkCourse)) {
            return MapMessage.errorMessage("无效的参数");
        }
        TalkFunCourse course = talkFunCourseDao.load(periodId);
        if (course == null) {
            course = new TalkFunCourse();
            course.setPeriodId(periodId);
        }
        course.setCourseId(funTalkCourse);
        course.setIsManual(true);
        talkFunCourseDao.upsert(course);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteCourse(String periodId) {
        // 校验参数
        MapMessage validMsg = validateCourse(periodId, false);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        // 准备参数
        MicroCoursePeriod period = (MicroCoursePeriod) validMsg.get("period");
        talkFunCourseDao.remove(periodId);
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "course_id", period.getId()); // 课程id
        // 调用请求
        MapMessage retMsg = TalkFunHttpUtils.post(params, TalkFunCommand.DELETE_COURSE, RuntimeMode.current());
        if (!retMsg.isSuccess()) {
            return retMsg;
        }
        return retMsg;
    }

    @Override
    public MapMessage accessCourse(String periodId, String userId, String userName, String role, Map<String, Object> optionMap) {
        // 校验参数
        if (!TalkFunUtils.validRole(role)) {
            return MapMessage.errorMessage("无效的用户身份：" + role);
        }
        MicroCoursePeriod period = microCourseLoader.loadCoursePeriod(periodId);
        if (period == null || period.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的课程信息");
        }
        // 去找到对应的欢拓课程ID
        TalkFunCourse talkFunCourse = talkFunCourseDao.load(periodId);
        if (talkFunCourse == null || StringUtils.isBlank(talkFunCourse.getCourseId())) {
            return MapMessage.errorMessage("该课时尚未注册");
        }

        Date now = new Date();
        // 已经生成回放了，或者已经结束超过90分钟
        boolean replay = checkHasReplay(periodId) || (period.getEndTime() != null && DateUtils.addMinutes(period.getEndTime(), 90).before(now));
        TalkFunCommand command = replay ? TalkFunCommand.COURSE_REPLAY : TalkFunCommand.COURSE_LIVE;
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "course_id", talkFunCourse.getCourseId()); // 课程ID
        appendParam(params, "uid", userId); // 合作方观看用户唯一ID
        appendParam(params, "nickname", userName); // 合作方观看用户昵称
        appendParam(params, "role", role); // 用户身份
        Map<String, Object> options = new HashMap<>();
        if (MapUtils.isNotEmpty(optionMap)) {
            options.putAll(optionMap);
        }
        options.put("ssl", true); // 强制使用 https
        appendParam(params, "options", options); // 其他选项
        // 调用请求
        MapMessage retMsg = TalkFunHttpUtils.post(params, command, RuntimeMode.current());
        if (!retMsg.isSuccess()) {
            return retMsg;
        }
        retMsg.add("live", !replay);
        retMsg.add("title", period.getTheme());
        retMsg.add("text", period.getSpreadText());
        retMsg.add("link", period.getSpreadUrl());
        return retMsg;
    }

    @Override
    public MapMessage registerTeacher(String userId, String password) {
        // 校验参数
        MapMessage validMsg = validateTeacher(userId);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        // 准备参数
        MizarUser user = (MizarUser) validMsg.get("teacher");
        boolean exist = StringUtils.isNotBlank(user.getTalkFunId());
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "account", userId); // 主播唯一ID
        appendParam(params, "nickname", user.getRealName()); // 主播的昵称
        if (StringUtils.isNotBlank(password)) {
            appendParam(params, "password", password); //密码
        }
//        appendParam(params,"intro", user.getUserComment()); // 主播的简介
        TalkFunCommand command = exist ? TalkFunCommand.UPDATE_TEACHER : TalkFunCommand.ADD_TEACHER;
        // 调用请求
        MapMessage retMsg = TalkFunHttpUtils.post(params, command, RuntimeMode.current());
        if (!retMsg.isSuccess()) {
            return retMsg;
        }
        if (exist) return retMsg;
        // 解析返回的参数
        TK_TeacherData data = parseReturnData(JsonUtils.toJson(retMsg.get("data")), TK_TeacherData.class);
        // 处理返回值
        if (data == null) {
            return MapMessage.errorMessage("解析返回值失败!");
        }
        // 将欢拓后台返回的ID存入用户属性
        user.setTalkFunId(data.getBid());
        return mizarUserService.editMizarUser(user);
    }

    @Override
    public MapMessage launch(String periodId) {
        try {
            // 校验参数
            MicroCoursePeriod period = microCourseLoader.loadCoursePeriod(periodId);
            if (period == null || period.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的课程信息");
            }
            String courseId = fetchTkCourseId(period);
            if (StringUtils.isBlank(courseId)) {
                return MapMessage.errorMessage("该课时尚未注册").add("title", period.getTheme());
            }
            // 准备参数
            Map<String, Object> params = new HashMap<>();
            appendParam(params, "course_id", courseId); // 课程ID
            // 调用请求
            return TalkFunHttpUtils.get(params, TalkFunCommand.COURSE_LAUNCH, RuntimeMode.current(), StringUtils.isNotBlank(period.getTkCourse()));
        } catch (Exception ex) {
            logger.error("Failed launch Talk-Fun Course, id={}", periodId, ex);
            return MapMessage.errorMessage("调起欢拓获取直播器启动协议失败：" + StringUtils.firstLine(ex.getMessage()));
        }
    }

    @Override
    public MapMessage report(String periodId, Date start, Date end, Pageable pager, boolean live) {
        // 校验参数
        if (start != null && end != null && start.after(end)) {
            return MapMessage.errorMessage("无效的时间区间");
        }
        if (pager == null || !PAGE_RANGE.contains(pager.getPageNumber()) || !SIZE_RANGE.contains(pager.getPageSize())) {
            return MapMessage.errorMessage("无效的分页信息");
        }
        MicroCoursePeriod period = microCourseLoader.loadCoursePeriod(periodId);
        if (period == null || period.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的课程信息");
        }
        String courseId = fetchTkCourseId(period);
        if (StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("该课时尚未注册").add("title", period.getTheme());
        }
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "course_id", courseId);
        appendParam(params, "page", String.valueOf(pager.getPageNumber()));
        appendParam(params, "size", String.valueOf(pager.getPageSize()));

        Map<String, Object> options = new HashMap<>();
        if (start != null) {
            appendParam(options, "start_time", DateUtils.dateToString(start)); // 开始时间
        }
        if (end != null) {
            appendParam(options, "end_time", DateUtils.dateToString(end)); // 结束时间
        }
        if (!options.isEmpty()) {
            appendParam(params, "options", options);
        }
        // 调用请求
        TalkFunCommand command = live ? TalkFunCommand.VISITOR_LIVE : TalkFunCommand.VISITOR_REPLAY;
        MapMessage retMsg = TalkFunHttpUtils.get(params, command, RuntimeMode.current(), StringUtils.isNotBlank(period.getTkCourse()));
        if (!retMsg.isSuccess()) {
            return retMsg;
        }
        // 解析返回的参数, data里面是一个List
        List<TK_CourseReport> dataList = parseReturnList(JsonUtils.toJson(retMsg.get("data")), TK_CourseReport.class);
        return retMsg.add("dataList", dataList).add("title", period.getTheme());
    }

    @Override
    public Map<String, TalkFunCourse> loadTalkFunCourses(Collection<String> periodIds) {
        if (CollectionUtils.isEmpty(periodIds)) {
            return Collections.emptyMap();
        }
        return talkFunCourseDao.loads(periodIds);
    }

    @Override
    public MapMessage finishClazz(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("无效的课程ID");
        }
        try {
            talkFunCourseDao.updateCourseStatus(courseId, TalkFunCourse.Status.FINISHED);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed update talk-fun course status, courseId={}, status={}", courseId, TalkFunCourse.Status.FINISHED, ex);
            return MapMessage.errorMessage("状态更新失败");
        }
    }

    @Override
    public MapMessage replayDone(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("无效的课程ID");
        }
        try {
            talkFunCourseDao.updateCourseStatus(courseId, TalkFunCourse.Status.REPLAY_DONE);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed update talk-fun course status, courseId={}, status={}", courseId, TalkFunCourse.Status.REPLAY_DONE, ex);
            return MapMessage.errorMessage("状态更新失败");
        }
    }

    @Override
    public boolean checkClassFinished(String periodId) {
        if (StringUtils.isBlank(periodId)) {
            return false;
        }
        TalkFunCourse course = talkFunCourseDao.load(periodId);
        return course != null && (course.isCourseFinished() || course.isReplayDone());
    }

    @Override
    public boolean checkHasReplay(String periodId) {
        if (StringUtils.isBlank(periodId)) {
            return false;
        }
        TalkFunCourse course = talkFunCourseDao.load(periodId);
        return course != null && course.isReplayDone();
    }

    @Override
    public MapMessage changeTalkFunStatus(String periodId, TalkFunCourse.Status status) {
        if (StringUtils.isBlank(periodId) || status == null) {
            return MapMessage.errorMessage("无效的参数");
        }
        try {
            TalkFunCourse course = talkFunCourseDao.load(periodId);
            if (course == null) {
                course = new TalkFunCourse();
                course.setPeriodId(periodId);
            }
            // 高亢原话
            // 如果当前无任何状态，可手动结束，可手动生成回放
            // 如果当前已经finish，可手动生成回放
            // 如果已经回放，则没有操作。
            if (course.isReplayDone()) {
                return MapMessage.errorMessage("已经生成回放");
            }
            if (course.isCourseFinished() && status != TalkFunCourse.Status.REPLAY_DONE) {
                return MapMessage.successMessage("课程已经结束");
            }
            course.setCourseStatus(status.name());
            talkFunCourseDao.upsert(course);
            return MapMessage.successMessage("状态[" + status.name() + "]更新完毕");
        } catch (Exception ex) {
            logger.error("Failed update talkFunCourse status, peroid={}, status={}", periodId, status.name(), ex);
            return MapMessage.errorMessage("状态[" + status.name() + "]更新失败:" + ex.getMessage());
        }
    }

    @Override
    public MapMessage courseEntrance(String periodId, String userId, String userName, String role, Map<String, Object> optionMap) {
        if (StringUtils.isBlank(periodId)) {
            return MapMessage.errorMessage("无效的参数");
        }
        MicroCoursePeriod period = microCourseLoader.loadCoursePeriod(periodId);
        if (period == null || period.isDisabledTrue()) {
            return MapMessage.errorMessage("课程已经下线");
        }

        // 准备数据
        Date now = new Date();
        // 已经生成回放了，或者已经结束超过90分钟
        boolean replay = checkHasReplay(periodId) || (period.getEndTime() != null && DateUtils.addMinutes(period.getEndTime(), 90).before(now));
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "uid", userId); // 合作方观看用户唯一ID
        appendParam(params, "nickname", userName); // 合作方观看用户昵称
        appendParam(params, "role", role); // 用户身份
        Map<String, Object> options = new HashMap<>();
        if (MapUtils.isNotEmpty(optionMap)) {
            options.putAll(optionMap);
        }
        // https 通过参数控制
//        options.put("ssl", true); // 强制使用 https
        appendParam(params, "options", options); // 其他选项
        // 数据准备完毕
        // 优先使用自助欢拓ID
        if (StringUtils.isNotBlank(period.getTkCourse())) {
            return fetchTalkFunCourse(params, replay, period.getTkCourse(), true)
                    .add("title", period.getTheme())
                    .add("text", period.getSpreadText())
                    .add("link", period.getSpreadUrl());
        }

        // 使用默认的欢拓课程，去找到对应的欢拓课程ID
        TalkFunCourse talkFunCourse = talkFunCourseDao.load(periodId);
        if (talkFunCourse == null || StringUtils.isBlank(talkFunCourse.getCourseId())) {
            return MapMessage.errorMessage("该课程尚未注册");
        }
        return fetchTalkFunCourse(params, replay, talkFunCourse.getCourseId(), false)
                .add("title", period.getTheme())
                .add("text", period.getSpreadText())
                .add("link", period.getSpreadUrl());
    }

    @Override
    public MapMessage generateUrl(String periodId, String userId, String userName, String role, Map<String, Object> options) {
        if (StringUtils.isBlank(periodId)) {
            return MapMessage.errorMessage("无效的参数");
        }
        MicroCoursePeriod period = microCourseLoader.loadCoursePeriod(periodId);
        if (period == null || period.isDisabledTrue()) {
            return MapMessage.errorMessage("课程已经下线");
        }
        Date now = new Date();
        // 已经生成回放了，或者已经结束超过90分钟
        boolean replay = checkHasReplay(periodId) || (period.getEndTime() != null && DateUtils.addMinutes(period.getEndTime(), 90).before(now));
        TalkFunCommand command = replay ? TalkFunCommand.COURSE_REPLAY : TalkFunCommand.COURSE_LIVE;
        // 准备参数
        boolean backup = false;
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "uid", userId); // 合作方观看用户唯一ID
        appendParam(params, "nickname", userName); // 合作方观看用户昵称
        appendParam(params, "role", role); // 用户身份
        appendParam(params, "options", options); // 其他选项
        if (StringUtils.isNotBlank(period.getTkCourse())) {
            appendParam(params, "course_id", period.getTkCourse());
            backup = true;
        } else {
            // 去找到对应的欢拓课程ID
            TalkFunCourse talkFunCourse = talkFunCourseDao.load(periodId);
            if (talkFunCourse == null || StringUtils.isBlank(talkFunCourse.getCourseId())) {
                return MapMessage.errorMessage("该课时尚未注册");
            }
            appendParam(params, "course_id", talkFunCourse.getCourseId()); // 课程ID
        }
        try {
            String url = TalkFunHttpUtils.generateUrl(params, command, RuntimeMode.current(), backup);
            return MapMessage.successMessage().set("url", url).set("replay", replay);
        } catch (NullPointerException e) {
            return MapMessage.errorMessage("获取欢拓Token,生成Url失败");
        }
    }

    @Override
    public MapMessage fetchAccessKey(String periodId, String userId, String userName, String role, Map<String, Object> options) {
        if (StringUtils.isBlank(periodId)) {
            return MapMessage.errorMessage("无效的参数");
        }
        MicroCoursePeriod period = microCourseLoader.loadCoursePeriod(periodId);
        if (period == null || period.isDisabledTrue()) {
            return MapMessage.errorMessage("课程已经下线");
        }
        // 去找到对应的欢拓课程ID
        TalkFunCourse talkFunCourse = talkFunCourseDao.load(periodId);
        if (talkFunCourse == null || StringUtils.isBlank(talkFunCourse.getCourseId())) {
            return MapMessage.errorMessage("该课时尚未注册");
        }
        Date now = new Date();
        // 已经生成回放了，或者已经结束超过90分钟
        boolean replay = checkHasReplay(periodId) || (period.getEndTime() != null && DateUtils.addMinutes(period.getEndTime(), 90).before(now));
        int mode = replay ? 2 : 1; //2.回放 1.直播
        // 准备参数
        boolean backup = false;
        Map<String, Object> params = new HashMap<>();
        appendParam(params, "uid", userId); // 合作方观看用户唯一ID
        appendParam(params, "nickname", userName); // 合作方观看用户昵称
        appendParam(params, "role", role); // 用户身份
        appendParam(params, "options", options); // 其他选项
        if (StringUtils.isNotBlank(period.getTkCourse())) {
            appendParam(params, "course_id", period.getTkCourse());
            backup = true;
        } else {
            appendParam(params, "course_id", talkFunCourse.getCourseId()); // 课程ID
        }
        try {
            String accessKey = TalkFunUtils.generateAccessKey(params, RuntimeMode.current(), backup);
            return MapMessage.successMessage().set("accessKey", accessKey).set("rMode", mode).set("spreadText", period.getSpreadText()).set("spreadUrl", period.getSpreadUrl());
        } catch (Exception e) {
            return MapMessage.errorMessage("生成accessKey失败");
        }
    }

    /**
     * 校验课时相关信息
     *
     * @param periodId 课时Id
     * @param withUser 是否校验老师信息
     */
    private MapMessage validateCourse(String periodId, boolean withUser) {
        if (StringUtils.isBlank(periodId)) {
            return MapMessage.errorMessage("无效的参数");
        }
        MapMessage validMsg = MapMessage.successMessage();
        MicroCoursePeriod period = microCourseLoader.loadCoursePeriod(periodId);
        if (period == null || period.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的课程信息");
        }
        validMsg.add("period", period);
        if (withUser) {
            // 根据课时找到课程
            MicroCoursePeriodRef periodRef = microCourseLoader.findCoursePeriodRefByPeriod(periodId);
            if (periodRef == null) {
                return MapMessage.errorMessage("欢拓：课时尚未关联有效的课程");
            }
            // 根据课程找到主讲老师
            String teacherId = microCourseLoader.findCourseUserRefByCourse(periodRef.getCourseId())
                    .stream()
                    .filter(ref -> MicroCourseUserRef.CourseUserRole.Lecturer == ref.getRole())
                    .map(MicroCourseUserRef::getUserId)
                    .findFirst().orElse(null);
            MizarUser teacher = mizarUserLoader.loadUser(teacherId);
            if (teacher == null || !teacher.isValid()) {
                return MapMessage.errorMessage("欢拓：无效的老师信息");
            }
            validMsg.add("teacher", teacher);
        }
        return validMsg;
    }

    /**
     * 校验老师信息
     *
     * @param userId 老师ID
     */
    private MapMessage validateTeacher(String userId) {
        if (StringUtils.isBlank(userId)) {
            return MapMessage.errorMessage("无效的参数");
        }
        MapMessage validMsg = MapMessage.successMessage();
        MizarUser teacher = mizarUserLoader.loadUser(userId);
        if (teacher == null || !teacher.isValid()) {
            return MapMessage.errorMessage("无效的老师信息");
        }
        validMsg.add("teacher", teacher);
        return validMsg;
    }

    private MapMessage fetchTalkFunCourse(Map<String, Object> params, boolean replay, String courseId, boolean backup) {
        if (MapUtils.isEmpty(params) || StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("无效的参数");
        }
//        TalkFunCommand command = replay ? TalkFunCommand.COURSE_REPLAY : TalkFunCommand.COURSE_LIVE;
        Map<String, Object> paramMap = new HashMap<>(params);
        appendParam(paramMap, "course_id", courseId); // 课程ID
        // 调用请求
//        MapMessage retMsg = TalkFunHttpUtils.post(paramMap, command, RuntimeMode.current(), backup);
//        if (!retMsg.isSuccess()) {
//            return retMsg;
//        }
//        Map<String, Object> data = (Map<String, Object>) retMsg.get("data");
        // 自行计算地址
        try {
            String liveUrl = replay ? TalkFunUtils.coursePlaybackUrl(paramMap, RuntimeMode.current(), backup) : TalkFunUtils.courseLiveUrl(paramMap, RuntimeMode.current(), backup);
            return MapMessage.successMessage()
                    .add("live", !replay)
                    .add("entrance", liveUrl);
//                    .add("accessToken", data.get("access_token"));
        } catch (Exception ex) {
            logger.error("Failed to generate course access url.", ex);
            return MapMessage.errorMessage("获取播放失败！");
        }
    }

    private String fetchTkCourseId(MicroCoursePeriod period) {
        if (StringUtils.isNotBlank(period.getTkCourse())) {
            return period.getTkCourse();
        }
        TalkFunCourse talkFunCourse = talkFunCourseDao.load(period.getId());
        return talkFunCourse == null ? null : talkFunCourse.getCourseId();
    }

}
