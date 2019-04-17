package com.voxlearning.washington.mapper.picturebook;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhiqian.ren
 */
@Data
public class PicBookSubjects implements Serializable {
    private static final long serialVersionUID = 6717059077294746602L;

    private String id;
    private String coverImg;
    private String headImg;
    private String subName;
    private List<String> ids;
}
