<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-grayf0"
title=""
pageJs=["init"]
pageJsFile={"init" : "public/script/mobile/student/studentmobilev3"}
pageCssFile={"init" : ["public/skin/mobile/student/app/css/skin"]}
>
<style>
    .go_achieve{
        position:absolute;
        background:#fdca2f;
        border-radius:20px;
        right:0.8rem;
        width:3.5rem;
        height:0.8rem;
        font-size:0.6rem;
        text-align:center;
        line-height:0.8rem;
        padding:0.25rem;
        color:#976515;
    }
    .go_people{
        position:absolute;
        left:0.8rem;
        margin-top:2.2rem;
        color:#0081d1;
    }
</style>
<div class="homeworkList-box">
    <div class="banner banner-3">
        <div class="head">
            <div class="image" style="background: none;">
                <span class="acv-icon">
                    <i class="ac-text" data-bind="text: level">--</i>
                    <img src="" data-bind="attr:{ src: '<@app.link href="public/skin/common/images/achievement/"/>' + type + '.png'}" width="100%"/>
                </span>
            </div>
            <div style="color: #fff;">
                <!-- ko if: type == 'HuanXingShi' -->
                <p class="go_pon">唤醒成功次数达到<span data-bind="text: condition"></span>次</p>
                <!--/ko-->
                <!-- ko if: type == 'ZiXueChengCai' -->
                <p class="go_pon">自学天数达到<span data-bind="text: condition"></span>天</p>
                <!--/ko-->
                <!-- ko if: type == 'YouCuoBiJiu' -->
                <p class="go_pon">累计订正作业全部正确<span data-bind="text: condition"></span>次</p>
                <!--/ko-->
                <!-- ko if: type == 'XueYouSuoCheng' -->
                <p class="go_pon">作业成绩90分以上达到<span data-bind="text: condition"></span>次</p>
                <!--/ko-->
                <!-- ko if: type == 'YueDuDaKa' -->
                <p class="go_pon">累计60分以上绘本完成<span data-bind="text: condition"></span>个</p>
                <!--/ko-->
                <!-- ko if: type == 'JinHuaTong' -->
                <p class="go_pon">口语练习80分以上达到<span data-bind="text: condition"></span>次</p>
                <!--/ko-->
                <!-- ko if: type == 'ShenSuanZi' -->
                <p class="go_pon">累计正确题数达到<span data-bind="text: condition"></span>道</p>
                <!--/ko-->
                <!-- ko if: type == 'QinXueKuLian' -->
                <p class="go_pon">完成作业<span data-bind="text: condition"></span>次</p>
                <!--/ko-->
                <!-- ko if: type == 'ShiQueBuYi' -->
                <p class="go_pon">阿分题错题工厂答对题目达<span data-bind="text: condition"></span>道题</p>
                <!--/ko-->
                <!-- ko if: type == 'XingGuangCuiCan' -->
                <p class="go_pon">星星数量达到<span data-bind="text: condition"></span></p>
                <!--/ko-->
            </div>
        </div>
        <#--<a href="/view/mobile/student/clazz/raidersdetail?new_page=blank"><div class="rule">攻略</div></a>-->
        <b class="go_people info">共<span data-bind="text: count">-</span>人获得</b>
        <a class="go_achieve" href="/studentMobile/achievement/index.vpage"><b>前往成就馆</b></a>
    </div>
    <!-- ko if: database() -->
    <div data-bind="template: {name: main(), data: database()}">
        <div style="text-align: center; padding: 100px 0 0; ">数据加载中...</div>
    </div>
    <!-- /ko -->
</div>

<script type="text/html" id="T:PageNull">
    <#--<div class="w-layer-fixedCenter w-layer-onroad" data-bind="text: info">暂时还没有达人～111</div>-->
</script>

<script type="text/html" id="T:今日达人">
<div class="hl-list" style="margin-top:-0.4rem">
    <ul data-bind="foreach: $data">
        <li>
            <!-- ko if: '${(currentUser.id)!0}' != userId -->
            <div class="right">
                <a href="javascript:void(0);" class="btn-tight"  data-bind="click: $root.clickLike, text: likeCount ? likeCount : 0, css : {praised : (!liked)}"></a>
            </div>
            <!-- /ko -->
            <!-- ko if: '${(currentUser.id)!0}' == userId -->
            <div class="right">
                <a href="javascript:void(0);" class="btn-tight"  data-bind=" text: likeCount ? likeCount : 0, css : {praised : (liked)}"></a>
            </div>
            <!-- /ko -->
            <div class="left">

                <div class="ornFace" style="width:2.25rem;height:2.25rem; padding:0; margin-right:0.4rem;">
                    <!-- ko if: userImg -->
                    <img src="" data-bind="attr:{ src: '<@app.avatar href="/"/>' + userImg}">
                    <!--/ko-->
                    <!-- ko ifnot: userImg -->
                    <img src="" data-bind="attr:{ src: '<@app.avatar href=""/>'}">
                    <!--/ko-->
                </div>

                <div class="column">
                    <div class="name" data-bind="text: userName"></div>
                    <div class="icon-bean"><span data-bind="text: receiveDate"></span>获得</div>
                </div>
            </div>
        </li>
    </ul>
</div>
</script>

<#include '../block.ftl'/>
<script type="text/javascript">
    var initMode = "TalentMode";
</script>
</@layout.page>