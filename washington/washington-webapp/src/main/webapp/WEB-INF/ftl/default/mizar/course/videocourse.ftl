<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='精品视频课程'
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
    <#--<div class="exqVideo-pop">
        <div class="inner">
            <ul>
                <li>
                    全  部
                </li>
                <li>
                    语  文
                </li>
                <li>
                    数  学
                </li>
                <li>
                    英  语
                </li>
            </ul>
        </div>
    </div>-->
    <div class="exqVideo-box">
        <#--<div class="exq-tab">
            <ul>
                <li>
                    <a href="javascript:void(0);">年龄段：四年级</a>
                </li>
                <li>
                    <a href="javascript:void(0);">分类：英语</a>
                </li>
            </ul>
        </div>-->
        <div class="ipt-content" style="margin-top: 1rem;">
            <div data-bind="foreach: rows">
                <a href="" data-bind="attr: {href: redirectUrl != '' ? redirectUrl : 'javascript:;'}">
                    <!--ko if: background != ''-->
                    <div class="image">
                        <img src="" data-bind="attr: {src: background}"/>

                        <div class="cont-tag">
                            <span>${(tag)!}</span>

                        </div>
                    </div>
                    <!--/ko-->
                    <div class="topic" data-bind="text: title"></div>
                    <div class="cont-side">
                        <div class="name"><span data-bind="text: keynoteSpeaker"></span> 主讲</div>

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
    var pageCategory = "${category!'VIDEO_COURSE'}";
</script>
</@layout.page>