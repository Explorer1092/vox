<#include "../../parentmobile/constants.ftl">
<#assign
    basePublicPath = "public/skin/download_app/"
    studentDownUrl = "//wx.17zuoye.com/download/17studentapp?cid=102002"
    parsentDownUrl = "//wx.17zuoye.com/download/17parentapp?cid=202002"
    teacherDownUrl = "//wx.17zuoye.com/download/17teacherapp?cid=302002"
    pcDownUrl = {
        "parent" : [parsentDownUrl, "//itunes.apple.com/cn/app/jia-zhang-tong-yi-qi-zuo-ye/id913817574?l=zh&ls=1&mt=8"],
        "student" : [studentDownUrl, "//itunes.apple.com/cn/app/yi-qi-zuo-ye-xue-sheng-duan/id1004963943?mt=8"],
        "teacher" : [teacherDownUrl, "//itunes.apple.com/cn/app/yi-qi-zuo-ye-lao-shi-duan/id961582881?l=zh&ls;=1&mt;=8"]
    }
>

<#assign refrerer = (refrerer!"")?trim >
<#if refrerer == "mobile">
    <#include "./download_mobile.ftl">
<#elseif refrerer == "pc">
    <#include "./download_pc.ftl">
<#else>
<script>
    location.href = "/help/downloadApp.vpage?refrerer=" + (
                    (/android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/.test(window.navigator.userAgent.toLowerCase()) && ("ontouchstart" in document) ) ? "mobile" : "pc"
            );
</script>
</#if>
