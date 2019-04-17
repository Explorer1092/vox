package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 12/1/16.
 */
@Getter
@Setter
public class PicListenProductMapper implements Serializable {
    private static final long serialVersionUID = -2412566825813543316L;

    private String productId;
    private String productName;
    private String ablumId;
    private String albumImg;
    private String albumAuthor;
}
