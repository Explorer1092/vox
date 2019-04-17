<#import './layout.ftl' as layout>

    <@layout.page title="最新作业线" className='Homework' pageJs="homework" globalJs = ["DateExtend"]>

    <#assign  url = '/parentMobile/homework/doLoadhomeworks.vpage?sid=${sid!""}&tid=${tid!""}&subject=' subject = subject!"">

    <#assign tabInfo = [
        {
            "name" : '英语作业',
            "subject" : 'ENGLISH',
            "data" : {
                "ajaxUrl"  : '${url}ENGLISH',
                "tabTargetEl" : '#tabContent',
                "tabTemplateEl" : '#homeworkline'
            }
        },
        {
            "name" : '数学作业',
            "subject" : 'MATH',
            "data" : {
                "ajaxUrl"  : '${url}MATH',
                "tabTargetEl" : '#tabContent',
                "tabTemplateEl" : '#homeworkline'
            }
        }
    ]>

    <#if subject == "MATH">
        <#assign tabInfo = tabInfo?reverse>
    </#if>
    <#assign topType = "topTab">
    <#assign topTabHtml>
        <#list tabInfo as tab >
            <a
                href = "javascript:;"
                class="doTab <#if tab_index == 0 >active</#if>"
                data-tab_ajax_url = "${tab.data.ajaxUrl!''}"
                data-tab_template_el = "${tab.data.tabTemplateEl!''}"
                data-tab_target_el = "${tab.data.tabTargetEl!''}"
                data-tab_local = "${tab.data.tabLocal!''}"
                >${tab.name}
            </a>
        </#list>
    </#assign>
    <div class="doTabBlock">
        <#include "./top.ftl" >
        <div id="tabContent">
        </div>
    </div>

    <script id="homeworkline" type="text/html">

        <% if(!success){ %>
            <%= info %>
            <%= errorCode %>

        <%
                return ;
            }
        %>

        <% if(homeworks.length === 0){ %>
            <#assign tipType = "card">
            <#assign tipText = "老师暂时还没布置作业哦">
            <#include "./tip.ftl" >
        <% }else{%>
            <div class="parentApp-messageTab">
                <div class="tabText">今天有<%= todayTotalHomeworkCount %>份<%= subject %>作业，待完成<%= todayTotalHomeworkCount - todayFinishHomeworkCount %>份</div>
            </div>
            <div class="parentApp-workList">
                <div class="workLine"></div>
                <%
                    var alreadyFindFirstHomeWork = false;
                    homeworks.forEach(function(homework, index){
                        <#-- TODO 最新的定义：only the firsh homework in homework_lines -->
                        var isQUIZ = homework.homeworkLocation.homeworkType.substring(0,5) === "QUIZ_",
                            CanShowSendFlow = !isQUIZ && !alreadyFindFirstHomeWork && (alreadyFindFirstHomeWork = true);
                %>
                    <div>
                        <% var formatCreateTime =  window.PM.formatDate('yyyy-MM-dd', new window.Date(homework.createTime) ) %>
                        <div class="workHd"><span class="hdDot"></span><%= formatCreateTime %></div>
                        <div class="workMain">
                            <%
                                if( homework.finished){
                                var homeworkType = homework.homeworkLocation.homeworkType||'',
                                    track_type = homeworkType === "ENGLISH" ? "en" : "math";
                            %>
                                <div class="mainTextBox">
                                    <a
                                        data-ct="<%= formatCreateTime %>"
                                        data-track = "hwdetail|<%= track_type %>_open"
                                        href="/parentMobile/homework/homeworkdetail.vpage?ct=<%= formatCreateTime %>&sid=<%=homework.studentId%>&wc=<%=homework.wrongCount || ''%>&hid=<%=homework.homeworkLocation.homeworkId%>&ht=<%=homeworkType%>"
                                        class="doTrack textHd">

                                        <em class="ico"></em>
                                        <span class="hdState hdState-over">已完成</span>
                                        <% if( homework.wrongCount){ %>
                                            <span class="hdFt">出现<em><%=homework.wrongCount%></em>道错题</span>
                                        <%}%>
                                        <span class="hdFt hdFt-view">查看详情</span>
                                    </a>
                                    <% if(isQUIZ){ %>
                                        <div class="textFt">
                                            <span class="ftText">测验已完成</span>
                                        </div>
                                    <% }else if(CanShowSendFlow){ %>
                                        <div class="textFt">
                                            <% if(homework.sentFlag){ %>
                                                <span class="ftText ftRose">我
                                                    <% if( homework.flowerCount > 1){ %>
                                                    和<em><%= homework.flowerCount - 1 %></em>位家长
                                                    <% } %>
                                                    已送花
                                                </span>
                                            <% }else{ %>
                                                <span class="ftText ftRose">已有<em><%=homework.flowerCount%></em>位家长送花</span>
                                                <a href="javascript:;" class="ftBtn doSendflower"
                                                   data-send_flower_sid="<%=homework.studentId||''%>"
                                                   data-send_flower_tid="<%=homework.teacherId||''%>"
                                                   data-send_flower_hid="<%=homework.homeworkLocation.homeworkId||''%>"
                                                   data-send_flower_htype="<%=homeworkType%>"
                                                        >我要送花</a>
                                            <% } %>
                                        </div>
                                    <% }%>
                                </div>
                            <% }else{ %>
                                <% var endTime = homework.endTime; %>

                                <%if(
                                    (endTime && (new window.Date() - new window.Date(endTime) > 0 ))
                                    ||
                                    homework.checked
                                ){%>
                                    <div class="mainTextBox">
                                        <div class="textHd">
                                            <span class="hdState">未完成</span>
                                            <span class="hdFt">请到历史作业中补做</span>
                                        </div>
                                    </div>
                                <%}else{%>
                                    <div class="mainTextBox">
                                        <div class="textHd">
                                            <span class="hdState">待完成</span>
                                            <% if( endTime){ %>
                                            <span class="hdFt">作业已布置，请于<%= window.PM.formatDate('M月d号', new window.Date(endTime) ) %>完成</span>
                                            <% } %>
                                        </div>
                                        <% if( homework.homeworkLocation.homeworkType.length > 5 && homework.homeworkLocation.homeworkType.substring(0,5) == "QUIZ_") { %>
                                        <div class="textFt">
                                            <span class="ftText">测验未完成</span>
                                        </div>
                                        <% }else if(CanShowSendFlow){ %>
                                        <div class="textFt">
                                            <span class="ftText ftRose">已有<em><%=homework.flowerCount%></em>位家长送花</span>
                                            <a href="javascript:;" class="ftBtn doSendflower" data-send_flower_not_finised = "1" >我要送花</a>
                                        </div>
                                        <% }%>
                                    </div>
                                <%}%>

                            <% } %>
                        </div>
                    </div>
                <% }); %>
            </div>
        <% }%>
    </script>
</@layout.page>

