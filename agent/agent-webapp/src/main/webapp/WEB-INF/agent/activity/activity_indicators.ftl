<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='活动管理' page_num=19>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i>活动指标配置</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div class="form-horizontal" style="width: 80%">
                    <table class="table table-striped table-bordered">
                        <thead>
                        <tr>
                            <th class="sorting" style="width: 100px;">指标名称</th>
                            <th class="sorting" style="width: 20px;">本次使用</th>
                            <th class="sorting" style="width: 60px;">天玑展示名称</th>
                            <th class="sorting" style="width: 60px;">顺序</th>
                        </tr>
                        </thead>
                    <#if indicatorList?? && indicatorList?size gt 0>
                        <#list indicatorList as list>
                        <tr>
                            <td>${list.indicatorName !''}</td>
                            <td><input class="used" name="used" type="checkbox" value="${list.indicator!''}" <#if list?? && list.selected>checked</#if>></td>
                            <td><input class="alias" name="alias" type="text" value="${list.alias!''}"></td>
                            <td><input class="sortNo" name="sortNo" type="number" value="${list.sortNo!''}"></td>
                        </tr>
                        </#list>
                    </#if>
                </table>
                <div class="form-actions">
                    <button type="button" class="btn btn-primary submitBtn" data-info="0">取消</button>
                    <button type="button" class="btn btn-primary submitBtn" data-info="1">保存</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
$(function () {
    var activityId = getQuery('activityId');

    $('.submitBtn').on('click',function () {
        var info = $(this).data('info');
        if(info == 0){
            window.history.back();
        }else{
            var list = [];
            var item = $('input[name="used"]:checked');
            item.each(function (index,i) {
                if($(i).parents('tr').find('.alias').val().trim() == '' || $(i).parents('tr').find('.sortNo').val() == ''){
                    layer.alert('请填写完整的信息');
                    return;
                }else{
                    list.push({
                        indicator:$(i).val(),
                        alias:$(i).parents('tr').find('.alias').val().trim(),
                        sortNo:$(i).parents('tr').find('.sortNo').val().trim()
                    })
                }
            });
            if(item.length == list.length){
                $.post('save_indicators.vpage',{
                    activityId:activityId,
                    indicators:JSON.stringify(list)
                },function (res) {
                    if(res.success){
                        layer.alert('保存成功',function () {
                            window.history.back();
                        });
                    }else{
                        layer.alert(res.info);
                    }
                });
            }

        }
    });
});
</script>
</@layout_default.page>
