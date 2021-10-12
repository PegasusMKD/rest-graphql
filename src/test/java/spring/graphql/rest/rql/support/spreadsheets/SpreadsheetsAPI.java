package spring.graphql.rest.rql.support.spreadsheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpreadsheetsAPI {
	// TODO: Update properties so they get configured through application.properties
	private final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private final String TOKENS_DIRECTORY_PATH = "tokens";
	private final String spreadsheetId = "1RdRL2uLPQtm_skPM-zTr_lHaK9wZ7ztgbYo-2JGna9s";

	private final JsonFactory JSON_FACTORY = new JacksonFactory();

	private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private final Border SOLID_BORDER = new Border().setStyle("SOLID");
	private final Border NO_BORDER = new Border().setStyle("NONE");

	@Value("classpath:/credentials.json")
	Resource resource;

	private Sheets.Spreadsheets sheets;

	@PostConstruct
	void load() {
		sheets = getSpreadsheets();
	}

	private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = resource.getInputStream();
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	private Sheets.Spreadsheets getSpreadsheets() {
		try {
			// Build a new authorized API client service.
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME)
					.build().spreadsheets();
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void batchUpdate(List<SpreadsheetsRequest> funcs) {
		BatchUpdateSpreadsheetRequest updateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest()
				.setRequests(funcs.stream().map(SpreadsheetsRequest::execute).collect(Collectors.toList()));
		try {
			sheets.batchUpdate(spreadsheetId, updateSpreadsheetRequest).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ParsedRange parse(String range) {
		String actualRange = range.split("!")[1];
		String start = actualRange.split(":")[0];
		String end = actualRange.split(":")[1];
		return ParsedRange.builder()
				.startColumnIndex(start.charAt(0) - 'A')
				.startRowIndex(Integer.parseInt(start.substring(1)) - 1)
				.endColumnIndex((end.charAt(0) - 'A') + 1)
				.endRowIndex(!end.substring(1).equals("") ? Integer.parseInt(end.substring(1)) : null)
				.build();
	}

	@NotNull
	private Request updateBordersRequest(ParsedRange range, Border border) {
		return new Request().setUpdateBorders(
				new UpdateBordersRequest()
						.setRange(
								new GridRange()
										.setStartColumnIndex(range.getStartColumnIndex())
										.setStartRowIndex(range.getStartRowIndex())
										.setEndColumnIndex(range.getEndColumnIndex())
										.setEndRowIndex(range.getEndRowIndex())
										.setSheetId(0)
						)
						.setTop(border)
						.setBottom(border)
						.setRight(border)
						.setLeft(border)
						.setInnerVertical(border)
						.setInnerHorizontal(border)
		);
	}

	public void clearLatest(String range) {
		ClearValuesRequest req = new ClearValuesRequest();
		try {
			sheets.values().clear(spreadsheetId, range, req)
					.execute();
			batchUpdate(Lists.newArrayList(
					() -> updateBordersRequest(parse(range), NO_BORDER)
			));
		} catch (IOException e) {
			System.out.println("Exception while clearing data.");
			e.printStackTrace();
		}
	}

	public void append(List<List<Object>> values, String range, boolean overwrite) {
		ValueRange data = new ValueRange().setValues(values);
		try {
			AppendValuesResponse result = sheets.values().append(spreadsheetId, range, data)
					.setValueInputOption("USER_ENTERED")
					.setInsertDataOption(overwrite ? "OVERWRITE" : "INSERT_ROWS")
					.execute();
			batchUpdate(Lists.newArrayList(
					() -> updateBordersRequest(parse(result.getUpdates().getUpdatedRange()), SOLID_BORDER)
			));
		} catch (IOException e) {
			System.out.println("Error while appending data.");
			e.printStackTrace();
		}
	}


}
