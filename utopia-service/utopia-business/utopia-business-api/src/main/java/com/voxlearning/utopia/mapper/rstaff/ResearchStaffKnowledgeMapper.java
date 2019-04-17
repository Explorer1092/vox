package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Changyuan on 2015/1/15.
 * 教研员语言知识Mapper
 */
@Data
public class ResearchStaffKnowledgeMapper implements Serializable {

    private static final long serialVersionUID = -5752088055486099000L;

    private long countyCount;   // 区域数，仅省教研员使用

    private long schoolCount;   // 学校数

    private long validSchoolCount;  // 有效学校数

    private List<ResearchStaffKnowledgeUnitMapper> knowledgeUnitList = new ArrayList<>();

    // 区/学校最高学生数
    private long maxStudentCount;
    // 区/学校总数量
    private long totalStudentCount;
}
