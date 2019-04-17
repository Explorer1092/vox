<#if title?has_content>
<script type="text/javascript">
    document.title = "${title!''}";
</script>
</#if>

<#if requireFlag!false>
    <script src="${getVersionUrl('public/plugin/requirejs/require.2.1.9.min')}.js" type="text/javascript"></script>
<#--JSLoad-->
<script type="text/javascript">
    <#--配置JS模块包-->
    var paths = {
        'jquery' : "${getVersionUrl('public/plugin/jquery/jquery-1.7.1.min')}",
        'WebUploader' : "${getVersionUrl('public/plugin/YQuploader-1.0/lib/webuploader/webuploader.min')}",
        'weui' : "${getVersionUrl('public/plugin/weui/jquery-weui.min')}",
        'swiper' : "${getVersionUrl('public/plugin/weui/swiper.min')}",
        'template' : "${getVersionUrl('public/plugin/template.min')}",
        'knockout' : "${getVersionUrl('public/plugin/knockoutjs-3.3.0/knockout' + (getWebInfo("isDev")!false)?string('.debug', ''))}",
        'komapping' : "${getVersionUrl('public/plugin/knockoutjs-3.3.0/knockout.mapping-latest' + (getWebInfo("isDev")!false)?string('.debug', ''))}",
        'prompt' : "${getVersionUrl('public/plugin/jquery-impromptu/jquery-impromptu')}",
        'datetimepicker' : "${getVersionUrl('public/plugin/datetimepicker/bootstrap-datetimepicker.min')}",
        "jqform" : "${getVersionUrl('public/plugin/form/jquery-form.min')}",
        'paginator' : "${getVersionUrl('public/plugin/jqPaginator/jqPaginator.min')}",
        'clipboard': "${getVersionUrl('public/plugin/clipboard/clipboard.min')}",
        'echarts': "//cdn-cnc.17zuoye.cn/s17/lib/echarts/4.0.4/echarts.min",
        '$17': "${getVersionUrl('public/script/$17')}",
        "fancytree":"${getVersionUrl('public/plugin/fancytree/jquery.fancytree-all.min')}",
        "jquery-ui": "${getVersionUrl('public/plugin/jquery/jquery-ui-1.10.3.custom.min')}"
    };

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
        'prompt' : {deps : ['jquery', 'css!${getVersionUrl('public/plugin/jquery-impromptu/impromptu-atuo-ui')}']},
        'datetimepicker' : {deps : ['jquery', 'css!${getVersionUrl('public/plugin/datetimepicker/datetimepicker')}']},
        'paginator' : {deps : ['jquery']},
        'weui' : {deps : ['jquery', 'css!${getVersionUrl('public/plugin/weui/weui.min')}', 'css!${getVersionUrl('public/plugin/weui/jquery-weui.min')}']},
        "jqform" : {deps : ['jquery']},
        'swiper' : {deps : ['jquery', 'css!${getVersionUrl('public/plugin/weui/jquery-weui.min')}']},
        'jquery-ui' : {deps:['jquery']},
        "fancytree":{deps:['jquery-ui','css!${getVersionUrl('public/plugin/fancytree/skin-lion/ui.fancytree.min')}','css!${getVersionUrl('public/plugin/font-awesome/css/font-awesome.min')}']}
    };

    pageExtend(shimPaths, requireShimPaths);

    requirejs.config({
        paths : paths,
        map : {
            '*' : {
                css : "${getVersionUrl('/public/plugin/require-css/css.min')}.js"
            }
        },
        shim : shimPaths,
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
