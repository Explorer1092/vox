package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaguang.wang on 2016/5/4.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegionDictionaryData implements Serializable, Comparable {
    private static final long serialVersionUID = 4386408884669134213L;

    private Long id;
    private Date createDateTime;
    private Date updateDateTime;
    private Integer regionCode;     // 地区编码
    private String cityModel;       // 直营/代理
    private String cityLevel;       // A/B/C 类城市
    private String springMainCity;  // 主城区/非主城区
    private String citySettlement;  // 低渗/高渗
    private String marketStuLevel;  // 小学/中学
    private Long addBudget;         // 新增认证预算
    private Long marBudget;         // 三月月活预算
    private Long aprBudget;         // 四月月活预算
    private Long mayBudget;         // 五月月活预算
    private Long junBudget;         // 六月月活预算
    private Long doubleSubjectBudget;// 双科认证预算
    private Long stuNumExpSix;       // 排除毕业班的学生数
    private Long gradeMathAddBudget; //1~2年级新增数学认证数
    private String regionName;       //所在地区名称

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
