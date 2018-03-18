package db.mysql;

import java.util.HashSet;
import entity.Item.ItemBuilder;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import external.TicketMasterAPI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLConnection implements DBConnection{
	private Connection conn;
	
	public MySQLConnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void close() {
		if(conn != null) {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if(conn == null) return;
		try {
			String sql = "INSERT INTO history (user_id,item_id) VALUES(?,?)";
			PreparedStatement statement = conn.prepareStatement(sql);
			for(String itemId:itemIds) {
				statement.setString(1, userId);
				statement.setString(2, itemId);
				statement.execute();
			}
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if(conn == null) return;
		try {
			String sql = "DELETE FROM history where user_id = ? and item_id =? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			for(String itemId:itemIds) {
				statement.setString(1, userId);
				statement.setString(2, itemId);
				statement.execute();
			}
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if(conn ==null) {
			return new HashSet<String>();
		}
		Set<String> favoriteItems = new HashSet<>();
		
		try {
			String sql = "select item_id from history where user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				favoriteItems.add(rs.getString("item_id"));
			}
			
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if(conn ==null) {
			return new HashSet<Item>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		try {
			for(String itemId :itemIds) {
				String sql ="select * from items where item_id = ?";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, itemId);
				
				ResultSet rs = statement.executeQuery();
				// item_id,   name,  rating, url, image_url ,  ...., distance  
                //                                                             
                //  abcd      abcd     1      xx      xxx             5     
                //  1234      1234     2      yy      yyy             5     <-

				ItemBuilder builder = new ItemBuilder();
                while (rs.next()) {
                    builder.setItemId(rs.getString("item_id"));
                    builder.setName(rs.getString("name"));
                    builder.setAddress(rs.getString("address"));
                    builder.setImageUrl(rs.getString("image_url"));
                    builder.setUrl(rs.getString("url"));
                    builder.setCategories(getCategories(itemId));
                    builder.setDistance(rs.getDouble("distance"));
                    
                    favoriteItems.add(builder.build());
                }

			}
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
            return null;
        }
        Set<String> categories = new HashSet<>();
        try {
            String sql = "SELECT category from categories WHERE item_id = ? ";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, itemId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return categories;
    }

	

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
        List<Item> items = tmAPI.search(lat, lon, term);
        for(Item item:items) {
        	saveItem(item);
        }
		return items;
	}

	@Override
	public void saveItem(Item item){
		if(conn == null) return;
		try {
			String sql ="INSERT IGNORE INTO items VALUES(?,?,?,?,?,?)";//SQL injection
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			statement.setDouble(6, item.getDistance());
			statement.execute();	//executeQuery will return some thing
			
			sql ="INSERT IGNORE INTO categories VALUES(?,?)";//SQL injection
			for(String category:item.getCategories()) {
			statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, category);
			}
			statement.execute();	//executeQuery will return some thing
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
            return null;
        }
        String name = "";
        try {
            String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return name;

	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
            return false;
        }
        try {
            String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;

		
	}

}
