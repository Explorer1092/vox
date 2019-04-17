<div data-bind="visible: $data.showCheckBox()" class="mhw-slideBox" style="display: none; margin-top: 4rem;">
    <div class="mask"></div>
    <div class="innerBox">
        <div class="hd">
            <span data-bind="text :$data.checkHomeworkDetail().userName"></span>
            <span class="close" data-bind="click: closeCheckBoxBtn">×</span>
        </div>
        <div class="mn mhw-picBox">
            <!--ko if: $data.showType() == 1-->
            <div class="pic-list">
                <ul>
                    <!--ko foreach: {data: $data.checkHomeworkDetail().showPics, as : "_cd"}-->
                    <!--ko if: _cd.indexOf('.mp3') == -1-->
                    <li><img src="" data-bind="attr: {'src':_cd+$root.pictureQuality}"></li>
                    <!--/ko-->
                    <!--/ko-->
                </ul>
            </div>
            <!--/ko-->
            <div class="level-lable">
                <!--ko foreach: {data: $data.homeworkLevel(), as : "_le"}-->
                    <span data-bind="text: _le.showName(),visible: _le.show(),css: {'active': _le.checked()},click: $root.levelClick">--</span>
                <!--/ko-->
            </div>
        </div>
        <div class="mhw-btns">
            <a href="javascript:void(0)" class="w-btn" data-bind="click: $data.checkSubmitBtn">确认</a>
        </div>
    </div>
</div>