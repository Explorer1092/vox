<#import '../layout.ftl' as layout>

<#include "../constants.ftl">

<@layout.page className='OrderList parentApp-bgColor' pageJs="second" title="订单列表"  extraJs=extraJs![] >
    <#escape x as x?html>
		<div class="parentApp-myOrder">
			<#list [
				{
					"href" : "/parentMobile/ucenter/orderlistForInterest.vpage",
					"title" : "趣味学习订单",
					"memo" : "可查看趣味学习订单"
				}
				] as category>
                <#if category.gray!true>
                    <a href="${category.href}" class="box boxBg-${category_index + 1}">
                        <span class="head">${category.title}</span>
                        <span class="text">${category.memo}</span>
                    </a>
				</#if>
		</#list>
    </#escape>
</@layout.page>
