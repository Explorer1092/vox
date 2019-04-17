<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="转校审核" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<div class="span11">
    <legend>
        <a href="/crm/school_clue/clue_list.vpage">学校信息审核</a>&nbsp;&nbsp;
        <a href="/crm/teacher_fake/teacher_fakes.vpage">判假老师审核</a>&nbsp;&nbsp;
        <a href="/crm/teacher_appeal/index.vpage">老师申诉审核</a>&nbsp;&nbsp;
        转校审核
    </legend>

    <div>
        <form id="query_info_form" method="post" action="teacherTransferSchoolInfo.vpage" class="form-horizontal" >
            <fieldset>
                <ul class="inline">
                    <li>
                        转出校
                        <select id="sourceSchoolDict" name="sourceSchoolDict">
                            <option value="all">全部</option>
                            <option value="true">重点</option>
                            <option value="false">非重点</option>
                        </select>
                    </li>
                    <li>
                        类型
                        <select id="changeType" name="changeType">
                            <option value="ALL">全部</option>
                            <option value="WITHCLAZZS">带班转</option>
                            <option value="WITHOUTCLAZZS">不带班转</option>
                        </select>
                    </li>
                    <li>
                        是否认证
                        <select id="authenticationState" name="authenticationState">
                            <option value="all">全部</option>
                            <option value="true">是</option>
                            <option value="false">否</option>
                        </select>
                    </li>
                    <li>
                        结果
                        <select id="checkResult" name="checkResult">
                            <option value="NOTHANDLED">待处理</option>
                            <option value="TRUE">正确</option>
                            <option value="FALSE">错误</option>
                        </select>
                    </li>
                    <br/>
                    <br/>
                    <li>
                        <button id="query_info_btn" type="button" class="btn btn-primary">查 询</button>
                        <button id="reset_btn" type="button" class="btn btn-primary">重 置</button>
                        <input type="hidden" id="currentPage" name="currentPage" value="${currentPage!1}" />
                        <input type="hidden" id="totalPage" name="totalPage" value="${totalPage!1}" />
                    </li>
                </ul>
            </fieldset>
        </form>
    </div>

    <div id = "dataInfoId">
        <#if crmTeacherTransferSchoolRecords?has_content>
            <div class="table_soll">
                <table class="table table-striped table-bordered" id="crmTeacherTransferSchoolRecords">
                    <tr>
                        <th>老师ID</th>
                        <th>老师姓名</th>
                        <th>认证状态</th>
                        <th>类型</th>
                        <th>转出校</th>
                        <th>转入校</th>
                        <th>操作人</th>
                        <th>操作时间</th>
                        <th>问题描述</th>
                        <#if checkResultType == "NOTHANDLED">
                            <th>操作</th>
                        <#elseif  checkResultType == "TRUE">
                            <th>审核人</th>
                            <th>审核时间</th>
                        <#elseif  checkResultType == "FALSE">
                            <th>审核人</th>
                            <th>审核时间</th>
                            <th>备注</th>
                        </#if>
                        </tr>
                    <#list crmTeacherTransferSchoolRecords as crmTeacherTransferSchoolRecord>
                        <tr>
                            <td>
                                <a href='/crm/teachernew/teacherdetail.vpage?teacherId=${crmTeacherTransferSchoolRecord.teacherId!""}'  target="_blank">${crmTeacherTransferSchoolRecord.teacherId!""}</a>
                            </td>
                            <td>${crmTeacherTransferSchoolRecord.teacherName!""}</td>
                            <td>${crmTeacherTransferSchoolRecord.authenticationState?string('是', '否')}</td>
                            <td>
                                <#if crmTeacherTransferSchoolRecord.changeType == "WITHCLAZZS">
                                    带班转
                                <#else>
                                    不带班转
                                </#if>
                            </td>
                            <td>${crmTeacherTransferSchoolRecord.sourceSchoolName!""}<a href='/crm/school/schoolhomepage.vpage?schoolId=${crmTeacherTransferSchoolRecord.sourceSchoolId!""}'  target="_blank">${crmTeacherTransferSchoolRecord.sourceSchoolId!""}</a></td>
                            <td>${crmTeacherTransferSchoolRecord.targetSchoolName!""}<a href='/crm/school/schoolhomepage.vpage?schoolId=${crmTeacherTransferSchoolRecord.targetSchoolId!""}'  target="_blank">${crmTeacherTransferSchoolRecord.targetSchoolId!""}</a></td>
                            <td>${crmTeacherTransferSchoolRecord.operator!""}</td>
                            <td>${crmTeacherTransferSchoolRecord.operationTime!""}</td>
                            <td>${crmTeacherTransferSchoolRecord.changeSchoolDesc!""}</td>
                            <#if checkResultType == "NOTHANDLED">
                            <td>
                                <button class="check_true_btn btn btn-primary" data-id='${crmTeacherTransferSchoolRecord.id!""}'>正确</button>
                                <button class="check_false_bth btn btn-primary" data-id='${crmTeacherTransferSchoolRecord.id!""}' >错误</button>
                            </td>
                            <#elseif  checkResultType == "TRUE">
                                <td>${crmTeacherTransferSchoolRecord.checkOperator!""}</td>
                                <td>${crmTeacherTransferSchoolRecord.updateTime!""}</td>
                            <#elseif  checkResultType == "FALSE">
                                <td>${crmTeacherTransferSchoolRecord.checkOperator!""}</td>
                                <td>${crmTeacherTransferSchoolRecord.updateTime!""}</td>
                                <td>${crmTeacherTransferSchoolRecord.checkDesc!""}</td>
                            </#if>
                        </tr>
                    </#list>
                </table>
            </div>
            <div>
                <ul class="inline">
                    <li>
                        <a id='firstpageId' href="javascript:void(0)">首页</a>
                    </li>
                    <li>
                        <a id='prepageId' href="javascript:void(0)">上一页</a>
                    </li>
                    <li>
                        <a id='nextpageId' href="javascript:void(0)">下一页</a>
                    </li>
                    <li>
                        <a id='lastpageId' href="javascript:void(0)">末页</a>
                    </li>
                    <li>
                    当前第${currentPage!"1"}页 &nbsp;&nbsp; 共${totalPage!"1"}页
                    </li>
                </ul>
            </div>
        <#elseif nodata??>
            暂无相关数据
        </#if>
    </div>
</div>
<div id="check_false_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>转校判错</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>错误说明*</dt>
                    <dd><textarea id="checkDescText" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <input type="hidden" id="recordId"/>
        <button id="check_false_dialog_confirm_btn" class="btn btn-primary">确定</button>
        <button id="check_false_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>
<script type="application/javascript">
    $(function () {
        var Conditions={};
        <#if conditions??>
            Conditions={
                sourceSchoolDict : '${conditions.sourceSchoolDict!""}',
                changeType : '${conditions.changeType!""}',
                authenticationState : '${conditions.authenticationState!""}',
                checkResult : '${conditions.checkResult!""}'
            };
            setConditions();
        </#if>

        $('#firstpageId').click(function () {
            var currentPage = $('#currentPage').val();
            if(currentPage == 1){
                return;
            }
            currentPage = 1;
            $('#currentPage').val(currentPage);
            queryTeacherTransferSchoolInfoByPage();
        });

        $('#prepageId').click(function () {
            var currentPage =$('#currentPage').val();
            if(currentPage == 1){
                return;
            }
            currentPage = new Number(currentPage);
            currentPage = currentPage -1;
            $('#currentPage').val(currentPage);
            queryTeacherTransferSchoolInfoByPage();
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
            queryTeacherTransferSchoolInfoByPage();
        });

        $('#lastpageId').click(function () {
            var currentPage =$('#currentPage').val();
            var totalPage =$('#totalPage').val();
            if(totalPage ==1 || currentPage == totalPage){
                return;
            }
            currentPage = totalPage;
            $('#currentPage').val(currentPage);
            queryTeacherTransferSchoolInfoByPage();
        });


        function setConditions() {
            $('#sourceSchoolDict').val(Conditions.sourceSchoolDict);
            $('#changeType').val(Conditions.changeType);
            $('#authenticationState').val(Conditions.authenticationState);
            $('#checkResult').val(Conditions.checkResult);
        }


        $("#query_info_btn").click(function () {
            $("#currentPage").val(1);
            $("#query_info_form").submit();
        });

        $("#reset_btn").click(function () {
            Conditions.sourceSchoolDict = 'all';
            Conditions.changeType = 'ALL';
            Conditions.authenticationState ="all";
            Conditions.checkResult ="NOTHANDLED";
            setConditions();
            $("#dataInfoId").remove();
        });

        function queryTeacherTransferSchoolInfoByPage() {
            setConditions();
            $("#query_info_form").submit();
        }

        $(".check_true_btn").click(function () {
            var id = $(this).attr("data-id");
            var checkResult = "TRUE";
            $.post("/crm/teachertransfer/setcheckresult.vpage", { id:id,checkResult:checkResult}, function(data){
                if(data.success){
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            });
        });

        $(".check_false_bth").click(function () {
            $("#recordId").val($(this).attr("data-id"));
            $('#check_false_dialog').modal("show");
        });

        $("#check_false_dialog_cancel_btn").click(function () {
            $("#recordId").val("");
            $('#check_false_dialog').modal("hide");
        });

        $("#check_false_dialog_confirm_btn").click(function () {
            var id = $("#recordId").val();
            var checkResult = "FALSE";
            var checkDesc = $("#checkDescText").val();
            if(isBlank(checkDesc)){
                alert("请填写判错原因!");
                return false;
            }
            $.post("/crm/teachertransfer/setcheckresult.vpage", { id:id,checkResult:checkResult,checkDesc:checkDesc}, function(data){
                if(data.success){
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            });
        });

        //验证是否未定义或null或空字符串
        function isBlank(str){
            return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
        }
    });
</script>
</@layout_default.page>