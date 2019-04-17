<#--基础库+单独配置的库-->
<#assign jsConfigModule = {
<#--基础库-->
"jquery" : {'url' : "/public/js/jquery-1.9.1.min"},
"handlebars" : {'url' : "/public/rebuildRes/lib/handlebar/handlebars-v4.0.5"},
"compileTemp" : {'url' : "/public/rebuildRes/common/compileTemp"}
} + singleJsConfigModule/>


<script type='text/javascript'>
    <#--注意：该模块压缩功能，该模块禁止使用'//'来注释-->
    <@compress single_line=true>
    requirejs.config({
        paths: {
            <#list jsConfigModule?keys as jsFile>
                '${jsFile}': "${jsConfigModule[jsFile].url}"<#if jsFile_has_next>,</#if>
            </#list>
        },
        shim   : {
            "jquery": {
                exports: "jquery"
            }
        },
        urlArgs: <#if (ProductDevelopment.isDevEnv())!false>"timestamp=" + (new Date()).getTime()<#else>""</#if>
    });
    </@compress>

    (function () {
        /*组件自动加载*/
        require(['${pageJs}'], function () {});
        var str= navigator.userAgent.toLowerCase();
        var ver=str.match(/cpu iphone os (.*?) like mac os/);
        if(!ver){//非IOS系统
            // 引入fastclick文件
            if ('addEventListener' in document) {
                document.addEventListener('DOMContentLoaded', function() {
                    FastClick.attach(document.body);
                }, false);
            }
        }else{
            var ver0 = ver[1].split('_')[0];
            var ver1 = ver[1].split('_')[1];
            if((ver0 >= 11 && ver1 >= 3) || ver0 >=12){
                //不必引入fastclick文件
            }else{
                // 引入fastclick文件
                if ('addEventListener' in document) {
                    document.addEventListener('DOMContentLoaded', function() {
                        FastClick.attach(document.body);
                    }, false);
                }
            }
        }


    })();
</script>
