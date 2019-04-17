<div class="w-base-container" data-bind="if: bookInfo().unitList != null">
    <dl class="w-imageText-list t-homework-teaching">
        <dt data-bind="click: changeBook">
            <dl class="w-imageText-list">
                <dt>
                    <span data-bind="attr: { class: 'w-build-image w-build-image-' + bookInfo().color() }">
                    <strong class="wb-title" data-bind="text: bookInfo().viewContent()"></strong>
                    <!-- ko if: bookInfo().latestVersion() -->
                    <span class="wb-new"></span>
                    <!-- /ko -->
                    </span>
                </dt>
            </dl>
            <h4 style="cursor: pointer;" data-bind="text: bookInfo().bookName()"></h4>
            <a href="javascript:void(0);" class="w-btn w-btn-mini" style="width: 130px;">
                <span class="w-icon-public w-icon-switch"></span><span class="w-icon-md">换教材</span>
            </a>
        </dt>
        <dd>
            <ul class="t-hootArrow-list">
                <!-- ko foreach: bookInfo().unitList() -->
                <li class="hoot-line" data-bind="css: { 'w-blue': $parent.focusUnit() == unitId() }">
                    <div class="hoot-box">
                        <p class="c-1" data-bind="click: $parent.changeUnit.bind($data, $index(), $parent)">
                            <span class="w-hook" data-bind="css: { 'w-hook-current': $parent.focusUnit() == unitId() }"></span>
                        </p>
                        <p class="c-2" data-bind="text: cname(), attr: { title: cname() }, click: $parent.changeUnit.bind($data, $index(), $parent)"></p>
                        <p class="c-4" title="展开" data-bind="if: $parent.hasContent($index(), $parent) > 0, visible: $parent.hasContent($index(), $parent) > 0, click: $parent.changeInfoStaus">
                            <span class="w-icon-arrow" data-bind="css: { 'w-icon-arrow-blue': $parent.focusUnit() == unitId() && !isOpen(), 'w-icon-arrow-topBlue': $parent.focusUnit() == unitId() && isOpen(), 'w-icon-arrow-top': $parent.focusUnit() != unitId() && isOpen() }"></span>
                        </p>
                        <p class="c-3" data-bind="click: $parent.changeUnit.bind($data, $index(), $parent)">
                            <span data-bind="if: abacus.englishBasic() > 0, visible: abacus.englishBasic() > 0">基础练习 <strong data-bind="text: abacus.englishBasic()"></strong></span>
                            <span data-bind="if: abacus.mathBasic() > 0, visible: abacus.mathBasic() > 0">计算练习 <strong data-bind="text: abacus.mathBasic()"></strong></span>
                            <span data-bind="if: abacus.special() > 0, visible: abacus.special() > 0">专项训练 <strong data-bind="text: abacus.special()"></strong></span>
                            <span data-bind="if: abacus.reading() > 0, visible: abacus.reading() > 0">阅读练习 <strong data-bind="text: abacus.reading()"></strong></span>
                            <span data-bind="if: abacus.exam() > 0, visible: abacus.exam() > 0">同步习题 <strong data-bind="text: abacus.exam()"></strong></span>
                        </p>
                    </div>
                    <div class="hoot-info" data-bind="visible: isOpen()">
                        <p data-bind="if: abacus.englishBasic() > 0, visible: abacus.englishBasic() > 0">
                            <span class="hi-count">基础练习已经选择
                                <strong data-bind="text: abacus.englishBasic()"></strong> 道题
                            </span>
                            <span class="hi-time">估计用时
                                <strong data-bind="text: abacus.englishBasicTime()"></strong> 分钟
                            </span>
                        </p>
                        <p data-bind="if: abacus.mathBasic() > 0, visible: abacus.mathBasic() > 0">
                            <span class="hi-count">计算练习已经选择
                                <strong data-bind="text: abacus.mathBasic()"></strong> 道题
                            </span>
                            <span class="hi-time">估计用时
                                <strong data-bind="text: abacus.mathBasicTime()"></strong> 分钟
                            </span>
                        </p>
                        <p data-bind="if: abacus.special() > 0, visible: abacus.special() > 0">
                            <span class="hi-count">专项训练已经选择
                                <strong data-bind="text: abacus.special()"></strong> 道题
                            </span>
                            <span class="hi-time">估计用时
                                <strong data-bind="text: abacus.specialTime()"></strong> 分钟
                            </span>
                        </p>
                        <p data-bind="if: abacus.reading() > 0, visible: abacus.reading() > 0">
                            <span class="hi-count">阅读练习已经选择
                                <strong data-bind="text: abacus.reading()"></strong> 道题
                            </span>
                            <span class="hi-time">估计用时
                                <strong data-bind="text: abacus.readingTime()"></strong> 分钟
                            </span>
                        </p>
                        <p data-bind="if: abacus.exam() > 0, visible: abacus.exam() > 0">
                            <span class="hi-count">同步习题已经选择
                                <strong data-bind="text: abacus.exam()"></strong> 道题
                            </span>
                            <span class="hi-time">估计用时
                                <strong data-bind="text: abacus.examTime()"></strong> 分钟
                            </span>
                        </p>
                    </div>
                </li>
                <!-- /ko -->
            </ul>
        </dd>
    </dl>
</div>