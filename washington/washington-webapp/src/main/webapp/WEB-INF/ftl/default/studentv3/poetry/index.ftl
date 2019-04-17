<#import '../layout/layout.ftl' as temp>
<@temp.page>

<div class="t-app-homework-box">
    <div class="t-app-homework">
        <div style="width: 375px; margin: 0 auto;">
            <iframe class="vox17zuoyeIframe" src="${gameUrl!}" width="100%" marginwidth="0" height="667" marginheight="0" scrolling="auto" frameborder="0"></iframe>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        var logFrom,from = $17.getQuery("from");
        switch (from){
            case "indexCard":
                logFrom = "首页卡片";
                break;
            default:
                logFrom = "";
        }

        $17.voxLog({
            module: "m_FeJejhY7pq",
            op : "o_oxIWu2EfHD",
            s0 : logFrom
        }, 'student');
    });
</script>
</@temp.page>

