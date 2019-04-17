package com.voxlearning.washington.controller.mobile.student.headline.helper;

import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;

import javax.inject.Named;
import java.util.EnumMap;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/6
 * Time: 14:32
 */
@Named
public class HeadlineExecutorFactory extends ApplicationObjectSupport implements InitializingBean {

    private final EnumMap<ClazzJournalType, AbstractHeadlineExecutor> container = new EnumMap<>(ClazzJournalType.class);

    @Override
    public void afterPropertiesSet() {
        BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), AbstractHeadlineExecutor.class, false, true)
                .values()
                .forEach(this::register);
    }

    private void register(AbstractHeadlineExecutor executor) {
        executor.journalTypes().forEach(journalType -> container.put(journalType, executor));
    }

    /**
     * 获取头条处理对象
     *
     * @param clazzJournalType 记录内容
     */
    public AbstractHeadlineExecutor getHeadlineExecute(ClazzJournalType clazzJournalType) {
        return container.get(clazzJournalType);
    }

}
