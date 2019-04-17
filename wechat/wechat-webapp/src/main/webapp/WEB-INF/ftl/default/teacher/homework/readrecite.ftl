<div data-bind="visible : $data.selectedHomeworkType() == 'READ_RECITE' " style="display: none;">
    <div class="mhw-kwdbaType">
        <div class="fl"><span class="text">答题方式：</span></div>
        <div id="answerWayBox" class="fr">
            <span class="label active" data-way="1000">朗读</span>
            <span class="label" data-way="1001">背诵</span>
        </div>
    </div>
    <div class="mse-main" style="border-bottom: 1px solid #d3d8df;">
        <div class="kwdb-title">
            <div class="fl">全部题目</div>
            <div class="fr">
            <span id="readReciteSelectAllBtn" class="allCheck">
                <i class="text">全选</i>
                <i class="w-icon w-icon-check"></i>
            </span>
            </div>
        </div>


        <!-- ko foreach: {data : $data.readReciteDetail(), as : '_read'} -->

        <div class="mhw-base noBorder-b">
            <div class="mb-hd clearfix">
                <div class="fl txt-grey">
                    <span data-bind="text: _read.paragraphCName()"></span>
                    <span data-bind="text: _read.articleName()"></span>
                </div>
            </div>
            <div class="mb-mn pad-30">
                <div>
                    <div data-bind="attr:{id : 'readReciteImg' + $index()}"></div>
                    <div data-bind="text:$root.loadReadReciteImg(_read.questionId(),$index())"></div>
                </div>

                <div class="mhw-selectBtns mar-t14">
                    <!--ko ifnot: _read.checked()-->
                    <a data-bind="click: $root.addReadRecite" href="javascript:void(0)" class="kwdb-correct">
                        <i class="icon"></i>
                    </a>
                    <!--/ko-->

                    <!--ko if: _read.checked()-->
                    <a data-bind="click: $root.removeReadRecite" class="kwdb-correct active" href="javascript:void(0)">
                        <i class="icon"></i>
                    </a>
                    <!--/ko-->

                </div>
            </div>
        </div>
        <!--/ko-->
    </div>
</div>



