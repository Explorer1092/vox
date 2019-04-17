<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page header="hide">
    <style type="text/css">
        /*American-hero-main*/
        .American-hero-main{}
        .American-hero-main .head, .American-hero-main .head .inner{background: url(<@app.link href = '/public/skin/studentv3/images/travelamerica/ah-head.jpg' />) no-repeat center 0; width: 100%; height: 550px;}
        .American-hero-main .inner{ width: 1000px!important; margin: 0 auto;}
        .American-hero-main .head .inner{ position: relative;}
        .American-hero-main .head .inner .back-index{ width: 100px; height: 63px; position: absolute; top: 0; left: 0;}
        .American-hero-main .head .inner .info-box{ font-size: 16px; color: #552800; position: absolute; top: 417px; left: 100px; height: 73px;}
        .American-hero-main .head .inner .info-box h3{ font-size: 20px; padding: 5px 0 15px 0;}
        .American-hero-main .content-list-box, .American-hero-main .content-list-box .inner{ background: url(<@app.link href = '/public/skin/studentv3/images/travelamerica/American-con-box.jpg' />) no-repeat center 0; width: 100%; height: 506px;}
        .American-hero-main .content-list-box ul li{ width: 333px; float: left; text-align: center;}
        .American-hero-main .content-list-box ul li .cl{ display: block;  width: 333px; position: relative; cursor: pointer; }
        .American-hero-main .content-list-box ul li .cl .light{  width: 374px; position: absolute; top: -5000px; left: 51px; z-index: 8; height: 300px; background: url(<@app.link href = '/public/skin/studentv3/images/travelamerica/lamp-light.png' />) no-repeat 0 0;}
        .American-hero-main .content-list-box ul li .hover .light{ top: 0;}
        .American-hero-main .icon{ background: url(<@app.link href = '/public/skin/studentv3/images/travelamerica/person.png' />) no-repeat 5000px 5000px; width: 333px; height: 376px; display: block;}
        .American-hero-main .icon-1{ background-position: 0 0;}
        .American-hero-main .icon-2{ background-position: -333px 0;}
        .American-hero-main .icon-3{ background-position: -666px 0;}
        .American-hero-main .red-btn{ background: url(<@app.link href = '/public/skin/studentv3/images/travelamerica/red-btn.png'/>) no-repeat 0 0; width: 224px; height: 70px; display:inline-block; font-size: 24px; color: #fff; line-height: 70px;}
        .American-hero-main .red-btn:hover{ background-position: 0 -90px;}
        .American-hero-main .red-btn:active{ background-position: 0 -180px;}
        .American-hero-main .foot-box, .American-hero-main .foot-box .inner{ background: url(<@app.link href = '/public/skin/studentv3/images/travelamerica/American-info-box.jpg' />) no-repeat center 0; width: 100%; height: 461px;}
        .American-hero-main .foot-box .inner .fb-title{ font-size: 22px; color: #f25c2e; padding: 70px 0 0 100px; height: 100px; text-align: center;}
        .American-hero-main .foot-box .inner .fb-title p.small{ font-size: 16px; padding: 12px 0;}
        .American-hero-main .foot-box .inner .fb-font{}
        .American-hero-main .foot-box .inner .fb-font ul li{width: 34%; float: left; color: #cd5700; font-size: 16px; padding: 0 60px 0 92px;}
        .American-hero-main .foot-box .inner .fb-font ul li p{ line-height: 30px;}
        .American-hero-main .foot-box .inner .fb-font ul li p.title{ font-size: 20px; color: #cd5700; padding-bottom: 10px;}

        .hit-eggs-bg{ background: url(<@app.link href = '/public/skin/studentv3/images/travelamerica/eggs-alert.png'/>) no-repeat 5000px 5000px;}
        .hit-eggs-alertBox{ width: 340px; height: 385px; position: relative; background-position: 0 0;}
        .hit-eggs-alertBox .hp-btn{ position: absolute; bottom: 70px; right: 90px;}
        .hit-eggs-alertBox .hp-btn a{ width: 164px; height: 55px; text-decoration: none; display: inline-block; background-position: -98px -410px; color: #fff; font-size: 28px; line-height: 55px; text-align: center;}
        .hit-eggs-alertBox .hp-btn a:hover{ background-position: -101px -484px;}
        .hit-eggs-alertBox .hp-btn a:active{ background-position: -101px -564px;}
        .hit-eggs-alertBox .hp-close{ position: absolute; top: 42px; right: 21px; z-index: 5;}
        .hit-eggs-alertBox .hp-close a{ width: 38px; height: 38px; display: inline-block;}
        .hit-eggs-alertBox .hp-con{ position: absolute; width: 100%; text-align: center; top: 50px;}
        .hit-eggs-alertBox .h-icon{ background: url(<@app.link href = '/public/skin/studentv3/images/travelamerica/eggs-title.png'/>) no-repeat 5000px 5000px; width: 302px; height: 146px; display: inline-block;}
        .hit-eggs-alertBox .h-icon-1{ background-position: -13px -22px; margin-top: 50px;}
        .hit-eggs-alertBox .h-icon-2{ background-position: -13px -194px; height: 120px; margin-top: 60px;}
    </style>
    <@sugar.capsule js=["DD_belatedPNG"] />
    <div class="American-hero-main">
        <div class="head">
            <div class="inner">
                <a class="back-index" href="/"></a>
                <div class="info-box">
                    <h3>活动内容：</h3>
                    5月15日英雄降世，猜猜看，即将登录的新英雄是谁？猜中即可获得这位价值1200钻石新英雄，与他一起并肩战斗！
                </div>
            </div>
        </div>
        <div class="content-list-box">
            <div class="inner">
                <ul id="roleListBox">
                    <li data-role="captain">
                        <div class="cl">
                            <span class="icon icon-1"></span>
                            <div class="btn-box">
                                <a class="red-btn" href="javascript:void (0);">美国队长</a>
                            </div>
                            <div class="light PNG_24"></div>
                        </div>
                    </li>
                    <li data-role="">
                        <div class="cl">
                            <span class="icon icon-2"></span>
                            <div class="btn-box">
                                <a class="red-btn" href="javascript:void (0);">超    人</a>
                            </div>
                            <div class="light PNG_24" style="left: 58px;"></div>
                        </div>
                    </li>
                    <li data-role="thunder">
                        <div class="cl">
                            <span class="icon icon-3"></span>
                            <div class="btn-box">
                                <a class="red-btn" href="javascript:void (0);">雷    神</a>
                            </div>
                            <div class="light PNG_24" style="left: 68px;"></div>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
        <div class="foot-box">
            <div class="inner">
                <div class="fb-title">
                    <p>5月15日这位英雄的降临，还为全国小朋友带来福音</p>
                    <p class="small">温馨提示：不论使用哪个角色都能获得以下福利哦</p>
                </div>
                <div class="fb-font">
                    <ul>
                        <li>
                            <p class="title">福音一</p>
                            <p>
                                TA富有极强的责任感，在有危险时挺身而出，主持正义，在玩家完成小游戏时，获得的钻石、园丁豆奖励将增加。
                            </p>
                        </li>
                        <li>
                            <p class="title">福音二</p>
                            <p>
                                TA是人民的精神领袖，拥有不可思议的力量，在玩家升级到特定等级时，将获得钻石奖励。
                            </p>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <script type="text/html" id="tipBox">
        <div class="hit-eggs-alertBox hit-eggs-bg">
            <div class="hp-close">
                <a class="hit-eggs-bg" href="javascript:void (0)" onclick="$.prompt.close();"></a>
            </div>
            <div class="hp-con">
                <%if(state == 'success') {%>
                    <span class="h-icon h-icon-1"></span>
                <%}else{%>
                    <span class="h-icon h-icon-2"></span>
                <%}%>
            </div>
            <div class="hp-btn">
                <a id="enterBut" class="hit-eggs-bg" href="javascript:void (0);">进 入</a>
            </div>
        </div>
    </script>

    <script type="text/javascript">
        $(function () {
            //鼠标滑动效果
            $("#roleListBox li div.cl").hover(function () {
                $(this).addClass("hover");
            }, function () {
                $(this).removeClass("hover");
            });

            $("#roleListBox li").on('click', function () {
                var role = $(this).data('role');

                $.post('/campaign/30/appoint.vpage', {appKey: 'TravelAmerica', extInfo: role}, function (data) {
                    if (data.success) {
                        $.prompt(template("tipBox", {state: 'success'}), {
                            prefix: "null-popup",
                            buttons: {},
                            classes: {
                                fade: 'jqifade',
                                close: 'w-hide'
                            }
                        });
                    } else {
                        if (data.code == '400') {
                            $.prompt(template("tipBox", {state: 'finished'}), {
                                prefix: "null-popup",
                                buttons: {},
                                classes: {
                                    fade: 'jqifade',
                                    close: 'w-hide'
                                }
                            });
                        } else {
                            $17.alert(data.info);
                        }
                    }
                });
                $17.tongji('走遍美国新版活动', '宣传页按钮');
            });

            //宣传页进入按钮
            $(document).on('click', '#enterBut', function () {
                $17.tongji('走遍美国新版活动', '宣传页进入按钮');
                setTimeout(function () {
                    location.href = '/student/apps/index.vpage?app_key=TravelAmerica';
                }, 200);
            });
        });
    </script>

</@temp.page>