<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="开学大礼包" header="show">
<@app.css href="public/skin/project/termbegin201609/css/termbegin.css" />
<@sugar.capsule js=[ "alert","voxLogs"]/>
<#assign beanText=currentTeacherDetail.isJuniorTeacher()?string("学豆","园丁豆") />
<#assign beanUrl=currentTeacherDetail.isJuniorTeacher()?string("","_1") />
<#--打点-->
<#assign

logLoad     = "o_hfEG00Sp"  <#--页面加载-->
logNotSet   = currentTeacherDetail.isJuniorTeacher()?string("o_XW7sDzGr","o_kscVhYUz")  <#--确认任教班级弹窗_教的班级不变按钮_被点击-->
logSet      = currentTeacherDetail.isJuniorTeacher()?string("o_IX5rXICo","o_IeEkPhy7")  <#--确认任教班级弹窗_有变动去调整按钮_被点击-->
logHomework = currentTeacherDetail.isJuniorTeacher()?string("o_gP2WE60S","o_oNtPCvuC")  <#--去布置按钮被点击-->
logWakeUp   = "o_IPDKxbZC" <#--去唤醒按钮被点击-->
/>
<div class="main-wrapper">
    <div class="main">
    <#if currentTeacherDetail.isJuniorTeacher()>
        <div class="block block5">
            <div class="tip tip2" style="margin-top:0;">
                <a href="${(ProductConfig.getJuniorSchoolUrl())!}/teacher/assign/index" class="btn btn2 inner-right js-homework">
                    去布置
                </a>
                每天布置作业，每天可获得 <var>1</var> 次抽奖机会
            </div>
        </div>
    <#else>
        <div class="block block1">
            <div class="tip">
                <a href="/teacher/homework/batchassignhomework.vpage" target="_blank" class="btn btn2 inner-right js-homework">
                    去布置
                </a>
                每天布置作业，每天可获得 <var>1</var> 次抽奖机会<br />
                （布置过寒假作业的老师可获得双倍抽奖机会）
            </div>
            <div class="tip tip2">
                <a href="/teacher/invite/activateteacher.vpage?type=HXUSER" target="_blank" class="btn btn2 inner-right js-wakeup">
                    去唤醒
                </a>
                唤醒 1 位老师，获得 <var>5</var> 次抽奖机会
            </div>
            <div class="wake-wrapper">
                <div class="t-head">
                    <div>
                        唤醒中 ${activatingTeachers?size} 人
                    </div>
                    <div>
                        已唤醒 ${sucessTeachers?size} 人
                    </div>
                </div>
                <ul class="wake waking">
                    <#if (activatingTeachers?size gt 0)!false>
                        <#list activatingTeachers as ats>
                            <li class="item">${((ats.userName)?has_content)?string("${ats.userName}", "---")} ${ats.userId}</li>
                        </#list>
                    <#else>
                        <li>暂时没有老师</li>
                        <li>快去唤醒吧！</li>
                    </#if>
                </ul>
                <ul class="wake waked">
                    <#if (sucessTeachers?size gt 0)!false>
                        <#list sucessTeachers as ats>
                            <li class="item">${((ats.userName)?has_content)?string("${ats.userName}", "---")} ${ats.userId}</li>
                        </#list>
                    <#else>
                        <li>暂时没有老师</li>
                        <li>快去唤醒吧！</li>
                    </#if>
                </ul>
            </div>
        </div>
    </#if>

        <div class="block block2">
            <div class="tip tip3">
                共获得<var>${(myHistory?size + freeChance)!0}</var>次抽奖机会，已抽取<var id="used-chance">${(myHistory?size)!0}</var>次，剩余<var id="free-chance">${freeChance!0}</var>次机会！
            </div>
            <div class="prize-wrapper" id="lotteryItems">
                <div class="prize item" data-type="7" data-index="0">
                    <img src="<@app.link href="public/skin/project/termbegin201609/images/prize1${beanUrl}.png"/>" />
                </div>
                <div class="prize" data-type="1" data-index="1">
                    <img src="<@app.link href="public/skin/project/termbegin201609/images/prize2.png"/>" />
                </div>
                <div class="prize" data-type="6" data-index="2">
                    <img src="<@app.link href="public/skin/project/termbegin201609/images/prize3${beanUrl}.png"/>" />
                </div>
                <div class="prize" data-type="2" data-index="7">
                    <img src="<@app.link href="public/skin/project/termbegin201609/images/prize7.png"/>" />
                </div>
                <div id="lotterySubmit" class="prize js-go" style="cursor:pointer">
                    <img src="<@app.link href="public/skin/project/termbegin201609/images/trigger.png"/>" />
                </div>
                <div class="prize" data-type="3" data-index="3">
                    <img src="<@app.link href="public/skin/project/termbegin201609/images/prize8.png"/>" />
                </div>
                <div class="prize" data-type="5" data-index="6">
                    <img src="<@app.link href="public/skin/project/termbegin201609/images/prize6${beanUrl}.png"/>" />
                </div>
                <div class="prize the" data-type="8" data-index="5">
                    <img src="<@app.link href="public/skin/project/termbegin201609/images/prize5.png"/>" />
                </div>
                <div class="prize" data-type="4" data-index="4">
                    <img src="<@app.link href="public/skin/project/termbegin201609/images/prize4.png"/>" />
                </div>
            </div>
        </div>
        <div class="block block3">
            <table class="t-head">
                <tr>
                    <th>抽奖次数</th>
                    <th>抽奖时间</th>
                    <th>抽奖奖励</th>
                </tr>
            </table>
            <div class="t-body">
                <table>
                    <#if (myHistory?size gt 0)!false>
                        <#list myHistory as his>
                            <tr>
                                <td style="width: 120px">第${myHistory?size - his_index}次</td>
                                <td>${(his.lotteryDate)!}</td>
                                <td style="width: 120px">${(his.awardName)!}</td>
                            </tr>
                        </#list>
                    <#else>
                        <tr>
                            <td colspan="3">
                                暂无数据
                            </td>
                        </tr>
                    </#if>
                </table>
            </div>
        </div>
        <div class="block block4">
            <div class="dynamic">
            <#if (campaignLotteryResultsBig?size gt 0)!false>
                <#list campaignLotteryResultsBig as lot>
                    <#--<#if lot_index lt 4>-->
                        <p class="tr">
                            <span class="td item1">${(lot.lotteryDate)!}</span>
                            <span class="td item2">${(lot.userName)!} - ${(lot.schoolName)!}</span>
                            <span class="td item3">${(lot.awardName)!}</span>
                        </p>
                    <#--</#if>-->
                </#list>
            <#else>
                <p class="tr" style="text-align: center">暂无数据</p>
            </#if>
            </div>
        </div>
        <div class="footer">
            ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
        </div>
    </div>
</div>
<script type="text/html" id="T:结果提示">
    <%if(winningId < 8){%>
    <div class="t-winning-pop">
        <%if(winningId <= 4){%>
        <div class="winning-title">
            恭喜您获得 <var class="award-name"><%==(awardName)%></var>
        </div>
        <div class="notice">
            实物奖品3月30日之后统一寄送，<br />
            请注意查收电话通知哦~
        </div>
        <div class="btn-wrapper" style="margin-top:30px;">
            <a href="javascript:void(0);" class="btn btn2 sure-btn center" onclick="$.prompt.close();">知道了</a>
        </div>
        <%}%>

        <%if(winningId > 4 && winningId <= 7){%>
        <div class="winning-title">
            恭喜您获得 <var class="award-name"><%==(awardName)%></var> ${beanText}！
        </div>
        <div class="notice">
            可在“我的${beanText}”历史中查看记录
        </div>
        <div class="btn-wrapper" style="margin-top:30px;">
            <a href="javascript:void(0);" class="btn btn2 sure-btn center" onclick="$.prompt.close();">确定</a>
        </div>
        <%}%>
    </div>
    <%}else{%>
    <div class="t-winning-pop">
        <div class="winning-title">
            再接再厉！
        </div>
        <div class="tip">
            呀！很遗憾，奖品溜走了！
        </div>
        <div class="btn-wrapper">
            <a href="javascript:void(0);" class="btn btn2 sure-btn center" onclick="$.prompt.close();">知道了</a>
        </div>
    </div>
    <%}%>
</script>
<script type="text/html" id="T:系统提示">
    <div class="t-winning-pop">
        <div class="winning-title">
            <%=title%>
        </div>
        <div class="tip">
            <%=tip%>
        </div>
        <div class="btn-wrapper">
            <a href="javascript:void(0);" class="btn btn2 sure-btn center" onclick="$.prompt.close();">知道了</a>
        </div>
    </div>
</script>
<script type="text/html" id="T:调整班级">
    <div class="t-winning-pop">
        <div class="winning-title">
            新学期任教班级是否变动？
        </div>
        <div class="btn-wrapper">
            <a href="javascript:void(0);" class="btn btn2 sure-btn center" onclick="$.prompt.close();" style="width:6.4rem;-webkit-transform:translateX(-106%);-moz-transform:translateX(-106%);-ms-transform:translateX(-106%);-o-transform:translateX(-106%);transform:translateX(-106%)">教的班级不变</a>
            <a href="javascript:void(0);" class="btn btn2 sure-btn center clazz" onclick="$.prompt.close();" style="width:7rem;-webkit-transform:translateX(6%);-moz-transform:translateX(6%);-ms-transform:translateX(6%);-o-transform:translateX(6%);transform:translateX(6%)">有变动，去调整</a>
        </div>
    </div>
</script>
<script type="text/html" id="T:布置作业">
    <div class="t-winning-pop">
        <div class="winning-title">
            抽奖活动9月1日开启，<br />您可以先去布置作业哦！
        </div>
        <div class="btn-wrapper" style="margin-top:20px;">
            <#if currentTeacherDetail.isJuniorTeacher()>
                <a href="${(ProductConfig.getJuniorSchoolUrl())!}/teacher/assign/index" class="btn btn2 sure-btn center">去布置作业</a>
            <#else>
                <a href="/teacher/homework/batchassignhomework.vpage" class="btn btn2 sure-btn center">去布置作业</a>
            </#if>
        </div>
    </div>
</script>
<script type="text/javascript">
    (function(){
        var getUrlParam=function(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
            var r = window.location.search.substr(1).match(reg);  //匹配目标参数
            if (r != null) return unescape(r[2]); return null; //返回参数值
        };

        <#if !currentTeacherDetail.isJuniorTeacher()>
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_rQjVWe1G',
                op : "${logLoad}",
                s0 : getUrlParam("s0")
            });
            //打点
            $(document).on("click",".js-homework",function(){
                YQ.voxLogs({
                    database: 'web_teacher_logs',
                    module: 'm_rQjVWe1G',
                    op : "${logHomework}"
                });
            });
            $(document).on("click",".js-wakeup",function(){
                YQ.voxLogs({
                    database: 'web_teacher_logs',
                    module: 'm_rQjVWe1G',
                    op : "${logWakeUp}"
                });
            });
        </#if>

        //领取奖励
        $(document).on("click", ".v-receivevhrewardBtn", function(){
            <#if (.now gt "2017-03-11 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
                if(true){
                    $.prompt( template("T:系统提示",{title:"活动已经结束！"}) ,{
                        prefix : "null-popup",
                        buttons : { },
                        classes : {
                            fade: 'jqifade',
                            close: 'w-hide'
                        }
                    });
                    return false;
                }
            </#if>
        });
    })();
    var freeChance = $("#free-chance"), usedChance= $("#used-chance");
    (function(){
        /*抽奖功能*/
        var currentIndex = 5, winningId = 8, speed = 200, rotaNumber = 0, lotteryAnimate, recordFreeChance = ${freeChance!0};

        $("#lotterySubmit").on({
            click : function(){
                <#if currentTeacherDetail.authenticationState != 1>
                    $.prompt( template("T:系统提示",{title:"认证老师才能参与活动哦！",tip:"快去达成认证吧~"}) ,{
                        prefix : "null-popup",
                        buttons : { },
                        classes : {
                            fade: 'jqifade',
                            close: 'w-hide'
                        }
                    });
                    return false;
                </#if>

                if( recordFreeChance < 1 ){
                    $.prompt( template("T:系统提示",{title:"很遗憾，您当前剩余抽奖次数为0次！",tip:"活动期间每天布置作业<#if (currentTeacherDetail.isPrimarySchool())!false>、唤醒老师均</#if>可获得抽奖机会！"}) ,{
                        prefix : "null-popup",
                        buttons : { },
                        classes : {
                            fade: 'jqifade',
                            close: 'w-hide'
                        }
                    });
                    return false;
                }

                var $this = $(this);
                <#if (.now gt "2017-03-25 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
                    if(true){
                        $.prompt( template("T:系统提示",{title:"抽奖活动已经结束！"}) ,{
                            prefix : "null-popup",
                            buttons : { },
                            classes : {
                                fade: 'jqifade',
                                close: 'w-hide'
                            }
                        });
                        return false;
                    }
                </#if>
                <#if (.now lt "2017-02-20 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && !ftlmacro.devTestStagingSwitch)!false>
                    $.prompt( template("T:布置作业",{}) ,{
                        prefix : "null-popup",
                        buttons : { },
                        classes : {
                            fade: 'jqifade',
                            close: 'w-hide'
                        }
                    });
                <#else>
                    if($this.hasClass("dis")){
                        return false;
                    }

                    $this.addClass("dis");

                    $.post("/activity/dolottery.vpage", {}, function(data){
                        if(data.success){
                            var $awardName = '';
                            if(data.win){
                                winningId = data.lottery.awardId;
                                $awardName = data.lottery.awardName
                            }else{
                                winningId = 8;
                            }

                            lotteryAnimate = setInterval(function(){
                                startRotational(function(){
                                    //更新剩余抽奖次数
                                    recordFreeChance=parseInt(freeChance.html());
                                    if(recordFreeChance>0){
                                        --recordFreeChance;
                                    }
                                    var usedNum=${(myHistory?size + freeChance)!0}-recordFreeChance;
                                    freeChance.html(recordFreeChance);
                                    usedChance.html(usedNum);

                                    if(winningId > 4 && winningId <= 7){
                                        $awardName=parseInt($awardName);
                                    }

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
                            $.prompt( template("T:系统提示",{title:data.info}) ,{
                                prefix : "null-popup",
                                buttons : { },
                                classes : {
                                    fade: 'jqifade',
                                    close: 'w-hide'
                                }
                            });
                            $this.removeClass("dis");
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
            var $lotteryBox = null;
            $("#lotteryItems").children().each(function(){
                if($(this).data().index== currentIndex){
                    $lotteryBox=$(this);
                }
            });
            currentIndex = startLottery(currentIndex, 7);
            $lotteryBox.addClass("the").siblings().removeClass("the");

            //最后转动
            if(rotaNumber >= 5){
                clearInterval(lotteryAnimate);

                if(currentIndex == 0){
                    rotaNumber += 1;
                }

                if(rotaNumber >= 6 && (winningId >= 8 || winningId < 1)){
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