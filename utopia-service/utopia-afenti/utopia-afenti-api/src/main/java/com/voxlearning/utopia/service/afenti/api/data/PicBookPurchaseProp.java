package com.voxlearning.utopia.service.afenti.api.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Summer on 2018/3/30
 */
@Getter
@Setter
public class PicBookPurchaseProp implements Serializable {

    private static final long serialVersionUID = 2731950719787491623L;

    private String name;
    private Integer num;
    private String desc;
    private String img;
    private String id; // 道具ID
}
