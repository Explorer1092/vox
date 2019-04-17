<#import 'layout.ftl' as parentRewardTem>
<@parentRewardTem.p_reward pageName='index'>
    <#--发表心愿-->
    <div class="parent-time-line-box">
        <dl class="first">
            <dt>
                <span class="wish-icon icon-add"></span>
                <span class="big-arrow">◀</span>
            </dt>
            <dd>
                <div class="parent-time-wish" >
                    <div class="pt-title">
                        ${.now?string("yyyy年MM月dd日")}
                    </div>
                    <div class="pt-content">
                        <div class="pt-txt">
                            <i class="icon-simple-icon"></i>
                            <#if canMakeWish!false>
                                <div class="p-txt">
                                    <label for="wishContentInt" style="position: absolute; top: 10px; left: 10px; cursor: text; font-size: 14px;">请在此输入你的心愿…</label>
                                    <textarea class="v-wishContent" id="wishContentInt" maxlength="200"></textarea>
                                </div>
                                <a class="parent-btn v-submitWish" href="javascript:void(0);"><span class="btn-in">提交心愿</span></a>
                            <#else>
                                <div class="p-txt">
                                    <label for="wishContentInt" style="position: absolute; top: 10px; left: 10px; font-size: 14px;">本周已经许愿</label>
                                    <textarea class="v-wishContent" readonly="readonly"></textarea>
                                </div>
                                <a class="parent-btn parent-btn-dis" href="javascript:void(0);"><span class="btn-in">已提交</span></a>
                            </#if>
                            <div class="p-hot v-textScrollInfo">
                                <ul>
                                    <li>
                                        <span>有同学许愿：<strong>去欢乐谷</strong></span>
                                        <span>有同学许愿：<strong>买书</strong></span>
                                        <span>有同学许愿：<strong>吃烧烤</strong></span>
                                    </li>
                                    <li>
                                        <span>有同学许愿：<strong>出去旅游</strong></span>
                                        <span>有同学许愿：<strong>去游泳</strong></span>
                                        <span>有同学许愿：<strong>买手机</strong></span>
                                    </li>
                                    <li>
                                        <span>有同学许愿：<strong>看电影</strong></span>
                                        <span>有同学许愿：<strong>买滑板</strong></span>
                                        <span>有同学许愿：<strong>买玩具</strong></span>
                                    </li>
                                    <li>
                                        <span>有同学许愿：<strong>玩游戏</strong></span>
                                        <span>有同学许愿：<strong>吃冰激凌</strong></span>
                                    </li>
                                </ul>
                            </div>
                            <script type="text/javascript">
                                $(function(){
                                    $(".v-textScrollInfo").textScroll({
                                        line: 1,
                                        speed: 1000,
                                        timer : 3000
                                    });
                                });
                            </script>
                        </div>
                    </div>
                </div>
            </dd>
        </dl>
    </div>
    <#include "timeline.ftl"/>
<script type="text/javascript">
    $(function(){
        //提交心愿
        $(document).on("click", ".v-submitWish", function(){
            var $this = $(this);
            var $wishContent = $(".v-wishContent");
            var $wishVal = $wishContent.val();

            if($this.hasClass("dis")){
                return false;
            }

            if($17.isBlank($wishVal)){
                $wishContent.addClass("w-int-error");
                return false;
            }

            if($wishVal.length > 200){
                $17.alert("请输入200以内的字符");
                return false;
            }

            $this.addClass("dis");
            $.post("/student/parentreward/makewish.vpage", { wish : $wishVal}, function(data){
                if(data.success){
                    $this.find("span").text("已提交");
                    $17.alert("已经提交请到家长通查看");
                }else{
                    $this.removeClass("dis");
                    $17.alert(data.info);
                }
            });

            $17.tongji('家长奖励-点击-提交心愿');
        });

        $(document).on("keyup", ".v-wishContent", function(){
            var $this = $(this);

            if( $17.isBlank($this.val()) ) {
                $this.siblings("label").show();
            }else{
                $this.siblings("label").hide();
            }
            $this.removeClass("w-int-error");
        });
    });
</script>
</@parentRewardTem.p_reward>