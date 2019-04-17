<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="体验券查询" page_num=3>
<div id="main_container" class="span9">
    <div>
        <form method="post" action="list.vpage" class="form-horizontal">
            <legend>体验券查询</legend>
            <ul class="inline form_datetime">
                <li>
                    <label for="userId">
                        用户ID
                        <input name="userId" id="userId" type="text"/>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-primary">查询</button>
                </li>
            </ul>
        </form>
    </div>
    <br/>
    <#if datas?has_content>
        <div>
            <legend>查询结果：</legend>
            <table class="table table-hover table-striped table-bordered">
                <tr>
                    <th>用户ID</th>
                    <th> 创建时间</th>
                    <th> 优惠劵名称</th>
                    <th> 优惠劵号码</th>
                    <th> 是否使用</th>
                    <th> 操作</th>
                </tr>
                    <#list datas as data>
                        <tr>
                            <td>${data.userId!}</td>
                            <td>${data.createTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                            <td>${data.couponName!""}</td>
                            <td>${data.couponNO!""}</td>
                            <td id="used_${data.id!}">${data.used?string('是','否')}</td>
                            <td>
                                <#if !data.used>
                                    <a href="javascript:void(0);" id="change_used_${data.id!}">修改(使用)</a>
                                </#if>
                            </td>
                        </tr>
                    </#list>
            </table>
        </div>
    </#if>
</div>
<script type="text/javascript">
    <#if userId?has_content && userId lt 0>
        $('#userId').val('${userId!''}')
    </#if>
    $(function(){
        $("a[id^='change_used_']").on('click',function() {
            var couponId = $(this).attr("id").substring("change_used_".length);
            $.post('changeused.vpage',{
                couponId:couponId
            },function(data){
                if(!data.success){
                    alert("操作失败！");
                } else {
                    alert("操作成功！");
                    $('#used_'+couponId).html('是');
                    $('#change_used_'+couponId).remove();
                }
            });
        });
    });
</script>
</@layout_default.page>