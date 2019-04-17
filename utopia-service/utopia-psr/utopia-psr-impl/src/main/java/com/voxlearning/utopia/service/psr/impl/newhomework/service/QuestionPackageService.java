package com.voxlearning.utopia.service.psr.impl.newhomework.service;

import com.google.common.collect.Maps;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.psr.entity.newhomework.MathQuestionBox;
import com.voxlearning.utopia.service.psr.entity.newhomework.QuestionPackage;
import com.voxlearning.utopia.service.psr.impl.dao.newhomework.QuestionPackageDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/27
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
@Named
public class QuestionPackageService extends SpringContainerSupport {

    @Inject
    private QuestionPackageDao questionPackageDao;


    public Map<String, MathQuestionBox> loadQuestionPackagesBySectionId(String bkcId) {
        List<QuestionPackage> packages = questionPackageDao.getPackagesBySectionIds(Collections.singletonList(bkcId)).get(bkcId);
        if (packages.isEmpty()) return Collections.emptyMap();
        Map<String, MathQuestionBox> result = Maps.newHashMap();
        for (QuestionPackage questionPackage : packages) {
            String pakType = questionPackage.getPakType();
            MathQuestionBox mathQuestionBox = new MathQuestionBox();
            mathQuestionBox.setType(7);
            mathQuestionBox.setId("OC_1_7_1_" + questionPackage.getId());
            mathQuestionBox.setQuestionIds(questionPackage.getQuestionIds());
            mathQuestionBox.setAlternative(true);
            result.put(pakType, mathQuestionBox);
        }
        return result;
    }

    public Map<String,Map<String, MathQuestionBox>> loadQuestionPackagesBySectionIds(List<String> bkcIds) {
        Map<String,List<QuestionPackage>> packagesMap = questionPackageDao.getPackagesBySectionIds(bkcIds);
        if (packagesMap.isEmpty()) return Collections.emptyMap();

        Map<String,Map<String, MathQuestionBox>> ret = Maps.newHashMap();
        for(Map.Entry<String,List<QuestionPackage>> entry : packagesMap.entrySet()){
            String bkcId = entry.getKey();
            List<QuestionPackage> packages = entry.getValue();
            if(packages.isEmpty()) {
                ret.put(bkcId,Collections.emptyMap());
                continue;
            }
            Map<String, MathQuestionBox> result = Maps.newHashMap();
            packages.stream().forEach(
                    e->{
                        String pakType = e.getPakType();
                        MathQuestionBox mathQuestionBox = new MathQuestionBox();
                        mathQuestionBox.setType(7);
                        mathQuestionBox.setId("OC_1_7_1_" + e.getId());
                        mathQuestionBox.setQuestionIds(e.getQuestionIds());
                        mathQuestionBox.setAlternative(true);
                        result.put(pakType, mathQuestionBox);
                    }
            );
            ret.put(bkcId,result);
        }
        return ret;
    }

    public Map<String,List<String>> loadQuestionsByUnitIds(List<String> bkcIds) {
        Map<String,List<String>> ret = Maps.newHashMap();
        Map<String,List<QuestionPackage>> packagesMap = questionPackageDao.getPackagesByUnitIds(bkcIds);

        for(Map.Entry<String,List<QuestionPackage>> entry : packagesMap.entrySet()){
            String bkcId = entry.getKey();
            List<QuestionPackage> packages = entry.getValue();

            if(packages.isEmpty()) {
                ret.put(bkcId,Collections.emptyList());
                continue;
            }
            List<String> result = packages.stream().map(QuestionPackage::getQuestionIds).flatMap(Collection::stream).collect(Collectors.toList());
            ret.put(bkcId,result);
        }
        return ret;
    }
}
