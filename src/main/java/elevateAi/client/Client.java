package elevateAi.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import  com.fasterxml.jackson.databind.*;

public class Client {
	public static Client newInstance(String apiBaseUrl, String apiToken) {
		return new Client(apiBaseUrl, apiToken);
	}

	private String baseUrl;
	private String apiToken;
	private HttpClient cli;
	private ObjectMapper jsonMapper;
	private String boundary = new BigInteger(256, new Random()).toString();

	private Client(String baseUrl, String apiToken){
		this.baseUrl = baseUrl;
		this.apiToken = apiToken;
		this.cli = HttpClient.newBuilder().build();

		jsonMapper = new ObjectMapper();
	}

	/**
	 * Declare only
	 * @param languageTag
	 * @param vertical
	 * @param audioTranscriptionMode
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> declare(String languageTag, String vertical, String audioTranscriptionMode) throws Exception {
		return declare(languageTag, vertical, audioTranscriptionMode, null, null, false);
	}

	/**
	 * Declare and upload with default language, vertical, and transcription mode.
	 * @param mediaUri
	 * @param mediaFile
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> declare(String mediaUri, String mediaFile ) throws Exception {
		return declare("en-us", "default", "highAccuracy", mediaUri, mediaFile, true);
	}

	/**
	 * Declare and optionally upload/confirm status
	 * @param languageTag
	 * @param vertical
	 * @param audioTranscriptionMode
	 * @param mediaUri
	 * @param mediaFile
	 * @param confirm
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> declare(String languageTag, String vertical, String audioTranscriptionMode,
									   String mediaUri, String mediaFile, boolean confirm ) throws Exception {

		Map pl = Map.of("type", "audio", "languageTag",languageTag, "vertical", vertical,
				"audioTranscriptionMode", audioTranscriptionMode, "includeAiResults", true
		);
		if(mediaUri!=null)
			pl.put("downloadUrl", mediaUri);

		var req = HttpRequest.newBuilder()
				.uri(new URI(baseUrl+"/interactions"))
				.header("X-API-TOKEN", apiToken)
				.header("Content-Type", "application/json;charset=UTF-8")
				.POST(HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(pl)))
				.build();
		HttpResponse<String> rsp = cli.send(req, HttpResponse.BodyHandlers.ofString());
		if(rsp.statusCode()!=201)
			throw new RuntimeException("Failed to delclare: "+rsp.statusCode());
		var it = jsonMapper.readValue(rsp.body(), Map.class);
		if(mediaUri==null && mediaFile!=null && upload(it, mediaFile))
			it.put("mediaFile", mediaFile);
		if(confirm)
			it.put("status", status(it));
		return it;
	}

	/**
	 * Retrieve interaction status
	 * @param interaction
	 * @return
	 * @throws Exception
	 */
	public String status(Object interaction) throws Exception {
		var interactionId = interaction instanceof Map?
				((Map)interaction).get("interactionIdentifier") : interaction;

		HttpRequest req = HttpRequest.newBuilder()
				.uri(new URI(String.format(baseUrl+"/interactions/%s/status", interactionId)))
				.header("X-API-TOKEN", apiToken)
				.GET()
				.build();
		HttpResponse<String> rsp = cli.send(req, HttpResponse.BodyHandlers.ofString());
		if(rsp.statusCode()!=200)
			throw new RuntimeException("Failed: "+rsp.statusCode());
		var it = jsonMapper.readValue(rsp.body(), Map.class);
		return (String)it.get("status");
	}

	/**
	 * Upload media content for declared interaction.
	 */
	public boolean upload(Object interaction, String mediaFile) throws Exception {
		var interactionId = interaction instanceof Map?
				((Map)interaction).get("interactionIdentifier") : interaction;
		Map pl = Map.of("file", Path.of(mediaFile));

		var req = HttpRequest.newBuilder()
				.uri(new URI(String.format(baseUrl+"/interactions/%s/upload", interactionId)))
				.header("X-API-TOKEN", apiToken)
				.header("Content-Type", "multipart/form-data;boundary=" + boundary)
				.POST(ofMimeMultipartData(pl, boundary))
				.build();
		HttpResponse<String> rsp = cli.send(req, HttpResponse.BodyHandlers.ofString());
		return rsp.statusCode()==200;
	}

	/**
	 * Retrieve interaction transcripts
	 * @param interaction
	 * @return
	 * @throws Exception
	 */
	public Object transcripts(Object interaction, boolean punctuated)  throws Exception {
		var interactionId = interaction instanceof Map?
				((Map)interaction).get("interactionIdentifier") : interaction;
		var path = punctuated? "/transcripts/punctuated" : "/transcripts";
		var req = HttpRequest.newBuilder()
				.uri(new URI(String.format(baseUrl+"/interactions/%s%s", interactionId, path)))
				.header("X-API-TOKEN", apiToken)
				.header("Content-Type", "application/json;charset=UTF-8")
				.GET()
				.build();
		HttpResponse<String> rsp = cli.send(req, HttpResponse.BodyHandlers.ofString());
		if(rsp.statusCode()!=200)
			throw new RuntimeException("Failed: "+rsp.statusCode());
		var it = jsonMapper.readValue(rsp.body(), Map.class);
		return it;
	}

	/**
	 * Retrieve interaction AI results
	 * @param interaction
	 * @return
	 * @throws Exception
	 */
	public Object aiResults(Object interaction)  throws Exception {
		var interactionId = interaction instanceof Map?
				((Map)interaction).get("interactionIdentifier") : interaction;
		var req = HttpRequest.newBuilder()
				.uri(new URI(String.format(baseUrl+"/interactions/%s/ai", interactionId)))
				.header("X-API-TOKEN", apiToken)
				.header("Content-Type", "application/json;charset=UTF-8")
				.GET()
				.build();
		HttpResponse<String> rsp = cli.send(req, HttpResponse.BodyHandlers.ofString());
		if(rsp.statusCode()!=200)
			throw new RuntimeException("Failed: "+rsp.statusCode());
		var it = jsonMapper.readValue(rsp.body(), Map.class);
		return it;
	}

	private HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data, String boundary) throws IOException {
		List<byte[]> byteArrays = new ArrayList<>();
		byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);

		for (var entry : data.entrySet()) {
			byteArrays.add(separator);
			if (entry.getValue() instanceof Path) {
				var path = (Path) entry.getValue();
				String mimeType = Files.probeContentType(path);
				byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + path.getFileName()
						+ "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
				byteArrays.add(Files.readAllBytes(path));
				byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
			} else {
				byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
						.getBytes(StandardCharsets.UTF_8));
			}
		}
		byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
		return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
	}
}
