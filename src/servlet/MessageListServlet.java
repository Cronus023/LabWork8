package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javafx.scene.control.Alert;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import entity.ChatMessage;
import entity.ChatUser;

@WebServlet("/messages")
public class MessageListServlet extends ChatServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if((String)request.getSession().getAttribute("name") == null){
			response.sendError(404, "Несанкционированный просмотр чата!!!! Ай Ай АЙ ");
		}
		// Установить кодировку HTTP-ответа UTF-8
		response.setCharacterEncoding("utf8");
		// Получить доступ к потоку вывода HTTP-ответа
		PrintWriter pw = response.getWriter();
		// Записть в поток HTML-разметку страницы
		pw.println("<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/><meta http-equiv='refresh' content='10'></head>");
		pw.println("<body>");
		pw.println("<a href='/chat/view.html'>Перейти на главную страницу</a>");
		pw.println("<h1 align='center'>Чат</h1>");
		// В обратном порядке записать в поток HTML-разметку для каждогo сообщения
		for (int i=messages.size()-1; i>=0; i--) {
		    ChatMessage aMessage = messages.get(i);
		    pw.println("<div><strong>" + aMessage.getAuthor().getName()+ "</strong>: " + aMessage.getMessage() + "</div>");
		}
		pw.println("</body></html>");
	}

}
