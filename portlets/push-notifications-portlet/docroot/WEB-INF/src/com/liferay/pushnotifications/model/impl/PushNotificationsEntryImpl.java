/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.pushnotifications.model.impl;

import com.liferay.portal.kernel.json.JSON;
import com.liferay.portal.kernel.json.JSONObject;

/**
 * @author Bruno Farache
 */
public class PushNotificationsEntryImpl extends PushNotificationsEntryBaseImpl {

	public PushNotificationsEntryImpl() {
	}

	@JSON
	@Override
	public JSONObject getUser() {
		return _userJSONObject;
	}

	@Override
	public void setUser(JSONObject userJSONObject) {
		_userJSONObject = userJSONObject;
	}

	private JSONObject _userJSONObject;

}