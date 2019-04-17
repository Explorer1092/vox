<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='每日一课'
pageJs=["init"]
pageJsFile={"init" : "public/script/mobile/course/main"}
pageCssFile={"css" : ["public/skin/mobile/course/css/skin"]}
bodyClass='bg-f4'
>
<div data-bind="template: {name: templateBox(), data: database()}"></div>

<script type="text/html" id="T:loading">
    <div style="line-height: 150%; padding: 50px 0 ; text-align: center; color: #bbb; font-size: .625rem;">
        数据加载中...
    </div>
</script>

<script type="text/html" id="T:nullContent">
    <div style="line-height: 150%; padding: 50px 0 ; text-align: center; color: #bbb; font-size: .625rem;">
        <span data-bind="text: (info ? info : '还没有数据!')"></span>
    </div>
</script>
<script type="text/html" id="T:listContent">
    <div data-bind="foreach: rows">
        <div class="zx-list" data-bind="css: {'zx-txtPic' : background && background != ''}">
            <a href="" data-bind="attr: {href: redirectUrl != '' ? redirectUrl : 'javascript:;'}" class="innerBox">
                <!--ko if: background && background != ''-->
                <div class="pic">
                    <img src="" data-bind="attr: {src: background}"/>
                </div>
                <!--/ko-->
                <div class="section">
                    <div class="header" data-bind="text: title"></div>
                    <div class="article" data-bind="text: description"></div>
                    <div class="footer">
                        <div class="fl">
                            <!--ko if: readCount && readCount > 0 -->
                            <span style="display: inline-block; vertical-align: middle;"><span data-bind="text: readCount"></span> 阅读</span>
                            <!--/ko-->
                        </div>
                        <div class="fr">
                            <span data-bind="foreach: tags" style="display: inline-block; vertical-align: middle;"><i data-bind="text: $data"></i></span>
                        </div>
                    </div>
                </div>
            </a>
        </div>
    </div>
    <!-- ko if: $root.isShowLoading() -->
    <div class="weui-infinite-scroll" style="font-size: 0.5rem; color: #999;">
        <div class="infinite-preloader"></div>
        正在加载...
    </div>
    <!-- /ko -->
</script>

<script type="text/javascript">
    var initMode = "CourseListMode";
    var pageCategory = "${category!'DAY_COURSE'}";
</script>
</@layout.page>