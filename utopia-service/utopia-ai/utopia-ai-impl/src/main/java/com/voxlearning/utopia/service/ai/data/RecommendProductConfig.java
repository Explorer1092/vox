package com.voxlearning.utopia.service.ai.data;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class RecommendProductConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String beImage; //广告页图片
    private Date openDate;  //开放日期
    private String originalBook;
    private String recommendBook; //推荐的教材
    private String recommendProduct;
    private Integer grade;
}
