package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.business.api.entity.ClassStudySitutation;
import com.voxlearning.utopia.business.api.entity.KnowledgeAbilityAnalysis;
import com.voxlearning.utopia.business.api.entity.SchoolReportSituation;
import com.voxlearning.utopia.service.user.api.entities.School;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * /**
 *
 * @author fugui.chang
 * @since 2016-9-27
 */
@ServiceVersion(version = "20160927")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface SchoolMasterDataLoader extends IPingable {
    @CacheMethod(type = KnowledgeAbilityAnalysis.class,writeCache = false)//没有插入数据处理，每天清理缓存和每天进入数据，一天的数据可能是缺失的，考虑是接受内，所以方法走缓存，下面两个方法一致
    Map<String, List<Map<String, Object>>> getKnowledgeAbilityAnalysisData(School school, @CacheParameter("SCHOOLID") Long schoolId, @CacheParameter("SUBJECT") String subject, @CacheParameter("BEGINDT") Long beginDt, @CacheParameter("ENDDT") Long endDt);

    @CacheMethod(type = SchoolReportSituation.class,writeCache = false)
    List<Map<String, Object>> getSchoolSitutaion(@CacheParameter("SCHOOLID") Long schoolId, @CacheParameter("YEARCHMONTH") Long yearchmonth);

    @CacheMethod(type = ClassStudySitutation.class,writeCache = false)
    Map<String, List<ClassStudySitutation>> loadClassStudySitutationBySchoolIdDtSubjectData(@CacheParameter("SCHOOLID") Long schoolId, @CacheParameter("YEARCHMONTH") Long dt, @CacheParameter("SUBJECT") String subject);

}
