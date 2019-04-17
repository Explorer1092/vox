<#import "../../layout/project.module.ftl" as temp />
<@temp.page header="show">
    <@app.css href="public/skin/project/newexamad/css/skin.css" />
<div class="termEnd-banner"></div>
<div class="termEnd-main" id="newexam_ad">
    <div class="termEnd-name"><i class="pIcon pIcon-l"></i>布置测试，赢三重奖励<i class="pIcon pIcon-r"></i></div>
    <div class="termEnd-section">
        <div class="hd"><span class="label">奖励1</span>人人可得奖</div>
        <div class="informTxt">布置专项测试，每有1个学生完成，即可获得<span class="txtRed">1个园丁豆</span></div>
    </div>
    <div class="termEnd-section">
        <div class="hd">
            <span class="label">奖励2</span>多测多得奖
            <div class="clazzSelect" data-bind="if:$root.currentRewardStatus && $root.currentRewardStatus(),singleHover:true">
                <!--ko text:$root.currentRewardStatus().clazzName--><!--/ko--><i class="arrowIcon"></i>
                <div class="selectBox">
                    <ul data-bind="foreach:{data:$root.rewardStatus(),as:'reward'}">
                        <li data-bind="css:{'active': $root.currentRewardStatus() && reward.groupId == $root.currentRewardStatus().groupId},text:reward.clazzName,click:$root.changeClazz.bind($data,$index(),$root)"></li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="informTxt">达成任务获得的园丁豆将在6月26日－7月7日统一发放</div>
        <div class="rewardList">
            <div class="reward">
                <p class="txtBlue">30园丁豆</p>
                <p data-bind="css:{'txtGray-s' : !($root.currentRewardStatus() && $root.currentRewardStatus().finishWord),'txtOrange-s' : $root.currentRewardStatus() && $root.currentRewardStatus().finishWord},text:$root.currentRewardStatus() && $root.currentRewardStatus().finishWord ? '(已获得)' : '(未获得)'">&nbsp;</p>
            </div>
            <div class="info">
                <p>布置【单词】全部试卷<span class="label">夯实基础</span></p>
                <p class="txtGray">每份试卷至少10人完成</p>
            </div>
        </div>
        <div class="rewardList">
            <div class="reward">
                <p class="txtBlue">20园丁豆</p>
                <p data-bind="css:{'txtGray-s' : !($root.currentRewardStatus() && $root.currentRewardStatus().finishGrammar),'txtOrange-s' : $root.currentRewardStatus() && $root.currentRewardStatus().finishGrammar},text:$root.currentRewardStatus() && $root.currentRewardStatus().finishGrammar ? '(已获得)' : '(未获得)'">&nbsp;</p>
            </div>
            <div class="info">
                <p>布置【句式语法】全部试卷<span class="label">巩固提高</span></p>
                <p class="txtGray">每份试卷至少10人完成</p>
            </div>
        </div>
        <div class="rewardList">
            <div class="reward">
                <p class="txtBlue">30园丁豆</p>
                <p data-bind="css:{'txtGray-s' : !($root.currentRewardStatus() && $root.currentRewardStatus().finishAll),'txtOrange-s' : $root.currentRewardStatus() && $root.currentRewardStatus().finishAll},text:$root.currentRewardStatus() && $root.currentRewardStatus().finishAll ? '(已获得)' : '(未获得)'">&nbsp;</p>
            </div>
            <div class="info">
                <p>布置【单词·语法·听说】全部试卷<span class="label">全面诊断</span></p>
                <p class="txtGray">每份试卷至少10人完成</p>
            </div>
        </div>
    </div>
    <div class="termEnd-section" style="display:none;" data-bind="if:$root.rankNum() > 0 ,visible:$root.rankNum() > 0">
        <div class="hd"><span class="label">奖励3</span>班级荣耀奖</div>
        <div class="informTxt">排行榜前<!--ko text:$root.rankNum()--><!--/ko-->名获得超大容量移动电源</div>
        <div class="noContent" data-bind="if:$root.teacherClazzs().length == 0 && $root.noTeacherClazzs().length == 0,visible:$root.teacherClazzs().length == 0 && $root.noTeacherClazzs().length == 0">
            <i class="emptyIcon"></i>
            <p>亲爱的老师，炫酷的荣耀榜奖励活动将在几天后上线，<br>荣耀榜上线之前，您可以先布置专项测试</p>
        </div>
        <div class="rulesBox" data-bind="if:$root.teacherClazzs().length == 0 && $root.noTeacherClazzs().length == 0,visible:$root.teacherClazzs().length == 0 && $root.noTeacherClazzs().length == 0">
            <p>排行榜规则：</p>
            <p class="txtGray">1.完成人数＞15人的测试，每次的班级平均分累加，得到班级总分</p>
            <p class="txtGray">2.同1份试卷布置次数大于1次，取前2次成绩的最高分计入总分</p>
            <p class="txtGray">3.班级总分从高到低排名，若总分相当，参与人次更高的班级优先</p>
        </div>
        <div class="rankingBox" style="display: none;" data-bind="if:$root.teacherClazzs().length != 0 || $root.noTeacherClazzs().length != 0,visible:$root.teacherClazzs().length != 0 || $root.noTeacherClazzs().length != 0">
            <div class="title"><i class="tIcon"></i>全省排行榜<i class="tIcon"></i></div>
            <div class="time">活动时间：5月22日－6月25日</div>
            <div class="tableRank">
                <table cellpadding="0" cellspacing="0">
                    <thead>
                    <tr>
                        <td width="160">排名</td>
                        <td>班级信息</td>
                        <td width="180">参与人次</td>
                        <td width="180">班级总分</td>
                    </tr>
                    </thead>
                    <tbody>
                    <!--ko foreach:{data:$root.teacherClazzs(),as:'clazz'}-->
                    <tr class="current" data-bind="visible:$root.expandAll() || $index() < 5">
                        <td><span class="num" data-bind="css:{'num01' : clazz.rank == 1,'num02':clazz.rank == 2,'num03':clazz.rank == 3,'txtRed':clazz.rank > 3},text:clazz.rank > 3 ? clazz.rank : ''">&nbsp;</span></td>
                        <td>
                            <p class="schoolName" data-bind="text:clazz.schoolName">&nbsp;</p>
                            <p class="clazz" data-bind="text:clazz.clazzName">&nbsp;</p>
                        </td>
                        <td>
                            <span class="count"><!--ko text:clazz.joinCount--><!--/ko-->人</span>
                        </td>
                        <td>
                            <span class="count"><!--ko text:clazz.totalScore--><!--/ko-->分</span>
                        </td>
                    </tr>
                    <!--/ko-->
                    <!--ko foreach:{data:$root.noTeacherClazzs(),as:'clazz'}-->
                    <tr data-bind="visible:($root.expandAll() || ($index() + $root.teacherClazzs().length) < 5)">
                        <td><span class="num" data-bind="css:{'num01' : clazz.rank == 1,'num02':clazz.rank == 2,'num03':clazz.rank == 3},text:clazz.rank > 3 ? clazz.rank : ''">&nbsp;</span></td>
                        <td>
                            <p class="schoolName" data-bind="text:clazz.schoolName">&nbsp;</p>
                            <p class="clazz" data-bind="text:clazz.clazzName">&nbsp;</p>
                        </td>
                        <td>
                            <span class="count"><!--ko text:clazz.joinCount--><!--/ko-->人</span>
                        </td>
                        <td>
                            <span class="count"><!--ko text:clazz.totalScore--><!--/ko-->分</span>
                        </td>
                    </tr>
                    <!--/ko-->
                    </tbody>
                </table>
                <div class="showMore" data-bind="visible:!$root.expandAll(),click:$root.expandAllClick">点击查看更多排行榜<i class="arrowIcon"></i></div>
            </div>
        </div>
    </div>
    <div class="btnBox"><a href="/teacher/newexam/independent/index.vpage?subject=ENGLISH" class="w-btn">去布置专项测试</a></div>
</div>
<@sugar.capsule js=["ko"] />
<script type="text/javascript">
$(function(){
    "use strict";
    ko.bindingHandlers.singleHover = {
        init: function(element, valueAccessor){
            $(element).hover(
                    function(){
                        $(element).addClass("show");
                    },
                    function(){
                        $(element).removeClass("show");
                    }
            );
        }
    };


    function NewExamAd(){
        this.rewardStatus = ko.observableArray([]);
        this.currentRewardStatus = ko.observable(null);
        this.rankNum = ko.observable(0);
        this.teacherClazzs = ko.observableArray([]);
        this.noTeacherClazzs = ko.observableArray([]);
        this.expandAll = ko.observable(false);
        this.run();
    }
    NewExamAd.prototype = {
        constructor : NewExamAd,
        run : function(){
            var self = this;
            $.get("/teacher/termreview/app/index.vpage",{},function(data){
                if(data.success){
                    var rewardStatus = $.isArray(data.rewardStatus) ? data.rewardStatus : [];
                    self.rewardStatus(rewardStatus);
                    rewardStatus.length > 0 && self.currentRewardStatus(rewardStatus[0]);
                    self.rankNum(+data.rankNum || 0);
                    var rankList = $.isArray(data.rankList) ? data.rankList : [];
                    var teacherClazzs = [],noTeacherClazzs = [];
                    $.each(rankList,function(index,obj){
                        if(_.isBoolean(obj.isCurrentTeacherClazz) && obj.isCurrentTeacherClazz){
                            teacherClazzs.push(obj);
                        }else{
                            noTeacherClazzs.push(obj);
                        }
                    });
                    self.teacherClazzs(teacherClazzs);
                    self.noTeacherClazzs(noTeacherClazzs);
                }
            }).fail(function(){

            });
        },
        changeClazz : function(index,self){
            var rewardStatus = self.rewardStatus();
            if(index > rewardStatus.length - 1){
                return false;
            }
            self.currentRewardStatus(self.rewardStatus()[index]);
        },
        expandAllClick : function(){
            this.expandAll(!this.expandAll());
        }
    };
    ko.applyBindings(new NewExamAd(), document.getElementById('newexam_ad'));
});
</script>
</@temp.page>