
<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='好课试听'
pageJs=["init"]
pageJsFile={"init" : "public/script/mobile/course/main"}
pageCssFile={"css" : ["public/skin/mobile/course/css/skin"]}
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
    <div class="exqVideo-box">

        <div class="ipt-content" style="margin-top: 1rem;">
            <div data-bind="foreach: rows">
                <a href="" data-bind="attr: {href: redirectUrl != '' ? redirectUrl : 'javascript:;'}">
                    <!--ko if: background != ''-->
                    <div class="image">
                        <img src="" data-bind="attr: {src: background}"/>
                        <div data-bind="foreach: tags">
                            <!--ko if: $index() == 0 -->
                            <div class="state yellow" data-bind="text: $data" style="overflow: hidden;">--</div>
                            <!--/ko-->
                        </div>
                    </div>
                    <!--/ko-->
                    <div class="topic" data-bind="text: title"></div>
                    <div class="cont-side">
                        <div class="name"><span data-bind="text: keynoteSpeaker"></span></div>

                        <!--ko if: readCount && readCount > 0 -->
                        <div class="info"><span data-bind="text: readCount"></span>人观看</div>
                        <!--/ko-->
                    </div>
                </a>
            </div>
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
    var pageCategory = "${category!'GOOD_COURSE'}";
</script>
</@layout.page>