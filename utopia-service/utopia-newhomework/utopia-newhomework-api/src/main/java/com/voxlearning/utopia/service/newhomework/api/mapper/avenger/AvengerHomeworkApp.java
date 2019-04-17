package com.voxlearning.utopia.service.newhomework.api.mapper.avenger;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AvengerHomeworkApp implements Serializable {

    private static final long serialVersionUID = -5984749586848057885L;

    private String stoneDataId;             // 石头堆id
    private String stoneDataType;           // 石头堆数据类型(口语交际用)
    private List<String> moduleTypes;       // 模块类型列表(字词讲练用)
}
