package com.voxlearning.utopia.service.psr.impl.newhomework.loader;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.psr.api.DPSimilarQuestionLoader;
import com.voxlearning.utopia.service.psr.entity.newhomework.MathQuestionBox;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Named
@ExposeService(interfaceClass = DPSimilarQuestionLoader.class)
public class DPSimilarQuestionLoaderImpl extends SpringContainerSupport implements DPSimilarQuestionLoader {

    @Inject private SimilarQuestionLoaderImpl similarQuestionLoader;

    @Override
    public Map<String, List<MathQuestionBox>> loadQuestionPackagesOfSections(Collection<String> catalogIds) {
        return similarQuestionLoader.loadQuestionPackagesOfSections(catalogIds);
    }
}
