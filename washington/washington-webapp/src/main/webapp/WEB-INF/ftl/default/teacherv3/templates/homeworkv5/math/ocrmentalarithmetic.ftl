<script id="t:OCR_MENTAL_ARITHMETIC" type="text/html">
    <div class="paperWorkBox">
        <div class="paperWorkCard" data-bind="visible:$root.schema() == 'WRITE'">
            <div class="paperWork">
                <div class="paperBox">练习册名称</div>
                <div class="outInput">
                    <input placeholder="输入口算练习册名称或自由添加作业内容" class="paperInput2" data-bind="textInput: workBookName" type="text">
                <#--<span class="choiceSub" data-bind="click:$root.workBookPopup">选择练习册</span>-->
                </div>
            </div>
            <div class="paperWork">
                <div class="paperBox">
                    <span class="paperTitle">练习详情（页码）</span>
                </div>
                <div class="outInput">
                    <input placeholder="建议输入练习册页码 例：3-5" class="paperInput2" data-bind="attr:{'placeholder':homeworkDetailPlaceholder},textInput: homeworkDetail" type="text">
                </div>
            </div>
            <div class="paperCard">
                <span class="arran">＊布置页数不能超过5页</span>
                <span class="lookSup" data-bind="click:$root.supportTypePopup">查看支持题型</span>
            </div>
            <div class="addBtn" data-bind="click:$root.addWorkBookItem">添加</div>
            <div class="explainBox">
                <p class="explainTitle2">新增：诊断学生计算错因，讲解计算方法</p>
                <div class="step">
                    <div class="stepSign">
                        <span class="stepPic1"></span>
                        <p>口算拍照</p>
                    </div>
                    <div class="arrow"></div>
                    <div class="stepSign">
                        <span class="stepPic2"></span>
                        <p>诊断错误</p>
                    </div>
                    <div class="arrow"></div>
                    <div class="stepSign">
                        <span class="stepPic3"></span>
                        <p>微课学习</p>
                    </div>
                </div>
                <span class="paperTitle" style="cursor: pointer;" data-bind="click:$root.viewCourse"><i></i>查看课程示例</span>
            </div>
        </div>
        <div class="paperInfoBox" data-bind="visible:$root.schema() == 'READ'">
            <div class="paperTop">
                <div class="exerBook">
                    <p class="exerTitle">练习册名称</p>
                    <p class="exerCon" data-bind="text:$root.workBookName">&nbsp;</p>
                </div>
                <div class="edit" data-bind="click:$root.editWorkBookItem">编辑</div>
            </div>
            <div class="paperTop">
                <div class="exerBook2">
                    <p class="exerTitle">作业详情（页码）</p>
                    <p class="exerCon" data-bind="text:$root.homeworkDetail">&nbsp;</p>
                </div>
            </div>
        </div>
        <p class="paperInfo">若布置过程中遇到问题。建议使用移动客户端布置</p>
    </div>
</script>

<script id="t:WORK_BOOK_POPUP" type="text/html">
    <div class="unitInfo" data-bind="foreach:{data:$root.workBooks(),as:'workBook'}">
        <div class="exerCard" data-bind="click:$root.selectWorkBookClick.bind($data,$index(),$root)">
            <div class="exerPic" data-bind="css:{'active':$index() == $root.focusIndex()}">
                <img data-bind="attr:{src:workBook.coverImageUrl}" alt="">
            </div>
            <div class="exerInfo" data-bind="text:workBook.workBookName">&nbsp;</div>
        </div>
    </div>
</script>

<script id="t:OCR_MENTAL_SUPPORT_QUESTION_TYPE" type="text/html">
    <div class="unitInfo">
        <div class="tips">
            此版本支持自动识别并批改的题型如下，更多题型正在加紧支持，敬请期待。
        </div>
        <div class="subPic"></div>
    </div>
</script>