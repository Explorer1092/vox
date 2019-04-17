package com.voxlearning.utopia.service.psr.termreport.client;

import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.psr.entity.termreport.*;

/**
 * 学期报告，个性化题包推荐
 * Created by mingming.zhao on 2016/10/20
 */
public interface IPsrTermReportIPackageLoaderClient extends IPingable {
    /**
     * 题包获取接口
     *
     * @param groupId    班级id
     * @param unitId   单元id
     * @return GroupUnitReportPackage， 包含老师布置题数目，学生答题情况
     */
    GroupUnitReportPackage loadGroupUnitReportPackage(Integer groupId, String unitId);

    /**
     * 题包获取接口
     *
     *  @param yearId    月份
     * @param termId    学期id
     * @param groupId   班级id
     * @param subjectName 科目名称
     * @return GroupUnitReportPackage， 包含老师布置题数目，学生答题情况
     */
    TermReportPackage loadTermtReportPackage(Integer yearId, Integer termId, Integer groupId, String subjectName);

    /* 测试题包获取接口,for php dubbo*/
    //TODO 上线之后去掉
    GroupUnitReportPackage testLoadGroupUnitReportPackage(Integer groupId, String unitId);

    TermReportPackage testLoadTermtReportPackage(Integer yearId, Integer termId, Integer groutId, String subjectName);

}
