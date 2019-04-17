package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ChipsRank implements Serializable {

    private static final long serialVersionUID = -8888857252959630529L;
    private Long userId;
    private String userName;
    private String image;
    private Integer number;
    private Integer rank;

    public static ChipsRank newInstance(Long userId, Integer number) {
        ChipsRank rank = new ChipsRank();
        rank.setUserId(userId);
        rank.setNumber(number);
        return rank;
    }

}
