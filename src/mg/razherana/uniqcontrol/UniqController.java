package mg.razherana.uniqcontrol;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.razherana.uniqcontrol.annotations.PathParameter;
import mg.razherana.uniqcontrol.exceptions.InvalidRouteException;
import mg.razherana.uniqcontrol.exceptions.UnknownMethodException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

abstract public class UniqController extends HttpServlet {
    private class RouteMethod {
        private enum Types {
            PATH, PARAMETER, REQUEST, RESPONSE, ROUTEPATHVARS
        }

        private Method method;
        private ArrayList<PathParameter> pathParameters = new ArrayList<>();
        private HashMap<String, PathParameter> pathMethodParameters = new HashMap<>();

        private List<Parameter> methodParameters = new ArrayList<>();
        private ArrayList<mg.razherana.uniqcontrol.annotations.Parameter> parameters = new ArrayList<>();
        private HashMap<String, mg.razherana.uniqcontrol.annotations.Parameter> annotMethodParameters = new HashMap<>();
        private ArrayList<Types> orderOfTypes = new ArrayList<>();

        public RouteMethod(Method method) {
            this.method = Objects.requireNonNull(method, "The method is null");

            for (Parameter parameter : (methodParameters = Arrays.asList(method.getParameters()))) {
                var pathParameter = parameter.getAnnotation(PathParameter.class);
                var parameterAnnotation = parameter.getAnnotation(mg.razherana.uniqcontrol.annotations.Parameter.class);

                if (pathParameter != null) {
                    pathParameters.add(pathParameter);
                    pathMethodParameters.put(parameter.getName(), pathParameter);
                    orderOfTypes.add(Types.PATH);
                } else if (parameterAnnotation != null) {
                    parameters.add(parameterAnnotation);
                    annotMethodParameters.put(parameter.getName(), parameterAnnotation);
                    orderOfTypes.add(Types.PARAMETER);
                } else if (parameter.getType().equals(HttpServletRequest.class)) {
                    orderOfTypes.add(Types.REQUEST);
                } else if (parameter.getType().equals(HttpServletResponse.class)) {
                    orderOfTypes.add(Types.RESPONSE);
                } else if (parameter.getType().equals(HashMap.class) && parameter.getParameterizedType().getTypeName()
                        .equals("java.util.HashMap<java.lang.String, java.lang.String>")) {
                    orderOfTypes.add(Types.ROUTEPATHVARS);
                } else {
                    throw new RuntimeException("Unsupported parameter type: " + parameter.getType().getName());
                }
            }
        }

        public RouteInterface makeInterface(HttpServletRequest request, HttpServletResponse response,
                HashMap<String, String> routeParams) throws IOException, ServletException {
            Object[] args = new Object[orderOfTypes.size()];

            int i = 0;
            int pathIndex = 0;
            int parameterIndex = 0;
            for (Types types : orderOfTypes) {
                switch (types) {
                    case PATH:
                        String name = pathParameters.get(pathIndex).name();

                        if (name.isBlank()) {
                            name = methodParameters.get(i).getName();
                            if (name.matches("arg\\d+"))
                                throw new RuntimeException(
                                        "If you wanna use the default name, you must not use arg\\d+ or add -parameters to the compiler. Use @PathParameter(name = \"\") instead.");
                        }

                        var value1 = routeParams.get(name);

                        if (value1 == null) {
                            if (pathParameters.get(pathIndex).required()) {
                                final PathParameter pathParameter = pathParameters.get(pathIndex);
                                System.out.println("Not Found for = " + name);
                                System.out.println("PathParameters = " + routeParams);
                                return (re, rs, rp) -> {
                                    rs.sendError(pathParameter.requiredError());
                                };
                            } else
                                value1 = pathParameters.get(pathIndex).defaultValue();
                        }

                        args[i] = Helpers.castParameterToDesiredElement(value1, methodParameters.get(i).getType());

                        pathIndex++;
                        break;
                    case PARAMETER:
                        String name1 = parameters.get(parameterIndex).name();

                        if (name1.isBlank()) {
                            name1 = methodParameters.get(i).getName();
                            if (name1.matches("arg\\d+"))
                                throw new RuntimeException(
                                        "If you wanna use the default name, you must not use arg\\d+ or add -parameters to the compiler. Use @Parameter(name = \"\") instead.");
                        }

                        var value = request.getParameter(name1);

                        if (value == null) {
                            if (parameters.get(parameterIndex).required()) {
                                final mg.razherana.uniqcontrol.annotations.Parameter parameter = parameters
                                        .get(parameterIndex);
                                return (re, rs, rp) -> {
                                    rs.sendError(parameter.requiredError());
                                };
                            } else
                                value = parameters.get(parameterIndex).defaultValue();
                        }

                        if (parameters.get(parameterIndex).multiple()) {
                            Object[] multiple = request.getParameterValues(parameters.get(parameterIndex).name());

                            if (multiple == null && parameters.get(parameterIndex).required()) {
                                final mg.razherana.uniqcontrol.annotations.Parameter parameter = parameters
                                        .get(parameterIndex);
                                return (re, rs, rp) -> {
                                    rs.sendError(parameter.requiredError());
                                };
                            }

                            // We cast every value inside to match the type of the parameter
                            var type = methodParameters.get(i).getType();
                            if (!type.isArray())
                                throw new RuntimeException(
                                        "The parameter " + parameters.get(parameterIndex).name()
                                                + " is not an array but the method parameter is marked as multiple");

                            type = type.getComponentType();

                            for (int j = 0; j < multiple.length; j++)
                                multiple[j] = Helpers.castParameterToDesiredElement((String) multiple[j], type);
                            args[i] = multiple;
                        } else {
                            // We cast the value before anything
                            args[i] = Helpers.castParameterToDesiredElement(value,
                                    methodParameters.get(i).getType());
                        }

                        parameterIndex++;
                        break;
                    case REQUEST:
                        args[i] = request;
                        break;
                    case RESPONSE:
                        args[i] = response;
                        break;
                    case ROUTEPATHVARS:
                        args[i] = routeParams;
                        break;
                }
                i++;
            }

            return (re, rs, rp) -> {
                try {
                    method.invoke(UniqController.this, args);
                } catch (Exception e) {
                    if (e instanceof IOException)
                        throw (IOException) e;
                    if (e instanceof ServletException)
                        throw (ServletException) e;
                    throw new RuntimeException(e);
                }
            };
        }
    }

    private HttpServletRequest globalRequest;
    private HttpServletResponse globalResponse;

    private HashMap<String, Route> getRoutes = new HashMap<>();
    private HashMap<String, Route> deleteRoutes = new HashMap<>();
    private HashMap<String, Route> putRoutes = new HashMap<>();
    private HashMap<String, Route> postRoutes = new HashMap<>();

    private HashMap<String, RouteMethod> routeMethods = new HashMap<>();

    private String[] base = new String[0];

    private Route all(String path, RouteInterface routeInterface, RouteInterface defaultRoute,
            HashMap<String, Route> container) {
        if (routeInterface == null)
            routeInterface = defaultRoute;

        Objects.requireNonNull(path, "The path of the route is null");

        path = path.trim().replaceAll("^/|/$", "");

        var route = new Route(routeInterface);

        for (String string : base) {
            container.put(string + "/" + path, route);
            System.out.println("[INFO] -> Writing route: " + string + "/" + path);
        }

        return route;
    }

    private Route all(String path, String routeMethodName, RouteInterface defaultRoute,
            HashMap<String, Route> container) {
        if (routeMethodName.isBlank())
            throw new RuntimeException("The routeMethodName is empty");

        if (!routeMethods.containsKey(routeMethodName))
            throw new RuntimeException("The method " + routeMethodName + " is not defined");

        var routeMethod = routeMethods.get(routeMethodName);
        return all(path, (req, res, all) -> routeMethod.makeInterface(req, res, all).handle(req, res, all),
                defaultRoute,
                container);
    }

    final protected Route get(String path, RouteInterface routeInterface) {
        return all(path, routeInterface, (re, rs, vars) -> super.doGet(re, rs), getRoutes);
    }

    final protected Route get(String path, String routeMethodName) {
        return all(path, routeMethodName, (re, rs, vars) -> super.doGet(re, rs), getRoutes);
    }

    final protected Route post(String path, RouteInterface routeInterface) {
        return all(path, routeInterface, (re, rs, vars) -> super.doPost(re, rs), postRoutes);
    }

    final protected Route post(String path, String routeMethodName) {
        return all(path, routeMethodName, (re, rs, vars) -> super.doPost(re, rs), postRoutes);
    }

    final protected Route put(String path, RouteInterface routeInterface) {
        return all(path, routeInterface, (re, rs, vars) -> super.doPut(re, rs), putRoutes);
    }

    final protected Route put(String path, String routeMethodName) {
        return all(path, routeMethodName, (re, rs, vars) -> super.doPut(re, rs), putRoutes);
    }

    final protected Route delete(String path, RouteInterface routeInterface) {
        return all(path, routeInterface, (re, rs, vars) -> super.doDelete(re, rs), deleteRoutes);
    }

    final protected Route delete(String path, String routeMethodName) {
        return all(path, routeMethodName, (re, rs, vars) -> super.doDelete(re, rs), deleteRoutes);
    }

    final protected void passForward(String path)
            throws ServletException, IOException {
        globalRequest.getRequestDispatcher(path).forward(globalRequest, globalResponse);
    }

    final protected void passInclude(String path)
            throws ServletException, IOException {
        globalRequest.getRequestDispatcher(path).include(globalRequest, globalResponse);
    }

    final protected void redirect(String path)
            throws IOException, ServletException {
        globalResponse.sendRedirect(
                globalRequest.getContextPath().trim().replaceAll("/+$", "") + "/"
                        + path.trim().replaceAll("^/|/$", ""));
    }

    final protected void sendError(int code)
            throws IOException {
        globalResponse.sendError(code);
    }

    final protected void sendError(int code, String message)
            throws IOException {
        globalResponse.sendError(code, message);
    }

    final protected void header(String name, String value) {
        globalResponse.setHeader(name, value);
    }

    final protected String header(String name) {
        return globalResponse.getHeader(name);
    }

    final protected void attribute(String name, Object value) {
        globalRequest.setAttribute(name, value);
    }

    final protected Object attribute(String name) {
        return globalRequest.getAttribute(name);
    }

    final protected String parameter(String name) {
        return globalRequest.getParameter(name);
    }

    final protected String[] parameters(String name) {
        return globalRequest.getParameterValues(name);
    }

    final protected HttpServletRequest request() {
        return globalRequest;
    }

    final protected HttpServletResponse response() {
        return globalResponse;
    }

    @Override
    final public void init() throws ServletException {
        if (getClass().equals(UniqController.class))
            return;

        base = Objects.requireNonNull(routeBase(), "The routeBase must be non null");

        if (base.length == 0)
            throw new RuntimeException("The routeBase is empty...");

        for (int i = 0; i < base.length; i++)
            base[i] = base[i].replaceAll("/+$", "");

        for (Method method : getClass().getDeclaredMethods()) {
            mg.razherana.uniqcontrol.annotations.RouteMethod routeMethodAnnotation = method
                    .getAnnotation(mg.razherana.uniqcontrol.annotations.RouteMethod.class);

            if (routeMethodAnnotation == null)
                continue;

            var routeMethod = new RouteMethod(method);

            String name = routeMethodAnnotation.value();

            if (name.isBlank())
                name = method.getName();

            if (routeMethods.containsKey(name))
                throw new RuntimeException("The method " + name + " is already defined");

            routeMethods.put(name, routeMethod);

            for (String route : routeMethodAnnotation.routes()) {
                String[] splitted = route.split(":");

                if (splitted.length != 2) {
                    throw new InvalidRouteException("The format of this route is incorrect " + route);
                }

                String path = "/" + splitted[1].trim().replaceAll("^/|/$", "");
                String methodRoute = splitted[0].trim().toLowerCase();
                switch (methodRoute) {
                    case "get":
                        get(path, name);
                        break;
                    case "post":
                        post(path, name);
                        break;
                    case "delete":
                        delete(path, name);
                        break;
                    case "put":
                        put(path, name);
                        break;
                    default:
                        throw new UnknownMethodException(
                                "The method " + methodRoute.toUpperCase() + " of the route " + name);
                }
            }
        }

        routes();
    }

    /**
     * Defines the bases of the routes, for example {@code "/home", "/accueil", ...}
     */
    protected String[] routeBase() {
        return new String[] { "/" };
    }

    /**
     * You define your routes here...
     */
    abstract protected void routes() throws ServletException;

    @Override
    final protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(deleteRoutes, req, resp);
    }

    @Override
    final protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(getRoutes, req, resp);
    }

    @Override
    final protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(postRoutes, req, resp);
    }

    @Override
    final protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(putRoutes, req, resp);
    }

    private void handle(HashMap<String, Route> container, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        var uri = req.getRequestURI().substring(req.getContextPath().length(), req.getRequestURI().length());

        uri = uri.replaceAll("^/|/$", "");

        String[] splittedUri = uri.split("/");

        HashMap<String, String> vars = null;

        for (String route : container.keySet()) {
            vars = Helpers.extractVariablesFromUri(splittedUri, route);

            if (vars != null) {
                container.get(route).handle(req, resp, vars);
                return;
            }
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    final protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        // We add global vars here
        globalRequest = arg0;
        globalResponse = arg1;
        System.out.println("[INFO] -> Used controller " + getClass().getName() + ", choosing the route...");
        var routeHelper = new RouteHelper(arg0, arg1);
        attribute("routeHelper", routeHelper);

        super.service(arg0, arg1);
    }
}
