<#import "../../layout_default.ftl" as layout_default>
<#import "abnormalclazzquery.ftl" as abnormalclazzquery>
<#import "../headsearch.ftl" as headsearch>
<@layout_default.page page_title="异常班级查询" page_num=3>
<style>
    .table_soll{ overflow-y:hidden; overflow-x: auto;}
    .table_soll table td,.table_soll table th{white-space: nowrap;}
</style>
<div id="main_container" class="span9">
    <@headsearch.headSearch/>
    <@abnormalclazzquery.queryPage/>
    <#if dislocationGroupDetailList?has_content>
        <div class="table_soll">
            <table class="table table-striped table-bordered" id="dislocationGroups">
                <tr>
                    <th>序号</th>
                    <th>创建时间</th>
                    <th>上次修改时间</th>
                    <th>GroupId</th>
                    <th>目前所在学校</th>
                    <th>目标学校</th>
                    <th>操作</th>
                </tr>
                <#list dislocationGroupDetailList as dislocationGroupDetail>
                    <tr>
                        <#--<td>${dislocationGroupDetail_index+dataIndex}</td>-->
                        <td>${dislocationGroupDetail.id!""}</td>
                        <td>${dislocationGroupDetail.createDatetime!""}</td>
                        <td>${dislocationGroupDetail.updateDatetime!""}</td>
                        <td><a href='/crm/clazz/groupinfo.vpage?groupId=${dislocationGroupDetail.groupId!""}' target="_blank">${dislocationGroupDetail.groupId!""}</a></td>
                        <td>${dislocationGroupDetail.currentSchoolName!""}<br/><a href='/crm/school/schoolhomepage.vpage?schoolId=${dislocationGroupDetail.currentSchoolId!""}'  target="_blank">${dislocationGroupDetail.currentSchoolId!""}</a></td>
                        <td>${dislocationGroupDetail.realSchoolName!""}<br/><a href='/crm/school/schoolhomepage.vpage?schoolId=${dislocationGroupDetail.realSchoolId!""}'  target="_blank">${dislocationGroupDetail.realSchoolId!""}</a></td>
                        <td>
                            <button id="editDislocationGroup_btn"  class="edittype btn btn-primary" data-id='${dislocationGroupDetail.id!""}' data-groupId='${dislocationGroupDetail.groupId!""}' data-currentSchoolId='${dislocationGroupDetail.currentSchoolId!""}' data-currentSchoolName='${dislocationGroupDetail.currentSchoolName!""}' data-realSchoolId='${dislocationGroupDetail.realSchoolId!""}' data-realSchoolName='${dislocationGroupDetail.realSchoolName!""}'}>编辑</button>
                            <button id="deleteDislocationGroup_btn"  class="deletetype btn btn-primary" data-id='${dislocationGroupDetail.id!""}' data-groupId='${dislocationGroupDetail.groupId!""}' data-currentSchoolId='${dislocationGroupDetail.currentSchoolId!""}' data-currentSchoolName='${dislocationGroupDetail.currentSchoolName!""}' data-realSchoolId='${dislocationGroupDetail.realSchoolId!""}' data-realSchoolName='${dislocationGroupDetail.realSchoolName!""}'}>删除</button>
                        </td>
                    </tr>
                </#list>
            </table>
        </div>
    <#elseif nodata??>
        暂无相关数据
    </#if>
</div>

<div id="deleteDislocationGroupData_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>删除数据</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>GroupId</dt>
                    <dd><input id="deleteGroupId" type='text' readonly="readonly"/></dd>
                </li>
                <li>
                    <dt>当前所在学校</dt>
                    <dd>
                        <div id="currentSchoolInfoForDelete"></div>
                    </dd>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>目标学校Id</dt>
                    <dd><input id="deleteRealSchoolId" type='text' readonly="readonly"/></dd>
                </li>
                <li>
                    <dt>目标学校信息</dt>
                    <dd>
                        <div id="realSchoolInfoForDelete"></div>
                    </dd>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>操作备注</dt>
                    <dd><textarea id="operationNotesForDelete" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="deleteDislocationClazzInfo_dialog_confirm_btn" class="btn btn-primary">确定</button>
        <button id="deleteDislocationClazzInfo_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>

<div id="editDislocationGroupData_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>编辑数据</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>GroupId</dt>
                    <dd><input id="editGroupId" type='text' readonly="readonly"/></dd>
                </li>
                <li>
                    <dt>当前所在学校</dt>
                    <dd>
                        <div id="currentSchoolInfoForEdit"></div>
                    </dd>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>目标学校Id</dt>
                    <dd><input id="editRealSchoolId" type='text' onchange="onEditRealSchoolIdChange(this.value)" /></dd>
                </li>
                <li>
                    <dt>目标学校信息</dt>
                    <dd>
                        <div id="realSchoolInfoForEdit"></div>
                    </dd>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>操作备注</dt>
                    <dd><textarea id="operationNotesForEdit" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="editDislocationClazzInfo_dialog_confirm_btn" class="btn btn-primary" data-id="">确定</button>
        <button id="editDislocationClazzInfo_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $("#dislocationGroups").on("click","button",function () {
            var $button = $(this).closest("button");
            if (!$button.hasClass("active")) {
                $button.addClass("active").siblings("li").removeClass("active");
                var dislocationGroupDetailData = {};
                dislocationGroupDetailData["data-id"]=$button.attr("data-id");
                dislocationGroupDetailData["data-groupId"]=$button.attr("data-groupId");
                dislocationGroupDetailData["data-currentSchoolId"]=$button.attr("data-currentSchoolId");
                dislocationGroupDetailData["data-currentSchoolName"]=$button.attr("data-currentSchoolName");
                dislocationGroupDetailData["data-realSchoolId"]=$button.attr("data-realSchoolId");
                dislocationGroupDetailData["data-realSchoolName"]=$button.attr("data-realSchoolName");

                //处理编辑和删除函数
                if($button.hasClass("edittype")){
                    hadleEditAndDeleteDislocationGroup(dislocationGroupDetailData,"edittype");
                } else if($button.hasClass("deletetype")){
                    hadleEditAndDeleteDislocationGroup(dislocationGroupDetailData,"deletetype");
                }
            }
            $button.removeClass("active");
            return false;
        });

        function hadleEditAndDeleteDislocationGroup(dislocationGroupDetailData,type) {
            var id = dislocationGroupDetailData["data-id"];
            var groupId = dislocationGroupDetailData["data-groupId"];
            var currentSchoolId = dislocationGroupDetailData["data-currentSchoolId"];
            var currentSchoolName = dislocationGroupDetailData["data-currentSchoolName"];
            var realSchoolId = dislocationGroupDetailData["data-realSchoolId"];
            var realSchoolName = dislocationGroupDetailData["data-realSchoolName"];

            if(type == "edittype"){//编辑
                //禁止input数据groupId
                $('#editDislocationClazzInfo_dialog_confirm_btn').attr("data-id",id);
                $('#editGroupId').val(groupId);
                $('#currentSchoolInfoForEdit').html(currentSchoolName+"校区(<a href='/crm/school/schoolhomepage.vpage?schoolId=" + currentSchoolId + "' target='_blank'>" + currentSchoolId + "</a>)");
                $('#editRealSchoolId').val(realSchoolId);
                $('#realSchoolInfoForEdit').html(realSchoolName+"校区(<a href='/crm/school/schoolhomepage.vpage?schoolId=" + realSchoolId + "' target='_blank'>" + realSchoolId + "</a>)")
                $("#editDislocationGroupData_dialog").modal("show");
            }else if(type == "deletetype"){//删除
                //禁止input在输入数据
                $('#deleteGroupId').val(groupId);
                $('#currentSchoolInfoForDelete').html(currentSchoolName+"校区(<a href='/crm/school/schoolhomepage.vpage?schoolId=" + currentSchoolId + "' target='_blank'>" + currentSchoolId + "</a>)");
                $('#deleteRealSchoolId').val(realSchoolId);
                $('#realSchoolInfoForDelete').html(realSchoolName+"校区(<a href='/crm/school/schoolhomepage.vpage?schoolId=" + realSchoolId + "' target='_blank'>" + realSchoolId + "</a>)");
                $("#deleteDislocationGroupData_dialog").modal("show");
            }
        }

        $('#deleteDislocationClazzInfo_dialog_confirm_btn').click(function () {
            var groupId = $('#deleteGroupId').val();
            var operationNotes = $('#operationNotesForDelete').val();
            $.post("deletedislocationgroup.vpage", {groupId:groupId,operationNotes:operationNotes}, function (data) {
                if (data.success) {
                    window.location.reload();
                    alert("删除成功");
                } else {
                    alert(data.info);
                }
            });
            $("#deleteDislocationGroupData_dialog").modal("hide");
        });
        $('#deleteDislocationClazzInfo_dialog_cancel_btn').click(function () {
            $("#deleteDislocationGroupData_dialog").modal("hide");
        });

        $('#editDislocationClazzInfo_dialog_confirm_btn').click(function () {
            var id = $('#editDislocationClazzInfo_dialog_confirm_btn').attr('data-id');
            var groupId = $('#editGroupId').val();
            var realSchoolId = $('#editRealSchoolId').val();
            var operationNotes = $('#operationNotesForEdit').val();
            $.post("updatedislocationgroup.vpage", {id:id,groupId:groupId,realSchoolId:realSchoolId,operationNotes:operationNotes}, function (data) {
                if (data.success) {
                    window.location.reload();
                    alert("更新成功");
                } else {
                    alert(data.info);
                }
            });
            $("#editDislocationGroupData_dialog").modal("hide");
        });
        $('#editDislocationClazzInfo_dialog_cancel_btn').click(function () {
            $("#editDislocationGroupData_dialog").modal("hide");
        });
    });
    function onEditRealSchoolIdChange(realSchoolId) {
        if (realSchoolId == undefined) {
            $('#realSchoolInfoForEdit').html("目标学校Id参数输入有误")
            return;
        }
        //后台查询后展示
        $.post("searchschool.vpage", {realSchoolId: realSchoolId}, function (data) {
            if (data.success) {
                $('#realSchoolInfoForEdit').html(data.schoolName + "校区(<a href='/crm/school/schoolhomepage.vpage?schoolId=" + data.schoolId + "' target='_blank'>" + data.schoolId + "</a>)")
            } else {
                $('#realSchoolInfoForEdit').html(data.info);
                alert(data.info);
            }
        });
    }
</script>
</@layout_default.page>