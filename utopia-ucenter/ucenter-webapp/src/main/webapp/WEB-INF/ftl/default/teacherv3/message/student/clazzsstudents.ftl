<#if userMappers?? && userMappers?size gt 0>
    <#list userMappers as row>
    <li data-receiver_id="${row.id!}" data-receiver_name="${(row.profile.realname)!}">
        <label>
            <b>
                <img src="<@app.avatar href="${row.fetchImageUrl()!}"/>" width="60" height="60" onerror="this.onerror='';this.src='<@app.avatar href=""/>'"/>
                <i class="checkboxs checkboxs_active"></i>
            </b>
            <span>${(row.profile.realname)!}</span>
        </label>
    </li>
    </#list>
<#else>
    <div style="font-size: 14px;color: #ff0000;margin: 10px;">该班暂无学生。</div>
</#if>
