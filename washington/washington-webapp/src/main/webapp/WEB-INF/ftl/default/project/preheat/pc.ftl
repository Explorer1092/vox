<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="新品预热大狂欢" header="hide">
<@app.css href="public/skin/project/preheat/pc/css/preheat.css"/>

<#function hasClazzFlag items=[]>
    <#if (currentUser.userType == 3 && items?seq_contains(currentStudentDetail.clazz.classLevel) )!false>
        <#return true/>
    <#else>
        <#return false/>
    </#if>
</#function>

<div class="preheat-container">
    <div class="preheat-inner">
        <div style="position: absolute; width: 120px; height: 50px; top: 30px; left: 0;">
            <a href="/" style="display: block; width: 100%; height: 100%;"></a>
        </div>
        <div class="p-module module1">
            <dl>
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_icon1.png'/>"></dt>
                <dd>
                    <div class="title">悟空识字</div>
                    <div class="info">电脑版</div>
                </dd>
            </dl>
            <div class="p-tips">大闹天宫、花果山...超有料噢！</div>
            <div class="p-btn">
                <#if (dataMap.WukongShizi)!false>
                    <a href="/student/apps/index.vpage?app_key=WukongShizi&refer=preheat" target="_blank" class="try_btn js-clickVoxLog" data-op="click_shizi_pc">立即试用</a>
                <#else>
                    <a href="javascript:void(0);" class="expect_btn"><#if hasClazzFlag(['1'])!false>敬请期待<#else>仅开放1年级</#if></a>
                </#if>
            </div>
        </div>
        <div class="p-module module2">
            <dl>
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_icon2.png'/>"></dt>
                <dd>
                    <div class="title">酷跑学单词</div>
                    <div class="info">手机版</div>
                </dd>
            </dl>
            <div class="p-tips">和沃克一起开启奇幻探险吧！</div>
            <div class="p-btn">
                <#if (dataMap.GreatAdventure)!false>
                    <a href="/help/download-student-app.vpage?refer=preheat" target="_blank" class="phone_btn">请在手机上试用</a>
                <#else>
                    <a href="javascript:void(0);" class="expect_btn">敬请期待</a>
                </#if>
            </div>
        </div>
        <div class="p-module module3">
            <dl>
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_icon3.png'/>"></dt>
                <dd>
                    <div class="title">悟空拼音</div>
                    <div class="info">电脑版</div>
                </dd>
            </dl>
            <div class="p-tips">悟空学艺，龙宫寻宝......更多关卡等你挑战，还等什么？</div>
            <div class="p-btn">
                <#if (dataMap.WukongPinyin)!false>
                    <a href="/student/apps/index.vpage?app_key=WukongPinyin&refer=preheat" target="_blank" class="try_btn js-clickVoxLog" data-op="click_pinyin_pc">立即试用</a>
                <#else>
                    <a href="javascript:void(0);" class="expect_btn"><#if hasClazzFlag(['1', '2'])!false>敬请期待<#else>仅开放1-2年级</#if></a>
                </#if>
            </div>
        </div>
        <div class="p-module module4">
            <dl>
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_icon4.png'/>"></dt>
                <dd>
                    <div class="title">阿分题数学</div>
                    <div class="info">电脑版</div>
                </dd>
            </dl>
            <div class="p-tips">又想玩游戏又想数学考试有好成绩怎么办？快让阿分题来帮你！</div>
            <div class="p-btn">
                <#--<#if (dataMap.AfentiMath)!false>-->
                <#if (false)!false>
                    <a href="/student/apps/index.vpage?app_key=AfentiMath&refer=preheat" target="_blank" class="try_btn js-clickVoxLog" data-op="click_afenti_pc">立即试用</a>
                <#else>
                    <a href="javascript:void(0);" class="expect_btn">敬请期待</a>
                </#if>
            </div>
        </div>
        <div class="p-module module5">
            <dl>
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_icon4.png'/>"></dt>
                <dd>
                    <div class="title">阿分题数学</div>
                    <div class="info">手机版</div>
                </dd>
            </dl>
            <div class="p-btn">
                <#if (dataMap.AfentiMath)!false>
                <#--<#if (false)!false>-->
                    <a href="/help/download-student-app.vpage?refer=preheat" target="_blank" class="phone_btn">请在手机上试用</a>
                <#else>
                    <a href="javascript:void(0);" class="expect_btn">敬请期待</a>
                </#if>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $(".js-clickVoxLog").on("click", function(){
            var $thisOP = $(this).data("op");

            if( $17.isBlank($thisOP) ){
                return false;
            }

            $17.voxLog({
                module : "huodongyure",
                op : $thisOP
            }, "student");
        });

        $17.voxLog({
            module : "huodongyure",
            op : "studentPCLoad"
        }, "student");
    });
</script>
</@temp.page>