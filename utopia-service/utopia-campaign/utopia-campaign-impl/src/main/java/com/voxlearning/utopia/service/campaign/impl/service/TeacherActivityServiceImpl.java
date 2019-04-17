package com.voxlearning.utopia.service.campaign.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.entity.activity.MathActivityRecord;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityService;
import com.voxlearning.utopia.service.campaign.api.entity.*;
import com.voxlearning.utopia.service.campaign.api.mapper.CourseInvitation;
import com.voxlearning.utopia.service.campaign.api.mapper.MathActivityConfig;
import com.voxlearning.utopia.service.campaign.impl.dao.*;
import com.voxlearning.utopia.service.campaign.impl.internal.Internal17JTService;
import com.voxlearning.utopia.service.campaign.impl.internal.filter.CourseQueryFilter;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.core.util.CacheKeyGenerator.generateCacheKey;
import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;
import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;
import static java.util.stream.Collectors.toMap;

@Named
@Service(interfaceClass = TeacherActivityService.class)
@ExposeService(interfaceClass = TeacherActivityService.class)
public class TeacherActivityServiceImpl extends SpringContainerSupport implements TeacherActivityService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherActivityService.class);
    private static final String COMMON_CONFIG_KEY = "TAIAN_MATH_ACTIVITY_CONFIG";
    private static final String COURSE_INVITATION_ATOMIC_CACHE = "TeacherActivity:CourseInvitation";

    @Inject private TeacherVocationLotteryDao teacherVocationLotteryDao;
    @Inject private MathActivityRecordDao mathActDao;
    @Inject private CampaignCacheSystem cacheSystem;
    @Inject private DeprecatedGroupLoaderClient groupLoaderCli;
    @Inject private TeacherLoaderClient teacherLoaderCli;
    @Inject private SchoolLoaderClient schoolLoaderCli;
    @Inject private Internal17JTService _17jtService;
    @Inject private UserIntegralServiceClient usrIntegralSrvCli;
    @Inject private YiqiJTCourseCatalogDao courseCatalogDao;
    @Inject private YiqiJTCourseChoiceNoteDao courseChoiceNoteDao;
    @Inject private YiqiJTCourseGradeDao courseGradeDao;
    @Inject private YiqiJTCourseSubjectDao courseSubjectDao;
    @Inject private YiqiJTCourseDao courseDao;
    @Inject private YiqiJTCourseGradeDao gradeDao;
    @Inject private YiqiJTCourseSubjectDao subjectDao;
    @Inject private YiqiJTCourseOuterchainDao outerchainDao;

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public TeacherVocationLottery loadTeacherVocationLottery(Long teacherId) {
        return teacherVocationLotteryDao.loadByTeacherId(teacherId);
    }

    @Override
    public MapMessage updateTeacherVocationLottery(TeacherVocationLottery lotteryRecord) {
        try {
            teacherVocationLotteryDao.upsert(lotteryRecord);
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage incTVLRecordFields(Long teacherId, Map<String, Object> fieldDeltaMap) {
        MapMessage resultMsg = new MapMessage();
        resultMsg.setSuccess(teacherVocationLotteryDao.incMultiFields(teacherId, fieldDeltaMap));

        return resultMsg;
    }

    private List<MathActivityRecord> initTAMActivityRecord(Long tId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<List<MathActivityRecord>>newBuilder()
                    .keyPrefix("TAMInitRecord")
                    .keys(tId)
                    .callback(() -> {
                        // 考虑包班制
                        List<Long> allTchIds = new ArrayList<>(teacherLoaderCli.loadSubTeacherIds(tId));
                        allTchIds.add(tId);

                        // 肯定是只看数学老师
                        TeacherDetail td = teacherLoaderCli.loadTeacherDetails(allTchIds)
                                .values()
                                .stream()
                                .filter(t -> t.getSubject() == Subject.MATH)
                                .findFirst()
                                .orElse(null);
                        if (td == null) {
                            logger.error("TAM:初始化数据失败!未找到数学老师,mainId:{}", tId);
                            return Collections.emptyList();
                        }

                        List<GroupTeacherMapper> groups = groupLoaderCli.loadTeacherGroups(td.getId(), true);
                        int currPhase = getCurrentPhase();
                        return groups.stream().map(group -> {
                            List<MathActivityRecord> newRecords = new ArrayList<>();
                            // 把老师从第一期开始到最近这一期的所有数据都初始化好
                            for (int phase = 1; phase <= currPhase; phase++) {
                                MathActivityRecord record = new MathActivityRecord();
                                record.setTeacherId(tId);
                                record.setTeacherName(td.fetchRealname());

                                Clazz clazz = raikouSDK.getClazzClient()
                                        .getClazzLoaderClient()
                                        .loadClazz(group.getClazzId());
                                record.setGroupId(group.getId());
                                record.setClazz(clazz.getClazzLevel().getLevel());
                                record.setStuNum(group.getStudents().size());
                                record.setPhase(phase);
                                record.setRank(null);
                                record.setClazzName(clazz.getClassName());

                                School school = schoolLoaderCli.getSchoolLoader()
                                        .loadSchool(td.getTeacherSchoolId())
                                        .getUninterruptibly();
                                // 用简称
                                if (school != null)
                                    record.setSchoolName(school.getShortName());
                                // 初始化的数据，要和参加模块成绩为零的数据区分开
                                record.setFinishNum(null);
                                record.setAvgScore(null);

                                record = mathActDao.upsert(record);
                                newRecords.add(record);
                            }
                            return newRecords;
                        }).flatMap(Collection::stream).collect(Collectors.toList());
                    })
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return Collections.emptyList();
        } catch (Throwable t) {
            logger.error("Init TAM record error!teacherId:{}", tId, t);
            return Collections.emptyList();
        }
    }

    @Override
    public List<MathActivityRecord> loadTeacherRecords(Long teacherId) {
        List<MathActivityRecord> records = mathActDao.loadByTeacherId(teacherId);
        // 没有记录的话，则初始化
        if (CollectionUtils.isEmpty(records)) {
            return initTAMActivityRecord(teacherId);
        }

        return records;
    }

    @Override
    public int gupSelectedClazz(Long teacherId, Integer clazz) {
        int expireSeconds = DateUtils.getCurrentToMonthEndSecond();
        Cache cache = cacheSystem.CBS.persistence;

        List<Long> allTchIds = new ArrayList<>();
        allTchIds.add(teacherId);
        allTchIds.addAll(teacherLoaderCli.loadSubTeacherIds(teacherId));

        // 取老师教的年级最低的那个班
        Function<Long, Integer> lowestClazzFunc = tId -> groupLoaderCli.loadTeacherGroups(allTchIds, false)
                .values()
                .stream()
                .flatMap(Collection::stream)
                // 这个地方要过滤年纪，不然很恶心
                .filter(gm -> gm.getSubject() == Subject.MATH)
                .map(g -> raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(g.getClazzId()))
                .map(c -> c.getClazzLevel().getLevel())
                .sorted(Integer::compare)
                .findFirst()
                .orElse(1);

        return cache.<Long, Integer>createCacheValueLoader()
                .cacheHitListener(key -> {
                    if (clazz != null && clazz > 0) cache.set(key, expireSeconds, clazz);
                })
                .keyGenerator(this::genSelectedClazzCacheKey)
                .keys(Collections.singleton(teacherId))
                .loads()
                .expiration(expireSeconds)
                .externalLoader(tchIds -> tchIds.stream().collect(toMap(a -> a, lowestClazzFunc)))
                .loadsMissed()
                .write()
                .getAndResortResult()
                .getOrDefault(teacherId, 0);
    }

    @Override
    public MathActivityConfig loadActivityConfig() {
        try {
            String configStr = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), COMMON_CONFIG_KEY);
            return JsonUtils.fromJson(configStr, MathActivityConfig.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getCurrentPhase() {
        MathActivityConfig config = loadActivityConfig();
        if (config == null)
            return 1;

        return config.judgePhase(new Date());
    }

    private String genSelectedClazzCacheKey(Long teacherId) {
        return generateCacheKey("TaiAnMathAct:selectedClazz", new String[]{"teacherId"}, new Object[]{teacherId});
    }

    @Override
    public List<MathActivityRecord> loadActivityRank(int phase, int clazz) {
        return mathActDao.loadByPhaseAndClazz(phase, clazz, 50)
                .stream()
                .filter(r -> r.getAvgScore() != null)
                .filter(r -> {
                    if (phase == TOTAL_RANK_PHASE)
                        return toInt(r.getFinishNum()) >= 3;
                    else
                        return toInt(r.getFinishNum()) >= TAM_RANK_FINN_LIMIT;
                })
                .filter(r -> toInt(r.getStuNum()) >= TAM_RANK_STUN_LIMIT)
                .collect(Collectors.toList());
    }

    @Override
    public MapMessage upsertMathMatchRecord(MathActivityRecord record) {
        mathActDao.upsert(record);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage flushMathMatchRank(int phase, int clazz) {
        String cacheKey = generateCacheKey(
                MathActivityRecord.class,
                new String[]{"phase", "clazz"},
                new Object[]{phase, clazz});
        mathActDao.getCache().delete(cacheKey);
        return MapMessage.successMessage();
    }

    @Override
    public Date loadCourseBuyTime(Long teacherId, Long courseId) {
        return _17jtService.loadCourseBuyTime(teacherId, courseId).orElse(null);
    }

    @Override
    public MapMessage load17JTUserCourse(Long teacherId, Long courseId) {
        if (teacherId == null || courseId == null)
            return MapMessage.errorMessage("参数为空!");

        YiqiJTCourse course = _17jtService.loadCourseById(courseId);
        if (course == null)
            return MapMessage.errorMessage("课程不存在!");

        boolean bought = true;
        long price = course.getPrice();
        MapMessage resultMsg = MapMessage.successMessage();
        if (price > 0) {
            Optional<Date> optionalDate = _17jtService.loadCourseBuyTime(teacherId, courseId);
            Date buyTime = optionalDate.map(t -> optionalDate.get()).orElse(null);
            bought = buyTime != null;

            resultMsg.add("buyTime", buyTime);
            resultMsg.add("bought", bought);

            // 是否在有效期内
            boolean inValidPeriod = Optional.ofNullable(buyTime)
                    .map(t -> DateUtils.addSeconds(t, toInt(course.getActiveTime())))
                    .map(t -> (new Date()).before(t))
                    .orElse(false);
            resultMsg.add("inValidPeriod", inValidPeriod);
        } else {//免费课程
            resultMsg.add("buyTime", new Date());
            resultMsg.add("bought", bought);
            resultMsg.add("inValidPeriod", true);
        }

        Date now = new Date();
        // 查看是否开启了
        resultMsg.add("isOpen", Optional.ofNullable(course.getOpenTime()).map((now)::after).orElse(false));

        if (bought) {
            Date videoExpTime = DateUtils.addSeconds(now, 1800);
            // url添加鉴权参数
            resultMsg.add("url", _17jtService.wrapAuth(course.getUrl(), videoExpTime));
        }
        String unit = "学豆";
        resultMsg.add("unit", unit);
        resultMsg.add("cost", price);
        return resultMsg;
    }

    @Override
    public Map<String, Object> load17JTUserData(Long userId) {
        if (userId == null || userId == 0)
            return null;

        return null;
    }

    @Override
    public List<YiqiJTCourse> load17JTCourseList() {
        // FIXME 着急上活动，最开始只有3个视频，先写死，后面入库
        // FIXME 时间的配置也是
        return _17jtService.loadAllCourses();
    }

    @Override
    public MapMessage buy17JTCourse(Long userId, Long courseId) {
        try {
            Teacher teacher = teacherLoaderCli.loadTeacher(userId);
            Validate.notNull(teacher, "老师不存在!");
            Validate.isTrue(courseId != null && courseId > 0, "课程不存在!");

            int num = QIYIJI_PRICE;
            String unit = "学豆";
            if (teacher.isPrimarySchool()) {
                unit = "园丁豆";
                num = QIYIJI_PRICE / 10;
            }

            YiqiJTCourse course = _17jtService.loadCourseById(courseId);
            if (course == null) {
                return MapMessage.errorMessage("课程不存在!");
            }

            if (course.getPrice() > 0) {
                IntegralHistory integralHistory = new IntegralHistory(userId, IntegralType.TEACHER_17JT_BUY_COURSE, -SafeConverter.toInt(course.getPrice()));
                integralHistory.setComment("购买一起讲堂视频课程");

                MapMessage resultMsg = usrIntegralSrvCli.getUserIntegralService().changeIntegral(integralHistory);
                Validate.isTrue(resultMsg.isSuccess(), "您的" + unit + "不够" + num + "个哦");
            }

            _17jtService.updateBuyData(userId, courseId, new Date());
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage unlockJTCourse(Long userId, Long courseId) {
        try {
            _17jtService.updateBuyData(userId, courseId, new Date());
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage fixJTData(Long userId) {
        return _17jtService.fixData(userId);
    }

    @Override
    public String wrapAuth(String url, Date expTime) {
        return _17jtService.wrapAuth(url, expTime);
    }

    @Override
    public YiqiJTCourse loadCourseById(long courseId) {
        return courseDao.load(courseId);
    }

    @Override
    public YiqiJTCourse upsertCourse(YiqiJTCourse course) {
        YiqiJTCourse upsert = courseDao.upsert(course);
        delYQJTRawsCache();
        return upsert;
    }

    @Override
    public void upsertCourseSubject(List<YiqiJTCourseSubject> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return;
        }
        subjectDao.deleteByCourseId(subjects.get(0).getCourseId());
        subjectDao.inserts(subjects);
        delYQJTRawsCache();
    }

    @Override
    public void upsertCourseGeade(List<YiqiJTCourseGrade> grades) {
        if (grades == null || grades.isEmpty()) {
            return;
        }
        gradeDao.deleteByCourseId(grades.get(0).getCourseId());
        gradeDao.inserts(grades);
        delYQJTRawsCache();
    }

    @Override
    public void addCourseCatalog(YiqiJTCourseCatalog courseCatalog) {
        courseCatalogDao.insert(courseCatalog);
        delYQJTRawsCache();
    }

    @Override
    public boolean delCourseCatalog(long catalogId) {
        boolean remove = courseCatalogDao.remove(catalogId);
        delYQJTRawsCache();
        return remove;
    }

    @Override
    public List<YiqiJTCourseCatalog> getCourseCatalogsByCourseId(long courseId) {
        return courseCatalogDao.getCourseCatalogsByCourseId(courseId);
    }

    @Override
    public YiqiJTCourseCatalog getCourseCatalogById(long id) {
        return courseCatalogDao.load(id);
    }

    @Override
    public List<YiqiJTCourseCatalog> loadCourseCatalogList() {
        return courseCatalogDao.query();
    }

    @Override
    public void upsertCourseChoiceNote(YiqiJTChoiceNote choiceNote) {
        courseChoiceNoteDao.upsert(choiceNote);
        delYQJTRawsCache();
    }

    @Override
    public boolean delCourseChoiceNote(long noteid) {
        boolean remove = courseChoiceNoteDao.remove(noteid);
        delYQJTRawsCache();
        return remove;
    }

    @Override
    public List<YiqiJTChoiceNote> getCourseNotesByCourseId(long courseId) {
        return courseChoiceNoteDao.getCourseNotesByCourseId(courseId);
    }

    @Override
    public YiqiJTChoiceNote getCourseNoteById(long id) {
        return courseChoiceNoteDao.load(id);
    }

    @Override
    public List<YiqiJTChoiceNote> loadCourseNoteList() {
        return courseChoiceNoteDao.query();
    }

    @Override
    public List<YiqiJTCourseGrade> getGradesByCourdeId(long courdeId) {
        return courseGradeDao.getCourseNotesByCourseId(courdeId);
    }

    @Override
    public List<YiqiJTCourseGrade> getAllGrade() {
        return courseGradeDao.query();
    }

    @Override
    public List<YiqiJTCourseSubject> getAllSubject() {
        return courseSubjectDao.query();
    }

    @Override
    public List<YiqiJTCourseSubject> getSubjectsByCourseId(long courdeId) {
        return courseSubjectDao.getCourseNotesByCourseId(courdeId);
    }

    @Override
    public int updateCourseTopNum(long courseId, int topNum) {
        int i = courseDao.updateCourseTopNum(courseId, topNum);
        delYQJTRawsCache();
        return i;

    }

    @Override
    public int updateCourseStatus(long courseId, int status) {
        int i = courseDao.updateCourseStatus(courseId, status);
        delYQJTRawsCache();
        return i;
    }

    @Override
    public List<YiqiJTCourseOuterchain> getCourseOuterchainsByCourseId(long coueseId) {
        return outerchainDao.getCourseOuterchainByCourseId(coueseId);
    }

    @Override
    public YiqiJTCourseOuterchain getCourseOuterchainById(long id) {
        return outerchainDao.load(id);
    }

    @Override
    public void addCourseOuterchain(YiqiJTCourseOuterchain courseOuterchain) {
        outerchainDao.insert(courseOuterchain);
        delYQJTRawsCache();
    }

    @Override
    public boolean delCourseOuterchain(long id) {
        boolean remove = outerchainDao.remove(id);
        delYQJTRawsCache();
        return remove;
    }

    @Override
    public List<YiqiJTCourse> select17JTCourseList(String courseName, List<Long> gradeList, List<Long> subjectList) {
        CourseQueryFilter filter = new CourseQueryFilter();
        filter.setTitle(courseName);
        filter.setGrade(gradeList);
        filter.setSubject(subjectList);
        return courseDao.select17JTCourseList(filter);
    }

    @Override
    public MapMessage add17JTReadCount(Long id) {
        try {
            courseDao.incrReadCount(id, 1L);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage add17JTCollectCount(Long id, Long incrValue) {
        if (incrValue == null) incrValue = 1L;
        try {
            courseDao.incrCollectCount(id, incrValue);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage startCourseInvitation(Long teacherId, Long courseId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance().<MapMessage>newBuilder()
                    .keyPrefix(COURSE_INVITATION_ATOMIC_CACHE)
                    .keys(teacherId, courseId)
                    .callback(() -> atomicStartCourseInvitation(teacherId, courseId))
                    .build().execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请重试...");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return MapMessage.errorMessage();
        }
    }

    private MapMessage atomicStartCourseInvitation(Long teacherId, Long courseId) {
        String cacheKey = genWechatUserCacheKey(teacherId, courseId);
        CacheObject<Object> cacheObject = cacheSystem.CBS.persistence.get(cacheKey);
        if (cacheObject.containsValue()) {
            CourseInvitation old = ((CourseInvitation) cacheObject.getValue());
            if (!old.fetchExpire() && CollectionUtils.isNotEmpty(old.getHelpers())) {
                return MapMessage.errorMessage("邀请正在进行中");
            }
        }

        CourseInvitation courseInvitation = new CourseInvitation();
        courseInvitation.setUserId(teacherId);
        courseInvitation.setCourseId(courseId);
        courseInvitation.setHelpers(new ArrayList<>());
        courseInvitation.setInvitationDate(new Date());

        cacheSystem.CBS.persistence.set(cacheKey, courseInvitation.fetchCacheExpirationInSeconds(), courseInvitation);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage cancelCourseInvitation(Long teacherId, Long courseId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance().<MapMessage>newBuilder()
                    .keyPrefix(COURSE_INVITATION_ATOMIC_CACHE)
                    .keys(teacherId, courseId)
                    .callback(() -> atomicCancelCourseInvitation(teacherId, courseId))
                    .build().execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请重试...");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return MapMessage.errorMessage();
        }
    }

    private MapMessage atomicCancelCourseInvitation(Long teacherId, Long courseId) {
        String cacheKey = genWechatUserCacheKey(teacherId, courseId);
        cacheSystem.CBS.persistence.delete(cacheKey);
        return MapMessage.successMessage();
    }

    @Override
    public CourseInvitation loadCourseInvitation(Long teacherId, Long courseId) {
        String cacheKey = genWechatUserCacheKey(teacherId, courseId);
        CacheObject<Object> cacheObject = cacheSystem.CBS.persistence.get(cacheKey);
        if (cacheObject.containsValue()) {
            return (CourseInvitation) cacheObject.getValue();
        }
        return null;
    }

    @Override
    public MapMessage helperCourseInvitation(String openId, String nickName, String imgUrl, Long teacherId, Long courseId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance().<MapMessage>newBuilder()
                    .keyPrefix(COURSE_INVITATION_ATOMIC_CACHE)
                    .keys(teacherId, courseId)
                    .callback(() -> atomicHelperCourseInvitation(openId, nickName, imgUrl, teacherId, courseId))
                    .build().execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请重试...");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return MapMessage.errorMessage();
        }
    }

    private MapMessage atomicHelperCourseInvitation(String openId, String nickName, String imgUrl, Long teacherID, Long courseId) {
        try {
            // 这里按照 openId 再次加锁
            return AtomicCallbackBuilderFactory.getInstance().<MapMessage>newBuilder()
                    .keyPrefix(COURSE_INVITATION_ATOMIC_CACHE)
                    .keys(openId)
                    .callback(() -> atomicHelperCourseInvitation(teacherID, courseId, openId, nickName, imgUrl))
                    .build().execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请重试...");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return MapMessage.errorMessage();
        }
    }

    private MapMessage atomicHelperCourseInvitation(Long teacherId, Long courseId, String openId, String nickName, String imgUrl) {
        // 判断助力者是否还有机会
        if (!haveOpportunity(openId)) {
            return MapMessage.errorMessage("您的助力次数已耗尽，请看看其他课程吧");
        }

        String cacheKey = genWechatUserCacheKey(teacherId, courseId);
        CacheObject<Object> cacheObject = cacheSystem.CBS.persistence.get(cacheKey);
        if (cacheObject.containsValue()) {
            CourseInvitation courseInvitation = (CourseInvitation) cacheObject.getValue();
            if (courseInvitation.fetchExpire()) {
                return MapMessage.errorMessage("邀请已过期");
            }
            List<CourseInvitation.Helper> helpers = courseInvitation.getHelpers();
            if (helpers == null) helpers = new ArrayList<>();

            if (helpers.size() >= 3) {
                return MapMessage.errorMessage("邀请已结束");
            }
            for (CourseInvitation.Helper helper : helpers) {
                if (Objects.equals(helper.getOpenId(), openId)) {
                    return MapMessage.errorMessage("您已助力");
                }
            }

            CourseInvitation.Helper helper = new CourseInvitation.Helper();
            helper.setOpenId(openId);
            helper.setNickName(nickName);
            helper.setImgUrl(imgUrl);
            helper.setCreateTime(new Date());
            helpers.add(helper);

            cacheSystem.CBS.persistence.set(cacheKey, courseInvitation.fetchCacheExpirationInSeconds(), courseInvitation);
            incrOpportunity(openId);

            // 助力结束, 给老师添加购买记录
            if (helpers.size() >= 3) {
                _17jtService.updateBuyData(teacherId, courseId, new Date());
            }
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("邀请已过期");
    }

    /**
     * 判断微信用户是否还有助力机会(每人每月3次)
     */
    private boolean haveOpportunity(String openId) {
        String openIdCacheKey = genWechatUserCacheKey(openId);
        CacheObject<Object> objectCacheObject = cacheSystem.CBS.persistence.get(openIdCacheKey);
        if (objectCacheObject.containsValue()) {
            String opportunity = (String) objectCacheObject.getValue();
            int opportunityInt = SafeConverter.toInt(opportunity.trim()); // 这里有坑
            return opportunityInt < 3;
        }
        return true;
    }

    /**
     * 消耗一次微信用户的助力机会
     */
    private void incrOpportunity(String openId) {
        String openIdCacheKey = genWechatUserCacheKey(openId);
        cacheSystem.CBS.persistence.incr(openIdCacheKey, 1, 1, DateUtils.getCurrentToMonthEndSecond());
    }

    @NotNull
    private String genWechatUserCacheKey(Long teacherId, Long courseId) {
        return CacheKeyGenerator.generateCacheKey("TeacherActivity:CourseInvitation", new String[]{"TID", "CID"}, new Object[]{teacherId, courseId});
    }

    @NotNull
    private String genWechatUserCacheKey(String openId) {
        return CacheKeyGenerator.generateCacheKey("CourseInvitation:helper", new String[]{"OID"}, new Object[]{openId});
    }

    /**
     * 清除一起讲堂的全量 cache (老师教学助手)
     */
    private void delYQJTRawsCache() {
        cacheSystem.CBS.flushable.delete(YQJT_ALL_RAW_CACHE_KEY);
    }

}
