package com.voxlearning.utopia.service.parent.homework.impl.template.bookList.intelligentTeaching;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserProgressLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserProgress;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * 默认教材
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Named("IntelligentTeachingDefaultBookProcessor")
public class DefaultBookProcessor implements HomeworkProcessor {
    //local variables
    @Inject
    private HomeworkUserProgressLoader homeworkUserProgressLoader;
    UtopiaCache utopiaCache = CacheSystem.CBS.getCache("flushable");

    //Logic

    /**
     * exec
     *
     * @param hc args
     */
    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam homeworkParam = hc.getHomeworkParam();
        Long studentId = homeworkParam.getStudentId();
        String bizType = homeworkParam.getBizType();

        //上次选择教材
        Map<String, String> select = utopiaCache.load(HomeworkUtil.generatorID(studentId, bizType, "selectUnit"));
        String selectBookId = ObjectUtils.get(()->select.get("bookId"));
        //上次学习教材
        if(ObjectUtils.anyBlank(selectBookId)){
            HomeworkUserProgress homeworkUserProgress = homeworkUserProgressLoader.loadUserProgress(studentId, bizType);
            selectBookId = ObjectUtils.get(()->homeworkUserProgress.getUserProgresses().get(0).getBookId());
        }
        //第一个教材
        List<Map<String, Object>> bookList = (List<Map<String, Object>>)hc.getData().get("bookList");
        if(!ObjectUtils.anyBlank(selectBookId)){
            String _selectBookId = selectBookId;
            if(!bookList.stream().anyMatch(b->b.get("value").equals(_selectBookId))){
                selectBookId = null;
            }
        }
        if(ObjectUtils.anyBlank(selectBookId)){
            selectBookId = (String)bookList.get(0).get("value");
        }
        // 选择的教材
        hc.getData().put("selectedBookId", selectBookId);
    }
}
