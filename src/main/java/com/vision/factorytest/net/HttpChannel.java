package com.vision.factorytest.net;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.factorytest.Constant;
import com.vision.factorytest.manager.ReceiveMessageManager;
import com.vision.factorytest.utils.ShowUtils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpChannel {
	private static final Logger log = LoggerFactory.getLogger(HttpChannel.class);
	/**
	 * 使用http发送消息（get方式）
	 * 
	 * @param urlOrigin
	 *            网络访问接口
	 * @param requestParams
	 *            请求参数
	 */
	public static void sendMessageGet(final String urlOrigin, Map<String, String> requestParams) {
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request;
		if (requestParams != null) {
			request = new Request.Builder().url(Constant.SERVER_URL + urlOrigin + "?" + parseGetParams(requestParams))
					.build();
		} else {
			request = new Request.Builder().url(Constant.SERVER_URL + urlOrigin).build();
		}

		Call call = okHttpClient.newCall(request);
		call.enqueue(new Callback() {

			@Override
			public void onFailure(Call arg0, IOException arg1) {
				log.info("http返回{}", "网络异常" + arg1.getMessage());
				ShowUtils.errorMessage("网络异常" + arg1.getMessage());
			}

			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				if (arg1.code() == 200) {
					ReceiveMessageManager.defaultManage().HttpResponse(arg1.body().string(), urlOrigin);
				} else {
					log.info("http返回{}", "网络异常" + arg1.code());
					ShowUtils.errorMessage("网络异常" + arg1.code());
				}
			}
		});
	}

	/**
	 * 使用http发送消息（post方式）
	 * 
	 * @param urlOrigin
	 *            网络访问接口
	 * @param requestParams
	 *            请求参数
	 */
	public static void sendMessagePost(final String urlOrigin, Map<String, String> requestParams) {
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder().url(Constant.SERVER_URL + urlOrigin)
				.post(parsePostParams(requestParams)).build();
		Call call = okHttpClient.newCall(request);
		call.enqueue(new Callback() {

			@Override
			public void onFailure(Call arg0, IOException arg1) {
				log.info("http返回{}", "网络异常" + arg1.getMessage());
				ShowUtils.errorMessage("网络异常" + arg1.getMessage());
			}

			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				if (arg1.code() == 200) {
					ReceiveMessageManager.defaultManage().HttpResponse(arg1.body().string(), urlOrigin);
				} else {
					log.info("http返回{}", "网络异常" + arg1.code());
					ShowUtils.errorMessage("网络异常" + arg1.code());
				}
			}
		});
	}

	/**
	 * 解析请求参数（get方式）
	 * 
	 * @param map
	 *            请求参数
	 * @return 已解析的请求参数
	 */
	private static String parseGetParams(Map<String, String> map) {
		if (map == null) {
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			stringBuffer.append(entry.getKey() + "=" + entry.getValue());
			stringBuffer.append("&");
		}
		String params = stringBuffer.toString();
		if (params.endsWith("&")) {
			params = params.substring(0, params.length() - 1);
		}
		return params;
	}

	/**
	 * 解析请求参数（post方式）
	 * 
	 * @param map
	 *            请求参数
	 * @return 已解析的请求参数
	 */
	private static RequestBody parsePostParams(Map<String, String> map) {
		Builder builder = new Builder();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			builder.add(entry.getKey(), entry.getValue());
		}
		return builder.build();
	}
}
