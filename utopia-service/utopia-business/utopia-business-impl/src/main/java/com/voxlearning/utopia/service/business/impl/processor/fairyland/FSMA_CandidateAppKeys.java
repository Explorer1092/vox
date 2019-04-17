package com.voxlearning.utopia.service.business.impl.processor.fairyland;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.business.impl.processor.AbstractExecuteTask;

import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 取VendorApps和FairylandProducts的交集作为备选应用
 *
 * @author Ruib
 * @since 2019/1/2
 */
@Named
public class FSMA_CandidateAppKeys extends AbstractExecuteTask<FetchStudentAppContext> {

    @Override
    public void execute(FetchStudentAppContext context) {
        List<Set<String>> iterables = Arrays.asList(context.getVam().keySet(), context.getFpm().keySet());
        context.setCandidates(CollectionUtils.intersection(iterables));
    }
}
