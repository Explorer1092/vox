package com.voxlearning.utopia.service.newhomework.api.entity.outside;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class OutsideReadingPractice implements Serializable {
    private static final long serialVersionUID = 5319868156072419701L;

    private String bookId;                          // 课本id
    private List<OutsideReadingMission> missions;   // 关卡列表
}
