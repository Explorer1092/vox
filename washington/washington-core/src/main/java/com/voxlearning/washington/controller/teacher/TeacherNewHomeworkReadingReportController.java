package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ReadingDubbingRecommend;
import com.voxlearning.utopia.service.newhomework.consumer.ReadingReportLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.ReadingReportServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher/new/homework/reading")
public class TeacherNewHomeworkReadingReportController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private ReadingReportLoaderClient readingReportLoaderClient;
    @Inject private ReadingReportServiceClient readingReportServiceClient;

    @RequestMapping(value = "fetchclazzinfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchClazzInfo() {
        Teacher teacher = getSubjectSpecifiedTeacher(Subject.ENGLISH);
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        List<Map<String, Object>> clazzMaps = getClazzList();
        Set<Long> clazzIds = clazzMaps
                .stream()
                .map(clazz -> SafeConverter.toLong(clazz.get("id")))
                .collect(Collectors.toSet());
        Map<Long, List<GroupMapper>> groupMaps = deprecatedGroupLoaderClient.loadClazzGroups(clazzIds);

        List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .getTeacherGroupIds(teacher.getId());
        List<Map<String, Object>> clazzList = new LinkedList<>();
        for (Map<String, Object> clazzMap : clazzMaps) {
            Long clazzId = SafeConverter.toLong(clazzMap.get("id"));
            List<GroupMapper> groupMappers = groupMaps.get(clazzId);
            if (CollectionUtils.isNotEmpty(groupMappers)) {
                for (GroupMapper groupMapper : groupMappers) {
                    if (groupMapper.getSubject() == Subject.ENGLISH && teacherGroupIds.contains(groupMapper.getId())) {
                        clazzMap.put("gid", groupMapper.getId());
                        clazzList.add(clazzMap);
                        break;
                    }
                }
            }
        }
        return MapMessage.successMessage().add("clazzList", clazzList).add("subject", Subject.ENGLISH)
                .add("subjectName", Subject.ENGLISH.getValue());
    }

    @RequestMapping(value = "fetchpicturesemesterreport.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchPictureSemesterReport() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        long gid = this.getRequestLong("gid");
        if (gid <= 0) {
            return MapMessage.errorMessage("gid 参数缺失");
        }
        return readingReportLoaderClient.fetchPictureSemesterReport(gid);
    }


    @RequestMapping(value = "fetchpicturesemesterreportfrombigdata.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchPictureSemesterReportFromBigData() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        long gid = this.getRequestLong("gid");
        if (gid <= 0) {
            return MapMessage.errorMessage("gid 参数缺失");
        }
        return readingReportLoaderClient.fetchPictureSemesterReportFromBigData(gid);
    }


    @RequestMapping(value = "fetchabilityanalysis.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchAbilityAnalysis() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        long gid = this.getRequestLong("gid");
        if (gid <= 0) {
            return MapMessage.errorMessage("gid 参数缺失");
        }
        return readingReportLoaderClient.fetchAbilityAnalysis(gid);
    }


    @RequestMapping(value = "fetchabilityanalysisfromdata.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchAbilityAnalysisFromBigData() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        long gid = this.getRequestLong("gid");
        if (gid <= 0) {
            return MapMessage.errorMessage("gid 参数缺失");
        }
        return readingReportLoaderClient.fetchAbilityAnalysisFromBigData(gid);
    }


    @RequestMapping(value = "fetchpictureinfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchPictureInfo() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("教师请登入");
        }
        String hid = this.getRequestString("hid");
        if (StringUtils.isBlank(hid)) {
            return MapMessage.errorMessage("hid 参数缺失");
        }
        return readingReportLoaderClient.fetchPictureInfo(teacher, hid);
    }

    @RequestMapping(value = "submitreadingdubbingdata.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage submitReadingDubbingData() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.successMessage("请登入账号");
        }
        String hid = this.getRequestString("hid");
        String pictureId = this.getRequestString("pictureId");
        String recommendComment = this.getRequestString("recommendComment");
        String readingDubbingStr = this.getRequestString("readingDubbings");
        List<ReadingDubbingRecommend.ReadingDubbing> recommendVoiceList = JsonUtils.fromJsonToList(readingDubbingStr, ReadingDubbingRecommend.ReadingDubbing.class);
        if (StringUtils.isAnyBlank(hid, pictureId) || CollectionUtils.isEmpty(recommendVoiceList)) {
            return MapMessage.errorMessage("参数错误");
        }
        return AtomicLockManager.instance().wrapAtomic(readingReportServiceClient)
                .keys(hid, pictureId)
                .proxy()
                .submitReadingDubbingData(teacher, hid, ObjectiveConfigType.LEVEL_READINGS, pictureId, recommendVoiceList, recommendComment);
    }


}
