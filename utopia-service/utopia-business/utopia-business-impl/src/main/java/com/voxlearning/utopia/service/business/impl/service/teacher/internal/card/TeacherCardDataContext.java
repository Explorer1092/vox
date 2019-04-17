package com.voxlearning.utopia.service.business.impl.service.teacher.internal.card;


import com.voxlearning.utopia.mapper.TeacherCardMapper;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tanguohong on 2017/4/17.
 */
@Data
public class TeacherCardDataContext implements Serializable {
    private static final long serialVersionUID = -3581748854478979452L;
    // in
    public Teacher teacher;
    public String sys;
    public String ver;
    public String imgDomain;

    // out
    public List<TeacherCardMapper> taskCards = new LinkedList<>();

    public TeacherCardDataContext(Teacher teacher, String sys, String ver, String imgDomain) {
        this.teacher = teacher;
        this.sys = sys;
        this.ver = ver;
        this.imgDomain = imgDomain;
    }
}
