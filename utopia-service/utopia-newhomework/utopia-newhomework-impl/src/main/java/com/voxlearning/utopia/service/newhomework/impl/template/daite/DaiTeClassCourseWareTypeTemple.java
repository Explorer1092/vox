package com.voxlearning.utopia.service.newhomework.impl.template.daite;

import com.google.common.collect.Lists;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;
import com.voxlearning.utopia.service.newhomework.impl.template.DaiTeTypeTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.question.api.TeacherCoursewareLoader;
import com.voxlearning.utopia.service.question.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/2/26
 * \* Time: 3:33 PM
 * \* Description: 课件
 * \
 */
@Named
public class DaiTeClassCourseWareTypeTemple implements DaiTeTypeTemplate {
    @Inject
    private TeacherCoursewareLoader teacherCoursewareLoader;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Override
    public DaiTeType getDaiTeType() {
        return DaiTeType.CLASS_COURSE_WARE;
    }

    @Override
    public Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper, Map params) {
        Pageable page = params.get("page") == null ? null : (Pageable) params.get("page");
        List<Map> teacherCourseWareList = Lists.newArrayList();
        Page<TeacherCourseware> coursewarePage = teacherCoursewareLoader.loadTeacherCoursewareByUnitId(mapper.getUnitId(), page);
        List<TeacherCourseware> teacherCoursewaresList = coursewarePage.getContent();
        if (CollectionUtils.isEmpty(teacherCoursewaresList)) {
            return
                    MapUtils.m("content", teacherCourseWareList, "size", 0,
                            "number", 0, "totalPages", 0,
                            "totalElements", 0, "numberOfElements", 0, "pageNumber", page.getPageNumber());

        }
        List<Long> teacherIds = teacherCoursewaresList.stream().map(TeacherCourseware::getTeacherId).collect(Collectors.toList());
        Map<Long, TeacherDetail> teacherDetailMap = teacherLoaderClient.loadTeacherDetails(teacherIds);
        teacherCoursewaresList.forEach(courseware -> {
            String schoolName = "";
            if (courseware.getTeacherId() != null) {
                TeacherDetail teacher = MapUtils.isNotEmpty(teacherDetailMap) ? teacherDetailMap.get(courseware.getTeacherId()) : null;
                schoolName = teacher != null ? teacher.getTeacherSchoolName() : "";
            }
            Map course = MapUtils.m("id", courseware.getId(), "coverUrl", courseware.getCoverUrl(),
                    "title", courseware.getTitle(), "schoolName", schoolName,
                    "teacherId", courseware.getTeacherId(), "teacherName", courseware.getTeacherName(),
                    "createTime", DateUtils.dateToString(courseware.getCreateTime(), DateUtils.FORMAT_SQL_DATE), "totalScore", courseware.getTotalScore(),
                    "commentNum", courseware.getCommentNum(),
                    "pptCoursewareFile", StringUtils.isNotEmpty(courseware.getPptCoursewareFile()) ? courseware.getPptCoursewareFile() : courseware.getCoursewareFile()
            );
            teacherCourseWareList.add(course);
        });
        return
                MapUtils.m("content", teacherCourseWareList, "size", coursewarePage.getSize(),
                        "number", coursewarePage.getNumber(), "totalPages", coursewarePage.getTotalPages(),
                        "totalElements", coursewarePage.getTotalElements(), "numberOfElements", coursewarePage.getNumberOfElements(),
                        "pageNumber", page.getPageNumber()
                );
    }
}
