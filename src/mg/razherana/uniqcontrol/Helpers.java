package mg.razherana.uniqcontrol;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Helpers {
  public static HashMap<String, String> extractVariablesFromUri(String[] splittedUri, String route) {
    String regex = "@(\\w+)(?:\\[(.*)\\])?";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(route);

    HashMap<String, String> vars = new HashMap<>();

    // Trim / from the beginning and the end of the route
    route = route.replaceAll("^/|/$", "");

    String[] splittedRoute = route.split("/");

    if (splittedRoute.length != splittedUri.length) {
      System.out.println("splittedRoute.length != splittedUri.length, splittedRoute.length = " + splittedRoute.length
          + ", splittedUri.length = " + splittedUri.length);
      System.out.println("splittedRoute = " + String.join(",", splittedRoute));
      System.out.println("splittedUri = " + String.join(",", splittedUri));
      return null;
    }

    for (int i = 0; i < splittedRoute.length; i++) {
      matcher.reset(splittedRoute[i]);
      boolean found = matcher.find();

      if (!found) {
        if (splittedRoute[i].equals(splittedUri[i]))
          continue;
        System.out.println("splittedRoute[i] != splittedUri[i], splittedRoute[i] = " + splittedRoute[i]
            + ", splittedUri[i] = " + splittedUri[i]);
        System.out.println("splittedRoute = " + String.join(",", splittedRoute));
        System.out.println("splittedUri = " + String.join(",", splittedUri));
        return null;
      }

      if (found) {
        String varName = matcher.group(1);
        String varValue = splittedUri[i];

        if (matcher.group(2) != null
            && !Pattern.compile(matcher.group(2)).matcher(varValue).matches()) {
          System.out.println("Doesn't match the regex check : " + matcher.group(2) + " " + varValue);
          return null;
        }

        vars.put(varName, varValue);
      }
    }

    return vars;
  }

  public static Object castParameterToDesiredElement(String value, Class<?> clazz) {
    switch (clazz.getSimpleName()) {
      case "String":
        return value;
      case "Integer":
      case "int":
        return Integer.parseInt(value);
      case "Double":
      case "double":
        return Double.parseDouble(value);
      case "Float":
      case "float":
        return Float.parseFloat(value);
      case "Long":
      case "long":
        return Long.parseLong(value);
      case "Short":
      case "short":
        return Short.parseShort(value);
      case "Byte":
      case "byte":
        return Byte.parseByte(value);
      case "Boolean":
      case "boolean":
        return Boolean.parseBoolean(value);
      default:
        throw new RuntimeException("Unsupported type: " + clazz.getSimpleName());
    }
  }
}
