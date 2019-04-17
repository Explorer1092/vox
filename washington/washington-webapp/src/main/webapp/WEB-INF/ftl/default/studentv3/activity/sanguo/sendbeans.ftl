<#import "../../../layout/project.module.student.ftl" as temp />
<@temp.page title="进击的三国" header="show">
<@sugar.capsule js=["DD_belatedPNG_class"] css=[] />
<@app.css href="public/skin/project/sanguo/css/skin.css?1.0.1" />
<!--//start-->
<div class="main">
    <div class="header">
        <#--<div class="text-back header-text"></div>-->
    </div>
    <div class="container">
        <div class="container-inner">
            <!--//start-->
            <!--少年郎，看你骨骼精奇，《进击的三国》让你成为英雄！-->
            <div class="text-back title-text-7"></div>
            <!--進擊的三国 登錄就送：-->
            <dl class="item-container">
                <dt><h3 class="text-back title-text-2">進擊的三国 登錄就送：</h3></dt>
                <dd style="overflow: hidden;">
                    <a href="javascript:void(0);" class="s-btn s-btn-w click-registration-btn"></a>
                    <div class="text-back title-text-10"></div>
                </dd>
                <dd>
                    <div class="item-inner-block" style="margin-right: 96px;">
                        <div class="item-inner-avt"><img src="<@app.link href="public/skin/project/sanguo/images/k-1.png"/>"></div>
                        <p>累计登录4次送5星貂蝉</p>
                    </div>
                    <div class="item-inner-block">
                        <div class="item-inner-avt"><img src="<@app.link href="public/skin/project/sanguo/images/k-3.png"/>"></div>
                        <p>活动期间每日登录送5000园丁豆、5钻石</p>
                    </div>
                </dd>
            </dl>
            <!--呼朋引伴 狂搶学豆：-->
            <dl class="item-container" style="margin-bottom: 60px;">
                <dt><h3 class="text-back title-text-3">進擊的三国 登錄就送：</h3></dt>
                <dd style="overflow: hidden;">
                    <a href="javascript:void(0);" class="s-btn s-btn-w s-btn-green click-conscription-btn"></a>
                    <div class="text-back title-text-9"></div>
                </dd>
                <dd>
                    <div class="item-inner-block" style="margin-right: 96px;">
                        <div class="item-inner-avt"><img src="<@app.link href="public/skin/project/sanguo/images/k-2.png"/>"></div>
                    </div>
                    <div class="item-inner-block">
                        <p>每天19点至20点登录，赠送2学豆</p>
                        <p>每天19点至20点，同班在线8人，每人赠送2学豆</p>
                        <p>每天19点至20点，同班在线9~11人，每人赠送3学豆</p>
                        <p>每天19点至20点，同班在线≧12人，每人赠送4学豆</p>
                    </div>
                </dd>
            </dl>
            <div class="answer-container">
                <div class="answer-container-inner">
                    <div class="answer-container-top PNG_24"></div>
                    <div class="answer-container-mid PNG_24">
                        <!--//start-->
                        <!--测一测你是哪种英雄-->
                        <div class="text-back title-text-8"></div>
                        <!--end//-->
                    </div>
                    <div class="answer-container-bot PNG_24"></div>
                </div>
            </div>
            <!--end//-->
        </div>
        <div class="left-carton PNG_24"></div>
    </div>
</div>
<!--end//-->
<script type="text/javascript">
    $(function(){
        $17.tongji("进击的三国-征兵送学豆-loading");

        $(".click-registration-btn").on("click", function(){
            $.post("/campaign/29/appoint.vpage", {
                campaignId : 29,
                appKey : "SanguoDmz"
            }, function(data){
                if(data.success){
                    $17.tongji("进击的三国-征兵送学豆-报名成功");
                    $17.alert("报名成功！");
                }else{
                    $17.alert(data.info);
                }
            });
        });

        $(".click-conscription-btn").on("click", function(){
            $.post("/campaign/sharesanguo.vpage", {}, function(data){
                if(data.success){
                    $17.alert("成功分享到班级！");
                    $17.tongji("进击的三国-征兵送学豆-分享成功");
                }else{
                    $17.alert(data.info);
                }
            });
        });
    });
</script>
</@temp.page>