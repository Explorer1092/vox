<div id="reviewhomework" style="display:none" class="h-homeworkPreview">
    <div class="w-base"><h2 class="h2-title">预览</h2></div>
</div>

<script id="T:EXAM_REVIEW" type="text/html">
    <div class="hPreview-main <%=type%>">
        <div class="hp-title">
            <h3><%=typeName%></h3>
        </div>
        <%for(var i = 0; i < data.length; i++){%>
            <div class="h-set-homework" style="display:<%if(i >= 3){ %> none <%}%>;">
                <div class="seth-hd">
                    <p class="fl">
                        <%if(type == 'BASIC_KNOWLEDGE' && data[i].testMethodName){%><span><%= data[i].testMethodName%></span><%}%>
                        <%if(type != 'BASIC_KNOWLEDGE'){%><span><%= data[i].questionType%></span><%}%>
                        <span><%= data[i].difficultyName%></span>
                        <%if(data[i].assignTimes > 0 && data[i].teacherAssignTimes == 0){%><span class="noBorder">被使用<%= data[i].assignTimes%>次</span><%}%></p>
                </div>
                <div class="seth-mn" id="review_<%=type%>_<%= data[i].id%>">题目加载中...</div>
                <div class="h-btnGroup">
                    <a href="javascript:void(0)" class="J_subjectRemove btn cancel" groupid="<%=data[i].groupId%>" qid="<%= data[i].id%>" seconds="<%= data[i].seconds%>" category="<%=type%>">移除</a>
                </div>
            </div>
        <%}%>
        <%if(data.length > 3){%>
        <div class="J_showMoreQuestions t-dynamic-btn" data-tabtype="<%=type%>" data-lastindex="3" style="margin: 8px 15px;">
            <a class="more" href="javascript:void(0);">展开更多</a>
        </div>
        <%}%>
    </div>
</script>

<script id="T:MENTAL_REVIEW" type="text/html">
    <div class="hPreview-main <%=type%>">
        <div class="hp-title">
            <h3><%=typeName%></h3>
        </div>
        <div class="h-set-homework">
            <div class="seth-mn t-choose-Knowledge">
                <table class="t-questionBox">
                    <tbody>
                    <%for(var i = 0; i < data.length; i++){%>
                        <%if(i%3==0){%><tr><% }%>
                            <td><div class="t-question" id="<%=('HR-' + data[i].questionId + '-' + i)%>">正在加载...</div></td>
                        <%if(i+1%3==0){%></tr><% }%>
                    <%}%>
                    </tbody>
                </table>
                <div class="w-clear"></div>
            </div>
            <div class="h-btnGroup">
                <a href="javascript:void(0)" class="J_subjectRemove btn cancel" category="<%=type%>">移除</a>
            </div>
        </div>
    </div>
</script>

<script id="T:QUIZ_REVIEW" type="text/html">
    <div class="hPreview-main <%=type%>">
        <div class="hp-title">
            <h3><%= typeName%></h3>
        </div>
        <div class="testPaper-info">
            <p class="title" title="<%= paperName%>"><%= paperName%></p>
            <p>出卷人：<%= paperSource%></p>
            <div class="J_deleteAll-quiz h-btnGroup" category="<%=type%>">
                <a href="javascript:void(0)" class="btn cancel">移除试卷</a>
            </div>
        </div>
        <%for(var i = 0; i < data.length; i++){%>
        <div class="h-set-homework" style="display:<%if(i >= 3){ %> none <%}%>;">
            <div class="seth-hd">
                <p class="fl"><span><%= data[i].questionType%></span><span><%= data[i].difficultyName%></span><%if(data[i].assignTimes > 0){%><span class="noBorder">被使用<%= data[i].assignTimes%>次</span><%}%></p>
            </div>
            <div class="seth-mn" id="review_<%=type%>_<%= data[i].id%>">题目加载中...</div>
            <div class="h-btnGroup">
                <a href="javascript:void(0)" class="J_subjectRemove btn cancel" qid="<%=data[i].id%>" category="<%=type%>" seconds="<%=data[i].seconds%>">移除</a>
            </div>
        </div>
        <%}%>

        <%if(data.length >= 3){%>
        <div class="J_showMoreQuestions t-dynamic-btn" data-tabtype="<%=type%>" data-lastindex="3" style="margin: 8px 15px;">
            <a class="more" href="javascript:void(0);">展开更多</a>
        </div>
        <%}%>
    </div>
</script>

<script id="T:PHOTO_VOICE_REVIEW" type="text/html">
    <div class="hPreview-main <%=type%>">
        <div class="hp-title">
            <h3><%=typeName%></h3>
        </div>
        <%for(var i = 0; i < data.length; i++){%>
        <div class="h-set-homework" style="display:<%if(i >= 3){ %> none <%}%>;">
            <div class="seth-hd">
                <p class="fl"><span><%= typeName%></span><span class="border-none">被使用<%= data[i].assignTimes%>次</span></p>
            </div>
            <div class="seth-mn" id="review_<%=type%>_<%= data[i].questionId%>">题目加载中...</div>
            <div class="h-btnGroup">
                <a href="javascript:void(0)" class="J_subjectRemove btn cancel" qid="<%=data[i].questionId%>" seconds="<%=data[i].seconds%>" category="<%=type%>">移除</a>
            </div>
        </div>
        <%}%>

        <%if(data.length >= 3){%>
        <div class="J_showMoreQuestions t-dynamic-btn" data-tabtype="<%=type%>" data-lastindex="3" style="margin: 8px 15px;">
            <a class="more" href="javascript:void(0);">展开更多</a>
        </div>
        <%}%>
    </div>
</script>

<script id="T:BASIC_APP_REVIEW" type="text/html">
    <div class="hPreview-main <%=type%>">
        <div class="hp-title">
            <h3><%=typeName%></h3>
        </div>
        <div class="e-lessonsBox">
            <%for(var i=0;i<data.length;i++){%>
            <div class="e-lessonsList" lid="<%=data[i].lessonId%>">
                <%if(data[i].lessonName){%><div class="el-title"><%=data[i].lessonName%></div><%}%>
                <div class="el-name"><%=data[i].sentences%></div>
                <div class="el-list">
                    <ul>
                        <%for(var j=0;j<data[i].items.length;j++){%>
                        <li categoryId="<%=data[i].items[j].categoryId%>" lessonId="<%=data[i].items[j].lessonId%>">
                            <div class="J_basicMask lessons-text">
                                <div class="lessons-mask">预览</div>
                                <i class="e-icons"><img src="<%=categoryIconPrefixUrl%>e-icons-<%=data[i].items[j].categoryIcon%>.png"></i>
                                <span class="text"><%=data[i].items[j].categoryName%></span>
                            </div>
                            <div class="J_subjectRemove lessons-btn" groupid="<%=data[i].groupId%>" category="<%=type%>" seconds="<%=data[i].items[j].seconds%>">
                                <div><p>移除</p></div>
                            </div>
                            <%if(data[i].items[j].teacherAssignTimes > 0){%>
                            <div class="w-bean-location"><i class="w-icon w-icon-34"></i></div><!--已布置icon-->
                            <%}%>
                        </li>
                        <%}%>
                    </ul>
                </div>
            </div>
            <%}%>
        </div>
    </div>
</script>

<script id="T:NATURAL_SPELLING_REVIEW" type="text/html">
    <div class="hPreview-main <%=type%>">
        <div class="hp-title">
            <h3><%=typeName%></h3>
        </div>
        <div class="e-lessonsBox">
            <%for(var i=0;i<data.length;i++){%>
            <div class="e-lessonsList" lid="<%=data[i].lessonId%>">
                <%if(data[i].lessonName){%><div class="el-title"><%=data[i].lessonName%></div><%}%>
                <%for(var t = 0,categoryGroups = data[i].categoryGroups,tLen = categoryGroups.length; t < tLen; t++){%>
                <div class="el-name" ctgroupid="<%=categoryGroups[t].categoryGroupId%>">
                    <%for(var j = 0,jLen = categoryGroups[t].sentences.length; j < jLen; j++){%>
                        <%if(categoryGroups[t].newLine){%>
                            <p><%=categoryGroups[t].sentences[j]%></p>
                        <%}else{%>
                            <%=categoryGroups[t].sentences[j]%><%if(j != jLen -1){%>/<%}%>
                        <%}%>
                    <%}%>
                </div>
                <div class="el-list" ctgroupid="<%=categoryGroups[t].categoryGroupId%>">
                    <ul>
                        <%for(var j=0,categories = categoryGroups[t].categories;j<categories.length;j++){%>
                        <li categoryId="<%=categories[j].categoryId%>" homeworkType="<%=type%>" lessonId="<%=categories[j].lessonId%>">
                            <div class="J_basicMask lessons-text">
                                <div class="lessons-mask">预览</div>
                                <i class="e-icons"><img src="<%=categoryIconPrefixUrl%>e-icons-<%=categories[j].categoryIcon%>.png"></i>
                                <span class="text"><%=categories[j].categoryName%></span>
                            </div>
                            <div class="J_subjectRemove lessons-btn" category="<%=type%>" seconds="<%=categories[j].seconds%>">
                                <div><p>移除</p></div>
                            </div>
                            <%if(categories[j].teacherAssignTimes > 0){%>
                            <div class="w-bean-location"><i class="w-icon w-icon-34"></i></div><!--已布置icon-->
                            <%}%>
                        </li>
                        <%}%>
                    </ul>
                </div>
                <%}%>
            </div>
            <%}%>
        </div>
    </div>
</script>



<script id="T:READ_RECITE_REVIEW" type="text/html">
    <div class="hPreview-main <%=type%>">
        <div class="hp-title">
            <h3>课文读背题</h3>
        </div>
        <%for(var i = 0; i < data.length; i++){%>
        <div class="h-set-homework" style="display:<%if(i >= 3){ %> none <%}%>;">
            <div class="seth-hd">
                <p class="fl"><span><%= data[i].paragraphCName%></span><span class="noBorder"><%= data[i].articleName%></span></p>
            </div>
            <div class="seth-mn" id="review_<%=type%>_<%= data[i].questionId%>">题目加载中...</div>
            <div class="h-btnGroup">
                <a href="javascript:void(0)" class="J_subjectRemove btn cancel" qid="<%= data[i].questionId%>" seconds="<%=data[i].seconds%>" category="<%=type%>">移除</a>
            </div>
        </div>
        <%}%>
        <%if(data.length >= 3){%>
        <div class="J_showMoreQuestions t-dynamic-btn" data-tabtype="<%=type%>" data-lastindex="3" style="margin: 8px 15px;">
            <a class="more" href="javascript:void(0);">展开更多</a>
        </div>
        <%}%>
    </div>
</script>

<script id="T:NEW_READ_RECITE_REVIEW" type="text/html">
    <div id="newReadReciteTPL" class="read-aDetails hPreview-main NEW_READ_RECITE">
        <div class="r-title r-sub-tit">课文读背</div>
        <!--ko foreach: $root.selData-->
        <!--ko if: $data.data.length > 0-->
        <div class="aDetails-section aDetails-popup">
            <div class="r-title" data-bind="text:$data.name"></div>
            <!--ko foreach:{data : $data.data,as:'item'}-->
            <div class="r-inner">
                <div class="readTxt-list2">
                    <div class="frAside" data-bind="css:{'showDetails':item.showDetail},click:$root.showDetail">
                        <span>展开段落</span>
                        <span class="arrow"></span>
                    </div>
                    <div class="fl-aside">
                        <p class="name" data-bind="text:item.lessonName"></p>
                        <p class="textGray" data-bind="text:item.selectchaper"></p>
                    </div>
                </div>
                <div class="readTxt-details" style="display: none;" data-bind="visible: item.showDetail()">
                    <!--ko foreach:item.sortQuestions-->
                    <div class="readInner" data-bind="attr:{pNumber:$data.paragraphNumber}">
                        <div class="r-sub-title">
                            <span data-bind="text:'第'+$data.paragraphNumber+'段'"></span>
                            <!--ko if: $data.paragraphImportant-->
                            <span class="label">（重点段落）</span>
                            <!--/ko-->
                        </div>
                        <div class="r-inner" data-bind="attr:{id:'review_NEW_READ_RECITE_'+ $data.id}">题目加载中...</div>
                        <div class="r-playBtn">
                            <a href="javascript:void(0)" class="J_subjectRemove btn-option" data-bind="attr:{qid:$data.id,seconds:$data.seconds,category:'NEW_READ_RECITE',qBoxid:item.questionBoxId}">移除</a>
                        </div>
                    </div>
                    <!--/ko-->
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->
        <!--/ko-->
    </div>
</script>


<script id="T:READ_RECITE_WITH_SCORE_REVIEW" type="text/html">
    <div id="ReadReciteWithScoreTPL" class="read-aDetails hPreview-main READ_RECITE_WITH_SCORE">
        <div class="r-title r-sub-tit">课文读背</div>
        <!--ko foreach: $root.selData-->
        <!--ko if: $data.data.length > 0-->
        <div class="aDetails-section aDetails-popup">
            <div class="r-title" data-bind="text:$data.name"></div>
            <!--ko foreach:{data : $data.data,as:'item'}-->
            <div class="r-inner">
                <div class="readTxt-list2">
                    <div class="frAside" data-bind="css:{'showDetails':item.showDetail && item.showDetail()},click:$root.showDetail">
                        <span data-bind="text:item.showDetail && item.showDetail() ? '收起段落' : '展开段落'"></span>
                        <span class="arrow"></span>
                    </div>
                    <div class="fl-aside">
                        <p class="name" data-bind="text:item.lessonName"></p>
                        <p class="textGray" data-bind="text:item.selectchaper"></p>
                    </div>
                </div>
                <div class="readTxt-details" style="display: none;" data-bind="visible: item.showDetail && item.showDetail()">
                    <!--ko foreach:item.sortQuestions-->
                    <div class="readInner" data-bind="attr:{pNumber:$data.paragraphNumber}">
                        <i class="itag" style="display: none;" data-bind="visible:$data.paragraphImportant"></i>
                        <div class="r-sub-title">
                            <span data-bind="text:'第'+$data.paragraphNumber+'段'"></span>
                            <i class="label audio-play" style="display: none;" data-bind="css:{'audio-play':$data.id != $root.playingQuestionId(),'audio-pause':$data.id == $root.playingQuestionId()},visible:$data.listenUrls && $data.listenUrls.length > 0,click:$root.playAudio.bind($data,$root,$element)"></i>
                        </div>
                        <div class="r-inner" data-bind="attr:{id:'review_READ_RECITE_WITH_SCORE_'+ $data.id}">题目加载中...</div>

                        <div class="r-playBtn">
                            <a href="javascript:void(0)" class="J_subjectRemove btn-option remove" data-bind="attr:{qid:$data.id,seconds:$data.seconds,category:'READ_RECITE_WITH_SCORE',qBoxid:item.questionBoxId}">移除</a>
                        </div>
                    </div>
                    <!--/ko-->
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->
        <!--/ko-->
    </div>
</script>

<script id="T:READING_REVIEW" type="text/html">
    <div class="hPreview-main <%=type%>">
        <div class="hp-title">
            <h3>绘本阅读</h3>
        </div>
        <div class="hp-picture">
            <div class="e-pictureBox">
                <ul class="clearfix">
                    <%for(var i = 0; i < data.length; i++){%>
                    <li class="e-pictureList" qid="<%= data[i].pictureBookId%>">
                        <div class="J_readingPreview title"><a href="javascript:void(0)"><%= data[i].pictureBookName%></a></div>
                        <div class="J_readingPreview lPic">
                            <a href="javascript:void(0)"><img src="<%= data[i].pictureBookThumbImgUrl%>"></a>
                        </div>
                        <div class="rInfo">
                            <p class="text"><%= data[i].levels%></p>
                            <p class="text"><%= data[i].topics%></p>
                            <p class="text"><%= data[i].pictureBookSeries%></p>
                        </div>
                        <div class="h-btnGroup">
                            <a href="javascript:void(0)" class="J_subjectRemove btn cancel" qid="<%= data[i].pictureBookId%>" seconds="<%=data[i].seconds%>" category="<%=type%>">移除</a>
                        </div>
                    </li>
                    <%}%>
                </ul>
            </div>
        </div>
    </div>
</script>

<script id="T:LEVEL_READINGS_REVIEW" type="text/html">
    <div class="hPreview-main <%=type%>">
        <div class="hp-title">
            <h3>绘本阅读</h3>
        </div>
        <div class="hp-picture">
            <div class="e-pictureBox">
                <ul class="clearfix">
                    <%for(var i = 0; i < data.length; i++){%>
                    <li class="e-pictureList diffNew" qid="<%= data[i].pictureBookId%>">
                        <div class="J_levelReadingsPreview lPic">
                            <a href="javascript:void(0)"><img src="<%= data[i].pictureBookThumbImgUrl%>"></a>
                        </div>
                        <div class="rInfo">
                            <div class="J_levelReadingsPreview title"><a href="javascript:void(0)"><%= data[i].pictureBookName%></a></div>
                            <p class="text"><%= data[i].pictureBookClazzLevelName%></p>
                            <p class="text"><%= data[i].pictureBookTopics.join('、')%></p>
                            <p class="text"><%= data[i].pictureBookSeries%></p>
                            <p class="text">
                                <%for(var m = 0,mLen = data[i].practices.length; m < mLen; m++){%>
                                <span class="radioIcon selected"><i></i><%=data[i].practices[m].typeName%></span>
                                <%if((m+1)%2 == 0){%><br><%}%>
                                <%}%>
                            </p>
                        </div>
                        <div class="h-btnGroup">
                            <a href="javascript:void(0)" class="J_subjectRemove btn cancel" qid="<%= data[i].pictureBookId%>" seconds="<%=data[i].seconds%>" category="<%=type%>">移除</a>
                        </div>
                    </li>
                    <%}%>
                </ul>
            </div>
        </div>
    </div>
</script>

<script id="T:DUBBING_REVIEW" type="text/html">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3>趣味配音</h3>
        </div>
        <div class="e-picture-list">
            <div class="e-pictureBox">
                <ul class="clearfix">
                    <%for(var i = 0; i < data.length; i++){%>
                    <li class="e-pictureList-2">
                        <div class="picbox">
                            <div class="pic-box J_DubbingPreview" data-type="<%=type%>" qid="<%= data[i].dubbingId%>">
                                <img style="width: 100%;" src="<%=data[i].coverUrl%>" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'">
                                <a class="play-btn" href="javascript:void(0)"><span class=""></span></a>
                            </div>
                        </div>
                        <div class="video-info">
                            <p class="title"><%=data[i].name%></p>
                            <p class="text"><%=data[i].albumName%></p>
                            <p class="text"><%=data[i].clazzLevel + ' 共' + data[i].sentenceSize + '句'%></p>
                            <p class="text"><%=data[i].topics.join('、')%></p>
                        </div>
                        <div class="h-btnGroup">
                            <a href="javascript:void(0)" class="J_subjectRemove btn sbtn-remove" qid="<%= data[i].dubbingId%>" seconds="<%=data[i].seconds%>" category="<%=type%>">移除</a>
                        </div>
                    </li>
                    <%}%>
                </ul>
            </div>
        </div>
    </div>
</script>

<script id="T:CLAZZ_TYPE_REVIEW" type="text/html">
    <div class="gradeSelect-label gradeFilter">
        <p class="tipsGrey">根据各班级每周作业情况，提取共性错题，每周五更新。布置仅对指定班级有效。</p>
        <div class="labelBox">
            <%for(var i = 0; i < data.length; i++){%>
            <span data-groupId="<%=data[i].groupId%>" class="J_clazzTabType <%if(data[i].groupId == focusGroupId){%>active<%}%>"><%=data[i].groupName%></span>
            <%}%>
        </div>
    </div>
</script>


<script type="text/html" id="t:DUBBING_DETAIL_PREVIEW">
    <div class="tDubbing-popup">
        <div class="dubPopup-video">
            <div class="video" id="dubbingPlayVideoContainer"></div>
            <span class="playBtn" style="display: none;"></span>
        </div>
        <div class="dubPopup-info">
            <p class="name">
                <!--ko text:dubbingObj.albumName --><!--/ko--> • <!--ko text:dubbingObj.name--><!--/ko-->
                <!--ko if:homeworkType == 'DUBBING_WITH_SCORE'-->
                <a data-bind="click:$root.collectDubbing,css:{'selected':$root.isCollect()}" class="collect" href="javascript:void(0);"></a>
                <!--/ko-->
            </p>
            <p class="grade"><!--ko text:dubbingObj.clazzLevel--><!--/ko--><span class="txt-gray">被使用<i data-bind="text:dubbingObj.teacherAssignTimes">1</i>次</span></p>
            <p class="label">
                <!--ko foreach:{data:dubbingObj.topics,as:'topic'} -->
                <span data-bind="text:topic"></span>
                <!--/ko-->
            </p>
        </div>
        <div class="dubPopup-point">
            <div class="describe"><!--ko text:dubbingObj.summary--><!--/ko--></div>
            <div class="pointTitle">本集知识点</div>
            <div class="pointInfo">
                <p class="txt-type">• 词汇:</p>
                <p class="txt-point">
                    <!--ko foreach:{data:dubbingObj.keyWords,as:'keyWord'}-->
                    <span><!--ko text:keyWord.englishWord --><!--/ko--> <!--ko text:keyWord.chineseWord--><!--/ko--></span>
                    <!--/ko-->
                </p>
            </div>
            <div class="pointInfo">
                <p class="txt-type">• 语法:</p>
                <!--ko foreach:{data:dubbingObj.keyGrammars,as:'grammar'}-->
                <p class="txt-point"><!-- ko text:grammar.grammarName--><!--/ko--></p>
                <p class="txt-point"><!-- ko text:grammar.exampleSentence--><!--/ko--></p>
                <!--/ko-->
            </div>
        </div>
    </div>
</script>




<script id="T:OCR_MENTAL_REVIEW" type="text/html">
    <div class="hPreview-main OCR_MENTAL_ARITHMETIC" id="OCR_MENTAL_REVIEW_TPL">
        <div class="hp-title">
            <h3>纸质口算练习</h3>
        </div>
        <div class="h-set-homework">
            <div class="seth-mn">
                <div class="paperInfoBox">
                    <div class="paperTop">
                        <div class="exerBook">
                            <p class="exerTitle">练习册名称</p>
                            <p class="exerCon" data-bind="text:workBook.workBookName">&nbsp;</p>
                        </div>
                    </div>
                    <div class="paperTop">
                        <div class="exerBook2">
                            <p class="exerTitle">练习详情（页码）</p>
                            <p class="exerCon" data-bind="text:workBook.homeworkDetail">&nbsp;</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="h-btnGroup">
                <a href="javascript:void(0)" class="btn cancel" data-bind="click:$root.deleteWorkBook">移除</a>
            </div>
        </div>
    </div>
</script>

<script id="T:WORD_COGNITION_AND_READING_REVIEW" type="text/html">
    <div class="read-aDetails hPreview-main <%=type%>">
        <div class="r-title r-sub-tit">生字认读</div>
        <div class="aDetails-section aDetails-popup">
            <%for(var j = 0,jLen = packageList.length; j < jLen; j++){%>
            <div class="r-inner">
                <div class="readTxt-list2" style="position:relative;">
                    <div class="frAside" style="position: absolute;right: -56px;top: 14px;">
                        <a class="J_subjectRemove" href="javascript:void(0);" qid="<%=packageList[j].questionBoxId%>" seconds="<%=packageList[j].seconds%>" category="<%=type%>" style="display: inline-block;width: 62px;height: 38px;text-align: center;line-height: 40px;color: #2180c1;background: #e1f0fc;border: 1px solid #abc1d3;border-radius:2px;">移除</a>
                    </div>
                    <div class="fl-aside">
                        <p class="name"><%=packageList[j].lessonName%></p>
                        <p class="textGray"><%=('生字认读:共'+ packageList[j].questionNum + '个生字')%></p>
                    </div>
                </div>
            </div>
            <%}%>
        </div>
    </div>
</script>

<script id="T:ORAL_COMMNUNICATION_REVIEW" type="text/html">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3>口语交际</h3>
        </div>
        <div class="e-picture-list">
            <div class="e-pictureBox">
                <ul class="clearfix">
                    <%for(var i = 0; i < data.length; i++){%>
                    <li class="e-pictureList-2">
                        <div class="picbox">
                            <div class="pic-box J_DubbingPreview" data-type="<%=type%>" qid="<%= data[i].oralCommunicationId%>">
                                <img style="width: 100%;" src="<%=data[i].thumbUrl%>" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'">
                                <a class="play-btn" style="display:none;" href="javascript:void(0)"><span class=""></span></a>
                            </div>
                        </div>
                        <div class="video-info">
                            <p class="title"><%=data[i].oralCommunicationName%></p>
                            <p class="text"><%=data[i].clazzLevel%></p>
                            <#--<p class="text"><%=data[i].sentences.join('、')%></p>-->
                        </div>
                        <div class="h-btnGroup">
                            <a href="javascript:void(0)" class="J_subjectRemove btn sbtn-remove" qid="<%= data[i].oralCommunicationId%>" seconds="<%=data[i].seconds%>" category="<%=type%>">移除</a>
                        </div>
                    </li>
                    <%}%>
                </ul>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="t:ORAL_COMMUNICATION_DETAIL_PREVIEW">
    <div class="tDubbing-popup">
        <div class="dubPopup-video">
            <div class="video" style="display: block;">
                <img style="width: 100%;" data-bind="attr:{src:item.coverUrl}" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'"  >
            </div>
            <span class="playBtn" style="display: none;"></span>
        </div>
        <div class="dubPopup-info">
            <p class="name">
                <!--ko text:item.oralCommunicationName --><!--/ko-->
            </p>
            <p class="grade">
                <span class="txt-gray" data-bind="if:item.teacherAssignTimes > 0,visible:item.teacherAssignTimes > 0">被使用<i data-bind="text:item.teacherAssignTimes">1</i>次</span>
            </p>
            <p class="label" style="display: none;">
                <span></span>
            </p>
        </div>
        <div class="dubPopup-point">
            <div class="describe"><!--ko text:item.description--><!--/ko--></div>
            <div class="pointTitle">内容信息</div>
            <div class="pointInfo">
                <p class="txt-point">
                    学习句型：
                    <span><!--ko text:item.sentences ? item.sentences.join(" / ") : '' --><!--/ko--></span>
                </p>
                <p class="txt-point">年级：<!--ko text:item.clazzLevel--><!--/ko--></p>
                <p class="txt-point">生词量：<!--ko text:item.wordsCount--><!--/ko-->个</p>
            </div>
        </div>
        <div style="font-size: 14px; line-height: 14px; color: #f93a18;">提示：若想了解情景包的更多内容，请登录一起小学老师端app</div>
    </div>
</script>

<script type="text/html" id="T:WORD_TEACH_AND_PRACTICE_PREVIEW">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3>字词讲练</h3>
        </div>
        <%for(var t = 0,tLen = sectionList.length; t < tLen; t++){%>
        <div class="wordBox J_sectionBox">
            <p class="wordTitle"><%=sectionList[t].sectionName%></p>
            <div class="packetCard">
                <%for(var z = 0,zLen = sectionList[t].stoneData.length; z < zLen; z++){%>
                <div class="packet J_packet">
                    <div class="packetTitle">
                        <span class="packetName">字词讲练</span>
                        <div class="select remove J_subjectRemove" stonedataid="<%= sectionList[t].stoneData[z].stoneDataId%>" seconds="<%=sectionList[t].stoneData[z].seconds%>" category="<%=type%>"><i></i>移除</div>
                    </div>
                    <div class="packetBox">
                        <%
                        var selectModules = sectionList[t].stoneData[z].selectModules;
                        for(var k = 0,kLen = selectModules.length; k < kLen; k++ ){
                            var moduleObj,moduleUnit;
                            var wordsPractice = sectionList[t].stoneData[z].wordsPractice;
                            switch(selectModules[k]){
                                case "CHINESECHARACTERCULTURE":
                                    moduleObj = wordsPractice.chineseCharacterCultureMap;
                                    moduleUnit = "题";
                                    break;
                                case "IMAGETEXTRHYME":
                                    moduleObj = wordsPractice.imageTextMap;
                                    moduleUnit = "篇";
                                    break;
                                case "WORDEXERCISE":
                                    moduleObj = wordsPractice.wordExerciseMap;
                                    moduleUnit = "课程";
                                    break;
                                default:
                                    break;
                            }
                        %>
                        <%if(moduleObj){%>
                        <div class="singlePacket">
                            <%if(moduleObj.showAssigned){%>
                            <i class="recommended"></i>
                            <%}%>
                            <p class="signlePacketTitle"><%=moduleObj.questionBoxTypeTitle%><i></i></p>
                            <p class="packetCon">共<%=moduleObj.questionCount%><%=moduleUnit%></p>
                            <div class="prohibitBtn"></div>
                        </div>
                        <%}%>
                        <%}%>
                    </div>
                </div>
                <%}%>
            </div>
        </div>
        <%}%>
    </div>
</script>

<script type="text/html" id="t:DICTATION_PREVIEW">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3>单词听写</h3>
        </div>
        <div class="new-topicPackage-hd">
            <div class="l-topic-inner">
                <div class="title">
                    <div class="dictation-title"><%=title%></div>
                </div>
            </div>
            <div class="allCheck checked dictation-delete-all" category="<%=type%>" seconds="<%=seconds%>"><!--全选被选中时添加类checked-->
                <p class="check-btn">
                    <span class="w-checkbox"></span>
                    <span class="w-icon-md">全移除</span>
                </p>
            </div>
        </div>

        <%for(var m = 0,mLen = lessonList.length; m < mLen; m++){%>
        <div class="h-set-homework examTopicBox lesson-dictation">
            <div class="seth-hd">
                <p class="fl">
                    <span class="noBorder"><%=lessonList[m].lessonName%></span>
                </p>
            </div>
            <div class="seth-mn">
                <div class="testPaper-info">
                    <div class="inner">
                        <div class="wordListBox">
                            <ul>
                                <%for(var k = 0,kLen = lessonList[m].questions.length; k < kLen; k++){%>
                                <li class="worldSingle selectedSingle single-dictation-question" qid="<%=lessonList[m].questions[k].id%>" seconds="<%=lessonList[m].questions[k].seconds%>" category="<%=type%>"><%=lessonList[m].questions[k].sentence%></li>
                                <%}%>
                            </ul>
                        </div>
                    </div>
                    <div class="btnGroup">
                        <a href="javascript:void(0)" class="J_subjectRemove btn cancel" lessonid="<%=lessonList[m].lessonId%>" seconds="<%=lessonList[m].seconds%>" category="<%=type%>"><i class="h-set-icon h-set-icon-cancel"></i>移除</a>
                    </div>
                </div>
            </div>
        </div>
        <%}%>
    </div>
</script>