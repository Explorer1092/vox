<#import "../../reward/layout/layout.ftl" as temp />
<@temp.page index="lottery">
    <@sugar.capsule js=["jquery.flashswf"] css=["project.activity"] />
<div class="head">
    <div class="inner">
        <div class="code-area">
            <dl>
                <dt id="weChatBackgroundAreaCode"></dt>
                <dd>
                    微信扫一扫，手机也能抽大奖！<br/>
                    微信布置作业每天免费抽奖5次
                </dd>
            </dl>
            <div style="color: #65b2f3; font-size: 14px; line-height: 22px;">
                <#if (currentUser.fetchCertificationState() == "SUCCESS")!false>
                    <#if count?? && count gt 0>
                        <#--今天在电脑上还可以免费抽奖<strong style="color: #ff5955; font-size: 24px;">${count!0}</strong>次<br/>-->
                    </#if>
                <#else>
                    认证老师才能参与本次活动 <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage" class="w-blue">现在去认证>></a><br/>
                </#if>
                <span>温馨提示:  每天在电脑上最多抽奖5次，超过5次请用微信扫描上方二维码，到微信上抽取</span>
            </div>
        </div>
        <div class="game" id="movie"></div>
    </div>
</div>
<div class="angle_box">
    <div class="inner"></div>
</div>
<div class="content_box">
    <div class="inner">
        <div class="con-left">
            <div class="award">
                <h3>奖项设置</h3>
                <div class="table">
                    <table>
                        <thead>
                        <tr>
                            <td>一等奖</td>
                            <td>二等奖</td>
                            <td>三等奖</td>
                            <td>四等奖</td>
                            <td>五等奖</td>
                            <td>六等奖</td>
                            <td>七等奖</td>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>1000园丁豆</td>
                            <td>500园丁豆</td>
                            <td>100园丁豆</td>
                            <td>50园丁豆</td>
                            <td>10园丁豆</td>
                            <td>5园丁豆</td>
                            <td>1园丁豆</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="tip">
                <h3>抽奖规则</h3>
                <ul>
                    <li>1、抽奖资格：认证老师才可以参与抽奖；</li>
                    <li>2、抽奖次数：每天可在电脑上抽奖5次；</li>
                    <li>3、活动时间：2016年7月1日-2016年8月31日；</li>
                    <li>4、奖项设置：寒暑假期间，取消平板电脑和手机，所有奖品更换为园丁豆；</li>
                    <li>5、发放规则：园丁豆即刻到账；</li>
                    <li>6、中奖分享：获奖的老师可以将获奖的照片和心情分享到教师论坛。<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;本活动最终解释权归一起作业网所有</li>
                </ul>
            </div>
        <#--<h3 style="margin: 30px 0; height: auto;">更多活动-->
        <#--<span style="padding:0 0 0 20px;">-->
        <#--<a style="font-size: 16px; color: #2665cc; " href="/teacher/center/mylevel.vpage" target="_blank">提高教师等级，获得免费抽奖次数></a>-->
        <#--</span>-->
        <#--</h3>-->
        </div>
        <div class="con-right">
            <h4>获奖动态</h4>
            <#--<div class="cr-up">
                <ul>
                    <#if campaignLotteryResultsBig?size gt 0 >
                        <#list campaignLotteryResultsBig as cr>
                            <#if cr_index lt 3>
                                <li>
                                    <i class="number number${cr_index+1}">${cr_index+1}</i>
                                    ${cr.userName!''}获得了<span>${cr.awardName!'0'}</span><br>
                                    <p style="width: 214px; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">${cr.schoolName!''}</p>
                                </li>
                            </#if>
                        </#list>
                    <#else>
                        <li style="text-align: center; padding: 40px 0;">暂无数据</li>
                    </#if>
                </ul>
            </div>-->
            <div class="cr-down">
                <#--<h5 class="moving-title">获奖动态</h5>-->
                <div class="list">
                    <ul>
                        <#if campaignLotteryResults?size gt 0 >
                            <#list campaignLotteryResults as cr>
                                <#if cr_index lt 6>
                                    <li>
                                    ${cr.userName!''} ${cr.schoolName!''}  <strong
                                            style="display: inline-block;">获得了<span>${cr.awardName!'0'}</span></strong>
                                    </li>
                                </#if>
                            </#list>
                        <#else>
                            <li style="text-align: center; padding: 40px 0;">暂无数据</li>
                        </#if>
                    </ul>
                </div>
            </div>

        </div>
    </div>
</div>
<script type="text/html" id="T:抽奖Success">
    <div class="travelLottery-popup travelLottery-popup-4">
        <div class="close"><a href="javascript:void(0);" title="关闭"></a></div>
        <div class="content">
            恭喜您获得<strong><%if(content.awardName != ""){%><%=content.awardName%><%}%></strong>！
        <#--<%if(content.awardId == 1 || content.awardId == 2){%>
            <p>审核通过后7个工作日内发货，请耐心等待！</p>
        <%}%>-->
        </div>
        <div class="btn">
            <a href="javascript:void(0);" class="sub-btn"></a>
        </div>
    </div>
</script>
<script type="text/html" id="T:抽奖Error">
    <div class="travelLottery-popup travelLottery-popup-5">
        <div class="close"><a href="javascript:void(0);" title="关闭"></a></div>
        <div class="content">
            <%if(typeInfo == 1){%>
            您还未认证，认证老师才能参与本次活动<br/><a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage" style="color: #ff0;">现在去认证>></a>
            <%}else if(typeInfo == 2){%>
            您本周还未布置作业，布置作业后才能参与本次活动 <a href="/teacher/homework/batchassignhomework.vpage">去布置作业>></a>
            <%}else{%>
            <%=dataInfo%>
            <%}%>
        </div>
        <div class="btn">
            <a href="javascript:void(0);" class="sub-btn"></a>
        </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        var p = {};
        p.domain = '${requestContext.webAppBaseUrl}/';
        p.campaignId = 7;
        p.needPayJudgeFlag = ${(hasChance!false)?string};

        $("#movie").getFlash({
            id       : "lotteryFlash",
            width    : 454,//flash 宽度
            height   : 454, //flash 高度
            movie    : '<@flash.plugin name="TeacherLottery"/>',
            flashvars: p,
            wmode    : 'transparent'
        });

    });

    var weiXinCode = "//cdn.17zuoye.com/static/project/app/publiccode_teacher.jpg";

    if(!$17.getQuery("codeLink")){
        $.get("/teacher/qrcode.vpage?campaignId=20", function(data){
            if(data.success){
                weiXinCode = data.qrcode_url;
            }
            $("#weChatBackgroundAreaCode").html("<img src='" + weiXinCode + "' width='100'/>");
        });
    }else{
        weiXinCode = $17.getQuery("codeLink");
        $("#weChatBackgroundAreaCode").html("<img src='" + $17.getQuery("codeLink") + "' width='100'/>");
    }

    function payJudge(){
        <#if (currentUser.fetchCertificationState())?? && currentUser.fetchCertificationState() == "SUCCESS">
            $.prompt("<div class='w-ag-center' style='font-size: 16px;'>免费抽奖次数已用完<br/>继续抽奖消耗<strong>1</strong>园丁豆</div>", {
                title  : '系统提示',
                focus  : 1,
                buttons: { "取消": false, "继续抽奖": true },
                close  : function(){
                    document.getElementById("lotteryFlash").payJudgeSuccessCbk(false);
                },
                submit : function(e, v){
                    if(v){
                        $.prompt.close();
                        document.getElementById("lotteryFlash").payJudgeSuccessCbk(true);
                    }else{
                        document.getElementById("lotteryFlash").payJudgeSuccessCbk(false);
                    }
                }
            });
        <#else>
            $.prompt(template("T:抽奖Error", { typeInfo: 1, dataInfo: "" }), {
                prefix : "null",
                title  : '系统提示',
                buttons: {},
                loaded : function(){
                    var $popupBox = $(".travelLottery-popup");
                    $popupBox.find(".sub-btn, .close").on("click", function(){
                        $.prompt.close();
                        //                    location.href = "/campaign/teacherlottery.vpage?codeLink=" + weiXinCode;
                    });
                },
                classes: {
                    fade : 'jqifade',
                    close: 'w-hide',
                    title: 'w-hide'
                }
            });
        </#if>
    }

    function drawEndCbk($data){
        var jsonObj = eval('(' + $data + ')');

        if(jsonObj.awardId == 8){
            $17.alert(jsonObj.awardName, function(){
                location.href = "/campaign/teacherlottery.vpage?codeLink=" + weiXinCode;
            });
        }else{
            $.prompt(template("T:抽奖Success", { content: jsonObj }), {
                prefix : "nullSuccess",
                title  : '系统提示',
                buttons: {},
                loaded : function(){
                    var $popupBox = $(".travelLottery-popup");
                    $popupBox.find(".sub-btn, .close").on("click", function(){
                        $.prompt.close();
                        location.href = "/campaign/teacherlottery.vpage?codeLink=" + weiXinCode;
                    });
                },
                classes: {
                    fade : 'jqifade',
                    close: 'w-hide',
                    title: 'w-hide'
                }
            });
        }
    }

    function getLotteryDataFailCbk($data){
        var jsonObj = eval('(' + $data + ')');
        var typeInfo = 0;

        if(jsonObj.info.indexOf("认证") >= 0){
            typeInfo = 1;
        }else if(jsonObj.info.indexOf("未布置作业") >= 0){
            typeInfo = 2;
        }else if(jsonObj.info.indexOf("今日抽奖次数已用完") >= 0){
            $.prompt("<div style='font-size: 16px; text-align: center; line-height: 30px;padding-bottom: 20px;'>继续抽奖请用微信扫描下方二维码，到手机上抽奖<br/>电脑上每天只能抽奖5次！<br/><img src='" + weiXinCode + "' width='144'/></div>", {
                title  : '系统提示',
                buttons: {}
            });
            return false;
        }

        $.prompt(template("T:抽奖Error", { typeInfo: typeInfo, dataInfo: jsonObj.info }), {
            prefix : "null",
            title  : '系统提示',
            buttons: {},
            loaded : function(){
                var $popupBox = $(".travelLottery-popup");
                $popupBox.find(".sub-btn, .close").on("click", function(){
                    $.prompt.close();
                    location.href = "/campaign/teacherlottery.vpage?codeLink=" + weiXinCode;
                });
            },
            classes: {
                fade : 'jqifade',
                close: 'w-hide',
                title: 'w-hide'
            }
        });
    }
</script>
<script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8"></script>
</@temp.page>