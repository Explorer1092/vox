<#import "../layout.ftl" as summerCamp>
<@summerCamp.page title="2016夏令营" pageJs="summercampskupay">
    <@sugar.capsule css=['wintercamp','jbox'] /><#--复用冬令营的样式-->
    <#assign productIntroduction= {
        '22': {
            "img": "/public/images/parent/summercamp/manNature.jpg",
            "introduction": "一群男孩子 二三首军歌 六七种战术 八九天苦训 十几天改变",
            "name":"野战突击—激发男子汉本能营"
        },
        '23': {
            "img": "/public/images/parent/summercamp/honorSoldier.jpg",
            "introduction": "抗战胜利70周年大阅兵教官;真部队、真军人、真枪真弹、真坦克 ;1:1仿真训练枪配比",
            "name":"陆战队—仪仗小兵主题营"
        },
        '24': {
            "img": "/public/images/parent/summercamp/outdoorSurivival.jpg",
            "introduction": "部队作训科全程参与设计指导;野战军级别野外生存训练;全真野战军装备(迷彩服+作训服+军帽+装备）",
            "name":"野战军—野外生存主题营"
        },
        '25': {
            "img": "/public/images/parent/summercamp/medicalAidGroup.jpg",
            "introduction": "真部队、真军人、真枪真弹、真坦克;军事训练科目由部队作训科参与设计 ;1:1仿真训练枪配比，全真陆军装备(迷彩服+军帽+作训服+女军官制服）;野战部队、陆战部队轮流训练",
            "name":"少女军官—医疗救护小组制集训营"
        },
        '26': {
            "img": "/public/images/parent/summercamp/uninhabitedIslandSurvival.jpg",
            "introduction": "学习安全正确的求生方法，深度了解海岛地理和生存方案，探寻属于自己的荒岛生存",
            "name":"野战军—无人岛生存主题营（上海营）"
        },
        '27': {
            "img": "/public/images/parent/summercamp/5CSpiritualGrowth.jpg",
            "introduction": "国内外专家倾力打造；让家长更懂孩子，走进心灵；VIP团队精英式培养；丰富活动，在快乐中成长",
            "name":"5C心灵成长夏令营"
        },
        '28': {
            "img": "/public/images/parent/summercamp/tenProvidersUpgrade.jpg",
            "introduction": "泰斗级名师指导，让您和孩子体验极具专业性的服务；与孩子“零距离”；问题导向，活动丰富，让孩子不断发现新乐趣",
            "name":"十商提升夏令营"
        },
        '29': {
            "img": "/public/images/parent/summercamp/basketballMaster.jpg",
            "introduction": "何为篮球？一种运动、一种坚持、一种态度、一种人生，也是一群人和一个球之间的约定，无伙伴不篮球，篮球给你了赛场上的魅力和荣誉",
            "name":"篮高手篮球营"
        },
        '30': {
            "img": "/public/images/parent/summercamp/snowySki.jpg",
            "introduction": "在雪道顶端飞驰而下，学习刺激有趣的滑雪技能，锻炼强健的身体素质，无论你有无经验，都可以在这里收获快乐与成长",
            "name":"雪域奇兵滑雪营"
        },
        '31': {
            "img": "/public/images/parent/summercamp/futureWorldAerospace.jpg",
            "introduction": "炫酷科技，炫酷未来，未来世界，由你创造",
            "name":"未来世界航天航空营"
        },
        '32': {
            "img": "/public/images/parent/summercamp/norhtQingVipStudyTour.jpg",
            "introduction": "一流的师资辅导员团队；最为专业的讲解队伍，感受最原始的北京文化；深度体验百年名校；配备随行队医，应急预案完备",
            "name":"清北VIP励志游学营"
        },
        '33': {
            "img": "/public/images/parent/summercamp/varietyGirl.jpg",
            "introduction": "国学礼仪与西方礼仪相结合，理论与实践系统化教学，学习不同场合的礼仪，教会孩子知礼懂礼；强大师资阵容:中国礼学中心指导、资深形象礼仪专家讲解、清华等名校辅导员全程陪伴",
            "name":"百变女孩夏令营"
        },
        '34': {
            "img": "/public/images/parent/summercamp/usaTopSchoolVisits.jpg",
            "introduction": "感受美国本土文化、探访美国八大名校、畅游全美知名景点、游历美国五座名城、名校讲解学以致用",
            "name":"名校交流--美国顶尖名校尊享考察营"
        },
        '35': {
            "img": "/public/images/parent/summercamp/californiaCampSummerCourse.jpg",
            "introduction": "合理的课程与实践内容相结合；严格分班，与美国学生一同上课，真正的能力提升；入住经过严格筛选的美国家庭，体验纯正的美式家庭生活；路线踏遍美国东西海岸，遍访美国名城，寻求美国文化根源",
            "name":"微留学—加州全真暑期课程体验营"
        }
    }>
    <#assign shopId = (shop.shopId?string)!'0'>

<div class="wc-wrap">
    <div class="wc-payBox">
        <div class="banner">
            <img src="${productIntroduction[shopId]['img']}" />
        </div>
        <div class="main">
            <div class="intro pdm">
                <h4>产品介绍：</h4>
                <p>${productIntroduction[shopId]['name']}</p>
                <p class="txtGrey">${productIntroduction[shopId]['introduction']}</p>
            </div>
            <div class="child pdm">
                <h4>选择孩子：</h4>
                <ul id="studentListBox">
                    <#if students?? && students?size gt 0>
                        <#list students as students>
                            <li data-student_id="${students.id!0}">
                                <img src="<@app.avatar href="${students.img!}"/>" /><p>${students.name!0}</p><i></i>
                            </li>
                        </#list>
                    <#else>
                        <li>暂无可选择的孩子</li>
                    </#if>
                </ul>
            </div>
            <div class="period pdm">
                <h4>选择种类：</h4>
                <div>
                    <ul id="productListBox">
                        <#if trusteeTypes?? && trusteeTypes?size gt 0>
                            <#list trusteeTypes as tt>
                                <li data-name="${tt.name!''}" data-price="${tt.price!0}">
                                    <table cellpadding="0" cellspacing="0">
                                        <tr>
                                            <td class="wtxt">${tt.description!''}</td>
                                            <td class="wprice">￥${tt.price!0}</td>
                                        </tr>
                                    </table>
                                </li>
                            </#list>
                        <#else>
                            <li>暂无数据</li>
                        </#if>

                    </ul>
                </div>
            </div>
        </div>
        <div class="empty"></div>
        <!--有孩子未支付-->
        <form id="payForm" action="/parent/trustee/order.vpage" method="post">
            <div class="sfooter">
                <div class="pf-l"><span>需支付：<strong id="payPriceBox">--</strong></span></div>
                <input type="hidden" name="trusteeType">
                <input type="hidden" name="sid">
                <div class="pf-r"><a id="paySubmitBtn" href="javascript:void(0)">确认并支付</a></div>
            </div>
        </form>

        <!--所有孩子都已支付-->
        <div id="payAllBtn" class="sfooter pay-success" style="display: none;">
            <a href="javascript:void(0)" class="pay-success">支付成功</a>
        </div>
    </div>
</div>
</@summerCamp.page>