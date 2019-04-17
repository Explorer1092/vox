<#import "../../../nuwa/teachershellv3.ftl" as temp />
<@temp.page show="resource-reading" showNav="hide">
<@sugar.capsule js=["template", "reading", "jquery.flashswf"] css=["common.so", "new_teacher.quiz"] />
<style type="text/css">
    .w-select ul {
        width: 255px;/*重设树的宽度*/
    }
</style>
<div class="section">
    <script id="t:第一步" type="text/html">
        <div class="stepOne" style="height: 375px;">
            <h2 class="text_black">1、设置阅读基本属性</h2>
            <dl class="horizontal_vox quiz_ys_border">
                <dt style="width: 20%"><span class="text_red">[必填] </span>标题：</dt>
                <dd style="padding-bottom: 20px;">
                    <input id="step1_title" placeholder="请输入阅读标题" class="int_vox check_mistake">
                </dd>
                <dt style="width: 20%"><span class="text_red">[必填] </span>话题：</dt>
                <dd>
                    <div id="selectTree" class="w-select">
                        <div class="current check_mistake pointsSelect" style="width:241px;">
                            <span class="content">请选择</span><span class="w-icon w-icon-arrow"></span>
                        </div>
                        <div id="topicTree"></div>
                    </div>
                </dd>
                <dt style="width: 20%"><span class="text_red">[必填] </span>体裁：</dt>
                <dd>
                    <div id="step1_styles" class="int_vox">
                        <a href="javascript:void(0);" class="area"><b class="title"></b><i class="arrow"></i></a>
                        <ul style="z-index: 999;display:none;">
                            <li data-value="" class="active">请选择</li>
                            <#list readingWritingStyle as item>
                                <li data-value="${item}">${item.comment}</li>
                            </#list>
                        </ul>
                    </div>
                </dd>
                <dt style="width: 20%"><span class="text_red">[必填] </span>适用年级：</dt>
                <dd style="margin-left: 20%">
                    <div id="step1_levels" class="int_vox" style="width:243px!important;">
                        <a href="javascript:void(0);" class="area"><b class="title"></b><i class="arrow"></i></a>
                        <ul style="display:none;">
                            <li data-value="" class="active">请选择</li>
                            <li data-value="1">1-3年级(一起);3-4年级(三起)</li>
                            <li data-value="2">4-6年级(一起);5-6年级(三起)</li>
                            <li data-value="3">6-7年级</li>
                            <li data-value="4">8-9年级</li>
                        </ul>
                    </div>
                </dd>
            </dl>

        </div>
    </script>
    <script id="t:第二步" type="text/html">
        <div class="stepTwo">
            <h2>2、添加阅读封皮</h2>
            <dl class="horizontal_vox quiz_ys_border" style="margin-bottom: 20px;">
                <dt>模板：</dt>
                <dd id="step2_topside" style="overflow: hidden;">
                    <% if(colorId == 1){ %>
                    <p>
                        <span data-coverindex="1" class="ic <% if(coverIndex == 1){ %>active active_type1<% } %>">
                            <img src="<@app.link href="public/skin/teacher/images/newquiz/book_num1.png"/>" width="144px" height="192">
                            <strong></strong>
                        </span>
                        <strong>圆图</strong>
                    </p>
                    <p>
                        <span data-coverindex="2" class="ic <% if(coverIndex == 2){ %>active active_type1<% } %>">
                            <img src="<@app.link href="public/skin/teacher/images/newquiz/book_num2.png"/>"  width="144px" height="192">
                            <strong></strong>
                        </span>
                        <strong>方图</strong>
                    </p>
                    <p style="display: none;">
                        <span data-coverindex="3" class="ic <% if(coverIndex == 3){ %>active active_type1<% } %>">
                            <img src="<@app.link href="public/skin/teacher/images/newquiz/book_num3.png"/>" width="144px" height="192">
                            <strong></strong>
                        </span>
                        <strong>无图</strong>
                    </p>
                    <% } %>
                    <% if(colorId == 2){ %>
                    <p>
                        <span data-coverindex="1" class="ic <% if(coverIndex == 1){ %>active active_type2<% } %>">
                            <img src="<@app.link href="public/skin/teacher/images/newquiz/book_bluenum02.png"/>" width="144px" height="192">
                            <strong></strong>
                        </span>
                        <strong>圆图</strong>
                    </p>
                    <p>
                        <span data-coverindex="2" class="ic <% if(coverIndex == 2){ %>active active_type2<% } %>">
                            <img src="<@app.link href="public/skin/teacher/images/newquiz/book_bluenum03.png"/>"  width="144px" height="192">
                            <strong></strong>
                        </span>
                        <strong>方图</strong>
                    </p>
                    <p style="display: none;">
                        <span data-coverindex="3" class="ic <% if(coverIndex == 3){ %>active active_type2<% } %>">
                            <img src="<@app.link href="public/skin/teacher/images/newquiz/book_bluenum01.png"/>" width="144px" height="192">
                            <strong></strong>
                        </span>
                        <strong>无图</strong>
                    </p>
                    <% } %>
                    <% if(colorId == 3){ %>
                    <p>
                        <span data-coverindex="1" class="ic <% if(coverIndex == 1){ %>active active_type3<% } %>">
                            <img src="<@app.link href="public/skin/teacher/images/newquiz/book_greennum02.png"/>" width="144px" height="192">
                            <strong></strong>
                        </span>
                        <strong>圆图</strong>
                    </p>
                    <p>
                        <span data-coverindex="2" class="ic <% if(coverIndex == 2){ %>active active_type3<% } %>">
                            <img src="<@app.link href="public/skin/teacher/images/newquiz/book_greennum03.png"/>"  width="144px" height="192">
                            <strong></strong>
                        </span>
                        <strong>方图</strong>
                    </p>
                    <p style="display: none;">
                        <span data-coverindex="3" class="ic <% if(coverIndex == 3){ %>active active_type3<% } %>">
                            <img src="<@app.link href="public/skin/teacher/images/newquiz/book_greennum01.png"/>" width="144px" height="192">
                            <strong></strong>
                        </span>
                        <strong>无图</strong>
                    </p>
                    <% } %>
                </dd>
                <dt>色调：</dt>
                <dd id="step2_colors" style="overflow: hidden;">
                    <s data-colorid="1" class="yellow <% if(colorId == '1'){ %>active<% } %>"></s>
                    <s data-colorid="2" class="blue <% if(colorId == '2'){ %>active<% } %>"></s>
                    <s data-colorid="3" class="green <% if(colorId == '3'){ %>active<% } %>"></s>
                </dd>

                <dt <% if(cover == ""){ %>style="display: none;"<% } %>>封皮：</dt>
                <dd style="margin-bottom: 20px; <% if(cover == ""){ %>display: none;<% } %>">
                <p><img width="144px" height="192" id="upload_cover" src="${cdn_url!}<%= cover %>"/></p>

                <p><img width="100px" height="124" id="upload_unfinished" src="${cdn_url!}<%= unfinished %>"/></p>

                <p><img width="80px" height="100" id="upload_least" src="${cdn_url!}<%= least %>"/></p>
                </dd>

                <dd id="upload_flash" style="display: none;" data-userid="${currentUser.id}" data-uploadimageurl="${uploadreadingcover!}" class="main" style="<% if(coverIndex == 3){ %>display: none;<% } %>"></dd>
                <dd style="margin-left: 330px; <% if(coverIndex == 3){ %>display: none;<% } %>">
                    <a id="upload_cover_btn" href="javascript:void(0);" class="btn_mark btn_mark_well"><strong><% if(cover == ""){ %>上传封皮<% }else{ %>更新封皮<% } %></strong></a>
                </dd>
            </dl>
        </div>
    </script>
    <script id="t:第三步" type="text/html">
        <div class="stepThree">
            <h2>3、添加阅读内容</h2>
            <div class="quizHeader">
                <div class="show_block" id="manualUFO">
                    <div class="inner">
                        <div class="select_block" id="Abacus">
                            <h3>第 <%= pageNum %> 页</h3>
                        </div>
                    </div>
                </div>
            </div>
            <div class="main">
                <dl class="horizontal_vox" style="margin-bottom: 20px;">
                    <dt>模板：</dt>
                    <dd>
                        <p class="con_p">
                            <span data-layout="ptpt" class="model ic <% if(pageLayout == 'ptpt'){ %>model_active<% } %>">
                                <img src="<@app.link href="public/skin/teacher/images/newquiz/book_num4.png"/>" width="157px" height="111">
                                <strong></strong>
                            </span>
                            <strong>图文混排</strong>
                        </p>
                        <p class="con_p">
                            <span data-layout="pt" class="model ic <% if(pageLayout == 'pt'){ %>model_active<% } %>">
                                <img src="<@app.link href="public/skin/teacher/images/newquiz/book_num5.png"/>" width="157px" height="111">
                                <strong></strong>
                            </span>
                            <strong>左图右文</strong>
                        </p>
                        <p class="con_p">
                            <span data-layout="tp" class="model ic <% if(pageLayout == 'tp'){ %>model_active<% } %>">
                                <img src="<@app.link href="public/skin/teacher/images/newquiz/book_num6.png"/>" width="157px" height="111">
                                <strong></strong>
                            </span>
                            <strong>左文右图</strong>
                        </p>
                        <p class="con_p">
                            <span data-layout="tt" class="model ic <% if(pageLayout == 'tt'){ %>model_active<% } %>">
                                <img src="<@app.link href="public/skin/teacher/images/newquiz/book_num7.png"/>" width="157px" height="111">
                                <strong></strong>
                            </span>
                            <strong>全文字-分页</strong>
                        </p>
                        <p class="con_p">
                            <span data-layout="wp" class="model ic <% if(pageLayout == 'wp'){ %>model_active<% } %>">
                                <img src="<@app.link href="public/skin/teacher/images/newquiz/book_num8.png"/>" width="157px" height="111">
                                <strong></strong>
                            </span>
                            <strong>上图下文</strong>
                        </p>
                    </dd>
                </dl>
                <% if(pageLayout == 'ptpt'){ %>
                    <#include "chip_ptpt.ftl" />
                <% } %>
                <% if(pageLayout == 'pt'){ %>
                    <#include "chip_pt.ftl" />
                <% } %>
                <% if(pageLayout == 'tp'){ %>
                    <#include "chip_tp.ftl" />
                <% } %>
                <% if(pageLayout == 'tt'){ %>
                    <#include "chip_tt.ftl" />
                <% } %>
                <% if(pageLayout == 'wp'){ %>
                    <#include "chip_wp.ftl" />
                <% } %>
                <div class="foot_ib">
                    <a id="step3_delete_page" href="javascript:void(0);" class="btn_mark btn_mark_well"><strong>删除本页</strong></a>
                    <a id="step3_add_page" href="javascript:void(0);" class="btn_mark btn_mark_well btn_mark_primary"><strong>新建一页</strong></a>
                </div>
            </div>
        </div>
    </script>
    <script id="t:第四步" type="text/html">
        <div class="stepFour">
            <h2>4、 添加题目 (请选择正确答案选项)</h2>
            <% if(questions.length > 0){ %>
                <% for(var i = 0; i < questions.length; i++){ %>
                    <% if(questions[i].type == 1){ %>
                        <div data-rank="<%= questions[i].rank %>" class="select_box">
                            <dl class="horizontal_vox">
                                <dt><%= questions[i].rank %>、题干：</dt>
                                <dd style="padding-bottom: 20px; margin-left: 17%;">
                                    <textarea data-index="<%= i %>" class="int_vox selectionContent check_mistake" style="width: 416px!important; height: 70px; overflow-x: hidden; overflow-y: auto;"><%= questions[i].content %></textarea>
                                </dd>
                                <dd class="text_gray_6" style=" margin-top: -15px;">提示：请用 [&nbsp&nbsp&nbsp] 将正确答案括住</dd>
                            </dl>
                            <div class="foot_ib">
                                <a href="javascript:void(0);" data-rank="<%= questions[i].rank %>" class="quizBtn quizBtn_blue quizBtn_well step4_delete_question"><strong>删除本题</strong></a>
                            </div>
                            <div class="quiz_ugc_spot"></div>
                        </div>
                    <% } %>
                    <% if(questions[i].type == 2){ %>
                        <div data-rank="<%= questions[i].rank %>" class="select_box">
                            <dl class="horizontal_vox">
                                <dt><%= questions[i].rank %>、题干：</dt>
                                <dd style="padding-bottom: 20px;">
                                    <input placeholder="请输入提干" data-index="<%= i %>" class="int_vox selectionContent check_mistake" style="width: 280px!important;" value="<%= questions[i].content %>">
                                </dd>
                            </dl>
                            <dl class="horizontal_vox selection">
                                <dt>选项：</dt>
                                <% for(var j = 0; j < questions[i].answerOptions.length; j++){ %>
                                    <dd style="padding-bottom: 20px; margin-left: 17%;">
                                        <span data-value="<%= j %>" class="radios <% if(questions[i].rightAnswer == j){ %>radios_active<% } %>"></span><%=["A", "B", "C", "D"][j]%>.
                                        <input placeholder="选项内容" data-index="<%= i %>" data-value="<%= j %>" class="int_vox selectionOptionContent check_mistake" style="margin-right: 20px;" value="<%=questions[i].answerOptions[j]%>">
                                        <i data-index="<%= i %>" data-value="<%= j %>" class='w-btn w-btn-mini plus_button' style="width: 27px; cursor: pointer; <% if(questions[i].answerOptions.length == 4){ %>display: none;<% } %>">+</i>
                                        <i data-index="<%= i %>" data-value="<%= j %>" class="quiz_ugc_right_ys icposition minus_button" style="right: 270px; top: 11px; cursor: pointer; <% if(questions[i].answerOptions.length == 2){ %>display: none;<% } %>"></i>
                                    </dd>
                                <% } %>
                            </dl>
                            <div class="foot_ib">
                                <a href="javascript:void(0);" data-rank="<%= questions[i].rank %>" class="quizBtn quizBtn_blue quizBtn_well step4_delete_question"><strong>删除本题</strong></a>
                            </div>
                            <div class="quiz_ugc_spot"></div>
                        </div>
                    <% } %>
                    <% if(questions[i].type == 3){ %>
                        <div data-rank="<%= questions[i].rank %>" class="select_box">
                            <dl class="horizontal_vox">
                                <dt><%= questions[i].rank %>、题干：</dt>
                                <dd style="padding-bottom: 20px;">
                                    <input placeholder="请输入提干" data-index="<%= i %>" class="int_vox selectionContent check_mistake" value="<%= questions[i].content %>">
                                </dd>
                            </dl>
                            <dl class="horizontal_vox selection">
                                <dt>选项：</dt>
                                <dd>
                                    <span data-value="0" class="radios <% if(questions[i].rightAnswer == 0){ %>radios_active<% } %>"></span> T
                                </dd>
                                <dd style="padding-bottom: 20px; margin-left: 17%;">
                                    <span data-value="1" class="radios <% if(questions[i].rightAnswer == 1){ %>radios_active<% } %>"></span> F
                                </dd>
                            </dl>
                            <div class="foot_ib">
                                <a href="javascript:void(0);" data-rank="<%= questions[i].rank %>" class="quizBtn quizBtn_blue quizBtn_well step4_delete_question"><strong>删除本题</strong></a>
                            </div>
                            <div class="quiz_ugc_spot"></div>
                        </div>
                    <% } %>
                <% } %>
            <% }else{ %>
                <div class="text_center text_big text_gray_6" style="padding: 50px 0 70px 0;">您还未添加题目，点击下面的功能键就可以添加哦～</div>
            <% } %>
            <ul id="step4_quiz_buttons" class="foot_info_box">
                <li id="step4_add_danxuan"><a class="text_gray_6" href="javascript:void(0);">添加单选题<i class="icon_vox icon_vox_344"></i></a></li>
                <li id="step4_add_panduan"><a class="text_gray_6" href="javascript:void(0);">添加判断题<i class="icon_vox icon_vox_344"></i></a></li>
                <li id="step4_add_tiankong"><a class="text_gray_6" href="javascript:void(0);">添加填空题<i class="icon_vox icon_vox_344"></i></a></li>
            </ul>
        </div>
    </script>
    <script id="t:popMenu" type="text/html">
        <div class="inner">
            <% if(showView == "true"){ %>
            <a id="showView" class="quizBtn quizBtn_well" href="javascript:void(0);">预览</a>
            <% } %>
            <% if(showViewAll == "true"){ %>
            <a id="showViewAll" class="quizBtn quizBtn_well" href="javascript:void(0);">预览全部</a>
            <% } %>
            <a id="savaReading" class="quizBtn quizBtn_well" href="javascript:void(0);">保存当前进度</a>
            <a id="<%= popMenuId %>" class="quizBtn quizBtn_blue quizBtn_well" href="javascript:void(0);"><%= btnText %></a>
        </div>
    </script>
    <script id="t:左导航" type="text/html">
        <div class="rightPopUpSelect">
            <div class="select_info_box rightPopUpSelect_clear">
                <h1>功能导航</h1>
                <ul class="level_one">
                    <li class="title <% if(step == 1){ %>quiz_active<% } %>"><a id="leftMenuToStep1" href="javascript:void(0);" class="level">1.设置阅读基本属性 </a></li>
                    <li class="title <% if(step == 2){ %>quiz_active<% } %>"><a id="leftMenuToStep2" href="javascript:void(0);" class="level">2.添加阅读封皮</a></li>
                    <li class="title active <% if(step == 3){ %>quiz_active<% } %>">
                        <p><a id="leftMenuToStep3" href="javascript:void(0);" class="level">3.添加阅读内容 </a></p>
                        <ul>
                            <% for(var i = 0; i < pages.length; i++){ %>
                                <li class="title_info"><a class="leftMenuToPages <% if(step == 3 && focus == i){ %>level<% } %>" data-pageindex="<%= i %>" href="javascript:void(0);">第 <%= i + 1 %> 页</a></li>
                            <% } %>
                        </ul>
                    </li>
                    <li class="title <% if(step == 4){ %>quiz_active<% } %>"><a id="leftMenuToStep4" href="javascript:void(0);" class="level">4.添加题目</a></li>
                </ul>
            </div>
        </div>
    </script>
</div>
<script id="t:预览" type="text/html">
    <div id="showViewContent"></div>
</script>
<div id="steps1" class="quiz_ugc_box"></div>
<div id="steps" class="quiz_ugc_box"></div>
<div class="quiz_footPopUpMenu"></div>
<div class="quiz_footPopUpMenu_contrast"></div>
<div id="points" class="quiz_ys_border" style="display: none; width: 500px; height: 400px; position: relative; top: -450px; left: 33%; background-color: white; z-index: 9999; overflow-y: scroll;"></div>
<div id="UGC-DATA" style="display: none;" data-tts-url="${tts_url!}"></div>
<script type="text/javascript">
    var ReadingApp = null;

    $(function(){
        ReadingApp = {
            DataBase : {},
            $el : $("#steps"),
            Point : ${point!}[0],
            imgDomain : '<@app.link_shared href='' />',
            FlashURL   : "${readingFlashUrl!'public/skin/teacher/flash/ReadingUGC.swf'}",
            Constructor: {
                readingDraftTemp            : ${readingDraftTemp},
                readingDraftHalfPageTemp    : ${readingDraftHalfPageTemp},
                readingDraftPageTemp        : ${readingDraftPageTemp},
                readingDraftSentenceTemp    : ${readingDraftSentenceTemp},
                readingDraftQuestionTemp    : ${readingDraftQuestionTemp}
            },
            step1: {
                tempInfo    : {
                    tempId      : "t:第一步",
                    showView    : "false",
                    showViewAll : "false",
                    btnText     : "下一步 添加封皮",
                    popMenuId   : "step1_next"
                },
                evenConfig  : {
                    "#step1_title -> blur"             : function(){ ReadingApp.DataBase.ename = $(this).val(); },
                    "#step1_next -> click"             : showStep2,
                    "#step1_styles ul -> mouseleave"   : setStyleValue,
                    "#step1_levels ul -> mouseleave"   : setLevelValue,
                    "#savaReading -> click"            : readingSave
                }
            },
            step2: {
                tempInfo : {
                    tempId      : "t:第二步",
                    showView    : "true",
                    showViewAll : "false",
                    btnText     : "下一步 添加阅读内容",
                    popMenuId   : "step2_next"
                },
                evenConfig  : {
                    "#step2_topside span[data-coverindex] -> click" : radioCover,
                    "#step2_colors s[data-colorid] -> click"        : radioColor,
                    "#upload_cover_btn -> click"                    : upload_cover_btn,
                    "#step2_next -> click"                          : showStep3,
                    "#savaReading -> click"                         : readingSave,
                    "#showView -> click"                            : showView2
                }
            },
            step3: {
                tempInfo : {
                    tempId      : "t:第三步",
                    showView    : "true",
                    showViewAll : "false",
                    btnText     : "下一步 添加习题",
                    popMenuId   : "step3_next"
                },
                evenConfig  : {
                    "span.model.ic -> click"                : step3_change_template,
                    "#step3_delete_page -> click"           : step3_delete_page,
                    "#step3_add_page -> click"              : step3_add_page,
                    "#front_done_button -> click"           : step3_parts_front_done,
                    "#back_done_button -> click"            : step3_parts_back_done,
                    "#parts_done_button -> click"           : step3_parts_done,
                    "a.addSentencePlusBtn -> click"         : step3_addSentencePlusBtn,
                    "a.addFrontSentenceBtn -> click"        : step3_addFrontSentenceBtn,
                    "a.editFrontSentenceBtn -> click"       : step3_editFrontSentenceBtn,
                    "a.deleteFrontSentenceBtn -> click"     : step3_deleteFrontSentenceBtn,
                    "a.addBackSentenceBtn -> click"         : step3_addBackSentenceBtn,
                    "a.editBackSentenceBtn -> click"        : step3_editBackSentenceBtn,
                    "a.deleteBackSentenceBtn -> click"      : step3_deleteBackSentenceBtn,
                    "a.addSentenceBtn -> click"             : step3_addSentenceBtn,
                    "a.editSentenceBtn -> click"            : step3_editSentenceBtn,
                    "a.deleteSentenceBtn -> click"           : step3_deleteSentenceBtn,
                    "i.keyMapFrontPlusBtn -> click"         : step3_keyMapFrontPlusBtn,
                    "i.keyMapBackPlusBtn -> click"          : step3_keyMapBackPlusBtn,
                    "i.keyMapPlusBtn -> click"              : step3_keyMapPlusBtn,
                    "input.keyMap_enbox -> blur"            : step3_keyMap_box_blur,
                    "input.keyMap_cnbox -> blur"            : step3_keyMap_box_blur,
                    "span.keyMapFrontMinus -> click"        : step3_keyMapFrontMinus,
                    "span.keyMapBackMinus -> click"         : step3_keyMapBackMinus,
                    "span.keyMapMinus -> click"             : step3_keyMapMinus,
                    "#step3_next -> click"                  : showStep4,
                    "#savaReading -> click"                 : readingSave,
                    "#showView -> click"                    : showView3
                }
            },
            step4: {
                tempInfo : {
                    tempId      : "t:第四步",
                    showView    : "true",
                    showViewAll : "true",
                    btnText     : "完成发布",
                    popMenuId   : "readingSubmit",
                    questions   : null
                },
                evenConfig  : {
                    "#step4_quiz_buttons li -> hover"   : function(){
                        $("#step4_quiz_buttons").find("i.icon_vox_344").removeClass("icon_vox_blue");
                        $(this).radioClass("active").find("i.icon_vox_344").addClass("icon_vox_blue");
                    },
                    "#step4_add_danxuan -> click"           : step4_add_danxuan,
                    "#step4_add_panduan -> click"           : step4_add_panduan,
                    "#step4_add_tiankong -> click"          : step4_add_tiankong,
                    "a.step4_delete_question -> click"      : step4_delete_question,
                    "dl.selection span.radios -> click"     : step4_selection,
                    ".selectionContent -> blur"             : step4_setContent,
                    "input.selectionOptionContent -> blur"  : step4_setOption,
                    "i.plus_button -> click"                : step4_plusOption,
                    "i.minus_button -> click"               : step4_minusOption,
                    "#readingSubmit -> click"               : readingSubmit,
                    "#savaReading -> click"                 : readingSave,
                    "#showView -> click"                    : showView4,
                    "#showViewAll -> click"                 : showViewAll
                }
            },
            leftMenu: {
                evenConfig : {
                    "#leftMenuToStep1 -> click"     : showStep1,
                    "#leftMenuToStep2 -> click"     : showStep2,
                    "#leftMenuToStep3 -> click"     : showStep3,
                    "#leftMenuToStep4 -> click"     : showStep4,
                    "a.leftMenuToPages -> click"    : step3_leftMenuToPages
                }
            },
            init: function(){

                var readingDraft = ${readingDraft};

                if($17.isBlank(readingDraft)){
                    this.DataBase = $.extend({}, this.Constructor.readingDraftTemp);
                    this.step4.tempInfo.questions = $17.isBlank(this.DataBase.questions) ? [] : this.DataBase.questions;
                }else{
                    this.DataBase = readingDraft.content;
                    this.DataBase.id = readingDraft.id;
                }

                ReadingApp.DataBase.pagesIndex = ReadingApp.DataBase.pagesIndex || 0;

                showStep1();

                $(window).scroll(checkPopMenuPosition);
            }
        };

        ReadingApp.init();

        //FIXME 竟然用中文挂事件，长见识了。。。 生成音频
        $('body').on( 'click', 'input[value=生成音频], input[value=上传音频]', function ( e ) {
            var t = $( this), siblings = t.siblings();

            t.prop('checked', true);
            t.siblings('div.' + t.val() ).show();

            t.siblings('input').prop('checked', false);
            t.siblings('div').not('div.' + t.val() ).hide();
        } );
        //FIXME 竟然用中文挂事件，长见识了。。。 生成音频事件
        $('body').on( 'click', 'div.生成音频 input[value=生成]', function () {
            var audio_upload_callback = $(this).attr('data-callback-fn') ? "hou" : "qian";
            var content = jQuery.trim($(this).closest('dl').parent().find('textarea').eq(0).val() );
            // 收集生成音频数据
            function getData ( elem, content ) {
                return { role: elem.filter( 'select.lis_role' ).val(),
                    volume : +elem.filter( 'select.lis_volume' ).val(),
                    speed: +elem.filter( 'select.lis_speed' ).val(),
                    content: content
                };
            }
            if ( content ) {
                $.ajax( {
                    type : 'post',
                    url : '/tts/listening/generateSentence.vpage',
                    data : $.toJSON( getData( $( this).siblings(), content ) ),
                    success : function ( data ) {
                        if ( data.success ) {
                            if(audio_upload_callback == "qian"){
                                var $target = null;

                                if(!$17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage)){
                                    $target = $("#sentence_editer_front");

                                    if($target.attr("data-actiontype") == "edit"){
                                        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[$target.attr("data-sentenceid")*1].audioUri = "/fs-tts/" + data.info;
                                    }else{
                                        var newParts = $.extend(true, {}, ReadingApp.Constructor.readingDraftSentenceTemp);
                                        if($17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[$target.attr("data-sentenceid")*1])){
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences.splice($target.attr("data-sentenceid")*1, 0, newParts);
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[$target.attr("data-sentenceid")*1].audioUri = "/fs-tts/" + data.info;
                                            ReadingApp.doneType = "little";
                                        }else{
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences.splice($target.attr("data-sentenceid")*1+1, 0, newParts);
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[$target.attr("data-sentenceid")*1+1].audioUri = "/fs-tts/" + data.info;
                                            ReadingApp.doneType = "big";
                                        }
                                        ReadingApp.isDone = true;
                                    }
                                }else{
                                    $target = $("#sentence_editer");

                                    if($target.attr("data-actiontype") == "edit"){
                                        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$target.attr("data-sentenceid")*1].audioUri = "/fs-tts/" + data.info;
                                    }else{
                                        var newParts = $.extend(true, {}, ReadingApp.Constructor.readingDraftSentenceTemp);
                                        if($17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$target.attr("data-sentenceid")*1])){
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences.splice($target.attr("data-sentenceid")*1, 0, newParts);
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$target.attr("data-sentenceid")*1].audioUri = "/fs-tts/" + data.info;
                                            ReadingApp.doneType = "little";
                                        }else{
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences.splice($target.attr("data-sentenceid")*1+1, 0, newParts);
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$target.attr("data-sentenceid")*1+1].audioUri = "/fs-tts/" + data.info;
                                            ReadingApp.doneType = "big";
                                        }
                                        ReadingApp.isDone = true;
                                    }
                                }

                                $("#audio_upload_one_input").val("/fs-tts/" + data.info);
                            }else{// audio_upload_callback == "hou"
                                var $target = null;

                                if(!$17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage)){
                                    $target = $("#sentence_editer_back");

                                    if($target.attr("data-actiontype") == "edit"){
                                        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[$("#sentence_editer_back").attr("data-sentenceid")*1].audioUri = "/fs-tts/" + data.info;
                                    }else{
                                        var newParts = $.extend(true, {}, ReadingApp.Constructor.readingDraftSentenceTemp);
                                        if($17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[$("#sentence_editer_back").attr("data-sentenceid")*1])){
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences.splice($target.attr("data-sentenceid")*1, 0, newParts);
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[$("#sentence_editer_back").attr("data-sentenceid")*1].audioUri = "/fs-tts/" + data.info;
                                            ReadingApp.doneType = "little";
                                        }else{
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences.splice($target.attr("data-sentenceid")*1+1, 0, newParts);
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[$("#sentence_editer_back").attr("data-sentenceid")*1+1].audioUri = "/fs-tts/" + data.info;
                                            ReadingApp.doneType = "big";
                                        }
                                        ReadingApp.isDone = true;
                                    }
                                }else{
                                    $target = $("#sentence_editer");

                                    if($target.attr("data-actiontype") == "edit"){
                                        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$("#sentence_editer").attr("data-sentenceid")*1].audioUri = "/fs-tts/" + data.info;
                                    }else{
                                        var newParts = $.extend(true, {}, ReadingApp.Constructor.readingDraftSentenceTemp);
                                        if($17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$("#sentence_editer").attr("data-sentenceid")*1])){
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences.splice($target.attr("data-sentenceid")*1, 0, newParts);
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$("#sentence_editer").attr("data-sentenceid")*1].audioUri = "/fs-tts/" + data.info;
                                            ReadingApp.doneType = "little";
                                        }else{
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences.splice($target.attr("data-sentenceid")*1+1, 0, newParts);
                                            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$("#sentence_editer").attr("data-sentenceid")*1+1].audioUri = "/fs-tts/" + data.info;
                                            ReadingApp.doneType = "big";
                                        }
                                        ReadingApp.isDone = true;
                                    }
                                }
                                $("#audio_upload_two_input").val("/fs-tts/" + data.info);
                            }
                        }
                    },
                    error : function () {
                        // 超时，停止
                    },
                    timeout: 6000,
                    dataType : 'json',
                    contentType : 'application/json;charset=UTF-8'
                } );
            }
        } );
    });
</script>
</@temp.page>