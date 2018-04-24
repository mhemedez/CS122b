import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	 private static final long serialVersionUID = 1L;


	    /**
	     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	     */
	    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
	
	    	
	    	String title = request.getParameter("title");
	        String year = request.getParameter("eyear");
	        String director = request.getParameter("director");
	        String starname = request.getParameter("starname");
	        
	        String loginUser = "mytestuser";
	        String loginPasswd = "mypassword";
	        String loginUrl = "jdbc:mysql://ec2-18-222-68-209.us-east-2.compute.amazonaws.com:3306/moviedb";
    
	        PrintWriter out = response.getWriter();
	        out.println("<html>");
	        out.println("<head><title>FabFlix</title></head>");
	        out.println("<body>");
	        
	        try 
	        {
	    		Class.forName("com.mysql.jdbc.Driver").newInstance();
	    		// create database connection
	    		Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
	    		// declare statement
	    		Statement statement = connection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, 
	    				   				ResultSet.CONCUR_READ_ONLY);
	    		// prepare query
	    	
	    		
	    		String query ="SELECT m.id, m.title, m.year, m.director, r.rating, GROUP_CONCAT(s.name SEPARATOR ', ') AS stars, GROUP_CONCAT(g.name SEPARATOR ', ') AS genres\r\n" + 
	    				"	FROM movies AS m, stars AS s, stars_in_movies AS SM, ratings AS r, genres AS g, genres_in_movies AS gm\r\n" + 
	    				"    WHERE m.id = SM.movieId AND SM.starId = s.id AND r.movieId = m.id AND g.id = gm.genreId AND gm.movieId = m.id AND"; 

	    		
    			if(title != null && !title.equals("")) 
    				query += "m.title LIKE '%" + title + "%' AND ";
    			
    			if(year != null && !year.equals("")) 
    				query += "m.year ='" + year + "' AND ";

    			if(director != null && !director.equals("")) 
    				query += "m.director LIKE '%" + director + "%' AND ";	

    			if(starname != null && !starname.equals("")) {
    				String delim = "[ ]+";
    				String[] parsed = starname.split(delim);
    				query += "s.name LIKE '"+ parsed[0] + "%' " + "OR " + "s.name LIKE '%" + parsed[1];
    			}

	    		if(query.endsWith("AND ")) 
	    		{
	    			query = query.substring(0, query.length() - 4);
	    		}
	    		
	    		query += "GROUP BY m.id "
	    				+ "ORDER BY m.year ASC "
	    				+ "LIMIT 20 "
	    				+ "OFFSET 0;";
	    		
	    		out.println(query);
	    		ResultSet resultSet = statement.executeQuery(query);
	    		
	    		
	    		ArrayList<String> movieTitles = new ArrayList<String>();
	    		HashMap<String, HashSet<String>> actors = new HashMap<String, HashSet<String>>();
	    		
	    		
	    		HashMap<String, HashSet<String>> genres = new HashMap<String, HashSet<String>>();
	    		
	    		
	    		while(resultSet.next()) {
	    			String movieName = resultSet.getString("title");
	    			actors.put(movieName, new HashSet<String>());
	    			StringTokenizer st = new StringTokenizer(resultSet.getString("stars"),",");
	    			while(st.hasMoreTokens()) {
	    				actors.get(movieName).add(st.nextToken());
	    			}
	    			
	    		}
    			
	    		
	    		
	    		
	    		
	    		
	    		
	    		
	    		
	    		
	    		
	    		if (movieTitles.size()>0) {
	    			
	                request.setAttribute("movies", movieTitles);
	                request.setAttribute("query", query);
	                request.setAttribute("actors", actors);
	    			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/movielist.jsp");
	                dispatcher.forward(request, response);
	            }else {
	            	// no movies in the search
	            	request.setAttribute("movies", "Error no movies found with search criteria");

	    			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/movielist.jsp");
	                dispatcher.forward(request, response);
	            }
	    		
	    		

	    		
	    		statement.close();
	    		connection.close();
	    	}
	        catch (Exception e) 
	        {
	    		e.printStackTrace();	
	    		out.println("<h3> ");
    			out.println(e.getMessage());
    			out.println("</h3>");
	    	}
	        out.println("</body>");
	        out.println("</html>");
	    }
}
