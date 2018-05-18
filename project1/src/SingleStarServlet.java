import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
	private static final long serialVersionUID = 2L;

	// Create a dataSource which registered in web.xml
	//@Resource(name = "jdbc/moviedb")
	//private DataSource dataSource;
	String loginUser = "mytestuser";
    String loginPasswd = "mypassword";

    String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json"); // Response mime type

		// Retrieve parameter id from url request.
		String id = request.getParameter("id");

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {
			// Get a connection from dataSource
			//Connection dbcon = dataSource.getConnection();
			// create database connection
    		Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

			// Construct a query with parameter represented by "?"
    		String query = "SELECT s.id AS starId, s.name, s.birthYear, m.id AS movieId, m.title, m.director, m.year, r.rating, r.numVotes, GROUP_CONCAT(DISTINCT g.name SEPARATOR ', ') AS genres "
    				+ "FROM stars AS s LEFT JOIN stars_in_movies AS sm ON s.id = sm.starId LEFT JOIN movies AS m ON sm.movieId = m.id LEFT JOIN ratings AS r ON m.id = r.movieId LEFT JOIN genres_in_movies AS gm ON m.id = gm.movieId LEFT JOIN genres AS g ON gm.genreId = g.id "
    				+ "WHERE s.id = ?	 GROUP BY m.id;";

			// Declare our statement
			PreparedStatement statement = connection.prepareStatement(query);

			// Set the parameter represented by "?" in the query to the id we get from url,
			// num 1 indicates the first "?" in the query
			statement.setString(1, id);

			// Perform the query
			ResultSet rs = statement.executeQuery();

			JsonArray jsonArrayMovies = new JsonArray();

			// Iterate through each row of rs
			while (rs.next()) {
				String starId = rs.getString("starId");
				String starName = rs.getString("name");
				String starDob = rs.getString("birthYear");
				
				String movieId = rs.getString("movieId");
				String movieTitle = rs.getString("title");
				String movieYear = rs.getString("year");
				String movieDirector = rs.getString("director");
				String movieRating = rs.getString("rating");

				if(rs.wasNull())
					movieRating = "N/A";
				String movieNumVotes = rs.getString("numVotes");
				if(rs.wasNull())
					movieNumVotes = "N/A";
				String movieGenres = rs.getString("genres");
				if(rs.wasNull())
					movieGenres = "N/A";

				JsonArray jsonArrayGenres = new JsonArray();
				if(movieGenres.equals("N/A"))
				{
					JsonObject jsonGenre = new JsonObject();
					jsonGenre.addProperty("genre_name", "N/A");
					jsonArrayGenres.add(jsonGenre);
				}
				else
				{
					for(String genre: movieGenres.split(", "))
					{
						JsonObject jsonGenre = new JsonObject();
						jsonGenre.addProperty("genre_name", genre);
						jsonArrayGenres.add(jsonGenre);
					}
				}
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("star_id", starId);
				jsonObject.addProperty("star_name", starName);
				jsonObject.addProperty("star_dob", starDob);
				jsonObject.addProperty("movie_id", movieId);
				jsonObject.addProperty("movie_title", movieTitle);
				jsonObject.addProperty("movie_year", movieYear);
				jsonObject.addProperty("movie_director", movieDirector);
				jsonObject.addProperty("movie_rating", movieRating);
				jsonObject.addProperty("movie_num_votes", movieNumVotes);
				jsonObject.add("movie_genres", jsonArrayGenres);

				jsonArrayMovies.add(jsonObject);
			}
			
            // write JSON string to output
            out.write(jsonArrayMovies.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

			rs.close();
			statement.close();
			connection.close();
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		out.close();

	}

}
