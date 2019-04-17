<#import "../layout.ftl" as ucenter>
<@ucenter.page title='课外乐园' pageJs="menu">
    <@sugar.capsule css=['product'] />
<div class="gra-shoot">
    <div class="gs-main">
        <h3 style="text-align: center;">课外乐园的应用均由第三方公司提供，请自愿使用！</h3>
        <div class="gs-info">
            <ul>
                <#if avalibleProductTypes?? && avalibleProductTypes?has_content>
                    <#list avalibleProductTypes as name>
                        <#switch name>
                            <#case 'Stem101'>
                                <li onclick="location.href='/parent/product/info-stem.vpage'">
                                    <img src="/public/images/parent/product/stem101.png"/>
                                    <div class="gs-infors">
                                        <h2>趣味数学训练营</h2>
                                        <p class="gs-intro">只需动动手指，就能引起一场头脑风暴！</p>
                                        <div>
                                            <a href="/parent/product/info-stem.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>
                            <#case 'AfentiExam'>
                                <li onclick="location.href='/parent/product/info-afenti.vpage'">
                                    <img src="/public/images/parent/product/afenti.png"/>
                                    <div class="gs-infors">
                                        <h2>阿分题${cityEditionPrefix!''}：帮你提分</h2>
                                        <p class="gs-intro">每天学会5个知识点，举一反三改错题，学豆奖励专注学习。</p>
                                        <div>
                                            <p class="tag">热卖</p>
                                            <a href="/parent/product/info-afenti.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>

                            <#case 'TravelAmerica'>
                                <li onclick="location.href='/parent/product/info-america.vpage'">
                                    <img src="/public/images/parent/product/America.png"/>
                                    <div class="gs-infors">
                                        <h2>走遍美国VIP特权：学玩结合</h2>
                                        <p class="gs-intro">比非VIP会员每天多10次学单词机会，180天突破小升初1200单词。</p>
                                        <div>
                                            <p class="tag tag-2">抢购</p>
                                            <a href="/parent/product/info-america.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>

                            <#case 'Walker'>
                                <li onclick="location.href='/parent/product/info-walker.vpage'">
                                    <img src="/public/images/parent/product/Walker.png"/>
                                    <div class="gs-infors">
                                        <h2>沃克大冒险：更有效率的单词记忆</h2>
                                        <p class="gs-intro">以话题分类记忆方式，提升实践能力；提供交互性较强的跑酷形式，可挑战其他同学来，来查看自己记忆效果。</p>
                                        <div>
                                            <a href="/parent/product/info-walker.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>

                            <#case 'iandyou100'>
                                <li onclick="location.href='/parent/product/info-iandyou100.vpage'">
                                    <img src="/public/images/parent/product/idy.png"/>
                                    <div class="gs-infors">
                                        <h2>爱儿优：把数学变简单</h2>
                                        <p class="gs-intro">数学不是背公式，动漫式同步教学，让抽象数学具象化，稳步提高考试成绩</p>
                                        <div>
                                            <a href="/parent/product/info-iandyou100.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>

                            <#case 'SanguoDmz'>
                                <li onclick="location.href='/parent/product/info-sanguodmz.vpage'">
                                    <img src="/public/images/parent/product/sanguo.jpg"/>
                                    <div class="gs-infors">
                                        <h2>进击的三国：成为学霸</h2>
                                        <p class="gs-intro">数学英语全涵盖，随机出题，寓教于乐</p>
                                        <div>
                                            <p class="tag tag-1">推荐</p>
                                            <a href="/parent/product/info-sanguodmz.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>

                            <#case 'KaplanPicaro'>
                                <li onclick="location.href='/parent/product/info-picaro.vpage'">
                                    <img src="/public/images/parent/product/picro.png"/>
                                    <div class="gs-infors">
                                        <h2>Picaro：帮你把英语变成母语</h2>
                                        <p class="gs-intro">剑桥英语学习思维，助学习更轻松；1000多种学习场景，听说读写全面抓；与国际接轨，达到雅思2~3分同等水平。</p>
                                        <div>
                                            <a href="/parent/product/info-picaro.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>

                            <#case 'PetsWar'>
                                <li onclick="location.href='/parent/product/info-pets.vpage'">
                                    <img src="/public/images/parent/product/pets.png"/>
                                    <div class="gs-infors">
                                        <h2>宠物大乱斗：个性评估、分切目标，提升兴趣。</h2>
                                        <p class="gs-intro">快乐记忆适合“孩子自己”的词汇认、拼、读。</p>
                                        <div>
                                            <a href="/parent/product/info-pets.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>

                            <#case 'A17ZYSPG'>
                                <li onclick="location.href='/parent/product/info-fairyland.vpage'">
                                    <img src="/public/images/parent/product/fairyland.jpg"/>
                                    <div class="gs-infors">
                                        <h2>洛亚传说：英语快速提分</h2>
                                        <p class="gs-intro">作业单词同步练，英语快速提分</p>
                                        <div>
                                            <p class="tag tag-1">推荐</p>
                                            <p class="tag">热卖</p>
                                            <a href="/parent/product/info-fairyland.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>

                            <#case 'WukongShizi'>
                                <li onclick="location.href='/parent/product/info-wukongshizi.vpage'">
                                    <img src="/public/images/parent/product/wukongshizi.png"/>
                                    <div class="gs-infors">
                                        <h2>悟空识字</h2>
                                        <p class="gs-intro">作业单词同步练，英语快速提分</p>
                                        <div>
                                            <a href="/parent/product/info-wukongshizi.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>
                            <#case 'WukongPinyin'>
                                <li onclick="location.href='/parent/product/info-wukongpinyin.vpage'">
                                    <img src="/public/images/parent/product/wukongpinyin.png"/>
                                    <div class="gs-infors">
                                        <h2>悟空拼音</h2>
                                        <p class="gs-intro">让你快速学习声母、韵母、整体认读音节等拼音知识，全面掌握拼读技巧，从此拼音不再难</p>
                                        <div>
                                            <a href="/parent/product/info-wukongpinyin.vpage" class="look-btn">去看看</a>
                                        </div>
                                    </div>
                                </li>
                                <#break>
                        </#switch>
                    </#list>
                <#else>
                    暂无产品可购买
                </#if>
            </ul>
        </div>
    </div>
</div>
<div style=" height: 125px;"></div>
<#include "../menu.ftl">
<script>
    var svalue = location.search.match(new RegExp('[\?\&]' + "_from" + '=([^\&]*)(\&?)', 'i'));
    var fromPlace = svalue ? decodeURIComponent(svalue[1]) : '';

    function pageLog(){
        require(['logger'], function(logger) {
            if(fromPlace != ""){
                logger.log({
                    module: 'product',
                    op: 'product_pv_form_'+fromPlace
                })
            }
            logger.log({
                module: 'product',
                op: 'product_pv_form_page_load'
            })

        })
    }
</script>
</@ucenter.page>