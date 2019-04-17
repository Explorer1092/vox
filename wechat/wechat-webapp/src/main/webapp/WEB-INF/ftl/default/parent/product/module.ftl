<@sugar.capsule css=['product'] />
<#assign productIntroduction= {
    "AfentiExam": {
        "img": "/public/images/parent/product/detail/afenti.jpg",
        "introduction": "每天学会5个知识点，举一反三改错题，学豆奖励专注学习",
        "name":"阿分题"
    },
    "TravelAmerica": {
        "img": "/public/images/parent/product/detail/America.jpg",
        "introduction": "比非VIP会员每天多10次学单词机会，180天突破小升初1200单词。",
        "name":"走遍美国VIP特权"
    },
    "Walker": {
        "img": "/public/images/parent/product/detail/walker.jpg",
        "introduction": "以话题分类记忆方式，提升实践能力；提供交互性较强的跑酷形式，可挑战其他同学来，来查看自己记忆效果。",
        "name":"沃克大冒险"
    },
    "iandyou100": {
        "img": "/public/images/parent/product/detail/idy.jpg",
        "introduction": "台湾著名教育公司研究10年成果，与各版本教材同步，特级教师同款课件，有效提高考试成绩。动手又动脑，让孩子在动画中实验数学原理，在游戏中体验数学的神奇",
        "name":"爱儿优"
    },
    "SanguoDmz": {
        "img": "/public/images/parent/product/detail/sanguo.jpg",
        "introduction": "数学英语全覆盖，班级PK，赢学豆，用知识称霸三国",
        "name":"进击的三国"
    },
    "KaplanPicaro": {
        "img": "",
        "introduction": "剑桥英语学习思维，助学习更轻松；1000多种学习场景，听说读写全面抓；与国际接轨，达到雅思2~3分同等水平。",
        "name":"Picaro"
    },
    "A17ZYSPG": {
        "img": "/public/images/parent/product/detail/fairyland.jpg?1.0.0",
        "introduction": "作业单词同步练，多种练习方式相结合，小学英语快速提分",
        "name":"洛亚传说"
    },
    "PetsWar":{
        "img": "/public/images/parent/product/petsbanner.png",
        "introduction": "个性评估、分切目标，提升兴趣。快乐记忆适合“孩子自己”的词汇认、拼、读。",
        "name":"宠物大乱斗"
    },
    "WukongShizi":{
        "img": "/public/images/parent/product/detail/wukongshizi.png",
        "introduction": "轻松掌握语文词汇，拓展知识面，快乐学语文",
        "name":"悟空识字"
    },
    "WukongPinyin":{
        "img": "/public/images/parent/product/detail/wukongpinyin.jpg",
        "introduction": "让你快速学习声母、韵母、整体认读音节等拼音知识，全面掌握拼读技巧，从此拼音不再难",
        "name":"悟空拼音"
    }

}>

<form action="/parent/product/order.vpage" method="post">
    <div class="pro-detail">
        <div class="pd-main">
            <div class="banner">
                <img src="${productIntroduction[productType]['img']}" />
            </div>
            <div class="intro pdm">
                <h4>产品介绍：</h4>
                <p>
                    <span id='afenti_clazz_tip'></span>${productIntroduction[productType]['introduction']}
                </p>
            </div>
            <div class="child pdm">
                <h4>选择孩子：</h4>
                <ul id="child_list_box" data-selected_student="${sid!}">
                <#if infos?? && infos?size gt 0>
                    <#list infos as info>
                        <#if info.products?? && info.products?size gt 0>
                            <li data-student_id="${info.uid!0}" data-products='${json_encode(info.products)}'>
                                <img src="<@app.avatar href="${info.img!}"/>" /><i></i>
                                <p class="name">${info.name!''}</p>
                            </li>
                        </#if>
                    </#list>
                </#if>
                </ul>
            </div>
            <div class="period pdm" style="border: none;">
                <h4>选择周期：</h4>
                <ul id="cycle_list_box">

                </ul>
            </div>
            <div class="price">
                <div style="padding-left: 165px;">
                    <p>共</p><span class="money price_box"></span>
                    <p>(低至<span id="dayPrice">0</span>元/天，省<span id="savePrice">0</span>元！)</p>
                </div>
            </div>

        </div>
        <div class="pro-footer">
            <div class="pf-l">需支付金额：<span class="price_box"></span></div>
            <div id="buy_but" class="pf-r" style="cursor: pointer;"><a href="javascript:void(0);">确认并支付</a></div>
            <input type="hidden" value="" name="sid" id="array-student"/>
            <input type="hidden" value="" name="productId" id="array-product"/>
        </div>
    </div>
</form>



