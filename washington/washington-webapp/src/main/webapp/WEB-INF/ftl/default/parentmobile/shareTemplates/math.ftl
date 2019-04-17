<#include "../constants.ftl">
<ul>
    <li>
        <p style="font-size:18px;margin-bottom: 12px;">成绩趋势</p>
        <div class="schoolReport_con">
            <p>上周平均 <span class="text_red">${beforeWeekAvgScore!0}</span>分，本周平均<span class="text_red">${currentWeekAvgScore!0}</span>分</p>
            <p>${studentName!""}<span class="text_red">${diff!""}</span></p>
        </div>
    </li>
</ul>
${buildAutoTrackTag("report|math_transcript_open", true)}
