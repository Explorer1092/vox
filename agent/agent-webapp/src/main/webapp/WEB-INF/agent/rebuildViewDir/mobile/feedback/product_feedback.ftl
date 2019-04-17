<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign id = feedback?? && feedback.id?has_content>
<#assign teacher = feedback?? && feedback.teacherName?has_content>
<#assign teacherName = feedback?? && feedback.teacherName?has_content && feedback.id?has_content>
<#assign teacherId = feedback?? && feedback.teacherId?has_content>
<#assign id = feedback?? && feedback.id?has_content>
<@layout.page title="产品反馈" pageJs="feedback" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['feedback']/>
<style>body{background-color:rgb(241,242,245)}</style>
<script src="/public/rebuildRes/js/common/common.js"></script>
<a href="javascript:void(0);" style="display:none;" class="inner-right js-submitSchoolSRecord"><#if id><#else>提交</#if></a>
<div class="flow">
    <#if feedback?? && feedback.id?has_content>
        <div class="fBack-section">
            <div class="fBack-message">
                <div class="ft"><#if feedback.teacherId?has_content><span
                        class="time">编号：${feedback.id?string('000000')!''}</span></#if><span
                        class="num">反馈日期：${feedback.createDatetime?string("yyyy-MM")}</span></div>
            </div>
        </div>
    </#if>
    <#if feedback?? && feedback.id?has_content>
        <div class="fBack-section" style="margin-top:.5rem;background: #fff;padding:.5rem .75rem">
            <div class="fBack-message">
                <div class="hd"><span class="time" style="color:#3b73af">反馈回复：</span><span class="txtRed">${feedback.feedbackStatus.desc!''}</span></div>
            </div>
            <div class="aside">
                <#if applyProcessResult?? && applyProcessResult?size gt 0>
                    <ul>
                        <#list applyProcessResult as process>
                            <li style="font-size: .65rem;word-break: break-all"><i class="avatar"></i>${process.accountName!''}：${process.processNotes!''}</li>
                        </#list>
                    </ul>
                </#if>
            </div>
        </div>
    </#if>
    <div style="background: #fff">
    <#if feedback?? && feedback.id?has_content>
        <div class="fBack-section" style="margin-top:.5rem">
            <div class="fBack-message">
                <div class="hd"><span class="time" style="color:#3b73af">反馈详情：</span></div>
            </div>
        </div>
    </#if>
    <div class="item select-partner" style="position: relative">
        类别
        <div class="inner-right" style="background: none" id="partnerDisplay"><#if feedback?? && feedback.feedbackType??>${feedback.feedbackType.desc!''}<#else>请选择</#if></div>
    <#if id>
    <#else>
        <select id="selectPartner" style="width:100%;position:absolute;height:100%;left:0;top:0;opacity: 0;border:none;" class="js-postData">
            <option value="0" data-name="0" data-type="0">无</option>
            <#if feedbackType?? && feedbackType?has_content>
                <#list feedbackType as type>
                    <option value="${type.type!0}"  <#if feedback?? && feedback.feedbackType?? && feedback.feedbackType.type == type.type >
                            selected </#if>>${type.desc!""}</option>
                </#list>
            </#if>
        </select>
    </#if>
    </div>
    <#if feedback??>
    <div class="item GPS clearfix ">
        反馈人
        <div class="inner-right <#if id><#else> mainTitle</#if>" style="padding:0" data-einfo="请填写反馈人">
            <div class="btn-stroke fix-width <#if id><#else>js-chooseRole</#if> <#if feedback.mySelf?? &&  feedback.mySelf!false>the</#if>"
                 style="width:3.75rem;margin-right:0.5rem;">我自己</div>
            <div class="btn-stroke fix-width <#if id><#else>js-chooseRole</#if> <#if feedback.mySelf?? && !(feedback.mySelf!false) && feedback.teacherId?has_content>the</#if>"
                 style="width:3.75rem;">老师</div>
        </div>
        <div style="font-size:.45rem;color:#3b73af;padding:0 0 .5rem 0;line-height:.5rem">
            C端产品老师的反馈采纳率会更高哦~
        </div>
    </div>
    <div class="teacherRole" <#if !(feedback.mySelf!false) && feedback.teacherId?has_content><#else>style="display:none;"</#if>>
        <div class="item choice_teacher">
            反馈老师
            <div id="chooseTeacher" class="inner-right <#if teacherName><#else>js-chooseTeacher</#if>"><#if teacher>${feedback.teacherName}<#else>请选择</#if></div>
            <input hidden type="text" id="schoolId" name="schoolId" value="<#if teacher>${feedback.teacherName}<#else>请选择</#if>" class="js-need" data-einfo="请选择反馈老师"/>
        </div>

        <div class="item GPS clearfix choice_teacher_notice">
            允许发送消息感谢老师
            <div name="instructorAttend" class="inner-right <#if id><#else>js-ulItem mainTitle grateful</#if>" style="padding:0" data-einfo="请选择是否允许感谢老师">
                <div data-opvalue="1" class="btn-stroke fix-width <#if feedback?has_content && feedback.noticeFlag?has_content && feedback.noticeFlag!false>the</#if>" data-type="1" style="width:3.75rem;margin-right:0.5rem;">是</div>
                <div data-opvalue="2" class="btn-stroke fix-width <#if feedback?has_content && feedback.noticeFlag?has_content && !feedback.noticeFlag!false>the</#if>" data-type="0" style="width:3.75rem;">否</div>
            </div>
        </div>
    </div>
<div class="tab_one" style="display: none;">
    <div class="item">
        教材名称
        <div class="inner-right-text">
            <input type="text" id="" placeholder="请填写" class="js-check js-postData bookName" name="bookName" maxlength="20" data-einfo="请填写教材名称">
        </div>
    </div>
    <div class="item">
        年级
        <div class="inner-right-text">
            <input type="text" id="" placeholder="请填写" class="js-check js-postData gradeLevel" name="gradeLevel" maxlength="20" data-einfo="请填写年级">
        </div>
    </div>
    <div class="item unit">
        单元
        <div class="inner-right-text">
            <input type="text" id="" placeholder="请填写" class="js-check js-postData bookUnit" name="gradeLevel" maxlength="20" data-einfo="请填写单元">
        </div>
    </div>
    <div class="item area">
        覆盖地区
        <div class="inner-right-text">
            <input type="text" id="" placeholder="请填写" class="js-check js-postData part" name="part" maxlength="20" data-einfo="请填写覆盖区域">
        </div>
    </div>
    <div class="item count">
        覆盖学生数
        <div class="inner-right-text">
            <input type="telphone" onkeypress="return event.keyCode>=48&&event.keyCode<=57" placeholder="请填写数字" class=" js-check js-postData studentCount" name="studentCount" maxlength="6" data-einfo="请填写覆盖学生数">
        </div>
    </div>
</div>
    <#if id>
    <#else>
    <div class="tab_three" style="display: none">
        <div class="item tip">
            反馈内容
            <textarea rows="5" style="border:1px solid #eaeaea" class="content meetingNote" id="" name="meetingContent" maxlength="200" data-einfo="请填写反馈内容" placeholder="请点击填写..."><#if feedback?has_content && feedback.content?has_content>${feedback.content!''}</#if></textarea>
        </div>
        <div class="item">
            附图 （选填，最多上传三张照片）
        <div class="photp_container">
            <!--    照片添加    -->
            <div class="z_photo">
                <div class="has_content">
                    <input type="hidden" class="photoUrl1" name="photoUrl">
                    <span <#if feedback.pic1Url?? && feedback.pic1Url?has_content>class="fix"</#if>></span>
                    <div class="z_file1 z_file">
                        <img style="width:100%;height:100%" src="<#if feedback.pic1Url?? && feedback.pic1Url?has_content>${feedback.pic1Url!''}</#if>"/>
                    </div>
                </div>
                <div class="has_content">
                    <input type="hidden" class="photoUrl2" name="photoUrl">
                    <span <#if feedback.pic2Url?? && feedback.pic2Url?has_content>class="fix"</#if>></span>
                    <div class="z_file2 z_file">
                        <img style="width:100%;height:100%" src="<#if feedback.pic2Url?? && feedback.pic2Url?has_content>${feedback.pic2Url!''}</#if>"/>
                    </div>
                </div>
                <div class="has_content">
                    <input type="hidden" class="photoUrl3" name="photoUrl">
                    <span <#if feedback.pic3Url?? && feedback.pic3Url?has_content>class="fix"</#if>></span>
                    <div class="z_file3 z_file">
                        <img style="width:100%;height:100%" src="<#if feedback.pic3Url?? && feedback.pic3Url?has_content>${feedback.pic3Url!''}</#if>"/>
                    </div>
                </div>
            </div>

            <!--遮罩层-->
            <div class="z_mask">
                <!--弹出框-->
                <div class="z_alert" >
                    <p>确定要删除这张图片吗？</p>
                    <p>
                        <span class="z_cancel">取消</span>
                        <span class="z_sure">确定</span>
                    </p>
                </div>
            </div>
        </div>
        </div>
    </div>
    </div>
    </#if>
    <#if feedback?? && feedback.id?has_content && feedback.feedbackType??>
        <#if feedback.feedbackType.type == 6 || feedback.feedbackType.type == 7>
            <div class="item">
                教材名称
                <div class="inner-right-text">
                    ${feedback.bookName!''}
                </div>
            </div>
            <div class="item">
                年级
                <div class="inner-right-text">
                    ${feedback.bookGrade!''}
                </div>
            </div>
            <#if feedback.feedbackType.type == 6>
                <div class="item">
                    单元
                    <div class="inner-right-text">
                        ${feedback.bookUnit!''}
                    </div>
                </div>
            </#if>
            <#if feedback.feedbackType.type == 7>
                <div class="item">
                    覆盖地区
                    <div class="inner-right-text">
                        ${feedback.bookCoveredArea!''}
                    </div>
                </div>
                <div class="item">
                    覆盖学生数
                    <div class="inner-right-text">
                        ${feedback.bookCoveredStudentCount!''}
                    </div>
                </div>
            </#if>
        </#if>
        <div class="item tip">
            反馈内容
            <span style="height:5rem;border:1px solid #eaeaea;word-wrap:break-word" class="content meetingNote" id="" name="meetingContent" data-einfo="请填写反馈内容" placeholder="请点击填写...">${feedback.content!''}</span>
        </div>
        <div class="item">
            附图
            <div class="photp_container">
                <!--    照片添加    -->
                <div class="z_photo">

                    <#if feedback.pic1Url??>
                        <div class="z_addImg pic_out">
                            <img class="img_num" src="${feedback.pic1Url!''}"/>
                        </div>
                    </#if>
                    <#if feedback.pic2Url??>
                        <div class="z_addImg pic_out">
                            <img class="img_num" src="${feedback.pic2Url!''}"/>
                        </div>
                    </#if>
                    <#if feedback.pic3Url??>
                        <div class="z_addImg pic_out">
                            <img class="img_num" src="${feedback.pic3Url!''}"/>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </#if>
    </#if>
</div>

<div class="alert_pic" style="width:100%;height:100%;position:fixed;top:0;display:none;background:rgba(0,0,0,0.60);">
    <img src="" alt="" style="width:100%;margin-top:2.7rem">
</div>
<script>
    <#if feedback?? && feedback.id?has_content>
        var showRight = "";
        var needCallBackFn = false;
        <#else>
        var showRight = "提交";
        var needCallBackFn = true;
    </#if>
    var chooseRole = false;
    $(document).on("click",".js-chooseRole",function(){
        if($(this).html().trim() == "我自己"){
            $('.teacherRole').hide();
            chooseRole = true;
            $('.grateful').removeClass("js-ulItem");
        }else{
            $('.teacherRole').show();
            chooseRole = false;
            $('.grateful').addClass("js-ulItem");
        }

    });
    var arr = [];
    $(document).on('click','.pic_out',function(){
        var pic_src = $(this).find('img').attr('src');
        $('.alert_pic').show().find('img').attr('src',pic_src);
    });
    $(document).on('click','.alert_pic',function(){
       $(this).hide();
    });
    $(document).ready(function(){
        var postData = {};

        $(".z_file1").on("click",function(){
            var data =  {
                uploadUrlPath:"mobile/file/upload.vpage",
                NeedAlbum:true,
                NeedCamera:true,
                uploadPara: {}
            };
            do_external('getImageByHtml',JSON.stringify(data));
            vox.task.setImageToHtml = function(res){
                var resJson = JSON.parse(res);
                if(!resJson.errorCode){
                    /*前端展示*/
                    $(".photoUrl1").val("");
                    setTimeout(function(){
                        if(resJson.fileUrl){
                            var url = resJson.fileUrl;
                            $(".photoUrl1").val(url);
                            $(".z_file1").parent().find('span').addClass('fix');
                            $(".z_file1").find("img").attr("src",url);
                        }else{
                            AT.alert("客户端未获取到图片")
                        }
                    },150);
                }else{
                    AT.alert("客户端出错")
                }
            };
        });
        $(".z_file2").on("click",function(){
            var data =  {
                uploadUrlPath:"mobile/file/upload.vpage",
                NeedAlbum:true,
                NeedCamera:true,
                uploadPara: {}
            };
            do_external('getImageByHtml',JSON.stringify(data));
            vox.task.setImageToHtml = function(res){
                var resJson = JSON.parse(res);
                if(!resJson.errorCode){
                    /*前端展示*/
                    $(".photoUrl2").val("");
                    setTimeout(function(){
                        if(resJson.fileUrl){
                            var url = resJson.fileUrl;
                            $(".photoUrl2").val(url);
                            $(".z_file2").parent().find('span').addClass('fix');
                            $(".z_file2").find("img").attr("src",url);
                        }else{
                            AT.alert("客户端未获取到图片")
                        }

                    },150);
                }else{
                    AT.alert("客户端出错")
                }
            };
        });
        $(".z_file3").on("click",function(){
            var data =  {
                uploadUrlPath:"mobile/file/upload.vpage",
                NeedAlbum:true,
                NeedCamera:true,
                uploadPara: {}
            };
            do_external('getImageByHtml',JSON.stringify(data));
            vox.task.setImageToHtml = function(res){
                var resJson = JSON.parse(res);
                if(!resJson.errorCode){
                    /*前端展示*/
                    $(".photoUrl3").val("");
                    setTimeout(function(){
                        if(resJson.fileUrl){
                            var url = resJson.fileUrl;
                            $(".photoUrl3").val(url);
                            $(".z_file3").parent().find('span').addClass('fix');
                            $(".z_file3").find("img").attr("src",url);
                        }else{
                            AT.alert("客户端未获取到图片")
                        }

                    },150);
                }else{
                    AT.alert("客户端出错")
                }
            };
        });
        showTab($("#selectPartner option:selected").val());
    });
    $(document).on('change','#selectPartner',function(){
        showTab($("#selectPartner option:selected").val());
    });
    $(document).on('click','.fix',function(){
        if(confirm("确定要删除照片吗")){
            $(this).removeClass('fix').next().find('img').attr('src','');
        }
    });
    function showTab(selected) {
        switch (selected) {
            case '7' :
                $('.tab_one').show().find('.js-check').addClass('js-postData');
                $('.tab_one').find('.unit').hide().find('.js-check').removeClass('js-postData');
                $('.tab_one').find('.area').show().find('.js-check').addClass('js-postData');
                $('.tab_one').find('.count').show().find('.js-check').addClass('js-postData');
                $('.tab_three').show().find('.js-check').addClass('js-postData');
                break;
            case '6' :
                $('.tab_one').show()
                $('.tab_one').find('.unit').show().find('.js-check').addClass('js-postData');
                $('.tab_one').find('.area').hide().find('.js-check').removeClass('js-postData');
                $('.tab_one').find('.count').hide().find('.js-check').removeClass('js-postData');
                $('.tab_three').show().find('.js-check').addClass('js-postData');
                break;
            case '0':
                $('.tab_one').hide().find('.js-check').removeClass('js-postData');
                $('.tab_two').hide().find('.js-check').removeClass('js-postData');
                $('.tab_three').hide().find('.js-check').removeClass('js-postData');
                break;

            default:
                $('.tab_one').hide().find('.js-check').removeClass('js-postData');
                $('.tab_two').hide().find('.js-check').removeClass('js-postData');
                $('.tab_three').show().find('.js-check').addClass('js-postData');
                break;

        }
    }

    var submitAble = true;
    var signSuccess = false;
    var imageSuccess = true;
    var callBackFlag = "";
    var vox = vox || {};
    vox.task = vox.task || {};

    var AT = new agentTool();
    AT.cleanAllCookie();

    var schoolLevel = ${schoolLevel!'0'};

    var selectedType = "";
        <#if workTitle??>
        selectedType = "${workTitle!}";
        </#if>
    $(function(){
        //检测提交数据
        var postData = {};

        //跳页面之前保存信息
        var saveInfoToNewPage = function(url){
            var agencyClue = 0;
            if($('[name="isAgencyClue"]>div.the').length != 0){
                agencyClue = $('[name="isAgencyClue"]>div.the').attr("data-opvalue");
            }

            var postDate = {
                //类别
                fbType:$("#selectPartner option:selected").val(),
                //反馈人
                mySelf:chooseRole,
                //是否发消息感谢老师
                noticeFlag:$(".grateful>div.the").data("type"),
                //反馈内容
                content:$(".meetingNote").val(),
                //年级
                bookGrade:$('.gradeLevel').val(),
                //覆盖地区
                bookCoveredArea:$('.part').val(),
                //覆盖学生数
                bookCoveredStudentCount:parseInt($(".studentCount").val()),
                //教材名称
                bookName: $('.bookName').val(),
                //单元
                bookUnit:$('.bookUnit').val(),
                pic1Url : $('.z_file1 img').attr('src'),
                pic2Url : $('.z_file2 img').attr('src'),
                pic3Url : $('.z_file3 img').attr('src')
            };
            $.post("/mobile/feedback/operate/savesession.vpage",postDate,function(res){
                if(res.success){
                    location.href = url;
                }else{
                    AT.alert(res.info);
                }
            });
        };

        if(selectedType.length != 0){
            $('.<#if recordId??><#else>mainTitle</#if>').children().eq(selectedType-11).addClass("the");
        }

        $(".js-chooseTeacher").on("click",function(){
            saveInfoToNewPage("searchteacher.vpage?back=feedbackinfo.vpage");
        });

        $("div.mainTitle>div").on("click",function(){
            $(this).addClass("the").siblings("div").removeClass("the");
        });

        $(".js-signBtn").on("click",function(){
            testLog("sign btn click");
            getSignLocation();
        });

        $("#reSignBtn").on("click",function(){
            $("#confirmSignDialog").hide();
            getSignLocation();
        });

        $("#getSchoolGateImageBtn").on("click",function(){
            $("#confirmSignDialog").hide();
            getSchoolImage();
        });

        $("#close_win").on("click", function () {
            $("#expected_data").hide();
        });
        $(".cancel_btn").on("click", function () {
            $(".schoolRecord-pop").hide();
        });

        $("#selectPartner").on("change",function(){
            var type = $(this).val();
            $('#partner').val(type);
            $('#partnerDisplay').html($('#selectPartner option:selected').text());
        });

        <#if (schoolLevel!0) != 4>
            $("#selectAgencyId").on("change",function(){
                var type = $(this).val();
                $('#agencyId').val(type);
                $('#agencyDisplay').html($('#selectAgencyId option:selected').text());
            });
        </#if>
        $(".js-submitSchoolSRecord").on("click",function(){
            if(submitAble){
                if(checkData()){
                    submitAble = false;
                    var postData = {};
                    $.each($(".js-postData"),function(i,item){
                        postData[item.name] = $(item).val();
                    });
                    //类别
                    postData["fbType"] = $("#selectPartner option:selected").val();
                    //反馈人
                    postData["mySelf"] = chooseRole ;
                    //是否发消息感谢老师
                    postData["noticeFlag"] = $(".grateful>div.the").data("type");
                    //反馈内容
                    postData["content"] = $(".meetingNote").val();
                    //年级
                    postData["bookGrade"]=$(".gradeLevel").val();
                    //覆盖区域
                    postData["bookCoveredArea"]=$(".part").val();
                    //覆盖学生人数
                    postData["bookCoveredStudentCount"]=parseInt($(".studentCount").val());
                    //教材名称
                    postData["bookName"]=$(".bookName").val();
                    //单元
                    postData["bookUnit"]=$(".bookUnit").val();
                    //图片路径
                    postData["pic1Url"] = $('.z_file1 img').attr('src');
                    postData["pic2Url"] = $('.z_file2 img').attr('src');
                    postData["pic3Url"] = $('.z_file3 img').attr('src');
                    $.post("/mobile/feedback/operate/feedbackinfo.vpage",postData, function (res) {
                        submitAble = true;
                        if(res.success){
                            AT.alert("提交成功");
                            try{
                                setTimeout("disMissViewCallBack()",1500);
                            }catch(v){
                                alert(v)
                            }
                        }else{
                            AT.alert(res.info);
                        }
                    })
                }
            }else{
                AT.alert("记录已被提交正在处理,请稍候...");
                submitAble = true;
            }

        });

        //检测提交数据
        var checkData = function () {
            var flag = true;
            if(flag){
                if($("#selectPartner option:selected").val() == '0'){
                    AT.alert('请选择类别');
                    return false;
                }
            }
            $.each($(".js-postData"), function (i, item) {
                postData[item.name] = $(item).val();
                if (!($(item).val())) {
                    AT.alert($(item).data("einfo"));
                    flag = false;
                    return false;
                }
            });


            if (flag) {
                if ($('[name="isAgencyClue"]').hasClass("disabled")) {
                    $('[name="isAgencyClue"]').removeClass("js-ulItem");
                }
                $.each($(".js-ulItem"), function (i, item) {
                    if ($(item).children("div.the").length == 0) {
                        AT.alert($(item).data("einfo"));
                        flag = false;
                        return false;
                    } else {
                        postData[$(item).attr("name")] = $(item).children("div.the").attr("data-opvalue");
                    }
                });
            }
            return flag;
        };

        $("#nextVisitTime").on("click",function(){
            $("#visitDateDialog").show();
        });

        $("#visDateSure").on("click",function(){
            var startVal = $("#nextVisitDate").val();
            $("#nextVisitTimeDisplay").html(startVal);
            $("#nextVisitTime").val(startVal);
            $("#visitDateDialog").hide();
        });

        $("#visDateCancel").on("click",function(){
            $("#visitDateDialog").hide();
        });

        $(".js-ulItem>div").on("click",function(){
            $(this).addClass("the").siblings("div").removeClass("the");
        });

        $("#nextVisitTime").on("change",function(){
            $(".select-date").html($(this).val());
        });


        $("#nextVisitTime").width($("#js-selectDate").outerWidth());
    });
</script>
</@layout.page>