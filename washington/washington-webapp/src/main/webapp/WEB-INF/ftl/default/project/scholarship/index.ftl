<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="自学奖学金"
pageJs=["scholarship"]
pageJsFile={"scholarship" : "public/script/parentMobile/fairyland/scholarship"}
pageCssFile={"scholarship" : ["public/skin/project/scholarship/css/skin"]}
>
<div class="selfScholarship-box">
    <div class="ssp-banner">
        <img src="<@app.link href='/public/skin/project/scholarship/images/scholarship/bg_01.jpg' />">
    </div>
    <div class="ssp-main">
        <div class="ssp-content">
            <div class="info"><span class="num">800</span>元奖学金<span class="sub">共12名</span></div>
            <div class="info"><span class="num">800</span>元奖学金<span class="sub">共12名</span></div>
        </div>
        <img src="<@app.link href='/public/skin/project/scholarship/images/scholarship/bg_03.jpg' />">
        <img src="<@app.link href='/public/skin/project/scholarship/images/scholarship/bg_04.jpg' />">
    </div>
    <div class="ssp-footer">
        <div id="js-toast" class="inner toast" style="display: none">
            <p style="text-align: center; width:100%;"></p>
        </div>
        <div class="inner bg">
            <a href="javascript:void(0)" class="js_apply default_btn">我要申请</a>
            <a href="javascript:void(0)" class="js_obtain default_btn red_btn">获得资格</a>
        </div>
    </div>
</div>
<script>
    var currentUser = ${json_encode(currentUser)};
</script>
</@layout.page>