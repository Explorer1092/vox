<#import "../layout.ftl" as layout>
<@layout.page title="我的证书">
    <@sugar.capsule css=['chipsAd'] />

<style>
    html, body, input{
        font: inherit;
    }
    .awardDetail .awardIcon01 {
        margin: 2rem auto 4rem;
        display: block;
        width: 12.05rem;
        height: 3.075rem;
    }
    .awardDetail  .username{
        color: #090909;
        text-align: center;
        margin-top: 0.5rem;
    }
</style>

<div class="awardCertificate" id="get_certificate">
    <div class="awardBox">
        <div class="awardDetail">
            <p class="username">${userName!}</p>
            <img class="awardIcon01" src="/public/images/parent/chips/get_certificate_txt.png" alt="">
            <div class="awardFoot">
                <p class="awardDate" id="time"></p>
                <p>17 Tech</p>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    function time(){
        var myDate = new Date();
        var y = myDate.getFullYear();
        var m = myDate.getMonth();
        var d = myDate.getDate();
        return y + "-" + (m+1) + "-" + d;
    }
    document.getElementById("time").innerHTML = time()

    function pageLog(){
        require(['logger'], function(logger) {
            // 证书页_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'certificate_load'
            })
        })
    }
</script>

</@layout.page>

