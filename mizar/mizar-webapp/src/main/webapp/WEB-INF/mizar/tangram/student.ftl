<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="学生评审页"
pageJs=["tangram"]
pageCssFile={"tangram" : ["public/skin/css/tangram/css/skin","public/skin/css/skin"]}
pageJsFile={"tangram" : "public/script/activity/tangram"}
>

<div class="qqb-header">
    <div class="inner">
        <a href="/" class="logo"></a>
        <span>七巧板作品评选</span>
        <a class="logout" href="javascript:;">退出登录</a>
    </div>
</div>
<div class="qqb-main">
    <div class="inner">
        <div class="uploadInfo">
            <div class="stuInfo">${student.schoolName!''}</div>
            <div class="stuInfo">
                <span class="label first">${student.studentCode!''}</span>
                <span class="label">四年级${student.className!''}班</span>
                <span class="label last">${student.studentName!''}</span>
            </div>
            <div class="picBox">
                <#if student.masterpiece1?? && student.masterpiece1?has_content>
                    <img class="JS-pic" src="${student.masterpiece1!''}">
                </#if>
                <#if student.masterpiece2?? && student.masterpiece2?has_content>
                    <img class="JS-pic" src="${student.masterpiece2!''}">
                </#if>
                <#if student.masterpiece3?? && student.masterpiece3?has_content>
                    <img class="JS-pic" src="${student.masterpiece3!''}">
                </#if>
            </div>
            <div class="reviewBox">
                <h3>评级</h3>
                <p class="gradeLabel">
                    <#if student.score?? && student.score?has_content>
                    <span <#if student.score == 'A'>class="active"</#if>>A</span>
                    <span <#if student.score == 'B'>class="active"</#if>>B</span>
                    <span <#if student.score == 'C'>class="active"</#if>>C</span>
                    <span <#if student.score == 'D'>class="active"</#if>>D</span>
                    <span <#if student.score == 'E'>class="active"</#if>>E</span>
                    </#if>
                </p>
                <h3>文字评价</h3>
                <div class="JS-inputNum"><span class="JS-isNumber"></span>/500</div>
                <textarea <#if student.comment?? && student.comment?has_content><#else> placeholder="评论在500字以内"</#if> maxlength="500" class="JS-comment"><#if student.comment?? && student.comment?has_content>${student.comment}</#if></textarea>
            </div>
            <a href="javascript:void(0)" class="submitBtn">提交</a>
        </div>
    </div>
</div>
<!--作品预览浮层-->
<div class="picReview-pop" style="display:none;">
    <div class="reviewInner">
        <div class="arrow arrowL"></div>
        <div class="arrow arrowR"></div>
        <div class="arrow arrowB" style="display:block"></div>
        <div class="scrollWrap">
            <ul>
                <li><img class="JS-reviewImg" src=""></li>
            </ul>
        </div>
    </div>
</div>
<!--弹窗1-->
<div class="commonPopup JS-success" style="display: none">
    <div class="popInner">
        <div class="p-close"></div>
        <div class="txtBox">已成功提交评审结果！</div>
        <div class="btnBox">
            <a href="javascript:void(0)" class="p-btn JS-goList disabled">返回</a>
            <a href="javascript:void(0)" class="p-btn JS-next">下一个</a>
        </div>
    </div>
</div>
<!--弹窗2-->
<div class="commonPopup JS-successEnd" style="display: none">
    <div class="popInner">
        <div class="p-close"></div>
        <div class="txtBox">当前学校学生作品已全部评审完毕</div>
        <div class="btnBox">
            <a href="javascript:void(0)" class="p-btn JS-goList">确定</a>
        </div>
    </div>
</div>
<script>
    var picBox = [];
    <#if student.masterpiece1?? && student.masterpiece1?has_content>
    picBox.push('${student.masterpiece1!''}');
    </#if>
    <#if student.masterpiece2?? && student.masterpiece2?has_content>
    picBox.push('${student.masterpiece2!''}');
    </#if>
    <#if student.masterpiece3?? && student.masterpiece3?has_content>
    picBox.push('${student.masterpiece3!''}');
    </#if>

    var schoolId = '${student.schoolId!''}';
</script>
</@layout.page>