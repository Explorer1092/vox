package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class WorkBookConfig implements Serializable {
    private static final long serialVersionUID = 7689724184808001044L;

    private Integer pcode;
    private Integer ccode;
    private Integer acode;
    private String workBookId;
    private Integer priority;
}
