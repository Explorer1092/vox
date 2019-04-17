<!doctype html>
<html>
<head>
<#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core", "alert", "ebox", "template", "DD_belatedPNG", "student"] css=["plugin.alert", "new_student.base", "new_student.module", "new_student.widget"] />
    <style>
        .vox_reduced_state{ clear: both; height: 60px; position: relative; z-index: 10000;}
        .vox_reduced_state .popup{ width: 100%; background-color: #fff2d0; /*position: fixed; top: 0; left: 0; _position:absolute;  _top:expression(documentElement.scrollTop);*/}
        .vox_reduced_state .inner{ width: 100%; overflow: hidden; margin: 0 auto; position: relative; color: #98682a; text-align: center; font-size: 16px; padding: 20px 0; white-space: nowrap;}
        .vox_reduced_state .inner .close{ position: absolute; top:5px; right: 10px; cursor: pointer; line-height: 1.125;}
    </style>
<@sugar.site_traffic_analyzer_begin />
</head>
<body>
<div class="vox_reduced_state">
    <div class="popup">
        <div class="inner text_yahei">
            该内容由一起作业网免费提供，更多学习资源请访问：<a href="/" class="w-blue" onclick="$17.tongji('gwchina-返回首页')">www.17zuoye.com</a>
        </div>
    </div>
</div>
<div class="t-learn-container">
    <div class="t-learn-inner">
        <div id="l_selfStudy_box" class="learn-con " style="margin: 0 auto;">
            <div class="learn-del-box">
                <#if books?has_content>
                    <div class="t-learn-mater">
                    <#list books as b>
                        <dl>
                            <dt>
                                <span class="w-build-image w-build-image-${b.color!''}">
                                    <strong class="wb-title">${b.viewContent!''}</strong>
                                    <#if b.latestVersion!false>
                                        <span class="wb-new"></span>
                                    </#if>
                                </span>
                            </dt>
                            <dd>
                                <a href="javascript:void (0)" data-subject="${b.bookSubject!''}" class="change more_books_but">换教材</a>
                                <p>${b.bookName!''}</p>
                                <div class="learn-btn w-ag-center">
                                    <a data-l_index="${b_index}" data-subject="${b.bookSubject!''}" data-book_id="${b.bookId!''}" class="w-btn w-btn-green l_selfStudy_but" href="javascript:void(0);">开始自学</a>
                                </div>
                                <#if b.bookSubject == 'ENGLISH'>
                                    <span class="w-ag-center ln-title ln-title-1">英语教材</span>
                                <#elseif b.bookSubject == 'MATH'>
                                    <span class="w-ag-center ln-title ln-title-2">数学教材</span>
                                <#elseif b.bookSubject == 'CHINESE'>
                                    <span class="w-ag-center ln-title ln-title-3">语文教材</span>
                                </#if>
                            </dd>
                        </dl>
                    </#list>
                    </div>
                </#if>
                <div class="w-clear"></div>
            </div>

            <div id="unitsListBox" class="t-learn-unit" style="display: none;"><#--unitsList--></div>

            <div id="lessonsListBox" class="t-learn-unit-list" style="display: none;"><#--lessonsList--></div>
        </div>
        <div id="l_homework_history_box" style="display: none"><#--作业历史专区--></div>
    </div>
</div>

<script type="text/html" id="t:unitsList">
    <div class="l-skin-box">
        <%if(unitsData.length > 0){%>
            <ul id="l_units_list_box">
                <%for(var i = 0;i < unitsData.length; i++){%>
                <li title="<%=unitsData[i].cname%>" data-book_id="<%=unitsData[i].bookId%>" data-unit_id="<%=unitsData[i].id%>">
                    <a href="javascript:void (0)"><%=unitsData[i].cname%></a>
                </li>
                <%}%>
            </ul>
        <%}else{%>
            <div style="padding: 50px 0; text-align: center;">没有该课本的单元信息</div>
        <%}%>
    </div>
    <span class="learn-arrow learn-arrow-<%=index%>"></span>
</script>

<#--英语lesson模板-->
<script type="text/html" id="t:englishLessonsList">
    <%if(lessonsData.length > 0){%>
        <div class="l-skin-box">
            <h2 class="title"><%=unit.cname%></h2>
            <%for(var i = 0; i < lessonsData.length; i++){%>
                <%if(lessonsData[i].practiceTypes.length > 0){%>
                    <div class="lu-box">
                        <span class="learn-bar" title="<%=lessonsData[i].cname%>"><%=lessonsData[i].cname%></span>
                        <div class="lb-list">
                            <div class="w-game-list">
                                <ul>
                                    <%for(var j = 0; j < lessonsData[i].practiceTypes.length; j++){%>
                                        <li title="<%=lessonsData[i].practiceTypes[j].value%>" class=" selfStartBut"  data-url="/flash/loader/selfstudy-<%=lessonsData[i].practiceTypes[j].key%>-${(currentUser.id)!}-<%=bookId%>-<%=unit.id%>-<%=lessonsData[i].id%>.vpage">
                                            <span style="background-image: url(<@app.link href="public/skin/common/images/practice/small/<%=lessonsData[i].practiceTypes[j].key%>.png"/>)" class="w-game-icon">
                                                <%if(lessonsData[i].practiceTypes[j].needRecord){%>
                                                    <i class="w-game-mack" data-title="mack"></i>
                                                <%}%>
                                            </span>
                                            <h5><%=lessonsData[i].practiceTypes[j].value%></h5>
                                        </li>
                                    <%}%>
                                </ul>
                                <div class="w-clear"></div>
                            </div>
                        </div>
                    </div>
                <%}%>
            <%}%>
        </div>
    <%}else{%>
        <div class="l-skin-box"> <div class="lu-box" style="padding: 50px 0; text-align: center;">本单元没有知识点内容，您可以选择其他单元自学。</div> </div>
    <%}%>
</script>

<#--数学lesson模板-->
<script type="text/html" id="t:mathLessonsList">
    <%if(lessonsData.length > 0){%>
    <div class="l-skin-box">
        <%for(var i = 0; i < lessonsData.length; i++){%>
            <h2 class="title"><%=lessonsData[i].cname%></h2>
            <%for(var j = 0; j < lessonsData[i].mathPointMapperList.length; j++){%>
                <div class="lu-box">
                    <span class="learn-bar" title="<%=lessonsData[i].mathPointMapperList[j].cname%>"><%=lessonsData[i].mathPointMapperList[j].cname%></span>
                    <div class="lb-list">
                        <div class="w-game-list">
                            <ul style="overflow-y:inherit;">
                                <%for(var k = 0; k < lessonsData[i].mathPointMapperList[j].practiceTypeMapperList.length; k++){%>
                                    <%if(lessonsData[i].mathPointMapperList[j].dataTypeCountList != null && lessonsData[i].mathPointMapperList[j].dataTypeCountList.length > 0){%>
                                        <%for(var ii = 0; ii < lessonsData[i].mathPointMapperList[j].dataTypeCountList.length; ii++){%>
                                            <%for(var jj in lessonsData[i].mathPointMapperList[j].dataTypeCountList[ii]){%>
                                                <li title="<%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].name%>" class="disabled selfStartBut" style="cursor: pointer;" data-url="/flash/loader/mathselfstudy-<%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].id%>-${(currentUser.id)!}-<%=bookId%>-<%=unit.id%>-<%=lessonsData[i].id%>-<%=lessonsData[i].mathPointMapperList[j].id%>.vpage?dataType=<%=jj%>" data-type="<%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].id%>">
                                                    <span style="background-image: url(<@app.link href="public/skin/common/images/mathpractice/small/<%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].id%>.png"/>)" class="w-game-icon">
                                                    <%if(jj == 91){%>
                                                            <span class="w-game-yu"></span>
                                                        <%}else if(jj == 92){%>
                                                            <span class="w-game-gu"></span>
                                                        <%}else if(jj == 93){%>
                                                            <span class="w-game-tuo"></span>
                                                        <%}%>
                                                    </span>
                                                    <h5>
                                                        <%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].name%>
                                                    </h5>
                                                </li>
                                            <%}%>
                                        <%}%>
                                    <%}else{%>
                                        <li title="<%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].name%>" class="disabled selfStartBut" style="cursor: pointer;" data-url="/flash/loader/mathselfstudy-<%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].id%>-${(currentUser.id)!}-<%=bookId%>-<%=unit.id%>-<%=lessonsData[i].id%>-<%=lessonsData[i].mathPointMapperList[j].id%>.vpage" data-type="<%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].id%>">
                                            <span style="background-image: url(<@app.link href="public/skin/common/images/mathpractice/small/<%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].id%>.png"/>)" class="w-game-icon"></span>
                                            <h5><%=lessonsData[i].mathPointMapperList[j].practiceTypeMapperList[k].name%></h5>
                                        </li>
                                    <%}%>
                                <%}%>
                            </ul>
                            <div class="w-clear"></div>
                        </div>
                    </div>
                </div>
            <%}%>
        <%}%>
    </div>
    <%}else{%>
        <div class="l-skin-box"> <div class="lu-box" style="padding: 50px 0; text-align: center;">本单元没有知识点内容，您可以选择其他单元自学。</div> </div>
    <%}%>
</script>

<#--语文lesson模板-->
<script type="text/html" id="t:chineseLessonsList">
    <%if(lessonsData.length > 0){%>
    <div class="l-skin-box">
        <h2 class="title"><%=unit.cname%></h2>
        <%for(var i = 0; i < lessonsData.length; i++){%>
            <%if(lessonsData[i].practiceTypes.length > 0){%>
                <div class="lu-box">
                    <span class="learn-bar" title="<%=lessonsData[i].cname%>"><%=lessonsData[i].cname%></span>
                    <div class="lb-list">
                        <div class="w-game-list">
                            <ul style="overflow-y:inherit;">
                                <%for(var j = 0; j < lessonsData[i].practiceTypes.length; j++){%>
                                    <#-- lessonsData[i].practiceTypes[j].key == 113 开心跟读正式环境都不显示  -->
                                    <li <%if(lessonsData[i].practiceTypes[j].key == 113 && !(${ftlmacro.devTestStagingSwitch?string}) ){%> style="display:none;" <%}%> title="<%=lessonsData[i].practiceTypes[j].value%>" class="disabled selfStartBut"  data-url="/flash/loader/chineseselfstudy-<%=lessonsData[i].practiceTypes[j].key%>-${(currentUser.id)!}-<%=bookId%>-<%=unit.id%>-<%=lessonsData[i].id%>.vpage">
                                        <span style="background-image: url(<@app.link href="public/skin/common/images/practice/small/<%=lessonsData[i].practiceTypes[j].key%>.png"/>)" class="w-game-icon">
                                            <%if(lessonsData[i].practiceTypes[j].needRecord){%>
                                                <i class="w-game-mack" data-title="mack"></i>
                                            <%}%>
                                        </span>
                                        <h5><%=lessonsData[i].practiceTypes[j].value%></h5>
                                    </li>
                                <%}%>
                            </ul>
                            <div class="w-clear"></div>
                        </div>
                    </div>
                </div>
            <%}%>
        <%}%>
    </div>
    <%}else{%>
        <div class="l-skin-box"> <div class="lu-box" style="padding: 50px 0; text-align: center;">本单元没有知识点内容，您可以选择其他单元自学。</div> </div>
    <%}%>
</script>

<@sugar.capsule js=["fastLiveFilter"] />
<script type="text/javascript">

    //关闭练习浏览
    function nextHomeWork() {
        $.prompt.close();
    }

    var currentSubject;

    $(function(){
        var historyMeunBox = $("#l_history_meun_box");
        var historyBox = $('#l_homework_history_box');
        var selfStudyBox = $("#l_selfStudy_box");

        //作业历史菜单切换
        historyMeunBox.on('click', 'li', function(){
            var $this = $(this);
            var subjectName = $this.data('subject_type');

            $this.addClass('current').siblings().removeClass('current');
            selfStudyBox.hide();
            historyBox.html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>').show().load('/student/learning/history/'+subjectName + '.vpage');
        });

        //点击作业历史
        $("#l_left_meun_box div a.l_left_meun_but").on('click', function(){
            var $this = $(this);
            var meunType = $this.data('m_type');

            if(meunType != "writing"){
                $this.addClass('active');
                $this.parent().siblings().find("a.l_left_meun_but").removeClass('active');
            }
            switch (meunType){
                case 'history':
                    historyMeunBox.show();
                    $17.tongji('左侧学习中心菜单-作业历史');
                    historyMeunBox.find('li').removeClass('current').eq(0).click();
                    break;
                case 'selfStudy':
                    historyMeunBox.hide();
                    selfStudyBox.show();
                    historyBox.hide();
                    $17.tongji('左侧学习中心菜单-自学中心');
                    break;
            }
        });

        $('#noHomeworkHistory').on("click", function(){
            $("#l_left_meun_box div a.l_left_meun_but[data-m_type='history']").click();
            return false;
        });

        //repair
        <#if history?has_content>
            historyMeunBox.show();
            $("#l_history_meun_box li[data-subject_type=${history?lower_case}]").click();
        </#if>

        //开始自学
        $(".l_selfStudy_but").on('click', function(){
            var $this = $(this);
            var index = $this.data('l_index');
            var bookId = $this.data('book_id');
            var subjectType = $this.data('subject');
            currentSubject = subjectType;
            $('.learn-arrow').removeClass("learn-arrow-0 learn-arrow-1 learn-arrow-2").addClass('learn-arrow-'+index);
            $(".l_selfStudy_but").show();
            $("#lessonsListBox").hide();
            $this.hide();

            // 加载units列表
            $("#unitsListBox").html('<div style="padding: 20px; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>').show();
            var parem = {bookId : bookId, subjectType : subjectType};
            $.get('/student/book/units.vpage?'+$.param(parem), function(data){
                if(data.success){
                    $("#unitsListBox").html(template("t:unitsList", {unitsData : data.units, index : index})).show();
                    $("#l_units_list_box li:first").trigger('click');
                }else{
                    $17.alert('数据加载失败，请重试');
                }
            });

            $17.tongji('自学中心-'+subjectType+'学科开始自学按钮');
        });

        //根据unit 加载lesson
        var lessonsCache = {};
        $(document).on( "click", "#l_units_list_box li", function() {
            var $this = $(this);
            $this.find('a').addClass('current');
            $this.siblings().find('a').removeClass('current');

            var bookId = $this.data('book_id');
            var unitId = $this.data('unit_id');
            var uniqueId = currentSubject + bookId + unitId;
            var lessonsListBox = $("#lessonsListBox");
            var parem = {bookId : bookId, unitId : unitId}, url, templates;

            switch (currentSubject){
                case "ENGLISH" :
                    url  = '/student/book/lessons.vpage?'+ $.param(parem);
                    templates = 'englishLessonsList';
                    break;

                case 'MATH' :
                    url  = '/student/book/mathlessons.vpage?'+ $.param(parem);
                    templates = 'mathLessonsList';
                    break;

                case 'CHINESE' :
                    url  = '/student/book/chineselessons.vpage?'+ $.param(parem);
                    templates = 'chineseLessonsList';
                    break;
            }

            //根据缓存内容加载lessons
            if(!$17.isBlank(lessonsCache[uniqueId])){
                lessonsListBox.html(template("t:"+templates, {lessonsData : lessonsCache[uniqueId][0].lessons, unit : lessonsCache[uniqueId][0].unit, bookId : lessonsCache[uniqueId][0].bookId})).show();
                return false;
            }

            lessonsListBox.html('<div class="t-learn-unit-list"> <div class="l-skin-box" style="padding: 20px; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div></div>').show();
            $.get(url, function(data){
                if(data.success){
                    lessonsListBox.html(template("t:"+templates, {lessonsData : data.lessons, unit : data.unit, bookId : data.bookId})).show();
                    lessonsCache[uniqueId] = [].concat(data);
                }else{
                    $17.alert('数据加载失败，请重试');
                }

            }).fail(function(){
                lessonsListBox.html('<div class="t-learn-unit-list"> <div class="l-skin-box" style="padding: 20px 20px 20px !important;">数据加载失败，请重试</div></div>').show();
            });
        });

        //自 学
        $(document).on('click', '.selfStartBut',function(){
            var data = '<iframe class="vox17zuoyeIframe" src="' + $(this).attr("data-url") + '" width="780" height="470" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"></iframe>';
            $.prompt(data, {
                title: "<strong>自 学</strong>",
                buttons : {},
                position : { width: 760 },
                close: function(){
                    $('iframe').each(function() {
                        var win = this.contentWindow || this;
                        if(win.destroyHomeworkJavascriptObject)
                            win.destroyHomeworkJavascriptObject();
                    })
                }
            });
            return false;

        });

        //换教材
        $(".more_books_but").on('click', function(){
            var subjectType = $(this).data('subject');
            var subjectName = '', titleClass = '';
            if(subjectType == 'ENGLISH'){
                subjectName = '英语';
                titleClass = 'jqititle-red';
            }else if(subjectType == 'MATH'){
                subjectName = '数学';
                titleClass = 'jqititle-blue';
            }else if(subjectType == 'CHINESE'){
                subjectName = '语文';
                titleClass = 'jqititle-green';
            }
            $17.tongji('自学中心-'+subjectName+'学科更换教材按钮');

            $.get('/student/learning/books.vpage?subjectType='+subjectType, function(data){
                $.prompt(data,{
                    title : '更换'+subjectName+'自学教材',
                    position : {width: '850'},
                    buttons : {},
                    classes : {
                        title : titleClass
                    }
                });
            });
        });

        //当是从更换教材返回时，自动展开自学
        <#if currentBookId??>
            $(".l_selfStudy_but[data-subject=${subject!''}]").trigger('click');
        <#else>
            $(".l_selfStudy_but:first").click();
        </#if>
    });
</script>
<#--右下角弹窗模板-->
<script id="t:右下角新消息" type="text/html">
    <ul>
        <%for(var i = 0; i < msgList.length; i++){%>
        <li><%==msgList[i]%></li>
        <%}%>
    </ul>
</script>
<@sugar.site_traffic_analyzer_end />
</body>
</html>