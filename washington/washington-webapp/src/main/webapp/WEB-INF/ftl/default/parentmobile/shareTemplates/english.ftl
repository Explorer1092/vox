<#include "../constants.ftl">
<ul>
    <li>
        <p style="font-size:18px;margin-bottom: 12px;">词汇量</p>
        <div class="schoolReport_con">
            <p>教学要求掌握 <span class="text_red">${needLearnWordsCount!0}</span>个，班平均掌握<span class="text_red">${avglearnwords!0}</span>个</p>
            <p>${studentName!""}掌握 <span class="text_red">${learnWordsCount!0}</span>个</p>
        </div>
    </li>
</ul>
${buildAutoTrackTag("report|en_transcript_open", true)}
