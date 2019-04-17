package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/2/26
 * \* Time: 3:20 PM
 * \* Description: 处理戴特题型数据，去除if选择结构代码
 * \
 */
@Named
public class DaiTeTypeFactory extends SpringContainerSupport {

    private Map<DaiTeType, DaiTeTypeTemplate> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, DaiTeTypeTemplate> beans = applicationContext.getBeansOfType(DaiTeTypeTemplate.class);
        for (DaiTeTypeTemplate bean : beans.values()) {
            this.templates.put(bean.getDaiTeType(), bean);
        }
    }

    public DaiTeTypeTemplate getTemplate(DaiTeType daiTeType) {
        if (daiTeType == null) {
            return null;
        }
        return this.templates.get(daiTeType);
    }

}
