package com.voxlearning.utopia.agent.view.school;

import com.voxlearning.utopia.api.constant.EduSystemType;
import lombok.Getter;
import lombok.Setter;

/**
 * SchoolBasicExtData
 *
 * @author song.wang
 * @date 2018/7/20
 */
@Getter
@Setter
public class SchoolBasicExtData {
    private Long schoolId;
    private EduSystemType eduSystem;
    private String eduSystemDesc;

    private Integer englishStartGrade;           // 英语起始年级
    private Integer schoolSize;                  // 学校规模

    private Integer externOrBoarder;             // 走读 or 寄宿 1:走读 、2:寄宿、3 走读/寄宿（半寄宿）

    private Integer auditStatus;    //审核状态  1: 待审核
}
