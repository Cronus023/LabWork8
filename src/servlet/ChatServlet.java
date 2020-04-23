package servlet;

import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import entity.ChatMessage;
import entity.ChatUser;


public class ChatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// current users
	protected HashMap<String, ChatUser> activeUsers;
	// chat list of messages
	protected ArrayList<ChatMessage> messages;
	
	@SuppressWarnings("unchecked")
	public void init() throws ServletException {
		super.init();
		// get users and messages from servlet context
		activeUsers = (HashMap<String, ChatUser>)
		    getServletContext().getAttribute("activeUsers");
		messages = (ArrayList<ChatMessage>)
		    getServletContext().getAttribute("messages");
		
		if (activeUsers==null) {
			// create new map
			activeUsers = new HashMap<String, ChatUser>();
			// set new map in servlet context (other servlets can get this map)
			getServletContext().setAttribute("activeUsers", activeUsers);
		}
		
		if (messages==null) {
			// create new list
			messages = new ArrayList<ChatMessage>(100);
			// set new list in servlet context (other servlets can get this map)
			getServletContext().setAttribute("messages", messages);
		}

	}

}
