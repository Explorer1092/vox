package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class AgentDictSchoolExportData implements Serializable {
    private static final long serialVersionUID = 6650283831910539901L;
    private Long regionGroupId;          //大区ID
    private String regionGroupName;      //大区名称
    private Long areaGroupId;            //区域ID
    private String areaGroupName;        //区域名称
    private Long cityGroupId;            //分区ID
    private String cityGroupName;        //分区名称
    private Long cityManagerId;             //市经理ID
    private String cityManagerName;         //市经理姓名
    private Long businessDeveloperId;       //专员ID
    private String businessDeveloperName;   //专员姓名
}

