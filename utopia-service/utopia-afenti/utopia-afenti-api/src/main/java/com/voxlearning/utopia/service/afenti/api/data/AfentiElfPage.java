package com.voxlearning.utopia.service.afenti.api.data;

import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by DELL on 2017/10/31.
 */
@Getter
@Setter
public class AfentiElfPage implements Serializable {

    private static final long serialVersionUID = -6080881460203045903L;
    private List<WrongQuestionLibrary> questions;
    private Integer totalNumber;
    private Integer afentiNumber;
    private Integer homeworkNumber;

}
