package com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 讲练测服务接口
 *
 * @author Wenlong Meng
 * @since Feb 13, 2019
 */
@ServiceVersion(version = "20190222")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
public interface IntelligentTeachingService {

    //Logic
    /**
     * 根据科目id获取可用教材id列表
     *
     * @param subjectId 科目id
     * @return
     */
    List<String> loadBookIds(Integer subjectId);

    /**
     * 根据课时id获取讲练测课程
     *
     * @param sectionId 课时id
     * @return
     */
    List<IntelDiagnosisCourse> loadCoursesBySectionId(String sectionId);

    /**
     * 查询讲练测题id对应题id
     *
     * @param iDQId 讲练测题id
     * @return
     */
    String loadQuestionIdByIDQId(String iDQId);

    /**
     * 查询题qid查询对应的题zid
     *
     * @param qid 题id
     * @return
     */
    String loadZIdByQId(String qid);


}
