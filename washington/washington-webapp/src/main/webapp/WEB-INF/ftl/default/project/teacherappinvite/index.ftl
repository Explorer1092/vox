<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="五周年感恩"
pageJs=['jquery', 'template']
pageCssFile={"css" : ["public/skin/project/teacherappinvite/css/skin"]}
>
<div class="rulesPopup" id="popupInfo" style="display: none; z-index: 12;">
    <div class="rulesInner">
        <div class="innerBox">
            <span class="close JS-clickInfoPopup" data-type="close"></span>
            <div class="title">活动规则详解</div>
            <div class="text">
                <p class="txt">活动时间：<br>2016年11月10日-2016年12月31日（认证时间）</p>
                <p class="txt">活动规则：<br>注册一起作业并达到如下条件的小学数学老师，最高可领40元话费补贴，在满足条件后的72小时内到账。</p>
                <p class="txt">1. 认证后累计布置3天作业，每天的作业在到期前均大于等于30人完成，获得20元话费补贴；</p>
                <p class="txt">2. 认证后累计布置3天作业，每天的作业在到期前均大于等于45人完成，累计获得30元话费补贴；</p>
                <p class="txt">3. 认证后累计布置3天作业，每天的作业在到期前均大于等于60人完成，累计获得40元话费补贴；</p>
                <p class="txt">4. 布置作业的预计时长需大于5分钟；</p>
                <p class="txt">5. 直接完成3天45/60人作业时，可直接获得对应档位的补贴，无需分步领取；</p>
                <p class="txt">6. 本次活动为小学数学老师专属活动；</p>
                <p class="txt">7. 活动有效期为达到认证的30天内，即：如果认证后的30天之后再达到条件，无话费补贴.</p>
            </div>
        </div>
    </div>
</div>
<header>
    <img src="<@app.link href="public/skin/project/teacherappinvite/images/banner.png"/>">
    <div class="tips">领奖倒计时<span class="time JS-datetime-1">0</span><span class="time JS-datetime-2">0</span>天</div>
    <a href="javascript:;" class="ruleLink JS-clickInfoPopup" data-type="open">详细规则</a>
</header>
<article id="AuthContent"></article>

<aside>
    <p class="txt"><span class="num">1、</span>布置作业的预计时长需要大于5分钟;</p>
    <p class="txt"><span class="num">2、</span>在作业截止日期前的完成人数（排重后）大于或等于30/45/60;</p>
    <p class="txt"><span class="num">3、</span>领奖活动有效期为达到认证后的30天内;</p>
    <p class="txt"><span class="num">4、</span>以上领奖进度隔日更新.</p>
</aside>

<script type="text/html" id="T:认证状态">
    <section class="active">
        <div class="text">
            <p><span class="tGreen">完成注册</span></p>
        </div>
        <div class="state">
            <span class="tGreen">已完成</span>
        </div>
    </section>
    <section class="<%=(content.isAuth ? 'active' : '')%>">
        <div class="text">
            <p>达到认证条件<a href="javascript:;" class="tBlue JS-clickInfoPopup" data-type="open">(点击查看如何认证)</a></p>
        </div>
        <div class="state">
            <%if(content.isAuth){%>
                <span class="tGreen">已认证</span>
            <%}else{%>
                <span class="tRed">尚未认证</span>
            <%}%>
        </div>
    </section>
    <section class="<%=(content.phase >= 1 ? 'active' : '')%>">
        <div class="text">
            <p>认证后累计布置3天30人作业</p>
            <p>可获得20元话费补贴</p>
        </div>
        <div class="state">
            <%if(content.phase >= 1){%>
            <span class="tGreen">已完成</span>
            <%}else{%>
            <a href="javascript:;" class="btn JS-EnterHomework">去布置</a>
            <%}%>
        </div>
    </section>
    <section class="<%=(content.phase >= 2 ? 'active' : '')%>">
        <div class="text">
            <p>认证后累计布置3天45人作业</p>
            <p>累计获得30元话费补贴</p>
        </div>
        <div class="state">
            <%if(content.phase >= 2){%>
            <span class="tGreen">已完成</span>
            <%}else{%>
            <a href="javascript:;" class="btn JS-EnterHomework">去布置</a>
            <%}%>
        </div>
    </section>
    <section class="<%=(content.phase >= 3 ? 'active' : '')%>">
        <div class="text">
            <p>认证后累计布置3天60人作业</p>
            <p>累计获得40元话费补贴</p>
        </div>
        <div class="state">
            <%if(content.phase >= 3){%>
            <span class="tGreen">已完成</span>
            <%}else{%>
            <a href="javascript:;" class="btn JS-EnterHomework">去布置</a>
            <%}%>
        </div>
    </section>
</script>
<script type="text/javascript">
    signRunScript = function ($) {
        //script start
        $(document).on("click", ".JS-clickInfoPopup", function () {
            var $self = $(this);
            var $popupInfo = $("#popupInfo");

            if ($self.attr("data-type") == "open") {
                $popupInfo.show();

            } else {
                $popupInfo.hide();
            }
        });

        var $dataContent = {};

        $.get("/teacherMobile/invite/draw/reward/progressnew.vpage", {}, function (data) {
            $dataContent.remindDays = data.remindDays || 0;
            $dataContent.phase = data.phase || 0;
            $dataContent.isAuth = data.isAuth || false;

            $("#AuthContent").html( template("T:认证状态", {
                content: $dataContent
            }) );

            var _arrayList = $dataContent.remindDays.toString().split("");

            if(_arrayList.length > 1){
                $(".JS-datetime-1").text( _arrayList[0] );
                $(".JS-datetime-2").text(_arrayList[1]);
            }else{
                $(".JS-datetime-2").text(_arrayList[0]);
            }
        });

        $(document).on("click", ".JS-EnterHomework", function(){
            <#if (currentTeacherDetail.isJuniorTeacher())!false>
                //小学
                if(window['external'] && window.external['forwardPage']){
                    window.external.forwardPage( JSON.stringify({
                        type: 'juniorGoSetHomework'
                    }) );
                    return false
                }
            <#else>
                //小学
                if(window['external'] && window.external['forwardPage']){
                    window.external.forwardPage( JSON.stringify({
                        type: 'primaryGoSetHomework '
                    }) );
                    return false
                }
            </#if>

            location.href = getWeChatUrl() + "/teacher/homework/index.vpage";
        });

        function getWeChatUrl(){
            if(location.href.indexOf("https://") > -1){
               return "https://" + location.host.replace(/www./g, "wechat.");
            }else{
                if(location.href.indexOf("www.") > -1){
                    return "http://" + location.host.replace(/www./g, "wechat.");
                }else{
                    return "http://wechat.test.17zuoye.net";
                }
            }
        }
    }
</script>
</@layout.page>