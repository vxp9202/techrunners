package com.javalab.sample;
import java.io.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class NewServlet
 */
public class NewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewServlet() {
        super();
        // TODO Auto-generated constructor stub
    	

    }
    

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		PrintWriter out = response.getWriter();
		//out.println("NewServlet is reached...");
		
		String keyword1 = request.getParameter("search");
		out.println("Entered query is : " + keyword1);
		String[] terms = keyword1.split(" ");
		for(String t:terms) {
			out.println(t);
		}
		//String[] myDocs;
	
		
		Matrix m = new Matrix();
		
	
		HashMap<Integer, Double> result = m.rankSearch(terms);
		System.out.println("ans:" + result);
		
		
		
		List<String> snippets = new ArrayList<String>();
		for(Integer key: result.keySet()) {
			out.println("documents are " + key + "\n");
			System.out.println("documents are " + key + "\n");
			String line=null;
			
			String path = m.folderN + "/" + m.myDocs[key.intValue()-1];
			try {
				BufferedReader reader = new BufferedReader(new FileReader(path));
					line =reader.readLine();
					snippets.add(line);
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		
		String[] finalans = snippets.toArray(new String[snippets.size()]);
		for(String s:finalans) {
			//out.println(s);
			//request.setAttribute("result", s);
			
			//out.println("-------------------------------------------------------");
			//out.println();
		}
		request.setAttribute("result", finalans);
		request.setAttribute("search", m.reformedquery);
		request.getRequestDispatcher("results.jsp").forward(request, response);
		
		//request.setAttribute("result", finalans);
		
		//response.sendRedirect("results.jsp");
		
		
	}

	



	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
		
	}

}
