/**
 * @file: UserDatabase.java
 *
 * @author: Xiaojun Li
 *
 * @date: April 29, 2016 1:13:37 AM EST
 *
 */


import java.util.HashMap;

public class UserDatabase {

    private HashMap<String, String> map = new HashMap<String, String>();
    public UserDatabase() {
        // TODO Auto-generated constructor stub
        map.put("user1", "user1");
        map.put("user2", "user2");
        map.put("user3", "user3");
    }
    
    public HashMap<String, String> getMap() {
        return map;
    }

    public void setMap(String user, String pass) {
        map.put(user, pass);
    }

}
