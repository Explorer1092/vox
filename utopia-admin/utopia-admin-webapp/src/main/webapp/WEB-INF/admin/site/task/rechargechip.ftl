<#-- @ftlvariable name="adminDictList" type="java.util.List<com.voxlearning.utopia.admin.persist.entity.AdminDict>" -->
<ul class="pager">
<#if (teachers.hasPrevious())>
    <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
<#else>
    <li class="disabled"><a href="#">上一页</a></li>
</#if>
<#if (teachers.hasNext())>
    <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
<#else>
    <li class="disabled"><a href="#">下一页</a></li>
</#if>
    <li>当前第 ${pageNumber!} 页 |</li>
    <li>共 ${teachers.totalPages!} 页</li>
</ul>
<div>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>老师ID</th>
            <th>完成学生数量</th>
            <th>月份</th>
            <th>充值金额</th>
            <th>是否满足充值</th>
        </tr>
    <#if teachers.content??>
        <#list teachers.content as teacher>
            <tr>
                <th>${teacher.teacherId!}</th>
                <td>${teacher.stuCount!}</td>
                <td>${teacher.month!}</td>
                <td>${teacher.rechargeAmount!}</td>
                <td><#if teacher.recharged>是<#else>否</#if></td>
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