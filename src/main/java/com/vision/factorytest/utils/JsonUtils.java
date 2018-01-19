package com.vision.factorytest.utils;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Json解析工具
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class JsonUtils {

	private static Gson gson = new Gson();

	public static String toJsonString(Object obj) {
		return gson.toJson(obj).replace("\\\\", "\\");
	}

	public static <T> String toJsonString(List<T> list) {
		return gson.toJson(list);
	}

	public static JsonObject toJsonObject(Object obj) {
		return (new JsonParser().parse(toJsonString(obj)).getAsJsonObject());
	}

	public static JsonArray toJsonArray(Object obj) {
		JsonArray ja = new JsonParser().parse(toJsonString(obj)).getAsJsonArray();
		return ja;
	}

	public static <T> JsonArray toJsonArray(List<T> list) {
		JsonArray ja = new JsonParser().parse(toJsonString(list)).getAsJsonArray();
		return ja;
	}

	public static HashMap toHashMap(String obj) {
		HashMap<String, String> data = new HashMap<String, String>();
		data = gson.fromJson(obj, new TypeToken<HashMap<String, String>>() {
		}.getType());
		return data;
	}

	public static HashMap toHashMap(Object obj) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		data = gson.fromJson(toJsonString(obj), new TypeToken<HashMap<String, Object>>() {
		}.getType());
		return data;
	}

	public static <T> T fromJsonString(String jsonStr, Class<T> classOfT) {
		T bean = gson.fromJson(jsonStr, classOfT);
		return bean;
	}

	public static <T> T fromJsonObject(JsonObject jsonObj, Class<T> classOfT) {
		T bean = gson.fromJson(jsonObj, classOfT);
		return bean;
	}
}
