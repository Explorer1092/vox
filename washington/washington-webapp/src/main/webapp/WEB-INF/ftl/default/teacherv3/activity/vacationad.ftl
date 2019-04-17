<#import "../../layout/project.module.ftl" as temp />
<@temp.page header="show">
    <@app.css href="public/skin/project/vacationhomework/css/skin.css" />
<div class="sVacation-banner"></div>
<div class="sVacation-main" id="vacation_ad">
    <!--ko if:$root.loading() == 'loading'-->
    <div style="height: 200px; background-color: white; width: 98%;">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="display:block;margin: 0 auto;" />
    </div>
    <!--/ko-->
    <!--ko if:$root.loading() == 'fail'-->
    <div class="sVacation-section" style="padding-top: 20px;">
        <div class="informTxt" style="font-size: 16px;" data-bind="text:$root.info()"></div>
    </div>
    <!--/ko-->
    <!--ko if:$root.loading() == 'success'-->
    <div class="rewardRule"></div>
    <div class="sVacation-section">
        <div class="hd"><span class="label">奖励1</span>布置立得100园丁豆<span class="label lightBlue fr" data-bind="text:$root.hasReward1() ? '已获得' : '未获得'">&nbsp;</span></div>
        <div class="informTxt">人人有奖！布置假期作业立即获得<span class="txtYellow">100园丁豆</span></div>
    </div>
    <div class="sVacation-section">
        <div class="hd">
            <span class="label">奖励2</span>复习 · 第1阶段奖励
            <span class="tipsLabel"></span>
        </div>
        <div class="informTxt">10人完成本阶段作业，检查获得园丁豆</div>
        <div class="informTxt">第一阶段：前两周作业（第1-8天）</div>
        <!--ko foreach:{data:$root.rewardStatus(),as:'clazz'}-->
        <div class="rewardList">
            <div class="reward">
                <a href="javascript:void(0)" class="btn" data-bind="css:{'disabled' : clazz.reward2Status() == 'REWARDED' || clazz.reward2Status() == 'UNFINISHED'},text: clazz.reward2Status() == 'REWARDED' ? '已检查':'检查作业',click:$root.checkHomework.bind($data,$root,'reward2Status',1)">&nbsp;</a>
                <p class="txtBlue-s">+50园丁豆</p>
            </div>
            <div class="info">
                <p data-bind="text:$root.generateClazzName($data,1)">&nbsp;</p>
            </div>
        </div>
        <!--/ko-->
    </div>
    <div class="sVacation-section">
        <div class="hd">
            <span class="label">奖励3</span>复习·第二阶段
        </div>
        <div class="informTxt">10人完成本阶段作业，检查获得园丁豆</div>
        <div class="informTxt">第二阶段：第三四周作业（第9-16天）</div>
        <!--ko foreach:{data:$root.rewardStatus(),as:'clazz'}-->
        <div class="rewardList">
            <div class="reward">
                <a href="javascript:void(0)" class="btn" data-bind="css:{'disabled' : clazz.reward3Status() == 'REWARDED' || clazz.reward3Status() == 'UNFINISHED'},text: clazz.reward3Status() == 'REWARDED' ? '已检查':'检查作业',click:$root.checkHomework.bind($data,$root,'reward3Status',2)">&nbsp;</a>
                <p class="txtBlue-s">+50园丁豆</p>
            </div>
            <div class="info">
                <p data-bind="text:$root.generateClazzName($data,2)">&nbsp;</p>
            </div>
        </div>
        <!--/ko-->
    </div>
    <div class="sVacation-section">
        <div class="hd">
            <span class="label">奖励4</span>
            <!--ko if:$root.subject() == 'ENGLISH'-->
            <!--ko text:'预习·经典动画拓展'--><!--/ko-->
            <!--/ko-->
            <!--ko if:$root.subject() == 'MATH'-->
            <!--ko text:'复习收关·预习新概念'--><!--/ko-->
            <!--/ko-->
            <!--ko if:$root.subject() == 'CHINESE'-->
            <!--ko text:'预习·新字词/课文'--><!--/ko-->
            <!--/ko-->
        </div>
        <div class="informTxt">10人完成本阶段作业，检查获得园丁豆</div>
        <div class="informTxt">第五六七周作业（第17-28天）</div>
        <!--ko foreach:{data:$root.rewardStatus(),as:'clazz'}-->
        <div class="rewardList">
            <div class="reward">
                <a href="javascript:void(0)" class="btn" data-bind="css:{'disabled' : clazz.reward4Status() == 'REWARDED' || clazz.reward4Status() == 'UNFINISHED'},text: clazz.reward4Status() == 'REWARDED' ? '已检查':'检查作业',click:$root.checkHomework.bind($data,$root,'reward4Status',3)">&nbsp;</a>
                <p class="txtBlue-s">+70园丁豆</p>
            </div>
            <div class="info">
                <p data-bind="text:$root.generateClazzName($data,3)">&nbsp;</p>
            </div>
        </div>
        <!--/ko-->
    </div>
    <!--/ko-->
</div>
    <@sugar.capsule js=["ko"] />
<script type="text/javascript">
    $(function(){
        "use strict";
        function VacationAd(){
            var self = this;
            self.loading = ko.observable("loading"); //loading : 加载中,success: 加载成功,fail:加载失败
            self.info   = ko.observable("");
            self.subject = ko.observable("");
            self.hasReward1 = ko.observable(false);
            self.rewardStatus = ko.observableArray([]);
            self.checkHomeworkLocks = [];
            self.run();
        }
        VacationAd.prototype = {
            constructor : VacationAd,
            checkHomework     : function(self,rewardLevel,level){
                var clazzObj = this;
                var cacheKey = [clazzObj.groupId(),rewardLevel].join("_");
                var lockCaches = self.checkHomeworkLocks;

                if(clazzObj[rewardLevel]() == "UNFINISHED" || clazzObj[rewardLevel]() == "REWARDED"){

                    return false;
                }
                if(lockCaches.indexOf(cacheKey) != -1){

                    $17.alert("请求已经发送，请勿重复点击");
                    return false;
                }

                lockCaches.push(cacheKey);
                $.get("/teacher/vacation/check.vpage",{
                    level   : level,
                    groupId : clazzObj.groupId()
                },function(data){
                    if(data.success){

                        clazzObj[rewardLevel]("REWARDED");
                    }else{

                        $17.alert(data.info || "获取奖励失败");
                    }
                }).always(function(){
                    var index = lockCaches.indexOf(cacheKey);
                    (index != -1) && (lockCaches.splice(lockCaches.indexOf(cacheKey),1));
                });
                $17.voxLog({
                    module: "m_elhqnSjz",
                    op : "vacation_homework_activity_check_click",
                    s0 : self.subject()
                });
            },
            generateClazzName : function(clazz,rewardLevel){
                var name = [clazz.subject(),rewardLevel].join("_");
                var tempText = clazz && clazz.clazzName && clazz.clazzName() ? clazz.clazzName() : "";
                switch(name){
                    case "ENGLISH_1":
                        tempText += "- 个性化学期巩固·主题绘本";
                        break;
                    case "ENGLISH_2":
                        tempText += "- 学期巩固收关·绘本内容升级";
                        break;
                    case "ENGLISH_3":
                        tempText += "- 新学期单词/课文·经典动画";
                        break;
                    case "CHINESE_1":
                        tempText += "- 学期知识·温故知新";
                        break;
                    case "CHINESE_2":
                        tempText += "- 学期知识·温故知新";
                        break;
                    case "CHINESE_3":
                        tempText += "- 新学期字词/课文早接触";
                        break;
                    case "MATH_1":
                        tempText += "- 个性化学期巩固·生活数学绘本";
                        break;
                    case "MATH_2":
                        tempText += "- 个性化学期巩固·生活数学绘本";
                        break;
                    case "MATH_3":
                        tempText += "- 学期巩固收关·精彩视频学习";
                        break;
                    default:
                }
                return tempText;
            },
            run         : function(){
                var self = this;
                $.get("/teacher/vacation/activity/index.vpage",{},function(data){
                    if(data.success){
                        self.subject(data.subject || "");
                        self.hasReward1(data.hasReward1 || false);
                        $.isArray(data.rewardStatus) && (self.rewardStatus(ko.mapping.fromJS(data.rewardStatus)()));

                        //打点加载这，不然subject拿不到
                        $17.voxLog({
                            module: "m_elhqnSjz",
                            op : "vacation_homework_activity_load",
                            s0 : data.subject
                        });
                    }else{
                        self.info(data.info || "数据加载失败,请稍候再试");
                    }
                    self.loading(data.success ? "success" : "fail");
                }).fail(function(){
                    self.info("数据加载失败,请稍候再试");
                    self.loading("fail");
                });
            }
        };

        ko.applyBindings(new VacationAd(), document.getElementById('vacation_ad'));
    });
</script>
</@temp.page>