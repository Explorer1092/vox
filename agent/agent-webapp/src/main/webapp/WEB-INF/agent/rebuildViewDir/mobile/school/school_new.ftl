<div class="flow">
    <p style="padding:0.4rem 0;background:#f6f6f6;text-align:center;font-size: 60%;">地区、阶段、名称为必填项，且创建后无法修改</p>
    <div class="item">
        地区
        <div class="inner-right js-place" id="regionName">
        <#if regionName??>${regionName!''}<#else>请选择</#if>
        </div>
        <input type="hidden" name="regionCode" id="regionCode" value="<#if regionCode??>${regionCode!''}</#if>" data-einfo="请选择地区">
    </div>
    <div class="item" style="position:relative;">
        阶段
        <div class="inner-right js-stage">
            请选择
        </div>
        <select id="schoolPhase" name="phase" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
            <option value="0">请选择</option>
        <#list phase as data>
            <option <#if data.selected?? && data.selected>selected</#if> value="${data.id!0}">${data.phase!''}</option>
        </#list>
        </select>
    </div>
    <div class="item" style="height:4.5rem">
        名称
        <div style="width:10rem;height:4.5rem;<#if name??><#else>text-align:right</#if>" class="inner-right js-name">
        <#if name??>${name!''}<#else>请填写</#if>
        </div>
        <input  type="hidden" name="schoolName" id="schoolName" value="<#if name??>${name!''}</#if>" data-einfo="请填写学校名称">
    </div>
    <p style="padding:0.4rem 0;background:#f6f6f6;text-align:center;font-size: 60%;">选填照片和位置信息，审核通过后，学校将同时鉴定通过：</p>
    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt" style="font-size: .75rem;">
        <ul class="mobileCRM-V2-list">
            <li>
                <div class="box">
                    <div class="side-fl" style="margin:10px 0;">照片
                        <a class="schooleImageDetail" href="/mobile/school_clue/photodesc.vpage"></a></div>
                    <div class="side-fr photoSchoolShortIcon" id="photoShow" style="width:48px;height: 48px;background-size:100% 100%;"><img src="<#if photoUrl??>${(photoUrl)!''}${shortIconTail}</#if>"></div>
                </div>
            </li>
            <li>
                <div class="box">
                    <div class="side-fl">位置</div>
                    <input type="text" readonly="readonly" placeholder="通过照片直接获取，无需填写" name="address"
                           style="width: 89%;font-size:.75rem"
                           id="address" value="<#if address??>${(address)!''}</#if>">
                </div>
            </li>
        </ul>
    </div>
</div>