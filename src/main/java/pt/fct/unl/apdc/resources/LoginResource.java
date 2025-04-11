package pt.fct.unl.apdc.resources;

import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import pt.fct.unl.apdc.util.AuthToken;
import pt.fct.unl.apdc.util.LoginData;

import com.google.gson.Gson;


@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();
	
	public LoginResource() {
		
	}
	
	//JAKARTA
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);
		
		if(data.username.equals("almiscar") && data.password.equals("123")) { 
			AuthToken at = new AuthToken(data.username);
			return Response.ok(g.toJson(at)).build();
		}

		// Se o username ou password estiverem errados, retorna um erro 403 (Forbidden)
		// 403 - Forbidden: O servidor entendeu o pedido, mas recusa-se a autorizá-lo.
		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();

		// 	response.ok().build() dá return do code HTTP 200 
		/* 
		*	return Response.ok(new AuthToken(data.username)).build();
		*/
	}
	
	/* 	
	*	Valores entre {} no @PATH representam variáveis, ou seja 
	*	valores de entrada que são enviados no URL
	*/
	@GET
	@Path("/{username}") // diz que o username é um parâmetro de entrada e vai ser passado na URL
	// GET http://localhost:8080/apdc/api/login/almiscar é suposto retornar false
	// false = not available
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if (username.trim().equals("almiscar")) {
			return Response.ok().entity(g.toJson(false)).build();					
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
		
	}

}
