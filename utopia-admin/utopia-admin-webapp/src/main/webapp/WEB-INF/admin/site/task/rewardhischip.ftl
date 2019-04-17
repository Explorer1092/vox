<#-- @ftlvariable name="adminDictList" type="java.util.List<com.voxlearning.utopia.admin.persist.entity.AdminDict>" -->
<ul class="pager">
<#if (rewardHistories.hasPrevious())>
    <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
<#else>
    <li class="disabled"><a href="#">上一页</a></li>
</#if>
<#if (rewardHistories.hasNext())>
    <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
<#else>
    <li class="disabled"><a href="#">下一页</a></li>
</#if>
    <li>当前第 ${pageNumber!} 页 |</li>
    <li>共 ${rewardHistories.totalPages!} 页</li>
</ul>
<div>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>老师ID</th>
            <th>学校ID</th>
            <th>月份</th>
            <th>奖励园丁豆</th>
        </tr>
    <#if rewardHistories.content??>
        <#list rewardHistories.content as history>
            <tr>
                <th>${history.teacherId!}</th>
                <td>${history.schoolId!}</td>
                <td>${history.month!}</td>
                <td>${history.amount!}</td>
            </tr>
        </#list>
    </#if>
    </table>
</div>
<script type="text/javascript">
    function pagePost(pageNumber){
        $('#data_table').load('gettaskdata.vpage',
                {taskName : $('#taskName').val(),pageNumber : pageNumber}
        );
    }

</script>