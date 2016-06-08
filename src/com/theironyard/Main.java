package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();


    public static void main(String[] args) {
        Spark.staticFileLocation("public");
        Spark.init();
        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    HashMap m = new HashMap();
                    if (username == null) {
                        return new ModelAndView(m, "index.html");
                    }
                    else {
                        User user = users.get(username);
                        m.put("name", user.name);
                        m.put("messages", user.messages); // the key here must line up with the {{#____}} field
                        return new ModelAndView(m, "messages.html");
                    }
                },
                new MustacheTemplateEngine() // really only going to use this on a get route
        );
        Spark.post(
                "/create-user",
                (request, response) -> {
                    String username = request.queryParams("username");
                    String password = request.queryParams("password");
                    if (username == null || password == null) {
                        throw new Exception("Please fill out all fields.");
                    }
                    User user = users.get(username);
                    if (user == null) {
                        user = new User(username, password);
                        users.put(username, user);
                    }
                    else if (!password.equals(user.password)) {
                        throw new Exception("Wrong password");
                    }

                    Session session = request.session();
                    session.attribute("username", username);

                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/create-message",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    String text = request.queryParams("message");
                    Message message = new Message(text);
                    User user = users.get(username);
                    user.messages.add(message);
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "delete-message",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("Not logged in");
                    }
                    User user = users.get(username);

                    int id = Integer.valueOf(request.queryParams("id"));
                    if (id <= 0 || id - 1 > user.messages.size()) {
                        throw new Exception("Invalid ID");
                    }
                    user.messages.remove(id - 1);

                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "edit-message",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("Not logged in");
                    }
                    User user = users.get(username);

                    int id2 = Integer.valueOf(request.queryParams("editid"));

                    if (id2 <= 0 || id2 - 1 > user.messages.size()) {
                        throw new Exception("Invalid ID");
                    }

                    String newMessage = request.queryParams("edit");
                    Message message = new Message(newMessage);

                    user.messages.set(id2 - 1, message);

                    response.redirect("/");
                    return "";
                }
        );
    }
}
