package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Created by tanguohong on 2016/11/29.
 */
@Getter
@Setter
public class AssignVacationHomeworkContext extends AbstractContext<AssignVacationHomeworkContext>{

    private static final long serialVersionUID = 7860083582352653016L;

    private HomeworkSource source;
    // ========================================================================
    // We can add some hack parameters here for controlling the process of
    // homework checking.
    // ========================================================================

    private boolean validateSameSubject = true;             // validate teacher and homework has same subject
    private boolean validateTeacherClazzPermission = true;  // validate teacher has clazz permission

    // ========================================================================
    // Put result values here.
    // ========================================================================

    private Teacher teacher;
    private HomeworkSourceType homeworkSourceType;
    private Date homeworkStartTime;
    private Date homeworkEndTime;
    private Integer plannedDays;
    private String remark;
    private Map<Long, String> groupBookIdMap;
    private Map<Long, String> groupSubjectMap;
    private Map<Long, Long> groupTeacherMap;
    private final Set<ClazzGroup> clazzGroups = new HashSet<>();
    private final LinkedHashMap<ClazzGroup, VacationHomeworkPackage> assignedHomeworks = new LinkedHashMap<>();
    private Integer integral;
    private Integer lotteryNumber;
}
