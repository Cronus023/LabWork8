package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import entity.ChatUser;


@WebServlet(
		urlPatterns = { "/login" }, 
		initParams = { 
				@WebInitParam(name = "SESSION_TIMEOUT", value = "3600")
		})
public class LoginServlet extends ChatServlet {
	private static final long serialVersionUID = 1L;

	private int sessionTimeout = 600;

	public void init(ServletConfig config) throws ServletException {
		//super.init();
		/*String value = getServletConfig().getInitParameter("SESSION_TIMEOUT");
		if (value!=null) {
		    sessionTimeout = Integer.parseInt(value);
		}*/
	}

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Проверить, есть ли уже в сессии заданное имя пользователя?
		String name = (String)request.getSession().getAttribute("name");
		// Извлечь из сессии сведения о предыдущей ошибке (возможной)
		String errorMessage =(String)request.getSession().getAttribute("error");
		// Идентификатор предыдущей сессии изначально пуст
		String previousSessionId = null;
		// Если в сессии имя не сохранено, то попытаться восстановить имя через cookie
		if (name==null) {
			// Найти cookie с именем sessionId
			for (Cookie aCookie: request.getCookies()) {
			    if (aCookie.getName().equals("sessionId")) {
			        // Запомнить значение этого cookie –
			        // это старый идентификатор сессии
			        previousSessionId = aCookie.getValue();
			        break;
			    }
			}
			if (previousSessionId!=null) {
				// Мы нашли session cookie
				// Попытаться найти пользователя с таким sessionId
				for (ChatUser aUser: activeUsers.values()) {
				    if(aUser.getSessionId().equals(previousSessionId)) {
				        // Мы нашли такого, т.е. восстановили имя
				        name = aUser.getName();
				        aUser.setSessionId(request.getSession().getId());
				    }
				}
			}
		}
		
		if (name!=null && !"".equals(name)) {
		    errorMessage = processLogonAttempt(name, request, response);
		}
		
		// Пользователю необходимо ввести имя. Показать форму
	
		// Получить поток вывода для HTTP-ответа
		PrintWriter pw = response.getWriter();
		pw.println("<html><head><title>Mega-chat!!</title><meta httpequiv='Content-Type' content='text/html; charset=utf-8'/></head>");
		// Если возникла ошибка - сообщить о ней
		if (errorMessage!=null) {
		    pw.println("<p><font color='red'>" + errorMessage + "</font></p>");
		}
		// Вывести форму
		pw.println("<form method='post'>Enter name:<input type='text' name='name' value=''><input type='submit' value='Login to chat'>");
		pw.println("</form></body></html>");
		// Сбросить сообщение об ошибке в сессии
		request.getSession().setAttribute("error", null);
	}


	@SuppressWarnings("deprecation")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Задать кодировку HTTP-запроса - очень важно!
		// Иначе вместо символов будет абракадабра
		request.setCharacterEncoding("UTF-8");
		// Извлечь из HTTP-запроса значение параметра 'name'
		String name = (String)request.getParameter("name");
		System.out.println(name);
		//System.out.println(request);
		// Полагаем, что изначально ошибок нет
		String errorMessage = null;
		
		if (name==null || "".equals(name)) {
			PrintWriter pw = response.getWriter();
			pw.println("<p><font color='red'>" + "Enter name!!!!" + "</font></p>");
			pw.println("<a href = '/chat/login' >Return to form</a>");
		}
		else{
			// Если name не пустое, то попытаться обработать запрос
			errorMessage = processLogonAttempt(name, request, response);
		}
		if (errorMessage!=null) {
			// Сбросить имя пользователя в сессии
			request.getSession().setAttribute("name", null);
			// Сохранить в сессии сообщение об ошибке
			request.getSession().setAttribute("error", errorMessage);
			// Переадресовать обратно на исходную страницу с формой
			response.sendRedirect(response.encodeRedirectURL("/chat/"));
		}
	}
	
	// Возвращает текстовое описание возникшей ошибки или null
	String processLogonAttempt(String name, HttpServletRequest request,HttpServletResponse response) throws IOException {
	    // Определить идентификатор Java-сессии пользователя
	    String sessionId = request.getSession().getId();
	    // Извлечь из списка объект, связанный с этим именем
	    ChatUser aUser = activeUsers.get(name);
	    if (aUser==null) {
	        // Если оно свободно, то добавить
	        // нового пользователя в список активных
	        aUser = new ChatUser(name,Calendar.getInstance().getTimeInMillis(), sessionId);
	        // Так как одновременно выполняются запросы
	        // от множества пользователей
	        // то необходима синхронизация на ресурсе
	        synchronized (activeUsers) {
	            activeUsers.put(aUser.getName(), aUser);
	        }
	    }
	    if (aUser.getSessionId().equals(sessionId) || aUser.getLastInteractionTime()<(Calendar.getInstance().getTimeInMillis()-sessionTimeout*1000)) {
	        // Если указанное имя принадлежит текущему пользователю,
	        // либо оно принадлежало кому-то другому, но сессия истекла,
	        // то одобрить запрос пользователя на это имя
	        // Обновить имя пользователя в сессии
	        request.getSession().setAttribute("name", name);
	        // Обновить время взаимодействия пользователя с сервером
	        aUser.setLastInteractionTime(Calendar.getInstance().getTimeInMillis());
	        // Обновить идентификатор сессии пользователя в cookies
	        Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
	        // Установить срок годности cookie 1 год
	        sessionIdCookie.setMaxAge(60*60*24*365);
	        // Добавить cookie в HTTP-ответ
	        response.addCookie(sessionIdCookie);
	        // Перейти к главному окну чата
	        response.sendRedirect(response.encodeRedirectURL("/chat/view.htm"));
	        // Вернуть null, т.е. сообщений об ошибках нет
	         return null;
	    }else {
	           // Сохранѐнное в сессии имя уже закреплено за кем-то другим.
	           // Извиниться, отказать и попросить ввести другое имя
	           return "Извините, но имя <strong>" + name + "</strong> уже кем-то занято. Пожалуйста выберите другое имя!";
	    }
	}

}
