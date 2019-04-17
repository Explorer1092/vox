package com.voxlearning.utopia.service.psr.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PsrMathAppResultDetail implements Serializable {

    private static final long serialVersionUID = 1755848410977971916L;
    private long baseId;                                         //题ID
    private boolean pass;                                           //是否正确
    private String answer;                                        //答案
}
