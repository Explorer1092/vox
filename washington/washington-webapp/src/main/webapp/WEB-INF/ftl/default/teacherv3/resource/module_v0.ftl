<#-- @ftlvariable name="currentUser" type="com.voxlearning.utopia.service.user.api.entities.User" -->
<#-- @ftlvariable name="currentTeacherDetail" type="com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail" -->
<#-- @ftlvariable name="currentTeacherWebGrayFunction" type="com.voxlearning.washington.support.gray.WebGrayFunction" -->

<#import "../../nuwa/teachershellv3.ftl" as temp />
<#macro page title="资源" level="上传资源">
    <@temp.page <#--index="resource" columnType="normal"-->>
        <@sugar.capsule js=["swfupload", "resource.handlers"] />
        <style type="text/css">
            .form_wrap li.lab input, .form_wrap li.lab select{ background-color: #fff; border: 1px solid #dedede; color: #555; width: 150px; padding: 2px 9px; line-height: 19px; height: 19px; vertical-align: middle;}
            .form_wrap li.lab input:focus, .form_wrap li.lab input.w-int-active{ border-color: #189cfb; background-color: #fff}
            .form_wrap li.lab select{ height: 24px;}
            .form_wrap li.lab b{ font-weight: normal; color: #666;}
            .form_wrap li.lab { margin: 0 0 15px;}
            .securityList li .btn{ width: 140px;}
            .securityEdit .sAvatar .sar{ margin-right: 20px;}
            .updateone{ height: 100%;}

            /*progressWrapper */
            .tableFileListBox { margin: 15px;}
            .tableFileListBox h3{ font-size: 12px; color: #666; padding: 0 0 10px;}
            .tableFileListBox .uploadBarWarp{ background-color: #f5f5f5; padding: 10px; border: 1px solid #ddd; margin-top: -1px;}
            .tableFileListBox .uploadBarWarp .fileTotalSize{ float: right; color: #999; margin-top: 6px;}
            #resourceUploadProgress{ line-height: 50px; text-align: center}
            .progressWrapper {margin: 0; clear: both; background-color: #fff;}
            .progressWrapper .title{ background-color: #f5f5f5;}
            .progressWrapper .progressContainer { border: 1px solid #ddd;  height: 36px; margin: -1px 0 0; overflow: hidden; position: relative; background-color: #fff;}
            .progressWrapper .progressContainer div{border: solid #ddd; border-width: 0 0 0 1px; width: 25%; margin: -1px 0 0 -1px; padding: 0; height: 36px; line-height: 36px; text-indent: 20px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; float: left; }
            .progressWrapper .progressContainer .progressCancel{ float: right; width: 15%; text-align: center; text-indent: 0;}
            .progressWrapper .progressContainer .progressName{ float: left; width: 35%;}
            .progressWrapper .progressContainer .progressBarSize{ float: left;}
            .progressWrapper .progressContainer .progressBarStatus{ float: left; }
            /*进度条*/
            .progressWrapper .progressContainer .progressBarBox, .progressWrapper .progressContainer .progressBarComplete{ width: 65%; text-indent: 0; display: none; text-align: center; right: 0; position: absolute; top: 0; background-color: #fff;}
            .progressWrapper .progressContainer .progressBarBox .progressBarWarp{ width: 195px; height: 12px; padding: 6px 0 0 6px; font: 1px/1px arial; margin: 10px auto 0; float: none; background: url(/public/skin/blue/images/progressback.png) no-repeat 0 0; border: none;}
            .progressWrapper .progressContainer .progressBarBox .progressBarWarp .progressBarSpeed{ width: 100%; height: 7px; background: url(/public/skin/blue/images/progressback.png) no-repeat 0 -31px; font: 1px/1px arial; border: none;}

            /*paperInfoBox*/
            .paperInfoBox{ margin:10px auto; padding:0 !important; clear:both; border:1px solid #ccc; border-radius:5px; overflow:hidden;}
            .paperInfoBox h4{ padding:10px; text-align:center; font:14px/1.125 arial; color:#666; background:#f8f8f8; border-bottom:1px solid #ccc;}
            .paperInfoBox ul{ padding:20px 0;}
            .paperInfoBox .typeTag a{ display:inline-block; font:12px/1.125 arial; padding:3px 5px; background:#78c3fd; border:1px solid #78c3fd; margin:3px 2px 3px 0; color:#fff; outline: none;}
            .paperInfoBox .typeTag a:hover,.form_wrap li .typeTag .even{ color:#999; border-color:#e9c418; background:#fef5b7;}
            .paperInfoNameBox{ padding:10px; text-align:center; clear:both; font: bold 24px/1.125 "微软雅黑", "Microsoft YaHei", Arial; color:#f93;}
        </style>
            <div class="w-base">
                <div class="w-base-title">
                    <h3>资源</h3>
                    <div class="w-base-right w-base-switch">
                        <ul>
                            <#if [500000]?seq_contains(currentTeacherDetail.rootRegionCode) && (currentTeacherDetail.subject)?? && currentTeacherDetail.subject == "ENGLISH">
                                <li <#if level=="公开课视频">class="active"</#if>><a href="/project/downloadpublicclass/index.vpage"><span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span><strong>公开课视频</strong></a></li>
                            </#if>
                            <#-- ResourcePlatform 2015.11.03 Sir0xb -->
                            <#if currentTeacherDetail.subject != "ENGLISH" || !currentTeacherWebGrayFunction.isAvailable("ResourcePlatform", "Open")>
                                <#if ((currentTeacherDetail.subject)?? && (currentTeacherDetail.subject == "ENGLISH" || currentTeacherDetail.subject == "MATH")) >
                                    <#if subjects??>
                                        <#list subjects as teacherSubject >
                                            <#if ((teacherSubject == "ENGLISH" || teacherSubject == "MATH")) >
                                                <li <#if level=="${teacherSubject!}资源">class="active"</#if>><a href="/teacher/resource/share.vpage?subject=${teacherSubject!}"><span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span><strong>${teacherSubject.value!}资源</strong></a></li>
                                            </#if>
                                        </#list>
                                    </#if>
                                <#else>
                                    <li <#if level=="我的上传">class="active"</#if>><a href="/teacher/resource/list.vpage"><span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span><strong>我的上传</strong></a></li>
                                </#if>
                            </#if>
                        </ul>
                    </div>
                </div>
                <!--template container-->
                <div class="w-base-container">
                    <!--//start-->
                    <#nested>
                    <!--end//-->
                </div>
            </div>

    <script type="text/javascript">
        $(function(){
            <#-- ResourcePlatform 2015.11.03 Sir0xb -->
            <#if currentTeacherDetail.subject != "ENGLISH" || !currentTeacherWebGrayFunction.isAvailable("ResourcePlatform", "Open")>
                LeftMenu.focus("resource");
            </#if>
        });
    </script>
    </@temp.page>
</#macro>
