package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.utopia.service.ai.constant.AIBookStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class AIUserBookInfo implements Serializable {

    private static final long serialVersionUID = -2530754344589299463L;

    private String id;
    private String name;
    private AIBookStatus status;
    private String image;
    private boolean active;
}
