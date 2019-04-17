<#escape y as y?html>

<#include "./constants.ftl">

<#assign sid = sid!"">

<#-- second tab -->
<script type="text/html" id="secondTabTemp">
<%
    var secondTabInfo =  secondTabInfo || [];
    if(secondTabInfo.length === 0){
        return new window.String("");
    }
%>

<div class="parentApp-messageTab" style="left : 100000px;">
    <% (secondTabInfo || []).forEach(function(tabInfo, index){ %>
        <div class="doTab <%= index === 0 ? "active" : "" %>"
            data-tab_ajax_url = "<%= tabInfo.ajaxUrl || "" %>"
            data-tab_template_el = "<%= tabInfo.tabTemplateEl || "" %>"
            data-tab_target_el = "<%= tabInfo.tabTargetEl || "" %>"
            data-tab_local = "<%= tabInfo.tabLocal || "" %>"
        >
        <%= tabInfo.text %>
        </div>
    <% }); %>
</div>

</script>


<#-- 错题列表 -->
<script type="text/html" id="errorReport">
    <% if(wrongList.length === 0){ %>
        <div class="parentApp-messageNull">暂时没内容，何不休息休息</div>
    <% }else{ %>

        <%
            subject = subject.toUpperCase();

            if(subject.search('CHINESE') === -1){
                var re_do = subject.search("ENGLISH") > -1 ?
                {
                    subject : "英语",
                    product_type : "AfentiExam"
                }: {
                    subject : "数学",
                    product_type : "AfentiMath"
                };
        %>
            <div class="parentApp-messageTab parentApp-messageBeans doRedoParent" style="background-color: #FFF; ">
                <div class="tabBtn doRedo doTrack" data-product_type="<%= re_do.product_type %>" ${buildTrackData("report|faultnotes_add_click")} data-is_vip="<%= isVip ? 1 : 0 %>">导入错题</div>
                <div class="tabText"><%= re_do.subject %>错题都可以在阿分题错题工厂中重新练习</div>
            </div>
        <% } %>

        <div class="parentApp-workList">
            <div class="workLine"></div>
            <%
                wrongList.forEach(function(dayInfo, index){
                    var date = dayInfo.date;
            %>
                <div>
                    <div class="workHd"><span class="hdDot"></span><%= date %></div>
                    <div class="workMain">
						<%
							[
                                <#-- TODO 这里有一个在oppo r817t 型号上的坑, 就是Object的key 必须用"包围  初步怀疑是因为artTemplate做的是JSON.stringify 或者是因为 -->
								{
									"class" : "homeworkMapList",
									"title" : "作业错题"
								}
							].forEach(function(key){
								(dayInfo[key.class]||[]).forEach(function(wrongInfo){
						%>
							<a href="/parentMobile/homework/wrongQuestionDetail.vpage?sid=${sid}&homeworkType=<%= wrongInfo.homeworkType %>&homeworkId=<%= wrongInfo.homeworkId %>" class="mainLinkBox">
								<span class="btnTitle"><%= key.title %></span>
								<span class="btnNum">共<%= wrongInfo.wrongCount %>题</span>
							</a>
						<%
							});
						});
						%>
                    </div>
                </div>
            <% }); %>
        </div>
    <% } %>
</script>



<#-- 敬请期待 -->
<script type="text/html" id="willFollow">
    <#assign tipType = "qinqin">
    <#assign tipText = "敬请期待">
    <#include "./tip.ftl" >
</script>

</#escape>
