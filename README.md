# UniqControl

## Overview

This is a FrontController using the servlet-api.

## How to use

- Make some controllers like HomeController, add WebServlet and everything. Make that controller extends the UniqController.
- Override the method routeBase, it should return the bases of the routes like "/home" or "/user" or both.
- Override the method route, you add every routes you want to use in your controller there.
- Use the method get, post, put, delete to add the routes.

## Types

There are 2 types of how to add routes, the first one is using FunctionalInterface, the second one is using the `@RouteMethod` methods.

### FunctionalInterface

```java
@WebServlet(name = "HomeController", urlPatterns = {"/home/*"})
public class HomeController extends UniqController {

    @Override
    public String[] routeBase() {
        return new String[]{"/home"};
    }

    @Override
    public void route() {
        get("/", (req, res, pathVars) -> {
            res.getWriter().println("Hello World");
        });

        // You can add middlewares too !
        get("/list", (req, res, pathVars) -> {
            res.getWriter().println("Hello World Lists");
        }).addBeforeMiddleware((req, res, pathVars) -> {
            System.out.println("Before");
        }).addAfterMiddleware((req, res, pathVars) -> {
            System.out.println("After");
        });
    }
}
```

### @RouteMethod

```java
@WebServlet(name = "HomeController", urlPatterns = {"/home/*"})
public class HomeController extends UniqController {

    @Override
    public String[] routeBase() {
        return new String[]{"/home"};
    }

    @Override
    public void route() {
        get("/", "index");

        // Maybe you want to use a pathVar, and some parameters
        // You can add middlewares too !
        get("/list", "list").addBeforeMiddleware((req, res, pathVars) -> {
            System.out.println("Before");
        }).addAfterMiddleware((req, res, pathVars) -> {
            System.out.println("After");
        });

        // Maybe make some parameters optional, make some parameters multiple
        get("/list2", "list2");

        // Or just a simple route with response and pathVars
        get("/list3", "list3");
    }

    @RouteMethod(name = "index")
    public void index() throws IOException, ServletException {
      res.getWriter().println("Hello World");
    }

    // Maybe you want to use a pathVar, and some parameters
    @RouteMethod
    public void list(@PathParameter int id, @Parameter String name) throws IOException, ServletException {
      res.getWriter().println("Hello World Lists " + id + " " + name);
    }

    // Maybe make some parameters optional, make some parameters multiple
    // You can use the same method's name but make the name() in the annotation different
    @RouteMethod(name = "list2")
    public void list(
      @PathParameter
        int id,
      @Parameter(required = false, defaultValue = "NoName")
        String name,
      @Parameter(multiple = true)
        int[] choices
    ) throws IOException, ServletException {
      res.getWriter().println("Hello World Lists " + id + " " + name + " " + lastName + " ");
      res.getWriter().println("Choices : " + Arrays.toString(choices));
    }

    // Or just a simple route with response and pathVars
    @RouteMethod(name = "list3")
    public void list3(
      HttpServletRequest req,
      /* Who said it isn't dynamic ? */
      HttpServletRequest req2,
      HashMap<String, String> pathParameters,
      HttpServletResponse res,
      HttpServletResponse res2
    ) throws IOException, ServletException {
      // Keep in mind that the req == req2 and res == res2
      // You can add more of the request if you want 🤣
      res.getWriter().println("Hello World Lists");
    }
}
```
