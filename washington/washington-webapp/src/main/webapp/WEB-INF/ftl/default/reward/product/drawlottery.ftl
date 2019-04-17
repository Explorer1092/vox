<#if (currentUser.userType) == 1>

<#if (currentTeacherDetail.isJuniorTeacher())!false>
    <#assign countBean = 50/>
<#else>
    <#assign countBean = 5/>
</#if>

<script id="T:drawlottery" type="text/html">
    <div class="t-rewordShopLottery-box">
        <div class="rs-close">x</div>
        <div class="rs-lottery">
            <ul>
                <li name="lottery" method="1">
                </li>
                <li name="lottery" method="2">
                </li>
                <li name="lottery" method="3">
                </li>
            </ul>
        </div>
        <div class="rs-btn">
            <a href="javascript:void(0);" class="btn-share" style="display: none" id="resubmit" method="">${countBean!}<@ftlmacro.garyBeansText/>再试一次</a>
        </div>
    </div>
</script>

<script type="text/javascript">
    $(function(){

        var beanNum=0;
        <#if (currentUser.userType) == 1>
            beanNum = ${currentTeacherDetail.userIntegral.usable!0};
        <#elseif (currentUser.userType) == 8>
            beanNum = ${currentResearchStaffDetail.userIntegral.usable!0};
        </#if>

        var tipArray={};
        tipArray.box ={
            "big":'<div class="open-big open-big-1"><div class="ob-content"> <p>运气爆棚啦！</p> <p>奖品将在<span>下月20号左右</span></p> <p>寄到您手里(如遇寒暑假，则开学后发货)</p> </div> </div>',
            "small":'<div class="open-small open-small-1"><div class="ob-content"></div></div>'
        };
        tipArray.bean = {
            "big":'<div class="open-big open-big-2"><div class="ob-content"> <p>手气不错</p> <p>获得<span>{#beanNum}</span><@ftlmacro.garyBeansText/></p> </div> </div>',
            "small":'<div class="open-small open-small-2"><div class="ob-content"></div> </div>'
        };
        tipArray.empty = {
            "big": '<div class="open-big open-big-3" ><div class="ob-content"> <p>很遗憾</p> <p>里面是空的</p> </div> </div>',
            "small":'<div class="open-small open-small-3"><div class="ob-content"></div> </div>'
        };
        $(document).off("click","#drawlottery");
        $(document).on("click","#drawlottery",function(){
            var discountPrice = beanNum;
            discountPrice = parseInt(discountPrice);
            if(discountPrice>= ${countBean}){
                $.prompt("<div id='drawlotteryPopupBox'>"+template("T:drawlottery",{})+"</div>",{
                    prefix : "null-popup",
                    buttons : { },
                    classes : {
                        fade: 'jqifade',
                        close: 'w-hide'
                    },
                    loaded:renderLottery
                });
            }
            else{
                $17.alert("每次抽奖需要消耗掉${countBean}个<@ftlmacro.garyBeansText/>，您的<@ftlmacro.garyBeansText/>数不足${countBean}个。")
            }
            YQ.voxLogs({module: "m_2ekTvaNe", op: "o_VZmeaKkE", s0: "${productId!0}", s1: "${(currentUser.userType)!0}"});
        });

        function renderLottery(){
            $(document).off("click","a#resubmit");
            $(document).on("click","a#resubmit",function(){
                var elem = $(this);
                var method = elem.attr("method");
                if(method&&method.indexOf("http:")!==-1){
                    window.location.href = method;
                }
                else{
                    $("#drawlotteryPopupBox").html(template("T:drawlottery",{}));
                    $(document).off("click","li[name='lottery']");
                    $(document).on("click","li[name='lottery']",lotterySubmit);
                }
            });
            $(document).off("click",".rs-close");
            $(document).on("click",".rs-close",function(){
                $.prompt.close();
            });

            function reduceArray(arry,value){
                var result = [];
                for(var i=0;i<arry.length;i++){
                    if(arry[i]!==value){
                        result.push(arry[i]);
                    }
                }
                return result;
            }

            function renderDefault(method,type){
                var map1=["1","2","3"];
                var map2=['box','bean','empty'];
                var resultMap1 = reduceArray(map1,method);
                var resultMap2 = reduceArray(map2,type);
                for(var i=0;i<resultMap1.length;i++){
                    $("li[method='"+resultMap1[i]+"']").html(tipArray[resultMap2[i]].small);
                }
            }

            function lotterySubmit(){
                var curElement = $(this);
                var method = $(this).attr("method");
                var productId = ${productId!0};
                var skuId = $("a#skusId.active").attr("data-skus_id");

                if(!skuId){
                    productId = ${(wishDetail.productId)!0};
                    skuId= ${(wishDetail["skus"][0].id)!0};
                }

                var reqParams = {
                    "productId":productId,
                    "skuId":skuId
                };
                var type;
                $(document).off("click","li[name='lottery']");
                $.ajax({
                    url:"/reward/order/openmoonlightbox.vpage",
                    type:"POST",
                    data:reqParams,
                    success:function(data){
                        var result = data.success;
//                        result =true;
                        if(result){
                            var type = data.box&&data.box.awardId;
//                            type= 4;
                            switch(type){
                                case 1:
                                    curElement.html(tipArray.box.big);
                                    $("#resubmit").html("分享到论坛")
                                    $("#resubmit").attr("method","http://www.17huayuan.com/forum.php?mod=post&action=newthread&fid=2");
                                    type="box";
                                    break;
                                case 2:
                                    curElement.html(tipArray.bean.big.replace("{#beanNum}",${countBean!}));
                                    type="bean";
                                    $("#resubmit").html("${countBean!}<@ftlmacro.garyBeansText/>试一次");
                                    $("#resubmit").attr("method","");
                                    break;
                                case 3:
                                    curElement.html(tipArray.bean.big.replace("{#beanNum}",1<#if (currentTeacherDetail.isJuniorTeacher())!false>*10</#if>));
                                    type="bean";
                                    $("#resubmit").html("${countBean!}<@ftlmacro.garyBeansText/>再试一次");
                                    $("#resubmit").attr("method","");
                                    break;
                                case 4:
                                    curElement.html(tipArray.empty.big);
                                    type="empty";
                                    $("#resubmit").html("${countBean!}<@ftlmacro.garyBeansText/>试一次");
                                    $("#resubmit").attr("method","");
                                    break;
                            }
                            $("#resubmit").show();
                            renderDefault(method,type);
                        }else{
                            var infoBtn = {"知道了" : true};
                            var infoUrl = function(){$.prompt.close();};

                            if(data['authentication']){
                                infoBtn = {"去认证" : true};
                                infoUrl = function(){
                                    window.open('${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage',"_blank");
                                };
                            }else if(data['bindMobile']){
                                infoBtn = {"去填写" : true};
                                infoUrl = function(){
                                    window.open('${(ProductConfig.getUcenterUrl())!''}/teacher/center/securitycenter.vpage',"_blank");
                                };
                            }else if(data['address']){
                                infoBtn = {"去填写" : true};
                                infoUrl = function(){
                                    window.open('${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage',"_blank");
                                };
                            }

                            $(document).off("click","li[name='lottery']");
                            $(document).on("click","li[name='lottery']",lotterySubmit);
                            $.prompt(data.info, {
                                title : "",
                                buttons : infoBtn,
                                submit : infoUrl
                            });

                        }
                    },error:function(data){
                        $17.alert(data.info);
                        $(document).off("click","li[name='lottery']");
                        $(document).on("click","li[name='lottery']",lotterySubmit);
                    }
                });
            }
            $(document).off("click","li[name='lottery']");
            $(document).on("click","li[name='lottery']",lotterySubmit);
        }
    });
</script>
</#if>