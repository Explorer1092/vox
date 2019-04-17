package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.SchoolDepartmentInfo;
import com.voxlearning.utopia.agent.bean.export.ExportAble;

import java.io.Serializable;
import java.util.List;

/**
 * 数据报表数据
 * Created by yaguang.wang on 2017/2/15.
 */
public abstract class ReportData implements ExportAble,Serializable {

    private static final long serialVersionUID = 7443645570572168237L;

    protected static String createNumPer(String num) {
        Double doubleNum = SafeConverter.toDouble(num);
        int intNum = (int) (doubleNum * 10000);
        doubleNum = intNum / 100.0d;
        return doubleNum + "%";
    }

    protected static void fillDepartmentInfo(List<Object> result, SchoolDepartmentInfo departmentInfo) {
        if (departmentInfo == null) {
            result.add("");
            result.add("");
            result.add("");
        } else {
            result.add(departmentInfo.getRegionGroupName());
            result.add(departmentInfo.getGroupName());
            result.add(StringUtils.isNoneBlank(departmentInfo.getBusinessDeveloperName()) ? departmentInfo.getBusinessDeveloperName() : departmentInfo.getCityManagerName());
        }
    }
}
