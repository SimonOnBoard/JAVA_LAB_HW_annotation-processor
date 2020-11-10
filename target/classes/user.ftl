<form method="${method}" action="${action}">
    <#list inputs as input>
        <input type="${input.type()}" name="${input.name()}" placeholder="${input.placeholder()}">
    </#list>
</form>