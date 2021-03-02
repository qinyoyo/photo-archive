package qinyoyo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

public class HttpUtils {
	/**
	 * 将http返回转换为JSONObject
	 * @param response  http响应
	 * @return 转换结果
	 * @throws IOException 异常
	 */
	public static <T> T getHttpEntity(HttpResponse response, Class<T> clazz) throws IOException {
		if (response == null) return null;
		HttpEntity entity = response.getEntity();
		if (entity.getContentLength() == 0 || !entity.isStreaming()) {
			return null;
		}
		String line = null;
		StringBuilder entityStringBuilder = new StringBuilder();
		BufferedReader b = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"), 8 * 1024);
		while ((line = b.readLine()) != null) {
			entityStringBuilder.append(line + "\n");
		}
		Gson gson = new GsonBuilder()
				.create();
		return gson.fromJson(entityStringBuilder.toString(),clazz);
	}
	/**
	 * 将http返回转换为String
	 * @param response  http响应
	 * @return 转换结果
	 * @throws IOException 异常
	 */
	public static String getHttpString(HttpResponse response) throws IOException {
		if (response==null) return null;
		HttpEntity entity = response.getEntity();
		if (entity.getContentLength() == 0 || !entity.isStreaming()) {
			return "{\"code\":" + response.getStatusLine().getStatusCode() + "}";
		}
		String line = null;
		StringBuilder entityStringBuilder = new StringBuilder();
		BufferedReader b = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"), 8 * 1024);
		while ((line = b.readLine()) != null) {
			entityStringBuilder.append(line + "\n");
		}
		return entityStringBuilder.toString();
	}	

	/*
	 * get
	 * 
	 * @param host
	 * @param headers
	 * @param querys
	 * @return  HttpResponse
	 * @throws IOException 出错抛出异常
	 */
	public static HttpResponse doGet(String host, Map<String, Object> headers, Map<String, Object> querys) throws IOException {    	
		HttpClient httpClient = wrapClient(host);
		HttpGet request = new HttpGet(buildUrl(host, querys));
		if (headers != null)
			for (Map.Entry<String, Object> e : headers.entrySet()) {
				request.addHeader(e.getKey(), Util.toString(e.getValue()));
			}
		return httpClient.execute(request);
    }
	/**
	 * 以json与http进行get操作
	 * @param host  url地址
	 * @param querys  可选的参数
	 * @return  json结果
	 * @throws IOException 异常
	 */
	@SuppressWarnings("serial")
	public static <T> T doJsonGet(String host, Map<String, Object> querys, Class<T> clazz) throws IOException {
		Map<String, Object> headers=new HashMap<String, Object>() {{
			put("content-type", "application/json; charset=UTF-8");
			put("Accept-Charset", "UTF-8");
            put("Connection", "Keep-Alive");
		}};
		return getHttpEntity(doGet(host,headers,querys),clazz);
	}
	/*
	 * post form
	 * 
	 * @param host
	 * @param headers
	 * @param querys
	 * @param bodys
	 * @return
	 * @throws IOException 出错抛出异常
	 */
	public static HttpResponse doPost(String host, Map<String, Object> headers,	Map<String, Object> querys,	Map<String, Object> bodys)  throws IOException {    	
			HttpClient httpClient = wrapClient(host);
			HttpPost request = new HttpPost(buildUrl(host, querys));
			if (headers != null)
				for (Map.Entry<String, Object> e : headers.entrySet()) {
					request.addHeader(e.getKey(), Util.toString(e.getValue()));
				}
			if (bodys != null) {
				List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();

				for (String key : bodys.keySet()) {
					nameValuePairList.add(new BasicNameValuePair(key, Util.toString(bodys.get(key))));
				}
				UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, "utf-8");
				formEntity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
				request.setEntity(formEntity);
			}

			return httpClient.execute(request);
    }	
	/**
	 * 以json与http进行post操作
	 * @param host  url地址
	 * @param headers  请求头参数
	 * @param querys  可选的参数
	 * @param bodys  post的参数
	 * @return  json结果
	 * @throws IOException 异常
	 */
	@SuppressWarnings("serial")
	public static <T> T doJsonPost(String host, Map<String, Object> headers,Map<String, Object> querys,Map<String, Object> bodys, Class<T> clazz)  throws IOException {
		if (headers == null) headers = new HashMap<String, Object>();
		headers.put("content-type", "application/json; charset=UTF-8");
		headers.put("Accept-Charset", "UTF-8");
		headers.put("Connection", "Keep-Alive");
		return getHttpEntity(doPost(host, headers, querys, bodys),clazz);
	}
	/*
	 * Post String
	 * 
	 * @param host
	 * @param headers
	 * @param querys
	 * @param body
	 * @return
	 * @throws IOException 出错抛出异常
	 */
	public static HttpResponse doPost(String host, Map<String, Object> headers,Map<String, Object> querys,String body) throws IOException {    	
		HttpClient httpClient = wrapClient(host);

		HttpPost request = new HttpPost(buildUrl(host, querys));
		if (headers != null)
			for (Map.Entry<String, Object> e : headers.entrySet()) {
				request.addHeader(e.getKey(), Util.toString(e.getValue()));
			}

		if (!Util.isEmpty(body)) {
			request.setEntity(new StringEntity(body, "utf-8"));
		}

		return httpClient.execute(request);
    }
	/**
	 * 以json与http进行post操作
	 * @param host  url地址
	 * @param querys  可选的参数
	 * @param body  post的参数
	 * @return  json结果
	 * @throws IOException 异常
	 */
	@SuppressWarnings("serial")
	public static <T> T doJsonPost(String host, Map<String, Object> querys,String body,Class<T> clazz)  throws IOException {
		Map<String, Object> headers=new HashMap<String, Object>() {{
			put("content-type", "application/json; charset=UTF-8");
			put("Accept-Charset", "UTF-8");
            put("Connection", "Keep-Alive");
		}};
		return getHttpEntity(doPost(host,headers,querys,body),clazz);
	}
	/*
	 * Post stream
	 * 
	 * @param host
	 * @param headers
	 * @param querys
	 * @param body
	 * @return
	 * @throws IOException 出错抛出异常
	 */
	public static HttpResponse doPost(String host, Map<String, Object> headers, 	Map<String, Object> querys,	byte[] body) throws IOException {    	
		HttpClient httpClient = wrapClient(host);

		HttpPost request = new HttpPost(buildUrl(host, querys));
		if (headers != null)
			for (Map.Entry<String, Object> e : headers.entrySet()) {
				request.addHeader(e.getKey(), Util.toString(e.getValue()));
			}

		if (body != null) {
			request.setEntity(new ByteArrayEntity(body));
		}

		return httpClient.execute(request);
    }
	public static HttpResponse doPost(String host) throws IOException {
		Map<String, Object> headers = null;
		Map<String, Object> querys = null;
		Map<String, Object> bodys=null;
		return doPost(host,headers,querys,bodys);
	}
	/*
	 * Put String
	 * @param host
	 * @param headers
	 * @param querys
	 * @param body
	 * @return
	 * @throws IOException 出错抛出异常
	 */
	public static HttpResponse doPut(String host, Map<String, Object> headers,Map<String, Object> querys,	String body) throws IOException {    	
		HttpClient httpClient = wrapClient(host);
		HttpPut request = new HttpPut(buildUrl(host, querys));
		if (headers != null)
			for (Map.Entry<String, Object> e : headers.entrySet()) {
				request.addHeader(e.getKey(), Util.toString(e.getValue()));
			}
		if (!Util.isEmpty(body)) {
			request.setEntity(new StringEntity(body, "utf-8"));
		}

		return httpClient.execute(request);
    }
	
	/*
	 * Put stream
	 * @param host
	 * @param headers
	 * @param querys
	 * @param body
	 * @return
	 * @throws IOException 出错抛出异常
	 */
	public static HttpResponse doPut(String host, String path, String method, Map<String, Object> headers,	Map<String, Object> querys,	byte[] body) throws IOException {    	
			HttpClient httpClient = wrapClient(host);
			HttpPut request = new HttpPut(buildUrl(host, querys));
			if (headers != null)
				for (Map.Entry<String, Object> e : headers.entrySet()) {
					request.addHeader(e.getKey(), Util.toString(e.getValue()));
				}
			if (body != null) {
				request.setEntity(new ByteArrayEntity(body));
			}
			return httpClient.execute(request);
    }
	
	/*
	 * Delete
	 *  
	 * @param host
	 * @param headers
	 * @param querys
	 * @return
	 * @throws IOException 出错抛出异常
	 */
	public static HttpResponse doDelete(String host, Map<String, Object> headers,	Map<String, Object> querys)
            throws IOException {
		HttpClient httpClient = wrapClient(host);
		HttpDelete request = new HttpDelete(buildUrl(host, querys));
		if (headers != null)
			for (Map.Entry<String, Object> e : headers.entrySet()) {
				request.addHeader(e.getKey(), Util.toString(e.getValue()));
			}
		return httpClient.execute(request);
    }
	
	private static String buildUrl(String host, Map<String, Object> querys) throws IOException {
    	StringBuilder sbUrl = new StringBuilder();
    	sbUrl.append(host);
    	if (null != querys) {
    		StringBuilder sbQuery = new StringBuilder();
        	for (Map.Entry<String, Object> query : querys.entrySet()) {
        		if (0 < sbQuery.length()) {
        			sbQuery.append("&");
        		}
        		if (Util.isEmpty(query.getKey()) && !Util.isEmpty(query.getValue())) {
        			sbQuery.append(query.getValue());
                }
        		if (!Util.isEmpty(query.getKey())) {
        			sbQuery.append(query.getKey());
        			if (!Util.isEmpty(query.getValue())) {
        				sbQuery.append("=");
        				sbQuery.append(URLEncoder.encode(Util.toString(query.getValue()), "utf-8"));
        			}        			
                }
        	}
        	if (0 < sbQuery.length()) {
        		sbUrl.append(host.indexOf("?")>=0 ? "&":"?").append(sbQuery);
        	}
        }
    	
    	return sbUrl.toString();
    }
	
	private static HttpClient wrapClient(String host) {
		if (host.toLowerCase().startsWith("https://")) {
			return sslClient();
		} else return HttpClients.createDefault(); 
	}
	
	private static HttpClient sslClient() {        
        try {
            // 在调用SSL之前需要重写验证方法，取消检测SSL
            X509TrustManager trustManager = new X509TrustManager() {
                @Override public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override public void checkClientTrusted(X509Certificate[] xcs, String str) {}
                @Override public void checkServerTrusted(X509Certificate[] xcs, String str) {}
            };
            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[] { trustManager }, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
            // 创建Registry
            RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setExpectContinueEnabled(Boolean.TRUE).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM,AuthSchemes.DIGEST))
                    .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https",socketFactory).build();
            // 创建ConnectionManager，添加Connection配置信息
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager(connectionManager).setSSLSocketFactory(socketFactory)
                    .setDefaultRequestConfig(requestConfig).build();
            return closeableHttpClient;
        } catch (KeyManagementException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
     }
}