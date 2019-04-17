<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="rewardreceivebonus" showNav="show">
<style>
    *{ padding: 0; margin: 0; list-style: none;}
    .mic-active-box{ width: 760px;}
    .mic-active-box .mic-head{ position: relative; margin-bottom: 20px; width: 760px; height: 295px; background:url(//cdn.17zuoye.com/static/project/mobileHomework/weixinHead.jpg) no-repeat 0 0;}
    .mic-active-box .mic-head .info{ position: absolute; top: 38px; left: 76px;}
    .mic-active-box .mic-content{}
    .mic-active-box .mic-content .title{ font-size: 16px; font-weight: normal; color: #ff4759; text-align: center; padding: 20px 0 0;}
    .mic-active-box .mic-content .mt-list {background-color: #fff;}
    .mic-active-box .mic-content .mt-list dl{  height: 185px; overflow: hidden; *zoom: 1;}
    .mic-active-box .mic-content .mt-list dl.gray{ background-color: #fafafa;}
    .mic-active-box .mic-content .mt-list dl dt{ float: left; width: 250px; text-align: center;}
    .mic-active-box .mic-content .mt-list dl .luck-icon{ background: url(//cdn.17zuoye.com/static/project/mobileHomework/luck-icon-v1.png) no-repeat 5000px 5000px; width: 156px; height: 185px; display: inline-block;}
    .mic-active-box .mic-content .mt-list dl .luck-01{ background-position: 0 0;}
    .mic-active-box .mic-content .mt-list dl .luck-02{ background-position: 0 -185px;}
    .mic-active-box .mic-content .mt-list dl .luck-03{ background-position: 0 -369px;}
    .mic-active-box .mic-content .mt-list dl dd{ margin-left: 250px; overflow: hidden; *zoom: 1; padding: 40px 0 0 0;}
    .mic-active-box .mic-content .mt-list dl dd .mi-info{ width: 355px; float: left;}
    .mic-active-box .mic-content .mt-list dl dd .mi-info h2{ font-size: 20px; color: #ff4759; padding: 4px 0 8px 0;}
    .mic-active-box .mic-content .mt-list dl dd .mi-info p{ font-size: 16px; line-height: 36px;}
    .mic-active-box .mic-content .mt-list dl dd .luck-btn{ width: 140px; float: right; margin-top: 48px;}
    .mic-active-box .mic-content .mt-list dl dd .luck-btn a{ padding: 12px 0;}
    .mic-active-box .mic-content .mt-list dl.info-list dd{ margin-left: 0; text-align: center;}
    .mic-active-box .mic-content .mt-list dl.info-list dd .mi-info{ width: 100%; padding-top: 20px;}
    .mic-active-box .mic-content .mt-list dl.info-list dd .mi-info h2{ font-size: 16px; font-weight: normal; }
    .reward-box p{line-height: 29px !important;}
    .mic-active-box .mic-content .w-btn{background-color: #ff4659;}
    .mic-active-box .mic-content .w-btn:hover{background-color: #fd6171;}
    .mic-active-box .mic-content .w-btn:active{background-color: #ff2037;}
    .w-btn-disabled, .w-btn-disabled:hover, .w-btn-disabled:active{ background-color: #dfdfdf!important;}
</style>

<div class="mic-active-box">
    <div class="mic-head">
        <div class="info" id="weChatBackgroundAreaCode"></div>
    </div>
    <div class="mic-content">
        <div class="mt-list">
            <h3 class="title">扫描一下页面上方二维码即可参与以下微信专属活动</h3>
            <dl>
                <dt><i class="luck-icon luck-01"></i></dt>
                <dd>
                    <div class="mi-info reward-box">
                        <h2>班费奖励:</h2>
                        <#if currentUser.fetchCertificationState() == "SUCCESS">
                            <p>课堂奖励：活跃课堂气氛、提高课堂效率！</p>
                            <p>绑定微信，系统赠送100学豆奖励学生；</p>
                            <p>用微信检查作业，每天每班再送10学豆！</p>
                        <#else>
                            <p>认证后使用微信课堂奖励，活跃课堂神器</p>
                            <p>认证后使用微信检查作业，每天赠送10学豆</p>
                        </#if>

                    </div>
                    <div class="luck-btn">
                        <#if currentUser.fetchCertificationState() == "SUCCESS">
                            <#if wxbinded>
                                <a class="w-btn w-btn-mini w-btn-disabled" href="javascript:void (0);">已领取</a>
                            <#else>
                                <a style="background-color:#ff4659;" class="w-btn w-btn-mini" href="javascript:void (0);" id="rewardBtn">领取班费</a>
                            </#if>
                        <#else>
                            <a onclick="$17.tongji('老师-班费奖励-认证');" class="w-btn w-btn-mini" href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage" target="_blank">去认证</a>
                        </#if>
                    </div>
                </dd>
            </dl>
            <dl class="gray">
                <dt><i class="luck-icon luck-02"></i></dt>
                <dd>
                    <div class="mi-info">
                        <h2>抽奖活动：</h2>
                        <p>
                            专享小米平板和红米手机等奖品，<br>每天布置作业就送5次免费抽奖
                        </p>
                    </div>
                </dd>
            </dl>
            <dl>
                <dt><i class="luck-icon luck-03"></i></dt>
                <dd>
                    <div class="mi-info">
                        <h2>功能投票：</h2>
                        <p>
                            定期举办投票，选出老师们想要的<br>功能！只要你想要，我们就能造！
                        </p>
                    </div>
                </dd>
            </dl>
            <#--<dl class="gray info-list">
                <dd>
                    <div class="mi-info">
                        <h2>奖励学生：（即将推出，敬请期待）</h2>
                        <p>
                            系统每月赠送老师学豆用于奖励学生！比电脑上更好用，随时随地超方便！
                        </p>
                    </div>
                </dd>
            </dl>-->
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        LeftMenu.focus("mobileHomework");

        var weiXinCode = "//cdn.17zuoye.com/static/project/app/publiccode_teacher.jpg";
        <#if ((wxbinded)!false)>
            $("#weChatBackgroundAreaCode").html("<img src='"+ weiXinCode +"' width='130'/>");
        <#else>
            $.get("/teacher/qrcode.vpage?campaignId=24", function(data){
                if(data.success){
                    if ($.browser.msie && parseInt($.browser.version, 10) == 6) {
                        weiXinCode = (data.qrcode_url).replace('https://', 'http://');
                    }else{
                        weiXinCode = data.qrcode_url;
                    }
                }
                $("#weChatBackgroundAreaCode").html("<img src='"+ weiXinCode +"' width='130'/>");
            });
        </#if>

        $("#rewardBtn").on("click", function(){
            $17.tongji('老师-领取班费-绑定微信');
            $.prompt("<div class='w-ag-center'><img src='"+ weiXinCode +"' width='200'/><p>微信扫一扫<br/>立即领取学豆奖励</p></div>", {
                title: "绑定微信",
                buttons: {"确定" : true},
                position: {width: 500},
                submit: function(){
                    location.reload();
                },
                close: function(){
                    location.reload();
                }
            });
        });
    });
</script>
</@shell.page>