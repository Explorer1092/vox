package com.voxlearning.washington.controller.parent.homework.wrapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class QuestionBoxParam implements Serializable {
    private String boxId;
    private String subject;
    private String unitId;
    private String bookId;
    private String bizType;
}
