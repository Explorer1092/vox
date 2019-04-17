<#import '../layout.ftl' as layout>

<#assign extraRequireJs = [
    "public/script/parentMobile/activity/thanksGivingDay"
]>

<@layout.page className='parentMobileThanksGivingDay' pageJs="activityThanksGivingDay_isExtraRequireJs" title="感恩节" extraRequireJs = extraRequireJs>
    <#assign staticImgBaseUrl = "activity/thanksGivingDay/">

    <#include "../constants.ftl">
    ${buildLoadStaticFileTag("thanksGivingDay", "css")}

    <#escape x as x?html>
        <#assign topType = "topTitle">
        <#assign topTitle = "感恩节">
        <#include "../top.ftl" >
    </#escape>

    <div class="t-thanksGiving-box">

        <div class="banner"><img src="${buildStaticFilePath("${staticImgBaseUrl}thanksgiving-header.png", "img")}" alt="banner"/></div>
        <div class="banner"><img src="${buildStaticFilePath("${staticImgBaseUrl}thanksgiving-header-2.jpg", "img")}" alt="banner"/></div>
        <div class="list">
            <div class="title">
                <h1>达成目标</h1>
                <select class="int " id="doMyChilds" >
                    <option value=""></option>
                </select>
            </div>
            <h3>
				<#assign imgPre>
                    <@app.avatar href="avatar_normal.gif"/>
                </#assign>
                <#assign imgPre = imgPre?replace("avatar_normal.gif", "")>

                <img src="" data-img_pre = "${imgPre}" data-default_src="<@app.avatar href=""/>" id="doStudentImg" alt="头像"/>
                <span class="name" id="doStudentName">四喜</span>的今天目标：<span id="doTarget"></span>
            </h3>
            <div class="table-box" id="doClazzStudents">
            </div>
        </div>
        <div class="content">
            <h1>感谢老师</h1>
            <div class="column">
                <h2>已有 <span id="doThanksTeacherCount">16</span> 位家长感谢了老师 <i class="icon icon-1"></i></h2>
                <p class="info">感恩节，孩子们正在用实际行动感谢老师，家长们也来表达一下对老师的感恩吧！</p>
                <div class="btn"><a href="javascript:void(0);" class="btn-thanks doThankTeacher">感谢老师</a></div>
            </div>
            <#--
            <div class="column">
                <h2 style="line-height: 105px;">已有 <span id="doIntegralParentCount">16</span> 位家长贡献了 <span id="doIntegralCount">333</span> 个学豆<i class="icon icon-2"></i></h2>
                <p class="info" style="width:275px;padding:0 60px;margin:0 0 0 0;">你可以充实班级学豆，支持老师鼓励学生进步</p>
                <div class="btn" style="margin:10px 0 0 0;"><a href="javascript:;" class="btn-send doSendIntegr">给班级送学豆</a></div>
            </div>
            -->
        </div>
    </div>
<script type="text/html" id="doClazzStudentsTemp">
    <%
        if(isBindClazz){
            rankInfo = rankInfo || [];
            if(rankInfo.length === 0){
    %>
            <div class="parentApp-workBox parentApp-workBox-none">老师还没有奖励过学生哦</div>
        <% }else{ %>
        <table>
            <thead>
            <tr>
                <td style="width:33%">学生</td>
                <td style="width:33%">获得<i class="icon icon-3"></i></td>
                <td style="width:33%">获得<i class="icon icon-2"></i></td>
            </tr>
            </thead>
            <tbody>
            <% rankInfo.forEach(function(student){ %>
                <tr>
                    <td><span><%= (student.studentName || "") %></span></td>
                    <td><%= (student.chickenCount || 0) %></td>
                    <td><%= (student.coinCount || 0) %></td>
                </tr>
            <% }); %>
            </tbody>
        </table>
            <% } %>
    <% }else{ %>
        <div class="parentApp-workBox parentApp-workBox-none">还没绑定班级，快让孩子找老师绑定班级吧</div>
    <% } %>
</script>

</@layout.page>

