package mg.razherana.uniqcontrol;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RouteHelper {
  private HttpServletRequest request;
  private HttpServletResponse response;
  String contextPathTrimmed;

  public RouteHelper(HttpServletRequest request, HttpServletResponse response) {
    setRequest(request);
    setResponse(response);
    contextPathTrimmed = request.getContextPath().trim().replaceAll("/+$", "");
  }

  public String route(String path) {
    return contextPathTrimmed + "/" + path.trim().replaceAll("^/|/$", "");
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }
}
