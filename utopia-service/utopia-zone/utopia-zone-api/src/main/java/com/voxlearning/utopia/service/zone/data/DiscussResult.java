package com.voxlearning.utopia.service.zone.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author chensn
 * @date 2018-10-24 12:17
 */
@Getter
@Setter
public class DiscussResult implements Serializable {
    private static final long serialVersionUID = -5440102264603435906L;
    private Boolean isPay;
    private String productId;
    private DiscussDetail dubbing;
    private DiscussDetail pictureBook;

}
