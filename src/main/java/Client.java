import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    private static final int CONNECTION_TIMEOUT = 10000;
    private static final String site = "https://easy.test-assignment-a.loyaltyplant.net/";
    private static final String key = "72b56103e43843412a992a8d64bf96e9";
    private static final String endPointGenres = "3/genre/movie/list?api_key=$";
    private static final String endPointFilms = "3/discover/movie?api_key=$";
    private static boolean info = false;
    private static boolean stop = false;
    private static final ArrayList<String> getList = new ArrayList<>();
    private static String forWho;

    public static void main(final String[] args){
        System.out.println("Привет, я твой помощник! Запусти меня, указав жанр фильма.\n" +
                "Если хочешь получать информацию о процессе выполнения - напиши \"info\" и жанр\n" +
                "Хочешь остановить поиск? Напиши \"stop\" и жанр\n");
        Scanner scanner = new Scanner(System.in);
        while (true)  {
            String message = scanner.nextLine();
            Matcher stopMatch = Pattern.compile("(?<=stop )[a-zA-Z]+").matcher(message);
            Matcher infoMatch = Pattern.compile("(?<=info )[a-zA-Z]+").matcher(message);
            if (infoMatch.find()) {
                info = true;
                forWho = infoMatch.group();
            }
            else if (stopMatch.find()) {
                stop = true;
                forWho = stopMatch.group();
            }
            else if (!getList.contains(message)) {
                getList.add(message);
                Thread thread = new Thread(() -> {
                    try {
                        getRequest(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            } else System.out.println("Такой запрос уже есть");
        }
    }

    static void getRequest(String genre) throws Exception{
        String allGenres = connected(site + endPointGenres + key);
        Matcher genresMatch = Pattern.compile("[0-9]+(?=,\"name\":\"" + genre + "\")").matcher(allGenres);
        if (genresMatch.find()) {
            String id = genresMatch.group();//если жанр найден
            checkAllFilms(genre, id);
        }
        else System.out.println("Такого жанра нет. Повторите попытку");
    }

    private static void checkAllFilms(String genre, String id) throws IOException {
        int pageNumber = 0;
        int lastPage = 1;
        double sum=0;
        int count=0;
        do{
            if ((stop || info) && genre.equals(forWho)) { //обработка команд stop и info
                forWho = "";
                System.out.println("Для жанра " + genre + " было собрано " + (100.0 * pageNumber / lastPage) + "% "
                        + "оценка: " + sum / count);
                info = false;
                if (stop) {
                    getList.remove(genre);
                    stop = false;
                    return;
                }
            }
            pageNumber++;
            String allFilms = connected(site + endPointFilms + key + "&page=" + pageNumber); //поиск кино на странице
            Matcher average = Pattern.compile("(?<=\"vote_average\":)(\\d.\\d)").matcher(allFilms); //и их обработка
            Matcher genres = Pattern.compile("(?<=\"genre_ids\":\\[)[\\d,]*").matcher(allFilms);
            while (average.find() && genres.find()){
                if (genres.group().contains(id)) {
                    sum += Double.parseDouble(average.group());
                    count++;
                }
            }
            if (lastPage==1) { //определение последней страницы
                lastPage = lastPageFinder(allFilms);
            }
        } while (pageNumber!=lastPage);
        System.out.println("Для жанра " + genre + " сбор завершен: " + (100.0 * pageNumber / lastPage) + "% "
                + "оценка: " + sum / count);
    }

    private static int lastPageFinder(String allFilms){
        Matcher total = Pattern.compile("(?<=\"total_pages\":)(\\d+)").matcher(allFilms);
        if (total.find()) return Integer.parseInt(total.group()) - 1;
        return 0;
    }

    static String connected(String str) throws IOException {
        final URL url = new URL(str);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(CONNECTION_TIMEOUT);
        final String content = readInputStream(connection);
        connection.disconnect();
        return content;
    }

    private static String readInputStream(final HttpURLConnection con){
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            final StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        } catch (final Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }


}