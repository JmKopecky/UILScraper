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
            System.out.println("Input the competition level: (state, region, or district)");
            String confTest = scanInput.nextLine().trim();
            if (confTest.equals("state") || confTest.equals("region") || confTest.equals("district")) {
                level = confTest;
                break;
            } else {
                System.out.println("Incorrectly formatted. Please input a string matching your target exactly");
            }
        }

        //check if a specific district is required
        System.out.println();
        int specifiedDistrict = -1;
        if (level.equals("district")) {
            while (true) {
                System.out.println("Do you want to see results for a specific district? (y/n)");
                String response = scanInput.nextLine().trim();
                if (response.equals("y")) {
                    while (true) {
                        System.out.println("Specify a district number: (1-32)");
                        try {
                            int dist = Integer.parseInt(scanInput.nextLine());
                            if (dist >= 1 && dist <= 32) {
                                specifiedDistrict = dist;
                                break;
                            }
                            System.out.println("Invalid input: format should be (1-32)");
                        } catch (Exception _) {
                            System.out.println("Invalid input: format should be (1-32)");
                        }
                    }
                    break;
                }
                if (response.equals("n")) {
                    break;
                }
                System.out.println("Invalid input... please respond with y or n");
            }
        }


        //check if a specific region is desired
        System.out.println();
        int specifiedRegion = -1;
        if (level.equals("region")) {
            while (true) {
                System.out.println("Do you want to see results for a specific region? (y/n)");
                String response = scanInput.nextLine().trim();
                if (response.equals("y")) {
                    while (true) {
                        System.out.println("Specify a region number: (1-4)");
                        try {
                            int reg = Integer.parseInt(scanInput.nextLine());
                            if (reg >= 1 && reg <= 4) {
                                specifiedRegion = reg;
                                break;
                            }
                            System.out.println("Invalid input: format should be (1-4)");
                        } catch (Exception _) {
                            System.out.println("Invalid input: format should be (1-4)");
                        }
                    }
                    break;
                }
                if (response.equals("n")) {
                    break;
                }
                System.out.println("Invalid input... please respond with y or n");
            }
        }


        //see if a specific region of districts is desired
        System.out.println();
        int distsInRegion = -1;
        if (level.equals("district") && specifiedDistrict == -1) {
            while (true) {
                System.out.println("Do you want to see results for districts in a specific region? (y/n)");
                String response = scanInput.nextLine().trim();
                if (response.equals("y")) {
                    while (true) {
                        System.out.println("Specify a region number: (1-4)");
                        try {
                            int reg = Integer.parseInt(scanInput.nextLine());
                            if (reg >= 1 && reg <= 4) {
                                distsInRegion = reg;
                                break;
                            }
                            System.out.println("Invalid input: format should be (1-4)");
                        } catch (Exception _) {
                            System.out.println("Invalid input: format should be (1-4)");
                        }
                    }
                    break;
                }
                if (response.equals("n")) {
                    break;
                }
                System.out.println("Invalid input... please respond with y or n");
            }
        }




        //base for reference: "https://postings.speechwire.com/r-uil-academics.php?groupingid=ID_COMP&Submit=View+postings&AREA_TYPE=ID_AREA&conference=ID_CONF&seasonid=ID_SEASON";
        String newURL = urlString.replace("AREA_TYPE", level);
        newURL = newURL.replace("ID_COMP", competition);
        newURL = newURL.replace("ID_CONF", conference);
        newURL = newURL.replace("ID_SEASON", seasonID);

        //now we have a url that only needs modification to specify ID_AREA


        System.out.println("--- Setup Succeeded ---\n\n\n");


        System.out.println("Retrieving Data...");

        ArrayList<IndividualResult> individualResults = new ArrayList<>();
        ArrayList<TeamResult> teamResults = new ArrayList<>();

        if (conference.equals("-1")) {
            for (int i = 1; i <= 6; i++) {
                newURL = newURL.replace("=-1", "=" + i);
                HashMap<Integer, ArrayList<Object>> data = doSpawnDataRetrievers(newURL, competition, level, distsInRegion, specifiedRegion, specifiedDistrict);
                for (Map.Entry<Integer, ArrayList<Object>> entry : data.entrySet()) {
                    for (IndividualResult result : (ArrayList<IndividualResult>) entry.getValue().get(0)) {
                        result.setDistrict(String.valueOf(entry.getKey()));
                        result.setConference(String.valueOf(i));
                        individualResults.add(result);
                    }
                    for (TeamResult result : (ArrayList<TeamResult>) entry.getValue().get(1)) {
                        result.setDistrict(String.valueOf(entry.getKey()));
                        result.setConference(String.valueOf(i));
                        teamResults.add(result);
                    }
                }
            }
        } else {
            HashMap<Integer, ArrayList<Object>> data = doSpawnDataRetrievers(newURL, competition, level, distsInRegion, specifiedRegion, specifiedDistrict);
            for (Map.Entry<Integer, ArrayList<Object>> entry : data.entrySet()) {
                for (IndividualResult result : (ArrayList<IndividualResult>) entry.getValue().get(0)) {
                    result.setDistrict(String.valueOf(entry.getKey()));
                    result.setConference(conference);
                    individualResults.add(result);
                }
                for (TeamResult result : (ArrayList<TeamResult>) entry.getValue().get(1)) {
                    result.setDistrict(String.valueOf(entry.getKey()));
                    result.setConference(conference);
                    teamResults.add(result);
                }
            }
        }


        //output data
        System.out.println("\n\n ---- Output ---- ");

        System.out.println("\n\nIndividual Results:");
        Collections.sort(individualResults);
        int counter = 1;
        for (IndividualResult result : individualResults) {
            if (conference.equals("-1")) {
                System.out.printf("\t%-5d - %5.0f: {d%-2s} %s (%sA) %s\n", counter, result.score, result.district, result.name, result.conference, result.school);
            } else {
                System.out.printf("\t%-5d - %5.0f: {d%-2s} %s (%s)\n", counter, result.score, result.district, result.name, result.school);
            }
            counter++;
        }

        System.out.println("\n\nTeam Results:");
        Collections.sort(teamResults);
        counter = 1;
        for (TeamResult result : teamResults) {
            if (conference.equals("-1")) {
                System.out.printf("\t%-5d - %5.0f: {d%-2s} %s (%sA)\n", counter, result.score, result.district, result.school, result.conference);
            } else {
                System.out.printf("\t%-5d - %5.0f: {d%-2s} %s\n", counter, result.score, result.district, result.school);
            }
            counter++;
        }
    }


    public static HashMap<Integer, ArrayList<Object>> doSpawnDataRetrievers(String url, String competition, String level, int distsInRegion, int targetRegion, int targetDist) {
        //if state, max ID_AREA is 1. If region, 4. If district, 32

        HashMap<Integer, ArrayList<Object>> data = null;

        switch (level) {
            case "state" -> {
                data = retrieveData(url, 1, 1, competition);
            }

            case "region" -> {
                if (targetRegion != -1) {
                    data = retrieveData(url, targetRegion, targetRegion, competition);
                } else {
                    data = retrieveData(url, 1, 4, competition);
                }
            }

            case "district" -> {
                if (targetDist != -1) {
                    data = retrieveData(url, targetDist, targetDist, competition);
                } else {
                    if (distsInRegion != -1) {
                        data = retrieveData(url, 1 + (8 * (distsInRegion - 1)), 8 * (distsInRegion), competition);
                    } else {
                        data = retrieveData(url, 1, 32, competition);
                    }
                }
            }
        }

        return data;
    }


    //start of util methods

    public static HashMap<Integer, ArrayList<Object>> retrieveData(String newURL, int start, int end, String comp) {
        HashMap<Integer, ArrayList<Object>> data = new HashMap<>();

        for (int i = start; i <= end; i++) {
            String urlToUse = newURL.replace("ID_AREA", "" + i);
            Document doc;
            try {
                doc = connectAndRetrieveResponse(urlToUse);
            } catch (Exception e) {
                System.out.println("Encountered error reading url: " + urlToUse + " --- " + e.getMessage());
                continue;
            }

            data.put(i, parseData(doc, comp));
        }

        return data;
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


    public static ArrayList<Object> parseData(Document doc, String comp) {

        ArrayList<IndividualResult> individualResults = new ArrayList<>();
        ArrayList<TeamResult> teamResults = new ArrayList<>();


        doc.getElementsByTag("tbody").forEach((table) -> {

            try {
                Element headerRow = table.getElementsByTag("tr").first();
                //if first column header is place, this is a table we care about.
                if (headerRow.getElementsByTag("td").first().text().equals("Place")) {
                    //if fourth column has heading "Code", then it is individual. Else, team.
                    if (headerRow.getElementsByTag("td").get(3).text().equals("Code")) {



                        //in order to determine which column has score data, count until we reach either "Written" or "Total Score"
                        int column = 0;
                        for (Element colHeader : headerRow) {
                            if (colHeader.text().equals("Written") || colHeader.text().equals("Total")) {
                                column--;
                                break;
                            }
                            column++;
                        }
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
                            individualResults.add(new IndividualResult(name, school, code, score, place));
                        }
                    } else {



                        int column = 0;
                        for (Element colHeader : headerRow) {
                            if (colHeader.text().equals("Total")) {
                                column--;
                                break;
                            }
                            column++;
                        }
                        //now that we know which column to get, go through each row and build the data necessary for an IndividualResult object
                        Iterator<Element> rowIterator = table.getElementsByTag("tr").iterator();
                        rowIterator.next();
                        while (rowIterator.hasNext()) {
                            Element row = rowIterator.next();
                            String place = row.getElementsByTag("td").get(0).text();
                            String schoolText = row.getElementsByTag("td").get(1).html();
                            double score = Double.parseDouble(row.getElementsByTag("td").get(column).text());
                            ArrayList<String> parsedSchoolContent = new ArrayList<>();
                            for (String val : schoolText.split("<br>")) {
                                //parse this html to remove extra stuff
                                while (val.contains("<") && val.contains(">")) {
                                    val = val.substring(0,val.indexOf("<")) + val.substring(val.indexOf(">") + 1);
                                }
                                val = val.trim();
                                parsedSchoolContent.add(val);
                            }
                            String school = parsedSchoolContent.removeFirst();
                            teamResults.add(new TeamResult(school, parsedSchoolContent, score, place));
                        }
                    }
                }
            } catch (NullPointerException _) {

            }
        });


        Collections.sort(individualResults);
        Collections.sort(teamResults);

        ArrayList<Object> output = new ArrayList<>();
        output.add(individualResults);
        output.add(teamResults);
        return output;
    }
}



class IndividualResult implements Comparable<IndividualResult> {
    String name;
    String school;
    String code;
    double score;
    String place;
    String district = "unset";
    String conference = "unset";

    public IndividualResult(String name, String school, String code, double score, String place) {
        this.name = name;
        this.school = school;
        this.code = code;
        this.score = score;
        this.place = place;
    }


    public void setDistrict(String dist) {
        this.district = dist;
    }

    public void setConference(String conf) {
        this.conference = conf;
    }


    @Override
    public int compareTo(IndividualResult o) {
        return -1 * Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return String.format("%4.0f: %s (%s)", score, name, school);
    }
}



class TeamResult implements Comparable<TeamResult> {
    String school;
    ArrayList<String> students;
    double score;
    String place;
    String district = "unset";
    String conference = "unset";

    public TeamResult(String school, ArrayList<String> students, double score, String place) {
        this.school = school;
        this.students = students;
        this.score = score;
        this.place = place;
    }


    public void setDistrict(String dist) {
        this.district = dist;
    }

    public void setConference(String conf) {
        this.conference = conf;
    }


    @Override
    public int compareTo(TeamResult o) {
        return -1 * Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return String.format("%4.0f: %s", score, school);
    }
}