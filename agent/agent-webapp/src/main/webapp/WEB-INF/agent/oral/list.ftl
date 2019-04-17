<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='统考管理' page_num=8>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-user"></i> 统考测试一览</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="addOral" class="btn btn-success" href="addoral.vpage">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>
                &nbsp;
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">创建人</label>
                        <div class="controls">
                            <label class="control-label">
                                <input type="text" id="creator" name="creator" value="">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">类型</label>
                        <div class="controls">
                            <label class="control-label">
                               <select id="regionLevel" name="regionLevel">
                                   <option value="">请选择</option>
                                   <option value="city">市级</option>
                                   <option value="country">区级</option>
                                   <option value="school">校级</option>
                               </select>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学科</label>
                        <div class="controls">
                            <label class="control-label">
                                <select id="subjectId" name="subjectId">
                                    <option value="">请选择</option>
                                    <#if subjects?has_content && subjects?size gt 0>
                                        <#list subjects?keys as subjectId>
                                            <option value="${subjectId}">${subjects[subjectId]}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">状态</label>
                        <div class="controls">
                            <label class="control-label">
                                <select id="status" name="status">
                                    <option value="">请选择</option>
                                    <option value="NEW">待录入</option>
                                    <option value="DRAFT">录入中</option>
                                    <option value="ONLINE">已发布</option>
                                </select>
                            </label>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="queryOralBtn" type="button" class="btn btn-primary">查询</button>
                    </div>
                </fieldset>
            </form>
            <div class="dataTables_wrapper" role="grid">

                <table class="table table-striped table-bordered bootstrap-datatable" id="oralTableList" >
                    <thead>

                    <tr>
                        <th class="sorting" style="width: 60px;">创建人</th>
                        <th class="sorting" style="width: 60px;">考试名称</th>
                        <th class="sorting" style="width: 60px;">学科</th>
                        <th class="sorting" style="width: 60px;">类型</th>
                        <th class="sorting" style="width: 60px;">状态</th>
                        <th class="sorting" style="width: 100px;">时间</th>
                        <th class="sorting" style="width: 140px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </div>
        </div>
    </div>

</div>

<#--查看内容-->
<div id="viewOralDetail" class="modal fade hide">

</div>


<script type="text/javascript">
    $(function(){
        $("#queryOralBtn").on("click",function(){
            var oTable = $('#oralTableList').dataTable( {
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sProcessing": "正在加载中......",
                    "sLengthMenu": "每页显示 _MENU_ 条记录",
                    "sZeroRecords": "对不起，查询不到相关数据！",
                    "sEmptyTable": "表中无数据存在！",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
                    "sInfoEmpty": "当前显示 0 到 0 条，共 0 条记录",
                    "sInfoFiltered": "数据表中共为 _MAX_ 条记录",
                    "sSearch": "搜索",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": "上一页",
                        "sNext": "下一页",
                        "sLast": "末页"
                    }
                },
                "bStateSave": false, //翻页或页面数量改变时不保存在cookie中
                "bSort": false,
                "bLengthChange" : false,  //不允许用户修改单页条数
                "bFilter" : false, // 不允许过滤
                "bDestroy" : true,
                "aoColumns": [
                    { "sTitle": "创建人", "mDataProp": "agentCode" },
                    { "sTitle": "考试名称", "mDataProp": "name" },
                    { "sTitle": "学科", "mDataProp": "subjectId",
                        "fnRender": function ( rowObj,cellVal ) {
                            if(cellVal >= 101 && cellVal <= 103){
                                return "小学" + rowObj.aData["subjectName"];
                            }
                            return rowObj.aData["subjectName"];
                        }
                    },
                    { "sTitle": "类型", "mDataProp": "regionLevelName" },
                    { "sTitle": "状态", "mDataProp": "statusName"},
                    { "sTitle": "创建时间", "mDataProp": "createDateStr"},
                    { "sTitle": "操作",  "mDataProp": "self",
                        "fnRender": function ( rowObj,cellVal ) {
                            var operationStr = '<a class="btn btn-success viewBtn" data-id="' + rowObj.aData["id"] + '" href="javascript:void(0);" style="margin-right: 19px;"><i class="icon-info-sign icon-white"></i>查看</a>';
                            if(cellVal){
                                if(rowObj.aData["status"] == 'NEW' || rowObj.aData["status"] == 'ONLINE') {
                                    operationStr += '<a class="btn btn-info editBtn" href="editoral.vpage?id=' + rowObj.aData["id"] + '" style="margin-right: 19px;"><i class="icon-edit icon-white"></i>编辑</a>';
                                }
                                if(rowObj.aData["status"] == 'NEW'){
                                    operationStr += '<a class="btn btn-danger deleteBtn" data-id="' + rowObj.aData["id"] + '" href="javascript:void(0);"><i class="icon-trash icon-white"></i>删除</a>';
                                }
                            }
                            return operationStr;
                        }
                    }
                ],
                "bServerSide": true,
                "sAjaxSource": "search.vpage",
                "sServerMethod": "POST",
                "fnServerParams": function ( aoData ) {

                    aoData.push({ "name": "creator", "value": $("#creator").val()});
                    aoData.push({ "name": "regionLevel", "value": $("#regionLevel").val() });
                    aoData.push({ "name": "status", "value":$("#status").val()});
                    aoData.push({ "name": "subjectId", "value": $("#subjectId").val()});
                },
                "sAjaxDataProp": "dataList"
            });
        });

        $("#queryOralBtn").trigger("click");

        $(document).on("click","a.deleteBtn",function(){
            var id = $(this).attr("data-id");
            if(!confirm("确定要删除此条记录?")){
                return false;
            }
            $.post('deleteoral.vpage',{
                id:id
            },function(data){
                if(!data.success){
                    alert("删除失败，请重新查询后再试");
                }else{
                    window.location.href = 'list.vpage';
                }
            });
        });

        $(document).on("click","a.viewBtn",function(){
            var id = $(this).attr("data-id");

            $.get('oraldetail.vpage',{
                id:id
            },function(data){
                var $viewOralDetail = $('#viewOralDetail');
                $viewOralDetail.empty().html(data);
                $viewOralDetail.modal('show');
            });
        });

    });
</script>
</@layout_default.page>
