<#import "../../../reward/layout/layout.ftl" as temp />
<@temp.page index="lottery">
<@app.css href="public/skin/project/teachernewlottery/css/skin.css" />
<style>
    .w-head{background-color: #fff;}
    .footer .nav ul li, .footer .nav ul li a,
    .footer .copyright p{color: #fff;}
    .w-content{width: auto;min-width: 1000px;}
</style>
<div class="section-01">
    <div class="wrap">
        <div class="main" style="overflow: visible;">
            <div class="clearfix">
                <div class="code" id="weChatBackgroundAreaCode"><img src="<@app.link href='public/skin/project/teachernewlottery/images/img-01.png'/>"></div>
                <div class="white clearfix">
                    <p class="font18">作业结束日期不超过当周周日23:59</p>
                    <div class="prom">温馨提示:  每天在电脑上最多抽奖5次，超过5次请用微信扫描上方二维码，到老师APP上抽取</div>
                </div>
            </div>
            <a href="javascript:;" class="btn JS-submitLottery">立即抽奖</a>
            <p class="remind" style="text-align: center; color: #fff; margin-top: -5px;">免费抽奖<span id="currentCount">${count!0}</span>次</p>
        </div>
        <div id="lotteryItems">
            <div class="icon-01" data-type="7"></div>
            <div class="icon-02" data-type="6"></div>
            <div class="icon-03" data-type="8"></div>
            <div class="icon-04" data-type="5"></div>
            <div class="icon-05" data-type="4"></div>
            <div class="icon-06" data-type="3"></div>
            <div class="icon-07" data-type="1"></div>
            <div class="icon-08" data-type="2"></div>
        </div>
    </div>
</div>
<div class="head">抽奖详情</div>
<div class="section-02">
    <div class="wrap">
        <div class="head-1">奖项设置</div>
        <div class="head-2">大奖得主</div>
        <div class="head-3">奖励动态</div>
        <div class="head-4">抽奖规则</div>
        <div class="text-1">
            <div>一等奖：小米平板</div>
            <div>二等奖：红米手机</div>
            <div>三等奖：100园丁豆</div>
            <div>四等奖：50园丁豆</div>
            <div>五等奖：10园丁豆</div>
            <div>六等奖：5园丁豆</div>
            <div>七等奖：1园丁豆</div>
        </div>
        <div class="text-2">
            <#if campaignLotteryResultsBig?size gt 0 >
                <#list campaignLotteryResultsBig as cr>
                    <#if cr_index lt 3>
                        <div class="n${cr_index+1}">
                            <#if (cr.userName)?has_content>${((cr.userName)!'-')?substring(0, 1)}</#if>老师
                            &nbsp;&nbsp;&nbsp;${cr.schoolName!''}&nbsp;&nbsp;<span>获得了${cr.awardName!'0'}</span>
                        </div>
                    </#if>
                </#list>
            <#else>
                <div style="text-align: center; padding: 20px 0;">暂无数据</div>
            </#if>
        </div>
        <div class="text-3">
            <#if campaignLotteryResults?size gt 0 >
                <#list campaignLotteryResults as cr>
                    <#if cr_index lt 6>
                        <div>
                            <#if (cr.userName)?has_content>${((cr.userName)!'-')?substring(0, 1)}</#if>老师
                            &nbsp;&nbsp;&nbsp;${cr.schoolName!''}&nbsp;&nbsp;<span>获得了${cr.awardName!'0'}</span>
                        </div>
                    </#if>
                </#list>
            <#else>
                <div style="text-align: center; padding: 20px 0;">暂无数据</div>
            </#if>
        </div>
        <div class="text-4" style="padding-bottom: 20px; line-height: 180%;">
            <div>1、抽奖资格：认证老师参与抽奖</div>
            <div>2、抽奖条件：老师周五布置作业，学生在周日24点前完成人数≥10人，老师检查作业时将获得10次免费抽奖机会（仅限当日使用）</div>
            <div>3、活动时间：2017年2月24日-2017年6月30日</div>
            <div>4、抽奖次数：每天可在电脑上抽奖5次，布置指定类型作业，中奖几率更高</div>
            <div>5、指定类型：</div>
            <div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;小学英语：高频错题、口语练习(非跟读)、绘本阅读</div>
            <div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;小学数学：高频错题、查缺补漏、口算、重难点视频专练、单元薄弱巩固</div>
            <div>7、发放规则：园丁豆即刻到账；实物奖品10个工作日内安排发放</div>
            <div style="font-weight: bold">为鼓励学生周末巩固，特举办此活动，其他时间老师布置作业暂不参与此活动</div>
            <div>本活动最终解释权归一起作业网所有</div>
        </div>
    </div>
</div>
<script type="text/html" id="T:抽奖Success">
    <div class="lotteryFlayer">
        <div class="lotteryInner tipsSuccess">
            <div class="title"><span class="text">恭喜！</span><span class="close" onclick="$.prompt.close();"></span></div>
            <div class="lt-main">
                <div class="txt" style="height: auto;">恭喜您获得<%if(content.awardName != ""){%><%=content.awardName%><%}%>！</div>
                <%if(content.awardId == 1 || content.awardId == 2){%>
                    <div class="txt" style="font-size: 18px; height: auto;">审核通过后7个工作日内发货，请耐心等待！</div>
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
                    <div class="txt" style="font-size: 18px; line-height: 150%; height: auto;">您还未认证，认证老师才能参与本次活动</div>
                    <div class="btnBox">
                        <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage" onclick="$.prompt.close();" class="btn">现在去认证</a>
                    </div>
                <%}else if(typeInfo == 2){%>
                    <div class="txt" style="font-size: 18px; line-height: 150%; height: auto;">您本周还未布置作业，布置作业后才能参与本次活动</div>
                    <div class="btnBox">
                        <a href="/teacher/homework/batchassignhomework.vpage" onclick="$.prompt.close();" class="btn">去布置作业</a>
                    </div>
                <%}else if(typeInfo == 3){%>
                    <div class="txt" style="font-size: 18px; line-height: 150%; height: auto;">
                        平台抽奖次数已用完<br/>
                        继续抽奖请用下载老师APP，到手机上抽奖<br/>电脑上每天只能抽奖5次！
                    </div>
                    <div class="btnBox">
                        <a href="/help/downloadApp.vpage?refrerer=pc&count=0" target="_blank"  onclick="$.prompt.close();" class="btn">立即下载老师APP</a>
                    </div>
                <%}else{%>
                    <div class="txt" style="line-height: 150%; height: auto; padding-bottom: 15px;"><%=dataInfo%></div>
                <%}%>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
    (function(){
        /*抽奖功能*/
        var currentIndex = 5, winningId = 8, speed = 200, rotaNumber = 0, lotteryAnimate;
        var freeLotteryCount = ${count!0};

        $(document).on("click", ".JS-submitLottery", function(){
            var $this = $(this);
            var $infoContent = "免费抽奖次数已用完<br/>继续抽奖消耗<strong>1</strong>园丁豆";

            if($this.hasClass("dis")){
                return false;
            }

            if(freeLotteryCount > 0){
                $infoContent = "好棒,本次抽奖机会免费!";
            }

            $.prompt($infoContent, {
                focus : 1,
                title : "系统提示",
                buttons : { "取消":false, "确定" : true },
                submit : function(e, v){
                    if(v){
                        lotteryPost($this);
                    }
                }
            });
        });

        function lotteryPost($this){
            $this.addClass("dis");

            $.post("/campaign/7/lottery.vpage", {
                clientType: "PC"
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
                                    loaded : function(){

                                    },
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

                    $.prompt(template("T:抽奖Error", { typeInfo: typeInfo, dataInfo: data.info}), {
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
    }());
</script>
</@temp.page>