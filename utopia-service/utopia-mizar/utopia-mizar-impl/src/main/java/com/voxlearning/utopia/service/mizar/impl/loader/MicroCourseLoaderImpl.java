package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseCategory;
import com.voxlearning.utopia.service.mizar.api.constants.microcourse.MicroCourseStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.*;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCourse;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.loader.MicroCourseLoader;
import com.voxlearning.utopia.service.mizar.api.mapper.CoursePeriodMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarCourseMapper;
import com.voxlearning.utopia.service.mizar.impl.dao.microcourse.*;
import com.voxlearning.utopia.service.mizar.impl.dao.user.MizarUserDao;
import com.voxlearning.utopia.service.mizar.impl.service.AsyncMizarCacheServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 微课堂相关 Loader Implementation Class
 * Created by alex on 2016/8/16.
 */
@Named
@Service(interfaceClass = MicroCourseLoader.class)
@ExposeService(interfaceClass = MicroCourseLoader.class)
public class MicroCourseLoaderImpl implements MicroCourseLoader {

    @Inject private AsyncMizarCacheServiceImpl asyncMizarCacheService;

    @Inject private MicroCourseDao microCourseDao;
    @Inject private MicroCoursePeriodDao microCoursePeriodDao;
    @Inject private MicroCourseUserRefDao microCourseUserRefDao;
    @Inject private MicroCoursePeriodRefDao microCoursePeriodRefDao;
    @Inject private CoursePeriodUserRefDao coursePeriodUserRefDao;
    @Inject private MizarUserDao mizarUserDao;
    @Inject private MizarLoaderImpl mizarLoader;

    //--------------------------------------------------------------------
    //------------------          课程相关               -----------------
    //--------------------------------------------------------------------
    @Override
    public Map<String, MicroCourse> loadMicroCourses(Collection<String> courseIds) {
        return microCourseDao.loads(courseIds);
    }

    @Override
    public Page<MicroCourse> findCoursesByParam(String courseName, String category, MicroCourseStatus status, Pageable pageable) {
        // category维度有缓存，优先查询
        if (StringUtils.isNotBlank(category)) {
            List<MicroCourse> resultList = microCourseDao.findCourseByCategory(category)
                    .stream()
                    .filter(c -> !c.isDisabledTrue())
                    .filter(c -> StringUtils.isBlank(courseName) || StringUtils.contains(c.getName(), courseName))
                    .filter(c -> status == null || status.getOrder() == c.getStatus())
                    .sorted(Comparator.comparing(MicroCourse::getStatus))
                    .collect(Collectors.toList());
            return PageableUtils.listToPage(resultList, pageable);
        }
        return microCourseDao.findCourseByName(courseName, status, pageable);
    }

    @Override
    public PageImpl<MizarCourseMapper> loadMicroCoursePage(Long parentId, MizarCourseCategory category, String tag, Pageable pageable) {
        // 获取当前用户所有可见的列表
        List<MizarCourse> courses = mizarLoader.loadUserCourseListIncludeOffline(parentId);
        if (CollectionUtils.isEmpty(courses)) {
            return new PageImpl<>(Collections.emptyList());
        }
        if (MizarCourseCategory.MICRO_COURSE_OPENING != category && MizarCourseCategory.MICRO_COURSE_NORMAL != category) {
            return new PageImpl<>(Collections.emptyList());
        }
        // 根据中间页信息过滤
        courses = courses.stream()
                .filter(c -> category.name().equals(c.getCategory()))
                .filter(c -> StringUtils.isBlank(tag) || StringUtils.equals(tag, c.getSubTitle()))
                .filter(c -> c.getStatus() != null && c.getStatus() == MizarCourse.Status.ONLINE)
                .sorted(MizarCourse::sortCourse)  // 排序 根据是否上线 + 置顶 + 优先级 + 时间
                .collect(toList());
        if (CollectionUtils.isEmpty(courses)) {
            return new PageImpl<>(Collections.emptyList());
        }
        // 先分页 再添加阅读数
        long total = courses.size();
        if (pageable.getPageNumber() * pageable.getPageSize() > total) {
            // 请正确填写页码
            return new PageImpl<>(Collections.emptyList());
        }
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min((int) total, ((pageable.getPageNumber() + 1) * pageable.getPageSize()));
        courses = new LinkedList<>(courses.subList(start, end));
        List<MizarCourseMapper> mappers = convertMappers(courses);
        return new PageImpl<>(mappers, pageable, total);
    }

    @Override
    public Set<String> loadOnlineCourses() {
        return microCourseDao.loadOnlineCourses();
    }

    //--------------------------------------------------------------------
    //------------------          课时相关               -----------------
    //--------------------------------------------------------------------
    @Override
    public Map<String, MicroCoursePeriod> loadCoursePeriods(Collection<String> periodIds) {
        return microCoursePeriodDao.loads(periodIds);
    }

    @Override
    public CoursePeriodMapper loadPeriodMapperById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        // 优先根据课时去加载
        MicroCoursePeriod period = loadCoursePeriod(id);
        if (period != null) {
            return loadPeriodMapperByPeriod(period);
        }

        // 加载不到再根据课程ID去加载
        return loadPeriodMapperByCourse(id);
    }

    @Override
    public Page<MicroCoursePeriod> findPeriodPage(String courseId, String theme, Pageable pageable) {
        if (pageable == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        // 优先按照课程ID查询
        if (StringUtils.isNotBlank(courseId)) {
            List<String> periodIds = microCoursePeriodRefDao.findByCourse(courseId)
                    .stream()
                    .map(MicroCoursePeriodRef::getPeriodId)
                    .distinct()
                    .collect(toList());

            List<MicroCoursePeriod> values = microCoursePeriodDao.loads(periodIds).values()
                    .stream()
                    .sorted(Comparator.comparing(MicroCoursePeriod::getCreateTime).reversed())
                    .collect(toList());
            return PageableUtils.listToPage(values, pageable);
        }
        return microCoursePeriodDao.findByName(theme, pageable);
    }

    //--------------------------------------------------------------------
    //------------------          课程用户关联           -----------------
    //--------------------------------------------------------------------
    @Override
    public List<MicroCourseUserRef> findCourseUserRefByUser(String userId) {
        return microCourseUserRefDao.findByUser(userId);
    }

    @Override
    public List<MicroCourseUserRef> findCourseUserRefByCourse(String courseId) {
        return microCourseUserRefDao.findByCourse(courseId);
    }

    //--------------------------------------------------------------------
    //------------------          课程课时关联           -----------------
    //--------------------------------------------------------------------
    @Override
    public List<MicroCoursePeriodRef> findCoursePeriodRefByCourse(String courseId) {
        return microCoursePeriodRefDao.findByCourse(courseId);
    }

    @Override
    public MicroCoursePeriodRef findCoursePeriodRefByPeriod(String periodId) {
        return microCoursePeriodRefDao.findByPeriod(periodId);
    }

    //--------------------------------------------------------------------
    //------------------          课时用户关联           -----------------
    //--------------------------------------------------------------------
    @Override
    public List<CoursePeriodUserRef> findPeriodUserRefByPeriod(String periodId) {
        return coursePeriodUserRefDao.findByPeriod(periodId);
    }

    //--------------------------------------------------------------------
    //------------------             其  他              -----------------
    //--------------------------------------------------------------------


    //--------------------------------------------------------------------
    //------------------         PRIVATE METHODS         -----------------
    //--------------------------------------------------------------------
    private List<MizarCourseMapper> convertMappers(List<MizarCourse> courses) {
        if (CollectionUtils.isEmpty(courses)) {
            return Collections.emptyList();
        }
        List<MizarCourseMapper> mappers = new ArrayList<>();
        Date now = new Date();
        DecimalFormat df = new DecimalFormat("0.00");
        for (MizarCourse course : courses) {
            MizarCourseMapper mapper = new MizarCourseMapper();
            mapper.setId(course.getId()); // ID
            mapper.setColor(course.getBackground()); // 颜色
            mapper.setStatus(course.getStatus()); // 状态
            mapper.setCategory(course.getCategory()); // 类别
            mapper.setBackground(course.getSpeakerAvatar()); // 图片
            mapper.setRedirectUrl("/mizar/course/go.vpage?id=" + course.getId());
            mapper.setReadCount(asyncMizarCacheService.MizarCourseReadCountManager_loadReadCount(course.getId()).getUninterruptibly()); // 阅读数
            // 获取到课时信息
            String periodId = course.getTitle();
            CoursePeriodMapper periodMapper = loadPeriodListItem(periodId);
            if (periodMapper != null) {
                mapper.setTags(Collections.singletonList(periodMapper.getCategory())); // 标签
                mapper.setTitle(periodMapper.getPeriodName()); // 标题
                mapper.setClassTime(periodMapper.fetchClassTime()); // 上课时间
                mapper.setKeynoteSpeaker(periodMapper.getLecturerName()); // 主讲人名称
                mapper.setSpeakerAvatar(periodMapper.getLecturerAvatar()); // 主讲人头像
                mapper.setSoldOut(periodMapper.live(now)); // 正在直播
                mapper.setPrice(df.format(periodMapper.getPrice())); // 价格
            }
            mappers.add(mapper);
        }
        return mappers;
    }

    private CoursePeriodMapper loadPeriodListItem(String periodId) {
        if (StringUtils.isBlank(periodId)) {
            return null;
        }
        MicroCoursePeriod period = loadCoursePeriod(periodId);
        if (period == null) {
            return null;
        }
        MicroCourse course = null;
        MizarUser lecturer = null;
        // 获取课时与课程关联
        MicroCoursePeriodRef ref = findCoursePeriodRefByPeriod(periodId);
        if (ref != null) {
            course = loadMicroCourse(ref.getCourseId());
            // 列表页需要取主讲老师信息
            lecturer = findLecturer(ref.getCourseId());
        }
        return CoursePeriodMapper.newInstance()
                .withPeriod(period)
                .withLecturer(lecturer)
                .withCourse(course); // 一定要最后再初始化课程，以便根据课程购买
    }

    private CoursePeriodMapper loadPeriodMapperByPeriod(MicroCoursePeriod period) {
        if (period == null) {
            return null;
        }
        // 获取课时与课程关联
        MicroCoursePeriodRef ref = findCoursePeriodRefByPeriod(period.getId());
        MicroCourse course = null;
        List<MicroCoursePeriod> series = null;
        if (ref != null) {
            course = loadMicroCourse(ref.getCourseId());
            // 详情页面需要取系列类课程
            series = findSeries(ref.getCourseId())
                    .stream()
                    .filter(p -> !period.getId().equals(p.getId()))
                    .collect(toList());
        }
        return CoursePeriodMapper.newInstance()
                .withPeriod(period)
                .withSeries(series)
                .withCourse(course); // 一定要最后再初始化课程，以便根据课程购买
    }

    private CoursePeriodMapper loadPeriodMapperByCourse(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return null;
        }
        MicroCourse course = loadMicroCourse(courseId);
        if (course == null) {
            return null;
        }
        // 获取课时与课程关联
        List<MicroCoursePeriod> series = null;
        MicroCoursePeriod period = null;

        // 详情页面需要取系列类课程
        List<MicroCoursePeriod> periods = findSeries(courseId);
        if (periods.size() > 0) {
            period = periods.get(0);
            series = periods.subList(1, periods.size());
        }
        return CoursePeriodMapper.newInstance()
                .withPeriod(period)
                .withSeries(series)
                .withCourse(course); // 一定要最后再初始化课程，以便根据课程购买
    }

    private List<MicroCoursePeriod> findSeries(String courseId) {
        // 列表页需要取主讲老师信息
        List<String> periodRefs = findCoursePeriodRefByCourse(courseId)
                .stream()
                .map(MicroCoursePeriodRef::getPeriodId)
                .distinct()
                .collect(toList());
        return new LinkedList<>(loadCoursePeriods(periodRefs).values());
    }

    private MizarUser findLecturer(String courseId) {
        // 列表页需要取主讲老师信息
        List<String> lecturerIds = findCourseUserRefByCourse(courseId)
                .stream()
                .filter(r -> MicroCourseUserRef.CourseUserRole.Lecturer == r.getRole())
                .map(MicroCourseUserRef::getUserId)
                .collect(toList());
        return mizarUserDao.loads(lecturerIds).values()
                .stream()
                .filter(MizarUser::isValid)
                .findFirst().orElse(null);
    }

}
