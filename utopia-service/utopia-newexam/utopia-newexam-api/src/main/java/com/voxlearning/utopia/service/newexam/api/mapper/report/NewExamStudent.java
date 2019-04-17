package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class NewExamStudent implements Serializable {
    private static final long serialVersionUID = -6931504321344261329L;
    private Long userId;
    private String userName;
}
