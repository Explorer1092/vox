<#import "../layout_default.ftl" as layout_default>
<style>
body { padding-top: 60px; }
</style>
<@layout_default.page page_title='17zuoye Manage System Login' page_num=0>
    <!-- /container -->
    <form class="form-signin" method="post" action="?">
        <h2 class="form-signin-heading">Manage System</h2>
        <input type="text" name="username" class="input-block-level" placeholder="Username">
        <input type="password" name="password" class="input-block-level" placeholder="Password">
        <label class="checkbox">
            <input type="checkbox" value="remember-me"> Remember me
        </label>
        <button class="btn btn-large btn-primary" type="submit">Sign in</button>
    </form>
    <!-- /container -->
</@layout_default.page>
