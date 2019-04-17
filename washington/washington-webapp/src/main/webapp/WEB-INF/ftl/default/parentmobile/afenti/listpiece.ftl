<#if afentiMathEnglishFlag!false>
    <style>
        html{font:20px/1.5 "Microsoft YaHei",Arial}
        .afenti-expand{margin:0 2rem 1.5rem;height:6.3rem;background:#ffedd4;overflow:hidden;border-radius:.2rem;border:.05rem solid #ffddaf}
        .afenti-expand p{margin:0;padding:0}
        .afenti-expand .left{float:left;width:.8rem;height:6.3rem;background:#ffddaf}
        .afenti-expand .right{cursor:pointer;float:right;width:3.7rem;height:6.3rem;background:#fdb353 url(<@app.link href="public/skin/project/afentidetailapp/images/arrow-right.png"/>) no-repeat center center}
        .afenti-expand .aex-main{float:left}
        .afenti-expand .aex-main .info{padding:1.1rem 0;margin-left:6.5rem;font-size:1.2rem;color:#474747}
        .afenti-expand .aex-main .text-yellow{color:#e48e1e;font-size:1.1rem}
        .afenti-expand .aex-main .text-yellow span{font-size:1.3rem}
        .afenti-expand .gift-icon{float:left;margin:.3rem;width:5.7rem;height:5.7rem;background:url(<@app.link href="public/skin/project/afentidetailapp/images/gift-icon.png"/>) no-repeat}
    </style>
    <div class="afenti-expand">
        <a href="shoppinginfo.vpage?specialProductType=AfentiSuit&sid=${sid!0}&productType=AfentiMath">
            <div class="left"></div>
            <div class="aex-main">
                <div class="gift-icon"></div>
                <div class="info">
                    <p>阿分题英语＋数学，双科提升</p>
                    <p class="text-yellow">双科同购，优惠<span>5-100</span>元</p>
                </div>
            </div>
            <div class="right"></div>
        </a>
    </div>
</#if>
