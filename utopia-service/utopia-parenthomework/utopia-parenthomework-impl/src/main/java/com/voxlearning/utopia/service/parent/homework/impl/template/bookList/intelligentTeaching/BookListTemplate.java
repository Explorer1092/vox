package com.voxlearning.utopia.service.parent.homework.impl.template.bookList.intelligentTeaching;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SupportType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractProcessorTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.TemplateProcessor;

import javax.inject.Named;

/**
 * 教材列表模板
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Named("IntelligentTeaching.BookTemplate")
@Processors({
        BookListProcessor.class,
        DefaultBookProcessor.class
})
@SupportType(bizType = "INTELLIGENT_TEACHING", op="bookList")
public class BookListTemplate extends AbstractProcessorTemplate implements TemplateProcessor {

    /**
     * 教材列表
     *
     * @param hc
     * @return
     */
    public void process(HomeworkContext hc) {
        // 流程处理
        processor.accept(hc);

        MapMessage mapMessage = hc.getMapMessage();
        if (mapMessage == null) {
            mapMessage = MapMessage.successMessage();
        }
        if (mapMessage.isSuccess()) {
            mapMessage.add("data", hc.getData());
        }
        hc.setMapMessage(mapMessage);
    }
}
