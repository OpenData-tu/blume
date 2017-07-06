package edu.tu_berlin.ise.opendata.blume;

import edu.tu_berlin.ise.opendata.blume.model.DailyMeasurements;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.LocalDate;
import java.util.concurrent.Future;

/**
 * Created by aardila on 2017-07/03.
 */
public class DailyImporter {

    private static final String IMPORTER_ID = "BLUME";
    private static final String KAFKA_SERVER_ENV_VAR_KEY = "KAFKA_HOST";

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            throw new Exception("Mandatory import date argument missing!");
        }

        final String kafkaServer = System.getenv(KAFKA_SERVER_ENV_VAR_KEY);

        if (kafkaServer == null || kafkaServer.isEmpty()) {
            throw new Exception(String.format("Env var %s was not present", KAFKA_SERVER_ENV_VAR_KEY));
        }

//        System.out.println("Waiting for Kafka to spin up. Sleeping for 20 s...");
//        Thread.sleep(20000);

        String dateArg = args[0];
        LocalDate dateToImport = LocalDate.parse(dateArg);
        System.out.println("Downloading measurements... ");
        DailyMeasurements dailyMeasurements = BlumeWrapper.getDailyMeasurements(dateToImport);
        System.out.println("Done!");

        System.out.println("Serializing... ");
        String jsonString = Serializer.serializeDailyMeasurements(dailyMeasurements);
        System.out.println("Done!");


        System.out.println("Sending json to Kafka queue on " + kafkaServer);
        KafkaQueue kafkaQueue = new KafkaQueue(IMPORTER_ID, kafkaServer);
        Future<RecordMetadata> future = kafkaQueue.publish(jsonString);
        System.out.println("Done!");

        System.out.println("Waiting for Kafka to acknowledge...");
        RecordMetadata rm = future.get();
        System.out.println(String.format("Topic: %s. Offset: %s. Partition: %s", rm.topic(), rm.offset(), rm.partition()));

        System.out.println("Done importing! Tschüss!");
    }
}
