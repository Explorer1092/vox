package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * PerformanceDfData
 *
 * @author song.wang
 * @date 2017/8/24
 */
@Getter
@Setter
public class PerformanceDfData  implements Serializable {
    private static final long serialVersionUID = -6319468886799199349L;

    private Long schoolId;
    private Integer day;
    private Integer schoolLevel;

    // 小学部分
    // 注册认证
    private int incRegStuDf;              // 注册学生数日浮
    private int incAuthStuDf;              // 新增认证学生数日浮

    // 单科
    private int finSglSubjHwEq1AuStuCountDf;              // 认证学生当月完成1套任一科目作业学生数日浮
    private int finSglSubjHwEq2AuStuCountDf;              // 认证学生当月完成2套任一科目作业学生数日浮
    private int finSglSubjHwGte3AuStuCountDf;              // 认证学生当月完成3套及以上任一科目作业学生数日浮
    private int finSglSubjHwGte3IncAuStuCountDf;              // 认证学生当月完成3套及以上任一科目作业学生数（新增）日浮
    private int finSglSubjHwGte3StBfAuStuCountDf;              // 认证学生当月完成3套及以上任一科目作业学生数（短回）日浮
    private int finSglSubjHwGte3LtBfAuStuCountDf;              // 认证学生当月完成3套及以上任一科目作业学生数（长回）日浮

    // 初高中部分
    // 数学
    private int finMathAnshEq1StuCountDf;              // 当月作答1次数学试卷学生数日浮
    private int finMathAnshGte2StuCountDf;              // 当月作答2次及以上数学试卷学生数日浮
    private int finMathAnshGte2IncStuCountDf;              // 当月作答2次及以上数学试卷学生数（新增）日浮
    private int finMathAnshGte2BfStuCountDf;              // 当月作答2次及以上数学试卷学生数（回流）日浮

    // 英语
    private int finEngAnshEq1StuCountDf;              // 当月作答1次英语试卷学生数日浮
    private int finEngAnshGte2StuCountDf;              // 当月作答2次及以上英语试卷学生数日浮
    private int finEngAnshGte2IncStuCountDf;              // 当月作答2次及以上英语试卷学生数（新增）日浮
    private int finEngAnshGte2BfStuCountDf;              // 当月作答2次及以上英语试卷学生数（回流）日浮

    // 物理
    private int finPhyAnshEq1StuCountDf;              // 当月作答1次物理试卷学生数日浮
    private int finPhyAnshGte2StuCountDf;              // 当月作答2次及以上物理试卷学生数日浮
    private int finPhyAnshGte2IncStuCountDf;              // 当月作答2次及以上物理试卷学生数（新增）日浮
    private int finPhyAnshGte2BfStuCountDf;              // 当月作答2次及以上物理试卷学生数（回流）日浮

    // 化学
    private int finCheAnshEq1StuCountDf;              // 当月作答1次化学试卷学生数日浮
    private int finCheAnshGte2StuCountDf;              // 当月作答2次及以上化学试卷学生数日浮
    private int finCheAnshGte2IncStuCountDf;              // 当月作答2次及以上化学试卷学生数（新增）日浮
    private int finCheAnshGte2BfStuCountDf;              // 当月作答2次及以上化学试卷学生数（回流）日浮

    // 生物
    private int finBiolAnshEq1StuCountDf;              // 当月作答1次生物试卷学生数日浮
    private int finBiolAnshGte2StuCountDf;              // 当月作答2次及以上生物试卷学生数日浮
    private int finBiolAnshGte2IncStuCountDf;              // 当月作答2次及以上生物试卷学生数（新增）日浮
    private int finBiolAnshGte2BfStuCountDf;              // 当月作答2次及以上生物试卷学生数（回流）日浮

    // 语文
    private int finChnAnshEq1StuCountDf;              // 当月作答1次语文试卷学生数日浮
    private int finChnAnshGte2StuCountDf;              // 当月作答2次及以上语文试卷学生数日浮
    private int finChnAnshGte2IncStuCountDf;              // 当月作答2次及以上语文试卷学生数（新增）日浮
    private int finChnAnshGte2BfStuCountDf;              // 当月作答2次及以上语文试卷学生数（回流）日浮

    // 历史
    private int finHistAnshEq1StuCountDf;              // 当月作答1次历史试卷学生数日浮
    private int finHistAnshGte2StuCountDf;              // 当月作答2次及以上历史试卷学生数日浮
    private int finHistAnshGte2IncStuCountDf;              // 当月作答2次及以上历史试卷学生数（新增）日浮
    private int finHistAnshGte2BfStuCountDf;              // 当月作答2次及以上历史试卷学生数（回流）日浮

    // 地理
    private int finGeogAnshEq1StuCountDf;              // 当月作答1次地理试卷学生数日浮
    private int finGeogAnshGte2StuCountDf;              // 当月作答2次及以上地理试卷学生数日浮
    private int finGeogAnshGte2IncStuCountDf;              // 当月作答2次及以上地理试卷学生数（新增）日浮
    private int finGeogAnshGte2BfStuCountDf;              // 当月作答2次及以上地理试卷学生数（回流）日浮

    // 政治
    private int finPolAnshEq1StuCountDf;              // 当月作答1次政治试卷学生数日浮
    private int finPolAnshGte2StuCountDf;              // 当月作答2次及以上政治试卷学生数日浮
    private int finPolAnshGte2IncStuCountDf;              // 当月作答2次及以上政治试卷学生数（新增）日浮
    private int finPolAnshGte2BfStuCountDf;              // 当月作答2次及以上政治试卷学生数（回流）日浮

    // 中学英语月活
    private int finEngHwEq1AuStuCountDf;              // 认证学生当月完成1套英语作业学生数日浮
    private int finEngHwEq2AuStuCountDf;              // 认证学生当月完成2套英语作业学生数日浮
    private int finEngHwGte3AuStuCountDf;              // 认证学生当月完成3套及以上英语作业学生数日浮

    public Map<String, Integer> fetchDfData(){
        if(SchoolLevel.JUNIOR.getLevel() == schoolLevel){

        }else if(SchoolLevel.MIDDLE.getLevel() == schoolLevel){

        }else if(SchoolLevel.HIGH.getLevel() == schoolLevel){

        }
        return new HashMap<>();
    }

}
