package com.voxlearning.utopia.service.vendor.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Ruib
 * @since 2017/2/15
 */
@Getter
@Setter
public class FairylandProductUrl implements Serializable {
    private static final long serialVersionUID = 5885726364941328838L;

    private String launchUrl;
    private String orientation;
    private String browser;
    private String appKey;
}
