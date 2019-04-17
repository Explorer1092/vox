<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="统考测评" pageJs="workflow_process" navBar="hidden">
    <@sugar.capsule css=['audit']/>
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
                        <p class="reason"><span style="color:#ff7d5a">考试类型：</span>
                            <#if item.apply.testPaperType?? && item.apply.testPaperType == 'ORALLANGUAGE'>口语考试
                            <#elseif item.apply.testPaperType?? && item.apply.testPaperType == 'UNORALLANGUAGE'>非口语考试
                            <#elseif item.apply.testPaperType?? && item.apply.testPaperType == 'COMPREHENSIVEEXAMINATION'>口语+非口语综合考试
                            </#if>
                        </p>
                        <p class="reason"><span style="color:#ff7d5a">年级：</span>${item.apply.gradeLevel!''}年级</p>
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
                        <p class="reason"><span style="color:#ff7d5a">试卷总分：</span>${item.apply.score!''}分</p>
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
                <div class="c-opts gap-line c-flex c-flex-2" style="position:absolute;bottom:0;background: #fff">
                    <span class="js-submit" data-result="2">驳回</span>
                    <span class="js-submit" data-result="1">同意</span>
                </div>
            </#if>
            <div class="schoolParticular-pop submitBox" id="repatePane" style="display: none;">
                <div class="inner">
                    <h1></h1>
                    <p class="info">是否确认？</p>
                    <div class="btn">
                        <a href="javascript:void(0);" class="white_btn">否</a>
                        <a href="javascript:void(0);" class="submitBtn">是</a>
                    </div>
                </div>
            </div>
            <div class="apply_pop submitBox" style="display: none">
                <div class="inner">
                    <div class="apply_info">填写驳回原因：</div>
                    <div class="apply_text">
                        <textarea></textarea>
                    </div>
                    <div class="apply_btn">
                        <a href="javascript:void(0);" class="btn white_btn">取消</a>
                        <a href="javascript:void(0);" class="btn orange_btn submitBtn">确认</a>
                    </div>
                </div>
            </div>
        </div>
    <#--已通过-->
    </div>
</div>
<script>
    var workflowId =<#if applyData?? && applyData.apply??> ${applyData.apply.workflowId!0}<#else>0</#if>;
    var applyType =<#if applyType??> '${applyType!""}'<#else>""</#if>;
</script>
</@layout.page>