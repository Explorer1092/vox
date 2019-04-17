package com.voxlearning.utopia.service.ai.data;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChipsMiniProgramQRBO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String content;
    private String image;
    private Date createDate;

}
