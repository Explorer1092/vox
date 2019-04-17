package com.voxlearning.utopia.service.zone.api.entity.giving;

import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossUserAward;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 吃鸡 业务类
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
public class ChickenStudent implements Serializable {
    private static final long serialVersionUID = -2817707488165114925L;
    private Boolean joinClass; //是否已经累计到班级 总数
    //key 值 1：烤箱 2：托盘  3：鸡 value :是对应的数量
    private Map<String,Integer> chickenMap;
    //用户身上奖励
    private Map<String,ClazzBossUserAward> userAwardMap;
    private Boolean isReceiedPerson;
    private Boolean isReceiedClazz;
}
