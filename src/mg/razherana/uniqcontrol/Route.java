package mg.razherana.uniqcontrol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.uniqcontrol.middlewares.BeforeMiddleware;
import mg.razherana.uniqcontrol.middlewares.DefaultMiddleware;
import mg.razherana.uniqcontrol.middlewares.AfterMiddleware;

public class Route {
  private RouteInterface method;
  private ArrayList<BeforeMiddleware> befores = new ArrayList<>();
  private ArrayList<AfterMiddleware> afters = new ArrayList<>();

  public Route(RouteInterface method) {
    this.method = method;
  }

  public RouteInterface getMethod() {
    return method;
  }

  public void setMethod(RouteInterface method) {
    this.method = method;
  }

  public ArrayList<BeforeMiddleware> getBefores() {
    return befores;
  }

  public void setBefores(ArrayList<BeforeMiddleware> befores) {
    this.befores = befores;
  }

  public ArrayList<AfterMiddleware> getAfters() {
    return afters;
  }

  public void setAfters(ArrayList<AfterMiddleware> afters) {
    this.afters = afters;
  }

  public Route addAfterMiddleware(AfterMiddleware middleware) {
    afters.add(Objects.requireNonNull(middleware));
    return this;
  }

  public Route addBeforeMiddleware(BeforeMiddleware middleware) {
    befores.add(Objects.requireNonNull(middleware));
    return this;
  }

  public Route addMiddleware(DefaultMiddleware middleware) {
    Objects.requireNonNull(middleware);

    if (middleware instanceof AfterMiddleware a) {
      afters.add(a);
      return this;
    }

    befores.add((BeforeMiddleware) middleware);
    return this;
  }

  public void handle(HttpServletRequest request, HttpServletResponse response, HashMap<String, String> vars)
      throws ServletException, IOException {
    for (BeforeMiddleware beforeMiddleware : befores)
      if (!beforeMiddleware.handle(request, response, vars))
        return;

    method.handle(request, response, vars);

    for (AfterMiddleware afterMiddleware : afters)
      if (!afterMiddleware.handle(request, response, vars))
        return;
  }
}
