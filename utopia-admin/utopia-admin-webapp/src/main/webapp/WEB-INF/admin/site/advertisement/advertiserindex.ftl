<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="Advertiser Management" page_num=4>
<div id="main_container" class="span9">
    <legend>
        广告主管理
        &nbsp;&nbsp;&nbsp;&nbsp;
        <a href="advertisementindex.vpage">广告管理</a>
        <button id="add_advertiser_btn" type="button" class="btn btn-info" style="float: right">增加</button>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" class="form-horizontal" method="post" action="advertiserindex.vpage" >
                    <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                    机构名称：<input type="text" class="input-xlarge" placeholder="机构名称" name="name" value="${name!}">
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    创建时间：
                    <input id="startDate" type="text" class="input-small" placeholder="开始时间" name="startDate" value="${startDate!}">~
                    <input id="endDate" type="text" class="input-small" placeholder="结束时间" name="endDate" value="${endDate!}">
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="submit" class="btn btn-primary">查询</button>
                </form>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>ID</td>
                        <td>机构名称</td>
                        <td>联系人</td>
                        <td>联系人电话</td>
                        <td>联系人QQ</td>
                        <td>创建日期</td>
                        <td>接口人</td>
                        <td>操作</td>
                    </tr>
                    <#if advertiserList?? >
                        <#list advertiserList as advertiser >
                            <tr>
                                <td>${advertiser.id!}</td>
                                <td id="name_${advertiser.id!}">${advertiser.name!}</td>
                                <td id="contactUser_${advertiser.id!}">${advertiser.contactUser!}</td>
                                <td id="contactPhone_${advertiser.id!}">${advertiser.contactPhone!}</td>
                                <td id="contactQQ_${advertiser.id!}">${advertiser.contactQQ!}</td>
                                <td>${advertiser.createDatetime?string('yyyy-MM-dd HH:mm:ss')!}</td>
                                <td id="personInCharge_${advertiser.id!}">${advertiser.personInCharge!}</td>
                                <td>
                                    <a id="edit_${advertiser.id!}" href="javascript:void(0);">编辑</a>
                                    <a id="delete_${advertiser.id!}" href="javascript:void(0);">删除</a>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</div>

<div id="add_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h3 class="modal-title">添加/编辑广告主</h3>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible;">
                    <div class="control-group">
                        <label class="col-sm-2 control-label"><strong>机构名称</strong></label>
                        <div class="controls">
                            <input type="text" id="name" width="25px"/><span style="color: red">*必填</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label"><strong>联系人</strong></label>
                        <div class="controls">
                            <input type="text" id="contact_user" width="25px"/><span style="color: red">*必填</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label"><strong>联系人手机号</strong></label>
                        <div class="controls">
                            <input type="text" id="contact_phone" width="25px"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label"><strong>联系人QQ</strong></label>
                        <div class="controls">
                            <input type="text" id="contact_qq" width="25px"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label"><strong>接口人</strong></label>
                        <div class="controls">
                            <input type="text" id="person_in_charge" width="25px"/><span style="color: red">*必填</span>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_modal_submit" type="button" class="btn btn-primary">保存</button>
                </div>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<input type="hidden" id="curid" value=""/>
<script type="text/javascript">

    Date.prototype.format = function(format){
        var o = {
            "M+" : this.getMonth()+1, //month
            "d+" : this.getDate(), //day
            "h+" : this.getHours(), //hour
            "m+" : this.getMinutes(), //minute
            "s+" : this.getSeconds(), //second
            "q+" : Math.floor((this.getMonth()+3)/3), //quarter
            "S" : this.getMilliseconds() //millisecond
        }

        if(/(y+)/.test(format)) {
            format = format.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
        }

        for(var k in o) {
            if(new RegExp("("+ k +")").test(format)) {
                format = format.replace(RegExp.$1, RegExp.$1.length==1 ? o[k] : ("00"+ o[k]).substr((""+ o[k]).length));
            }
        }
        return format;
    }
    function isNumber(value){
        var reg = /^[0-9]+$/;
        if($.trim(value) == '' || !reg.test(value)){
            return false;
        }
        return true;
    }

    function loadAddDialog(){
        $('#add_dialog').modal('show');
        $('#name').val('');
        $('#contact_user').val('');
        $('#curid').val('');
        $("#contact_phone").val('');
        $("#contact_qq").val('');
        $("#person_in_charge").val('');
    }

    function loadEditDialog(id){
        $('#add_dialog').modal('show');
        $('#name').val($('#name_'+id).html().trim());
        $('#contact_user').val($('#contactUser_'+id).html().trim());
        $('#curid').val(id);
        $("#contact_phone").val($('#contactPhone_'+id).html().trim());
        $("#contact_qq").val($('#contactQQ_'+id).html().trim());
        $("#person_in_charge").val($('#personInCharge_'+id).html().trim());
    }

    function validateInput(name, contact_user, contact_phone, contact_qq, person_in_charge) {
        if(name.trim() == ''){
            alert("请填写机构名称!");
            return false;
        }
        if(contact_user.trim() == ''){
            alert("请填写联系人名称");
            return false;
        }
        if(person_in_charge.trim() == ''){
            alert("请填写接口人名称");
            return false;
        }

        if(contact_phone.trim() == '' && contact_qq.trim() == ''){
            alert("联系人手机号和QQ至少填写一项！");
            return false;
        }
        return true;
    }
    $(function(){

        $("#startDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });


        $("#add_advertiser_btn").on("click",function(){
            loadAddDialog();
        });

        $("#btn_modal_submit").on("click",function(){
            var name = $("#name").val();
            var contactUser = $("#contact_user").val();
            var contactPhone = $("#contact_phone").val();
            var contactQQ = $("#contact_qq").val();
            var personInCharge = $("#person_in_charge").val();
            var id = $("#curid").val();
            if(!validateInput(name, contactUser, contactPhone, contactQQ, personInCharge)) {
                return false;
            }
            $.post('saveadvertiser.vpage',{
                id:id,
                name:name,
                contactUser:contactUser,
                contactPhone:contactPhone,
                contactQQ:contactQQ,
                personInCharge:personInCharge
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });

        $('[id^="edit_"]').on('click', function(){
            loadEditDialog($(this).attr("id").substring("edit_".length));
        });

        $('[id^="delete_"]').on('click', function(){
            if(!confirm("确定要删除吗？")){
                return false;
            }
            var id = $(this).attr("id").substring("delete_".length);
            $.post('deladvertiser.vpage',{
                id:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });
</script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
</@layout_default.page>