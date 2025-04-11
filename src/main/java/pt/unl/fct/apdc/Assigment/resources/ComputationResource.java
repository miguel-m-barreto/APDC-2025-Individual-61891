package pt.unl.fct.apdc.assigment.resources;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


import com.google.cloud.tasks.v2.*;
import com.google.gson.Gson;
import com.google.protobuf.Timestamp;


@Path("/utils")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8") 
public class ComputationResource {

	private static final Logger LOG = Logger.getLogger(ComputationResource.class.getName()); 
	private final Gson g = new Gson();

	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public ComputationResource() {} //nothing to be done here @GET

	@GET
	@Path("/hello")
	@Produces(MediaType.TEXT_PLAIN)
	public Response hello() throws IOException{
		try {
			LOG.fine("Saying hello.");
			return Response.ok().entity("Hello human!").build();

			//throw new IOException("UPS");
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Exception on Method /hello", e);
			return Response.temporaryRedirect(URI.create("/error/500.html")).build();
		}
	}
	
	@GET
	@Path("/time")
	public Response getCurrentTime() {

		LOG.fine("Replying to date request.");
		return Response.ok().entity(g.toJson(fmt.format(new Date()))).build();
	}

	@GET
	@Path("/compute")
	public Response triggerExecuteComputeTask() throws IOException {
		String projectId = "shining-expanse-453014-c4";
		String queueName = "Default";
		String location = "europe-west6";
		LOG.log(Level.INFO, projectId + " :: " + queueName + " :: " + location );

		try (CloudTasksClient client = CloudTasksClient.create()) {
			String queuePath = QueueName.of(projectId, location, queueName).toString();
			
			Task.Builder taskBuilder = 
				Task.newBuilder().setAppEngineHttpRequest(AppEngineHttpRequest.newBuilder()
						.setRelativeUri("/rest/utils/compute").setHttpMethod(HttpMethod.POST)
						.build());

			taskBuilder.setScheduleTime(Timestamp.newBuilder().setSeconds(Instant.now(Clock.systemUTC()).getEpochSecond()));
			
			client.createTask(queuePath, taskBuilder.build());
		} 

		return Response.ok().build();
	}

	@POST
	@Path("/compute")
	// POST http://localhost:8080/rest/utils/compute
	public Response executeComputeTask() {
		LOG.fine("Starting execute computation task.");
		try {
			// Simulate a long computation task
			Thread.sleep(60*1000*10); // Sleep for 5 seconds
		} catch (InterruptedException e) {
			LOG.logp(Level.SEVERE, this.getClass().getCanonicalName(), "executeComputeTask", "Task interrupted an exception ocurred", e);
			return Response.serverError().entity("Computation task interrupted").build();
		}
		return Response.ok().entity("Computation task completed").build();
	}
}