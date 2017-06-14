<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="shr" uri="/WEB-INF/SprHibRad.tld"%>
<!DOCTYPE html >
<html>
<shr:form action="login">
	<table>
		<tr class="formButtons" height='20px' >
			<td colspan="2" ></td>
		</tr>
		<shr:perror param="error" msgKey="login.wrongCrd" />
		<shr:perror param="logout" msgKey="login.logout" />
		<shr:perror param="unavailable" msgKey="login.unavailable" />
		<tr>
			<shr:dataItem label="_label.username" path="username">
				<shr:sinput />
			</shr:dataItem>
		</tr>
		<tr>
			<shr:dataItem label="_label.password" path="password">
				<shr:sinput type="password" />
			</shr:dataItem>
		</tr>
		<tr class="formButtons" >
			<td colspan="2" ><shr:sinput type="submit" msgKey="login.login" /></td>
		</tr>
	</table>
</shr:form>
</html>	
</html>