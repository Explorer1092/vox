<#import "module.ftl" as center>
<@center.studentCenter currentPage='integral'>
    <div class="t-center-box w-fl-right">
        <span class="center-rope"></span>
        <span class="center-rope center-rope-1"></span>
        <div class="t-center-safe">
            <div class="bean-title">
                <h2 class="w-fl-right">学豆获得记录</h2>
                <strong class="w-fl-left">我的学豆:</strong><span class="w-fl-left">${currentStudentDetail.userIntegral.usable}</span>
                <a onclick="$17.tongji('个人中心-我的学豆-进入奖品中心');" class="w-btn-dic w-btn-yellow-well w-fl-left" href="${(ProductConfig.getMainSiteBaseUrl())!''}/reward/index.vpage" target="_blank">进入奖品中心</a>
            </div>
            <div class="t-center-list">
                <ul class="bean" id="integral_history_list_box"><#--学豆获得记录--></ul>
                <div class="w-page"></div>
                <div class="message_page_list"></div>
                <div class="w-fl-left" style="margin-top: -30px; padding: 0 30px;">学豆记录仅显示最近三个月的明细</div>
            </div>

            <div class="t-center-info">
                <div >
                    <h2>兑奖说明</h2>
                    <p>1. 为了防止作弊，保证真实有效，需要你在兑奖前先设定家长手机号码 <a href="${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage?updateType=parent">“去设定 ”</a></p>
                    <p>2. 经过一起作业认证的学生可以用学豆在奖品中心兑换奖品，我们会统一寄送给老师，同学们就到老师那里去领取吧。</p>
                </div>
                <div  style="margin: 30px 0;">
                    <h2>如何认证？</h2>
                    <p>1.统一注册的师生：由一起作业统一注册的学校和班级的老师和学生，自动被认证。</p>
                    <p>2.自主注册的师生：请电话联系一起作业客服进行认证</p>
                </div>
                <div >
                    <h2>如何获得学豆？</h2>
                    <div class="w-table w-table-border">
                        <table>
                            <thead>
                            <tr>
                                <td>作业形式</td>
                                <td>按时完成作业-获得学豆规则</td>
                                <td>补做完成获得学豆规则</td>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>基础练习</td>
                                <td>按分数给学豆，满分10个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>同步习题</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>阅读绘本</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>口算</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>配套试卷</td>
                                <td>按分数给学豆，满分10个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <#--<tr>-->
                                <#--<td>动手做一做</td>-->
                                <#--<td>完成即给5个学豆</td>-->
                                <#--<td>补做完成，1个</td>-->
                            <#--</tr>-->
                            <#--<tr>-->
                                <#--<td>概念说一说</td>-->
                                <#--<td>完成即给5个学豆</td>-->
                                <#--<td>补做完成，1个</td>-->
                            <#--</tr>-->
                            <tr>
                                <td>生字词练习</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>听力练习</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>课文读背</td>
                                <td>完成即给5个学豆</td>
                                <td>补做完成，1个</td>
                            </tr>
                            <tr>
                                <td>口语习题</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>基础知识</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>阅读</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>重点视频专练</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>知识点查缺补漏</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            <tr>
                                <td>高频错题</td>
                                <td>按分数给学豆，满分5个</td>
                                <td>补做完成且满60分，1个</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>

                    <#--<div class="w-table " style="margin: 20px 0 0;">
                        <table>
                            <tbody>
                                <tr>
                                    <td>每周班级PK排行榜第一(PK次数胜利最多，且班级参与PK人数大于2人)  </td>
                                    <td>+10 学豆</td>
                                </tr>
                                <tr>
                                    <td>第一次 PK 并获胜后   </td>
                                    <td> +5 学豆</td>
                                </tr>
                                <tr>
                                    <td>累计 PK 战胜全班同学(只能获得一次，并且全班同学总人数不足 20 人则无法获取此奖励)</td>
                                    <td> +50 学豆</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>-->
                    <div class="w-table w-table-border" style="margin: 20px 0;">
                        <table>
                            <thead>
                            <tr>
                                <td>作业形式得分</td>
                                <td>满分10学豆时，奖励学豆</td>
                                <td>满分5学豆时，奖励学豆</td>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>作业分数小于20分时</td>
                                <td>0</td>
                                <td>0</td>
                            </tr>
                            <tr>
                                <td>作业分数大于等于20分，小于40分时</td>
                                <td>1</td>
                                <td>1</td>
                            </tr>
                            <tr>
                                <td>作业分数大于等于40分，小于60分时</td>
                                <td>3</td>
                                <td>2</td>
                            </tr>
                            <tr>
                                <td>作业分数大于等于60分，小于80分时</td>
                                <td>5</td>
                                <td>3</td>
                            </tr>
                            <tr>
                                <td>作业分数大于等于80分，小于100分时</td>
                                <td>8</td>
                                <td>4</td>
                            </tr>
                            <tr>
                                <td>作业分数等于100分时</td>
                                <td>10</td>
                                <td>5</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="w-table " style="margin: 20px 0 0;">
                        <table>
                            <tbody>
                            <tr>
                                <td>按时完成所有作业形式，才算一次作业完成，此时按照上述规则给学生计算获得的学豆总数量；</td>
                            </tr>
                            <tr>
                                <td>未按时完成所有作业形式且补做完成之前，不给学生奖励；经补做才完成作业，按照补做的规则给学生计算学豆</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>

                </div>
                <div style="margin:20px 0">
                    <h2>学豆的使用规则</h2>
                    <p>1.学豆不能赠送</p>
                    <p>2.学豆与园丁豆不能互换（学生只能获得学豆，老师只能获得园丁豆）</p>
                    <p>3.小学毕业班学生的学豆可继续使用，但在奖品中心兑换的实物奖品将不再寄送。</p>
                    <p>4.学豆清零规则（如果用户没有出现严重违规行为，学豆就不会被清零）</p>
                </div>
            </div>
        </div>

    </div>


    <script type="text/javascript">
        function createPageList(index) {
            var integral_history_list_box = $("#integral_history_list_box");
            integral_history_list_box.html('<div style="padding: 20px; text-align: center;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
            $.get('/student/center/integralchip.vpage?pageNumber='+index, function (data) {
                integral_history_list_box.html(data);
            }).fail(function(){
                integral_history_list_box.html('<div style="padding: 20px; text-align: center;">数据加载失败</div>');
            });
        }
        $(function () {
            /*初始化*/
            createPageList(1);
        });
    </script>
</@center.studentCenter>