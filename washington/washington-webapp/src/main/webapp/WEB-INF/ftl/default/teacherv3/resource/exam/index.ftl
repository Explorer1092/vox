<#import "../module.ftl" as temp>
<@temp.page title="资源" level="我的试卷">
<div style="margin: 10px;">
	<table class="table_vox">
		<thead>
			<tr>
				<td width="150">创建时间</td>
				<td>教材名称</td>
				<th width="120">单元</th>
				<th width="80">操作</th>
			</tr>
		</thead>	
		<tbody id="exam_list_box"></tbody>	
	</table>
	<div id="message_page_list" class="message_page_list"></div>
</div>
<script type="text/javascript">
	function getexamList( index ){
		$("#exam_list_box").html( '<tr><td colspan="4" style="text-align:center;">数据加载中...</td></tr>' );
		$.get("/teacher/resource/exam/list/" + index + ".vpage", function( data ){
			$("#exam_list_box").html( data );
		});
	}

	$(function(){
		getexamList( 1 );
	});
</script>	
</@temp.page>