<#import "../../layout.ftl" as homeworkReport>
<@homeworkReport.page title="作业报告" pageJs="voiceRecommend">
    <@sugar.capsule css=['homework','jbox'] />
    <div data-bind="visible: showStep1()" style="display: none">
        <div class="mjd-content">
            <div class="title-blue"><!--ko text: clazzName-->--<!--/ko--></div>
            <div class="hl-scrollNav">
                <ul data-bind="style: {'width': 10.6 * categoryVoiceList().length+'rem'}"><!--10.6rem*li个数-->
                    <!-- ko foreach : {data : categoryVoiceList(), as : '_list'} -->
                    <li class="active" data-bind="css: {'active': _list.checked()}">
                        <a href="javascript:void(0)" data-bind="text: _list.name(),click: $root.categoryBtn"></a>
                        <span class="tips" data-bind="text: _list.count(), visible: _list.count() > 0" style="display: none;">0</span>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
            <div class="hl-head" style="background: #fff; text-align: center;">
                <p>请选择要推荐的录音，最多可选5个</p>
            </div>
            <div class="hl-record">
                <table>
                    <thead>
                    <tr>
                        <td>姓名</td>
                        <td>录音</td>
                        <td>得分</td>
                        <td class="check-icon">是否推荐</td>
                    </tr>
                    </thead>
                    <tbody>
                    <!-- ko foreach : {data : categoryVoiceList, as : '_cv'} -->
                    <!--ko if: _cv.checked()-->
                    <!-- ko foreach : {data : _cv.studentList, as : '_student'} -->
                    <tr>
                        <td><span class="name" data-bind="text: _student.studentName()"></span></td>

                        <td>
                            <span class="record-btn" data-bind="css: {'record-stop': _student.isPlay()},click: $root.voicePlayOrStopBtn.bind($data,$parent)">
                                <i class="icon"></i>点击<!--ko if: _student.isPlay()-->停止<!--/ko--><!--ko ifnot: _student.isPlay()-->播放<!--/ko-->
                            </span>
                        </td>
                        <td><span data-bind="text: _student.score()">--</span></td>
                        <td class="check-icon" data-bind="css: {'checked': _student.checked()}, click: $root.isRecommendBtn.bind($data,$parent)"><span class="icon"></span></td>
                    </tr>

                    <!--/ko-->
                    <!--/ko-->
                    <!--/ko-->
                    </tbody>
                </table>
            </div>
        </div>
        <div class="footer-empty" style="height: 7rem;">
            <div class="mhw-btns fixFooter" style="padding: 0;">
                <a href="javascript:void(0)" data-bind="visible: selectedStudentsCount() > 0, click: gotoStep2Btn" style="display: none;" class="w-btn">选好了，去推荐</a>
                <a href="javascript:void(0)" data-bind="visible: selectedStudentsCount() == 0" style="display: none;" class="w-btn disabled">选好了，去推荐</a>
            </div>
        </div>
    </div>

    <div id="jplayerId"></div>

    <div data-bind="visible: showStep2()" style="display: none">
        <div class="mjd-content">
            <div class="title-blue"><!--ko text: clazzName-->--<!--/ko--></div>
            <div class="hl-head">
                <p>添加评语（选填）</p>
            </div>
            <div class="hl-textarea">
                <textarea placeholder="这些同学读得不错哟！" data-bind="value: recommendComment" maxlength="100"></textarea>
            </div>
        </div>
        <div class="mjd-content">
            <div class="hl-head">
                <p>已选择以下录音</p>
            </div>
            <div class="hl-record">
                <table>
                    <thead>
                    <tr>
                        <td>姓名</td>
                        <td>题型</td>
                        <td>录音</td>
                    </tr>
                    </thead>
                    <tbody>
                    <!-- ko foreach : {data : categoryVoiceList, as : '_cv'} -->
                    <!-- ko foreach : {data : _cv.studentList, as : '_student'} -->
                    <!--ko if: _student.checked()-->
                    <tr>
                        <td><span class="name" data-bind="text: _student.studentName()"></span></td>
                        <td><span data-bind="text: _cv.name()">--</span></td>
                        <td>
                            <span class="record-btn" data-bind="css: {'record-stop': _student.isPlay()},click: $root.voicePlayOrStopBtn.bind($data,$parent)">
                                <i class="icon"></i>点击<!--ko if: _student.isPlay()-->停止<!--/ko--><!--ko ifnot: _student.isPlay()-->播放<!--/ko-->
                            </span>
                        </td>

                    </tr>
                    <!--/ko-->
                    <!--/ko-->
                    <!--/ko-->
                    </tbody>
                </table>
            </div>
        </div>
        <div class="footer-empty" style="height: 7rem;">
            <div class="mhw-btns fixFooter" style="padding: 0;">
                <a href="javascript:void(0)" class="w-btn" data-bind="click: saveVoiceCommendBnt">选好了，去推荐</a>
            </div>
        </div>
    </div>
</@homeworkReport.page>