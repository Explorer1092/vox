package com.voxlearning.utopia.service.ai.data;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Summer on 2019/1/14
 */
@Data
public class ChipsUserCourseMapper implements Serializable {

    private static final long serialVersionUID = -3295144025501959723L;

    private Long id;
    private String bookId;
    private String bookName;
    private String productId;
    private String productName;
    private String status;
    private String serviceBeginDate;
    private String serviceEndDate;
    private String operation;
    private Integer rank;
}
