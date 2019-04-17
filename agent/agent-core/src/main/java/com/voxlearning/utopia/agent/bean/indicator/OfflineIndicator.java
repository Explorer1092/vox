package com.voxlearning.utopia.agent.bean.indicator;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * OfflineIndicator
 *
 * @author song.wang
 * @date 2018/8/2
 */
@Getter
@Setter
public class OfflineIndicator implements Serializable {

    private Integer klxTotalNum;            // 快乐学考号数

    // 结算过线上作业学生本月作答1次及以上试卷学生数（周测）
    private Integer settlementNumSglSubj;                  // 所有科目
    private Integer settlementNumMath;                     // 数学
    private Integer settlementNumPhy;                      // 物理
    private Integer settlementNumChe;                      // 化学
    private Integer settlementNumBio;                      // 生物
    private Integer settlementNumEng;                      // 英语
    private Integer settlementNumChi;                      // 语文
    private Integer settlementNumPol;                      // 政治
    private Integer settlementNumHis;                      // 历史
    private Integer settlementNumGeo;                      // 地理
    private Integer settlementNumSci;                      // 科学
    private Integer settlementNumHisSoc;                   // 历史与社会
    private Integer settlementNumGen;                      // 通用技术
    private Integer settlementNum;                         //老师、班组指标不需要加科目后缀

    // 结算过线上作业学生本月作答2次及以上试卷学生数（周测）
    private Integer settlementGte2NumSglSubj;                  // 所有科目
    private Integer settlementGte2NumMath;                     // 数学
    private Integer settlementGte2NumPhy;                      // 物理
    private Integer settlementGte2NumChe;                      // 化学
    private Integer settlementGte2NumBio;                      // 生物
    private Integer settlementGte2NumEng;                      // 英语
    private Integer settlementGte2NumChi;                      // 语文
    private Integer settlementGte2NumPol;                      // 政治
    private Integer settlementGte2NumHis;                      // 历史
    private Integer settlementGte2NumGeo;                      // 地理
    private Integer settlementGte2NumSci;                      // 科学
    private Integer settlementGte2NumHisSoc;                   // 历史与社会
    private Integer settlementGte2NumGen;                      // 通用技术
    private Integer settlementGte2Num;                         //老师、班组指标不需要加科目后缀

    // 未结算过线上作业学生本月作答1次及以上试卷学生数（周测）
    private Integer unsettlementNumSglSubj;                  // 所有科目
    private Integer unsettlementNumMath;                     // 数学
    private Integer unsettlementNumPhy;                      // 物理
    private Integer unsettlementNumChe;                      // 化学
    private Integer unsettlementNumBio;                      // 生物
    private Integer unsettlementNumEng;                      // 英语
    private Integer unsettlementNumChi;                      // 语文
    private Integer unsettlementNumPol;                      // 政治
    private Integer unsettlementNumHis;                      // 历史
    private Integer unsettlementNumGeo;                      // 地理
    private Integer unsettlementNumSci;                      // 科学
    private Integer unsettlementNumHisSoc;                   // 历史与社会
    private Integer unsettlementNumGen;                      // 通用技术
    private Integer unsettlementNum;                         //老师、班组指标不需要加科目后缀

    // 未结算过线上作业学生本月作答2次及以上试卷学生数（周测）
    private Integer unsettlementGte2NumSglSubj;                  // 所有科目
    private Integer unsettlementGte2NumMath;                     // 数学
    private Integer unsettlementGte2NumPhy;                      // 物理
    private Integer unsettlementGte2NumChe;                      // 化学
    private Integer unsettlementGte2NumBio;                      // 生物
    private Integer unsettlementGte2NumEng;                      // 英语
    private Integer unsettlementGte2NumChi;                      // 语文
    private Integer unsettlementGte2NumPol;                      // 政治
    private Integer unsettlementGte2NumHis;                      // 历史
    private Integer unsettlementGte2NumGeo;                      // 地理
    private Integer unsettlementGte2NumSci;                      // 科学
    private Integer unsettlementGte2NumHisSoc;                   // 历史与社会
    private Integer unsettlementGte2NumGen;                      // 通用技术
    private Integer unsettlementGte2Num;                         //老师、班组指标不需要加科目后缀

    // 扫描试卷老师数
    private Integer scanTeacherNumMath;                     // 数学
    private Integer scanTeacherNumPhy;                      // 物理
    private Integer scanTeacherNumChe;                      // 化学
    private Integer scanTeacherNumBio;                      // 生物
    private Integer scanTeacherNumEng;                      // 英语
    private Integer scanTeacherNumChi;                      // 语文
    private Integer scanTeacherNumPol;                      // 政治
    private Integer scanTeacherNumHis;                      // 历史
    private Integer scanTeacherNumGeo;                      // 地理
    private Integer scanTeacherNumSci;                      // 科学
    private Integer scanTeacherNumHisSoc;                   // 历史与社会
    private Integer scanTeacherNumGen;                      // 通用技术

    private Integer scanStuNumSglSubj;                      // 扫描学生数数

    // 本学期扫描学生数  每个学生每学期只计算1次（大考+周测）
    private Integer scanTermStuNumSglSubj;                  // 使用sumtype=month取该指标

    private Integer scanPaperNum;           //扫描试卷套数
}
