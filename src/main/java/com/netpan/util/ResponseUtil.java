package com.netpan.util;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class ResponseUtil {

	public static void write(HttpServletResponse response,Object o){
		try{
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out=response.getWriter();
			out.println(o.toString());
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
