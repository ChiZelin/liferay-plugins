/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.knowledgebase.util;

import com.liferay.knowledgebase.model.KBArticle;
import com.liferay.knowledgebase.util.comparator.KBArticleCreateDateComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleKBTemplateIdComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleModifiedDateComparator;
import com.liferay.knowledgebase.util.comparator.KBArticlePriorityComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleStatusComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleTitleComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleUserNameComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleVersionComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleViewCountComparator;
import com.liferay.knowledgebase.util.comparator.KBStructureCreateDateComparator;
import com.liferay.knowledgebase.util.comparator.KBStructureModifiedDateComparator;
import com.liferay.knowledgebase.util.comparator.KBStructureUserNameComparator;
import com.liferay.knowledgebase.util.comparator.KBTemplateCreateDateComparator;
import com.liferay.knowledgebase.util.comparator.KBTemplateEngineTypeComparator;
import com.liferay.knowledgebase.util.comparator.KBTemplateModifiedDateComparator;
import com.liferay.knowledgebase.util.comparator.KBTemplateTitleComparator;
import com.liferay.knowledgebase.util.comparator.KBTemplateUserNameComparator;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.UniqueList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Peter Shin
 * @author Brian Wing Shun Chan
 */
public class KnowledgeBaseUtil {

	public static OrderByComparator getKBArticleOrderByComparator(
		String orderByCol, String orderByType) {

		if (Validator.isNull(orderByCol) || Validator.isNull(orderByType)) {
			return null;
		}

		boolean ascending = false;

		if (orderByType.equals("asc")) {
			ascending = true;
		}

		if (orderByCol.equals("create-date")) {
			return new KBArticleCreateDateComparator(ascending);
		}
		else if (orderByCol.equals("modified-date")) {
			return new KBArticleModifiedDateComparator(ascending);
		}
		else if (orderByCol.equals("priority")) {
			return new KBArticlePriorityComparator(ascending);
		}
		else if (orderByCol.equals("status")) {
			return new KBArticleStatusComparator(ascending);
		}
		else if (orderByCol.equals("template-id")) {
			return new KBArticleKBTemplateIdComparator(ascending);
		}
		else if (orderByCol.equals("title")) {
			return new KBArticleTitleComparator(ascending);
		}
		else if (orderByCol.equals("user-name")) {
			return new KBArticleUserNameComparator(ascending);
		}
		else if (orderByCol.equals("version")) {
			return new KBArticleVersionComparator(ascending);
		}
		else if (orderByCol.equals("view-count")) {
			return new KBArticleViewCountComparator(ascending);
		}

		return null;
	}

	public static Sort[] getKBArticleSorts(
		String orderByCol, String orderByType) {

		if (Validator.isNull(orderByCol) || Validator.isNull(orderByType)) {
			return SortFactoryUtil.getDefaultSorts();
		}

		boolean reverse = true;

		if (orderByType.equals("asc")) {
			reverse = false;
		}

		if (orderByCol.equals("create-date")) {
			String fieldName = "createDate";

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.LONG_TYPE, reverse),
				SortFactoryUtil.create(null, Sort.SCORE_TYPE, false)
			};
		}
		else if (orderByCol.equals("modified-date")) {
			String fieldName = Field.MODIFIED;

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.LONG_TYPE, reverse),
				SortFactoryUtil.create(null, Sort.SCORE_TYPE, false)
			};
		}
		else if (orderByCol.equals("score")) {
			String fieldName = null;

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.SCORE_TYPE, !reverse),
				SortFactoryUtil.create(Field.MODIFIED, Sort.LONG_TYPE, true)
			};
		}
		else if (orderByCol.equals("template-id")) {
			String fieldName = "kbTemplateId";

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.LONG_TYPE, reverse),
				SortFactoryUtil.create(null, Sort.SCORE_TYPE, false)
			};
		}
		else if (orderByCol.equals("title")) {
			String fieldName = Field.TITLE + "Keyword";

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.STRING_TYPE, reverse),
				SortFactoryUtil.create(null, Sort.SCORE_TYPE, false)
			};
		}
		else if (orderByCol.equals("user-name")) {
			String fieldName = Field.USER_NAME + "Keyword";

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.STRING_TYPE, reverse),
				SortFactoryUtil.create(null, Sort.SCORE_TYPE, false)
			};
		}

		return SortFactoryUtil.getDefaultSorts();
	}

	public static String getKBArticleURL(
		long plid, long resourcePrimKey, String portalURL, boolean maximized) {

		StringBundler sb = new StringBundler(15);

		sb.append(portalURL);
		sb.append(PortalUtil.getPathMain());
		sb.append("/portal/knowledge_base/find_kb_article");
		sb.append(StringPool.QUESTION);
		sb.append("plid");
		sb.append(StringPool.EQUAL);
		sb.append(HttpUtil.encodeURL(String.valueOf(plid)));
		sb.append(StringPool.AMPERSAND);
		sb.append("resourcePrimKey");
		sb.append(StringPool.EQUAL);
		sb.append(HttpUtil.encodeURL(String.valueOf(resourcePrimKey)));
		sb.append(StringPool.AMPERSAND);
		sb.append("maximized");
		sb.append(StringPool.EQUAL);
		sb.append(HttpUtil.encodeURL(String.valueOf(maximized)));

		return sb.toString();
	}

	public static OrderByComparator getKBStructureOrderByComparator(
		String orderByCol, String orderByType) {

		if (Validator.isNull(orderByCol) || Validator.isNull(orderByType)) {
			return null;
		}

		boolean ascending = false;

		if (orderByType.equals("asc")) {
			ascending = true;
		}

		if (orderByCol.equals("create-date")) {
			return new KBStructureCreateDateComparator(ascending);
		}
		else if (orderByCol.equals("modified-date")) {
			return new KBStructureModifiedDateComparator(ascending);
		}
		else if (orderByCol.equals("user-name")) {
			return new KBStructureUserNameComparator(ascending);
		}

		return null;
	}

	public static OrderByComparator getKBTemplateOrderByComparator(
		String orderByCol, String orderByType) {

		if (Validator.isNull(orderByCol) || Validator.isNull(orderByType)) {
			return null;
		}

		boolean ascending = false;

		if (orderByType.equals("asc")) {
			ascending = true;
		}

		if (orderByCol.equals("create-date")) {
			return new KBTemplateCreateDateComparator(ascending);
		}
		else if (orderByCol.equals("engine-type")) {
			return new KBTemplateEngineTypeComparator(ascending);
		}
		else if (orderByCol.equals("modified-date")) {
			return new KBTemplateModifiedDateComparator(ascending);
		}
		else if (orderByCol.equals("title")) {
			return new KBTemplateTitleComparator(ascending);
		}
		else if (orderByCol.equals("user-name")) {
			return new KBTemplateUserNameComparator(ascending);
		}

		return null;
	}

	public static Long[][] getParams(Long[] params) {
		if ((params == null) || (params.length == 0)) {
			return null;
		}

		if (params.length <= _SQL_DATA_MAX_PARAMETERS) {
			return new Long[][] {new Long[0], params};
		}

		return new Long[][] {
			ArrayUtil.subset(params, _SQL_DATA_MAX_PARAMETERS, params.length),
			ArrayUtil.subset(params, 0, _SQL_DATA_MAX_PARAMETERS)
		};
	}

	public static List<KBArticle> sort(
		long[] resourcePrimKeys, List<KBArticle> kbArticles) {

		Map<Long, KBArticle> map = new HashMap<Long, KBArticle>();

		for (KBArticle kbArticle : kbArticles) {
			map.put(kbArticle.getResourcePrimKey(), kbArticle);
		}

		kbArticles.clear();

		for (long resourcePrimKey : resourcePrimKeys) {
			if (map.containsKey(resourcePrimKey)) {
				kbArticles.add(map.get(resourcePrimKey));
			}
		}

		return kbArticles;
	}

	public static String[] splitKeywords(String keywords) {
		List<String> keywordsList = new UniqueList<String>();

		StringBundler sb = new StringBundler();

		for (char c : keywords.toCharArray()) {
			if (Character.isWhitespace(c)) {
				if (sb.length() > 0) {
					keywordsList.add(sb.toString());

					sb = new StringBundler();
				}
			}
			else if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			}
			else {
				return new String[] {keywords};
			}
		}

		if (sb.length() > 0) {
			keywordsList.add(sb.toString());
		}

		return StringUtil.split(StringUtil.merge(keywordsList));
	}

	private static final int _SQL_DATA_MAX_PARAMETERS =
		GetterUtil.getInteger(PropsUtil.get(PropsKeys.SQL_DATA_MAX_PARAMETERS));

}