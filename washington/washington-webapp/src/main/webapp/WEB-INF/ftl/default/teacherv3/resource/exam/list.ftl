<#if (resultset.teacherExamPaperForUnits)?exists && resultset.teacherExamPaperForUnits?size gt 0>
	<#list resultset.teacherExamPaperForUnits as re>
		<tr>
			<td>${(re.createAt)!}</td>
			<td>${(re.paper.book.name)!}</td>
			<th>${(re.paper.unit.name)!}</th>
			<th><a target="_blank" href="/teacher/exam/previewresource.vpage?examId=${re.id!}&owener=my"><s class="icon_new_all detail_u"></s> 查看详情</a></th>
		</tr>
	</#list>
    <script type="text/javascript">
		$(function(){
            $("#message_page_list").page({
                total           : ${resultset.totalPages!},
                current         : ${currentPage!0},
                jumpCallBack    : getexamList
            });
		});
	</script>
<#else>
	<tr>
        <td colspan="4" style="text-align: center;" >
           暂无相关数据
        </td>
	</tr>
</#if>
