<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="提示"
pageCssFile={"init" : ["public/skin/mobile/student/app/css/skin"]}
>
<#if result?has_content>
<div class="w-layer-confirm">
    <div class="text center">${(result.info)!}</div>
    <div class="w-footer no-fixed">
        <div class="inner">
            <div class="btn-box">
                <a href="javascript:;" onclick="blackHome();" class="btn">返回首页</a>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function blackHome(){
        if(window.external && ('jumpMainTab' in window.external)){
            window.external.jumpMainTab(JSON.stringify({'type': 'baby_tab'}));
        }
    }
</script>
</#if>
</@layout.page>