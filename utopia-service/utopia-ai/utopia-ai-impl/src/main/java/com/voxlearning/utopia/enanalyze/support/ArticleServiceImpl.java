package com.voxlearning.utopia.enanalyze.support;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.nature.commons.lang.encrypt.MD5Util;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageSystem;
import com.voxlearning.utopia.enanalyze.ErrorCode;
import com.voxlearning.utopia.enanalyze.MessageBuilder;
import com.voxlearning.utopia.enanalyze.api.ArticleService;
import com.voxlearning.utopia.enanalyze.assemble.AINLPClient;
import com.voxlearning.utopia.enanalyze.assemble.AIOCRClient;
import com.voxlearning.utopia.enanalyze.convertor.ArticleBuilder;
import com.voxlearning.utopia.enanalyze.entity.ArticleEntity;
import com.voxlearning.utopia.enanalyze.entity.UserEntity;
import com.voxlearning.utopia.enanalyze.exception.support.ThirdPartyServiceException;
import com.voxlearning.utopia.enanalyze.facade.SentencePersistenceFacade;
import com.voxlearning.utopia.enanalyze.model.*;
import com.voxlearning.utopia.enanalyze.mq.Message;
import com.voxlearning.utopia.enanalyze.mq.MessageBroker;
import com.voxlearning.utopia.enanalyze.mq.Topic;
import com.voxlearning.utopia.enanalyze.persistence.*;
import com.voxlearning.utopia.enanalyze.service.RankAbilityService;
import com.voxlearning.utopia.enanalyze.service.support.ArticleAbilityServiceImpl;
import com.voxlearning.utopia.enanalyze.service.support.ArticleEvaluator;
import com.voxlearning.utopia.enanalyze.service.support.ArticleReportor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 作文服务实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Named
@Slf4j
@ExposeService(interfaceClass = ArticleService.class)
public class ArticleServiceImpl implements ArticleService {

    String URL_PREFIX = "http://oss-data.17zuoye.com";

    @Resource
    ArticleDao articleDao;

    @Resource
    UserDao userDao;

    @Resource
    UserGroupDao userGroupDao;

    @Resource
    ArticleEvaluator articleEvaluator;

    @Resource
    ArticleReportor articleReportor;

    @Resource
    AIOCRClient ocrClient;

    @Resource
    AINLPClient nlpClient;

    @Resource
    FileRecordDao fileRecordDao;

    @Resource
    ArticleBuilder articleBuilder;

    @Resource
    ArticleAbilityServiceImpl abilityService;

    @Resource
    RankAbilityService rankAbilityService;

    @Resource
    RankFrequencyCache rankFrequencyCache;

    @Resource
    SentencePersistenceFacade sentenceCacheFacade;

    @Resource
    IdempotentCache idempotentCache;

    @Resource
    MessageBroker messageBroker;

    @StorageClientLocation(system = StorageSystem.OSS, storage = "homework")
    private StorageClient storageClient;

    @Override
    public MapMessage nlp(ArticleNLPParams params) {

        AINLPClient.Result nlpResult;

        try {
            String md5 = MD5Util.encrypt(params.getText());
            if (idempotentCache.exist(md5)) {
                String cacheJsonStr = idempotentCache.get(md5);
                nlpResult = JSON.parseObject(cacheJsonStr, AINLPClient.Result.class);
            } else {
                // 请求AI服务
                nlpResult = nlpClient.nlp(params.getText());
                idempotentCache.set(md5, JSON.toJSONString(nlpResult), 60);
            }
        } catch (ThirdPartyServiceException e) {
            return MessageBuilder.error(ErrorCode.AI_NLP_ERROR.CODE, e.getMessage());
        } catch (Exception e) {
            return MessageBuilder.error(ErrorCode.AI_NLP_ERROR.CODE, e.getMessage());
        }

        // 更新作业批改记录
        ArticleEntity entity = new ArticleEntity();
        entity.setId(params.getArticleId());
        entity.setText(params.getText());
        Date now = new Date();
        entity.setUpdateDate(now);
        entity.setNlpResult(nlpResult);
        ArticleBasicAbility basicAbility = abilityService.getBasicAbility(nlpResult);
        ArticleCompositeAbility compositeAbility = abilityService.getCompositeAbility(nlpResult);
        entity.setBasicAbility(basicAbility);
        entity.setCompositeAbility(compositeAbility);

        // 创建作文批改记录
        if (StringUtils.isBlank(params.getArticleId())) {
            entity.setOpenId(params.getOpenId());
            entity.setText(params.getText());
            entity.setCreateDate(now);
            entity.setUpdateDate(now);
            entity.setDisable(false);
            articleDao.insert(entity);
        } else {
            articleDao.update(entity);
        }

        // 发送新增批改记录消息
        messageBroker.send(new Message(Topic.ARTICLE_CREATE, entity.getId()));

        return MessageBuilder.success(articleBuilder.build(entity));
    }


    @Override
    public MapMessage ocr(String openId, byte[] bytes) {

        MapMessage message;

        AIOCRClient.Request ocrRequest = new AIOCRClient.Request();
        ocrRequest.setBytes(bytes);

        try {
            AIOCRClient.Result ocrResult;

            String md5 = MD5Util.encrypt(bytes);
            if (idempotentCache.exist(md5)) {
                String cacheJsonStr = idempotentCache.get(md5);
                ocrResult = JSON.parseObject(cacheJsonStr, AIOCRClient.Result.class);
            } else {
                ocrResult = ocrClient.ocr(ocrRequest);
                idempotentCache.set(md5, JSON.toJSONString(ocrResult), 60);
            }

            ArticleOCRResult ocrOutput = new ArticleOCRResult();
            ocrOutput.setText(ocrResult.getEssay());
            message = MessageBuilder.success(ocrOutput);

        } catch (ThirdPartyServiceException e) {
            message = MessageBuilder.error(ErrorCode.AI_OCR_ERROR.CODE, e.getMessage());
        } catch (Exception e) {
            message = MessageBuilder.error(ErrorCode.AI_OCR_ERROR.CODE, "未能处理的异常：" + e.getMessage());
        }
        return message;
    }

    @Override
    public MapMessage queryPage(ArticlePageParams input) {

        // 查询出所有数据
        List<ArticleEntity> entityList = articleDao.findByOpenId(input.getOpenId());

        // 分页
        ArrayList<ArticleGeneralInfo> records = new ArrayList<>();
        int page = input.getPage();
        int size = input.getSize();
        if (entityList.size() >= page * size) {
            int from = page * size;
            records = entityList.stream()
                    .sorted(Comparator.comparing(ArticleEntity::getCreateDate).reversed())
                    .skip(from)
                    .limit(size)
                    .map(i -> {
                        ArticleGeneralInfo record = new ArticleGeneralInfo();
                        record.setId(i.getId());
                        record.setCreateDate(i.getCreateDate());
                        record.setText(i.getText());
                        return record;
                    }).collect(Collectors.toCollection(ArrayList::new));
        }
        return MessageBuilder.success(records);
    }

    @Override
    public MapMessage retrieve(String articleId) {
        ArticleEntity entity = articleDao.findById(articleId);
        if (null != entity) {
            Article article = articleBuilder.build(entity);
            return MessageBuilder.success(article);
        } else {
            return MessageBuilder.error(ErrorCode.DAO_RS_EMPTY);
        }
    }

    @Override
    public MapMessage delete(String articleId) {
        try {
            articleDao.disable(articleId);
            messageBroker.send(new Message(Topic.ARTICLE_DELETE, articleId));
            return MessageBuilder.success(true);
        } catch (Exception e) {
            return MessageBuilder.error(ErrorCode.DAO_EXE_ERROR);
        }

    }

    @Override
    public MapMessage report(String openId) {
        MapMessage message;
        ArticleReport report = new ArticleReport();

        // 查询用户
        UserEntity user = userDao.findByOpenId(openId);
        report.setNickName(user.getNickName());

        // 获取所有记录
        List<ArticleEntity> articles = articleDao.findByOpenId(openId);

        // 计算能力分数
        ArticleCompositeAbility abilty = abilityService.getAverageCompositeAbility(
                articles.stream()
                        .filter(i -> null != i.getNlpResult())
                        .map(ArticleEntity::getNlpResult)
                        .collect(Collectors.toList())
        );

        // 计算tag
        report.setTag(articleReportor.getTag(abilty.getScore()));
        report.setRecords(
                articles.stream()
                        .map(i -> {
                            if (null != i.getNlpResult()) {
                                AINLPClient.Result.EssayRating essayRating = i.getNlpResult().getEssayRating();
                                if (null != essayRating) {
                                    ArticleReport.Record record = new ArticleReport.Record();
                                    record.setCreateDate(i.getCreateDate());
                                    record.setContentScore(essayRating.getContent_score());
                                    record.setLexicalScore(essayRating.getLexical_score());
                                    record.setSentenceScore(essayRating.getSentence_score());
                                    record.setStructureScore(essayRating.getStructure_score());
                                    return record;
                                }
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .toArray(ArticleReport.Record[]::new)
        );
        report.setOpenId(openId);
        report.setAbility(abilty);
        report.setAbilityEvaluation("差一点点就无人能敌了~文章表达符合英语习惯，文章中过渡性词汇丰富，多增加些固定搭配和短语使用文章会更出彩。");
        report.setRecordEvaluation(null);
        // 计算总共天数，按照用户的创建日期开始计算
        Date beginDate = user.getCreateDate();
        int days = Double.valueOf(
                // 向上取正，也就是，1.1 = 2
                Math.ceil(
                        // 当前时间 - 用户创建时间 的毫秒数
                        Double.valueOf(new Date().getTime() - beginDate.getTime())
                                // 一天的毫秒数
                                / Double.valueOf(1000L * 60 * 60 * 24)))
                .intValue();
        // 获取排名
        Long rank = rankFrequencyCache.get(openId);
        // 获取总人数
        long count = userDao.totalCount();
        int beyond = 0;
        if (null != rank) {
            if (1L == count)
                // 就一个人 => 超越100%
                beyond = 100;
            else {
                // 超越的人数 / 总人数
                beyond = BigDecimal.valueOf(
                        Double.valueOf(count - rank - 1)
                                / Double.valueOf(count - 1) * 100)
                        .setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            }
        }
        // 排除最大值
        if (100 == beyond) beyond = 99;
        // 排除最小值
        if (0 == beyond) beyond = 1;
        report.setBeyondRate(beyond);
        // 组装评论
        if (abilty.isSideBySide()) {
            report.setEvaluation(String.format("在过去的%d天中，" +
                            "你以超认真的态度修改了%d篇大作，" +
                            "超越了%d%%的伙伴！" +
                            "你在英语写作上的各项能力都有在均衡的发展哦～让我们一起加油，向星辰大海进发吧！",
                    days, articles.size(), beyond));
        } else {
            report.setEvaluation(String.format("在过去的%d天中，" +
                            "你以超认真的态度修改了%d篇大作，" +
                            "超越了%d%%的伙伴！" +
                            "你的%s是如此闪耀，" +
                            "在%s上还有再努力一下的空间哦！让我们一起加油吧！",
                    days, articles.size(), beyond,
                    abilty.getHighestDimension().DESC,
                    abilty.getLowestDimension().DESC));
        }
        message = MessageBuilder.success(report);
        return message;
    }
}
