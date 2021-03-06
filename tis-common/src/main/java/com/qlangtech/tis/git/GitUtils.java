/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.git;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.collect.Sets;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GitUtils {

	private static final ConfigFileContext.Header PRIVATE_TOKEN = new ConfigFileContext.Header("PRIVATE-TOKEN",
			"123456");

	private static final ConfigFileContext.Header DELETE_METHOD = new ConfigFileContext.Header("method", "DELETE");

	public static final int WORKFLOW_GIT_PROJECT_ID = 1372;

	public static final int DATASOURCE_PROJECT_ID = 1375;

	public static final int DATASOURCE_PROJECT_ID_ONLINE = 1374;

	private GitUtils() {
	}

	private static final GitUtils SINGLEN = new GitUtils();

	public static GitUtils $() {
		return SINGLEN;
	}

	public static void main(String[] args) throws Exception {
	}

	@SuppressWarnings("all")
	public IncrMonitorIndexs getIncrMonitorIndexs(RunEnvironment runtime) {
		IncrMonitorIndexs result = new IncrMonitorIndexs();
		return result;

	}

	public static class IncrMonitorIndexs {

		public final Set<String> includes = Sets.newHashSet();

		public void addInclude(String indexName) {
			this.includes.add(indexName);
		}

		public void addIncludeAll(Collection<String> indexNames) {
			this.includes.addAll(indexNames);
		}
	}

	/**
	 * http://localhost/tis-fullbuild-workflow/blob/
	 * master/mars/hive/search4totalpay/join.xml
	 *
	 * @param collectionName
	 * @param runtime
	 * @return
	 */
	@SuppressWarnings("all")
	public InputStream getHiveJoinTaskConfig(String collectionName, RunEnvironment runtime) {
		URL url = null;
		try {
			String filePath = getHiveJoinPath(collectionName);
			url = new URL("http://git.tis.com/api/v3/projects/" + WORKFLOW_GIT_PROJECT_ID
					+ "/repository/files?file_path=" + filePath + "&ref="
					+ (runtime == RunEnvironment.DAILY ? "develop" : "master"));
			return getFileContent(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException("url:" + url, e);
		}

	}

	public static String getHiveJoinPath(String collectionName) {
		return "mars/hive/" + collectionName + "/join.xml";
	}

	private InputStream getFileContent(URL url) {
		return HttpUtils.processContent(url, new GitStreamProcess<InputStream>() {

			@Override
			public InputStream p(int status, InputStream stream, String md5) {
				try {
					JSONTokener tokener = new JSONTokener(IOUtils.toString(stream, "utf8"));
					JSONObject o = new JSONObject(tokener);
					return new ByteArrayInputStream(Base64.decodeBase64(o.getString("content")));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		});
	}

	private abstract static class GitStreamProcess<T> extends StreamProcess<T> {

		@Override
		public final List<ConfigFileContext.Header> getHeaders() {
			return createHeaders(super.getHeaders());
		}
	}

	private static List<ConfigFileContext.Header> createHeaders(List<ConfigFileContext.Header> orther) {
		List<ConfigFileContext.Header> heads = new ArrayList<>();
		heads.addAll(orther);
		heads.add(PRIVATE_TOKEN);
		return heads;
	}

	private static void createFile() {
	}
}
