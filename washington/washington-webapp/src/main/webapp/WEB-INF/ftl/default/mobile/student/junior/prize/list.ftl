<#assign extraJs = [
    {
        "path" : "prize"
    }
]>

<#include "../layout.ftl">

<@layout.page className=CONFIG.CSSPRE + 'reward_list' title="奖品中心" headBlock=headBlock bottomBlock=bottomBlock>
	<div class="studentJuniorSchool-mallTop-box {{canShowList ? 'studentJuniorSchool-layerBox studentJuniorSchool-mallTop-boxFixed' : ''}}"  id="categories">
		<div class="studentJuniorSchool-mallTop-inner">
			<div class="studentJuniorSchool-mallTop-beans">
				<div class="beans">
					<div class="icon"></div>
					<div class="text">
						<span class="num">${usable_integral!""}</span>
						<span>学豆剩余</span>
					</div>
				</div>
				<div class="btns"><a href="/studentMobile/center/juniorintegral.vpage">学豆记录</a><a href="/studentMobile/center/juniorexchangehistory.vpage">兑换记录</a></div>
			</div>
			<div class="studentJuniorSchool-mallTop-bar">
				<div class="text">{{categroyName}}奖品</div>
				<div class="btn" v-on:click="toggleShowList">类型</div>
			</div>
			<div class="studentJuniorSchool-mallTop-list" v-show="canShowList" >
				<#assign defaultCategore = {
					"categoryName" : "全部"
				}>
				<#list ([defaultCategore] + (categories![])) as category>
                    <a href="javascript:;"  data-category_id="${category.id!''}" v-on:click="changeList" >${category.categoryName!""}</a>
				</#list>
			</div>
		</div>
	</div>
	<div id="prizes">
		<div v-if="prizes.length === 0" class="studentJuniorSchool-mallList studentJuniorSchool-mallList-null">
			<div class="head">该分类暂时还没有奖品</div>
			<div class="text">敬请期待！</div>
		</div>
		<div v-else class="studentJuniorSchool-mallList">
			<a href="/studentMobile/center/rewarddetail.vpage?productId={{prize.id}}" v-for="prize in prizes">
				<div class="imgBox"><img v-bind:src="'<@app.avatar href='test.jpg'/>'.replace('test.jpg',  prize.image)"  alt=""></div>
				<div class="textBox">{{prize.productName}}</div>
				<div class="numBox"><span>{{prize.price}}</span></div>
			</a>
		</div>
	</div>
</@layout.page>

