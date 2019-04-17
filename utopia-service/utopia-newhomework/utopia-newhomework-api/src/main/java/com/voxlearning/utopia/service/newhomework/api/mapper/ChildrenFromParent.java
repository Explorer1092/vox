package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ChildrenFromParent implements Serializable {
    private static final long serialVersionUID = -8723306873643251626L;
    private String studentName;
    private Long studentId;
    private String imageUrl;
}
