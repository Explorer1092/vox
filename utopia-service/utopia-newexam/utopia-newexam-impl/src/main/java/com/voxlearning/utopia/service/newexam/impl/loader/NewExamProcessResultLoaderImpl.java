package com.voxlearning.utopia.service.newexam.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.loader.NewExamProcessResultLoader;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/7.
 */
@Named
@Service(interfaceClass = NewExamProcessResultLoader.class)
@ExposeService(interfaceClass = NewExamProcessResultLoader.class)
public class NewExamProcessResultLoaderImpl extends NewExamSpringBean implements NewExamProcessResultLoader {

    @Override
    public NewExamProcessResult loadById(String id) {
        return newExamProcessResultDao.load(id);
    }

    @Override
    public Map<String, NewExamProcessResult> loadByIds(Collection<String> ids) {
        return newExamProcessResultDao.loads(ids);
    }
}
