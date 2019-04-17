package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.utopia.api.constant.VoiceEngineType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author tanguohong
 * @since 2016/6/27
 */
@Data
public class StudentHomeworkAnswer implements Serializable {

    private static final long serialVersionUID = -1148650611336021388L;

    private String questionId;              // 题ID
    private List<List<String>> answer;      // 用户答案
    private Long durationMilliseconds;      // 完成时长
    private List<List<String>> fileUrls;    // 文件地址 用于有作答过程的试题
    //语音类特有属性
    private VoiceEngineType voiceEngineType;    // 语音引擎
    private String voiceCoefficient;            // 打分系数
    private String voiceMode;                   // 打分模式
    // voiceScoringMode = 'Normal'; //正常
    // voiceScoringMode = 'SkipByUser_NetworkError'; //由于网络错误，用户主动要求跳过麦克风
    // voiceScoringMode = 'SkipByUser_DeviceError'; //由于设备错误，用户主动要求跳过麦克风
    // voiceScoringMode = 'SkipByUser_LowScore';//由于读音打分过低，用户主动要求跳过麦克风
    // voiceScoringMode = 'SkipByUser_OtherError'; //由于其他错误，用户主动要求跳过麦克风
    // voiceScoringMode = 'ListenOnly'; //只听句
    // voiceScoringMode = ''; //由于其他错误，用户主动要求跳过麦克风

    private String voiceScoringMode;            // 语音类游戏录音打分模式
    private Integer sentenceType;      // 基础应用才有,同com.voxlearning.utopia.service.content.api.entity.Sentence.type

    // 用于错题订正
    private String sourceQuestionId;        // 原题id
    private QuestionWrongReason wrongReason;// 错题原因

    //口语详情部分
    private List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails; //口语题详情

    private String courseId;            // 巩固课程ID
    private Boolean courseGrasp;        // 课程是否掌握(只针对没有后测题的课程)

    //口语交际 : 对话id
    private String dialogId;

}
