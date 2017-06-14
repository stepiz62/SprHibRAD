<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="shr" uri="/WEB-INF/SprHibRad.tld"%>
<!DOCTYPE html >
<html>
    <shr:form>
 	<h1>Session expired</h1>
	<p>Please, authenticate yourself in the login page again.</p>
	<p>
		<a href="${pageContext.request.contextPath}/login">Login</a>
	</p>
    </shr:form>
 </html>
 