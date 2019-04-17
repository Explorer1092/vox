package com.voxlearning.utopia.service.newhomework.impl.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkProcessResultHBase;
import com.voxlearning.utopia.service.newhomework.impl.hbase.HomeworkProcessResultHBasePersistence;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveHomeworkProcessResultHBaseCommand;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/8/15
 */
@Named
public class SaveHomeworkProcessResultCommandHandler {

    @Inject private HomeworkProcessResultHBasePersistence homeworkProcessResultHBasePersistence;

    public void handle(SaveHomeworkProcessResultHBaseCommand command) throws Exception {
        if (command != null && CollectionUtils.isNotEmpty(command.getResults())) {
            List<HomeworkProcessResultHBase> list = transform(command.getResults());
            homeworkProcessResultHBasePersistence.inserts(list);
        }
    }

    private List<HomeworkProcessResultHBase> transform(List<SubHomeworkProcessResult> origList) {
        List<HomeworkProcessResultHBase> destList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(origList)) {
            for (SubHomeworkProcessResult orig : origList) {
                HomeworkProcessResultHBase dest = new HomeworkProcessResultHBase();
                PropertiesUtils.copyProperties(dest, orig);
                destList.add(dest);
            }
        }
        return destList;
    }
}
