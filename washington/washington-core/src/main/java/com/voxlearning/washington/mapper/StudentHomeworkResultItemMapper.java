package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xinxin
 * @since 12/8/2016
 */
@Getter
@Setter
public class StudentHomeworkResultItemMapper implements Serializable {
    private static final long serialVersionUID = -3754723221099571091L;

    private String userName;
    private Long userId;
    private String userImg;
    private String finishDate;

}
