<#import "module.ftl" as temp />
<@temp.page title='myexperience'>
    <div class="w-content">
        <div class="t-prizesCenter-box">
            <div class="my_order_box">
                <#if orderMapList?has_content>
                    <#list orderMapList as c>
                        <div class="my_order_inner_box">
                            <#if c.discount?has_content && c.discount lt 1>
                                <div class="coupon-number JS-couponNumber">${c.discount * 10}折</div>
                            </#if>
                            <div class="my_order_product_box clearfix">
                                <#if c.image??>
                                    <#if c.image?index_of("oss-image.17zuoye.com")!=-1>
                                        <img src="${c.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" class="float_left" />
                                    <#else>
                                        <img src="<@app.avatar href="${c.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" />" class="float_left" />
                                    </#if>
                                <#else>
                                    <img src="<@app.avatar href="${c.image!''}" />" class="float_left" />
                                </#if>
                                <dl class="float_left">
                                    <dt>
                                        <p>${c.productName!''}</p>

                                        <#if c.oneLevelCategoryType?has_content && c.oneLevelCategoryType == 5 && c.couponResource?has_content && c.couponResource != 'DUIBA'>
                                            <p class="pc-time">券码：${c.couponNo!''}</p>
                                        </#if>

                                        <#if c.oneLevelCategoryType?has_content && c.oneLevelCategoryType == 4>
                                            <p class="pc-time">请在手机app端观看课程视频</p>
                                        </#if>

                                        <#if c.oneLevelCategoryType?has_content && c.oneLevelCategoryType == 5 && c.couponResource?has_content && c.couponResource == 'DUIBA'>
                                            <p class="pc-time">去APP端查看兑换结果</p>
                                        </#if>

                                        <#if c.createTime?has_content>
                                            <p class="pc-time">
                                                兑换时间：${c.createTime!''}
                                            </p>
                                        </#if>
                                    </dt>
                                </dl>
                                <#if c.totalPrice?has_content>
                                    <p class="float_left my_order_price_box"><span>价格：</span><strong class="J_red orderPrice">${c.totalPrice}</strong><@ftlmacro.garyBeansText/></p>
                                </#if>
                                <#if c.quantity?has_content>
                                    <p class="float_left pc-number">
                                        <span class="float_left">数量：</span>
                                        <span class="my_order_number float_left">${c.quantity!''}</span>
                                    </p>
                                </#if>
                                <#if currentUser.userType == 1>
                                    <#if c.oneLevelCategoryType?has_content && c.oneLevelCategoryType == 6>
                                        <#if c.used?? && c.used!false>
                                            <a class="float_right usedbtn" href="javascript:void(0);">
                                                <strong>已使用</strong>
                                            </a>
                                        <#else>
                                            <a class="float_right usingbtn JS-useBtn" data-id="${c.productId!''}" <#if c.url?has_content>data-link="${c.url}"</#if> href="javascript:void(0);">
                                                <strong>立即使用</strong>
                                            </a>
                                        </#if>
                                    </#if>
                                <#else>
                                    <#if c.productRebated?has_content>
                                        <#if c.rebated?? && c.rebated!false>
                                            <a class="float_right usedbtn" href="javascript:void(0);">
                                                <strong>已领取成功</strong>
                                            </a>
                                        <#else>
                                            <#--<#if c.detailId?has_content>
                                                <a data-detail_id="${c.detailId!''}" data-detail_name="${c.productName!''}" class="float_right usingbtn rebate_but" href="javascript:void(0);">
                                                    <strong>申请返1000学豆</strong>
                                                </a>
                                            </#if>-->
                                        </#if>
                                    </#if>
                                </#if>
                            </div>
                        </div>
                    </#list>
                <#else>
                    <div class="no_order_box" style=" border: 1px solid #f5e6d6; margin-top: -1px;">
                        <div class="no_order_bg"></div>
                        <p class="btn_box font_twenty">您还没有兑换虚拟奖品呢</p>
                        <p class="btn_box J_light_gray" style="padding-top:6px;">继续加油吧！</p>
                    </div>
                </#if>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            /*$('.rebate_but').on('click', function(){
                var $this = $(this);
                var detailId = $this.data('detail_id');
                var name = $this.data('detail_name');
                if($this.hasClass('loading')) return false;
                $this.addClass('loading');
                $.post('/reward/order/rebate.vpage', {detailId : detailId, name : name}, function(data){
                    if(data.success){
                        $this.hide().after('已领取成功');
                    }
                    $17.alert(data.info);
                    $this.removeClass('loading');
                });
            });*/
            $(".JS-useBtn").on("click",function () {
                var roleTypes = "web_teacher_logs";
                <#if (currentUser.userType) == 3>
                    roleTypes = "web_student_logs";
                </#if>
                YQ.voxLogs({database:roleTypes, module : "m_2ekTvaNe", op : "o_MNgSPacO", s0: $(this).attr("data-id")});

                if ($(this).attr("data-link")) {
                    window.location.href = $(this).attr("data-link");
                }
            });
        })
    </script>
</@temp.page>