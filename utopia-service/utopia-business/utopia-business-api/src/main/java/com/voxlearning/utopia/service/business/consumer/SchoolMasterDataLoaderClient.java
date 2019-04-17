package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.business.api.SchoolMasterDataLoader;
import com.voxlearning.utopia.business.api.entity.ClassStudySitutation;
import com.voxlearning.utopia.service.business.base.AbstractSchoolMasterDataLoader;
import com.voxlearning.utopia.service.user.api.entities.School;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * /**
 *
 * @author fugui.chang
 * @since 2016-9-27
 */
public class SchoolMasterDataLoaderClient extends AbstractSchoolMasterDataLoader {
    @ImportService(interfaceClass = SchoolMasterDataLoader.class)
    private SchoolMasterDataLoader remoteReference;

    @Override
    public Map<String, List<Map<String, Object>>> getKnowledgeAbilityAnalysisData(School school, Long schoolId, String subject, Long beginDt, Long endDt) {
        if (schoolId == null || StringUtils.isBlank(subject) || beginDt == null || endDt == null) {
            return Collections.emptyMap();
        }
        return remoteReference.getKnowledgeAbilityAnalysisData(school, schoolId, subject, beginDt, endDt);
    }

    @Override
    public List<Map<String, Object>> getSchoolSitutaion(Long schoolId, Long yearchmonth) {
        if (schoolId == null || yearchmonth == null) {
            return Collections.emptyList();
        }
        return remoteReference.getSchoolSitutaion(schoolId, yearchmonth);
    }

    @Override
    public Map<String, List<ClassStudySitutation>> loadClassStudySitutationBySchoolIdDtSubjectData(Long schoolId, Long dt, String subject) {
        if (schoolId == null || dt == null || subject == null) {
            return Collections.emptyMap();
        }
        return remoteReference.loadClassStudySitutationBySchoolIdDtSubjectData(schoolId, dt, subject);
    }
}
