<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='消息列表'>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th-list"></i> 消息列表 </h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper">

                <table class="table table-striped table-bordered bootstrap-datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">消息类型</th>
                        <th class="sorting" style="width: 300px;">消息内容</th>
                        <th class="sorting" style="width: 60px;">时间</th>
                        <th class="sorting" style="width: 60px;">下载附件</th>
                        <th class="sorting" style="width: 60px;" >操作 </th>
                    </tr>
                    </thead>

                    <tbody>
                        <#if notifies??>
                            <#list notifies as notify>
                                <tr class="odd">
                                    <td class="center  sorting_1">${notify.notifyType!}</td>
                                    <td class="center  sorting_1"><#if notify.notifyTitle?has_content><strong>${notify.notifyTitle!}</strong><br/></#if>${notify.notifyContent!}</td>
                                    <td class="center  sorting_1">${notify.createDatetime?string('yyyy-MM-dd HH:mm:ss')!}</td>
                                    <td class="center  sorting_1">
                                        <#if notify.file1??><a href="${notify.file1!}">附件1</a></#if>
                                        <#if notify.file2??><a href="${notify.file2!}">附件2</a></#if>
                                    </td>
                                    <td class="center ">
                                        <#if notify.readFlag?c = 'false'>
                                            <a class="btn btn-info" id="mark_notify_read_${notify.id!}">
                                                <i class="icon-edit icon-white"></i>
                                                标记已读
                                            </a>
                                        <#else>
                                            已读
                                        </#if>
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

<script type="text/javascript">
    $(function(){
        $("a[id^='mark_notify_read_']").live('click',function(){
            var id = $(this).attr("id").substring("mark_notify_read_".length);
            $.post('readnotify.vpage',{
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
</@layout_default.page>
