<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="每个熊孩子背后都有对熊爸妈？00后家庭生活大测试"
pageJs=["imageLoad","main","loading","dialog"]
pageJsFile={"imageLoad" : "public/script/project/lifetest/imageLoad","main" : "public/script/project/lifetest/main","loading" : "public/script/project/lifetest/loading","dialog" : "public/script/project/lifetest/dialog"}
pageCssFile={"reward" : ["public/skin/project/lifetest/css/main"]}
>

<div style="position: absolute;opacity: 0;filter: alpha(opacity=0); ">
    <img src='<@app.link href='/public/skin/project/lifetest/images/lifetest_logo.png'/>'/>
</div>

<script type="text/javascript" charset="utf-8" src="<@app.link href='public/script/project/lifetest/zepto.js'/>"></script>
<script type="text/javascript" charset="utf-8" src="<@app.link href='public/script/voxLogs.js'/>"></script>

<!-- loading start -->
<div class="loading-wrap loading bg" id="J_Loading">
    <div class="loading-box">
        <div class="loading-img-w" style="padding-bottom:15px; ">
            <img src="<@app.link href='public/skin/project/lifetest/images/txt1.png'/>" alt="">
        </div>
        <div class="progress_bar"><b></b><span></span></div>
        <div class="progress"><span>0%</span></div>
    </div>
    <div class="loading-tm-box">
        <p class='tm tm1'>90后空巢老人表示十分期待</p>
        <p class='tm tm2'>前方高能预警，非战斗人员迅速退下</p>
        <p class='tm tm3'>不要回答！不要回答！</p>
        <p class='tm tm4'>边吃老干妈边看内牛满面</p>
        <p class='tm tm5'>80后垂死病中惊坐起续命围观</p>
    </div>
</div>
<!-- loading end -->

<!-- 题目 start -->
<div class="sal-box box1 web-box" id="J_SalIndex" style='display: none;'>
    <div class="music-set">
        <div  class="music-outer">
            <p class="music-on" id="music-on"><img src="<@app.link href='public/skin/project/lifetest/images/music_on.png'/>" alt=""></p>
            <p class="music-off" id="music-off" style="top:-100px;"><img src="<@app.link href='public/skin/project/lifetest/images/music_off.png'/>" alt=""></p>
        </div>
    </div>
    <div class="sec1"></div>
    <div class="sec2" style="margin-top: 0%;height: 60%;">
        <div class="item2">
            <div class="page1-icons">
                <div class="page1-icon1">
                    <img class="page1-icon1-1" src="<@app.link href='public/skin/project/lifetest/images/page1-icon-1.png'/>" alt=""/>
                    <img class="page1-icon1-2" src="<@app.link href='public/skin/project/lifetest/images/page1-icon-2.png'/>" alt=""/>
                </div>
                <div class="page1-icon2">
                    <img src="<@app.link href='public/skin/project/lifetest/images/page1-icon-3.png'/>" alt=""/>
                </div>
                <div class="page1-icon3">
                    <img src="<@app.link href='public/skin/project/lifetest/images/page1-icon-4.png'/>" alt=""/>
                </div>
            </div>
            <!--<div class="i1"></div>
            <div class="i2"></div>-->
        </div>
    </div>
    <div class="sec3" style="margin-top: 7%">
        <div class="box1-bottom-pic1">
            <img src="<@app.link href='public/skin/project/lifetest/images/box1-bottom-pic-left.png'/>" alt="">
        </div>
        <div class="box1-bottom-pic2">
            <img src="<@app.link href='public/skin/project/lifetest/images/box1-bottom-pic.png'/>" alt="">
        </div>
        <img src="" alt="">
    </div>
    <div class="sec4" id="J_Btn1"></div>
</div>
<div class="sal-box box2 bg subject1" id="J_SalBox1">
    <!--<div class="sec1"></div>-->
    <div class="sec2" >
        <div class="item1"></div>
        <div class="item2" id="replace-img"></div>
        <div class="item3"></div>
        <div class="item4"></div>
    </div>
    <div class="sec3" style="margin-top: -5px;">
        <div class="btns">
            <div class="btn btn1" data-score="0">
                <span style="left: 22px;background-size: 80% 100%;"></span>
                <b></b>
            </div>
            <div class="btn btn2" data-score="10">
                <span style="left:24px;"></span>
                <b></b>
            </div>
            <!--<div class="btn btn3" data-score="20">
                <span></span>
                <b></b>
            </div>-->
        </div>
        <div class="next-btn J_NextBtn"></div>
    </div>
</div>
<div class="sal-box box2 bg subject2" id="J_SalBox2">
    <!--<div class="sec1"></div>-->
    <div class="sec2" style="background-size: 100% 90%">
        <div class="sec-pic01"></div>
        <!--<div class="sec-pic02"></div>-->
        <!--<div class="sec-pic03"></div>-->
        <div class="sec-pic04">
            <div class="sec-pic04-icon">
                <img src="<@app.link href='public/skin/project/lifetest/images/page2-content-icon.png'/>" alt="">
            </div>
            <div class="sec-pic04-icon1">
                <img src="<@app.link href='public/skin/project/lifetest/images/page2-content-icon1.png'/>" alt="">
            </div>
        </div>
    </div>
    <div class="sec3" style="margin-top: -9%;">
        <div class="btns">
            <div class="btn btn1  add-img" data-score="0">
                <div class="sec-pic05"></div>
                <div class="sec-pic06 sec-animate1"></div>
            </div>
            <div class="btn btn2  add-img" data-score="0">
                <div class="sec-pic07"></div>
                <div class="sec-pic08 sec-animate2"></div>
            </div>
            <div class="btn btn3  add-img" data-score="10">

                <div class="sec-pic09"></div>
                <div class="sec-pic10 sec-animate3"></div>
            </div>
            <div class="btn btn4  add-img" data-score="0">

                <div class="sec-pic11">
                    <img src="<@app.link href='public/skin/project/lifetest/images/page2-option-name4.png'/>" style="height: 90%" alt=""/>
                </div>
                <div class="sec-pic12 sec-animate4">
                    <img src="<@app.link href='public/skin/project/lifetest/images/page2-option-head4.png'/>" style="height: 90%" alt=""/>
                </div>
            </div>
        </div>
        <div class="next-btn J_NextBtn"></div>
    </div>
</div>
<div class="sal-box box2 bg subject3" id="J_SalBox3">
    <!--<div class="sec1"></div>-->
    <div class="sec2" style="background-size: 100% 98%">
        <div class="item1"></div>
        <!--<div class="item2"></div>-->
        <div class="item3"></div>
        <div class="item4"></div>
        <div class="item5"></div>
        <div class="item6"></div>
    </div>
    <div class="sec3" style="margin-top: -15px;">
        <div class="btns">
            <div class="btn btn1" data-score="10" style="height: 23%;">
                <span class="mod-s1"></span>
                <!--<b></b>-->
            </div>
            <div class="btn btn2" data-score="0" style="margin-top: 3px;height: 23%;">
                <span class="mod-s1"></span>
                <!--<b></b>-->
            </div>
            <div class="btn btn3" data-score="0" style="margin-top: 3px;height: 23%;">
                <span class="mod-s1"></span>
                <!--<b></b>-->
            </div>
        </div>
        <div class="next-btn J_NextBtn"></div>
    </div>
</div>
<div class="sal-box box2 bg subject4" id="J_SalBox4">
    <!--<div class="sec1"></div>-->
    <div class="sec2">
        <div class="item1"></div>
        <div class="item2"></div>
        <div class="item8"></div>
        <div class="item3 animated rotate"></div>
        <div class="item4 animated pulse"></div>
        <div class="item5"></div>
        <!--<div class="item6"></div>-->
    </div>
    <div class="sec3" style="margin-top: -2%;">
        <div class="btns">
            <div class="btn btn1" data-score="10">
                <span class="mod-s2"></span>
                <b></b>
            </div>
            <div class="btn btn2" data-score="0">
                <span class="mod-s2"></span>
                <b></b>
            </div>
            <!--<div class="btn btn3" data-score="0">
                <span></span>
                <b></b>
            </div>-->
        </div>
        <div class="next-btn J_NextBtn"></div>
    </div>
</div>
<div class="sal-box box2 bg subject5" id="J_SalBox5">
    <div class="sec2">
        <div class="item1"></div>
        <!--<div class="item2 animated shake"></div>-->
        <div class="page5-content">
            <img src="<@app.link href='public/skin/project/lifetest/images/page5-content-pic.png'/>" alt="">
            <div class="page5-content-icon1">
                <img src="<@app.link href='public/skin/project/lifetest/images/page5-content-icon1.png'/>" alt="">
            </div>
            <div class="page5-content-icon2">
                <img src="<@app.link href='public/skin/project/lifetest/images/page5-content-icon2.png'/>" alt="">
            </div>
            <div class="page5-content-icon3">
                <img src="<@app.link href='public/skin/project/lifetest/images/page5-content-icon3.png'/>" alt="">
            </div>
            <div class="page5-content-icon4">
                <img src="<@app.link href='public/skin/project/lifetest/images/page5-content-icon4.png'/>" alt="">
            </div>
            <div class="page5-content-icon5">
                <img src="<@app.link href='public/skin/project/lifetest/images/page5-content-icon5.png'/>" alt="">
            </div>
        </div>
        <!--<div class="item4 animated fadeIn"></div>
        <div class="item5 animated fadeIn"></div>
        <div class="item6"></div>-->
    </div>
    <div class="sec3" style="margin-top: -10px;">
        <div class="btns">
            <div class="btn btn1" data-score="10" style="height: 23%;">
                <span class="mod-s3"></span>
                <b></b>
            </div>
            <div class="btn btn2" data-score="0" style="margin-top: 3px;height: 23%;">
                <span class="mod-s3"></span>
                <b></b>
            </div>
            <div class="btn btn3" data-score="0" style="margin-top: 3px;height: 23%;">
                <span class="mod-s3"></span>
                <b></b>
            </div>
        </div>
        <div class="next-btn J_NextBtn"></div>
    </div>
</div>
<!-- 题目 end -->

<!-- 每一步后的解析 start -->
<div class="overlay sub-over-item" id="J_OverLay">
    <div class="over-wrap">
        <div class="over-con over-con1" id="J_SubOverItem1">
            <div class="over-con-title" style="top:-47%;">
                <img src="<@app.link href='public/skin/project/lifetest/images/page1-answer-title.png'/>" alt="" style="width: 90%;">
            </div>
            <div class="item item1"></div>
            <!--<div class="item item2 animated shake"></div>-->
            <!--<div class="item item3"></div>-->
            <div class="item item4">
                <img src="<@app.link href='public/skin/project/lifetest/images/page1-answer.gif'/>" alt="" style="width: 90%;">
            </div>
        </div>
        <div class="over-con over-con2" id="J_SubOverItem2">
            <div class="over-con-title" style="top:-48%;">
                <img src="<@app.link href='public/skin/project/lifetest/images/page2-answer-title.png'/>" alt="">
            </div>
            <div class="item item1"></div>
            <div class="item item2">
                <img src="<@app.link href='public/skin/project/lifetest/images/page2-answer.gif'/>" alt="" style="width: 90%;">
            </div>
        </div>
        <div class="over-con over-con3" id="J_SubOverItem3">
            <div class="over-con-title" style="top:-48%;">
                <img src="<@app.link href='public/skin/project/lifetest/images/page3-answer-title.png'/>" alt="">
            </div>
            <div class="item item1"></div>
            <div class="item item2">
                <img src="<@app.link href='public/skin/project/lifetest/images/page3-answer.gif'/>" alt="" style="width: 90%">
            </div>
        </div>
        <div class="over-con over-con4" id="J_SubOverItem4">
            <div class="over-con-title" style="top:-51%;">
                <img src="<@app.link href='public/skin/project/lifetest/images/page4-answer-title.png'/>" alt="">
            </div>
            <div class="item item1"></div>
            <div class="item item8">
                <img src="<@app.link href='public/skin/project/lifetest/images/page4-answer.gif'/>" alt="" style="width: 90%;">
            </div>
            <!--<div class="item item2"></div>
            <div class="item item3"></div>
            <div class="item item4"></div>-->
        </div>
        <div class="over-con over-con5" id="J_SubOverItem5">
            <div class="over-con-title" style="top:-48%;">
                <img src="<@app.link href='public/skin/project/lifetest/images/page5-answer-title.png'/>" alt="">
            </div>
            <div class="item item1"></div>
            <div class="item item5">
                <img src="<@app.link href='public/skin/project/lifetest/images/page5-answer.gif'/>" alt="" style="width: 90%;">
            </div>
            <!--<div class="item item2 animated shake"></div>-->
            <!--<div class="item item3"></div>-->
        </div>
        <div class="over-btn-wrap">
            <img src="<@app.link href='public/skin/project/lifetest/images/over-btn-arrow.png'/>" alt="">
            <span class="over-btn" href="javascript:;" id="J_OverBtn"></span>
        </div>
    </div>
</div>
<!-- 每一步后的解析 end -->


<!-- 改卷中 start-->
<div class="paper-inspection bg" id="J_paperInspection">
    <div class="pi-01">
        <div class="line1"><i></i></div>
        <div class="line2"><i></i></div>
        <div class="line3"><i></i></div>
        <div class="line4"><i></i></div>
    </div>
    <div class="pi-02"></div>
</div>
<!-- 改卷中 end-->

<!-- 结果 start-->
<div class="result bg" id="J_Result">
    <div style="text-align: right;" id="J_ShareTipBtn_new"><img src="<@app.link href='public/skin/project/lifetest/images/share1.png'/>" id="share-img" style="width: 25%; alt=""></div>
    <!--只答对一道题-->
    <div class="result-item result-01" id="J_Result_01">
        <!-- <div class="top1"></div>
        <div class="box">
            <div class="text"></div>
            <div class="item1"></div>
            <div class="item2 animated shake"></div>
            <div class="item3"></div>
        </div> -->
        <div class="result-item1-head last-number-img"  style="text-align: center">
            <img src="<@app.link href='public/skin/project/lifetest/images/result-item1-head-0.png'/>" style="width: 90%;margin: 3px auto 0;" alt="">
        </div>
        <div class="result-item-head-btn">
            <div class="result-btn-content">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-item1-head-btn.png'/>" alt="">
            </div>
            <div class="result-btn-icon1">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-btn-icon1.png'/>" alt="">
            </div>
            <div class="result-btn-icon2">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-btn-icon2.png'/>" alt="">
            </div>
        </div>
        <div class="result-item1-content">
            <img src="<@app.link href='public/skin/project/lifetest/images/result-item1-content.png'/>" alt="">
            <div class="result-item1-content-icon1">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-item1-content-icon1.png'/>" alt="">
            </div>
            <div class="result-item1-content-icon2">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-item1-content-icon2.png'/>" alt="">
            </div>
            <div class="result-item1-content-icon3">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-item1-content-icon3.png'/>" alt="">
            </div>
        </div>
    </div>
    <!-- 答对2-3道题 -->
    <div class="result-item result-02" id="J_Result_02">
        <div class="result-item2-head last-number-img"  style="text-align: center">
            <img src="<@app.link href='public/skin/project/lifetest/images/result-item2-head-2.png'/>" style="width: 90%; margin: 3px auto 0;" alt="">
        </div>
        <div class="result-item-head-btn">
            <div class="result-btn-content">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-item1-head-btn.png'/>" alt="">
            </div>
            <div class="result-btn-icon1">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-btn-icon1.png'/>" alt="">
            </div>
            <div class="result-btn-icon2">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-btn-icon2.png'/>" alt="">
            </div>
        </div>
        <div class="result-item2-content"  style="text-align: center;">
            <img src="<@app.link href='public/skin/project/lifetest/images/result-item2-content.png'/>" alt="" style="width: 90%;">
            <div class="result-item2-content-icon1">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-item2-content-icon1.png'/>" alt="">
            </div>
            <div class="result-item2-content-icon2">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-item2-content-icon2.png'/>" alt="">
            </div>
        </div>
    </div>
    <!--答对 4-5道题 -->
    <!--<div class="result-item result-03" id="J_Result_03">
        <div class="top1"></div>
        <div class="box">
            <div class="text"></div>
            <div class="pic"></div>
            <div class="item1"></div>
            <div class="item2 animated fadeIn"></div>
            <div class="item3 animated shake"></div>
        </div>
    </div>-->
    <div class="result-item result-03" id="J_Result_03">
        <div class="result-item3-head last-number-img" style="text-align: center">
            <img src="<@app.link href='public/skin/project/lifetest/images/result-item3-head-4.png'/>" style="width: 90%;margin: 3px auto 0;" alt="">
        </div>
        <div class="result-item-head-btn">
            <div class="result-btn-content">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-item1-head-btn.png'/>" alt="">
            </div>
            <div class="result-btn-icon1">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-btn-icon1.png'/>" alt="">
            </div>
            <div class="result-btn-icon2">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-btn-icon2.png'/>" alt="">
            </div>
        </div>
        <div class="result-item3-content" style="text-align: center;">
            <img src="<@app.link href='public/skin/project/lifetest/images/result-item3-content.png'/>" alt="" style="width: 90%;">
            <div class="result-item3-content-icon1">
                <img src="<@app.link href='public/skin/project/lifetest/images/result-item3-content-icon1.png'/>" alt="">
            </div>
        </div>
    </div>
    <div class="result-btns" id="newPan">
        <#--<a href="javascript:void(0);" class="btn1" id="J_ShareTipBtn">
            <img id="share-btn" src="<@app.link href='public/skin/project/lifetest/images/result-btn1_308e1fd-a.png'/>" alt="">
        </a>-->
        <a href="javascript:void(0);" class="btn1" id="J_ShareTipBtn_app" style="display: none;">
            <img id="share-btn" src="<@app.link href='public/skin/project/lifetest/images/result-btn1_308e1fd-a.png'/>" alt="">
        </a>
    </div>
</div>
<!-- 结果 end-->

<div class="shareTip" id="J_ShareTip" ><span></span></div>
<audio src="<@app.link href='public/skin/project/lifetest/audio/bg.mp3'/>" autoplay="autoplay" loop="loop" preload="auto" id="J_BgAudio"></audio>
<script>
    var musicObj = document.getElementById('J_BgAudio');
    musicObj.play();
    document.addEventListener("WeixinJSBridgeReady", function () {
        musicObj.play();
    }, false);
    function getQuery(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    };
    var ua = window.navigator.userAgent.toLowerCase();
    if(ua.match(/MicroMessenger/i) == 'micromessenger'){
        YQ.voxLogs({
            database : 'wechat_logs_new',
            module: 'parenting',
            op : 'page_parenting_load',
            s0 : getQuery('refer')
        });
    }else{
        YQ.voxLogs({
            module: 'parenting',
            op : 'page_parenting_load',
            s0 : getQuery('refer')
        });
    }

    var cdnLocation = "";
    var hosts = location.host;
    if (hosts.indexOf("test.17zuoye")>-1){
        cdnLocation = '//cdn-cnc.test.17zuoye.net/';
    }else if(hosts.indexOf("staging.17zuoye")>-1){
        cdnLocation = '//cdn-cnc.staging.17zuoye.net/';
    }else if(hosts.indexOf("17zuoye.com")>-1){
        cdnLocation = '//cdn-cnc.17zuoye.cn/';
    }
    var imgPath = cdnLocation + "/public/skin/project/lifetest/images/";
    var loadImgArr = [
        imgPath + 'bg.jpg',
        imgPath + 'txt1.png',
        imgPath + 'btn2_f1ca824.png',
        imgPath + 'btn2_v_6469d45.png',
        imgPath + 'btn3_0143bd5.png',
        imgPath + 'btn4_71e3ae3.png',
        imgPath + 'btn5_2c29589.png',
        imgPath + 'btn5_v_6ab6453.png',
        imgPath + 'img2_f84e243.png',
        imgPath + 'img3_23bd631.png',
        imgPath + 'img4_ccd2b52.png',
        imgPath + 'img5_0cfaab8.png',
        imgPath + 'img6_75edcd5.png',
        imgPath + 'img7_3e7c6d8.png',
        imgPath + 'img8_b104371.png',
        imgPath + 'img9_2d6df82.png',
        imgPath + 'img10_7c39acf.png',
        imgPath + 'l1_bddf239.png',
        imgPath + 'l2_f92492f.png',
        imgPath + 'logo_822e42a.png',
        imgPath + 'over1-01_0df1a20.png',
        imgPath + 'over1-02_b9b053f.png',
        imgPath + 'over1-03_035932c.png',
        imgPath + 'over1-04_5d516df.png',
        imgPath + 'over1-05_b5d13be.png',
        imgPath + 'over1-06_adb6d76.png',
        imgPath + 'over1-07_a0e8c88.png',
        imgPath + 'over1-08_719a39b.png',
        imgPath + 'over2-01_92761e8.png',
        imgPath + 'over2-02_ca05d3b.png',
        imgPath + 'over2-03_20b5eb2.png',
        imgPath + 'over2-04_0ee9df0.png',
        imgPath + 'over3-01_0493841.png',
        imgPath + 'over3-02_9d17a26.png',
        imgPath + 'over3-03_f023292.png',
        imgPath + 'over3-04_0f6ed91.png',
        imgPath + 'over3-05_7560b93.png',
        imgPath + 'over4-01_7836639.png',
        imgPath + 'over4-02_0d377df.png',
        imgPath + 'over4-03_7e9698e.png',
        imgPath + 'over4-04_ed8c42f.png',
        imgPath + 'over4-05_380995a.png',
        imgPath + 'over4-06_540bbde.png',
        imgPath + 'over4-07_840a113.png',
        imgPath + 'over5-01_685fd46.png',
        imgPath + 'over5-01_685fd46.png',
        imgPath + 'over5-01_685fd46.png',
        imgPath + 'over5-02_09ebac6.png',
        imgPath + 'over5-03_7f1f901.png',
        imgPath + 'over6-01_f829bef.png',
        imgPath + 'over6-01_f829bef.png',
        imgPath + 'over6-02_7364d96.png',
        imgPath + 'over6-03_984a935.png',
        imgPath + 'over7-01_7fe26c4.png',
        imgPath + 'over7-02_1c3b51d.png',
        imgPath + 'over7-03_1530d12.png',
        imgPath + 'over7-04_c57f24e.png',
        imgPath + 'over8-01_7b71341.png',
        imgPath + 'over8-02_7172fdf.png',
        imgPath + 'over8-03_10f6727.png',
        imgPath + 'over8-04_28f98e9.png',
        imgPath + 'over8-05_5a58aed.png',
        imgPath + 'overBg_d9aec85.png',
        imgPath + 'pi-01_6678378.png',
        imgPath + 'pi-02_f06f3eb.png',
        imgPath + 'pi-line1_477dfce.png',
        imgPath + 'pi-line2_5baeaac.png',
        imgPath + 'pi-line3_c3d6386.png',
        imgPath + 'pi-line4_6e3eeb0.png',
        imgPath + 'result-01_677429e.png',
        imgPath + 'result-01-t2_2240bd0.png',
        imgPath + 'result-02_97b07e1.png',
        imgPath + 'result-02-t1_4e9bd0b.png',
        imgPath + 'result-02-t2_334d3ac.png',
        imgPath + 'result-03_61db031.png',
        imgPath + 'result-03-t1_0cb22a2.png',
        imgPath + 'result-03-t2_8926a17.png',
        imgPath + 'result-04_6fd1403.png',
        imgPath + 'result-04-t1_8a99840.png',
        imgPath + 'result-btn1_308e1fd.png',
        imgPath + 'result-btn2_b85128b.png',
        imgPath + 'result-main_7602801.png',
        imgPath + 'result-top_efbf57b.png',
        imgPath + 'result-top1_ef81986.png',
        imgPath + 'shareImg_7f45c8b.jpg',
        imgPath + 'subject1-01_ff7a01e.png',
        imgPath + 'subject1-02_0a71a6d.png',
        imgPath + 'subject1-03_e500cf2.png',
        imgPath + 'subject1-04_f5206f2.png',
        imgPath + 'subject1-05_7907efe.png',
        imgPath + 'subject1-06_5b3d19b.png',
        imgPath + 'subject1-07_ecc9d25.png',
        imgPath + 'subject1-08_6d413f3.png',
        imgPath + 'subject1-09_bb5c05e.png',
        imgPath + 'subject1-10_bfe6c9b.png',
        imgPath + 'subject1-11_81fe0d0.png',
        imgPath + 'subject1-12_a6eb8ae.png',
        imgPath + 'subject1-13_55362e0.png',
        imgPath + 'subject1-14_1aafd75.png',
        imgPath + 'subject2-01_42eb90d.png',
        imgPath + 'subject2-02_4bb6acd.png',
        imgPath + 'subject2-03_a9028f1.png',
        imgPath + 'subject2-04_2894b2b.png',
        imgPath + 'subject2-05_df5a4cc.png',
        imgPath + 'subject2-06_b093c4a.png',
        imgPath + 'subject2-06-1_f3faf8b.png',
        imgPath + 'subject2-06-2_640dce8.png',
        imgPath + 'subject2-07_5ddf549.png',
        imgPath + 'subject2-08_1d83fe3.png',
        imgPath + 'subject2-09_7dfc4e8.png',
        imgPath + 'subject2-10_a9f2d56.png',
        imgPath + 'subject2-11_c121532.png',
        imgPath + 'subject2-12_c824055.png',
        imgPath + 'subject2-13_c6689d7.png',
        imgPath + 'subject2-14_226abf8.png',
        imgPath + 'subject2-15_a2c0c62.png',
        imgPath + 'subject2-16_af029fb.png',
        imgPath + 'subject3-01_1fb260f.png',
        imgPath + 'subject3-02_0065edd.png',
        imgPath + 'subject3-02-1_c531951.png',
        imgPath + 'subject3-03_d58a024.png',
        imgPath + 'subject3-04_d4c9a71.png',
        imgPath + 'subject3-04-1_d161e82.png',
        imgPath + 'subject3-04-2_614ac7b.png',
        imgPath + 'subject3-05_db68b97.png',
        imgPath + 'subject3-06_34cb7d5.png',
        imgPath + 'subject3-07_dc5a07e.png',
        imgPath + 'subject3-08_1f6ba7a.png',
        imgPath + 'subject3-09_8965570.png',
        imgPath + 'subject3-10_c3c38c5.png',
        imgPath + 'subject4-01_c59d138.png',
        imgPath + 'subject4-02_b8e2b5c.png',
        imgPath + 'subject4-03_b8483c8.png',
        imgPath + 'subject4-04_771a02e.png',
        imgPath + 'subject4-05_48f4ebe.png',
        imgPath + 'subject4-06_a11b333.png',
        imgPath + 'subject4-07_c48da54.png',
        imgPath + 'subject4-08_55c3b02.png',
        imgPath + 'subject4-09_b2f8c2c.png',
        imgPath + 'subject4-09-1_7d509fb.png',
        imgPath + 'subject4-09-2_04e8548.png',
        imgPath + 'subject4-09-3_a09e7ff.png',
        imgPath + 'subject4-10_3809261.png',
        imgPath + 'subject4-11_66d8f76.png',
        imgPath + 'subject4-12_d7364c9.png',
        imgPath + 'subject4-13_404dcff.png',
        imgPath + 'subject4-14_cc4cda7.png',
        imgPath + 'subject4-15_6545a3c.png',
        imgPath + 'subject5-01_69ca7dd.png',
        imgPath + 'subject5-02_365ff0f.png',
        imgPath + 'subject5-02_365ff0f-a.png',
        imgPath + 'subject5-03_f2ac14a.png',
        imgPath + 'subject5-04_64237e4.png',
        imgPath + 'subject5-05_0d35b88.png',
        imgPath + 'subject5-06_ff9b6a1.png',
        imgPath + 'subject5-07_b1ef4c5.png',
        imgPath + 'subject5-08_7d842e8.png',
        imgPath + 'subject5-09_20f70f3.png',
        imgPath + 'subject5-10_bd09908.png',
        imgPath + 'subject5-11_51499a6.png',
        imgPath + 'subject6-01_f7f54ef.png',
        imgPath + 'subject6-02_9de9c28.png',
        imgPath + 'subject6-03_76ecf56.png',
        imgPath + 'subject6-04_376ef4e.png',
        imgPath + 'subject6-05_8b7d4b2.png',
        imgPath + 'subject6-06_95ff7e2.png',
        imgPath + 'subject6-07_821d6c0.png',
        imgPath + 'subject6-08_f179e26.png',
        imgPath + 'subject6-09_2efc2c5.png',
        imgPath + 'subject6-10_1c06032.png',
        imgPath + 'subject6-11_554f44f.png',
        imgPath + 'subject6-12_aadc3e6.png',
        imgPath + 'subject7-01_efcf83e.png',
        imgPath + 'subject7-02_519a52d.png',
        imgPath + 'subject7-03_bd7b345.png',
        imgPath + 'subject7-04_9416305.png',
        imgPath + 'subject7-05_9eaa1d3.png',
        imgPath + 'subject7-06_d29776f.png',
        imgPath + 'subject7-07_ab851a8.png',
        imgPath + 'subject7-08_e250840.png',
        imgPath + 'subject7-09_dedbeed.png',
        imgPath + 'subject7-10_fcec494.png',
        imgPath + 'subject8-01_0beb6b6.png',
        imgPath + 'subject8-02_a7d8c8c.png',
        imgPath + 'subject8-03_6cf8c84.png',
        imgPath + 'subject8-04_957a350.png',
        imgPath + 'subject8-05_6a55b46.png',
        imgPath + 'subject8-06_c48da54.png',
        imgPath + 'subject8-07_f464157.png',
        imgPath + 'subject8-08_91c3dfc.png',
        imgPath + 'subject8-09_8efdfd3.png',
        imgPath + 'subject8-10_8691cc8.png',
        imgPath + 'subject8-11_40d661c.png',
        imgPath + 'subject8-12_ad312af.png',
        imgPath + 'music_on.png',
        imgPath + 'share1.png',
        imgPath + 'share2.png',
        imgPath + 'result-btn1_308e1fd-a.png'
    ];
    $(function () {
        var J_ShareTipBtn_app = $("#J_ShareTipBtn_app");
        if (window['external'] && window.external['shareMethod']) {
            window.external.shareMethod( JSON.stringify({
                type: 'SHOW_NATIVE_BUTTON',
                title: "每个熊孩子背后都有对熊爸妈？00后家庭生活大测试",
                content: '查看详情',
                channel: 4,
                url: location.href
            }) );
        }

        J_ShareTipBtn_app.on('click', function () {
            if (window['external'] && window.external['shareMethod']) {
                window.external.shareMethod( JSON.stringify({
                    type: 'SHARE',
                    title: "每个熊孩子背后都有对熊爸妈？00后家庭生活大测试",
                    content: '查看详情',
                    channel: 4,
                    url: location.href
                }) );
            }
        });

        var isFromParentApp = window.navigator.userAgent.toLowerCase().indexOf("17parent") > -1;
        if(isFromParentApp && window['external'] && window.external['shareMethod']){
            $("#J_ShareTipBtn_new").hide();
            J_ShareTipBtn_app.show();
        }
    });
</script>
</@layout.page>