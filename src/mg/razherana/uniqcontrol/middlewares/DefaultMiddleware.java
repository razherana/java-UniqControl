package mg.razherana.uniqcontrol.middlewares;

import java.io.IOException;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface DefaultMiddleware {
  /**
   * @param req
   * @param resp
   * @param vars 
   * @return True if should continue to process the request, else false
   * @throws ServletException
   * @throws IOException
   */
  abstract public boolean handle(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String> vars) throws ServletException, IOException;
}
