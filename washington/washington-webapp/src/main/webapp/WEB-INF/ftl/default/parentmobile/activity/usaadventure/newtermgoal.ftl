<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page title="走遍美国" pageCssFile={"travelAmerica":["public/skin/project/travelAmerica/css/travelAmerica"]}>
<div class="ta-header"></div>
<div class="ta-main">
    <div class="main01">
        <div class="title"><i class="star-icon"></i><span>您的孩子 ${userName!''} <#if learningGoalWordsNum != 0>
            设立了新学期的目标<#else>还未设置目标</#if></span></div>
        <div class="count"><span class="num">${learningGoalWordsNum!0}</span>个单词</div>
    </div>
    <div class="main02">
        <div class="title"><i class="star-icon"></i><span>如何帮助孩子达成目标：</span></div>
        <div class="pic01 pic"></div>
        <div class="pic02 pic"></div>
        <div class="pic03 pic"></div>
    </div>
</div>
<div class="ta-footer">
    <div class="inner">
        <a href="/parentMobile/ucenter/shoppinginfo.vpage?sid=${sid!0}&productType=UsaAdventure" class="btn">
            <#if orderStatus?? && orderStatus == "using">立即续费
            <#else>立即开通</#if>
        </a>
    </div>
</div>
</@layout.page>