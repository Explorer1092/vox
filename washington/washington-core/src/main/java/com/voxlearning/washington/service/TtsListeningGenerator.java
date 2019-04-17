package com.voxlearning.washington.service;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.convert.ConversionServiceProvider;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.business.api.constant.TtsListeningTagType;
import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;
import com.voxlearning.utopia.business.api.mapper.TtsListeningQuestion;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSentence;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSubQuestion;
import com.voxlearning.utopia.business.api.mapper.TtsListeningTag;
import com.voxlearning.washington.service.tts.TtsFlashHelper;
import com.voxlearning.washington.service.tts.TtsHtmlHelper;
import com.voxlearning.washington.service.tts.TtsXmlParser;
import org.slf4j.Logger;
import org.springframework.core.convert.ConversionService;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Junjie Zhang
 * @since 2016-03-17
 */
public class TtsListeningGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TtsListeningGenerator.class);
    private static final String[] X_SPEED_DEF = {"x-slow", "slow", "medium", "fast", "x-fast"};  //讯飞五级速度对应关系
    private static final String[] V_SPEED_DEF = {"53%", "73%", "83%", "90%", "100%"};            //IVONA五级速度对应关系
    private static final String[] V_VOLUME_DEF = {"-5dB", "-2dB", "default", "+2dB", "+5dB"};    //IVONA音量对应关系
    private static final String[] X_VOLUME_DEF = {"3", "4", "5", "6", "7"};                      //讯飞音量对应关系

    //讯飞不发音字符 标点符号
    private static final Pattern SILENT_PATTERN = Pattern.compile("^[\\p{P}\\p{S}\\p{Z}]*$");

    private static final ConversionService conversionService = ConversionServiceProvider.instance().getConversionService();


    private static TtsListeningSentence generateSentenceVoice(String url, TtsListeningSentence sentence) {
        if (sentence == null || (!StringUtils.isEmpty(sentence.getVoice()) && sentence.getDuration() != null && sentence.getDuration() > 0)) {
            return sentence;
        }
        Map<String, String> params = new HashMap<>();
        //文本内容
        if (StringUtils.isBlank(sentence.getContent()))
            return sentence;
        sentence.setContent(sentence.getContent().trim());

        if (StringUtils.isBlank(sentence.getContent()))
            return sentence;


        if (StringUtils.equals(sentence.getRole(), "p") || StringUtils.equals(sentence.getRole(), "pm")) {
            params.put("g", "x");  //科大讯飞引擎
            params.put("r", "f");  //小燕，女，普通话
            if (StringUtils.equals(sentence.getRole(), "pm")) {
                params.put("r", "m"); //小宇，男，普通话
            }
            if (StringUtils.isNotBlank(sentence.getContent()) && SILENT_PATTERN.matcher(sentence.getContent()).find()) {
                //全是不发音字符，不生成音频
                return sentence;
            }
        } else if (StringUtils.equals(sentence.getRole(), "m")
                || StringUtils.equals(sentence.getRole(), "f")
                || StringUtils.equals(sentence.getRole(), "c")
                || StringUtils.equals(sentence.getRole(), "bm")
                || StringUtils.equals(sentence.getRole(), "bf")
                ) {
            params.put("g", "v");  //ivona引擎
            params.put("r", sentence.getRole()); //美式男音，美式女音，美式童音，英式男音，英式女音
            String content = sentence.getContent();
            //全角字符转换为半角字符
            content = TtsXmlParser.toSemiangle(content);
            sentence.setContent(content);

        } else {
            logger.error("generate tts voice error role " + sentence.getRole());
            return sentence;
        }

        params.put("t", sentence.getContent());

        //五级语速
        if (sentence.getSpeed() == null || sentence.getSpeed() < 1 || sentence.getSpeed() > 5) {
            sentence.setSpeed(3);
        }
        if (StringUtils.equals(params.get("g"), "x")) {
            params.put("s", X_SPEED_DEF[sentence.getSpeed() - 1]);
        } else {
            params.put("s", V_SPEED_DEF[sentence.getSpeed() - 1]);
        }
        //五级音量
        if (sentence.getVolume() == null || sentence.getVolume() < 1 || sentence.getVolume() > 5) {
            sentence.setVolume(3);
        }
        if (StringUtils.equals(params.get("g"), "x")) {
            params.put("v", X_VOLUME_DEF[sentence.getVolume() - 1]);
        } else {
            params.put("v", V_VOLUME_DEF[sentence.getVolume() - 1]);
        }
        params.put("c", "l");  //返回音频id

        POST post = HttpRequestExecutor.defaultInstance().post(url);
        params.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        AlpsHttpResponse response = post.execute();
        if (response.getStatusCode() == 200) {
            Map<String, String> map = JsonUtils.fromJsonToMap(response.getResponseString(), String.class, String.class);
            //1002 讯飞空音频 2002 ivona空音频
            //输入字符都是不发音字符，会生成空音频
            if (!StringUtils.equals(map.get("error"), "0") && !StringUtils.equals(map.get("error"), "1002")
                    && !StringUtils.equals(map.get("error"), "2002")) {
                logger.error("generate tts voice error, error code = " + map.get("error"));
                return sentence;
            }
            sentence.setVoice(map.get("id"));
            sentence.setDuration(conversionService.convert(map.get("duration"), Float.class));
            return sentence;
        } else {
            logger.error("generate tts voice error, http code = " + response.getStatusCode());
            return sentence;
        }
    }

    public static TtsListeningSentence generateSentenceVoice(String url, String json) {
        TtsListeningSentence sentence = JsonUtils.fromJson(json, TtsListeningSentence.class);
        return generateSentenceVoice(url, sentence);
    }

    private static TtsListeningSubQuestion generateSubQuestionVoice(String url, TtsListeningSubQuestion subQuestion) {
        if (subQuestion == null || subQuestion.getSentences() == null)
            return subQuestion;
        for (TtsListeningSentence sentence : subQuestion.getSentences()) {
            if (StringUtils.isEmpty(sentence.getVoice()))
                generateSentenceVoice(url, sentence);
        }
        return subQuestion;
    }

    public static TtsListeningSubQuestion generateSubQuestionVoice(String url, String json) {
        TtsListeningSubQuestion subQuestion = JsonUtils.fromJson(json, TtsListeningSubQuestion.class);
        return generateSubQuestionVoice(url, subQuestion);
    }

    private static boolean generateVoiceFromPaper(String url, TtsListeningPaper paper) {
        if (paper == null)
            return false;
        if (SafeConverter.toInt(paper.getFormat()) == 1) {
            //flash格式
            if (paper.getTagList() == null)
                return false;
            for (TtsListeningTag tag : paper.getTagList()) {
                if (tag.getTagType() == TtsListeningTagType.SENTENCE)
                    generateSentenceVoice(url, tag.getSentence());
            }
        } else {
            //html格式
            if (paper.getQuestions() == null)
                return false;
            //处理小题
            for (TtsListeningQuestion question : paper.getQuestions()) {
                if (question == null)
                    continue;
                if (question.getTipSentence() != null) {
                    question.setTipSentence(generateSentenceVoice(url, question.getTipSentence()));
                }
                for (TtsListeningSubQuestion subQuestion : question.getSubQuestions()) {
                    generateSubQuestionVoice(url, subQuestion);
                }
            }

            //考前提示音
            if (paper.getBeginningSentence() != null) {
                paper.setBeginningSentence(generateSentenceVoice(url, paper.getBeginningSentence()));
            }
            //考后提示音
            if (paper.getEndingSentence() != null) {
                paper.setEndingSentence(generateSentenceVoice(url, paper.getEndingSentence()));
            }
        }
        return true;
    }

    /**
     * 生成HTML试卷音频并保存
     *
     * @param url
     * @param json
     * @param userId
     * @param userType
     * @param authorName
     * @return
     */
    public static TtsListeningPaper generatePaperVoice(String url, String json, Long userId, Integer userType, String authorName) {
        TtsListeningPaper paper = JsonUtils.fromJson(json, TtsListeningPaper.class);
        if (paper == null)
            return null;
        paper.setFormat(0);
        paper.setAuthor(userId);
        boolean rlt = generateVoiceFromPaper(url, paper);
        if (rlt) {
            paper.setAuthorName(authorName);
            paper.setUserType(userType);
            paper.setCreateDatetime(new Date());
            paper.setUpdateDatetime(new Date());
            //计算试卷时长
            paper.setDuration(calcPaperDuration(paper));

        } else {
            return null;
        }
        return paper;
    }

    /**
     * 生成FLASH试卷音频并保存
     *
     * @param url
     * @param paper
     * @return
     */
    public static MapMessage generatePaperVoiceByPaper(String url, TtsListeningPaper paper) {
        if (paper == null)
            return MapMessage.errorMessage("试卷为空");
        //设置格式为flash格式
        paper.setFormat(1);
        //解析xml到Tag标签
        if (paper.getTagList() == null) {
            MapMessage msg = translate(paper.getRichText());
            if (!msg.isSuccess())
                return MapMessage.errorMessage(msg.getInfo());
            paper.setTagList((List<TtsListeningTag>) msg.get("value"));
        }

        boolean rlt = generateVoiceFromPaper(url, paper);
        if (rlt) {
            paper.setCreateDatetime(new Date());
            if (paper.getId() != null) {
                //TtsListeningPaper old = getListeningPaperById(paper.getId());
                //if (old != null)
                //    paper.setCreateDatetime(old.getCreateDatetime());
            }
            paper.setUpdateDatetime(new Date());
            //计算试卷时长
            paper.setDuration(calcPaperDuration(paper));
            //保存试卷
            //saveListeningPaper(paper);

        } else {
            return MapMessage.errorMessage("生成音频失败，请稍候再试。");
        }

        MapMessage msg = MapMessage.successMessage();
        return msg;
    }

    public static MapMessage generateVoice(String url, String xml) {
        TtsListeningPaper paper = new TtsListeningPaper();
        //设置为flash格式
        paper.setFormat(1);
        paper.setRichText(xml);
        if (paper.getTagList() == null) {
            MapMessage msg = translate(xml);
            if (!msg.isSuccess())
                return MapMessage.errorMessage(msg.getInfo());
            paper.setTagList((List<TtsListeningTag>) msg.get("value"));
        }
        List<TtsListeningSentence> list = TtsFlashHelper.getPlayList(paper);
        for (TtsListeningSentence sentence : list) {
            generateSentenceVoice(url, sentence);
        }
        MapMessage rlt = MapMessage.successMessage();
        rlt.set("value", list);
        return rlt;
    }



    /**
     * 计算听力试卷时长
     *
     * @param paper
     * @return
     */
    private static float calcPaperDuration(TtsListeningPaper paper) {
        if (paper == null)
            return 0f;
        if (paper.getDuration() != null && paper.getDuration() > 0)
            return paper.getDuration();
        if (SafeConverter.toInt(paper.getFormat()) == 1) {
            //flash格式
            return TtsFlashHelper.calcPaperDuration(paper);
        } else {
            //html格式
            return TtsHtmlHelper.calcPaperDuration(paper);
        }
    }


    public static List<TtsListeningSentence> getPlayList(TtsListeningPaper paper) {
        if (paper == null)
            return new ArrayList<>();
        if (SafeConverter.toInt(paper.getFormat()) == 1) {
            //flash格式
            return TtsFlashHelper.getPlayList(paper);
        } else {
            //html格式
            return TtsHtmlHelper.getPlayList(paper);
        }
    }

    public static String getCompleteVoice(String url, TtsListeningPaper paper) {

        Map<String, String> params = new HashMap<>();
        List<TtsListeningSentence> list = getPlayList(paper);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            TtsListeningSentence s = list.get(i);
            if (i > 0)
                sb.append(",");
            if (s.getPause() == null)
                s.setPause(0.2f);
            if (!StringUtils.isBlank(s.getVoice()) && s.getVoice().length() == 24) {
                //mongoId 24位
                sb.append("fid:").append(s.getVoice()).append(",silence:").append(String.format("%.2f", s.getPause()));
            } else {
                sb.append("silence:").append(String.format("%.2f", s.getPause()));
            }
        }
        params.put("list", sb.toString());

        POST post = HttpRequestExecutor.defaultInstance().post(url);
        params.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        AlpsHttpResponse response = post.execute();
        if (response.getStatusCode() == 200) {
            Map<String, String> map = JsonUtils.fromJsonToMap(response.getResponseString(), String.class, String.class);
            if (!StringUtils.equals(map.get("error"), "0")) {
                logger.error("generate tts voice error, error code = " + map.get("error"));
                return "";
            }
            return map.get("id");
        } else {
            logger.error("generate tts voice error, http code = " + response.getStatusCode());
            return "";
        }
    }



    private static MapMessage translate(String content) {
        TtsXmlParser parser = new TtsXmlParser(content);
        String result = parser.translate();
        if (StringUtils.isBlank(result)) {
            MapMessage msg = MapMessage.successMessage();
            msg.set("value", parser.getTagList());
            return msg;
        }
        return MapMessage.errorMessage(result);
    }
}
