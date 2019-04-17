<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="统考测评" pageJs="" footerIndex=4>
    <@sugar.capsule css=['audit']/>
<style>
    .total_score_box li{padding:10px 0;overflow:hidden}
    .total_score_box li .left{float:left;color:#636880;font-size:16px}
    #rankContainer{margin-left:100px}
    rankContainer .check{margin-right:20px}
    rankContainer .score{width:100px;border:1px solid #dde2ea;outline:0;color:#636880;font-size:14px}
    rankContainer .list{padding:0 0 18px 0}
    rankContainer .list input{border:1px solid #dde2ea;outline:0;color:#636880;font-size:14px}
    rankContainer .list .txt{width:100px}
    rankContainer .list .num,.total_score_box li .right .list .per{width:60px}
    rankContainer .list .add_btn,.total_score_box li .right .list .del_btn{padding:0 10px;display:inline-block;text-align:center;font-size:16px;color:#ff7d5a}
</style>
<div class="crmList-box resources-box">
    <div class="tab-main" style="clear:both">
    <#--待审核-->
        <div>
            <#if applyData?? && applyData.apply??>
                <#assign item = applyData>
                <div class="adjustmentExamine-box" style="margin-top: .5rem">
                    <div class="adjust-content">
                        <p class="title"><span style="color:#ff7d5a">考试名称：</span>${item.apply.unifiedExamName!''}</p>
                        <p class="area"><span style="color:#ff7d5a">是否重复使用已录入试卷：</span><#if item.apply.testPaperSourceType?has_content && item.apply.testPaperSourceType.type == 0>否</#if><#if item.apply.testPaperSourceType?has_content && item.apply.testPaperSourceType.type == 1>是</#if></p>
                        <p class="pattern"><span style="color:#ff7d5a">学科：</span>
                            <#if item.apply.subject?? && item.apply.subject == '101'>小学语文
                            <#elseif item.apply.subject?? && item.apply.subject == '102'>小学数学
                            <#elseif item.apply.subject?? && item.apply.subject == '103'>小学英语
                            <#elseif item.apply.subject?? && item.apply.subject == '201'>中学语文
                            <#elseif item.apply.subject?? && item.apply.subject == '202'>中学数学
                            <#elseif item.apply.subject?? && item.apply.subject == '203'>中学英语
                            </#if>
                        </p>
                        <p class="pattern"><span style="color:#ff7d5a">使用试卷ID：</span>
                            <#if item.apply.fetchPaperId()??>
                                <#list item.apply.fetchPaperId() as itemId>
                                    ${itemId!""}<#if itemId_has_next>、</#if>
                                </#list>
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">学生获取试卷方式：</span>
                            <#if item.apply.distribution??>
                                <#if item.apply.distribution == 0>
                                    轮流获取
                                <#else>
                                    完全随机获取
                                </#if>
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">考试场景：</span>
                            <#if item.apply.testScene?? && item.apply.testScene == 0>在线考试
                            <#elseif item.apply.testScene?? && item.apply.testScene == 1>集中考试
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">考试类型：</span>
                            <#if item.apply.fetchTestPaperType()?? && item.apply.fetchTestPaperType()?size gt 0 >

                            <#list item.apply.fetchTestPaperType() as paper>
                                <#if paper == "ORAL">口语考试
                                <#elseif paper == 'LISTENING'>非口语考试
                                <#elseif paper == 'NORMAL'>口语+非口语综合考试
                                </#if>
                            </#list>
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">年级：</span>
                            <#if item.apply.fetchGradeLevel()?has_content>
                            <#list item.apply.fetchGradeLevel() as level>
                                <#if level == 1>
                                    一年级 &nbsp
                                <#elseif level == 2>
                                    二年级 &nbsp
                                <#elseif level == 3>
                                    三年级 &nbsp
                                <#elseif level == 4>
                                    四年级 &nbsp
                                <#elseif level == 5>
                                    五年级 &nbsp
                                <#elseif level == 6>
                                    六年级 &nbsp
                                <#elseif level == 7>
                                    七年级 &nbsp
                                <#elseif level == 8>
                                    八年级 &nbsp
                                <#elseif level == 9>
                                    九年级 &nbsp
                                </#if>
                            </#list>
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">级别：</span>
                            <#if item.apply.regionLeve?? && item.apply.regionLeve == 'city'>市级
                            <#elseif item.apply.regionLeve?? && item.apply.regionLeve == 'country'>区级
                            <#elseif item.apply.regionLeve?? && item.apply.regionLeve == 'school'>校级
                            </#if>
                        </p>
                        <#--<p class="grade"><span style="color:#ff7d5a">使用教材：</span>${item.apply.bookCatalogName!''}</p>-->
                        <p class="reason"><span style="color:#ff7d5a">考试开始时间：</span>${item.apply.unifiedExamBeginTime!''}</p>
                        <p class="reason"><span style="color:#ff7d5a">考试结束时间：</span>${item.apply.unifiedExamEndTime!''}</p>
                        <p class="reason"><span style="color:#ff7d5a">老师批改截止时间：</span>${item.apply.correctingTestPaper!''}</p>
                        <p class="reason"><span style="color:#ff7d5a">成绩发布时间：</span>${item.apply.achievementReleaseTime!''}</p>
                        <p class="reason"><span style="color:#ff7d5a">考试开始后：</span>${item.apply.minSubmittedTestPaper!''}分钟后方能交卷</p>
                        <p class="reason"><span style="color:#ff7d5a">答题时长：</span>${item.apply.maxSubmittedTestPaper!''}</p>
                        <p class="reason"><span style="color:#ff7d5a">成绩分制：</span>
                            <#if item.apply.gradeType?? && item.apply.gradeType == 0>分数制
                            <#elseif item.apply.gradeType?? && item.apply.gradeType == 1>等第制
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">试卷总分：</span>${item.apply.score!''}分</p>
                        <#if item.apply.ranks??>
                        <p class="reason"><span style="color:#ff7d5a">等第设置：</span>
                        <div id="rankContainer" class="right"></div>
                        </p>
                        </#if>
                    </div>
                </div>
                <div class="adjustmentExamine-box" style="margin-top: .5rem">
                    <p style="font-size:.6rem;color:#898c91;line-height:1rem;height:1rem">审核进度</p>
                    <ul class="schoolClueContent">
                        <#if item.processResultList?? && item.processResultList?size gt 0>
                            <#list item.processResultList as list>
                                <li>
                                    <div>${list.accountName!""}</div>
                                    <div <#if list.result??>style="<#if list.result == "同意">color:#99cc66<#elseif list.result == "驳回" || list.result == "撤销">color:#ff7d5a</#if>"</#if>>${list.result!""}</div>
                                    <div><#if list.processDate??>${list.processDate?string("MM-dd HH:mm")}</#if></div>
                                </li>
                                <#if list.result?? && list.result == "驳回">
                                    <li style="color:#ff7d5a">${list.processNotes!""}</li>
                                </#if>
                            </#list>
                        </#if>
                        <li>
                            <div>${item.apply.accountName!''}</div>
                            <div>发起申请</div>
                            <div><#if item.apply.createDatetime?has_content>${item.apply.createDatetime?string("MM-dd HH:mm")}</#if></div>
                        </li>
                    </ul>
                </div>
            </#if>
        </div>
    <#--已通过-->
    </div>
</div>
<script type="text/html" id="ranksContainer">
    <div class="right">
        <%for(var i=0;i< res.length;i++){%>
        <div class="list">等第名称：
            <input type="text" class="txt" value="<%=res[i].rankName%>" disabled>
            <input type="text" class="num" value="<%=res[i].bottom%>" disabled>
            % <=分数区间<=
            <input type="text" class="per" value="<%=res[i].top%>" disabled>%
        </div>
        <#--<tr class="trd show">-->
            <#--<td>等第名称</td>-->
            <#--<td><%=res[i].rankName%></td>-->
            <#--<td><%=res[i].bottom%>%</td>-->
            <#--<td><=分数区间<</td>-->
            <#--<td><%=res[i].top%>%</td>-->
        <#--</tr>-->
        <%}%>
    </div>
</script>
<script>
    <#if applyData?? && applyData.apply.ranks?? && item.apply.ranks != "">
    var ranks = ${applyData.apply.ranks!""};
    $('#rankContainer').html(template("ranksContainer",{res:ranks}));
    </#if>
</script>
</@layout.page>