package com.voxlearning.utopia.service.zone.api.entity.boss;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author dongfeng.xue
 * @date 2018-11-06
 */
@Setter@Getter
public class AttackMonsterResult {
    // {"type":"revise_boss","studentId":"30013",attributes:{"isSuccess":"true","appKey":"AfentiMath"}}
    private String type;//类型
    private Long studentId;
    private Map<String,Object> attributes;
}
