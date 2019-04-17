package com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.Getter;
import lombok.Setter;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/7/25
 */
@Getter
@Setter
public class OralStudentQuestionResp extends BaseResp {

    private static final long serialVersionUID = -2795147921390588260L;
    private Long studentId;         //学生ID
    private String studentName;     //学生姓名
    private String preStatus;       //前测情况(通过or未通过)
    private String voiceUrl;        //前测音频地址

}
