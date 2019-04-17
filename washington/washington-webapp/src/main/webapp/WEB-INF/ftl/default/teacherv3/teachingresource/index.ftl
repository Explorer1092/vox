<#import "layout.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["plugin.venus-pre"] css=["plugin.venus-pre","teachingresource.index"] />
    <style type="text/css">
        .leftLeafSpring, .contentLeafSpring{
            transition: height 1s;
            -moz-transition: height 1s; /* Firefox 4 */
            -webkit-transition: height 1s; /* Safari 和 Chrome */
            -o-transition: height 1s; /* Opera */
        }
    </style>
     <div class="mainBox" id="teachingresource">
         <div class="leftBox">
             <div class="leftTopBox">
                 <div class="logoBox"></div>
                 <div class="allMenu">全部目录</div>
             </div>
             <div class="leftMenu">
                 <div class="leftLeafSpring" v-bind:style="{height: springSwitch ? '190px' : 0 }"></div>
                 <menu-panel :menu-list="menuList"
                            v-on:onelevel-click="oneLevelClickCb" v-on:twolevel-click="twoLevelClickCb"></menu-panel>
             </div>
             <div class="choiceBox">
                 <div v-if="!isEmptySubject" class="choiceSubject" @click="subjectClick"><span v-text="subjectName"></span><i v-show="subjectList.length > 1"></i></div>
                 <div v-if="!isEmptySubject" class="choiceGrade" @click="bookNameClick"><span v-text="book.bookName || '选择年级教材'"></span> <i></i></div>
             </div>
         </div>
         <div class="shadowLayer" style="display: none" v-show="showShadowLayer"></div>
         <subject-choice-list
                 :subject="subject"
                 :subject-list="subjectList"
                 :show-panel="showSubjectPanel"
                 v-on:change-subject="changeSubject" v-on:close-subject-box="subjectClick"></subject-choice-list>
         <book-list :level="book.clazzLevel"
                    :term="book.termType"
                    :show-panel="bookListPanel"
                    :subject="subject"
                    v-on:book-confirm="bookConfirmCb"
                    v-on:close-book-box="bookNameClick"></book-list>
         <div class="containerBox">
             <div class="containerCard">
                 <div class="containerCon">
                     <div class="contentLeafSpring" v-bind:style="{height: springSwitch ? '360px' : 0}" v-if="currentType.type!=='LEVEL_READINGS' && currentType.type!=='NATURAL_SPELLING'"></div>
                     <component v-if="messageObj && messageObj.success"
                                v-bind:is="getComponentTag(currentType.type)"
                                v-bind="currentType" v-on:content-message="contentLoadingCb"
                                v-on:preview-reading="previewReadingCb" v-on:previewtype="previewTypeCb"
                                v-on:preview-word="previewWordCb" v-on:preview-video="previewVideoCb" v-on:preview-word-id="previewWordIdCb"
                     ></component>
                     <a target="_blank" rel="noopener noreferrer" v-bind:href="targetPreviewUrl" ><span ref="targetTag"></span></a>
                     <#--<tip-area v-bind="messageObj" v-if="messageObj && !messageObj.success"></tip-area>-->
                     <#--{{messageObj}}-->
                     <div class="tipsCard" v-if="messageObj && !messageObj.success">
                         <div class="tipsPic" v-bind:class="{'noResources':messageObj.noResources,'noNetWork':messageObj.noNetWork,'isLoading':messageObj.isLoading}"></div>
                         <p class="tipsCon" v-if="messageObj.noResources" v-text="'暂无资源'"></p>
                         <p class="tipsCon" v-if="messageObj.noNetWork" v-text="'当前网络异常,请刷新页面'"></p>
                         <p class="tipsCon" v-if="messageObj.isLoading" v-text="'正在加载…'"></p>
                     </div>
                 </div>
                 <div class="containerBottom">
                     <tab-type-list :type-list="typeList" v-on:tab-click="tabClickCb"></tab-type-list>
                     <div class="dropDown" style="display: none" v-show="!isDateduppt" v-on:click="springSwitchClick" v-text="springSwitch ? '收起' : '下拉'"></div>
                     <div class="previewWord" style="display: none" v-show="isDateduppt" @click="previewWord(previewWordIdList[0],previewWordIdList)">预览({{previewWordIdList.length}}个)</div>
                     <#--<div class="clear" style="display: none" v-show="isDateduppt" @click="clearWord">清空</div>-->
                     <#--<div class="closePage" v-if="!isDateDu" v-on:click="closePage">关闭</div>-->
                 </div>
             </div>
         </div>
     </div>

     <script type="text/html" id="T:UNIT_LIST">
         <ul style="display:none;" v-show="menuList.length > 0">
             <li v-for="(item,index) in menuList" v-bind:class="{'pullDown' : item.childrens.length > 0,'active':item.id == oneLevelId}" v-on:click="oneLevelClick(item,index)">
                 <a class="oneLevel" href="javascript:void(0)" v-text="item.name"></a>
                 <div class="pullMenu" v-if="item.childrens.length > 0">
                     <a href="javascript:void(0)"
                        v-for="(subItem,zIndex) in item.childrens"
                        v-bind:class="{'current' : subItem.id == twoLevelId}"
                        v-text="subItem.name"
                        v-on:click.stop="twoLevelClick(subItem,zIndex)"></a>
                 </div>
             </li>
         </ul>
     </script>

     <script type="text/html" id="T:CHOICE_SUBJECT_BOX">
         <div class="choiceSubjectBox" style="display: none;" v-show="showPanel">
             <div class="teach-jqiclose" v-on:click="closeSubjectBox">×</div>
             <div class="teach-jqistates">
                 <div class="teach-jqistate" style="">
                     <div class="lead teach-jqititle ">选择学科</div>
                     <div class="subjectBox">
                         <div class="subjectInfo"
                              v-for="(item,index) in subjectList"
                              :key="item.subject"
                              v-bind:class="{'active':item.subject == subject}"
                              v-text="item.subjectName"
                              v-on:click="changeSubject(index)"></div>
                     </div>
                     <div class="bottom">选择学科 <i></i></div>
                 </div>
             </div>
         </div>
     </script>

     <script type="text/html" id="t:CHANGE_BOOK_POPUP">
         <div class="gradeBox" style="display: none" v-show="showPanel">
             <div class="teach-jqiclose" v-on:click="closeBookBox">×</div>
             <div class="teach-jqistates">
                 <div class="teach-jqistate" style="">
                     <div class="lead teach-jqititle ">换课本</div>
                     <div class="teach-jqimessage ">
                         <div class="h-homework-dialog04 h-homework-dialog">
                             <div class="inner">
                                 <p>
                                     <span class="iname">册别：</span>
                                     <label style="cursor: pointer;" v-for="(item,index) in termList" v-bind:class="{'w-radio-current':item.key == focusTerm}" v-on:click="termClick(item,index)">
                                         <span class="w-radio"></span> <span class="w-icon-md" v-text="item.name"></span>&nbsp;&nbsp;
                                     </label>
                                 </p>
                                 <p>
                                     <span class="iname">年级：</span>
                                     <label style="cursor: pointer;" v-for="(levelObj,index) in levelList" v-bind:class="{'w-radio-current':levelObj.level == focusLevel}" v-on:click="levelClick(levelObj,index)">
                                         <span class="w-radio"></span> <span class="w-icon-md" v-text="levelObj.levelName"></span>&nbsp;&nbsp;
                                     </label>
                                 </p>
                                 <div class="list-box">
                                     <div class="list-hd">
                                         <p class="hdl">教材列表</p>
                                     </div>
                                     <div class="list-mn">
                                         <a href="javascript:void(0);"
                                            v-for="(book,index) in bookList"
                                            :title="book.name"
                                            v-text="book.name"
                                            v-on:click="selectBook(book,index)"
                                            v-bind:class="{'active':focusBookId == book.id}"></a>
                                     </div>
                                 </div>
                             </div>
                         </div>
                     </div>
                 </div>
             </div>
             <div class="bottom">选择年级教材 <i></i></div>
         </div>
     </script>

     <#include "../templates/teachingresource/basicapp.ftl">
     <#include "../templates/teachingresource/levelreadings.ftl">
     <#include "../templates/teachingresource/intelligentteaching.ftl">
     <#include "../templates/teachingresource/courseware.ftl">
     <#include "../templates/teachingresource/wordrecognitionandreading.ftl">
     <#include "../templates/teachingresource/keypoints.ftl">
     <#include "../templates/teachingresource/cuotibao.ftl">
     <#include "../templates/teachingresource/wordteachandpractice.ftl">
     <#include "../templates/teachingresource/naturalspelling.ftl">

     <script type="text/html" id="T:HOMEWORK_TYPE_LIST">
         <p>
             <label style="cursor: pointer;"
                    v-for="(item,index) in typeList" v-bind:class="{'w-radio-current':focusTypeObj.type == item.type}" v-on:click="tabClick(item,index)">
                 <span class="w-radio"></span> <span class="w-icon-md" v-text="item.typeName"></span>
             </label>
         </p>
     </script>
    <script type="text/html" id="T:INFO_TIP">
        <div v-text="info" style="padding: 20px; font-size: 14px;color:#b7afaf; "></div>
    </script>
    <script id="t:LOAD_IMAGE" type="text/html">
        <div style="height: 200px; background-color: white; width: 98%;">
            <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="display:block;margin: 0 auto;" />
        </div>
    </script>
     <script type="text/javascript">
         var constantObj = {
             subject     : "${subject!}",
             subjectList : ${subjects![]},
             imgDomain   : '${imgDomain!''}',
             domain      : '${requestContext.webAppBaseUrl}/',
             env         : <@ftlmacro.getCurrentProductDevelopment />,
             categoryIconPrefixUrl : '<@app.link href='public/skin/teacherv3/images/homework/english-icon/' />'
         };
     </script>
    <@sugar.capsule js=["teachingresource.index"]/>
</@shell.page>