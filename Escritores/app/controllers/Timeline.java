package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Amigos;
import models.Pessoa;
import models.Posts;
import models.Usuario;
import play.cache.Cache;
import play.mvc.Before;

public class Timeline extends Application {
	static List<Amigos> amigos = null;
	static int cont = 0;

	@Before
	static void checkUser() {
		if (connected() == null) {
			flash.error("Please log in first");
			Application.index2();
		}
	}
	
	
	public static void index() {
		List<Usuario> contacts = Cache.get("contacts", List.class);
		if (contacts == null) {
			contacts = Usuario.all().fetch();
			Cache.set("contacts", contacts, "30mn");
		}
		render(contacts);
	}

	public static void editarPerfil(Usuario user) {
		Pessoa pessoa = Pessoa.find("byPes_usu_codigo", user.usu_codigo).first();
		render(pessoa);
	}
	
	public static void solicitarAmizade(Usuario usu) {
		Usuario usuario = null;
		Amigos amigo = new Amigos();
		if (renderArgs.get("user") != null) {
			usuario = renderArgs.get("user", Usuario.class);
			amigo.usu_codigo1 = usuario.usu_codigo;
			amigo.usu_codigo2 = usu.usu_codigo;
			amigo.ami_solicitada = true;
			amigo.ami_aceita = false;
			amigo.save();
			amigo = new Amigos();
			amigo.usu_codigo1 = usu.usu_codigo;
			amigo.usu_codigo2 = usuario.usu_codigo;
			amigo.ami_solicitada = false;
			amigo.ami_aceita = false;
			amigo.save();
		}
		perfil(usu);
	}

	public static void teste(Usuario usuario) {
		cont = 0;
		amigos = Amigos.find("byUsu_codigo1AndAmi_aceita", usuario.usu_codigo,
				true).fetch();
		List<Amigos> amigosSolicitantes = Amigos.find(
				"byUsu_codigo1AndAmi_solicitadaAndAmi_aceita",
				usuario.usu_codigo, false, false).fetch();
		List<Usuario> amigosSoli = null;
		List<Usuario> contacts = null;

		if (amigos != null) {
			contacts = new ArrayList<Usuario>();
			for (Amigos amigos1 : amigos) {
				contacts.add((Usuario) Usuario.find("byUsu_codigo",
						amigos1.usu_codigo2).first());

			}
		}

		List<Object> posts = new ArrayList<Object>();
		List<Integer> codAmigo = new ArrayList<Integer>();
		for (Amigos integer : amigos) {
			codAmigo.add(integer.usu_codigo2);
			posts.addAll(Posts.find(
					"pos_cod_usuario in ? order by pos_codigo desc",
					integer.usu_codigo2).fetch());

		}
		Cache.set("posts", posts, "30mn");
		
		int tamanho = posts.size();
		if (amigosSolicitantes != null) {
			amigosSoli = new ArrayList<Usuario>();
			for (Amigos amigos2 : amigosSolicitantes) {
				amigosSoli.add((Usuario) Usuario.find("byUsu_codigo",
						amigos2.usu_codigo2).first());

			}
		}

		int qtdAmigos;
		if (contacts == null) {
			contacts = Usuario.all().fetch();
			Cache.set("contacts", contacts, "30mn");
			qtdAmigos = contacts.size();
		} else
			qtdAmigos = contacts.size();

		render(contacts, qtdAmigos, amigosSoli,tamanho);
	}

	public static void busca(String busca) {
		busca = params.get("busca");
		List<Usuario> usuariosBuscados = Usuario.find("byUsu_nome", busca)
				.fetch();
		render(usuariosBuscados);
	}

	public static void perfil(Usuario usu) {
		Usuario usuario = null;
		Amigos amigo = null;
		boolean ami_aceita = false;
		boolean amizade = false;
		if (renderArgs.get("user") != null) {
			usuario = renderArgs.get("user", Usuario.class);
			amigo = Amigos.find("byUsu_codigo1AndUsu_codigo2",
					usuario.usu_codigo, usu.usu_codigo).first();
		}

		if (amigo != null) {
			ami_aceita = amigo.ami_aceita;
			amizade = true;
			render(usu, amizade, ami_aceita);
		}

		render(usu, amizade, ami_aceita);
	}

	public static void aceitar(Usuario usu) {
		Usuario usuario = renderArgs.get("user", Usuario.class);
		Amigos ami = Amigos.find("byUsu_codigo1AndUsu_codigo2", usu.usu_codigo,
				usuario.usu_codigo).first();
		ami.ami_aceita = true;
		ami.save();
		ami = new Amigos();
		ami = Amigos.find("byUsu_codigo1AndUsu_codigo2", usuario.usu_codigo,
				usu.usu_codigo).first();
		ami.ami_aceita = true;
		ami.save();
		teste(usuario);
	}

	public static void postar(Posts post) {
		/*
		 * validation.required(name); validation.required(rarazaoSocial);
		 */
		Usuario usu = null;
		if (validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
			validation.keep(); // keep the errors for the next request
			index2();
		}

		String username = session.get("user");
		if (username != null) {
			usu = Usuario.find("byUsu_nome", username).first();
		}
		post.pos_cod_usuario = usu.usu_codigo;
		post.save();
		teste(usu);
	}

	public static void userPhotoTimeline() {
		
		List<Object> posts = Cache.get("posts", List.class);
		System.out.println(posts.size() + " id " + cont);
		
		final Usuario user = Usuario
				.findById(((Posts) posts.get(cont)).pos_cod_usuario);
		notFoundIfNull(user);
		response.setContentTypeIfNotSet(user.usu_ima.type());
		
		cont++;
		renderBinary(user.usu_ima.get());

	}
	
	
	public static void listProjetos(int page) {
		List<Object> posts = Cache.get("posts", List.class);
		System.out.println("pagina"+posts.get(page));
		System.out.println(posts.size());
		renderJSON(posts);
	}
	
	
}