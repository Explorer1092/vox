<@app.css href="public/skin/project/afenti/globalmath/skin.css"/>
<div class="t-gmcMatch-box">
    <div class="bgs">
        <div class="bg01"></div>
        <div class="bg02"></div>
        <div class="bg03">
            <div class="container">
                <div class="c-top">
                    <div class="time"><p class="tag">比赛时间</p>2016年<span>3</span>月<span>25</span>日17:00~<span>27</span>日（周日）23:00</div>
                    <div class="time"><p class="tag">报名截止时间</p>2016年3月27日（比赛当天）</div>
                </div>
                <div class="c-mid">
                    <h1>标准版参赛价格58元</h1>
                    <div class="cm-title">现在报名就<span>送</span>价值28元的<span>趣味数学训练营</span>课程<br><span>让你赢在起跑线上！</span></div>
                    <div class="cm-tips">请根据自己的年级或学习程度，选择下面的一个课程，点击按钮报名。</div>
                    <div class="cm-column">
                        <div class="cm-left"><img src="<@app.link href="public/skin/project/afenti/globalmath/match-gmc-image.png"/>"></div>
                        <div class="cm-right">
                            <ul>
                                <li data-productid="801" data-item="122" class="js-selectProduct">
                                    <label for="122">
                                        <input type="radio" name="global" id="122" class="choose" <#if (stemItem == "122")!false>checked="checked"</#if> <#if stemItem?has_content>disabled="disabled"</#if>>
                                        <div class="info">初露头角篇 二段<br><span>（适合1～2年级）</span></div>
                                    </label>
                                </li>
                                <li data-productid="801" data-item="132" class="js-selectProduct">
                                    <label for="132">
                                        <input type="radio" name="global" id="132" class="choose" <#if (stemItem == "132")!false>checked="checked"</#if> <#if stemItem?has_content>disabled="disabled"</#if> >
                                        <div class="info">技压群芳篇 二段<br><span>（适合3～4年级）</span></div>
                                    </label>
                                </li>
                                <li data-productid="801" data-item="142" class="js-selectProduct">
                                    <label for="142">
                                        <input type="radio" name="global" id="142" class="choose" <#if (stemItem == "142")!false>checked="checked"</#if> <#if stemItem?has_content>disabled="disabled"</#if>>
                                        <div class="info">独步天下篇 二段<br><span>（适合 5～6 年级）</span></div>
                                    </label>
                                </li>
                            </ul>
                        </div>
                    </div>
                    <div class="cm-btn">
                        <#if gmcBought!false>
                            <a href="/redirector/apps/go.vpage?app_key=GlobalMath" target="_blank" class="standard-btn js-voxLog" data-op="buy"></a>
                        <#else>
                            <a href="javascript:void(0);" class="standard-btn js-voxLog" data-op="buy" id="buy"></a>
                        </#if>
                    </div>
                </div>
                <div class="c-foot">
                    <a href="/redirector/apps/go.vpage?app_key=GlobalMath" target="_blank" class="trial-btn js-voxLog" data-op="try">试用版报名/参赛</a>
                </div>
            </div>
        </div>
        <div class="bg04"></div>
        <div class="bg05"></div>
        <div class="bg06"></div>
    </div>
</div>
<form action="?" method="post" id="frm">
    <input type="hidden" name="p" value="0"/>
    <input type="hidden" name="productId" value="801"/>
    <input type="hidden" name="stemItem" value="${stemItem!}"/>
</form>
<script type="text/javascript">
    $(function(){
        var $frm = $("#frm");

        //选择购买类型
        $(document).on("click", ".js-selectProduct", function(){
            var $this = $(this);

            $frm.find("[name='p']").val( $this.index() );
            $frm.find("[name='stemItem']").val( $this.attr("data-item") );
        });

        //submit buy
        $(document).on("click", "#buy", function(){
            var $this = $(this);

            if($this.hasClass("getOrange_gray")){
                return false;
            }else{
                if($frm.find("[name='p']").val() == "" || $frm.find("[name='stemItem']").val() == ""){
                    $17.alert("请选择需要购买的类型");
                    return false;
                }

                $frm.submit();
            }
        });

        $(document).on("click", ".js-voxLog", function(){
            $17.voxLog({
                module : "globalmathModule",
                op : $(this).attr("data-op")
            }, "student");
        });
    });
</script>

