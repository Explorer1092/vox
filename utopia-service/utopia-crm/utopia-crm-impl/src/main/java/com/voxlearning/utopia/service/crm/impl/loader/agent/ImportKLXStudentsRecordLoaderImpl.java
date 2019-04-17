package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.ImportKLXStudentsRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.ImportKLXStudentsRecordLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.ImportKLXStudentsRecordDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by tao.zang
 * on 2017/4/11.
 */
@Named
@Service(interfaceClass = ImportKLXStudentsRecordLoader.class)
@ExposeService(interfaceClass = ImportKLXStudentsRecordLoader.class)
public class ImportKLXStudentsRecordLoaderImpl implements ImportKLXStudentsRecordLoader {
    @Inject
    private ImportKLXStudentsRecordDao importKLXStudentsRecordDao;
    public List<ImportKLXStudentsRecord> findByOperatorIdAndSourceType(Long OperatorId, String SourceType) {
        if(OperatorId ==0L || StringUtils.isBlank(SourceType)){
            return new ArrayList();
        }
        return importKLXStudentsRecordDao.loadImportKLXStudentsRecords(OperatorId,SourceType);
    }


    public void saveImportKLXStudentsRecords(Collection<ImportKLXStudentsRecord> importKLXStudentsRecords) {
        if(CollectionUtils.isNotEmpty(importKLXStudentsRecords)){
            importKLXStudentsRecordDao.$inserts(importKLXStudentsRecords);
        }
    }

    @Override
    public ImportKLXStudentsRecord findByRecordId(String recordId) {
        if(StringUtils.isBlank(recordId)){
            return null;
        }
        return importKLXStudentsRecordDao.load(recordId);
    }
}
