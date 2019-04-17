<div id="clue-new" title="新建线索" style="font-size: small; display: none" targetIds="" targetType="">
    <table width="100%">
        <tr>
            <td style="text-align: left">线索类型：</td>
            <td style="text-align: left">
                <select id="clue-new-type" style="width:180px">
                <#if clueTypes?has_content>
                    <#list clueTypes as clueType>
                        <option value="${clueType.name()}">${clueType.name()}</option>
                    </#list>
                </#if>
                </select>
            </td>
        </tr>

        <tr>
            <td colspan="2" style="text-align: right">
                <input type="button" value="提交" onclick="clue.save()"/>
                <input type="button" value="取消" onclick="closeDialog('clue-new')"/>
            </td>
        </tr>
    </table>
</div>
