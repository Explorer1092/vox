<script id="t:WORD_TEACH_AND_PRACTICE" type="text/html">
    <div data-bind="if:$root.packageList().length > 0,visible:$root.packageList().length > 0">
        <!--ko foreach:{data : $root.packageList,as:'sectionObj'}-->
        <div class="wordBox">
            <p class="wordTitle" data-bind="text:sectionObj.sectionName">&nbsp;</p>
            <div class="packetCard" data-bind="foreach:{data:sectionObj.stoneData,as:'packageObj'}">
                <div class="packet">
                    <div class="packetTitle">
                        <span class="packetName">字词讲练</span>
                        <div class="select" data-bind="css:{'remove' : packageObj.packageChecked()},click:$root.addPackage.bind($data,$root,sectionObj)">
                            <i></i><!--ko text:packageObj.packageChecked() ? '移除' : '选入'--><!--/ko-->
                        </div>
                    </div>
                    <div class="packetBox">
                        <div class="singlePacket" data-bind="if:packageObj.wordsPractice.wordExerciseMap,visible:packageObj.wordsPractice.wordExerciseMap,
                        click:$root.previewModule.bind(packageObj.wordsPractice.wordExerciseMap,$data,$root,sectionObj)">
                            <i class="recommended" data-bind="visible:packageObj.wordsPractice.wordExerciseMap.showAssigned"></i>
                            <p class="signlePacketTitle" data-bind="text:packageObj.wordsPractice.wordExerciseMap.questionBoxTypeTitle">&nbsp; <i></i></p>
                            <p class="packetCon">共<!--ko text:packageObj.wordsPractice.wordExerciseMap.questionCount--><!--/ko-->题&nbsp;|&nbsp;预计<!--ko text:Math.ceil(packageObj.wordsPractice.wordExerciseMap.seconds/60)--><!--/ko-->分钟</p>
                            <div class="removeBtn"
                                 data-bind="css:{'selectBtn':packageObj.selectModules.indexOf(packageObj.wordsPractice.wordExerciseMap.questionBoxType) != -1,
                             'removeBtn':packageObj.selectModules.indexOf(packageObj.wordsPractice.wordExerciseMap.questionBoxType) == -1},
                            click: $root.moduleClick.bind(packageObj.wordsPractice.wordExerciseMap,$data,$root,sectionObj),clickBubble:false"></div>
                        </div>
                        <div class="singlePacket" data-bind="if:packageObj.wordsPractice.imageTextMap,visible:packageObj.wordsPractice.imageTextMap,
                        click:$root.previewModule.bind(packageObj.wordsPractice.imageTextMap,$data,$root,sectionObj)">
                            <i class="recommended" data-bind="visible:packageObj.wordsPractice.imageTextMap.showAssigned"></i>
                            <p class="signlePacketTitle" data-bind="text:packageObj.wordsPractice.imageTextMap.questionBoxTypeTitle">&nbsp; <i></i></p>
                            <p class="packetCon">共<!--ko text:packageObj.wordsPractice.imageTextMap.questionCount--><!--/ko-->篇&nbsp;|&nbsp;预计<!--ko text:Math.ceil(packageObj.wordsPractice.imageTextMap.seconds/60)--><!--/ko-->分钟</p>
                            <div class="removeBtn"
                                 data-bind="css:{'selectBtn':packageObj.selectModules.indexOf(packageObj.wordsPractice.imageTextMap.questionBoxType) != -1,
                             'removeBtn':packageObj.selectModules.indexOf(packageObj.wordsPractice.imageTextMap.questionBoxType) == -1},
                              click: $root.moduleClick.bind(packageObj.wordsPractice.imageTextMap,$data,$root,sectionObj),clickBubble:false"></div>
                        </div>
                        <div class="singlePacket" data-bind="if:packageObj.wordsPractice.chineseCharacterCultureMap,
                        visible:packageObj.wordsPractice.chineseCharacterCultureMap,click:$root.previewModule.bind(packageObj.wordsPractice.chineseCharacterCultureMap,$data,$root,sectionObj)">
                            <i class="recommended" data-bind="visible:packageObj.wordsPractice.chineseCharacterCultureMap.showAssigned"></i>
                            <p class="signlePacketTitle" data-bind="text:packageObj.wordsPractice.chineseCharacterCultureMap.questionBoxTypeTitle">&nbsp; <i></i></p>
                            <p class="packetCon">
                                共<!--ko text:packageObj.wordsPractice.chineseCharacterCultureMap.questionCount--><!--/ko-->课程&nbsp;|&nbsp;预计<!--ko text:Math.ceil(packageObj.wordsPractice.chineseCharacterCultureMap.seconds/60)--><!--/ko-->分钟
                            </p>
                            <div class="removeBtn"
                                 data-bind="css:{'selectBtn':packageObj.selectModules.indexOf(packageObj.wordsPractice.chineseCharacterCultureMap.questionBoxType) != -1,
                             'removeBtn':packageObj.selectModules.indexOf(packageObj.wordsPractice.chineseCharacterCultureMap.questionBoxType) == -1},
                            click: $root.moduleClick.bind(packageObj.wordsPractice.chineseCharacterCultureMap,$data,$root,sectionObj),clickBubble:false"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!--/ko-->
    </div>
</script>

<script type="text/html" id="t:CHINESECHARACTERCULTURE_PREVIEW">
    <div class="wordInfoBox" style="margin-top: -20px;">
        <!--ko if:!$root.focusId()-->
        <div class="packetTitle">
            <span class="packetName" data-bind="text:$root.questionBoxTypeTitle">&nbsp;</span>
            <div class="select" data-bind="css:{'remove' : $root.selectModules.indexOf($root.questionBoxType) != -1},click:$root.addPackage.bind($data)">
                <i></i><!--ko text:$root.selectModules.indexOf($root.questionBoxType) != -1 ? '移除' : '选入'--><!--/ko-->
            </div>
        </div>
        <ul class="wordInfoCard" data-bind="if:$root.courses.length > 0,visible:$root.courses.length > 0">
            <!--ko foreach:{data:$root.courses,as:'courseObj'}-->
            <li>
                <div class="wordPic" data-bind="click:$root.switchMode.bind($root,courseObj.id)">
                    <img alt="汉字文化" data-bind="attr:{src:courseObj.cover.fileUrl}">
                </div>
                <div class="wordContent">
                    <p class="wordContentTitle" data-bind="text:courseObj.name">&nbsp;</p>
                    <p class="wordConTime">预计<!--ko text:Math.ceil(courseObj.seconds/60)--><!--/ko-->分钟完成</p>
                </div>
                <i class="recommended" data-bind="if:courseObj.showAssigned,visible:courseObj.showAssigned"></i>
            </li>
            <!--/ko-->
        </ul>
        <!--/ko-->
        <div class="vox17zuoyeIframeContainer" style="width: 100%;" data-bind="if:$root.focusId(),visible:$root.focusId()">
            <p class="popupTitle"><i class="speColor" data-bind="click:$root.switchMode.bind($root,null)">汉字文化&gt;</i>预览</p>
            <iframe class="vox17zuoyeIframe" data-bind="attr:{src: '/teacher/new/homework/previewteachingcourse.vpage?courseId=' + $root.focusId()}" width="100%" marginwidth="0" height="393" marginheight="0" scrolling="no" frameborder="0"></iframe>
        </div>
    </div>
</script>

<script type="text/html" id="t:IMAGETEXTRHYME_PREVIEW">
    <div class="wordInfoBox" style="margin-top: -20px;">
        <!--ko if:!$root.focusId()-->
        <div class="packetTitle">
            <span class="packetName" data-bind="text:$root.questionBoxTypeTitle">&nbsp;</span>
            <div class="select" data-bind="css:{'remove' : $root.selectModules.indexOf($root.questionBoxType) != -1},click:$root.addPackage.bind($data)">
                <i></i><!--ko text:$root.selectModules.indexOf($root.questionBoxType) != -1 ? '移除' : '选入'--><!--/ko--></div>
        </div>
        <ul class="wordInfoCard2" data-bind="if:$root.imageTextRhymeList.length > 0,visible:$root.imageTextRhymeList.length > 0">
            <!--ko foreach:{data:$root.imageTextRhymeList,as:'imgObj'}-->
            <li>
                <div class="wordPic" data-bind="click:$root.switchMode.bind($root,imgObj.id)">
                    <img alt="图片" data-bind="attr:{'src':imgObj.imageUrl}">
                </div>
                <div class="wordContent">
                    <p class="wordContentTitle" data-bind="text:imgObj.title">&nbsp;</p>
                    <p class="wordConTime">预计<!--ko text:Math.ceil(imgObj.seconds/60)--><!--/ko-->分钟完成</p>
                </div>
                <i class="recommended" data-bind="if:imgObj.showAssigned,visible:imgObj.showAssigned"></i>
            </li>
            <!--/ko-->
        </ul>
        <!--/ko-->
        <div class="vox17zuoyeIframeContainer" style="width: 100%;" data-bind="if:$root.focusId(),visible:$root.focusId()">
            <p class="popupTitle"><i class="speColor" data-bind="click:$root.switchMode.bind($root,null)">图文入韵&gt;</i>预览</p>
            <div style="width:320px; margin: 0 auto;">
                <iframe class="vox17zuoyeIframe" data-bind="attr:{src: $root.fetchPreviewUrl()}" width="100%" height="568" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"></iframe>
            </div>
            <p style="text-align: center;">注：电脑端暂时不支持打分，默认给3星。布置后学生答题，会根据学生朗读结果评星。</p>
        </div>
    </div>
</script>

<script type="text/html" id="t:WORDEXERCISE_PREVIEW">
    <div class="wordInfoBox" style="margin-top: -20px;">
        <!--ko if:!$root.focusId()-->
        <div class="packetTitle">
            <span class="packetName" data-bind="text:$root.questionBoxTypeTitle">&nbsp;</span>
            <div class="select" data-bind="css:{'remove' : $root.selectModules.indexOf($root.questionBoxType) != -1},click:$root.addPackage.bind($data)">
                <i></i><!--ko text:$root.selectModules.indexOf($root.questionBoxType) != -1 ? '移除' : '选入'--><!--/ko--></div>
        </div>
        <div id="formulaContainer" class="container-dialog" data-bind="if:$root.questions.length > 0,visible:$root.questions.length > 0">
            <!--ko foreach:{data:$root.questions,as:'question'}-->
            <div class="h-set-homework" data-bind="singleExamHover:false">
                <div class="seth-hd">
                    <p class="fl">
                        <span data-bind="text:question.questionType">单选</span>
                        <span data-bind="text:question.difficultyName">容易</span>
                        <!--ko if:question.assignTimes && question.assignTimes > 0 && (!question.teacherAssignTimes || question.teacherAssignTimes == 0)-->
                        <span class="noBorder" data-bind="text:'共被使用' + question.assignTimes() + '次'"></span>
                        <!--/ko-->
                    </p>
                    <p class="fr">
                        <!--ko if:question.teacherAssignTimes && question.teacherAssignTimes > 0-->
                        <span class="txtYellow" data-bind="style:{marginRight:(question.readingType ? '20px' : '0px')}">布置过</span>
                        <!--/ko-->
                    </p>
                </div>
                <div class="seth-mn">
                    <div class="testPaper-info">
                        <div class="inner">
                            <ko-venus-question params="questions:$root.getQuestion(question.id),contentId:'mathExamImg' + $index(),formulaContainer:'formulaContainer',onSendLog:$root.onSendLog.bind($root)"></ko-venus-question>
                        </div>
                        <div class="linkGroup">
                            <a href="javascript:void(0)" style="display: none;" class="viewExamAnswer" data-bind="click:$root.switchMode.bind($root,question.id)">查看答案解析</a>
                        </div>
                    </div>
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->
        <div class="vox17zuoyeIframeContainer" style="width: 100%;" data-bind="if:$root.focusId(),visible:$root.focusId()">
            <p class="popupTitle"><i class="speColor" data-bind="click:$root.switchMode.bind($root,null)">字词训练&gt;</i>预览</p>
            <iframe class="vox17zuoyeIframe" data-bind="attr:{src: '/teacher/new/homework/viewquestion.vpage?showIntervene=true&qids=' + $root.focusId()}" width="100%" marginwidth="0" height="393" marginheight="0" scrolling="no" frameborder="0"></iframe>
        </div>
    </div>
</script>