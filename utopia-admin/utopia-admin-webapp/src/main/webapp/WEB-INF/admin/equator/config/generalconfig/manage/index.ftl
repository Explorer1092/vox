<#import "../../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="通用配置" page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<style>
    .beautiful-pre {color: green; margin: 0; padding: 0; border: none; font-size: smaller; line-height: 12px;}
</style>

<div id="main_container" class="span9" style="font-size: 14px">
    <legend style="font-weight: 700;">通用配置管理</legend>

    <input id="addGeneralConfigBtn" class="btn btn-info" type="button" value="新增通用配置">
    <br/><br/>

    <form class="form-horizontal" action="/equator/config/generalconfig/manage/index.vpage" method="get" id="iform">
        配置的Key&nbsp;
        <input type="text" id="generalConfigKey" name="generalConfigKey" width="25px"
               value="<#if generalConfigKey??>${generalConfigKey!}</#if>"/>

        <button type="submit" id="generalConfigSelect" class="keySelect btn btn-primary">查询</button>
        <button type="submit" id="generalConfigRegexSelect" class="keySelect btn btn-primary">
            key前缀查询
        </button>
        <input style="display:none" id="selectType" name="selectType"/>

        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" type="hidden" value="5">
    </form>

    <#if generalConfigList ??>
        <div class="table_soll">
            <table class="table table-bordered">
                <tr>
                    <th style="width:200px">id</th>
                    <th style="width:200px">key</th>
                    <th>数据</th>
                    <th style="width:140px">生效时间</th>
                    <th style="width:100px">描述</th>
                    <th style="width:20px">操作</th>
                </tr>
                <tbody id="tbody">
                    <#list generalConfigList.content as oneConfig>
                    <tr
                        <#if ! oneConfig.hasBeenBetweenTime()>style="text-decoration:line-through;"</#if>
                        title="创建时间：${(oneConfig.createTime)?default("")} , 更新时间：${(oneConfig.updateTime)?default("")}">
                        <td>${(oneConfig.id)?default("")}</td>
                        <td>${(oneConfig.key)?default("")}</td>
                        <td>
                            <div style="max-height: 400px; overflow-y: scroll;">
                                <span id="actual_${(oneConfig.id)?default("")}" style="display: none">${(oneConfig.value)?default("")}</span>
                                <pre class="beautiful-pre">${(oneConfig.value)?default("")}</pre>
                            </div>
                        </td>
                        <td>${(oneConfig.beginTime)?string('yyyy-MM-dd HH:mm:ss')}~
                            <br>${(oneConfig.endTime)?string('yyyy-MM-dd HH:mm:ss')}
                        </td>
                        <td>${(oneConfig.desc)?default("")}</td>
                        <td>
                            <button class="btn btn-default editGeneralConfigBtn"
                                    data-id="${(oneConfig.id)?default("")}"
                                    data-key="${(oneConfig.key)?default("")}"
                                    data-value="${(oneConfig.value)?default("")}"
                                    data-beginTime="${(oneConfig.beginTime)?default("")?string('yyyy-MM-dd HH:mm:ss')}"
                                    data-endTime="${(oneConfig.endTime)?default("")?string('yyyy-MM-dd HH:mm:ss')}"
                                    data-desc="${(oneConfig.desc)?default("")}"
                            >编辑
                            </button>
                        </td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

    <#assign pager=generalConfigList!>
    <#include "../../../../crm/pager_foot.ftl">

</div>


<#--新增或编辑一条配置的弹窗-->
<div id="general_config_dialog" class="modal hide fade" style="width:700px;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>新增配置</h3>
    </div>
    <div class="modal-body" style="max-height:600px">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>id</dt>
                    <dd>
                        <input id="editId" type="text" placeholder="新增数据默认的id是空" value="" style="width: 386px;"/>
                    </dd>

                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>配置的key</dt>
                    <dd>
                        <input id="editKey" type="text" placeholder="必填" value="" style="width: 386px;"/>
                    </dd>

                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>生效时间内的数据</dt>
                    <dd>
                        <textarea id="editValue" rows="5" style="resize: none; width: 386px;"></textarea>
                    </dd>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>描述</dt>
                    <dd>
                        <textarea id="editDesc" cols="40" rows="2" placeholder="*必填"
                                  style="resize: none;width: 386px;"></textarea>
                    </dd>

                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>生效开始时间</dt>
                    <dd>
                        <input id="editBeginTime" type="text" value="" placeholder="默认今天0时开始"
                               style="width: 386px;"/>
                    </dd>

                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>生效结束时间</dt>
                    <dd>
                        <input id="editEndTime" type="text" value="" placeholder="默认2029年12月31"
                               style="width: 386px;"/>
                    </dd>

                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="upsert_general_config_dialog_btn" data-status="insert" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<script>
    $(function () {
        $('.beautiful-pre').each(function () {
            if ($(this).text()) {
                try {
                    const obj = JSON.parse($(this).text());
                    if (typeof obj === 'object' && obj) {
                        $(this).text(JSON.stringify(JSON.parse($(this).text()), null, 2));
                    }
                } catch (e) {
                    // do nothing
                }
            }
        });

        /*查询*/
        $("#generalConfigSelect").on("click", function () {
            $("#selectType").val("byKey");
            $("#iform").submit();
        });

        /*key前缀查询*/
        $("#generalConfigRegexSelect").on("click", function () {
            $("#selectType").val("byPrefix");
            $("#iform").submit();
        });

        /*新增弹窗*/
        $("#addGeneralConfigBtn").on("click", function () {
            $("#editId").val("").attr("readonly", true);
            $("#editKey").val("");
            $("#editValue").val("");
            $("#editDesc").val("");
            $("#editBeginTime").val("");
            $("#editEndTime").val("");
            $("#upsert_general_config_dialog_btn").attr("data-status", "insert");
            $("#general_config_dialog").modal("show");
        });


        /*编辑弹窗*/
        $(".editGeneralConfigBtn").on("click", function () {
            var dataId = $(this).attr("data-id");
            var dataValue = $("#actual_" + dataId).html().trim();

            $("#editId").val(dataId).attr("readonly", true);
            $("#editKey").val($(this).attr("data-key"));
            // $("#editValue").val($(this).attr("data-value"));
            $("#editValue").val(dataValue);
            $("#editBeginTime").val($(this).attr("data-beginTime"));
            $("#editEndTime").val($(this).attr("data-endTime"));
            $("#editDesc").val($(this).attr("data-desc"));
            $("#upsert_general_config_dialog_btn").attr("data-status", "update");
            $("#general_config_dialog").modal("show");
        });


        //新增或编辑数据
        $("#upsert_general_config_dialog_btn").on("click", function () {
            var isInsert = $("#upsert_general_config_dialog_btn").attr("data-status") === "insert";
            var id = $("#editId").val();
            var key = $("#editKey").val();
            var value = $("#editValue").val();
            var desc = $("#editDesc").val();
            var beginTime = $("#editBeginTime").val();
            var endTime = $("#editEndTime").val();

            if (isBlank(key) || isBlank(desc)) {
                alert("key和描述都不能为空");
                return;
            }

            $.post('/equator/config/generalconfig/manage/editconfig.vpage', {
                isInsert: isInsert,
                id: id,
                key: key,
                value: value,
                desc: desc,
                beginTime: beginTime,
                endTime: endTime
            }, function (data) {
                if (data.success) {
                    alert("配置成功，服务数分钟后生效");
                    window.location.href = "/equator/config/generalconfig/manage/index.vpage?generalConfigKey=" + key;
                } else {
                    alert(data.info);
                }
            });
        });

    });

    function isBlank(str) {
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    $('#editBeginTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',
        minuteStep: 1
    });

    $('#editEndTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',
        minuteStep: 1
    });

</script>
</@layout_default.page>