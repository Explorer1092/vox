<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="分享活动"
pageJs=["share"]
pageJsFile={"share" : "public/script/project/qrcodeshare"}
pageCssFile={"share" : ["public/skin/project/share/css/skin"]}
>
<#--通用页面：生成二维码，手机扫码分享-->
<div class="share-wrap">
    <div class="qrcode-content">
        <div class="wechat-tip" id="wxTip" style="display: none;"></div>
        <div class="qrcode-box" id="qrcodeBox" style="display: none;">
            <#-- size: 256*256-->
            <div class="qrcode-box" id="shareQrcode"></div>
        </div>
        <div class="not-support" id="notSupportTip" style="display: none;">
            <p>当前浏览器不支持生成二维码，推荐使用谷歌浏览器、火狐浏览器打开或手动复制链接到手机端打开</p><br>
            <p id="shareLink"></p>
        </div>
    </div>
</div>

<script>

</script>
</@layout.page>