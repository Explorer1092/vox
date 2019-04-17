package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/2/10
 */
@Setter
@Getter
public class SelfStudyHomeworkInfoMapper implements Serializable {

    private static final long serialVersionUID = 9065101673018155716L;

    //private Integer wrongQuestionCount = 0;                 // 总的作业错题量
    private Integer doCount = 0;                            // 完成数量（基础应用：应用个数，阅读绘本：绘本个数，其他：题数）
    private List<SubHomeworkProcessResult> questionProcess;
}
