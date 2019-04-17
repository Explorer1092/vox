<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="学分体系" page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <legend>
        学分设置
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <div class="form-horizontal">
                    <input type="hidden" id="id" name="id" value="${credit.id!}"/ad>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">行为名称</label>
                        <div class="controls">
                            <input type="text" id="name" name="name" class="form-control" value="${credit.name!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">行为终端</label>
                        <div class="controls">
                            <select class="selectpicker" id="itemId" name="itemId" onchange="selectType()">
                                <option value="" selected>选择终端</option>
                                <#list itemTerms as term>
                                    <option value="${term!}" <#if credit.term?? && credit.term == term>selected</#if>>${term!}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">行为类型</label>
                        <div class="controls">
                            <select class="selectpicker" id="itemTypeId" name="itemTypeId">
                                <option value="" selected>选择行为类型学</option>
                                <#list itemTypes as type>
                                    <option value="${type!}" <#if credit.itemType?? && credit.itemType == type>selected</#if>>${type!}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">分值</label>
                        <div class="controls">
                            <input type="text" id="creditValue" name="creditValue" class="form-control" value="${credit.value!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">学科</label>
                        <div class="controls">
                            <select id="subject" name="subject">
                                <option value="ENGLISH" <#if credit.subject?? && credit.subject == "ENGLISH">selected</#if>>英语</option>
                                <option value="MATH" <#if credit.subject?? && credit.subject == "MATH">selected</#if>>数学</option>
                                <option value="CHINESE" <#if credit.subject?? && credit.subject == "CHINESE">selected</#if>>语文</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">取消</a> &nbsp;&nbsp; <button id="add_ad_btn" type="button" class="btn btn-primary">保存</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">

    function validateInput(name, itemTypeId, creditValue, subject) {

        if(name == '') {
            alert('业务名称不允许为空！');
            return false;
        }
        if(itemTypeId == '') {
            alert('行为类型不允许为空！');
            return false;
        }
        if (creditValue == '' || creditValue == 0) {
            alert('分值不允许为空或大于0');
            return false;
        }
        if(subject == '') {
            alert('学科不允许为空！');
            return false;
        }
        return true;
    }

    function selectType() {
        //选中终端
        var term = $("#itemId").val();
        $("#itemTypeId").find("option:not(:first)").remove();
        $.get('type.vpage', {
            term:term
        }, function (data) {
            for (var i in data) {
                var temp = data[i];
                $("#itemTypeId").append(new Option(temp, temp));
            }
        })
    }

    $(function() {

        $("#add_ad_btn").on("click",function(){
            var name = $("#name").val().trim();
            var value = $("#creditValue").val().trim();
            var itemType = $("#itemTypeId").find('option:selected').val();
            var subject = $("#subject").find('option:selected').val();
            var id = $("#id").val();
            if(!validateInput(name, value)) {
                return false;
            }
            $.post('save.vpage',{
                id:id,
                name:name,
                subject:subject,
                itemType:itemType,
                value:value           },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.href = 'index.vpage';
                }
            });
        });

    });

</script>
</@layout_default.page>