package com.voxlearning.utopia.service.parent.homework.impl.template.bookList;

import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractSubProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

import javax.inject.Named;
import java.util.function.Consumer;

@Named
@Processors({
        BookLoaderProcessorSub.class
})
public class BookProcessor extends AbstractSubProcessor implements HomeworkProcessor {

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam homeworkParam = hc.getHomeworkParam();
        Consumer process = subProcessor.get(homeworkParam.getBizType());
        if (process != null) {
            process.accept(hc);
        }
    }
}
