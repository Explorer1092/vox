<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='亲子活动'
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
        <span data-bind="text: (info ? info : '还没有活动!')"></span>
    </div>
</script>
<script type="text/html" id="T:listContent">
    <div class="auditionGood-box">
        <div class="ipt-banner">
            <ul class="ipt-bannerImg" data-bind="foreach: rows">
            <!--ko if: status == 'ONLINE' && !soldOut -->
                <li>
                    <a href="" data-bind="attr: {href: redirectUrl != '' ? redirectUrl : 'javascript:;'}">
                        <!--ko if: background != ''-->
                            <img src="" data-bind="attr: {src: background}"/>
                            <em class="ipt-mask"></em>
                        <!--/ko-->
                        <div class="ipt-tag" data-bind="foreach: tags" style="overflow: hidden;"><span data-bind="text: $data, visible: $index() == 0" class="tagLabel"></span></div>
                        <div class="ipt-sideTitle" data-bind="text: title"></div>
                        <div class="ipt-sidebarTitle"><span class="info" data-bind="text: subTitle"></span></div>
                        <div class="ipt-explain" data-bind="text: description"></div>
                    </a>
                </li>
            <!--/ko-->
            </ul>

            <#--卖光了-->
            <ul class="ipt-bannerImg aug-Overdue" data-bind="foreach: rows">
                <!--ko if: status == 'ONLINE' && soldOut -->
                <li>
                    <a href="javascript:;">
                        <!--ko if: background != ''-->
                        <img src="" data-bind="attr: {src: background}"/>
                        <!--/ko-->
                        <div class="ipt-tag" data-bind="foreach: tags" style="overflow: hidden;"><span data-bind="text: $data" class="tagLabel"></span></div>

                        <div class="ipt-sideTitle" data-bind="text: title"></div>
                        <div class="ipt-sidebarTitle"><span class="info" data-bind="text: subTitle"></span></div>
                        <div class="ipt-explain" data-bind="text: subTitle"></div>
                        <em>已售罄</em>
                    </a>
                </li>
                <!--/ko-->
            </ul>

            <ul class="ipt-bannerImg aug-Overdue" data-bind="foreach: rows">
            <!--ko if: status == 'OFFLINE'-->
                <!--ko if: offLineTips-->
                <li style="margin: 0; height: auto;"><div class="aug-title" style="position: static;">往期活动</div></li>
                <!--/ko-->
                <li>
                    <a href="javascript:;">
                        <!--ko if: background != ''-->
                        <img src="" data-bind="attr: {src: background}"/>
                        <!--/ko-->
                        <div class="ipt-tag" data-bind="foreach: tags" style="overflow: hidden;"><span data-bind="text: $data" class="tagLabel"></span></div>

                        <div class="ipt-sideTitle" data-bind="text: title"></div>
                        <div class="ipt-sidebarTitle"><span class="info" data-bind="text: subTitle"></span></div>
                        <div class="ipt-explain" data-bind="text: subTitle"></div>
                        <em>已过期</em>
                    </a>
                </li>
            <!--/ko-->
            </ul>
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
    var pageCategory = "${category!'PARENTAL_ACTIVITY'}";
</script>
</@layout.page>