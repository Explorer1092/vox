package com.voxlearning.washington.mapper.picturebook;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhiqian.ren
 */
@Data
public class PictureBookTag implements Serializable {
    private static final long serialVersionUID = 8010430604429688610L;

    private String tagId;
    private String tagName;
    private Integer isHomeShow;
    private List<String> topics;
}
