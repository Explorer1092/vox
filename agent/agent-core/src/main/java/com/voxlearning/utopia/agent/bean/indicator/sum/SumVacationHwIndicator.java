package com.voxlearning.utopia.agent.bean.indicator.sum;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.bean.indicator.BaseOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * SumVacationHwIndicator
 *
 * @author deliang.che
 * @since  2019/1/3
 */
@Getter
@Setter
@UtopiaCacheRevision("20190103")
public class SumVacationHwIndicator implements Serializable {

    private static final long serialVersionUID = 8620454757105831465L;
    private Integer day;                            // 日期
    private Long id;
    private Integer idType;                       // 1:部门   2：user  3:school
    private Integer schoolLevel;                    // 1: 小学，   2： 初中，  4：高中，   5：学前 ，  24：初高中,
    private String name;                            // 名称

    private Integer subjectCode;

    private Integer vacationHwTeaNum;   //布置老师
    private Integer teaScale;           //老师基数
    private Integer settleStuNum;       //结算学生
    private Double vacationHwRate;      //布置率
    private Integer authUnAssignNum;    //认证未布置
    public static String ck_id_type_day_level_subject(Long id, Integer idType, Integer day, Integer schoolLevel,Integer subjectCode) {
        return CacheKeyGenerator.generateCacheKey(SumVacationHwIndicator.class,
                new String[]{"i", "t", "d", "l","s"},
                new Object[]{id, idType, day, schoolLevel,subjectCode});
    }
}
