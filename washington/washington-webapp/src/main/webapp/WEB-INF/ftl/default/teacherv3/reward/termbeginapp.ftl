<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <!--页面窗口自动调整到设备宽度，并禁止用户缩放页面-->
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
    <!--关闭电话号码识别：-->
    <meta name="format-detection" content="telephone=no" />
    <!--关闭邮箱地址识别：-->
    <meta name="format-detection" content="email=no" />
    <!-- iOS 的 safari 顶端状态条的样式 可选default、black、black-translucent-->
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>开学大礼包</title>
    <@app.css href="public/skin/project/termbegin201609/css/termbegin_m.css" />
    <@sugar.capsule js=["jquery", "core", "alert", "template","voxLogs"] css=["plugin.alert", "new_teacher.widget", "specialskin"] />
</head>
<body>
<#assign beanText=currentTeacherDetail.isJuniorTeacher()?string("学豆","园丁豆") />
<#assign beanUrl=currentTeacherDetail.isJuniorTeacher()?string("","_1") />

<div class="app-wrapper">
    <div style="height:12.5rem;position:relative;">
        <img class="bg center" style="max-width:568px;left:50%;" width="100%" src="<@app.link href='public/skin/project/termbegin201609/images/termappbg.jpg'/>" />
    </div>
    <div class="main">
        <div class="block block1_2" style="height:7.3rem;">
            <img class="bg" width="100%" src="<@app.link href='public/skin/project/termbegin201609/images/block1_2.png'/>" />
            <div class="info abs">
                <a href="javascript:void(0);" class="btn btn2 middle go-to-homework js-homework">
                    去布置
                </a>
                <span class="bold">每天布置作业，每天可得<var> 1 </var>次抽奖机会</span>（布置过寒假作业的老师可获得双倍抽奖机会）
            </div>
        </div>
        <#if !currentTeacherDetail.isJuniorTeacher()>
            <div class="block block1_3" style="height:6.6rem;">
                <img class="bg" width="100%" src="<@app.link href='public/skin/project/termbegin201609/images/block1_3.png'/>" />
                <div class="info abs">
                    <span class="bold">活动期间，在电脑上成功唤醒一位老师，获得<var> 5 </var>次抽奖机会</span>
                </div>
            </div>
        </#if>

        <div class="block block2">
            <div class="tip tip3">
                您当前剩余抽奖机会<var id="free-chance"> ${freeChance!0} </var>次
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
            <div class="tip tip3">
                实物奖品将于<var>3月30号</var>后统一寄出
            </div>
            <div class="table">
                <table class="t-head">
                    <tr>
                        <th>抽奖时间</th>
                        <th>抽奖奖励</th>
                    </tr>
                </table>
                <div class="t-body">
                    <table>
                        <#if (myHistory?size gt 0)!false>
                            <#list myHistory as his>
                                <tr>
                                    <td>${(his.lotteryDate)!}</td>
                                    <td style="width: 120px">${(his.awardName)!}</td>
                                </tr>
                            </#list>
                        <#else>
                            <tr>
                                <td colspan="2">
                                    暂无数据
                                </td>
                            </tr>
                        </#if>
                    </table>
                </div>
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
    </div>
    <div>
        <img width="100%" src="<@app.link href='public/skin/project/termbegin201609/images/bg3.jpg'/>" />
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
        <div class="btn-wrapper">
            <a href="javascript:void(0);" class="btn btn2 sure-btn center" onclick="$.prompt.close();">知道了</a>
        </div>
        <%}%>

        <%if(winningId > 4 && winningId <= 7){%>
        <div class="winning-title">
            中奖了！
        </div>
        <div class="notice">
            恭喜您获得 <var class="award-name"><%==(awardName)%></var> ${beanText}！
        </div>
        <div class="btn-wrapper">
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
            <a href="javascript:void(0);" class="btn btn2 sure-btn center  js-not-change" onclick="$.prompt.close();" style="width:6.4rem;-webkit-transform:translateX(-106%);-moz-transform:translateX(-106%);-ms-transform:translateX(-106%);-o-transform:translateX(-106%);transform:translateX(-106%)">教的班级不变</a>
            <a href="javascript:void(0);" class="btn btn2 sure-btn center clazz js-change" onclick="$.prompt.close();" style="width:7rem;-webkit-transform:translateX(6%);-moz-transform:translateX(6%);-ms-transform:translateX(6%);-o-transform:translateX(6%);transform:translateX(6%)">有变动，去调整</a>
        </div>
    </div>
</script>
<script type="text/html" id="T:布置作业">
    <div class="t-winning-pop">
        <div class="winning-title">
            抽奖活动9月1日开启，您可以先去布置作业哦！
        </div>
        <div class="btn-wrapper">
            <a href="javascript:void(0);" class="btn btn2 sure-btn center homework" onclick="$.prompt.close();">去布置作业</a>
        </div>
    </div>
</script>
<script type="text/html" id="T:下载App">
    <div class="t-winning-pop" style="width:90%;background-size:100% 90%;">
        <div style="text-align: center;color:#fff;position:relative;">
            <a href="javascript:void(0)" onclick="$.prompt.close()" class="close-btn"></a>
            <img style="width:4rem;margin-top:0.85rem;" src="<@app.link href='public/skin/project/termbegin201609/images/qrcode.png'/>">
            <p style="font-size: 0.85rem;line-height:1.5rem;">手机扫一扫下载手机App</p>
            <p style="font-size: 0.55rem;line-height:0.6rem;">（领取方式：从首页进入活动页领取）</p>
        </div>
    </div>
</script>
<script>
    var getUrlParam=function(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
        var r = window.location.search.substr(1).match(reg);  //匹配目标参数
        if (r != null) return unescape(r[2]); return null; //返回参数值
    };

    var hasAdjust = ${((!hasAdjustClazz)!false)?string};
    function hasAdjustPopup(){
        $.prompt(template("T:调整班级",{}) ,{
            prefix : "null-popup",
            buttons : { },
            classes : {
                fade: 'jqifade',
                close: 'w-hide'
            }
        });
    }

    //打点
    <#if s == 'app'>
        YQ.voxLogs({
            database: 'web_teacher_logs',
            module: 'm_MdBUbSrw',
            op : "o_7z9LqQrM",
            s0 : getUrlParam("s0")
        });
        $(document).on("click",".js-not-change",function(){
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_MdBUbSrw',
                op : "o_KXub9arS"
            });
        });
        $(document).on("click",".js-change",function(){
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_MdBUbSrw',
                op : "o_NRBcU0oD"
            });
        });
    <#else>
        YQ.voxLogs({
            database: 'web_teacher_logs',
            module: 'm_g8KVRSRA',
            op : "o_Xi0qAzaI"
        });
        $(document).on("click",".js-change",function(){
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_g8KVRSRA',
                op : "o_Pl675s4c"
            });
        });
        $(document).on("click",".js-not-change",function(){
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_g8KVRSRA',
                op : "o_lyOHDlkI"
            });
        });
        $(document).on("click",".js-homework",function(){
            YQ.voxLogs({
                database: 'web_teacher_logs',
                module: 'm_g8KVRSRA',
                op : "o_4o2uZvyF"
            });
        });
    </#if>
    $(document).on("click",".js-not-change",function(){
        $.get("/activity/recordadjust.vpage", {}, function(){
            hasAdjust = false;
        });
    });
    $(document).on("click",".js-change",function(){
        $.get("/activity/recordadjust.vpage", {}, function(data){
            hasAdjust = false;
        });
    });

    var linkToByEnv = function (url) {
        if(url){
            var linkHeader = '';
            <#if ProductDevelopment.isTestEnv()>
                linkHeader = "//wechat.test.17zuoye.net/";
            <#elseif ProductDevelopment.isStagingEnv()>
                linkHeader = "//wechat.staging.17zuoye.net/";
            <#elseif ProductDevelopment.isProductionEnv()>
                linkHeader = "//wechat.17zuoye.com/";
            </#if>
            location.href = linkHeader+url;
        }
    };

    $(document).on("click",".homework",function(){
        <#if s == 'app'>
            try {
                if(window['external'] && window.external['goArrangeHW']){
                    window.external.goArrangeHW();
                }else{
                    alert('请求失败');
                }
            }catch(e) {
                alert(JSON.stringify(e));
            }
        <#else>
            linkToByEnv('teacher/homework/index.vpage');
        </#if>
    });
    $(document).on("click",".clazz",function(){
        <#if s == "app">
            try {
                if(window['external'] && window.external['goClazzManage']){
                    window.external.goClazzManage();
                }else{
                    alert('请求失败');
                }
            }catch(e) {
                alert(JSON.stringify(e));
            }
        <#else>
            linkToByEnv('teacher/clazzmanage/list.vpage');
        </#if>
    });
    (function(){
        //去布置
        $(document).on("click",".go-to-homework",function(){
            <#if s == "app">
                try {
                    if(hasAdjust){
                        hasAdjustPopup();
                        return false;
                    }else{
                        if(window['external'] && window.external['goArrangeHW']){
                            window.external.goArrangeHW();
                        }else{
                            alert('请求失败');
                        }
                    }
                }catch(e) {
                    alert(JSON.stringify(e));
                }
            <#else>
                if(hasAdjust){
                    $(document).one("click",".js-not-change",function(){
                        linkToByEnv('teacher/homework/index.vpage');
                    });
                    hasAdjustPopup();
                    return false;
                }else {
                    linkToByEnv('teacher/homework/index.vpage');
                }
            </#if>
        });
    })();

    var freeChance = $("#free-chance");
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
            <#if s != "app">
                if(hasAdjust){
                    hasAdjustPopup();
                    return false;
                }
            </#if>

                if(recordFreeChance < 1){
                    $.prompt( template("T:系统提示",{title:"很遗憾，剩余抽奖次数为0！",tip:"活动期间每天布置作业<#if (currentTeacherDetail.isPrimarySchool())!false>、唤醒老师均</#if>可获得抽奖机会！"}) ,{
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
                if(${((.now gt "2017-03-25 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false)?string}){
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
                        var $awardName='';
                        if(data.win && data.lottery.awardId){
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
                                freeChance.html(recordFreeChance);

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
</body>
</html>