package com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.monkeyconsole.editors.utils.CloudServiceException;
import com.gorillalogic.monkeyconsole.editors.utils.CloudServices;

public class DeviceMatrixTreeViewModel {
	private JSONObject jsonPhoneVersions = null;

	public List<AndroidVersion> getCategories() throws JSONException, CloudServiceException {
		List<AndroidVersion> versions = new ArrayList<AndroidVersion>();

		// Call up ole cloud services and get the devices
		if (jsonPhoneVersions == null) {
			jsonPhoneVersions = CloudServices.getDeviceTypes();
		}

		JSONArray ja = jsonPhoneVersions.getJSONArray("params");
		for (int i = 0; i < ja.length(); i++) {
			AndroidVersion ver = new AndroidVersion();
			ver.setName(ja.getJSONObject(i).getString("os"));
			versions.add(ver);
			for (int j = 0; j < ja.getJSONObject(i).getJSONArray("values").length(); j++) {
				JSONObject entry = ja.getJSONObject(i).getJSONArray("values").getJSONObject(j);
				String value = entry.getString("value");
				String examples = entry.getString("examples");
				String param = entry.getString("param");
				String summary = value;
				
				if (value.indexOf(':') > -1) {
					summary = value.substring(value.indexOf(":") + 1);
				} else if (value.indexOf("==") > -1) {
					// Android Devices
					String[] parts = value.split("==");
					if (parts.length > 0) {
						// manufacturer
						summary = parts[0];
					}
					if (parts.length > 1) {
						// deviceName
						summary += " " + parts[1];
					}
				}
				
				Resolution res = new Resolution(summary, examples, param);
				res.setAndroidVersion(ver.getName());
				ver.getTodos().add(res);
			}
		}

		return versions;
	}
}
