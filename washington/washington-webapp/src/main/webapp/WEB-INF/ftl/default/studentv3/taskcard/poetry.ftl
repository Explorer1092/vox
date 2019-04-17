<#if data.ancientPoetryActivityCards?has_content>
    <#assign activityObj = data.ancientPoetryActivityCards[0]>
    <li class="practice-block stepNoviceTwoBox">
        <div class="practice-content">
            <h4>
                <span class="w-discipline-tag w-discipline-tag-3">诗词大会</span>
            </h4>
            <div class="no-content">
                <p class="n-1"><span class="w-icon w-icon-13"></span></p>
                <p class="n-2"><strong></strong></p>
            </div>
            <div class="pc-btn">
                <a onclick="$17.atongji('首页-学习任务卡片-诗词大会','/ancient/poetry/index.vpage?from=indexCard&activityId=${activityObj.activityId!''}');" href="javascript:void (0);" class="w-btn w-btn-blue">学习古诗</a>
            </div>
        </div>
    </li>
</#if>