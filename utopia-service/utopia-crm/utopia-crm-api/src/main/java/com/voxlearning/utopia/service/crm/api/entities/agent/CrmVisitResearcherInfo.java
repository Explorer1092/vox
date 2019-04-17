package com.voxlearning.utopia.service.crm.api.entities.agent;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 拜访教研员情况
 * @author deliang.che
 * @since 2018/7/2
 */
@Getter
@Setter
public class CrmVisitResearcherInfo implements Serializable{
    private static final long serialVersionUID = -6115103717085022803L;
    private Long researcherId;      // 教研员ID
    private String researcherName;  // 教研员姓名
    private String conclusion;      // 达成结果
}
