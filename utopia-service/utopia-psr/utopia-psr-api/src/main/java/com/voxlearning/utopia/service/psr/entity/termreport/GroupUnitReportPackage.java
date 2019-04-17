package com.voxlearning.utopia.service.psr.entity.termreport;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.util.List;


/**
 * Created by mingming.zhao on 2016/10/20.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class GroupUnitReportPackage implements Serializable  {
    private static final long serialVersionUID = 2609165752530533242L;
    private List<StudentGroupUnitReport> StudentGroupUnitReport;
    private Integer LayoutHomeworkTimes;
}
