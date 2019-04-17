<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='通用课程实验报告' page_num=25>
<link href="${requestContext.webAppContextPath}/public/css/select2/select2.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>

<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/select2/select2.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>
<div id="main_container" class="span9">
    <legend>${preId}+${postId}+${courseId}|(${courseName})</legend>
    <h10>交互预览</h10>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>页面</td>
                <td>加载人数</td>
                <td>退出人数</td>
                <td>向前浏览人数</td>
                <td>向后浏览人数</td>
                <td>平均停留时长</td>
                <td>默认停留时长</td>
            </tr>
            <#if pageData?? && pageData?size gt 0>
                <#list pageData as e >
                    <tr>
                        <td>${e.page!}</td>
                        <td>${e.loadNum!}</td>
                        <td>${e.quitNum!}</td>
                        <td>${e.preNum!}</td>
                        <td>${e.postNum!}</td>
                        <td>${e.stayTimeAvg!}</td>
                        <td>${e.stayTimeDefult!}</td>
                    </tr>
                </#list>
            <#else >
                <tr>
                    <td colspan="7"><strong>暂无数据</strong></td>
                </tr>
            </#if>
        </table>
        <h10>QA页分析</h10>
        <table class="table table-striped table-bordered">
            <tr>
                <td>页面</td>
                <td>答题时长</td>
                <td>答案及占比</td>
            </tr>
            <#if courseAnswerList?? && courseAnswerList?size gt 0>
                <#list courseAnswerList as q >
                    <tr>
                        <#assign  index = 0 >
                        <#list q.answerList as answer >
                            <#assign index = index+1>
                            <#if index == 1>
                                <td rowspan="${q.answerSize!}">${q.page!}</td>
                                <td rowspan="${q.answerSize!}">${q.courseAnswerTimeAvg!}</td>
                            </#if>
                            <td>${answer.userAnswer!}</td>
                            <td>${answer.rate!}</td>
                            <td>${answer.result?c}</td>
                        </tr>
                        </#list>
                </#list>
            <#else >
                <tr>
                    <td colspan="7"><strong>暂无数据</strong></td>
                </tr>
            </#if>
        </table>
    </div>
</@layout_default.page>