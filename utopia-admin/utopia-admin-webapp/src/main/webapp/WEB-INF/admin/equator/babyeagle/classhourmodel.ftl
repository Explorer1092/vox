<#import "../../layout_default.ftl" as layout_default>
<style>
    .panel-info {
        border-color: #bce8f1;
    }
    .panel {
        margin-bottom: 20px;
        background-color: #fff;
        border: 1px solid transparent;
        border-radius: 4px;
        -webkit-box-shadow: 0 1px 1px rgba(0,0,0,.05);
        box-shadow: 0 1px 1px rgba(0,0,0,.05);
    }
    .panel-info>.panel-heading {
        color: #31708f;
        background-color: #d9edf7;
        border-color: #bce8f1;
    }
    .panel-heading {
        padding: 10px 15px;
        border-bottom: 1px solid transparent;
        border-top-left-radius: 3px;
        border-top-right-radius: 3px;
    }
    .panel-title {
        margin-top: 0;
        margin-bottom: 0;
        font-size: 16px;
        color: inherit;
    }
    .panel-body {
        padding: 15px;
    }
</style>
    <@layout_default.page page_title = "小鹰学堂管理" page_num= 24 >
    <link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
    <span class="span9">
    <div id="legend" class="">
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation"><a href="studentlearninfo.vpage?studentId=${studentId!''}">学生课程管理</a></li>
            <li role="presentation"><a href="courseinfoindex.vpage">课程内容管理</a></li>
            <li role="presentation"><a href="coursekindindex.vpage">课程种类管理</a></li>
            <li role="presentation"><a href="teacherindex.vpage">教师管理</a></li>
            <li role="presentation" class="active"><a href="classhourmodel.vpage">单日课时模板</a></li>
        </ul>
    </div>

<input type="button" class="btn btn-primary" id="addclasshourmodel" value="创建单日课时模板"><br/><br/>
<div class="inline">
<#if classhourmodelList ?? >
     <#list classhourmodelList as classhourmodel>
         <div class="panel panel-info">
      <div class="panel-heading">
        <h3 class="panel-title">${(classhourmodel.templetName)!}
            <span style="float: right;"><input type="button" class="btn btn-danger" name="deleteclasshourmodel"
                         data-modelId="${(classhourmodel.id)!}" data-modelName=${(classhourmodel.templetName)!} value="删除单日课时模板" >
                <input type="button" class="btn btn-default" name="addclasshour" value="增加课时" data-modelId="${(classhourmodel.id)!}"></span>

            </h3>
      </div>
      <div class="panel-body">
          <#if classhourmodel.classHourList ?? >
              <div class="btn-group btn-group-lg" role="group" aria-label="Large button group">
                  <#list classhourmodel.classHourList as classhour>
                      <button type="button" class="btn btn-default deleteclasshour" title="点击删除" data-modelId="${(classhourmodel.id)!}" data-time="${classhour!}">${classhour!}</button>
                  </#list>
                 </div>
          </#if>
      </div>
    </div>

     </#list>
</#if>
</div>

        <div id="addclasshourmodel_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>新增单日课时模板</h3>
        </div>
        <div class="modal-body">

            <form class="form-horizontal" action="" method="post" enctype="multipart/form-data">

                <div class="control-group">
                    <label class="col-sm-2 control-label">模板名称</label>
                    <div class="controls">
                        <input type="text" id="modelname" class="form-control"
                        />
                        <span class="controls-desc"></span>
                    </div>
                </div>

        </form>

    </div>
    <div class="modal-footer">
        <button id="addclasshourmodel_btn"
                class="btn btn-primary">确 定
        </button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

       <div id="deleteclasshourmodel_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>删除单日课时模板</h3>
        </div>
        <div class="modal-body">

            <form class="form-horizontal" action="" method="post" enctype="multipart/form-data">

                 <input type="hidden" id="templetId" />

                <div class="control-group">
                    <label class="col-sm-2 control-label">模板名称</label>
                    <div class="controls">
                        <input type="text" id="templetName" class="form-control" readonly />
                        <span class="controls-desc"></span>
                    </div>
                </div>
        </form>
    </div>
    <div class="modal-footer">
        <button id="deleteclasshourmodel_btn"
                class="btn btn-primary">确 定
        </button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="addclasshour_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>新增课时</h3>
        </div>
        <div class="modal-body">
             <dl class="dl-horizontal">
                  <input type="hidden" id="id" />
                        <ul class="inline">
                    <li>
                         <dt>开始时间</dt>
                    <dd>
                         <input id="startTime" class="datetimepicker"
                                type="text" readonly/>
                       </dd>
                    </li>
                </ul>
              <ul class="inline">
                    <li>
                        <dt>结束时间</dt>
                        <dd><input id="endTime" class="datetimepicker"
                                   type="text" readonly/></dd>


                    </li>
                </ul>
             </dl>
    </div>
    <div class="modal-footer">
        <button id="addclasshour_btn"
                class="btn btn-primary">确 定
        </button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="deleteclasshour_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>删除课时</h3>
        </div>
        <div class="modal-body">
             <dl class="dl-horizontal">
                  <input type="hidden" id="modelId" />
                        <ul class="inline">
                    <li>
                         <dt>开始时间</dt>
                    <dd>
                         <input id="deletestartTime" type="text" readonly/>
                       </dd>
                    </li>
                </ul>
              <ul class="inline">
                    <li>
                        <dt>结束时间</dt>
                        <dd><input id="deleteendTime" type="text" readonly/></dd>
                    </li>
                </ul>
             </dl>
    </div>
    <div class="modal-footer">
        <button id="deleteclasshour_btn"
                class="btn btn-primary">确 定
        </button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

    </span>
    <script>
        $(function () {
            $('.datetimepicker').datetimepicker(
                {format: 'hh:ii' ,
                 startView: 'day',
                 minView: 'hour',
                 maxView:'day'
                 }
         );

         $("input[name='deleteclasshourmodel']").on('click', function () {
            var modelId = $(this).attr("data-modelId");
            var modelName = $(this).attr("data-modelName");
            $("#templetId").val(modelId);
            $("#templetName").val(modelName);
            $("#deleteclasshourmodel_dialog").modal("show");
        });

         $(".deleteclasshour").on('click', function () {
            $("#deleteclasshour_dialog").modal("show");
            var time = $(this).attr("data-time");
            var modelId = $(this).attr("data-modelId");
            var times=new Array();
            times=time.split('-');
            $("#deletestartTime").val(times[0]);
            $("#deleteendTime").val(times[1]);
            $("#modelId").val(modelId);
        });

         $("#addclasshourmodel").on('click', function () {
            $("#addclasshourmodel_dialog").modal("show");
        });

         $("input[name='addclasshour']").on('click', function () {
            var modelId = $(this).attr("data-modelId");
            $("#id").val(modelId);
            $("#addclasshour_dialog").modal("show");
        });

        $('#addclasshourmodel_btn').on('click', function () {
            var name = $("#modelname").val();

            $.get('/equator/babyeagle/addclasshourmodel.vpage', {
                modelname:name
            }, function (data) {
                if (data.success) {
                    alert("新增成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        $('#deleteclasshourmodel_btn').on('click', function () {
            var templetId = $("#templetId").val();

            $.get('/equator/babyeagle/deleteclasshourmodel.vpage', {
                templetId:templetId
            }, function (data) {
                if (data.success) {
                    alert("删除成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        $('#deleteclasshour_btn').on('click', function () {
            var id = $("#modelId").val();
            var startTime = $("#deletestartTime").val();
            var endTime = $("#deleteendTime").val();
            $.get('/equator/babyeagle/deletemodelclasshour.vpage', {
                modelId: id,
                startTime:startTime,
                endTime:endTime
            }, function (data) {
                if (data.success) {
                    alert("编辑成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        $('#addclasshour_btn').on('click', function () {
            var id = $("#id").val();
            var startTime = $("#startTime").val();
            var endTime = $("#endTime").val();
            $.get('/equator/babyeagle/addmodelclasshour.vpage', {
                modelId: id,
                startTime:startTime,
                endTime:endTime
            }, function (data) {
                if (data.success) {
                    alert("编辑成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
        });
    </script>
</@layout_default.page>