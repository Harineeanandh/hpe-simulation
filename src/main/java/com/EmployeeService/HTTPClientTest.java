package com.EmployeeService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPClientTest {
    public static void main(String[] args) throws Exception {
        // The URL of your POST endpoint
        URL url = new URL("http://localhost:8080/employees");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // The JSON data you want to send in the request body
        String jsonInputString = "{"
                + "\"employeeId\": \"4\","
                + "\"firstName\": \"Sam\","
                + "\"lastName\": \"Wilson\","
                + "\"email\": \"sam.wilson@example.com\","
                + "\"title\": \"Developer\""
                + "}";

        // Send the request data
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
            os.flush();  // Ensure the data is fully sent
        }

        // Get the response code (success code: 200)
        int responseCode = connection.getResponseCode();
        System.out.println("POST Response Code: " + responseCode);

        // Read the response from the server (optional)
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            System.out.println("Response: " + response.toString());  // Check what the server responds with
        }
    }
}
