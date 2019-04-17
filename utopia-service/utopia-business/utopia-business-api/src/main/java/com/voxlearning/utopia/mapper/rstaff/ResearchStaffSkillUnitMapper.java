package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Changyuan on 2015/1/14.
 * 教研员语言技能页面中单行mapper
 */
@Data
public class ResearchStaffSkillUnitMapper implements Serializable {

    private static final long serialVersionUID = -4603989476591086690L;

    private String name;

    private ResearchStaffSkillDetailMapper listening;
    private ResearchStaffSkillDetailMapper speaking;
    private ResearchStaffSkillDetailMapper reading;
    private ResearchStaffSkillDetailMapper written;

    private long studentCount;
}
