<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<style>

form.main input[type=text]{
width: 70%;
border-width:5px;
height:50px;
margin-top:80px;
}
form.main button{
width:10%;
border-width:5px;
height:50px;
font-size: 20px;
margin-top:80px;
}
#maindiv{
border: 1px solid black;
font-size: 15px;
padding: 5px;
}
h1{
color:blue;
text-align: center;
}
body{
background-color: grey;}
</style>
<title>TechRunner</title>
</head>
<body>
<h1>TechRunner</h1>
<div>
<form class="main" action="NewServlet" style="margin: auto;max-width:1000px">
<input type="text"  name="search" >
<button type="submit" onclick="first()"  value="Search">Search</button>
</form>
</div>

<script>
function first(){
	var ans1 = document.createElement("P");
	document.getElementById("div").appendchild(ans1);
}
</script>
</body>
</html>