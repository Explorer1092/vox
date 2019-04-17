<#function hasClazzFlag items=[]>
    <#if (currentUser.userType == 3 && items?seq_contains(currentStudentDetail.clazz.classLevel) )!false>
        <#return true/>
    <#else>
        <#return false/>
    </#if>
</#function>
<div class="preheatActivity-box">
    <div class="pa-banner1"></div>
    <div class="pa-bg1">
        <div class="pa-column">
            <dl>
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_phone2.png'/>"></dt>
                <dd>
                    <div class="pa-title">阿分题数学</div>
                    <div class="pa-info">又想玩游戏又想数学考试有好成绩怎么办？快让阿分题来帮你！</div>
                    <div class="pa-content">
                        <div class="image"><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_ico1.png'/>"></div>
                        <div class="btn">
                        <#if (dataMap.AfentiMath)!false>
                        <#--<#if (false)!false>-->
                            <a href="javascript:void(0);" class="trial_btn js-clickVoxLog" data-op="click_afenti_app">去自学乐园使用</a>
                        <#else>
                            <a href="javascript:void(0);" class="expect_btn">敬请期待</a>
                        </#if>
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
        <div class="pa-column">
            <dl class="list1">
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_phone1.png'/>"></dt>
                <dd>
                    <div class="pa-title">酷跑学单词</div>
                    <div class="pa-info">进入沃克的世界，跟沃克一起开启奇幻探险吧！</div>
                    <div class="pa-content">
                        <div class="image"><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_ico2.png'/>"></div>
                        <div class="btn">
                        <#if (dataMap.GreatAdventure)!false>
                            <a href="javascript:void(0);" class="use_btn js-clickVoxLog" data-op="click_kupao_app">立即使用</a>
                        <#else>
                            <a href="javascript:void(0);" class="expect_btn">敬请期待</a>
                        </#if>
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
        <div class="pa-column">
            <dl class="list2">
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_pc1.png'/>"></dt>
                <dd>
                    <div class="pa-title">悟空识字</div>
                    <div class="pa-info">花果山、大闹天宫、三打白骨精…听着就很有料哦！</div>
                    <div class="pa-content">
                        <div class="image"><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_ico3.png'/>"></div>
                        <div class="btn">
                            <#if (dataMap.WukongShizi)!false>
                                <a href="javascript:void(0);" class="trial_btn js-clickVoxLog" data-op="click_shizi_app">请在电脑上试用</a>
                            <#else>
                                <a href="javascript:void(0);" class="expect_btn"><#if hasClazzFlag(['1'])!false>敬请期待<#else>仅开放1年级</#if></a>
                            </#if>
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
        <div class="pa-column">
            <dl class="list2 list-differ1">
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_pc2.png'/>"></dt>
                <dd>
                    <div class="pa-title">悟空拼音</div>
                    <div class="pa-info">悟空学艺，龙宫寻宝…更多关卡等你挑战，还等什么？</div>
                    <div class="pa-content">
                        <div class="image"><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_ico4.png'/>"></div>
                        <div class="btn">
                        <#if (dataMap.WukongPinyin)!false>
                            <a href="javascript:void(0);" class="trial_btn js-clickVoxLog" data-op="click_pinyin_app">请在电脑上试用</a>
                        <#else>
                            <a href="javascript:void(0);" class="expect_btn"><#if hasClazzFlag(['1', '2'])!false>敬请期待<#else>仅开放1-2年级</#if></a>
                        </#if>
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
        <div class="pa-column">
            <dl class="list2 list-differ2">
                <dt><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_pc3.png'/>"></dt>
                <dd>
                    <div class="pa-title">阿分题数学</div>
                    <div class="pa-info">又想玩游戏又想数学考试有好成绩怎么办？快让阿分题来帮你！手机版电脑版都有哦！</div>
                    <div class="pa-content">
                        <div class="image"><img src="<@app.link href='public/skin/project/preheat/pc/images/preheat_ico1.png'/>"></div>
                        <div class="btn">
                        <#--<#if (dataMap.AfentiMath)!false>-->
                        <#if (false)!false>
                            <a href="javascript:void(0);" class="trial_btn js-clickVoxLog" data-op="click_exam_app">请在电脑上试用</a>
                        <#else>
                            <a href="javascript:void(0);" class="expect_btn">敬请期待</a>
                        </#if>
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
    </div>
</div>