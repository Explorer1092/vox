<#-- 报名考试卡片 -->
<#if (data.registrableNewExamCards?has_content && data.registrableNewExamCards?size gt 0)!false>
<#list data.registrableNewExamCards as registrableCard>
    <li class="practice-block stepNoviceTwoBox">
        <div class="practice-content">
            <h4>
                <#switch registrableCard.subject>
                    <#case 'ENGLISH'>
                        <span class="w-discipline-tag w-discipline-tag-1">英语测试</span>
                        <#break >
                    <#case 'MATH'>
                        <span class="w-discipline-tag w-discipline-tag-2">数学测试</span>
                        <#break >
                    <#case 'CHINESE'>
                        <span class="w-discipline-tag w-discipline-tag-3">语文测试</span>
                        <#break >
                    <#default>
                        <span class="w-discipline-tag w-discipline-tag-3">测试</span>
                        <#break >
                </#switch>
            </h4>
            <div class="no-content">
                <p class="n-1"><span class="w-icon w-clockTime-icon"></span></p>
                <p style="font-size: 14px;"><span>报名截止时间</span><br/><span class="w-orange" style="font-weight: bold;">${registrableCard.endDate}</span></p>
            </div>
            <div class="pc-btn">
                <a class="w-btn w-btn-green" href="javascript:void (0);" onclick="$17.atongji('首页-学习任务卡片-报名','/student/newexam/apply.vpage?id=${registrableCard.id!}');">我要报名</a>
            </div>
        </div>
    </li>
</#list>
</#if>
<#-- 开始（继续）考试卡片 -->
<#if (data.enterableNewExamCards?has_content && data.enterableNewExamCards?size gt 0)!false>
<#list data.enterableNewExamCards as enterableCard>
<li class="practice-block stepNoviceTwoBox">
    <div class="practice-content">
        <h4>
            <#switch enterableCard.subject>
                <#case 'ENGLISH'>
                    <span class="w-discipline-tag w-discipline-tag-1">英语测试</span>
                    <#break >
                <#case 'MATH'>
                    <span class="w-discipline-tag w-discipline-tag-2">数学测试</span>
                    <#break >
                <#case 'CHINESE'>
                    <span class="w-discipline-tag w-discipline-tag-3">语文测试</span>
                    <#break >
                <#default>
                    <span class="w-discipline-tag w-discipline-tag-3">测试</span>
                    <#break >
            </#switch>
        </h4>
        <div class="no-content">
            <p class="n-1"><span class="w-icon w-clockTime-icon"></span></p>
            <p style="font-size: 14px;"><span>测试截止时间</span><br/><span class="w-orange" style="font-weight: bold;">${enterableCard.endDate!}</span></p>
        </div>
        <div class="pc-btn">
            <#if enterableCard.newExamStudentStatus?has_content && enterableCard.newExamStudentStatus == "BEGIN">
                <a class="w-btn w-btn-orange" href="javascript:void (0);" onclick="$17.atongji('首页-学习任务卡片-测试','/student/newexam/begin.vpage?from=card&id=${enterableCard.id!}');">
                    开始测试
                </a>
            </#if>
            <#if enterableCard.newExamStudentStatus?has_content && enterableCard.newExamStudentStatus == "CONTINUE">
                <a class="w-btn w-btn-orange" href="javascript:void (0);" onclick="$17.atongji('首页-学习任务卡片-测试','/student/newexam/begin.vpage?from=card&id=${enterableCard.id!}');">
                    继续测试
                </a>
            </#if>
        </div>
    </div>
</li>
</#list>
</#if>

<#-- 开始（继续）考试卡片 -->
<#if (data.enterableUnitTestCards?has_content && data.enterableUnitTestCards?size gt 0)!false>
<li class="practice-block stepNoviceTwoBox">
    <div class="practice-content">
        <h4>
            <#--<#switch enterableCard.subject>
                <#case 'ENGLISH'>
                    <span class="w-discipline-tag w-discipline-tag-1">英语测试</span>
                    <#break >
                <#case 'MATH'>
                    <span class="w-discipline-tag w-discipline-tag-2">数学测试</span>
                    <#break >
                <#case 'CHINESE'>
                    <span class="w-discipline-tag w-discipline-tag-3">语文测试</span>
                    <#break >
                <#default>
                    <span class="w-discipline-tag w-discipline-tag-3">测试</span>
                    <#break >
            </#switch>-->
            <span class="w-discipline-tag w-discipline-tag-2">单元检测</span>
        </h4>
        <div class="no-content">
            <p class="n-1"><span class="w-icon w-clockTime-icon"></span></p>
            <#--<p style="font-size: 14px;"><span>测试截止时间</span><br/><span class="w-orange" style="font-weight: bold;">${enterableCard.endDate!}</span></p>-->
        </div>
        <div class="pc-btn">
            <a class="w-btn w-btn-orange" href="javascript:void (0);" onclick="$17.atongji('首页-学习任务卡片-测试','/student/newexam/paperlist.vpage?from=card');">
                开始
            </a>
        </div>
    </div>
</li>
</#if>
