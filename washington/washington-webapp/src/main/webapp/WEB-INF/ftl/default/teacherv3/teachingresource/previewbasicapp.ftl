<#import "layout.ftl" as shell />
<@shell.page show="main">
<style type="text/css">
    iframe{
        position: absolute;
        left : 50%;
        top : 50%;
        transform: translate(-50%,-50%);
        -webkit-transform: translate(-50%,-50%);
        -moz-transform: translate(-50%,-50%);
        -o-transform: translate(-50%,-50%);
        max-width: 100%;
        max-height: 100%;
    }
</style>
<div id="basicAppIframe">
    <iframe class="vox17zuoyeIframe" v-bind:src="url" width="100%" marginwidth="0" height="100%" marginheight="0" scrolling="no" frameborder="0"></iframe>
</div>
<script type="text/javascript">
    $(function(){
        var paramObj = {
            qids : $17.getQuery("qids"),
            lessonId : $17.getQuery("lessonId"),
            practiceId : $17.getQuery("practiceId"),
            fromModule : ""
        };
        new Vue({
            el : "#basicAppIframe",
            data : {
                url : "/flash/loader/newselfstudywiththirdparty.vpage?" + $.param(paramObj)
            },
            created: function () {
                var vm = this;
                $17.info("preview basicapp created....");
                $17.info(vm.url);
            },
            mounted: function () {
                $17.info("preview basicapp mounted....");
            }
        });
    });
</script>

</@shell.page>