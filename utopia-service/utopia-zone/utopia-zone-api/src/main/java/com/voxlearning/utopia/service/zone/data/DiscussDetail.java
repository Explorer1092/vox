package com.voxlearning.utopia.service.zone.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author chensn
 * @date 2018-11-07 15:04
 */
@Getter
@Setter
public class DiscussDetail implements Serializable {
    private static final long serialVersionUID = 3488799991051240554L;
    private String id;
    private String coverUrl;
    private String coverThumbnailUrl;
}
