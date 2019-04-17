package com.voxlearning.utopia.service.business.api.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewTeacherResourceItemVo extends NewTeacherResourceWrapper {
    private static final long serialVersionUID = 7993281011107703887L;

    private String authorName;                          // 作者姓名
    private Long authorSchoolId;                        // 作者学校ID
    private String authorSchoolName;                    // 作者学校名称

    private Long readCount;                             // 阅读次数
    private Long collectCount;                          // 收藏次数
    private Long participateNum;                        // 任务参与人数
    private Long finishNum;                             // 任务完成人数

}
