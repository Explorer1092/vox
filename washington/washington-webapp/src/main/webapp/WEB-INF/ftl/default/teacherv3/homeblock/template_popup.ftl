<#--我要当校园大使-->
<#--<script type="text/html" id="T:我要当校园大使-popup">-->
    <#--<style>-->
        <#--.attack-kingdom-bg{ background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/pop-ambassador-v5.png"/>) no-repeat 5000px 5000px;}-->
        <#--.attack-kingdom-box{ width: 574px; height: 470px; position: relative; background-position: 0 0;}-->
        <#--.attack-kingdom-box .hp-btn{ position: absolute; bottom: 0; right: 148px; height: 81px;}-->
        <#--.attack-kingdom-box .hp-btn a{ width: 201px; height: 81px; display: inline-block;}-->
        <#--.attack-kingdom-box .hp-btn a:hover{ background-position: -100px -505px;}-->
        <#--.attack-kingdom-box .hp-btn a:active{ background-position: -349px -504px;}-->
        <#--.attack-kingdom-box .hp-close{ position: absolute; top: 20px; right: 0;}-->
        <#--.attack-kingdom-box .hp-close a{ width: 50px; height: 50px; display: inline-block;}-->
    <#--</style>-->
    <#--<div class="attack-kingdom-box attack-kingdom-bg">-->
        <#--<div class="hp-btn">-->
            <#--<a class="attack-kingdom-bg" href="/ambassador/schoolambassador.vpage" target="_blank" onclick="$.prompt.close();"></a>-->
        <#--</div>-->
        <#--<div class="hp-close">-->
            <#--<a class="attack-kingdom-bg" href="javascript:$.prompt.close();"></a>-->
        <#--</div>-->
    <#--</div>-->
<#--</script>-->

<#--老师邀请老师 - 07-04 下线-->
<script type="text/html" id="T:老师邀请老师-popup">
    <style>
        .t-invitation-pop{}
        .t-invitation-pop, .t-invitation-pop .hp-btn a,.t-invitation-pop .hp-close a{ width: 604px; height: 427px; position: relative; background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/invitation-pop.png"/>) no-repeat;}
        .t-invitation-pop .hp-btn{ position: absolute;bottom: -1px;right: 211px;}
        .t-invitation-pop .hp-btn a{ width: 162px; height: 64px; display: inline-block; background-position: -231px -363px;}
        .t-invitation-pop .hp-btn a:active{ background-position: -234px -462px;}
        .t-invitation-pop .hp-close{ position: absolute;top: 1px;right: 47px;}
        .t-invitation-pop .hp-close a{ width: 50px; height: 50px; display: inline-block; background-position: -507px -1px;}
        .t-invitation-pop .hp-close a:active{ background-position: -142px -476px;}
    </style>
    <div class="t-invitation-pop">
        <div class="hp-btn">
            <a class="invitation_btn" href="/teacher/invite/index.vpage" onclick="$.prompt.close();"></a>
        </div>
        <div class="hp-close">
            <a class="close" href="javascript:void(0);" onclick="$.prompt.close();"></a>
        </div>
    </div>
</script>

<script type="text/html" id="t:wechatInvitePopup">
    <style>
        .invite-prize-bg{ background: url(<@app.link href="public/skin/teacherv3/images/intive/inviteAlert.png"/>) no-repeat 5000px 5000px;}
        .invite-prize-box{ width: 542px; height: 406px; position: relative; background-position: 0 0;}
        .invite-prize-box .hp-box{ position: absolute; bottom: 71px; right: 211px; width: 120px; height: 120px;}
        .invite-prize-box .hp-box img{width: 120px; height: 120px;}
        .invite-prize-box .hp-close{ position: absolute; top: 52px; right: 0;}
        .invite-prize-box .hp-close a{ width: 32px; height: 32px; display: inline-block; background-position: -510px -52px;}
        .invite-prize-box .hp-close a:hover{ background-position: -449px -408px;}
        .invite-prize-box .hp-close a:active{ background-position: -481px -408px;}
    </style>
    <div class="invite-prize-box invite-prize-bg">
        <div class="hp-box" id="QRCodeImgUrlBox">
        <#--<img src="#">-->
        </div>
        <div class="hp-close">
            <a onclick="$17.tongji('老师-首页-微信弹窗-关闭');" class="invite-prize-bg" href="javascript:$.prompt.close();"></a>
        </div>
    </div>
</script>
<script type="text/html" id="t:听力材料页新增专属试卷">
    <style>
        .preview-ttsIndex-pop{ background: url(<@app.link href="public/skin/teacherv3/images/tts/tts-index-pop.png"/>) no-repeat 0 0; width: 672px; height: 445px; position: relative;}
        .preview-ttsIndex-pop h2{ color: #fff; text-align: center; font-size: 24px; font-weight: normal; height: 58px; line-height: 58px;}
        .preview-ttsIndex-pop .preview-ttsIndex-btn{ text-align: center; position: absolute; bottom: 40px; width: 100%;}
        .preview-ttsIndex-pop .preview-ttsIndex-btn a{ margin: 0 40px;}
        .preview-ttsIndex-pop .preview-ttsIndex-btn a.w-btn-green{ background-color: #97ce4f;}
        .preview-ttsIndex-pop .preview-ttsIndex-btn a.w-btn-green:hover{ background-color: #a1dc54;}
        .preview-ttsIndex-pop .preview-ttsIndex-btn a.w-btn-green:active{ background-color: #8ec249;}
        .preview-ttsIndex-pop .preview-ttsIndex-btn a.w-border-green{ border-color: #68b614;}
    </style>
    <div class="preview-ttsIndex-pop">
        <h2>听力材料页新增<#if currentUser.regionCode == 110112>通州<#else>潍坊</#if>专属试卷</h2>
        <div class="preview-ttsIndex-btn">
            <a href="javascript:$.prompt.close();" class="w-btn w-btn-green w-btn-red w-circular-5 w-border-green" >稍后再说</a>
            <a href="/tts/listening.vpage" class="w-btn w-btn-red w-circular-5 w-border-red" onclick="$17.tongji('O2O-TTS-Paper-首页弹窗点击查看');" >立即查看</a>
        </div>
    </div>
</script>
<#--我要当校园大使-->
<#--<script type="text/html" id="T:我要当校园大使-popup">-->
    <#--<style>-->
        <#--.attack-kingdom-bg{ background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/pop-ambassador-v5.png"/>) no-repeat 5000px 5000px;}-->
        <#--.attack-kingdom-box{ width: 574px; height: 470px; position: relative; background-position: 0 0;}-->
        <#--.attack-kingdom-box .hp-btn{ position: absolute; bottom: 0; right: 148px; height: 81px;}-->
        <#--.attack-kingdom-box .hp-btn a{ width: 201px; height: 81px; display: inline-block;}-->
        <#--.attack-kingdom-box .hp-btn a:hover{ background-position: -100px -505px;}-->
        <#--.attack-kingdom-box .hp-btn a:active{ background-position: -349px -504px;}-->
        <#--.attack-kingdom-box .hp-close{ position: absolute; top: 20px; right: 0;}-->
        <#--.attack-kingdom-box .hp-close a{ width: 50px; height: 50px; display: inline-block;}-->
    <#--</style>-->
    <#--<div class="attack-kingdom-box attack-kingdom-bg">-->
        <#--<div class="hp-btn">-->
            <#--<a class="attack-kingdom-bg" href="/ambassador/schoolambassador.vpage?ref=index-popup" target="_blank" onclick="$.prompt.close();"></a>-->
        <#--</div>-->
        <#--<div class="hp-close">-->
            <#--<a class="attack-kingdom-bg" href="javascript:$.prompt.close();"></a>-->
        <#--</div>-->
    <#--</div>-->
<#--</script>-->

<#--T:大使新活动-->
<script type="text/html" id="T:大使新活动">
    <style>
        .newamb-kingdom-bg{ background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/ambassador-popup.png"/>) no-repeat 5000px 5000px;}
        .newamb-kingdom-box{ width: 491px; height: 363px; position: relative; background-position: 0 0;}
        .newamb-kingdom-box .hp-btn{ position: absolute; bottom: 0; right: 148px; height: 81px;}
        .newamb-kingdom-box .hp-btn a{ width: 201px; height: 81px; display: inline-block;}
        .newamb-kingdom-box .hp-btn a:hover{ background-position: -100px -505px;}
        .newamb-kingdom-box .hp-btn a:active{ background-position: -349px -504px;}
        .newamb-kingdom-box .hp-close{ position: absolute; top: 20px; right: 0;}
        .newamb-kingdom-box .hp-close a{ width: 50px; height: 50px; display: inline-block;}
    </style>
    <div class="newamb-kingdom-box newamb-kingdom-bg">
        <div class="hp-btn">
            <a class="newamb-kingdom-bg" href="/ambassador/center.vpage?ref=popup" target="_blank" onclick="$.prompt.close();"></a>
        </div>
        <div class="hp-close">
            <a class="newamb-kingdom-bg" href="javascript:$.prompt.close();"></a>
        </div>
    </div>
</script>

<#--T:杭州老师APP推广-->
<script type="text/html" id="T:杭州老师APP推广">
    <style>
        .extension-pop-bg{ background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/extension_pop.png"/>) no-repeat 5000px 5000px;}
        .extension-pop{ width: 460px; height: 296px; position: relative; background-position: 0 0;}
        .extension-pop .hp-btn{ position: absolute;bottom: 10px;right: 154px;}
        .extension-pop .hp-btn a{ width: 155px; height: 31px; display: inline-block;}
        .extension-pop .hp-close{ position: absolute; top: 0; right: 0;}
        .extension-pop .hp-close a{ width: 31px; height: 31px; display: inline-block;}
    </style>
    <div class="extension-pop extension-pop-bg">
        <div class="hp-btn">
            <a class="extension-pop-bg js-log" href="/project/extension/index.vpage" target="_blank" onclick="$.prompt.close();"></a>
        </div>
        <div class="hp-close">
            <a class="extension-pop-bg" href="javascript:$.prompt.close();"></a>
        </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        $(document).on("click",".js-log",function(){
            $17.voxLog({
                module : "project-extension",
                op : "btnJump"
            });
        });
    });
</script>

<#--首页广告位弹窗-->
<script type="text/html" id="T:PUBLIC-POPUP-BOX">
    <%var popupItems = result.data[index];%>
    <div class="crm-popup-box" style="text-align: center;">
        <div style="text-align: center; display: inline-block; position: relative;">
            <div class="cp-close" style="position: absolute; right: -10px; top: -10px; width: 38px ; height: 38px; ">
                <a  href="javascript:$.prompt.close();" style="width: 100%; height: 100%; display: block; color: #fff; font-size: 32px;line-height: 100%; text-align: center; background-color: #000; border-radius: 100%;" title="关闭">×</a>
            </div>
            <a href="<%=result.goLink%>?aid=<%=popupItems.id%>&index=<%=index%>" onclick="$.prompt.close();" target="_blank">
                <img src="<%=result.imgDoMain%>/gridfs/<%=popupItems.img%>" alt="<%=popupItems.name%>">
            </a>
        </div>
    </div>
</script>

<#--暑假、寒假作业弹窗-->
<script type="text/html" id="T:VACATION_POPUP">
    <style>
        .vacation-pop{ background: url(<@app.link href="public/skin/teacherv3/images/vacation/vacation-dialog.png?v=1.0.2"/>) no-repeat 5000px 5000px; position:relative;width:433px;height:367px;;background-position: 0 0}
        .vacation-pop .vp-btn{position:absolute;bottom:30px;left:62px}
        .vacation-pop .vp-btn a{width:270px;height:46px;display:inline-block}
        .vacation-pop .vp-close{position:absolute;top:0;right:0}
        .vacation-pop .vp-close a{width:32px;height:30px;display:inline-block}
    </style>
    <div class="vacation-pop">
        <div class="vp-btn">
            <a class="" href="/teacher/vacation/index.vpage?subject=<%=subject%>"></a>
        </div>
        <div class="vp-close">
            <a class="" href="javascript:$.prompt.close()"></a>
        </div>
    </div>
</script>