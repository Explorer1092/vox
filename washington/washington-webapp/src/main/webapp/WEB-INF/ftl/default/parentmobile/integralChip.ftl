<#import './layout.ftl' as layout>

<#assign title = (student_name!"您孩子") + "的学豆">

<@layout.page className='IntegralChip bg-fff' pageJs='second' title="${title}" specialCss="skin2" specialHead='
   	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
	<meta name="format-detection" content="telephone=no" />
	<meta name="format-detection" content="email=no" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>${title}</title>
'>

    <#escape x as x?html>

        <#assign topType = "topTitle">
        <#assign topTitle = "${title}">
        <#assign isUseNewTitle = true><#--使用新的UI2 title-->
        <script><#--改版的样式，不适用adapt-->
            window.notUseAdapt=true;
        </script>
        <#include "./top.ftl" >

        <#noescape>${buildAutoTrackTag("report|beanrecord_open", true)}</#noescape>


        <#if result.success>
            <div id="doIntegralInfo" data-current_page = "${currentPage!1}">
            </div>
            <script type="text/html" class="hide" id="doIntegralTemp">
                <%
                    var integrals = pagination.content;
                    if(integrals.length === 0){
                %>
                <div class="null-box">
                    <div class="no-record"></div>
                    <div class="null-text">还没有学豆纪录哦。</div>
                </div>
                <%
                    }else{

                %>
                <div class="expression-box">
                    <div class="e-head">
                        <#--<a href="javascript:void(0);" class="rule-btn J-doClick" data-tip_content=".doStarTip" data-operate="get-help-show">学豆规则</a>-->
                        <p class="tips"><span><%= integral %></span>学豆</p>
                    </div>
                    <div class="e-list">
                        <ul>
                            <% integrals.forEach(function(integral){ %>
                            <li>
                                <%
                                    var integralCount = +integral.integral,
                                        isIncrease = integralCount > 0,
                                        className = "blue",
                                        integralSymbol = "-";

                                    if(isIncrease){
                                        className = "orange",
                                        integralSymbol = "+";
                                    }
                                %>
                                <div class="right icon-bean <%= className %>"><%= integralSymbol + window.Math.abs(integralCount) %></div>
                                <div class="left">
                                    <div class="name"><%= integral.comment %></div>
                                    <div class="time"><%= integral.dateYmdString %></div>
                                </div>
                            </li>
                            <%});%>
                        </ul>
                        </div>
                        <div style="width:94%; margin:0 auto; text-align:center; padding-bottom:20px;">
                            <%
                                var pageBaseUrl = '/parentMobile/home/integralchip.vpage?sid=${sid}&pageIndex=',
                                    haveMore = pagination.totalPages > currentPage;

                                [
                                    currentPage>1 && ['上', currentPage - 1],
                                    haveMore && ['下', currentPage + 1]
                                ].filter(function(page){
                                    return   typeof(page) !== "boolean";
                                }).forEach(function(pageArr, index){
                            %>
                                <a style="color:#41bb54;font-size:15px;margin:0 40px;" href="javascript:void(0);" data-page = "<%= pageArr[1] %>" class="doPage ui-btn ui-btn-b ui-corner-all"><%= pageArr[0] %>一页</a>
                            <%
                                });
                            %>
                        </div>
                </div>
                <% } %>
            </script>

            <div class="popUp-box" style="display:none;" id="J-get-help-box">
                <div class="popInner" id="J-get-help-box-inner">
                    <div class="close J-doClick" data-operate="get-help-close"></div>
                    <div style="height: 100%;overflow-y: auto;">
                        <div class="title">如何获得学豆？</div>
                        <div class="content">
                            <P>1、按时完成老师布置的作业，可得1-10颗学豆奖励。 100分得10颗；20分以下无奖励；补做完成作业，且平均分超过60分，也可得1颗</P>
                            <P>2、老师奖励,老师可以用班级获得的学豆或家长贡献的学豆，奖励学生。每次颗数由老师决定</P>
                            <P>3、家长奖励,家长每月可免费获得10个学豆用于奖励学生，具体操作由家长在“家长奖励”中设置孩子在规定次数内完成的某项任务，完成任务后即可获得学豆。</P>
                            <P>4、家长通签到奖励,每月在家长通签到可获得奖励学豆：1位家长本月签到奖5学豆，2位不同身份家长本月签到奖20学豆</P>
                            <P>5、每月可在一起作业官方网站的通天塔PK中获得5-10颗学豆的奖励，且每月逐增</P>
                            <P>6、购买课外乐园内产品，可获得不等学豆
                            <p>7、老师的鲜花可转换为班级学豆，老师每月根据上月家长送花数可领取班级学豆用于奖励给本班学生</P>
                        </div>
                    </div>
                </div>
            </div>
            <#include "./what_is_integral_rule.ftl">
        <#else>
            <#noescape>${buildAutoTrackTag("report|beanrecord_error", true)}</#noescape>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">
        </#if>

    </#escape>

</@layout.page>
