import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.*;
import java.util.*;
import java.net.*;

public class ScrapeUILResults {

    public static final String urlString = "https://postings.speechwire.com/r-uil-academics.php?groupingid=ID_COMP&Submit=View+postings&AREA_TYPE=ID_AREA&conference=ID_CONF&seasonid=ID_SEASON";

    public static void main(String[] args) throws IOException {

        //request basic config options from the user.

        Scanner scanInput = new Scanner(System.in);
        System.out.println("--- Setting Up ---\n");

        //season id
        boolean provideId = false;
        String seasonID = "";
        while (true) {
            System.out.println("Do you have a season id? (y/n)");
            String response = scanInput.nextLine().trim();
            if (response.equals("y")) {
                provideId = true;
                break;
            }
            if (response.equals("n")) {
                provideId = false;
                break;
            }
            System.out.println("Invalid input... please respond with y or n");
        }
        if (provideId) {
            while (true) {
                System.out.println("Provide season id: ");
                String id = scanInput.nextLine().trim();
                System.out.println("Testing...");
                if (testId(id)) {
                    System.out.println("Success!");
                    seasonID = id;
                    break;
                }
                System.out.println("Failed. Ensure that your ID is valid or re-run the program to input a url.");
            }
        } else {
            while (true) {
                System.out.println("Provide a link from the Speechwire website from the season in question.");
                String link = scanInput.nextLine().trim();
                System.out.println("Testing...");
                if (!link.contains("seasonid=")) {
                    System.out.println("Invalid link. Ensure that it has the property 'sessionid=' included.");
                    continue;
                }
                String id = link.substring(link.indexOf("seasonid=") + 9);
                try {
                    Integer.parseInt(id);
                } catch (NumberFormatException _) {
                    System.out.println("Failed to parse session id");
                    continue;
                }
                if (testId(id)) {
                    System.out.println("Success!");
                    seasonID = id;
                    break;
                }
            }
        }

        //conference
        System.out.println();
        String conference = "";
        while (true) {
            System.out.println("Which conference is the target? (1-6, or -1 if all)");
            String confTest = scanInput.nextLine().trim();
            try {
                int val = Integer.parseInt(confTest);
                if (val >= 1 && val <= 6 || val == -1) {
                    System.out.println("Succeeded.");
                    conference = "" + val;
                    break;
                } else {
                    throw new NumberFormatException("" + val);
                }
            } catch (NumberFormatException _) {
                System.out.println("Incorrectly formatted. Please input an integer between 1 and 6, or input -1");
            }
        }

        //competition
        System.out.println();
        String competition = "";
        boolean continueCompLoop = true;
        while (continueCompLoop) {
            System.out.println("Input the number corresponding to the target competition:" +
                    "\n\t1: Accounting" +
                    "\n\t2: Computer Science" +
                    "\n\t3: Mathematics" +
                    "\n\t4: Number Sense" +
                    "\n\t5: Calculator Applications");
            String confTest = scanInput.nextLine().trim();
            try {
                int val = Integer.parseInt(confTest);
                switch (val) {
                    case 1 -> {competition = "1"; continueCompLoop = false;} //accounting
                    case 2 -> {competition = "9"; continueCompLoop = false;} //computer science
                    case 3 -> {competition = "10"; continueCompLoop = false;} //mathematics
                    case 4 -> {competition = "11"; continueCompLoop = false;} //number sense
                    case 5 -> {competition = "8"; continueCompLoop = false;} //calculator applications
                    default -> throw new NumberFormatException(confTest);
                }
            } catch (NumberFormatException _) {
                System.out.println("Incorrectly formatted. Please input an integer corresponding to a competition.");
            }
        }

        //which level?
        System.out.println();
        String level = "";
        while (true) {
            System.out.println("Input either state, region, or district");
            String confTest = scanInput.nextLine().trim();
            if (confTest.equals("state") || confTest.equals("region") || confTest.equals("district")) {
                level = confTest;
                break;
            } else {
                System.out.println("Incorrectly formatted. Please input a string matching your target exactly");
            }
        }

        //base for reference: "https://postings.speechwire.com/r-uil-academics.php?groupingid=ID_COMP&Submit=View+postings&AREA_TYPE=ID_AREA&conference=ID_CONF&seasonid=ID_SEASON";
        String newURL = urlString.replace("AREA_TYPE", level);
        newURL = newURL.replace("ID_COMP", competition);
        newURL = newURL.replace("ID_CONF", conference);
        newURL = newURL.replace("ID_SEASON", seasonID);

        //now we have a url that only needs modification to specify ID_AREA


        System.out.println("--- Setup Succeeded ---\n\n\n");


        if (conference.equals("-1")) {
            for (int i = 1; i <= 6; i++) {
                //if state, max ID_AREA is 1. If region, 4. If district, 32
                newURL = newURL.replace("=-1", "=" + i);
                switch (level) {
                    case "state" -> {
                        retrieveData(newURL, 1, competition);
                    }

                    case "region" -> {
                        retrieveData(newURL, 4, competition);
                    }

                    case "district" -> {
                        retrieveData(newURL, 32, competition);
                    }
                }
            }
        } else {
            switch (level) {
                case "state" -> {
                    retrieveData(newURL, 1, competition);
                }

                case "region" -> {
                    retrieveData(newURL, 4, competition);
                }

                case "district" -> {
                    retrieveData(newURL, 32, competition);
                }
            }
        }


    }


    //start of util methods

    public static void retrieveData(String newURL, int iterations, String comp) {
        for (int i = 1; i <= iterations; i++) {
            String urlToUse = newURL.replace("ID_AREA", "" + i);
            Document doc;
            try {
                doc = connectAndRetrieveResponse(urlToUse);
            } catch (Exception e) {
                System.out.println("Encountered error reading url: " + urlToUse + " --- " + e.getMessage());
                continue;
            }

            //System.out.println(doc.html());
            parseData(doc, comp);

        }
    }


    public static boolean testId(String id) {
        String urlStr = "https://postings.speechwire.com/r-uil-academics.php?seasonid=" + id;
        try {
            URL url = new URL(urlStr);
            int responseCode = ((HttpURLConnection) url.openConnection()).getResponseCode();
            if (responseCode == 200) {
                return true;
            } else {
                System.out.println("Received response " + responseCode + " while verifying session id " + id);
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }


    public static Document connectAndRetrieveResponse(String urlStr) throws Exception {
        System.out.println(urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection.getResponseCode() != 200) {
            throw new IOException("Response code to url was not 200. Instead: " + connection.getResponseCode());
        }

        String html = "";
        InputStream input = connection.getInputStream();
        int c;
        while (((c = input.read()) != -1)) {
            html += (char) c;
        }
        input.close();
        Document document = Jsoup.parse(html);
        return document;
    }


    public static void parseData(Document doc, String comp) {
        doc.getElementsByTag("tbody").forEach((table) -> {

            try {
                Element headerRow = table.getElementsByTag("tr").first();
                //if first column header is place, this is a table we care about.
                if (headerRow.getElementsByTag("td").first().text().equals("Place")) {
                    //if fourth column has heading "Code", then it is individual. Else, team.

                    if (headerRow.getElementsByTag("td").get(3).text().equals("Code")) {

                        System.out.println("\n\n\n\n\n\n\n\n\n --- Individual Results ---");

                        //in order to determine which column has score data, count until we reach either "Written" or "Total Score"
                        int column = 0;
                        for (Element colHeader : headerRow) {
                            if (colHeader.text().equals("Written") || colHeader.text().equals("Total")) {
                                break;
                            }
                            column++;
                        }
                        ArrayList<IndividualResult> results = new ArrayList<>();
                        //now that we know which column to get, go through each row and build the data necessary for an IndividualResult object
                        Iterator<Element> rowIterator = table.getElementsByTag("tr").iterator();
                        rowIterator.next();
                        while (rowIterator.hasNext()) {
                            Element row = rowIterator.next();
                            String place = row.getElementsByTag("td").get(0).text();
                            String school = row.getElementsByTag("td").get(1).text();
                            String name = row.getElementsByTag("td").get(2).text();
                            String code = row.getElementsByTag("td").get(3).text();
                            double score = Double.parseDouble(row.getElementsByTag("td").get(column).text());
                            results.add(new IndividualResult(name, school, code, score, place));
                        }
                        Collections.sort(results);
                        results.forEach(System.out::println);
                    } else {

                        System.out.println("\n\n\n\n\n\n\n\n\n --- Team Results ---");

                    }
                }
            } catch (NullPointerException _) {

            }
        });
    }
}



class IndividualResult implements Comparable<IndividualResult> {
    String name;
    String school;
    String code;
    double score;
    String place;

    public IndividualResult(String name, String school, String code, double score, String place) {
        this.name = name;
        this.school = school;
        this.code = code;
        this.score = score;
        this.place = place;
    }

    @Override
    public int compareTo(IndividualResult o) {
        return -1 * Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return String.format("%-5s - %4.0f: %s (%s)", place, score, name, school);
    }
}