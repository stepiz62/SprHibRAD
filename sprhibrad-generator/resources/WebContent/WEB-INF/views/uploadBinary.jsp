<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="shr" uri="/WEB-INF/SprHibRad.tld"%>
<!DOCTYPE html >
<html>
	<shr:nvform action="${action}"  enctype="multipart/form-data" innerCaption="app.upload">
	    <input type="file" name="file" ${accept} />
	    <br>
	    <shr:formButtons actCaption='upload' />
	    <input type="hidden" name="op" value="${op}" />
	    <input type="hidden" name="pp" value="${pp}" />
	</shr:nvform>
 </html>
 