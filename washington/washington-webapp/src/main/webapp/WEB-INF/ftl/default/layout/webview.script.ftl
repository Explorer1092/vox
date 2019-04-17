<#if title?has_content>
<script type="text/javascript">
    document.title = "${title!''}";
</script>
</#if>

<#include "webview.include.ftl"/>
<#--是否使用Min文件-->
<#--<#if fastClickFlag!false>
    <@app.script href="public/plugin/fastClick.js"/>
<script type="text/javascript">
    if ('addEventListener' in document) {
        document.addEventListener('DOMContentLoaded', function() {
            FastClick.attach(document.body);
        }, false);
    }
</script>-->
<#--</#if>-->
<#if requireFlag!false>
    <@app.script href="public/plugin/requirejs/require.2.1.9.min.js"/>

    <#--JSLoad-->
    <script type="text/javascript">
        <#--配置JS模块包-->
        var protocol = location.protocol,wxUrl;
        if (protocol == "https"){
            wxUrl = '${getVersionUrl('public/plugin/weixin/jweixin-1.0.0ssl')}';
        }else{
            wxUrl = '${getVersionUrl('public/plugin/weixin/jweixin-1.0.0')}';
        }
        var paths = {
            'jquery' : "${getVersionUrl('public/plugin/jquery/jquery-1.7.1.min')}",
            'vue' : "${getVersionUrl('public/plugin/vue/2.1.6/vue' + (getWebInfo("isDev")!false)?string('.debug', '.min'))}",
            'mock' : "${getVersionUrl('public/plugin/mock/mock.min')}",
            'WebUploader' : "${getVersionUrl('public/plugin/YQuploader-1.0/lib/webuploader/webuploader.min')}",
            'weui' : "${getVersionUrl('public/plugin/jquery-weui/jquery-weui', '.js')}",
            '$17' : "${getVersionUrl('public/script/$17', '.js')}",
            'jquery.cookie' : "${getVersionUrl('public/script/project/jquery.cookie', '.js')}",
            'jquery.form' : "${getVersionUrl('public/plugin/jquery-form/jquery-form', '.js')}",
            'utils' : "${getVersionUrl('public/plugin/jquery-utils/jquery-utils', '.js')}",
            'json2' : "${getVersionUrl('public/plugin/json2', '.js')}",
            'template' : "${getVersionUrl('public/plugin/template', '.js')}",
            'knockout' : "${getVersionUrl('public/plugin/knockoutjs-3.3.0/knockout' + (getWebInfo("isDev")!false)?string('.debug', ''))}",
            'komapping' : "${getVersionUrl('public/plugin/knockoutjs-3.3.0/knockout.mapping-latest' + (getWebInfo("isDev")!false)?string('.debug', ''))}",
            'knockout-switch-case' : "${getVersionUrl('public/plugin/knockout-switch-case/knockout-switch-case.min')}",
            'underscore' : "${getVersionUrl('public/plugin/underscore1.8.2/underscore-min')}",
            'flexSlider' : "${getVersionUrl('public/plugin/jquery.flexslider/jquery.flexslider-min')}",
            'echarts' : "${getVersionUrl('public/plugin/echarts-4.1.0/echarts.min')}",
            'echarts-4.2.0' : "${getVersionUrl('public/plugin/echarts-4.2.0/echarts.common.min')}",
            'screenfull' : "${getVersionUrl('public/plugin/screenfull/screenfull')}",
            'echarts-adminteacher' : "${getVersionUrl('public/plugin/echarts-4.1.0/theme/adminteacher-walden-v2')}",
            'echarts-report' : "${getVersionUrl('public/plugin/echarts-4.2.0/theme/report')}",
            'html2canvas' : "${getVersionUrl('public/plugin/html2canvas/html2canvas.min')}",
            'fastClick' : "${getVersionUrl('public/plugin/fastClick', '.js')}",
            'impromptu' : "${getVersionUrl('public/plugin/jquery-impromptu/jquery-impromptu', '.js')}",
            'openApp' : "${getVersionUrl('public/script/mobile/common/web_open_app', '.js')}",
            'voxSpread' : "${getVersionUrl('public/script/voxSpread', '.js')}",
            'YQ' : "${getVersionUrl('public/script/YQ', '.js')}",
            'knockoutscroll' : "${getVersionUrl('public/script/mobile/mizar/knockoutscroll', '.js')}",
            'versionCompare' : "${getVersionUrl('public/script/parentMobile/versionCompare', '.js')}",
            'radialIndicator' : "${getVersionUrl('public/plugin/radialIndicator/radialIndicator.min')}",
            'LazyLoad' : "${getVersionUrl('public/skin/mobile/student/app/activity/dns/js/lazyload.min')}",
            'jqPaginator' : "${getVersionUrl('public/plugin/jquery-paginator/jqPaginator.min')}",
            'jqPaging' : "${getVersionUrl('public/plugin/jquery-paging/paging')}",
            'qrcode' : "${getVersionUrl('public/plugin/jquery-qrcode/jquery.qrcode.min')}",
            'jqueryDatePickerElementUI': "${getVersionUrl('public/plugin/jquery-datepicker-elementui/datepicker.all')}",
            'moment': "${getVersionUrl('public/plugin/momentjs-2.10.6/moment.min')}",
            'external': "https://cdn-cnc.17zuoye.cn/s17/??lib/shanks/2.0.9/seed.min.js?date",
            'voxLogs' : "${getVersionUrl('public/script/voxLogs', '.js')}",
            'weChat'    : wxUrl,
            /*css base link*/
            'weuiCss': '${getVersionUrl('public/plugin/jquery-weui/weui.min')}',
            'weuiJqueryCss': '${getVersionUrl('public/plugin/jquery-weui/jquery-weui.min')}'
        };

        paths.radialIndicator = "${getVersionUrl('public/plugin/radialIndicator/radialIndicator.min')}";
        paths.stellar = "${getVersionUrl('public/plugin/jquery.stellar.min')}";

        paths.datepicker = "${getVersionUrl('public/plugin/jquery-datepicker/jquery.datepicker')}";

    <#--页面配置需启动JS-->
    <#if pageJsFile??>
        <#list pageJsFile?keys as file>
        paths["${file}"] = "${getVersionUrl(pageJsFile[file], '.js')}";
        </#list>
    </#if>

        pageExtend(paths, requirePaths);

        <#--依赖JS或CSS-->
         var shimPaths = {
             'jquery': {
                 exports: "jquery"
             },
             '$17' : {deps: ['jquery', 'utils', 'json2']},
             'radialIndicator':{deps: ['jquery']},
             'utils' : {deps: ['jquery']},
             'voxSpread' : {deps: ['jquery']},
             'jqPaginator' : {deps: ['jquery']},
             'jqPaging' : {deps: ['jquery']},
             'qrcode' : {deps: ['jquery']},
             'jqueryDatePickerElementUI' : {
                 deps: ['jquery'],
                 init: function(){
                     require(['css!${getVersionUrl('public/plugin/jquery-datepicker-elementui/css/datepicker.css')}']);
                 }
             },
             'flexSlider' : {
                 deps: ['jquery'],
                 init: function(){
                    require(['css!${getVersionUrl('public/plugin/jquery.flexslider/flexslider.css')}']);
                 }
             },
             'openApp' : {deps: ['jquery']},
             'stellar' : {deps: ['jquery']},
             'datepicker' : {deps: ['jquery'],
                 init: function(){
                    require(['css!${getVersionUrl('public/plugin/jquery-datepicker/css/datepicker.blue.css')}']);
                 }
             },
             'impromptu' : {
                 deps : ['jquery'],
                 init: function(){
                    require(['css!${getVersionUrl('public/plugin/jquery-impromptu/impromptu-atuo-ui.css')}']);
                 }
             },
             'weui' : {
                 deps : ['jquery'],
                 init: function(){
                    require(['css!weuiCss', 'css!weuiJqueryCss']);
                 }
             }
         };

        pageExtend(shimPaths, requireShimPaths);

        requirejs.config({
            paths : paths,
            map : {
                '*' : {
                    css : "${getVersionUrl('public/plugin/require-css/css.min.js')}"
                }
            },
            shim : shimPaths,
            waitSeconds : 0,
            urlArgs: <#if (ProductDevelopment.isDevEnv())!false>"bust=" + (new Date()).getTime()<#else>""</#if>
        });

        <#--组件自动加载-->
        var pageJs = [${(getPageJs(pageJs))!}];

        Array.prototype.push.apply(pageRunJs, pageJs);

        require(pageRunJs, signRunScript);

        function pageExtend(child, parent){
            var $key;
            for($key in parent){
                if(parent.hasOwnProperty($key)){
                    child[$key] = parent[$key];
                }
            }
        }
    </script>
</#if>
