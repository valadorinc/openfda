package org.djw.app.restletresources;

import openfda.classes.OpenFDAClient;
import openfda.classes.ServerAuth;
import org.apache.log4j.Logger;
import org.djw.tools.json.ResponseJson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class OpenFDAResource extends ServerResource {
	final static Logger logger = Logger.getLogger(OpenFDAResource.class);

	@Get
	public Representation represent() throws JSONException {

		JSONObject jBody = new JSONObject();
		JSONObject ReportOutput = new JSONObject();
		String Message = "";
		int Status = 0;
		try {

			String ServerKey = "";
			if (getRequest().getAttributes().get("ServerKey") != null)
				ServerKey = (String) getRequest().getAttributes().get(
						"ServerKey");
			ServerAuth serverAuth = new ServerAuth();
			boolean Authenticated = serverAuth.authenticate(ServerKey);

			if (Authenticated) {
				String ReactionList = "";
				if (getRequest().getAttributes().get("reactionlist") != null)
					ReactionList = (String) getRequest().getAttributes().get(
							"reactionlist");
ReactionList = ReactionList.replace("%7E","~");

				String[] reactions = ReactionList.split("~");
				String Reaction = "";
				for (int r = 0; r < reactions.length; r++) {
					Reaction += "patient.reaction.reactionmeddrapt:\""
							+ reactions[r] + "\"";
					if (r < reactions.length - 1) {
						Reaction += "+AND+";
					}
				}

				String ServiceURI = "/event.json?search=" + Reaction
						+ "&count=patient.drug.openfda.substance_name.exact";
				if (logger.isDebugEnabled()){
					logger.debug("ServiceURI: " + ServiceURI);
				}


				if (logger.isDebugEnabled()){
					logger.debug("ServiceURI: " + ServiceURI);
				}

				OpenFDAClient restClient = new OpenFDAClient();
				JSONObject json = restClient.getService(ServiceURI);
				JSONArray results = json.getJSONArray("results");
				JSONArray cols = new JSONArray();
				cols.put("Drug Name");
				cols.put("Occurrences");
				JSONArray rows = new JSONArray();
				for (int i = 0; i < results.length(); i++) {
					JSONArray row = new JSONArray();
					JSONObject result = results.getJSONObject(i);
					String Drug = result.getString("term");
					int Occurrences = result.getInt("count");
					row.put(Drug);
					row.put(Occurrences);
					rows.put(row);
				}

				ReportOutput.put("cols", cols);
				ReportOutput.put("rows", rows);

				// logger.debug(metaResults.toString(1));

			} else {
				Status = 1;
				Message = "invalid authentication token";
			}
		} catch (Exception e) {
			Status = 1;
			Message = "an error has occurred: " + e;
		}

		jBody.put("ReportOutput", ReportOutput);

		ResponseJson jResponse = new ResponseJson();
		Representation rep = null;
		jResponse.setStatusCode(Status);
		jResponse.setStatusMessage(Message);
		jResponse.setBody(jBody);
		rep = new JsonRepresentation(jResponse.getResponse(jResponse));
		return rep;
	}

}
