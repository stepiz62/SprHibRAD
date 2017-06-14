
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IEngineTask;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

import com.sprhibrad.framework.controller.ShrBirtView;

/**
 * BirtView is used to run and render BIRT reports.
 * This class expects the request to contain a ReportName and ReportFormat
 * parameter. Parameters setting is delegated to the ShrBirtView ancestor class .
 */

public class BirtView extends ShrBirtView /* SprHibRAD */  {

	private IReportEngine birtEngine;

	protected void renderMergedOutputModel(Map map, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		IRenderOption renderOptions = null;
		String reportName = shr_getName(map);
		String format = shr_getFormat(map);
		ServletContext sc = request.getSession().getServletContext();
		if (format == null) {
			format = "html";
		}
		IReportRunnable runnable = null;
		runnable = birtEngine.openReportDesign(sc.getRealPath(getReportsPath(map, request)) + "/" + reportName + ".rptdesign");
		IRunAndRenderTask task = birtEngine.createRunAndRenderTask(runnable);

		shr_setParameters(task, request, map); /* SprHibRAD */

		response.setContentType(birtEngine.getMIMEType(format));
		IRenderOption options = null == renderOptions ? new RenderOption() : renderOptions;
		if (format.equalsIgnoreCase("html")) {
			HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
			htmlOptions.setOutputFormat("html");
			htmlOptions.setOutputStream(response.getOutputStream());
			htmlOptions.setImageHandler(new HTMLServerImageHandler());
			htmlOptions.setBaseImageURL(request.getContextPath() + "/images");
			htmlOptions.setImageDirectory(sc.getRealPath("/images"));
			task.setRenderOption(htmlOptions);

		} else if (format.equalsIgnoreCase("pdf")) {
			PDFRenderOption pdfOptions = new PDFRenderOption(options);
			pdfOptions.setOutputFormat("pdf");
			pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
			pdfOptions.setOutputStream(response.getOutputStream());
			task.setRenderOption(pdfOptions);
		} else {
			String att = "download." + format;
			String uReportName = reportName.toUpperCase();
			if (uReportName.endsWith(".RPTDESIGN")) {
				att = uReportName.replace(".RPTDESIGN", "." + format);
			}
			response.setHeader("Content-Disposition", "attachment; filename=\"" + att + "\"");
			options.setOutputStream(response.getOutputStream());
			options.setOutputFormat(format);
			task.setRenderOption(options);
		}
		task.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request);
		task.run();
		task.close();

	}

	public void setBirtEngine(IReportEngine birtEngine) {
		this.birtEngine = birtEngine;
	}

	@Override /* SprHibRAD */
	protected void shr_setParameter(Object task, String name, Object value) {
		((IEngineTask) task).setParameterValue(name, value);		
	}

}
