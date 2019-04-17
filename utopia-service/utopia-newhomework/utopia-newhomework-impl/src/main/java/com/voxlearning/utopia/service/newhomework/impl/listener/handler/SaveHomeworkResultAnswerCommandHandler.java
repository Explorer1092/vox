package com.voxlearning.utopia.service.newhomework.impl.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultAnswerHBase;
import com.voxlearning.utopia.service.newhomework.impl.hbase.HomeworkResultAnswerHBasePersistence;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveHomeworkResultAnswerHBaseCommand;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/8/21
 */
@Named
public class SaveHomeworkResultAnswerCommandHandler {

    @Inject private HomeworkResultAnswerHBasePersistence homeworkResultAnswerHBasePersistence;

    public void handle(SaveHomeworkResultAnswerHBaseCommand command) throws Exception {
        if (command != null && CollectionUtils.isNotEmpty(command.getResults())) {
            List<HomeworkResultAnswerHBase> list = transform(command.getResults());
            homeworkResultAnswerHBasePersistence.inserts(list);
        }
    }

    private List<HomeworkResultAnswerHBase> transform(List<SubHomeworkResultAnswer> origList) {
        List<HomeworkResultAnswerHBase> destList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(origList)) {
            for (SubHomeworkResultAnswer orig : origList) {
                HomeworkResultAnswerHBase dest = new HomeworkResultAnswerHBase();
                PropertiesUtils.copyProperties(dest, orig);
                destList.add(dest);
            }
        }
        return destList;
    }
}
