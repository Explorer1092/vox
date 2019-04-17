package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityService;
import com.voxlearning.utopia.service.campaign.api.entity.*;
import com.voxlearning.utopia.service.campaign.api.enums.CourseStatus;
import com.voxlearning.utopia.service.campaign.api.mapper.*;
import org.slf4j.Logger;

import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class YiqiJTServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(YiqiJTServiceClient.class);

    @ImportService(interfaceClass = TeacherActivityService.class)
    private TeacherActivityService remoteReference;

    public List<YiqiJTCourse> load17JTCourseList() {
        return remoteReference.load17JTCourseList()
                .stream()
                .sorted((t1, t2) -> {
                    int result = t2.getTopNum() - t1.getTopNum();
                    if (result == 0) {
                        result = t2.getUpdateDatetime().after(t1.getUpdateDatetime())==true ? 1:-1;
                    }
                    return result;
                })
                .collect(Collectors.toList());
    }

    public List<YiqiJTCourseListMapper> load17JTCourseListMapper() {
        return this.load17JTCourseList().stream()
                .filter(t -> t.getStatus() != CourseStatus.OFFLINE.getStatus())
                .map(t -> {
                    YiqiJTCourseListMapper mapper = new YiqiJTCourseListMapper();
                    try {
                        BeanUtils.copyProperties(mapper, t);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        logger.error("load17JTCourseListMapper copyProperties error", e);
                    }
                    return mapper;
                })
                .collect(Collectors.toList());
    }

    public String wrapAuth(String url, Date expTime) {
        return remoteReference.wrapAuth(url, expTime);
    }

    public Date loadCourseBuyTime(Long teacherId, Long courseId) {
        return remoteReference.loadCourseBuyTime(teacherId,courseId);
    }

    public List<YiqiJTCourseMapper> load17JTCourseMapperAll() {
        // 这个...  哎~很难受 量不大，先这样?
        List<YiqiJTCourse> yiqiJTCourses = load17JTCourseList();
        return yiqiJTCourses.stream().map(i -> load17JTCourseMapper(i.getId())).collect(Collectors.toList());
    }

    public YiqiJTCourseMapper load17JTCourseMapper(long courseId) {
        YiqiJTCourseMapper mapper = new YiqiJTCourseMapper();
        try {
            BeanUtils.copyProperties(mapper, this.loadCourseById(courseId));
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("load17JTCourseMapper copyProperties error", e);
        }
        mapper.setChoiceNoteList(remoteReference.getCourseNotesByCourseId(courseId)
            .stream()
            .map(t -> {
                YiqiJTChoiceNoteMapper noteMapper = new YiqiJTChoiceNoteMapper();
                try {
                    BeanUtils.copyProperties(noteMapper, t);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return noteMapper;
            })
            .collect(Collectors.toList()));
        mapper.setCataloglist(remoteReference.getCourseCatalogsByCourseId(courseId)
            .stream()
            .map(t -> {
                YiqiJTCourseCatalogMapper noteMapper = new YiqiJTCourseCatalogMapper();
                try {
                    BeanUtils.copyProperties(noteMapper, t);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return noteMapper;
            })
            .sorted(Comparator.comparing(YiqiJTCourseCatalogMapper::getTimeNodeSec))
            .collect(Collectors.toList()));
        mapper.setGradeList(remoteReference.getGradesByCourdeId(courseId)
                .stream()
                .map(YiqiJTCourseGrade::getGradeId)
                .distinct()
                .collect(Collectors.toList()));
        mapper.setSubjectList(remoteReference.getSubjectsByCourseId(courseId)
                .stream()
                .map(YiqiJTCourseSubject::getSubjectId)
                .distinct()
                .collect(Collectors.toList()));
        mapper.setOuterchainList(remoteReference.getCourseOuterchainsByCourseId(courseId)
                .stream()
                .map(s -> {
                    YiqiJTCourseOuterchainMapper outerchainMapper = new YiqiJTCourseOuterchainMapper();
                    try {
                        BeanUtils.copyProperties(outerchainMapper, s);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return outerchainMapper;

                }).collect(Collectors.toList()));
        return mapper;
    }

    public YiqiJTCourse loadCourseById(long courseId) {
        return remoteReference.loadCourseById(courseId);
    }

    public YiqiJTCourse upsertCourse(YiqiJTCourse course) {
        return remoteReference.upsertCourse(course);
    }

    public void addCourseCatalog(YiqiJTCourseCatalog courseCatalog) {
        remoteReference.addCourseCatalog(courseCatalog);
    }

    public boolean delCourseOuterchain(long id) {
        return remoteReference.delCourseOuterchain(id);
    }

    public void addCourseOuterchain(YiqiJTCourseOuterchain outerchain) {
        remoteReference.addCourseOuterchain(outerchain);
    }

    public List<YiqiJTCourseOuterchain> getCourseOuterchainsByCourseId(long courseId) {
        return remoteReference.getCourseOuterchainsByCourseId(courseId);
    }

    public YiqiJTCourseOuterchain getCourseOuterchainById(long courseId) {
        return remoteReference.getCourseOuterchainById(courseId);
    }

    public YiqiJTCourseCatalog getCourseCatalogById(long id) {
        return remoteReference.getCourseCatalogById(id);
    }

    public boolean delCourseCatalog(long catalogId) {
        return remoteReference.delCourseCatalog(catalogId);
    }
    public List<YiqiJTCourseCatalog> getCourseCatalogsByCourseId(long courseId) {
        return remoteReference.getCourseCatalogsByCourseId(courseId);
    }

    public Map<Long, YiqiJTCourseCatalogMapper> loadCourseCatalogMap() {
        return remoteReference.loadCourseCatalogList()
                .stream()
                .map(t -> {
                    YiqiJTCourseCatalogMapper mapper = new YiqiJTCourseCatalogMapper();
                    try {
                        BeanUtils.copyProperties(mapper, t);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return mapper;
                })
                .collect(Collectors.toMap(YiqiJTCourseCatalogMapper::getId, Function.identity()));
    }

    public void upsertCourseSubject(long courseId, List<Integer> subjectIds ) {
        if (courseId == 0 || subjectIds == null || subjectIds.isEmpty()) {
            return;
        }

        remoteReference.upsertCourseSubject(
                subjectIds.stream()
                    .map(s -> {YiqiJTCourseSubject subject = new YiqiJTCourseSubject();
                    subject.setCourseId(courseId);
                    subject.setSubjectId(s);
                    return subject;
                }).collect(Collectors.toList())
        );
    }

    public void upsertCourseGeade(long courseId, List<Integer> gradeIds) {
        if (courseId == 0 || gradeIds.isEmpty()) {
            return;
        }

        remoteReference.upsertCourseGeade(gradeIds.stream()
                .map(s -> {YiqiJTCourseGrade grade = new YiqiJTCourseGrade();
                    grade.setCourseId(courseId);
                    grade.setGradeId(s);
                    return grade;
                })
                .collect(Collectors.toList()));
    }

    public void upsertCourseChoiceNote(YiqiJTChoiceNote choiceNote){
        remoteReference.upsertCourseChoiceNote(choiceNote);
    }

    public boolean delCourseChoiceNote(long noteid) {
        return remoteReference.delCourseChoiceNote(noteid);
    }

    public List<YiqiJTChoiceNote> getCourseNotesByCourseId(long courseId) {
        return remoteReference.getCourseNotesByCourseId(courseId);
    }

    public YiqiJTChoiceNote getCourseNoteById(long id) {
        return remoteReference.getCourseNoteById(id);
    }

    public Map<Long, Set<Integer>> getAllGradeMap() {
         Map<Long, Set<Integer>> result = new HashMap<>();
         remoteReference.getAllGrade()
                .stream()
                .forEach(t -> {
                    if (result.containsKey(t.getCourseId())) {
                        result.get(t.getCourseId()).add(t.getGradeId());
                    } else {
                        Set<Integer> list = new HashSet<>();
                        list.add(t.getGradeId());
                        result.put(t.getCourseId(), list);
                    }
                });
        return result;
    }

    public List<Integer> getGradeIdsByCourseId(long courseId) {
        return remoteReference.getGradesByCourdeId(courseId)
                .stream()
                .map(YiqiJTCourseGrade::getGradeId)
                .collect(Collectors.toList());
    }

    public List<Integer> getSubjectIdsByCourseId(long courseId) {
        return remoteReference.getSubjectsByCourseId(courseId)
                .stream()
                .map(YiqiJTCourseSubject::getSubjectId)
                .collect(Collectors.toList());
    }

    public Map<Long, Set<Integer>> getAllSubjectMap() {
        Map<Long, Set<Integer>> result = new HashMap<>();
        List<YiqiJTCourseSubject> subjects = remoteReference.getAllSubject();
        subjects.stream()
                .forEach(t -> {
            if (result.containsKey(t.getCourseId())) {
                result.get(t.getCourseId()).add(t.getSubjectId());
            } else {
                Set<Integer> list = new HashSet<>();
                list.add(t.getSubjectId());
                result.put(t.getCourseId(), list);
            }
        });
        return result;
    }

    public Map<Long, YiqiJTChoiceNoteMapper> loadCourseNoteMap() {
        return remoteReference.loadCourseNoteList()
                .stream()
                .map(t -> {
                    YiqiJTChoiceNoteMapper mapper = new YiqiJTChoiceNoteMapper();
                    try {
                        BeanUtils.copyProperties(mapper, t);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return mapper;
                })
                .collect(Collectors.toMap(YiqiJTChoiceNoteMapper::getId, Function.identity()));
    }

    public int updateCourseTopNum(long courseId, int topNum) {
        return remoteReference.updateCourseTopNum(courseId, topNum);
    }

    public int updateCourseStatus(long courseId, int status) {
        return remoteReference.updateCourseStatus(courseId, status);
    }

    public List<YiqiJTCourse> select17JTCourseList(String courseName, List<Long> gradeList, List<Long> subjectList) {
        return remoteReference.select17JTCourseList(courseName, gradeList, subjectList);
    }

    public MapMessage add17JTReadCount(Long id){
        return remoteReference.add17JTReadCount(id);
    }

    public MapMessage add17JTCollectCount(Long id, Long incrValue) {
        return remoteReference.add17JTCollectCount(id, incrValue);
    }
}
