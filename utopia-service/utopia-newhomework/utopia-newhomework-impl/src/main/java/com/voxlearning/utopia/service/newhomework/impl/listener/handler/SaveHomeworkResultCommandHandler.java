package com.voxlearning.utopia.service.newhomework.impl.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultHBase;
import com.voxlearning.utopia.service.newhomework.impl.hbase.HomeworkResultHBasePersistence;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveHomeworkResultHBaseCommand;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/8/15
 */
@Named
public class SaveHomeworkResultCommandHandler {

    @Inject private HomeworkResultHBasePersistence homeworkResultHBasePersistence;

    public void handle(SaveHomeworkResultHBaseCommand command) throws Exception {
        if (command != null && CollectionUtils.isNotEmpty(command.getResults())) {
            List<HomeworkResultHBase> list = transform(command.getResults());
            homeworkResultHBasePersistence.inserts(list);
        }
    }

    private List<HomeworkResultHBase> transform(List<SubHomeworkResult> origList) {
        List<HomeworkResultHBase> destList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(origList)) {
            for (SubHomeworkResult orig : origList) {
                HomeworkResultHBase dest = new HomeworkResultHBase();
                PropertiesUtils.copyProperties(dest, orig);
                destList.add(dest);
            }
        }
        return destList;
    }
}
