package no.nav.foreldrepenger.fordel.web.app.util;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import no.nav.vedtak.log.mdc.MDCOperations;

public class DokumentfordenselseTestClient {
    private String ID_token = "<insert-id-token-her-før-du-kjører-testen>";

    public static void main(String[] args) throws IOException {
        new DokumentfordenselseTestClient().createMultipartPost();
    }

    public void createMultipartPost() throws IOException {
        String url = "http://localhost:8090/fpfordel/api/dokumentforsendelse";
        String testdataDir = new File(this.getClass().getClassLoader().getResource("testdata/metadata.json").getPath()).getParent();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
            .setMimeSubtype("mixed")
            .setMode(HttpMultipartMode.RFC6532);

        builder.addPart(buildPart("metadata", new File(testdataDir, "metadata.json"), ContentType.APPLICATION_JSON, null))
            .addPart(buildPart("hoveddokument", new File(testdataDir, "ES-F.xml"), ContentType.create("application/xml"), "<some ID 1>"))
            .addPart(buildPart("hoveddokument", new File(testdataDir, "ES-f.pdf"), ContentType.create("application/pdf"), "<some ID 2>"))
            .addPart(buildPart("vedlegg", new File(testdataDir, "terminbekreftelse.pdf"), ContentType.create("application/pdf"), "<some ID 3>"));

        HttpPost post = new HttpPost(url); // Setting up a HTTP Post method with the target url
        post.setEntity(builder.build()); // Setting the multipart Entity to the post method
        post.setHeader("Authorization", "Bearer " + ID_token);
        post.setHeader("Accept", APPLICATION_JSON);
        post.setHeader(MDCOperations.HTTP_HEADER_CONSUMER_ID, "DokumentfordenselseTestClient");
        post.setHeader(MDCOperations.HTTP_HEADER_CALL_ID, "DTC_" + MDCOperations.generateCallId());

        try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();
                CloseableHttpResponse resp = client.execute(post);
                Scanner scanner = new Scanner(resp.getEntity().getContent(), "utf-8")) {
            scanner.useDelimiter("\\A");
            System.out.println(resp.getStatusLine());

            List<Header> headers = Arrays.asList(resp.getAllHeaders());
            for (Header header : headers) {
                System.out.println(header);
            }

            System.out.println(scanner.hasNext() ? scanner.next() : "");
        }
    }

    private FormBodyPart buildPart(String name, File file, ContentType contentType, String contentId) {
        FormBodyPartBuilder builder = FormBodyPartBuilder.create()
            .setName(name)
            .setBody(new FileBody(file, contentType));
        if (contentId != null) {
            builder.setField("Content-ID", contentId);
        }
        return builder.build();
    }

}
