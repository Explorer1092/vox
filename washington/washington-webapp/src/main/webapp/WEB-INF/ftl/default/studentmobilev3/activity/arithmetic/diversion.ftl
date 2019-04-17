<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='速算脑力王'
pageJs=["jquery", "YQ","versionCompare", "voxLogs"]
pageCssFile={"arithmetic" : ["public/skin/mobile/student/app/activity/arithmetic/css/skin"]}

>

<div class="sc-bg"><img src="<@app.link href="public/skin/mobile/student/app/activity/arithmetic/images/bg01.jpg"/>"></div>
<div class="sc-bg"><img src="<@app.link href="public/skin/mobile/student/app/activity/arithmetic/images/bg02.jpg"/>"></div>
<div class="sc-bg"><img src="<@app.link href="public/skin/mobile/student/app/activity/arithmetic/images/bg03.jpg"/>"></div>
<div class="sc-bg"><img src="<@app.link href="public/skin/mobile/student/app/activity/arithmetic/images/bg04.jpg"/>"></div>
<div class="sc-bg"><img src="<@app.link href="public/skin/mobile/student/app/activity/arithmetic/images/bg05.jpg"/>"></div>
<div class="sc-bg"><img src="<@app.link href="public/skin/mobile/student/app/activity/arithmetic/images/bg06.jpg"/>"></div>
<div class="sc-footer">
    <div class="sc-btnBox" id="openGameBtn">
        <a href="javascript:void(0)" class="btn"></a>
    </div>
</div>
<!--弹窗-->
<div class="sc-popup" style="display: none;">
    <div class="popupInner">
        <span id="upgradeBoxCloseBtn" class="close"></span>
        <span id="upgradeBtn" class="btn"></span>
    </div>
</div>

<script type="text/javascript">
    signRunScript = function ($, $17, versionCompare) {
        var module = 'm_7zMiBxWc';
        var isNotAndroid = window.navigator.userAgent.toLowerCase().indexOf('android') === -1;
        var supportVersion = isNotAndroid ? '2.7.7' : '2.7.8';

        //打开速算脑力王
        $("#openGameBtn").on('click', function () {
            var appVersion = $17.getAppVersion();
            if (versionCompare(appVersion, supportVersion) > -1) {
                $17.openGameApp({
                    appKey: 'Arithmetic'
                });
            } else {
                $('div.sc-popup').show();
                YQ.voxLogs({
                    module: module,
                    op: "o_Pa4DoJlh"
                });
            }
            YQ.voxLogs({
                module: module,
                op: "o_6sQ05rBr"
            });
        });

        //去升级
        $('#upgradeBtn').on('click', function () {
            setTimeout(function () {
                location.href = 'https://wx.17zuoye.com/download/17studentapp?cid=103002';
            }, 200);

            YQ.voxLogs({
                module: module,
                op: "o_V7wNNLhb"
            });
        });

        //关闭弹窗
        $('#upgradeBoxCloseBtn').on('click', function () {
            $(this).closest('div.sc-popup').hide();
            YQ.voxLogs({
                module: module,
                op: "o_iPjTdSTy"
            });
        });

        YQ.voxLogs({
            module: module,
            op: "o_tCCM0u6o"
        });
    };
</script>

</@layout.page>