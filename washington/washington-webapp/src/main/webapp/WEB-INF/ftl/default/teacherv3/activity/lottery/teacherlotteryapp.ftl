<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="布置作业大抽奖"
pageJs=['jquery', 'impromptu', 'template']
pageCssFile={"css" : ["public/skin/project/teachernewlottery/app/css/skin"]}
>
<style>
    .w-hide{display: none;}
</style>
<div class="banner">
    <a href="/teacher/activity/lotteryappdesc.vpage" target="_blank" id="HeaderInfo" style="display: none;"><div class="tips">布置作业大抽奖活动下线预告</div></a>
</div>
<div class="lotteryBox">
    <div class="lotteryInner" id="lotteryItems">
        <div class="reward00" data-type="8"><i class="icon icon00"></i></div><!--谢谢参与-->
        <div class="reward01" data-type="5"><i class="icon icon03"></i></div><!--10园丁豆-->
        <div class="reward02" data-type="4"><i class="icon icon04"></i></div><!--50园丁豆-->
        <div class="reward03" data-type="3"><i class="icon icon05"></i></div><!--100园丁豆-->
        <div class="reward04" data-type="1"><i class="icon icon07"></i></div><!--小米平板电脑-->
        <div class="reward05" data-type="2"><i class="icon icon06"></i></div><!--红米手机-->
        <div class="reward06" data-type="7"><i class="icon icon01"></i></div><!--1园丁豆-->
        <div class="reward07" data-type="6"><i class="icon icon02"></i></div><!--5园丁豆-->
    </div>
    <div class="lotteryBtn">
        <a href="javascript:void(0)" class="btn JS-submitLottery">立即抽奖</a><!--disabled不可抽奖状态-->
        <p class="remind">免费抽奖<span id="currentCount">${count!0}</span>次</p>
    </div>
</div>
<div class="tipsBox">
   <#-- <p class="font30">最红星期五</p>
    <p>每周五布置作业后,可获得额外5次免费抽奖机会</p>-->
    <p class="title">抽奖详情</p>
</div>
<div class="rankList">
    <div class="title">大奖得主</div>
    <ul class="rank">
        <#if campaignLotteryResultsBig?size gt 0 >
            <#list campaignLotteryResultsBig as cr>
                <#if cr_index lt 3>
                    <li>
                        <i class="cup cup0${cr_index+1}"></i>
                        <span class="name"><#if (cr.userName)?has_content>${((cr.userName)!'-')?substring(0, 1)}</#if>老师</span><span class="address">${cr.schoolName!''}</span><span class="yellow">获得了${cr.awardName!'0'}</span>
                    </li>
                </#if>
            </#list>
        <#else>
            <div style="text-align: center; padding: 20px 0; font-size: .8rem; color: #fff;">暂无数据</div>
        </#if>
    </ul>
    <div class="title title02">获奖动态</div>
    <ul class="rank">
        <#if campaignLotteryResults?size gt 0 >
            <#list campaignLotteryResults as cr>
                <#if cr_index lt 6>
                    <li>
                        <span class="name"><#if (cr.userName)?has_content>${((cr.userName)!'-')?substring(0, 1)}</#if>老师</span><span class="address">${cr.schoolName!''}</span><span class="yellow">获得了${cr.awardName!'0'}</span>
                    </li>
                </#if>
            </#list>
        <#else>
            <div style="text-align: center; padding: 20px 0; font-size: .8rem; color: #fff;">暂无数据</div>
        </#if>
    </ul>
</div>
<div class="ruleBox">
    <div class="hd-bg"></div>
    <div class="ruleInner">
        <p class="title">抽奖规则</p>
        <p class="txt">1、抽奖资格：认证老师参与抽奖</p>
        <p class="txt">2、抽奖条件：老师周五布置作业，学生在周日24点前完成人数≥10人，老师检查作业时将获得10次免费抽奖机会（仅限当日使用）</p>
        <p class="txt">3、活动时间：2017年2月24日-2017年6月30日</p>
        <p class="txt">4、抽奖次数：手机端不限次抽奖，布置指定类型作业，中奖几率更高</p>
        <p class="txt">5、指定类型：</p>
        <p class="txt">小学英语：高频错题、口语练习(非跟读)、绘本阅读</p>
        <p class="txt">小学数学：高频错题、查缺补漏、口算、重难点视频专练、单元薄弱巩固</p>
        <p class="txt">7、发放规则：园丁豆即刻到账；实物奖品10个工作日内安排发放</p>
        <p class="txt" style="font-weight: bold">为鼓励学生周末巩固，特举办此活动，其他时间老师布置作业暂不参与此活动</p>
        <p class="txt">本活动最终解释权归一起作业网所有</p>
    </div>
</div>
<script type="text/html" id="T:抽奖Success">
    <div class="lotteryFlayer">
        <div class="lotteryInner tipsSuccess">
            <div class="title"><span class="text">恭喜！</span><span class="close" onclick="$.prompt.close();"></span></div>
            <div class="lt-main">
                <div class="txt" style="height: auto;">恭喜您获得<%if(content.awardName != ""){%><%=content.awardName%><%}%>！</div>
                <%if(content.awardId == 1 || content.awardId == 2){%>
                <div class="txt" style="font-size: .5rem; height: auto;">审核通过后7个工作日内发货，请耐心等待！</div>
                <%}%>
                <div class="btnBox">
                    <a href="javascript:void(0)" class="btn" onclick="$.prompt.close();">知道了</a>
                </div>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="T:抽奖Error">
    <div class="lotteryFlayer">
        <div class="lotteryInner">
            <div class="title"><span class="text">系统提示</span><span class="close" onclick="$.prompt.close();"></span></div>
            <div class="lt-main">
                <%if(typeInfo == 1){%>
                    <div class="txt" style="font-size:.7rem; line-height: 150%; height: auto;">您还未认证，认证老师才能参与本次活动</div>
                    <div class="btnBox">
                        <a href="javascript:;" onclick="$.prompt.close();" class="btn">知道了</a>
                    </div>
                <%}else if(typeInfo == 2){%>
                    <div class="txt" style="font-size: .7rem; line-height: 150%; height: auto;">您本周还未布置作业，布置作业后才能参与本次活动</div>
                    <div class="btnBox">
                        <a href="javascript:;" onclick="$.prompt.close();" class="btn">知道了</a>
                    </div>
                <%}else if(typeInfo == 3){%>
                    <div class="txt" style="font-size: .7rem; line-height: 150%; height: auto; padding-bottom: 15px;">平台抽奖次数已用完</div>
                    <%if(isWeChat){%>
                        <div class="btnBox">
                            <a href="http://wx.17zuoye.com/download/17teacherapp?cid=300136" target="_blank"  onclick="$.prompt.close();" class="btn">立即下载</a>
                        </div>
                    <%}%>
                <%}else{%>
                    <div class="txt" style="line-height: 150%; height: auto; padding-bottom: 15px; font-size: .7rem;"><%==dataInfo%></div>
                    <%if(btnType){%>
                    <div class="btnBox">
                        <a href="javascript:;" class="btn JS-confirmLottery" onclick="$.prompt.close();" data-type="<%=btnType%>">确定</a>
                    </div>
                    <%}%>
                <%}%>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
    signRunScript = function () {
        /*抽奖功能*/
        var currentIndex = 5, winningId = 8, speed = 200, rotaNumber = 0, lotteryAnimate;
        var freeLotteryCount = ${count!0};
        var isWeChat = function(){
            return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") != -1);
        };

        if(isWeChat()){
            $("#HeaderInfo").show();
        }

        var currentThisBtn;
        $(document).on("click", ".JS-submitLottery", function(){
            var $this = $(this);
            var $infoContent = "免费抽奖次数已用完<br/>继续抽奖消耗<strong>1</strong>园丁豆";

            if($this.hasClass("dis")){
                return false;
            }

            if(freeLotteryCount > 0){
                $infoContent = "好棒,本次抽奖机会免费!";
            }

            currentThisBtn = $this;

            $.prompt(template("T:抽奖Error", { typeInfo: 0, dataInfo: $infoContent, btnType: 'start'}), {
                prefix : "null",
                title  : '系统提示',
                buttons: {},
                classes: {
                    fade : 'jqifade',
                    close: 'w-hide',
                    title: 'w-hide'
                }
            });
        });

        $(document).on("click", ".JS-confirmLottery[data-type='start']", function(){
            if(currentThisBtn != null && !currentThisBtn.hasClass("dis")){
                lotteryPost(currentThisBtn);
            }
        });

        function lotteryPost($this){
            $this.addClass("dis");

            $.post("/campaign/7/lottery.vpage", {
                clientType: "APP"
            }, function(data){
                if(data.success && data.win){
                    //success
                    var $awardName;

                    if(freeLotteryCount > 0){
                        freeLotteryCount -= 1;

                        $("#currentCount").text(freeLotteryCount);
                    }

                    if(data.success && data.lottery.awardId){
                        winningId = data.lottery.awardId;
                        $awardName = data.lottery.awardName;
                    }



                    lotteryAnimate = setInterval(function(){
                        startRotational(function(){
                            if(winningId == 8){
                                $.prompt(template("T:抽奖Error", { typeInfo: 0, dataInfo: $awardName, btnType: '8'}), {
                                    prefix : "null",
                                    title  : '系统提示',
                                    buttons: {},
                                    classes: {
                                        fade : 'jqifade',
                                        close: 'w-hide',
                                        title: 'w-hide'
                                    }
                                });
                            }else{
                                $.prompt(template("T:抽奖Success", { content: {awardName: $awardName, awardId: winningId} }), {
                                    prefix : "nullSuccess",
                                    title  : '系统提示',
                                    buttons: {},
                                    classes: {
                                        fade : 'jqifade',
                                        close: 'w-hide',
                                        title: 'w-hide'
                                    }
                                });
                            }

                            $this.removeClass("dis");
                        });
                    }, speed);
                }else{
                    var typeInfo = 0;

                    if(data.info.indexOf("认证") >= 0){
                        typeInfo = 1;
                    }else if(data.info.indexOf("未布置作业") >= 0){
                        typeInfo = 2;
                    }else if(data.info.indexOf("今日平台抽奖次数已用完") >= 0){
                        typeInfo = 3;
                    }

                    $.prompt(template("T:抽奖Error", { typeInfo: typeInfo, dataInfo: data.info, isWeChat: isWeChat()}), {
                        prefix : "null",
                        title  : '系统提示',
                        buttons: {},
                        classes: {
                            fade : 'jqifade',
                            close: 'w-hide',
                            title: 'w-hide'
                        }
                    });

                    $this.removeClass("dis");
                }
            });
        }

        function startLottery(index, maxNum){
            if(index >= maxNum){
                index = 0;
            }else{
                index++
            }

            return index;
        }

        function startRotational(callback){
            var $lotteryBox = $("#lotteryItems div").eq(currentIndex);
            currentIndex = startLottery(currentIndex, 8);
            $lotteryBox.addClass("active").siblings().removeClass("active");

            //最后转动
            if(rotaNumber >= 5){
                clearInterval(lotteryAnimate);

                if(currentIndex == 0){
                    rotaNumber += 1;
                }

                if(rotaNumber >= 6 && (winningId > 8 || winningId <= 0)){
                    winningId = 8;
                }

                if(rotaNumber >= 6 && $lotteryBox.data("type") == winningId){
                    clearInterval(lotteryAnimate);
                    rotaNumber = 0;
                    if(callback){
                        callback();
                    }
                    return false;
                }

                lotteryAnimate = setInterval(function(){
                    startRotational(callback);
                }, 200);
            }else{
                if(currentIndex == 0){
                    rotaNumber += 1;
                }

                if(speed > 50){
                    speed -= 50;
                }

                //第二次转动
                if(speed <= 50){
                    clearInterval(lotteryAnimate);

                    lotteryAnimate = setInterval(function(){
                        startRotational(callback);
                    }, speed);
                }
            }
        }
    }
</script>
</@layout.page>