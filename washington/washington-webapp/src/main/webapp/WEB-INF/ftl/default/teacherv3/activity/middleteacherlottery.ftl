<#import "../../reward/layout/layout.ftl" as temp />
<@temp.page index="lottery">
<@app.css href="public/skin/project/middleteacherlottery/css/skin.css" />
<div class="t-arrangeDraw-box">
    <div class="container">
        <div class="side-lottery">
            <div class="lottery-container">
                <div class="btn-luck">
                    <a href="javascript:void(0);" class="turntable-btn" id="lotterySubmit"></a>
                </div>
                <ul id="lotteryItems">
                    <li class="turntable-unit-1" data-type="5">
                        <div class="winning-inner"><div class="winning-img-4"></div></div>
                    </li>
                    <li class="turntable-unit-2" data-type="4">
                        <div class="winning-inner"><div class="winning-img-1"></div></div>
                    </li>
                    <li class="turntable-unit-3" data-type="2">
                        <div class="winning-inner"><div class="winning-img-5"></div></div>
                    </li>
                    <li class="turntable-unit-4" data-type="7">
                        <div class="winning-inner"><div class="winning-img-2"></div></div>
                    </li>
                    <li class="turntable-unit-5" data-type="3">
                        <div class="winning-inner"><div class="winning-img-6"></div></div>
                    </li>
                    <li class="turntable-unit-6" data-type="8">
                        <div class="winning-inner"><div class="winning-img-7"></div></div>
                    </li>
                    <li class="turntable-unit-7" data-type="1">
                        <div class="winning-inner"><div class="winning-img-3"></div></div>
                    </li>
                    <li class="turntable-unit-8" data-type="6">
                        <div class="winning-inner"><div class="winning-img-8"></div></div>
                    </li>
                </ul>
            </div>
        </div>
        <div class="ad-right">
            <div class="award-dynamic">
                <div class="ad-title">获奖动态</div>
                <div class="top"></div>
                <div class="mid">
                    <ul style="height: 220px;">
                        <#-- 大奖得主-->
                        <#if (campaignLotteryResultsBig?size gt 0)!false>
                            <#list campaignLotteryResultsBig as cr>
                                <#if cr_index lt 3>
                                    <li class="list">
                                        <span class="name"><i class="tag-${cr_index+1}"></i>${cr.userName!''}</span><span class="area" title="${cr.schoolName!''}">${cr.schoolName!''}</span><span class="prize">获得了${cr.awardName!'0'}</span>
                                    </li>
                                </#if>
                            </#list>
                        <#else>
                            <li style="text-align: center; padding: 45px 0;">暂无大奖得主</li>
                        </#if>
                        <li class="list"><div class="line"></div></li>
                        <#--获奖动态-->
                        <#if (campaignLotteryResults?size gt 0)!false>
                            <#list campaignLotteryResults as cr>
                                <#if cr_index lt 3>
                                    <li class="list">
                                        <span class="name" style=" margin-left: 25px;">${cr.userName!''}</span><span class="area" title="${cr.schoolName!''}">${cr.schoolName!''}</span><span class="prize">获得了${cr.awardName!'0'}</span>
                                    </li>
                                </#if>
                            </#list>
                        <#else>
                            <li style="text-align: center; padding: 45px 0;">暂无获奖动态</li>
                        </#if>
                    </ul>
                </div>
                <div class="bot"></div>
            </div>
            <div class="ad-info">
                <div class="ad-rule">抽奖规则</div>
                <p>1.只有认证老师才可以参与抽奖，每次抽奖需消耗10学豆； </p>
                <p>2.布置作业后，老师当天可获5次免费抽奖机会（仅限当天使用）。</p>
                <p>3.抽中的学豆即刻到账；抽中的实物由工作人员联系后寄送；</p>
                <p>4.本期抽奖活动，2017年2月24日-2017年3月31日；</p>
                <p>5.在法律允许的范围内，一起作业拥有对本抽奖规则的解释权。</p>
            </div>
        </div>
    </div>
    <div class="m-footer">
        <div class="copyright">
        ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
        </div>
    </div>
</div>

<script type="text/html" id="T:结果提示">
    <%if(winningId < 8){%>
    <div class="t-winning-pop">
        <%if(winningId <= 2){%>
        <div class="winning-title">
            恭喜您获得 <%==(awardName)%>
        </div>
        <div class="notice">
            奖品将在审核通过后的7个工作日内发货，请耐心等待。
        </div>
        <%}%>

        <%if(winningId >= 3){%>
        <div class="winning-title">
            恭喜您获得
        </div>
        <div class="winning-info">
            <span><%==(awardName)%>&nbsp;</span>
        </div>
        <%}%>
        <div class="btn">
            <a href="javascript:void(0);" class="sure-btn" onclick="$.prompt.close();">确定</a>
        </div>
    </div>
    <%}else{%>
    <div class="t-notWinning-pop">
        <div class="notWinning-title">
            <i class="icon"></i>
            再接再厉
        </div>
        <div class="tip">
            很遗憾，什么也没抽中
        </div>
        <div class="btn">
            <a href="javascript:void(0);" class="sure-btn" onclick="$.prompt.close();">确定</a>
        </div>
    </div>
    <%}%>
</script>

<script type="text/javascript">
    (function(){
        /*抽奖功能*/
        var currentIndex = 5, winningId = 8, speed = 200, rotaNumber = 0, lotteryAnimate;
        var freeLotteryCount = ${count!0};

        $("#lotterySubmit").on({
            click : function(){
                var $this = $(this);
                var $infoContent = "本次抽奖将消耗10学豆";

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
            }
        });

        function lotteryPost($this){
            $this.addClass("dis");

            $.post("/campaign/46/lottery.vpage", {}, function(data){
                if(data.success && data.win){
                    //success
                    var $awardName;

                    if(freeLotteryCount > 0){
                        freeLotteryCount -= 1;
                    }

                    if(data.success && data.lottery.awardId){
                        winningId = data.lottery.awardId;
                        $awardName = data.lottery.awardName;
                    }

                    lotteryAnimate = setInterval(function(){
                        startRotational(function(){
                            $.prompt( template("T:结果提示", {winningId : winningId, awardName : $awardName}) ,{
                                prefix : "null-popup",
                                buttons : { },
                                classes : {
                                    fade: 'jqifade',
                                    close: 'w-hide'
                                }
                            });

                            $this.removeClass("dis");
                        });
                    }, speed);
                }else{
                    // error
                    if(data.info.indexOf("认证") >= 0){
                        $.prompt(data.info, {
                            title : "系统提示",
                            buttons : {"去认证" : true},
                            submit : function(e, v){
                                if(v){
                                    window.location.href = "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage";
                                }
                            }
                        });
                    }else if(data.info.indexOf("未布置作业") >= 0){
                        $.prompt(data.info, {
                            title : "系统提示",
                            buttons : {"去布置" : true},
                            submit : function(e, v){
                                if(v){
                                    window.location.href = "${(ProductConfig.getJuniorSchoolUrl())!''}/teacher/assign/index";
                                }
                            }
                        });
                    }else{
                        $17.alert(data.info, function(){
                            $this.removeClass("dis");
                        });
                    }
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
            var $lotteryBox = $("#lotteryItems li").eq(currentIndex);
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
    })();
</script>
</@temp.page>