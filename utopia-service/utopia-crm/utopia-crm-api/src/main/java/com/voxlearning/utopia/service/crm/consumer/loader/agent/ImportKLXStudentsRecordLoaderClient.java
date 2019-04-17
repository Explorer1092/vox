package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.ImportKLXStudentsRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.ImportKLXStudentsRecordLoader;

import java.util.Collection;
import java.util.List;

/**
 * Created by tao.zang
 * on 2017/4/11.
 */
public class ImportKLXStudentsRecordLoaderClient implements ImportKLXStudentsRecordLoader {

    @ImportService(interfaceClass = ImportKLXStudentsRecordLoader.class)
    private ImportKLXStudentsRecordLoader remoteReference;

    public List<ImportKLXStudentsRecord> findByOperatorIdAndSourceType(Long OperatorId, String SourceType) {
        return remoteReference.findByOperatorIdAndSourceType(OperatorId,SourceType);
    }

    @Override
    public void saveImportKLXStudentsRecords(Collection<ImportKLXStudentsRecord> importKLXStudentsRecords) {
        remoteReference.saveImportKLXStudentsRecords(importKLXStudentsRecords);
    }

    @Override
    public ImportKLXStudentsRecord findByRecordId(String recordId) {
        return remoteReference.findByRecordId(recordId);
    }
}
