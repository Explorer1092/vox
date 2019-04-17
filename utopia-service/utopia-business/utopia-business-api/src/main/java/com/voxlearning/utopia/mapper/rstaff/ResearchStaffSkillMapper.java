package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Changyuan on 2015/1/14.
 * 教研员语言技能Mapper
 */
@Data
public class ResearchStaffSkillMapper implements Serializable {

    private static final long serialVersionUID = 5694086341230827656L;

    private long countyCount;   // 区域数，仅省教研员使用

    private long schoolCount;   // 学校数

    private long validSchoolCount;  // 有效学校数

    private List<ResearchStaffSkillUnitMapper> skillUnitList = new ArrayList<>();

    // 区/学校最高学生数
    private long maxStudentCount;
    // 区/学校总数量
    private long totalStudentCount;
}
