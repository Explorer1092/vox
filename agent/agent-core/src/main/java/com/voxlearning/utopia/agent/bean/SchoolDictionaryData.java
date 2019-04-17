package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/5/3.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDictionaryData implements Serializable, Comparable{
    private static final long serialVersionUID = -5191754937854699919L;

    private Long id;                        //数据ID
    private Long schoolId;                  //学校ID
    private String schoolName;              //学校名称
    private Integer regionCode;             //所在地区ID
    private String regionName;              //所在地区名称
    private String citySettlement;          //高渗/低渗
    private String marketStuLevel;          //学校级别

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
