package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DateRangeUnit;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.utopia.api.constant.FlowerSourceType;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.campaign.api.MoralMedalService;
import com.voxlearning.utopia.service.campaign.api.constant.MoralMedalConstant;
import com.voxlearning.utopia.service.campaign.api.entity.MoralMedal;
import com.voxlearning.utopia.service.campaign.api.enums.MoralMedalEnum;
import com.voxlearning.utopia.service.campaign.api.mapper.ClazzMoralPerformance;
import com.voxlearning.utopia.service.campaign.api.mapper.HistoryMedal;
import com.voxlearning.utopia.service.campaign.api.mapper.MoralMedalClazzDetail;
import com.voxlearning.utopia.service.campaign.api.mapper.MoralMedalTeacherClazz;
import com.voxlearning.utopia.service.campaign.impl.dao.MoralMedalDao;
import com.voxlearning.utopia.service.campaign.impl.support.MoralMedalShareUtils;
import com.voxlearning.utopia.service.campaign.impl.support.SendUtils;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.flower.api.FlowerConditionService;
import com.voxlearning.utopia.service.user.api.constants.UserConstants;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.utopia.service.campaign.api.constant.MoralMedalConstant.MORAL_MEDAL_ACTIVITY_NAME;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Named
@Slf4j
@ExposeService(interfaceClass = MoralMedalService.class)
public class MoralMedalServiceImpl implements MoralMedalService {

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private MoralMedalDao moralMedalDao;
    @Inject
    protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject
    private GroupLoaderClient groupLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    protected ParentLoaderClient parentLoaderClient;

    @ImportService(interfaceClass = FlowerConditionService.class)
    private FlowerConditionService flowerConditionService;

    @Inject
    private SendUtils sendUtils;

    /**
     * 这个 topic 名字很怪, 不要关注名字了
     */
    @AlpsPubsubPublisher(topic = "utopia.campaign.teacher.new.term.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher parentPublisher;

    @Override
    public MapMessage loadTeacherGroup(Long teacherId) {
        // 查询老师 主副老师所有组
        List<GroupMapper> teacherGroups = getTeacherGroups(teacherId);

        Set<Long> clazzIds = teacherGroups.stream().map(GroupMapper::getClazzId).collect(toSet());
        Set<Long> groupIds = teacherGroups.stream().map(GroupMapper::getId).collect(toSet());
        Map<Long, Clazz> clazzMap = deprecatedClazzLoaderClient.getRemoteReference().loadClazzs(clazzIds);

        // 排序
        Comparator<Clazz> clazzComparator = Comparator.comparing(Clazz::getClazzLevel).thenComparing(o -> findNumber(o.formalizeClazzName()));
        Comparator<GroupMapper> groupComparator = Comparator.comparing(GroupMapper::getSubject);
        teacherGroups.sort(((Comparator<GroupMapper>) (o1, o2) -> {
            Clazz clazz1 = clazzMap.get(o1.getClazzId());
            Clazz clazz2 = clazzMap.get(o2.getClazzId());
            return clazzComparator.compare(clazz1, clazz2);
        }).thenComparing(groupComparator));

        Map<Long, List<User>> groupStudentMap = studentLoaderClient.loadGroupStudents(groupIds);

        List<MoralMedalTeacherClazz> resultList = new ArrayList<>();

        for (GroupMapper teacherGroup : teacherGroups) {
            long groupStudentCount = groupStudentMap.getOrDefault(teacherGroup.getId(), Collections.emptyList())
                    .stream()
                    .filter(i -> !Objects.equals(i.getProfile().getRealname(), UserConstants.EXPERIENCE_ACCOUNT_NAME))
                    .distinct()
                    .count();

            Clazz clazz = clazzMap.get(teacherGroup.getClazzId());
            if (clazz == null) continue;

            String createTime = teacherGroup.getCreateDatetime() == null ? ""
                    : DateFormatUtils.format(teacherGroup.getCreateDatetime(), "yyyy-MM-dd");

            MoralMedalTeacherClazz medalTeacherClazz = new MoralMedalTeacherClazz();
            medalTeacherClazz.setClazzId(clazz.getId());
            medalTeacherClazz.setClazzName(clazz.formalizeClazzName());
            medalTeacherClazz.setGroupId(teacherGroup.getId());
            medalTeacherClazz.setGroupSubject(teacherGroup.getSubject().name());
            medalTeacherClazz.setGroupSubjectName(teacherGroup.getSubject().getValue());
            medalTeacherClazz.setCreateTime(createTime);
            medalTeacherClazz.setStudentSize(groupStudentCount);
            resultList.add(medalTeacherClazz);
        }
        return MapMessage.successMessage().add("data", resultList);
    }

    @Override
    @SuppressWarnings("all")
    public MapMessage loadTeacherGroupDetail(Long teacherId, Long groupId) {
        Group group = groupLoaderClient.getGroupLoader().loadGroup(groupId).getUninterruptibly();
        if (group == null) {
            return MapMessage.errorMessage("班组不存在");
        }

        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadClazz(group.getClazzId());
        if (group == null) {
            return MapMessage.errorMessage("班级不存在");
        }

        // 查询老师 主副老师所有组
        Set<Long> teacherGroupIdSet = getTeacherGroups(teacherId).stream().map(i -> i.getId()).collect(toSet());
        if (!teacherGroupIdSet.contains(groupId)) {
            return MapMessage.errorMessage("老师和班组没有关系");
        }

        Collator compare = Collator.getInstance(java.util.Locale.CHINA);

        List<User> userList = studentLoaderClient.loadGroupStudents(groupId)
                .stream()
                .filter(i -> !Objects.equals(i.getProfile().getRealname(), UserConstants.EXPERIENCE_ACCOUNT_NAME))
                .distinct()
                .sorted((o1, o2) -> compare.compare(o1.getProfile().getRealname(), o2.getProfile().getRealname()))
                .collect(toList());

        // 查询勋章
        Set<Long> studentIdList = userList.stream().map(LongIdEntity::getId).collect(toSet());
        Map<Long, List<MoralMedal>> studentMoralMap = moralMedalDao.loadByStudentIds(studentIdList);

        List<MoralMedalClazzDetail.Student> studentList = userList.stream().map(i -> {
            MoralMedalClazzDetail.Student student = new MoralMedalClazzDetail.Student();
            student.setId(i.getId());
            student.setName(i.getProfile().getRealname());
            student.setImg(i.fetchImageUrl());

            List<MoralMedal> studentMedal = studentMoralMap.getOrDefault(i.getId(), Collections.emptyList());
            student.setMoralCount(studentMedal.size());
            return student;
        }).collect(toList());

        MoralMedalClazzDetail detail = new MoralMedalClazzDetail();
        detail.setClazzId(clazz.getId());
        detail.setClazzName(clazz.formalizeClazzName());
        detail.setGroupId(group.getId());
        detail.setGroupName(group.getGroupName());
        detail.setStudentSize(studentList.size());
        detail.setStudentList(studentList);

        DayRange todayRange = DayRange.current();
        List<MoralMedal> moralMedals = moralMedalDao.loadByTeacherIdGroupId(teacherId, group.getId());
        long todayCount = moralMedals.stream().filter(i -> todayRange.contains(i.getCreateDatetime())).count();

        detail.setTodayCount(todayCount);
        detail.setSemesterCount(moralMedals.size());

        return MapMessage.successMessage().add("data", detail);
    }

    @Override
    public MapMessage loadMedalDetail(Long moralMedalId) {
        MoralMedal medal = moralMedalDao.load(moralMedalId);
        if (medal == null) {
            return MapMessage.errorMessage();
        }

        MoralMedalEnum moralMedalEnum = MoralMedalEnum.valueOfById(medal.getMedalId());
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(medal.getTeacherId());

        assert moralMedalEnum != null;

        return MapMessage.successMessage()
                .add("teacher_id", teacherDetail.getId())
                .add("teacher_name", teacherDetail.fetchRealname())
                .add("medal_id", medal.getId())
                .add("medal_name", moralMedalEnum.name())
                .add("medal_icon", moralMedalEnum.getIcon())
                .add("parent_text", MoralMedalShareUtils.getShareText(Collections.singleton(medal.getMedalId())))
                .add("liked", medal.getLiked());
    }

    @Override
    public MapMessage parentLike(Long parentId, Long moralMedalId) {
        try {
            MoralMedal medal = moralMedalDao.load(moralMedalId);
            if (medal == null) {
                return MapMessage.errorMessage();
            }

            // 如果已经被点赞了, 告诉前端已经成功
            if (medal.getLiked()) {
                return MapMessage.successMessage();
            }

            // 检查家长学生的关系
            List<StudentParentRef> parentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
            boolean noRef = parentRefs.stream().noneMatch(i -> Objects.equals(i.getStudentId(), medal.getStudentId()));
            if (noRef) {
                return MapMessage.errorMessage("不被允许的操作");
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(medal.getStudentId());
            if (studentDetail.getClazzId() == null) {
                return MapMessage.errorMessage("学生未加入班级");
            }

            AtomicCallback<MapMessage> atomicCallback = () -> {
                MoralMedal moralMedal = moralMedalDao.load(moralMedalId);
                if (moralMedal.getLiked()) {
                    return MapMessage.successMessage();
                }

                // 发鲜花有个骚操作, 不能都存主老师ID ,会造成显示错误, 而是各学科用各学科的子老师 ID
                Group group = groupLoaderClient.getGroupLoader().loadGroup(moralMedal.getGroupId()).getUninterruptibly();
                Long sendTeacherId = getSubjectTeacherId(moralMedal.getTeacherId(), group.getSubject());

                moralMedal.setLiked(true);
                moralMedalDao.upsert(moralMedal);

                MapMessage sendFlowerMsg = flowerConditionService.sendFlower(moralMedal.getStudentId(), parentId,
                        sendTeacherId, studentDetail.getClazzId(), moralMedal.getGroupId(),
                        FlowerSourceType.MORAL_MEDAL, "").getUninterruptibly();

                if (!sendFlowerMsg.isSuccess()) {
                    log.warn("德育点赞调用送鲜花接口失败：" + sendFlowerMsg.getInfo());
                }

                return MapMessage.successMessage();
            };

            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("MoralMedalService:parentLike")
                    .keys(moralMedalId)
                    .callback(atomicCallback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("处理中,请稍后重试...");
        } catch (Exception ex) {
            return MapMessage.errorMessage();
        }
    }


    @Override
    public MapMessage sendMedal(Long teacherId, Long groupId, List<Long> studentIds, List<Integer> medalIds) {
        try {
            for (Integer medalId : medalIds) {
                MoralMedalEnum moralMedalEnum = MoralMedalEnum.valueOfById(medalId);
                if (moralMedalEnum == null) {
                    return MapMessage.errorMessage("勋章类型有误");
                }
            }

            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (teacher == null) {
                return MapMessage.errorMessage("教师不存在");
            }

            // 老师所有的组(包班制)
            List<GroupMapper> teacherGroups = getTeacherGroups(teacherId);
            GroupMapper subjectGroup = teacherGroups.stream().filter(i -> Objects.equals(i.getId(), groupId)).findFirst().orElse(null);
            if (subjectGroup == null) {
                return MapMessage.errorMessage("班级和老师没有关联关系");
            }

            Set<Long> groupIds = teacherGroups.stream().map(GroupMapper::getId).collect(toSet());
            if (!groupIds.contains(groupId)) {
                return MapMessage.errorMessage("班级和老师没有关联关系");
            }

            // 从老师所有的学生中查找出符合要求的
            HashSet<Long> hashSet = new HashSet<>(studentIds);
            Set<User> studentIdSet = studentLoaderClient.loadGroupStudents(groupIds).values().stream().flatMap(List::stream)
                    .filter(i -> hashSet.contains(i.getId()))
                    .collect(toSet());

            AtomicCallback<MapMessage> callback = () -> {
                for (User studentDetail : studentIdSet) {
                    for (Integer medalId : medalIds) {
                        MoralMedal medal = new MoralMedal();
                        medal.setTeacherId(teacherId);
                        medal.setStudentId(studentDetail.getId());
                        medal.setGroupId(groupId);
                        medal.setMedalId(medalId);
                        moralMedalDao.insert(medal);

                        notice(medal, studentDetail.getProfile().getRealname(), teacher.getProfile().getRealname());
                    }
                }
                return MapMessage.successMessage();
            };

            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("MoralMedalService:sendMedal")
                    .keys(teacherId)
                    .callback(callback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("处理中,请稍后重试...");
        } catch (Exception ex) {
            return MapMessage.errorMessage();
        }
    }

    private Long getSubjectTeacherId(Long teacherId, Subject subject) {
        if (subject == null) {
            return teacherId;
        }
        Long id = teacherLoaderClient.loadRelTeacherIdBySubject(teacherId, subject);
        return id == null ? teacherId : id;
    }

    @Override
    public MapMessage loadClazzMoral(Long id, Long groupId, String date) {
        DateRange dateRange = DateRange.newInstance(DateUtils.stringToDate(date, FORMAT_SQL_DATE).getTime(), DateRangeUnit.DAY);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        List<Date> dateList = calculateSmemsterDate();
        if (startDate.getTime() < dateList.get(0).getTime() || startDate.getTime() > dateList.get(1).getTime()) {
            return MapMessage.errorMessage("时间不在本学期内");
        }


        //计算是否有上一天和下一天的数据按钮
        Date nextDay = DateUtils.nextDay(startDate, 1);
        Date previousDay = DateUtils.nextDay(startDate, -1);
        boolean previous = previousDay.getTime() >= MoralMedalConstant.ON_LINE_DATE.getTime()
                && previousDay.getTime() >= dateList.get(0).getTime();
        boolean next = nextDay.getTime() < dateList.get(1).getTime()
                && nextDay.getTime() < System.currentTimeMillis();

        Teacher teacher = teacherLoaderClient.loadTeacher(id);
        if (teacher == null) {
            return MapMessage.errorMessage("教师不存在");
        }

        List<MoralMedal> moralMedals = moralMedalDao.loadByTeacherIdGroupId(id, groupId);

        if (CollectionUtils.isEmpty(moralMedals)) {
            return generateClazzMoralPerformance(teacher, new ArrayList<>(), previous, next);
        }

        Collator compare = Collator.getInstance(java.util.Locale.CHINA);


        Map<String, List<MoralMedal>> studentMedalMap = moralMedals.stream()
                .filter(moralMedal -> (startDate.before(moralMedal.getCreateDatetime()) || startDate.equals(moralMedal.getCreateDatetime()))
                        && (endDate.after(moralMedal.getCreateDatetime()) || endDate.equals(moralMedal.getCreateDatetime())))
                .sorted(new Comparator<MoralMedal>() {
                    @Override
                    public int compare(MoralMedal o1, MoralMedal o2) {
                        return o2.getCreateDatetime().compareTo(o1.getCreateDatetime());
                    }
                }).collect(Collectors.groupingBy(m -> m.getStudentId() + "-" + m.getMedalId(), Collectors.toList()));

        if (studentMedalMap == null || CollectionUtils.isEmpty(studentMedalMap.keySet())) {
            return generateClazzMoralPerformance(teacher, new ArrayList<>(), previous, next);
        }

        List<ClazzMoralPerformance.StudentMedal> sortStudentMedal = new ArrayList<>();

        for (String studentMedal : studentMedalMap.keySet()) {
            List<MoralMedal> list = studentMedalMap.get(studentMedal);
            ClazzMoralPerformance.StudentMedal stuMedal = new ClazzMoralPerformance.StudentMedal();
            stuMedal.setDate(list.get(0).getCreateDatetime());
            stuMedal.setCount(list.size());
            stuMedal.setMedalId(SafeConverter.toInt(studentMedal.split("-")[1]));
            stuMedal.setSid(SafeConverter.toLong(studentMedal.split("-")[0]));
            sortStudentMedal.add(stuMedal);
        }

        sortStudentMedal = sortStudentMedal.stream().sorted(new Comparator<ClazzMoralPerformance.StudentMedal>() {

            @Override
            public int compare(ClazzMoralPerformance.StudentMedal o1, ClazzMoralPerformance.StudentMedal o2) {
                if (o1.getDate().getTime() == o2.getDate().getTime()) {

                    Set<Long> set = new HashSet();
                    set.add(o1.getSid());
                    set.add(o2.getSid());

                    Map<Long, Student> longStudentMap = studentLoaderClient.loadStudents(set);
                    return compare.compare(longStudentMap.get(o1.getSid()).getProfile().getRealname(),
                            longStudentMap.get(o2.getSid()).getProfile().getRealname());
                }

                return o2.getDate().compareTo(o1.getDate());
            }
        }).collect(Collectors.toList());


        return generateClazzMoralPerformance(teacher, sortStudentMedal, previous, next);
    }


    @Override
    public MapMessage hotMedal(Long sid, String date) {

        Student student = studentLoaderClient.loadStudent(sid);
        if (student == null) {
            return MapMessage.errorMessage("不存在此学生");
        }

        //计算本周最热勋章的时间范围,参数错误返回当前周
        List<Date> list = calculateDate(date);
        Date startDate = list.get(0);
        Date endDate = list.get(1);
        Date firstDayOfWeek = DateUtils.getFirstDayOfWeek(new Date());

        List<Date> dateList = calculateSmemsterDate();

        List<MoralMedal> moralMedals = moralMedalDao.loadByStudentId(sid);

        boolean previous = startDate.getTime() > MoralMedalConstant.ON_LINE_DATE.getTime()
                && startDate.getTime() > dateList.get(0).getTime();
        boolean next = endDate.getTime() < dateList.get(1).getTime() && endDate.getTime() < firstDayOfWeek.getTime();

        //本学期累积发布勋章个数
        long count = calculateThisSemesterMedalCount(moralMedals);

        //本周前三最热勋章
        List<HistoryMedal.Medal> medals = calculateHotMedal(startDate, endDate, moralMedals);
        String shareText = "";
        if (CollectionUtils.isNotEmpty(medals)) {
            List<Integer> medalIdList = medals.stream().map(HistoryMedal.Medal::getMedalId).collect(toList());
            shareText = MoralMedalShareUtils.getShareText(medalIdList);
        }

        return MapMessage.successMessage().add("medalNum", count)
                .add("previous", previous)
                .add("next", next)
                .add("medals", medals)
                .add("shareText", shareText)
                .add("startDate", DateUtils.dateToString(startDate, FORMAT_SQL_DATE))
                .add("endDate", DateUtils.dateToString(endDate, FORMAT_SQL_DATE));
    }

    @Override
    public MapMessage historyMedal(Long sid, Integer page, Integer pagesize) {

        Student student = studentLoaderClient.loadStudent(sid);
        if (student == null) {
            return MapMessage.errorMessage("不存在此学生");
        }

        List<MoralMedal> moralMedals = moralMedalDao.loadByStudentId(sid);

        List<HistoryMedal> historyMedals = calculateThisSemesterMedalHistory(moralMedals, page, pagesize);


        return MapMessage.successMessage().add("historyMedals", historyMedals)
                .add("sname", student.getProfile().getRealname());
    }

    @Override
    public MapMessage deleteAll() {
        return MapMessage.errorMessage();
    }

    private static List<Date> calculateDate(String date) {

        List<Date> result = new ArrayList<>();

        Date toDate = DateUtils.stringToDate(date, FORMAT_SQL_DATE);

        if (StringUtils.isEmpty(date) || toDate == null) {
            Date currentDate = new Date();
            result.add(DateUtils.getFirstDayOfWeek(currentDate));
            result.add(DateUtils.getLastDayOfWeek(currentDate));
            return result;
        }

        Date firstDayOfWeek = DateUtils.getFirstDayOfWeek(toDate);
        Date lastDayOfWeek = DateUtils.getLastDayOfWeek(toDate);
        String firstDay = DateUtils.dateToString(firstDayOfWeek, FORMAT_SQL_DATE);
        String lastDay = DateUtils.dateToString(lastDayOfWeek, FORMAT_SQL_DATE);

        if (date.equals(firstDay)) {
            Date date1 = DateUtils.nextDay(firstDayOfWeek, -1);
            result.add(DateUtils.getFirstDayOfWeek(date1));
            result.add(DateUtils.getLastDayOfWeek(date1));
            return result;
        }

        if (date.equals(lastDay)) {
            Date date1 = DateUtils.nextDay(lastDayOfWeek, 1);
            result.add(DateUtils.getFirstDayOfWeek(date1));
            result.add(DateUtils.getLastDayOfWeek(date1));
            return result;
        }

        Date currentDate = new Date();
        result.add(DateUtils.getFirstDayOfWeek(currentDate));
        result.add(DateUtils.getLastDayOfWeek(currentDate));
        return result;
    }

    public static void main(String[] args) {

        List<Date> list = calculateSmemsterDate();
        System.out.println(list.get(0));
        System.out.println(list.get(1));

    }

    /**
     * 算出本学期的起始日期和结束日期
     *
     * @return
     */
    private static List<Date> calculateSmemsterDate() {

        //获取当年上学期的开始时间和结束时间
        Date date = new Date(System.currentTimeMillis());
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Date upStart = DateUtils.stringToDate(year + MoralMedalConstant.UP_SEMESTER_START);
        Date upEnd = DateUtils.stringToDate(year + MoralMedalConstant.UP_SEMESTER_END);

        List<Date> list = new ArrayList<>();

        if (date.getTime() > upEnd.getTime()) {
            upStart = DateUtils.stringToDate(year + MoralMedalConstant.DOWN_SEMESTER_START);
            upEnd = DateUtils.stringToDate(year + 1 + MoralMedalConstant.DOWN_SEMESTER_END);
        } else if (date.getTime() < upStart.getTime()) {
            upStart = DateUtils.stringToDate(year - 1 + MoralMedalConstant.DOWN_SEMESTER_START);
            upEnd = DateUtils.stringToDate(year + MoralMedalConstant.DOWN_SEMESTER_END);
        }
        list.add(upStart);
        list.add(upEnd);
        return list;
    }

    private List<HistoryMedal> calculateThisSemesterMedalHistory(List<MoralMedal> moralMedals,
                                                                 Integer page,
                                                                 Integer pagesize) {

        if (CollectionUtils.isEmpty(moralMedals)) {
            return null;
        }

        List<Date> dateList = calculateSmemsterDate();
        Date startDate = dateList.get(0);
        Date endDate = dateList.get(1);

        //查询本学期的勋章评价，并按照创建时间降序排序，依照创建时间分组yyyy-MM-dd分组
        Map<String, List<MoralMedal>> historyMap = moralMedals.stream()
                .filter(m -> (m.getCreateDatetime().after(startDate) || m.getCreateDatetime().equals(startDate))
                        && (m.getCreateDatetime().before(endDate) || m.getCreateDatetime().equals(endDate)))
                .sorted(new Comparator<MoralMedal>() {
                    @Override
                    public int compare(MoralMedal o1, MoralMedal o2) {
                        return o2.getCreateDatetime().compareTo(o1.getCreateDatetime());
                    }
                })
                .collect(Collectors.groupingBy(m -> DateUtils.dateToString(m.getCreateDatetime(), FORMAT_SQL_DATE), Collectors.toList()));

        if (historyMap == null || CollectionUtils.isEmpty(historyMap.keySet())) {
            return null;
        }

        List<HistoryMedal> resultList = new ArrayList<>();

        for (String date : historyMap.keySet()) {
            List<HistoryMedal.Medal> medalList = generateMedalList(historyMap.get(date));
            HistoryMedal historyMedal = new HistoryMedal();
            historyMedal.setDate(date);
            historyMedal.setList(medalList);
            resultList.add(historyMedal);
        }

        List<HistoryMedal> collect = resultList.stream().sorted(new Comparator<HistoryMedal>() {
            @Override
            public int compare(HistoryMedal o1,
                               HistoryMedal o2) {
                Date o1date = DateUtils.stringToDate(o1.getDate(), FORMAT_SQL_DATE);
                Date o2date = DateUtils.stringToDate(o2.getDate(), FORMAT_SQL_DATE);
                return o2date.compareTo(o1date);
            }
        }).collect(Collectors.toList());

        return listPaging(collect, page, pagesize);
    }

    /**
     * 数据集合的分页方法，根据传入总共的数据跟页码，返回页码所需要显示多少条的数据
     *
     * @param f        带有需要进行分页的数据集合
     * @param pageNo   第几页
     * @param dataSize 显示多少条数据
     * @return 进过分页之后返回的数据
     */
    public static <F> List<F> listPaging(List<F> f, int pageNo, int dataSize) {

        if (f == null) {
            f = new ArrayList<F>();
        }
        if ((Object) pageNo == null) {
            pageNo = 1;
        }
        if ((Object) dataSize == null) {
            dataSize = 1;
        }
        if (pageNo <= 0) {
            pageNo = 1;
        }

        int totalitems = f.size();
        List<F> afterList = new ArrayList<F>();

        for (int i = (pageNo - 1) * dataSize;
             i < (((pageNo - 1) * dataSize) + dataSize > totalitems ? totalitems : ((pageNo - 1) * dataSize) + dataSize);
             i++) {

            afterList.add(f.get(i));
        }

        return afterList;
    }

    private List<HistoryMedal.Medal> generateMedalList(List<MoralMedal> moralMedals) {

        List<HistoryMedal.Medal> medalList = new ArrayList<>();

        for (MoralMedal moralMedal : moralMedals) {
            HistoryMedal.Medal medal = new HistoryMedal.Medal();
            Long teacherId = moralMedal.getTeacherId();
            Integer medalId = moralMedal.getMedalId();

            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            String desc = teacher.getProfile().getRealname();

            medal.setDesc(desc);
            medal.setIcon(MoralMedalEnum.getIconById(medalId));
            medal.setName(MoralMedalEnum.valueOfById(medalId).name());
            medalList.add(medal);
        }

        return medalList;
    }

    private long calculateThisSemesterMedalCount(List<MoralMedal> moralMedals) {

        if (CollectionUtils.isEmpty(moralMedals)) {
            return 0l;
        }

        List<Date> dateList = calculateSmemsterDate();
        long count = moralMedals.stream()
                .filter(m -> (m.getCreateDatetime().getTime() >= dateList.get(0).getTime())
                        && (m.getCreateDatetime().getTime() <= dateList.get(1).getTime())).count();

        return count;
    }

    private List<HistoryMedal.Medal> calculateHotMedal(Date start, Date end, List<MoralMedal> moralMedals) {

        if (CollectionUtils.isEmpty(moralMedals)) {
            return null;
        }

        Map<Integer, Long> hotWeekMedalMap = moralMedals.stream()
                .filter(m -> (m.getCreateDatetime().after(start) || m.getCreateDatetime().equals(start))
                        && (m.getCreateDatetime().before(end) || m.getCreateDatetime().equals(end)))
                .collect(Collectors.groupingBy(m -> m.getMedalId(), Collectors.counting()));

        if (hotWeekMedalMap == null || CollectionUtils.isEmpty(hotWeekMedalMap.keySet())) {
            return null;
        }

        List<HistoryMedal.Medal> medalList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            int medalId = 0, max = 0;
            for (Integer medal : hotWeekMedalMap.keySet()) {
                int count = SafeConverter.toInt(hotWeekMedalMap.get(medal));
                if (count > max) {
                    medalId = medal;
                    max = count;
                }
            }

            if (medalId != 0 && max != 0) {
                HistoryMedal.Medal medal = new HistoryMedal.Medal();
                medal.setMedalId(medalId);
                medal.setName(MoralMedalEnum.valueOfById(medalId).name());
                medal.setNum(SafeConverter.toString(max));
                medal.setIcon(MoralMedalEnum.getIconById(medalId));
                hotWeekMedalMap.remove(medalId);
                medalList.add(medal);
            }
        }

        return medalList;
    }

    private MapMessage generateClazzMoralPerformance(Teacher teacher, List<ClazzMoralPerformance.StudentMedal> list, boolean previous, boolean next) {

        List<Integer> moralMedalList = list.stream()
                .map(ClazzMoralPerformance.StudentMedal::getMedalId)
                .collect(toList());

        ClazzMoralPerformance clazzMoralPerformance = new ClazzMoralPerformance();
        clazzMoralPerformance.setPrevious(previous);
        clazzMoralPerformance.setNext(next);
        clazzMoralPerformance.setTid(teacher.getId());
        clazzMoralPerformance.setTName(teacher.getProfile().getRealname());
        clazzMoralPerformance.setMoralDesc(com.voxlearning.utopia.service.campaign.impl.support.MoralMedalShareUtils.getShareText(moralMedalList));

        Set<Long> sidList = list.stream().map(m -> m.getSid()).collect(toSet());
        Map<Long, Student> studentMap = studentLoaderClient.loadStudents(sidList);

        List<ClazzMoralPerformance.StudentMoral> stuMoralList = new ArrayList<>();
        for (ClazzMoralPerformance.StudentMedal studentMedal : list) {

            ClazzMoralPerformance.StudentMoral stuMoral = new ClazzMoralPerformance.StudentMoral();
            stuMoral.setSid(studentMedal.getSid());
            stuMoral.setSname(studentMap.get(studentMedal.getSid()).getProfile().getRealname());
            stuMoral.setSphoto(studentMap.get(studentMedal.getSid()).getProfile().getImgUrl());
            stuMoral.setTagId(studentMedal.getMedalId());
            stuMoral.setTagIcon(MoralMedalEnum.getIconById(studentMedal.getMedalId()));
            stuMoral.setTagName(MoralMedalEnum.valueOfById(studentMedal.getMedalId()).name());
            stuMoral.setCount(studentMedal.getCount());
            stuMoralList.add(stuMoral);
        }
        clazzMoralPerformance.setList(stuMoralList);

        return MapMessage.successMessage().add("data", clazzMoralPerformance);
    }

    private void notice(MoralMedal moralMedal, String sname, String tname) {
        try {
            MoralMedalEnum moralMedalEnum = MoralMedalEnum.valueOfById(moralMedal.getMedalId());
            assert moralMedalEnum != null;

            String parentMsg = String.format("%s老师刚刚表扬了您的孩子%s'%s'" + ",快去看看吧～", tname, sname, moralMedalEnum.name());
            String parentLinkUrl = String.format(MoralMedalConstant.PARENT_ACTIVITY_INDEX, moralMedal.getStudentId(), moralMedal.getId());
            String stuMsg = String.format("%s老师刚刚表扬了你'%s'" + ",快去看看吧～", tname, moralMedalEnum.name());


            Map<String, Object> noticeMessage = new LinkedHashMap<>();
            noticeMessage.put("teacherId", moralMedal.getTeacherId());
            noticeMessage.put("studentId", moralMedal.getStudentId());
            noticeMessage.put("linkUrl", parentLinkUrl);
            noticeMessage.put("content", parentMsg);

            String text = JsonUtils.toJson(noticeMessage);
            parentPublisher.publish(Message.newMessage().withPlainTextBody(text));

            sendUtils.sendAppMessage(moralMedal.getStudentId(), MORAL_MEDAL_ACTIVITY_NAME,
                    stuMsg, parentLinkUrl,
                    StudentAppPushType.ACTIVITY_REMIND.getType(), false);
            sendUtils.sendStudentPush(moralMedal.getStudentId(), stuMsg,
                    StudentAppPushType.ACTIVITY_REMIND.getType(), parentLinkUrl);


            List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(moralMedal.getStudentId());
            if (CollectionUtils.isEmpty(studentParents)) {
                return;
            }
            for (StudentParent studentParent : studentParents) {
                sendUtils.sendAppMessage(studentParent.getParentUser().getId(), MORAL_MEDAL_ACTIVITY_NAME,
                        parentMsg, parentLinkUrl, ParentMessageType.REMINDER.getType(), true);
                sendUtils.sendParentPush(studentParent.getParentUser().getId(), parentMsg,
                        ParentAppPushType.ACTIVITY.name(), parentLinkUrl, ParentMessageTag.通知.name());
            }

        } catch (Exception e) {
            log.error("发勋章后通知家长端异常", e);
        }
    }

    @NotNull
    private List<GroupMapper> getTeacherGroups(Long teacherId) {
        Set<Long> allTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);

        List<GroupMapper> groupList = deprecatedGroupLoaderClient.loadTeacherGroupsByTeacherId(allTeacherIds, false)
                .values().stream().flatMap(List::stream)
                .filter(i -> !i.getIsVirtual())
                .distinct().collect(toList());

        // 要考虑去掉毕业班的班组
        Set<Long> clazzIds = groupList.stream().map(GroupMapper::getClazzId).collect(toSet());
        Set<Long> clazzIdSet = deprecatedClazzLoaderClient.getRemoteReference().loadClazzs(clazzIds)
                .values().stream()
                .filter(Clazz::isPublicClazz)
                .filter(i -> !i.isTerminalClazz())
                .map(LongIdEntity::getId)
                .collect(toSet());

        return groupList.stream().filter(i -> clazzIdSet.contains(i.getClazzId())).collect(Collectors.toList());
    }

    private Integer findNumber(String string) {
        String[] ss = string.replaceAll("[^0-9]", ",").split(",");
        int maxSize = 0;
        String response = "";
        for (int i = 0; i < ss.length; i++) {
            if (ss[i].length() > maxSize) {
                maxSize = ss[i].length();
                response = ss[i];
            }
        }
        return SafeConverter.toInt(response);
    }
}
