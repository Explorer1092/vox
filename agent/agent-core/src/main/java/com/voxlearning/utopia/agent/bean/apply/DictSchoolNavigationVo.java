package com.voxlearning.utopia.agent.bean.apply;

import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-21 20:00
 **/
@Builder
@Data
public class DictSchoolNavigationVo {
    private Boolean dictSchoolFlag;
    private Boolean managentFlag;
    private Boolean juniorOrHighSchoolFlag;
    private Boolean limitChangeResponsibleTimeFlag;
    private Boolean cityManagerOrBusinessDeveloperFlag;
    private Boolean inAuditFlag;
    private String changeResponsibleOpentime;
}
