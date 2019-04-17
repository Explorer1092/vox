package com.voxlearning.washington.controller.mobile.student.headline.helper;


import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/9
 * Time: 15:26
 */
@Getter
@Setter
public class HeadlineMapperContext {

    private Long currentUserId;
    private Map<Long, StudentInfo> studentInfoMap;
    private Set<Long> groupIds;
    private Integer studentCount;
    private Map<Long, List<User>> currentClassmates;

    public static HeadlineMapperContext newInstance(Long currentUserId) {
        HeadlineMapperContext context = new HeadlineMapperContext();
        context.setCurrentUserId(currentUserId);
        return context;
    }

}
