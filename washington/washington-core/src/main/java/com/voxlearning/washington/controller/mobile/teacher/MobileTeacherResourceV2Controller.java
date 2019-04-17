package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Named;
import java.util.Objects;

@Named
@RequestMapping("/teacherMobile/teachingres/v2/")
public class MobileTeacherResourceV2Controller extends MobileTeachingResourceController {

    @ResponseBody
    @RequestMapping("subject_clazz_level.vpage")
    public MapMessage subjectClazzLevel() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }

        return teacherResourceServiceClient.getRemoteReference().loadSubjectClazzLevel(user.getId());
    }

    @ResponseBody
    @RequestMapping("book_list.vpage")
    public MapMessage bookList() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }

        // 学科
        Integer subjectId = getRequestInt("subject_id");
        if (Objects.equals(subjectId, 0)) {
            subjectId = null;
        }

        // 上下册
        Integer clazzLevelId = getRequestInt("level_id");
        if (Objects.equals(clazzLevelId, 0)) {
            clazzLevelId = null;
        }

        // 年级
        Integer levelTerm = getRequestInt("level_term");
        if (Objects.equals(levelTerm, 0)) {
            levelTerm = null;
        }

        return teacherResourceServiceClient.getRemoteReference().loadBookList(user.getId(), subjectId, clazzLevelId, levelTerm);
    }


    @ResponseBody
    @RequestMapping("list.vpage")
    public MapMessage loadResource() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }

        Integer subjectId = getRequestInt("subject_id");
        if (Objects.equals(subjectId, 0)) {
            subjectId = null;
        }
        String bookId = getRequestString("book_id");
        Integer source = getRequestInt("source");
        Integer page = getRequestInt("page", 1);
        Integer pageSize = getRequestInt("page_size", 10);

        return teacherResourceServiceClient.getRemoteReference().loadResource(user.getId(), subjectId, bookId, source, page, pageSize);
    }

    @ResponseBody
    @RequestMapping("detail.vpage")
    public MapMessage detail() {
        Long teacherId = null;
        User user = currentUser();
        if (user != null && user.isTeacher()) {
            teacherId = user.getId();
        }

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }
        teacherResourceServiceClient.getRemoteReference().incrReadCount(id, 1L);
        return teacherResourceServiceClient.getRemoteReference().loadDetailMsgById(id, teacherId);
    }

    @ResponseBody
    @RequestMapping("receive_resource.vpage")
    public MapMessage receiveResource() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }
        return teacherResourceServiceClient.getRemoteReference().receiveResource(id, user.getId());
    }

    @ResponseBody
    @RequestMapping("collect.vpage")
    public MapMessage collect() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }
        return teacherResourceServiceClient.getRemoteReference().collect(id, user.getId());
    }

    @ResponseBody
    @RequestMapping("disable_collect.vpage")
    public MapMessage disableCollect() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }
        return teacherResourceServiceClient.getRemoteReference().disableCollect(id, user.getId());
    }

    @ResponseBody
    @RequestMapping("share_parent.vpage")
    public MapMessage shareParent() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }
        return teacherResourceServiceClient.getRemoteReference().shareParent(user.getId(), id);
    }

}


