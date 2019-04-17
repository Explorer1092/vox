<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<#list map_top_list as map>
		<#if map_index % 2 == 0>
			<tr>
		</#if>
		<#if map_index % 2 != 0>
			<td width="1%" rowspan="2" valign="top" style=" font-size:14px; font-weight:bold; padding: 0 0 20px"><img src="http://www.17zuoye.com/public/app/default/images/list_mid.png"></td>
		</#if>
			<td valign="top" width="50%" style=" font-size:14px; font-weight:bold; padding: 0 0 30px">
				<table width="100%" border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td>学校：<span style="color:#ff0000;">${map.schoolName!'某某'}</span></td>
						<td width="1%" align="right"><img src="http://www.17zuoye.com/public/app/default/images/logo_small.png"></td>
					</tr>
					<tr>
						<td colspan="2">英语老师：<span style="color:#ff0000;">${map.teacherName!'xxx'}</span></td>
					</tr>
					<tr>
						<td colspan="2">班级：<span style="color:#ff0000;">${map.clazzName!'xxx'}</span></td>
					</tr>
					<tr>
						<td colspan="2">姓名：<span style="color:#ff0000;">${map.name!'某某'}</span></td>
					</tr>
					<tr>
						<td colspan="2">一起作业学号：<span style="color:#ff0000;">${map.userId!'xxx'} </span></td>
					</tr>
					<tr>
						<td colspan="2">密码：<span style="color:#ff0000;">${map.pwd!'xxx'}</span></td>
					</tr>
					<tr>
						<td colspan="2">网址：<span style="color:#ff0000;">www.17zuoye.com</span></td>
					</tr>
				</table>
			</td>
			
		<#if map_index % 2 != 0>
			</tr>
			<tr>
				<td valign="top" style=" font-size:14px; font-weight:bold; "><img src="http://www.17zuoye.com/public/app/default/images/list_bot.png" width="293" height="32"></td>
				<td valign="top" style=" font-size:14px; font-weight:bold; "><img src="http://www.17zuoye.com/public/app/default/images/list_bot.png" width="293" height="32"></td>
			</tr>
		</#if>
	</#list>
</table>
