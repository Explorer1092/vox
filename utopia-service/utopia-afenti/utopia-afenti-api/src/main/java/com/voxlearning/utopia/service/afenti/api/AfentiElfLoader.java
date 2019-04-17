package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiElfPage;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author songtao
 * @since 2019/9/20
 */
@ServiceVersion(version = "20171030")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
public interface AfentiElfLoader extends IPingable {

    List<WrongQuestionLibrary> loadAndUpdateWrongQuestionLibraryByUserIdAndSubject(Long studentId, Subject subject, AfentiWrongQuestionStateType type);

    List<WrongQuestionLibrary> loadWrongQuestionLibraryByUserIdAndSubject(Long studentId, Subject subject);

    Map<String, NewQuestion> loadQuestionsByDocIds(List<String> allDocIds);

    Map<String, NewQuestion> loadQuestionsByDocIds(List<String> allDocIds, int step);

}
