package com.voxlearning.utopia.service.newhomework.api.mapper.response;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.Getter;
import lombok.Setter;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/3/6
 */
@Getter
@Setter
public class StudentActivityStatistic extends BaseResp {
    private static final long serialVersionUID = -7925568153511994853L;

    private Integer leanPoetryNum = 0;              // 已学古诗数
    private Integer noCorrectionNum = 0;            // 待订正题数
    private Integer parentChildNum = 0;             // 待完成亲子任务
}
