<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='查看事项' page_num=6>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>查看事项</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content school_manage">
            <form class="form-horizontal">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">类型</label>
                    <div class="controls">
                        <select id="typeId" class="js-postData form-control js-needed" disabled data-einfo="请选择类型" name="typeId" style="width: 280px;">
                            <option value="">请选择</option>
                            <#if dataList?has_content && dataList?size gt 0>
                                <#list dataList as list>
                                    <option value="${list.id!''}" <#if (list.id!0) == (selfHelp.typeId!0)> selected</#if>>${list.typeName!''}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">事项名称</label>
                    <div class="controls">
                        <input name="title" id="title" value="${selfHelp.title!''}" class="js-postData input-xlarge focused js-needed" readonly type="text" data-einfo="请填写事项名称">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">联系人姓名</label>
                    <div class="controls">
                        <input id="contact" name="contact" value="${selfHelp.contact!''}" class="js-postData input-xlarge focused js-needed" readonly type="text" data-einfo="请填写联系人姓名">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">邮箱</label>
                    <div class="controls">
                        <input id="email" name="email" value="${selfHelp.email!''}" class="js-postData input-xlarge focused js-needed" readonly type="text" data-einfo="请填写邮箱">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">微信群</label>
                    <div class="controls">
                        <input id="wechatGroup" name="wechatGroup" value="${selfHelp.wechatGroup!''}" class="js-postData input-xlarge focused js-needed" readonly type="text" data-einfo="请填写微信群">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">说明</label>
                    <div class="controls">
                        <textarea name="comment" id="comment" class="js-postData input-xlarge focused js-needed" readonly data-einfo="请填写说明" cols="30" rows="5">${selfHelp.comment?replace('<br>','\n')}</textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">相关资料</label>
                    <div class="controls">
                    <#list packets as p>
                        <p>${p.contentTitle!''}</p>
                    </#list>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn btn-primary submitBtn" data-info="0">返回</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        $(document).on('click','.submitBtn',function () {
            var info = $(this).data('info');
            if(info==0){
                window.history.back();
            }
        })
    });
</script>
</@layout_default.page>
