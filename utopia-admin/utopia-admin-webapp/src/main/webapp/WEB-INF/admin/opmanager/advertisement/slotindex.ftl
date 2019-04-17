<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="广告运营管理平台" page_num=9>
<div id="main_container" class="span9">
    <legend>
        <a href="adindex.vpage">广告管理</a>&nbsp;&nbsp;&nbsp;&nbsp;
        <strong>广告位信息</strong> &nbsp;&nbsp;&nbsp;&nbsp;
        <a href="adarrangement.vpage">广告排期管理</a> &nbsp;&nbsp;&nbsp;&nbsp;
        <button id="add_slot_btn" class="btn btn-info" style="float: right">添加广告位</button>
    </legend>
    <pre>
        广告位操作须知：
           1. 当该广告位有上线的广告时，不允许编辑/删除广告位，故编辑/删除广告位之前，请先确认该广告位没有关联的广告。
           2. 新增广告位时，广告位ID请遵循以下规则：
             2.1) 广告位ID为6位字符串
             2.2) 最左至右第一位表示用户类型 1-老师 2-家长 3-学生 9-市场 (与UserType枚举一致)
             2.3) 最左至右第二位表示投放端 1-小学PC端 2-小学APP端 3-小学微信端  4-中学PC端 5-中学APP端 9-天玑APP端 (以上两项，如果需要改动请先联系开发人员，以免影响使用)
             2.4) 最左至右三、四两位表示页面编码，暂无明确规定，建议参考已有广告位填写
             2.5) 最左至右五、六两位为广告标识，暂无明确规定，建议参考已有广告位填写
           3. 其他内容请酌情填写
    </pre>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="slot-query" class="form-horizontal" method="get"
                      action="${requestContext.webAppContextPath}/opmanager/advertisement/slotindex.vpage">
                    <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
                    <ul class="inline">
                        <li>
                            <label>广告位ID：&nbsp;
                                <input id="qSlotId" name="qSlotId" value="${qSlotId!}" type="text" style="width: 130px;"/>
                            </label>
                        </li>
                        <li>
                            <label>用户类型：&nbsp;
                                <select id="ut" name="ut" style="width: 160px;">
                                    <option value=-1>所有用户</option>
                                    <option <#if ut??><#if ut == 1> selected="selected"</#if></#if> value=1>老师</option>
                                    <option <#if ut??><#if ut == 2> selected="selected"</#if></#if> value=2>家长</option>
                                    <option <#if ut??><#if ut == 3> selected="selected"</#if></#if> value=3>学生</option>
                                    <option <#if ut??><#if ut == 9> selected="selected"</#if></#if> value=9>市场</option>
                                </select>
                            </label>
                        </li>
                        <li>
                            <label>所属端：&nbsp;
                                <select id="ep" name="ep" style="width: 160px;">
                                    <option value="">所有端</option>
                                    <option <#if ep??><#if ep == "pc"> selected="selected"</#if></#if> value="pc">小学PC端</option>
                                    <option <#if ep??><#if ep == "app"> selected="selected"</#if></#if> value="app">小学APP端</option>
                                    <option <#if ep??><#if ep == "wechat"> selected="selected"</#if></#if> value="wechat">小学微信端</option>
                                    <option <#if ep??><#if ep == "jpc"> selected="selected"</#if></#if> value="jpc">中学PC端</option>
                                    <option <#if ep??><#if ep == "japp"> selected="selected"</#if></#if> value="japp">中学APP端</option>
                                    <option <#if ep??><#if ep == "mapp"> selected="selected"</#if></#if> value="mapp">天玑APP端</option>

                                </select>
                            </label>
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">查 询</button>
                        </li>
                    </ul>
                </form>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="width: 75px;">广告位ID</th>
                        <th>广告位名称</th>
                        <th style="display: none;">ut</th>
                        <th>用户类型</th>
                        <th>投放端</th>
                        <th>Width(px)</th>
                        <th>Height(px)</th>
                        <th>类型</th>
                        <th>容纳数量</th>
                        <th style="width: 90px;">操作</th>
                    </tr>
                    </thead>
                    <#if slotPage?? && slotPage.content?? >
                        <tbody>
                            <#list slotPage.content as slot >
                            <tr>
                                <td><strong>${slot.id!"--"}</strong></td>
                                <td><strong><a id="name_${slot.id}">${slot.name!''}</a></strong></td>
                                <td id="ut_${slot.id}" style="display: none;" >${slot.userType!0}</td>
                                <td id="userType_${slot.id}"><#if slot.userType == 1>老师<#elseif slot.userType == 2>家长<#elseif slot.userType == 3>学生<#elseif slot.userType == 9>市场<#else>未知</#if></td>
                                <td id="endpoint_${slot.id}">${slot.endpoint!'--'}</td>
                                <td id="width_${slot.id}">${slot.width!0}</td>
                                <td id="height_${slot.id}">${slot.height!0}</td>
                                <td id="type_${slot.id}">${slot.type!'--'}</td>
                                <td id="capacity_${slot.id}">${slot.capacity!0}</td>
                                <td>
                                    <button class="btn btn-danger" onclick="delSlot(${slot.id})">
                                        <i class="icon-trash icon-white"></i> 删 除
                                    </button>
                                </td>
                            </tr>
                            </#list>
                        </tbody>
                    </#if>
                </table>
                <ul class="pager">
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>
<style>
    .table td, .table th {
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }
</style>
<div id="edit_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>编辑广告位信息</h3>
    </div>
    <div class="modal-body dl-horizontal"  style="overflow: hidden;">
        <input id="mode" type="hidden" value="add">
        <dl>
            <dt>广告位ID</dt>
            <dd>
                <input type="text" id="slotId" name="slotId"/>
            </dd>
        </dl>
        <dl>
            <dt>广告位名称</dt>
            <dd>
                <input type="text" id="slotName" name="slotName"/>
            </dd>
        </dl>
        <dl>
            <dt>用户类型</dt>
            <dd>
                <select id="userType">
                    <option value=>--请选择--</option>
                    <option value=1>老师</option>
                    <option value=2>家长</option>
                    <option value=3>学生</option>
                    <option value=9>市场</option>
                </select>
            </dd>
        </dl>
        <dl>
            <dt>所属端</dt>
            <dd>
                <select id="endpoint">
                    <option value="">--请选择--</option>
                    <option value="pc">小学PC端</option>
                    <option value="app">小学APP端</option>
                    <option value="wechat">小学微信端</option>
                    <option value="jpc">中学PC端</option>
                    <option value="japp">中学APP端</option>
                    <option value="mapp">天玑APP端</option>
                </select>
            </dd>
        </dl>
        <dl>
            <dt>广告位规格</dt>
            <dd>
                <input type="text" id="width" name="width" style="width: 85px;" placeholder="width"/>&nbsp;×&nbsp;
                <input type="text" id="height" name="height" style="width: 85px;" placeholder="height"/>
            </dd>
        </dl>
        <dl>
            <dt>展示类型</dt>
            <dd>
                <select id="slotType">
                    <option value="">--请选择--</option>
                    <#if slotTypeList??>
                    <#list slotTypeList as slotType>
                        <option value="${slotType}">${slotType}</option>
                    </#list>
                    </#if>
                </select>
            </dd>
        </dl>
        <dl>
            <dt>容纳数量</dt>
            <dd>
                <input type="text" id="capacity" name="capacity"/>
            </dd>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取消</button>
        <button class="btn btn-primary" id="save_slot_btn">保存广告位</button>
    </div>
</div>

<script>
    $(function () {
        $("[data-toggle='tooltip']").tooltip();

        $('#add_slot_btn').on('click', function () {
            $('#mode').val("add");
            $('#slotId').val('');
            $('#slotId').attr("disabled", false);
            $('#slotName').val('');
            $('#userType').val('');
            $('#userType').attr("disabled", false);
            $('#endpoint').val('');
            $('#endpoint').attr("disabled", false);
            $('#width').val('');
            $('#width').attr("disabled", false);
            $('#height').val('');
            $('#height').attr("disabled", false);
            $('#slotType').val('');
            $('#capacity').val('');
            $("#edit_dialog").modal('show');
        });

        $("a[id^='name_']").on('click', function () {
            var id = $(this).attr('id').substring("name_".length);
            var slot = {
                id: id,
                name: $(this).html().trim(),
                userType: $("#ut_" + id).html().trim(),
                endpoint: $("#endpoint_" + id).html().trim(),
                width: $("#width_" + id).html().trim(),
                height: $("#height_" + id).html().trim(),
                type: $("#type_" + id).html().trim(),
                capacity: $("#capacity_" + id).html().trim()
            };
            $('#mode').val("edit");
            $('#slotId').val(slot.id);
            $('#slotId').attr("disabled", true);
            $('#slotName').val(slot.name);
            $('#userType').val(slot.userType);
            $('#userType').attr("disabled", true);
            $('#endpoint').val(slot.endpoint);
            $('#endpoint').attr("disabled", true);
            $('#width').val(slot.width);
            $('#width').attr("disabled", true);
            $('#height').val(slot.height);
            $('#height').attr("disabled", true);
            $('#slotType').val(slot.type);
            $('#capacity').val(slot.capacity);
            $("#edit_dialog").modal('show');
        });

        $("#save_slot_btn").on('click', function () {
            var slot = {
                mode: $('#mode').val(),
                id: $('#slotId').val(),
                name: $('#slotName').val(),
                userType :$('#userType').val(),
                endpoint :$('#endpoint').val(),
                width: $('#width').val(),
                height: $('#height').val(),
                type: $('#slotType').val(),
                capacity: $('#capacity').val()
            };
            var valid = validate(slot);
            if (valid.length > 0) {
                alert(valid);
                return false;
            }
            $.post('saveslot.vpage', slot, function (data) {
                if (data.success) {
                    alert("保存成功！");
                    window.location.href = 'slotindex.vpage';
                } else {
                    alert(data.info);
                }
            });
        });

    });

    function delSlot(slotId) {
        if (!confirm("是否确认删除？")) {
            return false;
        }
        if (!confirm("真的确认该广告位没有关联的广告了么？")) {
            return false;
        }
        $.post('delslot.vpage', {slotId: slotId}, function (data) {
            if (data.success) {
                alert("删除完成!\n请务必去相关页面确认该广告位的删除没有影响系统功能！");
                window.location.href = 'slotindex.vpage';
            } else {
                alert(data.info);
            }
        });
    }

    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#slot-query").submit();
    }

    function validate(slot) {
        var msg = "";
        if (slot.id == '' || slot.id.length != 6) {
            msg += "请填写正确的广告位ID，并不能与先前广告位ID重复！\n";
        }
        if (slot.name == '') {
            msg += "请填写广告位名称！\n";
        }
        if (slot.userType == '' || !$.isNumeric(slot.userType) || slot.userType <= 0 ||  slot.userType > 9) {
            msg += "请选择用户类型！\n";
        }
        if (slot.endpoint == '') {
            msg += "请选择所属端！\n";
        }
        if (slot.type != '' && slot.type != '纯文本') {
            if (slot.width == '' || !$.isNumeric(slot.width) || slot.width <= 0) {
                msg += "请填写正确的广告宽度！\n";
            }
            if (slot.height == '' || !$.isNumeric(slot.height) || slot.height <= 0) {
                msg += "请填写正确的广告高度！\n";
            }
        }
        if (slot.type == '') {
            msg += "广告位类型不能为空！\n";
        }
        if (slot.capacity == '' || !$.isNumeric(slot.capacity) || slot.capacity <= 0) {
            msg += "广告位容纳数量不能小于0！\n";
        }
        return msg;
    }
</script>
</@layout_default.page>