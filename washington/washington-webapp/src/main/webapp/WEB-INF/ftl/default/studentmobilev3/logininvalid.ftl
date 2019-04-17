<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="提示"
pageCssFile={"init" : ["public/skin/mobile/student/app/css/skin"]}
>
<div class="w-layer-confirm">
    <div class="text center">登录信息已失效，请重新登录~</div>
    <div class="w-footer no-fixed">
        <div class="inner">
            <div class="btn-box">
                <a href="javascript:;" onclick="redirectLogin();" class="btn">重新登录</a>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function redirectLogin(){
        if(window.external && ('redirectLogin' in window.external)){
            window.external.redirectLogin("");
        }else{
            location.href='/';
        }
    }
</script>
</@layout.page>