package com.voxlearning.utopia.service.newhomework.api.entity.outside;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class OutsideReadingMission implements Serializable {
    private static final long serialVersionUID = -525178382575596662L;

    private String missionId;                   // 关卡id
    private String missionName;                 // 关卡名称
    private List<String> questionIds;           // 应试题id列表
    private List<String> subjectiveQuestionIds; // 主观题id列表
}
