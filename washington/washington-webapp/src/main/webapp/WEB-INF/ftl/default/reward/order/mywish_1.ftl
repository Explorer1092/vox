<#import "module.ftl" as temp />
<@temp.page title='mywish'>
    <#if wishDetail?has_content>
    <div class="my_wish_box clearfix" style="margin-top:20px;">
        <div class="my_wish_img float_left">
            <i></i>
            <#if wishDetail.image??>
                <#if wishDetail.image?index_of("oss-image.17zuoye.com")!=-1>
                    <img src="${wishDetail.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" />
                <#else>
                    <img src="<@app.avatar href="${wishDetail.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" />" />
                </#if>
            <#else>
                <img src="<@app.avatar href="${wishDetail.image!''}" />" />
            </#if>
        </div>
        <#if temp.currentUserType == 'STUDENT'>
            <#assign userIntegralftl = currentStudentDetail.userIntegral.usable />
        <#elseif temp.currentUserType == 'TEACHER'>
            <#assign userIntegralftl = currentTeacherDetail.userIntegral.usable />
        <#elseif temp.currentUserType == 'RSTAFF'>
            <#assign userIntegralftl = currentResearchStaffDetail.userIntegral.usable />
        </#if>
        <div class="my_wish_msg_box float_right">
            <p class="title">${wishDetail.productName!''}</p>
            <div class="my_wish_instruction">
                <p class="clearfix">
                    <span class="float_left">已集齐：</span>
                    <strong class="float_left J_red" id="alreadyHaveBeans">${userIntegralftl}</strong>
                    <span class="float_left" style="margin-right:20px;"><@ftlmacro.garyBeansText/></span>
                    <#if userIntegralftl - wishDetail.price lt 0>
                        <span class="float_left">还需要：</span>
                        <strong class="float_left J_red" id="needBeans">${wishDetail.price - userIntegralftl}</strong>
                        <span class="float_left"><@ftlmacro.garyBeansText/></span>
                    </#if>
                </p>

                <div class="my_wish_msg_process">
                    <div class="my_wish_process_bg">
                        <div class="my_wish_status_box">
                            <div class="my_wish_status ProgressBarAnimate" style="width:0%;">
                                <div class="my_wish_status_inner"></div>
                                <div class="bean_point have_beans"><i class="J_sprites have_beans_bg"></i><p class="showIntegralNum">${userIntegralftl}<@ftlmacro.garyBeansText/></p></div>
                            </div>
                        </div>
                        <div class="bean_point need_beans"><p>${wishDetail.price!''}<@ftlmacro.garyBeansText/></p><i class="J_sprites need_beans_bg"></i></div>
                    </div>
                </div>
            </div>
            <p class="btn_box">
                <#if  (temp.currentUserType == "TEACHER" && currentTeacherDetail.isPrimarySchool())!false>
                    <#if wishDetail.price?? && wishDetail.price gt 4>
                        <a href="javascript:void(0);" class="J_btn" style="background-color: #ff6f48; margin-right: 15px; padding:15px 20px" id="drawlottery">${((currentTeacherDetail.isJuniorTeacher())!false)?string("50", "5")}<@ftlmacro.garyBeansText/>试手气</a>
                    </#if>
                </#if>
                <#if userIntegralftl - wishDetail.price lt 0>
                    <a href="javascript:void(0);" class="J_btn_disabled" title="你需要足够学豆才能实现愿望！">我要兑换</a>
                    <a id="w_delete_but" href="javascript:void(0);" class="delete_btn">删 除</a>
                <#else>
                    <a id="w_exchange_but" href="javascript:void(0);" class="J_btn">我要兑换</a>
                    <a id="w_delete_but" href="javascript:void(0);" class="delete_btn">删 除</a>
                </#if>
            </p>
        </div>
    </div>

    <script id="t:selectProductType" type="text/html">
        <div>
            <dl class="clearfix">
                <dt class="J_deep_red btn_box font_twenty" style="border-bottom:1px solid #f0f0f0; padding:10px 0 20px 0;">请选择你喜欢的款式吧！</dt>
                <dd id="p_style_list_box" class="select_style clearfix" style="padding:38px 70px 0px 70px;">
                    <#list wishDetail.skus as sk>
                        <a data-skus_id="${sk.id!''}" <#if sk_index == 0>class="active"</#if> href="javascript:void(0);">${sk.skuName!''}</a>
                    </#list>
                </dd>
            </dl>
            <p class="btn_box" style="padding:10px 0;">
                <a class="J_btn border_radius" href="javascript:void(0);" id="submitSelectProductTypeBut" style="font-size:18px; padding:14px 50px;">确定</a>
            </p>
        </div>
    </script>

    <script type="text/javascript">
        function addOrderById(skusId){
            var data = {
                wishOrderId : '${(wishDetail.wishOrderId)!''}',
                productId : '${(wishDetail.productId)!''}',
                skuId : skusId
            };
            $.post('/reward/order/achievewishorder.vpage', data, function(data){
                if(data.success){
                    $.prompt('奖品兑换成功', {
                        title : "",
                        buttons : {"去查看" : true},
                        submit : function(e,v){
                            location.href = '/reward/order/myorder.vpage';
                        }
                    });
                }else{
                    var infoBtn = {"知道了" : true};
                    var infoUrl = function(){
                        $.prompt.close();
                    };
                    if(!$17.isBlank(data.bindMobile)){
                        infoBtn = {"去绑定" : true};
                        infoUrl = function(){
                            <#if temp.currentUserType == 'STUDENT'>
                                window.open('${(ProductConfig.getUcenterUrl())!}/student/center/account.vpage?updateType=mobile',"_blank");
                            <#elseif temp.currentUserType == 'TEACHER'>
                                window.open('${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/securitycenter.vpage',"_blank");
                            <#elseif temp.currentUserType == 'RSTAFF'>
                                window.open('/rstaff/center/edit.vpage',"_blank");
                            </#if>
                        };
                    }

                    if(!$17.isBlank(data.authentication)){
                        infoBtn = {"去认证" : true};
                        infoUrl = function(){
                            window.open('${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage',"_blank");
                        };
                    }

                    if(!$17.isBlank(data.address)){
                        infoBtn = {"去填写" : true};
                        infoUrl = function(){
                            <#if temp.currentUserType == 'TEACHER'>
                                window.open("${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage","_blank");
                            <#elseif temp.currentUserType == 'RSTAFF'>
                                window.open('/rstaff/center/edit.vpage',"_blank");
                            </#if>

                        };
                    }
                    $.prompt(data.info, {
                        title : "",
                        buttons : infoBtn,
                        submit : infoUrl
                    });
                }
            });
        }

        //进度条动态效果
        function ProgressBarAnimate(currentIntegral){
            var wishDetailPrice = '${wishDetail.price}';
            if(currentIntegral/wishDetailPrice >= 1 ){
                $('.ProgressBarAnimate').css({'width' : '100%'});

            }else{
                $('.ProgressBarAnimate').css({'width' : currentIntegral/wishDetailPrice *100 +"%"});
            }
        }
        $(function(){
            //进度条初始化
            ProgressBarAnimate('${userIntegralftl}');

            //我要兑换
            $("#w_exchange_but").on('click', function(){
                <#if ProductDevelopment.isStagingEnv()>
                    $17.alert("奖品中心尚未开放，敬请期待");
                    return false;
                </#if>
                //库存为空时弹窗提示
                var wishDetail=${json_encode(wishDetail)},num=0;
                for(var i=0;i<wishDetail.skus.length;++i){
                    num+=wishDetail.skus[i].inventorySellable;
                }
                if(num){
                    //当“选择款式”只有一种时，不弹窗提示，当大于一种时，弹框选择
                    <#if wishDetail?has_content>
                        <#if wishDetail.skus?size == 1>
                            addOrderById("${wishDetail.skus[0].id!''}");
                        <#else>
                            $.prompt(template("t:selectProductType",{}), {
                                title : "",
                                buttons : {},
                                loaded : function(){
                                    $("#submitSelectProductTypeBut").on('click', function(){
                                        var skusId = $("#p_style_list_box a.active").data('skus_id');
                                        addOrderById(skusId);
                                    });
                                }
                            });
                        </#if>
                    </#if>
                }else{
                    $.prompt("奖品数量不足！",{
                        buttons:{"知道了":false}
                    });
                }

            });

            //删除愿望盒
            $("#w_delete_but").on('click', function(){
                var wishOrderId = "${(wishDetail.wishOrderId)!''}";
                $.post('/reward/order/removewishorder.vpage', {wishOrderId : wishOrderId}, function(data){
                    if(data.success){
                        $.prompt('愿望盒奖品已删除', {
                            buttons: { "知道了": true },
                            position:{width : 400},
                            submit : function(){
                                location.reload();
                            }
                        });
                    }else{
                        $.prompt(data.info,{
                            buttons: {'知道了':false  },
                            focus: 0,
                            submit:function(){
                                location.reload();
                            }
                        });
                    }
                });
            });

            //开通效果预览
            $(".animateBut").on('click',function(){
                var $this = $(this);
                var integral = $(this).data('integral');
                var userIntegral = '${userIntegralftl}';
                var total = integral*1 + userIntegral*1;
                var box = $('.showIntegralNum');
                var text_box = $('#alreadyHaveBeans');
                var need_box = $("#needBeans");
                ProgressBarAnimate(total);
                box.html(total+"<@ftlmacro.garyBeansText/>");
                text_box.html(total);
                if(total-${wishDetail.price!''} >= 0){
                    need_box.html(0);
                }else{
                    need_box.html(${wishDetail.price!''}-total);
                }
                if($this.hasClass('loading')){return false}
                $this.addClass('loading');
                setTimeout(function(){
                    ProgressBarAnimate(userIntegral*1);
                    box.html(userIntegral*1+"<@ftlmacro.garyBeansText/>");
                    text_box.html(userIntegral*1);
                    need_box.html(${wishDetail.price!''});
                    $this.removeClass('loading');
                },1000);
            });
        });
    </script>

    <#else>
    <div class="my_wish_box" style="margin-top:20px;">
        <div class="no_wish_bg"></div>
        <p class="font_twenty J_deep_gray no_wish_text">愿望盒里还没有添加奖品哦</p>
        <p class="J_light_gray no_wish_text">积累${temp.integarlType!''}，实现愿望！</p>
        <#if !(temp.currentUserType == 'STUDENT' && ((currentStudentWebGrayFunction.isAvailable("Reward", "OfflineShiWu",true))!false))>
            <p class="btn_box go_add"><a href="/reward/product/exclusive/index.vpage" class="J_blue_btn">去添加</a></p>
        </#if>
    </div>
    </#if>

    <#if temp.currentUserType == 'STUDENT'>
    <div class="my_wish_speed_box">
        <div class="my_wish_speed_bg"></div>
        <div class="clearfix">
            <div class="my_wish_speed_left">
                <div class="my_task_speed_box">
                    <p class="title">完成学习任务加速</p>
                    <ul>
                        <li>
                            <i class="float_left">1</i>
                            <div class="float_left my_task_content_box">
                                <p class="clearfix"><a class="float_left" href="/student/index.vpage" target="_blank">完成一次作业，最多获得</a><strong class="float_right J_red">+10学豆</strong></p>
                                <div class="J_sprites dotted_border"></div>
                            </div>
                        </li>
                        <!-- 小于二年级不显示 -->
                        <#if (currentStudentDetail.getClazzLevelAsInteger()?? && currentStudentDetail.getClazzLevelAsInteger() gt 2)>
                            <li>
                                <i class="float_left">2</i>
                                <div class="my_task_content_box float_left">
                                    <p class="clearfix"><a class="float_left" href="/student/index.vpage" target="_blank">完成一次测验，最多获得</a><strong class="float_right J_red">+10学豆</strong></p>
                                    <div class="J_sprites dotted_border"></div>
                                </div>
                            </li>
                        </#if>
                        <li>
                            <i class="float_left">3</i>
                            <div class="my_task_content_box float_left">
                                <p class="clearfix"><a class="float_left" href="/student/learning/index.vpage" target="_blank">补做一次作业，最多获得</a><strong class="float_right J_red">+1学豆</strong></p>
                                <div class="J_sprites dotted_border"></div>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <#elseif temp.currentUserType == 'TEACHER'>
    <#--<div class="my_wish_speed_box">
        <div class="my_wish_speed_bg"></div>
        <div class="clearfix">
            <div class="my_wish_speed_left">
                <div class="my_task_speed_box">
                    <p class="title">愿望攻略</p>
                    <ul>
                        <li>
                            <i class="float_left">1</i>
                            <div class="float_left my_task_content_box">
                                <p class="clearfix"><a class="float_left" href="/teacher/invite/index.vpage" target="_blank">邀请同校老师使用，最多获得</a><strong class="float_right J_red">+100 园丁豆</strong></p>
                                <div class="J_sprites dotted_border"></div>
                            </div>
                        </li>
                        <li>
                            <i class="float_left">2</i>
                            <div class="my_task_content_box float_left">
                                <p class="clearfix"><a class="float_left" href="/teacher/invite/index.vpage" target="_blank">邀请其他学校老师使用，最多获得 </a><strong class="float_right J_red">+500 园丁豆</strong></p>
                                <div class="J_sprites dotted_border"></div>
                            </div>
                        </li>
                        <li>
                            <i class="float_left">3</i>
                            <div class="my_task_content_box float_left">
                                <p class="clearfix"><a class="float_left" href="/teacher/homework/list.vpage" target="_blank">每周布置2次作业，更可享受奖品 </a><strong class="float_right J_red">9折优惠</strong></p>
                                <div class="J_sprites dotted_border"></div>
                            </div>
                        </li>
                    </ul>

                </div>
            </div>
        </div>
    </div>-->
    </#if>
    <#include "../product/drawlottery.ftl">
<script type="text/javascript">
    $(function(){
        YQ.voxLogs({module: "m_2ekTvaNe", op: "o_qwIRBuCn", s0:"${(wishDetail.productId)!0}", s1: "${(currentUser.userType)!0}"});
    });
</script>
</@temp.page>