<#include "../common/config.ftl">

<#import '../common/layout.ftl' as layout>
<#assign layout = layout>

<#assign headBlock = "">

<#assign bottomBlock>
<script>
    "use strict";

    (function(){
        var share = function(have_icon){

            var shareInfo = {
                title : "测试分享",
                content : "这是一个测试分享的页面 默认跳到我们首页",
                url : "http://www.17zuoye.com/"
            };

            if(have_icon){
                shareInfo.icon =  "http://cdn-cnc.17zuoye.cn/public/skin/parentMobile/images/activity/winterHoliday/parentApp-winterHoliday-beans.png";
            }

            window.app.doExternal("shareInfo", JSON.stringify(shareInfo));
        };

        document.getElementById("test_share").onclick = function(){
            share(true);
        };

        document.getElementById("test_share_no_icon").onclick = function(){
            share(false);
        };

    })();
</script>
</#assign>
