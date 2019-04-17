<#import "../layout.ftl" as winterCamp>
<@winterCamp.page title="2016成长冬令营" pageJs="wintercampskupay">
    <@sugar.capsule css=['wintercamp','jbox'] />
    <#assign productIntroduction= {
        '11': {
            "img": "/public/images/parent/wintercamp/payBanner.png",
            "introduction": "三亚时代精英训练营，开启最美的成长之旅！成为拥有创新思维，善于沟通合作，有领导力，有规划的有社会责任感的二十一世纪人才！ ",
            "name":"三亚游学冬令营"
        },
        '12': {
            "img": "/public/images/parent/wintercamp/payBanner.png",
            "introduction": "在无边的黑暗降临大地之前，你，勇敢而充满智慧的小朋友，作为被选召的孩子，即将乘坐最后的太空船，出发执行拯救全人类的任务…… ",
            "name":"北京科技冬令营"
        },
        '13': {
            "img": "/public/images/parent/wintercamp/payBanner.png",
            "introduction": "那里有北方独特的热情与豪爽，带孩子摆脱钢筋水泥般的城市，尽情游戏，尽兴嬉戏。寒假去长白山，我们不见不散！  ",
            "name":"长白山亲子冬令营"
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

                           <#-- <li class="active"><table><tr><td class="wtxt">晚托作业辅导（1个月）+书法培训（8次）</td><td class="wprice">￥300</td></tr></table></li>
                            <li class="disabled"><table><tr><td class="wtxt">晚托作业辅导（1个月）</td><td class="wprice">￥300</td></tr></table></li>
                            <li><table><tr><td class="wtxt">晚托作业辅导（1个月）+书法培训（8次）</td><td class="wprice">￥300</td></tr></table></li>-->
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
</@winterCamp.page>