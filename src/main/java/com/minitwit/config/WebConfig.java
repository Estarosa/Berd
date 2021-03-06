package com.minitwit.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.minitwit.util.PasswordUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.spark.sql.SparkSession;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;

import com.minitwit.model.LoginResult;
import com.minitwit.model.Message;
import com.minitwit.model.User;
import com.minitwit.service.impl.MiniTwitService;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import spark.ModelAndView;
import spark.Request;
import spark.template.freemarker.FreeMarkerEngine;
import spark.utils.IOUtils;
import spark.utils.StringUtils;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Part;
import javax.xml.soap.Text;

import static spark.Spark.*;


public class WebConfig {


	private static final String USER_SESSION_ID = "user";
	private MiniTwitService service;
	private static int i = 0;

	public WebConfig(MiniTwitService service) {
		this.service = service;
		staticFileLocation("/public");
		setupRoutes();
	}
	
	private void setupRoutes() {
		/*
		 * Shows a users timeline or if no user is logged in,
		 *  it will redirect to the public timeline.
		 *  This timeline shows the user's messages as well
		 *  as all the messages of followed users.
		 */
		get("/", (req, res) -> {
			User user = getAuthenticatedUser(req);
			Map<String, Object> map = new HashMap<>();
			map.put("pageTitle", "Timeline");
			map.put("user", user);
			List<Message> messages = service.getUserFullTimelineMessages(user);
			map.put("messages", messages);
			return new ModelAndView(map, "timeline.ftl");
        }, new FreeMarkerEngine());
		before("/", (req, res) -> {
			User user = getAuthenticatedUser(req);
			if(user == null) {
				res.redirect("/public");
				halt();
			}
		});

        //search
        get("/sh", (req, res) -> {
            String search = req.queryParams("input");
            User user = getAuthenticatedUser(req);
            Map<String, Object> map = new HashMap<>();
            map.put("pageTitle", "Public Timeline");
            map.put("user", user);
            List<Message> messages;
			String[] ary = search.split(":",2);
			ary[0] = ary[0].trim();
			if (ary.length>1) {
				ary[1] = ary[1].replaceAll("^\\s+", "");
			}
            if(ary[0].equals("msg") && !ary[1].equals("")) {
                messages = service.getSearchMessage(ary[1]);
            }else if(ary[0].equals("usr") && !ary[1].equals("")){
				messages = service.getSearchUser(ary[1]);
			}else if (ary[0].equals("flwr")){
				messages = service.getSearchUserFollowers(ary[1]);
            }else if (ary[0].equals("flwe")){
				messages = service.getSearchUserFollowees(ary[1]);
            }else if(ary[0].equals("top")) {
                try {

                    int b = Integer.parseInt(ary[1]);
                    messages = service.getTrendingtags(ary[1]);


                } catch (NumberFormatException e) {
                    messages = service.getSearchUserFollowees("");
                }
            } else{
				messages = service.getSearchUserFollowees("");
				}
            map.put("messages", messages);
            return new ModelAndView(map, "timeline.ftl");
        }, new FreeMarkerEngine());

		/*
		 * Displays the latest messages of all users.
		 */
		get("/public", (req, res) -> {
			User user = getAuthenticatedUser(req);
			Map<String, Object> map = new HashMap<>();
			map.put("pageTitle", "Public Timeline");
			map.put("user", user);
			List<Message> messages = service.getPublicTimelineMessages();
			map.put("messages", messages);
			return new ModelAndView(map, "timeline.ftl");
        }, new FreeMarkerEngine());
		
		
		/*
		 * Displays a user's tweets.
		 */
		get("/t/:username", (req, res) -> {
			String username = req.params(":username");
			User profileUser = service.getUserbyUsername(username);
			User authUser = getAuthenticatedUser(req);
			boolean followed = false;
			if(authUser != null) {
				followed = service.isUserFollower(authUser, profileUser);
			}
			List<Message> messages = service.getUserTimelineMessages(profileUser);
			Map<String, Object> map = new HashMap<>();
			map.put("pageTitle", username + "'s Timeline");
			map.put("user", authUser);
			map.put("profileUser", profileUser);
			map.put("followed", followed);
			map.put("messages", messages);
			return new ModelAndView(map, "timeline.ftl");
        }, new FreeMarkerEngine());
		/*
		 * Checks if the user exists
		 */
		before("/t/:username", (req, res) -> {
			String username = req.params(":username");
			User profileUser = service.getUserbyUsername(username);
			if(profileUser == null) {
				halt(404, "User not Found");
			}
		});
		
		
		/*
		 * Adds the current user as follower of the given user.
		 */
		get("/t/:username/follow", (req, res) -> {
			String username = req.params(":username");
			User profileUser = service.getUserbyUsername(username);
			User authUser = getAuthenticatedUser(req);
			
			service.followUser(authUser, profileUser);
			res.redirect("/t/" + username);
			return null;
        });
		/*
		 * Checks if the user is authenticated and the user to follow exists
		 */
		before("/t/:username/follow", (req, res) -> {
			String username = req.params(":username");
			User authUser = getAuthenticatedUser(req);
			User profileUser = service.getUserbyUsername(username);
			if(authUser == null) {
				res.redirect("/login");
				halt();
			} else if(profileUser == null) {
				halt(404, "User not Found");
			}
		});
		
		
		/*
		 * Removes the current user as follower of the given user.
		 */
		get("/t/:username/unfollow", (req, res) -> {
			String username = req.params(":username");
			User profileUser = service.getUserbyUsername(username);
			User authUser = getAuthenticatedUser(req);
			
			service.unfollowUser(authUser, profileUser);
			res.redirect("/t/" + username);
			return null;
        });
		/*
		 * Checks if the user is authenticated and the user to unfollow exists
		 */
		before("/t/:username/unfollow", (req, res) -> {
			String username = req.params(":username");
			User authUser = getAuthenticatedUser(req);
			User profileUser = service.getUserbyUsername(username);
			if(authUser == null) {
				res.redirect("/login");
				halt();
			} else if(profileUser == null) {
				halt(404, "User not Found");
			}
		});
		
		
		/*
		 * Presents the login form or redirect the user to
		 * her timeline if it's already logged in
		 */
		get("/login", (req, res) -> {
			Map<String, Object> map = new HashMap<>();
			if(req.queryParams("r") != null) {
				map.put("message", "You were successfully registered and can login now");
			}
			return new ModelAndView(map, "login.ftl");
        }, new FreeMarkerEngine());
		/*
		 * Logs the user in.
		 */
		post("/login", (req, res) -> {
			Map<String, Object> map = new HashMap<>();
			User user = new User();
			try {
				MultiMap<String> params = new MultiMap<String>();
				UrlEncoded.decodeTo(req.body(), params, "UTF-8");
				BeanUtils.populate(user, params);
			} catch (Exception e) {
				halt(501);
				return null;
			}
			//
			LoginResult result = service.checkUser(user);
			if(result.getUser() != null) {
				addAuthenticatedUser(req, result.getUser());
				res.redirect("/");
				halt();
			} else {
				map.put("error", result.getError());
			}
			map.put("username", user.getUsername());
			return new ModelAndView(map, "login.ftl");
        }, new FreeMarkerEngine());
		/*
		 * Checks if the user is already authenticated
		 */
		before("/login", (req, res) -> {
			User authUser = getAuthenticatedUser(req);
			if(authUser != null) {
				res.redirect("/");
				halt();
			}
		});
		
		
		/*
		 * Presents the register form or redirect the user to
		 * her timeline if it's already logged in
		 */
		get("/register", (req, res) -> {
			Map<String, Object> map = new HashMap<>();
			return new ModelAndView(map, "register.ftl");
        }, new FreeMarkerEngine());
		/*
		 * Registers the user.
		 */
		post("/register", (req, res) -> {
			Map<String, Object> map = new HashMap<>();
			User user = new User();
			try {
				MultiMap<String> params = new MultiMap<String>();
				UrlEncoded.decodeTo(req.body(), params, "UTF-8");
				BeanUtils.populate(user, params);
			} catch (Exception e) {
				halt(501);
				return null;
			}
			String error = user.validate();
			if(StringUtils.isEmpty(error)) {
				User existingUser = service.getUserbyUsername(user.getUsername());
				if(existingUser == null) {
					service.registerUser(user);
					res.redirect("/login?r=1");
					halt();
				} else {
					error = "The username is already taken";
				}
			}
			map.put("error", error);
			map.put("username", user.getUsername());
			map.put("email", user.getEmail());
			return new ModelAndView(map, "register.ftl");
        }, new FreeMarkerEngine());
		/*
		 * Checks if the user is already authenticated
		 */
		before("/register", (req, res) -> {
			User authUser = getAuthenticatedUser(req);
			if(authUser != null) {
				res.redirect("/");
				halt();
			}

		});

		get("/Pu", (req, res) -> {
			Map<String, Object> map = new HashMap<>();
			map.put("user",getAuthenticatedUser(req));
			return new ModelAndView(map, "Pu.ftl");
		}, new FreeMarkerEngine());
		/*
		 * Registers the user.
		 */
		post("/Pu", (req, res) -> {
			User authUser = getAuthenticatedUser(req);
			Map<String, Object> map = new HashMap<>();
			User us = new User();

			us.setEmail( (req.queryParams("email") != null && !req.queryParams("email").isEmpty()) ?  req.queryParams("email") : authUser.getEmail());
			us.setUsername( (req.queryParams("username") != null && !req.queryParams("username").isEmpty()) ? req.queryParams("username")  : authUser.getUsername());
			us.setPassword((req.queryParams("password") != null && !req.queryParams("password").isEmpty()) ? PasswordUtil.hashPassword(req.queryParams("password") ) : PasswordUtil.hashPassword(authUser.getUsername()));
			us.setId(authUser.getId());
			String error = "";
			if( req.queryParams("password") != null && !req.queryParams("password").isEmpty() && !req.queryParams("password").equals(req.queryParams("password2"))){
				error = "The two passwords do not match";
			}
			if(PasswordUtil.verifyPassword(req.queryParams("cp"),authUser.getPassword())) {
				if (StringUtils.isEmpty(error)) {
					User existingUser = service.getUserbyUsername(us.getUsername());
					if (existingUser == null || existingUser.getUsername().equals(authUser.getUsername())) {
						service.updateUser(authUser, us);
						removeAuthenticatedUser(req);
						addAuthenticatedUser(req, us);
						res.redirect("/");
						halt();
					} else {
						error = "The username is already taken";
					}
				}
			}else{
				error = "cp is wrong";
			}
			map.put("error", error);
			return new ModelAndView(map, "Pu.ftl");
		}, new FreeMarkerEngine());
		/*
		 * Checks if the user is already authenticated
		 */
		before("/Pu", (req, res) -> {
			User authUser = getAuthenticatedUser(req);
			if(authUser == null) {
				res.redirect("/");
				halt();
			}
		});

		/*
		 * Registers a new message for the user.
		 */
        post("/message", (req, res) -> {
            User user = getAuthenticatedUser(req);

            if (req.raw().getAttribute("org.eclipse.jetty.multipartConfig") == null) {
                MultipartConfigElement multipartConfigElement = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
                req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
            }
            Part file = req.raw().getPart("file");
            Part text = req.raw().getPart("text");
            String filename = file.getSubmittedFileName();
            String s = filename;
			String[] ary = s.split(Pattern.quote("."));
            Path filePath = Paths.get("./target/classes/public/images/",i + "."+ary[ary.length - 1]);
            if(text.getSize() > 0){
                try{
                    filename = org.apache.commons.io.IOUtils.toString(text.getInputStream(), StandardCharsets.UTF_8);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            if(s.length() != 0){
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                i++;
            }
               MultiMap<String> params = new MultiMap<String>();
                UrlEncoded.decodeTo(req.body(), params, "UTF-8");
                Message m = new Message();
                m.setUserId(user.getId());
                m.setPubDate(new Date());
                m.setText(filename);
            if(s.length() != 0) {
                m.setImg(i - 1 + "."+ary[ary.length - 1]);
            }else{
                m.setImg(null);
            }

                BeanUtils.populate(m, params);
                service.addMessage(m);

            res.redirect("/");

            return null;
        });
		/*
		 * Checks if the user is authenticated
		 */
		before("/message", (req, res) -> {
			User authUser = getAuthenticatedUser(req);
			if(authUser == null) {
				res.redirect("/login");
				halt();
			}
		});
		
		
		/*
		 * Logs the user out and redirects to the public timeline
		 */
		get("/logout", (req, res) -> {
			removeAuthenticatedUser(req);
			res.redirect("/public");
			return null;
        });
	}

	private void addAuthenticatedUser(Request request, User u) {
		request.session().attribute(USER_SESSION_ID, u);
		
	}

	private void removeAuthenticatedUser(Request request) {
		request.session().removeAttribute(USER_SESSION_ID);
		
	}

	private User getAuthenticatedUser(Request request) {
		return request.session().attribute(USER_SESSION_ID);
	}
    private static void logInfo(Request req, Path tempFile) throws IOException, ServletException {

    }
}
