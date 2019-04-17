<#-- @ftlvariable name="adminDictList" type="java.util.List<com.voxlearning.utopia.admin.persist.entity.AdminDict>" -->
<div>
    <ul class="inline">
        <#if wqList?has_content>
            <#list wqList as wq>
                <li><a href="#" onclick="viewWqImg('${wq!}');">${wq!}</a></li>
            </#list>
        </#if>
    </ul>
</div>