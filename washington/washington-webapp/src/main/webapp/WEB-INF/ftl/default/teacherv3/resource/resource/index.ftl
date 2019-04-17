<#import "../module.ftl" as com>
<@com.page t=1>
<#if pagination?exists && integraltotal?exists>
	<div id="resource_list_box">
		<table class="table_vox">
			<thead>
				<tr>
					<th width="130px;">审核通过的资源总数:</th>
					<th style="color:red;">${pagination.total!}</th>
					<th width="80px;">园丁豆总数:</th>
					<th style="color:red;">${integraltotal!}</th>
				</tr>
			</thead>
		</table>
		<table class="tab_all" style="margin-top:10px;">
			<thead>
				<tr>
					<th width="70">编号</th>
					<th width="60">分类</th>
					<th>题型</th>
					<th width="80">上传获得园丁豆数量</th>
					<th width="80">学生使用次数</th>
					<th width="80">使用获得园丁豆数量</th>
					<th width="50">园丁豆合计</th>
				</tr>
			</thead>
			<tbody>
			<#if pagination.pageCount gt 0>
			<#list pagination.rows as rs>
				<tr>
					<th>
						<div  class="numtitle">
							<span v="${rs.examinId!}" class="numbers resourceid_but">${rs.examinId!}</span>
							<#--
								<div id="numdetail_box_${rs.examinId!}" class="numdetail" style="display:none;">
									<div class="tle"><span class="arrow"></span><span class="text">知识点：${rs.examination.point.key!} <b class="color_orange">${rs.examination.point.value!}</b>   难度：<b class="color_orange">${rs.examination.level!}</b>   分类：<b class="color_orange">${rs.examination.grade!}</b></span><span class="close"><s v="${rs.examinId!}" class="icon_new_all failure_g_u"></s></span></div>
									<#list rs.examination.questions as que >
										<div class="ctn">
											<div class="answer"><span>答案：${que.answer!}</span>${que.content.text!}<div class="clear"></div></div>
											<dl>
												<dt></dt>
												<dd>
													<p></p>
												</dd>
												<dd class="clear"></dd>
											</dl>
										</div>
									</#list>
									<div class="btn">
										<a class="public_x_b blue_x_b" href="javascript:void(0);"><i><span>题目纠错</span></i></a>
										<a class="public_x_b blue_x_b" href="javascript:void(0);"><i><span>查看精讲</span></i></a>
										<a class="public_x_b blue_x_b" href="javascript:void(0);"><i><span>添加精讲</span></i></a>							
									</div>						
								</div>
							-->
						</div>
					</th>
					<th>${rs.examinType!}</th>
					<th>${rs.examinQuestions!}</th>
					<th>${rs.uploadIntegral!}</th>
					<th>${rs.userTimes!}</th>
					<th>${rs.userIntegralNum!}</th>
					<th>${rs.integralTotal!}</th>
				</tr>
				</#list>
				<#else>
					<tr><td colspan="7" style="text-align:center;">您还没有审核通过的资源！</td></tr>
				</#if>	
			</tbody>
		</table>
	</div>
	<div class="common_pagination resource_list" ></div>
    <script type="text/javascript">
		function createPageList( index ){
			$("#resource_list_box").html( '<div style="height: 200px; text-align: center; float: right; width: 100%;"></div>' );
			$.get('/teacher/resource/list-'+index+'.vpage', function( data ){
				$("#resource_list_box").html( data );
			});
		}
		
		$(function(){
			<#--
				$(".resourceid_but").live("click", function(){
					var _this = $(this);
					var _id = _this.attr("v");
					$("#numdetail_box_"+ _id +"").show();
				});
				
				$(".failure_g_u").live("click", function(){
					var _this = $(this);
					var _id = _this.attr("v");
					$("#numdetail_box_"+ _id +"").hide();
				});
			-->
            $(".resource_list").page({
                total           : ${pagination.pageCount!0},
                jumpCallBack    : createPageList
            });
        });
	</script>	
<#else>
	<div class="info_rmation"><s class="tip_icon tip_48 tip_48_4"></s>您还没有审核通过的资源！</span> </div>
</#if>	
</@com.page>	
