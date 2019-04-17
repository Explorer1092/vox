<script type="text/javascript">
    function switchCdnTypeReloadPage() {
        <#--
        FIXME
        flash加载器点“重新加载”之后，调用 js: switchCdnTypeReloadPage()
        如果返回 "ok"，flash就不需要做任何处理。否则，继续走默认流程。
        currentCdnType 和 cdnDomainMapKeys 在 WashingtonRequestContext 中定义
         -->
        var cdnType = ${json_encode(currentCdnType)};
        var cdnDomainMapKeys = ${json_encode(cdnDomainMapKeys)};

        if (cdnDomainMapKeys.length == 0) {
            alert('CDN配置错误，请联系客服或技术');
            return;
        }
        <#-- inArray找不到会返回 '-1' ，这个逻辑恰好还是对的 -->
        var idx = ($.inArray(cdnType, cdnDomainMapKeys) + 1) % cdnDomainMapKeys.length;
        var nct = cdnDomainMapKeys[idx];
        var t = new Date();
        t.setTime(t.getTime() + (nct == 'skip' ? 7200 * 1000 : 86400 * 14 * 1000));
        window.document.cookie = "cdntype=" + nct + ";path=/;expires=" + t.toGMTString();
        setTimeout(function () {
            var p = $17.getQueryParams();
            p['_set_cdntype'] = nct;
            p['_'] = t.getTime();
            window.location.href = $17.getBaseLocation() + '?' + $.param(p);
        }, 500);
        return "ok";
    }

    <#-- 预期是 flash 应用加载成功后会调用这个函数。如果超过n秒没调用，说明可能卡住了。现在已经有这个机制了。但是，担心误报，所以暂时还没用这套机制。 -->
    function flashAppStartSuccessfully() {
        //alert('success');
    }
</script>