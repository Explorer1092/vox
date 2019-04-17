package com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.ai.constant.AIBookStatus;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.data.ChipsEnglishClassInfo;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserSignRecord;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishUserSignRecordDao;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

@Named
public class CCDCR_CourseInfo extends AbstractAiSupport implements IAITask<ChipsContentDailyClassContext> {
    @Inject
    private ChipsEnglishUserSignRecordDao chipsEnglishUserSignRecordDao;

    private final static String VOICE_RADIO_CONFIG = "chips_voice_radio_config";

    @Override
    public void execute(ChipsContentDailyClassContext context) {
        if (context.getStatus() != AIBookStatus.InTime) {
            return;
        }

        ChipsEnglishClassInfo classInfo = new ChipsEnglishClassInfo();
        classInfo.setId(context.getUnit().getId());
        classInfo.setName(context.getUnit().getJsonData().getName());
        classInfo.setCurrentDay(true);
        classInfo.setRank(context.getRank());
        classInfo.setTitle(context.getUnit().getJsonData().getTitle());
        classInfo.setCardTitle(context.getUnit().getJsonData().getImage_title());
        classInfo.setImg(context.getUnit().getJsonData().getCover_image());
        classInfo.setCardDescription(context.getUnit().getJsonData().getImage_discription());

        // 是否完成
        AIUserUnitResultHistory result = aiUserUnitResultHistoryDao.load(context.getUser().getId(), context.getUnit().getId());
        if (result != null && result.getFinished()) {
            classInfo.setFinished(true);
            classInfo.setScore(result.getScore());
            classInfo.setStar(result.getStar());
        } else {
            classInfo.setFinished(false);
        }

        classInfo.setBookId(context.getBook().getId());
        classInfo.setType(Optional.ofNullable(context.getUnit())
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getUnit_type)
                .map(Enum::name)
                .orElse(""));
        classInfo.setTypeDesc(Optional.ofNullable(context.getUnit())
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getUnit_type)
                .map(ChipsUnitType::getDesc)
                .orElse(""));
        context.setClassInfo(classInfo);

        //是否签到
        ChipsEnglishUserSignRecord record = chipsEnglishUserSignRecordDao.loadByUserId(context.getUser().getId()).stream()
                .filter(u -> StringUtils.isNotBlank(u.getBookId()) && StringUtils.equals(u.getBookId(), context.getBookId()))
                .filter(u -> StringUtils.isNotBlank(u.getUnitId()) && StringUtils.equals(u.getUnitId(), context.getUnitId()))
                .findFirst().orElse(null);
        context.setCheckIn(record != null);

        //单元总结页
        String summaryUrl = "";
        ChipsUnitType unitType = Optional.ofNullable(context.getUnit())
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getUnit_type)
                .orElse(ChipsUnitType.unknown);
        switch (unitType) {
            case short_lesson:
                summaryUrl = "/view/mobile/parent/parent_ai/report";
                break;
            case dialogue_practice:
                summaryUrl = "/view/mobile/parent/parent_ai/conversation_practice_report";
                break;
            case topic_learning:
                summaryUrl = "/view/mobile/parent/parent_ai/topic_learning_report";
                break;
            case special_consolidation:
                summaryUrl = "/view/mobile/parent/parent_ai/consolidation_report";
                break;
            case mock_test:
                summaryUrl = "/view/mobile/parent/parent_ai/spoken_mock_test_report";
                break;
            case review_unit:
                summaryUrl = "/view/mobile/parent/parent_ai/consolidation_qa_report";
                break;
            case role_play_unit:
                summaryUrl = "/view/mobile/parent/parent_ai/role_play_report";
                break;
            case mock_test_unit_1:
            case mock_test_unit_2:
                summaryUrl = "/view/mobile/parent/parent_ai/mock_qa_report";
                break;
            case unknown:
                summaryUrl = "/view/mobile/parent/parent_ai/report";
                break;
        }
        summaryUrl = ProductConfig.getMainSiteBaseUrl().replace("http://", "https://") + summaryUrl;
        context.setSummaryUrl(summaryUrl);

        //打分系数
        String voiceRadio = Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(VOICE_RADIO_CONFIG))
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .map(ChipsEnglishPageContentConfig::getValue)
                .map(JsonUtils::fromJson)
                .map(e -> {
                    String val = SafeConverter.toString(e.get(context.getUnit().getId()));
                    if (StringUtils.isBlank(val)) {
                        val = SafeConverter.toString(e.get("default"), "1.0");
                    }
                    return val;
                }).orElse("");
        context.setVoiceRadio(voiceRadio);
    }
}
