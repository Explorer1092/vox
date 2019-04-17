<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="选择学校" pageJs="chooseSchool" footerIndex=4>
<@sugar.capsule css=['new_base','school']/>
<div class="search">
    <input class="s-input" placeholder="请输入学校名称 / ID" id="schoolSearchInput"/>
    <span class="s-go js-search">搜索</span>
</div>
<div class="s-list" id="schoolContainer"></div>
<div id="loading" style="display:none;position: absolute;top: 3.5rem;left: 0;width: 100%;height: 100%;z-index: 10;color: rgb(234, 164, 164);font-size: 38px;">
    <img style='display:block;padding:2rem 0;margin:3.4rem auto;width:6rem;' src='/public/rebuildRes/image/mobile/res/loading.gif' alt="正在加载……"/>
</div>
<script id="schoolListTemp" type="text/html">
    <%for(var i = 0; i < schools.length; i++) {%>
    <%var rank=schools[i]%>
    <div class="item js-schoolItem" data-sid="<%=rank.schoolId %>" data-name="<%=rank.schoolName %>">
        <%=rank.schoolName %>
        <% if(rank.level == 1){ %>
            <span class="inner-right" style="color: #60b08e;">小学</span>
        <% } else if(rank.level == 2) { %>
            <span class="inner-right" style="color: #6fa2e0;">中学</span>
        <% } else if(rank.level == 4){ %>
            <span class="inner-right" style="color: #6fa2e0;">高中</span>
        <% } else if(rank.level == 5){ %>
        <span class="inner-right" style="color: #6fa2e0;">学前</span>
        <% } %>
    </div>
    <% } %>
</script>
<script>
var backUrl = "${backUrl!""}";
var choiceTeacherAble = "${choiceTeacherAble?c}";
$(document).ready(function () {
    var setTopBar = {
        show:true,
        rightText:'',
        needCallBack: false
    };
    setTopBarFn(setTopBar);
})
</script>
</@layout.page>