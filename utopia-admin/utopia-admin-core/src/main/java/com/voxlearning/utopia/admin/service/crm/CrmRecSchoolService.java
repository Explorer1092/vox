package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.admin.dao.CrmRecSchoolDao;
import com.voxlearning.utopia.api.constant.CrmReporterVerifyMode;
import com.voxlearning.utopia.api.constant.CrmReporterVerifyStatus;
import com.voxlearning.utopia.entity.crm.CrmRecSchool;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jiang wei on 2016/7/28.
 */
@Named
public class CrmRecSchoolService {
    @Inject
    private CrmRecSchoolDao crmRecSchoolDao;


    public List<CrmRecSchool> loadSchoolsByType(Integer provinceId, Integer cityId, Integer countyId, String schoolName, String status) {
        String verify = "N";
        if (StringUtils.equals(status, "已通过")) {
            verify = "Y";
            List<CrmRecSchool> recSchoolList =crmRecSchoolDao.findSchoolsByAllType(provinceId, cityId, countyId, schoolName, status, verify);
            generateRecSchoolList(recSchoolList,status,verify);
            return recSchoolList;

        }
        List<CrmRecSchool> recSchoolList = crmRecSchoolDao.findSchoolsByAllType(provinceId, cityId, countyId, schoolName, status, verify);
        generateRecSchoolList(recSchoolList,status,verify);
        recSchoolList.stream().filter(crshcool -> StringUtils.equals(crshcool.getStatus(), "已通过")).forEach(recSchoolList::remove);
        return recSchoolList;
    }


    public List<CrmRecSchool> loadSchoolsByCityAndName(Integer provinceId, Integer cityId, String schoolName, String status) {
        String verify = "N";
        if (StringUtils.equals(status, "已通过")) {
            verify = "Y";
            List<CrmRecSchool> recSchoolList = crmRecSchoolDao.findSchoolsByCityAndName(provinceId, cityId, schoolName, status, verify);
            generateRecSchoolList(recSchoolList,status,verify);
            return recSchoolList;

        }
        List<CrmRecSchool> recSchoolList = crmRecSchoolDao.findSchoolsByCityAndName(provinceId, cityId, schoolName, status, verify);
        generateRecSchoolList(recSchoolList,status,verify);
        recSchoolList.stream().filter(crshcool -> StringUtils.equals(crshcool.getStatus(), "已通过")).forEach(recSchoolList::remove);
        return recSchoolList;
    }

    public List<CrmRecSchool> loadSchoolsByRegion(Integer provinceId, Integer cityId, Integer countyId, String status) {
        String verify = "N";
        if (StringUtils.equals(status, "已通过")) {
            verify = "Y";
            List<CrmRecSchool> recSchoolList = crmRecSchoolDao.findSchoolsByRegion(provinceId, cityId, countyId, status, verify);
            generateRecSchoolList(recSchoolList,status,verify);
            return recSchoolList;
        }
        List<CrmRecSchool> recSchoolList = crmRecSchoolDao.findSchoolsByRegion(provinceId, cityId, countyId, status, verify);
        generateRecSchoolList(recSchoolList,status,verify);
        recSchoolList.stream().filter(crshcool -> crshcool != null && StringUtils.equals(crshcool.getStatus(), "已通过")).forEach(recSchoolList::remove);
        return recSchoolList;
    }

    public List<CrmRecSchool> loadSchoolsByStatus(String status) {
        String verify = "N";
        if (StringUtils.equals(status, "已通过")) {
            verify = "Y";
            List<CrmRecSchool> recSchoolList = crmRecSchoolDao.findSchoolsByStatus(status, verify);
            generateRecSchoolList(recSchoolList,status,verify);
            return recSchoolList;

        }
        List<CrmRecSchool> recSchoolList = crmRecSchoolDao.findSchoolsByStatus(status, verify);
        generateRecSchoolList(recSchoolList,status,verify);
        recSchoolList.stream().filter(crshcool -> StringUtils.equals(crshcool.getStatus(), "已通过")).forEach(recSchoolList::remove);
        return recSchoolList;
    }

    public List<CrmRecSchool> loadSchoolsByCityAndProvince(Integer provinceId, Integer cityId, String status) {
        String verify = "N";
        if (StringUtils.equals(status, "已通过")) {
            verify = "Y";
            List<CrmRecSchool> recSchoolList = crmRecSchoolDao.findSchoolsByCityAndProvince(provinceId, cityId, status, verify);
            generateRecSchoolList(recSchoolList,status,verify);
            return recSchoolList;

        }
        List<CrmRecSchool> recSchoolList = crmRecSchoolDao.findSchoolsByCityAndProvince(provinceId, cityId, status, verify);
        generateRecSchoolList(recSchoolList,status,verify);
        recSchoolList.stream().filter(crshcool -> StringUtils.equals(crshcool.getStatus(), "已通过")).forEach(recSchoolList::remove);
        return recSchoolList;
    }


    public void passRecSchoolInfo(Integer schoolId, String schoolName, String schooladdr, Double schoolblat, Double schoolblon, String auditor, Date updateTime) {


        if (schoolId == null) {
            return;
        }
        List<CrmRecSchool> crmRecSchools = crmRecSchoolDao.findSchoolById(schoolId);
        for (CrmRecSchool crmRecSchool : crmRecSchools) {
            crmRecSchool.setSchoolName(schoolName);
            crmRecSchool.setAddr(schooladdr);
            crmRecSchool.setBlat(schoolblat);
            crmRecSchool.setBlon(schoolblon);
            crmRecSchool.setVerify("Y");
            crmRecSchool.setAuditor(auditor);
            crmRecSchool.setVerifyMode(CrmReporterVerifyMode.MANU.name());
            crmRecSchool.setStatus(CrmReporterVerifyStatus.已通过.name());
            crmRecSchool.setAuditResult(1);
            crmRecSchool.setUpdateTime(updateTime);
            crmRecSchoolDao.updateCrmRecSchool(crmRecSchool);
        }


    }

    public void rejectRecSchoolInfo(Integer schoolId, String auditor, Date updateTime) {


        if (schoolId == null) {
            return;
        }
        List<CrmRecSchool> crmRecSchools = crmRecSchoolDao.findSchoolById(schoolId);
        for (CrmRecSchool crmRecSchool : crmRecSchools) {
            crmRecSchool.setVerify("N");
            crmRecSchool.setAuditor(auditor);
            crmRecSchool.setVerifyMode(CrmReporterVerifyMode.NULL.name());
            crmRecSchool.setStatus(CrmReporterVerifyStatus.已驳回.name());
            crmRecSchool.setAuditResult(2);
            crmRecSchool.setUpdateTime(updateTime);
            crmRecSchoolDao.updateCrmRecSchool(crmRecSchool);
        }


    }

    private void generateRecSchoolList(List<CrmRecSchool> list,String status,String verify){
        Set<Integer> idSet=new HashSet<>();
        for (CrmRecSchool crmSchool:list) {
            if (idSet.contains(crmSchool.getSchoolId())){
                continue;
            }else {
                idSet.add(crmSchool.getSchoolId());
            }
        }
        list.clear();
        for (Integer schoolId:idSet){
            CrmRecSchool crmRecSchoolResult=crmRecSchoolDao.findSchoolByIdAndStatus(schoolId,status,verify).get(0);
            list.add(crmRecSchoolResult);
        }
    }

}
