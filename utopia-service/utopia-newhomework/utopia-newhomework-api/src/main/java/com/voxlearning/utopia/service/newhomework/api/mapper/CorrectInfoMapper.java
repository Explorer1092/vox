package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 和错因一一对应，每种错因的题量
 *
 * @author xuesong.zhang
 * @since 2016/8/11
 */
@Setter
@Getter
public class CorrectInfoMapper implements Serializable {

    private static final long serialVersionUID = -3159929397759244577L;

    private Integer misread = 0;    // 没理解题目
    private Integer mistake = 0;    // 计算错误
    private Integer missing = 0;    // 没掌握知识点
    private Integer other = 0;      // 其他

    private Integer wrongQuestionCount = 0;             // 总的原作业错题量
    private Integer alreadyCorrectQuestionCount = 0;    // 总的已订正题目数量
    private Integer correctWrongQuestionCount = 0;      // 总的已订正题目中的错题量

}
