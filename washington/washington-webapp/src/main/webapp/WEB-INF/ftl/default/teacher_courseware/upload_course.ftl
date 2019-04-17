<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='课件大赛'
pageJs=["index"]
pageJsFile={"index" : "public/script/teacher_courseware/upload"}
pageCssFile={"skin" : ["public/skin/teacher_courseware/css/skin"]}>
<div class="header">
    <div class="inline">
        <div class="title">我的课件</div>
    </div>
</div>
<div class="main_content">
    <div class="upload_box">
        <ul class="upload_tab">
            <li data-bind="css: {active: currentStep() == 1},click:switchStep.bind($data,1)"><span class="num">01</span>选择教材</li>
            <li data-bind="css: {active: currentStep() == 2},click:switchStep.bind($data,2)"><span class="num">02</span>上传课件</li>
            <li data-bind="css: {active: currentStep() == 3},click:switchStep.bind($data,3)"><span class="num">03</span>提交审核</li>
        </ul>
        <div data-bind="visible: currentStep() == 1">
            <div class="upload_info">
                <div class="up_step01">
                    <div class="selectItem">
                        <span class="label_type">册别：</span>
                        <div class="label_item" data-bind="foreach:termList">
                            <p data-bind="css: {selected:$root.term() == value},click:$root.switchTerm"><span class="radioIcon"></span><span data-bind="text:termDesc"></span></p>
                        </div>
                    </div>
                    <div class="selectItem">
                        <span class="label_type">年级：</span>
                        <div class="label_item" data-bind="foreach:gradeList">
                            <p data-bind="css: {selected:$root.clazzLevel() == value},click:$root.switchClassLevel"><span class="radioIcon"></span><span data-bind="text:classDesc"></span></p>
                        </div>
                    </div>
                    <div class="clearFloat">
                        <div class="selectItem w_750">
                            <div class="jc_title">教材列表
                                <span data-bind="text:bookDesc" style="font-size: 14px;"></span>
                                <div class="searchBox">
                                    <input placeholder="请输入关键字搜索" maxlength="40" data-bind="value:bookName,event:{keydown:searchTeachingBookInfo}">
                                    <i class="searchIcon" data-bind="click:getTeachingBookInfo"></i>
                                </div>
                            </div>
                            <table class="jc_table" data-bind="foreach: { data: bookList, as: 'book' },visible:bookList().length != 0">
                                <tr data-bind="foreach: { data: items, as: 'item' }">
                                    <td class="js-bItem" data-bind="click:$root.chooseBook,attr:{bid:item.id}"><span class="material" data-bind="text:item.name"></span></td>
                                </tr>
                            </table>
                            <div class="emptyData" data-bind="visible:bookList().length == 0">没有搜索到这本教材，换一个试试吧～</div>
                        </div>
                        <div class="selectItem w_350">
                            <span class="label_type">单元：</span>
                            <div class="label_item">
                                已选择：<span data-bind="text:unitDesc"></span>
                            </div>
                            <div class="pullDown_box" data-bind="css:{hide:showCurrentUnitList}"><!--hide收起-->
                                <div class="pullName" data-bind="click:switchUnitDisplay">更换单元<span class="arrowIcon pull"></span></div>
                                <div class="pullBox">
                                    <ul data-bind="foreach: { data: currentUnitList, as: 'item' }">
                                        <li data-bind="text:unitName,click:$root.chooseUnit,attr:{uid:item.unitId}" class="js-uItem"></li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="upload_btn">
                <a href="javascript:void(0)" class="up_btn" data-bind="click:updateBookInfo">下一步</a>
            </div>
        </div>

        <div data-bind="visible: currentStep() == 2">
            <div class="upload_info">
                <div class="up_step02">
                    <div class="jc_info">
                        <p><span class="label">册别：</span><span data-bind="text:termDesc"></span></p>
                        <p><span class="label">年级：</span><span data-bind="text:clazzDesc"></span></p>
                        <p><span class="label">教材：</span><span data-bind="text:bookDesc"></span></p>
                        <p><span class="label">单元：</span><span data-bind="text:unitDesc"></span></p>
                    </div>
                    <div class="jc_info">
                        <div class="title">课件名称：</div>
                        <div class="nameIpt">
                            <input placeholder="请输入课件名称（不能超过15个字）" size="15" maxlength="15" data-bind="value:course_name">
                        </div>
                    </div>
                    <div class="jc_info">
                        <div class="title">教学课件：</div>
                        <div class="docIpt">
                            <input value="test.pptx" disabled="disabled" data-bind="value:coursewareFileName">
                            <span class="docUpload">上传教学课件</span>
                            <input type="file" name="courseFile"
                                   data-bind="event:{change:chooseCourseFile}"
                                   style="width: 130px;height: 35px;margin-top: -33px;margin-left: 28px;opacity: 0;cursor: pointer;"
                                   accept="application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation">
                        </div>
                    </div>
                    <div class="jc_info">
                        <div class="title">教学设计：</div>
                        <div class="designIpt">
                            <textarea placeholder="请输入内容（不能超过500个字）" data-bind="value:teachingDesign" maxlength="500"></textarea>
                        </div>
                    </div>
                </div>
                <div class="upload_btn">
                    <a href="javascript:void(0)" class="up_btn <#--disabled-->" data-bind="click:lastStep">上一步</a>
                    <a href="javascript:void(0)" class="up_btn" data-bind="click:updateContentInfo">下一步</a>
                </div>
            </div>
        </div>

        <div data-bind="visible: currentStep() == 3">
            <div class="upload_info">
                <div class="up_step03">
                    <div class="courseInfo">
                        <div class="left"><img data-bind="attr: { src: '<@app.link href=""/>'+coverImageUrl(), title: '教材封面' }"></div>
                        <div class="right">
                            <p class="name" data-bind="text:course_name"></p>
                            <p><span class="label">册别：</span><span data-bind="text:termDesc"></span></p>
                            <p><span class="label">年级：</span><span data-bind="text:clazzDesc"></span></p>
                            <p><span class="label">教材：</span><span data-bind="text:bookDesc"></span></p>
                            <p><span class="label">单元：</span><span data-bind="text:unitDesc"></span></p>
                        </div>
                    </div>
                    <div class="clearFloat">
                        <div class="docPreview">
                            <div class="title">教学课件 <span data-bind="text: coursewareFileName" style="font-size: 14px;"></span></div>
                            <div class="previewBox" id="perViewListContainer">
                                <p style="text-align: center;color: #968e8e;margin-top: 120px;">还未上传课件，请返回第二步去上传</p>
                            </div>
                        </div>
                        <div class="designPreview">
                            <div class="title">教学设计</div>
                            <div class="previewBox">
                                <textarea data-bind="value:teachingDesign" disabled="disabled"></textarea>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="upload_btn">
                    <a href="javascript:void(0)" class="up_btn <#--disabled-->" data-bind="click:lastStep">上一步</a>
                    <a href="javascript:void(0)" class="up_btn" data-bind="click:submitApply">提交审核</a>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="pop-loading" data-bind="visible:showLoading" style="display: none;">
    <div class="loadingInner">
        <i class="loadingIcon"></i>
        <p class="txt">附件上传中，请稍后...</p>
    </div>
</div>
<script>
    var cdnHeader = '<@app.avatar href="/"/>'.replace("/gridfs","");
</script>
</@layout.page>