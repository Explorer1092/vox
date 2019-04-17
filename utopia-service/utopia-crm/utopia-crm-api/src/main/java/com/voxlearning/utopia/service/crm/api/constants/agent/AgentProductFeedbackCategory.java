package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * AgentProductFeedbackCategory
 *
 * @author song.wang
 * @date 2017/2/21
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentProductFeedbackCategory {

    // 一级分类
    // 作业产品
    作业内容_小学英语,
    作业内容_小学数学,
    作业内容_中学英语,
    作业内容_小学语文,
    作业操作,
    推题算法,
    报告与学情Goal目标,
    期中_期末_假期作业,
    学生端作业,
    老师端_智慧课堂等,
    语音打分_TTS,
    统考,



    // 平台类
    注册及账号,
    班级管理,
    学豆_成就体系,
    奖品中心,
    班级空间,
    家长通,
    特殊账户权限相关,
    学科相关,

    // 中学作业产品
    OTO教师端产品,
    @Deprecated 初中数学题库,
    @Deprecated 高中数学题库,
    @Deprecated 初中英语题库,
    @Deprecated 高中英语题库,
    @Deprecated 物理题库,
    @Deprecated 化学题库,
    @Deprecated 教务老师账号,
    @Deprecated 产品,
    @Deprecated 初中教研,
    @Deprecated 高中教研,
    布置练习,
    检查练习,
    做练习,

    // 自学与增值
    增值产品,
    成长世界,
    增值产品教研_除阿分提,
    增值运营,
    学生端APP成长世界运营,
    阿分提英语,
    免费自学,
    阿分提,
    付费增值,


    //直播商业化
    直播产品,
    翻转课堂小学英语,
    直播商业化小学数学,
    直播商业化小学语文,

    //运行活动
    线上教师运营活动,
    师训活动,
    教学课件,

    // 新增教材教辅
    //教材内容调整
    小学英语,
    小学数学,
    计算能力,
    初中英语,
    小学语文,
    题目报错_题目超纲,

    //市场物料
    物料经费,
    物料质量,
    物料种类,


    // 二级分类
    基础练习,
    同步习题,
    绘本阅读,
    新绘本阅读,
    配套试卷,
    口算,
    重点视频专练,
    针对训练,
    期中复习,
    中考口语专练,
    教辅选题,
    经典读物,
    阅读,
    课文读背,
    布置,
    检查_奖励,
    错作业_订正,
    评语,
    作业报告,
    学情分析Goal目标,
    作业单,
    期中作业,
    期末作业,
    假期作业,
    基础功能,
    资讯,
    自学工具,
    小U系列,
    微课,
    其他,

    //小学英语
    自然拼读,
    趣味配音,
    高频错题,
    听力专练,
    错题订正,
    课外拓展,
    模块复习,

    //小学数学
    趣味绘本,
    口算训练,
    单元复习,
    //小学语文
    基础知识,

    // 学生端作业
    作业记录,
    补做记录,

    // 注册及账号
    注册问题,
    账号问题,

    //学豆_成就体系
    园丁豆兑换比,
    学豆兑换比,

    // 奖品中心
    奖品种类,
    收获地址,
    奖品质量,

    //家长通
    点读机,
    点读机内容,

    // OTO 教师端产品
    出题组卷,
    作业中心,
    学情分析,
    错题本,
    校本题库,
    答题卡,
    扫描阅卷,

    // 布置练习
    同步练习,
    听说练习,
    复习练习,
    教辅相关,
    读物相关,
    课堂小测,
    单元卷,
    专题卷,
    期中卷,
    期末卷,
    假期练习,
    小测本,

    // 检查练习
    交卷及订正,
    统考及模考,
    奖励及评语,

    // 做练习
    练习记录,
    练习重做,
    录音问题,
    打分相关,
    题目相关,
    自学提分,

    // 统考
    口语测验,

    // 三级分类
    //家长通  基础功能
    亲子信_班群功能,
    家庭等级,
    家长奖励,
    学习成长进度,
    家长通临时活动,;

    // 构建三级分类的树形结构

    public static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> fetchCategory(int type) {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = null;
        switch (type) {
            case 1:
                retMap = generateHomeworkCategory();
                break;
            case 2:
                retMap = generatePlatformCategory();
                break;
            case 3:
                retMap = generateMiddleMathCategory();
                break;
            case 4:
                retMap = generateSelfStudyCategory();
                break;
            case 5:
                retMap = generateOperationCategory();
                break;
            case 6:
                retMap = generateBookContentAdjustmentcategory();
                break;
            case 7:
                retMap = generateAddBookcategory();
                break;
            case 10:
                retMap = generateLiveCommercializeCategory();
                break;
            case 11:
                retMap = generateAgentMaterialCategory();
                break;
        }

        if (retMap == null) {
            retMap = new HashMap<>();
        }
        return retMap;
    }

    //小学作业产品
    private static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> generateHomeworkCategory() {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = new HashMap<>();
        retMap.put(AgentProductFeedbackCategory.作业内容_小学英语, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.作业内容_小学数学, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.作业内容_中学英语, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.作业内容_小学语文, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.作业操作, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.推题算法, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.报告与学情Goal目标, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.期中_期末_假期作业, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.学生端作业, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.老师端_智慧课堂等, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.语音打分_TTS, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.统考, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.其他, new HashMap<>());

        // 设置二级分类
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.基础练习, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.同步习题, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.绘本阅读, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.新绘本阅读, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.配套试卷, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.自然拼读, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.趣味配音, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.高频错题, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.听力专练, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.错题订正, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.课外拓展, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.模块复习, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业内容_小学英语).put(AgentProductFeedbackCategory.其他, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.同步习题, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.口算, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.配套试卷, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.重点视频专练, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.针对训练, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.趣味绘本, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.口算训练, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.单元复习, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.计算能力, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.错题订正, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.高频错题, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学数学).put(AgentProductFeedbackCategory.其他, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业内容_中学英语).put(AgentProductFeedbackCategory.同步习题, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_中学英语).put(AgentProductFeedbackCategory.期中复习, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_中学英语).put(AgentProductFeedbackCategory.中考口语专练, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_中学英语).put(AgentProductFeedbackCategory.教辅选题, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_中学英语).put(AgentProductFeedbackCategory.经典读物, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_中学英语).put(AgentProductFeedbackCategory.其他, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业内容_小学语文).put(AgentProductFeedbackCategory.基础练习, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学语文).put(AgentProductFeedbackCategory.基础知识, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业内容_小学语文).put(AgentProductFeedbackCategory.阅读, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学语文).put(AgentProductFeedbackCategory.课文读背, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学语文).put(AgentProductFeedbackCategory.配套试卷, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业内容_小学语文).put(AgentProductFeedbackCategory.其他, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业操作).put(AgentProductFeedbackCategory.布置, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业操作).put(AgentProductFeedbackCategory.检查_奖励, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业操作).put(AgentProductFeedbackCategory.错作业_订正, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.作业操作).put(AgentProductFeedbackCategory.评语, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.作业操作).put(AgentProductFeedbackCategory.其他, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.报告与学情Goal目标).put(AgentProductFeedbackCategory.作业报告, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.报告与学情Goal目标).put(AgentProductFeedbackCategory.学情分析Goal目标, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.报告与学情Goal目标).put(AgentProductFeedbackCategory.作业单, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.报告与学情Goal目标).put(AgentProductFeedbackCategory.其他, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.学生端作业).put(AgentProductFeedbackCategory.作业记录, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.学生端作业).put(AgentProductFeedbackCategory.补做记录, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.期中_期末_假期作业).put(AgentProductFeedbackCategory.期中作业, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.期中_期末_假期作业).put(AgentProductFeedbackCategory.期末作业, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.期中_期末_假期作业).put(AgentProductFeedbackCategory.假期作业, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.统考).put(AgentProductFeedbackCategory.口语测验, new HashSet<>());


        return retMap;
    }

    // 平台类
    private static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> generatePlatformCategory() {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = new HashMap<>();
        retMap.put(AgentProductFeedbackCategory.注册及账号, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.班级管理, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.学豆_成就体系, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.奖品中心, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.班级空间, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.家长通, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.特殊账户权限相关, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.学科相关, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.其他, new HashMap<>());

        retMap.get(AgentProductFeedbackCategory.家长通).put(AgentProductFeedbackCategory.基础功能, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.家长通).put(AgentProductFeedbackCategory.点读机, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.家长通).put(AgentProductFeedbackCategory.点读机内容, new HashSet<>());
        //retMap.get(AgentProductFeedbackCategory.家长通).put(AgentProductFeedbackCategory.资讯, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.家长通).put(AgentProductFeedbackCategory.自学工具, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.家长通).put(AgentProductFeedbackCategory.小U系列, new HashSet<>());


        retMap.get(AgentProductFeedbackCategory.注册及账号).put(AgentProductFeedbackCategory.注册问题, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.注册及账号).put(AgentProductFeedbackCategory.账号问题, new HashSet<>());


        retMap.get(AgentProductFeedbackCategory.学豆_成就体系).put(AgentProductFeedbackCategory.园丁豆兑换比, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.学豆_成就体系).put(AgentProductFeedbackCategory.学豆兑换比, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.奖品中心).put(AgentProductFeedbackCategory.奖品种类, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.奖品中心).put(AgentProductFeedbackCategory.收获地址, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.奖品中心).put(AgentProductFeedbackCategory.奖品质量, new HashSet<>());
        //retMap.get(AgentProductFeedbackCategory.家长通).put(AgentProductFeedbackCategory.微课, new HashSet<>());


        //构建三级分类
        retMap.get(AgentProductFeedbackCategory.家长通).get(AgentProductFeedbackCategory.基础功能).add(AgentProductFeedbackCategory.亲子信_班群功能);
        retMap.get(AgentProductFeedbackCategory.家长通).get(AgentProductFeedbackCategory.基础功能).add(AgentProductFeedbackCategory.家庭等级);
        retMap.get(AgentProductFeedbackCategory.家长通).get(AgentProductFeedbackCategory.基础功能).add(AgentProductFeedbackCategory.家长奖励);
        retMap.get(AgentProductFeedbackCategory.家长通).get(AgentProductFeedbackCategory.基础功能).add(AgentProductFeedbackCategory.作业报告);
        retMap.get(AgentProductFeedbackCategory.家长通).get(AgentProductFeedbackCategory.基础功能).add(AgentProductFeedbackCategory.学习成长进度);
        retMap.get(AgentProductFeedbackCategory.家长通).get(AgentProductFeedbackCategory.基础功能).add(AgentProductFeedbackCategory.家长通临时活动);

        return retMap;
    }

    // 中学作业产品
    private static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> generateMiddleMathCategory() {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = new HashMap<>();
        retMap.put(AgentProductFeedbackCategory.OTO教师端产品, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.布置练习, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.检查练习, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.做练习, new HashMap<>());


        retMap.get(AgentProductFeedbackCategory.OTO教师端产品).put(AgentProductFeedbackCategory.出题组卷, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.OTO教师端产品).put(AgentProductFeedbackCategory.作业中心, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.OTO教师端产品).put(AgentProductFeedbackCategory.学情分析, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.OTO教师端产品).put(AgentProductFeedbackCategory.错题本, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.OTO教师端产品).put(AgentProductFeedbackCategory.校本题库, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.OTO教师端产品).put(AgentProductFeedbackCategory.答题卡, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.OTO教师端产品).put(AgentProductFeedbackCategory.扫描阅卷, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.OTO教师端产品).put(AgentProductFeedbackCategory.其他, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.同步练习, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.听说练习, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.复习练习, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.教辅相关, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.读物相关, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.课堂小测, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.单元卷, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.专题卷, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.期中卷, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.期末卷, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.假期练习, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.布置练习).put(AgentProductFeedbackCategory.小测本, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.检查练习).put(AgentProductFeedbackCategory.交卷及订正, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.检查练习).put(AgentProductFeedbackCategory.统考及模考, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.检查练习).put(AgentProductFeedbackCategory.学情分析, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.检查练习).put(AgentProductFeedbackCategory.奖励及评语, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.检查练习).put(AgentProductFeedbackCategory.其他, new HashSet<>());

        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.练习记录, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.练习重做, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.录音问题, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.打分相关, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.题目相关, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.假期练习, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.自学提分, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.错题本, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.统考及模考, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.做练习).put(AgentProductFeedbackCategory.其他, new HashSet<>());

        return retMap;
    }

    // 自学与增值
    private static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> generateSelfStudyCategory() {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = new HashMap<>();
        retMap.put(AgentProductFeedbackCategory.增值产品, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.成长世界, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.增值产品教研_除阿分提, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.增值运营, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.学生端APP成长世界运营, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.阿分提英语, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.免费自学, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.阿分提, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.付费增值, new HashMap<>());
        return retMap;
    }

    // 运营活动
    private static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> generateOperationCategory() {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = new HashMap<>();
        retMap.put(AgentProductFeedbackCategory.线上教师运营活动, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.师训活动, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.教学课件, new HashMap<>());
        return retMap;
    }

    // 直播商业化
    private static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> generateLiveCommercializeCategory() {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = new HashMap<>();
        retMap.put(AgentProductFeedbackCategory.直播产品, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.翻转课堂小学英语, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.直播商业化小学数学, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.直播商业化小学语文, new HashMap<>());
        return retMap;
    }

    // 教材内容调整
    private static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> generateBookContentAdjustmentcategory() {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = new HashMap<>();
        retMap.put(AgentProductFeedbackCategory.小学英语, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.小学数学, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.初中英语, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.小学语文, new HashMap<>());
        retMap.get(AgentProductFeedbackCategory.小学英语).put(AgentProductFeedbackCategory.题目报错_题目超纲, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.小学数学).put(AgentProductFeedbackCategory.题目报错_题目超纲, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.初中英语).put(AgentProductFeedbackCategory.题目报错_题目超纲, new HashSet<>());
        retMap.get(AgentProductFeedbackCategory.小学语文).put(AgentProductFeedbackCategory.题目报错_题目超纲, new HashSet<>());

        return retMap;
    }

    // 新增教材教辅
    private static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> generateAddBookcategory() {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = new HashMap<>();
        retMap.put(AgentProductFeedbackCategory.小学英语, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.小学数学, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.初中英语, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.小学语文, new HashMap<>());
        return retMap;
    }

    // 市场物料
    private static Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> generateAgentMaterialCategory() {
        Map<AgentProductFeedbackCategory, Map<AgentProductFeedbackCategory, Set<AgentProductFeedbackCategory>>> retMap = new HashMap<>();
        retMap.put(AgentProductFeedbackCategory.物料经费, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.物料质量, new HashMap<>());
        retMap.put(AgentProductFeedbackCategory.物料种类, new HashMap<>());
        return retMap;
    }

    public static AgentProductFeedbackCategory nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

}
