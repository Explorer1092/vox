<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑公众号" page_num=16>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        ${accounts.name}－运营内容配置
    </legend>
    <a href="javascript:void(0)" id="add_message" type="button" class="btn btn-info">新建内容</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered" >
                    <thead>
                    <tr>
                        <th>公众号名称</th>
                        <th>发送日期</th>
                        <th>内容详情</th>
                        <th>添加人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#if messageList ??>
                        <#list messageList as message>
                        <tr>
                            <td>期末磨耳朵</td>
                            <td>${message.date}</td>
                            <td>${message.content}</td>
                            <td>${message.creator}</td>
                            <td class="delete-message" data-date="${message.date}"><a type="button" class="btn btn-info">删除</a></td>
                        </tr>
                        </#list>
                    </#if>

                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<div id="add-message-dialog" class="modal fade hide" aria-hidden="true" style="display:none">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 class="modal-title">新建内容</h3>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="form-horizontal">
                <form id="config-admins-frm">
                    <div class="control-group">
                        <label class="col-sm-2 control-label">发送日期:</label>
                        <div class="controls">
                            <input id="date" name="date" class="date-input" value=""/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">发送内容:</label>
                        <div class="controls">
                            <textarea id="add-message-content" name="content" rows="10" placeholder="不超过30个字"></textarea>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            <button id="add-follow-btn" type="button" class="btn btn-primary">保存</button>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $(".date-input").datepicker({
            dateFormat      : 'yymmdd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
        $("#add_message").on("click",function(){
            $("#date").val("");
            $("#add-message-content").val("");
            $("#add-message-dialog").modal('show');
        });
        $("#add-follow-btn").on("click",function () {
            var date = $("#date").val();
            var content = $("#add-message-content").val();
            if(date == undefined || date == ''){
                alert("日期不能为空");
                return;
            }
            if(content == undefined || content == ''){
                alert("内容不能为空");
                return;
            }
            $.post("savedatemessageforjob.vpage",{
                date:date,
                content:content
            },function (data) {
               if(data.success){
                   alert("保存成功");
                   $("#add-message-dialog").modal('hide');
                   window.location.href = 'savedatemessageforjob.vpage?accountId=' + ${accounts.id};
               } else{
                   alert(data.info);
               }
            });
        });
        $(".delete-message").on("click",function () {
            var $this = $(this);
            var date = $this.data("date");
            if(date == undefined || date == ''){
                alert("日期不能为空");
                return;
            }
            if(!confirm("确认删除" + date + "的内容吗？")){
                return;
            }
            $.post("deletemessageforjob.vpage",{
                date:date
            },function (data) {
                if(data.success){
                    alert("删除成功");
                    window.location.href = 'savedatemessageforjob.vpage?accountId=' + ${accounts.id};
                } else{
                    alert(data.info);
                }
            });
        })
    });

</script>
<style>
    .legend-btn{
        margin-bottom: 5px;
    }
</style>
</@layout_default.page>