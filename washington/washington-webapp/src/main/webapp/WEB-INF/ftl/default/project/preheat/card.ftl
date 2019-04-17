<#if [0, 2, 4, 6]?seq_contains( (currentStudentDetail.studentSchoolRegionCode%7)!1 ) && ((!currentStudentDetail.inPaymentBlackListRegion)!false)>
    <li class="practice-block">
        <div class="practice-content" style="background-image: url(<@app.link href='public/skin/project/preheat/pc/images/preheat-card01-v1.png'/>)">
            <div class="pc-btn">
                <a class="w-btn w-btn-orange" href="/project/preheat/index.vpage?refer=card_pc" target="_blank">立即查看</a>
            </div>
        </div>
    </li>
</#if>