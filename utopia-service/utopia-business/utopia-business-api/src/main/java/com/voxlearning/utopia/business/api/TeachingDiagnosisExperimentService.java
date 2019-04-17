package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.DiagnosisExperimentContent;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.DiagnosisExperimentGroup;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.ExperimentType;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentGroupConfig;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180719")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries()
@CyclopsMonitor("utopia")
public interface TeachingDiagnosisExperimentService extends IPingable {
    List<DiagnosisExperimentGroup> fetchAllExperimentGroup(ExperimentType type);

    MapMessage createExperimentGroup(String name, ExperimentType type, String updater);

    MapMessage deleteExperimentGroup(String id);

    MapMessage fetchExperimentInfoById(String id);

    MapMessage createExperiment(String name, String groupId, String updater);

    MapMessage updateExperimentStatus(String id, TeachingDiagnosisExperimentConfig.Status to, String updater);

    MapMessage updateExperiment(DiagnosisExperimentContent content, String updater);

    MapMessage deleteExperiment(String id, String updater);

    TeachingDiagnosisExperimentGroupConfig loadGroupById(String id);

    TeachingDiagnosisExperimentConfig loadExperimentById(String id);

    Map<String, TeachingDiagnosisExperimentConfig> loadExperimentByIds(Collection<String> ids);
}

