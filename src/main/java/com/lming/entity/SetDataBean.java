package com.lming.entity;

import lombok.Data;

@Data
public class SetDataBean {

	private String sign;
	private String timestamp;
	private String bdstoken;
	private String uk;
	private String shareid;
	private FileList file_list;
	private String photo; // 头像
	private String linkusername;

	@Data
	public static class FileList {
		String errno;
		List[] list;
	}

	@Data
	public static class List {
		String app_id;
		String fs_id;
		String server_filename;
		String server_mtime;
		String size;
	}

}
