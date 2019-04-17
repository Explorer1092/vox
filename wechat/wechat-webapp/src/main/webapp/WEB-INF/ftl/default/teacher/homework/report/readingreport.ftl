
<div style="height: 100%; overflow: hidden; overflow-y: scroll;-webkit-overflow-scrolling : touch;">
    <div class="mhw-emptyBox"></div>
    <div class="mhw-header mar-b14">
        <div class="header-inner">
            <h2 class="title"><!--ko text: $root.getCategoryNameByValue('READING')--><!--/ko-->答题详情</h2>
        </div>
    </div>
    <div class="single-details">
        <div class="s-table">
            <table colspan="0" cellpadding="0" cellspacing="0">
                <thead>
                <tr>
                    <td>绘本</td>
                    <td>完成</td>
                    <td>得分</td>
                    <td>用时</td>
                </tr>
                </thead>
                <tbody>
                <!-- ko foreach : {data : $data.currentReportDetail().pictureBookInfo, as : '_reading'} -->
                <tr>
                    <td class="pointText"><a href="javascript:void(0)" data-bind="text: _reading.pictureBookName(), click: $root.readingScoreDetailClick">--</a></td>
                    <td data-bind="text: _reading.finishedCount()+'/'+_reading.totalUserNum()">--/--</td>
                    <td data-bind="text: _reading.avgScore()">--</td>
                    <td class="time"><span data-bind="text: $root.secondsToMinute(_reading.avgDuration())"></span></td>
                </tr>
                <!--/ko-->
                </tbody>
            </table>
        </div>
    </div>
    <div class="mhw-emptyBox"></div>
</div>

<#--绘本阅读成绩详情-->
<!--ko if: $root.readingScoreDetailBox-->
<div class="mhw-slideBox" data-bind="visible: $root.readingScoreDetailBox" style="display: none;">
    <div class="mask"></div>
    <div class="innerBox">
        <div class="hd">成绩详情<span class="close" data-bind="click: function(){$root.readingScoreDetailBox(false)}">×</span>
        </div>
        <div class="mn mhw-slideOverflow">
            <ul class="pb-gradeDetails">
                <!-- ko foreach : {data : $root.readingScoreDetail(), as : '_de'} -->
                <li>
                    <div class="name" data-bind="text: _de.userName()">--</div>
                    <div class="grade" data-bind="text: _de.score()+'分'">--</div>
                </li>
                <!--/ko-->

            </ul>
        </div>
        <div class="mhw-btns">
            <a href="javascript:void(0)" class="w-btn" data-bind="click: function(){$root.readingScoreDetailBox(false)}">关闭</a>
        </div>
    </div>
</div>
<!--/ko-->

