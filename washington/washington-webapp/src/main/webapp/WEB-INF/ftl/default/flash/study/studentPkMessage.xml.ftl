<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:PK xmlns:ns2="http://jaxb.vlesson.vlt.com">
<userId>${userId!''}</userId>
<homeworkId>${homeworkId!''}</homeworkId>
<classId>${classId!''}</classId>
<imgUrl><@app.link href="${imgUrl!''}" /></imgUrl>
<time>${time!''}</time>
<pkhomeworklist>
<#if pkHomeworks?exists>
	<#list pkHomeworks as pkh>
		<pkhomework>
			<bookId>${pkh.bookId!''}</bookId>
			<unitId>${pkh.unitId!''}</unitId>
			<lessonId>${pkh.lessonId!''}</lessonId>
			<practiceType>${pkh.practiceType!''}</practiceType>
			<practiceName>${pkh.practiceTypeName!''}</practiceName>
			<score>${pkh.score!''}</score>	
			<randomPk>${pkh.randomPk!''}</randomPk>
			<choicePk>${pkh.choicePk!''}</choicePk>		
		</pkhomework>
	</#list>
<#else>
<pkhomework>null</pkhomework>
</#if>
</pkhomeworklist>
<pkuserlist>
<#if pkUsers?exists>
	<#list pkUsers as pk>
		<pkuser>
			<userId>${pk.userId!''}</userId>
			<realName>${pk.realName!''}</realName>
			<pkUserId>${pk.choicePkUserId!''}</pkUserId>
			<bePkUser>${pk.beChoicePk!''}</bePkUser>
			<randomPkUser>${pk.randomPk!''}</randomPkUser>
			<imgUrl><@app.link href="${pk.imgUrl!''}" /></imgUrl>	
			<actionType>${pk.actionType!''}</actionType>
			<finished>${pk.finished!''}</finished>
		</pkuser>
	</#list>
<#else>
	<pkuser>null</pkuser>
</#if>
</pkuserlist>
</ns2:PK>