package mg.razherana.uniqcontrol;

import java.io.IOException;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface RouteInterface {
  public void handle(HttpServletRequest request, HttpServletResponse response, HashMap<String, String> vars) throws ServletException, IOException;
}
