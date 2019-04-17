package com.voxlearning.utopia.entity.crm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * CrmKlxGroupStudentInfo 快乐学班组学生信息
 *
 * @author song.wang
 * @date 2017/4/5
 */
@Getter
@Setter
@NoArgsConstructor
public class CrmKlxGroupStudentInfo implements Serializable {

    private static final long serialVersionUID = 5294063305650175803L;
    private Long klxtn; // 学生快乐学考号
    private Boolean islmcsactive;// 上月当前科目是否活跃
    private Boolean istmcsactive;//本月当前科目是否活跃
    private Integer tmcsanshcount;//学生当月答题卡作答此科目试卷数
    private Integer lmcsanshcount;//学生上月答题卡作答此科目试卷数

}
