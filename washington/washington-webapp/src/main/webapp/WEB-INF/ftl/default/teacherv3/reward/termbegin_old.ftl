<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="开学大礼包" header="show">
<@app.css href="public/skin/project/termbegin_h5/skin.css" />
<div class="t-schoolSpree-box">

    <div class="banner">
        <p>活动规则</p>
        <p>参与对象：2016.2.10日之前注册的老师</p>
        <p>活动时间：2016.2.22-2016.3.18</p>
    </div>
    <div class="container">
    <#if (currentTeacherDetail.subject != "CHINESE")!false>
        <div class="column-1">
            <div class="c-title">寒假作业，完结礼包</div>
            <div class="c-info" style="position: relative; z-index: 2;">
                <div class="c-btn">
                    <#assign count_30 = 30 count_60 = 60 count_90 = 90/>
                    <#if ftlmacro.devTestStagingSwitch>
                        <#assign count_30 = 1 count_60 = 2 count_90 = 3/>
                    </#if>
                    <#if ((.now gt "2016-03-07 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") || ftlmacro.devTestStagingSwitch) && vacationCount gte count_30)!false>
                        <a href="javascript:void(0);" class="open-btn receive-btn v-receivevhrewardBtn">点击领取</a>
                    <#else>
                        <a href="javascript:void(0);" class="open-btn">3月6日开启</a>
                    </#if>
                </div>
                <div class="c-txt">您目前有 <span>${(vacationCount)!0}</span> 名学生完成寒假作业，<#if (vacationCount gte count_90)!false>可领取<span>150</span><#elseif (vacationCount gte count_60)!false>可领取<span>100</span><#elseif (vacationCount gte count_30)!false>可领取<span>50</span><#else>不足30人无法领取</#if>园丁豆奖励</div>
            </div>
        </div>
    </#if>
        <div class="column-2">
            <div class="luck-draw">
                <div class="l-up">
                    <div class="l-head">开学大礼包，实物抽大奖</div>
                    <div class="l-tip" style="text-align: center;"> 共送出12台智能电视，24部红米手机</div>
                    <div class="l-content">
                        <div class="l-btn">
                            <a href="/teacher/homework/batchassignhomework.vpage" target="_blank" class="arrange-btn">去布置</a>
                        </div>
                        <p>每天布置作业，每天可获得 <span>2</span> 次抽奖机会</p>
                    </div>
                    <div class="l-content">
                        <div class="l-btn">
                            <a href="/teacher/invite/activateteacher.vpage?type=HXUSER" target="_blank" class="wakeUp-btn">去唤醒</a>
                        </div>
                        <p>唤醒1位老师，获得 <span>5</span> 次抽奖机会</p>
                    </div>
                </div>
                <div class="l-down">
                    <div class="l-left">
                        <div class="h1">唤醒中 <b>${activatingTeachers?size}</b> 人</div>
                        <ul>
                            <#if (activatingTeachers?size gt 0)!false>
                                <#list activatingTeachers as ats>
                                    <li><span class="name">${((ats.userName)?has_content)?string("${ats.userName}", "---")}</span> <span class="number">${ats.userId}</span></li>
                                </#list>
                            <#else>
                                <li><p style="margin-top: 30px; font-size: 14px;">暂时没有老师，<br>快去唤醒吧！</p></li>
                            </#if>
                        </ul>
                    </div>

                    <div class="l-left">
                        <div class="h1">已唤醒 <b>${sucessTeachers?size}</b> 人</div>
                        <ul>
                            <#if (sucessTeachers?size gt 0)!false>
                                <#list sucessTeachers as ats>
                                    <li><span class="name">${((ats.userName)?has_content)?string("${ats.userName}", "---")}</span> <span class="number">${ats.userId}</span></li>
                                </#list>
                            <#else>
                                <li><p style="margin-top: 30px; font-size: 14px;">暂时没有老师，<br>快去唤醒吧！</p></li>
                            </#if>
                        </ul>
                    </div>
                </div>
            </div>
            <div class="side-lottery">
                <div class="lottery-container">
                    <div class="btn-luck">
                        <a href="javascript:void(0);" class="turntable-btn" id="lotterySubmit"></a>
                    </div>
                    <ul id="lotteryItems">
                        <li class="turntable-unit-1" data-type="4">
                            <div class="winning-inner"><div class="winning-img-4"></div></div>
                        </li>
                        <li class="turntable-unit-2" data-type="1">
                            <div class="winning-inner"><div class="winning-img-1"></div></div>
                        </li>
                        <li class="turntable-unit-3" data-type="5">
                            <div class="winning-inner"><div class="winning-img-5"></div></div>
                        </li>
                        <li class="turntable-unit-4" data-type="2">
                            <div class="winning-inner"><div class="winning-img-2"></div></div>
                        </li>
                        <li class="turntable-unit-5" data-type="6">
                            <div class="winning-inner"><div class="winning-img-6"></div></div>
                        </li>
                        <li class="turntable-unit-6 active" data-type="7">
                            <div class="winning-inner"><div class="winning-img-7"></div></div>
                        </li>
                        <li class="turntable-unit-7"  data-type="3">
                            <div class="winning-inner"><div class="winning-img-3"></div></div>
                        </li>
                        <li class="turntable-unit-8" data-type="2">
                            <div class="winning-inner"><div class="winning-img-2"></div></div>
                        </li>
                    </ul>
                </div>
                <div class="noticed">注：实物奖品于3月18日之后统一寄送，请注意查收电话通知</div>
            </div>
            <div style="clear: both; width: 100%;"></div>
        </div>
        <div class="column-3">
            <div class="lottery-table">
                <div class="record">抽奖记录：共获得${(myHistory?size + freeChance)!0}次抽奖机会,已抽取${(myHistory?size)!0}次  剩余<span>${freeChance!0}</span>次机会</div>
                <table>
                    <thead>
                    <tr>
                        <td style="width: 120px">抽奖次数</td>
                        <td>抽奖时间</td>
                        <td style="width: 120px">抽奖奖励</td>
                    </tr>
                    </thead>
                </table>
                <div style="overflow: hidden; overflow-y: auto; height: 195px; position: relative;border:2px solid #4f9f10; background-color: #fff; width: 472px;">
                    <table style="border: none;">
                        <tbody>
                            <#if (myHistory?size gt 0)!false>
                                <#list myHistory as his>
                                <tr>
                                    <td style="width: 120px">第${his_index + 1}次</td>
                                    <td class="center">${(his.lotteryDate)!}</td>
                                    <td style="width: 120px">${(his.awardName)!}</td>
                                </tr>
                                </#list>
                            <#else>
                            <tr>
                                <td colspan="3">
                                    <div style="text-align: center; padding: 30px;">暂无数据</div>
                                </td>
                            </tr>
                            </#if>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="award-dynamic">
                <div class="top"></div>
                <div class="mid">
                    <p class="title"><span style="float: right;">每日大奖将在第二天公布</span>大奖动态</p>
                    <ul style="height: 206px;">
                        <#if (campaignLotteryResultsBig?size gt 0)!false>
                            <#list campaignLotteryResultsBig as lot>
                                <#if lot_index lt 4>
                                    <li class="list">
                                        <span class="time" style="width: 110px;">${(lot.lotteryDate)!}</span><span class="area">${(lot.userName)!} - ${(lot.schoolName)!}</span><span class="prize" style=" width: 100px;">${(lot.awardName)!}</span>
                                    </li>
                                </#if>
                            </#list>
                        <#else>
                            <li class="list">
                                <div style="text-align: center; padding: 30px;">暂无数据</div>
                            </li>
                        </#if>
                    </ul>
                </div>
                <div class="bot"></div>
            </div>
        </div>
    </div>
</div>

<script type="text/html" id="T:结果提示">
<%if(winningId < 7){%>
    <div class="t-winning-pop">
        <%if(winningId <= 2){%>
            <div class="winning-title">
                恭喜您获得 <%==(awardName)%>
            </div>
            <div class="notice">
                实物奖品在3月18日之后统一寄送
                请注意查收电话通知
            </div>
        <%}%>

        <%if(winningId > 2 && winningId <= 6){%>
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
        //领取奖励
        var hasAdjust = ${((!hasAdjustClazz)!false)?string};
        $(document).on("click", ".v-receivevhrewardBtn", function(){
            <#if (.now gt "2016-03-21 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
                if(true){
                    $17.alert("活动已经结束！");
                    return false;
                }
            </#if>

            if(hasAdjust){
                hasAdjustPopup();
                return false;
            }

            $.post("/teacher/reward/receivevacationreward.vpage", { amount : ${(vacationCount)!0} }, function(data){
                if(data.success){
                    $17.alert("领取成功！");
                }else{
                    $17.alert(data.info);
                }
            });
        });

        function hasAdjustPopup(){
            $.prompt("<div class='w-ag-center' style='font-size: 18px;color: #4e5656;'>布置作业前，请先 <span style='color: #f66741;'>确认任教班级</span>！<div style='color: #4e5656; font-size: 16px; padding: 15px; border-radius: 3px; border: 1px solid #dfdfdf; margin-top: 20px;'>如任教班级不正确，请去“我的班级”里调整</div></div>", {
                focus : 1,
                title: "系统提示",
                buttons: { "教的班级不变": false , "查看并调整": true },
                position: {width: 500},
                close : function(){
                    $.get("/activity/recordadjust.vpage", {}, function(data){
                        $.prompt.close();
                        $17.tongji("新学生调整班级", "保持不变");
                        hasAdjust = false;
                    });
                },
                classes : {
                    close: 'w-hide'
                },
                submit : function(e, v){
                    if(v){
                        $.get("/activity/recordadjust.vpage", {}, function(data){
                            $.prompt.close();
                            $17.tongji("新学生调整班级", "去调整");
                            location.href = "${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage?ref=editClazz&refType=project";
                        });
                    }
                }
            });
        }
    })();

    (function(){
        /*抽奖功能*/
        var currentIndex = 5, winningId = 7, speed = 200, rotaNumber = 0, lotteryAnimate;

        $("#lotterySubmit").on({
            click : function(){
                var $this = $(this);
                <#if (.now gt "2016-03-22 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
                    if(true){
                        $17.alert("抽奖活动已经结束！");
                        return false;
                    }
                </#if>
                <#if (.now lt "2016-02-22 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && !ftlmacro.devTestStagingSwitch)!false>
                    $.prompt("<div style='font-size: 16px; text-align: center;'>抽奖活动2月22日开启，您可以先去布置作业哦！</div>", {
                        title: "系统提示",
                        buttons: {"去布置作业": true },
                        position: {width: 500},
                        submit : function(e, v){
                            if(v){
                                location.href = "/teacher/homework/batchassignhomework.vpage";
                            }
                        }
                    });
                <#else>
                    if($this.hasClass("dis")){
                        return false;
                    }

                    $this.addClass("dis");

                    $.post("/teacher/reward/dolottery.vpage", {}, function(data){
                        if(data.success && data.win){
//                        if(true){
                            var $awardName;
                            if(data.success && data.lottery.awardId){
                                winningId = data.lottery.awardId;
                                $awardName = data.lottery.awardName
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
                            $17.alert(data.info, function(){
                                $this.removeClass("dis");
                            });
                        }
                    });
                </#if>
            }
        });

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
            currentIndex = startLottery(currentIndex, 7);
            $lotteryBox.addClass("active").siblings().removeClass("active");

            //最后转动
            if(rotaNumber >= 5){
                clearInterval(lotteryAnimate);

                if(currentIndex == 0){
                    rotaNumber += 1;
                }

                if(rotaNumber >= 6 && (winningId > 7 || winningId <= 0)){
                    winningId = 7;
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