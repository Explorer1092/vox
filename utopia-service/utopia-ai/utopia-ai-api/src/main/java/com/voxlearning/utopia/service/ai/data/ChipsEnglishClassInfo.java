package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ChipsEnglishClassInfo implements Serializable {

    private static final long serialVersionUID = -2530754344589299463L;

    private String id;
    private String bookId;
    private String name;
    private Integer score;
    private String type;
    private String typeDesc;
    private String title;//标题
    private String cardTitle;//卡片标题
    private String cardDescription;//卡片描述
    private String img;
    private Integer rank;
    private Boolean finished;
    private Boolean lock;
    private Integer star;
    private Boolean currentDay;

}
