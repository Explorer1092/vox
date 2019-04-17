<h3>Daily Term Report  ${action} Test Page</h3>
<br>
<html>
<body>
<form action="/psr/${action}.do" method="get">
<table>
    <tr>
        <td>
            group_id:
        </td><td>
        <input type="text" name="group_id" value="${group_id}" placeholder="请输入" />
    </td><td>
        (*)
    </td>
    </tr>
    <tr>
        <td>
            unit_id:
        </td><td>
            <input type="text" name="unit_id" value="${unit_id}" placeholder="请输入" />
        </td><td>
        (*)
        </td><td>
            <input type="submit" id="submit" value="提交" />
        </td>
    </tr>
</table>
</form>
<table border="1"><tr><td>${retMsg}</td></tr></table>
</body>
</html>
