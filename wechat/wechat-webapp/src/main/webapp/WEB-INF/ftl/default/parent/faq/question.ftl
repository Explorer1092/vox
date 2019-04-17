<#import "../layout.ftl" as faq>
<@faq.page title="自动答疑" pageJs="">
    <style type="text/css">
        .main_title {  font-size: 36px;  line-height: 40px;  padding: 32px 0 28px 40px;  }
        .main_con {  overflow: hidden;  padding: 0 40px 70px;  position: relative;  }
    </style>

    <div class="main">
        <#if id == 568 || id == 569 || id == 570 >
            ${content!''}
        </#if>
        <p class="main_title">${title!''}</p>
        <div class="main_con">${content!''}</div>
    </div>
    <script type="text/javascript">
        function pageLog(){
            require(['logger'], function(logger) {
                logger.log({
                    module: 'faq',
                    op: 'pv_faqquestion'
                })
            })
        }
    </script>
</@faq.page>
