package com.theironyard;

import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    static HashMap<String, User> users = new HashMap<>();
    static final String SAVE_FILE = "data.json";
    static HashMap jsonData;


    public static void main(String[] args) {
//        jsonData = load(SAVE_FILE);

        Spark.staticFileLocation("public");
        Spark.init();
        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    User user = users.get(username);

                    HashMap m = new HashMap();
                    if (username == null) {
                        return new ModelAndView(m, "index.html");
                    }
                    else {
                        int id = 1;
                        for (Message msg: user.messages) {
                            msg.id = id;
                            id++;
                        }
//                        User user = users.get(username);
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

//                    jsonData.put("list", username);
//                    save(jsonData, SAVE_FILE);

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

//    public static HashMap load(String filename) {
//        File f = new File(filename);
//
//        try {
//            Scanner scanner = new Scanner(f);
//            scanner.useDelimiter("\\Z");
//            String contents = scanner.next();
//            JsonParser parser = new JsonParser();
//            return parser.parse(contents);
//
//        }
//        catch (FileNotFoundException e) {
//        }
//        return null;
//    }

//    public static void save(HashMap jsonData, String filename) {
//        File f = new File(filename);
//        JsonSerializer serializer = new JsonSerializer();
//        String json = serializer.serialize(jsonData);
//
//        try {
//            FileWriter fw = new FileWriter(f);
//            fw.write(json);
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

}
