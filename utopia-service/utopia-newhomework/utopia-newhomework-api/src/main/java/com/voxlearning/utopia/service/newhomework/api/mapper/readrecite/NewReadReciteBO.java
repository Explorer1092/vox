package com.voxlearning.utopia.service.newhomework.api.mapper.readrecite;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/6/1 15:39
 */
@Getter
@Setter
public class NewReadReciteBO implements Serializable {
    private static final long serialVersionUID = 1516623280929501596L;
    private String lessonId;
    private String lessonName;
    private List<ReadReciteBO> readReciteList;
}
