package com.voxlearning.utopia.service.psr.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.psr.entity.newhomework.MathQuestionBox;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2019.02.28")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPSimilarQuestionLoader {

    Map<String, List<MathQuestionBox>> loadQuestionPackagesOfSections(Collection<String> catalogIds);

}
