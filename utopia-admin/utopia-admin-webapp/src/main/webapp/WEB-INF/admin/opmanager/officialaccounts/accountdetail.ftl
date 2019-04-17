<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑公众号" page_num=16>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/advertisement/advertisement.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        ${accounts.name}－基本信息
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel"
           class="btn">返回</a> &nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered" >
                    <thead>
                        <tr>
                            <th>公众号ID</th>
                            <th>公众号名称</th>
                            <th>允许主动关注</th>
                            <th>创建时间</th>
                            <th>关注用户数</th>
                            <th>高级管理员</th>
                            <th>普通管理员</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <th>${accountId}</th>
                            <td>${accounts.name}</td>
                            <td>${accounts.followLimit?string('是','否')}</td>
                            <td>${accounts.createDatetime}</td>
                            <td></td>
                            <td>${accounts.seniorAdminUsers}</td>
                            <td>${accounts.generalAdminUsers}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <legend style="margin-bottom: 10px;">当前状态
        <labe>操作: </labe>
        <#switch accounts.status>
            <#case "Online">
                <button name="offline-btn" class="btn btn-danger legend-btn" ${(isTopAuditor || isSeniorAuditor)?string('','disabled="disabled"')}>下线</button>
                <#break>
            <#case "Offline">
                <button name="online-btn" class="btn btn-success legend-btn" ${(isTopAuditor || isSeniorAuditor)?string('','disabled="disabled"')}>上线</button>
                <#break>
            <#default>
        </#switch>
    </legend>
    <div>
        <strong style="font-size:25px;">${(accounts.status == "Online")?string('上线','已下线')}</strong>
    </div>
    <legend style="margin-top: 25px;margin-bottom: 10px;">公众号说明
        <#if isTopAuditor || isSeniorAuditor>
        <button type="button" class="btn legend-btn" id="edit-instruction-btn">编辑</button>
        </#if>
    </legend>
    <div style="margin-bottom: 50px">
        <div id="instruction">${accounts.instruction!''}</div>
        <div id="edit-instruction" style="display:none">
            <textarea cols="10" rows="6" maxlength="200"></textarea>
            <button class="btn" id="save-instruction-btn">保存</button>
        </div>
    </div>

    <#if accountId?? && accounts??>
        <legend>公众号图标</legend>
        <div class="well">
            <table class="table table-striped table-condensed table-bordered">
                <tr>
                    <td>素材预览</td>
                    <td>素材地址</td>
                    <td>操作</td>
                </tr>
                <tr style="display: none;">
                    <td></td><td></td><td></td>
                    <td><form id="form_wtf" onsubmit="return false;"></form></td>
                </tr>
                <tr>
                    <td style="position:relative;">
                        <#if accounts.imgUrl?? && accounts.imgUrl?has_content>
                            <img id="imgSrc" src="${accounts.imgUrl!}" style="height:150px;"/>
                        <#else>
                            <div class="img-size-tip" style="font-size:2.5em;color:#ccc;float:left;position:absolute;left:0;top:40%;height:100%;width:100%;">100x100</div>
                            <img id="imgSrc" style="height:150px;"/>
                        </#if>
                    </td>
                    <td><#if accounts.imgUrl?? && accounts.imgUrl?has_content><a href="${accounts.imgUrl!}">${accounts.imgUrl!}</a></#if></td>
                    <td>
                        <#if isTopAuditor || isSeniorAuditor>
                        <form id="form_img" name="form_img" enctype="multipart/form-data" action="uploadsrc.vpage" method="post">
                            <input name="accountId" type="hidden" value="${accountId!0}" />
                            <a href="javascript:void(0);" class="uploader">
                                <input type="file" name="file" id="file" accept="image/*">选择素材
                            </a>
                            <input type="hidden" name="type" value="img" >
                            <a title="确认上传" href="javascript:void(0);" class="uploader" id="upload_img">
                                <i class="icon-ok"></i>
                            </a>
                            <#if accounts.imgUrl?? && accounts.imgUrl?has_content>
                                <a title="删除" href="javascript:void(0);" class="uploader" id="clear_img">
                                    <i class="icon-trash"></i>
                                </a>
                            </#if>
                        </form>
                        </#if>
                    </td>
                </tr>
            </table>
        </div>
    </#if>
</div>
<script type="text/javascript">
    $(function(){
        $('#file').on('change',function(){
            $(".img-size-tip").remove();
            previewImg(this);
        });
        function previewImg(file) {
            var prevDiv = $('#imgSrc');
            if (file.files && file.files[0]) {
                var reader = new FileReader();
                reader.onload = function(evt) {
                    prevDiv.attr("src", evt.target.result);
                };
                reader.readAsDataURL(file.files[0]);
            }
            else {
                prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
            }
        }

        // 上传、清除素材
        $("a[id^='upload_']").on('click', function () {
            var type = $(this).attr("id").substring("upload_".length);
            $("#form_" + type).submit();
        });

        $("form[id^='form_']").on('submit', function () {
            $(this).ajaxSubmit({
                type: 'post',
                url: 'uploadsrc.vpage',
                success: function (data) {
                    if (data.success) {
                        alert("素材上传成功！");
                    } else {
                        alert("素材上传失败:" + data.info);
                    }
                    window.location.reload();
                },
                error: function (msg) {
                    alert("素材上传失败！");
                }
            });
            return false;
        });

        $("a[id^='clear_']").on('click', function () {
            if (!confirm("是否确认删除？")) {
                return false;
            }
            var type = $(this).attr("id").substring("clear_".length);
            $.post('clearsrc.vpage', {accountId: ${accountId!0}, type: type}, function (data) {
                if (!data.success) {
                    alert("清除素材失败:" + data.info);
                }
                window.location.reload();
            });
        });

        $('#frm').on('submit',function () {

            $('#frm').ajaxSubmit({
                type: 'post',
                url: 'save.vpage',
                success : function(data) {
                    if (data.success) {
                        alert("保存成功");
                        window.location.href = 'accountdetail.vpage?accountId='+data.id;
                    } else {
                        alert(data.info);
                        clearImg();
                    }
                },
                error: function() {
                    alert("保存失败！");
                }
            });
            return false;
        });

        $('#save_btn').on('click', function () {
            var detail = {
                accountId: $('#accountId').val(),
                name: $('#name').val().trim()
            };
            if (validateInput(detail)) {
                if (confirm("是否确认保存？")) {
                    $('#frm').submit();
                }
            }
        });

        function validateInput(detail) {
            var msg = "";
            if (detail.name == '') {
                msg += "请输入公众号名称！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        }

        $(document).on('click', 'button[name=online-btn]', function () {
            var $this = $(this);
            $.post('accountonline.vpage', {accountId: ${accountId}}, function (res) {
                if (res.success) {
                    //$this.removeClass('on-btn').addClass('off-btn').html('下线');
                    alert("上线成功!");
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        $(document).on('click', 'button[name=offline-btn]', function () {
            var $this = $(this);
            $.post('accountoffline.vpage', {accountId: ${accountId}}, function (res) {
                if (res.success) {
                    // $this.removeClass('off-btn').addClass('on-btn').html('上线');
                    alert("下线成功!");
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        $("button#edit-instruction-btn").click(function(){
            $("div#instruction").hide();
            $("div#edit-instruction").show();

            // 添上最新的值
            var $area = $("div#edit-instruction > textarea");
            $area.val($("div#instruction").html());
        });

        $("button#save-instruction-btn").click(function(){
            $.post("updateinstruction.vpage",
                    {"accountId":${accountId},"instruction":$("div#edit-instruction > textarea").val()},
            function(data){
                if(data.success){
                    alert("修改成功!");
                    $("div#instruction").html(data.newInstruction).show();
                    $("div#edit-instruction").hide();
                }else{
                    alert(data.info);
                }
            });
        });
    });
</script>
<style>
    .legend-btn{
        margin-bottom: 5px;
    }
</style>
</@layout_default.page>