package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.utopia.admin.dao.CrmUgcStudentResultDao;
import com.voxlearning.utopia.entity.crm.CrmUgcStudentResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jiang wei on 2016/7/28.
 */


@Named
public class CrmUgcStudentResultService {
    @Inject
    private CrmUgcStudentResultDao crmUgcStudentResultDao;

    public List<CrmUgcStudentResult> loadUgcStudentResultByReleId(Integer id) {
        List<CrmUgcStudentResult> results = crmUgcStudentResultDao.findResults(id);
        generateStudentList(results,id);
        return results;


    }

    public void passUgcStudentResult(List<String> idList, Integer schoolId) {
        for (String id : idList) {
            CrmUgcStudentResult crmUgcStudentResult = crmUgcStudentResultDao.findResultById(id);
            crmUgcStudentResult.setStatus(2);
            crmUgcStudentResult.setReleId(schoolId);
            crmUgcStudentResultDao.updateUgcStudentResult(crmUgcStudentResult);
        }

    }

    public void rejectUgcStudentResult(List<String> idList) {
        for (String id : idList) {
            CrmUgcStudentResult crmUgcStudentResult = crmUgcStudentResultDao.findResultById(id);
            crmUgcStudentResult.setStatus(3);
            crmUgcStudentResult.setReleId(0);
            crmUgcStudentResultDao.updateUgcStudentResult(crmUgcStudentResult);
        }
    }


    private void generateStudentList(List<CrmUgcStudentResult> list,Integer releId){
        Set<Long> idSet=new HashSet<>();
        for (CrmUgcStudentResult crmUgcStudentResult:list) {
            if (idSet.contains(crmUgcStudentResult.getStudentId())){
                continue;
            }else {
                idSet.add(crmUgcStudentResult.getStudentId());
            }
        }
        list.clear();
        for (Long studentId:idSet){
            CrmUgcStudentResult StudentResult=crmUgcStudentResultDao.findResultBySidAndRid(studentId,releId).get(0);
            list.add(StudentResult);
        }
    }

}
