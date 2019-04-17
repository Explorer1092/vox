package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author deliang.che
 * @create 2018-04-08
 **/
@Getter
@Setter
public class ClassOnlineReportData extends ClassBaseReportData implements ExportAble{
    private String engTeacher;                  //英语老师
    private String mathTeacher;                 //数学老师
    private String chnTeacher;                  //语文老师
    private int tmEngHwSc;                      //本月布置英语作业
    private int tmMathHwSc;                     //本月布置数学作业
    private int tmChnHwSc;                      //本月布置语文作业
    private int engSettlementStuCount;          //英语累计新增
    private int tmFinEngHwEq1IncStuCount;       //英语新增1套
    private int tmFinEngHwEq2IncStuCount;       //英语新增2套
    private int tmFinEngHwGte3IncAuStuCount;    //英语新增3套
    private int engBfEq1StuCount;               //英语回流1套
    private int engBfEq2StuCount;               //英语回流2套
    private int engBfGte3StuCount;              //英语回流3套
    private int finEngHwGte3AuStuCount;         //英语本月月活
    private int lmFinEngHwGte3AuStuCount;       //英语上月月活
    private int mathSettlementStuCount;         //数学累计新增
    private int tmFinMathHwEq1IncStuCount;      //数学新增1套
    private int tmFinMathHwEq2IncStuCount;      //数学新增2套
    private int tmFinMathHwGte3IncAuStuCount;   //数学新增3套
    private int mathBfEq1StuCount;              //数学回流1套
    private int mathBfEq2StuCount;              //数学回流2套
    private int mathBfGte3StuCount;             //数学回流3套
    private int finMathHwGte3AuStuCount;        //数学本月月活
    private int lmFinMathHwGte3AuStuCount;      //数学上月月活
    private int chnSettlementStuCount;          //语文累计新增
    private int tmFinChnHwEq1IncStuCount;       //语文新增1套
    private int tmFinChnHwEq2IncStuCount;       //语文新增2套
    private int tmFinChnHwGte3IncAuStuCount;    //语文新增3套
    private int chnBfEq1StuCount;               //语文回流1套
    private int chnBfEq2StuCount;               //语文回流2套
    private int chnBfGte3StuCount;              //语文回流3套
    private int finChnHwGte3AuStuCount;         //语文本月月活
    private int lmFinChnHwGte3AuStuCount;       //语文上月月活

    private String termReviewSubject;           //布置期末作业包科目（语、数、英）
    private String vacnHwSubject;               //布置暑假作业科目（语、数、英）
    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(getDay());
        result.add(getChargePerson());
        result.add(getCityName());
        result.add(getCountyName());
        result.add(getSchoolId());
        result.add(getSchoolName());
        result.add(null != getSchoolLevel() ? getSchoolLevel().getDescription():"");
        ClazzLevel parse = null;
        if (null != getClazzLevel()){
            parse = ClazzLevel.parse(getClazzLevel());
        }
        result.add(null != parse?parse.getDescription():"");
        result.add(getClazzName());
        result.add(getRegStuCount());
        result.add(getAuStuCount());
        result.add(engTeacher);
        result.add(mathTeacher);
        result.add(chnTeacher);
        result.add(tmEngHwSc);
        result.add(tmMathHwSc);
        result.add(tmChnHwSc);
        result.add(engSettlementStuCount);
        result.add(tmFinEngHwEq1IncStuCount);
        result.add(tmFinEngHwEq2IncStuCount);
        result.add(tmFinEngHwGte3IncAuStuCount);
        result.add(engBfEq1StuCount);
        result.add(engBfEq2StuCount);
        result.add(engBfGte3StuCount);
        result.add(finEngHwGte3AuStuCount);
        result.add(lmFinEngHwGte3AuStuCount);
        result.add(mathSettlementStuCount);
        result.add(tmFinMathHwEq1IncStuCount);
        result.add(tmFinMathHwEq2IncStuCount);
        result.add(tmFinMathHwGte3IncAuStuCount);
        result.add(mathBfEq1StuCount);
        result.add(mathBfEq2StuCount);
        result.add(mathBfGte3StuCount);
        result.add(finMathHwGte3AuStuCount);
        result.add(lmFinMathHwGte3AuStuCount);
        result.add(chnSettlementStuCount);
        result.add(tmFinChnHwEq1IncStuCount);
        result.add(tmFinChnHwEq2IncStuCount);
        result.add(tmFinChnHwGte3IncAuStuCount);
        result.add(chnBfEq1StuCount);
        result.add(chnBfEq2StuCount);
        result.add(chnBfGte3StuCount);
        result.add(finChnHwGte3AuStuCount);
        result.add(lmFinChnHwGte3AuStuCount);
        result.add(termReviewSubject);
        result.add(vacnHwSubject);
        return result;
    }
}
