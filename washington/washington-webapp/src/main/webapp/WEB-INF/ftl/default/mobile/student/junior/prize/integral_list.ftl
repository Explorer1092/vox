<#assign extraJs = [
	{
        "path" : "prize"
	}
]>

<#include "../layout.ftl">

<@layout.page className=CONFIG.CSSPRE + 'integralList' title="学豆记录" headBlock=headBlock bottomBlock=bottomBlock>
	<div id="integralList">
		<ul v-if = "integralList.length > 0" class="studentJuniorSchool-mallRecordList">
			<li v-for="integral in integralList">
				<div v-if="integral.integral < 0" class="listNum listMinus">{{ integral.integral }}</div>
				<div v-else class="listNum">+{{ integral.integral }}</div>

				<div class="listHead">{{integral.comment}}</div>
				<div class="listTime">{{integral.dateYmdString}}</div>
			</li>
		</ul>
		<div v-else class="studentJuniorSchool-layerBox">
			<div class="studentJuniorSchool-notSupport">
				<div class="head">你还没有获得过学豆哦</div>
			</div>
		</div>
	</div>
</@layout.page>
