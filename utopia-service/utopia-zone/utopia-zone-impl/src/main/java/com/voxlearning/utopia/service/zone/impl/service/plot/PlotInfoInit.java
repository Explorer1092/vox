package com.voxlearning.utopia.service.zone.impl.service.plot;

import com.voxlearning.utopia.service.zone.api.entity.plot.PlotInfo;
import com.voxlearning.utopia.service.zone.api.plot.PlotActivityService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author : kai.sun
 * @version : 2018-11-10
 * @description :
 **/
@Component
public class PlotInfoInit {

    @Resource
    private
    PlotActivityService plotActivityService;

    public void init(Integer activityId){

        plotActivityService.savePlotInfo(activityId,1,1,"初见小王子","小王子住在宇宙里一个很小的星球上，初次见面，你可以送给他一个小礼物吗？",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-1.mp3",
                "1_很开心能认识你！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-2.mp3____https://cdn-va.17zuoye.cn/class/plot/1.1nightBG.png,2_谢谢你的礼物！我会好好珍惜的！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-3.mp3____https://cdn-va.17zuoye.cn/class/plot/1.2nightcatBG.png",
                false,null,null,1,2);

        plotActivityService.savePlotInfo(activityId,1,2,"初见小王子","小王子很喜欢你送的小苗，可是他不会种植，你可以帮帮他吗？",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-4.mp3",
                "1_长出来的会是什么植物呢？_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-5.mp3____https://cdn-va.17zuoye.cn/class/plot/2.1daycatBG.png,2_原来是一朵玫瑰花！真漂亮！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-6.mp3____https://cdn-va.17zuoye.cn/class/plot/2.2day.png",
                true,1,1,1,3);

        plotActivityService.savePlotInfo(activityId,1,3,"初见小王子","小王子和玫瑰花一起看日出日落，度过了一段非常幸福的时光。",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-7.mp3",
                "1_我是全宇宙唯一一朵玫瑰花。_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-8.mp3____https://cdn-va.17zuoye.cn/class/plot/3.1afternooncatBG.png",
                true,1,2,1,4);

        plotActivityService.savePlotInfo(activityId,1,4,"初见小王子","后来他们吵架了，小王子想要离开，去外面学习与人相处。这时一群候鸟出现了……",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-9.mp3",
                "1_我们可以带你去旅行呀！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-10.mp3____https://cdn-va.17zuoye.cn/class/plot/3.2doveBG.png,2_请你不要离开我。_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-11.mp3_小王子还会回到玫瑰花的身边吗？_会_不会_https://cdn-va.17zuoye.cn/class/plot/3.3princefly.png",
                true,1,3,2,1);


        plotActivityService.updatePlotInfo(PlotInfo.generatorId(activityId,1,1),"https://cdn-va.17zuoye.cn/class/plot/unlock-plot1.png","https://cdn-va.17zuoye.cn/class/plot/dubbing/BGM1.mp3");

        plotActivityService.savePlotInfo(activityId,2,1,"宇宙探险","小王子去了很多小星球，一个神秘人说他知道一个地方可以帮助小王子……",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-12.mp3",
                "1_我可以告诉你这个星球在哪！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-13.mp3____https://cdn-va.17zuoye.cn/class/plot/4.1eveningdoveBG.png,2_去地球吧！ 在那你会遇到一个意想不到的人哦~ _https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-14.mp3____https://cdn-va.17zuoye.cn/class/plot/4.2eveningBG.png",
                false,1,4,2,2);

        plotActivityService.savePlotInfo(activityId,2,2,"宇宙探险","一到达地球，候鸟们就离开了。突然，小王子听见了一阵歌声，会是谁呢？",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-15.mp3",
                "1_ ♫ ♪ ♩ ♫ ♬ ~_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-16.mp3____https://cdn-va.17zuoye.cn/class/plot/5.1skyandcatBG.png,2_喵喵喵~我是小白猫~_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-17.mp3____https://cdn-va.17zuoye.cn/class/plot/5.2skyBG.png",
                true,2,1,2,3);

        plotActivityService.savePlotInfo(activityId,2,3,"宇宙探险","小白猫说她只和朋友一起玩儿，可是，怎么才能跟他成为朋友呢？",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-18.mp3",
                "1_我喜欢跟充满好奇爱学习的人做好朋友哦~_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-19.mp3____https://cdn-va.17zuoye.cn/class/plot/5.2skyBG.png,2_现在我们是好朋友啦，一起玩儿吧！~_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-20.mp3_小王子会和小白猫永远在一起吗？_会_不会_https://cdn-va.17zuoye.cn/class/plot/effect9.gif_210_623_https://cdn-va.17zuoye.cn/class/plot/5.2skyBG.png",
                true,2,2,3,1);


        plotActivityService.updatePlotInfo(PlotInfo.generatorId(activityId,2,1),"https://cdn-va.17zuoye.cn/class/plot/unlock-plot2.png","https://cdn-va.17zuoye.cn/class/plot/dubbing/BGM1.mp3");

        plotActivityService.savePlotInfo(activityId,3,1,"沙漠救援","他们跑进了一片玫瑰园，小王子这才发现，原来他的玫瑰花并不是唯一的……",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-21.mp3",
                "1_你的玫瑰花骗你啦！世上有很多玫瑰花呢。_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-22.mp3____https://cdn-va.17zuoye.cn/class/plot/7.1gardenandcatBG.png",
                false,2,3,3,2);

        plotActivityService.savePlotInfo(activityId,3,2,"沙漠救援","望着伤心的小王子，小白猫想安慰他，他提出了一个建议……",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-23.mp3",
                "1_你们一起经历过很多事情，所以她是特别的玫瑰花！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-24.mp3____https://cdn-va.17zuoye.cn/class/plot/7.2gardenBG.png,2_你应该回到你的玫瑰花身边。_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-25.mp3____https://cdn-va.17zuoye.cn/class/plot/7.2gardenBG.png",
                true,3,1,3,3);

        plotActivityService.savePlotInfo(activityId,3,3,"沙漠救援","分别前，小白猫告诉了小王子一个秘密作为离别礼物……",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-26.mp3",
                "1_我知道能带你飞回去的办法！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-27.mp3____https://cdn-va.17zuoye.cn/class/plot/5.2skyBG.png,2_沙漠里掉落了一架飞机，你去找找吧！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-28.mp3____https://cdn-va.17zuoye.cn/class/plot/5.2skyBG.png",
                true,3,2,3,4);

        plotActivityService.savePlotInfo(activityId,3,4,"沙漠救援","小王子在沙漠里找到了飞机，还有一个垂死的可怜飞行员……",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-29.mp3",
                "1_好渴啊……谁能救救我……_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-30.mp3____https://cdn-va.17zuoye.cn/class/plot/9.1desertcryBG.png,2_谢谢你给我水！我该怎么报答你呢？_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-31.mp3_小王子和飞行员会修好飞机吗？_会_不会_https://cdn-va.17zuoye.cn/class/plot/9.2desertBG.png",
                true,3,3,4,1);


        plotActivityService.updatePlotInfo(PlotInfo.generatorId(activityId,3,1),"https://cdn-va.17zuoye.cn/class/plot/unlock-plot3.png","https://cdn-va.17zuoye.cn/class/plot/dubbing/BGM1.mp3");

        plotActivityService.savePlotInfo(activityId,4,1,"大结局","飞行员答应把小王子送回小星球，但飞机却无法起飞，怎么办呢……"
                ,"https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-32.mp3",
                "1_飞机坏了，必须找工具把它修好才行！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-33.mp3____https://cdn-va.17zuoye.cn/class/plot/10.1desertBG.png,2_飞机修好啦！现在我就送你回去！_https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-34.mp3____https://cdn-va.17zuoye.cn/class/plot/10.2desertBG.png",
                false,3,4,4,2);

        plotActivityService.savePlotInfo(activityId,4,2,"大结局","小王子终于回到了玫瑰花身边！经过这次分别，他们都学会了关心对方，从此幸福地生活在了一起……",
                "https://cdn-va.17zuoye.cn/class/plot/dubbing/dubbing-35.mp3",
                "1______https://cdn-va.17zuoye.cn/class/plot/3.1afternooncatBG.png",
                true,4,1,null,null);

        plotActivityService.updatePlotInfo(PlotInfo.generatorId(activityId,4,1),"https://cdn-va.17zuoye.cn/class/plot/unlock-plot4.png","https://cdn-va.17zuoye.cn/class/plot/dubbing/BGM1.mp3");
    }

}
