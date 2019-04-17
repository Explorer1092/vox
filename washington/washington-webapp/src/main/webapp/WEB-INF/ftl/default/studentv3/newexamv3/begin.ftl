<#import '../layout/layoutblank.ftl' as temp>
<@temp.page pageName="newexamv3" clazzName=''>
    <#if (ProductDevelopment.isDevEnv())!false>
        <#assign domain = "//www.test.17zuoye.net">
    <#else>
        <#assign domain = "${requestContext.webAppBaseUrl}">
    </#if>
<#--
<iframe class="vox17zuoyeIframe" style="width: 100%;height: 100%;"  src="${domain}/resources/apps/hwh5/funkyexam/V1_0_0/index.vhtml?server_type=<@ftlmacro.getCurrentProductDevelopment />&id=${id!}&imgDomain=<@app.link_shared href='' />"></iframe>
-->

<script type="text/javascript">
    var noTimeProtection = 'close';
    var env = <@ftlmacro.getCurrentProductDevelopment />;

    if(!!navigator.userAgent.match(/AppleWebKit.*Mobile.*/)){
        var goHome = function(){ location.href="/student/index.vpage"; return false; };
        $17.alert("请使用电脑打开浏览器或下载一起小学学生APP完成考试哦~", goHome, goHome);
    }else{
        setTimeout(function(){
            location.replace("${domain}/resources/apps/hwh5/funkyexam/V1_0_0/index.vhtml?server_type=" + env + "&id=${id!}&img_domain=<@app.link_shared href='' />");
        },1);
    }

</script>
</@temp.page>

