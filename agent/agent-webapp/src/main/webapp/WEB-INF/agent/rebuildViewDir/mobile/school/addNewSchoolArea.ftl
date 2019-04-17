<div class="flow" style="background:#fff">
    <p style="padding:0.4rem 0 0.4rem 0.8rem;color:#9199bb;background:#f6f6f6;text-align:left;font-size: 60%;">必填项，创建后无法修改</p>
    <div class="item" style="margin:0 0 0 1rem;padding:0 1rem 0 0;">
        地区
        <div class="inner-right js-place" id="regionName">
        <#if regionName??>${regionName!''}<#else>请选择</#if>
        </div>
        <input type="hidden" name="regionCode" id="regionCode" value="<#if regionCode??>${regionCode!''}</#if>" data-einfo="请选择地区">
    </div>
    <div class="item" style="margin:0 0 0 1rem;padding:0 1rem 0 0">
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
    <div class="item" style="margin:0 0 0 1rem;padding:0 1rem 0 0;">
        名称<div style="width:10rem;<#if name??><#else>text-align:right</#if>" class="inner-right js-name">
        <#if cname??>${cname!''}<#else>请填写</#if>
        </div><input  type="hidden" name="schoolName" id="schoolName" value="<#if cname??>${cname!''}</#if>" data-einfo="请填写学校名称">
    </div>
    <div class="item schoolLength" id="eduSystemType" style="margin:0 0 0 1rem;padding:0 1rem 0 0;" >
        学制
        <div class="inner-right js-length">
            ${eduSystemName!''}
        </div>
    <#if locked?? && !locked>
        <select id="eduSystem" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
            <option value="0">请选择</option>
            <#list eduSystemTypes as data>
                <option class="${data.group!""}" <#if data.selected?? && data.selected>selected</#if>
                        value="${data.code!''}">${data.name!''}</option>
            </#list>
        </select>
    </#if>
    </div>
    <div class="item externOrBoarderType" id="externOrBoarderType" style="margin:0 0 0 1rem;padding:0 1rem 0 0;border:0;" >
        走读方式
        <#if externOrBoarder??>
            <#if externOrBoarder == 1>
                <div class="inner-right js-externOrBoarder">走读</div>
            </#if>
            <#if externOrBoarder == 3>
                <div class="inner-right js-externOrBoarder">半寄宿</div>
            </#if>
            <#if externOrBoarder == 2>
                <div class="inner-right js-externOrBoarder">全寄宿</div>
            </#if>
        <#else>
        <div class="inner-right js-externOrBoarder">走读</div>
        </#if>
        <select id="externOrBoarder" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
            <#if externOrBoarder?? && externOrBoarder == 1>
                <option value="1" selected>走读</option>
            <#else>
                <option value="1" selected>走读</option>
            </#if>
            <option value="3" <#if externOrBoarder?? && externOrBoarder == 3>selected</#if>>半寄宿</option>
            <option value="2" <#if externOrBoarder?? && externOrBoarder == 2>selected</#if>>全寄宿</option>
        </select>
    </div>
    <p style="padding:0.4rem 0 0.4rem 0.8rem;background:#f6f6f6;color:#9199bb;text-align:left;font-size: 60%;">选填（鉴定学校使用）</p>
    <div class="schoolParticular-box">
        <div class="particular-image">
            <div class="left"><p class="p-1">照片 <a class="schooleImageDetail" href="javascript:;" onclick="openSecond('/mobile/school_clue/photodesc.vpage')"></a></p></div>
            <div class="right photoSchoolShortIcon" id="photoShow">
                <a href="javascript:void(0);">
                    <img src="<#if photoUrl??>${(photoUrl)!''}${shortIconTail}</#if>">
                </a>
            </div>
        </div>
        <div class="particular-image">
            <div class="left">位置</div>
            <div class="right"><#if address??>${address!''}</#if></div>
        </div>
    </div>
</div>