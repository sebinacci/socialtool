package com.bepolite.tool;

import static ch.lambdaj.Lambda.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import ch.lambdaj.function.matcher.Predicate;

import com.bepolite.model.Record;
import com.bepolite.model.Result;
import com.bepolite.model.ResultType;
import com.bepolite.utils.EmailUtil;
import com.bepolite.utils.HttpUtil;
import com.bepolite.utils.PropertyUtil;
import com.bepolite.utils.VelocityTemplateUtil;

public class App {


    public static void main(String[] args) throws IOException, InterruptedException {
        DateTime lastCheckTime = DateTime.now().minusDays(2);
        while (true) {
            ArrayList<Result> resultList = new ArrayList<>();

            try {
				HashMap<?, ?> pollingUrls = PropertyUtil.getPollingUrls();
                for (Object key : pollingUrls.keySet()) {
                    ResultType resultTypeKey = (ResultType) key;
                    Result result = getLatestRecords((String) pollingUrls.get(resultTypeKey), resultTypeKey,lastCheckTime);
                    if (result != null)
                        resultList.add(result);
                }
                lastCheckTime = getLatestCheckTime(resultList,lastCheckTime);
                sendEmail(resultList);
                System.out.println("Time now " + new DateTime() + " sleeping now");
                Thread.sleep(PropertyUtil.getPollIntervalInMillis());
            } catch (Exception e) {
                System.out.println("An error has occurred - retrying " + e.getMessage());
            }
        }
    }

    private static DateTime getLatestCheckTime(List<Result> resultList, DateTime lastCheckTime) {
        DateTime latestTime=lastCheckTime;
        for(Result result : resultList){
            DateTime latestRecordTime = result.getRecords().get(0).getTime();
            if(latestRecordTime.isAfter(latestTime))
               latestTime= latestRecordTime;
        }
       return latestTime;
    }

    private static void sendEmail(ArrayList<Result> results) {
        if (results.isEmpty())
            return;
        String emailBody = VelocityTemplateUtil.constructEmailBody(results);
        if (!emailBody.equals("")) {
            System.out.println("Sending Email\n");
            EmailUtil.sendEmail("Some Updates...", emailBody);
        }
    }

    private static Result getLatestRecords(String url, ResultType resultType, final DateTime lastCheckTime) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Result result = objectMapper.readValue(HttpUtil.getLatestUpdate(url), Result.class);
        Predicate<?> predicate = new Predicate<Object>() {
            @Override
            public boolean apply(Object o) {
                Record record = (Record) o;
                return record.getTime().isAfter(lastCheckTime);
            }
        };

        List<Record> latestRecords = filter(predicate, result.getRecords());
        return latestRecords.isEmpty() ? null : new Result(latestRecords, resultType);
    }


}