<#if teacherResources?exists && teacherResources.hasContent()>
<div class="w-table w-table-border">
    <table>
        <thead>
        <tr>
            <td>文档标题</td>
            <td>上传时间</td>
            <td>文件操作</td>
        </tr>
        </thead>
        <tbody>
            <#list teacherResources.getContent() as rs>
            <tr>
                <td>${rs.fileName!}</td>
                <td>${rs.createTime!}</td>
                <th><a href="/teacher/resource/download.vpage?resourceId=${rs.gfsId!}" target="_blank">下载</a></th>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<script type="text/javascript">
    $(function () {
        $(".source_list").page({
            total: ${teacherResources.getTotalPages()!},
            current: ${currentPage!},
            jumpCallBack: createPageList
        });
    });
</script>
<#else>
<div class="w-noData-box">
    <span class="w-icon-md">您没有上传过资源！</span>
    <#--<a href="/teacher/resource/index.vpage" class="w-btn w-btn-small">
        上传资源
    </a>-->
</div>
</#if>
