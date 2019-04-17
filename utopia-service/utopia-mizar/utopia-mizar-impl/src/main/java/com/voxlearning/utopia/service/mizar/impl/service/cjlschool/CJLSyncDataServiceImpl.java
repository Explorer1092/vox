package com.voxlearning.utopia.service.mizar.impl.service.cjlschool;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLConstants;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLClass;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLStudent;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacherCourse;
import com.voxlearning.utopia.service.mizar.api.service.cjlschool.CJLSyncDataService;
import com.voxlearning.utopia.service.mizar.impl.dao.cjlschool.CJLClassDao;
import com.voxlearning.utopia.service.mizar.impl.dao.cjlschool.CJLStudentDao;
import com.voxlearning.utopia.service.mizar.impl.dao.cjlschool.CJLTeacherCourseDao;
import com.voxlearning.utopia.service.mizar.impl.dao.cjlschool.CJLTeacherDao;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
@Named
@Service(interfaceClass = CJLSyncDataService.class)
@ExposeService(interfaceClass = CJLSyncDataService.class)
public class CJLSyncDataServiceImpl extends SpringContainerSupport implements CJLSyncDataService {

    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherServiceClient teacherServiceClient;
    @Inject private ThirdPartyLoaderClient thirdPartyLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;

    @Inject private CJLClassDao cjlClassDao;
    @Inject private CJLTeacherDao cjlTeacherDao;
    @Inject private CJLTeacherCourseDao cjlTeacherCourseDao;
    @Inject private CJLStudentDao cjlStudentDao;

    @ImportService(interfaceClass = ThirdPartyService.class)
    private ThirdPartyService thirdPartyService;

    //============================================================
    //================         Class           ===================
    //============================================================

    @Override
    public void syncClass(List<CJLClass> classList) {
        if (CollectionUtils.isEmpty(classList)) {
            return;
        }
        // 先同步信息
        cjlClassDao.syncBatch(classList);
    }

    @Override
    public void modifyClass(CJLClass sourceClass) {
        if (sourceClass == null) {
            return;
        }
        try {
            cjlClassDao.syncOne(sourceClass);
        } catch (Exception ex) {
            logger.error("Failed sync CJL School Class Data", ex);
        }
    }

    //============================================================
    //================        Teacher          ===================
    //============================================================

    @Override
    public void syncSchoolTeacher(String sourceSchoolId, List<CJLTeacher> sourceTeacherList) {
        if (StringUtils.isBlank(sourceSchoolId) || CollectionUtils.isEmpty(sourceTeacherList)) {
            return;
        }

        // 先同步信息
        cjlTeacherDao.syncBatch(sourceTeacherList);

        // 非数学老师稍后再议
        sourceTeacherList = sourceTeacherList.stream().filter(CJLTeacher::isMathTeacher).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sourceTeacherList)) {
            return;
        }

        Long schoolId = getSchoolIdMapping().get(sourceSchoolId);
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return;
        }

        // 学校的全部老师, 按照姓名做一个分组
        Map<String, List<Teacher>> teacherNameMap = teacherLoaderClient.loadSchoolTeachers(schoolId)
                .stream()
                .filter(t -> StringUtils.isNotBlank(t.fetchRealname()))
                .collect(Collectors.groupingBy(Teacher::fetchRealname));

        // 导入名称的老师也根据姓名处理
        Map<String, CJLTeacher> sourceTeacherMap = sourceTeacherList.stream().collect(Collectors.toMap(
                CJLTeacher::getName, Function.identity(), (u, v) -> {
                    logger.error("Duplicate CJLTeacher Name found, name={}, id=({} , {})", u.getName(), u.getId(), v.getId());
                    return null; // ignore it
                }, LinkedHashMap::new));

        // 开始处理导入数据
        for (Map.Entry<String, CJLTeacher> entry : sourceTeacherMap.entrySet()) {
            String teacherName = entry.getKey();
            CJLTeacher sourceTeacher = entry.getValue();
            if (sourceTeacher == null) {
                continue; // 下一个
            }
            // 先检查一下这个老师是不是已经做过关联
            LandingSource source = thirdPartyLoaderClient.loadLandingSource(SsoConnections.CjlSchool.getSource(), sourceTeacher.getId());
            if (source != null && source.getUserId() != null) {
                // 如果已经做过关联, 只用检查手机号需不需要同步
                String mobile = sourceTeacher.getMobile();
                if (MobileRule.isMobile(mobile) && userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                    userServiceClient.activateUserMobile(source.getUserId(), mobile, false);
                }
                continue;
            }

            // 如果没有做过关联的话, 就去看看有没有同名老师可以绑定
            List<Teacher> teachers = teacherNameMap.get(teacherName);
            Teacher matchTeacher = null;
            if (CollectionUtils.isNotEmpty(teachers)) {
                // FIXME 目前只支持数学学科同步
                matchTeacher = teachers.stream()
                        .filter(t -> t.getSubject() != null && Subject.MATH == t.getSubject())
                        .findFirst().orElse(null);
            }

            if (matchTeacher == null) {
                // 找不到同名的，那么就直接新建一个老师
                internalSyncTeacher(sourceTeacher, schoolId);
                continue;
            }

            // 直接创建关联
            thirdPartyService.persistLandingSource(SsoConnections.CjlSchool.getSource(), sourceTeacher.getId(), sourceTeacher.getLoginName(), matchTeacher.getId());
            // 绑定手机号
            String mobile = sourceTeacher.getMobile();
            if (MobileRule.isMobile(mobile) && userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                userServiceClient.activateUserMobile(matchTeacher.getId(), mobile, false);
            }
        }
    }

    @Override
    public void modifyTeacher(CJLTeacher sourceTeacher) {
        if (sourceTeacher == null) {
            return;
        }
        try {
            // 先同步信息
            cjlTeacherDao.syncOne(sourceTeacher);

            // 是不是数学老师
            if (!sourceTeacher.isMathTeacher()) {
                return;
            }
            Long schoolId = getSchoolIdMapping().get(sourceTeacher.getSchoolId());
            if (schoolId == null) {
                return;
            }

            Teacher matchTeacher = findMatchTeacher(sourceTeacher.getId());
            // 这是一个新老师？
            if (matchTeacher == null) {
                internalSyncTeacher(sourceTeacher, schoolId);
                return;
            }

            // 姓名变更
            if (StringUtils.isNotBlank(sourceTeacher.getName()) && !StringUtils.equals(sourceTeacher.getName(), matchTeacher.fetchRealname())) {
                // Feature #54929
                //if (!ForbidModifyNameAndPortrait.check()) {
                    userServiceClient.changeName(matchTeacher.getId(), sourceTeacher.getName());
                //}
            }

            // 手机变更
            if (StringUtils.isNotBlank(sourceTeacher.getMobile()) && MobileRule.isMobile(sourceTeacher.getMobile())) {
                if (userLoaderClient.loadMobileAuthentication(sourceTeacher.getMobile(), UserType.TEACHER) == null) {
                    userServiceClient.cleanupBindedMobile("CJL Sync System", sourceTeacher.getMobile(), UserType.TEACHER);
                    userServiceClient.activateUserMobile(matchTeacher.getId(), sourceTeacher.getMobile(), false);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed modify CJL School Teacher Data, tid={} ", sourceTeacher.getId(), ex);
        }
    }

    @Override
    public CJLTeacher findUserByLoginName(String loginName) {
        return cjlTeacherDao.findByLoginName(loginName);
    }

    //============================================================
    //=============        Teacher Course          ===============
    //============================================================
    @Override
    public void syncTeacherCourse(CJLTeacherCourse course) {
        $syncTeacherCourse(course);
    }

    @Override
    public void modifyTeacherCourse(CJLTeacherCourse course) {
        $syncTeacherCourse(course);
    }

    //============================================================
    //================        Student          ===================
    //============================================================
    @Override
    public void syncStudents(List<CJLStudent> students) {
        if (CollectionUtils.isEmpty(students)) {
            return;
        }
        try {
            // 先同步这个内容
            cjlStudentDao.syncBatch(students);
        } catch (Exception ex) {
            logger.error("Failed sync CJL School Student Data", ex);
        }
    }

    @Override
    public void modifyStudent(CJLStudent sourceStudent) {
        if (sourceStudent == null) {
            return;
        }
        try {
            // 先同步信息
            cjlStudentDao.upsert(sourceStudent);

            // 去看看学校有没有做映射
            Long schoolId = getSchoolIdMapping().get(sourceStudent.getSchoolId());
            // TODO
            if (schoolId == null || StringUtils.isBlank(sourceStudent.getKlxStudentId())) {
                // 啥都没有，就没有这个必要了
                return;
            }
            newKuailexueServiceClient.modifyKlxStudent(
                    schoolId, sourceStudent.getKlxStudentId(), sourceStudent.getName(), sourceStudent.getStudentNumber()
            );
        } catch (Exception ex) {

        }
    }

    @Override
    public List<CJLTeacher> findAllTeacher() {
        return cjlTeacherDao.$findAllForJob();
    }

    @Override
    public List<CJLClass> findAllClass() {
        return cjlClassDao.$findAllForJob();
    }

    @Override
    public List<CJLStudent> findAllStudent() {
        return cjlStudentDao.$findAllForJob();
    }

    @Override
    public List<CJLTeacherCourse> findAllTeacherCourseForJob() {
        return cjlTeacherCourseDao.$findAllForJob();
    }


    //============================================================
    //================         Others          ===================
    //============================================================

    /**
     * 通过 CommonConfig 获取学校的映射关系
     * 测试 : 陈经纶中学(高中部)(414008)
     * 线上 : 陈经纶中学(高中部)(405492)
     */
    private Map<String, Long> getSchoolIdMapping() {
        String schoolMapConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.MIDDLE_PLATFORM_GENERAL.getType(), "CJL_SCHOOL_MAP"
        );

        Map<String, Long> schoolIdMap = new HashMap<>();
        Stream.of(schoolMapConfig.split(",")).forEach(pair -> {
            String[] split = pair.split(":");
            schoolIdMap.put(split[0], SafeConverter.toLong(split[1]));
        });
        return schoolIdMap;
    }

    /**
     * 根据陈经纶用户ID找到对应的User
     */
    private Teacher findMatchTeacher(String uid) {
        LandingSource landingSource = thirdPartyLoaderClient.loadLandingSource(SsoConnections.CjlSchool.getSource(), uid);
        if (landingSource == null) {
            return null;
        }
        return teacherLoaderClient.loadTeacher(landingSource.getUserId());
    }

    private MapMessage internalSyncTeacher(CJLTeacher sourceTeacher, Long schoolId) {
        if (!sourceTeacher.isMathTeacher()) {
            return MapMessage.errorMessage("暂时只支持数学老师");
        }
        // 同步LandingSource
        LandingSource landingSource = thirdPartyLoaderClient.loadLandingSource(SsoConnections.CjlSchool.getSource(), sourceTeacher.getId());

        // 理论上就不用再做什么了
        if (landingSource != null && landingSource.getUserId() != null && userLoaderClient.loadUser(landingSource.getUserId()) != null) {
            return MapMessage.successMessage();
        }

        // 创建一个Teacher关联上吧
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
        neonatalUser.setUserType(UserType.TEACHER);
        neonatalUser.setMobile(sourceTeacher.getMobile());
        neonatalUser.setPassword(CJLConstants.SYNC_TEACHER_DEFAULT_PASSWORD);
        neonatalUser.setRealname(sourceTeacher.getName());
        neonatalUser.setWebSource(CJLConstants.SYNC_TEACHER_WEB_SOURCE);

        MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
        if (!message.isSuccess()) {
            return message;
        }
        User newTeacher = (User) message.get("user");

        // 绑定LandingSource
        thirdPartyService.persistLandingSource(SsoConnections.CjlSchool.getSource(), sourceTeacher.getId(), sourceTeacher.getLoginName(), newTeacher.getId());

        // 绑定手机号
        String mobile = sourceTeacher.getMobile();
        if (MobileRule.isMobile(mobile) && userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
            userServiceClient.activateUserMobile(newTeacher.getId(), mobile, false);
        }

        // 更新学科，FIXME  目前只有高中数学
        Subject subject = Subject.MATH;
        Ktwelve ktwelve = Ktwelve.SENIOR_SCHOOL;
        teacherServiceClient.setTeacherSubjectSchool(newTeacher, subject, ktwelve, schoolId);
        return MapMessage.successMessage();
    }

    private void $syncTeacherCourse(CJLTeacherCourse course) {
        if (course == null) {
            return;
        }

        // 找到关联的老师和班级, 如果没有同步该老师和班级的数据，直接忽略吧
        CJLTeacher teacherData = cjlTeacherDao.load(course.getTeacherId());
        if (teacherData == null) {
            logger.debug("Can not find teacher of this id , CJLTeacherId={}", course.getTeacherId());
            return;
        }

        CJLClass classData = cjlClassDao.load(course.getClassId());
        if (classData == null) {
            logger.debug("Match teacherId = {}, But Can not find class of this id , CJLClassId = {}", teacherData.getId(), course.getClassId());
            return;
        }

        cjlTeacherCourseDao.syncOne(course);
    }

}
