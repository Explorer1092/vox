<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="选择学校" pageJs="schoolChose" footerIndex=4>
    <@sugar.capsule css=['new_base','school']/>
<div class="search">
    <input class="s-input" placeholder="请输入学校名称 / ID" id="schoolSearchInput"/>
    <span class="s-go js-search">搜索</span>
</div>
<div class="s-list" id="schoolContainer">
    <#if schoolCardList?? && schoolCardList?size gt 0>
        <#list schoolCardList as list >
            <div class="item js-schoolItem" data-sid="${list.schoolId!0}">
                ${list.fullName!""}
                <#if list.schoolLevel??>
                    <#if list.schoolLevel == "JUNIOR"><span class="inner-right" style="color: #60b08e;">小学</span>
                    <#elseif list.schoolLevel == "MIDDLE"><span class="inner-right" style="color: #60b08e;">中学</span>
                    <#elseif list.schoolLevel == "HIGH"><span class="inner-right" style="color: #60b08e;">高中</span>
                    <#elseif list.schoolLevel == "INFANT"><span class="inner-right" style="color: #60b08e;">学前</span></#if>
                    </#if>
            </div>
        </#list>
    </#if>
</div>

<script id="schoolListTemp" type="text/html">
    <%for(var i = 0; i < data.length; i++) {%>
    <%var rank=data[i]%>
    <div class="item js-schoolItem" data-sid="<%=rank.schoolId %>">
        <%=rank.schoolName %>
        <% if(rank.schoolLevel == "JUNIOR"){ %>
        <span class="inner-right" style="color: #60b08e;">小学</span>
        <% } else if(rank.schoolLevel == "MIDDLE") { %>
        <span class="inner-right" style="color: #60b08e;">中学</span>
        <% } else if(rank.schoolLevel == "HIGH"){ %>
        <span class="inner-right" style="color: #60b08e;">高中</span>
        <% } else if(rank.schoolLevel == "INFANT"){ %>
        <span class="inner-right" style="color: #60b08e;">学前</span>
        <% } %>
    </div>
    <% } %>
</script>

</@layout.page>