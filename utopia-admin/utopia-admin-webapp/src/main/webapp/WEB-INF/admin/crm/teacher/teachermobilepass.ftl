<#-- @ftlvariable name="conditionMap" type="java.util.LinkedHashMap" -->
<#macro queryConditons>
<div>
  <form method="post" action="teachermobile.vpage" class="form-horizontal">
    <fieldset>
      <legend>老师手机注册后门</legend>
      <ul class="inline">
        <li>
          <label for="teacherMobileNumber">
            老师手机
            <input name="teacherMobileNumber" id="teacherMobileNumber" type="text">
          </label>
        </li>
      </ul>


      <ul class="inline">
        <li>
          <button id='submit' type="submit" class="btn btn-primary">修 改</button>
        </li>
      </ul>
      <br/>
    </fieldset>
  </form>
</div>

</#macro>