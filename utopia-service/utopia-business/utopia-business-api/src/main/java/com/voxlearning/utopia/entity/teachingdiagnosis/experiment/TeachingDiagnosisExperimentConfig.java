package com.voxlearning.utopia.entity.teachingdiagnosis.experiment;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.DiagnosisExperimentConfig;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.DiagnosisExperimentContent;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.ExperimentType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "mdl_course_experiment_config")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180725")
public class TeachingDiagnosisExperimentConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private static String SEP = ",";
    @DocumentId
    private String id;
    @DocumentField("group_id")
    private String groupId;
    @DocumentField("group_name")
    private String groupName;
    private ExperimentType type;
    private String name;
    private Status status;
    private List<Integer> grades;
    private List<String> regions;
    private List<String> labels;
    @DocumentField("beginning_prompt_words")
    private String preBonusDescription;
    private Integer bonus;
    @DocumentField("doc_id")
    private String preQuestion;
    @DocumentField("post_doc_id")
    private String postQuestion;
    private List<ExperimentCourseConfig> diagnoses;

    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static TeachingDiagnosisExperimentConfig toExperiment(DiagnosisExperimentContent content) {
        if (content == null) {
            return null;
        }
        TeachingDiagnosisExperimentConfig experimentConfig = new TeachingDiagnosisExperimentConfig();
        experimentConfig.setId(content.getId());
        experimentConfig.setPreBonusDescription(content.getPreDescription());
        experimentConfig.setBonus(content.getBonus());
        experimentConfig.setPreQuestion(content.getPreviewQuestion());
        experimentConfig.setPostQuestion(content.getPostQuestion());
        experimentConfig.setDiagnoses(ExperimentCourseConfig.toCourseConfigList(content.getConfigList()));

        experimentConfig.setGrades(Collections.emptyList());
        experimentConfig.setRegions(Collections.emptyList());
        experimentConfig.setLabels(Collections.emptyList());
        if (StringUtils.isNotBlank(content.getGrades())) {
            List<Integer> grades = new ArrayList<>();
            for(String string : content.getGrades().split(SEP)) {
                grades.add(SafeConverter.toInt(string));
            }
            experimentConfig.setGrades(grades);
        }

        if (StringUtils.isNotBlank(content.getRegions())) {
            experimentConfig.setRegions(Arrays.asList(content.getRegions().split(SEP)));
        }

        if (StringUtils.isNotBlank(content.getLabels())) {
            experimentConfig.setLabels(Arrays.asList(content.getLabels().split(SEP)));
        }

        return experimentConfig;
    }



    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisExperimentConfig.class, id);
    }

    public static String ck_group_id(String groupId) {
        return CacheKeyGenerator.generateCacheKey(TeachingDiagnosisExperimentConfig.class,
                new String[]{"GROUP"},
                new Object[]{groupId});
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Status {
        ONLINE("上线"), OFFLINE("下线"), WAITING("配置中");
        @Getter
        private final String description;
    }


    @Getter
    @Setter
    public static class ExperimentCourseConfig implements Serializable  {
        private static final long serialVersionUID = 4489780176395963148L;
        private String answers;
        private List<String> course_ids;

        public static ExperimentCourseConfig toCourseConfig(DiagnosisExperimentConfig config) {
            ExperimentCourseConfig res = new ExperimentCourseConfig();
            res.setAnswers(config.getAnswers());
            res.setCourse_ids(StringUtils.isNotBlank(config.getCourseIds()) ? Arrays.asList(config.getCourseIds().split(",")) : Collections.emptyList());
            return res;
        }

        public static List<ExperimentCourseConfig> toCourseConfigList(List<DiagnosisExperimentConfig> configList) {
            if (CollectionUtils.isEmpty(configList)) {
                return Collections.emptyList();
            }
            List<ExperimentCourseConfig> res = new ArrayList<>();
            for(DiagnosisExperimentConfig config : configList){
                res.add(toCourseConfig(config));
            }
            return res;
        }
    }
}
