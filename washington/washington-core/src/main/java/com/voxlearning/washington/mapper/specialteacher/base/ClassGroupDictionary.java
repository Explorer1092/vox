package com.voxlearning.washington.mapper.specialteacher.base;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.KlxStudent;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 打散换班、复制教学班等功能用的基础数据
 * Created by Yuechen.Wang on 2017/8/28.
 */
public class ClassGroupDictionary implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ClassGroupDictionary.class);

    private static final long serialVersionUID = 4529580904438587292L;

    /**
     * 班级基础数据
     */
    private List<Clazz> clazzList = new LinkedList<>();

    /**
     * 班级 ID-Clazz 字典
     * key   : clazzId
     * value : Clazz 实体
     */
    private Map<Long, Clazz> clazzMap = new HashMap<>();

    /**
     * 班级 Key-ClazzId 字典
     * key   : clazzLevel_clazzName
     * value : clazzId
     */
    private Map<String, Long> clazzNameMap = new HashMap<>();

    /**
     * 班级下所有组
     * key   : clazzId
     * value : list of GroupMapper
     */
    private Map<Long, List<Group>> groupMap = new HashMap<>();

    /**
     * 所有分组对应的快乐学学生
     */
    private Map<Long, List<KlxStudent>> klxGroupStudents = new HashMap<>();

    /**
     * 快乐学学生字典
     * key   : klxStudentId
     * value : klxStudent
     */
    private Map<String, KlxStudent> klxStudentMap = new HashMap<>();

    /**
     * 年级内有重名的学生
     * key   : clazzLevel_studentName
     * value : klxStudentIds
     */
    private Map<String, Set<KlxStudentClazz>> dupKlxStudentInfo = new HashMap<>();

    /**
     * 学生信息
     * key   : clazzLevel_studentName
     * value : klxStudentId
     */
    private Map<String, KlxStudentClazz> klxStudentInfo = new HashMap<>();


    //=================================================================================================
    //==========================               以下是初始化方法              ============================
    //=================================================================================================

    public static ClassGroupDictionary newInstance() {
        return new ClassGroupDictionary();
    }

    public ClassGroupDictionary initClazzList(List<Clazz> clazzList) {
        this.clazzList = clazzList;
        this.clazzMap = clazzList.stream().collect(Collectors.toMap(Clazz::getId, Function.identity(), (u, v) -> u, LinkedHashMap::new));

        this.clazzNameMap = clazzList.stream().collect(Collectors.toMap(p -> StringUtils.join(p.getClassLevel(), "_", p.getClassName()), Clazz::getId,
                (u, v) -> {
                    logger.warn("Duplicate ClazzName Found For TeacherClazzController/buildParams, clazzId=({} , {})", u, v);
                    return u;
                }, LinkedHashMap::new));
        return this;
    }

    public ClassGroupDictionary initGroups(Map<Long, List<Group>> groupMap) {
        this.groupMap = groupMap;
        return this;
    }

    public ClassGroupDictionary initStudents(Map<Long, List<KlxStudent>> klxGroupStudents) {
        this.klxGroupStudents = klxGroupStudents;
        return this;
    }

    public ClassGroupDictionary buildParam() {
        for (Long clazzId : clazzMap.keySet()) {
            buildGroup(clazzId);
        }
        return this;
    }

    //==================================================================================================
    //==========================               以下是一些判断方法            =============================
    //==================================================================================================

    public Map<String, Long> clazzNameData() {
        return clazzList.stream().collect(Collectors.toMap(p -> StringUtils.join(p.getClazzLevel().getDescription(), "_", p.getClassName()), Clazz::getId,
                (u, v) -> {
                    logger.warn("Duplicate ClazzName Found For TeacherClazzController/buildParams, clazzId=({} , {})", u, v);
                    return u;
                }, LinkedHashMap::new));
    }

    /**
     * 判断 班级ID 是否存在
     *
     * @return true -> 存在; false -> 不存在
     */
    public boolean existClass(Long clazzId) {
        return clazzId != null && clazzMap.containsKey(clazzId);
    }

    /**
     * 判断班级下是否有班组
     *
     * @return true -> 没有班组;  false -> 有班组
     */
    public boolean checkNoneGroup(Long clazzId) {
        return clazzId != null && CollectionUtils.isEmpty(groupMap.get(clazzId));
    }

    public boolean emptyKlxStudentInfo() {
        return MapUtils.isEmpty(klxStudentMap);
    }

    public boolean containsDuplicateKlxStudents(String studentKey) {
        return dupKlxStudentInfo.containsKey(studentKey);
    }

    //==================================================================================================
    //==========================               以下是一些查询方法            =============================
    //==================================================================================================

    /**
     * 取出所有班级
     */
    public List<Clazz> queryAllClazz() {
        return clazzList;
    }

    /**
     * 根据 班级信息 找到 班级
     */
    public Clazz queryClazzByKey(String classKey) {
        Long clazzId = clazzNameMap.get(classKey);
        return clazzMap.get(clazzId);
    }

    /**
     * 根据 班级信息 找到 班级
     */
    public Clazz queryClazzById(Long clazzId) {
        return clazzMap.get(clazzId);
    }

    /**
     * 班级下组的重复学科
     */
    public Set<Subject> queryDuplicateSubjects(Long clazzId) {
        List<Group> groups = groupMap.get(clazzId);
        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptySet();
        }
        Set<Subject> duplicate = new HashSet<>();
        groups.stream().collect(Collectors.groupingBy(Group::getSubject))
                .forEach((sub, g) -> {
                    if (g.size() > 1) duplicate.add(sub);
                });
        return duplicate;
    }

    /**
     * 班级下的所有组ID
     */
    public List<Group> queryClazzGroups(Long clazzId) {
        if (clazzId == null) {
            return Collections.emptyList();
        }
        return groupMap.get(clazzId);
    }

    /**
     * 教学班班级下的组
     */
    public Group queryWalkingClazzGroup(Long clazzId) {
        if (clazzId == null) {
            return null;
        }
        Clazz clazz = queryClazzById(clazzId);
        if (clazz == null || !clazz.isWalkingClazz()) {
            return null;
        }
        return groupMap.getOrDefault(clazzId, Collections.emptyList())
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 班级下的所有组ID
     */
    public Set<Long> queryClazzGroupIds(String classKey) {
        Long clazzId = clazzNameMap.get(classKey);
        if (clazzId == null) {
            return Collections.emptySet();
        }
        List<Group> groups = groupMap.get(clazzId);
        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptySet();
        }
        return groups.stream().map(Group::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * 根据 学生信息 找到 学生ID
     */
    public KlxStudentClazz queryKlxStudentId(String studentKey) {
        return klxStudentInfo.getOrDefault(studentKey, null);
    }

    /**
     * 根据 学生ID 找到 学生实体
     */
    public KlxStudent queryKlxStudent(String klxStudentId) {
        if (StringUtils.isBlank(klxStudentId)) {
            return null;
        }
        return klxStudentMap.getOrDefault(klxStudentId, null);
    }

    /**
     * 根据 学生信息 找到 重复的学生ID集合
     */
    public Set<KlxStudentClazz> queryDuplicateKlxStudents(String studentKey) {
        return dupKlxStudentInfo.getOrDefault(studentKey, new HashSet<>());
    }

    //=================================================================================================
    //==========================               以下是  私有方法            =============================
    //=================================================================================================

    private void buildGroup(Long clazzId) {
        Clazz clazz = clazzMap.get(clazzId);
        List<Group> groups = groupMap.get(clazzId);
        if (clazz == null || CollectionUtils.isEmpty(groups)) {
            return;
        }
        List<Long> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());
        for (Long groupId : groupIds) {
            List<KlxStudent> klxStudents = klxGroupStudents.get(groupId);
            if (CollectionUtils.isEmpty(klxStudents)) {
                continue;
            }
            for (KlxStudent klxStudent : klxStudents) {
                klxStudentMap.put(klxStudent.getId(), klxStudent);
                String stuKey = genStudentKey(clazz, klxStudent.getName());
                if (klxStudentInfo.containsKey(stuKey)) {
                    KlxStudentClazz klxStu = klxStudentInfo.get(stuKey);
                    if (!Objects.equals(klxStu.getKlxId(), klxStudent.getId())) {
                        Set<KlxStudentClazz> dupStuIds = dupKlxStudentInfo.get(stuKey);
                        if (dupStuIds == null) {
                            dupStuIds = new HashSet<>();
                        }
                        KlxStudentClazz klxStudentClazz = new KlxStudentClazz();
                        klxStudentClazz.setClazz(clazz);
                        klxStudentClazz.setKlxId(klxStudent.getId());
                        dupStuIds.add(klxStudentClazz);
                        dupKlxStudentInfo.put(stuKey, dupStuIds);
                    }
                } else {
                    KlxStudentClazz klxStudentClazz = new KlxStudentClazz();
                    klxStudentClazz.setClazz(clazz);
                    klxStudentClazz.setKlxId(klxStudent.getId());
                    klxStudentInfo.put(stuKey, klxStudentClazz);
                }
            }
        }
    }

    private String genStudentKey(Clazz clazz, String name) {
        return StringUtils.join(clazz.getClassLevel(), "_", name);
    }

}
