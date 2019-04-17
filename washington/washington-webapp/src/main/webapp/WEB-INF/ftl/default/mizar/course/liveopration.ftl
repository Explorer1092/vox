<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="17师训讲堂"
pageJs=['init']
pageJsFile={"init" : "public/script/mobile/mizar/liveopration"}
pageCssFile={"css" : ["public/skin/mobile/mizar/css/liveopration"]}
>
<div class="liveWrap" style="display:none;">
    <div class="liveSection"><img src="<@app.link href='public/skin/mobile/mizar/images/live-bg01.png'/>"></div>
    <div class="liveSection"><img src="<@app.link href='public/skin/mobile/mizar/images/live-bg02.png'/>"></div>
    <div class="liveSection mar80 liveScroll">
        <div class="lessonBox">
            <div class="lessonList">
                <span class="num num01"></span>
                <div class="txt">
                    <p>名师支招,让你的课堂效率事半功倍!</p>
                    <p class="time">5月5日 19:00-20:00</p>
                </div>
                <a href="javascript:void(0)" class="btn" data-bind="click: liveCourse.bind($data,'58feb1bf12a0cb646e85f8a7',timeFlag1(),payAll1(),status1(),waiting1(),offline1()),css:{ 'btn-green': timeFlag1() == 'BEFORE' && status1() ==0, 'btn-gray': timeFlag1() == 'BEFORE' && status1() == 2,'btn-red': timeFlag1() == 'ING','btn-gray2': timeFlag1() == 'AFTER' && waiting1(),'btn-yellow': timeFlag1() == 'AFTER' && !waiting1()}"></a>
            </div>
            <div class="lessonList">
                <span class="num num02"></span>
                <div class="txt">
                    <p>暖心家校沟通,从这里开始!</p>
                    <p class="time">5月12日 19:00-20:00</p>
                </div>
                <a href="javascript:void(0)" class="btn" data-bind="click: liveCourse.bind($data,'58feb20512a0cb646e860179',timeFlag2(),payAll2(),status2(),waiting2(),offline2()),css:{ 'btn-green': timeFlag2() == 'BEFORE' && status2() ==0, 'btn-gray': timeFlag2() == 'BEFORE' && status2() == 2,'btn-red': timeFlag2() == 'ING','btn-gray2': timeFlag2() == 'AFTER' && waiting2(),'btn-yellow': timeFlag2() == 'AFTER' && !waiting2()}"></a>
            </div>
            <div class="lessonList">
                <span class="num num03"></span>
                <div class="txt">
                    <p>不吼?不骂?!怎么管理"熊孩子"?</p>
                    <p class="time">5月19日 19:00-20:00</p>
                </div>
                <a href="javascript:void(0)" class="btn" data-bind="click: liveCourse.bind($data,'58feb24f12a0cb646e860ab1',timeFlag3(),payAll3(),status3(),waiting3(),offline3()),css:{ 'btn-green': timeFlag3() == 'BEFORE' && status3() ==0, 'btn-gray': timeFlag3() == 'BEFORE' && status3() == 2,'btn-red': timeFlag3() == 'ING','btn-gray2': timeFlag3() == 'AFTER' && waiting3(),'btn-yellow': timeFlag3() == 'AFTER' && !waiting3()}"></a>
            </div>
        </div>
    </div>
    <div class="liveSection pad25 mar80">
        <img src="<@app.link href='public/skin/mobile/mizar/images/live-bg03.png'/>">
    </div>
    <div class="liveSection pad25">
        <img src="<@app.link href='public/skin/mobile/mizar/images/live-bg04.png'/>">
        <a href="javascript:void(0)" class="btn-order" data-bind="click: gotoLiveLink"></a>
    </div>
    <div class="liveSection pad25 mar80">
        <img src="<@app.link href='public/skin/mobile/mizar/images/live-bg05.png'/>">
    </div>
    <div class="liveSection pad25 mar80">
        <img src="<@app.link href='public/skin/mobile/mizar/images/live-bg06.png'/>">
        <a href="javascript:void(0)" class="btn-order" data-bind="click: gotoLiveLink"></a>
    </div>
    <div class="liveSection pad25">
        <img src="<@app.link href='public/skin/mobile/mizar/images/live-bg07.png'/>">
        <a href="javascript:void(0)" class="btn-order" data-bind="click: gotoLiveLink"></a>
    </div>
</div>
</@layout.page>