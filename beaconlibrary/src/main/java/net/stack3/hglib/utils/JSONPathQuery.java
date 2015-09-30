package net.stack3.hglib.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Get value from JSONObject easily.
 * 
 * Usage:
 *   JSONPathQuery pathQuery = new JSONPathQuery(json);
 *   String value1 = pathQuery.stringForPath("user.name");    
 *   String value2 = pathQuery.stringForPath("user.name", "Nanashi-san");    
 *   JSONArray friends = pathQuery.arrayForPath("user.friends");    
 * 
 * @author MIYAMOTO, Hideaki. stack3.net
 * 
 * MIT License. 
 *
 */
public class JSONPathQuery {
	private JSONObject json;
	
	public JSONPathQuery(JSONObject json) {
		this.json = json;
	}

	private class SplittedPath {
		String[] pathArray;
		String name;
	}
	
	private JSONObject JSONObjectForPathArray(String[] pathArray) {
		if (pathArray.length == 0) {
			return null;
		}
		
		JSONObject target = json;
		for (String key : pathArray) {
			try {
				target = target.getJSONObject(key);
			} catch (JSONException ex) {
				return null;
			}
		}
		return target;
	}
	
	private SplittedPath splitPath(String path) {
		SplittedPath splittedPath = new SplittedPath();
		
		String[] pathArray = path.split("\\.");
		if (pathArray.length <= 1) {
			splittedPath.name = path;
			splittedPath.pathArray = null;
			return splittedPath;
		}
		
		ArrayList<String> pathArrayList = new ArrayList<String>();
		for (int i = 0; i < pathArray.length-1; i++) { 
			pathArrayList.add(pathArray[i]);
		}

		splittedPath.pathArray = pathArrayList.toArray(new String[pathArray.length-1]);
		splittedPath.name = pathArray[pathArray.length - 1];
		return splittedPath;
	}
	
	public JSONObject JSONObjectForPath(String path, JSONObject defval) {
		SplittedPath splittedPath = splitPath(path);
		try {
			if (splittedPath.pathArray == null) {
				if (!json.isNull(splittedPath.name)) {
					return json.getJSONObject(splittedPath.name);
				}
			} else {
				JSONObject obj = this.JSONObjectForPathArray(splittedPath.pathArray);
				if (!obj.isNull(splittedPath.name)) {
					return obj.getJSONObject(splittedPath.name);
				}
			}
		} catch (Exception ex) {
		}
		return defval;
	}

	public JSONObject JSONObjectForPath(String path) {
		return this.JSONObjectForPath(path, null);
	}
	
	public JSONPathQuery JSONPathQueryForPath(String path, JSONObject defval) {
		JSONObject obj = this.JSONObjectForPath(path);
		if (obj != null) {
			return new JSONPathQuery(obj);
		} else {
			return new JSONPathQuery(defval);
		}
	}

	public JSONPathQuery JSONPathQueryForPath(String path) {
		return this.JSONPathQueryForPath(path, null);
	}
		
	
	public String stringForPath(String path, String defval) {
		SplittedPath splittedPath = splitPath(path);
		try {
			if (splittedPath.pathArray == null) {
				if (!json.isNull(splittedPath.name)) {
					return json.getString(splittedPath.name);
				}
			} else {
				JSONObject obj = this.JSONObjectForPathArray(splittedPath.pathArray);
				if (!obj.isNull(splittedPath.name)) {
					return obj.getString(splittedPath.name);
				}
			}
		} catch (Exception ex) {
		}
		return defval;
	}
	
	public String stringForPath(String path) {
		return this.stringForPath(path, null);
	}
	
	public int intForPath(String path, int defval) {
		SplittedPath splittedPath = splitPath(path);
		try {
			if (splittedPath.pathArray == null) {
				if (!json.isNull(splittedPath.name)) {
					return json.getInt(splittedPath.name);
				}
			} else {
				JSONObject obj = this.JSONObjectForPathArray(splittedPath.pathArray);
				if (!obj.isNull(splittedPath.name)) {
					return obj.getInt(splittedPath.name);
				}
			}
		} catch (Exception ex) {
		}
		return defval;
	}
	
	public int intForPath(String path) {
		return this.intForPath(path, 0);
	}	
	
	public boolean booleanForPath(String path, boolean defval) {
		SplittedPath splittedPath = splitPath(path);
		try {
			Object value = null;
			if (splittedPath.pathArray == null) {
				if (!json.isNull(splittedPath.name)) {
					value = json.get(splittedPath.name);
				}
			} else {
				JSONObject obj = this.JSONObjectForPathArray(splittedPath.pathArray);
				if (!obj.isNull(splittedPath.name)) {
					value = obj.get(splittedPath.name);
				}
			}
			if (value != null) {
				if (value instanceof Boolean) {
					return ((Boolean) value).booleanValue();
				} else if (value instanceof Integer) {
					return ((Integer) value).intValue() == 0 ? false : true;
					
				} else if (value instanceof String) {
					return value.equals("0") ? false : true;
				}
			}
			
		} catch (Exception ex) {
		}
		return defval;
	}
	
	public boolean boooleanForPath(String path) {
		return this.booleanForPath(path, false);
	}	
	
	public long longForPath(String path, long defval) {
		SplittedPath splittedPath = splitPath(path);
		try {
			if (splittedPath.pathArray == null) {
				if (!json.isNull(splittedPath.name)) {
					return json.getLong(splittedPath.name);
				}
			} else {			
				JSONObject obj = this.JSONObjectForPathArray(splittedPath.pathArray);
				if (!obj.isNull(splittedPath.name)) {
					return obj.getLong(splittedPath.name);
				}
			}
		} catch (Exception ex) {
		}
		return defval;
	}
	
	public long longForPath(String path) {
		return this.longForPath(path, 0);
	}	

	public double doubleForPath(String path, double defval) {
		SplittedPath splittedPath = splitPath(path);
		try {
			if (splittedPath.pathArray == null) {
				if (!json.isNull(splittedPath.name)) {
					return json.getDouble(splittedPath.name);
				}
			} else {
				JSONObject obj = this.JSONObjectForPathArray(splittedPath.pathArray);
				if (!obj.isNull(splittedPath.name)) {
					return obj.getDouble(splittedPath.name);
				}
			}
		} catch (Exception ex) {
		}
		return defval;
	}
	
	public double doubleForPath(String path) {
		return this.doubleForPath(path, 0);
	}	
	
	public JSONArray arrayForPath(String path, JSONArray defval) {
		SplittedPath splittedPath = splitPath(path);
		try {
			if (splittedPath.pathArray == null) {
				if (!json.isNull(splittedPath.name)) {
					return json.getJSONArray(splittedPath.name);
				}
			} else {
				JSONObject obj = this.JSONObjectForPathArray(splittedPath.pathArray);
				if (!obj.isNull(splittedPath.name)) {
					return obj.getJSONArray(splittedPath.name);
				}
			}
		} catch (Exception ex) {
		}
		return defval;
	}

	public JSONArray arrayForPath(String path) {
		return this.arrayForPath(path, null);
	}	
}
