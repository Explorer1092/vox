package com.voxlearning.utopia.service.newhomework.api.entity.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/5/28
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorQuestion implements Serializable {

    private static final long serialVersionUID = -2884614957939832100L;

    private String errorQuestionId;        //错题ID
    private String errorCauseId;          //错因id
}
