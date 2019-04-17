<#--年级和班级模板-->
<script id="t:年级和班级是好朋友" type="text/html">
    <ul>
        <% for(var i = 0, l = batchclazzs.length; i < l; i++){ %>
        <% if(batchclazzs[i].length > 0){ %>
        <li data-level="<%= i + 1 %>" class="v-level"><%= i + 1 %>年级</li>
        <% } %>
        <% } %>

        <% for(var i = 0, l = batchclazzs.length; i < l; i++){ %>
        <% if(batchclazzs[i].length > 0){ %>
        <li data-level-bycontent="<%= i + 1 %>" class="v-parent pull-down" style="display: none;">
        <#--//start - 是否能布置分组作业-->
            <%if(groupSupported){%>
            <%var groupFlag = false%>
            <% for(var f = 0, kl = batchclazzs[i].length; f < kl; f++){ %>
            <%if(batchclazzs[i][f].curTeacherArrangeableGroups.length > 0){%>
            <%groupFlag = true%>
            <%}%>
            <% } %>
            <%if(groupFlag){%>
            <div class="v-switch-arrange" data-level="<%= i + 1 %>" style="clear: both;padding: 10px;text-align: left;">
                <label style="cursor: pointer;" data-type="v-targets" class="w-radio-current"><span class="w-radio"></span> <span class="w-icon-md">按班级布置</span></label>
                <label style="cursor: pointer;" data-type="v-targets-groups"><span class="w-radio"></span> <span class="w-icon-md">按分组布置</span></label>
            </div>
            <%}%>
            <%}%>
        <#--end//-->
            <p class="v-alltarget">
                <span class="w-checkbox"></span>
                <span class="w-icon-md">全部</span>
            </p>
            <% for(var j = 0, jl = batchclazzs[i].length; j < jl; j++){ %>
        <#--分组方式-->
            <%var group = batchclazzs[i][j].curTeacherArrangeableGroups%>
            <% for(var g = 0, gp = group.length; g < gp; g++){ %>
            <p class="v-targets-groups" data-value="<%= batchclazzs[i][j].id %>" data-clazzname="<%= batchclazzs[i][j].className %>-<%=group[g].groupName%>" data-groupid="<%= group[g].clazzId %>_<%= group[g].id %>" title="<%= batchclazzs[i][j].className %>-<%=group[g].groupName%>" style="display: none;">
                <span class="w-checkbox"></span>
                <span class="w-icon-md"><%= batchclazzs[i][j].className %>-<%=group[g].groupName%></span>
            </p>
            <% } %>
        <#--班级方式-->
            <%if(group.length > 0){%>
        <#--是否有分组班级-->
            <p class="v-targets" data-value="<%= batchclazzs[i][j].id %>" data-clazzname="<%= batchclazzs[i][j].className %>" data-groupid="<% for(var g = 0, gp = group.length; g < gp; g++){ %><%= group[g].clazzId %>_<%= group[g].id %><%if(g < gp-1){%>,<%}%><% } %>" title="<%= batchclazzs[i][j].className %>">
                <span class="w-checkbox"></span>
                <span class="w-icon-md"><%= batchclazzs[i][j].className %></span>
            </p>
            <%}else{%>
            <p class="v-targets" data-value="<%= batchclazzs[i][j].id %>" data-clazzname="<%= batchclazzs[i][j].className %>" data-groupid="<%= batchclazzs[i][j].id %>" title="<%= batchclazzs[i][j].className %>">
                <span class="w-checkbox"></span>
                <span class="w-icon-md"><%= batchclazzs[i][j].className %></span>
            </p>
            <%}%>
            <% } %>
        </li>
        <% } %>
        <% } %>
    </ul>
</script>
<#--主观作业-->
<script id="t:O2O主观作业" type="text/html">
    <div class="w-base">
        <div id="v-subjectivePanel" style="padding: 22px 0;">
            <#--<div class="w-base-right w-base-more" style="float: right;font-size: 13px;margin-right: 10px;margin-top: -14px;">
                <div class="qm-icon" id="handoutHelp">
                    <a href="javascript:void(0);">
                        <span class="w-icon-md"> 学生如何交作业</span>
                    </a>
                </div>
            </div>-->
            <div class="w-form-table">
                <dl>
                    <dt style="width: 130px;">
                        请输入作业内容：
                    </dt>
                    <dd style="margin-left: 140px;">
                        <%if(subject == 'ENGLISH') {%>
                            <div class="t-subject-icon-box">
                                <a href="javascript:void(0);" data-index="0" class="sub-icon sub-icon-1 v-template-icon js-read">朗读</a>
                                <a href="javascript:void(0);" data-index="1" class="sub-icon sub-icon-2 v-template-icon js-recite">背诵</a>
                                <a href="javascript:void(0);" data-index="2" class="sub-icon sub-icon-3 v-template-icon js-handWri">抄写</a>
                                <a href="javascript:void(0);" data-index="3" class="sub-icon sub-icon-4 v-template-icon js-listWri">听写</a>
                                <a href="javascript:void(0);" data-index="4" class="sub-icon sub-icon-5 v-template-icon js-writSin">默写</a>
                            </div>
                        <%} else if(subject == 'MATH') {%>
                            <div class="t-math-icon-box">
                                <a href="javascript:void(0);" data-index="0" class="math-icon math-icon-1 v-template-icon js-caluWri">计算书写</a>
                                <a href="javascript:void(0);" data-index="1" class="math-icon math-icon-2 v-template-icon js-handAct">动手操作</a>
                                <a href="javascript:void(0);" data-index="2" class="math-icon math-icon-3 v-template-icon js-findObj">找物体</a>
                                <a href="javascript:void(0);" data-index="3" class="math-icon math-icon-4 v-template-icon js-writPap">手抄报</a>
                                <a href="javascript:void(0);" data-index="4" class="math-icon math-icon-5 v-template-icon js-talk">说一说</a>
                            </div>
                        <%} else {%>
                            <div class="t-subject-icon-box">
                                <a href="javascript:void(0);" data-index="0" class="sub-icon sub-icon-1 v-template-icon js-readLes">读课文</a>
                                <a href="javascript:void(0);" data-index="1" class="sub-icon sub-icon-2 v-template-icon js-recite-chin">背诵</a>
                                <a href="javascript:void(0);" data-index="2" class="sub-icon sub-icon-3 v-template-icon js-handWri-chin">抄写</a>
                                <a href="javascript:void(0);" data-index="3" class="sub-icon sub-icon-4 v-template-icon js-pric">习作</a>
                                <a href="javascript:void(0);" data-index="4" class="sub-icon sub-icon-5 v-template-icon js-readOutClass">课外阅读</a>
                            </div>
                        <%}%>
                        <div class="w-intPopLabel" style="position: relative;">
                            <%if(subject == 'ENGLISH'){%>
                            <label class="w-placeholder v-textarea-placeholder" for="v-sub-content" <%if(content != ""){%>style="display:none;"<%}%> >
                                您可以自定义作业内容，也可以点击上面的作业类型选择对应模板
                            </label>
                                <textarea id="v-sub-content" class="w-int" maxlength="140" style="height: 100px; overflow: auto; width: 600px; font-size: 14px;"><%=content%></textarea>
                            <%} if(subject == 'MATH') {%>
                            <label class="w-placeholder v-textarea-placeholder" for="v-sub-content" <%if(content != ""){%>style="display:none;"<%}%> >
                                您可以自定义作业内容，也可以点击上面的作业类型选择对应模板
                            </label>
                                <textarea id="v-sub-content" class="w-int" maxlength="140" style="height: 100px; overflow: auto; width: 600px; font-size: 14px;"><%=content%></textarea>
                            <%} if(subject == 'CHINESE') {%>
                            <label class="w-placeholder v-textarea-placeholder" for="v-sub-content" <%if(content != ""){%>style="display:none;"<%}%> >
                                例：把今天学过的课文读给父母听，让他们评价你读得怎么样，并把评价拍照提交
                            </label>
                                <textarea id="v-sub-content" class="w-int" maxlength="140" style="height: 100px; overflow: auto; width: 600px; font-size: 14px;"><%=content%></textarea>
                            <%}%>
                            <div style="position: absolute; left: 493px; top: 85px; width: 120px; text-align: right; color: #999;"><span id="v-sub-textCount">0</span>/140</div>
                        </div>
                    </dd>
                    <dt style="width: 130px;">学生提交方式：</dt>
                    <dd style="margin-left: 140px;">
                        <%if (fileType == "IMAGE") {%>
                            <label data-type="IMAGE" class="btnSub v-fileType w-btnBlue" style="cursor: pointer;" title="支持学生提交多张照片">
                                <span class="w-icon-sub icon-1">拍照提交</span>
                            </label>
                            <span style="color: #FF0000;">（暂不能更改提交方式）</span>
                        <%} else if (fileType == "AUDIO") {%>
                            <label data-type="AUDIO" class="btnSub v-fileType w-btnBlue" style="cursor: pointer; position: relative;" title="录音提交需要学生下载一起作业手机端哦，请提前告知学生~">
                                <span class="w-icon-sub icon-2">录音提交</span>
                            </label>
                            <span style="color: #FF0000;">（暂不能更改提交方式）</span>
                        <%} else if (fileType == "AUDIOIMAGE") {%>
                            <label data-type="AUDIOIMAGE" class="btnSub v-fileType w-btnBlue" style="cursor: pointer;" title="作业包含拍照作业和录音作业，只支持手机端提交">
                                <span class="w-icon-sub icon-3">拍照+录音</span>
                            </label>
                            <span style="color: #FF0000;">（暂不能更改提交方式）</span>
                        <%} else {%>
                            <label data-type="IMAGE" class="btnSub v-fileType" style="cursor: pointer;" title="支持学生提交多张照片">
                                <span class="w-icon-sub icon-1">拍照提交</span>
                            </label>
                            <label data-type="AUDIO" class="btnSub v-fileType" style="cursor: pointer; position: relative;" title="录音提交需要学生下载一起作业手机端哦，请提前告知学生~">
                                <span class="w-icon-sub icon-2">录音提交</span>
                            </label>
                            <% if (show) {%>
                            <label data-type="AUDIOIMAGE" class="btnSub v-fileType" style="cursor: pointer;" title="作业包含拍照作业和录音作业，只支持手机端提交">
                                <span class="w-icon-sub icon-3">拍照+录音</span>
                            </label>
                            <%}%>
                        <%}%>
                    </dd>
                </dl>
            </div>
            <div class="t-pubfooter-btn">
                <a id="v-sub-preview-btn" href="javascript:void(0);" class="w-btn w-btn-grey">预览</a>
                <a id="v-sub-confirm-btn" href="javascript:void(0);" class="w-btn">确认布置</a>
            </div>
        </div>
    </div>
</script>


<#--提交帮助说明-->
<script id="t:handoutHelpTemplate" type="text/html">
    <div class="subjective-job" style="margin: -30px -20px -20px;">
        <div class="subjective-info">
            <div class="left">
                <div class="subjective-info-1"></div>
                <div class="con">请尽量让学生下载一起作业学生端，来完成主观作业的提交，提交更方便</div>
            </div>
            <div class="center">
                <div class="subjective-info-2"></div>
                <div class="con">拍照作业支持通过电脑和一起作业手机版提交，老师会得到学生的作业照片(暂只支持一张照片)</div>
            </div>
            <div class="right">
                <div class="subjective-info-3"></div>
                <div class="con">录音作业只支持通过一起作业手机版提交，老师会得到学生的作业录音(暂只支持一段录音)</div>
            </div>
        </div>
    </div>
</script>

<script id="t:预览主观作业" type="text/html">
    <div class="w-form-table">
        <dl>
            <dt style="width: 130px;">题目内容：</dt>
            <dd style="margin-left: 140px;">
                <div style="line-height: 28px;"><%=content%></div>
            </dd>
            <dt style="width: 130px;">题目要求：</dt>
            <dd style="margin-left: 140px;">
                <% if (fileType == "IMAGE") {%>
                    <div style="line-height: 28px;">上传作业照片提交作业，照片一定要按老师要求来拍清晰哦。</div>
                <%} else {%>
                    <div style="line-height: 28px;">本次作业需要提交录音，请使用一起作业学生端完成作业并上交。</div>
                <%}%>
            </dd>
        </dl>
        <% if (fileType == "IMAGE") {%>
            <div class="studentHomeLayer-box png24">
                <div class="studentTab">
                    <span class="tab-1 active">手机上传作业照片</span>
                    <span class="tab-2">电脑上传作业照片</span>
                </div>
                <div style="display:block;">
                    <div class="studentBox studentBox-top">
                        <div class="studentCode">
                            <#--<img src="images/photo.jpg" alt="">-->
                            <div style="width: 135px; height: 135px; line-height: 135px; border: 1px dashed #ddd; display: inline-block">二维码</div>
                            <div class="codeHd">扫描上方二维码提交作业照片</div>
                            <#--<div class="codeTxt">也可以直接用手机浏览器登录 www.17zuoye.com提交主观作业</div>-->
                        </div>
                    </div>
                    <div class="studentBox">
                        <em class="ico" style="left: 32px;"></em>
                        <div class="studentText">
                            <div class="textLeft">
                                <div class="textHd"><span class="ico-1">使用微信扫一扫</span></div>
                                <div class="textStep step-1"><span>第一步：</span><span class="ico-1">登录手机微信</span></div>
                                <div class="textStep step-1"><span>第二步：</span><span class="ico-2">从“发现”进入“扫一扫”</span></div>
                            </div>
                            <div class="textRight">
                                <div class="textHd"><span class="ico-2">使用QQ扫一扫</span></div>
                                <div class="textStep step-2"><span class="ico-1">登录手机QQ</span></div>
                                <div class="textStep step-2"><span class="ico-2">点击“右上角菜单”进入“扫一扫”</span></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        <%}%>
    </div>
</script>