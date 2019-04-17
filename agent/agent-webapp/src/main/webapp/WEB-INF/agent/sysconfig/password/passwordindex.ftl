<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='重设用户密码' page_num=6>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th-list"></i> 管理用户一览 </h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper">

                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">用户名</th>
                        <th class="sorting" style="width: 200px;"> 姓名 </th>
                        <th class="sorting" style="width: 200px;"> 设备ID </th>
                        <th class="sorting" style="width: 60px;" >操作 </th>
                    </tr>
                    </thead>

                    <tbody>
                        <#if users??>
                            <#list users as user>
                            <tr class="odd">
                                <td class="center  sorting_1">${user.accountName!}</td>
                                <td class="center  sorting_1">${user.realName!}</td>
                                <td class="center  sorting_1" id="device_id_${user.id!}">${user.deviceId!}</td>
                                <td class="center ">
                                    <a class="btn btn-info" id="reset_password_${user.id!}">
                                        <i class="icon-edit icon-white"></i>
                                        重设密码
                                    </a>
                                    <a class="btn btn-info" id="clear_device_id_${user.id!}">
                                        <i class="icon-edit icon-white"></i>
                                        清除设备ID
                                    </a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</div>

<div id="reset_password_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">重新设置用户密码</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">新密码</label>
                        <div class="controls">
                            <input id="newPassword1" class="input-xlarge focused" type="password" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">确认新密码</label>
                        <div class="controls">
                            <input id="newPassword2" class="input-xlarge focused" type="password" value="">
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="reset_password_btn" type="button" class="btn btn-primary">保存</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="clear_device_id_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">清除设备ID</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" style="width: 250px;" for="focusedInput">确定删除该用户的设备ID吗？</label>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="clear_device_id_btn" type="button" class="btn btn-primary">确定</button>
                </div>
            </div>
        </div>
    </div>
</div>

<input type="hidden" id="userId" value=""/>
<script type="text/javascript">
    $(function(){
        $("a[id^='reset_password_']").live('click',function(){
            var id = $(this).attr("id").substring("reset_password_".length);
            $('#userId').val(id);
            $('#newPassword1').val('');
            $('#newPassword2').val('');
            $('#reset_password_dialog').modal('show');
        });
        $("#reset_password_btn").live('click',function(){
            var userId = $('#userId').val();
            var newPassword1 = $('#newPassword1').val().trim();
            var newPassword2 = $('#newPassword2').val().trim();

            if(!check(newPassword1,newPassword2)){
                return false;
            }
            $.post('reset.vpage',{
                userId:parseInt(userId),
                newPassword:newPassword1
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    alert(data.info);
                    $('#reset_password_dialog').modal('hide');
                }
            });
        });

        $("a[id^='clear_device_id_']").live('click',function(){
            var id = $(this).attr("id").substring("clear_device_id_".length);
            $('#userId').val(id);
            $('#clear_device_id_dialog').modal('show');
        });

        $("#clear_device_id_btn").live('click',function(){
            var userId = $('#userId').val();

            $.post('clear_device_id.vpage',{
                userId:parseInt(userId)
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    alert(data.info);
                    clearDeviceIdTd(userId);
                    $('#clear_device_id_dialog').modal('hide');
                }
            });
        });

    });

    function clearDeviceIdTd(userId){
        $("#device_id_"+ userId).html("");
    }

    function check(password1,password2){
        if(password1 == ''){
            alert("请输入新密码!");
            return false;
        }
        if(password2 == ''){
            alert("请输入确认新密码!");
            return false;
        }
        if(password1 != password2){
            alert("两次输入的密码不一致!");
            return false;
        }
        return true;
    }
</script>
</@layout_default.page>
