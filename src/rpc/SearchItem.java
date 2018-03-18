package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import external.TicketMasterAPI;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");

		double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("lon"));
        // term can be empty or null.
        String term = request.getParameter("term");
        DBConnection conn = DBConnectionFactory.getDBConnection();
        String userId = request.getParameter("user_id");
        
        List<Item> items = conn.searchItems(lat, lon, term);
        JSONArray array = new JSONArray();
        try {
            for (Item item : items) {
            	JSONObject obj = item.toJSONObject();
            	
            	Set<Item> favoriteItems = conn.getFavoriteItems(userId);

            	obj.put("favorite", favoriteItems.contains(item));
            	//obj.append("favorite", favoriteItems.contains(item));
            	
            	
         
                array.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        RpcHelper.writeJsonArray(response, array);
        conn.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
