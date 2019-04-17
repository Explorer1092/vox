<#--例：layout 1 -->
<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="run"
pageJs=['demo']
pageJsFile={"demo" : "public/script/project/demo"}
pageCssFile={"demo" : ["public/skin/project/afentijump/css/jumpdetails"]}
>
<div class="main">
    <div class="inner">welcome Web
        count : <input type="text" data-bind="textInput: count" value=""/>
        <span data-bind="text: count"></span>
        <input type="button" class="js-btn-openDialog" value="打开">
    </div>
</div>
<#--如果页面很简单，就可以用这种方式，单页实现JS效果-->
<script type="text/javascript">
    signRunScript = function () {
        //在html js实现执行, pageJs=['jquery', 'template'] 需要依赖的JS包，可以不设置pageJsFile 单页面js文件
    }
</script>
</@layout.page>

<#--
<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page>
<div class="main">
    <div class="inner">welcome Web
        count : <input type="text" data-bind="textInput: count" value=""/>
        <span data-bind="text: count"></span>
        <input type="button" class="js-btn-openDialog" value="打开">
    </div>
</div>
<script type="text/javascript">
    //可在当前页面配置调用JS and CSS 模块;
    requirePaths.demo = "${layout.getVersionUrl("public/script/project/demo", ".js")}";
    requireShimPaths.demo = ["css!${layout.getVersionUrl("public/skin/project/afentijump/css/jumpdetails", ".css")}"];
    pageRunJs.push("demo");
</script>
</@layout.page>-->
