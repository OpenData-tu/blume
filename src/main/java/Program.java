import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Andres Ardila
 */
public class Program {

    public static void main(String[] args) throws IOException {
        final String urlFormat =
                "http://www.stadtentwicklung.berlin.de/umwelt/luftqualitaet/de/messnetz/tageswerte/download/%s.html";

        Scanner reader = new Scanner(System.in);
        System.out.print("> ");
        String date = reader.nextLine();

        final String baseUrl = String.format(urlFormat, date);
        URL url = new URL(baseUrl);
        InputStream inputStream = url.openStream();
        try (Scanner s = new Scanner(inputStream)){
            String responseBody = s.useDelimiter("\\A").next();
            System.out.println(responseBody);
        }
        finally {
            inputStream.close();
        }
    }
}
