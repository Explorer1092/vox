package com.voxlearning.utopia.service.business.api.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewTeacherResourceListVo extends NewTeacherResourceWrapper {
    private static final long serialVersionUID = 7993281011107703887L;

    private Long readCount;                             // 阅读次数
    private Long collectCount;                          // 收藏次数
    private Long participateNum;                        // 任务参与人数
    private Long finishNum;                             // 任务完成人数
}
