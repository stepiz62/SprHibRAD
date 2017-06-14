<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="shr" uri="/WEB-INF/SprHibRad.tld"%>
<!DOCTYPE html >
<html>
	<shr:dataForm modelAttribute="appuser" entityId="${appuser.id}" >
		<table>
			<tbody>
				<tr>
					<shr:dataItem label="_label.oldPwd" path="oldPwd">
						<shr:sinput type="password" />
					</shr:dataItem>
				</tr>
				<tr>
					<shr:dataItem  path='password'  >
						<shr:sinput type="password" /><shr:errors />
					</shr:dataItem>
				</tr>
				<tr>
					<shr:dataItem label="_label.repeatPwd" path="pwd2">
						<shr:sinput type="password" />
					</shr:dataItem>
				</tr>
			</tbody>
		</table>
		<input type="hidden" name="username" value="${appuser.username}" />
		<input type="hidden" name="currPwd" value="${appuser.password}" />
		<shr:formButtons />
	</shr:dataForm>
</html>