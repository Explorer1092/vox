<#include "./layout.ftl">

<@layout.page className=CONFIG.CSSPRE + 'reward_list' title="介绍学校" headBlock=headBlock bottomBlock=bottomBlock viewportWidth='595'>
    <#escape x as x?html>
        <#if result.success>
            <div class="dataCollect-wrapper">

                <#if record?exists >
                    <#assign unAnswerQuestions = record.unAnswerQuestions![]>

                    <input type="hidden" name="recordId" value="${record.recordId!''}">
                    <div class="dataCollect-banner"><img src="${buildStaticFilePath(CONFIG.CSS_BASE_PATH + "../images/dataCollect-banner.png", "img", true)}" alt=""></div>
                    <#list record.unAnswerQuestions as question>
                        <div class="dataCollect-head do_questions" data-pid="${question.questionId!''}">
                        ${question_index+1}. <#noescape> ${buildHtmlByQuestion(question, question_index+1)} </#noescape>
                        </div>
                    </#list>
                    <div class="dataCollect-footSubmitBox">
                        <div class="dataCollect-footSubmitInner"><a href="javascript:;" class="doSubmit btnSubmit">立即提交</a></div>
                    </div>
                    <div class="dataCollect-banner" style="height: auto;"><img src="${buildStaticFilePath(CONFIG.CSS_BASE_PATH + "../images/green_integral.jpg", "img", true)}" alt=""></div>
                <#else>
                    <div class="dataCollect-submitSuccess">问卷已提交，感谢反馈！</div>
                </#if>

            </div>
        <#else>
            ${result.info}
        </#if>
    </#escape>
</@layout.page>

