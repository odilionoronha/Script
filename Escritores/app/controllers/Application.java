package controllers;

import play.*;
import play.cache.Cache;
import play.data.validation.Valid;
import play.libs.OAuth2;
import play.mvc.*;

import java.util.*;


import models.*;
public class Application extends Controller {
	
	
    public static void index2() {
    	  List<Usuario> contacts = Cache.get("contacts", List.class);
    	  if(contacts == null) {
    		  contacts = Usuario.all().fetch();
              Cache.set("contacts", contacts, "30mn");
          }
        render(contacts);
    }
        
   
    public static void login(String username, String password) {
    	Usuario user = Usuario.find("byUsu_nomeAndUsu_senha", username, password).first();
        if(user != null) {
            session.put("user", user.usu_nome);
           Timeline.teste(user);         
        }
        // Oops
        flash.put("username", username);
        flash.error("Login failed");
        index2();
    }
   

    @Before
    static void addUser() {
    	Usuario user = connected();
        if(user != null) {
            renderArgs.put("user", user);
        }
    }
    
   
    
    static Usuario connected() {
        if(renderArgs.get("user") != null) {
            return renderArgs.get("user", Usuario.class);
        }
        String username = session.get("user");
        if(username != null) {
            return Usuario.find("byUsu_nome", username).first();
        } 
        return null;
    }
    
    
    public static void logout() {
        session.clear();
        index2();
    }
    
    public static void registrar() {
       
        render();
    }
    
    public static void saveUser(@Valid Usuario user, String verifyPassword, String verifyEmail) {
       //validation.required(verifyPassword).message("Campo Obrigatorio");
       //validation.equals(verifyPassword, user.usu_senha).message("Seu password não confere");
       // validation.required(verifyEmail).message("Campo Obrigatorio");
       // validation.equals(verifyEmail, user.email).message("Seu email não confere");
        if(validation.hasErrors()) {
            render("@registrar", user, verifyPassword);
        }
        user.save();
        Pessoa pessoa = new Pessoa();
        pessoa.pes_nome = user.usu_nome;
        
        session.put("user", user.usu_nome);
        index2();
    }
    
    public static void userPhoto(int id) {
    	   final Usuario user = Usuario.findById(id);
    	   notFoundIfNull(user);
    	   response.setContentTypeIfNotSet(user.usu_ima.type());
    	   renderBinary(user.usu_ima.get());
    	}
    
    
}