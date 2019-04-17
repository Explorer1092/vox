package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.api.tag.UserTagService;
import com.voxlearning.utopia.service.user.api.entities.ChannelCUserAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.LiveCastCourseService;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastCourse;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastCourseStudentSub;
import com.voxlearning.utopia.service.vendor.buffer.LiveCastCourseBuffer;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastCourseDao;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastCourseStudentSubDao;
import com.voxlearning.utopia.service.vendor.impl.version.LiveCastCourseBufferVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2018-09-21 下午7:28
 **/
@Named
@ExposeService(interfaceClass = LiveCastCourseService.class)
public class LiveCastCourseServiceImpl extends SpringContainerSupport implements LiveCastCourseService {


    @Inject
    private LiveCastCourseDao liveCastCourseDao;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private LiveCastCourseStudentSubDao liveCastCourseStudentSubDao;

    @Inject
    private LiveCastCourseBufferVersion liveCastCourseBufferVersion;


    @ImportService(interfaceClass = UserTagService.class)
    private UserTagService userTagService;

    private ManagedNearBuffer<List<LiveCastCourse>, LiveCastCourseBuffer> liveCastCourseBuffer;


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        liveCastCourseBuffer = NearBufferBuilder.<List<LiveCastCourse>, LiveCastCourseBuffer>newBuilder()
                .name("LiveCastCourseBuffer")
                .category("SERVER")
                .nearBufferClass(LiveCastCourseBuffer.class)
                .initializeNearBuffer(() -> {
                    long version = liveCastCourseBufferVersion.current();
                    List<LiveCastCourse> topicList = makeLiveCastCourseBufferData();
                    return new VersionedBufferData<>(version, topicList);
                })
                .reloadNearBuffer((version, attributes) -> {
                    long current = liveCastCourseBufferVersion.current();
                    if (version < current) {
                        List<LiveCastCourse> topicList = makeLiveCastCourseBufferData();
                        return new VersionedBufferData<>(current, topicList);
                    }
                    return null;
                })
                .reloadNearBuffer(2, TimeUnit.MINUTES)
                .eagerInitUnderProduction(false)
                .autoDestroyWhenShutdown(true)
                .autoReloadUnderUnitTest(false)
                .autoResetUnderUnitTest(true)
                .build();
    }

    private List<LiveCastCourse> makeLiveCastCourseBufferData() {
        List<LiveCastCourse> courseList = liveCastCourseDao.query();
        List<LiveCastCourse> bufferList = new ArrayList<>(courseList.size());
        for (LiveCastCourse liveCastCourse : courseList) {
            List<LiveCastCourse.Segment> lessonSegments = liveCastCourse.getLessonSegments();
            if (CollectionUtils.isEmpty(lessonSegments)) {
                logger.warn("主播同步的课程时段是空的！ course_id = {}", liveCastCourse.getCourseId());
                continue;
            }
            List<LiveCastCourse.Segment> segmentList = lessonSegments.stream().sorted(Comparator.comparing(LiveCastCourse.Segment::getStartTime)).collect(Collectors.toList());
            liveCastCourse.setLessonSegments(segmentList);
            liveCastCourse.setStartDate(segmentList.get(0).getStartTime());
            liveCastCourse.setEndDate(segmentList.get(segmentList.size() - 1).getEndTime());
            Integer subjectId = liveCastCourse.getSubjectId();
            if (subjectId == null) {
                liveCastCourse.setSubject(Subject.ENGLISH);
            } else {
                switch (subjectId) {
                    case 1:
                        liveCastCourse.setSubject(Subject.ENGLISH);
                        break;
                    case 2:
                        liveCastCourse.setSubject(Subject.MATH);
                        break;
                    case 3:
                        liveCastCourse.setSubject(Subject.CHINESE);
                        break;
                    default:
                        liveCastCourse.setSubject(Subject.ENGLISH);
                        break;
                }
            }
            bufferList.add(liveCastCourse);
        }
        return bufferList;
    }

    @Override
    public MapMessage loadLiveCastCardList(Long studentId) {
        String key = "livecast_course_list";
        String positionKey = "top";
        Integer clazzLevel = 3;
        if (studentId != null && studentId != 0) {
            boolean show = true;
            try {
                Set<String> userTag = userTagService.getUserTag(studentId);
                if (userTag.contains("T_06_0003_1")) {
                    show = false;
                }
            } catch (Exception ignore) {

            }
            if (!show) {
                return MapMessage.successMessage().add(key, Collections.emptyList());
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
                return MapMessage.errorMessage();
            }
            if (studentDetail.isInfantStudent()) {
                return MapMessage.successMessage().add(key, Collections.emptyList());
            } else if (studentDetail.isJuniorStudent() || (null != studentDetail.getClazz() && studentDetail.getClazz().isTerminalClazz())) {
                clazzLevel = 7;
            } else if (null != studentDetail.getClazzLevelAsInteger()) {
                clazzLevel = studentDetail.getClazzLevelAsInteger();
            } else {
                ChannelCUserAttribute channelCUserAttribute = studentLoaderClient.loadStudentChannelCAttribute(studentId);
                if (channelCUserAttribute != null) {
                    ChannelCUserAttribute.ClazzCLevel clazzCLevelByClazzJie = ChannelCUserAttribute.getClazzCLevelByClazzJie(channelCUserAttribute.getClazzJie());
                    if (clazzCLevelByClazzJie != null) {
                        if (clazzCLevelByClazzJie.getLevel() < 1 || clazzCLevelByClazzJie.getLevel() > 6) {
                            return MapMessage.successMessage().add(key, Collections.emptyList());
                        }
                        clazzLevel = clazzCLevelByClazzJie.getLevel();
                    }
                }
            }
        }

        Map<String, LiveCastCourse> liveCastCourseMap = liveCastCourseBuffer.getNativeBuffer().getMap();
        if (MapUtils.isEmpty(liveCastCourseMap)) {
            return MapMessage.successMessage().add(key, Collections.emptyList());
        }

        //舍弃以前以时间判断公共课和非公课进行过滤条件
        List<LiveCastCourse> courseList = new ArrayList<>(3);
        for (LiveCastCourse liveCastCourse : liveCastCourseMap.values()) {
            if (clazzLevel.equals(liveCastCourse.getGrade())) {
                courseList.add(liveCastCourse);
            }
        }

        Map<String, LiveCastCourseStudentSub> liveCastCourseStudentSubMap = new HashMap<>();
        if (studentId != null && studentId != 0) {
            Set<String> ids = courseList.stream().map(t -> LiveCastCourseStudentSub.generateId(studentId, t.getCourseId())).collect(Collectors.toSet());
            liveCastCourseStudentSubMap = liveCastCourseStudentSubDao.loads(ids).values().stream().collect(Collectors.toMap(LiveCastCourseStudentSub::getCourseId, Function.identity()));
        }

        List<LiveCastCourse> sortedCourseList = courseList.stream().sorted(Comparator.comparingInt(LiveCastCourse::getRank)).collect(Collectors.toList());
        boolean liveCasting = false;
        List<Map<String, Object>> mapList = new ArrayList<>(sortedCourseList.size());
        for (LiveCastCourse liveCastCourse : sortedCourseList) {
            String courseId = liveCastCourse.getCourseId();
            LiveCastCourseStudentSub studentSub = liveCastCourseStudentSubMap.get(courseId);
            Map<String, Object> courseMap = new LinkedHashMap<>(17);
            courseMap.put("course_id", courseId);
            courseMap.put("title", liveCastCourse.getCourseName());
            courseMap.put("subject", liveCastCourse.getSubject().name());
            courseMap.put("teacher_avatar", liveCastCourse.getCasterAvatarUrl());
            courseMap.put("teacher_name", liveCastCourse.getCourseName());
            courseMap.put("clazz_level", liveCastCourse.getGrade());
            courseMap.put("course_tag", liveCastCourse.getTag());
            courseMap.put("jump_url", liveCastCourse.getDetailUrl());
            //去掉以前根据时间授课，改为课题授课
            if (liveCastCourse.safeIsPayCourse()) {
                courseMap.put("start_date", date2String(liveCastCourse.getStartDate()));
                courseMap.put("note", liveCastCourse.getNote());
                courseMap.put("subscribe_num", -1);
                courseMap.put("label", "￥" + liveCastCourse.getRealPrice());
                courseMap.put("button_text", "立即抢购");
            } else if (liveCastCourse.safeIsPublicCourse()) {
                LiveCastCourse.Segment latestSegment = getLatestSegment(liveCastCourse);
                courseMap.put("start_date", latestSegment == null ? "已结课" : date2String(latestSegment.getStartTime()));

                Long courseSubCount = loadCourseSubCount(courseId);
                courseMap.put("subscribe_num", courseSubCount);
                courseMap.put("note", courseSubCount + "名学生预约");

                String label = labelText(liveCastCourse, studentSub);
                liveCasting = ("直播中".equals(label) && studentSub != null) | liveCasting;
                courseMap.put("label", label);
                courseMap.put("button_text", buttonText(liveCastCourse, studentSub, label));
            }
            mapList.add(courseMap);
        }

        return MapMessage.successMessage().add(key, mapList.stream().count() > 3 ? mapList.stream().limit(3).collect(Collectors.toList()) : mapList).add(positionKey, liveCasting && clazzLevel == 3);
    }

    private String buttonText(LiveCastCourse liveCastCourse, LiveCastCourseStudentSub studentSub, String label) {
        if (studentSub == null) {
            return "免费预约";
        }
        if ("报名中".equals(label) || "已报名".equals(label)) {
            if (CollectionUtils.isEmpty(liveCastCourse.getLessonSegments())) {
                return "没课可上";
            }
            LiveCastCourse.Segment latestSegment = getLatestSegment(liveCastCourse);
            if (latestSegment == null) {
                return "周八上课";
            }
            String week = getWeek(latestSegment.getStartTime());
            return week + "上课";
        } else if ("直播中".equals(label)) {
            return "正在直播";
        } else {
            return "看回放";
        }
    }

    private LiveCastCourse.Segment getLatestSegment(LiveCastCourse liveCastCourse) {
        if (liveCastCourse == null || CollectionUtils.isEmpty(liveCastCourse.getLessonSegments())) {
            return null;
        }
        for (LiveCastCourse.Segment segment : liveCastCourse.getLessonSegments()) {
            if (segment.before15Min() || segment.in15MinPeriod()) {
                return segment;
            }
        }
        return null;
    }

    private String labelText(LiveCastCourse liveCastCourse, LiveCastCourseStudentSub studentSub) {
        for (LiveCastCourse.Segment segment : liveCastCourse.getLessonSegments()) {
            if (segment.in15MinPeriod()) {
                return "直播中";
            }
        }
        long timeMillis = System.currentTimeMillis();
        if (liveCastCourse.getEndDate().getTime() + 15 * 60 * 1000 <= timeMillis) {
            return "回放中";
        }
        if (studentSub == null) {
            return "报名中";
        } else {
            return "已报名";
        }
    }

    private String courseSubCountKey(String courseId) {
        return CacheKeyGenerator.generateCacheKey("liveCastSubCount", new String[]{"cid"}, new Object[]{courseId});
    }

    private Long loadCourseSubCount(String courseId) {
        String key = courseSubCountKey(courseId);
        return VendorCache.getVendorPersistenceCache().load(key);
    }

    public void setCourseSubCount(String courseId, Long subCount) {
        String key = courseSubCountKey(courseId);
        VendorCache.getVendorPersistenceCache().set(key, 0, subCount);
    }

    private String date2String(Date date) {
        String day = DateUtils.dateToString(date, "yyyy/MM/dd");
        String week = getWeek(date);
        String time = DateUtils.dateToString(date, "HH:mm");
        return day + " " + week + " " + time;
    }

    private String getWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case 1:
                return "周日";
            case 2:
                return "周一";
            case 3:
                return "周二";
            case 4:
                return "周三";
            case 5:
                return "周四";
            case 6:
                return "周五";
            case 7:
                return "周六";
            default:
                return "每天";
        }
    }
}
