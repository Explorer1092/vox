<#import "../../reward/layout/layout.ftl" as temp />
<@temp.page index="lottery">
<@app.css href="public/skin/project/studentlottery/css/skin.css" />
<#--41, 42, 43, 44-->
<#assign flagABC = (campaignId == 41 || campaignId == 42 || campaignId == 43)!false lotteryBeanCount = 2/><#--差异 bean count-->
<#assign flagD = (campaignId == 44)!false/><#--差异 priez-->
<#macro lotteryContent type=1>
    <div class="prize-content lottery_result_${type!1}">
    <#list 1..5 as item>
        <#if flagD>
            <p class="t-drawIcon t-drawIcon-2"><span>10学豆</span></p>
            <p class="t-drawIcon t-drawIcon-2"><span>10学豆</span></p>
            <p class="t-drawIcon t-drawIcon-3"><span>50学豆</span></p>
            <p class="t-drawIcon t-drawIcon-4"><span>100学豆</span></p>
            <p class="t-drawIcon t-drawIcon-5"><span>200学豆</span></p>
            <p class="t-drawIcon t-drawIcon-6"><span>500学豆</span></p>
       <#else>
           <p class="t-drawIcon t-drawIcon-2"><span>10学豆</span></p>
           <p class="t-drawIcon t-drawIcon-1"><span>50学豆碎片</span></p>
           <p class="t-drawIcon t-drawIcon-3"><span>50学豆</span></p>
           <p class="t-drawIcon t-drawIcon-4"><span>100学豆</span></p>
           <p class="t-drawIcon t-drawIcon-5"><span>200学豆</span></p>
           <p class="t-drawIcon t-drawIcon-6"><span>500学豆</span></p>
        </#if>
    </#list>
    </div>
</#macro>
<div class="t-studentsDraw-box">
    <div class="turntable">
        <div class="turntable-title">3张图片一样表示中奖哟！</div>
        <div class="slider-box">
            <ul>
                <li class="child">
                    <@lotteryContent type=1/>
                </li>
                <li class="child">
                    <@lotteryContent type=2/>
                </li>
                <li class="child">
                    <@lotteryContent type=3 />
                </li>
            </ul>
        </div>
        <div class="hp-btn">
            <a class="draw-btn <#--disabled-->" id="submitInfo" href="javascript:void(0);"></a>
        </div>
        <div class="turntable-info"><i class="turntable-triangle"></i><p>抽奖一次需要<br>花费${lotteryBeanCount!1}学豆</p></div>
    </div>

    <div class="sd-container">
        <#if flagABC!false>
            <div class="sd-fragment">
                获得“50学豆碎片”的个数：<i class="fragment-icon"></i> <span class="symbol"> ×<span class="js-fragmentCount">${fragmentCount!}</span></span> <b><i class="warning-icon"></i>（每集齐5个，直接兑换50学豆。碎片有效期10天，过期作废）</b>
            </div>
            <div class="sd-left">
                <div class="sd-title">
                    <h1>免费抽奖机会</h1>
                    <p>（抽奖过程中随机赠送）</p>
                </div>
                <div class="sd-content">
                    <a href="javascript:void(0);" class="share_student shareStudentFreeBtn" data-type="defaultFree">分享给同学</a>
                    <p>我的免费抽奖次数：<span class="js-countNo">${myCount!0}</span></p>
                    <p>可分享给同学的免费抽奖次数：<span>${sendCount!0}</span></p>
                    <p class="gray">免费抽奖机会当天有效，抓紧使用哦！</p>
                </div>
            </div>
            <div class="sd-right">
                <h2>谁赠送我抽奖机会：</h2>
                <ul>
                <#if (sendList?size gt 0)!false>
                    <#list sendList as item>
                        <#if item_index lt 4>
                            <li>${(item.senderName)!'---'}送给我 <span>${(item.count)!'--'}</span> 次免费抽奖机会</li>
                        </#if>
                    </#list>
                <#else>
                    <li class="sd-empty" style="padding: 40px 0;">没有免费机会</li>
                </#if>
                </ul>
            </div>
        </#if>
        <div class="sd-column">
            <h3>奖项设置</h3>
            <div class="award">
                <ul>
                    <li><b>一等奖</b><p class="t-drawIcon t-drawIcon-6"><span>500学豆</span></p></li>
                    <li><b>二等奖</b><p class="t-drawIcon t-drawIcon-5"><span>200学豆</span></p></li>
                    <li><b>三等奖</b><p class="t-drawIcon t-drawIcon-4"><span>100学豆</span></p></li>
                    <li><b>四等奖</b><p class="t-drawIcon t-drawIcon-3"><span>50学豆</span></p></li>
                    <li><b>五等奖</b><p class="t-drawIcon t-drawIcon-2"><span>10学豆</span></p></li>
                </ul>
            </div>
        </div>
        <div class="sd-column">
            <h3>活动规则</h3>
            <div class="info">
                <p> <b>01</b> 每抽一次消耗${lotteryBeanCount!1}学豆</p>
                <p> <b>02</b> 每天抽奖不限次数</p>
                <p> <b>03</b> 学豆即刻到账</p>
            </div>
        </div>
        <div class="sd-column">
            <h3>获奖动态</h3>
            <#if (campaignLotteryResults?size gt 0)!false>
                <div class="list">
                    <ul>
                        <#list campaignLotteryResults as item>
                            <li class="<#if item_index%4 == 0 || item_index%4 == 1></#if>bgColor1"><span class="con">${(item.userName)!'---'}同学抽中了</span><span>${(item.awardName)!'---'}</span></li>
                        </#list>
                    </ul>
                </div>
            <#else>
                <div class="empty">
                    <span class="emptyBg">还没有同学中奖哦，说不定你就是第一个呢！</span>
                </div>
            </#if>
        </div>
    </div>
    <div class="m-footer">
        <div class="copyright">
        ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
        </div>
    </div>
</div>

<script type="text/html" id="T:结果提示">
    <div class="t-studentsDraw-pop<#-- sharePop-->">
        <div class="close close-popupBtn">×</div>
        <div class="prompt"><%=info%></div>
        <div class="btn">
            <%if(type == 1){%><#--是否开始抽奖-->
                <a href="javascript:void(0);" class="btn-cancel close-popupBtn">取消</a>
                <a href="javascript:void(0);" class="btn-determine submitLotteryBtn">确定</a>
            <%}else if(type == 2){%><#--未中奖-->
                <a href="javascript:void(0);" class="btn-next close-popupBtn">下回再说</a>
                <a href="javascript:void(0);" class="btn-determine submitLotteryBtn">确定</a>
            <%}else if(type == 0){%>
                <a href="javascript:void(0);" class="btn-next close-popupBtn">知道了</a>
            <%}else{%>
                <a href="javascript:void(0);" class="btn-gotIt submitLotteryBtn">再抽一次</a>
            <%}%>
        </div>
    </div>
</script>

<script type="text/html" id="T:送免费抽奖">
    <div class="sharePop">
        <div class="close close-popupBtn">×</div>
        <div class="prompt">
            <p>没中奖别灰心，送你 <span>5</span>次免费抽奖机会</p>
            <p><span>3</span>次可以自己抽，<span>2</span>次分享给同学</p>
        </div>
        <div class="btn">
            <a href="javascript:void(0);" class="btn-firstDraw submitLotteryBtn">自己先抽</a>
            <a href="javascript:void(0);" class="btn-share shareStudentFreeBtn">分享给同学</a>
        </div>
    </div>
</script>

<script type="text/html" id="T:systemPromptPop">
    <div class="systemPromptPop1">
        <div class="sp-title">送给任意同学2次免费抽奖机会</div>
        <ul class="js-studentName">
            <%for (var i = 0; i < studentList.length; i++) {%>
                <li data-id="<%=studentList[i].studentId%>">
                    <a href="javascript:void(0);"><i class="name-icon"></i><%=studentList[i].studentName%></a>
                </li>
            <%}%>
        </ul>
    </div>
    <div style="width: 100%; clear: both;"></div>
</script>

<script type="text/html" id="T:fragment-pop">
<%if(exchanged){%>
    <div class="fragment-pop1">
        <div class="fp-close">
            <a class="close" href="javascript:void(0);" onclick="$.prompt.close();"></a>
        </div>
        <div class="fp-title">你抽中1张”50学豆碎片“，已集齐5张，得到50学豆</div>
        <div class="fp-btn">
            <a class="submitLotteryBtn" href="javascript:void(0);">再抽一次</a>
        </div>
    </div>
<%}else{%>
    <div class="fragment-pop2">
        <div class="fp-close">
            <a class="close" href="javascript:void(0);" onclick="$.prompt.close();"></a>
        </div>
        <div class="fp-title">你抽中1张”50学豆碎片“，集齐5张将会得到50学豆</div>
        <div class="fp-btn">
            <a class="submitLotteryBtn" href="javascript:void(0);">再抽一次</a>
        </div>
    </div>
<%}%>
</script>

<script type="text/javascript">
    (function(){
        // 抽奖页面下线，重定向到空页面提示
        window.location.replace('/project/common/emptytip.vpage?type=1');

        var userIntegral = ${((currentUser.userType == 3)!false)?string("${(currentStudentDetail.userIntegral.usable)!0}", "0")};
        var campaignId = ${campaignId!0};
        var lotteryBeanCount = ${lotteryBeanCount!2};
        var myFreeLotteryCount = ${myCount!0};
        var myFragmentCount = ${fragmentCount!0};

        //分享入口
        var currentStudentId;
        var currentSendCount = ${sendCount!0};

        $(document).on("click", ".shareStudentFreeBtn",function(){
            var $dataType = $(this).attr("data-type");

            if(currentSendCount < 1 && $dataType == "defaultFree"){
                $.prompt("暂时还没有可以分享给同学的抽奖机会哦！", {
                    title: "系统提示",
                    buttons: {"知道了": true }
                });
            }else {
                currentStudentId = null;
                $.get("/campaign/getclassmates.vpage", {}, function (result) {
                    if (result.success) {
                        var $statesDemo = {
                            state0: {
                                title : "分享给同学",
                                html : template("T:systemPromptPop", {studentList : result.data}),
                                position : { width : 600},
                                buttons: { 确定赠送: true },
                                submit:function(e,v){
                                    if(v){
                                        e.preventDefault();

                                        //是否选择学生
                                        if(!$17.isBlank(currentStudentId)){
                                            currentSendCount -= 2;

                                            $.post("/campaign/sendlotterychance.vpage", {
                                                studentId : currentStudentId,
                                                campaignId : ${campaignId!0}
                                            }, function(data){
                                                if(data.success){
                                                    $.prompt.goToState('state1');
                                                }else{
                                                    $17.alert(data.info);
                                                }
                                            });
                                        }else{
                                            $.prompt.goToState('state2');
                                        }
                                        return false;
                                    }
                                }
                            },
                            state1: {
                                html:'赠送成功！快去告诉他这个好消息！',
                                buttons: { "知道了" :false },
                                submit:function(e){
                                    e.preventDefault();
                                    $.prompt.close();
                                    location.reload();
                                }
                            },
                            state2: {
                                html:'请先选择要赠送的同学！',
                                buttons: { "知道了" :false },
                                submit:function(e){
                                    e.preventDefault();
                                    $.prompt.goToState('state0');
                                }
                            }
                        };

                        $.prompt($statesDemo);
                    }
                });
            }
        });

        //分享选择学生
        $(document).on("click",".js-studentName li",function(){
            var $this = $(this);
            $this.addClass("active").siblings().removeClass("active");

            currentStudentId = $this.attr("data-id");
        });

        //close popup
        $(document).on("click", ".close-popupBtn", function(){
            $.prompt.close();
            //location.reload();
        });

        //info
        $(document).on("click", "#submitInfo", function(){
            var infoObj = {info : (myFreeLotteryCount > 0 ? "免费抽奖机会" : ("是否要消耗"+ lotteryBeanCount +"学豆，抽1次奖？")), type : 1};

            if(userIntegral < lotteryBeanCount && myFreeLotteryCount < 1){
                infoObj = {info : "你的学豆不足！", type : 0};
            }

            if( $(this).hasClass("disabled") ){
                return false;
            }

            $.prompt( template("T:结果提示", infoObj) ,{
                prefix : "null-popup",
                buttons : { },
                classes : {
                    fade: 'jqifade',
                    close: 'w-hide'
                }
            });
        });

        //click submit lottery
        $(document).on("click", ".submitLotteryBtn", function(){
            var $lotteryBtn = $("#submitInfo");

            $.prompt.close();

            if( $lotteryBtn.hasClass("disabled") ){
                return false;
            }

            $lotteryBtn.addClass("disabled");

            if(myFreeLotteryCount > 0){
                myFreeLotteryCount -= 1;
                $(".js-countNo").text(myFreeLotteryCount);
            }

            $.ajax({
                type: "GET",
                url: "/campaign/" + campaignId + "/lottery.vpage",
                data: {},
                success: function(data){
                    var resultItem = {
                        target1 : ".lottery_result_1",
                        target2 : ".lottery_result_2",
                        target3 : ".lottery_result_3"
                    };

                    if(data.success && data.win){
                        if(data.win && data.lottery.awardId <= 6){
                            resultItem.awardId = data.lottery.awardId;

                            var getRewardCount = [0, 500, 200, 100, 50, 0, 10];

                            if(data.exchanged){
                                getRewardCount[5] = 50;
                            }else{
                                getRewardCount[5] = 0;
                            }

                            userIntegral += getRewardCount[resultItem.awardId];
                        }

                        lotteryGoTo(resultItem, function(){
                            var relustInfo = {};

                            if(myFreeLotteryCount < 1){
                                userIntegral -= lotteryBeanCount;
                            }

                            //碎片 || 学豆
                            if(data.fragment){
                                $.prompt( template("T:fragment-pop",{exchanged : (data.exchanged ? true : false)}) ,{
                                    prefix : "null-popup",
                                    buttons : { },
                                    classes : {
                                        fade: 'jqifade',
                                        close: 'w-hide'
                                    }
                                });

                                if(data.exchanged){
                                    $(".js-fragmentCount").text(myFragmentCount -= 4);
                                }else{
                                    $(".js-fragmentCount").text(myFragmentCount += 1);
                                }
                            }else{
                                //送免费抽奖机会
                                if(data.free){
                                    $.prompt( template("T:送免费抽奖",{}) ,{
                                        prefix : "null-popup",
                                        buttons : { },
                                        classes : {
                                            fade: 'jqifade',
                                            close: 'w-hide'
                                        }
                                    });

                                    myFreeLotteryCount += 3;
                                }else{
                                    if(data.lottery.awardId <= 6){
                                        relustInfo = {info : "运气爆棚！你抽中了"+ data.lottery.awardName +"！"};
                                    }else{
                                        relustInfo = {info : "啊噢，运气差了点，再来一次吧！", type : 2};
                                    }

                                    $.prompt( template("T:结果提示", relustInfo) ,{
                                        prefix : "null-popup",
                                        buttons : { },
                                        classes : {
                                            fade: 'jqifade',
                                            close: 'w-hide'
                                        }
                                    });
                                }
                            }

                            $lotteryBtn.removeClass("disabled");
                        });
                    }else{
                        $17.alert(data.info, function(){
                            $lotteryBtn.removeClass("disabled");
                        });
                    }
                },
                error : function(){
                    $17.alert("网络错误!", function(){
                        $lotteryBtn.removeClass("disabled");
                    });
                }
            });
        });

        //重复
        function repeatItem(a, b){
            if(a == b){
                b = parseInt(Math.random() * 4);
                console.info("==== : " + b);
                if(a == b){
                    b = repeatItem(a, b);
                }
            }

            return b;
        }

        //转动效果
        function lotteryGoTo(items, callback){
            var TextNum1, TextNum2, TextNum3;
            var rewardItems = [5, 4, 3, 2, 1, 0];
            var prizeHeight = 93;
            var animateCount = 25;

            TextNum1 = parseInt(Math.random()*4);
            TextNum2 = parseInt(Math.random()*4);
            TextNum3 = parseInt(Math.random()*4);

            //中奖结果
            if(items.awardId && items.awardId <= 6){
                TextNum1 = TextNum2 = TextNum3 = (items.awardId - 1);
            }else{
                //未中奖，防止重复
                if(TextNum1 == TextNum2){
                    TextNum2 = repeatItem(TextNum1, TextNum2);
                }
            }

            $(items.target1).animate({"top":"-" + animateCount * prizeHeight}, 1500, "linear", function () {
                $(this).css("top",0).animate({"top":"-" + rewardItems[TextNum1] * prizeHeight}, 1000,"linear");
            });

            $(items.target2).animate({"top":"-" + animateCount * prizeHeight}, 1000, "linear", function () {
                $(this).css("top",0).animate({"top":"-" + rewardItems[TextNum2] * prizeHeight}, 1400,"linear");
            });

            $(items.target3).animate({"top":"-" + animateCount * prizeHeight}, 1200, "linear", function () {
                $(this).css("top",0).animate({"top":"-" + rewardItems[TextNum3] * prizeHeight}, 1800,"linear", function(){
                    if(callback){
                        callback();
                    }
                });
            });
        }

        YQ.voxLogs({module : "m_2ekTvaNe", op : "o_hz4gzTig", s1: "${(currentUser.userType)!0}"});
    })();
</script>
</@temp.page>