package com.lming.tools;

    import java.io.BufferedReader;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.io.OutputStreamWriter;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.net.URLConnection;
    import java.util.HashMap;
    import java.util.Map;

    public class HttpUtils {
        /**
         * 向指定URL发送GET方法的请求 返回cookie，body等
         * 会返回流里面的内容，如果下载大文件，需要修改，实时保存，避免内存溢出
         */
        public static Map<String, String> get(String url, String cookie) {
            BufferedReader in = null;
            Map<String, String> map = new HashMap<String, String>();

            try {
                URL realUrl = new URL(url);
                // 打开和URL之间的连接
                URLConnection connection = realUrl.openConnection();
                // 设置通用的请求属性
                connection.setRequestProperty("accept", "*/*");
                connection.setRequestProperty("connection", "Keep-Alive");
                connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                if (null != cookie) {
                    //System.out.println("携带的Cookie:" + cookie);
                    connection.setRequestProperty("Cookie", cookie);
                }
                // 建立实际的连接
                connection.connect();
                // 定义 BufferedReader输入流来读取URL的响应
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line + "\n");
                    //System.out.println("内容：" + line);
                }
                String c = connection.getHeaderField("Set-Cookie");
                map.put("cookie", c);
                map.put("body", sb.toString());
                return map;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return null;
        }

        /**
         * 保存验证码
         */
        public static Map<String, String> saveImage(String url, String cookie) {
            Map<String, String> map = new HashMap<String, String>();
            InputStream in = null;
            FileOutputStream fos = null;
            try {
                URL realUrl = new URL(url);
                // 打开和URL之间的连接
                URLConnection connection = realUrl.openConnection();
                // 设置通用的请求属性
                connection.setRequestProperty("accept", "*/*");
                connection.setRequestProperty("connection", "Keep-Alive");
                connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                if (null != cookie) {
                    connection.setRequestProperty("Cookie", cookie);
                }
                // 建立实际的连接
                connection.connect();

                in = connection.getInputStream();
                fos = new FileOutputStream("img.jpeg");

                int b;
                while((b= in.read())!=-1){
                    fos.write(b);
                }

                return map;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if(fos != null){
                        fos.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return null;
        }


        static int downloadSize = 0;
        static boolean isReading = false;//是否在读流，更新进度
        static long startTime = 0 ;
        static long endTime = 0 ;
        /**
         * 下载文件
         */
        public static Map<String, String> download(String url, String cookie, String filename, final String totalSize) {
            InputStream in = null;
            FileOutputStream fos = null;
            Map<String, String> map = new HashMap<String, String>();
            try {
                URL realUrl = new URL(url);
                // 打开和URL之间的连接
                URLConnection connection = realUrl.openConnection();
                // 设置通用的请求属性
                connection.setRequestProperty("accept", "*/*");
                connection.setRequestProperty("connection", "Keep-Alive");
                connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                connection.setConnectTimeout(10000);
    //          connection.setReadTimeout(5000);
                if (null != cookie) {
                    connection.setRequestProperty("Cookie", cookie);
                }
                // 建立实际的连接
                connection.connect();

                in = connection.getInputStream();
                fos = new FileOutputStream(filename);

                System.out.println("保存文件名称：" + filename);
                System.out.println("文件总大小：" + totalSize);
                //利用字符数组读取流数据

                startTime = System.currentTimeMillis();

                //每隔1s读一次数据
                new Thread(){
                    @Override
                    public void run() {
                        String total = UnitSwitch.formatSize(Long.parseLong(totalSize));
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < 20; i++) {
                            sb.append("\b");
                        }
                        while(isReading){
                            endTime = System.currentTimeMillis();
                            String speed = UnitSwitch.calculateSpeed(downloadSize, endTime-startTime);
                            System.out.println(UnitSwitch.formatSize(downloadSize) + "/" + total + "，平均下载速率：" + speed);
                            try {
                                sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }.start();

                isReading = true;
                int len;
                byte[] arr = new byte[1024 * 8];
                while((len = in.read(arr)) != -1) {
                    fos.write(arr, 0, len);
                    downloadSize += len;
                }

                endTime = System.currentTimeMillis();
                String speed = UnitSwitch.calculateSpeed(downloadSize, endTime-startTime);
                String total = UnitSwitch.formatSize(Long.parseLong(totalSize));
                System.out.println(UnitSwitch.formatSize(downloadSize) + "/" + total + "，平均下载速率：" + speed);
                System.out.println("下载完成，总耗时：" + (endTime - startTime)/1000 + "秒");
                return map;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            } finally {
                isReading = false;
                try {
                    if (in != null) {
                        in.close();
                    }
                    if(fos != null){
                        fos.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return null;
        }

        /**
         * 发送HttpPost请求
         * 
         * @param strURL
         *            服务地址
         * @param params
         * 
         * @return 成功:返回json字符串<br/>
         */
        public static String post(String strURL, Map<String, String> params, String cookie) {
            try {
                URL url = new URL(strURL);// 创建连接
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("POST"); // 设置请求方式
                connection.setRequestProperty("Accept", "*/*"); // 设置接收数据的格式
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"); // 设置发送数据的格式
                if (null != cookie) {
                    System.out.println("携带的Cookie:" + cookie);
                    connection.setRequestProperty("Cookie", cookie);
                }

                connection.connect();
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码

                StringBuffer sb = new StringBuffer();
                for (String s : params.keySet()) {
                    sb.append(s + "=" + params.get(s) + "&");
                }
                System.out.println("携带的参数：" + sb.toString().substring(0, sb.length() - 1));
                out.append(sb.toString().substring(0, sb.length() - 1));
                out.flush();
                out.close();

                int code = connection.getResponseCode();
                BufferedReader in = null;
                if (code == 200) {
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                System.out.println("响应码：" + code);

                // 定义BufferedReader输入流来读取URL的响应
                String line;
                StringBuffer sb1 = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    sb1.append(line);
                }
                return sb1.toString();

            } catch (IOException e) {
                System.err.println(e.getMessage());
                return "error";
            }
        }
    }
