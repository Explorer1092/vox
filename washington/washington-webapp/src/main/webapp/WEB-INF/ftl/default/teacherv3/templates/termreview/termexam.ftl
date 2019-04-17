<script id="T:TERM_EXAM" type="text/html">
    <!--ko if: $root.packageList().length > 0-->
    <div class="h-topicPackage" style="margin-bottom: 0" data-bind="attr:{name:'J_'+$root.termType}">
        <div class="topicBox" style="padding: 0 18px;">
            <ul>
                <!--ko foreach:$root.packageList()-->
                <li class="slideItem" data-bind="css:{active: $index()==0},click:$root.changePackage.bind($data,$element,$root), clickBubble: false">
                    <p data-bind="text:$data.name()"></p>
                    <!--ko if: $data.selCount() > 0-->
                    <span class="state" data-bind="text:$data.selCount()"></span>
                    <!--/ko-->
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>
    <!--/ko-->
    <!--ko if: $root.currentPackage().questions && $root.currentPackage().questions().length > 0-->
    <div class="h-topicPackage-hd">
        <div class="allCheck" data-bind="css:{'checked':$root.currentPackageSelected},click:$root.addQuestion.bind($data,$element,$data,'click_package')">
            <p class="check-btn">
                <span class="w-checkbox"></span>
                <span class="w-icon-md" data-bind="text:$root.currentPackageQidCount('totalCount')"></span>
            </p>
            <span class="txt-left">预计<i data-bind="text:$root.currentPackageQidCount('totalTime')"></i></span>
        </div>
    </div>
    <!--ko foreach:$root.focusExamList()-->
    <div class="h-set-homework examTopicBox">
        <div class="seth-hd">
            <p class="fl">
                <span data-bind="text:$data.questionType"></span>
                <span data-bind="text:$data.difficultyName"></span>
                <!--ko if:$data.assignTimes() > 0-->
                <span class="noBorder" data-bind="text:'被使用'+$data.assignTimes()+'次'"></span>
                <!--/ko-->
            </p>
        </div>
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" data-bind="attr:{id:'subjective_'+$data.id()}">题目加载中...</div>
                <div style="display: none;" data-bind="text:$root.initSubject($data.id())"></div>
                <div class="btnGroup">
                    <a href="javascript:void(0)" data-bind="click:$root.viewQuestion.bind($data,$root)">预览</a>
                    <!--ko if:$data.checked()-->
                    <a href="javascript:void(0)" class="btn cancel" data-bind="click:$root.addQuestion.bind($data,$element,$parent,'click_question')">移除</a>
                    <!--/ko-->
                    <!--ko if:!$data.checked()-->
                    <a href="javascript:void(0)" class="btn" data-bind="click:$root.addQuestion.bind($data,$element,$parent,'click_question')">选入</a>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->
    <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:$root.termPage,if:$root.termPage != null}"></div>
    <!--/ko-->
    <!--ko if: $root.packageList().length == 0-->
    <div class="h-set-homework current">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p>暂无数据，请选择其他内容</p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->
</script>
