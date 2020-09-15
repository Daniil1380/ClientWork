import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientTest {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(output));
    }

    @Test
    public void testGetRequestWithoutRightFirst() throws Exception {
        Client.getRequest("ABC");
        Assert.assertEquals("Такого жанра нет. Повторите попытку\r", output.toString());
    }

    @Test
    public void testGetRequestWithoutRightSecond() throws Exception {
        Client.getRequest("42");
        Assert.assertEquals("Такого жанра нет. Повторите попытку\r", output.toString());
    }

    @Test
    public void testGetRequestWithRightFirst() throws Exception {
        Assert.assertEquals(6.5, testGetRequestWithRight("Music"), 0.1);

    }

    public double testGetRequestWithRight(String genre) throws Exception {
        Client.getRequest(genre);
        String answer = output.toString();
        Matcher parseDoubleNumber = Pattern.compile("(?<=оценка: )(\\d.)+").matcher(answer);
        double actual = 0;
        if (parseDoubleNumber.find()) {
            actual = Double.parseDouble(parseDoubleNumber.group());
        }
        return actual;
    }

    @Test
    public void testGetRequestWithRightSecond() throws Exception {
        Assert.assertEquals(6.3, testGetRequestWithRight("Drama"), 0.1);
    }

    @Test
    public void testConnectedBin() throws Exception {
        String answer = Client.connected("http://httpbin.org/get");
        Assert.assertTrue(answer.contains("\"url\": \"http://httpbin.org/get\""));
        Assert.assertTrue(answer.contains("\"headers\""));
        Assert.assertTrue(answer.contains("\"Host\": \"httpbin.org\", "));

    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }


}
