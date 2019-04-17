<#assign isFirstStep = (step!"v1") == "v1">
<#assign sid = sid!"">
<#assign freeGold = freeGold!10>

<div class="view-detailMore-main doStepV2" <#if isFirstStep > style="display: none;" </#if>>

    <input type="hidden" name="sid" value="${sid}"/>
    <#if missionid?exists>
        <input type="hidden" name="missionId" value="${missionid!''}"/>
    </#if>
    <input type="hidden" name="missionType" value="OTHER"/>
    <input type="hidden" name="totalCount" value="1"/>
    <input type="hidden" name="wishType" value="${isFirstStep?string('', 'CUSTOMIZE')}"/>

    <div style="" class="check-class-box">
        <h2>目标次数</h2>
        <div class="cc-b">
            <#assign countInfo = [
                1, 2, 3,
                5, 7, 10
            ]>

            <ul class="showClazzListByEdu">
            <#list countInfo as count>
                <li data-num="${count}"><span class="doSetMission clazz-t <#if count_index == 0 >active</#if>"><i class="icon-check-purple"></i>${count}次</span></li>
            </#list>
            </ul>
        </div>
    </div>
    <div class="check-class-box">
        <h2>目标内容</h2>
        <input class="w-input" name="mission" maxlength="20" type="text" placeholder="如去爬山、买书包等，由家长自己提供">
        <h2>家长奖励</h2>
        <div class="integralShow">
            <input class="w-input" name="wish" maxlength="20" type="text" placeholder="自定义奖励" value="${wish!''}">
            <#if isFirstStep>
                <span style="margin: 0px 0px 0px 50px;" class="fixIntegral">${freeGold}学豆</span>
            </#if>
        </div>
    </div>
    <a id="do_set_customize_mission" class="btn_mark_purple doTrack" data-track="parent|setgoal_success" style="color: #ffffff;" href="javascript:;" data-wish_type="INTEGRAL">确定</a>
</div>
