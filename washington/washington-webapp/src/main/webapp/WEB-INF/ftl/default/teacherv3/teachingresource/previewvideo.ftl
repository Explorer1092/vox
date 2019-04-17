<#import "layout.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["vue"] css=["plugin.venus-pre"]/>
<style>
    video{
        object-fit: contain;
        position: fixed !important;
        top: 0px !important;
        right: 0px !important;
        bottom: 0px !important;
        left: 0px !important;
        box-sizing: border-box !important;
        min-width: 0px !important;
        max-width: none !important;
        min-height: 0px !important;
        max-height: none !important;
        width: 100% !important;
        height: 100% !important;
        transform: none !important;
        margin: 0px !important;
    }
</style>
<div id="previewVideo" v-cloak>
    <video controls="" v-bind:poster="videoConverUrl" autoplay="autoplay" name="media"><source :src="videoUrl" type="video/mp4"></video>
</div>
<script type="text/javascript">
    $(function(){
        var videoUrl = $17.getQuery("videoUrl");
        var videoConverUrl = $17.getQuery("videoConverUrl");
        new Vue({
            el : "#previewVideo",
            data : {
                videoUrl : videoUrl,
                videoConverUrl : videoConverUrl
            },
            methods : {
            },
            created: function () {
                $17.info("preview created....");
            },
            mounted: function () {
                $17.info("preview mounted....");
            }
        });
    });
</script>

</@shell.page>