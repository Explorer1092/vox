package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/26
 */
@Getter
@Setter
public class CalculateResult implements Serializable {
    private static final long serialVersionUID = -4044525419183559022L;

    private Double score; // 作业中某个练习类型的总得分
    private Long duration; // 作业中某个练习类型的净时长，单位为秒
    private Integer rightNum; //答对题数
    private Integer errorNum; //答错题数
}
