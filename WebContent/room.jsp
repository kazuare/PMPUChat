<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page session="true" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src='http://code.jquery.com/jquery-latest.min.js' type='text/javascript'></script>
<title>room</title>
<style>
body {
    background-color: #81E254;
    width:100%;
    overflow-x: hidden;
}
#main {
    width:60%;
    margin: auto;
} 
#bordered{
    width:100%;
    background-color:#9ae876;
    border-color: #81E254 #508e33 #508e33; 
    border-style: solid;  
    border-width: 1px;
}
#answerDiv{
    width:100%;
    text-align:right;
} 
#answer {
    width:100%;
    height:5em;
    border: none;
    padding:0;
}  
#chat {
    width:100%;
} 
#chat div{
    width:90%;
    margin: 1em;
    border-color: #508e33; 
    border-style: solid;  
    border-width: 1px;	
}
#greeting {
	
}
h1 {
    text-align:center;
} 
</style>
</head>
<body>
<div id='main'>
<p id='greeting'><%= (session.getAttribute("username")!=null) ? "Hello, "+session.getAttribute("username") :
	"Not logged in" %>.</p>
<audio src='/music/Free.mp3' controls ></audio>
<div id='bordered'>
<textarea id='answer'></textarea>
<div id='answerDiv'>
<button type='button' id='answerButton'>Отправить сообщение</button> 
</div>
<div id='chat'>
</div>
</div>
</div>
<script>
//if changing chatlength, change the same property in Chat.java
chatLength = 100;

$(document).ready(function(){
function refresh(message_id){
	$.ajax({
	  type: "POST",
	  //we got message_id from the previous refresh call
	  url: 'Chat',
	  data:{refreshFrom: message_id.toString()},
      timeout:1000 * 50,
	  success: function( data ) {
		charIndex=data.indexOf('#');
		//this gets the actual number of messages at client and server side (these are equal at the moment)
		lastId=parseInt(data.substring(0, charIndex));
		data=data.substring(charIndex + 1);
		
		if(lastId != message_id)
	  		$( '#chat' ).prepend(data);
		
		if($( ".msg" ).size() > chatLength)
			for(var i = chatLength; i < $( ".msg" ).size();)
				$( ".msg" ).toArray()[chatLength].remove();

		//set timeout to the next refresh call
		setTimeout(function(lastId){
			refresh(lastId);	
			console.log("messages refresh");
		}, 500,lastId);},
	  error: function() {
			console.log("refresh error");
			refresh();	
	  }
	
	});
}
//starting with no messages at all, get all at first iteration, then ask for updates with recursion
refresh(-1);
});
<%= (session.getAttribute("username")!=null) ? "" :	
	"var name;"+
	"serverAnswer='0';"+
	" serverAnswer=$.ajax({"+
		"type: 'POST',"+
		"url: 'Autorizator',"+
		" async:false, "+
		"success: function(){$('#greeting').text('Hello, ' + name)},"+
		"data:{username:name=prompt('Enter your username:')}"+
	"}).responseText;"+
	"while(serverAnswer!='1')" +
	"serverAnswer=$.ajax({"+
		"type: 'POST',"+
		"url: 'Autorizator',"+
		"async:false, "+
		"success: function(){$('#greeting').text('Hello, ' + name)},"+
		"data:{username:name=prompt('This username is invalid or is already taken, \\n please choose another one:')}"+
	"}).responseText;"
		%>

$("#answerButton").click(function(){
	$.ajax({
		  type: "POST",
		  url: 'Chat',
		  data:{sendMessage:$("#answer").val()},
		  //do nothing on success, updating is refresh()'s job
		  success: function( data ) {}
		});
	//clear the text field
	$("#answer").val("");
});
</script>
</body>
</html>