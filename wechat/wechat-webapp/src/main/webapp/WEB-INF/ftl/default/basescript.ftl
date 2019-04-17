<#--是否使用min文件-->
<#assign jskid = "" />
<#if (!ProductDevelopment.isDevEnv())!false>
    <#assign jskid = ".min" />
</#if>

<#function buildRequirejsScriptUrl withOutExtUrl jskid>
    <#assign scriptUrl>
    <#--处理examCore.js-->
        <#if withOutExtUrl?index_of('/wechat/js/examCore') == -1 >
            <@app.link href=withOutExtUrl + jskid + '.js'/>
        <#else>
            <@app.link href=withOutExtUrl + jskid + '.js' cdnTypeFtl='skip'/>
        </#if>
    </#assign>

    <#if scriptUrl?index_of('?') == -1>
        <#return scriptUrl?replace(".js", "")>
    <#else>
        <#return scriptUrl>
    </#if>
</#function>

<#--基础库+单独配置的库-->
<#assign jsConfigModule = {
    <#--基础库-->
    "jquery" : {'url' : "public/lib/jquery/dist/jquery.min",<#--兼容遗留依赖jquery-->'useJsKid': false},
    "knockout" : {'url' : 'public/lib/knockout/dist/knockout','useJsKid': false},
    "komapping" : {'url' : 'public/lib/knockout.mapping/knockout-mapping','useJsKid': false},
    "$17" : {'url' : 'public/js/$17','useJsKid': true},
    "jbox" : {'url' : 'public/lib/jbox/Source/jBox','useJsKid': true},
    "logger" : {'url' : 'public/js/utils/logger','useJsKid': true}
} + singleJsConfigModule/>


<script type='text/javascript'>
    <#--注意：该模块压缩功能，该模块禁止使用'//'来注释-->
    <@compress single_line=true>
        requirejs.config({
            paths: {
            <#list jsConfigModule?keys as jsFile>
                <#if jsFile == 'examCore_new'>
                'examCore_new':
                    <#if ProductDevelopment.isDevEnv()>
                        "${scheme}://www.test.17zuoye.net/resources/apps/hwh5/exam/wechat/js/examCore"
                    <#else>
                        "${buildRequirejsScriptUrl("/resources/apps/hwh5/exam/wechat/js/examCore",jskid)}"
                    </#if>
                    <#if jsFile_has_next>,</#if>
                <#else>
                    '${jsFile}': "${buildRequirejsScriptUrl("${jsConfigModule[jsFile].url}", jsConfigModule[jsFile].useJsKid?string(jskid,''))}"<#if jsFile_has_next>,</#if>
                </#if>
            </#list>
            },
            shim   : {
                "jquery": {
                    exports: "jquery"
                },
                'jbox': {
                    deps: ['jquery']
                },
                "examCore" : {
                    deps : ['jquery','knockout']
                },
                "flexslider" : {
                    deps : ['jquery']
                },
                "radialIndicator":{
                    deps : ['jquery']
                },
                "weuijs":{
                    deps : ['jquery']
                }
            },
            urlArgs: <#if (ProductDevelopment.isDevEnv())!false>"bust=" + (new Date()).getTime()<#else>""</#if>
        });
    </@compress>

    (function () {
        /*组件自动加载*/
        require(['${pageJs}'], function () {});

        if ('addEventListener' in document) {
            document.addEventListener('DOMContentLoaded', function() {
                FastClick.attach(document.body);
            }, false);
        }
        console.info("white_screen_time: "+(pf_white_screen_time_end - pf_time_start) +"ms");
    })();
</script>
