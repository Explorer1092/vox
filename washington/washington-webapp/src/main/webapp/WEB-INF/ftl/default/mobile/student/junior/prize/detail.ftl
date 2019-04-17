<#include "../../../common/config.ftl">

<#assign extraJs = [
	{
		"path" : "${DEFAULT_CONFIG.js.jquery}",
		"spcial" : true,
		"addKid" : false
	},
	{
		"path" : "slide"
	}
]>

<#include "../layout.ftl">

<@layout.page className=CONFIG.CSSPRE + 'reward_detail studentJuniorSchool-bgWhite' title="奖品详情" headBlock=headBlock bottomBlock=bottomBlock>
	<#if result.success>
		<div class="studentJuniorSchool-mallInfo">
			<div class="infoFocus doSlide">
				<ul class="focusInner">
					<#list detail.images as image>
						<li class="focusBox">
							<img src="<@app.avatar href='${image.location!""}'/>" alt="">
						</li>
					</#list>
				</ul>
				<div class="focusTag doSlideDots">
					<#list detail.images as image>
                        <div class="doDot ${(image_index == 0)?string("active", "")}"></div>
					</#list>
				</div>
			</div>
			<div class="infoText">
				<div class="textHead">
					<p>${detail.productName!""}</p>
					<span>${detail.price!""}</span>
				</div>
				<div class="textMain">
					<div>奖品描述：</div>
					<p>${detail.description!""}</p>
				</div>
			</div>
		</div>
		<div class="studentJuniorSchool-footSubmit studentJuniorSchool-footSubmitWhite studentJuniorSchool-footSubmitFixed">
			<div class="footInner"><a href="javascript:;" class="btnSubmit">奖品兑换即将开始，敬请期待</a></div> <#-- 如果按钮可点击，在此处加上“btnSubmit-green”样式 -->
		</div>
	</#if>
</@layout.page>

