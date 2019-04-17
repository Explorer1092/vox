<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语" pageJs="chipsFormalAdvertisement">
    <@sugar.capsule css=["chipsFormalAdvertisement"] />

<style>
    [v-cloak] { display: none }
    body,html{
        overflow-x: hidden;
    }
</style>
<div id="formal-advertisement" class="formal-ad-wrapper">
    <div class="head">
        <#if grade?? && grade == 1>
            <img width="100%" src="/public/images/parent/chips/header_G1.png" alt="">
        </#if>
        <#if grade?? && grade == 2>
            <img width="100%" src="/public/images/parent/chips/header_G2.png" alt="">
        </#if>
        <#if grade?? && grade == 3>
            <img width="100%" src="/public/images/parent/chips/header_G3.png" alt="">
        </#if>
        <div class="start-date">
            <div class="txt">开课时间</div>
            <div class="date">${  beginDate }</div>
        </div>
    </div>
    <div style="text-align: center;">
        <div class="circle-txt">保过班</div>
    </div>
    <div class="describe" style="margin-top: 1rem;">
        <div class="sub-title">全面升级为保过班</div>
        <div class="content-txt">
            薯条英语课程全新升级为保过班！学完对应课程即可以参加伦敦三一口语 (GESE) ${grade!''} 级考试，考试不通过可以免费再学一次！
        </div>
    </div>
    <div class="describe" style="margin-top: 0.5rem;">
        <div class="sub-title">一对一专属服务</div>
        <div class="content-txt">
            每天进入 App 学习完成后，会有专属老师微信纠错点评。
        </div>
    </div>
    <#if grade ?? && grade == 1>
        <div>
            <div class="describe" style="margin-top: 2rem;">
                <div class="h1-title">课程详情</div>
                <div class="content-txt">
                    以美国插班生活为主题，讲述和同学 Tom 和 Lily的学校学习生活，包括讨论颜色，使用 20 以内的数字，日常服装名称，认识动物等等。在学校生活中学习三一口语（GESE）1 级口语技能。
                </div>
            </div>
            <div class="describe" style="margin-top: 2rem;">
                <div class="sub-title" style="font-size: 1rem;">初到美国插班生活体验 </div>
                <div class="card">
                    <div class="card-title">
                        <div style="font-size: 0.8rem; font-weight: bolder;">学习时间：58天</div>
                        <div style="font-size: 0.7rem; font-weight: 500;">周次&nbsp;&nbsp;&nbsp;&nbsp;学习内容</div>
                    </div>
                    <div class="card-content">
                        <ul>
                            <li>
                                <div class="order-title">第1周</div>
                                <div>Zoo Animals  | Sea Animals</div>
                            </li>
                            <li>
                                <div class="order-title">第2周</div>
                                <div>Trip to the Farm | Mock exam</div>
                            </li>
                            <li>
                                <div class="order-title">第3周</div>
                                <div>Hi Neighbour  | Practicing Counting</div>
                            </li>
                            <li>
                                <div class="order-title">第4周</div>
                                <div>Lemonade Stand | Mock exam</div>
                            </li>
                            <li>
                                <div class="order-title">第5周</div>
                                <div>The Color of Snow Mountain | Look at the Rainbow</div>
                            </li>
                            <li>
                                <div class="order-title">第6周</div>
                                <div>Mixing Colors | Mock exam</div>
                            </li>
                            <li>
                                <div class="order-title">第7周</div>
                                <div>Buying Clothes | What should I wear?</div>
                            </li>
                            <li>
                                <div class="order-title">第8周</div>
                                <div>Where is my jacket? | Mock exam</div>
                            </li>
                            <li>
                                <div class="order-title">第9周</div>
                                <div>Final exam A | Final exam B</div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </#if>
    <#if grade ?? && grade == 2>
        <div>
            <div class="describe" style="margin-top: 2rem;">
                <div class="h1-title">课程详情</div>
                <div class="content-txt">
                    以寄宿生活为主题，在 Tom 家寄宿，和 Tom 的爸爸 Jack 妈妈 Emily 一起生活，讨论养宠物，日期和月份，50 以内的数字，描述正在做某事，描绘个人所拥有的物品等等。轻松掌握三一口语（GESE）2 级口语技能。
                </div>
            </div>
            <div class="describe" style="margin-top: 2rem;">
                <div class="sub-title" style="font-size: 1rem;">成为寄宿家庭生活一员</div>
                <div class="card">
                    <div class="card-title">
                        <div style="font-size: 0.8rem; font-weight: bolder;">学习时间：58天</div>
                        <div style="font-size: 0.7rem; font-weight: 500;">周次&nbsp;&nbsp;&nbsp;&nbsp;学习内容</div>
                    </div>
                    <div class="card-content">
                        <ul>
                            <li>
                                <div class="order-title">第1周</div>
                                <div>Do you have a pet?  | I like rabbit</div>
                            </li>
                            <li>
                                <div class="order-title">第2周</div>
                                <div>I would like a pet | Mock exam</div>
                            </li>
                            <li>
                                <div class="order-title">第3周</div>
                                <div>What are you doing? | Street Art </div>
                            </li>
                            <li>
                                <div class="order-title">第4周</div>
                                <div>What is mom doing? | Mock exam</div>
                            </li>
                            <li>
                                <div class="order-title">第5周</div>
                                <div>Preparing for Birthday Party | Weighting food</div>
                            </li>
                            <li>
                                <div class="order-title">第6周</div>
                                <div>How much is the Lego? | Mock exam</div>
                            </li>
                            <li>
                                <div class="order-title">第7周</div>
                                <div>Festivals | School Timetable</div>
                            </li>
                            <li>
                                <div class="order-title">第8周</div>
                                <div>When is our math exam? | Mock exam</div>
                            </li>
                            <li>
                                <div class="order-title">第9周</div>
                                <div>Final exam A | Final exam B</div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </#if>
    <#if grade ?? && grade == 3>
        <div>
            <div class="describe" style="margin-top: 2rem;">
                <div class="h1-title">课程详情</div>
                <div class="content-txt">
                    以暑假生活为主题，孩子与 Tom 和 Lily 讨论家庭生活，学习场所，时间和日期，以及兴趣爱好，天气等等。在日常生活话题中轻松掌握三一口语（GESE）3 级口语技能。
                </div>
            </div>
            <div class="describe" style="margin-top: 2rem;">
                <div class="sub-title" style="font-size: 1rem;">在美国的第一个暑假</div>
                <div class="card">
                    <div class="card-title">
                        <div style="font-size: 0.8rem; font-weight: bolder;">学习时间：72天</div>
                        <div style="font-size: 0.7rem; font-weight: 500;">周次&nbsp;&nbsp;&nbsp;&nbsp;学习内容</div>
                    </div>
                    <div class="card-content">
                        <ul>
                            <li>
                                <div class="order-title">第1周</div>
                                <div>Doing Housework | Cooking</div>
                            </li>
                            <li>
                                <div class="order-title">第2周</div>
                                <div>Family Entertainment | Summer Activities</div>
                            </li>
                            <li>
                                <div class="order-title">第3周</div>
                                <div>Mock exam | What time is it?</div>
                            </li>
                            <li>
                                <div class="order-title">第4周</div>
                                <div>Plan for Tommorrow | Time Zones</div>
                            </li>
                            <li>
                                <div class="order-title">第5周</div>
                                <div>Movie Time | Mock exam</div>
                            </li>
                            <li>
                                <div class="order-title">第6周</div>
                                <div>New Gym | Riding the Bike</div>
                            </li>
                            <li>
                                <div class="order-title">第7周</div>
                                <div>Doing Sports | Playing Musical Instruments</div>
                            </li>
                            <li>
                                <div class="order-title">第8周</div>
                                <div>Mock exam| When is your birthday?</div>
                            </li>
                            <li>
                                <div class="order-title">第9周</div>
                                <div>Tom was in hospital | Go to the SeaWorld</div>
                            </li>
                            <li>
                                <div class="order-title">第10周</div>
                                <div>New Semester Begins| Mock exam</div>
                            </li>
                             <li>
                                <div class="order-title">第11周</div>
                                <div>Final exam A | Final exam B</div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </#if>
    <div style="text-align: center;margin: 1.5rem 0 1rem 0;">
        <div class="circle-txt">购买须知</div>
    </div>
    <ol class="describe" style="margin-bottom: 3.5rem;">
        <li>1.&nbsp;购买后请务必关注【薯条英语公众号】并添加你的【专属班主任老师】；</li>
        <li>2.&nbsp;课程每天 0 点开始更新；</li>
        <li>3.&nbsp;开课 2 天内无条件退款；</li>
        <li>4.&nbsp;课程有效期 3 年。</li>
    </ol>
    <div class="footer">
        <div class="origin-price">保过班价格：${ originalPrice }元</div>
        <div class="real-price" @click.stop="buy">¥${ price }&nbsp;&nbsp;限时抢购</div>
    </div>
</div>
<script type="text/javascript">
    var beginDate = '${beginDate!''}';
    var productId = '${productId!''}';
    var price = '${price!''}';
    var originalPrice = '${originalPrice!''}';
    var grade = ${grade!''};
    var productName = '${productName!''}';
</script>
</@layout.page>

<#--</@chipsIndex.page>-->
