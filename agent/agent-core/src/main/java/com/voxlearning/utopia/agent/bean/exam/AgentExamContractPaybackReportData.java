package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.agent.bean.export.ExportAble;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 合同回款明细导出数据
 *
 * @author deliang.che
 * @date 2018-05-03
 **/
@Getter
@Setter
public class AgentExamContractPaybackReportData implements ExportAble {
    private String contractNumber;      //合同编号
    private String paybackNumber;       //回款编号
    private Long schoolId;              //学校ID
    private String schoolName;          //学校名称
    private String schoolPopularityType;//学校等级
    private String contractType;        //合同类型
    private String contractorName;      //主签约人
    private Integer contractAmount;     //合同总金额
    private Integer hardwareCost;       //硬件成本
    private Date paybackDate;           //回款日期
    private Integer paybackAmount;      //回款金额
    private Integer havaPaybackAmount;  //本期前已回款金额

    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(contractNumber);
        result.add(paybackNumber);
        result.add(schoolId);
        result.add(schoolName);
        result.add(schoolPopularityType);
        result.add(contractType);
        result.add(contractorName);
        result.add(contractAmount);
        result.add(hardwareCost);
        result.add(null != paybackDate ? DateUtils.dateToString(paybackDate,DateUtils.FORMAT_SQL_DATE) : "");
        result.add(paybackAmount);
        result.add(havaPaybackAmount);
        return result;
    }
}
