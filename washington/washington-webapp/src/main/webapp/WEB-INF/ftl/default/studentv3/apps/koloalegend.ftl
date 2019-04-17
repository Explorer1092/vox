<#import '../layout/layout.ftl' as temp>
<@temp.page clazzName='lya-body-back'>
    <@sugar.capsule css=["student.koloalegend"] js=['slick'] />
    <div class="t-lya-container">
        <div class="t-lya-inner">
            <!--lya-head-->
            <div class="lya-head">
                <div class="lya-area">
                    <div id="messageBox" class="lya-info">
                        ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'KoloaLegendBanner')}
                    </div>
                </div>
            </div>
            <!--lya-charts-->
            <#--<div class="lya-charts">
                <div class="top"></div>
                <div class="mid">
                    &lt;#&ndash; 需求暂时关闭 ’扫码拿大奖‘功能 &ndash;&gt;
                    &lt;#&ndash;<div style="margin:-20px 0 0 11px;"><img src="<@app.link href="public/skin/studentv3/images/koloalegend/lya-code.png"/>" width="160" alt=""/></div>&ndash;&gt;
                    <div style="background: url(<@app.link href="public/skin/studentv3/images/koloalegend/border-line.png"/>) no-repeat bottom center;width:183px;height:5px;margin-top:10px;_margin-top:0;"></div>
                    <ul>
                       &lt;#&ndash; <li style="font-size: 30px;color:#fff66a;text-shadow: 1px 1px black;font-weight: 700">扫码拿大奖</li>&ndash;&gt;
                        <li><a class="notOpenTip" href="javascript:void(0);">收藏应用</a></li>
                        <li><a class="notOpenTip" href="javascript:void(0);">应用加速</a></li>
                        <li><a class="notOpenTip" href="javascript:void(0);">洛亚大明星</a></li>
                        <li><a class="notOpenTip" href="javascript:void(0);">新手指南</a></li>
                        <li><a class="opFeedback" href="javascript:void(0);">问题反馈</a></li>
                    </ul>
                </div>
                <div class="bot">
                    <a href="javascript:void(0);" class="change-btn notOpenTip"></a><!--lya-back-dis&ndash;&gt;
                </div>
            </div>-->
            <!--lya-content-->
            <div class="lya-content">
                <div class="lya-game-box" style="margin: 0 auto;">
                    <iframe style="position:absolute;left:12px;top:12px;" width="960" height="600" class="vox17zuoyeIframe" src="${curapp.appUrl!}?session_key=${sessionKey}&sig=${sig!}" name="apps_game_homework" scrolling="no" frameborder="0"></iframe>
                </div>
            </div>
            <#--<div class="lya-banner">
                <div class="lya-banner-left">
                    <ul>
                        <li><a class="notOpenTip" href="javascript:void(0);"><img src="<@app.link href="public/skin/studentv3/images/koloalegend/luoya-banner-04.jpg"/>" alt=""/></a></li>
                        <li><a class="notOpenTip" href="javascript:void(0);"><img src="<@app.link href="public/skin/studentv3/images/koloalegend/luoya-banner-05.jpg"/>" alt=""/></a></li>
                    </ul>
                </div>
                <div class="lya-banner-right">
                    <div class="lya-block">
                        <ul id="recommend_list_box" style="position: relative;">
                            <li>
                                <a class="notOpenTip" href="javascript:void(0);">
                                    <div><img src="<@app.link href="public/skin/studentv3/images/koloalegend/luoya-banner-03.png"/>" alt=""/></div>
                                    <h5>齐天大圣</h5>
                                </a>
                            </li>
                            <li>
                                <a class="notOpenTip" href="javascript:void(0);">
                                    <div><img src="<@app.link href="public/skin/studentv3/images/koloalegend/luoya-banner-01.png"/>" alt=""/></div>
                                    <h5>暗影魔龙</h5>
                                </a>
                            </li>
                            <li>
                                <a class="notOpenTip" href="javascript:void(0);">
                                    <div><img src="<@app.link href="public/skin/studentv3/images/koloalegend/luoya-banner-02.png"/>" alt=""/></div>
                                    <h5>光明圣狮</h5>
                                </a>
                            </li>
                        </ul>
                    </div>
                    <div class="tab-btn-box">
                        <a id="prevArrowBut" href="javascript:void(0);" class="lya-back lya-back-dis"></a>
                        <a id="nextArrowBut" href="javascript:void(0);" class="lya-next"></a><!--lya-back-dis&ndash;&gt;
                    </div>
                </div>
            </div>-->
        </div>
    </div>
    <script type="text/html" id="T:未付费">
        <style>
            .lya-tip-1 a{margin:15px auto;display: block}
        </style>
        <div class="lya-tip-1">
            <p>由于一起作业平台整体的产品线升级，</p>
            <p>对学习产品的教育价值有了更高的标准和期望。</p>
            <p>从即日起，停止《洛亚传说》所有的开通和续费功能，</p>
            <p>并将于2016年8月24日关闭试用。</p>
            <p>感谢大家一直以来对《洛亚传说》的喜爱，</p>
            <p>更多、更优质的学习产品即将陆续上线一起作业平台，敬请关注！</p>
        </div>
    </script>
    <script type="text/html" id="T:付费">
        <div class="lya-tip-2">
            <p>由于一起作业平台整体的产品线升级，我们对学习产品的教育价值有了更高的标准和期望。所以，怀着万分不舍，《洛亚传说》即将准备跟大家告别了。</p>
            <p>1、即日起，本产品的开通和续费功能将永久性地关闭。</p>
            <p>2、本产品将于2016年8月24日关闭试用。</p>
            <p>3、在包月有效期限内，您仍可继续登录和使用，直至包月到期或产品完全下线。</p>
            <p>感谢大家一直以来对《洛亚传说》的喜爱，更多、更优质的学习产品即将陆续上线一起作业平台，敬请关注！</p>
            <div style="text-align: center; padding: 10px 0 0;">
                <a href="javascript:void(0);" onclick="$.prompt.close();" class="w-btn-dic w-btn-gray-new">知道了</a>
                <a href="/apps/afenti/order/spg-cart.vpage" onclick="$.prompt.close();" class="w-btn-dic w-btn-green-new">了解详情</a>
            </div>
        </div>
    </script>
    <script type="text/javascript">
        $(function(){
            feedBackInner.homeworkType = 'Koloalegend';
            $('.opFeedback').on('click',function(){
                $('#message_right_sidebar').click();
            });
            //该功能暂未开放提示
            $('.notOpenTip').on('click',function(){
                $17.alert('该功能暂未开放');

                $17.tongji('课外乐园','洛亚传说','该功能暂未开放');
            });

            //推荐图片切换
            $('#recommend_list_box').slick({
                prevArrow : 'prevArrowBut',
                nextArrow : 'nextArrowBut',
                cardWidth : 94, //单张卡片的宽度
                disableClass : 'lya-back-dis'
            });

            //消息滚动
            $("#messageBox").textScroll({
                line: 1,
                speed: 1000,
                timer : 3000
            });

            $17.tongji('课外乐园','洛亚传说','页面展示');

            $.prompt(template("T:付费", {}), {
                position: { width: 600},
                buttons : { },
                title   : "公告："
            });
        });
    </script>
</@temp.page>