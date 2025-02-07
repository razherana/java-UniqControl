package tests;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import mg.razherana.uniqcontrol.UniqController;
import mg.razherana.uniqcontrol.annotations.Parameter;
import mg.razherana.uniqcontrol.annotations.PathParameter;
import mg.razherana.uniqcontrol.annotations.RouteMethod;

@WebServlet("/home/*")
public class HomeController extends UniqController {
  @Override
  protected String[] routeBase() {
    return new String[] { "/home" };
  }

  @Override
  protected void routes() throws ServletException {
    get("/", (req, res, __) -> {
      attribute("name", parameter("name"));

      passForward("/views/home.jsp");
    });

    get("/users/@id[[0-9]+]", "userDetails");

    get("/users", "userDetails2");
  }

  @RouteMethod
  public void userDetails(@PathParameter int id,
      @Parameter(defaultValue = "Please add a name", required = false) String name)
      throws ServletException, IOException {
    attribute("id", id);
    attribute("name", name);
    passForward("/views/userDetails.jsp");
  }

  @RouteMethod("userDetails2")
  public void userDetails() throws ServletException, IOException {
    attribute("id", 0);
    attribute("name", "Please add a name");
    passForward("/views/userDetails.jsp");
  }
}