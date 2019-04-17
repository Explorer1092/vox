package com.voxlearning.washington.controller.specialteacher;

import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ClazzConstants;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.entity.TeacherRoles;
import com.voxlearning.utopia.mapper.TakeUpKlxStudent;
import com.voxlearning.utopia.service.clazz.client.GroupServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.ClazzService;
import com.voxlearning.utopia.service.user.api.constants.*;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.TeacherRolesServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.washington.mapper.specialteacher.base.ExcelExportData;
import com.voxlearning.washington.mapper.specialteacher.base.ExcelTemplate;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import com.voxlearning.washington.mapper.specialteacher.studentimport.StudentImportData;
import com.voxlearning.washington.support.SessionUtils;
import com.voxlearning.washington.support.WorkbookUtils;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 教务老师相关
 */
@Controller
@RequestMapping("/specialteacher")
public class SpecialTeacherController extends AbstractSpecialTeacherController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private GroupServiceClient clazzGroupServiceClient;
    @Inject private TeacherRolesServiceClient teacherRolesServiceClient;
    @Inject com.voxlearning.utopia.service.user.consumer.GroupServiceClient groupServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        if (currentSpecialTeacher() == null) {
            return "redirect: /";
        }
        return "redirect:/specialteacher/clazz/index.vpage";
    }

    //教务老师的权限信息/
    @RequestMapping(value = "eaxmGroupManager.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage eaxmGroupManager() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        Long userId = specialTeacher.getId();
        List<TeacherRoles> roles = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(userId);
        MapMessage result = MapMessage.successMessage();
        result.put("permission", false);
        if (!CollectionUtils.isEmpty(roles)) {
            roles.forEach(r -> {
                if (TeacherRolesType.EXAM_GROUP_MANAGER.toString().equals(r.getRoleType())) {
                    result.put("permission", "true");
                }
            });
        }
        return result;
    }

    //========================================= 班级管理页面 =========================================

    @ResponseBody
    @RequestMapping(value = "createExamGroup.vpage", method = {RequestMethod.POST})
    public MapMessage createExamGroup(Integer gradeId, String subjects) {
        if (null == gradeId || StringUtils.isEmpty(subjects)) {
            return MapMessage.errorMessage("请输入年级ID以及学科信息");
        }
        List<Subject> subjectList = JsonUtils.fromJsonToList(subjects, Subject.class);
        Set<Subject> schoolValidSubjects = getSchoolValidSubjects();
        for (Subject subject : subjectList) {
            if (!schoolValidSubjects.contains(subject)) {
                return MapMessage.errorMessage("学校没有相关学科的权限");
            }
        }

        //循环对每一个学科做插入。这里本来一个方法搞定，由于对需求理解错了，每次只能新增一个。导致改变较大。由于项目时间关系，先循环插入
        for (Subject subject : subjectList) {
            MapMessage mapMessage = this.createExamGroupOne(gradeId, subject);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }
        }
        return MapMessage.successMessage("批量生成学科组成功");
    }

    /**
     * 生成大考班
     * 1.多班群不能生成
     * 2.只能对行政班生成
     * 3.只能对有权限的学科生成
     * 4.如果学科权限被删除，group不动
     * 5.如果新增学科或者班级，需要重新生成
     * 6.已有相关组的班不能重复生成组
     * 7.共享组之后，所有的组的学生关系需要复制
     *
     * @param gradeId 年级ID
     * @param subject 学科（枚举英文）{@link Subject}
     * @return
     * @author zhouwei
     */
    @ResponseBody
    @RequestMapping(value = "createExamGroupOne.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public MapMessage createExamGroupOne(Integer gradeId, Subject subject) {
        try {
            if (null == gradeId || subject == null) {
                return MapMessage.errorMessage("请输入年级ID以及学科信息");
            }

            School school = currentSchool();
            if (null == school) {
                return MapMessage.errorMessage("教务老师所在学校不存在");
            }

            ResearchStaff specialTeacher = currentSpecialTeacher();
            Long userId = specialTeacher.getId();

            boolean examPermission = teacherRolesServiceClient.hasRole(userId, school.getId(), TeacherRoleCategory.O2O.name(), TeacherRolesType.EXAM_GROUP_MANAGER.name());
            if (!examPermission) {
                return MapMessage.errorMessage("教务老师没有设置批量生成学科组的权限");
            }

            Set<Subject> schoolValidSubjects = getSchoolValidSubjects();
            if (!schoolValidSubjects.contains(subject)) {
                return MapMessage.errorMessage("学校没有相关学科的权限");
            }

            List<Clazz> clazzs = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(school.getId()).enabled().toList().stream()
                    .filter(t -> ClazzType.PUBLIC == t.getClazzType())
                    .filter(t -> Objects.equals(gradeId, t.getClazzLevel().getLevel()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(clazzs)) {
                return MapMessage.errorMessage("学校没有可用班级");
            }

            List<Long> clazzIds = clazzs.stream().map(Clazz::getId).collect(Collectors.toList());
            Map<Long, List<Group>> groupMap = groupLoaderClient.getGroupLoader().loadGroupsByClazzIds(clazzIds).getUninterruptibly();//获取班级所有的组

            // 检查是否有多班群情况
            // 多班群情况1， 有多个同名班级
            Map<String, List<Clazz>> nameGroupClazz = clazzs.stream().collect(Collectors.groupingBy(Clazz::getClassName, Collectors.toList()));
            for (String className : nameGroupClazz.keySet()) {
                if (nameGroupClazz.get(className).size() > 1) {
                    return MapMessage.errorMessage("有多个" + className + ",请先处理多班群问题再使用该功能");
                }
            }

            // 多班群情况2, 班级下面的组没有共享
            for (List<Group> clazzGroups : groupMap.values()) {
                if (CollectionUtils.isNotEmpty(clazzGroups) && clazzGroups.size() >= 2) {
                    Set<String> groupParents = clazzGroups.stream().map(p -> SafeConverter.toString(p.getGroupParent())).collect(Collectors.toSet());
                    if (groupParents.size() > 1 || (groupParents.size() == 1 && StringUtils.isBlank(groupParents.iterator().next()))) {
                        return MapMessage.errorMessage("班级内有非共享组，请先处理多班群问题再使用该功能");
                    }
                }
            }

            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("createExamGroupOne")
                    .keys(school.getId())
                    .callback(() -> {
                        for (Clazz clazz : clazzs) { // 生成大考班组

                            String groupParent = "";//组的父名称，班下面第一个组默认为空
                            ArtScienceType artScienceType = ArtScienceType.ARTSCIENCE;//组的分科，班下面的第一个组为文理不分科

                            List<Group> groupList = groupMap.get(clazz.getId());
                            if (CollectionUtils.isNotEmpty(groupList)) {
                                // 如果有同学科的组存在，不用创建了
                                Group subjectGroup = groupList.stream()
                                        .filter(p -> p.getSubject() == subject)
                                        .findAny().orElse(null);
                                if (subjectGroup != null) {
                                    continue;
                                }

                                groupParent = groupList.get(0).getGroupParent();
                                artScienceType = groupList.get(0).getArtScienceType();
                            }

                            Group group = new Group();
                            group.setClazzId(clazz.getId());
                            //group.setGroupName(subjectObj.getKey());
                            group.setSubject(subject);
                            group.setGroupType(GroupType.CLAZZ_GROUP);
                            group.setFreeJoin(true);
                            group.setGroupParent(groupParent);
                            group.setArtScienceType(artScienceType);
                            group.setIsVirtual(false);
                            group.setAuthenticationState(1);

                            group = clazzGroupServiceClient.getGroupService().insertGroup(group).getUninterruptibly();

                            if (CollectionUtils.isNotEmpty(groupList)) {//如果之前班下已经有一个组了，则需要处理共享组问题
                                groupServiceClient.getGroupService().shareGroupsAndStudent(group.getId(), groupList.get(0).getId());
                            }
                        }
                        return MapMessage.successMessage("批量生成学科组成功");
                    })
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("内部错误，导致操作失败");
        }
    }

    @ResponseBody
    @RequestMapping(value = "manageclazz.vpage", method = RequestMethod.GET)
    public MapMessage manageClazz() {
        School school = currentSchool();
        if (school == null) {
            return MapMessage.errorMessage("教务老师所在学校不存在");
        }
        Long schoolId = school.getId();
        // 找到学校下所有班级
        List<Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId).enabled().toList();
        // 找到班级下所有分组
        Map<Long, List<GroupMapper>> groupMap = deprecatedGroupLoaderClient.loadClazzGroups(clazzList.stream().map(Clazz::getId).collect(Collectors.toList()));
        List<Long> groupIdList = groupMap.values().stream().flatMap(Collection::stream).map(GroupMapper::getId).collect(Collectors.toList());
        // 找到所有分组对应的老师
        Map<Long, Long> groupTeacherIdMap = new LinkedHashMap<>();
        raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .groupByGroupIds(groupIdList)
                .values()
                .stream()
                .filter(e -> !e.isEmpty())
                .map(e -> {
                    List<GroupTeacherTuple> list = e.stream()
                            .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                            .collect(Collectors.toList());
                    return list.iterator().next();
                })
                .forEach(e -> groupTeacherIdMap.put(e.getGroupId(), e.getTeacherId()));

        // 找到所有分组下的老师信息
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(groupTeacherIdMap.values());
        // 找到所有分组对应的学生
        Map<Long, List<KlxStudent>> klxGroupStudents = newKuailexueLoaderClient.loadKlxGroupStudents(groupIdList);

        // 改成年级跟学校的学制走
        EduSystemType eduSystemType = EduSystemType.of(schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly());

        if (eduSystemType == null) {
            return MapMessage.errorMessage("请先联系管理员设置学校学制");
        }

        List<String> levelGroup = Arrays.asList(eduSystemType.getCandidateClazzLevel().split(","));
        // 年级去重
//        List<String> levelGroup = clazzList.stream().map(Clazz::getClassLevel).distinct().sorted().collect(Collectors.toList());

        MapMessage result = MapMessage.successMessage();

        List<Map<String, Object>> menu = new ArrayList<>();
        List<Map<String, Object>> adjustClazzList = new ArrayList<>();
        for (String level : levelGroup) {
            if (ClazzLevel.PRIMARY_GRADUATED.getLevel() == ConversionUtils.toInt(level)) {
                continue; // 小学毕业的班级不展示
            }
            String levelName = ClazzLevel.getDescription(ConversionUtils.toInt(level));
            List<Clazz> tmp = clazzList.stream().filter(clazz -> Objects.equals(clazz.getClassLevel(), level))
                    .sorted(new Clazz.ClazzLevelAndNameComparator()).collect(Collectors.toList());
            List<Map<String, Object>> clazzs = new ArrayList<>();
            List<Map<String, Object>> administrativeClass = new ArrayList<>();
            List<Map<String, Object>> teachingClass = new ArrayList<>();
            for (Clazz clazz : tmp) {
                if (!clazz.matchEduSystem(eduSystemType)) {
                    continue;
                }
                List<GroupMapper> groupMappers = groupMap.get(clazz.getId());
                if (groupMappers == null) {// 班级下没有分组，继续
                    Map<String, Object> map = new HashMap<>();
//                    map.put("key", levelName + clazz.getClassName());
                    map.put("clazzId", clazz.getId());
                    map.put("clazzName", levelName + clazz.getClassName());
                    map.put("clazzType", clazz.getClazzType());
                    map.put("groups", "");
                    clazzs.add(map);
                    continue;
                }
                Map<String, List<GroupMapper>> groupParentMap = groupMappers.stream()
                        .filter(groupMapper -> StringUtils.isNotEmpty(groupMapper.getGroupParent()))
                        .collect(Collectors.groupingBy(GroupMapper::getGroupParent, Collectors.toList()));
                List<Map<String, Object>> groupList = new ArrayList<>();
                int count = 1;
                for (int i = 0; i < groupMappers.size(); i++) {
                    GroupMapper gm = groupMappers.get(i);
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("clazzId", clazz.getId());
                    detail.put("groupName", levelName + clazz.getClassName() + " 班群" + count);
                    detail.put("groupType", gm.getGroupType() == GroupType.WALKING_GROUP ? "TEACHING_GROUP" : "SYSTEM_GROUP");
                    List<KlxStudent> klxStudents = klxGroupStudents.get(gm.getId());
                    if (klxStudents == null) {// 分组下没有学生
                        continue;
                    }
                    klxStudents = klxStudents.stream().filter(k -> !Objects.equals(k.getName(), UserConstants.EXPERIENCE_ACCOUNT_NAME)).collect(Collectors.toList());
                    List<Map<String, Object>> satList = new ArrayList<>();
                    Map<String, Object> sat = new HashMap<>();
                    List<Long> shareGroupIds = new ArrayList<>();//todo
                    List<KlxStudent> klxStudentList = new ArrayList<>();
                    if (StringUtils.isNotEmpty(gm.getGroupParent())) {
                        List<GroupMapper> shareGroups = groupParentMap.getOrDefault(gm.getGroupParent(), Collections.emptyList());
                        shareGroups.forEach(p -> {
                            Map<String, Object> st = new HashMap<>();
                            st.put("subject", p.getSubject());
                            st.put("groupId", p.getId());
                            Long teacherId = groupTeacherIdMap.get(p.getId());
                            if (teacherId != null) {// 分组下有老师
                                st.put("teacherName", teacherMap.get(teacherId) == null ? "--" : teacherMap.get(teacherId).fetchRealname());
                                st.put("teacherId", teacherId);
                            }
                            satList.add(st);
                            shareGroupIds.add(p.getId());
                            List<KlxStudent> klxStuList = klxGroupStudents.get(p.getId());
                            if (CollectionUtils.isNotEmpty(klxStuList) && !(klxStuList = klxStuList.stream().filter(k -> !Objects.equals(k.getName(), UserConstants.EXPERIENCE_ACCOUNT_NAME)).collect(Collectors.toList())).isEmpty()) {
                                klxStudentList.addAll(klxStuList);
                            }
                        });
                        detail.put("groupIds", shareGroupIds);
                        List<KlxStudent> klxStudentList1 = new ArrayList<>(
                                klxStudentList.stream()
                                        .collect(Collectors.toMap(KlxStudent::getId, Function.identity(), (u, v) -> u))
                                        .values()
                        );
                        detail.put("studentNum", klxStudentList1.size());
                        groupMappers.removeAll(shareGroups);
                        i--;
                    } else {
                        shareGroupIds.add(gm.getId());
                        detail.put("groupIds", shareGroupIds);
                        sat.put("subject", gm.getSubject());
                        Long teacherId = groupTeacherIdMap.get(gm.getId());
                        if (teacherId != null) {// 分组下有老师
                            sat.put("teacherName", teacherMap.get(teacherId).fetchRealname());
                            sat.put("teacherId", teacherMap.get(teacherId).getId());
                        }
                        satList.add(sat);
                        detail.put("studentNum", klxStudents.size());
                    }
                    detail.put("subjectAndTeacher", satList);
                    groupList.add(detail);
                    if (gm.getGroupType() == GroupType.WALKING_GROUP) {
                        String desc = gm.getStageType() == null ? StageType.UNKNOWN.getDesc() : gm.getStageType().getDesc();
                        detail.put("stageType", desc);
                        teachingClass.add(detail);
                    } else {
                        detail.put("artScienceType", gm.getArtScienceType());
                        administrativeClass.add(detail);
                    }
                    count++;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("clazzId", clazz.getId());
                map.put("clazzName", levelName + clazz.getClassName());
                map.put("clazzType", clazz.getClazzType());
                // 修正名字
                if (groupList.size() == 1) {
                    groupList.get(0).put("groupName", levelName + clazz.getClassName());
                }
                map.put("groups", groupList);
                clazzs.add(map);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("gradeId", level);
            map.put("gradeName", levelName);
            map.put("clazzs", clazzs);
            map.put("administrativeClass", administrativeClass);
            map.put("teachingClass", teachingClass);
            menu.add(map);// 添加年级内数据

            // 组装调整班级所需数据
            Map<String, Object> adjustMap = new HashMap<>();
            adjustMap.put("gradeId", level);
            adjustMap.put("gradeName", levelName);
            List<Map<String, Object>> aClassList = new ArrayList<>();
            List<Map<String, Object>> tClassList = new ArrayList<>();
            tmp.stream().filter(clazz -> clazz.getClassType().equals(ClazzType.PUBLIC.getType()))
                    .forEach(c -> {
                        Map<String, Object> temp = new HashMap<>();
                        temp.put("clazzId", c.getId());
                        temp.put("clazzName", c.getClassName());
                        aClassList.add(temp);
                    });
            tmp.stream().filter(clazz -> clazz.getClassType().equals(ClazzType.WALKING.getType()))
                    .forEach(c -> {
                        Map<String, Object> temp = new HashMap<>();
                        temp.put("clazzId", c.getId());
                        temp.put("clazzName", c.getClassName());
                        tClassList.add(temp);
                    });
            adjustMap.put("aClassList", aClassList);
            adjustMap.put("tClassList", tClassList);
            adjustClazzList.add(adjustMap);
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();

        // 调整 adjustClazz
        return result.add("menu", menu)
                .add("adjustClazz", adjustClazzList)
                .add("subjects", validSubjectList())
                .add("scanNumDigit", schoolExtInfo == null ? SchoolExtInfo.DefaultScanNumberDigit : schoolExtInfo.fetchScanNumberDigit())
                .add("schoolLevel", SchoolLevel.safeParse(school.getLevel()).name());
    }

    /**
     * 新增班级
     */
    @ResponseBody
    @RequestMapping(value = "addnewclazz.vpage", method = RequestMethod.POST)
    public MapMessage addNewClazz() {
        int clazzType = getRequestInt("clazzType"); // 1.行政班，3.教学班(原来叫教学班)
        String level = getRequestString("gradeId");
        Long walkingTeacherId = getRequestLong("walkingTeacherId");
        if (level == null) {
            return MapMessage.errorMessage("年级信息获取错误,请刷新重试");
        }
        ClazzLevel clazzLevel = ClazzLevel.parse(ConversionUtils.toInt(level));
        String clazzName = getRequestString("clazzName");
        if (StringUtils.isBlank(clazzName)) {
            return MapMessage.errorMessage("班级名称不能为空");
        }
        clazzName = StringUtils.remove(clazzName, "班") + "班";
        String subjectStr = getRequestString("subject");
        if (clazzType == ClazzType.WALKING.getType() && subjectStr == null) {
            return MapMessage.errorMessage("学科信息获取错误,请刷新重试");
        }
        Subject subject = Subject.of(subjectStr);
        Long schoolId = currentSchoolId();
        User user = currentUser();
        if (schoolId == null) {
            return MapMessage.errorMessage("教务老师所在学校不存在");
        }
        School school = currentSchool();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在");
        }
        String eduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
        if (clazzType == ClazzType.PUBLIC.getType()) {
            // 创建行政班流程
            String N = clazzName;
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId)
                    .enabled()
                    .clazzLevel(clazzLevel)
                    .toList()
                    .stream()
                    .filter(t -> StringUtils.equals(t.getClassName(), N))
                    .filter(Clazz::isSystemClazz)
                    .findFirst()
                    .orElse(null);
            if (clazz != null) {
                return MapMessage.errorMessage("此学校已经存在{}{}", clazzLevel.getDescription(), clazzName);
            }
            ClassMapper classMapper = new ClassMapper();
            classMapper.setSchoolId(schoolId);
            classMapper.setClassLevel(ConversionUtils.toString(clazzLevel.getLevel()));
            classMapper.setClazzName(clazzName);
            classMapper.setFreeJoin(Boolean.TRUE);
            classMapper.setEduSystem(eduSystem);
            classMapper.setOperatorId("教务老师:" + user.getId());
            MapMessage createSysClazzMessage = clazzServiceClient.createSystemClazz(Collections.singleton(classMapper));
            if (createSysClazzMessage.isSuccess()) {
                return createSysClazzMessage.setInfo("创建" + clazzLevel.getDescription() + clazzName + "成功");
            } else {
                return createSysClazzMessage.setInfo("创建" + clazzLevel.getDescription() + clazzName + "失败");
            }

        } else if (clazzType == ClazzType.WALKING.getType()) {
            // 创建教学班流程
            String N = Subject.of(subjectStr).getValue() + clazzName;
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId)
                    .enabled()
                    .clazzLevel(clazzLevel)
                    .toList()
                    .stream()
                    .filter(t -> StringUtils.equals(t.getClassName(), N))
                    .filter(Clazz::isSystemClazz)
                    .findFirst()
                    .orElse(null);
            if (clazz != null) {
                return MapMessage.errorMessage("此学校已经存在{}{}", clazzLevel.getDescription(), N);
            }

            int clazzNum = specialTeacherLoaderClient.findValidGroup(walkingTeacherId);

            if (clazzNum >= ClazzConstants.MAX_GROUP_COUNT) {
                return MapMessage.errorMessage("老师名下班级数不能超过8个");
            }

            ClassMapper classMapper = new ClassMapper();
            classMapper.setSchoolId(schoolId);
            classMapper.setClassLevel(ConversionUtils.toString(clazzLevel.getLevel()));
            classMapper.setClazzName(N);
            classMapper.setFreeJoin(Boolean.TRUE);
            classMapper.setEduSystem(eduSystem);
            classMapper.setOperatorId("教务老师:" + user.getId());
            MapMessage createSysClazzMessage = specialTeacherServiceClient.createWalkingClazz(Collections.singleton(classMapper), walkingTeacherId);
            if (createSysClazzMessage.isSuccess()) {
                schoolExtServiceClient.getSchoolExtService()
                        .addWalkingClazzName(schoolId, clazzLevel, subject, clazzName)
                        .getUninterruptibly();
                String stage = getRequestString("stageType");
                Long clazzId = ConversionUtils.toLong(createSysClazzMessage.get("clazzId"));
                GroupMapper groupMapper = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(walkingTeacherId, clazzId, false);
                clazzGroupServiceClient.getGroupService().updateGroupStageType(groupMapper.getId(), StageType.parse(stage)).getUninterruptibly();
            }
            return createSysClazzMessage;

        }
        return MapMessage.errorMessage();
    }

    /**
     * 搜索学生
     */
    @ResponseBody
    @RequestMapping(value = "searchstudent.vpage", method = RequestMethod.GET)
    public MapMessage searchStudent() {
        String key = getRequestString("key");
        if (StringUtils.isBlank(key)) {
            return MapMessage.errorMessage("请输入学生姓名／学号／填涂号搜索对应学生");
        }
        ResearchStaff user = currentSpecialTeacher();
        if (user == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }
        Long schoolId = currentSchoolId();
        // 找到学校下所有班级
        List<Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId).enabled().toList();
        Map<Long, Clazz> clazzMap = clazzList.stream().collect(Collectors.toMap(Clazz::getId, Function.identity()));
        // 找到班级下所有分组
        Map<Long, List<GroupMapper>> groupMap = deprecatedGroupLoaderClient.loadClazzGroups(clazzList.stream().map(Clazz::getId).collect(Collectors.toList()));
        List<Long> groupIdList = groupMap.values().stream().flatMap(Collection::stream).map(GroupMapper::getId).collect(Collectors.toList());
        // 找到分组下所有学生
        Map<Long, List<KlxStudent>> groupKlxMap = newKuailexueLoaderClient.loadKlxGroupStudents(groupIdList);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long clazzId : groupMap.keySet()) {
            Set<String> matchStudentIdSet = new HashSet<>();  // 用于同班学生去重 , 显示该学生所在的所有班级
            Clazz clazz = clazzMap.get(clazzId);
            List<Long> groupIds = groupMap.get(clazzId).stream().map(GroupMapper::getId).collect(Collectors.toList());

            for (Long groupId : groupIds) {
                List<KlxStudent> klxStudents = groupKlxMap.get(groupId);
                List<KlxStudent> filterStudentNum = klxStudents.stream().filter(s -> StringUtils.equals(key, s.getStudentNumber())).collect(Collectors.toList());
                List<KlxStudent> filterScanNum = klxStudents.stream().filter(s -> StringUtils.equals(key, s.getScanNumber())).collect(Collectors.toList());
                List<KlxStudent> filterStudentName = klxStudents.stream().filter(s -> StringUtils.equals(key, s.getName())).collect(Collectors.toList());

                List<KlxStudent> matchedStudents = new ArrayList<>();
                matchedStudents.addAll(filterStudentNum);
                matchedStudents.addAll(filterScanNum);
                matchedStudents.addAll(filterStudentName);

                matchedStudents.forEach(student -> {
                    if (matchStudentIdSet.contains(student.getId())) {
                        return;
                    }
                    matchStudentIdSet.add(student.getId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("studentName", student.getName());
                    map.put("studentNum", student.getStudentNumber());
                    map.put("clazzName", clazz.formalizeClazzName());
                    map.put("groupId", groupId);
                    result.add(map);
                });
            }
        }

        return MapMessage.successMessage().add("studentList", result);
    }

    /**
     * 搜索老师
     */
    @ResponseBody
    @RequestMapping(value = "searchteacher.vpage", method = RequestMethod.GET)
    public MapMessage searchTeacher() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        String key = getRequestString("key");
        String subjectName = getRequestString("subject");
        Subject subject = Subject.of(subjectName);

        Long schoolId = currentSchoolId();
        List<Teacher> teacherList = teacherLoaderClient.loadSchoolTeachers(schoolId);
        List<Teacher> teacherResultList = new ArrayList<>();
        if (StringUtils.isNotEmpty(subjectName)) {
            teacherList = teacherList.stream().filter(teacher -> teacher.getSubject() == subject).collect(Collectors.toList());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        if (StringUtils.isNotEmpty(key)) {
            // 如果搜索关键字是手机号
            if (MobileRule.isMobile(key)) {
                UserAuthentication ua = userLoaderClient.loadMobileAuthentication(key, UserType.TEACHER);
                if (ua != null && ua.isMobileAuthenticated()) {
                    List<Teacher> filterMobile = teacherList.stream().filter(t -> t.getId().equals(ua.getId())).collect(Collectors.toList());
                    teacherResultList.addAll(filterMobile);
                }

            }
            List<Teacher> filterId = teacherList.stream().filter(t -> t.getId().equals(ConversionUtils.toLong(key))).collect(Collectors.toList());
            List<Teacher> filterName = teacherList.stream().filter(t -> t.fetchRealname().contains(key)).collect(Collectors.toList());
            teacherResultList.addAll(filterId);
            teacherResultList.addAll(filterName);

        } else {
            teacherResultList = teacherList;
        }
        teacherResultList.forEach(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("teacherName", u.fetchRealname());
            map.put("teacherId", u.getId());
            result.add(map);
        });
        return MapMessage.successMessage().add("teacherList", result);
    }

    /**
     * 添加班群
     */
    @ResponseBody
    @RequestMapping(value = "addnewgroup.vpage", method = RequestMethod.POST)
    public MapMessage addNewGroup() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        Long teacherId = getRequestLong("teacherId");
        Long clazzId = getRequestLong("clazzId");

        int clazzNum = specialTeacherLoaderClient.findValidGroup(teacherId);

        if (clazzNum >= ClazzConstants.MAX_GROUP_COUNT) {
            return MapMessage.errorMessage("老师名下班级数不能超过8个");
        }

        List<Group> groups = clazzGroupServiceClient.getGroupService()
                .findGroupsByClazzId(clazzId)
                .getUninterruptibly();

        if (CollectionUtils.isNotEmpty(groups)) {
            return MapMessage.errorMessage("该班级已创建，请在详情页添加老师");
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz.isPublicClazz()) {
            return groupServiceClient.createTeacherGroup(teacherId, clazzId, null);
        } else if (clazz.isWalkingClazz()) {
            if (groups.size() > 0) {
                return MapMessage.errorMessage(clazz.formalizeClazzName() + "下已有分组，无法新建");
            }
            return groupServiceClient.createWalkingGroup(teacherId, clazzId);
        }

        return MapMessage.errorMessage();
    }

    /**
     * 合并班群
     */
    @ResponseBody
    @RequestMapping(value = "mergegroups.vpage", method = RequestMethod.POST)
    public MapMessage mergeGroups() {
        String groupIdStr = getRequestString("groupIds");
        String[] groupIdArray = groupIdStr.split(",");
        List<String> groupIdList = Arrays.asList(groupIdArray);
        List<Long> groupIds = new ArrayList<>();
        groupIdList.forEach(p -> {
            groupIds.add(ConversionUtils.toLong(p));
        });
        if (groupIds.size() < 2) {
            return MapMessage.errorMessage("合并班组少于2个");
        }
        groupServiceClient.shareGroups(groupIds, Boolean.FALSE);

        for (int i = 1; i < groupIds.size(); i++) {
            MapMessage msg = newKuailexueServiceClient.mergeKlxGroupStudent(groupIds.get(0), groupIds.get(i), "sync");
            if (!msg.isSuccess()) {
                return msg;
            }
        }
        groupServiceClient.shareGroups(groupIds, Boolean.TRUE);

        return MapMessage.successMessage();
    }

//  ========================================= 班群详情页 =========================================

    /**
     * 班群详情页
     */
    @ResponseBody
    @RequestMapping(value = "groupdetail.vpage", method = RequestMethod.POST)
    public MapMessage groupDetail() {
        String data = getRequestString("groupIds");
        List<Long> groupIds = JsonUtils.fromJsonToList(data, Long.class);
        Map<Long, List<KlxStudent>> klxStudentsMap = newKuailexueLoaderClient.loadKlxGroupStudents(groupIds);
        List<KlxStudent> klxStudents = klxStudentsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        klxStudents = klxStudents.stream().collect(Collectors.toMap(KlxStudent::getId, Function.identity(), (u, v) -> u))
                .values()
                .stream()
                .sorted((k1, k2) -> {
                    Long n1 = SafeConverter.toLong(k1.getStudentNumber());
                    Long n2 = SafeConverter.toLong(k2.getStudentNumber());
                    return Long.compare(n1, n2);
                })
                .collect(Collectors.toList());

        // 自动执行同步学生名单功能
        Map<Long, List<User>> studentsMap = studentLoaderClient.loadGroupStudents(groupIds);
        Map<Long, KlxStudent> klxStudentA17Maps = klxStudents.stream().filter(k -> k.getA17id() != null).collect(Collectors.toMap(KlxStudent::getA17id, Function.identity(), (u, v) -> u));
        List<KlxStudent> finalKlxStudents = new ArrayList<>();
        for (Long groupId : studentsMap.keySet()) {
            List<User> students = studentsMap.get(groupId);
            students.forEach(s -> {
                if (!klxStudentA17Maps.containsKey(s.getId())) {
                    // 创建快乐学学生
                    MapMessage klxStudentMessage = newKuailexueServiceClient.createKlxStudentAndNoneKlxId(currentSchoolId(), s, groupId);
                    if (klxStudentMessage.isSuccess()) {
                        KlxStudent klxStudent = JsonUtils.fromJson(JsonUtils.toJson(klxStudentMessage.get("klxStudent")), KlxStudent.class);
                        finalKlxStudents.add(klxStudent);
                    }
                }
            });
        }
        klxStudents.addAll(finalKlxStudents);
        // 合并学生功能
        Map<String, List<KlxStudent>> mergeSameNameStudent = getMergeSameNameStudent(klxStudents);
        Set<String> mergeStudentsNames = mergeSameNameStudent.keySet();
        List<Map<String, Object>> studentList = new ArrayList<>();
        for (KlxStudent tmp : klxStudents) {
            if (Objects.equals(tmp.getName(), UserConstants.EXPERIENCE_ACCOUNT_NAME)) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("studentName", tmp.getName());
            map.put("studentNum", tmp.getStudentNumber());
            map.put("a17id", tmp.getA17id());
            map.put("klxUserId", tmp.getId());
            String mobile;
            if (tmp.isRealStudent()) {
                mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(tmp.getA17id());
            } else {
                mobile = sensitiveUserDataServiceClient.loadKlxStudentMobileObscured(tmp.getId());
            }
            map.put("studentMobile", mobile);
            map.put("scanNum", tmp.getScanNumber());
            map.put("isMarked", tmp.getIsMarked());
            studentList.add(map);
        }

        // 共享分组下学生不一致要有错误提示
        Set<Long> studentNum = new HashSet<>();
        for (Long gid : klxStudentsMap.keySet()) {
            studentNum.add(klxStudentsMap.get(gid).stream().filter(s -> !UserConstants.EXPERIENCE_ACCOUNT_NAME.equals(s.getName())).count());
        }
        if (studentNum.size() > 1) {
            return MapMessage.successMessage().add("errorFlag", true).add("students", studentList);
        }

        return MapMessage.successMessage().add("students", studentList).add("mergeStudentsNames", mergeStudentsNames);
    }

    @ResponseBody
    @RequestMapping(value = "mergesamenamestudent.vpage", method = RequestMethod.POST)
    public MapMessage mergeSameNameStudent() {
        String data = getRequestString("groupIds");
        List<Long> groupIds = JsonUtils.fromJsonToList(data, Long.class);
        Map<Long, List<KlxStudent>> klxStudentsMap = newKuailexueLoaderClient.loadKlxGroupStudents(groupIds);
        List<KlxStudent> klxStudents = klxStudentsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        klxStudents = klxStudents.stream().collect(Collectors.toMap(KlxStudent::getId, Function.identity(), (u, v) -> u))
                .values()
                .stream()
                .sorted((k1, k2) -> {
                    Long n1 = SafeConverter.toLong(k1.getStudentNumber());
                    Long n2 = SafeConverter.toLong(k2.getStudentNumber());
                    return Long.compare(n1, n2);
                })
                .collect(Collectors.toList());
        // 需要合并的学生信息
        Map<String, List<KlxStudent>> mergeSameNameStudent = getMergeSameNameStudent(klxStudents);
        for (Map.Entry<String, List<KlxStudent>> studentEntry : mergeSameNameStudent.entrySet()) {
            List<KlxStudent> students = studentEntry.getValue();
            String klxId = "";
            Long a17Id = null;
            for (KlxStudent student : students) {
                if (!StringUtils.isBlank(student.getScanNumber()) || !StringUtils.isBlank(student.getStudentNumber())) {
                    klxId = student.getId();
                } else if (student.getA17id() != null) {
                    a17Id = student.getA17id();
                }
            }
            if (a17Id != null && StringUtils.isNoneBlank(klxId)) {
                // 将17id与klxId进行合并
                newKuailexueServiceClient.mergeOTOSingleStudent(klxId, a17Id);
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 同步共享分组学生
     */
    @ResponseBody
    @RequestMapping(value = "syncgroupstudents.vpage", method = RequestMethod.POST)
    public MapMessage syncGroupStudents() {
        String data = getRequestString("groupIds");
        List<Long> groupIds = JsonUtils.fromJsonToList(data, Long.class);

        MapMessage msg = newKuailexueServiceClient.syncSharedGroupStudent(groupIds.get(0));
        if (!msg.isSuccess()) {
            return msg;
        }

        return MapMessage.successMessage();
    }

    /**
     * 调整老师预处理接口
     */
    @ResponseBody
    @RequestMapping(value = "adjustteacherpre.vpage", method = RequestMethod.GET)
    public MapMessage adjustTeacherPre() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        String subjectName = getRequestString("subject");
        Subject subject = Subject.of(subjectName);
        Long schoolId = currentSchoolId();
        List<Teacher> teacherList = teacherLoaderClient.loadSchoolTeachers(schoolId);
        if (StringUtils.isNotEmpty(subjectName)) {
            teacherList = teacherList.stream().filter(teacher -> teacher.getSubject() == subject).collect(Collectors.toList());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        List<Long> dupTeacherIds = new ArrayList<>();
        teacherList.forEach(teacher -> {
            if (!dupTeacherIds.contains(teacher.getId())) {
                Map<String, Object> map = new HashMap<>();
                map.put("teacherName", teacher.getProfile().getRealname());
                map.put("teacherId", teacher.getId());
                map.put("subject", teacher.getSubject());
                result.add(map);
                dupTeacherIds.add(teacher.getId());
            }
        });
        return MapMessage.successMessage().add("teacherList", result);
    }

    /**
     * 调整老师接口
     */
    @ResponseBody
    @RequestMapping(value = "adjustteacher.vpage", method = RequestMethod.POST)
    public MapMessage adjustTeacher() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        Long oldTeacherId = getRequestLong("oldTeacherId");
        Long newTeacherId = getRequestLong("newTeacherId");
        Long groupId = getRequestLong("groupId");
        Long clazzId = getRequestLong("clazzId");
        if (Objects.equals(oldTeacherId, newTeacherId)) { //换同一个老师 直接返回
            return MapMessage.successMessage();
        }

        int clazzNum = specialTeacherLoaderClient.findValidGroup(newTeacherId);

        if (clazzNum >= ClazzConstants.MAX_GROUP_COUNT) {
            return MapMessage.errorMessage("老师名下班级数不能超过8个");
        }

        if (oldTeacherId > 0L) {//学科已经有老师，对老师做调整
            GroupMapper groupMapper = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(oldTeacherId, clazzId, false);
            if (groupMapper == null) {
                return MapMessage.errorMessage("老师{}在班级{}下没有有效分组", oldTeacherId, clazzId);
            }
            GroupMapper gm = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(newTeacherId, clazzId, false);
            if (Objects.nonNull(gm)) {
                return MapMessage.errorMessage("新老师{}在班级{}下已有分组", newTeacherId, clazzId);
            }
            MapMessage message = specialTeacherServiceClient.adjustTeacher(groupMapper.getId(), oldTeacherId, newTeacherId);
            if (message.isSuccess()) {
                //前老师退出班级
                clazzServiceClient.teacherExitSystemClazz(oldTeacherId, clazzId, Boolean.FALSE, OperationSourceType.crm);
                //调整老师发站内信
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(clazzId);
                Teacher teacher = teacherLoaderClient.loadTeacher(newTeacherId);
                String content = "您的" + clazz.formalizeClazzName() + "由教务老师" + specialTeacher.fetchRealname() + "转给了" + teacher.fetchRealname() + "老师，请知晓。";
                sendMessage(oldTeacherId, content);
            }
        } else {//学科没有老师，新增一个老师
            GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, false);
            specialTeacherServiceClient.addTeacher(groupId, newTeacherId);
        }

        return MapMessage.successMessage();
    }

    /**
     * 添加老师接口
     */
    @ResponseBody
    @RequestMapping(value = "addteacher.vpage", method = RequestMethod.POST)
    public MapMessage addTeacher() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        Long newTeacherId = getRequestLong("newTeacherId");
        Long clazzId = getRequestLong("clazzId");
        Long groupId = getRequestLong("groupId");

        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            return MapMessage.errorMessage("无效的班组信息");
        }

        int clazzNum = specialTeacherLoaderClient.findValidGroup(newTeacherId);

        if (clazzNum >= ClazzConstants.MAX_GROUP_COUNT) {
            return MapMessage.errorMessage("老师名下班级数不能超过8个");
        }

        List<Subject> subjects = deprecatedGroupLoaderClient.loadClazzGroups(clazzId)
                .stream()
                .map(GroupMapper::getSubject)
                .collect(Collectors.toList());
        Teacher teacher = teacherLoaderClient.loadTeacher(newTeacherId);
        if (subjects.contains(teacher.getSubject())) {
            return MapMessage.errorMessage("班级下已包含该学科老师");
        }

        Long newTeacherGroupId;
        // 读取对应共享分组
        Set<Long> sharedGroupIds = new HashSet<>();
        sharedGroupIds.add(groupId);//原来的组ID没查到结果会返回为空集合，那么将这个分组直接和新建老师的分组关联

        if (StringUtils.isEmpty(groupMapper.getGroupParent())) {
            MapMessage mapMessage = groupServiceClient.createTeacherGroup(newTeacherId, clazzId, null);
            newTeacherGroupId = ConversionUtils.toLong(mapMessage.get("groupId"));
            if (newTeacherGroupId != 0L) {
                sharedGroupIds.add(newTeacherGroupId);
                groupServiceClient.shareGroups(sharedGroupIds, Boolean.TRUE);
            }
        } else {
            MapMessage message = groupServiceClient.createTeacherGroup(newTeacherId, clazzId, groupMapper.getGroupParent());
            newTeacherGroupId = ConversionUtils.toLong(message.get("groupId"));
        }

        if (newTeacherGroupId == 0L) {
            return MapMessage.errorMessage();
        }

        Set<Long> groupIds = new HashSet<>(deprecatedGroupLoaderClient.loadSharedGroupIds(newTeacherGroupId));
        groupIds.add(newTeacherGroupId);
        groupServiceClient.shareGroups(groupIds, Boolean.FALSE);
        groupIds.remove(newTeacherGroupId);
        groupIds.forEach(gid ->
                newKuailexueServiceClient.mergeKlxGroupStudent(newTeacherGroupId, gid, "sync")
        );
        groupIds.add(newTeacherGroupId);
        groupServiceClient.shareGroups(groupIds, Boolean.TRUE);

        return MapMessage.successMessage();
    }

    /**
     * 删除班群老师接口
     */
    @ResponseBody
    @RequestMapping(value = "delteacher.vpage", method = RequestMethod.POST)
    public MapMessage delTeacher() {
        Long clazzId = getRequestLong("clazzId");
        Long groupId = getRequestLong("groupId");
        Long teacherId = getRequestLong("teacherId");

        Set<Long> sharedGroupIds = new HashSet<>(deprecatedGroupLoaderClient.loadSharedGroupIds(groupId));
        if (sharedGroupIds.size() == 0) {
            return MapMessage.errorMessage("班群中至少需要一个老师");
        }
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
        MapMessage msg = groupServiceClient.disableTeacherGroup(teacherId, clazzId, true);

        return msg.add("delGroupId", groupMapper.getId());
    }

    /**
     * 在线添加学生账号
     */
    @ResponseBody
    @RequestMapping(value = "addstudentsonline.vpage", method = RequestMethod.POST)
    public MapMessage addStudentsOnline() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        boolean checkRepeatStudent = getRequestBool("checkRepeatedStudent");
        boolean checkTakeUpStudent = getRequestBool("checkTakeUpStudent");
        boolean checkDeleteStudent = getRequestBool("checkDeleteStudent");
        boolean recoverStudent = getRequestBool("recoverStudent");
        Long clazzId = getRequestLong("clazzId");
        Long teacherId = getRequestLong("teacherId");
        String content = getRequestString("batchContext");
        String[] contents = content.split("\\n");
        Map<String, String> stuInfoMap = new HashMap<>();
        List<ImportStudentData> studentData = new LinkedList<>();
        List<String> dupStudentNames = new ArrayList<>();
        if (contents.length > 20) {
            return MapMessage.errorMessage("一次添加帐号不得超过20个");
        }
        for (int i = 0; i < contents.length; i++) {
            if (StringUtils.isEmpty(contents[i])) { //如果当前行为空则继续
                continue;
            }
            String[] contextArray = contents[i].trim().split("[\\s]+");
            String studentName = StringUtils.replaceAll(contextArray[0], "\\s*", "");
            String studentNumber = "";
            if (contextArray.length == 2) { // 学号存在才取
                studentNumber = StringUtils.replaceAll(contextArray[1], "\\s*", "");
            }
            // 检查姓名
            if (!SpecialTeacherConstants.checkChineseName(studentName)) {
                return MapMessage.errorMessage("第{}行 学生姓名{}错误，仅支持十个字以内中文、间隔符·，请更改！", i + 1, studentName);
            }
            dupStudentNames.add(studentName);
            //批量导入学生
            stuInfoMap.put(studentName, studentNumber);
            studentData.add(new ImportStudentData(studentName, studentNumber, i + 1));
        }
        if (dupStudentNames.size() != dupStudentNames.stream().distinct().collect(Collectors.toList()).size()) {
            return MapMessage.errorMessage("学生姓名重复，请更改！");
        }

        return importKlxStudents(stuInfoMap, studentData, checkRepeatStudent, checkTakeUpStudent, checkDeleteStudent, recoverStudent, clazzId, teacherId);
    }

    /**
     * 通过excel添加学生账号
     */
    @ResponseBody
    @RequestMapping(value = "addstudentbyexcel.vpage", method = RequestMethod.POST)
    @SuppressWarnings("unchecked")
    public MapMessage addStudentsByExcel() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        boolean checkRepeatStudent = getRequestBool("checkRepeatedStudent");
        boolean checkTakeUpStudent = getRequestBool("checkTakeUpStudent");
        boolean checkDeleteStudent = getRequestBool("checkDeleteStudent");
        boolean recoverStudent = getRequestBool("recoverStudent");
        Long clazzId = getRequestLong("clazzId");
        Long teacherId = getRequestLong("teacherId");

        // 解析excel文档
        Workbook workbook = getRequestWorkbook("adjustExcel");
        MapMessage checkMsg = checkImportExcel(workbook);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        //批量导入学生
        Map<String, String> stuInfoMap = (Map<String, String>) checkMsg.get("stuInfoMap");
        List<ImportStudentData> studentData = (List<ImportStudentData>) checkMsg.get("studentData");

        return importKlxStudents(stuInfoMap, studentData, checkRepeatStudent, checkTakeUpStudent, checkDeleteStudent, recoverStudent, clazzId, teacherId);
    }

    /**
     * 编辑学生信息
     */
    @ResponseBody
    @RequestMapping(value = "editstudentinfo.vpage", method = RequestMethod.POST)
    public MapMessage editStudentInfo() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        Long clazzId = getRequestLong("clazzId");
        Long a17id = getRequestLong("a17id");
        String klxUserId = getRequestString("klxUserId");
        String studentName = getRequestString("studentName");
        String studentNum = getRequestString("studentNum");
        String scanNum = getRequestString("scanNum");
        Boolean isMarked = getRequestBool("isMarked");
        if (StringUtils.isBlank(studentName) || StringUtils.isBlank(studentNum) || StringUtils.isBlank(scanNum)) {
            return MapMessage.errorMessage("您有未输入的信息");
        }
        if (studentName.length() > 20) {
            return MapMessage.errorMessage("填写的学生名过长");
        }
        if (!StringUtils.isNumeric(studentNum)) {
            return MapMessage.errorMessage("请输入纯数字学号");
        }
        if (studentNum.length() > 14) {
            return MapMessage.errorMessage("填写的校内学号过长");
        }
        if (clazzId == 0) {
            return MapMessage.errorMessage("修改失败");
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null || clazz.getSchoolId() == null) {
            return MapMessage.errorMessage("班级信息无效");
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(clazz.getSchoolId())
                .getUninterruptibly();
        int digit = schoolExtInfo == null ? SchoolExtInfo.DefaultScanNumberDigit : schoolExtInfo.fetchScanNumberDigit();

        if (!StringUtils.isNumeric(scanNum) || scanNum.length() != digit) {
            return MapMessage.errorMessage("阅卷机号须为" + digit + "位数字");
        }
        if (a17id == 0 && StringUtils.isBlank(klxUserId)) {//真实学生或虚拟学都不存在
            return MapMessage.errorMessage("无效的学生");
        }

        Long schoolId = currentSchoolId();
        return specialTeacherServiceClient.modifyKlxStudentInfo(schoolId, clazzId, a17id, klxUserId, studentName, studentNum, scanNum, isMarked);
    }

    /**
     * 重置学生密码
     */
    @ResponseBody
    @RequestMapping(value = "resetstudentpassword.vpage", method = RequestMethod.POST)
    public MapMessage resetStudentPassword() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        Long clazzId = getRequestLong("clazzId");
        Long studentId = getRequestLong("a17id"); //没有17id的学生不能重置密码
        String password = getRequestString("password");
        String confirmPassword = getRequestString("confirmPassword");

        if (studentId == 0) {
            return MapMessage.errorMessage("请添加一起作业ID");
        }
        User user = currentUser();
        ResearchStaffManagedRegion rsmr = specialTeacherLoaderClient.findSchoolId(user.getId());
        if (rsmr == null || rsmr.getResarchStaffUserType() != ResearchStaffUserType.AFFAIR.getType()) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }
        // 如果学生未绑定手机，老师可以直接重置密码
        // 如果学生绑定了家长手机，则一天只能重置一次密码，且通过给手机发送随机密码的方式重置
        // 统一用一个接口并增加检查的原因是防止用户通过接口直接重置
        String studentOrParentMobile = studentLoaderClient.loadStudentOrParentMobile(studentId, SafeConverter.toString(specialTeacher.getId()));
        if (StringUtils.isNotBlank(studentOrParentMobile)) {
            // 老师给学生重置密码行为，一天只能一次
            if (!asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherResetBindedStudentPWCacheManager_canResetPw(user.getId(), studentId)
                    .getUninterruptibly()) {
                return MapMessage.errorMessage("您已经帮助重置过，如果学生没有收到密码可以联系客服");
            }

            // 生成随机密码
            confirmPassword = password = RandomGenerator.generatePlainPassword();
        }

        // 修改密码
        try {
            MapMessage mesg = AtomicLockManager.instance().wrapAtomic(specialTeacherServiceClient)
                    .keyPrefix(ClazzService.MANAGE_CLAZZ_PREFIX)
                    .keys(studentId)
                    .proxy()
                    .changeStudentPassword(clazzId, studentId, password, confirmPassword);
            if (mesg.isSuccess()) {
                logUserServiceRecord(studentId, "教务老师重置学生[" + studentId + "]密码，操作端[pc]", UserServiceRecordOperationType.用户信息变更, "重置学生密码");

                // 重置密码后处理
                this.updateAppSessionKeyForStudent(studentId);

                User student = raikouSystem.loadUser(studentId);
                // 如果是绑定手机的学生，则发送重置密码短信
                if (StringUtils.isNotBlank(studentOrParentMobile)) {
                    String smsPayload = StringUtils.formatMessage(
                            "{}同学好，老师正在帮你重置密码，请用新密码：{}登录做作业（如孩子在学校使用，请尽快将新密码转发给老师）",
                            student.fetchRealname(),
                            password
                    );
                    smsServiceClient.createSmsMessage(studentOrParentMobile)
                            .content(smsPayload)
                            .type(SmsType.TEACHER_RESET_STUDENT_PASSWORD.name())
                            .send();
                }

                // 老师修改学生密码,需要强制学生修改密码
                asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                        .unflushable_setUserBehaviorCount(UserBehaviorType.STUDENT_FORCE_RESET_PW, studentId, 1L, 0)
                        .awaitUninterruptibly();
            }

            return mesg;
        } catch (DuplicatedOperationException ex) {
            return MapMessage.successMessage("正在处理，请不要重复提交");
        }
    }

    /**
     * 删除学生
     */
    @ResponseBody
    @RequestMapping(value = "deletestudent.vpage", method = RequestMethod.POST)
    public MapMessage deleteStudent() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        Long clazzId = getRequestLong("clazzId");
        Long groupId = getRequestLong("groupId");
        Long a17id = getRequestLong("a17id");
        String klxUserId = getRequestString("klxUserId");


        Long schoolId = currentSchoolId();

        // 删除快乐学用户
        if (StringUtils.isNotBlank(klxUserId)) {
            return specialTeacherServiceClient.deleteKlxStudent(schoolId, groupId, klxUserId);
        }

        // TODO: 2017/7/3 有17ID数据删除处理 参考TeacherClazzController.removeClazzStudent
        // 针对于初高中的学生删除是不是不用考虑有17Id的情况？？


        return MapMessage.errorMessage();
    }

    /**
     * 学生转班
     */
    @ResponseBody
    @RequestMapping(value = "changeclazz.vpage", method = RequestMethod.POST)
    public MapMessage changeClazz() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        Long srcGroupId = getRequestLong("srcGroupId");
        Long targetGroupId = getRequestLong("targetGroupId");
        String klxUserIds = getRequestString("klxUserIds");

        if (srcGroupId.equals(targetGroupId)) {
            return MapMessage.successMessage();
        }

        String[] klxUserIdArray = StringUtils.split(klxUserIds, ",");
        List<String> klxUserIdList = Arrays.asList(klxUserIdArray);
        List<GroupKlxStudentRef> groupKlxStudentRefs = new ArrayList<>();
        List<GroupKlxStudentRef> delGroupKlxStudentRefs = new ArrayList<>();
        Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(klxUserIdList);
        List<KlxStudent> targetGroupStudents = newKuailexueLoaderClient.loadKlxGroupStudents(targetGroupId);
        List<String> dupKlxUserIds = new ArrayList<>();
        for (String key : klxStudentMap.keySet()) {
            KlxStudent klxStudent = klxStudentMap.get(key);
            for (KlxStudent targetStu : targetGroupStudents) {
                if (klxStudent.getName().equals(targetStu.getName())) {
                    dupKlxUserIds.add(klxStudent.getId());
                }
            }
        }
        if (dupKlxUserIds.size() != 0) { // 返回重复学生
            return MapMessage.errorMessage().add("dupKlxUserIds", dupKlxUserIds);
        }

        Set<Long> sharedGroupIds = new HashSet<>(deprecatedGroupLoaderClient.loadSharedGroupIds(targetGroupId)); //转班的学生同时要关联到目标分组的共享分组下
        sharedGroupIds.add(targetGroupId);
        Set<Long> srcSharedGroupIds = new HashSet<>(deprecatedGroupLoaderClient.loadSharedGroupIds(srcGroupId)); //原来分组关联的共享分组下的学生也要删除
        srcSharedGroupIds.add(srcGroupId);
        for (String klxUserId : klxUserIdList) {
            KlxStudent klxStudent = klxStudentMap.get(klxUserId);
            List<GroupKlxStudentRef> groupKlxStudentRefList = asyncGroupServiceClient.getAsyncGroupService()
                    .findGroupKlxStudentRefsByStudentWithoutWalkingGroup(klxUserId)
                    .take();
            delGroupKlxStudentRefs.addAll(groupKlxStudentRefList);
//            for (Long groupId : srcSharedGroupIds) {
//                GroupKlxStudentRef ref = GroupKlxStudentRef.newInstance(groupId, klxUserId, klxStudent.getA17id());
//                delGroupKlxStudentRefs.add(ref);
//            }
            for (Long groupId : sharedGroupIds) {
                GroupKlxStudentRef ref = new GroupKlxStudentRef();
                ref.setGroupId(groupId);
                ref.setA17id(klxStudent.getA17id());
                ref.setKlxStudentId(klxUserId);
                groupKlxStudentRefs.add(ref);
            }
        }
        newKuailexueServiceClient.disableGroupKlxStudentRefs(delGroupKlxStudentRefs);
        newKuailexueServiceClient.persistGroupKlxStudentRefs(groupKlxStudentRefs);
        return MapMessage.successMessage();
    }

    /**
     * 复制到教学班（即原来的教学班）
     */
    @ResponseBody
    @RequestMapping(value = "copytoteachingclazz.vpage", method = RequestMethod.POST)
    public MapMessage copyToTeachingClazz() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("该老师不是教务老师");
        }

        Long groupId = getRequestLong("groupId");
        Group group = groupLoaderClient.getGroupLoader().loadGroup(groupId)
                .getUninterruptibly();
        if (group == null || GroupType.WALKING_GROUP != group.getGroupType()) {
            return MapMessage.errorMessage("无效的教学班");
        }

        String klxUserIds = getRequestString("klxUserIds");
        List<String> klxUserIdList = Arrays.asList(StringUtils.split(klxUserIds, ","));
        Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(klxUserIdList);
        for (String klxStudentId : klxUserIdList) {
            KlxStudent klxStudent = klxStudentMap.get(klxStudentId);
            if (klxStudent == null) {
                continue;
            }
            Set<Long> studentGroupIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findGroupKlxStudentRefsByStudent(klxStudentId)
                    .getUninterruptibly()
                    .stream()
                    .map(GroupKlxStudentRef::getGroupId)
                    .filter(g -> !Objects.equals(groupId, g))
                    .collect(Collectors.toSet());

            List<Long> sameSubjectClazzs = groupLoaderClient.getGroupLoader().loadGroups(studentGroupIds)
                    .getUninterruptibly()
                    .values()
                    .stream()
                    .filter(g -> GroupType.WALKING_GROUP == g.getGroupType())
                    .filter(g -> Objects.equals(group.getSubject(), g.getSubject()))
                    .map(Group::getClazzId)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(sameSubjectClazzs)) {
                Set<String> clazzName = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzs(sameSubjectClazzs)
                        .stream()
                        .map(Clazz::formalizeClazzName)
                        .collect(Collectors.toCollection(TreeSet::new));

                return MapMessage.errorMessage(
                        "学生{}已经存在于相同{}学科的教学班：({}), 不允许加入，请确认学生班级信息",
                        klxStudent.getName(),
                        group.getSubject() == null ? "" : group.getSubject().getValue(),
                        StringUtils.join(clazzName, "、")
                );
            }

        }

        List<KlxStudent> teachingGroupStudents = newKuailexueLoaderClient.loadKlxGroupStudents(groupId);
        List<String> dupKlxUserNotMatchId = new ArrayList<>();
        List<String> dupKlxUserMatchId = new ArrayList<>();
        for (String key : klxStudentMap.keySet()) {
            KlxStudent klxStudent = klxStudentMap.get(key);
            for (KlxStudent targetStu : teachingGroupStudents) {
                if (klxStudent.getName().equals(targetStu.getName()) && !klxStudent.getId().equals(targetStu.getId())) {//重名账号不同
                    dupKlxUserNotMatchId.add(klxStudent.getId());
                }
                if (klxStudent.getName().equals(targetStu.getName()) && klxStudent.getId().equals(targetStu.getId())) {//重名账号相同
                    dupKlxUserMatchId.add(klxStudent.getId());
                }
            }
        }
        if (dupKlxUserNotMatchId.size() != 0) { // 返回重名账号不同学生
            return MapMessage.errorMessage().add("dupKlxUserNotMatchId", dupKlxUserNotMatchId);
        }

        List<GroupKlxStudentRef> groupKlxStudentRefs = new ArrayList<>();
        for (String klxUserId : klxStudentMap.keySet()) {
            if (dupKlxUserMatchId.contains(klxUserId)) {
                continue;
            }
            GroupKlxStudentRef ref = new GroupKlxStudentRef();
            KlxStudent klxStudent = klxStudentMap.get(klxUserId);
            ref.setGroupId(groupId);
            ref.setA17id(klxStudent.getA17id());
            ref.setKlxStudentId(klxUserId);
            groupKlxStudentRefs.add(ref);
            newKuailexueServiceClient.persistGroupKlxStudentRefs(groupKlxStudentRefs);

        }
        return MapMessage.successMessage().add("teachingClassExistStudents", dupKlxUserMatchId);
    }

//  ========================================= 老师学生管理页面 =========================================

    /**
     * 下载学生名单
     */
    @ResponseBody
    @RequestMapping(value = "downloadstudents.vpage", method = RequestMethod.POST)
    public void downloadStudents(HttpServletResponse response) {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return;
        }

        //
        int scanNumberDigit = SchoolExtInfo.DefaultScanNumberDigit;
        ResearchStaffManagedRegion managedRegion = specialTeacherLoaderClient.findSchoolId(specialTeacher.getId());
        if (managedRegion != null) {
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(managedRegion.getManagedRegionCode()).getUninterruptibly();
            if (schoolExtInfo != null) {
                scanNumberDigit = schoolExtInfo.fetchScanNumberDigit();
            }
        }

        boolean mergeStatus = getRequestBool("mergeStatus");
        int gradeId = getRequestInt("gradeId");
        String clazzIdStr = getRequestString("clazzIds");
        String[] clazzIdArray = clazzIdStr.split(",");

        List<String> clazzIdList = Arrays.asList(clazzIdArray);
        List<Long> clazzIds = new ArrayList<>();
        clazzIdList.forEach(p -> {
            clazzIds.add(ConversionUtils.toLong(p));
        });
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Map<Long, List<KlxStudent>> clazzKlxStudentsMap = specialTeacherLoaderClient.loadKlxStudentsByClazzIds(clazzIds);
        try {
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            String filename = "";
            if (clazzIdArray.length == 1) {
                // 下载某个班的学生
                List<KlxStudent> klxStudents = clazzKlxStudentsMap.get(clazzIds.get(0));
                Clazz clazz = clazzMap.get(clazzIds.get(0));
                String clazzName = "";
                String sheetName = "" + System.currentTimeMillis();//应对某些奇葩的浏览器只能这样了
                if (CollectionUtils.isNotEmpty(klxStudents) && Objects.nonNull(clazz)) {
                    clazzName = clazz.getClazzLevel().getDescription() + clazz.getClassName();
                    sheetName = clazzName + "(" + clazz.getId() + ")";
                    klxStudents.sort(Comparator.comparingInt(mapper -> SafeConverter.toInt(mapper.getScanNumber())));
                }
                filename = clazzName + "学生名单.xls";

                //处理数据格式
                List<ExcelExportData> datas = new ArrayList<>();
                datas.add(toExcelExportData(klxStudents, clazzName, sheetName, scanNumberDigit));
                hssfWorkbook = createXlsExcelExportData(datas);
            } else {
                String gradeName = ClazzLevel.parse(gradeId).getDescription();
                List<ExcelExportData> datas = new ArrayList<>();
                if (mergeStatus) {
                    // 二期添加 下载某个年级的学生 合并到一个sheet
                    filename = gradeName + "学生名单(合).xls";
                    Map<Long, List<KlxStudent>> map = new HashMap<>();// key:clazzId value:List<KlxStudent>
                    for (Long clazzId : clazzIds) {
                        List<KlxStudent> klxStudents = clazzKlxStudentsMap.get(clazzId);
                        if (CollectionUtils.isNotEmpty(klxStudents)) {
                            klxStudents.sort(Comparator.comparingInt(mapper -> SafeConverter.toInt(mapper.getScanNumber())));
                            map.put(clazzId, klxStudents);
                        }
                    }
                    datas.add(toExcelExportData(map, gradeName, clazzMap));
                } else {
                    // 下载某个年级的学生 每个班为一个sheet
                    filename = gradeName + "学生名单(分).xls";
                    for (Long clazzId : clazzIds) {
                        Clazz clazz = clazzMap.get(clazzId);
                        List<KlxStudent> klxStudents = clazzKlxStudentsMap.get(clazzId);
                        String clazzName = clazz.getClazzLevel().getDescription() + clazz.getClassName();
                        String sheetName = clazzName + "(" + clazz.getId() + ")";
                        if (CollectionUtils.isNotEmpty(klxStudents)) { //有的班级没有学生
                            klxStudents.sort(Comparator.comparingInt(mapper -> SafeConverter.toInt(mapper.getScanNumber())));
                        }
                        datas.add(toExcelExportData(klxStudents, clazzName, sheetName, scanNumberDigit));
                    }
                }
                hssfWorkbook = createXlsExcelExportData(datas);
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            logger.error("Failed download students.", e);
        }
    }

    // 判断教务老师是不是考试管理员
    @ResponseBody
    @RequestMapping(value = "judgeexammanager.vpage", method = RequestMethod.GET)
    public MapMessage judgeExamManager() {
        User user = currentUser();

        School school = currentSchool();
        if (school == null) {
            return MapMessage.errorMessage("用户没有指定学校");
        }

        if (teacherRolesServiceClient.hasRole(user.getId(), school.getId(), TeacherRoleCategory.O2O.name(), TeacherRolesType.EXAM_MANAGER.name())) {
            return MapMessage.successMessage();
        }

        return MapMessage.errorMessage();
    }


    //  ========================================= 其他方法 =========================================
    @RequestMapping(value = "gettemplate.vpage", method = RequestMethod.GET)
    public void getImportTemplate() {
        School school = currentSchool();
        if (school == null) {
            return;
        }
        String templateId = getRequestString("template");
        ExcelTemplate template = ExcelTemplate.safeParse(templateId);
        if (template == null) {
            return;
        }
        // 根据初高中重新调整一下
        if (template == ExcelTemplate.CREATE_TEACHER_CLASS_MIDDLE && school.isSeniorSchool()) {
            template = ExcelTemplate.CREATE_TEACHER_CLASS_SENIOR;
        }
        String fileName = template.getFileName();
        String filePath = template.getTemplatePath();
        try {
            Resource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                logger.error("download special teacher  template {} failed - template not exists", filePath);
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);

            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download special teacher template {} failed - ExMsg : {};", filePath, e);
        }
    }

    /**
     * 修改密码后更新学生App的session key
     */
    private void updateAppSessionKeyForStudent(Long studentId) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", studentId);
        if (vendorAppsUserRef != null) {
            vendorServiceClient.expireSessionKey(
                    "17Student",
                    studentId,
                    SessionUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), studentId));
        }
    }

    private MapMessage importKlxStudents(Map<String, String> stuInfoMap, List<ImportStudentData> studentData, boolean checkRepeatStudent, boolean checkTakeUpStudent, boolean checkDeleteStudent, boolean recoverStudent, Long clazzId, Long teacherId) {
        List<KlxStudent> klxStudents = newKuailexueLoaderClient.loadKlxTeacherClazzStudents(teacherId, clazzId);
        Set<Integer> duplicateNumberList = new TreeSet<>();  // 重复学号的行数
        Map<String, KlxStudent> existNumbers;
        try {
            existNumbers = klxStudents.stream()
                    .filter(klxStudent -> StringUtils.isNotBlank(klxStudent.getStudentNumber()))
                    .collect(Collectors.toMap(KlxStudent::getStudentNumber, Function.identity(), (u, v) -> {
                        logger.error("Duplicate klxStudent StudentNumber found, please check it . studentId=({} , {}), studentNumber={}", u.getId(), v.getId(), u.getStudentNumber());
                        throw new IllegalArgumentException("班级内学生[" + u.getName() + "、" + v.getName() + "]学号[" + u.getStudentNumber() + "]重复，请先处理");
                    }, LinkedHashMap::new));
        } catch (IllegalArgumentException ex) {
            return MapMessage.errorMessage(ex.getMessage());
        }

        for (ImportStudentData data : studentData) {
            String stuNum = data.getStudentNumber();
            if (!existNumbers.containsKey(stuNum) || existNumbers.get(stuNum) == null) {
                continue;
            }
            // 姓名和填涂号都相同的话可以忽略
            if (!StringUtils.equals(data.getStudentName(), existNumbers.get(stuNum).getName())) {
                duplicateNumberList.add(data.getRow());
            }
        }

        if (CollectionUtils.isNotEmpty(duplicateNumberList)) {
            List<String> lines = duplicateNumberList.stream()
                    .map(line -> "第" + line + "行")
                    .collect(Collectors.toList());
            return MapMessage.errorMessage("导入的学生学号在班内重复，位于{}", StringUtils.join(lines, "、"));
        }

        //检查班内重名学生 无论是否有重复学生,一定会返回前端
        if (checkRepeatStudent) {
            //如果没有重复学生直接导入,否则返回重复的学生姓名
            List<String> repeatedStudentList = new ArrayList<>();

            Map<String, KlxStudent> existNames;
            try {
                existNames = klxStudents.stream()
                        .filter(klxStudent -> StringUtils.isNotBlank(klxStudent.realName()))
                        .collect(Collectors.toMap(KlxStudent::realName, Function.identity(), (u, v) -> {
                            logger.error("Duplicate klxStudent StudentName found, please check it . studentId=({} , {}), studentName={}", u.getId(), v.getId(), u.getName());
                            throw new IllegalArgumentException("班级内学生[" + u.getName() + "、" + v.getName() + "]姓名重复，请先处理");
                        }, LinkedHashMap::new));
            } catch (IllegalArgumentException ex) {
                return MapMessage.errorMessage(ex.getMessage());
            }

            for (ImportStudentData data : studentData) {
                String stuName = data.getStudentName();
                if (!existNames.containsKey(stuName) || existNames.get(stuName) == null) {
                    continue;
                }
                // 姓名和填涂号都相同的话可以忽略
                if (!StringUtils.equals(data.getStudentNumber(), existNames.get(stuName).getStudentNumber())) {
                    repeatedStudentList.add(stuName);
                }
            }

            // #55742 group内人数是否>100人 提示文案
            int count = 0;
            for (KlxStudent klxStudent : klxStudents) {
                if (stuInfoMap.keySet().contains(klxStudent.getName())) {
                    count++;
                }
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if ((klxStudents.size() + stuInfoMap.size() - count) > globalTagServiceClient.getGlobalTagBuffer()
                    .loadSchoolMaxClassCapacity(clazz.getSchoolId(), ClazzConstants.MAX_CLAZZ_CAPACITY)) {
                return MapMessage.errorMessage("班级学生数不能超过100人");
            }
            if (CollectionUtils.isNotEmpty(repeatedStudentList)) { // 如果有重名学生
                boolean moreStudent = false;
                if (repeatedStudentList.size() > 3) {
                    repeatedStudentList = repeatedStudentList.subList(0, 3);
                    moreStudent = true;
                }
                return MapMessage.successMessage().add("repeatedStudentList", repeatedStudentList).add("moreStudent", moreStudent);
            }
            return MapMessage.successMessage();
        }

        // 根据学号后N位,找到占用的学生班级老师信息;如果有占用的话,直接返回;否则,直接导入学生
        if (checkTakeUpStudent) {
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            Long schoolId = clazz == null ? null : clazz.getSchoolId();
            if (schoolId == null) {
                return MapMessage.errorMessage("学校不存在");
            }

            // 找到被占用的学生信息
            List<TakeUpKlxStudent> takeUpKlxStudents = newKuailexueLoaderClient.pickTakeUpKlxStudents(schoolId, teacherId, clazzId, stuInfoMap, 3);

            // 处理前端弹窗跳转
            if (CollectionUtils.isNotEmpty(takeUpKlxStudents)) {
                List<String> importNames = new ArrayList<>();
                for (TakeUpKlxStudent student : takeUpKlxStudents) {
                    String studentNumber = student.getOldStudentNumber();
                    stuInfoMap.forEach((name, number) -> {
                        if (Objects.equals(number, studentNumber)) {
                            importNames.add(name);
                        }
                    });
                }
                return MapMessage.successMessage().add("isTakeUp", true)
                        .add("importNames", importNames.size() > 3 ? importNames.subList(0, 3) : importNames)
                        .add("moreFlag", importNames.size() >= 3)
                        .add("takeUpInfo", takeUpKlxStudents);
            }
            return MapMessage.successMessage();
        }

        // 删除历史同名学生校验
        // 需要恢复的学生group
        Map<Long, List<Long>> recoverGroups = new HashMap<>();
        Map<String, String> recoverStudentNumber = new HashMap<>();
        Map<String, List<String>> deleteHistorySameName = getDeleteHistorySameName(stuInfoMap, clazzId, recoverGroups, recoverStudentNumber, klxStudents);
        List<String> sameNames = deleteHistorySameName.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        int recoverNum = sameNames.size();
        if (checkDeleteStudent) {
            if (!sameNames.isEmpty()) {
                return MapMessage.successMessage().add("klxDeleteSameName", true)
                        .add("deleteSameStudents", deleteHistorySameName);
            }
        }

        if (recoverStudent) {
            // 恢复同名学生删除之前的所有的组
            List<Long> groupKlxStudentRefs = recoverGroups.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            if (!recoverGroups.keySet().isEmpty()) {
                newKuailexueServiceClient.recoverGroupKlxStudentRefs(recoverGroups.keySet().stream().findFirst().get(), recoverStudentNumber, groupKlxStudentRefs);
            }
            // 移除恢复同名的学生
            deleteHistorySameName.values().stream().flatMap(Collection::stream).collect(Collectors.toList()).forEach(stuInfoMap::remove);
        }
        //处理本班级下的所有班组中(当前班组除外) 校内学号+姓名完全匹配的学生
        if (stuInfoMap.isEmpty()) {
            return MapMessage.successMessage().add("newSignNum", 0)
                    .add("updateNum", 0).add("recoverNum", recoverNum);
        }

        MapMessage mapMessage = newKuailexueServiceClient.batchImportKlxStudents(teacherId, clazzId, stuInfoMap, KlxStudent.ImportSource.administrativeteacher);
        if (recoverStudent) {
            mapMessage.add("recoverNum", recoverNum).add("klxDeleteSameName", false);
        }
        return mapMessage;
    }

    private Map<String, List<String>> getDeleteHistorySameName(Map<String, String> stuInfoMap, Long clazzId, Map<Long, List<Long>> recoverGroups, Map<String, String> recoverStudentNumber, List<KlxStudent> klxStudents) {
        // 查询该班下所有的组
        Clazz currentClazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        List<Group> groups = clazzGroupServiceClient.getGroupService().findGroupsByClazzId(clazzId).getUninterruptibly();
        List<GroupKlxStudentRef> deleteGroupKlxStudentRefs = new ArrayList<>();
        groups.forEach(g -> deleteGroupKlxStudentRefs.addAll(asyncGroupServiceClient.getAsyncGroupService().loadDeletedGroupKlxStudentRefs(g.getId()).getUninterruptibly()));

        Set<String> klxStudentIds = deleteGroupKlxStudentRefs.stream().map(GroupKlxStudentRef::getKlxStudentId).collect(Collectors.toSet());
        Map<String, List<GroupKlxStudentRef>> groupKlxStudentRefMap = asyncGroupServiceClient.getAsyncGroupService().findGroupKlxStudentRefsByStudents(klxStudentIds).getUninterruptibly();
        // 过滤掉有组的学生
        Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(klxStudentIds.stream().filter(s -> groupKlxStudentRefMap.get(s).isEmpty()).collect(Collectors.toSet()));
        // 重名恢复取最近更新的
        Map<String, KlxStudent> klxStudentNameMap = klxStudentMap.values().stream().collect(Collectors.toMap(KlxStudent::getName, Function.identity(), (u, v) -> {
            if (u.getUpdateTime().after(v.getUpdateTime())) {
                return u;
            }
            return v;
        }));
        // 学生的所有组，恢复的时候恢复所有的学生之前所在的组
        Map<String, List<GroupKlxStudentRef>> klxStudentRefIdsMap = deleteGroupKlxStudentRefs.stream().collect(Collectors.groupingBy(GroupKlxStudentRef::getKlxStudentId));
        // 班内所有的学生按姓名分组
        Map<String, KlxStudent> existNames = klxStudents.stream()
                .filter(klxStudent -> StringUtils.isNotBlank(klxStudent.realName()))
                .collect(Collectors.toMap(KlxStudent::realName, Function.identity(), (u, v) -> u));
        // 去掉班级里面的同名和删除列表
        List<KlxStudent> deletedStudents = klxStudentNameMap.values().stream().filter(s -> !existNames.containsKey(s.getName())).collect(Collectors.toList());
        // 与删除历史记录相同的学生
        Map<String, List<String>> deleteSameStudents = new HashMap<>();
        List<String> deleteSameStudentList = new ArrayList<>();
        deletedStudents.forEach(k -> {
            if (stuInfoMap.containsKey(k.getName())) {
                deleteSameStudentList.add(k.getName());
                // id 和 学号
                recoverStudentNumber.put(k.getId(), stuInfoMap.get(k.getName()));
                List<GroupKlxStudentRef> groupKlxStudentRefs = klxStudentRefIdsMap.get(k.getId());
                Map<Long, GroupKlxStudentRef> groupIds = groupKlxStudentRefs.stream().collect(Collectors.toMap(GroupKlxStudentRef::getGroupId, Function.identity(), (u, v) -> u));
                for (Map.Entry<Long, GroupKlxStudentRef> entry : groupIds.entrySet()) {
                    if (recoverGroups.containsKey(entry.getKey())) {
                        List<Long> refIds = recoverGroups.get(entry.getKey());
                        refIds.add(entry.getValue().getId());
                        recoverGroups.put(entry.getKey(), refIds);
                        continue;
                    }
                    List<Long> list = new ArrayList<>();
                    list.add(entry.getValue().getId());
                    recoverGroups.put(entry.getKey(), list);
                }
            }
        });
        deleteSameStudents.put(currentClazz.formalizeClazzName(), deleteSameStudentList);
        return deleteSameStudents;
    }

    private MapMessage checkImportExcel(Workbook workbook) {
        if (workbook == null) {
            return MapMessage.errorMessage("文档解析错误");
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage("excel文件中的sheet1内容不能为空");
        }

        int trLength = sheet.getLastRowNum() + 1;//总行数(包括首行)

        if (trLength < 2) {//excel第二行及以下的A列为空时，报错——excel为空
            return MapMessage.errorMessage("excel为空");
        }

        List<StudentImportData> importData = parseStudentImportData(sheet);
        if (importData.isEmpty()) {
            return MapMessage.errorMessage("excel为空");
        }

        if (trLength > 151) {//excel内容>151行时，报错——导入学生不能超过150人
            return MapMessage.errorMessage("导入学生不能超过150人");
        }

        List<String> errorList = new ArrayList<>();
        Map<String, String> stuInfoMap = new HashMap<>();
        List<ImportStudentData> studentData = new LinkedList<>();
        Map<String, List<Integer>> studentNames = new LinkedHashMap<>();
        Map<String, List<Integer>> studentNumbers = new LinkedHashMap<>();
        for (int rowIndex = 1; rowIndex < trLength; ++rowIndex) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            String studentName = "";
            String studentNumber = "";

            Cell studentNameCell = row.getCell(0);
            if (studentNameCell != null) {
                studentNameCell.setCellType(Cell.CELL_TYPE_STRING);
                studentName = StringUtils.replaceAll(studentNameCell.getStringCellValue(), "\\s*", "");
            }

            Cell studentNumberCell = row.getCell(1);
            if (studentNumberCell != null) {
                studentNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                studentNumber = StringUtils.replaceAll(studentNumberCell.getStringCellValue(), "\\s*", "");
            }

            // 读到了一行空数据, 忽略咯
            if (StringUtils.isBlank(studentName) && StringUtils.isBlank(studentNumber)) continue;

            stuInfoMap.put(studentName, studentNumber);
            studentData.add(new ImportStudentData(studentName, studentNumber, rowIndex + 1));
            errorList.addAll(checkLine(studentName, studentNumber, rowIndex + 1));
            collect(studentNames, studentName, rowIndex + 1);
            collect(studentNumbers, studentNumber, rowIndex + 1);
        }

        // 收集错误信息
        for (Map.Entry<String, List<Integer>> entry : studentNames.entrySet()) {
            if (entry.getValue().size() > 1) {
                errorList.add("表格中学生姓名【" + entry.getKey() + "】重复，位于第" + StringUtils.join(entry.getValue(), "、") + "行");
            }
        }
        for (Map.Entry<String, List<Integer>> entry : studentNumbers.entrySet()) {
            if (entry.getValue().size() > 1) {
                errorList.add("表格中学生学号【" + entry.getKey() + "】重复，位于第" + StringUtils.join(entry.getValue(), "、") + "行");
            }
        }

        if (errorList.size() == 0) {
            return MapMessage.successMessage().add("stuInfoMap", stuInfoMap).add("studentData", studentData);
        }

        if (errorList.size() > 5) {
            errorList = errorList.subList(0, 5);
        }
        return MapMessage.errorMessage(StringUtils.join(errorList, "<br/>"));
    }

    private void sendMessage(Long receiverId, String content) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        content = StringUtils.replace(content, "老师老师", "老师");
        teacherLoaderClient.sendTeacherMessage(receiverId, content);
    }

    private List<String> checkLine(String studentName, String studentNumber, int rowIndex) {
        List<String> errorList = new ArrayList<>();
        if (!StringUtils.isNumeric(studentNumber)) {
            errorList.add("校内学号请填写数字,位于第" + rowIndex + "行");
        }
        if (studentNumber.length() > 14) {
            errorList.add("填写的校内学号过长,位于第" + rowIndex + "行");
        }
        if (StringUtils.isBlank(studentName)) {
            errorList.add("学生姓名不能为空，位于第" + rowIndex + "行");
        }
        if (studentName.length() > 12) {
            errorList.add("填写的学生名过长，位于第" + rowIndex + "行");
        }
        return errorList;
    }

    private void collect(Map<String, List<Integer>> map, String key, Integer value) {
        map.computeIfAbsent(key, k -> new ArrayList<>());
        map.get(key).add(value);
    }

    @Getter
    @Setter
    private static class ImportStudentData {
        private String studentName;
        private String studentNumber;
        private int row;

        ImportStudentData(String studentName, String studentNumber, int row) {
            this.studentName = studentName;
            this.studentNumber = studentNumber;
            this.row = row;
        }

    }

    ExcelExportData toExcelExportData(Map<Long, List<KlxStudent>> map, String sheetName, Map<Long, Clazz> clazzMap) {

        String[] title = new String[]{"班级", "学生姓名", "校内学号", "阅卷机填涂号", "一起作业ID", "手机号"};
        int[] width = new int[]{6000, 6000, 6000, 6000, 6000, 6000};

        List<List<String>> data = new ArrayList<>();
        for (Long clazzId : map.keySet()) {
            List<KlxStudent> klxStudents = map.get(clazzId);
            if (CollectionUtils.isNotEmpty(klxStudents)) {
                Clazz clazz = clazzMap.get(clazzId);
                String clazzName = clazz.getClazzLevel().getDescription() + clazz.getClassName();
                for (KlxStudent klxStudent : klxStudents) {
                    String name = StringUtils.isBlank(klxStudent.getName()) ? "未添加" : klxStudent.getName();
                    String studentNumber = StringUtils.isBlank(klxStudent.getStudentNumber()) ? "未添加" : klxStudent.getStudentNumber();
                    String scanNumber = StringUtils.isBlank(klxStudent.getScanNumber()) ? "未添加" : klxStudent.getScanNumber();
                    String userId = klxStudent.isRealStudent() ? klxStudent.getA17id().toString() : "未注册";
                    String mobile;
                    if (klxStudent.isRealStudent()) {
                        mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(klxStudent.getA17id());
                    } else {
                        mobile = sensitiveUserDataServiceClient.loadKlxStudentMobileObscured(klxStudent.getId());
                    }
                    data.add(Arrays.asList(clazzName, name, studentNumber, scanNumber, userId, mobile));
                }
            }
        }
        return new ExcelExportData(sheetName, title, width, data, 6);
    }

    /**
     * 查询需要合并的学生
     * 姓名相同，其中一个有一起作业ID+手机，另一个阅卷机填涂号。
     *
     * @param klxStudents
     * @return
     */
    private Map<String, List<KlxStudent>> getMergeSameNameStudent(List<KlxStudent> klxStudents) {
        Map<String, List<KlxStudent>> mergeStudentsNameMap = new HashMap<>();
        if (klxStudents == null || klxStudents.isEmpty()) {
            return mergeStudentsNameMap;
        }
        Map<String, List<KlxStudent>> klxNamesMap = klxStudents.stream().collect(Collectors.groupingBy(KlxStudent::getName));
        for (Map.Entry<String, List<KlxStudent>> entry : klxNamesMap.entrySet()) {
            List<KlxStudent> value = entry.getValue();
            if (value.size() <= 1) {
                continue;
            }
            List<KlxStudent> studentsHas17Id = value.stream().filter(s ->
                    (s.getA17id() != null && StringUtils.isNotBlank(sensitiveUserDataServiceClient.loadUserMobileObscured(s.getA17id())) && (StringUtils.isBlank(s.getStudentNumber()) && StringUtils.isBlank(s.getScanNumber())))
            ).collect(Collectors.toList());
            List<KlxStudent> students = value.stream().filter((s -> (!(StringUtils.isBlank(s.getStudentNumber()) && StringUtils.isBlank(s.getScanNumber())) && s.getA17id() == null && StringUtils.isBlank(sensitiveUserDataServiceClient.loadKlxStudentMobileObscured(s.getId())))))
                    .collect(Collectors.toList());
            if (students.size() >= 1 && studentsHas17Id.size() >= 1) {
                students.addAll(studentsHas17Id);
                mergeStudentsNameMap.put(entry.getKey(), students);
            }
        }
        return mergeStudentsNameMap;
    }

    private List<StudentImportData> parseStudentImportData(Sheet sheet) {
        List<StudentImportData> data = new LinkedList<>();
        // 从第一行开始
        for (int index = 1; index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }
            String grade = WorkbookUtils.getCellValue(row.getCell(0));
            String className = WorkbookUtils.getCellValue(row.getCell(1));
            String studentName = WorkbookUtils.getCellValue(row.getCell(2));
            String studentNumber = WorkbookUtils.getCellValue(row.getCell(3));

            // 读到了一个空行，忽略吧
            if (StringUtils.isBlank(grade) && StringUtils.isBlank(className) && StringUtils.isBlank(studentName) && StringUtils.isBlank(studentNumber)) {
                continue;
            }
            data.add(new StudentImportData(index, grade, className, studentName, studentNumber));
        }
        return data;
    }

}
