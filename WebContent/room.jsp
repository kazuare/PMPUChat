<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src='http://code.jquery.com/jquery-latest.min.js' type='text/javascript'></script>
<title>room</title>
</head>
<body bgcolor='#81E254'>
<h1>PMPU MUSIC GUESSING GAME</h1>
<audio src='/music/Free.mp3' controls ></audio>
<p>MP3 test.</p>
<form>
<textarea id='answer' cols='40' rows='2'></textarea>
<button type='button' id='chatButton'>submit</button> 
</form>
<div class='chat'></div>
<script>
$(document).ready(function(){
function refresh(message_id){
	$.ajax({
	  type: "GET",
	  url: 'Chat?refreshFrom='+message_id.toString(),
      timeout:1000*50,
	  success: function( data ) {
		charIndex=data.indexOf('#');
		lastId=parseInt(data.substring(0,charIndex));
		data=data.substring(charIndex);
		
		if(lastId!=message_id)
	  		$( '.chat' ).prepend(data);
		
		setTimeout(function(lastId){
			refresh(lastId);	
			console.log("messages refresh");
		}, 1000,lastId);},
	  error: function() {
			console.log("refresh error");
			refresh();	
	  }
	
	});
}
refresh(-1);
});

$("#chatButton").click(function(){
	$.ajax({
		  type: "GET",
		  url: 'Chat?sendMessage='+$("#answer").val(),
		  success: function( data ) {}
		});
	$("#answer").val("");
});
</script>
</body>
</html>