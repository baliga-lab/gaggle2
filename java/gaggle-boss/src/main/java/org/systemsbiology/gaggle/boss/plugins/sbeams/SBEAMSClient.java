/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss.plugins.sbeams;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class SBEAMSClient {

    private String cookie = null;
    private String userName; 
    private String password;
    private boolean useGui = false;
    private int passwordAttempts = 3;
    private static String COOKIE_URL = "https://db.systemsbiology.net/sbeams/cgi/main.cgi";

    private static class Response {
        private String content;
        private String cookie;

        public Response(String content, String cookie) {
            this.content = content;
            this.cookie = cookie;
        }
        public String getCookie() { return cookie; }
        public String getContent() { return content; }
    }

    public SBEAMSClient() { }
    public SBEAMSClient(boolean useGui) { this.useGui = useGui; }
    public SBEAMSClient(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getCookie() { return cookie; }
    public void setCookie(String cookie) { this.cookie = cookie; }

    public boolean goodCookie() {
        if (cookie == null) return false;
        // Hash for translating month to number
        Map<String, Integer> monthHash = new HashMap<String, Integer>();
        monthHash.put("Jan", Integer.valueOf(1));
        monthHash.put("Feb", Integer.valueOf(2));
        monthHash.put("Mar", Integer.valueOf(3));
        monthHash.put("Apr", Integer.valueOf(4));
        monthHash.put("May", Integer.valueOf(5));
        monthHash.put("Jun", Integer.valueOf(6));
        monthHash.put("Jly", Integer.valueOf(7));
        monthHash.put("Aug", Integer.valueOf(8));
        monthHash.put("Sep", Integer.valueOf(9));
        monthHash.put("Oct", Integer.valueOf(10));
        monthHash.put("Nov", Integer.valueOf(11));
        monthHash.put("Dec", Integer.valueOf(12));

        // Extract cookie expiration information
        int cookieYear = 0;
        int cookieMonth = 0;
        int cookieDate = 0;
        int cookieHour = 0;
        int cookieMinute = 0;
        int cookieSecond = 0;
        Pattern expiration = Pattern.compile("expires\\=(\\w+)\\,\\s?(\\d+)\\-(\\w+)\\-(\\d+)\\s+(\\d+)\\:(\\d+)\\:(\\d+)\\;?");
        Matcher match = expiration.matcher(cookie);
        if (match.matches()) {
            cookieDate = (new Integer(match.group(2))).intValue();
            cookieMonth = ((Integer)monthHash.get(match.group(3))).intValue();
            cookieYear = (new Integer(match.group(4))).intValue();
            cookieHour = (new Integer(match.group(5))).intValue();
            cookieMinute = (new Integer(match.group(6))).intValue();
            cookieSecond = (new Integer(match.group(7))).intValue();
        } else return false;
        // Get current timestamp and compare it to cookie expiration information
        Calendar calendar = new GregorianCalendar();
        Date trialTime = new Date();
        calendar.setTime(trialTime);

        if (calendar.get(Calendar.YEAR) < cookieYear) return true;
        else if (calendar.get(Calendar.YEAR) > cookieYear) return false;

        if ((calendar.get(Calendar.MONTH)+1) < cookieMonth) return true;
        else if ((calendar.get(Calendar.MONTH)+1) > cookieMonth) return false;

        if (calendar.get(Calendar.DATE) < cookieDate) return true;
        else if (calendar.get(Calendar.DATE) > cookieDate) return false;

        if (calendar.get(Calendar.HOUR_OF_DAY) < cookieHour) return true;
        else if (calendar.get(Calendar.HOUR_OF_DAY) > cookieHour) return false;

        if (calendar.get(Calendar.MINUTE) < cookieMinute) return true;
        else if (calendar.get(Calendar.MINUTE) > cookieMinute) return false;

        if (calendar.get(Calendar.SECOND) < cookieSecond) return true;
        else if (calendar.get(Calendar.SECOND) > cookieSecond) return false;

        return false;
    }

    private Response postRequest(String urlString, String params) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
        uc.setDoInput(true);
        uc.setDoOutput(true);
        uc.setUseCaches(false);
        uc.setAllowUserInteraction(false);
        uc.setRequestMethod("POST");
        uc.setRequestProperty("ContentType", "application/x-www-form-urlencoded");
        uc.setRequestProperty("User-Agent", "CytoLinkFromMJ");

        if (cookie != null)
            uc.setRequestProperty("Cookie", cookie);

        PrintStream out = new PrintStream(uc.getOutputStream());
        out.print(params);
        out.flush();
        out.close();
        uc.connect();
        StringBuilder sb = new StringBuilder();
        String inputLine;
        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine + "\n");
        }
        in.close();

        return new Response(sb.toString(), uc.getHeaderField("Set-Cookie"));
    }

    public String fetchSbeamsPage(String urlString, String params) throws Exception {
        if (cookie == null) fetchCookie();
        String paramsInUrl = "";
        String unparameterizedUrl = urlString;
        Pattern potentialParams = Pattern.compile("(.*)\\?(.*)");
        Matcher match = potentialParams.matcher(urlString);
        if (match.matches()) {
            unparameterizedUrl = match.group(1);
            paramsInUrl = match.group(2);
            if (params == null) params = paramsInUrl;
            else params += paramsInUrl;
        }

        return postRequest(unparameterizedUrl, params).getContent();
    }

    public String fetchSbeamsPage(String url) throws Exception {
        return fetchSbeamsPage(url, "");
    }

    private boolean promptForUsernamePassword() {
        return promptForUsernamePassword(useGui);
    }

    // Not yet implemented in a good way (does not hide password on terminal)
    private boolean promptForUsernamePassword( boolean useGui) {
        boolean success = true;
        if (useGui == true) {
            PasswordDialog prompt = new PasswordDialog ("Cytoscape SBEAMS Login");

            if (userName != null ) {
                prompt.setName(userName);
                prompt.setPasswordFocus();
            }

            if (prompt.showDialog()) {
                userName = prompt.getName();
                password = prompt.getPass();
            } else {
                System.err.println("User selected cancel");
                success = false;
            }
            prompt.dispose();
        } else {
            System.out.print("Enter SBEAMS Username: ");
            userName = readFromStdin();
            if (userName == null) success = false;
            System.out.print("Enter Password: ");
            password = readFromStdin();
            if (password == null) success = false;
            System.out.println("Thanks for the Username/Password Information, " + userName + " !");
        }
        return success;
    }

    private String readFromStdin() {
        BufferedReader stdin = null;
        try {
            String line;
            stdin = new BufferedReader(new InputStreamReader(System.in));
            while ((line = stdin.readLine()) == null) { }
            return line;
        } catch(Exception e) { 
            return null;
        } finally {
            if (stdin != null) try { stdin.close(); } catch (Exception ignore) { }
        }
    }

    private void fetchCookie() throws Exception{
        while (passwordAttempts > 0) {
            if (userName == null || password== null)
                promptForUsernamePassword();

            StringBuilder params = new StringBuilder();
            //setting the trusted connection			
            params.append("username");
            params.append("=");
            params.append(URLEncoder.encode(userName, "UTF8"));
            params.append("&");
            params.append("password");
            params.append("=");
            params.append(URLEncoder.encode(password, "UTF8"));
            params.append("&");
            params.append("login");
            params.append("=");
            params.append(URLEncoder.encode(" Login ", "UTF8"));
            Response res = postRequest(COOKIE_URL, params.toString());
            this.cookie = res.getCookie();
            if (res.getCookie() == null) {
                password = null;
                passwordAttempts--;
            } else {
                break;
            }
        }
    }

    public static void main (String [] args) {
        System.out.println ("test SBEAMS Table Retrieval");
        try {
            SBEAMSClient client = new SBEAMSClient(true);
            System.out.println(client.fetchSbeamsPage("http://db/sbeams/cgi/Microarray/ViewFile.cgi?action=read&FILE_NAME=matrix_output&project_id=328&SUBDIR=20050104_154519"));
        } catch (IOException e) {
            System.err.println("Page Not Found");
        } catch (Exception t) {
            t.printStackTrace();
        }
        System.exit(0);
    }
}
