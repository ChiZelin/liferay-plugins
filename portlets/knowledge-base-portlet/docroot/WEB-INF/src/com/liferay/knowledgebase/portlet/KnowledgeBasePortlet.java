/**
 * Copyright (c) 2000-2008 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.knowledgebase.portlet;

import com.liferay.knowledgebase.ArticleTitleException;
import com.liferay.knowledgebase.ArticleVersionException;
import com.liferay.knowledgebase.KnowledgeBaseKeys;
import com.liferay.knowledgebase.NoSuchArticleException;
import com.liferay.knowledgebase.model.KBArticle;
import com.liferay.knowledgebase.service.KBArticleServiceUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.DocumentConversionUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.tags.EntryNameException;
import com.liferay.util.bridges.jsp.JSPPortlet;
import com.liferay.util.servlet.ServletResponseUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <a href="KnowledgeBasePortlet.java.html"><b><i>View Source</i></b></a>
 *
 * @author Jorge Ferrer
 * @author Bruno Farache
 *
 */
public class KnowledgeBasePortlet extends JSPPortlet {

	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		try {
			String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

			KBArticle article = null;

			if (cmd.equals(Constants.DELETE)) {
				deleteArticle(actionRequest);
			}
			else if (cmd.equals(Constants.UPDATE)) {
				article = updateArticle(actionRequest);
			}
			else if (cmd.equals(Constants.SUBSCRIBE)) {
				subscribe(actionRequest);
			}
			else if (cmd.equals(Constants.UNSUBSCRIBE)) {
				unsubscribe(actionRequest);
			}
			else if (cmd.equals(
				Constants.SUBSCRIBE + KnowledgeBaseKeys.ARTICLE)) {

				subscribeArticle(actionRequest);
			}
			else if (cmd.equals(
				Constants.UNSUBSCRIBE + KnowledgeBaseKeys.ARTICLE)) {

				unsubscribeArticle(actionRequest);
			}

			boolean preview = ParamUtil.getBoolean(actionRequest, "preview");

			if (preview) {
				actionResponse.setRenderParameters(
					actionRequest.getParameterMap());
			}
			else if (Validator.isNotNull(cmd)) {
				String redirect = ParamUtil.getString(
					actionRequest, "redirect");

				if (article != null) {
					String saveAndContinueRedirect = ParamUtil.getString(
						actionRequest, "saveAndContinueRedirect");

					if (Validator.isNotNull(saveAndContinueRedirect)) {
						redirect = saveAndContinueRedirect;
					}
					else if (redirect.endsWith("title=")) {
						redirect += article.getTitle();
					}
				}

				if (SessionErrors.isEmpty(actionRequest)) {
					SessionMessages.add(actionRequest, "request_processed");
				}

				sendRedirect(actionRequest, actionResponse, redirect);

				return;
			}
		}
		catch (Exception e) {
			if (e instanceof NoSuchArticleException ||
				e instanceof PrincipalException) {

				SessionErrors.add(actionRequest, e.getClass().getName());

				actionResponse.setRenderParameters(
					actionRequest.getParameterMap());
				actionResponse.setRenderParameter(
					Constants.CMD, "error");
			}
			else if (e instanceof ArticleTitleException ||
				e instanceof ArticleVersionException ||
				e instanceof EntryNameException ||
				e instanceof PrincipalException) {

				SessionErrors.add(actionRequest, e.getClass().getName());

				actionResponse.setRenderParameters(
					actionRequest.getParameterMap());
				actionResponse.setRenderParameter(
					Constants.CMD, "edit_article");
			}
			else {
				throw new PortletException(e);
			}
		}
	}

	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException, IOException {

		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String title = ParamUtil.getString(renderRequest, "title");

		try {
			KBArticle article = null;

			if (Validator.isNotNull(title)) {
				article = KBArticleServiceUtil.getArticle(
					themeDisplay.getPortletGroupId(), title);
			}

			renderRequest.setAttribute(KnowledgeBaseKeys.ARTICLE, article);

		}
		catch (Exception e) {
			throw new PortletException(e);
		}

		super.render(renderRequest, renderResponse);
	}

	public void serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws IOException, PortletException {

		convertArticle(resourceRequest, resourceResponse);
	}

	protected void sendRedirect(
			ActionRequest actionRequest, ActionResponse actionResponse,
			String redirect)
		throws IOException {

		if (SessionErrors.isEmpty(actionRequest)) {
			SessionMessages.add(actionRequest, "request_processed");
		}

		if (redirect == null) {
			redirect = ParamUtil.getString(actionRequest, "redirect");
		}

		if (Validator.isNotNull(redirect)) {
			actionResponse.sendRedirect(redirect);
		}
	}

	protected void convertArticle(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		{

		InputStream is = null;

		try {
			ThemeDisplay themeDisplay =
				(ThemeDisplay)resourceRequest.getAttribute(
					WebKeys.THEME_DISPLAY);

			PortletPreferences prefs = resourceRequest.getPreferences();

			long groupId = themeDisplay.getPortletGroupId();
			String title = ParamUtil.getString(resourceRequest, "title");
			double version = ParamUtil.getDouble(resourceRequest, "version");

			String targetExtension = ParamUtil.getString(
					resourceRequest, "targetExtension");

			String[] extensions = prefs.getValues(
				"extensions", new String[] {});

			boolean convert = false;

			for (String extension : extensions) {
				if (extension.equals(targetExtension)) {
					convert = true;

					break;
				}
			}

			if (!convert) {
				return;
			}

			KBArticle article =	KBArticleServiceUtil.getArticle(
				groupId, title, version);

			String content = article.getContent();

			StringBuilder sb = new StringBuilder();

			sb.append("<h1>");
			sb.append(title);
			sb.append("</h1>");
			sb.append(content);

			is = new ByteArrayInputStream(
				sb.toString().getBytes(StringPool.UTF8));

			String sourceExtension = "html";

			sb = new StringBuilder();

			sb.append(title);
			sb.append(StringPool.PERIOD);
			sb.append(sourceExtension);

			String fileName = sb.toString();

			String id = article.getUuid();

			InputStream convertedIS = DocumentConversionUtil.convert(
				id, is, sourceExtension, targetExtension);

			if (convertedIS != null) {
				sb = new StringBuilder();

				sb.append(title);
				sb.append(StringPool.PERIOD);
				sb.append(targetExtension);

				fileName = sb.toString();

				is = convertedIS;
			}

			String contentType = MimeTypesUtil.getContentType(fileName);

			HttpServletResponse response = PortalUtil.getHttpServletResponse(
				resourceResponse);

			ServletResponseUtil.sendFile(response, fileName, is, contentType);
		}
		catch (Exception e) {
			_log.error(e, e);
		}
		finally {
			ServletResponseUtil.cleanUp(is);
		}
	}

	protected void deleteArticle(ActionRequest actionRequest) throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String title = ParamUtil.getString(actionRequest, "title");

		KBArticleServiceUtil.deleteArticle(
			themeDisplay.getPortletGroupId(), title);
	}

	protected void subscribe(ActionRequest actionRequest)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long groupId = themeDisplay.getPortletGroupId();

		KBArticleServiceUtil.subscribe(groupId);
	}

	protected void unsubscribe(ActionRequest actionRequest)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long groupId = themeDisplay.getPortletGroupId();

		KBArticleServiceUtil.unsubscribe(groupId);
	}

	protected void subscribeArticle(ActionRequest actionRequest)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long groupId = themeDisplay.getPortletGroupId();
		String title = ParamUtil.getString(actionRequest, "title");

		KBArticleServiceUtil.subscribeArticle(groupId, title);
	}

	protected void unsubscribeArticle(ActionRequest actionRequest)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long groupId = themeDisplay.getPortletGroupId();
		String title = ParamUtil.getString(actionRequest, "title");

		KBArticleServiceUtil.unsubscribeArticle(groupId, title);
	}

	protected KBArticle updateArticle(ActionRequest actionRequest)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PortletPreferences prefs = actionRequest.getPreferences();

		String title = ParamUtil.getString(actionRequest, "title");
		double version = ParamUtil.getDouble(actionRequest, "version");

		String content = ParamUtil.getString(actionRequest, "content");
		String description = ParamUtil.getString(actionRequest, "description");
		boolean minorEdit = ParamUtil.getBoolean(actionRequest, "minorEdit");
		String parentTitle = ParamUtil.getString(actionRequest, "parentTitle");

		String[] tagsEntries = StringUtil.split(
			ParamUtil.getString(actionRequest, "tagsEntries"));

		return KBArticleServiceUtil.updateArticle(
			themeDisplay.getPortletGroupId(), title, version, content,
			description,  minorEdit, parentTitle, tagsEntries, prefs,
			themeDisplay);
	}

	private static Log _log = LogFactory.getLog(KnowledgeBasePortlet.class);

}