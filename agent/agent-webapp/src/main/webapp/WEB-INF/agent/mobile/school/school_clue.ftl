<#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<div class="mobileCRM-V2-box mobileCRM-V2-info ">
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="link link-ico">
                <div class="side-fl">地区</div>
                <#if clueType?? && clueType =="editClue">
                    <div class="side-fl side-orange">&nbsp;*</div>
                </#if>
                <input type="text" placeholder="请选择" class="side-fr side-time" name="regionName" readonly="readonly"
                       id="regionName"
                       value="<#if schoolClue.countyName ??> ${(schoolClue.cityName)!''} &nbsp;${(schoolClue.countyName)!''}</#if>">
                <input type="hidden" class="side-fr side-time" readOnly="true" name="regionCode"
                       id="regionCode" value="<#if schoolClue.countyCode ??>${(schoolClue.countyCode)!''}</#if>">
            </div>
        </li>
        <li>
            <div class="link link-ico">
                <div class="side-fl">阶段</div>
                <#if clueType?? && clueType =="editClue">
                    <div class="side-fl side-orange">&nbsp;*</div>
                </#if>
                <#if clueType?? && clueType =="schoolDetail">
                                <input type="hidden" name="schoolPhase" value="${(schoolClue.schoolPhase)!1}">
                </#if>
                <div class="side-fr side-time" name="schoolPhaseDisplay"><#if schoolClue?? && schoolClue.schoolPhase?? && schoolClue.schoolPhase == 2>
                    中学<#elseif schoolClue?? && schoolClue.schoolPhase?? && schoolClue.schoolPhase == 1>
                    小学<#else>请选择
                    </#if></div>
            </div>
            <select id="schoolPhase" <#if (clueType!"")!="newClue">disabled="disabled" </#if> name="schoolPhase">
                <option value="0">请选择</option>
                <option <#if schoolClue?? && schoolClue.schoolPhase?? && schoolClue.schoolPhase == 1>selected</#if>
                        value="1">小学
                </option>
                <option <#if schoolClue?? && schoolClue.schoolPhase?? && schoolClue.schoolPhase == 2>selected</#if>
                        value="2">中学
                </option>
                <option <#if schoolClue?? && schoolClue.schoolPhase?? && schoolClue.schoolPhase == 3>selected</#if>
                        value="3">中学
                </option>
            </select>
        </li>
        <li>
            <div class="box">
                <a href="javaScript:void(0)" class="link link-ico">
                    <div class="side-fl">名称</div>
                    <#if clueType?? && clueType =="editClue">
                        <div class="side-fl side-orange">&nbsp;*</div>
                    </#if>
                    <div name="schoolName" class="side-fr side-time" id="schoolName"><#if schoolClue.schoolName??>${(schoolClue.schoolName)!''}
                                                                                        <#elseif schoolClue.cmainName??>${(school.cmainName)!''}
                                                                                        <#if schoolClue.schoolDistrict??>(${(school.schoolDistrict)!''})</#if>
                                                                                        <#else>请填写</#if></div>
                    <input type="hidden" name="schoolId" id="schoolId"
                           value="<#if schoolClue.schoolId??>${(schoolClue.schoolId)!''}</#if>">
                </a>
            </div>
        </li>
    </ul>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="box">
                <div class="side-fl" style="margin:10px 0;">照片
                <#if clueType?? && clueType =="editClue">
                    (选填)
                </#if>&nbsp;
                    <a class="schooleImageDetail" onclick="openSecond('/mobile/school_clue/photodesc.vpage')"></a></div>
                <div class="side-fr photoSchoolShortIcon" id="photoShow"><img src="<#if schoolClue.photoUrl??>${(schoolClue.photoUrl)!''}${shortIconTail}</#if>"></div>
                <input type="hidden" id="photoUrl" name="photoUrl" value="<#if schoolClue.photoUrl??>${(schoolClue.photoUrl)!''}</#if>">
            </div>
        </li>
        <li>
            <div class="box">
                <div class="side-fl">位置</div>
                <input type="text" readonly="readonly" placeholder="通过照片直接获取，无需填写" class="side-fr side-time" name="address" style="width: 89%;"
                       id="address" value="<#if schoolClue.address??>${(schoolClue.address)!''}</#if>">
                <input type="hidden" class="side-fr side-time js-lat" id="lat" name="latitude"
                       value="<#if schoolClue.latitude??>${(schoolClue.latitude)!''}</#if>">
                <input type="hidden" class="side-fr side-time js-lng" id="lng" name="longitude"
                       value="<#if schoolClue.longitude??>${(schoolClue.longitude)!''}</#if>">
            </div>
        </li>
    </ul>
</div>


