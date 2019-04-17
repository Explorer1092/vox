<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">
<result >
	<status >${status}</status>
	<errorinfo >${errorinfo}</errorinfo>
	<#if status=='success'>
		<parentList>
		<#list parents as ps>  			
			<parent>
				<userName>${ps.realname}</userName>
				<userId>${ps.id}</userId>
				<userPhoto><@app.avatar href="${ps.imgUrl!}"/></userPhoto>
			</parent>  				
	   	</#list>
	   	</parentList>
	<#else>
		<userInfo>null</userInfo>
	</#if>
</result>
</ns2:Lesson>