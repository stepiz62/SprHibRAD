<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="shr" uri="/WEB-INF/SprHibRad.tld"%>
<!DOCTYPE html >
<html>
	<shr:dataForm modelAttribute="userPrefs" entityId="${userPrefs.user}" innerCaption="menu.userPrefs">
		<table>
			<tbody>
				<tr>
					<shr:dataItem path="locale">
		                 <shr:localeSelect>
		                 	 <form:options items="${locales}" itemLabel="name" itemValue="id"  />
		                 </shr:localeSelect> 
		                 <shr:errors /> 
					</shr:dataItem>
				</tr>
 				<tr>
					<shr:dataItem path="hmenu">
	                	<shr:radioButton value="1" valueLabel="attr.menuAtTheTop" /><br>
	               	 	<shr:radioButton value="0" valueLabel="attr.menuOnTheLeft" />
	                </shr:dataItem>		                
				</tr>
			<shr:formButtons ><td><shr:auxbutton action="changePwd" caption="menu.changePwd" /></td></shr:formButtons>
			</tbody>
		</table>
		<shr:statusBar />
	</shr:dataForm>
</html>