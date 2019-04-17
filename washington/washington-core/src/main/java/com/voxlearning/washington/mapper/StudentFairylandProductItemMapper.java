package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 2/24/17.
 */
@Getter
@Setter
public class StudentFairylandProductItemMapper implements Serializable {
    private static final long serialVersionUID = -4167205000589040141L;

    private String id;
    private String name;
    private Integer period;
}

