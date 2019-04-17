<#if data.makeUpHomeworkCards?has_content>
    <#list data.makeUpHomeworkCards as m>
        <li id="makeuphomework" class="practice-block stepNoviceTwoBox">
            <div class="practice-content">
                <#switch m.homeworkType>
                    <#case 'ENGLISH'>
                        <h4><span class="w-discipline-tag w-discipline-tag-1">英语作业</span></h4>
                        <#assign subjectType='english'>
                        <#break>
                    <#case 'MATH'>
                        <h4><span class="w-discipline-tag w-discipline-tag-2">数学作业</span></h4>
                        <#assign subjectType='math'>
                        <#break>
                    <#case 'CHINESE'>
                        <h4><span class="w-discipline-tag w-discipline-tag-3">语文作业</span></h4>
                        <#assign subjectType='chinese'>
                        <#break>
                </#switch>
                <div class="no-content">
                    <p class="n-1"><span class="w-icon w-icon-10"></span></p>
                    <p class="n-2"><strong>有一份可以补做的作业!</strong></p>
                </div>
                <div class="pc-btn">
                    <a onclick="$17.atongji('首页-学习任务卡片-补做','/student/homework/index.vpage?from=indexCard&homeworkId=${m.homeworkId!''}');" href="javascript:void (0);" class="w-btn w-btn-blue">去补做</a>
                </div>
            </div>
        </li>
    </#list>
</#if>