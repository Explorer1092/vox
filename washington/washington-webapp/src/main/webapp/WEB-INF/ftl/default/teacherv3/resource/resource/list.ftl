<#import "../module.ftl" as temp>
<@temp.page title="资源" level="我的上传">
	<div  id="resource_list_box" style="padding: 10px;">
		<#--内容显示区-->
	</div>
	<div class="message_page_list source_list"></div>
    <script type="text/javascript">
		function createPageList( index ){
			$("#resource_list_box").html( '<div style="height: 200px; text-align: center; float: right; width: 100%;">数据加载中...</div>' );
			$.get('/teacher/resource/listhtml.vpage?currentPage='+index, function( data ){
				$("#resource_list_box").html( data );
			});
		}
		
		$(function(){
		     createPageList(1);
		});
	</script>
</@temp.page>




