package com.voxlearning.utopia.service.afenti.api.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author peng.zhang.a
 * @since 16-11-30
 */

@Getter
@Setter
public class ClassmateRewardInfo implements Serializable {

    private static final long serialVersionUID = 2496856131798824248L;

    private Long userId;
    private String realName;
    private Integer integralNum;
    private String imgUrl;
}
