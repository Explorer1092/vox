package com.voxlearning.utopia.agent.bean.group;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 *  部门与其父级部门对应关系拼装类
 *  支持部门级别：市场、大区、区域、分区
 *
 * @author deliang.che
 * @since 2018/6/20
 **/
@Getter
@Setter
public class GroupData implements Serializable {
    private static final long serialVersionUID = -874287741104617783L;
    private Long groupId;           //本部门ID
    private Integer roleId;         //本部门级别
    private Long marketingId;       //市场ID（父级）
    private String marketingName;   //市场名称（父级）
    private Long regionId;          //大区ID（父级）
    private String regionName;      //大区名称（父级）
    private Long areaId;            //区域ID（父级）
    private String areaName;        //区域名称（父级）
    private Long cityId;            //分区ID（父级）
    private String cityName;        //分区名称（父级）
}
