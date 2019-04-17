<#import "../layout.ftl" as faq>
<@faq.page title="自动答疑" pageJs="">
<style>
    html,body{ height: 100%; width: 100%; font: 0.8rem/1.125 "Microsoft YaHei",Arial; color: #333; list-style: none;}
    html, body, h1, h2, h3, h4, h5, h6, p, ul, li{ margin: 0; padding: 0;}
    html{ font-size: 18px;}
    .face-register-head{background-color: #189cfb; color: #fff; font-size: 2rem; padding: 0.5rem 0.9rem;text-align: center;}
    .face-area-list{ background-color: #fff; width: 100%;}
    .face-area-list .first-list{width: 100%; text-align: center; }
    .face-area-list .first-list ul{display: block; z-index: 2; background-color: #fff; width: 100%;}
    .face-area-list .first-list ul li{ cursor: pointer; background: url('/public/images/help/arrow-right.png') no-repeat right center; background-size: auto 50%; padding: 0.625rem 0; background-color: #fff; width: 100%; text-align: left; border-bottom: 1px solid #e2e2e2; text-indent: 1.5rem; line-height: 40px;}
    .face-area-list .first-list ul li a{ color: #666; text-decoration: none;font-size: 1.3rem}
    .foot-boot-box{ position: fixed; height: 2rem; width: 100%; bottom: 0; left: 0;}
    .foot-boot-box .back{ background-color: #666; opacity: 0.6; width: 100%; padding: 1rem; position: absolute; bottom: 0; left: 0;}
    .foot-boot-box .icon-up{ background: url('/public/images/help/foot-arrow.png') center center no-repeat; background-size: auto 100%; height: 1.3rem; display: block; width: 100%; position: absolute; top: 0.5rem;}
</style>
<!--头部信息-->
<div class="face-register-head">
    <p>${(catalog.name)!''}</p>
</div>
<div class="face-area-list">
    <div class="first-list">
        <ul>
            <#if questions??>
                <#list questions as question>
                    <li onclick="location.href='/faq/question.vpage?id=${question.id!0}'">
                        <a href="/faq/question.vpage?id=${question.id!0}">
                            <span class="place">${question.title!0}</span>
                        </a>
                    </li>
                </#list>
            </#if>
        </ul>
    </div>
</div>
<div class="foot-boot-box">
    <div class="back"></div>
    <div class="icon-up"></div>
</div>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'faq',
                op: 'pv_faqcatalog'
            })
        })
    }
</script>
</@faq.page>
