/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.BookStatus;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.business.api.TeacherClazzService;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.ClazzBookRef;
import com.voxlearning.utopia.service.content.api.mapper.ClazzEnglishBook;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;

@Named
@Service(interfaceClass = TeacherClazzService.class)
@ExposeService(interfaceClass = TeacherClazzService.class)
public class TeacherClazzServiceImpl extends BusinessServiceSpringBean implements TeacherClazzService {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public boolean upgradeClazzBook(final Long clazzId, final Teacher teacher) {
        if (teacher == null) {
            logger.warn("班级老师不存在");
            return false;
        }

        final Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            logger.warn(clazzId + "班级不存在,退出课本升级");
            return false;
        }

        final GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);
        final Long groupId = group == null ? null : group.getId();

        final List<Book> currentBooks = ClazzEnglishBook
                .toBookList(clazzBookLoaderClient.loadGroupEnglishBooks(groupId));
        if (currentBooks.isEmpty()) {
            logger.warn("此班级" + clazzId + "没有课本");
            return false;
        }

        try {
            utopiaSql.withTransaction(new TransactionCallbackWithoutResult() {

                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    //获得版本系列和BookId的集合
                    Map<Long, Book> newBooks = new TreeMap<>(); //按id排序
                    Set<String> pressSet = new HashSet<>();
                    for (Book book : currentBooks) {
                        newBooks.put(book.getId(), book);
                        pressSet.add(book.getPress());
                    }

                    //根据版本系列和rank获得新课本
                    int rank = getBookRank(conversionService.convert(clazz.getClassLevel(), Integer.class),
                            SchoolYear.newInstance().currentTerm().getKey());
                    for (String press : pressSet) {
                        List<Book> books = englishContentLoaderClient.loadEnglishBooks()
                                .enabled()
                                .filter(t -> StringUtils.equals(t.getPress(), press))
                                .clazzLevel_termType_ASC()
                                .toList();
                        for (Book book : books) {
                            if (rank == SafeConverter.toInt(book.getRank())) {
                                newBooks.put(book.getId(), book);
                                break;
                            }
                        }
                    }

                    //升级必修课本
                    ClazzEnglishBook clazzEnglishBook = clazzBookLoaderClient.loadGroupCompulsoryEnglishBook(groupId);
                    Book compulsoryTextBook = clazzEnglishBook == null ? null : clazzEnglishBook.getBook();
                    Book newCompulsoryTextBook = null;
                    if (compulsoryTextBook != null) {
                        List<Book> bl = englishContentLoaderClient.loadEnglishBooks()
                                .enabled()
                                .filter(t -> StringUtils.equals(t.getPress(), compulsoryTextBook.getPress()))
                                .clazzLevel_termType_ASC()
                                .toList();
                        for (Book book : bl) {
                            if (rank == SafeConverter.toInt(book.getRank())) {
                                newCompulsoryTextBook = book;
                                break;
                            }
                        }
                        if (newCompulsoryTextBook != null && newCompulsoryTextBook.fetchBookStatus() == BookStatus.ONLINE) {
                            newBooks.put(newCompulsoryTextBook.getId(), newCompulsoryTextBook);
                        } else {
                            newCompulsoryTextBook = compulsoryTextBook;
                            logger.warn(clazz.getId() + "班必修课本未找到，无法升级必修课本");
                        }
                    }

                    //排除已下线的课本
                    List<Book> newBookList = newBooks.values().stream().filter(source -> source.fetchBookStatus() == BookStatus.ONLINE)
                            .collect(Collectors.toList());
                    changeClazzBooks(newBookList, newCompulsoryTextBook, clazzId, teacher, groupId);
                }
            });
        } catch (Exception e) {
            logger.error("课本升级失败 -> " + e.getMessage(), e);
            return false;
        }
        return true;

    }

    private void changeClazzBooks(List<Book> books, Book compulsoryTextBook, Long clazzId, Teacher teacher, Long groupId) {
        //构建bookJson
        Set<Long> bookIds = new HashSet<>();
        for (Book book : books) {
            bookIds.add(book.getId());
        }

        //处理必修课本
        Long mastBookId;
        if (compulsoryTextBook != null) {
            mastBookId = compulsoryTextBook.getId();
        } else {
            mastBookId = books.get(0).getId();
        }

        //删除班级学科的所有课本，并添加新的课本
        if (clazzId != null) {
            bookIds.stream()
                    .map(t -> {
                        ClazzBookRef.Location id = new ClazzBookRef.Location();
                        id.setGroupId(SafeConverter.toLong(groupId));
                        id.setClazzId(SafeConverter.toLong(clazzId));
                        id.setBookId(SafeConverter.toLong(t));
                        id.setSubject(teacher.getSubject() == null ? null : teacher.getSubject().name());
                        return id;
                    })
                    .forEach(clazzBookServiceClient::deleteClazzBook);

            ChangeBookMapper mapper = new ChangeBookMapper();
            mapper.setBooks(StringUtils.join(bookIds, ","));
            mapper.setClazzs(clazzId.toString());
            mapper.setType(0);

            contentServiceClient.setClazzBook(teacher, mapper);
        }
    }

    private Integer getBookRank(Integer classLevel, Integer termType) {
        if (termType == null || classLevel == null || termType < 0 || termType > 2)
            return 0;
        //针对于TermType=0的课本，此类课本一本书使用一学年，因此他的rank值就是他的CLASS_LEVEL
        if (termType == 0) {
            return classLevel;
        }
        //（年级-1）*2 + （上：1/下学期：2）
        int rank = 2 * (classLevel - 1) + termType;
        //20是最高的rank，指54年制的初四年级，2*(10-1)+2 = 20
        if (rank < 0 || rank > 20)
            return 0;
        return rank;
    }

    // ---------------------------------------------------------------------------------------------------------------

    private List<Map<String, Object>> separateForWechat(Teacher teacher, Long schoolId, ClazzLevel level, final String clazzName) {
        // 查询学校中指定年级中的所有班级，并找出指定名字的班级
        List<Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled()
                .clazzLevel(level)
                .toList()
                .stream()
                .filter(t -> StringUtils.equals(t.getClassName(), clazzName))
                .collect(Collectors.toList());
        // 分成有效列表和无效列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (Clazz clazz : clazzList) {
            List<Teacher> teachers = ClazzTeacher.toTeacherList(teacherLoaderClient.loadClazzTeachers(clazz.getId()));
            //没有同科老师就可以加入
            boolean valid = noSameSubjectTeacher(teachers, teacher.getSubject());
            if (valid) {
                result.add(this.convert(clazz, teachers));
            }
        }
        return result;
    }

    private boolean noSameSubjectTeacher(List<Teacher> teachers, Subject subject) {
        for (Teacher teacher : teachers) {
            if (teacher.getSubject() == subject) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Object> convert(Clazz clazz, List<Teacher> teachers) {
        Map<String, Object> clazzMap = new HashMap<>();
        // 班级信息
        clazzMap.put("clazzId", clazz.getId());
        clazzMap.put("clazzName", clazz.formalizeClazzName());
        // by changyuan.liu 天津新体系
        clazzMap.put("creatorType", clazz.getCreateBy());
        // 教师信息
        List<Map<String, Object>> teacherList = new ArrayList<>();
        for (Teacher teacher : teachers) { // 只显示姓，是否认证，学科，教师Id
            Map<String, Object> teacherMap = new HashMap<>();
            teacherMap.put("teacherId", teacher.getId());
            teacherMap.put("teacherSubject", teacher.getSubject());
            teacherMap.put("teacherSubjectName", teacher.getSubject() == null ? "" : teacher.getSubject().getValue());
            teacherMap.put("teacherAuth", teacher.fetchCertificationState() == SUCCESS);
            teacherMap.put("teacherName", StringUtils.substring(teacher.getProfile().getRealname(), 0, 1));
            teacherList.add(teacherMap);
        }
        clazzMap.put("teachers", teacherList);
        // 学生信息
        List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazz.getId());
        List<User> students = new ArrayList<>(userLoaderClient.loadUsers(studentIds).values());
        clazzMap.put("studentCount", students.size());
        String nameStr = "";
        for (int i = 0, j = 0; (i < students.size() && j < 3); i++) {
            User student = students.get(i);
            if (null != student && StringUtils.isNotBlank(student.getProfile().getRealname())) {
                nameStr += student.getProfile().getRealname() + ",";
                j++;
            }
        }
        if (nameStr.length() > 1) {
            nameStr = students.size() > 3 ? "<strong>" + nameStr.substring(0, nameStr.length() - 1) + "</strong> 等" : "<strong>" + (nameStr.substring(0, nameStr.length() - 1) + "</strong> ");
            nameStr = nameStr + "在此班级学习！";
        } else {
            nameStr = "该班级未上传学生名单";
        }
        clazzMap.put("nameStr", nameStr);

        return clazzMap;
    }

//    /**
//     * 查询重名班级，用于判断创建还是加入班级
//     */
//    @Override
//    public MapMessage findClazzWithSameName(Teacher teacher, String clazzLevel, final String[] clazzNames) {
//        if (null == teacher || StringUtils.isBlank(clazzLevel) || clazzNames.length == 0) {
//            return MapMessage.errorMessage("参数错误").add("error", "WRONG_DATA");
//        }
//        ClazzLevel level;
//        try {
//            level = ClazzLevel.parse(Integer.parseInt(clazzLevel));
//            if (level == null) {
//                throw new RuntimeException();
//            }
//        } catch (Exception ignored) {
//            return MapMessage.errorMessage("参数错误").add("error", "WRONG_DATA");
//        }
//        List<Clazz> myClazzs = clazzLoaderClient.loadTeacherClazzs(teacher.getId());
//        for (Clazz clazz : myClazzs) {
//            if (!clazz.isTerminalClazz() && StringUtils.equals(clazzLevel, clazz.getClassLevel()) &&
//                    Arrays.asList(clazzNames).contains(clazz.getClassName())) {
//                return MapMessage.errorMessage(clazz.formalizeClazzName() + "已经存在，不能重复创建班级哦~").add("error", "CLAZZ_EXIST");
//            }
//        }
//        School school = asyncTeacherServiceClient.getAsyncTeacherService()
//                .loadTeacherSchool(teacher.getId())
//                .getUninterruptibly();
//        if (null == school) {
//            return MapMessage.errorMessage("请先选择您的学校").add("error", "NO_SCHOOL");
//        }
//
//        List<Map<String, Object>> addList = new ArrayList<>();
//        List<Map<String, Object>> createList = new ArrayList<>();
//        for (String clazzName : clazzNames) {
//            Map<String, List<Map<String, Object>>> map = separate(teacher, school.getId(), level, clazzName);
//            if (map.get("valid").isEmpty()) { // 没有重名班级或者没有有效的重名班级，走创建班级路线
//                createList.add(MiscUtils.m("clazzLevel", clazzLevel, "clazzName", clazzName,
//                        "fullName", level.getDescription() + StringUtils.defaultString(clazzName),
//                        "invalidClazzs", map.get("invalid")));
//            } else { // 有有效的重名班级，走加入班级路线
//                addList.add(MiscUtils.m("clazzLevel", clazzLevel, "clazzName", clazzName,
//                        "fullName", level.getDescription() + StringUtils.defaultString(clazzName),
//                        "invalidClazzs", map.get("invalid"), "validClazzs", map.get("valid"),
//                        "creatorType", map.get("creatorType")));
//            }
//        }
//        return MapMessage.successMessage().add("createList", createList).add("addList", addList);
//    }

    /**
     * 查询重名班级，用于判断创建还是加入班级 微信  这里和之前平台的逻辑不一致， 干脆新写
     */
    @Override
    public MapMessage findClazzWithSameNameForWechat(Teacher teacher, String clazzLevel, final String[] clazzNames) {
        if (null == teacher || StringUtils.isBlank(clazzLevel) || clazzNames.length == 0) {
            return MapMessage.errorMessage("参数错误").add("error", "WRONG_DATA");
        }
        ClazzLevel level;
        try {
            level = ClazzLevel.parse(Integer.parseInt(clazzLevel));
            if (level == null) {
                throw new RuntimeException();
            }
        } catch (Exception ignored) {
            return MapMessage.errorMessage("参数错误").add("error", "WRONG_DATA");
        }
        List<Clazz> myClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());
        for (Clazz clazz : myClazzs) {
            if (!clazz.isTerminalClazz() && StringUtils.equals(clazzLevel, clazz.getClassLevel()) &&
                    Arrays.asList(clazzNames).contains(clazz.getClassName())) {
                return MapMessage.errorMessage(clazz.formalizeClazzName() + "已经存在，不能重复创建班级哦~").add("error", "CLAZZ_EXIST");
            }
        }
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacher.getId())
                .getUninterruptibly();
        if (null == school) {
            return MapMessage.errorMessage("请先选择您的学校").add("error", "NO_SCHOOL");
        }

        List<Map<String, Object>> addList = new ArrayList<>();
        for (String clazzName : clazzNames) {
            List<Map<String, Object>> mapList = separateForWechat(teacher, school.getId(), level, clazzName);
            addList.add(MiscUtils.m("clazzLevel", clazzLevel, "clazzName", clazzName,
                    "fullName", level.getDescription() + StringUtils.defaultString(clazzName),
                    "validClazzs", mapList));
        }
        return MapMessage.successMessage().add("addList", addList);
    }

    /**
     * 创建班级，学生名称重复提醒
     */
    @Override
    public List<Map<String, Object>> getStudentNameOverlapClazzs(ClassMapper command) {
        if (null == command || command.getNames().length == 0) {
            return Collections.emptyList();
        }
        ClazzLevel clazzLevel;
        try {
            clazzLevel = ClazzLevel.parse(Integer.parseInt(command.getClassLevel()));
            if (clazzLevel == null) {
                throw new RuntimeException();
            }
        } catch (Exception ex) {
            return Collections.emptyList();
        }
        Set<String> targetNameSet = new HashSet<>(Arrays.asList(command.getNames()));

        List<Map<String, Object>> result = new LinkedList<>();
        // 获取同年级的班级
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(command.getSchoolId())
                .enabled()
                .clazzLevel(clazzLevel)
                .toList();
        for (Clazz clazz : clazzs) {
            List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazz.getId());
            List<User> students = new ArrayList<>(userLoaderClient.loadUsers(studentIds).values());
            if (!students.isEmpty()) {
                Set<String> nameSet = new HashSet<>();
                List<String> names = new ArrayList<>();
                int noNameCount = 0;
                for (User student : students) {
                    String name = student.fetchRealname();
                    if (StringUtils.isNotBlank(name)) {
                        nameSet.add(name);
                        names.add(name);
                    } else {
                        noNameCount++;
                    }
                }
                Set<Set<String>> sets = new HashSet<>();
                sets.add(targetNameSet);
                sets.add(nameSet);
                int overlapCount = CollectionUtils.intersection(sets).size();
                if (overlapCount >= 3 && command.getNames().length - overlapCount <= noNameCount) { // 符合条件
                    Map<String, Object> map = new HashMap<>();
                    map.put("clazzId", clazz.getId());
                    map.put("clazzName", clazz.formalizeClazzName());
                    List<Teacher> teachers = ClazzTeacher.toTeacherList(teacherLoaderClient.loadClazzTeachers(clazz.getId()));
                    List<Map<String, Object>> teacherList = new ArrayList<>();
                    for (Teacher teacher : teachers) {
                        Map<String, Object> teacherMap = new HashMap<>();
                        teacherMap.put("teacherName", teacher.fetchRealname());
                        teacherMap.put("subject", teacher.getSubject().getValue());
                        teacherList.add(teacherMap);
                    }
                    map.put("teachers", teacherList);
                    String text = "";
                    int i = 0;
                    for (String name : names) {
                        text += name + "、";
                        if (++i == 10) {
                            break;
                        }
                    }
                    map.put("text", text.substring(0, text.length() - 1) + "等" + students.size() + "个学生");
                    result.add(map);
                }
            }
        }

        return result.size() >= 3 ? new ArrayList<>(result.subList(0, 3)) : result;
    }

    /**
     * 修改班级
     */
//    @Override
//    public MapMessage modify(final ClassMapper mapper, Teacher operator) {
//        try {
//            Validate.notNull(mapper.getClazzId());
//            Validate.notBlank(mapper.getClazzName());
//            Validate.notBlank(mapper.getClassLevel());
//            Validate.notBlank(mapper.getEduSystem());
//        } catch (Exception ex) {
//            return MapMessage.errorMessage("信息不全");
//        }
//        if (!teacherLoaderClient.getExtension().isManagingClazz(operator.getId(), mapper.getClazzId())) {
//            return MapMessage.errorMessage("您没有权限修改班级");
//        }
//
//        final Long clazzId = mapper.getClazzId();
//        logger.debug("Operator {} is going to modify class {}", operator.getId(), clazzId);
//        final MutableObject<String> errorMessage = new MutableObject<>(null);
//
//        try {
//            utopiaSql.withTransaction(transactionStatus -> {
//                Clazz clazz = clazzLoaderClient.loadClazz(clazzId);
//                if (clazz == null) {
//                    errorMessage.setValue("班级" + clazzId + "不存在或者已经被删除");
//                    throw new RuntimeException();
//                }
//                String[] clazzLevels = StringUtils.split(clazz.getEduSystem().getCandidateClazzLevel(), ",");
//                if (!Arrays.asList(clazzLevels).contains(mapper.getClassLevel())) {
//                    errorMessage.setValue("无效的年级");
//                    throw new RuntimeException();
//                }
//
//                // prepare clazz
//                ClazzLevel clazzLevel = ClazzLevel.of(ConversionUtils.toInt(mapper.getClassLevel()));
//                clazz.setJie(String.valueOf(ClassJieHelper.fromClazzLevel(clazzLevel)));
//                logger.debug("Class level is {}", clazzLevel);
//                ClazzType clazzType = clazzLevel == ClazzLevel.PRIVATE_GRADE ? ClazzType.PRIVATE : ClazzType.PUBLIC;
//                clazz.setClassType(clazzType.getType());
//                logger.debug("Class type is {}", clazzType);
//                clazz.setClassName(mapper.getClazzName());
//                clazz.setEduSystem(EduSystemType.valueOf(mapper.getEduSystem()));
//                clazz.setFreeJoin(mapper.getFreeJoin()); // free join
//
//                // update clazz
//                clazzServiceClient.updateClazz(clazz.getId(), clazz);
//                return clazz.getId();
//            });
//            return MapMessage.successMessage("编辑班级成功");
//        } catch (Exception ex) {
//            if (errorMessage.getValue() != null) {
//                return MapMessage.errorMessage(errorMessage.getValue());
//            } else {
//                logger.error("Failed to modify clazz", ex);
//                return MapMessage.errorMessage(ex.getMessage());
//            }
//        }
//    }

}