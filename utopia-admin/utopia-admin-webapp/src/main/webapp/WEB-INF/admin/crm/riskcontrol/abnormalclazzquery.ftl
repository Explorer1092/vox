<#-- @ftlvariable name="conditionMap" type="java.util.LinkedHashMap" -->
<#macro queryPage>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.autocomplete.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div>
    <form id="query_info_form" method="post" action="abnormalclazzList.vpage" class="form-horizontal" >
        <fieldset>
            <legend>异常班级新增和查询</legend>
            <ul class="inline">
                <li>
                    <button id="addDislocationClazzInfo_btn" type="button" class="btn btn-primary">新 增</button>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label for="groupId">
                        GroupId:
                        <input name="groupId" id="groupId" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="realSchoolId">
                        目标学校:
                        <input name="realSchoolId" id="realSchoolId" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="modifiedTime">
                        修改时间:
                        <input name="beginTime" id="beginTime" type="text" />-
                        <input name="endTime" id="endTime" type="text" />
                    </label>
                </li>
                <li>
                    <button id="query_info_btn" type="button" class="btn btn-primary">查 询</button>
                    <input type="hidden" id="currentPage" name="currentPage" value="${currentPage!1}" />
                    <input type="hidden" id="totalPage" name="totalPage" value="${totalPage!1}" />
                </li>
            </ul>
        </fieldset>
    </form>
</div>
<div>
    <#if dislocationGroupDetailList?has_content>
    <ul class="inline">
        <li>
            <a id='firstpageId' href="javascript:void(0)">首页</a>
        </li>
        <li>
            <a id='prepageId' href="javascript:void(0)">上一页</a>
        </li>
        <li>
        ${currentPage!"1"}/${totalPage!"1"}
        </li>
        <li>
            <a id='nextpageId' href="javascript:void(0)">下一页</a>
        </li>
        <li>
            <a id='lastpageId' href="javascript:void(0)">末页</a>
        </li>
    </ul>
    </#if>
</div>

<div id="addDislocationClazzInfo_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>新建数据</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>GroupId</dt>
                    <dd><input id="addGroupId" type='text' onchange="onaddGroupIdChange(this.value)"  /></dd>
                </li>
                <li>
                    <dt>当前学校所在</dt>
                    <dd>
                        <div id="currentSchoolInfoForAdd"></div>
                    </dd>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>目标学校Id</dt>
                    <dd><input id="addRealSchoolId" type='text' onchange="onaddRealSchoolIdChange(this.value)" /></dd>
                </li>
                <li>
                    <dt>目标学校信息</dt>
                    <dd>
                        <div id="realSchoolInfoForAdd"></div>
                    </dd>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>操作备注</dt>
                    <dd><textarea id="operationNotesForAdd" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="addDislocationClazzInfo_dialog_confirm_btn" class="btn btn-primary">确定</button>
        <button id="addDislocationClazzInfo_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>


<script type="text/javascript">
    $(function () {
        var Conditions={};
        <#if conditions??>
            Conditions={
                groupId : '${conditions.groupId!""}',
                realSchoolId : '${conditions.realSchoolId!""}',
                beginTime : '${conditions.beginTime!""}',
                endTime : '${conditions.endTime!""}'
            }
        </#if>
        $('#firstpageId').click(function () {
            var currentPage = $('#currentPage').val();
            if(currentPage == 1){
                return;
            }
            currentPage = 1;
            $('#currentPage').val(currentPage);
            queryDislocationClazzInfoByPage();
        });

        $('#prepageId').click(function () {
            var currentPage =$('#currentPage').val();
            if(currentPage == 1){
                return;
            }
            currentPage = new Number(currentPage);
            currentPage = currentPage -1;
            $('#currentPage').val(currentPage);
            queryDislocationClazzInfoByPage();
        });

        $('#nextpageId').click(function () {
            var currentPage =$('#currentPage').val();
            var totalPage =$('#totalPage').val();
            if(currentPage == totalPage){
                return;
            }
            currentPage = new Number(currentPage);
            currentPage =currentPage +1;
            $('#currentPage').val(currentPage);
            queryDislocationClazzInfoByPage();
        });

        $('#lastpageId').click(function () {
            var currentPage =$('#currentPage').val();
            var totalPage =$('#totalPage').val();
            if(totalPage ==1 || currentPage == totalPage){
                return;
            }
            currentPage = totalPage;
            $('#currentPage').val(currentPage);
            queryDislocationClazzInfoByPage();
        });

        $('#addDislocationClazzInfo_btn').click(function () {
            $('#addDislocationClazzInfo_dialog').modal("show");
        });
        $('#addDislocationClazzInfo_dialog_cancel_btn').click(function () {
            $('#addDislocationClazzInfo_dialog').modal("hide");
        });
        $('#addDislocationClazzInfo_dialog_confirm_btn').click(function () {
            var groupId = $('#addGroupId').val();
            var realSchoolId = $('#addRealSchoolId').val();
            var operationNotes = $('#operationNotesForAdd').val();
            if("" == operationNotes){
                alert("操作备注不能为空");
                return;
            }
            $.post("adddislocationgroup.vpage", {groupId: groupId,realSchoolId:realSchoolId,operationNotes:operationNotes}, function (data) {
                if(data.success){
                    alert("新增数据成功");
                    window.location = "abnormalclazzList.vpage?groupId="+groupId;
                }else{
                    alert(data.info);
                }
            });
            $('#addDislocationClazzInfo_dialog').modal("hide");
        });

        $("#query_info_btn").click(function () {
            $("#currentPage").val(1);
            $("#query_info_form").submit();
        });

        $("#beginTime").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $("#endTime").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        function setConditions() {
           $('#groupId').val(Conditions.groupId);
           $('#realSchoolId').val(Conditions.realSchoolId);
           $('#beginTime').val(Conditions.beginTime);
           $('#endTime').val(Conditions.endTime);
        }

        function queryDislocationClazzInfoByPage() {
            setConditions();
            $("#query_info_form").submit();
        }
    });

    function onaddGroupIdChange(groupId) {
        if (groupId == undefined) {
            $('#currentSchoolInfoForAdd').html("GroupId参数输入有误")
            return;
        }
        //后台查询后展示
        $.post("searchschool.vpage", {groupId: groupId}, function (data) {
            if (data.success) {
                $('#currentSchoolInfoForAdd').html(data.schoolName + "校区(<a href='/crm/school/schoolhomepage.vpage?schoolId=" + data.schoolId + "' target='_blank'>" + data.schoolId + "</a>)")
            } else {
                $('#currentSchoolInfoForAdd').html(data.info);
                alert(data.info);
            }
        });
    }

    function onaddRealSchoolIdChange(realSchoolId) {
        if (realSchoolId == undefined) {
            $('#realSchoolInfoForAdd').html("目标学校Id参数输入有误")
            return;
        }
        //后台查询后展示
        $.post("searchschool.vpage", {realSchoolId: realSchoolId}, function (data) {
            if (data.success) {
                $('#realSchoolInfoForAdd').html( data.schoolName + "校区(<a href='/crm/school/schoolhomepage.vpage?schoolId=" + data.schoolId + "' target='_blank'>" + data.schoolId + "</a>)")
            } else {
                $('#realSchoolInfoForAdd').html(data.info);
                alert(data.info);
            }
        });
    }


</script>
</#macro>