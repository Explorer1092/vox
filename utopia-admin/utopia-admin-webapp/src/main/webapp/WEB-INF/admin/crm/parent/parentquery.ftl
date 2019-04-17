<#-- @ftlvariable name="conditionMap" type="java.util.LinkedHashMap" -->
<#macro queryPage>
<div>
    <form method="post" action="parentlist.vpage" class="form-horizontal">
        <fieldset>
            <legend>家长查询</legend>
            <ul class="inline">
                <li>
                    <label for="parentId">
                        家长学号
                        <input name="parentId" id="parentId" type="text"/>
                    </label>
                </li>
              <li>
                <label for="parentId">
                  家长绑定手机号
                  <input name="mobile" id="mobile" type="text"/>
                </label>
              </li>
                <li>
                    <button id="query_info_btn" type="submit" class="btn btn-primary">查 询</button>
                </li>
            </ul>
        </fieldset>
    </form>
</div>
<script>
    $(function(){
        <#if parentList?has_content && parentList?size == 1>
            window.open("parenthomepage.vpage?parentId=${parentList[0].parentId!''}", "_blank");
        </#if>
        $(document).keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#query_info_btn').click();
            }
        });
        <#if conditionMap?has_content>
            $('#parentId').val('${conditionMap.parentId!''}');
            $('#parentName').val('${conditionMap.parentName!''}');
            $('#parentMobile').val('${conditionMap.parentMobile!''}');
            <#--$('#parentEmail').val('${conditionMap.parentEmail!''}');-->
        </#if>
    });
</script>
</#macro>