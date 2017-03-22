<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page session="true" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script>
function showProgressMessage(){
	var form = document.getElementById('postForm');
	form.style.display = 'none';	 
	var message = document.getElementById('message');
	message.style.display = 'block';	
}
</script>
<style>
	textarea {
	    resize: none;
	}
</style>
</head>
<body>

<form action="ToApprove" method="post" id="postForm" enctype="multipart/form-data">
	Имя:<br>
    <input type="text" name="name" <%= 	( session.getAttribute( "nickname" ) != null ) ? 
		"value='"+ session.getAttribute( "nickname" ) +"' ":	" " %>/><br>    
    Контактные данные (не обязательно):<br>
    <input type="text" name="contacts" <%= 	( session.getAttribute( "contacts" ) != null ) ? 
		"value='"+ session.getAttribute( "contacts" ) +"' ":	" " %> /><br>
    Фото связанной вещи в формате jpg или png:<br>
    <input type="file" name="file" /><br>
    Сообщение(до 240 символов):<br>
    <textarea rows="4" cols="100" name="message"></textarea><br>
    <input type="submit" onclick="showProgressMessage();" />
</form>
<div id="message" style="display:none;">Ваш пост загружается....</div>

</body>
</html>