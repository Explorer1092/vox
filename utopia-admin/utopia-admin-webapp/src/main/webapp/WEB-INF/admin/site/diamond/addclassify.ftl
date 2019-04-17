<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="金刚位管理" page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <legend>
        分类设置
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <div class="form-horizontal">
                    <input type="hidden" id="id" name="id" value="${types.id!}"/>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">中文名称</label>
                        <div class="controls">
                            <input type="text" id="chName" name="chName" class="form-control" value="${types.chName!}"/>
                            <span style="color: red">*</span>不允许为空
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">英文名称</label>
                        <div class="controls">
                            <input type="text" id="enName" name="enName" class="form-control" <#if types.enName?? && types.enName != ''>disabled="disabled"</#if> value="${types.enName!}"/>
                            <span style="color: red">*</span>不允许为空
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">取消</a> &nbsp;&nbsp;
                            <button id="add_ad_btn" type="button" class="btn btn-primary">保存</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function validateInput(chName, enName) {
        if(chName == '') {
            alert('中文分类名称不允许为空！');
            return false;
        }
        if(enName == '') {
            alert('英文分类名称不允许为空！');
            return false;
        }
        return true;
    }

    $(function() {
        $("#add_ad_btn").on("click",function(){
            var id = $("#id").val();
            var chName = $("#chName").val().trim();
            var enName = $("#enName").val().trim();
            if(!validateInput(chName, enName)) {
                return false;
            }
            if ('' == id) {
                $.post('add.vpage',{
                    id:id,
                    chName:chName,
                    enName:enName
                },function(data){
                    if(!data.success){
                        alert(data.info);
                    }else{
                        window.location.href = 'index.vpage';
                    }
                });
            } else {
                $.post('update.vpage',{
                    id:id,
                    chName:chName,
                    enName:enName
                },function(data){
                    if(!data.success){
                        alert(data.info);
                    }else{
                        window.location.href = 'index.vpage';
                    }
                });
            }
        });
    });

</script>
</@layout_default.page>