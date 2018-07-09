package com.example.sebastian.vocbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class CsvReader {
    public static ArrayList<VocCaseModel.VocCardModel> readCards(InputStream inputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")))) {
            ArrayList<VocCaseModel.VocCardModel> result = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] pair = line.split(";");
                if (pair.length == 2)
                    result.add(new VocCaseModel.VocCardModel(pair[0], pair[1]));
            }

            return result;
        }
    }
}
