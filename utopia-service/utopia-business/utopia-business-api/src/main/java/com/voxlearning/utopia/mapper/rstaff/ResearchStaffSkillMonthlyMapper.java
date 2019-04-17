package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Changyuan on 2015/1/16.
 */
@Data
public class ResearchStaffSkillMonthlyMapper implements Serializable {

    private static final long serialVersionUID = 4478599536999080277L;
    ResearchStaffSkillMonthlyUnitMapper listening;
    ResearchStaffSkillMonthlyUnitMapper speaking;
    ResearchStaffSkillMonthlyUnitMapper reading;
    ResearchStaffSkillMonthlyUnitMapper written;
}
