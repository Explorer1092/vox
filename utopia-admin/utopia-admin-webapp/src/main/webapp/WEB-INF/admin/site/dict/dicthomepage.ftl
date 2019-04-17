<#-- @ftlvariable name="adminDictGroupNameList" type="java.util.List<java.lang.String>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='Web manage' page_num=4>
<style>
    li, label {font-size: 16px;}
</style>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>字典管理</legend>
        </fieldset>
        <fieldset>
            <legend>增加</legend>
            <ul class="inline">
                <li>Group Name:</li>
                <li>
                    <label><select id="selectedGroupName">
                        <option value="">全部</option>
                        <#if adminDictGroupNameList?has_content>
                            <#list adminDictGroupNameList as adminDictGroupName>
                                <option value="${adminDictGroupName}">${adminDictGroupName}</option>
                            </#list>
                        </#if>
                    </select></label>
                </li>
                <li>
                    <label><input id="writtenGroupName" type="text" placeholder="请勿包含空白符"/></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>Group Member:<input id="groupMember" type="text" placeholder="请勿包含空白符"/></label>
                </li>
                <li>
                    <label>Description:<textarea id="description"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <button id="submit" class="btn btn-success">提交</button>
                </li>
            </ul>
        </fieldset>
    </div>
    <div>--------------------------------------------------------------------------------------------------------------</div>
    <div>
        <fieldset>
            <legend>查询</legend>
            <ul class="inline">
                <li>
                    <label>Group Name<select id="query_select">
                        <option value="">无</option>
                        <#if adminDictGroupNameList?has_content>
                            <#list adminDictGroupNameList as adminDictGroupName>
                                <option value="${adminDictGroupName}">${adminDictGroupName}</option>
                            </#list>
                        </#if>
                    </select></label>
                </li>
                <li>
                    <button id="query" class="btn btn-success">查询</button>
                </li>
            </ul>
            <div id="dict_list_chip"></div>
        </fieldset>
    </div>
    <script>
        $(function() {

            $('#submit').on('click', function() {

                var $querySelect = $('#query_select');
                var postData = {
                    selectedGroupName : $('#selectedGroupName').val(),
                    writtenGroupName : $('#writtenGroupName').val(),
                    groupMember : $('#groupMember').val(),
                    description : $('#description').val()
                };
                $.post('addadmindict.vpage', postData, function(data) {
                    alert(data.info);
                    if(data.success) {
                        $('#dict_list_chip').load('dictlistchip.vpage', {groupName : $querySelect.val()});
                        if(data.groupName) {
                            $('#selectedGroupName').append('<option value="' + data.groupName + '">' + data.groupName + '</option>');
                            $querySelect.append('<option value="' + data.groupName + '">' + data.groupName + '</option>');
                        }
                    }
                });
            });

            $('#query').on('click', function() {
                $('#dict_list_chip').load('dictlistchip.vpage', {groupName : $('#query_select').val()});
            });
        });
    </script>
</@layout_default.page>