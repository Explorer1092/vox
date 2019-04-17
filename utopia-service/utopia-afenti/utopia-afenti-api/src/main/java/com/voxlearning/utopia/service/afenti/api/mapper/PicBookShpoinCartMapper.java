package com.voxlearning.utopia.service.afenti.api.mapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 绘本购物车Mapper
 */
@Data
@EqualsAndHashCode(of = "bookId")
public class PicBookShpoinCartMapper implements Serializable{

    private static final long serialVersionUID = -510457813029042279L;

    private String bookId;
    private Long userId;
    private String name;
    private String coverUrl;
    private Double price;
    private Integer keyWords;
    private Integer seconds;
    private List<String> readLvl;
}
