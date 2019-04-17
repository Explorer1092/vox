package com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/6/29
 */
@Getter
@Setter
public class ClazzGroupInfoResp extends BaseResp {

    private static final long serialVersionUID = 3926224823864968945L;
    private Long clazzId;//班级ID
    private String clazzName;//年级名+班级名-->formalizeClazzName
    private List<GroupInfo> groupInfos;//班组信息

    @Getter
    @Setter
    @AllArgsConstructor
    public static class GroupInfo implements Serializable {

        private static final long serialVersionUID = 2812124684010161580L;
        private Long groupId;//组ID
        private Subject subject;//学科
        private String subjectName;//学科名
    }
}
