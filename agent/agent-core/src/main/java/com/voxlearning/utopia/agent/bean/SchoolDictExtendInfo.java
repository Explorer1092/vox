package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.api.constant.CrmSchoolClueStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 学校字典表扩展信息
 * Created by Administrator on 2016/9/6.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolDictExtendInfo {
    private Boolean hasGeographicPosition;              //SchoolExtInfo 中是否有位置信息 ture 是有
    private String  applyType;                          //CrmSchoolClue 中是否有确认学校的审核记录 特指  9月1日 23：59：59 以后提交的学校确认和信息完善的记录
    private CrmSchoolClueStatus applyRecordStatus;      //有申请记录的话申请记录的状态
    private Integer schoolSize;                         //学校规模
    private String schoolSizeSource;                    //学校规模来源
}
